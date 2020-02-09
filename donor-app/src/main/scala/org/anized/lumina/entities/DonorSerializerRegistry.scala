package org.anized.lumina.entities

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import org.anized.lumina.api.domain.model.Donor

import scala.collection.immutable.Seq

object DonorSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[Donor],
    JsonSerializer[DonorUpdated],
    JsonSerializer[DonorState],
    JsonSerializer[PlanChanged],
    JsonSerializer[Suspended],
    JsonSerializer[Get.type],
    JsonSerializer[Create],
    JsonSerializer[Lookup],
    JsonSerializer[Created],
    JsonSerializer[ChangePlan],
    JsonSerializer[Suspend],
    JsonSerializer[DonorException]
  )
}