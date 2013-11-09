name := "instrumenting-jmx"

organization := "com.dkhenry"

version :="0.1-SNAPSHOT"

scalaVersion := "2.10.2"

javaOptions in run += "-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=1099"

fork in run := true