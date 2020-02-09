package org.anized.lumina.service

import com.lightbend.lagom.scaladsl.persistence.slick.SlickReadSide
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, ReadSideProcessor}
import org.anized.lumina.entities.{Created, PlanChanged, DonorEvent, DonorUpdated}
import org.anized.lumina.persistence.DonorRepository

/* update read model database from change events */
class DonorProcessor(readSide: SlickReadSide,
                     repository: DonorRepository) extends ReadSideProcessor[DonorEvent] {

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[DonorEvent] =
    readSide
      .builder[DonorEvent]("donor-offset")
      .setGlobalPrepare(repository.createTable())
      .setEventHandler[Created] { envelope =>
        repository.create(envelope.event.donor)
      }
      .setEventHandler[DonorUpdated] { envelope =>
        repository.update(envelope.event.donor)
      }
      .setEventHandler[PlanChanged] { envelope =>
        repository.setPlan(envelope.entityId, envelope.event.newPlan)
      }
      .build()

  override def aggregateTags: Set[AggregateEventTag[DonorEvent]] = DonorEvent.Tag.allTags
}
