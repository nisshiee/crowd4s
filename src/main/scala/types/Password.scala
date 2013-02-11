package org.nisshiee.crowd4s

case class Password(value: String) extends AnyVal

object Password {

  implicit def str2password: String => Password = Password.apply
}
