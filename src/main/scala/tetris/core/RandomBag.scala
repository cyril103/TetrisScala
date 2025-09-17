package tetris.core

import scala.util.Random

/**
 * Implements a "7-bag" random generator.
 * Ensures that all 7 tetrominoes appear exactly once in every set of 7 pieces.
 */
class RandomBag {
  private var bag: List[Tetromino] = List.empty

  private def refillBag(): Unit = {
    // Tetromino.values gives an Array of all enum cases.
    bag = Random.shuffle(Tetromino.values.toList)
  }

  def nextShape(): Tetromino = {
    if (bag.isEmpty) {
      refillBag()
    }
    val shape = bag.head
    bag = bag.tail // Consume the piece from the bag
    shape
  }
}
