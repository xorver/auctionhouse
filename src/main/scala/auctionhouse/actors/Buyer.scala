package auctionhouse.actors

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Random
import akka.actor._
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.event.Logging
import akka.actor
import akka.pattern.ask
import scala.concurrent.Future

/*
* Buyer messages
*/
sealed trait BuyerMessage
final case class WakeUp extends BuyerMessage
final case class StartWorking extends BuyerMessage


class Buyer(keywords: List[String])  extends Actor {
  val log = Logging(context.system, this)
  val rand = Random;
  
  def receive = {
    case WakeUp =>
//	  log.info(s"$self has woke up !")	
      val keyword = keywords(rand.nextInt(keywords.length))
      val selection = context.actorSelection("../masterSearch")
      selection ! FindAuction(keyword)
      
    case message : AuctionResponse =>
      if (message.list.size == 0)
    	  context.system.scheduler.scheduleOnce(Duration.create(rand.nextInt(1000), TimeUnit.MILLISECONDS), self, WakeUp)
      else {
	      val auction = message.list.head
	      auction ! Bid(rand.nextInt(1000))
	      context.system.scheduler.scheduleOnce(Duration.create(rand.nextInt(1000), TimeUnit.MILLISECONDS), self, WakeUp)
      }
    case AuctionWon =>
      log.info(s"Buyer won an auction ${sender.path.name} !")
      
    case AuctionSold =>
      log.info(s"Buyer sold an auction ${sender.path.name} !")
  }
  
}