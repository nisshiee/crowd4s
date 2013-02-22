crowd4s
========================================

[![Build Status](https://travis-ci.org/nisshiee/crowd4s.png?branch=master)](https://travis-ci.org/nisshiee/crowd4s)

about
----------------------------------------

crowd4s is atlassian crowd API wrapper for Scala.


how to use
----------------------------------------

### sbt dependency

```
libraryDependencies += "org.nisshiee" %% "crowd4s" % "1.1.1"
```

### config

Create `application.conf` on your classpath root.

```
crowd4s = {
  urlPrefix = "https://my.crowd.net"
  appname = my-app
  password = password
  authorizedGroup = [ my-group, my-users ]
}
```

`urlPrefix` is crowd application URL prefix. it must not include the last `/`.

`appname` and `password` are your application's value which input on crowd.

`authorizedGroup` is group name list which you authorize.


### coding

```scala
import org.nisshiee.crowd4s._

// import connection info as implicit value
import Config.connection

// import authrozed group list as implicit value
import Config.authorizedGroup

// check
//  - username, password validity
//  - user belongs to authorized groups
Crowd.authorize("username", "password")
```

`Crowd.authorize` method's return value has type `Validation[AuthorizationResult]`.

`Validation` is Scalaz Validation.
It will become Failure when some network problems occur.

