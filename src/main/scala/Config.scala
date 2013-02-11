package org.nisshiee.crowd4s

import com.typesafe.config.{ Config => TConfig, ConfigFactory }
import scala.util.control.Exception.allCatch
import scala.collection.JavaConverters._

object Config {

  implicit lazy val connection: CrowdConnection =
    connectionOpt getOrElse CrowdConnection("http://input.crowd.url.prefix", "input-appname", "input-password")

  implicit lazy val authorizedGroups: Seq[String] =
    authorizedGroupsOpt getOrElse Seq()

  def connectionOpt: Option[CrowdConnection] = for {
    config <- allCatch opt ConfigFactory.load()
    urlPrefix <- allCatch opt config.getString("crowd4s.urlPrefix")
    appname <- allCatch opt config.getString("crowd4s.appname")
    password <- allCatch opt config.getString("crowd4s.password")
  } yield CrowdConnection(urlPrefix, appname, password)

  def authorizedGroupsOpt: Option[Seq[String]] = for {
    config <- allCatch opt ConfigFactory.load()
    authorizedGroups <- allCatch opt {
      config.getStringList("crowd4s.authorizedGroups")
        .asScala.toList
    }
  } yield authorizedGroups

}
