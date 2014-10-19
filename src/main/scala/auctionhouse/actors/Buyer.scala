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


class Buyer(auctionsToBid: List[ActorRef], auctionsToStart: List[ActorRef])  extends Actor {
  val log = Logging(context.system, this)
  val rand = new Random(System.currentTimeMillis());
  auctionsToStart.foreach(ref => ref ! Start(rand.nextInt(10000)))
  
  def receive = {
    case WakeUp =>
      val auction = auctionsToBid(rand.nextInt(auctionsToBid.length))
      auction ! Bid(rand.nextInt(100))
      context.system.scheduler.scheduleOnce(Duration.create(rand.nextInt(1000), TimeUnit.MILLISECONDS), self, Expired)
    case AuctionWon =>
      log.info(s"$self has won an auction $sender !")
    case AuctionSold =>
      log.info(s"$self has sold an item on auction $sender !")
  }
  
}