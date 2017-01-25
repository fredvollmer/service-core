package io.torchbearer.ServiceCore

/**
  * Created by fredricvollmer on 11/11/16.
  */
object Utils {
  def formatURLWithQueryParams(base: String, params: (String, Any)*): String = {
    val paramString = new StringBuilder(base)
    var prefix = "?"
    for ((key, value) <- params) {
      paramString append s"$prefix$key=$value"
      prefix = "&"
    }
    paramString.toString()
  }
}
