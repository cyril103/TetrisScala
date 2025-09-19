package tetris.core

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import scala.util.Try

case class LastStats(score: Long, lines: Int)

object StatsStorage {
  private var customPath: Option[Path] = None
  private val DefaultStats = LastStats(score = 0L, lines = 0)

  private def filePath: Path = customPath.getOrElse(Paths.get(System.getProperty("user.dir"), "stats.dat"))

  def withCustomPath[T](path: Path)(body: => T): T = synchronized {
    val previous = customPath
    customPath = Some(path)
    try body
    finally customPath = previous
  }

  def load(): LastStats = {
    val path = filePath
    if (!Files.exists(path)) {
      DefaultStats
    } else {
      val raw = new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim
      raw.split(";") match {
        case Array(scoreStr, linesStr) =>
          (Try(scoreStr.toLong).toOption, Try(linesStr.toInt).toOption) match {
            case (Some(score), Some(lines)) if lines >= 0 => LastStats(score, lines)
            case (Some(score), _) => LastStats(score, 0)
            case _ => DefaultStats
          }
        case _ => DefaultStats
      }
    }
  }

  def save(stats: LastStats): Unit = {
    val path = filePath
    Option(path.getParent).foreach(parent => if (!Files.exists(parent)) Files.createDirectories(parent))
    val normalized = LastStats(
      score = math.max(0L, stats.score),
      lines = math.max(0, stats.lines)
    )
    val content = s"${normalized.score};${normalized.lines}"
    Files.write(path, content.getBytes(StandardCharsets.UTF_8))
  }
}
