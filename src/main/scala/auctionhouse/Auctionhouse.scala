package scottdanzig.buddychat

import akka.actor.ActorSystem
import akka.event.Logging
import akka.actor.Props
import auctionhouse.actors.Auction
import auctionhouse.actors.Buyer
import auctionhouse.actors.StartWorking
import auctionhouse.actors.WakeUp

object Auctionhouse {
  
  val BUYERS_NUMBER = 10
  val system = ActorSystem()
  val log = Logging(system, Auctionhouse.getClass().getName())
  
  def main(args: Array[String]): Unit = run()
  
  def run() = {
    log.debug("Initializing chat system.")
    val auctions = (0 to BUYERS_NUMBER-1).map(num => system.actorOf(Props[Auction], "auction"+num)).toList
    val buyers = (0 to BUYERS_NUMBER-1).map(num => 
      system.actorOf(Props(classOf[Buyer], auctions diff List(auctions(num)), List(auctions(num))), "buyer"+num)
    ).toList
    
    buyers.foreach(buyer => buyer ! WakeUp)
  }
}