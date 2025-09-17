package tetris.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scalafx.scene.paint.Color
import tetris.core.Constants._
import tetris.core.GameConfig

import java.nio.charset.StandardCharsets
import java.nio.file.Files

class GameStateSpec extends AnyFunSuite with Matchers {

  private def withTempHighScore[A](initial: Option[Long] = None)(body: => A): A = {
    val dir = Files.createTempDirectory("tetris-highscore-test")
    val file = dir.resolve("highscore.dat")
    initial.foreach(value => Files.writeString(file, value.toString, StandardCharsets.UTF_8))
    try HighScoreStorage.withCustomPath(file) { body }
    finally {
      Files.deleteIfExists(file)
      Files.deleteIfExists(dir)
    }
  }

  private def setGrid(state: GameState, grid: Vector[Vector[Option[Color]]]): Unit = {
    val field = classOf[GameState].getDeclaredField("grid")
    field.setAccessible(true)
    field.set(state, grid)
  }

  test("update moves the active piece down by one row when unobstructed") {
    withTempHighScore() {
      val state = new GameState(GameConfig())
      state.currentPiece = Piece(Point(GridWidth / 2, 1), Tetromino.I, rotation = 0)

      val initialY = state.currentPiece.position.y
      state.update()

      state.currentPiece.position.y shouldEqual initialY + 1
    }
  }

  test("update does nothing while the game is paused") {
    withTempHighScore() {
      val state = new GameState(GameConfig())
      state.isPaused = true
      val initialPiece = state.currentPiece

      state.update()

      state.currentPiece shouldEqual initialPiece
    }
  }

  test("softDrop applies one gravity tick when not paused") {
    withTempHighScore() {
      val state = new GameState(GameConfig())
      val initialY = state.currentPiece.position.y

      state.softDrop()

      state.currentPiece.position.y shouldEqual initialY + 1
    }
  }

  test("togglePause flips the pause flag only when game is running") {
    withTempHighScore() {
      val state = new GameState(GameConfig())
      state.togglePause()
      state.isPaused shouldBe true

      state.isGameOver = true
      state.togglePause()
      state.isPaused shouldBe true // remains unchanged once game over
    }
  }

  test("high score loads the persisted value") {
    withTempHighScore(initial = Some(900L)) {
      val state = new GameState(GameConfig())
      state.highScore shouldEqual 900L
    }
  }

  test("starting level from config is applied on start and restart") {
    withTempHighScore() {
      val cfg = GameConfig(startingLevel = 3)
      val state = new GameState(cfg)

      state.level shouldEqual 3
      state.restart()
      state.level shouldEqual 3
    }
  }

  test("updateStartingLevel restarts the game with the new base level") {
    withTempHighScore() {
      val state = new GameState(GameConfig(startingLevel = 2))
      state.score = 400
      state.linesCleared = 6

      state.updateStartingLevel(5)

      state.level shouldEqual 5
      state.linesCleared shouldEqual 0
      state.score shouldEqual 0
      state.isGameOver shouldBe false
    }
  }

  test("line clear awards points, updates high score, and spawns the next piece") {
    withTempHighScore() {
      val state = new GameState(GameConfig())
      val color = Color.Coral
      val emptyRow = Vector.fill(GridWidth)(Option.empty[Color])
      val nearlyFullBottom = Vector.tabulate(GridWidth) { x =>
        if (x < GridWidth - 2) Some(color) else None
      }

      val customGrid = Vector.tabulate(GridHeight) { y =>
        if (y == GridHeight - 1) nearlyFullBottom else emptyRow
      }

      setGrid(state, customGrid)
      state.currentPiece = Piece(Point(GridWidth - 2, GridHeight - 3), Tetromino.O, rotation = 0)
      state.nextPiece = Piece(Point(0, 0), Tetromino.I, rotation = 0)

      state.update() // Drop into the nearly full row
      state.update() // Lock, clear, and spawn next piece

      state.linesCleared shouldEqual 1
      state.score shouldEqual 40 // single line at level 0
      state.highScore shouldEqual 40
      HighScoreStorage.load() shouldEqual 40
      state.currentPiece.position shouldEqual Point(GridWidth / 2, 1)
      state.isGameOver shouldBe false
      state.getGrid.exists(row => row.forall(_.isDefined)) shouldBe false
    }
  }

  test("restart resets the full game state but keeps high score") {
    withTempHighScore(initial = Some(500L)) {
      val state = new GameState(GameConfig())
      state.score = 1234
      state.level = 3
      state.linesCleared = 12
      state.isGameOver = true
      state.isPaused = true

      state.restart()

      state.score shouldEqual 0
      state.level shouldEqual 0
      state.linesCleared shouldEqual 0
      state.isGameOver shouldBe false
      state.isPaused shouldBe false
      state.highScore shouldEqual 500L
      state.getGrid.flatten.flatten shouldBe empty
    }
  }
}
