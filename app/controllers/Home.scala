package controllers

import com.mle.concurrent.FutureOps
import com.mle.playslick.db.{MyData, MyRow}
import play.api.data.Form
import play.api.data.Forms.nonEmptyText
import play.api.mvc._
import views.html

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 *
 * @author mle
 */
object Home extends Controller {
  val FEEDBACK = "feedback"
  val SEVERITY = "severity"
  val NAME = "name"
  val DANGER = "danger"
  val SUCCESS = "success"
  val db = MyData

  val form = Form[String](NAME -> nonEmptyText)

  def allRows = action(implicit req => {
    dbResult(db.rows)(
      rows => okIndex(rows),
      Redirect(routes.Home.error()).flashing(FEEDBACK -> "Unable to read database", SEVERITY -> DANGER))
  })

  def error = Action(implicit req => okIndex(Nil))

  def add = action(implicit req => {
    form.bindFromRequest().fold(
      errors => dbRead(rows => BadRequest(html.index(rows, errors))),
      ok => dbResult(db.add(MyRow(1, ok)))(
        _ => redir("Added item", SUCCESS),
        redir("Unable to add item", DANGER)
      )
    )
  })

  def remove(row: Int): Action[AnyContent] = action(implicit req => {
    dbResult(db.remove(row))(
      _ => redir(s"Removed item $row", SUCCESS),
      redir(s"Unable to delete item $row", DANGER)
    )
  })

  private def dbRead(rows: Seq[MyRow] => Result) = dbResult(db.rows)(
    rows,
    Redirect(routes.Home.error()).flashing(FEEDBACK -> "Unable to read database", SEVERITY -> DANGER))

  protected def okIndex(rows: Seq[MyRow])(implicit req: RequestHeader) = Ok(html.index(rows, form))

  protected def redir(feedback: String, severity: String) =
    Redirect(routes.Home.allRows()).flashing(FEEDBACK -> feedback, SEVERITY -> severity)

  protected def dbResult[T](dbAction: Future[T])(ok: T => Result, fail: Result) = dbAction.map(ok).recoverAll(_ => fail)

  protected def action(result: Request[AnyContent] => Future[Result]) = Action.async(req => result(req))
}
