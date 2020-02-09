package org.anized.lumina.stream.service

import com.lightbend.lagom.scaladsl.api.ServiceCall
import org.anized.lumina.api.DonorService
import org.anized.lumina.stream.api.DonorStreamService

import scala.concurrent.Future

/**
  * Implementation of the DonorStreamService.
  */
class DonorStreamInvoker(donorService: DonorService) extends DonorStreamService {
  def stream = ServiceCall { source =>
    Future.failed(new RuntimeException("not implemented"))
    //Future.successful(source.mapAsync(8)(donorService.get(_).invoke()))
  }
}
