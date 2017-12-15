package science.apolline.models;

import android.os.Parcel;

import com.google.gson.Gson;
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

    @Override
    public JsonObject toJson(){


        JsonObject obj = new JsonObject();

        JsonArray array1 = new JsonArray();

        array1.add(PM01Value);
        array1.add("ug/m3");
        obj.add("PM01Value",array1);
        JsonArray array2 = new JsonArray();
        array2.add(PM2_5Value);
        array2.add("ug/m3");
        obj.add("PM2_5Value",array2);
        JsonArray array3 = new JsonArray();
        array3.add(PM10Value);
        array3.add("ug/m3");
        obj.add("PM10Value",array3);
        JsonArray array4 = new JsonArray();
        array4.add(PM0_3Above);
        array4.add("ug/m3");
        obj.add("PM0_3Above",array4);
        JsonArray array5 = new JsonArray();
        array5.add(PM0_5Above);
        array5.add("ug/m3");
        obj.add("PM0_5Above",array5);
        JsonArray array6 = new JsonArray();
        array6.add(PM1Above);
        array6.add("ug/m3");
        obj.add("PM1Above",array6);
        JsonArray array7 = new JsonArray();
        array7.add(PM2_5Above);
        array7.add("ug/m3");
        obj.add("PM2_5Above",array7);
        JsonArray array8 = new JsonArray();
        array8.add(PM5Above);
        array8.add("ug/m3");
        obj.add("PM5Above",array8);
        JsonArray array9 = new JsonArray();
        array9.add(PM10Above);
        array9.add("ug/m3");
        obj.add("PM10Above",array9);
        JsonArray array10 = new JsonArray();
        array10.add(tempKelvin);
        array10.add("kelvin");
        obj.add("tempKelvin",array10);
        JsonArray array11 = new JsonArray();
        array11.add(RH);
        array11.add("%");
        obj.add("RH",array11);
        JsonArray array12 = new JsonArray();
        array12.add(RHT);
        array12.add("%");
        obj.add("RHT",array12);

        Gson gson = new Gson();


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
