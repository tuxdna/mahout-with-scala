import java.sql.DriverManager
import scala.io.Source
import java.io.File

object PopulateDbPreferences {

  /* 
   * CREATE TABLE taste_preferences ( 
   *  user_id BIGINT NOT NULL, 
   *  item_id BIGINT NOT NULL, 
   *  preference FLOAT NOT NULL, 
   *  PRIMARY KEY (user_id, item_id), 
   *  INDEX (user_id), INDEX (item_id)
   * );
   * 
   * load data infile '/tmp/grouplens/ml-100k/ua.base' into table taste_preferences
   *  fields terminated by '\t' lines terminated by '\n' (user_id, item_id, preference);
   *  
   * Also possible to load data from STDIN like this:
   * 
   * cat /tmp/grouplens/ml-100k/ua.base | mysql mia01 -u root -ppassword 
   *  "load data infile '/dev/stdin' into table taste_preferences
   *  fields terminated by '\t' lines terminated by '\n' (user_id, item_id, preference);"
   *  
   * http://eworbit.blogspot.in/2009/07/loading-mysql-data-via-stdin.html
   * 
   * Disable Apparmor in Ubuntu:
   * http://dijks.wordpress.com/2012/07/06/how-to-disable-apparmor-on-ubuntu-12-04-precise/
   *  
   */

  def main(args: Array[String]) {
    try {
      val HOST = "localhost"
      val DB = "mia01"
      val USER = "root"
      val PASS = "password"
      val connUrl = String.format(
        "jdbc:mysql://%s/%s?user=%s&password=%s",
        HOST, DB, USER, PASS)

      Class.forName("com.mysql.jdbc.Driver")
      val conn = DriverManager.getConnection(connUrl)
      println("Connection created...")
      val query = "INSERT INTO taste_preferences ( user_id, item_id, preference) VALUES (?, ?, ?)"
      val pst = conn.prepareStatement(query)
      println("Statement prepared...")
      val dataFile = new File(args(0))
      val src = Source.fromFile(dataFile)

      println("Read file and insert entries...")
      // ["1", "3", "4", "878542960\n"]
      val x = src.getLines.toSeq.map(l => l.split("\t"))
        .filter(e => e.length >= 3)
        .map(e => (e(0).toLong, e(1).toInt, e(2).toDouble))
        .map { e =>
          println(e)
          val (userId, itemId, preference) = e
          pst.setLong(1, userId)
          pst.setLong(2, itemId)
          pst.setDouble(3, preference)
          pst.execute()
          e
        }
      println(x.length)
    } catch {
      case e: Throwable => e.printStackTrace()
    }
  }
}