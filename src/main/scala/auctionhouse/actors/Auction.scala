package auctionhouse.actors

import akka.actor.Actor
import akka.actor.FSM
import akka.actor.ActorRef
import akka.actor.Props
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.TimeUnit

/**
* Auction messages
*/
sealed trait AuctionMessage
case class Expired() extends AuctionMessage
case class Start(expirationTime: Int) extends AuctionMessage
case class Bid(value: Int) extends AuctionMessage
case class Relist() extends AuctionMessage
case class AuctionSold() extends AuctionMessage
case class AuctionWon() extends AuctionMessage

/**
 * Auction states
 */
sealed trait AuctionState
case object Undefined extends AuctionState
case object Created extends AuctionState
case object Ignored extends AuctionState
case object Activated extends AuctionState
case object Sold extends AuctionState

/**
 * Auction data
 */
sealed trait Data
case object Uninitialized extends Data
case class AuctionData(expirationTime: Int, actualBid: Int, seller: ActorRef, buyer: ActorRef) extends Data


class Auction extends Actor with FSM[AuctionState, Data] {

  startWith(Undefined, Uninitialized)
  
  when(Undefined) {
    case Event(Start(expirationTime), Uninitialized) =>
      context.system.scheduler.scheduleOnce(Duration.create(expirationTime, TimeUnit.MILLISECONDS), self, Expired)
      goto(Created) using AuctionData(expirationTime, 0, sender, null)
  }
  
  onTransition {
    case Ignored -> Created => 
//      log.info(s"Changing state Ignored -> Created! $stateData")
      stateData match {
	  	case AuctionData(expirationTime, _, _, _) => 
  	      context.system.scheduler.scheduleOnce(Duration.create(expirationTime, TimeUnit.MILLISECONDS), self, Expired)
	  	case Uninitialized => Unit
      }
    case Created -> Ignored => 
//      log.info(s"Changing state Created -> Ignored! $stateData")
      stateData match {
	  	case AuctionData(expirationTime, _, _, _) => 
  	      context.system.scheduler.scheduleOnce(Duration.create(expirationTime, TimeUnit.MILLISECONDS), self, Expired)
	  	case Uninitialized => Unit
      }
    case Activated -> Sold =>
//      log.info(s"Changing state Activated -> Sold! $stateData")
      stateData match {
	  	case AuctionData(expirationTime, _, seller, buyer) => 
  	      context.system.scheduler.scheduleOnce(Duration.create(expirationTime, TimeUnit.MILLISECONDS), self, Expired)
	  	  seller ! AuctionSold
	  	  buyer ! AuctionWon
	  	case Uninitialized => Unit
      }
  }
  
  when(Created) {
    case Event(Expired, _) =>
      goto(Ignored)
    case Event(Bid(value), auctionData @ AuctionData(expirationTime, actualBid, seller, buyer)) if value > actualBid =>
      log.info(s"Bid $value, from ${sender.path.name}!")
      goto(Activated) using AuctionData(expirationTime, value, seller, sender)
    case Event(Bid(value), _) =>
//      log.info("got invalid bid!")
      stay
  }
  
  when(Activated) {
    case Event(Expired, _) =>
      log.info("Auction sold!")
      goto(Sold)
    case Event(Bid(value), auctionData @ AuctionData(expirationTime, actualBid, seller, buyer)) if value > actualBid => 
      log.info(s"Bid $value, from ${sender.path.name}!")
      stay using AuctionData(expirationTime, value, seller, sender)
    case Event(Bid(value), _)=>
//      log.info("got invalid bid in activated state!")
      stay
  }
  
  when(Ignored) {
    case Event(Relist, _) =>
      goto(Created)
    case Event(Expired, _) =>
      log.info("Auction ignored!")
      stop
  }
  
  when(Sold) {
    case Event(Expired, AuctionData(expirationTime, actualBid, seller, buyer)) =>
      log.info(s"Auction sold to ${buyer.path.name}, price: $actualBid!")
      stop
  }

  whenUnhandled {
    case Event(e, s) => {
//      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay
    }
  }
  
  def scheduleExpiration(auctionData: Data) : Unit = {
    stateData match {
	  case AuctionData(expirationTime, _, _, _) => 
	    context.system.scheduler.scheduleOnce(Duration.create(10000, TimeUnit.MILLISECONDS), self, Expired)
	    context.system.scheduler.scheduleOnce(Duration.create(expirationTime, TimeUnit.MILLISECONDS), self, Expired)
	  case Uninitialized => Unit
    }
  }
  
}