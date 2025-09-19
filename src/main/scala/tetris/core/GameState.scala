package tetris.core

import scalafx.scene.paint.Color
import tetris.core.Constants._
import tetris.core.HighScoreStorage
import tetris.core.ConfigStorage
import tetris.core.GameConfig

class GameState(config: GameConfig = ConfigStorage.load()) {
  // Random piece generator
  private var randomBag = new RandomBag()

  private def clampStartingLevel(level: Int): Int = math.max(0, math.min(20, level))
  private var startingLevelBase: Int = clampStartingLevel(config.startingLevel)

  // Game Stats
  var score: Long = 0
  var level: Int = startingLevelBase
  var linesCleared: Int = 0
  var isGameOver: Boolean = false
  var isPaused: Boolean = false
  var highScore: Long = HighScoreStorage.load()
  var lastClearedRows: Seq[Int] = Seq.empty
  var lastLineClearTimestamp: Long = 0L

  // The grid of landed blocks. A 2D vector of optional colors.
  private var grid: Vector[Vector[Option[Color]]] = Vector.fill(GridHeight, GridWidth)(None)

  // The current falling piece, initialized from the random bag
  var currentPiece: Piece = Piece(Point(GridWidth / 2, 1), randomBag.nextShape(), 0)
  var nextPiece: Piece = Piece(Point(0, 0), randomBag.nextShape(), 0)

  // Checks if a piece is in a valid position (within bounds and not colliding)
  private def isValid(piece: Piece): Boolean = {
    piece.blocks.forall { p =>
      p.x >= 0 && p.x < GridWidth && // Within horizontal bounds
      p.y < GridHeight && // Within vertical bounds (top is always fine)
      (p.y < 0 || grid(p.y)(p.x).isEmpty) // Not colliding with existing blocks
    }
  }

  // Locks the current piece onto the grid
  private def lockPiece(): Unit = {
    currentPiece.blocks.foreach { p =>
      if (p.y >= 0) {
        grid = grid.updated(p.y, grid(p.y).updated(p.x, Some(currentPiece.shape.color)))
      }
    }
  }

  private def clearLines(): Seq[Int] = {
    val rowsWithIndex = grid.zipWithIndex
    val (fullRows, nonFullRows) = rowsWithIndex.partition { case (row, _) => row.forall(_.isDefined) }
    val clearedIndices = fullRows.map(_._2)

    if (clearedIndices.nonEmpty) {
      val newEmptyRows = Vector.fill(clearedIndices.length, GridWidth)(None)
      grid = newEmptyRows ++ nonFullRows.map(_._1)
    }
    clearedIndices
  }

  private def updateHighScore(): Unit = {
    if (score > highScore) {
      highScore = score
      HighScoreStorage.save(highScore)
    }
  }

  private def updateScore(clearedCount: Int): Unit = {
    val points = clearedCount match {
      case 1 => 40 * (level + 1)
      case 2 => 100 * (level + 1)
      case 3 => 300 * (level + 1)
      case 4 => 1200 * (level + 1)
      case _ => 0
    }
    score += points
    linesCleared += clearedCount
    level = startingLevelBase + (linesCleared / 10) // Increase level every 10 lines
    updateHighScore()
  }

  // Spawns a new piece at the top
  private def spawnNewPiece(): Unit = {
    currentPiece = nextPiece.copy(position = Point(GridWidth / 2, 1))
    nextPiece = Piece(Point(0, 0), randomBag.nextShape(), 0) // Position doesn't matter here
    if (!isValid(currentPiece)) {
      isGameOver = true
    }
  }

  // The main update function, called periodically to apply gravity
  def update(): Unit = {
    if (!isPaused) {
      val movedDown = currentPiece.copy(position = currentPiece.position.copy(y = currentPiece.position.y + 1))
      if (isValid(movedDown)) {
        currentPiece = movedDown
      } else {
        lockPiece()
        val clearedRows = clearLines()
        if (clearedRows.nonEmpty) {
          updateScore(clearedRows.length)
          lastClearedRows = clearedRows
          lastLineClearTimestamp = System.nanoTime()
        }
        spawnNewPiece()
      }
    }
  }

  // Public accessor for the grid for rendering
  def getGrid: Vector[Vector[Option[Color]]] = grid

  def togglePause(): Unit = {
    if (!isGameOver) {
      isPaused = !isPaused
    }
  }

  // --- Player Actions ---

  def moveLeft(): Unit = {
    val moved = currentPiece.copy(position = currentPiece.position.copy(x = currentPiece.position.x - 1))
    if (isValid(moved)) currentPiece = moved
  }

  def moveRight(): Unit = {
    val moved = currentPiece.copy(position = currentPiece.position.copy(x = currentPiece.position.x + 1))
    if (isValid(moved)) currentPiece = moved
  }

  def rotateClockwise(): Unit = {
    val rotated = currentPiece.copy(rotation = currentPiece.rotation + 1)
    if (isValid(rotated)) currentPiece = rotated
  }

  def rotateCounterClockwise(): Unit = {
    val rotated = currentPiece.copy(rotation = currentPiece.rotation - 1)
    if (isValid(rotated)) currentPiece = rotated
  }

  def softDrop(): Unit = {
    // A soft drop is just a single step of gravity
    update()
  }

  def updateStartingLevel(newLevel: Int): Unit = {
    val clamped = clampStartingLevel(newLevel)
    if (clamped != startingLevelBase) {
      startingLevelBase = clamped
      restart()
    }
  }

  def restart(): Unit = {
    grid = Vector.fill(GridHeight, GridWidth)(None)
    score = 0
    level = startingLevelBase
    linesCleared = 0
    isGameOver = false
    isPaused = false
    lastClearedRows = Seq.empty
    lastLineClearTimestamp = 0L
    randomBag = new RandomBag()
    currentPiece = Piece(Point(GridWidth / 2, 1), randomBag.nextShape(), 0)
    nextPiece = Piece(Point(0, 0), randomBag.nextShape(), 0)
  }
}
