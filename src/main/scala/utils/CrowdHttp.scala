package org.nisshiee.crowd4s

import dispatch._
import com.ning.http.client.Response
import scalaz._, Scalaz._
import scala.util.control.Exception.allCatch
import org.json4s._
import org.json4s.jackson.JsonMethods._

trait CrowdHttp {

  def get
    (path: String, params: Map[String, String])
    (implicit conn: CrowdConnection)
    : Validation[ConnectionError, (Int, String)]

  def postJson
    (path: String, params: Map[String, String], body: JValue)
    (implicit conn: CrowdConnection)
    : Validation[ConnectionError, (Int, String)]
}

object CrowdHttp extends CrowdHttp {

  override def get
    (path: String, params: Map[String, String])
    (implicit conn: CrowdConnection)
    : Validation[ConnectionError, (Int, String)] = {
      val req = url(conn.urlPrefix + path) |>
        (_ as (conn.appname, conn.password)) |>
        (_ <<? params)

      allCatch opt { Http(req > { res: Response =>
        (res.getStatusCode, res.getResponseBody)
      })() } toSuccess ConnectionError
    }

  override def postJson
    (path: String, params: Map[String, String], body: JValue)
    (implicit conn: CrowdConnection)
    : Validation[ConnectionError, (Int, String)] = {
      val req = url(conn.urlPrefix + path) |>
        (_ as (conn.appname, conn.password)) |>
        (_ <:< List("Content-Type" -> "application/json")) |>
        (_ <<? params) |>
        (_ << compact(body))

      allCatch opt { Http(req > { res: Response =>
        (res.getStatusCode, res.getResponseBody)
      })() } toSuccess ConnectionError
    }
}
