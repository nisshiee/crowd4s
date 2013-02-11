package org.nisshiee.crowd4s

sealed trait AuthenticationResult extends Any

object AuthenticationResult {

  case class Success(user: User) extends AnyVal with AuthenticationResult
  case class Failure(reason: String, message: String) extends AuthenticationResult
}
