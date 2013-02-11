package org.nisshiee.crowd4s

import org.specs2._, matcher.DataTables
import scalaz._, Scalaz._

class ResponseParseHelperSpec extends Specification with DataTables { def is =

  "ResponseParseHelper"                                                         ^
    "parseNotFound"                                                             ^
      "valid json is parsed to Failure(NotFound)"                               ! e1^
      "invaild json is parsed to Failure(UnknownError)"                         ! e2^
                                                                                p^
    "parseBasicGetResponse"                                                     ^
      "known status code"                                                       ! e3^
      "unknown status code"                                                     ! e4^
                                                                                end

  import ResponseParseHelper._

  val notFoundJson = """{"reason":"USER_NOT_FOUND","message":"User <hoge> does not exist"}"""
  val notFound = NotFound("USER_NOT_FOUND", "User <hoge> does not exist")

  def e1 = {
    parseNotFound(notFoundJson).toEither must beLeft.like {
      case NotFound(reason, message) =>
        (reason must equalTo("USER_NOT_FOUND")) and
          (message must equalTo("User <hoge> does not exist"))
    }
  }

  def e2 = {
    val json = """{"reason":"USER_NOT_FOUND"}"""
    parseNotFound(json).toEither must beLeft.like {
      case UnknownError => ok
      case _ => ko
    }
  }

  def parseDummy: String => Validation[JsonParseError, Boolean] = _ => true.success

  def e3 =
    "response"          | "result"             |
    (200, "dummy")      ! true.success    |
    (401, "hoge")       ! Unauthorized.failure |
    (403, "hoge")       ! Forbidden.failure    |
    (404, notFoundJson) ! notFound.failure     |> { (response, result) =>
      parseBasicGetResponse(parseDummy)(response) must equalTo(result)
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
      parseBasicGetResponse(parseDummy)(code -> "hoge") must equalTo(result)
    }
}
