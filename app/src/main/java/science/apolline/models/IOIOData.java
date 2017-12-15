package science.apolline.models;

import android.os.Parcel;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class IOIOData implements IntfSensorData {
    private int count;
    private int[] buff = new int[64];

    private int PM01Value = 0;          //define PM1.0 value of the air detector module
    private int PM2_5Value = 0;         //define PM2.5 value of the air detector module
    private int PM10Value = 0;         //define PM10 value of the air detector module

    private int PM0_3Above = 0;
    private int PM0_5Above = 0;
    private int PM1Above = 0;
    private int PM2_5Above = 0;
    private int PM5Above = 0;
    private int PM10Above = 0;

    private float tempKelvin = 0;
    private float RH = 0;
    private double RHT = 0;

    public final byte LENG = 31;

    public IOIOData(){}

    protected IOIOData(Parcel in) {
        PM01Value = in.readInt();
        PM2_5Value = in.readInt();
        PM10Value = in.readInt();
        PM0_3Above = in.readInt();
        PM0_5Above = in.readInt();
        PM1Above = in.readInt();
        PM2_5Above = in.readInt();
        PM5Above = in.readInt();
        PM10Above = in.readInt();
        tempKelvin = in.readFloat();
        RH = in.readFloat();
        RHT = in.readDouble();
    }

    public static final Creator<IOIOData> CREATOR = new Creator<IOIOData>() {
        @Override
        public IOIOData createFromParcel(Parcel in) {
            return new IOIOData(in);
        }

        @Override
        public IOIOData[] newArray(int size) {
            return new IOIOData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(PM01Value);
        parcel.writeInt(PM2_5Value);
        parcel.writeInt(PM10Value);

        parcel.writeInt(PM0_3Above);
        parcel.writeInt(PM0_5Above);
        parcel.writeInt(PM1Above);
        parcel.writeInt(PM2_5Above);
        parcel.writeInt(PM5Above);
        parcel.writeInt(PM10Above);

        parcel.writeFloat(tempKelvin);
        parcel.writeFloat(RH);
        parcel.writeDouble(RHT);
    }

    

    public boolean checkValue() {
        boolean receiveflag = false;
        int receiveSum = 0;

        for (int i = 0; i < (LENG - 2); i++) {
            receiveSum = receiveSum + buff[i];
        }
        receiveSum = receiveSum + 0x42;

        if (receiveSum == ((buff[LENG - 2] << 8) + buff[LENG - 1])) //check the serial data
        {
            receiveflag = true;
        }
        return receiveflag;
    }
    
    public void parse(){
        PM01Value = ((buff[3] << 8) + buff[4]);
        PM2_5Value = ((buff[5] << 8) + buff[6]);
        PM10Value = ((buff[7] << 8) + buff[8]);
        PM0_3Above = ((buff[15] << 8) + buff[16]);
        PM0_5Above = ((buff[17] << 8) + buff[18]);
        PM1Above = ((buff[19] << 8) + buff[20]);
        PM2_5Above = ((buff[21] << 8) + buff[22]);
        PM5Above = ((buff[23] << 8) + buff[24]);
        PM10Above = ((buff[25] << 8) + buff[26]);
    }

    public float getTempCelcius(){
        return tempKelvin - 273.15f;
    }


    public enum Units {
        CONCENTRATION_UG_M3("ug/m3"),
        PERCENTAGE("%"),
        TEMPERATURE_KELVIN("K"),

        ;

        private final String name;

        Units(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }


    private void addNestedJsonArray(JsonObject obj, String property, double value, Units unit){
        JsonArray array = new JsonArray();
        array.add(value);
        array.add(String.valueOf(unit));
        obj.add(property,array);
    }

    @Override
    public JsonObject toJson(){
        JsonObject obj = new JsonObject();

        addNestedJsonArray(obj, "pm.01.value", PM01Value, Units.CONCENTRATION_UG_M3);
        addNestedJsonArray(obj, "pm.2.5.value", PM2_5Value, Units.CONCENTRATION_UG_M3);
        addNestedJsonArray(obj, "pm.10.value", PM10Value, Units.CONCENTRATION_UG_M3);

        addNestedJsonArray(obj, "pm.0.3.above", PM0_3Above, Units.CONCENTRATION_UG_M3);
        addNestedJsonArray(obj, "pm.0.5.above", PM0_5Above, Units.CONCENTRATION_UG_M3);
        addNestedJsonArray(obj, "pm.1.above", PM1Above, Units.CONCENTRATION_UG_M3);
        addNestedJsonArray(obj, "pm.2.5.above", PM2_5Above, Units.CONCENTRATION_UG_M3);
        addNestedJsonArray(obj, "pm.5.above", PM5Above, Units.CONCENTRATION_UG_M3);
        addNestedJsonArray(obj, "pm.10.above", PM10Above, Units.CONCENTRATION_UG_M3);

        addNestedJsonArray(obj, "temperature", tempKelvin, Units.TEMPERATURE_KELVIN);
        addNestedJsonArray(obj, "humidity", RH, Units.PERCENTAGE);
        addNestedJsonArray(obj, "humidity.componsated", RHT, Units.PERCENTAGE);

        return obj;
    }


    
////////////////////////////////////////////////////////////////////////////////////////////////////


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int[] getBuff() {
        return buff;
    }
    
    public void setBuff(int position,int value) {
        this.buff[position]=value;
    }

    public int getPM01Value() {
        return PM01Value;
    }

    public void setPM01Value(int PM01Value) {
        this.PM01Value = PM01Value;
    }

    public int getPM2_5Value() {
        return PM2_5Value;
    }

    public void setPM2_5Value(int PM2_5Value) {
        this.PM2_5Value = PM2_5Value;
    }

    public int getPM10Value() {
        return PM10Value;
    }

    public void setPM10Value(int PM10Value) {
        this.PM10Value = PM10Value;
    }

    public int getPM0_3Above() {
        return PM0_3Above;
    }

    public void setPM0_3Above(int PM0_3Above) {
        this.PM0_3Above = PM0_3Above;
    }

    public int getPM0_5Above() {
        return PM0_5Above;
    }

    public void setPM0_5Above(int PM0_5Above) {
        this.PM0_5Above = PM0_5Above;
    }

    public int getPM1Above() {
        return PM1Above;
    }

    public void setPM1Above(int PM1Above) {
        this.PM1Above = PM1Above;
    }

    public int getPM2_5Above() {
        return PM2_5Above;
    }

    public void setPM2_5Above(int PM2_5Above) {
        this.PM2_5Above = PM2_5Above;
    }

    public int getPM5Above() {
        return PM5Above;
    }

    public void setPM5Above(int PM5Above) {
        this.PM5Above = PM5Above;
    }

    public int getPM10Above() {
        return PM10Above;
    }

    public void setPM10Above(int PM10Above) {
        this.PM10Above = PM10Above;
    }

    public float getTempKelvin() {
        return tempKelvin;
    }

    public void setTempKelvin(float tempKelvin) {
        this.tempKelvin = tempKelvin;
    }

    public float getRH() {
        return RH;
    }

    public void setRH(float RH) {
        this.RH = RH;
    }

    public double getRHT() {
        return RHT;
    }

    public void setRHT(double RHT) {
        this.RHT = RHT;
    }


}
