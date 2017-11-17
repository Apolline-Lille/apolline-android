package science.apolline.sensor.ioio.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import science.apolline.sensor.ioio.model.IOIOData;

public class IOIOService extends ioio.lib.util.android.IOIOService{

    boolean led = true;



    @Override
    protected IOIOLooper createIOIOLooper() {
        return new BaseIOIOLooper(){
            private IOIOData data = new IOIOData();
            private DigitalOutput led_;
            private Uart uart_ = null;
            private InputStream uartIn_ = null;
            private AnalogInput inputTemp;
            private AnalogInput inputHum;
            private int freq = 1000;


            @Override
            protected void setup() throws ConnectionLostException,InterruptedException {
                led_ = ioio_.openDigitalOutput(0, true);
                uart_ = ioio_.openUart(7, 6, 9600, Uart.Parity.NONE, Uart.StopBits.ONE);
                uartIn_ = uart_.getInputStream();
                inputTemp = ioio_.openAnalogInput(44);
                inputHum = ioio_.openAnalogInput(42);
            }

            @Override
            public void loop() throws ConnectionLostException,InterruptedException {
                //Log.e("ioioService","loop");
                try {
                    int availableCount = this.uartIn_.available();
                    if (availableCount > 0) {
                        byte[] buffer = new byte[availableCount];
                        this.uartIn_.read(buffer);
                        for (int b : buffer) {
                            switch (b) {
                                case 0x42:
                                    data.setCount(0);
                                    break;
                                default:
                                    data.setBuff(data.getCount(), b & 0xff);
                                    data.setCount(data.getCount()+1);
                                    if (data.getCount() == data.LENG) {
                                        data.setCount(0);
                                        if (data.checkValue()) {
                                            led = !led;
                                            led_.write(led);

                                            //parse PM value
                                            data.parse();

                                            //Température
                                            float tensionTemp = inputTemp.getVoltage();
                                            data.setTempKelvin(tensionTemp*100);

                                            //humidity
                                            float tensionHum = inputHum.getVoltage();
                                            float RH = (float) (((tensionHum / 3.3) - 0.1515) / 0.0636); //pour avoir l'humidité à 25°C
                                            double RHT = (RH / (1.0546 - 0.00216 * ((tensionTemp * 100) - 273.15))) * 10; //compensé en t°
                                            data.setRH(RH);
                                            data.setRHT(RHT);


                                        }
                                    }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                
                Thread.sleep(freq);
                sendBroadcast(data);
            }
        };
    }

    private void sendBroadcast(IOIOData data){

        Intent intent = new Intent("IOIOdata");
        intent.putExtra("dataBundle",data);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
