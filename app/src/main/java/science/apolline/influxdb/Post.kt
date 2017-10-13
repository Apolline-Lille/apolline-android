package science.apolline.influxdb

/**
 * Created by sparow on 13/10/2017.
 */


data class Post(var dbName: String, var dbUserName: String, var dbPassword: String , var data: String){

    override fun toString(): String {
        return """
        |Database Name = $dbName ,
        |Database Username = $dbUserName ,
        |Database Password = $dbPassword ,
        |Database Requeste content = $data ,
        """.trimMargin()
    }
}