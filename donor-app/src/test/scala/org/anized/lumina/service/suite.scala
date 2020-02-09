package org.anized.lumina.service

import java.util.concurrent.atomic.AtomicInteger

import akka.Done
import akka.persistence.query.Sequence
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.{ReadSideTestDriver, ServiceTest}
import org.anized.lumina.entities.DonorEvent
import org.anized.lumina.service.suite.DonorFixture
import org.scalatest.{BeforeAndAfterAll, Suites}

import scala.concurrent.Future

class DonorSuite extends Suites(
  new DonorEntitySpec,
  new DonorRepoSpec(DonorFixture),
  new DonorServiceSpec(DonorFixture)
) with BeforeAndAfterAll {
  override def afterAll(): Unit = DonorFixture.shutdown()
}

package object suite {

  trait DonorFixture {
    def server: ServiceTest.TestServer[DonorApplication]
    def testDriver: ReadSideTestDriver
    def feedEvent(donorId: String, event: DonorEvent): Future[Done]
  }

  object DonorFixture extends DonorFixture {
    override val server: ServiceTest.TestServer[DonorApplication] =
      ServiceTest.startServer(ServiceTest.defaultSetup.withJdbc(true)) { ctx =>
        new DonorApplication(ctx) with LocalServiceLocator {
          override lazy val readSide: ReadSideTestDriver = new ReadSideTestDriver
        }
      }

    override val testDriver: ReadSideTestDriver = server.application.readSide.asInstanceOf[ReadSideTestDriver]
    private val offset = new AtomicInteger(0)

    override def feedEvent(donorId: String, event: DonorEvent): Future[Done] = {
      testDriver.feed(donorId, event, Sequence(offset.getAndIncrement))
    }

    def shutdown(): Unit = {
      TestKit.shutdownActorSystem(server.actorSystem)
      server.stop()
    }
  }
}