package auctionhouse.actors

import akka.actor.Actor
import akka.actor.ActorRef
import scala.util.Random
import akka.event.Logging
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import akka.dispatch.Foreach

/*
* Auction messages
*/
sealed trait BuyerMessage
final case class WakeUp extends BuyerMessage
final case class StartWorking extends BuyerMessage


class Buyer(auctionNames: List[String])  extends Actor {
  val log = Logging(context.system, this)
  val rand = new Random(System.currentTimeMillis());
  
  def receive = {
    case WakeUp =>
//	  log.info(s"$self has woke up !")	
      val auctionName = auctionNames(rand.nextInt(auctionNames.length))
//      val future = aaa ? GFFF()
//      future.wait()
//      auction ! Bid(rand.nextInt(1000))
      context.system.scheduler.scheduleOnce(Duration.create(rand.nextInt(1000), TimeUnit.MILLISECONDS), self, WakeUp)
    case AuctionWon =>
      log.info(s"Buyer won an auction ${sender.path.name} !")
    case AuctionSold =>
      log.info(s"Buyer sold an auction ${sender.path.name} !")
  }
  
}