package io.torchbearer.ServiceCore

import scalikejdbc.config._
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import io.torchbearer.ServiceCore.DataModel._

/**
  * Created by fredricvollmer on 11/1/16.
  */
object TorchbearerDB {
  val logger = LoggerFactory.getLogger(this.getClass)

  def init() = {
    println("Initializing TorchbearerDB...")

    DBs.setupAll()

    println("TorchbearerDB initialized")
  }

  // Query functions
  /************ Execution Points ************/
}
