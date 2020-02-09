package org.anized.lumina.entities

import java.time.Instant

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import org.anized.lumina.api.domain.model.{PlanType, Status, Donor}
import org.anized.lumina.api.domain.model.PlanType.PlanType
import org.slf4j.LoggerFactory

class DonorEntity extends PersistentEntity {
  private val logger = LoggerFactory.getLogger(this.getClass)
  override type Command = DonorCommand[_]
  override type Event = DonorEvent
  override type State = DonorState

  override def initialState: DonorState = DonorState("",PlanType.T0,status = Status.Created)

  override def behavior: Behavior = {
    case DonorState(_, _, _) =>
      Actions()
        .onCommand[Create, Done] {
          case (Create(email), ctx, state) =>
            logger.debug(s"creating view of $state with email $email")
            ctx.thenPersist(
              Created(Donor("",email), timestamp())
            ) { _ =>
              ctx.reply(Done)
            }
        }
        .onCommand[ChangePlan, Done] {
          case (ChangePlan(newPlan), ctx, state) =>
              logger.debug(s"changing plan of $state to $newPlan")
              ctx.thenPersist(
                PlanChanged(newPlan, timestamp())
              ) { _ =>
                ctx.reply(Done)
              }
          }
        .onCommand[Suspend, Done] {
          case (Suspend(_), ctx, state) if state.status == Status.Suspended =>
            logger.debug(s"cannot suspend $state: Donor already suspended")
            ctx.commandFailed(DonorException("Donor already suspended"))
            ctx.done
          case (Suspend(reason), ctx, state) =>
            logger.debug(s"suspending $state because $reason")
            ctx.thenPersist(
              Suspended(reason, timestamp())
            ) { _ =>
              ctx.reply(Done)
            }
        }
        .onReadOnlyCommand[Get.type, DonorState] {
          case (Get, ctx, state) =>
            ctx.reply(state)
        }
        .onReadOnlyCommand[Lookup, DonorState] {
          case (Lookup(_), ctx, state) =>
            ctx.reply(state)
        }
        .onEvent(eventHandlers)
  }

  def eventHandlers: EventHandler = {
    case (Created(Donor(_, email, _, _), _), state) =>
      state.setEmail(email)
    case (PlanChanged(planType: PlanType, _), state) =>
      state.changePlan(planType)
    case (_: Suspended, state) =>
      state.suspend
  }

  // TODO: inject a time service
  private val timestamp = () => Instant.now()
}
