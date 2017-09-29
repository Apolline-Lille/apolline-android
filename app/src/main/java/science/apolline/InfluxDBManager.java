package science.apolline;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

/**
 * Created by sparow on 29/09/2017.
 */

public class InfluxDBManager {


    public final String DEVICE = "deviceId";
    public final String SENSOR = "sensorId";
    public final String GAS = "gasId";
    public final String TEMPERATURE = "temperatureId";
    public final String HUMIDITY = "humidityId";
    public final String PM = "pmId";
    public final String UNIT = "unitId";
    public final String VALUE = "value";
    public final String UNKNOW = "unknow";


    public InfluxDB configInfluxDB(String dbName, String dbUrlPort, String dbUser, String dbPassword) {

        InfluxDB influxDB = InfluxDBFactory.connect(dbUrlPort, dbUser, dbPassword);
        influxDB.setLogLevel(InfluxDB.LogLevel.FULL);
        influxDB.setConsistency(InfluxDB.ConsistencyLevel.ALL);
        influxDB.createDatabase(dbName);

        return influxDB;
    }


    public void writePoint(InfluxDB influxDB, String dbMeasurment, JSONObject json) throws JSONException {

        Point.Builder pointBuilder = Point.measurement(dbMeasurment)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        pointBuilder.addField(DEVICE, json.optString(DEVICE, UNKNOW))
                .addField(SENSOR, json.optString(SENSOR, UNKNOW));

        if (json.optString(GAS) != null) {
            pointBuilder.addField(GAS, json.optString(GAS, UNKNOW));
        } else if (json.optString(TEMPERATURE) != null) {
            pointBuilder.addField(TEMPERATURE, json.optString(TEMPERATURE, UNKNOW));
        } else if (json.optString(HUMIDITY) != null) {
            pointBuilder.addField(HUMIDITY, json.optString(HUMIDITY, UNKNOW));
        } else if (json.optString(PM) != null) {
            pointBuilder.addField(PM, json.optString(PM, UNKNOW));
        }

        pointBuilder.addField(UNIT, json.optString(UNIT, UNKNOW))
                .addField(VALUE, json.optInt(VALUE, -1));

        influxDB.write(pointBuilder.build());

    }


}
