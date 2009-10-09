import sbt._
import sbt.Process._

class Configuration(info:ProjectInfo) extends DefaultProject(info) {
  override def mainClass = Some("crakka.PingPong")

  // repositories
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

  // dependencies
  val akka_actors = "se.scalablesolutions.akka" % "akka-actors" % "0.6"
}
