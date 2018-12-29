package com.reactlibrary;

// android util imports
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.os.Environment;

// java util imports
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// opencv imports
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.reactlibrary.SampleModel;

public class Util {

        private static HashMap<Integer, SampleModel> samples;
        private static Rect[] rects;
        private static int NB_BLOBS = 7;
        private static Bitmap changedBitmap;


        // public static boolean findBlobs(String imageAsBase64) {
        public static String findBlobs(String imageAsBase64) {

            // Converts Base64 image into Bitmap format from the demo app
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = true;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            byte[] decodedString = Base64.decode(imageAsBase64, Base64.DEFAULT);
            changedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                // samples basically is list of blobs found
                samples = new HashMap<>();

                rects = new Rect[NB_BLOBS];
                int rectIdx = 0;

                Mat raw = new Mat();
                Mat gray = new Mat();
                Mat th = new Mat();

                // converts fetched 'changedBitmap' image to Mat format and stores in 'raw'
                // So, raw is basically the Mat format of the image clicked
                org.opencv.android.Utils.bitmapToMat(changedBitmap, raw);

                // Converts an image from one color space to another
                // converts given RGB image to grayscale
                Imgproc.cvtColor(raw, gray, Imgproc.COLOR_RGB2GRAY);

                // // Widely used to reduce image noise
                Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);

                // threshold the image in binary format between 0 or maxValue
                // based on given thresholding algorithm
                Imgproc.adaptiveThreshold(
                    gray,
                    th,
                    255,
                    Imgproc.ADAPTIVE_THRESH_MEAN_C,
                    Imgproc.THRESH_BINARY,
                    85,
                    2
                );

                // blurs an image using the median filter
                // often used to remove noise from an image or signal
                Imgproc.medianBlur(th, th, 5);

                /// Returns a structuring element of the specified size and shape
                Mat se = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15));

                // // Use the OpenCV function morphologyEx to apply Morphological Transformation such as:
                //     // Opening
                //     // Closing
                //     // Morphological Gradient
                //     // Top Hat
                //     // Black Hat
                Imgproc.morphologyEx(th, th, Imgproc.MORPH_CLOSE, se);

                List<MatOfPoint> contours = new ArrayList<>();
                Mat hieh = new Mat();

                // Finds contours in a binary image.
                // contours: an outline representing or bounding the shape or form of something.
                Imgproc.findContours(
                    th,
                    contours,
                    hieh,
                    Imgproc.RETR_LIST,
                    Imgproc.CHAIN_APPROX_SIMPLE
                );
                hieh.release();

                // filling up the 'dict' with valid blobs
                HashMap<Integer, Integer> dict = new HashMap<>();
                for (int idx = 0; idx < contours.size(); idx++) {
                    MatOfPoint contour = contours.get(idx);
                    if (isValidBlob(contour)) {
                        Rect r = Imgproc.boundingRect(contour);
                        Point center = getCenter(r);
                        addToDict(dict, center);
                        Imgproc.rectangle(
                            raw,
                            new Point(r.x, r.y),
                            new Point(r.x + r.width, r.y + r.height),
                            new Scalar( 255, 0, 0, 128),
                            8
                        );
                    }
                }

                // Logic to determine if we have found all six blobs or not
                boolean foundSixBlobs = false;
                int centerY = 0;
                Iterator it = dict.entrySet().iterator();

                // iterating over dict to see if
                // value of some entry is NB_BLOBS - 1
                // because #square_blobs = total_blobs - 1
                int value = 0;
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    value = (int) pair.getValue();
                    if (value == NB_BLOBS-1) { // changed
                        centerY = (int) pair.getKey();
                        foundSixBlobs = true;
                    }
                }

                if (!foundSixBlobs)
                    return "six blobs not found";
                    // return false;

                // otherwise
                // return true;
            // }

            
            // 6 BLOBS found successfully
