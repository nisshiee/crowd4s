package org.nisshiee.crowd4s

import dispatch._
import com.ning.http.client.Response
import scalaz._, Scalaz._
import scala.util.control.Exception.allCatch

object CrowdHttp {

  def get(path: String, params: Map[String, String])(implicit conn: CrowdConnection) = {
    val req = url(conn.urlPrefix + path) |>
      (_ as (conn.appname, conn.password)) |>
      (_ <<? params)

    allCatch opt { Http(req > { res: Response =>
      (res.getStatusCode, res.getResponseBody)
    })() } toSuccess ConnectionError
  }
}
