package tetris.ui

import tetris.core.Constants._
import scalafx.Includes._
import tetris.core.{GameState, Piece}
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color
import scalafx.scene.layout.{VBox, Pane, HBox}
import scalafx.scene.text.Text
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.scene.control.{Button, Label, Slider, CheckBox}
import scalafx.beans.property.IntegerProperty

class GameView {
  val startingLevelDisplay: IntegerProperty = IntegerProperty(0)
  var onStartingLevelChanged: Int => Unit = _ => ()
  var onVolumeChanged: Int => Unit = _ => ()
  var onDarkModeChanged: Boolean => Unit = _ => ()
  var onResetHighScoreToggleChanged: Boolean => Unit = _ => ()
  var onResetHighScoreRequested: () => Unit = () => ()
  var onStartGameRequested: () => Unit = () => ()
  var onReturnToMenuRequested: () => Unit = () => ()

  val canvas = new Canvas(GridWidth * BlockSize, GridHeight * BlockSize)
  canvas.focusTraversable = false
  private val gc = canvas.graphicsContext2D

  // --- HUD Elements ---
  private val scoreText = new Text("Score: 0")
  private val highScoreText = new Text("High Score: 0")
  private val levelText = new Text("Level: 0")
  private val linesText = new Text("Lines: 0")
  private val nextPieceLabel = new Text("Suivante:")
  private val nextPieceCanvas = new Canvas(4 * BlockSize, 4 * BlockSize)
  private val nextPieceGc = nextPieceCanvas.graphicsContext2D

  private val gameOverText = new Text("GAME OVER") {
    style = "-fx-font-size: 40px; -fx-fill: red;"
    visible = false
  }

  private val pauseText = new Text("PAUSE") {
    style = "-fx-font-size: 32px; -fx-fill: gold;"
    visible = false
  }

  private val startingLevelLabel = new Label("Starting level: 0") {
    style = "-fx-text-fill: white; -fx-font-size: 16px;"
  }

  private val decreaseLevelButton = new Button("-") {
    style = "-fx-font-size: 18px; -fx-pref-width: 40px;"
    focusTraversable = false
  }

  private val increaseLevelButton = new Button("+") {
    style = "-fx-font-size: 18px; -fx-pref-width: 40px;"
    focusTraversable = false
  }

  private val startingLevelButtons = new HBox(6, decreaseLevelButton, increaseLevelButton) {
    alignment = Pos.CenterLeft
  }

  private val startingLevelControls = new VBox(4, startingLevelLabel, startingLevelButtons) {
    spacing = 4
  }

  private val volumeLabel = new Label("Volume: 100%") {
    style = "-fx-text-fill: white; -fx-font-size: 16px;"
  }

  private val volumeSlider = new Slider(0, 100, 100) {
    blockIncrement = 5
    majorTickUnit = 25
    showTickLabels = false
    showTickMarks = false
    maxWidth = 160
    focusTraversable = false
  }

  private val darkModeCheck = new CheckBox("Dark mode") {
    selected = true
    focusTraversable = false
    style = "-fx-text-fill: white; -fx-font-size: 16px;"
  }

  private val resetHighScoreOnStartCheck = new CheckBox("Reset high score on start") {
    focusTraversable = false
    style = "-fx-text-fill: white; -fx-font-size: 16px;"
  }

  private val startGameButton = new Button("Start Game") {
    style = "-fx-font-size: 16px;"
    focusTraversable = false
  }

  private val returnToMenuButton = new Button("Menu principal") {
    style = "-fx-font-size: 16px;"
    focusTraversable = false
  }

  private val resetHighScoreButton = new Button("Reset High Score now") {
    style = "-fx-font-size: 16px;"
    focusTraversable = false
  }

  private val optionsBox = new VBox(6,
    startGameButton,
    returnToMenuButton,
    volumeLabel,
    volumeSlider,
    darkModeCheck,
    resetHighScoreOnStartCheck,
    resetHighScoreButton
  ) {
    spacing = 6
  }

