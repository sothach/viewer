package org.anized.lumina.stream.service

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import org.anized.lumina.api.DonorService
import com.softwaremill.macwire._
import org.anized.lumina.stream.api.DonorStreamService

class DonorStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new DonorStreamApplication(context) {
      override def serviceLocator: NoServiceLocator.type = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new DonorStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[DonorStreamService])
}

abstract class DonorStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[DonorStreamService](wire[DonorStreamInvoker])

  // Bind the DonorService client
  lazy val donorService: DonorService = serviceClient.implement[DonorService]
}