// ------------------------------------------------------------------
            // Analysing 6 BLOBS


            int minX = raw.width();
            int maxX = 0;
            int heightSquareBlobs = centerY;

            // List of contours which are > 20 distance from the square blobs
            List<MatOfPoint> sampleCandidates = new ArrayList<>();
            for (int idx = 0; idx < contours.size(); idx++) {
                MatOfPoint contour = contours.get(idx);
                if (isValidBlob(contour)) {
                    Rect r = Imgproc.boundingRect(contour);
                    Point center = getCenter(r);
                    if (heightSquareBlobs - center.y > 20) {
                        sampleCandidates.add(contour);
                    }

                    if (Math.abs(heightSquareBlobs - center.y) <= 15 && rectIdx < NB_BLOBS-1) {
                        if (r.x < minX)
                            minX = r.x;
                        if (r.x + r.width > maxX)
                            maxX = r.x + r.width;
                            
                        rects[rectIdx] = new Rect(r.x, r.y, r.width, r.height);

                        SampleModel sample = getSampleModel(r, raw);

                        samples.put(rectIdx, sample);
                        rectIdx++;
                        Imgproc.rectangle(
                            raw,
                            new Point(r.x, r.y), 
                            new Point(r.x + r.width, r.y + r.height), 
                            new Scalar(0, 255, 0, 128), 
                            8
                        );
                    }

                }
            }

            double centerOfStandard = (minX + maxX) / 2;
            if (sampleCandidates.size() > 0) {
                MatOfPoint sample = sampleCandidates.get(0);
                Rect r = Imgproc.boundingRect(sample);
                Point center = getCenter(r);
                double delta = Math.abs(center.x - centerOfStandard);
                for (int idx = 1; idx < sampleCandidates.size(); idx++) {
                    MatOfPoint contour = sampleCandidates.get(idx);
                    r = Imgproc.boundingRect(contour);
                    center = getCenter(r);
                    double d = Math.abs(center.x - centerOfStandard);
                    if (d < delta) {
                        delta = d;
                        sample = contour;
                    }
                }
                r = Imgproc.boundingRect(sample);
                center = getCenter(r);
                rects[rectIdx] = new Rect(r.x, r.y, r.width, r.height);
                SampleModel sampleModel = getSampleModel(r, raw);
                samples.put(rectIdx, sampleModel);
                rectIdx++;
                Imgproc.rectangle(
                    raw, 
                    new Point(r.x, r.y),
                    new Point(r.x + r.width, r.y + r.height),
                    new Scalar(0, 255, 0, 128),
                    8
                );
            }
            org.opencv.android.Utils.matToBitmap(raw, changedBitmap);
            // raw.release();
            gray.release();
            th.release();
            if (rectIdx == NB_BLOBS) // changed
                return "true";

            // converting and saving the image to the local storage for better view
            Bitmap bmp = convertMatToBitmap(raw);
            try {
                saveBitmap(changedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // image to be viewed on debugger app
            String processedImage = convertMatToBase64(raw);
            return processedImage;
            // return "";
        }

        public static File saveBitmap(Bitmap bmp) throws IOException {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = Environment.getExternalStorageDirectory()
                    + File.separator + "ConcAnalyzer";
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();
            path += (File.separator + Long.toString(System.currentTimeMillis() / 1000) + ".jpg");
            File f = new File(path);
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
            return f;
        }

        private static void addToDict(HashMap<Integer, Integer> dict, Point center) {
            boolean found = false;
            Iterator it = dict.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                int Y = (int) pair.getKey();
                if (Math.abs(Y - center.y) <= 15) {
                    found = true;
                    Integer value = (Integer) pair.getValue();
                    value += 1;
                    pair.setValue(value);
                }
            }
            if (!found)
                dict.put((int) center.y, 1);
        }


        private static boolean isValidBlob(MatOfPoint contour) {
            boolean flag = true;
            double area = Imgproc.contourArea(contour);
            if (area < 2000 || area > 16000)
                flag = false;
            Rect r = Imgproc.boundingRect(contour);
            float aspectRatio;
            int w = r.width, h = r.height;

            if (w > h)
                aspectRatio = ((float) w) / h;
            else
                aspectRatio = ((float) h) / w;

            if (aspectRatio >= 1.5)
                flag = false;
            return flag;
        }

        // UTILITY FUNCTIONS
        public static String convertMatToBase64(Mat mat) {
            Bitmap bmp = null;
            bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
            org.opencv.android.Utils.matToBitmap(mat, bmp);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            bmp.recycle();
            String imageAsBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
            return imageAsBase64;
        }

        public static Bitmap convertMatToBitmap(Mat mat) {
            Bitmap bmp = null;
            bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
            org.opencv.android.Utils.matToBitmap(mat, bmp);
            return bmp;
        }

        public static Point getCenter(Rect r) {
            return new Point(r.x + r.width / 2, r.y + r.height / 2);
        }

        public static SampleModel getSampleModel(Rect r, Mat raw) {
            Rect rSmall = new Rect(r.x+10, r.y+10, r.width-20, r.height-20);
            Mat clip = new Mat(raw, rSmall);
            int bins[][] = new int[3][256];
            for (int y = 0; y < r.height-20; y++) {
                for (int x = 0; x < r.width-20; x++) {
                    bins[0][(int)clip.get(y, x)[0]]++;
                    bins[1][(int)clip.get(y, x)[1]]++;
                    bins[2][(int)clip.get(y, x)[2]]++;
                }
            }

            double mostFrequentIntensity = -1;
            double redIntensity = -1;
            double greenIntensity = -1;
            double blueIntensity = -1;
            int frequency = -1;
            
            // Red Intensity
            mostFrequentIntensity = -1;
            frequency = -1;
            for(int i=0; i<bins[0].length; i++) {
                if(bins[0][i] > frequency) {                    
                    frequency = bins[0][i];
                    mostFrequentIntensity = i;
                }
            }
            redIntensity = mostFrequentIntensity;

            // Green Intensity
            mostFrequentIntensity = -1;
            frequency = -1;
            for(int i=0; i<bins[1].length; i++) {
                if(bins[1][i] > frequency) {                    
                    frequency = bins[1][i];
                    mostFrequentIntensity = i;
                }
            }
            greenIntensity = mostFrequentIntensity;

            // Blue Intensity
            mostFrequentIntensity = -1;
            frequency = -1;
            for(int i=0; i<bins[2].length; i++) {
                if(bins[2][i] > frequency) {                    
                    frequency = bins[2][i];
                    mostFrequentIntensity = i;
                }
            }
            blueIntensity = mostFrequentIntensity;

            SampleModel sample = new SampleModel(
                redIntensity,
                greenIntensity,
                blueIntensity
            );
            clip.release();
            return sample;
        }


}