  private var suppressCallbacks = false
  private var pendingVolume: Int = 100

  private def runWithSuppressedCallbacks[T](body: => T): T = {
    val previous = suppressCallbacks
    suppressCallbacks = true
    try body
    finally suppressCallbacks = previous
  }

  private def clampVolume(value: Int): Int = math.max(0, math.min(100, value))

  private def refocusGameCanvas(): Unit = canvas.requestFocus()

  Seq(decreaseLevelButton, increaseLevelButton, resetHighScoreButton, startGameButton).foreach { button =>
    button.onMousePressed = _ => refocusGameCanvas()
    button.onMouseReleased = _ => refocusGameCanvas()
    button.onKeyReleased = _ => refocusGameCanvas()
  }

  Seq(darkModeCheck, resetHighScoreOnStartCheck).foreach { check =>
    check.onMousePressed = _ => refocusGameCanvas()
    check.onMouseReleased = _ => refocusGameCanvas()
    check.onKeyReleased = _ => refocusGameCanvas()
  }

  volumeSlider.onMousePressed = _ => refocusGameCanvas()
  volumeSlider.onMouseReleased = _ => refocusGameCanvas()
  volumeSlider.onKeyReleased = _ => refocusGameCanvas()

  volumeSlider.valueProperty.onChange { (_, _, newValue) =>
    val volumeValue = clampVolume(newValue.intValue)
    pendingVolume = volumeValue
    volumeLabel.text = s"Volume: $volumeValue%"
    if (!suppressCallbacks) {
      runWithSuppressedCallbacks {
        volumeSlider.value = volumeValue
      }
    }
  }

  volumeSlider.valueChangingProperty.onChange { (_, _, isChanging) =>
    if (!isChanging && !suppressCallbacks) {
      onVolumeChanged(pendingVolume)
    }
  }

  darkModeCheck.selectedProperty.onChange { (_, _, newValue) =>
    if (!suppressCallbacks) {
      onDarkModeChanged(newValue)
    }
  }

  resetHighScoreOnStartCheck.selectedProperty.onChange { (_, _, newValue) =>
    if (!suppressCallbacks) {
      onResetHighScoreToggleChanged(newValue)
    }
  }

  startGameButton.onAction = _ => onStartGameRequested()
  returnToMenuButton.onAction = _ => onReturnToMenuRequested()
  resetHighScoreButton.onAction = _ => onResetHighScoreRequested()

  private var boardBackgroundColor: Color = Color.Black
  private var boardGridColor: Color = Color.rgb(50, 50, 50)
  private var isDarkMode: Boolean = true
  private var lineClearEffectDurationNanos: Long = (0.3 * 1e9).toLong
  private var lineClearEffectBaseOpacity: Double = 0.75
  private val lightModeOpacityFactor = 0.7333333333333333

  private val hudTexts = Seq(scoreText, highScoreText, levelText, linesText, nextPieceLabel)

  private def applyTheme(dark: Boolean): Unit = {
    isDarkMode = dark
    boardBackgroundColor = if (dark) Color.Black else Color.White
    boardGridColor = if (dark) Color.rgb(50, 50, 50) else Color.rgb(160, 160, 160)

    val textFill = if (dark) "white" else "#111111"
    hudTexts.foreach(_.style = s"-fx-font-size: 18px; -fx-fill: $textFill;")
    startingLevelLabel.style = s"-fx-text-fill: $textFill; -fx-font-size: 16px;"
    volumeLabel.style = s"-fx-text-fill: $textFill; -fx-font-size: 16px;"
    darkModeCheck.style = s"-fx-text-fill: $textFill; -fx-font-size: 16px;"
    resetHighScoreOnStartCheck.style = s"-fx-text-fill: $textFill; -fx-font-size: 16px;"
    startGameButton.style = s"-fx-font-size: 16px; -fx-text-fill: $textFill;"
    resetHighScoreButton.style = s"-fx-font-size: 16px; -fx-text-fill: $textFill;"

    val hudBackground = if (dark) "#333333" else "#f2f2f2"
    hudPane.style = s"-fx-background-color: $hudBackground;"
  }

