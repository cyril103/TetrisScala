package tetris.ui

import tetris.core.Constants._
import tetris.core.GameState
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color
import scalafx.scene.layout.{VBox, Pane}
import scalafx.scene.text.Text
import scalafx.geometry.Insets

class GameView {
  val canvas = new Canvas(GridWidth * BlockSize, GridHeight * BlockSize)
  private val gc = canvas.graphicsContext2D

  // --- HUD Elements ---
  private val scoreText = new Text("Score: 0")
  private val levelText = new Text("Level: 0")
  private val linesText = new Text("Lines: 0")
  private val gameOverText = new Text("GAME OVER") {
    style = "-fx-font-size: 40px; -fx-fill: red;"
    visible = false
  }

  val hudPane: Pane = new VBox(20, scoreText, levelText, linesText) {
    padding = Insets(20)
    prefWidth = 180 // Fix the width of the HUD pane
    style = "-fx-background-color: #333;"
    Seq(scoreText, levelText, linesText).foreach(_.style = "-fx-font-size: 18px; -fx-fill: white;")
  }

  val gamePane: Pane = new Pane {
    children = Seq(canvas, gameOverText)
  }

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

  def updateHud(score: Long, level: Int, lines: Int): Unit = {
    scoreText.text = s"Score: $score"
    levelText.text = s"Level: $level"
    linesText.text = s"Lines: $lines"
  }

  def showGameOver(): Unit = {
    gameOverText.visible = true
    gameOverText.layoutX = (canvas.width.value - gameOverText.boundsInLocal.get.getWidth) / 2
    gameOverText.layoutY = canvas.height.value / 2
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
    // Show or hide the game over text based on the game state
    gameOverText.visible = gameState.isGameOver
    if (gameState.isGameOver) {
      // Center the text on the canvas
      gameOverText.layoutX = (canvas.width.value - gameOverText.boundsInLocal.get.getWidth) / 2
      gameOverText.layoutY = canvas.height.value / 2
    }
  }
}