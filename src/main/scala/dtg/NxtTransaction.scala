package dtg

import scala.slick.driver.H2Driver.simple._
import scala.math.BigInt

class NxtTransaction(tag: Tag) extends Table[(Int, String, Int, String, String, Int, Int, Option[Int], Int, String, String, Int, Int, Int, String, Option[String], Int, String)](tag, "TRANSACTION") {
  def dbId = column[Int]("DB_ID", O.PrimaryKey)
  def id = column[String]("ID")
  def deadline = column[Int]("DEADLINE")
  def senderPublicKey = column[String]("SENDER_PUBLIC_KEY")
  def recipientId = column[String]("RECIPIENT_ID")
  def amount = column[Int]("AMOUNT")
  def fee = column[Int]("FEE")
  def referencedTransactionId = column[Option[Int]]("REFERENCED_TRANSACTION_ID")
  def height = column[Int]("HEIGHT")
  def blockId = column[String]("BLOCK_ID")
  def signature = column[String]("SIGNATURE")
  def timestamp = column[Int]("TIMESTAMP")
  def txType = column[Int]("TYPE")
  def subtype = column[Int]("SUBTYPE")
  def senderId = column[String]("SENDER_ID")
  def attachment = column[Option[String]]("ATTACHMENT")
  def blockTimestamp = column[Int]("BLOCK_TIMESTAMP")
  def hash = column[String]("HASH")
  def * = (dbId, id, deadline, senderPublicKey, recipientId, amount, fee, referencedTransactionId, height, blockId, signature, timestamp, txType, subtype, senderId, attachment, blockTimestamp, hash)
}