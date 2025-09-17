package tetris

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.animation.AnimationTimer
import tetris.ui.GameView
import tetris.core.Constants._
import scalafx.scene.layout.BorderPane
import tetris.core.GameState
import tetris.input.InputHandler

object Main extends JFXApp3 {

  // Formula to determine gravity interval based on level
  def levelToInterval(level: Int): Long = {
    // This is a simplified formula, can be adjusted for difficulty
    val baseInterval = 500_000_000L // 0.5 seconds at level 0
    val speedIncrease = level * 45_000_000L
    math.max(80_000_000L, baseInterval - speedIncrease) // Cap at ~12 updates/sec
  }

  override def start(): Unit = {
    val gameView = new GameView()
    val gameState = new GameState()

    val rootPane = new BorderPane {
      center = gameView.gamePane
      right = gameView.hudPane
    }

    stage = new JFXApp3.PrimaryStage {
      title = "TetrisScala"
      scene = new Scene {
        root = rootPane
        new InputHandler(gameState, this)
      }
    }

    var lastUpdateTime = 0L

    val timer = AnimationTimer(now => {
      val gravityInterval = levelToInterval(gameState.level)

      if (now - lastUpdateTime > gravityInterval) {
        if (!gameState.isGameOver) {
          gameState.update()
          gameView.updateHud(gameState)
        }
        lastUpdateTime = now
      }

      gameView.render(gameState)

      if (gameState.isGameOver) {
        // The text is now shown/hidden automatically by the render method
        // timer.stop() // We keep the timer running to listen for the restart key
      }
    })

    timer.start()
  }
}
