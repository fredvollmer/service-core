package io.torchbearer.ServiceCore

/**
  * Created by fredricvollmer on 10/21/17.
  */

// Returns a degree in (-180, 180)
object CartographyUtils {
  def relativeBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double, initialBearing: Int): Int = {
    val lat1Rad = Math.toRadians(lat1)
    val lat2Rad = Math.toRadians(lat2)
    val longDiff = Math.toRadians(lon2 - lon1)
    val y = Math.sin(longDiff) * Math.cos(lat2Rad)
    val x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(longDiff)
    // atan2 returns bearing w.r.t. due north between -180 and 180. to put in context of initial bearing, we subtract.
    // Get initial bearing in range of -180 to 180 as well (it's currently in 0 to 360):
    val normalizedInitialBearing = if (initialBearing > 180) {
      -(360 - initialBearing)
    } else {
      initialBearing
    }

    (Math.toDegrees(Math.atan2(y, x)) - normalizedInitialBearing).toInt
  }
}
