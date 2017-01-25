package io.torchbearer.ServiceCore.DataModel

import java.sql.Timestamp

import scalikejdbc._
import org.apache.commons.codec.binary.Base64
import sqls.count
import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
  * Created by fredricvollmer on 11/9/16.
  */

case class ExecutionPoint(executionPointId: String,
                          lat: Double,
                          long: Double,
                          bearing: Int,
                          status: String,
                          saliencyHit: Option[String],
                          descriptionHit: Option[String],
                          sampleSet: Option[String],
                          created: Option[Timestamp],
                          lastModified: Option[Timestamp]) {

  def hash(): String = {
    val combinedValue = s"${lat}_${long}_$bearing"
    Base64.encodeBase64(combinedValue.getBytes).toString
  }
}

object ExecutionPoint extends SQLSyntaxSupport[ExecutionPoint] {

  override val tableName = "ExecutionPoints"
  implicit val formats = DefaultFormats

  def apply(ep: ResultName[ExecutionPoint])(rs: WrappedResultSet) = new ExecutionPoint(
    rs.string(ep.executionPointId), rs.double(ep.lat), rs.double(ep.long),
    rs.int(ep.bearing), rs.string(ep.status), rs.stringOpt(ep.saliencyHit),
    rs.stringOpt(ep.descriptionHit), rs.stringOpt(ep.sampleSet), rs.timestampOpt(ep.created),
    rs.timestampOpt(ep.lastModified))

  def apply(jsonString: String): ExecutionPoint = {
    val parsedJson = parse(jsonString)
    parsedJson.extract[ExecutionPoint]
  }

  /************ Query Functions ******************/

  def getExecutionPoint(hash: String): Option[ExecutionPoint] = {
    val s = new String(Base64.decodeBase64(hash), "UTF-8")
    val parts = s.split("_")

    if (parts.length != 3) {
      println("ServiceCore: Invalid executionPoint hash provided to getExecutionPoint")
      return None
    }

    getExecutionPoint(parts(0).toFloat, parts(1).toFloat, parts(2).toInt)
  }

  def getExecutionPoint(lat: Float, long: Float, bearing: Int): Option[ExecutionPoint] = {
    val ep = ExecutionPoint.syntax("ep")
    val point: Option[ExecutionPoint] = DB readOnly { implicit session: DBSession =>
      withSQL {
        select(ep.result.*)
          .from(ExecutionPoint as ep)
          .where
          .eq(ep.lat, lat)
          .and
          .eq(ep.long, long)
          .and
          .eq(ep.bearing, bearing)
      }.map(ExecutionPoint(ep.resultName)).single.apply
    }
    point
  }

  def getExecutionPoint(pointId: Int): Option[ExecutionPoint] = {
    val ep = ExecutionPoint.syntax("ep")
    val point: Option[ExecutionPoint] = DB readOnly { implicit session: DBSession =>
      withSQL {
        select(ep.result.*)
          .from(ExecutionPoint as ep)
          .where
          .eq(ep.executionPointId, pointId)
      }.map(ExecutionPoint(ep.resultName)).single.apply
    }
    point
  }

  def getPagedExecutionPoints(offset: Int, count: Int): List[ExecutionPoint] = {
    val ep = ExecutionPoint.syntax("ep")
    val points: List[ExecutionPoint] = DB readOnly { implicit session: DBSession =>
      withSQL {
        select(ep.result.*)
          .from(ExecutionPoint as ep)
          .limit(count)
          .offset(offset)
      }.map(ExecutionPoint(ep.resultName)).list.apply
    }
    points
  }

  def getCount: Int = {
    val ep = ExecutionPoint.syntax("ep")
    val total = DB readOnly { implicit session: DBSession =>
      withSQL {
        select(count(ep.executionPointId))
          .from(ExecutionPoint as ep)
      }.map(_.int(1)).single.apply.get
    }
    total
  }
}
