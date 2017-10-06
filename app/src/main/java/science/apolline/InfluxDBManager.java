package science.apolline;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by sparow on 29/09/2017.
 */

public class InfluxDBManager {


    public final String DEVICE = "device";
    public final String SENSOR = "sensor";
    public final String DATA = "gas";
    public final String UNIT = "unit";
    public final String VALUE = "value";
    public final String DATE = "date";


    public InfluxDB configInfluxDB(String dbName, String dbUrlPort, String dbUser, String dbPassword) {

        InfluxDB influxDB = InfluxDBFactory.connect(dbUrlPort, dbUser, dbPassword);
        influxDB.setLogLevel(InfluxDB.LogLevel.NONE);
        influxDB.setConsistency(InfluxDB.ConsistencyLevel.QUORUM);
        // influxDB.createDatabase(dbName);
        // Flush every 2000 Points, at least every 100ms
        influxDB.enableBatch(2000, 100, TimeUnit.NANOSECONDS);

        return influxDB;
    }


    public void write(InfluxDB influxDB, String dbName, String jsonArg) {

        BatchPoints batchPoints = BatchPoints
                .database(dbName)
                .build();

        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(jsonArg);

        if (jsonElement.isJsonObject()) {

            JsonObject json = jsonElement.getAsJsonObject();

            if (json.has(DATA)) {
                JsonObject tmp = json.get(DATA).getAsJsonObject();

                Set<Map.Entry<String, JsonElement>> entries = tmp.entrySet();
                for (Map.Entry<String, JsonElement> entry : entries) {

                    Point.Builder pointBuilder = Point.measurement(entry.getKey())
                            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                            .addField(VALUE, entry.getValue().toString())
                            .addField(DATE, json.get(DATE).getAsString())
                            .tag(DEVICE, json.get(DEVICE).getAsString())
                            .tag(SENSOR, json.get(SENSOR).getAsString())
                            .tag(UNIT, json.get(UNIT).getAsString());

                    Point point = pointBuilder.build();
                    batchPoints.point(point);

                }

            }

            influxDB.write(batchPoints);

        }


    }


}