  private def clampStartingLevel(level: Int): Int = math.max(0, math.min(20, level))

  private def refreshStartingLevelControls(level: Int): Unit = {
    startingLevelLabel.text = s"Starting level: $level"
    decreaseLevelButton.disable = level <= 0
    increaseLevelButton.disable = level >= 20
  }

  decreaseLevelButton.onAction = _ => adjustStartingLevel(-1)
  increaseLevelButton.onAction = _ => adjustStartingLevel(1)

  private def adjustStartingLevel(delta: Int): Unit = {
    val newLevel = clampStartingLevel(startingLevelDisplay.value + delta)
    if (newLevel != startingLevelDisplay.value) {
      onStartingLevelChanged(newLevel)
    }
  }

  startingLevelDisplay.onChange { (_, _, newValue) =>
    refreshStartingLevelControls(newValue.intValue)
  }

  refreshStartingLevelControls(startingLevelDisplay.value)

  val hudPane: Pane = new VBox(10, scoreText, highScoreText, levelText, linesText, startingLevelControls, optionsBox, nextPieceLabel, nextPieceCanvas) {
    padding = Insets(20)
    prefWidth = 180 // Fix the width of the HUD pane
  }

  applyTheme(isDarkMode)

  val gamePane: Pane = new Pane { children = Seq(canvas, gameOverText, pauseText) }

  private def renderGrid(): Unit = {
    gc.fill = boardBackgroundColor
    gc.fillRect(0, 0, canvas.width.value, canvas.height.value)
    gc.stroke = boardGridColor
    gc.lineWidth = 1
    for (x <- 0 to GridWidth) gc.strokeLine(x * BlockSize, 0, x * BlockSize, canvas.height.value)
    for (y <- 0 to GridHeight) gc.strokeLine(0, y * BlockSize, canvas.width.value, y * BlockSize)
  }

  private def renderBoard(grid: Vector[Vector[Option[Color]]]): Unit = {
    for (y <- 0 until GridHeight; x <- 0 until GridWidth) {
      grid(y)(x).foreach { color =>
        gc.fill = color
        gc.fillRect(x * BlockSize, y * BlockSize, BlockSize - 1, BlockSize - 1)
      }
    }
  }

  private def renderLineClearHighlights(rows: Seq[Int], startedAt: Long, now: Long): Unit = {
    if (rows.nonEmpty && startedAt > 0L && lineClearEffectDurationNanos > 0L) {
      val elapsed = now - startedAt
      if (elapsed >= 0L && elapsed < lineClearEffectDurationNanos) {
        val progress = math.min(1.0, math.max(0.0, elapsed.toDouble / lineClearEffectDurationNanos))
        val baseOpacity = {
          if (isDarkMode) lineClearEffectBaseOpacity
          else math.min(1.0, lineClearEffectBaseOpacity * lightModeOpacityFactor)
        }
        val opacity = baseOpacity * (1.0 - progress)
        if (opacity > 0.01) {
          val outerColor =
            if (isDarkMode) Color.rgb(255, 255, 255, opacity * 0.45)
            else Color.rgb(255, 200, 120, opacity * 0.45)
          val innerColor =
            if (isDarkMode) Color.rgb(255, 255, 255, opacity)
            else Color.rgb(255, 170, 30, opacity)

          val inset = BlockSize * 0.12 * progress
          val width = canvas.width.value - inset * 2

          rows.foreach { row =>
            if (row >= 0 && row < GridHeight) {
              val y = row * BlockSize
              gc.fill = outerColor
              gc.fillRect(0, y, canvas.width.value, BlockSize)
              gc.fill = innerColor
              gc.fillRect(inset, y + 2, math.max(0.0, width), BlockSize - 4)
            }
          }
        }
      }
    }
  }

