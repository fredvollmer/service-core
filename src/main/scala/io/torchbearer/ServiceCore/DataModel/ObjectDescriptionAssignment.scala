package io.torchbearer.ServiceCore.DataModel

import java.sql.Timestamp

import scalikejdbc._
import scalikejdbc.interpolation.SQLSyntax._
import io.torchbearer.ServiceCore.tyoes.Rectangle

/**
  * Created by fredricvollmer on 1/1/17.
  */
case class ObjectDescriptionAssignment(assignmentId: String,
                                       hitId: String,
                                       duration: Int,
                                       description: Option[String],
                                       turkerId: Option[String],
                                       dateCreated: Option[Timestamp])

object ObjectDescriptionAssignment extends SQLSyntaxSupport[ObjectDescriptionAssignment] {
  override val tableName = "ObjectDescriptionAssignments"

  def apply(os: ResultName[ObjectDescriptionAssignment])(rs: WrappedResultSet) = new ObjectDescriptionAssignment(
    rs.string(os.assignmentId), rs.string(os.hitId), rs.int(os.duration),
    rs.stringOpt(os.description), rs.stringOpt(os.turkerId), rs.timestampOpt(os.dateCreated))

  /*********** Query: GET **************/

  def getPagedObjectDescriptionAssignments(offset: Int, count: Int, hitId: Option[String]): List[ObjectDescriptionAssignment] = {
    val sa = ObjectDescriptionAssignment.syntax("sa")
    val hits: List[ObjectDescriptionAssignment] = DB readOnly { implicit session: DBSession =>
      withSQL {
        select(sa.result.*)
          .from(ObjectDescriptionAssignment as sa)
          .where(sqls.toAndConditionOpt(
            hitId.map(hid => sqls.eq(sa.hitId, hid))
          ))
          .limit(count)
          .offset(offset)
      }.map(ObjectDescriptionAssignment(sa.resultName)).list.apply
    }
    hits
  }

  def getCount(hitId: Option[String]): Int = {
    val sa = ObjectDescriptionAssignment.syntax("sa")
    val total = DB readOnly { implicit session: DBSession =>
      withSQL {
        select(count(sa.assignmentId))
          .from(ObjectDescriptionAssignment as sa)
          .where(sqls.toAndConditionOpt(
            hitId.map(hid => sqls.eq(sa.hitId, hid))
          ))
      }.map(_.int(1)).single.apply.get
    }
    total
  }

  /********** Query: INSERT *******************/

}
