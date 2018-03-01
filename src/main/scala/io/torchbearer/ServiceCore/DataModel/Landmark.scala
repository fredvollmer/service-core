package io.torchbearer.ServiceCore.DataModel

import java.sql.Timestamp

import io.torchbearer.ServiceCore.tyoes.{Description, Rectangle}
import org.json4s.{DefaultFormats, NoTypeHints}
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import scalikejdbc._
import scalikejdbc.interpolation.SQLSyntax._
import java.util.UUID

/**
  * Created by fredricvollmer on 11/9/16.
  */

object LandmarkStatus extends Enumeration {
  type LandmarkStatusType = Value

  val UNKNOWN = Value("UNKNOWN")
  val PENDING = Value("PENDING")
  val FAILED = Value("FAILED")
  val VERIFIED = Value("VERIFIED")

  implicit val intParameterBinderFactory: ParameterBinderFactory[LandmarkStatusType] = ParameterBinderFactory {
    value => (stmt, idx) => stmt.setString(idx, value.toString)
  }
}

case class Landmark(landmarkId: String,
                    hitId: Int,
                    var description: Option[String] = None,
                    var color: Option[List[String]] = None,
                    var position: Option[String] = None,
                    var computedDescription: Option[String] = None,
                    var rect: Option[Rectangle] = None,
                    var relativeBearing: Option[Int] = None,
                    var visualSaliencyScore: Double = 0,
                    var structuralSaliencyScore: Double = 0,
                    var semanticSaliencyScore: Double = 0,
                    var status: LandmarkStatus.LandmarkStatusType = LandmarkStatus.UNKNOWN,
                    var turkDescriptionAttempts: Int = 0,
                    descriptionHitId: Option[String] = None,
                    verificationHitId: Option[String] = None
                   ) {

  def updateComputedRectangle(rect: Rectangle): Unit = {
    implicit val formats = Serialization.formats(NoTypeHints)

    this.rect = Some(rect)

    val lm = Landmark.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        update(Landmark).set(
          lm.rect -> write(this.rect)
        ).where.eq(lm.landmarkId, this.landmarkId)
      }.update.apply()
    }
  }

  def updateStructuralSaliencyScore(score: Double): Unit = {
    this.structuralSaliencyScore = score

    val lm = Landmark.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        update(Landmark).set(
          lm.structuralSaliencyScore -> this.structuralSaliencyScore
        ).where.eq(lm.landmarkId, this.landmarkId)
      }.update.apply()
    }
  }

  def updateDescription(description: String): Unit = {
    implicit val formats = Serialization.formats(NoTypeHints)

    this.description = Some(description)

    val lm = Landmark.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        update(Landmark).set(
          lm.description -> this.description
        ).where.eq(lm.landmarkId, this.landmarkId)
      }.update.apply()
    }
  }

  def updateComputedDescription(description: String): Unit = {
    this.computedDescription = Some(description)

    val lm = Landmark.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        update(Landmark).set(
          lm.computedDescription -> this.computedDescription
        ).where.eq(lm.landmarkId, this.landmarkId)
      }.update.apply()
    }
  }

  def updateStatus(status: LandmarkStatus.LandmarkStatusType): Unit = {
    this.status = status

    val lm = Landmark.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        update(Landmark).set(
          lm.status -> this.status
        ).where.eq(lm.landmarkId, this.landmarkId)
      }.update.apply()
    }
  }

  def incrementDescriptionAttempts(byAmount: Int): Unit = {
    this.turkDescriptionAttempts += byAmount

    val lm = Landmark.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        update(Landmark).set(
          sqls"${lm.turkDescriptionAttempts} = ${lm.turkDescriptionAttempts} + $byAmount"
        ).where.eq(lm.landmarkId, this.landmarkId)
      }.update.apply()
    }
  }

  def buildInstructionWithString(instructionString: String): String = {
    if (computedDescription.isEmpty) {
      instructionString
    }
    else {
      s"At the ${computedDescription.get}, $instructionString."
    }
  }
}

object Landmark extends SQLSyntaxSupport[Landmark] {
  override val tableName = "Landmarks"
  implicit val formats = DefaultFormats

