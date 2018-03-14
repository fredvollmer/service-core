package io.torchbearer.ServiceCore.DataModel

import java.sql.Timestamp

import io.torchbearer.ServiceCore.tyoes.{Description, Rectangle}
import org.json4s.{DefaultFormats, NoTypeHints}
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import scalikejdbc._
import scalikejdbc.interpolation.SQLSyntax._

/**
  * Created by fredricvollmer on 11/9/16.
  */

case class Hit(hitId: Int,
               pipeline: String,
               var selectedLandmarkId: Option[String],
               executionPointId: Int,
               var saliencyHitId: Option[String] = None,
               var status: String = "UNKNOWN",
               var currentTaskToken: Option[String] = None,
               var processingStartTime: Option[Timestamp] = None,
               var processingEndTime: Option[Timestamp] = None,
               var cost: Int = 0
              ) {

  def updateStatus(status: String): Unit = {
    this.status = status

    val h = Hit.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        update(Hit).set(
          h.status -> status
        ).where.eq(h.hitId, this.hitId)
      }.update.apply()
    }
  }

  def updateProcessingStartTime(timestamp: Timestamp): Unit = {
    this.processingStartTime = Some(timestamp)

    val h = Hit.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        update(Hit).set(
          h.processingStartTime -> this.processingStartTime
        ).where.eq(h.hitId, this.hitId)
      }.update.apply()
    }
  }

  def getSelectedLandmark: Option[Landmark] = {
    if (this.selectedLandmarkId.isEmpty)
      return None

    val lm = Landmark.syntax("lm")
    DB readOnly { implicit session: DBSession =>
      withSQL {
        select(lm.result.*)
          .from(Landmark as lm)
          .where
          .eq(lm.landmarkId, this.selectedLandmarkId)
      }.map(Landmark(lm.resultName)).single.apply
    }
  }

  def updateSelectedLandmark(landmarkId: String) = {
    this.selectedLandmarkId = Some(landmarkId)

    val h = Hit.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        update(Hit).set(
          h.selectedLandmarkId -> landmarkId
        ).where.eq(h.hitId, this.hitId)
      }.update.apply()
    }
  }
}

object Hit extends SQLSyntaxSupport[Hit] {
  override val tableName = "Hits"
  implicit val formats = DefaultFormats

  def apply(executionPointId: Int, pipeline: String) = new Hit(0, pipeline, None,
    executionPointId)

  def apply(hit: ResultName[Hit])(rs: WrappedResultSet) = new Hit(rs.int(hit.hitId), rs.string(hit.pipeline),
    rs.stringOpt(hit.selectedLandmarkId), rs.int(hit.executionPointId), rs.stringOpt(hit.saliencyHitId),
    rs.string(hit.status), rs.stringOpt(hit.currentTaskToken), rs.timestampOpt(hit.processingStartTime),
    rs.timestampOpt(hit.processingEndTime))

  /** ********* Query: GET **************/

  def getHit(hitId: Int): Option[Hit] = {
    val hit = Hit.syntax("hit")
    DB readOnly { implicit session: DBSession =>
      withSQL {
        select(hit.result.*)
          .from(Hit as hit)
          .where
          .eq(hit.hitId, hitId)
      }.map(Hit(hit.resultName)).single.apply
    }
  }

  def getHitBySaliencyHitId(hitId: String): Option[Hit] = {
    val hit = Hit.syntax("hit")
    DB readOnly { implicit session: DBSession =>
      withSQL {
        select(hit.result.*)
          .from(Hit as hit)
          .where
          .eq(hit.saliencyHitId, hitId)
      }.map(Hit(hit.resultName)).single.apply
    }
  }

  def getPagedHitsForExecutionPoint(offset: Int, count: Int, executionPoint: Option[String]): List[Hit] = {
    val h = Hit.syntax("h")
    val hits: List[Hit] = DB readOnly { implicit session: DBSession =>
      withSQL {
        select(h.result.*)
          .from(Hit as h)
          .where(sqls.toAndConditionOpt(
            executionPoint.map(ep => sqls.eq(h.executionPointId, ep))
          ))
          .limit(count)
          .offset(offset)
      }.map(Hit(h.resultName)).list.apply
    }
    hits
  }

  def getHitForExecutionPointId(executionPointId: Int,
                                pipeline: String
                             ): Option[Hit] = {
    val h = Hit.syntax("od")
    DB readOnly { implicit session: DBSession =>
      withSQL {
        select(h.result.*)
          .from(Hit as h)
          .where
          .eq(h.executionPointId, executionPointId)
          .and
          .eq(h.pipeline, pipeline)
      }.map(Hit(h.resultName)).single.apply
    }
  }

  def getCount(executionPoint: Option[String]): Int = {
    val hit = Hit.syntax("hit")
    val total = DB readOnly { implicit session: DBSession =>
      withSQL {
        select(count(hit.hitId))
          .from(Hit as hit)
          .where(sqls.toAndConditionOpt(
            executionPoint.map(ep => sqls.eq(hit.executionPointId, ep))
          ))
      }.map(_.int(1)).single.apply.get
    }
    total
  }

  /** ******** Query: UPDATE *******************/
  def updateHitWithTask(hitId: Int, taskToken: String): Unit = {
    val h = Hit.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        update(Hit).set(
          h.currentTaskToken -> taskToken
        ).where.eq(h.hitId, hitId)
      }.update.apply()
    }
  }

  def incrementCost(hitId: Int, amount: Int): Unit = {
    val h = Hit.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        update(Hit).set(
          sqls"${h.cost} = ${h.cost} + $amount"
        ).where.eq(h.hitId, hitId)
      }.update.apply()
    }
  }

  def updateSaliencyHitIdForHit(hitId: Int, saliencyHitId: String) = {
    // Update Hit
    val h = Hit.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        update(Hit).set(
          h.saliencyHitId -> saliencyHitId
        ).where.eq(h.hitId, hitId)
      }.update.apply()
    }
  }

  /** ******** Query: INSERT *******************/
  def insertHit(hit: Hit): Unit = {
    implicit val formats = Serialization.formats(NoTypeHints)

    val h = Hit.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        insert.into(Hit).namedValues(
          h.executionPointId -> hit.executionPointId,
          h.saliencyHitId -> hit.saliencyHitId,
          h.pipeline -> hit.pipeline,
          h.status -> hit.status,
          h.processingStartTime -> hit.processingStartTime
        )
      }.update.apply()
    }
  }
}



