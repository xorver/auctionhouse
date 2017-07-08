package auctionhouse.actors

import java.security.cert.CertPathValidatorException.BasicReason

import akka.testkit._
import akka.actor._
import org.scalatest.WordSpecLike
import scala.concurrent.Future
import java.util.concurrent.Executor
import org.scalatest.BeforeAndAfterAll
import auctionhouse.actors.{Created, Auction}
import org.scalatest.matchers.ShouldMatchers

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{WordSpec, BeforeAndAfterAll}
import akka.actor.Actor._
import akka.testkit.{TestActorRef, TestFSMRef, ImplicitSender, TestKit}
import org.scalatest.{Matchers, WordSpecLike, BeforeAndAfterAll}



class AuctionSpec extends TestKit(ActorSystem("AuctionSpec"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender with Matchers {

  var masterSearchProbe : TestProbe = null
  var masterSearch : ActorRef = null

  override def beforeAll {
    masterSearchProbe = TestProbe()
    masterSearch = system.actorOf(Props(new ForwardActor(masterSearchProbe.ref)), "masterSearch")

//    masterSearch = TestActorRef(Props[MasterSearch], name = "masterSearch")
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An auction" must {

    "start in 'Undefined' state" in {
      val auction = TestFSMRef(new Auction("test"))
      auction.stateName should  equal (Undefined)
    }

    "register in masterSearch" in {
      val auction = TestFSMRef(new Auction("test"))
      auction ! Start(1000)

      masterSearchProbe.expectMsg(AddAuction("test"))
    }

    "relist when requested" in {
      val auction = TestFSMRef(new Auction("test"))
      auction ! Start(1000)

      auction ! Expired

      auction.stateName should  equal (Ignored)

      auction ! Relist

      auction.stateName should  equal (Created)
    }

    "change state to 'Activated'" in {
      val auction = TestFSMRef(new Auction("test"))
      auction ! Start(1000)

      auction ! Bid(1)

      auction.stateName should  equal (Activated)
    }

    "accept valid bids" in {
      val auction = TestFSMRef(new Auction("test"))
      auction ! Start(1000)
      auction ! Bid(1)
      auction ! Expired

      expectMsg(AuctionSold)
      expectMsg(AuctionWon)
    }

    "accept outbids" in {
      val auction = TestFSMRef(new Auction("test"))
      val buyerPrx = system.actorOf(Props(new ForwardActor(auction)))
      expectMsgAllOf(AuctionSold)
      expectMsgAllOf(AuctionWon)
      expectNoMsg()

      auction ! Start(1000)
      auction ! Bid(1)
      buyerPrx ! Bid(2)
      expectMsg(Sent)
      auction ! Expired

      expectMsg(AuctionSold)
      expectNoMsg()
    }

    "ignore invalid bids" in {
      val auction = TestFSMRef(new Auction("test"))
      val buyerPrx = system.actorOf(Props(new ForwardActor(auction)))
      auction ! Start(1000)
      auction ! Bid(2)
      buyerPrx ! Bid(1)
      auction ! Expired

      expectMsg(AuctionSold)
      expectMsg(AuctionWon)
    }
  }

  case class Sent

  class ForwardActor(to: ActorRef) extends Actor {
    def receive = {
      case Bid(v) =>
        to ! Bid(v)
        sender ! Sent
      case x => to ! x
    }
  }

}

