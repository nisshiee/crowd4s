package org.nisshiee.crowd4s

import org.specs2._, matcher.DataTables
import scalaz._, Scalaz._
import org.json4s._

class GetGroupSpec extends Specification with DataTables { def is =

  "GetGoup"                                                                     ^
    "parseGroup"                                                                ^
      "valid json is parsed to Success(Group)"                                  ! e1^
      "invalid json is parsed to Failure(JsonParseError)"                       ! e2^
                                                                                p^
    "parseResponse"                                                             ^
      "known status code"                                                       ! e3^
      "unknown status code"                                                     ! e4^
                                                                                end

  import GetGroup._

  val validGroupJson = """{"expand":"attributes","link":{"href":"http://example.com/rest/usermanagement/1/group?groupname=hogegroup","rel":"self"},"name":"hogegroup","description":"hogehoge","type":"GROUP","active":true,"attributes":{"attributes":[],"link":{"href":"http://example.com/crowd/rest/usermanagement/1/group/attribute?groupname=hogegroup","rel":"self"}}}"""

  val validGroup = Group("hogegroup", "hogehoge", true)

  val invalidGroupJson = """{"expand":"attributes","link":{"href":"http://example.com/rest/usermanagement/1/group?groupname=hogegroup","rel":"self"},"name":"hogegroup","type":"GROUP","active":true,"attributes":{"attributes":[],"link":{"href":"http://example.com/crowd/rest/usermanagement/1/group/attribute?groupname=hogegroup","rel":"self"}}}"""

  def e1 = parseGroup(validGroupJson).toOption must beSome.which(validGroup ==)

  def e2 =
    parseGroup(invalidGroupJson).toEither must beLeft.like {
      case JsonParseError => ok
      case _ => ko
    }

  val notFoundJson = """{"reason":"GROUP_NOT_FOUND","message":"Group <hoge> does not exist"}"""
  val notFound = NotFound("GROUP_NOT_FOUND", "Group <hoge> does not exist")

  def e3 =
    "response"            | "result"             |
    (200, validGroupJson) ! validGroup.success   |
    (401, "hoge")         ! Unauthorized.failure |
    (403, "hoge")         ! Forbidden.failure    |
    (404, notFoundJson)   ! notFound.failure     |> { (response, result) =>
      parseResponse(response) must equalTo(result)
    }

  def e4 =
    "statusCode" | "reulst"             |
    201          ! UnknownError.failure |
    202          ! UnknownError.failure |
    203          ! UnknownError.failure |
    400          ! UnknownError.failure |
    402          ! UnknownError.failure |
    405          ! UnknownError.failure |
    500          ! UnknownError.failure |
    503          ! UnknownError.failure |> { (code, result) =>
      parseResponse(code -> "hoge") must equalTo(result)
    }
}
