package com.reactlibrary;

// android util imports
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
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

            // error says, the native function getBytes() is being called on a null object.
            // but i do not even know where is even getBytes being called?


            byte[] decodedString = Base64.decode(imageAsBase64, Base64.DEFAULT);
            changedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            
            // checks if image is successfully received or not
            if (changedBitmap != null) {
                try {
                    // ??? What the hell is saveBitmap()
                    saveBitmap(changedBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // samples basically is list of blobs found
                samples = new HashMap<>();

                // ??? rects
                // ??? Rect
                // ??? NB_BLOBS
                rects = new Rect[NB_BLOBS];
                int rectIdx = 0;

                // // ??? Mat
                Mat raw = new Mat();
                Mat gray = new Mat();
                Mat th = new Mat();

                // // converts fetched 'changedBitmap' image to Mat format and stores in 'raw' // So, raw is basically the Mat format of the image clicked
                org.opencv.android.Utils.bitmapToMat(changedBitmap, raw);

                // // Converts an image from one color space to another // converts given RGB image to grayscale
                Imgproc.cvtColor(raw, gray, Imgproc.COLOR_RGB2GRAY);

                // // Widely used to reduce image noise
                Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);

                // // threshold the image in binary format between 0 or maxValue based on given thresholding algorithm
                Imgproc.adaptiveThreshold(gray, th, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 85, 2);

                // // blurs an image using the median filter
                // // The median filter is a nonlinear digital filtering technique, 
                // // often used to remove noise from an image or signal
                Imgproc.medianBlur(th, th, 5);
                
                // // Returns a structuring element of the specified size and shape for morphological operations.
                // // ??? Size()
                Mat se = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15));

                // // Use the OpenCV function morphologyEx to apply Morphological Transformation such as:
                //     // Opening
                //     // Closing
                //     // Morphological Gradient
                //     // Top Hat
                //     // Black Hat
                Imgproc.morphologyEx(th, th, Imgproc.MORPH_CLOSE, se);

                // // ??? List
                // // ??? MatOfPoint
                // // ??? ArrayList
                List<MatOfPoint> contours = new ArrayList<>();
                Mat hieh = new Mat();

                // // Finds contours in a binary image.
                // // contours: an outline representing or bounding the shape or form of something.
                Imgproc.findContours(th, contours, hieh, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

                // // ??? What does this function do?
                hieh.release();

                // // ??? HashMap()
                HashMap<Integer, Integer> dict = new HashMap<>();
                for (int idx = 0; idx < contours.size(); idx++) {
                    MatOfPoint contour = contours.get(idx);
                    
                    // ??? isValidBlob
                    if (isValidBlob(contour)) {
                        // ??? Rect, I am pretty sure, I have marked it above
                        Rect r = Imgproc.boundingRect(contour);

                        // ??? Point
                        Point center = new Point(r.x + r.width / 2, r.y + r.height / 2);

                        // ??? addToDict and why it is important
                        addToDict(dict, center);
                    }
                }


                // Logic to determine if we have found all six blobs or not
                boolean foundSixBlobs = false;
                int centerY = 0;

                // ??? Iterator
                Iterator it = dict.entrySet().iterator(); 

                while (it.hasNext()) {
                    // ??? Map
                    Map.Entry pair = (Map.Entry) it.next();
                    int value = (int) pair.getValue();
                    if (value == NB_BLOBS-1) { // changed
                        centerY = (int) pair.getKey();
                        foundSixBlobs = true;
                    }
                }
                if (!foundSixBlobs)
                    // return false;
                    return "false from upper bottom";
                
                // otherwise
                // return true;
                return "true from upper bottom";
            }

            // return false;
            return "false from lower bottom";
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
            if (area < 1000 || area > 16000)
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
    
    // getIntensityValues() {

    // }

}
