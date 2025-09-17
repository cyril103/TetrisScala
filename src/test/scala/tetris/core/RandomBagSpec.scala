package tetris.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class RandomBagSpec extends AnyFunSuite with Matchers {
  test("RandomBag yields each tetromino exactly once per cycle") {
    val bag = new RandomBag()
    val draws = (1 to 14).map(_ => bag.nextShape())

    draws.take(7).toSet shouldEqual Tetromino.values.toSet
    draws.drop(7).take(7).toSet shouldEqual Tetromino.values.toSet
  }
}
