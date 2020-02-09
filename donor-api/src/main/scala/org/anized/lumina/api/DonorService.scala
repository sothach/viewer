package org.anized.lumina.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import org.anized.lumina.api.domain.model.{Registration, Donor}

object DonorService {
  val TOPIC_NAME = "donor-event"
}

/**
  * The donor service interface
  * <p>
  * This describes everything that Lagom needs to know about how to serve and consume the DonorService
  */
trait DonorService extends Service {

  /**
    * Example: curl http://localhost:9000/api/get/12345
    */
  def create: ServiceCall[Registration, Done]

  def get(id: String): ServiceCall[NotUsed, Donor]

  def lookup(email: String): ServiceCall[NotUsed, Donor]

  def donorTopic: Topic[Donor]

  override final def descriptor: Descriptor = {
    import Service._

    named("donor")
      .withCalls(
        pathCall("/api/donor/:id", get _),
        pathCall("/api/donor/email/:id", lookup _),
        restCall(Method.POST, "/api/donor", create _)
      )
      .withTopics(
        topic(DonorService.TOPIC_NAME, donorTopic)
          .addProperty(
            KafkaProperties.partitionKeyStrategy,
            PartitionKeyStrategy[Donor](_.id)
          )
      )
      .withAutoAcl(true)
  }
}
