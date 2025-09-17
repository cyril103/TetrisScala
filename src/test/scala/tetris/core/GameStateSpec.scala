package tetris.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scalafx.scene.paint.Color
import tetris.core.Constants._

class GameStateSpec extends AnyFunSuite with Matchers {

  private def setGrid(state: GameState, grid: Vector[Vector[Option[Color]]]): Unit = {
    val field = classOf[GameState].getDeclaredField("grid")
    field.setAccessible(true)
    field.set(state, grid)
  }

  test("update moves the active piece down by one row when unobstructed") {
    val state = new GameState()
    state.currentPiece = Piece(Point(GridWidth / 2, 1), Tetromino.I, rotation = 0)

    val initialY = state.currentPiece.position.y
    state.update()

    state.currentPiece.position.y shouldEqual initialY + 1
  }

  test("update does nothing while the game is paused") {
    val state = new GameState()
    state.isPaused = true
    val initialPiece = state.currentPiece

    state.update()

    state.currentPiece shouldEqual initialPiece
  }

  test("softDrop applies one gravity tick when not paused") {
    val state = new GameState()
    val initialY = state.currentPiece.position.y

    state.softDrop()

    state.currentPiece.position.y shouldEqual initialY + 1
  }

  test("togglePause flips the pause flag only when game is running") {
    val state = new GameState()
    state.togglePause()
    state.isPaused shouldBe true

    state.isGameOver = true
    state.togglePause()
    state.isPaused shouldBe true // remains unchanged once game over
  }

  test("line clear awards points and spawns the next piece") {
    val state = new GameState()
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
    state.currentPiece.position shouldEqual Point(GridWidth / 2, 1)
    state.isGameOver shouldBe false
    state.getGrid.exists(row => row.forall(_.isDefined)) shouldBe false
  }

  test("restart resets the full game state") {
    val state = new GameState()
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
    state.getGrid.flatten.flatten shouldBe empty
  }
}
