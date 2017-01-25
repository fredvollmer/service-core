package io.torchbearer.ServiceCore.DataModel

import java.sql.Timestamp

import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write
import scalikejdbc._
import scalikejdbc.interpolation.SQLSyntax._
import io.torchbearer.ServiceCore.tyoes.Rectangle
import org.json4s.{NoTypeHints}

/**
  * Created by fredricvollmer on 1/1/17.
  */
  case class SaliencyAssignment(assignmentId: String,
                                hitId: String,
                                duration: Int,
                                rectangle: Option[Rectangle],
                                turkerId: Option[String],
                                dateCreated: Option[Timestamp])

  object SaliencyAssignment extends SQLSyntaxSupport[SaliencyAssignment] {
    override val tableName = "SaliencyAssignments"
    implicit val formats = Serialization.formats(NoTypeHints)

    def apply(os: ResultName[SaliencyAssignment])(rs: WrappedResultSet) = new SaliencyAssignment(
      rs.string(os.assignmentId), rs.string(os.hitId), rs.int(os.duration),
      rs.stringOpt(os.rectangle).map(s => Rectangle.apply(s)), rs.stringOpt(os.turkerId),
      rs.timestampOpt(os.dateCreated))

    /*********** Query: GET **************/

    def getPagedSaliencyAssignments(offset: Int, count: Int, hitId: Option[String]): List[SaliencyAssignment] = {
      val sa = SaliencyAssignment.syntax("sa")
      val hits: List[SaliencyAssignment] = DB readOnly { implicit session: DBSession =>
        withSQL {
          select(sa.result.*)
            .from(SaliencyAssignment as sa)
            .where(sqls.toAndConditionOpt(
              hitId.map(hid => sqls.eq(sa.hitId, hid))
            ))
            .limit(count)
            .offset(offset)
        }.map(SaliencyAssignment(sa.resultName)).list.apply
      }
      hits
    }

    def getCount(hitId: Option[String]): Int = {
      val sa = SaliencyAssignment.syntax("sa")
      val total = DB readOnly { implicit session: DBSession =>
        withSQL {
          select(count(sa.assignmentId))
            .from(SaliencyAssignment as sa)
            .where(sqls.toAndConditionOpt(
              hitId.map(hid => sqls.eq(sa.hitId, hid))
            ))
        }.map(_.int(1)).single.apply.get
      }
      total
    }

    /********** Query: INSERT *******************/
    def insertSaliencyAssignments(assignments: List[SaliencyAssignment]): Unit = {
      val sa = SaliencyAssignment.column
      DB localTx { implicit session: DBSession =>
        assignments.foreach(a => {
          withSQL {
            insert.into(SaliencyAssignment).namedValues(
              sa.assignmentId -> a.assignmentId,
              sa.hitId -> a.hitId,
              sa.duration -> 0,
              sa.rectangle -> write(a.rectangle),
              sa.turkerId -> a.turkerId
            )
          }.update.apply()
        })
      }
    }
}
