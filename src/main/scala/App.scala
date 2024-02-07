import org.mongodb.scala._
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.collection.mutable.Document
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.result.DeleteResult
import javax.swing.table.AbstractTableModel
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.swing._
import scala.swing.event.ButtonClicked
import scala.util.{Failure, Success}
import scala.swing.BorderPanel.Position

object App extends SimpleSwingApplication {
  private var TableModel: LaunchTableModel = _

  // Function to fetch launches from MongoDB
  def fetchLaunchesFromMongoDB(): Seq[Array[Any]] = {
    val connectionString = "mongodb://localhost:27017"
    val mongoClient: MongoClient = MongoClient(connectionString)
    val database: MongoDatabase = mongoClient.getDatabase("spacex")
    val collection: MongoCollection[Document] = database.getCollection("launches")

    val launches = collection.find().toFuture().map { result =>
      result.map { doc =>
        Array[Any](
          doc.get("_id").get.asObjectId().getValue.toHexString,
          doc.get("missionName").get.asString().getValue,
          doc.get("launchDate").get.asString().getValue,
          doc.get("rocketType").get.asString().getValue,
          doc.get("launchSite").get.asString().getValue
        )
      }
    }
    Await.result(launches, Duration.Inf).toSeq
  }

  def fetchLaunchesFromMongoDBAndUpdateTable(): Unit = {
    val connectionString = "mongodb://localhost:27017"
    val mongoClient: MongoClient = MongoClient(connectionString)
    val database: MongoDatabase = mongoClient.getDatabase("spacex")
    val collection: MongoCollection[Document] = database.getCollection("launches")

    val launches = collection.find().toFuture().map { result =>
      result.map { doc =>
        Array[Any](
          doc.get("_id").get.asObjectId().getValue.toHexString,
          doc.get("missionName").get.asString().getValue,
          doc.get("launchDate").get.asString().getValue,
          doc.get("rocketType").get.asString().getValue,
          doc.get("launchSite").get.asString().getValue
        )
      }
    }

    launches.onComplete {
      case Success(updatedLaunches) =>
        Swing.onEDT {
          TableModel.updateData(updatedLaunches)
        }
      case Failure(exception) =>
        println(s"Failed to fetch launches: ${exception.getMessage}")
    }

    // Close the client after fetching
    mongoClient.close()
  }
  def deleteLaunchFromMongoDB(id: ObjectId): Unit = {
    val connectionString = "mongodb://localhost:27017"
    val mongoClient: MongoClient = MongoClient(connectionString)
    val database: MongoDatabase = mongoClient.getDatabase("spacex")
    val collection: MongoCollection[Document] = database.getCollection("launches")

    import org.mongodb.scala.model.Filters._
    val deleteResult: Future[DeleteResult] = collection.deleteOne(equal("_id", id)).toFuture()

    try {
      val result = Await.result(deleteResult, Duration.Inf) // Wait for the delete operation to complete
      if (result.wasAcknowledged() && result.getDeletedCount > 0) {
        println(s"Launch with ID $id deleted successfully")
        // Fetch updated launches and update the table on the EDT
        fetchLaunchesFromMongoDBAndUpdateTable()
      } else {
        println(s"No launches deleted for ID $id")
      }
    } catch {
      case exception: Throwable =>
        println(s"Failed to delete launch with ID $id: ${exception.getMessage}")
    } finally {
      // Close the client after deletion or in case of an error
      mongoClient.close()
    }
  }

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


  // Custom TableModel class
  class LaunchTableModel(columnNames: Seq[String], data: Seq[Array[Any]]) extends AbstractTableModel {
    private var launchData: Seq[Array[Any]] = data

    def updateData(newData: Seq[Array[Any]]): Unit = {
      launchData = newData
      fireTableDataChanged() // Notify the table that the data has changed
    }
    override def getColumnName(column: Int): String = columnNames(column)
    override def getColumnCount: Int = columnNames.length
    override def getRowCount: Int = launchData.length
    override def getValueAt(row: Int, col: Int): Any = launchData(row)(col)
  }

