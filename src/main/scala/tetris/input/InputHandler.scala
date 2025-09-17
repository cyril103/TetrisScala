package tetris.input

import scalafx.Includes._ // Provides implicit conversions for event handlers
import tetris.core.GameState
import scalafx.scene.Scene
import scalafx.scene.input.{KeyCode, KeyEvent}

class InputHandler(gameState: GameState, scene: Scene) {

  scene.onKeyPressed = (e: KeyEvent) => {
    e.code match {
      // --- Movement ---
      case KeyCode.Left | KeyCode.A => gameState.moveLeft()
      case KeyCode.Right | KeyCode.D => gameState.moveRight()
      
      // --- Rotation ---
      case KeyCode.Up | KeyCode.X | KeyCode.W => gameState.rotateClockwise()
      case KeyCode.Z | KeyCode.Control => gameState.rotateCounterClockwise()

      // --- Dropping ---
      case KeyCode.Down | KeyCode.S => gameState.softDrop()
      
      // --- Game Flow ---
      case KeyCode.R => if (gameState.isGameOver) gameState.restart()

      // TODO: Add Pause (P)

      case _ => // Ignore other keys
    }
  }
}