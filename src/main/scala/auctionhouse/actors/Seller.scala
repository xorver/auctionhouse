package auctionhouse.actors

import akka.actor.Actor
import akka.actor.ActorRef
import scala.util.Random
import akka.event.Logging
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import akka.dispatch.Foreach
import akka.actor.Props


class Seller(auctionNames: List[String]) extends Actor{
	val log = Logging(context.system, this)
	val rand = new Random(System.currentTimeMillis());
	val auctions = auctionNames.map(name => context.system.actorOf(Props[Auction], name)).toList
	auctions.foreach(auction => auction ! Start(5000 + rand.nextInt(10000)))
	
	def receive = {
	    case AuctionSold =>
	    	log.info(s"${self.path.name} sold an auction ${sender.path.name} !")
  }
  
}