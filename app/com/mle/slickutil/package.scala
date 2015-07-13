package com.mle

import slick.dbio.Effect.{Read, Schema}
import slick.driver.H2Driver.api._
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author Michael
 */
package object slickutil {

  implicit class RichTable[T <: Table[_]](table: TableQuery[T]) {
    def tableExists: DBIOAction[Boolean, NoStream, Read] = MTable.getTables(table.baseTableRow.tableName).map(_.nonEmpty)

    def createIfNotExists: DBIOAction[Unit, NoStream, Read with Schema] =
      table.tableExists.flatMap(exists => DBIO.seq((if (exists) Nil else Seq(table.schema.create)): _*))
  }

}
