package io.torchbearer.ServiceCore.DataModel

import java.sql.Timestamp

import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write
import scalikejdbc._
import scalikejdbc.interpolation.SQLSyntax._
import io.torchbearer.ServiceCore.tyoes.Rectangle
import org.json4s.{DefaultFormats, NoTypeHints}

/**
  * Created by fredricvollmer on 1/1/17.
  */
  case class SaliencyAssignment(assignmentId: String,
                                hitId: Int,
                                duration: Long,
                                atRectangle: Option[Rectangle],
                                justBeforeRectangle: Option[Rectangle],
                                beforeRectangle: Option[Rectangle],
                                turkerId: Option[String],
                                dateCreated: Option[Timestamp])

  object SaliencyAssignment extends SQLSyntaxSupport[SaliencyAssignment] {
    override val tableName = "SaliencyAssignments"

    def apply(os: ResultName[SaliencyAssignment])(rs: WrappedResultSet) = new SaliencyAssignment(
      rs.string(os.assignmentId), rs.int(os.hitId), rs.int(os.duration),
      rs.stringOpt(os.atRectangle).map(s => Rectangle.apply(s)), rs.stringOpt(os.justBeforeRectangle).map(s => Rectangle.apply(s)),
      rs.stringOpt(os.beforeRectangle).map(s => Rectangle.apply(s)), rs.stringOpt(os.turkerId),
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
      implicit val formats = Serialization.formats(NoTypeHints)

      val sa = SaliencyAssignment.column
      DB localTx { implicit session: DBSession =>
        assignments.foreach(a => {
          withSQL {
            insert.into(SaliencyAssignment).namedValues(
              sa.assignmentId -> a.assignmentId,
              sa.hitId -> a.hitId,
              sa.duration -> 0,
              sa.atRectangle -> a.atRectangle.map(r => write(r)).orNull,
              sa.justBeforeRectangle -> a.justBeforeRectangle.map(r => write(r)).orNull,
              sa.beforeRectangle -> a.beforeRectangle.map(r => write(r)).orNull,
              sa.turkerId -> a.turkerId
            ).append(
              sqls"ON DUPLICATE KEY UPDATE ${sa.assignmentId}=${sa.assignmentId}"
            )
          }.update.apply()
        })
      }
    }
}
