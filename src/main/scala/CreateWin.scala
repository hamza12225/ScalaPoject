import scala.swing._
import scala.swing.event.ButtonClicked
import java.awt.{Dimension, Insets}
import java.text.SimpleDateFormat
import java.util.Date
import org.mongodb.scala._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.swing._
import scala.swing.Dialog.Message.Info
import scala.swing.event.ButtonClicked
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.bson.collection.mutable.Document

import scala.annotation.unused
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.swing.Dialog.showMessage

class CreateWindow extends MainFrame {
  val missionNameField = new TextField(columns = 20)
  val launchDateField = new TextField(columns = 20)
  val rocketTypeField = new TextField(columns = 20)
  val launchSiteField = new TextField(columns = 20)
  val submitButton = new Button("Submit")
  val notification = new Label("")

  val missionNameLabel = new Label("Mission Name:")
  val launchDateLabel = new Label("Launch Date:")
  val rocketTypeLabel = new Label("Rocket Type:")
  val launchSiteLabel = new Label("Launch Site:")

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

  def isValidDate(date: String): Boolean = {
    try {
      dateFormat.parse(date)
      true
    } catch {
      case _: Exception => false
    }
  }

  def saveToMongoDB(missionName: String, launchDate: String, rocketType: String, launchSite: String): Unit = {
    if (!isValidDate(launchDate)) {
      notification.text = "Invalid Launch Date. Please use the format: yyyy-MM-dd"
      return
    }

    val connectionString = "mongodb://localhost:27017"
    val mongoClient: MongoClient = MongoClient(connectionString)
    val database: MongoDatabase = mongoClient.getDatabase("spacex")
    val collection: MongoCollection[Document] = database.getCollection("launches")

    val launchDocument: Document = Document(
      "missionName" -> missionName,
      "launchDate" -> launchDate,
      "rocketType" -> rocketType,
      "launchSite" -> launchSite
    )

    val insertObservable = collection.insertOne(launchDocument)

    insertObservable.toFuture().onComplete {
      case Success(_) =>
        notification.text = "Data added successfully!"
      case Failure(exception) =>
        notification.text = s"Error adding data: ${exception.getMessage}"
    }
    // Close the client after insertion
    mongoClient.close()
  }

  val formPanel = new GridBagPanel {
    val c = new Constraints
    c.insets = new Insets(5, 5, 5, 5)

    c.gridx = 0
    c.gridy = 0
    layout(missionNameLabel) = c

    c.gridx = 1
    c.gridy = 0
    layout(missionNameField) = c

    c.gridx = 0
    c.gridy = 1
    layout(launchDateLabel) = c

    c.gridx = 1
    c.gridy = 1
    layout(launchDateField) = c

    c.gridx = 0
    c.gridy = 2
    layout(rocketTypeLabel) = c

    c.gridx = 1
    c.gridy = 2
    layout(rocketTypeField) = c

    c.gridx = 0
    c.gridy = 3
    layout(launchSiteLabel) = c

    c.gridx = 1
    c.gridy = 3
    layout(launchSiteField) = c

    c.gridx = 0
    c.gridy = 4
    c.gridwidth = 2
    layout(submitButton) = c
  }

  val boxPanel = new BoxPanel(Orientation.Vertical) {
    contents += formPanel
    contents += notification
    border = Swing.EmptyBorder(10, 10, 10, 10)
  }

  contents = boxPanel

  listenTo(submitButton)
  reactions += {
    case ButtonClicked(`submitButton`) =>
      val missionName = missionNameField.text
      val launchDate = launchDateField.text
      val rocketType = rocketTypeField.text
      val launchSite = launchSiteField.text

      saveToMongoDB(missionName, launchDate, rocketType, launchSite)

      showMessage(message = "Launch is created successfully!", title = "Notification", messageType = Info)

      close()
  }
}
