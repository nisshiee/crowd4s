package org.nisshiee.crowd4s

import scalaz._, Scalaz._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.util.control.Exception.allCatch

trait GetGroup {

  import GetGroup._

  def getGroup(groupname: String)(implicit conn: CrowdConnection, http: CrowdHttp) =
    http.get(path, Map("groupname" -> groupname)) flatMap parseResponse
}

object GetGroup {

  import ResponseParseHelper._

  val path = "/rest/usermanagement/1/group.json"

  def parseResponse = parseBasicGetResponse(parseGroup)

  def parseGroup(json: String): Validation[JsonParseError, Group] = {
    implicit val formats = DefaultFormats
    allCatch opt parse(json).extract[Group] toSuccess JsonParseError
  }
}
