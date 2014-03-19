package dtg

import scala.slick.driver.H2Driver.simple._
 
  class Transactions(tag: Tag) extends Table[(Int, String, String, Double, Int)](tag, "TRANSACTIONS") {
    def id = column[Int]("Id", O.PrimaryKey, O.AutoInc)
    def from = column[String]("FROM")
    def to = column[String]("TO")
    def amount = column[Double]("AMOUNT")
    def block = column[Int]("BLOCK")
    def * = (id, from, to, amount, block)
  }
 
  class Faucet(tag: Tag) extends Table[(String)](tag, "FAUCET") {
    def addr = column[String]("ADDR", O.PrimaryKey)
    def * = (addr)
  }
  
    class Balances(tag: Tag) extends Table[(String, Double)](tag, "BALANCES") {
    def addr = column[String]("ADDR", O.PrimaryKey)
    def amount = column[Double]("AMOUNT")
    def * = (addr, amount)
  }