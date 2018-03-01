package io.torchbearer.ServiceCore

/**
  * Created by fredricvollmer on 10/21/17.
  */
object CartographyUtils {
  def relativeBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int = {
    val lat1Rad = Math.toRadians(lat1)
    val lat2Rad = Math.toRadians(lat2)
    val longDiff = Math.toRadians(lon2 - lon1)
    val y = Math.sin(longDiff) * Math.cos(lat2Rad)
    val x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(longDiff)
    ((Math.toDegrees(Math.atan2(y, x)) + 360) % 360).toInt
  }
}
