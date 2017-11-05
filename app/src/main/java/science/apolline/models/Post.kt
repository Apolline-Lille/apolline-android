package science.apolline.models

/**
 * Created by sparow on 13/10/2017.
 */

data class Post(
        val dbName: String,
        val dbUserName: String,
        val dbPassword: String ,
        val body: String){

    override fun toString(): String {
        return """
        |dbName = $dbName ,
        |dbUsername = $dbUserName ,
        |dbPassword = $dbPassword ,
        |dbData = $body ,
        """.trimMargin()
    }
}