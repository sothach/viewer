package org.anized.lumina.persistence

import akka.Done
import org.anized.lumina.api.domain.model.PlanType.PlanType
import org.anized.lumina.api.domain.model.{Donor, PlanType, Status}
import slick.ast.BaseTypedType
import slick.dbio.{DBIO, Effect}
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DonorRepository(database: Database) {

  class DonorTable(tag: Tag) extends Table[Donor](tag, "donor") {
    def donorId  = column[String]   ("donor_id", O.PrimaryKey)
    def email     = column[String]   ("email")
    def plan      = column[PlanType] ("plan")
    def status    = column[Status.Value]   ("status")

    def * = (donorId, email, plan, status) <> ((Donor.apply _).tupled, Donor.unapply)
    implicit val planMapper: JdbcType[PlanType] with BaseTypedType[PlanType] = MappedColumnType.base[PlanType, String](
      e => e.toString,
      s => PlanType.withName(s))
    implicit val statusMapper: JdbcType[Status.Value] with BaseTypedType[Status.Value] = MappedColumnType.base[Status.Value, String](
      e => e.toString,
      s => Status.withName(s))
  }

  val donorTable = TableQuery[DonorTable]

  def createTable(): DBIOAction[Unit, NoStream, Effect.Schema] =
    donorTable.schema.createIfNotExists

  def findById(id: String): Future[Option[Donor]] =
    database.run(findByIdQuery(id))

  def create(donor: Donor): DBIO[Done] =
    findByIdQuery(donor.id).flatMap {
      case None => donorTable += donor
      case _ => DBIO.successful(Done)
    }.map(_ => Done).transactionally

  def update(donor: Donor): DBIO[Done] = {
    donorTable.filter(_.donorId === donor.id)
      .update(donor)
    }.map(_ => Done).transactionally

  def setPlan(donorId: String, planType: PlanType): DBIO[Done] = {
    findByIdQuery(donorId).flatMap {
      case Some(donor) => donorTable.insertOrUpdate(donor.copy(plan = planType))
      case None => throw new RuntimeException(s"could not find donor to set plan, donorId: $donorId")
    }.map(_ => Done).transactionally
  }

  private def findByIdQuery(donorId: String): DBIO[Option[Donor]] =
    donorTable
      .filter(_.donorId === donorId)
      .result.headOption
}
