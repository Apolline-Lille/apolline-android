package science.apolline.utils

import org.testcontainers.containers.GenericContainer

class InfluxDBContainer(imageName: String): GenericContainer<InfluxDBContainer>(imageName)
