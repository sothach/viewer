package org.anized.lumina.stream.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import org.anized.lumina.api.domain.model.Donor

/**
  * The donor stream interface.
  *
  * This describes everything that Lagom needs to know about how to serve and
  * consume the DonorStream service.
  */
trait DonorStreamService extends Service {

  def stream: ServiceCall[Source[String, NotUsed], Source[String, NotUsed]]

  override final def descriptor: Descriptor = {
    import Service._

    named("donor-stream")
      .withCalls(
        namedCall("stream", stream)
      ).withAutoAcl(true)
  }
}