  def apply(l: ResultName[Landmark])(rs: WrappedResultSet) = new Landmark(rs.string(l.landmarkId),
    rs.int(l.hitId), rs.stringOpt(l.description),
    rs.stringOpt(l.color).map(s => parse(s).extract[List[String]]), rs.stringOpt(l.position), rs.stringOpt(l.computedDescription),
    rs.stringOpt(l.rect).map(s => Rectangle(s)), rs.intOpt(l.relativeBearing),
    rs.doubleOpt(l.visualSaliencyScore) getOrElse -1, rs.doubleOpt(l.structuralSaliencyScore) getOrElse -1,
    rs.doubleOpt(l.semanticSaliencyScore) getOrElse -1, LandmarkStatus.withName(rs.string(l.status)), rs.int(l.turkDescriptionAttempts),
    rs.stringOpt(l.descriptionHitId), rs.stringOpt(l.verificationHitId))

  def apply(hitId: Int) = new Landmark(UUID.randomUUID().toString, hitId)

  /** ********* Query: GET **************/
  def getLandmarksForHit(hitId: Int, statuses: Option[Seq[LandmarkStatus.LandmarkStatusType]] = None): List[Landmark] = {
    val lm = Landmark.syntax("l")
    val landmarks: List[Landmark] = DB readOnly { implicit session: DBSession =>
      withSQL {
        select(lm.result.*)
          .from(Landmark as lm)
          .where(sqls.toAndConditionOpt(
            statuses.map(s => sqls.in(lm.status, s)),
            Option(sqls.eq(lm.hitId, hitId))
          ))
      }.map(Landmark(lm.resultName)).list.apply
    }
    landmarks
  }

  def getLandmarkByDescriptionHitId(descriptionHitId: String): Option[Landmark] = {
    val lm = Landmark.syntax("l")
    DB readOnly { implicit session: DBSession =>
      withSQL {
        select(lm.result.*)
          .from(Landmark as lm)
          .where
          .eq(lm.descriptionHitId, descriptionHitId)
      }.map(Landmark(lm.resultName)).single.apply
    }
  }

  def getLandmarkByVerificationHitId(verificationHitId: String): Option[Landmark] = {
    val lm = Landmark.syntax("l")
    DB readOnly { implicit session: DBSession =>
      withSQL {
        select(lm.result.*)
          .from(Landmark as lm)
          .where
          .eq(lm.verificationHitId, verificationHitId)
      }.map(Landmark(lm.resultName)).single.apply
    }
  }

  /** ********* UPDATE **************/
  def updateDescriptionHitIdForLandmark(landmarkId: String, descriptionHitId: String): Unit = {
    // Update Landmark
    val lm = Landmark.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        update(Landmark).set(
          lm.descriptionHitId -> descriptionHitId
        ).where.eq(lm.landmarkId, landmarkId)
      }.update.apply()
    }
  }

  def updateVerificationHitIdForLandmark(landmarkId: String, verificationHitId: String): Unit = {
    // Update Landmark
    val lm = Landmark.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        update(Landmark).set(
          lm.verificationHitId -> verificationHitId
        ).where.eq(lm.landmarkId, landmarkId)
      }.update.apply()
    }
  }

  /** ********* INSERT ****************/
  def insertLandmark(hitId: Int, rect: Option[Rectangle] = None, saliencyScore: Option[Double] = None, relativeBearing: Option[Int] = None): Unit = {
    implicit val formats = Serialization.formats(NoTypeHints)
    val landmarkId = java.util.UUID.randomUUID.toString

    val lm = Landmark.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        insert.into(Landmark).namedValues(
          lm.landmarkId -> landmarkId,
          lm.hitId -> hitId,
          lm.rect -> write(rect),
          lm.visualSaliencyScore -> saliencyScore,
          lm.relativeBearing -> relativeBearing
        )
      }.update.apply()
    }
  }

  def insertLandmarks(landmarks: List[Landmark]): Unit = {
    implicit val formats = Serialization.formats(NoTypeHints)
    val lm = Landmark.column
    DB localTx { implicit session: DBSession =>
      landmarks.foreach(landmark => {
        withSQL {
          insert.into(Landmark).namedValues(
            lm.landmarkId -> landmark.landmarkId,
            lm.hitId -> landmark.hitId,
            lm.rect -> write(landmark.rect),
            lm.relativeBearing -> landmark.relativeBearing,
            lm.description -> landmark.description,
            lm.color -> write(landmark.color),
            lm.position -> landmark.position,
            lm.computedDescription -> landmark.computedDescription,
            lm.visualSaliencyScore -> landmark.visualSaliencyScore,
            lm.structuralSaliencyScore -> landmark.structuralSaliencyScore,
            lm.semanticSaliencyScore -> landmark.semanticSaliencyScore,
            lm.status -> landmark.status,
            lm.descriptionHitId -> landmark.descriptionHitId,
            lm.verificationHitId -> landmark.verificationHitId
          )
        }.update.apply()
      })
    }
  }
}