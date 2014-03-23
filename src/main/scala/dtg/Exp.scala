package dtg

import scala.slick.driver.H2Driver.simple._

object Exp extends App {

  val balances = TableQuery[Balances]
  val transactions = TableQuery[Transactions]
  val faucet = TableQuery[Faucet]

  def ExportBalances(tx: Seq[(String, Double)], fileName: String) =
    {
      val f = new java.io.File(fileName)
      val p = new java.io.PrintWriter(f)

      tx.foreach(t => p.println(t._1 + "," + t._2))

      p.close()
    }

  def ExportDuplicates(tx: Seq[(String, Int, Double)], fileName: String) =
    {
      val f = new java.io.File(fileName)
      val p = new java.io.PrintWriter(f)

      tx.foreach(t => p.println(t._1 + "," + t._2 + "," + t._3))

      p.close()
    }

  def ExportTx(tx: Seq[(String, String, Double, Int, Boolean)], fileName: String) =
    {
      val f = new java.io.File(fileName)
      val p = new java.io.PrintWriter(f)

      tx.foreach(t => p.println(t._1 + "," + t._2 + "," + t._3 + "," + t._4 + "," + t._5))

      p.close()
    }

  // Placeholder for transactions being processed
  val workingTransactions = TableQuery[WorkingTransactions]

