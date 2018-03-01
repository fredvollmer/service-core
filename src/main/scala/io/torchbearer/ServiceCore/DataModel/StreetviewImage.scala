package io.torchbearer.ServiceCore.DataModel

import org.json4s.DefaultFormats
import scalikejdbc._
import io.torchbearer.ServiceCore.Extensions.MySQLSyntaxSupport._

/**
  * Created by fredricvollmer on 11/12/17.
  */
case class StreetviewImage(
                          executionPointId: Int,
                          position: String,
                          latitude: Double,
                          longitude: Double
                          ) {
  def add(): Unit = {
    val si = StreetviewImage.column
    DB localTx { implicit session: DBSession =>
      withSQL {
        insert.into(StreetviewImage).namedValues(
          si.executionPointId -> this.executionPointId,
          si.position -> this.position,
          si.latitude -> this.latitude,
          si.longitude -> this.longitude
        ).onDuplicateKeyUpdate(
          si.latitude -> sqls.values(si.latitude),
          si.longitude -> sqls.values(si.longitude)
        )
      }.update.apply()
    }
  }
}

object StreetviewImage extends SQLSyntaxSupport[StreetviewImage] {

  override val tableName = "StreetviewImages"
  implicit val formats = DefaultFormats

  def apply(si: ResultName[StreetviewImage])(rs: WrappedResultSet): StreetviewImage = StreetviewImage(rs.int(si.executionPointId),
    rs.string(si.position), rs.double(si.latitude), rs.double(si.longitude))

  def getStreetviewImagesForExecutionPoint(executionPointId: Int, position: Option[String] = None): List[StreetviewImage] = {
    val si = StreetviewImage.syntax("si")
    val query = select(si.result.*)
      .from(StreetviewImage as si)
      .where
      .eq(si.executionPointId, executionPointId)
      .map {sql: ConditionSQLBuilder[StreetviewImage] =>
        if (position.isDefined) sql.and.eq(si.position, position.get) else sql
      }

    DB readOnly { implicit session: DBSession =>
      withSQL {
        query
      }.map(StreetviewImage(si.resultName)).list.apply
    }
  }
}