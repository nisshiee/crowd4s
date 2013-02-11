package org.nisshiee.crowd4s

import org.specs2._
import scalaz._, Scalaz._

class ResponseParseHelperSpec extends Specification { def is =

  "ResponseParseHelper"                                                         ^
    "parseNotFound"                                                             ^
      "valid json is parsed to Failure(NotFound)"                               ! e1^
      "invaild json is parsed to Failure(UnknownError)"                         ! e2^
                                                                                end

  import ResponseParseHelper._

  def e1 = {
    val json = """{"reason":"USER_NOT_FOUND","message":"User <hoge> does not exist"}"""
    parseNotFound(json).toEither must beLeft.like {
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
}
