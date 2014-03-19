package dtg

import scala.slick.driver.H2Driver.simple._
 
object Exp extends App {
 
  val balances = TableQuery[Balances]
  val transactions = TableQuery[Transactions]
  val faucet = TableQuery[Faucet]
 
  Database.forURL("jdbc:h2:msc", driver = "org.h2.Driver") withSession {
    implicit session =>
 
      if (false) { // to create db and insert data
 
        (balances.ddl ++ transactions.ddl ++ faucet.ddl).create
 
        try {
          val fis = new java.io.FileInputStream("balancesMSC-all-20140318.csv")
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
          val fis = new java.io.FileInputStream("txMSC-all-20140318.csv")
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
 
      println("Reached addresses: " + faucetTx.run.groupBy(t => t._2).map(t => t._1).toSeq.length)
 
      println("RecieverBalances:")
 
      val bal = (for {
        f <- faucetTx
        b <- balances if f._2 === b.addr
      } yield (b.addr, b.amount)).run
 
      bal.foreach(b => println(b._1 + " " + b._2))
 
      println("Duplicates:")
 
      val duplicateFaucetPayments = faucetTx.run.groupBy(l => l._2)
        .map(t => (t._1, t._2.length, t._2))
        .filter(t => t._2 > 1)
        .map(t => (t._1, t._2, t._3.map(t => t._3).sum))
        .toSeq.sortBy(_._3).reverse
 
      // receiver, #tx, total amount
      duplicateFaucetPayments.foreach(t => println(t._1 + " " + t._2 + " " + t._3))
 
      println("Second tx:")
      // secondary tx
      val tx2nd = for {
        f <- faucetTx // <- need to be unique receiver
        t <- transactions if f._2 === t.from && f._4 <= t.block
      } yield (t.from, t.to, t.amount, t.block, if (t.to == f._2) true else false)
 
      val tx2res = tx2nd.run
      println(tx2res.length)
      println("Loop backs: " + tx2res.filter(t => t._5).length)
      println("Reached addresses: " + tx2res.groupBy(t => t._2).map(t => t._1).toSeq.length)
 
      // Secondary balances
      val bal2nd = (for {
        f <- tx2nd
        b <- balances if f._2 === b.addr
      } yield (b.addr, b.amount)).run
 
      println("Secondary balances:")
      bal.foreach(b => println(b._1 + " " + b._2))
 
      println("Third tx:")
      // secondary tx
      val tx3rd = for {
        prev <- tx2nd // <- need to be unique receiver
        t <- transactions if prev._2 === t.from && prev._4 <= t.block
      } yield (t.from, t.to, t.amount, t.block, if (t.to == prev._2) true else false)
 
      val tx3res = tx3rd.run
      println(tx3res.length)
      println("Loop backs: " + tx3res.filter(t => t._5).length)
 
      val f = faucetTx.run
      val loop = tx3res.map(t => f.filter(f => f._2 == t._2))
      println(loop.length)
 
      println("Reached addresses: " + tx3res.groupBy(t => t._2).map(t => t._1).toSeq.length)
 
      println("Fourth tx:")
      // secondary tx
      val tx4th = for {
        prev <- tx3rd // <- need to be unique receiver
        t <- transactions if prev._2 === t.from && prev._4 <= t.block
      } yield (t.from, t.to, t.amount, t.block, if (t.to == prev._2) true else false)
 
      val tx4res = tx4th.run
      println(tx4res.length)
      println("Loop backs: " + tx4res.filter(t => t._5).length)
      println("Reached addresses: " + tx4res.groupBy(t => t._2).map(t => t._1).toSeq.length)
 
      println("completed")
  }
}