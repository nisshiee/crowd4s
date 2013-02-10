package org.nisshiee.crowd4s

import com.typesafe.config.{ Config => TConfig, ConfigFactory }
import scala.util.control.Exception.allCatch

object Config {

  implicit lazy val connection: CrowdConnection =
    connectionOpt getOrElse CrowdConnection("http://input.crowd.url.prefix", "input-appname", "input-password")

  def connectionOpt: Option[CrowdConnection] = for {
    config <- allCatch opt ConfigFactory.load()
    urlPrefix <- allCatch opt config.getString("crowd4s.urlPrefix")
    appname <- allCatch opt config.getString("crowd4s.appname")
    password <- allCatch opt config.getString("crowd4s.password")
  } yield CrowdConnection(urlPrefix, appname, password)
}
