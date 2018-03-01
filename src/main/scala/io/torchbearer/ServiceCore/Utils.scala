package io.torchbearer.ServiceCore

import java.sql.Timestamp
import java.util.Calendar

import com.amazonaws.util.EC2MetadataUtils

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

  def getInstanceId: String = {
    if (sys.env.getOrElse("ENVIRONMENT", "development") != "production" ) {
      "Dev Machine"
    }
    EC2MetadataUtils.getInstanceId
  }

  def currentTimestamp: Timestamp = new java.sql.Timestamp(Calendar.getInstance().getTime.getTime)
}
