import java.awt.{Dimension, Insets}
import java.text.SimpleDateFormat
import java.util.Date
import org.mongodb.scala._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.swing._
import scala.swing.event.ButtonClicked
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.bson.collection.mutable.Document

import scala.swing.Dialog.Message.Info
import scala.swing.Dialog.showMessage
import scala.util.{Failure, Success}

class AddDataWindow(updateCallback: () => Unit, onDataSaved: () => Unit) extends Frame {
  title = "Add Launch Data"
  size = new Dimension(400, 200)

  val missionNameField = new TextField {
    columns = 20
  }
  val launchDateField = new TextField {
    columns = 20
  }
  val rocketTypeField = new TextField {
    columns = 20
  }
  val launchSiteField = new TextField {
    columns = 20
  }

  val saveButton = new Button("Save") {
    reactions += {
      case ButtonClicked(_) =>
        saveData()
        onDataSaved()
        showMessage(message = "Launch created successfully!", title = "Notification", messageType = Info)
        updateCallback()
        close()
    }
  }

  val cancelButton = new Button("Cancel") {
    reactions += {
      case ButtonClicked(_) => close()
    }
  }

  contents = new BoxPanel(Orientation.Vertical) {
    contents += new FlowPanel {
      contents += new Label("Mission Name:")
      contents += Swing.HStrut(5)
      contents += missionNameField
    }
    contents += new FlowPanel {
      contents += new Label("Launch Date:")
      contents += Swing.HStrut(18)
      contents += launchDateField
    }
    contents += new FlowPanel {
      contents += new Label("Rocket Type:")
      contents += Swing.HStrut(15)
      contents += rocketTypeField
    }
    contents += new FlowPanel {
      contents += new Label("Launch Site:")
      contents += Swing.HStrut(20)
      contents += launchSiteField
    }
    contents += Swing.VStrut(10)
    contents += new FlowPanel {
      contents += saveButton
      contents += Swing.HStrut(10)
      contents += cancelButton
    }
  }
  def saveData(): Unit = {
    val connectionString = "mongodb://localhost:27017"
    val mongoClient: MongoClient = MongoClient(connectionString)

    try {
      val database: MongoDatabase = mongoClient.getDatabase("spacex")
      val collection: MongoCollection[Document] = database.getCollection("launches")

      val document = Document(
        "missionName" -> missionNameField.text,
        "launchDate" -> launchDateField.text,
        "rocketType" -> rocketTypeField.text,
        "launchSite" -> launchSiteField.text
      )

      collection.insertOne(document).toFuture().onComplete {
        case Success(_) =>
          println("Data saved successfully.")

        case Failure(exception) =>
          println(s"Failed to save data: ${exception.getMessage}")
      }
    } finally {
      mongoClient.close()
    }
  }
}
