package org.nisshiee.crowd4s

import scalaz._, Scalaz._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.util.control.Exception.allCatch

trait GetGroupList {

  import GetGroupList._

  def getDirectGroupList(username: String)(implicit conn: CrowdConnection) =
    CrowdHttp.get(directPath, Map("username" -> username)) flatMap parseResponse

  def getNestedGroupList(username: String)(implicit conn: CrowdConnection) =
    CrowdHttp.get(nestedPath, Map("username" -> username)) flatMap parseResponse
}

object GetGroupList {

  import ResponseParseHelper._

  val directPath = "/rest/usermanagement/1/user/group/direct.json"
  val nestedPath = "/rest/usermanagement/1/user/group/nested.json"

  def parseResponse = parseBasicGetResponse(parseGroupList)

  def parseGroupList(json: String): Validation[JsonParseError, Seq[String]] = {
    implicit val formats = DefaultFormats
    allCatch opt {
      parse(json) \ "groups" \ "name" |> (_.extract[List[String]])
    } toSuccess JsonParseError
  }
}
