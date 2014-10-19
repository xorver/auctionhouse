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
      goto(Created) using AuctionData(expirationTime, 0, sender, null)
  }
  
  onTransition {
    case _ -> Created => 
      scheduleExpiration(stateData)
    case Created -> Ignored => 
      scheduleExpiration(stateData)
    case Activated -> Sold =>
      scheduleExpiration(stateData)
      stateData match {
	  	case AuctionData(_, _, seller, buyer) => 
	  	  seller ! AuctionSold
	  	  buyer ! AuctionWon
      }
  }
  
  when(Created) {
    case Event(Bid(value), auctionData @ AuctionData(expirationTime, actualBid, seller, buyer)) if value > actualBid =>
            log.warning("got valid bid!")
      goto(Activated) using AuctionData(expirationTime, value, seller, sender)
    case Event(Bid(value), _) =>
            log.warning("got invalid bid!")
      stay
    case Event(Expired, _) =>
      goto(Ignored)
  }
  
  when(Activated) {
    case Event(Bid(value), auctionData @ AuctionData(expirationTime, actualBid, seller, buyer)) if value > actualBid => 
      stay using AuctionData(expirationTime, value, seller, sender)
    case Event(Bid(value), _)=>
      stay
    case Event(Expired, _) =>
      goto(Sold)
  }
  
  when(Ignored) {
    case Event(Relist, _) =>
      goto(Created)
    case Event(Expired, _) =>
      log.info("auction $self ignored!")
      stop
  }
  
  when(Sold) {
    case Event(Expired, _) =>
      log.info(s"auction $self sold!")
      stop
  }

  whenUnhandled {
    case Event(e, s) => {
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay
    }
  }
  
  def scheduleExpiration(auctionData: Data) : Unit = {
    stateData match {
	  case AuctionData(expirationTime, _, _, _) => context.system.scheduler.scheduleOnce(Duration.create(expirationTime, TimeUnit.MILLISECONDS), self, Expired)
	  case Uninitialized => Unit
    }
  }
  
}