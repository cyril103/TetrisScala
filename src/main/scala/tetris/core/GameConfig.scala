package tetris.core

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._
import scala.util.Try
import scala.math.BigDecimal.RoundingMode

case class GameConfig(
  startingLevel: Int = 0,
  volume: Int = 100,
  darkMode: Boolean = true,
  resetHighScoreOnStart: Boolean = false,
  lineClearEffectDurationMs: Double = 300.0,
  lineClearEffectOpacity: Double = 0.75
)

object ConfigStorage {
  private val DefaultConfig = GameConfig()
  private val MinVolume = 0
  private val MaxVolume = 100
  private val MinLineClearDurationMs = 50.0
  private val MaxLineClearDurationMs = 2000.0
  private val MinLineClearOpacity = 0.05
  private val MaxLineClearOpacity = 1.0
  private var customPath: Option[Path] = None

  private def filePath: Path = customPath.getOrElse(Paths.get(System.getProperty("user.dir"), "config.properties"))

  def withCustomPath[T](path: Path)(body: => T): T = synchronized {
    val previous = customPath
    customPath = Some(path)
    try body
    finally {
      customPath = previous
    }
  }

  def load(): GameConfig = {
    val path = filePath
    if (!Files.exists(path)) {
      DefaultConfig
    } else {
      val entries = Files.readAllLines(path, StandardCharsets.UTF_8).asScala
        .map(_.trim)
        .filter(line => line.nonEmpty && !line.startsWith("#"))
        .flatMap { line =>
          line.split("=", 2) match {
            case Array(key, value) => Some(key.trim -> value.trim)
            case _ => None
          }
        }.toMap

      val startingLevel = entries.get("startingLevel")
        .flatMap(v => Try(v.toInt).toOption)
        .flatMap(level => if (level >= 0 && level <= 20) Some(level) else None)
        .getOrElse(DefaultConfig.startingLevel)

      val volume = entries.get("volume")
        .flatMap(v => Try(v.toInt).toOption)
        .map(value => math.max(MinVolume, math.min(MaxVolume, value)))
        .getOrElse(DefaultConfig.volume)

      val darkMode = entries.get("darkMode")
        .flatMap(v => Try(v.toBoolean).toOption)
        .getOrElse(DefaultConfig.darkMode)

      val resetHighScore = entries.get("resetHighScoreOnStart")
        .flatMap(v => Try(v.toBoolean).toOption)
        .getOrElse(DefaultConfig.resetHighScoreOnStart)

      val lineClearDuration = entries.get("lineClearEffectDurationMs")
        .flatMap(v => Try(v.toDouble).toOption)
        .map(value => math.max(MinLineClearDurationMs, math.min(MaxLineClearDurationMs, value)))
        .getOrElse(DefaultConfig.lineClearEffectDurationMs)

      val lineClearOpacity = entries.get("lineClearEffectOpacity")
        .flatMap(v => Try(v.toDouble).toOption)
        .map(value => math.max(MinLineClearOpacity, math.min(MaxLineClearOpacity, value)))
        .getOrElse(DefaultConfig.lineClearEffectOpacity)

      GameConfig(
        startingLevel = startingLevel,
        volume = volume,
        darkMode = darkMode,
        resetHighScoreOnStart = resetHighScore,
        lineClearEffectDurationMs = lineClearDuration,
        lineClearEffectOpacity = lineClearOpacity
      )
    }
  }

  def save(config: GameConfig): Unit = {
    val path = filePath
    Option(path.getParent).foreach(parent => if (!Files.exists(parent)) Files.createDirectories(parent))
    val clampedDuration = math.max(MinLineClearDurationMs, math.min(MaxLineClearDurationMs, config.lineClearEffectDurationMs))
    val clampedOpacity = math.max(MinLineClearOpacity, math.min(MaxLineClearOpacity, config.lineClearEffectOpacity))
    val durationStr = BigDecimal(clampedDuration).setScale(2, RoundingMode.HALF_UP).toString
    val opacityStr = BigDecimal(clampedOpacity).setScale(3, RoundingMode.HALF_UP).toString
    val content =
      s"startingLevel=${config.startingLevel}\n" +
      s"volume=${math.max(MinVolume, math.min(MaxVolume, config.volume))}\n" +
      s"darkMode=${config.darkMode}\n" +
      s"resetHighScoreOnStart=${config.resetHighScoreOnStart}\n" +
      s"lineClearEffectDurationMs=$durationStr\n" +
      s"lineClearEffectOpacity=$opacityStr\n"
    Files.write(path, content.getBytes(StandardCharsets.UTF_8))
  }
}
