package auctionhouse

import akka.actor.ActorSystem
import akka.event.Logging
import akka.actor.Props
import auctionhouse.actors.Auction
import auctionhouse.actors.Buyer
import auctionhouse.actors.StartWorking
import auctionhouse.actors.WakeUp
import auctionhouse.actors.Seller
import auctionhouse.actors.AuctionSearch
import auctionhouse.actors.MasterSearch

object Auctionhouse {
  
  val BUYERS_NUMBER = 5
  val system = ActorSystem()
  val log = Logging(system, Auctionhouse.getClass().getName())
  
  def main(args: Array[String]): Unit = run()
  
  def run() = {
    log.debug("Initializing chat system.")
    val masterSearch = system.actorOf(Props(classOf[MasterSearch]), "masterSearch")
    val auctionNames1 = List("Audi A6 diesel manual", "BMW X5 diesel") 
    val auctionNames2 = List("Opel Astra combi", "Fiat Punto 1.2") 
    val sellers = List(
    		system.actorOf(Props(classOf[Seller], auctionNames1), "seller1"),
    		system.actorOf(Props(classOf[Seller], auctionNames2), "seller2")
        )
    val buyers = List(
    		system.actorOf(Props(classOf[Buyer], List("diesel")), "buyer1"),    
    		system.actorOf(Props(classOf[Buyer], List("Fiat")), "buyer2"),
    		system.actorOf(Props(classOf[Buyer], List("Opel")), "buyer3")   
    	)
    
    buyers.foreach(buyer => buyer ! WakeUp)
  }
}