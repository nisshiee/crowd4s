package org.nisshiee.crowd4s

import org.specs2._, matcher.DataTables
import scalaz._, Scalaz._

class GetGroupListSpec extends Specification with DataTables { def is =

  "GetGroupList"                                                                ^
    "parseGroupList"                                                            ^
      "valid json is parsed to Success(List[String])"                           ! e1^
      "invalid json is parsed to Failure(JsonParseError)"                       ! e2^
                                                                                end

  import GetGroupList._

  val validJson = """{"expand":"group","groups":[{"link":{"href":"http://example.com/rest/usermanagement/1/group?groupname=group1s","rel":"self"},"name":"group1"},{"link":{"href":"http://example.com/rest/usermanagement/1/group?groupname=group2","rel":"self"},"name":"group2"}]}"""

  val validList = List("group1", "group2")

  val invalidJson = """{"expand":"group","groups":[{"link":{"href":"http://example.com/rest/usermanagement/1/group?groupname=group1s","rel":"self"}},{"link":{"href":"http://example.com/rest/usermanagement/1/group?groupname=group2","rel":"self"},"name":"group2"}]}"""

  def e1 = parseGroupList(validJson).toOption must beSome.which(validList ==)

  def e2 =
    parseGroupList(invalidJson).toEither must beLeft.like {
      case JsonParseError => ok
      case _ => ko
    }
}
