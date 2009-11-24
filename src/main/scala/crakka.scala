package crakka;

import se.scalablesolutions.akka.actor.Actor
import se.scalablesolutions.akka.nio.{RemoteServer, RemoteServerNode}
import se.scalablesolutions.akka.util.Logging
import se.scalablesolutions.akka.serialization._
import se.scalablesolutions.akka.Config.config

case class Ping() extends Serializable.SBinary[Ping] {
import sbinary.DefaultProtocol._
  
  implicit object PingFormat extends Format[Ping] {
	 def reads(in: Input) = Ping()
	 def writes(out: Output, value:Ping) = {
		write[Unit](out, Ping)
	 }
  }

  def fromBytes(bytes: Array[Byte]) = fromByteArray[Ping](bytes)
  def toBytes: Array[Byte] = toByteArray(this)
}

case class Pong() extends Serializable.SBinary[Pong] {
import sbinary.DefaultProtocol._
  
  implicit object PongFormat extends Format[Pong] {
	 def reads(in: Input) = Pong()
	 def writes(out: Output, value:Pong) = {
		write[Unit](out, Pong)
	 }
  }

  def fromBytes(bytes: Array[Byte]) = fromByteArray[Pong](bytes)
  def toBytes: Array[Byte] = toByteArray(this)
}

abstract class Service(hostname:String, port:Int) extends Actor with Logging {
  makeRemote(hostname, port)

  def handle: PartialFunction[Any, Unit]

  override def receive: PartialFunction[Any, Unit] = {
	 case "broadcast" => { println("broadcast") }
	 case event =>
		handle(event)
  }
}

abstract class Client(hostname:String, port:Int) extends Service(hostname, port) {
}

class PPService(hostname:String, port:Int) extends Service(hostname, port) {
  start

  def handle = {
	 case Ping =>
		println("Ping")
		reply(Pong())
	 case _ =>
		throw new RuntimeException("Unknown Message")
  }
}

class PPClient(hostname:String, port:Int) extends Client(hostname, port) {
  start

  def handle = {
	 case Pong =>
		println("Pong")
  }

  def ping(pps:PPService) {
	 pps ! Ping()
  }
}

class PingPong extends Actor with Logging {
//  makeTransactionRequired

//  val storage = TransactionalState.newMap[String, String]

  override def receive: PartialFunction[Any, Unit] = {
    case Ping() => {
		reply(Pong())
	 }
	 case Pong() => {
		reply(Ping())
	 }
  }

  def sendPing(other:PingPong) {
	 other !! Ping()
  }

  def sendPong(other:PingPong) {
	 other !! Pong()
  }

}

class PingPongServer(host:String, port:Int) {
	 val server:Thread = new Thread(new Runnable() {
		def run = {
		  //		 Logging.log.error("starting server")
		  RemoteServerNode.start(host, port)
		}
	 })
	 server.start
//	 Thread.sleep(1000)

  val p1 = new PingPong
  p1.makeRemote(host, port)
  p1.start

  val p2 = new PingPong
  p2.makeRemote(host, port)
  p2.start

  def startPingPong() {
	 p1.sendPing(p2)
	 p2.sendPong(p1)
  }
}

/* object PingPongService {
  //config.Config
  System.setProperty("akka.home", ".")

  def main(args: Array[String]): Unit = {
	 val s1 = new PingPongServer("localhost", 9990)
	 Thread.sleep(1000)
	 s1.startPingPong

	 val s2 = new PingPongServer("localhost", 9991)
	 s2
  }
} */

object PingPongService {
  System.setProperty("akka.home", ".")

  def startServer(hostname:String, port:Int):RemoteServer = {
	 val s = new RemoteServer
	 s.start(hostname, port)
	 s
  }

  def main(args:Array[String]): Unit = {
/*	 val myServer = new RemoteServer
	 myServer.start("localhost", 9990)

	 val myServer2 = new RemoteServer
	 myServer2.start("localhost", 9991) */
	 val s1 = startServer("localhost", 9990)
	 val s2 = startServer("localhost", 9991)

	 val pps = new PPService("localhost", 9990)
	 val ppc = new PPClient("localhost", 9991)

	 ppc.ping(pps)

	 s1.shutdown
	 s2.shutdown
  }
}
