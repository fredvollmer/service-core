package io.torchbearer.ServiceCore.DataModel

import java.sql.Timestamp

import io.torchbearer.ServiceCore.tyoes.Rectangle
import scalikejdbc._
import scalikejdbc.interpolation.SQLSyntax._

case class SaliencyHit(hitId: String,
                       executionPoint: String,
                       descriptionHit: Either[Option[String], Option[ObjectDescriptionHit]],
                       amount: Int,
                       questionVersion: String,
                       rectangle: Option[Rectangle],
                       dateCreated: Timestamp)

object SaliencyHit extends SQLSyntaxSupport[SaliencyHit] {
  override val tableName = "SaliencyHits"

  def apply(os: ResultName[SaliencyHit])(rs: WrappedResultSet) = new SaliencyHit(
    rs.string(os.hitId), rs.string(os.executionPoint), Left(rs.stringOpt(os.descriptionHit)), rs.int(os.amount),
    rs.string(os.questionVersion), rs.stringOpt(os.rectangle).map(s => Rectangle.apply(s)),
    rs.timestamp(os.dateCreated))

  /*********** Query: GET **************/

  def getSaliencyHit(hitId: String): Option[SaliencyHit] = {
      val os = SaliencyHit.syntax("os")
      val hit: Option[SaliencyHit] = DB readOnly { implicit session: DBSession =>
        withSQL {
          select(os.result.*)
            .from(SaliencyHit as os)
            .where
            .eq(os.hitId, hitId)
        }.map(SaliencyHit(os.resultName)).single.apply
      }
      hit
  }

  def getPagedSaliencyHits(offset: Int, count: Int, executionPoint: Option[String]): List[SaliencyHit] = {
    val os = SaliencyHit.syntax("os")
    val hits: List[SaliencyHit] = DB readOnly { implicit session: DBSession =>
      withSQL {
        select(os.result.*)
          .from(SaliencyHit as os)
          .where(sqls.toAndConditionOpt(
            executionPoint.map(ep => sqls.eq(os.executionPoint, ep))
          ))
          .limit(count)
          .offset(offset)
      }.map(SaliencyHit(os.resultName)).list.apply
    }
    hits
  }

  def getCount(executionPoint: Option[String]): Int = {
    val os = SaliencyHit.syntax("os")
    val total = DB readOnly { implicit session: DBSession =>
      withSQL {
        select(count(os.hitId))
          .from(SaliencyHit as os)
          .where(sqls.toAndConditionOpt(
            executionPoint.map(ep => sqls.eq(os.executionPoint, ep))
          ))
      }.map(_.int(1)).single.apply.get
    }
    total
  }

  /********** Query: INSERT *******************/

}





