import scala.swing._
import scala.swing.event.ButtonClicked
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.bson.collection.mutable.Document
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.set

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.swing.Dialog.Message.Info
import scala.swing.Dialog._

class ModifyLaunchWindow(id: ObjectId, currentData: Seq[String],updateCallback: () => Unit) extends MainFrame {
  title = "Modify Launch"
  preferredSize = new Dimension(400, 200)

  // Define Swing components for modifying data (TextField, Button, etc.)
  val missionNameField = new TextField(currentData(1))
  val launchDateField = new TextField(currentData(2))
  val rocketTypeField = new TextField(currentData(3))
  val launchSiteField = new TextField(currentData(4))

  val submitButton = new Button("Modify")
  val closeButton = new Button("Close")

  // Example: Layout
  val formPanel = new GridBagPanel {
    val c = new Constraints
    c.insets = new Insets(5, 5, 5, 5)

    // Add your components to the layout
    c.gridx = 0
    c.gridy = 0
    layout(new Label("Mission Name:")) = c
    c.gridx = 1
    layout(missionNameField) = c

    c.gridx = 0
    c.gridy = 1
    layout(new Label("Launch Date:")) = c
    c.gridx = 1
    layout(launchDateField) = c

    c.gridx = 0
    c.gridy = 2
    layout(new Label("Rocket Type:")) = c
    c.gridx = 1
    layout(rocketTypeField) = c

    c.gridx = 0
    c.gridy = 3
    layout(new Label("Launch Site:")) = c
    c.gridx = 1
    layout(launchSiteField) = c
  }

  contents = new BoxPanel(Orientation.Vertical) {
    contents += formPanel
    contents += submitButton
    contents += closeButton
    border = Swing.EmptyBorder(10, 10, 10, 10)
  }

  listenTo(submitButton, closeButton)

  def modifyLaunchInMongoDB(id: ObjectId, missionName: String, launchDate: String, rocketType: String, launchSite: String): Unit = {
    val connectionString = "mongodb://localhost:27017"
    val mongoClient: MongoClient = MongoClient(connectionString)
    val database: MongoDatabase = mongoClient.getDatabase("spacex")
    val collection: MongoCollection[Document] = database.getCollection("launches")

    // Update the document with the modified data
    val updateObservable = collection.updateOne(
      equal("_id", id),
      set("missionName", missionName) ::
        set("launchDate", launchDate) ::
        set("rocketType", rocketType) ::
        set("launchSite", launchSite) :: Nil
    )

    // Wait for the update operation to complete
    try {
      val updateResult = Await.result(updateObservable.toFuture(), Duration.Inf)

      if (updateResult.wasAcknowledged() && updateResult.getModifiedCount > 0) {
        println(s"Launch with ID $id modified successfully.")
      } else {
        println(s"No launches modified for ID $id.")
      }
    } catch {
      case e: Throwable =>
        println(s"Error modifying launch with ID $id: ${e.getMessage}")
    } finally {
      // Close the client after modification or in case of an error
      mongoClient.close()
    }
  }

  reactions += {
    case ButtonClicked(`submitButton`) =>
      val modifiedMissionName = missionNameField.text
      val modifiedLaunchDate = launchDateField.text
      val modifiedRocketType = rocketTypeField.text
      val modifiedLaunchSite = launchSiteField.text

      modifyLaunchInMongoDB(id, modifiedMissionName, modifiedLaunchDate, modifiedRocketType, modifiedLaunchSite)
      showMessage(message = "Launch modified successfully!", title = "Notification", messageType = Info)
      updateCallback()


      close()

    case ButtonClicked(`closeButton`) =>
      close()
  }

}

