package tetris.core

import scalafx.scene.paint.Color

// Represents a coordinate on the grid
case class Point(x: Int, y: Int)

// Represents a falling piece
case class Piece(position: Point, shape: Tetromino, rotation: Int) {
  // Returns the actual grid coordinates of the piece's blocks
  def blocks: Vector[Point] = shape.rotations(rotation % 4).map(p => Point(p.x + position.x, p.y + position.y))
}

// Enum for the 7 tetromino shapes, their colors, and rotation patterns
enum Tetromino(val color: Color, val rotations: Vector[Vector[Point]]) {
  case I extends Tetromino(Color.Cyan, Vector(
    Vector(Point(-1, 0), Point(0, 0), Point(1, 0), Point(2, 0)),
    Vector(Point(1, -1), Point(1, 0), Point(1, 1), Point(1, 2)),
    Vector(Point(-1, 1), Point(0, 1), Point(1, 1), Point(2, 1)),
    Vector(Point(0, -1), Point(0, 0), Point(0, 1), Point(0, 2))
  ))
  case O extends Tetromino(Color.Yellow, Vector(
    Vector(Point(0, 0), Point(1, 0), Point(0, 1), Point(1, 1)),
    Vector(Point(0, 0), Point(1, 0), Point(0, 1), Point(1, 1)),
    Vector(Point(0, 0), Point(1, 0), Point(0, 1), Point(1, 1)),
    Vector(Point(0, 0), Point(1, 0), Point(0, 1), Point(1, 1))
  ))
  case T extends Tetromino(Color.Purple, Vector(
    Vector(Point(-1, 0), Point(0, 0), Point(1, 0), Point(0, 1)),
    Vector(Point(0, -1), Point(0, 0), Point(1, 0), Point(0, 1)),
    Vector(Point(-1, 0), Point(0, 0), Point(1, 0), Point(0, -1)),
    Vector(Point(0, -1), Point(-1, 0), Point(0, 0), Point(0, 1))
  ))
  case S extends Tetromino(Color.Green, Vector(
    Vector(Point(0, 0), Point(1, 0), Point(-1, 1), Point(0, 1)),
    Vector(Point(0, -1), Point(0, 0), Point(1, 0), Point(1, 1)),
    Vector(Point(0, 0), Point(1, 0), Point(-1, 1), Point(0, 1)),
    Vector(Point(0, -1), Point(0, 0), Point(1, 0), Point(1, 1))
  ))
  case Z extends Tetromino(Color.Red, Vector(
    Vector(Point(-1, 0), Point(0, 0), Point(0, 1), Point(1, 1)),
    Vector(Point(1, -1), Point(0, 0), Point(1, 0), Point(0, 1)),
    Vector(Point(-1, 0), Point(0, 0), Point(0, 1), Point(1, 1)),
    Vector(Point(1, -1), Point(0, 0), Point(1, 0), Point(0, 1))
  ))
  case J extends Tetromino(Color.Blue, Vector(
    Vector(Point(-1, -1), Point(-1, 0), Point(0, 0), Point(1, 0)),
    Vector(Point(1, -1), Point(0, -1), Point(0, 0), Point(0, 1)),
    Vector(Point(-1, 0), Point(0, 0), Point(1, 0), Point(1, 1)),
    Vector(Point(0, -1), Point(0, 0), Point(0, 1), Point(-1, 1))
  ))
  case L extends Tetromino(Color.Orange, Vector(
    Vector(Point(1, -1), Point(-1, 0), Point(0, 0), Point(1, 0)),
    Vector(Point(0, -1), Point(0, 0), Point(0, 1), Point(1, 1)),
    Vector(Point(-1, 0), Point(0, 0), Point(1, 0), Point(-1, 1)),
    Vector(Point(-1, -1), Point(0, -1), Point(0, 0), Point(0, 1))
  ))
}
