package science.apolline.influxdb

/**
 * Created by sparow on 13/10/2017.
 */


data class Post(var dbName: String, var dbUserName: String, var dbPassword: String , var data: String){

    override fun toString(): String {
        return """
        |dbName = $dbName ,
        |dbUsername = $dbUserName ,
        |dbPassword = $dbPassword ,
        |dbData = $data ,
        """.trimMargin()
    }
}