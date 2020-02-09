package org.anized.lumina.service

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import org.anized.lumina.api.DonorService
import org.anized.lumina.api.domain.model.{Registration, Donor}
import org.anized.lumina.entities._

import scala.concurrent.ExecutionContext

/**
  * Implementation of the DonorService over a persistent repository
  */
class DonorServiceRepo(registry: PersistentEntityRegistry)
                      (implicit ec: ExecutionContext) extends DonorService {

  private def entityRef(id: String) = registry.refFor[DonorEntity](id)

  override def create: ServiceCall[Registration, Done] = ServiceCall { request =>
    val create = Create(request.email)
    entityRef("0000")
      .ask(create)
      .recover { case DonorException(message) =>
        throw BadRequest(message)
      }
  }

  override def get(id: String): ServiceCall[NotUsed, Donor] = ServiceCall { _ =>
    entityRef(id)
      .ask(Get)
      .map(state => materializeFromState(id,state))
  }

  override def lookup(email: String): ServiceCall[NotUsed, Donor] = ServiceCall { _ =>
    entityRef("0000")
      .ask(Lookup(email))
      .map(state => materializeFromState("0000",state))
  }

  override def donorTopic: Topic[Donor] =
    TopicProducer.taggedStreamWithOffset(DonorEvent.Tag) {
      (tag, fromOffset) =>
        registry.eventStream(tag, fromOffset)
          .mapAsync(4) {
            case EventStreamElement(id, _, offset) =>
              entityRef(id)
                .ask(Get)
                .map(state => materializeFromState(id, state) -> offset)
          }
    }

  private def materializeFromState(id: String, state: DonorState) =
    Donor(id, state.email, state.plan, state.status)

}
