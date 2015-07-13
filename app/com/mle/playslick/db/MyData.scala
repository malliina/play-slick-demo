package com.mle.playslick.db

import com.mle.slickutil.RichTable
import org.h2.jdbcx.JdbcConnectionPool
import slick.dbio.{NoStream, DBIOAction}
import slick.driver.H2Driver.api._

import scala.concurrent.Future

case class MyRow(id: Int, name: String)

class MyTable(tag: Tag) extends Table[MyRow](tag, "rows") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

  def name = column[String]("NAME")

  def * = (id, name) <>((MyRow.apply _).tupled, MyRow.unapply)
}

/**
 * @author Michael
 */
trait MyData {
  val databaseName = "playslick"
  val url = s"jdbc:h2:mem:$databaseName;DB_CLOSE_DELAY=-1"
  val pool = JdbcConnectionPool.create(url, "", "")
  val database = Database.forDataSource(pool)

  val myTable = TableQuery[MyTable]
  val tables = Seq(myTable)

  val setupActions = tables.map(_.createIfNotExists) ++ Seq(myTable ++= Seq(MyRow(1, "hey"), MyRow(2, "you")))
  val setup = DBIO.seq(setupActions: _*)

  def init(): Future[Unit] = database.run(setup)
}

object MyData extends MyData {

  def run[R](a: DBIOAction[R, NoStream, Nothing]): Future[R] = database.run(a)

  def rows: Future[Seq[MyRow]] = run(myTable.result)

  def remove(id: Int) = run(myTable.filter(_.id === id).delete)

  def add(row: MyRow) = run(myTable += row)
}
