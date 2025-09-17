package tetris.input

import scalafx.Includes._ // Provides implicit conversions for event handlers
import tetris.core.GameState
import scalafx.scene.Scene
import scalafx.scene.input.{KeyCode, KeyEvent}

class InputHandler(gameState: GameState, scene: Scene) {

  scene.onKeyPressed = (e: KeyEvent) => {
    val canControl = !gameState.isPaused && !gameState.isGameOver

    e.code match {
      // --- Movement ---
      case KeyCode.Left | KeyCode.A if canControl => gameState.moveLeft()
      case KeyCode.Right | KeyCode.D if canControl => gameState.moveRight()
      
      // --- Rotation ---
      case KeyCode.Up | KeyCode.X | KeyCode.W if canControl => gameState.rotateClockwise()
      case KeyCode.Z | KeyCode.Control if canControl => gameState.rotateCounterClockwise()

      // --- Dropping ---
      case KeyCode.Down | KeyCode.S if canControl => gameState.softDrop()
      
      // --- Game Flow ---
      case KeyCode.P => gameState.togglePause()
      case KeyCode.R => if (gameState.isGameOver) gameState.restart()

      case _ => // Ignore other keys
    }
  }
}