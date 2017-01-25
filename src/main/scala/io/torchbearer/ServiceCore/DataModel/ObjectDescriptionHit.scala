package io.torchbearer.ServiceCore.DataModel

import java.sql.Timestamp

import scalikejdbc._
import scalikejdbc.interpolation.SQLSyntax._

/**
  * Created by fredricvollmer on 11/9/16.
  */

case class ObjectDescriptionHit(hitId: String,
                                executionPoint: String,
                                saliencyHit: Either[Option[String], Option[SaliencyHit]],
                                amount: Int,
                                questionVersion: String,
                                description: Option[String],
                                dateCreated: Timestamp)

object ObjectDescriptionHit extends SQLSyntaxSupport[ObjectDescriptionHit] {
  override val tableName = "ObjectDescriptionHits"

  def apply(od: ResultName[ObjectDescriptionHit])(rs: WrappedResultSet) = new ObjectDescriptionHit(
    rs.string(od.hitId), rs.string(od.executionPoint), Left(rs.stringOpt(od.saliencyHit)), rs.int(od.amount),
    rs.string(od.questionVersion), rs.stringOpt(od.description), rs.timestamp(od.dateCreated))

  /*********** Query: GET **************/

  def getDescriptionHit(hitId: String): Option[ObjectDescriptionHit] = {
    val od = ObjectDescriptionHit.syntax("od")
    val hit: Option[ObjectDescriptionHit] = DB readOnly { implicit session: DBSession =>
      withSQL {
        select(od.result.*)
          .from(ObjectDescriptionHit as od)
          .where
          .eq(od.hitId, hitId)
      }.map(ObjectDescriptionHit(od.resultName)).single.apply
    }
    hit
  }

  def getPagedDescriptionHits(offset: Int, count: Int, executionPoint: Option[String]): List[ObjectDescriptionHit] = {
    val od = ObjectDescriptionHit.syntax("od")
    val hits: List[ObjectDescriptionHit] = DB readOnly { implicit session: DBSession =>
      withSQL {
        select(od.result.*)
          .from(ObjectDescriptionHit as od)
          .where(sqls.toAndConditionOpt(
            executionPoint.map(ep => sqls.eq(od.executionPoint, ep))
          ))
          .limit(count)
          .offset(offset)
      }.map(ObjectDescriptionHit(od.resultName)).list.apply
    }
    hits
  }

  def getCount(executionPoint: Option[String]): Int = {
    val dh = ObjectDescriptionHit.syntax("dh")
    val total = DB readOnly { implicit session: DBSession =>
      withSQL {
        select(count(dh.hitId))
          .from(ObjectDescriptionHit as dh)
          .where(sqls.toAndConditionOpt(
            executionPoint.map(ep => sqls.eq(dh.executionPoint, ep))
          ))
      }.map(_.int(1)).single.apply.get
    }
    total
  }

  /********** Query: INSERT *******************/
}



