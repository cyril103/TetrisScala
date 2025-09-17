package tetris

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.animation.AnimationTimer
import tetris.ui.GameView
import tetris.core.Constants._
import scalafx.scene.layout.BorderPane
import tetris.core.GameState
import tetris.core.ConfigStorage
import tetris.core.HighScoreStorage
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
    var config = ConfigStorage.load()

    if (config.resetHighScoreOnStart) {
      HighScoreStorage.save(0L)
    }

    val gameView = new GameView()
    gameView.setStartingLevel(config.startingLevel)
    gameView.setVolume(config.volume)
    gameView.setDarkMode(config.darkMode)
    gameView.setResetHighScoreOnStart(config.resetHighScoreOnStart)

    val gameState = new GameState(config)

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
    var isRunning = false

    gameView.onStartingLevelChanged = { newLevel =>
      val clamped = math.max(0, math.min(20, newLevel))
      if (clamped != config.startingLevel) {
        config = config.copy(startingLevel = clamped)
        ConfigStorage.save(config)
      }
      gameView.setStartingLevel(clamped)
      gameState.updateStartingLevel(clamped)
      lastUpdateTime = System.nanoTime()
      gameView.updateHud(gameState)
      gameView.render(gameState)
    }

    gameView.onVolumeChanged = { volume =>
      val clamped = math.max(0, math.min(100, volume))
      if (clamped != config.volume) {
        config = config.copy(volume = clamped)
        ConfigStorage.save(config)
      }
      gameView.setVolume(clamped)
    }

    gameView.onDarkModeChanged = { enabled =>
      if (enabled != config.darkMode) {
        config = config.copy(darkMode = enabled)
        ConfigStorage.save(config)
      }
      gameView.setDarkMode(enabled)
    }

    gameView.onResetHighScoreToggleChanged = { enabled =>
      if (enabled != config.resetHighScoreOnStart) {
        config = config.copy(resetHighScoreOnStart = enabled)
        ConfigStorage.save(config)
      }
      gameView.setResetHighScoreOnStart(enabled)
    }

    gameView.onResetHighScoreRequested = { () =>
      HighScoreStorage.save(0L)
      gameState.highScore = 0L
      gameView.updateHud(gameState)
    }

    gameView.onStartGameRequested = { () =>
      isRunning = true
      gameState.restart()
      lastUpdateTime = System.nanoTime()
      gameView.updateHud(gameState)
      gameView.render(gameState)
    }

    gameView.updateHud(gameState)

    val timer = AnimationTimer(now => {
      val gravityInterval = levelToInterval(gameState.level)

      if (!isRunning || gameState.isPaused) {
        lastUpdateTime = now
      } else if (now - lastUpdateTime > gravityInterval) {
        if (!gameState.isGameOver) {
          gameState.update()
          gameView.updateHud(gameState)
        }
        lastUpdateTime = now
      }

      gameView.render(gameState)

      if (gameState.isGameOver) {
        isRunning = false
        // The text is now shown/hidden automatically by the render method
        // timer.stop() // We keep the timer running to listen for the restart key
      }
    })

    timer.start()
  }
}
