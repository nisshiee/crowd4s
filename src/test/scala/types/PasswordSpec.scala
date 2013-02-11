package org.nisshiee.crowd4s

import org.specs2._

class PasswordSpec extends Specification { def is =

  "Password"                                                                    ^
    "str2password"                                                              ^
      "normal call"                                                             ! e1^
      "implicit call"                                                           ! e2^
                                                                                end

  def e1 =
    Password.str2password("pass") must equalTo(Password("pass"))

  def e2 = {
    val pass: Password = "pass"
    pass must equalTo(Password("pass"))
  }
}