  def createLaunchesWindow(): Frame = {
    val launches = fetchLaunchesFromMongoDB()

    val columnNames = Seq("ID", "Mission Name", "Launch Date", "Rocket Type", "Launch Site")
    val tableModel = new LaunchTableModel(columnNames, launches)
    TableModel = new LaunchTableModel(columnNames, launches)
    val table = new Table() {
      model = tableModel
      autoResizeMode = Table.AutoResizeMode.AllColumns
      rowHeight = 25
    }

    val closeButton = new Button("Close")
    val deleteButton = new Button("Delete Selected Row")
    val modifyButton = new Button("Modify Selected Row")
    val createButton = new Button("Add data")

    val selectedRow = table.selection

    listenTo(closeButton, deleteButton, modifyButton,createButton)
    reactions += {
      case ButtonClicked(`closeButton`) =>
        closeApp()
      case ButtonClicked(`deleteButton`) =>
        if (selectedRow.rows.size == 1) {
          val id = tableModel.getValueAt(selectedRow.rows.head, 0).toString // Assuming the ID is in the first column
          val idx: ObjectId = new ObjectId(id)
          deleteLaunchFromMongoDB(idx)
          val updatedLaunches = fetchLaunchesFromMongoDB()
          tableModel.updateData(updatedLaunches)
        } else {
          showNotification("Please choose a row to delete.")
        }
      case ButtonClicked(`modifyButton`) =>
        if (selectedRow.rows.size == 1) {
          val id = tableModel.getValueAt(selectedRow.rows.head, 0).toString
          val currentData = Seq(
            "", // Placeholder for ID
            tableModel.getValueAt(selectedRow.rows.head, 1).toString,
            tableModel.getValueAt(selectedRow.rows.head, 2).toString,
            tableModel.getValueAt(selectedRow.rows.head, 3).toString,
            tableModel.getValueAt(selectedRow.rows.head, 4).toString
          )
          val idx: ObjectId = new ObjectId(id)
          val modifyWindow = new ModifyLaunchWindow(idx, currentData, () => {
            Swing.onEDT {
              tableModel.updateData(fetchLaunchesFromMongoDB())
            }
          })
          modifyWindow.visible = true
        } else {
          showNotification("Please choose a row to modify.")
        }
      case ButtonClicked(`createButton`) =>
        println("Create button clicked!")
        val addDataWindow = new AddDataWindow(() => {
          Swing.onEDT {
            tableModel.updateData(fetchLaunchesFromMongoDB())
          }
        }, () => {})
        addDataWindow.visible = true
      case ButtonClicked(`closeButton`) =>
        closeApp()
    }
    def showNotification(message: String): Unit = {
      val notificationDialog = new Dialog() {
        title = "Notification"
        modal = true
        resizable = false

        val okButton = Button("OK") {
          close()
        }

        contents = new BorderPanel {
          add(new Label(message), Position.Center)
          add(okButton, Position.South)
        }

        peer.getRootPane().setDefaultButton(okButton.peer) // Set default button to OK
        preferredSize = new Dimension(300, 120) // Adjust the preferred size
        centerOnScreen() // Center the dialog on the screen
      }

      notificationDialog.open()
    }

    def closeApp(): Unit = {
      sys.exit(0)
    }

    val buttonPanel = new BoxPanel(Orientation.Horizontal) {
      contents += createButton
      contents += Swing.HStrut(10)  // Add some horizontal spacing
      contents += modifyButton
      contents += Swing.HStrut(10)
      contents += deleteButton
      contents += Swing.HStrut(10)
      contents += closeButton
    }

    val mainPanel = new BoxPanel(Orientation.Vertical) {
      contents += new ScrollPane(table)
      contents += buttonPanel
      border = Swing.EmptyBorder(20, 20, 20, 20)
    }

    val launchesFrame = new Frame {
      title = "SpaceX Launches"
      preferredSize = new Dimension(800, 400)
      contents = mainPanel
    }

    launchesFrame
  }
  override def top: Frame = createLaunchesWindow()
}
