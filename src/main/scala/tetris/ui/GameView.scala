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
import scalafx.scene.control.{Button, Label}
import scalafx.beans.property.IntegerProperty

class GameView {
  val startingLevelDisplay: IntegerProperty = IntegerProperty(0)
  var onStartingLevelChanged: Int => Unit = _ => ()

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

  val hudPane: Pane = new VBox(10, scoreText, highScoreText, levelText, linesText, startingLevelControls, nextPieceLabel, nextPieceCanvas) {
    padding = Insets(20)
    prefWidth = 180 // Fix the width of the HUD pane
    style = "-fx-background-color: #333;"
    Seq(scoreText, highScoreText, levelText, linesText, nextPieceLabel).foreach(_.style = "-fx-font-size: 18px; -fx-fill: white;")
  }

  val gamePane: Pane = new Pane { children = Seq(canvas, gameOverText, pauseText) }

  private def renderGrid(): Unit = {
    gc.fill = Color.Black
    gc.fillRect(0, 0, canvas.width.value, canvas.height.value)
    gc.stroke = Color.rgb(50, 50, 50)
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

  private def renderNextPiece(piece: Piece): Unit = {
    nextPieceGc.fill = Color.rgb(30, 30, 30)
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

  def render(gameState: GameState): Unit = {
    renderGrid()
    renderBoard(gameState.getGrid)

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
  def setStartingLevel(level: Int): Unit = {
    val clamped = clampStartingLevel(level)
    startingLevelDisplay.value = clamped
    refreshStartingLevelControls(clamped)
  }
}
