package org.nisshiee.crowd4s

import org.specs2._, matcher.DataTables
import scalaz._, Scalaz._

class GetGroupListSpec extends Specification with DataTables { def is =

  "GetGroupList"                                                                ^
    "parseGroupList"                                                            ^
      "valid json is parsed to Success(List[String])"                           ! e1^
      "invalid json is parsed to Failure(JsonParseError)"                       ! e2^
                                                                                p^
    "getDirectGroupList"                                                        ^
      "if user exists"                                                          ! e3^
      "if user doesn't exist, RETURN EMPTY LIST"                                ! e4^
                                                                                p^
    "getNestedGroupList"                                                        ^
      "if user exists"                                                          ! e5^
      "if user doesn't exist, RETURN EMPTY LIST"                                ! e6^
                                                                                end

  import GetGroupList._

  val validJson1 = """{"expand":"group","groups":[]}"""
  val validJson2 = """{"expand":"group","groups":[{"link":{"href":"http://example.com/rest/usermanagement/1/group?groupname=group1","rel":"self"},"name":"group1"}]}"""
  val validJson3 = """{"expand":"group","groups":[{"link":{"href":"http://example.com/rest/usermanagement/1/group?groupname=group1","rel":"self"},"name":"group1"},{"link":{"href":"http://example.com/rest/usermanagement/1/group?groupname=group2","rel":"self"},"name":"group2"}]}"""

  val validList1 = List()
  val validList2 = List("group1")
  val validList3 = List("group1", "group2")

  val invalidJson = """{"expand":"group","groups":[{"link":{"href":"http://example.com/rest/usermanagement/1/group?groupname=group1","rel":"self"}},{"link":{"href":"http://example.com/rest/usermanagement/1/group?groupname=group2","rel":"self"},"name":"group2"}]}"""

  def e1 =
    "json"     || "result"   |
    validJson1 !! validList1 |
    validJson2 !! validList2 |
    validJson3 !! validList3 |> { (json, result) =>
      parseGroupList(json).toOption must beSome.which(result ==)
    }

  def e2 =
    parseGroupList(invalidJson).toEither must beLeft.like {
      case JsonParseError => ok
      case _ => ko
    }

  def e3 = {
    import NormalTestEnv._
    implicit val c = case01
    Crowd.getDirectGroupList("user01") must equalTo(Seq("group01", "group02", "group90").success)
  }

  def e4 = {
    import NormalTestEnv._
    implicit val c = case01
    Crowd.getDirectGroupList("userZZ") must equalTo(Seq().success)
  }

  def e5 = {
    import NormalTestEnv._
    implicit val c = case01
    Crowd.getNestedGroupList("user01") must equalTo(Seq("group01", "group02", "group03", "group90").success)
  }

  def e6 = {
    import NormalTestEnv._
    implicit val c = case01
    Crowd.getNestedGroupList("userZZ") must equalTo(Seq().success)
  }

}
