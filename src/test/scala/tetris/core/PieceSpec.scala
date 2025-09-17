package tetris.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class PieceSpec extends AnyFunSuite with Matchers {
  test("Piece.blocks handles negative rotation indices") {
    val piece = Piece(Point(0, 0), Tetromino.T, rotation = -1)
    val expected = Tetromino.T.rotations(3).map(p => Point(p.x, p.y))

    piece.blocks shouldEqual expected
  }
}
