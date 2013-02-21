package org.nisshiee.crowd4s

import org.specs2._, matcher.DataTables
import scalaz._, Scalaz._

class AuthorizeSpec extends Specification with DataTables { def is =

  "Authorize"                                                                   ^
    "checkActive"                                                               ^
      "if the group is active"                                                  ! e1^
      "if the group is inactive"                                                ! e2^
      "if the group doesn't exist"                                              ! e3^
                                                                                p^
    "searchAuthorizedGroup"                                                     ^
      "if the user belongs to authorized group"                                 ! e4^
      "if the user belongs to authorized group but the group is inactive"       ! e5^
      "if the user doesn't belong to authorized group"                          ! e6^
                                                                                p^
    "checkAuthenticationResult"                                                 ^
      "if the authentication failure"                                           ! e7^
      "if the authentication success"                                           ! e8^
                                                                                p^
    "authorize"                                                                 ^
      "if authentication failure"                                               ! e9^
      "authorization success case"                                              ! e10^
      "authorization failure case"                                              ! e11^
      "if connection error"                                                     ! e12^
                                                                                end

  import Authorize._

  def e1 = {
    import NormalTestEnv._
    implicit val c = case01
    checkActive("group01").toEither must beRight.like {
      case Some(Group("group01", _, _)) => ok
      case _ => ko
    }
  }

  def e2 = {
    import NormalTestEnv._
    implicit val c = case01
    checkActive("group90").toEither must beRight.like {
      case None => ok
      case _ => ko
    }
  }

  def e3 = {
    import NormalTestEnv._
    implicit val c = case01
    checkActive("groupZZ").toEither must beLeft.like {
      case NotFound(_, _) => ok
      case _ => ko
    }
  }

  val user01 = User("user01", "01", "User", "User01", "user01@example.net", true)
  val belonged = Seq("group01", "group02", "group90")
  val group01 = Group("group01", "The first group", true)
  val group02 = Group("group02", "The second group", true)

  def e4 =
    "authorized"              | "resultGroup" |
    Seq("group01")            ! group01       |
    Seq("group01", "group02") ! group01       |
    Seq("group01", "group03") ! group01       |
    Seq("group01", "group04") ! group01       |
    Seq("group04", "group01") ! group01       |
    Seq("group02", "group01") ! group02       |
    Seq("group01", "group90") ! group01       |
    Seq("group90", "group01") ! group01       |> { (authorized, resultGroup) =>
      import NormalTestEnv._
      implicit val c = case01
      searchAuthorizedGroup(AuthorizedGroup(authorized), belonged)(user01) must equalTo {
        AuthorizationResult.Success(user01, resultGroup).success
      }
    }

  def e5 = {
    import NormalTestEnv._
    implicit val c = case01
    searchAuthorizedGroup(AuthorizedGroup(Seq("group90")), belonged)(user01) must equalTo {
      AuthorizationResult.AuthorizationFailure(user01).success
    }
  }

  def e6 = {
    import NormalTestEnv._
    implicit val c = case01
    searchAuthorizedGroup(AuthorizedGroup(Seq("group04")), belonged)(user01) must equalTo {
      AuthorizationResult.AuthorizationFailure(user01).success
    }
  }

  def e7 = {
    import NormalTestEnv._
    implicit val c = case01
    val authorized = AuthorizedGroup(Seq("group01"))
    checkAuthenticationResult(authorized)(AuthenticationResult.Failure("reason", "message")) must equalTo {
      AuthorizationResult.AuthenticationFailure(AuthenticationResult.Failure("reason", "message")).success
    }
  }

  def e8 = {
    import NormalTestEnv._
    implicit val c = case01
    val authorized = AuthorizedGroup(Seq("group01"))
    checkAuthenticationResult(authorized)(AuthenticationResult.Success(user01)) must equalTo {
      AuthorizationResult.Success(user01, group01).success
    }
  }

  def e9 =
    "username" || "password" |
    "user01"   !! "passZZ"   |
    "userZZ"   !! "passZZ"   |> { (username, password) =>
      import NormalTestEnv._
      implicit val c = case01
      implicit val authorized = Seq("group01") |> AuthorizedGroup.apply
      Crowd.authorize(username, password).toEither must beRight.like {
        case AuthorizationResult.AuthenticationFailure(_) => ok
        case _ => ko
      }
    }

  def e10 =
    "authorized"              | "resultGroup" |
    Seq("group01")            ! group01       |
    Seq("group01", "group02") ! group01       |
    Seq("group01", "group03") ! group01       |
    Seq("group01", "group04") ! group01       |
    Seq("group04", "group01") ! group01       |
    Seq("group02", "group01") ! group02       |
    Seq("group01", "group90") ! group01       |
    Seq("group90", "group01") ! group01       |> { (authorized, resultGroup) =>
      import NormalTestEnv._
      implicit val c = case01
      implicit val a = authorized |> AuthorizedGroup.apply
      Crowd.authorize("user01", "pass01").toEither must beRight.like {
        case AuthorizationResult.Success(user, group) =>
          (user must equalTo(user01)) and (group must equalTo(resultGroup))
        case _ => ko
      }
    }

  def e11 =
    "authorized"              | "resultUser" |
    Seq("group04")            ! user01       |
    Seq("group90")            ! user01       |> { (authorized, resultUser) =>
      import NormalTestEnv._
      implicit val c = case01
      implicit val a = authorized |> AuthorizedGroup.apply
      Crowd.authorize("user01", "pass01").toEither must beRight.like {
        case AuthorizationResult.AuthorizationFailure(user) => user must equalTo(resultUser)
        case _ => ko
      }
    }

  def e12 = {
    import IrregularTestEnv._

    "when"       | "error"         |
    Authenticate ! ConnectionError |
    GetGroupList ! ConnectionError |
    GetGroup     ! ConnectionError |> { (when, error) =>
      implicit val c: Case = Set[Func](when)
      implicit val authorized = Seq("group01") |> AuthorizedGroup.apply
      Crowd.authorize("user01", "pass01").toEither must beLeft.like {
      case ConnectionError => ok
      case _ => ko
      }
    }
  }
    
}