  private def renderNextPiece(piece: Piece): Unit = {
    val previewBackground = if (isDarkMode) Color.rgb(30, 30, 30) else Color.rgb(220, 220, 220)
    nextPieceGc.fill = previewBackground
    nextPieceGc.fillRect(0, 0, nextPieceCanvas.width.value, nextPieceCanvas.height.value)

    val blocks = piece.shape.rotations.head // Use the base rotation
    val minX = blocks.map(_.x).min
    val maxX = blocks.map(_.x).max
    val minY = blocks.map(_.y).min
    val maxY = blocks.map(_.y).max

    val pieceWidth = (maxX - minX + 1) * BlockSize
    val pieceHeight = (maxY - minY + 1) * BlockSize

    val startX = (nextPieceCanvas.width.value - pieceWidth) / 2
    val startY = (nextPieceCanvas.height.value - pieceHeight) / 2

    nextPieceGc.fill = piece.shape.color
    for (p <- blocks) {
        val drawX = startX - minX * BlockSize + p.x * BlockSize
        val drawY = startY - minY * BlockSize + p.y * BlockSize
        nextPieceGc.fillRect(drawX, drawY, BlockSize - 1, BlockSize - 1)
    }
  }

  def updateHud(gameState: GameState): Unit = {
    scoreText.text = s"Score: ${gameState.score}"
    highScoreText.text = s"High Score: ${gameState.highScore}"
    levelText.text = s"Level: ${gameState.level}"
    linesText.text = s"Lines: ${gameState.linesCleared}"
    renderNextPiece(gameState.nextPiece)
  }

  def setLineClearEffect(durationMs: Double, opacity: Double): Unit = {
    val clampedDurationMs = math.max(0.0, durationMs)
    lineClearEffectDurationNanos = (clampedDurationMs * 1e6).toLong
    lineClearEffectBaseOpacity = math.max(0.0, math.min(1.0, opacity))
  }

  def requestGameFocus(): Unit = {
    canvas.requestFocus()
  }

  def render(gameState: GameState, nowNanos: Long): Unit = {
    renderGrid()
    renderBoard(gameState.getGrid)
    renderLineClearHighlights(gameState.lastClearedRows, gameState.lastLineClearTimestamp, nowNanos)

    if (!gameState.isGameOver) {
      val piece = gameState.currentPiece
      gc.fill = piece.shape.color
      for (p <- piece.blocks) {
        if (p.y >= 0) gc.fillRect(p.x * BlockSize, p.y * BlockSize, BlockSize - 1, BlockSize - 1)
      }
    }

    gameOverText.visible = gameState.isGameOver
    if (gameState.isGameOver) {
      gameOverText.layoutX = (canvas.width.value - gameOverText.boundsInLocal.get.getWidth) / 2
      gameOverText.layoutY = canvas.height.value / 2
    }

    val showPause = gameState.isPaused && !gameState.isGameOver
    pauseText.visible = showPause
    if (showPause) {
      pauseText.layoutX = (canvas.width.value - pauseText.boundsInLocal.get.getWidth) / 2
      pauseText.layoutY = canvas.height.value / 2
    }
  }
  def setVolume(value: Int): Unit = {
    val clamped = clampVolume(value)
    pendingVolume = clamped
    runWithSuppressedCallbacks {
      volumeSlider.value = clamped
    }
    volumeLabel.text = s"Volume: $clamped%"
  }

  def setDarkMode(enabled: Boolean): Unit = {
    runWithSuppressedCallbacks {
      darkModeCheck.selected = enabled
    }
    applyTheme(enabled)
  }

  def setResetHighScoreOnStart(enabled: Boolean): Unit = {
    runWithSuppressedCallbacks {
      resetHighScoreOnStartCheck.selected = enabled
    }
  }

  def setStartingLevel(level: Int): Unit = {
    val clamped = clampStartingLevel(level)
    runWithSuppressedCallbacks {
      startingLevelDisplay.value = clamped
    }
    refreshStartingLevelControls(clamped)
  }
}

