package tetris

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.animation.AnimationTimer
import tetris.ui.{GameView, StartScreenView}
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
    val startScreen = new StartScreenView()

    def applyConfigToViews(): Unit = {
      gameView.setStartingLevel(config.startingLevel)
      gameView.setVolume(config.volume)
      gameView.setDarkMode(config.darkMode)
      gameView.setResetHighScoreOnStart(config.resetHighScoreOnStart)
      gameView.setLineClearEffect(config.lineClearEffectDurationMs, config.lineClearEffectOpacity)

      startScreen.setStartingLevel(config.startingLevel)
      startScreen.setVolume(config.volume)
      startScreen.setDarkMode(config.darkMode)
      startScreen.setResetHighScore(config.resetHighScoreOnStart)
      startScreen.updateTheme(config.darkMode)
    }

    applyConfigToViews()

    val gameState = new GameState(config)

    var lastUpdateTime = 0L
    var isRunning = false

    val gameRootPane = new BorderPane {
      center = gameView.gamePane
      right = gameView.hudPane
    }

    val mainScene = new Scene()

    stage = new JFXApp3.PrimaryStage {
      title = "TetrisScala"
      scene = mainScene
    }

    def showStartMenu(): Unit = {
      isRunning = false
      gameState.isPaused = true
      mainScene.root = startScreen.rootPane
      startScreen.requestInitialFocus()
    }

    def showGameScreen(): Unit = {
      mainScene.root = gameRootPane
      gameView.requestGameFocus()
    }

    def startNewGame(): Unit = {
      showGameScreen()
      if (config.resetHighScoreOnStart) {
        HighScoreStorage.save(0L)
        gameState.highScore = 0L
      }
      gameState.restart()
      val now = System.nanoTime()
      lastUpdateTime = now
      isRunning = true
      gameView.updateHud(gameState)
      gameView.render(gameState, now)
    }

    new InputHandler(gameState, mainScene, () => startNewGame(), () => showStartMenu())

    def updateStartingLevel(newLevel: Int): Unit = {
      val clamped = math.max(0, math.min(20, newLevel))
      if (clamped != config.startingLevel) {
        config = config.copy(startingLevel = clamped)
        ConfigStorage.save(config)
      }
      gameView.setStartingLevel(clamped)
      startScreen.setStartingLevel(clamped)
      gameState.updateStartingLevel(clamped)
      val now = System.nanoTime()
      lastUpdateTime = now
      gameView.updateHud(gameState)
      gameView.render(gameState, now)
    }

    def updateVolume(volume: Int): Unit = {
      val clamped = math.max(0, math.min(100, volume))
      if (clamped != config.volume) {
        config = config.copy(volume = clamped)
        ConfigStorage.save(config)
      }
      gameView.setVolume(clamped)
      startScreen.setVolume(clamped)
    }

    def updateDarkMode(enabled: Boolean): Unit = {
      if (enabled != config.darkMode) {
        config = config.copy(darkMode = enabled)
        ConfigStorage.save(config)
      }
      gameView.setDarkMode(enabled)
      startScreen.setDarkMode(enabled)
      startScreen.updateTheme(enabled)
    }

    def updateResetHighScoreToggle(enabled: Boolean): Unit = {
      if (enabled != config.resetHighScoreOnStart) {
        config = config.copy(resetHighScoreOnStart = enabled)
        ConfigStorage.save(config)
      }
      gameView.setResetHighScoreOnStart(enabled)
      startScreen.setResetHighScore(enabled)
    }

    gameView.onStartingLevelChanged = level => updateStartingLevel(level)
    startScreen.onStartingLevelChanged = level => updateStartingLevel(level)

    gameView.onVolumeChanged = volume => updateVolume(volume)
    startScreen.onVolumeChanged = volume => updateVolume(volume)

    gameView.onDarkModeChanged = enabled => updateDarkMode(enabled)
    startScreen.onDarkModeChanged = enabled => updateDarkMode(enabled)

    gameView.onResetHighScoreToggleChanged = enabled => updateResetHighScoreToggle(enabled)
    startScreen.onResetHighScoreToggleChanged = enabled => updateResetHighScoreToggle(enabled)

    val resetHighScoreAction = () => {
      HighScoreStorage.save(0L)
      gameState.highScore = 0L
      gameView.updateHud(gameState)
    }

    gameView.onResetHighScoreRequested = () => resetHighScoreAction()
    startScreen.onResetHighScoreRequested = () => resetHighScoreAction()

    gameView.onStartGameRequested = () => startNewGame()
    gameView.onReturnToMenuRequested = () => showStartMenu()
    startScreen.onStartRequested = () => startNewGame()
    startScreen.onQuitRequested = () => stage.close()

    showStartMenu()

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

      gameView.render(gameState, now)

      if (gameState.isGameOver) {
        isRunning = false
      }
    })

    timer.start()
  }
}
