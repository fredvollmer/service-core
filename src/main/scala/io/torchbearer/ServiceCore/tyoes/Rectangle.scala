package io.torchbearer.ServiceCore.tyoes

import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
  * Created by fredricvollmer on 11/15/16.
  */
case class Rectangle (x1:Double, x2:Double, y1:Double, y2:Double) {

}

object Rectangle {
  implicit val formats = DefaultFormats

  def apply(jsonString: String): Rectangle = {
    val parsedJson = parse(jsonString)
    parsedJson.extract[Rectangle]
  }
}
