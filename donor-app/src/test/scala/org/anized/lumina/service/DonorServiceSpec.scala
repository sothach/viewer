package org.anized.lumina.service

import akka.Done
import org.anized.lumina.api._
import org.anized.lumina.api.domain.model.{PlanType, Registration, Donor}
import org.anized.lumina.service.suite.DonorFixture
import org.scalatest.{AsyncWordSpec, Matchers}

class DonorServiceSpec(val fixture: DonorFixture) extends AsyncWordSpec with Matchers {
  import fixture._
  val client: DonorService = server.serviceClient.implement[DonorService]

  "donor service" should {
    "create a new donor" in {
      val donorReg = Registration("user@email.com")
      client.create.invoke(donorReg).map { answer =>
        answer should ===(Done)
      }
    }

    "look-up donor by email" in {
      client.lookup("user@email.com").invoke().map { answer =>
        answer should ===(Donor("0000","user@email.com"))
      }
    }

    "fail to look-up donor by non-existent email" in {
      client.lookup("test@email.com").invoke().map { answer =>
        answer should ===(Donor("0000","user@email.com"))
      }
    }

    "retrieve donor by donor id" in {
      client.get("1234").invoke().map { answer =>
        answer should ===(Donor("1234",""))
      }
    }
  }

}
