package io.torchbearer.ServiceCore.DataModel

import java.sql.Timestamp
import io.torchbearer.ServiceCore.Constants
import scalikejdbc._
import org.apache.commons.codec.binary.Base64
import sqls.count
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization

/**
  * Created by fredricvollmer on 11/9/16.
  */

case class ExecutionPoint(executionPointId: Int,
                          lat: Double,
                          long: Double,
                          bearing: Int,
                          executionPointType: String = Constants.EXECUTION_POINT_TYPE_MANEUVER,
                          closestIntersectionDistance: Double = 9999,
                          var sampleSet: Option[String] = None,
                          created: Option[Timestamp] = None,
                          lastModified: Option[Timestamp] = None) {

  def hash(): String = {
    val combinedValue = s"${lat}_${long}_$bearing"
    Base64.encodeBase64(combinedValue.getBytes).toString
  }
}

object ExecutionPoint extends SQLSyntaxSupport[ExecutionPoint] {

  override val tableName = "ExecutionPoints"
  implicit val formats = DefaultFormats

  def apply(ep: ResultName[ExecutionPoint])(rs: WrappedResultSet) = new ExecutionPoint(rs.int(ep.executionPointId),
    rs.double(ep.lat), rs.double(ep.long), rs.int(ep.bearing), rs.string(ep.executionPointType),
    rs.double(ep.closestIntersectionDistance), rs.stringOpt(ep.sampleSet),
    rs.timestampOpt(ep.created), rs.timestampOpt(ep.lastModified))

  def apply(jsonString: String): ExecutionPoint = {
    val parsedJson = parse(jsonString)
    parsedJson.extract[ExecutionPoint]
  }

  def apply(lat: Double, long: Double, bearing: Int): ExecutionPoint = new ExecutionPoint(0, lat, long, bearing)

  def apply(lat: Double, long: Double, bearing: Int, executionPointType: String, closestIntersectionDistance: Double): ExecutionPoint =
    new ExecutionPoint(0, lat, long, bearing, executionPointType, closestIntersectionDistance)

  /** ********** Query Functions ******************/

  def getExecutionPoint(hash: String): Option[ExecutionPoint] = {
    val s = new String(Base64.decodeBase64(hash), "UTF-8")
    val parts = s.split("_")

    if (parts.length != 3) {
      println("ServiceCore: Invalid executionPoint hash provided to getExecutionPoint")
      return None
    }

    getExecutionPoint(parts(0).toFloat, parts(1).toFloat, parts(2).toInt)
  }

  def getExecutionPoint(lat: Double, long: Double, bearing: Int): Option[ExecutionPoint] = {
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

  def getExecutionPoints(eps: List[ExecutionPoint]): List[ExecutionPoint] = {
    val querySeq = eps.map(p => (p.lat, p.long, p.bearing))

    val ep = ExecutionPoint.syntax("ep")
    val points: List[ExecutionPoint] = DB readOnly { implicit session: DBSession =>
      withSQL {
        select(ep.result.*)
          .from(ExecutionPoint as ep)
          .where.in((ep.lat, ep.long, ep.bearing), querySeq)
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

  /** ************* Insert *******************/
  def insertExecutionPointIfNotExists(p: ExecutionPoint): Unit = {
    implicit val formats = Serialization.formats(NoTypeHints)

    DB localTx { implicit session: DBSession =>
      sql"INSERT INTO ExecutionPoints (execution_point_type, closest_intersection_distance, lat, `long`, bearing, sample_set) VALUES (${p.executionPointType}, ${p.closestIntersectionDistance}, ${p.lat}, ${p.long}, ${p.bearing}, ${p.sampleSet}) ON DUPLICATE KEY UPDATE execution_point_type=${p.executionPointType}, lat=${p.lat}, `long`=${p.long}, bearing=${p.bearing}, sample_set=${p.sampleSet}"
        .update.apply()
    }
  }

  def insertExecutionPoints(points: List[ExecutionPoint]): Unit = {
    implicit val formats = Serialization.formats(NoTypeHints)

    val ep = ExecutionPoint.column
    DB localTx { implicit session: DBSession =>
      points.foreach(p => {
        // We need to hardcode SQL statement here to avoid reserved word issue in MySQL
        // TODO: Find way to integrate with QueryDSL
        sql"INSERT INTO ExecutionPoints (execution_point_type, closest_intersection_distance, lat, `long`, bearing) VALUES (${p.executionPointType}, ${p.closestIntersectionDistance}, ${p.lat}, ${p.long}, ${p.bearing})"
          .update.apply()
      })
    }
  }

  /** ************* Route processing ****************/

  /**
    * Returns a map of (lat, long, bearing) onto executionPointId from database
    *
    * @param points
    * @return
    */
  def ingestExecutionPoints(points: List[ExecutionPoint]): Map[(Double, Double, Int), Int] = {
    // Retrieve existing points from db
    val existingPoints = getExecutionPoints(points)

    // Difference points needed points and existing points
    var newPoints = points.filter(p0 => !existingPoints.exists(p1 =>
      p1.bearing == p0.bearing
        && p1.lat == p0.lat
        && p1.long == p0.long
    ))

    // Add those points to db
    insertExecutionPoints(newPoints)

    // Retrieve new execution points, so we have id's
    // TODO: Surely there has to be a more efficient way of doing this!!
    newPoints = getExecutionPoints(newPoints)

    // concatenate newly added points with existing points
    val combinedPoints = newPoints ++ existingPoints

    // Map (lat, long, bearing) onto points
    combinedPoints.map(p => {
      (p.lat, p.long, p.bearing) -> p.executionPointId
    })
      .toMap
  }
}
