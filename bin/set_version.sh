#!/bin/bash

PWD=`dirname $0`

cd "$PWD/../"

echo -n "new version: "
read NEW_VERSION

mv build.sbt{,.tmp}
sed 's/version := .*/version := "'"$NEW_VERSION"'"/' build.sbt.tmp > build.sbt
rm build.sbt.tmp

mv README.md{,.tmp}
sed 's/libraryDependencies += "org.nisshiee" %% "crowd4s" % .*/libraryDependencies += "org.nisshiee" %% "crowd4s" % "'"$NEW_VERSION"'"/' README.md.tmp > README.md
rm README.md.tmp
