package tetris.audio

import scalafx.scene.media.{Media, MediaPlayer}

class MusicManager(resourcePath: String) {
  private val player: Option[MediaPlayer] = Option(getClass.getResource(resourcePath)).map { url =>
    val media = new Media(url.toExternalForm)
    val mediaPlayer = new MediaPlayer(media)
    mediaPlayer.setCycleCount(MediaPlayer.Indefinite)
    mediaPlayer.setVolume(1.0)
    mediaPlayer
  }

  def play(): Unit = player.foreach { p =>
    if (p.getStatus != MediaPlayer.Status.PLAYING) {
      p.play()
    }
  }

  def stop(): Unit = player.foreach(_.stop())

  def setVolume(volumePercent: Int): Unit = {
    val clamped = math.max(0, math.min(100, volumePercent)) / 100.0
    player.foreach(_.setVolume(clamped))
  }

  def isAvailable: Boolean = player.isDefined
}
