package org.anized.lumina.service

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.anized.lumina.api.domain.model.{PlanType, Status, Donor}
import org.anized.lumina.entities._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class DonorEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("donor-entity-spec",
    JsonSerializerRegistry.actorSystemSetupFor(DonorSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(block: PersistentEntityTestDriver[DonorCommand[_], DonorEvent, DonorState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new DonorEntity, "donor-1")
    block(driver)
    driver.getAllIssues should have size 0
  }

  "Donor entity" should {

    "be created" in withTestDriver { driver =>
      val outcome = driver.run(Create("test@email.org"))
      outcome.replies should contain only Done
      outcome.events should have size 1
      outcome.events.head should matchPattern { case Created(Donor(_,"test@email.org",_,_), _) => }
      outcome.state should ===(DonorState("test@email.org",PlanType.T0,status=Status.Created))
    }

    "change plan" in withTestDriver { driver =>
      driver.run(Create("test@email.org"))
      val outcome = driver.run(ChangePlan(PlanType.T1))
      outcome.replies should contain only Done
      outcome.events should have size 1
      outcome.events.head should matchPattern { case PlanChanged(PlanType.T1, _) => }
      outcome.state should ===(DonorState("test@email.org",PlanType.T1,status=Status.Created))
    }

    "allow suspension of donor" in withTestDriver { driver =>
      driver.run(Create("test@email.org"))
      val outcome = driver.run(Suspend("pay"))
      outcome.replies should contain only Done
      outcome.events should have size 1
      outcome.events.head should matchPattern { case Suspended(_,_) => }
      outcome.state should ===(DonorState("test@email.org",PlanType.T0,status=Status.Suspended))
    }

    "allow getting the state" in withTestDriver { driver =>
      driver.run(Create("test@email.org"))
      driver.run(ChangePlan(PlanType.T1))
      val outcome = driver.run(Get)
      outcome.replies should contain only DonorState("test@email.org",PlanType.T1,status=Status.Created)
      outcome.events should have size 0
    }

    "fail when suspending a donor that is already suspended" in withTestDriver {
      driver =>
        driver.run(Create("test@email.org"))
        driver.run(Suspend("pay"))
        val outcome = driver.run(Suspend("terms"))
        outcome.replies should have size 1
        outcome.replies.head shouldBe a[DonorException]
        outcome.events should have size 0
      }

  }
}
