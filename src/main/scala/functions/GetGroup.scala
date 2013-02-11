package org.nisshiee.crowd4s

import scalaz._, Scalaz._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.util.control.Exception.allCatch

trait GetGroup {

  import GetGroup._

  def getGroup(groupname: String)(implicit conn: CrowdConnection) =
    CrowdHttp.get(path, Map("groupname" -> groupname)) flatMap parseResponse
}

object GetGroup {

  import ResponseParseHelper._

  val path = "/rest/usermanagement/1/group.json"

  def parseResponse: ((Int, String)) => Validation[RequestError, Group] = {
    case (200, json) => parseGroup(json)
    case (401, _) => Unauthorized.failure
    case (403, _) => Forbidden.failure
    case (404, json) => parseNotFound(json)
    case _ => UnknownError.failure
  }

  def parseGroup(json: String): Validation[JsonParseError, Group] = {
    implicit val formats = DefaultFormats
    allCatch opt parse(json).extract[Group] toSuccess JsonParseError
  }
}
