package org.anized.lumina.service

import java.time.Instant
import java.util.UUID

import org.anized.lumina.api.domain.model
import org.anized.lumina.api.domain.model.{PlanType, Status, Donor}
import org.anized.lumina.entities.{Created, PlanChanged}
import org.anized.lumina.service.suite.DonorFixture
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

class DonorRepoSpec(val fixture: DonorFixture)
  extends AsyncWordSpec with Matchers with ScalaFutures with OptionValues {
  import fixture._

  private val donorRepository = server.application.donorRepository

  "The donor processor" should {
    "create a new Donor" in {
      val donorId = UUID.randomUUID().toString
      withClue("Donor is not expected to exist") {
        donorRepository.findById(donorId).futureValue shouldBe None
      }

      val eventTime = Instant.now()
      for {
        _ <- feedEvent(donorId, Created(Donor(donorId,"test@email.com"), eventTime))
        donor <- donorRepository.findById(donorId)
      } yield {
        donor.value.plan shouldBe PlanType.T0
        donor.value.status shouldBe model.Status.Created
      }
    }

    "produce a Gold Donor on plan-changed event" in {
      val donorId = UUID.randomUUID().toString

      withClue("Donor should not exist") {
        donorRepository.findById(donorId).futureValue shouldBe None
      }

      val eventTime = Instant.now()
      val changeTime = eventTime.plusSeconds(30)

      for {
        _ <- feedEvent(donorId, Created(Donor(donorId,"another@email.com"), eventTime))
        _ <- feedEvent(donorId, PlanChanged(PlanType.T4, changeTime))
        donor <- donorRepository.findById(donorId)
      } yield {
        donor.value.plan shouldBe PlanType.T4
      }
    }

  }

}
