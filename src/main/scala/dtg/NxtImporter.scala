package dtg

import org.joda.time.DateTime
import scala.slick.driver.H2Driver.simple._

object NxtImporter extends App {

  // The query interface 
  val transactions: TableQuery[NxtTransaction] = TableQuery[NxtTransaction]

  def csvFile = "NtxTx-" + DateTime.now().toString("yyyMMdd").toString() + ".csv"

  def exportCSV(transactions: List[(String, String, Int)]) =
    {
      val file = new java.io.PrintWriter(csvFile)

      var i = 0
      for (tx <- transactions) {
        file.println(String.join(",", tx._1, tx._2, tx._3.toString))
        i = i + 1
      }
      println("tx: " + i)
    }

  def getTransactions(arg: String): List[(String, String, Int)] = {
    Database.forURL("jdbc:h2:" + arg, "sa", "sa", null, driver = "org.h2.Driver") withSession {
      implicit session =>
        val q = for { tx <- transactions }
          yield (tx.senderId, tx.recipientId, tx.amount)
        q.list
    }
  }

  val t = getTransactions("""E:\nxt-client-0.8.9\nxt\nxt_db\nxt""")
  exportCSV(t)
}
