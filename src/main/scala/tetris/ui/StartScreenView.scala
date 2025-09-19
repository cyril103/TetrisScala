package tetris.ui

import scalafx.Includes._
import scalafx.beans.property.IntegerProperty
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control.{Button, CheckBox, Label, Slider}
import scalafx.scene.layout.{HBox, Priority, Region, StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.{Font, FontWeight}

class StartScreenView {
  var onStartRequested: () => Unit = () => ()
  var onQuitRequested: () => Unit = () => ()
  var onStartingLevelChanged: Int => Unit = _ => ()
  var onVolumeChanged: Int => Unit = _ => ()
  var onDarkModeChanged: Boolean => Unit = _ => ()
  var onResetHighScoreToggleChanged: Boolean => Unit = _ => ()
  var onResetHighScoreRequested: () => Unit = () => ()

  private var suppressCallbacks = false

  private def runWithoutFeedback[T](body: => T): T = {
    val wasSuppressed = suppressCallbacks
    suppressCallbacks = true
    try body finally suppressCallbacks = wasSuppressed
  }

  private val startingLevelDisplay: IntegerProperty = IntegerProperty(0)

  private val titleLabel = new Label("TetrisScala") {
    font = Font.font("Arial", FontWeight.Bold, 42)
    textFill = Color.web("#f7f7f7")
  }

  private val subtitleLabel = new Label("Ajustez vos options puis appuyez sur Start") {
    font = Font.font("Arial", 18)
    textFill = Color.web("#d0d0d0")
  }

  private val startButton = new Button("Start") {
    prefWidth = 220
    style = "-fx-font-size: 20px; -fx-background-color: linear-gradient(#4facfe, #00f2fe); -fx-text-fill: white;"
    onAction = _ => onStartRequested()
    defaultButton = true
  }

  private val quitButton = new Button("Quitter") {
    prefWidth = 220
    style = "-fx-font-size: 16px;"
    onAction = _ => onQuitRequested()
  }

  private val levelValueLabel = new Label("0") {
    style = "-fx-text-fill: #f0f0f0; -fx-font-size: 18px; -fx-min-width: 40px; -fx-alignment: center;"
    alignment = Pos.Center
  }

  private val decreaseLevelButton = new Button("-") {
    style = "-fx-font-size: 18px; -fx-pref-width: 40px;"
    focusTraversable = false
    onAction = _ => adjustStartingLevel(-1)
  }

  private val increaseLevelButton = new Button("+") {
    style = "-fx-font-size: 18px; -fx-pref-width: 40px;"
    focusTraversable = false
    onAction = _ => adjustStartingLevel(1)
  }

  private val startingLevelControls = new HBox(10, decreaseLevelButton, levelValueLabel, increaseLevelButton) {
    alignment = Pos.CenterLeft
  }

  private val volumeSlider = new Slider(0, 100, 100) {
    prefWidth = 180
    majorTickUnit = 25
    showTickLabels = true
    showTickMarks = true
    blockIncrement = 5
  }

  volumeSlider.valueProperty.onChange { (_, _, newValue) =>
    if (!suppressCallbacks) {
      val rounded = newValue.doubleValue().round.toInt
      onVolumeChanged(rounded)
      setVolume(rounded)
    }
  }

  private val volumeValueLabel = new Label("100%") {
    style = "-fx-text-fill: white; -fx-font-size: 16px;"
    minWidth = 60
    alignment = Pos.CenterRight
  }

  private val darkModeCheck = new CheckBox("Mode sombre") {
    selected = true
    style = "-fx-text-fill: white; -fx-font-size: 16px;"
    onAction = _ => if (!suppressCallbacks) onDarkModeChanged(selected.value)
  }

  private val resetHighScoreCheck = new CheckBox("Reinitialiser le high score au demarrage") {
    style = "-fx-text-fill: white; -fx-font-size: 14px;"
    onAction = _ => if (!suppressCallbacks) onResetHighScoreToggleChanged(selected.value)
  }

  private val resetHighScoreButton = new Button("Effacer le high score maintenant") {
    style = "-fx-font-size: 14px;"
    onAction = _ => onResetHighScoreRequested()
  }

  private val optionsHeader = new Label("Options rapides") {
    font = Font.font("Arial", FontWeight.Bold, 20)
    textFill = Color.web("#f0f0f0")
    padding = Insets(10, 0, 4, 0)
  }

  private def wrapOption(label: String, control: Node): Node = {
    val description = new Label(label) {
      style = "-fx-text-fill: #f0f0f0; -fx-font-size: 16px;"
    }
    val spacer = new Region
    HBox.setHgrow(spacer, Priority.Always)
    new HBox(12, description, spacer, control) {
      alignment = Pos.CenterLeft
    }
  }

  private val optionsBox = new VBox(10,
    optionsHeader,
    wrapOption("Niveau de depart", startingLevelControls),
    new HBox(12, new Label("Volume") {
      style = "-fx-text-fill: #f0f0f0; -fx-font-size: 16px;"
    }, volumeSlider, volumeValueLabel) {
      alignment = Pos.CenterLeft
    },
    darkModeCheck,
    resetHighScoreCheck,
    resetHighScoreButton
  ) {
    padding = Insets(16, 20, 16, 20)
    style = "-fx-background-color: rgba(0, 0, 0, 0.35); -fx-background-radius: 10px;"
  }

  private val buttonBox = new VBox(12, startButton, quitButton) {
    alignment = Pos.Center
    padding = Insets(10, 0, 20, 0)
  }

  private val contentBox = new VBox(18, titleLabel, subtitleLabel, buttonBox, optionsBox) {
    alignment = Pos.Center
    padding = Insets(40)
  }

  private val backgroundRect = new Rectangle {
    fill = Color.rgb(10, 20, 40)
  }

  val rootPane: StackPane = new StackPane {
    children = Seq(backgroundRect, contentBox)
    alignment = Pos.Center
    padding = Insets(30)
  }

  backgroundRect.width <== rootPane.widthProperty
  backgroundRect.height <== rootPane.heightProperty

  startingLevelDisplay.onChange { (_, _, newValue) =>
    val value = newValue.intValue
    levelValueLabel.text = value.toString
    decreaseLevelButton.disable = value <= 0
    increaseLevelButton.disable = value >= 20
  }

  def requestInitialFocus(): Unit = {
    startButton.requestFocus()
  }

  private def adjustStartingLevel(delta: Int): Unit = {
    val current = startingLevelDisplay.value
    val candidate = math.max(0, math.min(20, current + delta))
    if (candidate != current) {
      startingLevelDisplay.value = candidate
      if (!suppressCallbacks) {
        onStartingLevelChanged(candidate)
      }
    }
  }

  def setStartingLevel(level: Int): Unit = runWithoutFeedback {
    startingLevelDisplay.value = math.max(0, math.min(20, level))
  }

  def setVolume(value: Int): Unit = runWithoutFeedback {
    val clamped = math.max(0, math.min(100, value))
    volumeSlider.value = clamped
    volumeValueLabel.text = s"$clamped%"
  }

  def setDarkMode(enabled: Boolean): Unit = runWithoutFeedback {
    darkModeCheck.selected = enabled
  }

  def setResetHighScore(enabled: Boolean): Unit = runWithoutFeedback {
    resetHighScoreCheck.selected = enabled
  }

  def updateTheme(isDarkMode: Boolean): Unit = {
    val textColor = if (isDarkMode) "#f0f0f0" else "#101010"
    val secondary = if (isDarkMode) "#d0d0d0" else "#505050"
    titleLabel.textFill = if (isDarkMode) Color.web("#f7f7f7") else Color.web("#1b1b1b")
    subtitleLabel.textFill = Color.web(secondary)
    optionsHeader.textFill = Color.web(textColor)
    optionsBox.style = if (isDarkMode)
      "-fx-background-color: rgba(0, 0, 0, 0.35); -fx-background-radius: 10px;"
    else
      "-fx-background-color: rgba(255, 255, 255, 0.80); -fx-background-radius: 10px;"
    startButton.style = if (isDarkMode)
      "-fx-font-size: 20px; -fx-background-color: linear-gradient(#4facfe, #00f2fe); -fx-text-fill: white;"
    else
      "-fx-font-size: 20px; -fx-background-color: linear-gradient(#f7971e, #ffd200); -fx-text-fill: #101010;"
    val labelStyle = s"-fx-text-fill: $textColor; -fx-font-size: 16px;"
    darkModeCheck.style = labelStyle
    resetHighScoreCheck.style = s"-fx-text-fill: $textColor; -fx-font-size: 14px;"
    volumeValueLabel.style = s"-fx-text-fill: $textColor; -fx-font-size: 16px;"
    levelValueLabel.style = s"-fx-text-fill: $textColor; -fx-font-size: 18px; -fx-min-width: 40px; -fx-alignment: center;"
  }
}
