package com.reactlibrary;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Abdul on 3/12/2016.
 * An instance of this class represents a detected region with an average intensity, type, and concentration.
 */
 // Next is a SampleModel class. First was DefaultsModel class
 // ok, this is good.
 // more similar code means better chances of understanding
public class SampleModel implements Parcelable {
    /**
     * The constant CREATOR for Parcelable interface.
     */
    public static final Parcelable.Creator<SampleModel> CREATOR = new Parcelable.Creator<SampleModel>() {
        @Override
        public SampleModel createFromParcel(Parcel parcel) {
            return new SampleModel(parcel);
        }

        @Override
        public SampleModel[] newArray(int i) {
            return new SampleModel[i];
        }
    };
    // ok, a variable for redIntensity. What does 'm' means. mediumPrecission?
    private double mRedIntensity;
    
    // green intensity variable
    private double mGreenIntensity;

    // blue intensity variable
    private double mBlueIntensity;

    // concentration variabls. maybe with medium precision
    private double mConcentration;

    // DataPointType? what the hell is this class and from where is this class imported?
    // found no import, I think this is some native feature of the language that I am unaware of.
    private DataPointType mDataPointType;

    // boolean can not be a mediumPrecision variable, so maybe that confirms it..
    private boolean mUpdated;

    /**
     * Instantiates a new Sample model.
     */
     // constructor which initializes all values to zero.
    public SampleModel() {
        this.mRedIntensity = 0;
        this.mGreenIntensity = 0;
        this.mBlueIntensity = 0;
        this.mConcentration = 0;
        this.mDataPointType = DataPointType.NONE;
        this.mUpdated = false;
    }

    // next we simply take the values from input variable
    private SampleModel(Parcel in) {
        this.mRedIntensity = in.readDouble();
        this.mGreenIntensity = in.readDouble();
        this.mBlueIntensity = in.readDouble();
        this.mConcentration = in.readDouble();
        this.mDataPointType = DataPointType.values()[in.readInt()];
        this.mUpdated = in.readByte() != 0;
    }

    /**
     * Instantiates a new Sample model.
     *
     * @param mRedIntensity the red intensity value
     * @param mGreenIntensity the green intensity value
     * @param mBlueIntensity the blue intensity value
     */
     // next, a constructor for taking variables individually.
    public SampleModel(double mRedIntensity, double mGreenIntensity, double mBlueIntensity) {
        this.mRedIntensity = mRedIntensity;
        this.mGreenIntensity = mGreenIntensity;
        this.mBlueIntensity = mBlueIntensity;
        this.mConcentration = 0;
        this.mDataPointType = DataPointType.NONE;
        this.mUpdated = false;
    }

    @Override
    // this is a lol function. ismein kujj bhi nahi hai. nautanki function hai yeh
    public int describeContents() {
        return 0;
    }

    @Override
    // given an input parcel variable, we can input the features of this object into that object
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(mRedIntensity);
        parcel.writeDouble(mGreenIntensity);
        parcel.writeDouble(mBlueIntensity);
        parcel.writeDouble(mConcentration);
        parcel.writeInt(mDataPointType.ordinal());
        parcel.writeByte((byte) (mUpdated ? 1 : 0));
    }

    /**
     * Returns true if this SampleModel has been updated with valid data.
     *
     * @return the boolean
     */
     // getter for updated variable
    public boolean isUpdated() {
        return mUpdated;
    }

    /**
     * Sets if this SampleModel has been updated with valid data.
     *
     * @param updated the updated
     */
     // setter for updated variable
    public void setUpdated(boolean updated) {
        this.mUpdated = updated;
    }

    /**
     * Gets concentration.
     *
     * @return the concentration
     */
     // getter for concentration variable
    public double getConcentration() {
        return mConcentration;
    }

    /**
     * Sets concentration.
     *
     * @param concentration the concentration
     */
     // setter for concentration variable
    public void setConcentration(double concentration) {
        this.mConcentration = concentration;
    }

    /**
     * Gets data point type.
     *
     * @return the data point type
     */
     // getter for datapoint variable
    public DataPointType getDataPointType() {
        return mDataPointType;
    }

    /**
     * Sets data point type.
     *
     * @param dataPointType the data point type
     */
     // setter for datapoint variable
    public void setDataPointType(DataPointType dataPointType) {
        this.mDataPointType = dataPointType;
    }

    /**
     * Gets intensity.
     *
     * @return the intensity
     */
     // getter for intensity variable
    public double[] getIntensities() {
        return new double[]{mRedIntensity, mGreenIntensity, mBlueIntensity};
    }

    @Override
    // to string for simply logging the object
    public String toString() {
        return "{Intensity:" + this.mGreenIntensity
                + ", Concentration:" + this.mConcentration
                + ", Updated:" + this.mUpdated
                + "}";
    }

    /**
     * The enum Data point type.
     */
    public enum DataPointType {
        /**
         * None data point type.
         */
        NONE,
        /**
         * Unknown data point type.
         */
        UNKNOWN,
        /**
         * Quality control data point type.
         */
        QUALITY_CONTROL,
        /**
         * Known data point type.
         */
        KNOWN
    }
}