  Database.forURL("jdbc:h2:tmsc", driver = "org.h2.Driver") withSession {
    implicit session =>

      if (true) { // to create db and insert data

        (balances.ddl ++ transactions.ddl ++ faucet.ddl ++ workingTransactions.ddl).create

        try {
          val fis = new java.io.FileInputStream("balancesTMSC-all-20140318.csv")
          val reader = new java.io.BufferedReader(new java.io.InputStreamReader(fis));

          var line = reader.readLine()
          while (line != null) {
            val s = line.split(",")
            balances.insert(s(0), s(1).toDouble)
            line = reader.readLine()
          }
          fis.close()
          reader.close()
        } finally {

        }
        try {
          val fis = new java.io.FileInputStream("txTMSC-all-20140318.csv")
          val reader = new java.io.BufferedReader(new java.io.InputStreamReader(fis));

          var line = reader.readLine()
          while (line != null) {
            val s = line.split(",")
            transactions.insert(0, s(0), s(1), s(2).toDouble, s(3).toInt)
            line = reader.readLine()
          }
          fis.close()
          reader.close()
        } finally {

        }
        try {
          val fis = new java.io.FileInputStream("faucet.txt")
          val reader = new java.io.BufferedReader(new java.io.InputStreamReader(fis));

          var line = reader.readLine()
          while (line != null) {
            faucet.insert(line)
            line = reader.readLine()
          }
          fis.close()
          reader.close()
        } finally {

        }
      }

      val faucetTx = (for {
        f <- faucet
        tx <- transactions if f.addr === tx.from
      } yield (f.addr, tx.to, tx.amount, tx.block))

      println("Faucet tx:")
      println(faucetTx.run.length)

      println("Reached addresses: " + faucetTx.run.groupBy(_._2).map(_._1).toSeq.length)

      val bal1 = (for {
        f <- faucetTx
        b <- balances if f._2 === b.addr
      } yield (b.addr, b.amount)).run

      ExportBalances(bal1, "tmscBalances1st.csv")

      //  bal.foreach(b => println(b._1 + " " + b._2))

      println("Duplicates:")

      val duplicateFaucetPayments = faucetTx.run.groupBy(_._2)
        .map(t => (t._1, t._2.length, t._2))
        .filter(_._2 > 1)
        .map(t => (t._1, t._2, t._3.map(t => t._3).sum))
        .toSeq.sortBy(_._3).reverse

      // receiver, #tx, total amount
      ExportDuplicates(duplicateFaucetPayments, "duplicatesTMSC.csv")
      //duplicateFaucetPayments.foreach(t => println(t._1 + " " + t._2 + " " + t._3))

      println("Deleted transactions: " + workingTransactions.delete)
      val uniqueRecipients = faucetTx.run.groupBy(_._2).map(t => (t._1, t._2)).map(t => (t._1, t._2.map(_._3).sum, t._2.map(_._4).max))
      for (u <- uniqueRecipients)
        workingTransactions += u

      println("Second tx:")
      // secondary tx
      val tx2nd = for {
        f <- workingTransactions // <- need to be unique receiver
        t <- transactions if f.addr === t.from && f.block <= t.block
      } yield (t.from, t.to, t.amount, t.block, if (t.to == f.addr) true else false)

      val tx2res = tx2nd.run
      println(tx2res.length)
      println("Loop backs: " + tx2res.filter(t => t._5).length)

      ExportTx(tx2res, "TMSCsecond.csv")

      // need to remove duplicates here
      val bal2 = (for {
        f <- tx2nd
        b <- balances if f._2 === b.addr
      } yield (b.addr, b.amount)).run

      //sender balances set to zero
      val bal2From = (for {
        t <- tx2nd
        b <- balances if t._1 === b.addr
      } yield (b.addr, 0.0)).run

      ExportBalances(bal2 ++ bal2From, "tmscBalances2nd.csv")

      // Secondary balances
      val bal2nd = (for {
        f <- tx2nd
        b <- balances if f._2 === b.addr
      } yield (b.addr, b.amount)).run

      // println("Secondary balances:")
      //bal.foreach(b => println(b._1 + " " + b._2))

      println("Deleted transactions: " + workingTransactions.delete)
      // receiver, sum amount, highest block number
      val uniqueRecipients2 = tx2res.groupBy(_._2).map(t => (t._1, t._2)).map(t => (t._1, t._2.map(_._3).sum, t._2.map(_._4).max))
      for (u <- uniqueRecipients2)
        workingTransactions += u

      println("Reached addresses (2nd): " + uniqueRecipients2.toSeq.length)

      println("Third tx:")
      // secondary tx
      val tx3rd = for {
        prev <- workingTransactions // <- need to be unique receiver
        t <- transactions if prev.addr === t.from && prev.block <= t.block
      } yield (t.from, t.to, t.amount, t.block, if (t.to == prev.addr) true else false)

      val tx3res = tx3rd.run
      println(tx3res.length)
      println("Loop backs: " + tx3res.filter(t => t._5).length)

      ExportTx(tx3res, "TMSCthird.csv")

      val bal3 = (for {
        f <- tx3rd
        b <- balances if f._2 === b.addr
      } yield (b.addr, b.amount)).run

      //sender balances set to zero
      val bal3From = (for {
        t <- tx3rd
        b <- balances if t._1 === b.addr
      } yield (b.addr, 0.0)).run

      ExportBalances(bal3 ++ bal3From, "tmscBalances3rd.csv")

      val f = faucetTx.run
      val loop = tx3res.map(t => f.filter(f => f._2 == t._2))
      println(loop.length)

      println("Deleted transactions: " + workingTransactions.delete)

      // receiver, sum amount, highest block number
      val uniqueRecipients3 = tx3res.groupBy(_._2).map(t => (t._1, t._2)).map(t => (t._1, t._2.map(_._3).sum, t._2.map(_._4).max))
      for (u <- uniqueRecipients3)
        workingTransactions += u

      println("Reached addresses (3rd): " + uniqueRecipients3.toSeq.length)

      println("Fourth tx:")
      // secondary tx
      val tx4th = for {
        prev <- workingTransactions // <- need to be unique receiver
        t <- transactions if prev.addr === t.from && prev.block <= t.block
      } yield (t.from, t.to, t.amount, t.block, if (t.to == prev.addr) true else false)

      val tx4res = tx4th.run
      println(tx4res.length)
      println("Loop backs: " + tx4res.filter(t => t._5).length)
      println("Reached addresses: " + tx4res.groupBy(t => t._2).map(t => t._1).toSeq.length)

      ExportTx(tx4res, "TMSCfourth.csv")

      val bal4 = (for {
        f <- tx4th
        b <- balances if f._2 === b.addr
      } yield (b.addr, b.amount)).run

      //sender balances set to zero
      val bal4From = (for {
        t <- tx4th
        b <- balances if t._1 === b.addr
      } yield (b.addr, 0.0)).run

      ExportBalances(bal4 ++ bal4From, "tmscBalances4th.csv")

      println("completed")
  }
}