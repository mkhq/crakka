package crakka;

import se.scalablesolutions.akka.actor.Actor
import se.scalablesolutions.akka.nio.{RemoteServer}
import se.scalablesolutions.akka.util.Logging
import se.scalablesolutions.akka.serialization._

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
		  RemoteServer.start(host, port)
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

object PingPongService {
  //config.Config
  def main(args: Array[String]): Unit = {
	 val s1 = new PingPongServer("localhost", 9990)
	 Thread.sleep(1000)
	 s1.startPingPong
  }
}
