package com.mle.playslick

import com.mle.playslick.db.MyData
import com.mle.util.Log
import play.api.{Application, GlobalSettings}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

/**
 * @author Michael
 */
object Global extends GlobalSettings with Log {
  override def onStart(app: Application): Unit = {
    super.onStart(app)
    val databaseSetup = MyData.init()
    log.info(s"Setting up database...")
    Await.result(databaseSetup, 5.seconds)
    log.info("Setup complete.")
  }
}
