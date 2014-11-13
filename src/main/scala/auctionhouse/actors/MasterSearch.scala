package auctionhouse.actors

import akka.routing._
import akka.actor.Actor
import akka.actor.Props

class MasterSearch extends Actor  {

  val SERACHERS_NUM=4
  val auctionSearchList = (1 to SERACHERS_NUM).map(num => context.actorOf(Props[AuctionSearch], "auctionSearch"+num)).toList
  val routees = Vector.fill(SERACHERS_NUM) {
	 val r = context.actorOf(Props[AuctionSearch])
     context watch r
	 ActorRefRoutee(r)
  }
  var routerBroadcast = Router(BroadcastRoutingLogic(), routees)
  var routerRoundRobin = Router(RoundRobinRoutingLogic(), routees)
  
  def receive = {
		case message: AuctionSearchMessage => message match{
		  case find : FindAuction =>
			routerRoundRobin.route(message, sender)
		  case find : AddAuction =>
			routerBroadcast.route(message, sender)
		}
		  
	}
}