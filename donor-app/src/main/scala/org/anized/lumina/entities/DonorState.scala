package org.anized.lumina.entities

import java.time.Instant

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag}
import org.anized.lumina.api.domain.model.PlanType.PlanType
import org.anized.lumina.api.domain.model.Status.Status
import org.anized.lumina.api.domain.model.{Status, Donor}
import play.api.libs.json._

case class DonorState(email: String, plan: PlanType, status: Status) {
  def materialize(id: String) = Donor(id,email,plan,status)
  def changePlan(newPlan: PlanType): DonorState = copy(plan = newPlan)
  def setEmail(email: String): DonorState = copy(email = email)
  def suspend: DonorState = copy(status = Status.Suspended)
}
object DonorState {
  implicit val format: Format[DonorState] = Json.format
}

sealed trait DonorEvent extends AggregateEvent[DonorEvent] {
  val eventTime: Instant
  def aggregateTag: AggregateEventShards[DonorEvent] = DonorEvent.Tag
}
object DonorEvent {
  val Tag: AggregateEventShards[DonorEvent] = AggregateEventTag.sharded[DonorEvent](10)
}

case class DonorUpdated(donor: Donor, eventTime: Instant) extends DonorEvent
object DonorUpdated {
  implicit val format: Format[DonorUpdated] = Json.format
}

case class PlanChanged(newPlan: PlanType, eventTime: Instant) extends DonorEvent
object PlanChanged {
  implicit val format: Format[PlanChanged] = Json.format
}

case class Created(donor: Donor, eventTime: Instant) extends DonorEvent
object Created {
  implicit val format: Format[Created] = Json.format
}

case class Suspended(planName: String, eventTime: Instant) extends DonorEvent
object Suspended {
  implicit val format: Format[Suspended] = Json.format
}

sealed trait DonorCommand[R] extends ReplyType[R]

case object Get extends DonorCommand[DonorState] {
  implicit val format: Format[Get.type] = Format(
    Reads(_ => JsSuccess(Get)),
    Writes(_ => Json.obj())
  )
}

case class Lookup(email: String) extends DonorCommand[DonorState]
object Lookup {
  implicit val format: Format[Lookup] = Json.format
}

case class Create(email: String) extends DonorCommand[Done]
object Create {
  implicit val format: Format[Create] = Json.format
}

case class ChangePlan(newPlan: PlanType) extends DonorCommand[Done]
object ChangePlan {
  implicit val format: Format[ChangePlan] = Json.format
}

case class Suspend(reason: String) extends DonorCommand[Done]
object Suspend {
  implicit val format: Format[Suspend] = Json.format
}

case class DonorException(message: String) extends RuntimeException(message)
object DonorException {
  implicit val format: Format[DonorException] = Json.format[DonorException]
}