package org.anized.lumina.api.domain

import org.anized.lumina.api.domain.model.PlanType.PlanType
import org.anized.lumina.api.domain.model.Status.Status
import play.api.libs.json.{Format, JsResult, JsValue, Json}

package object model {

  object PlanType extends Enumeration {
    type PlanType = Value
    val T0, T1, T2, T4 = Value
    implicit val format: Format[model.PlanType.Value] = new Format[PlanType.Value] {
      override def writes(o: PlanType.Value): JsValue = Json.toJson(o.toString)
      override def reads(json: JsValue): JsResult[PlanType.Value] = json.validate[String].map(PlanType.withName)
    }
  }
  object Status extends Enumeration {
    type Status = Value
    val Created, Active, Suspended, Closed = Value
    implicit val format: Format[model.Status.Value] = new Format[Status.Value] {
      override def writes(o: Status.Value): JsValue = Json.toJson(o.toString)
      override def reads(json: JsValue): JsResult[Status.Value] = json.validate[String].map(Status.withName)
    }
  }

  case class Donor(id: String, email: String,
                   plan: PlanType = PlanType.T0,
                   status: Status = Status.Created)
  object Donor {
    implicit val format: Format[Donor] = Json.format
  }

  case class Registration(email: String)
  object Registration {
    implicit val format: Format[Registration] = Json.format
  }
}