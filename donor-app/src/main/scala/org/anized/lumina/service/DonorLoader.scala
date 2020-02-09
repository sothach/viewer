package org.anized.lumina.service

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.slick.SlickPersistenceComponents
import play.api.libs.ws.ahc.AhcWSComponents
import org.anized.lumina.api.DonorService
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.softwaremill.macwire._
import org.anized.lumina.entities.{DonorEntity, DonorSerializerRegistry}
import org.anized.lumina.persistence.DonorRepository
import play.api.db.HikariCPComponents

class DonorLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new DonorApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new DonorApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[DonorService])
}

abstract class DonorApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with LagomKafkaComponents
    with SlickPersistenceComponents
    with HikariCPComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[DonorService](wire[DonorServiceRepo])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = DonorSerializerRegistry

  // Register the donor persistent entity
  persistentEntityRegistry.register(wire[DonorEntity])

  lazy val donorRepository: DonorRepository = wire[DonorRepository]

  readSide.register(wire[DonorProcessor])
}
