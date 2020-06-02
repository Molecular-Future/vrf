package org.mos.vrfblk.utils

import onight.oapi.scala.traits.OLog

trait SRunner extends Runnable with OLog {

  def runOnce()

  def getName(): String

  def run() = {
    val oldname = Thread.currentThread().getName;
    Thread.currentThread().setName(getName());
//          log.debug(getName() + ": ----------- [START]")

    try {
      runOnce()
    } catch {
      case e: Throwable =>
        log.debug(getName() + ":  ----------- Error", e);
    } finally {
//      log.debug(getName() + ":  ----------- [END]")
      Thread.currentThread().setName(oldname + "");
    }
  }
}