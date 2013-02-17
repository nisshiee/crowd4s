package org.nisshiee.crowd4s

import org.specs2._, matcher.DataTables
import scalaz._, Scalaz._

class GetUserSpec extends Specification with DataTables { def is =

  "GetUser"                                                                     ^
    "parseResponse"                                                             ^
      "known status code"                                                       ! e1^
      "unknown status code"                                                     ! e2^
                                                                                p^
    "getUser"                                                                   ^
      "if user exists"                                                          ! e3^
      "if user doesn't exist"                                                   ! e4^
                                                                                end

  import GetUser._


  val validUserJson = """{"expand":"attributes","link":{"href":"https://example.com/crowd/rest/usermanagement/1/user?username=hoge","rel":"self"},"name":"hoge","first-name":"Taro","last-name":"Hoge","display-name":"Taro Hoge","email":"hoge@example.com","password":{"link":{"href":"https://example.com/crowd/rest/usermanagement/1/user/password?username=hoge","rel":"edit"}},"active":true,"attributes":{"attributes":[],"link":{"href":"https://example.com/crowd/rest/usermanagement/1/user/attribute?username=hoge","rel":"self"}}}"""


  val validUser = User("hoge", "Taro", "Hoge", "Taro Hoge", "hoge@example.com", true)


  val invalidUserJson = """{"expand":"attributes","link":{"href":"https://example.com/crowd/rest/usermanagement/1/user?username=hoge","rel":"self"},"name":"hoge","first-name":"Taro","last-name":"Hoge","display-name":"Taro Hoge","email":"hoge@example.com","password":{"link":{"href":"https://example.com/crowd/rest/usermanagement/1/user/password?username=hoge","rel":"edit"}},"attributes":{"attributes":[],"link":{"href":"https://example.com/crowd/rest/usermanagement/1/user/attribute?username=hoge","rel":"self"}}}"""


  val notFoundJson = """{"reason":"USER_NOT_FOUND","message":"User <hoge> does not exist"}"""
  val notFound = NotFound("USER_NOT_FOUND", "User <hoge> does not exist")

  def e1 =
    "response"           | "result"             |
    (200, validUserJson) ! validUser.success    |
    (401, "hoge")        ! Unauthorized.failure |
    (403, "hoge")        ! Forbidden.failure    |
    (404, notFoundJson)  ! notFound.failure     |> { (response, result) =>
      parseResponse(response) must equalTo(result)
    }

  def e2 =
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

  def e3 = {
    import NormalTestEnv._
    implicit val c = case01
    Crowd.getUser("user01") must equalTo {
      User("user01", "01", "User", "User01", "user01@example.net", true).success
    }
  }

  def e4 = {
    import NormalTestEnv._
    implicit val c = case01
    Crowd.getUser("userZZ").toEither must beLeft.like {
      case NotFound(_, _) => ok
      case _ => ko
    }
  }
}
