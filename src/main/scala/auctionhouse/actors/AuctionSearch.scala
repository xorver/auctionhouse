package auctionhouse.actors

import akka.event.Logging
import akka.actor.Actor

/*
* AuctionSearch messages
*/
sealed trait AuctionSearchMessage
final case class FindAuction(name: String) extends AuctionSearchMessage
final case class AddAuction(name: String) extends AuctionSearchMessage
final case class AuctionResponse(list: List[akka.actor.ActorRef]) extends AuctionSearchMessage
final case class AuctionNotFound() extends AuctionSearchMessage

class AuctionSearch extends Actor{
	val log = Logging(context.system, this)
	var auctions = List[(String, akka.actor.ActorRef)]()
	
	def receive = {
		case message: AuctionSearchMessage => message match {
		  case FindAuction(name) => {
		    //log.info(s"finding auction: $name")
		    val found = auctions.filter(auction => auction._1.contains(name))
		    context.sender ! AuctionResponse(found.map(auction => auction._2))
		  }
		  case AddAuction(name) => {
			log.info(s"adding auction: $name")
		    auctions = auctions ++ List((name,context.sender))
		  }
	    }
	}
}