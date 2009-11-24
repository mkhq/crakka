package crakka;

import se.scalablesolutions.akka.actor.Actor
import se.scalablesolutions.akka.nio.{RemoteServer}
import se.scalablesolutions.akka.util.Logging
import se.scalablesolutions.akka.serialization._
import sbinary.DefaultProtocol._

case class ActorId(hostname:String, port:Int, Id:Int) extends Serializable.SBinary[ActorId] {
  implicit object ActorIDFormat extends Format[ActorId] {
	 def reads(in: Input) = ActorId(
		read[String](in),
		read[Int](in),
		read[Int](in))

	 def writes(out: Output, value: ActorId) = {
		write[String](out, value.hostname)
		write[Int](out, value.port)
		write[Int](out, value.Id)
	 }
  }

  def fromBytes(bytes: Array[Byte]) = fromByteArray[ActorId](bytes)
  def toBytes: Array[Byte] = toByteArray(this)
}

case class Event(from:ActorId, msg:AnyRef) extends Serializable.SBinary[Event] {
  implicit object EventFormat extends Format[Event] {
	 def reads(in: Input) = Event(
		read[ActorId](in),
		read[AnyRef](in))
	 def writes(out: Output, value: Event) = {
		write[ActorId](out, value.from)
		write[AnyRef](out, value.msg)
	 }
  }

  def fromBytes(bytes: Array[Byte]) = fromByteArray[Event](bytes)
  def toBytes: Array[Byte] = toByteArray(this)
}

case class Ping() extends Serializable.SBinary[Ping] {
  
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

class PingPongServer() {
	 val server:Thread = new Thread(new Runnable() {
		def run = {
		  //		 Logging.log.error("starting server")
		  val server = new RemoteServer
		  server.start
		}
	 })
	 server.start
//	 Thread.sleep(1000)

  val p1 = new PingPong
  p1.makeRemote(RemoteServer.HOSTNAME, RemoteServer.PORT)
  p1.start

  val p2 = new PingPong
  p2.makeRemote(RemoteServer.HOSTNAME, RemoteServer.PORT)
  p2.start

  def startPingPong() {
	 p1.sendPing(p2)
	 p2.sendPong(p1)
  }
}

object PingPongService {
  //config.Config
  def main(args: Array[String]): Unit = {
	 val s1 = new PingPongServer
	 Thread.sleep(1000)
	 s1.startPingPong
  }
}
