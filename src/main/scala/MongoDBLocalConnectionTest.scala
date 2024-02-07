import org.mongodb.scala._
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object MongoDBLocalConnectionTest {
  def main(args: Array[String]): Unit = {
    val connectionString = "mongodb://localhost:27017"
    val settings: MongoClientSettings = MongoClientSettings.builder()
      .applyConnectionString(new ConnectionString(connectionString))
      .build()

    val mongoClient: MongoClient = MongoClient(settings)

    try {
      val databaseNamesObservable: Observable[String] = mongoClient.listDatabaseNames()

      val databaseNamesFuture = databaseNamesObservable.toFuture()
      val databaseNames = Await.result(databaseNamesFuture, 10.seconds)

      println("Connection successful. Available databases:")
      databaseNames.foreach(println)
    } catch {
      case ex: Throwable =>
        println(s"Connection failed: ${ex.getMessage}")
    } finally {
      mongoClient.close()
    }
  }
}
