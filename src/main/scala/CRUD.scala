import scala.swing._
import scala.swing.event.ButtonClicked

object SpaceXFormh extends SimpleSwingApplication {

  def createLaunchWindow(): Dialog = {
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

    val formPanel = new GridBagPanel {
      val c = new Constraints
      c.insets = new Insets(5, 5, 5, 5)

      c.gridx = 0
      c.gridy = 0
      layout(missionNameLabel) = c

      c.gridx = 1
      c.gridy = 0
      layout(missionNameField) = c

      // Add similar layout for other fields and labels...

      c.gridx = 0
      c.gridy = 4
      c.gridwidth = 2
      layout(submitButton) = c
    }

    listenTo(submitButton)
    reactions += {
      case ButtonClicked(`submitButton`) =>
        val missionName = missionNameField.text
        val launchDate = launchDateField.text
        val rocketType = rocketTypeField.text
        val launchSite = launchSiteField.text

      // Save to MongoDB logic
      // ...

    }

    val createDialog = new Dialog {
      title = "Create SpaceX Launch"
      contents = new BoxPanel(Orientation.Vertical) {
        contents += formPanel
        contents += notification
        border = Swing.EmptyBorder(10, 10, 10, 10)
      }
    }

    createDialog
  }

  def createReadWindow(launches: List[String]): Dialog = {
    val launchList = new ListView[String](launches)
    val removeButton = new Button("Remove Selected")

    val readPanel = new BoxPanel(Orientation.Vertical) {
      contents += new ScrollPane(launchList)
      contents += removeButton
      border = Swing.EmptyBorder(10, 10, 10, 10)
    }

    listenTo(removeButton)
    reactions += {
      case ButtonClicked(`removeButton`) =>
        val selectedLaunches = launchList.selection.items.toList
      // Remove selected launches from MongoDB
      // ...

      // Update displayed list after removal
    }

    val readDialog = new Dialog {
      title = "SpaceX Launches"
      contents = readPanel
    }

    readDialog
  }

  def fetchLaunchesFromDB(): List[String] = {
    // Fetch launches from MongoDB and return as a list of strings
    // Replace with actual MongoDB query logic
    // For example:
    List("Launch 1", "Launch 2", "Launch 3") // Sample data
  }

  def top: MainFrame = new MainFrame {
    title = "SpaceX CRUD Operations"
    preferredSize = new Dimension(700, 400)

    val createButton = new Button("Create")
    val readButton = new Button("Read")
    val updateButton = new Button("Update")
    val deleteButton = new Button("Delete")

    listenTo(createButton, readButton, updateButton, deleteButton)
    reactions += {
      case ButtonClicked(`createButton`) =>
        val createWindow = createLaunchWindow()
        createWindow.open()

      case ButtonClicked(`readButton`) =>
        val launches = fetchLaunchesFromDB()
        val readWindow = createReadWindow(launches)
        readWindow.open()

      // Implement reactions for other buttons...
    }

    val buttonPanel = new GridBagPanel {
      val c = new Constraints
      c.insets = new Insets(5, 5, 5, 5)

      c.gridx = 0
      c.gridy = 0
      layout(createButton) = c

      c.gridx = 1
      c.gridy = 0
      layout(readButton) = c

      c.gridx = 0
      c.gridy = 1
      layout(updateButton) = c

      c.gridx = 1
      c.gridy = 1
      layout(deleteButton) = c
    }

    contents = new BoxPanel(Orientation.Vertical) {
      contents += buttonPanel
      border = Swing.EmptyBorder(10, 10, 10, 10)
    }
  }
}



