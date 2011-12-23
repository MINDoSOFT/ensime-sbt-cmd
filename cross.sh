#!bash

SRC=src/main/scala/org/ensime/sbt/

cp $SRC/Compat.scala.0.11.0 $SRC/Compat.scala
sbt11.0 $1
cp $SRC/Compat.scala.0.11.1 $SRC/Compat.scala
sbt11.1 $1
sbt11.2 $1

rm $SRC/Compat.scala