package tetris.core

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import scala.util.Try

object HighScoreStorage {
  private var customPath: Option[Path] = None

  private def filePath: Path = customPath.getOrElse(Paths.get(System.getProperty("user.dir"), "highscore.dat"))

  def withCustomPath[T](path: Path)(body: => T): T = synchronized {
    val previous = customPath
    customPath = Some(path)
    try body
    finally {
      customPath = previous
    }
  }

  def load(): Long = {
    val path = filePath
    if (Files.exists(path)) {
      val raw = new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim
      Try(raw.toLong).getOrElse(0L)
    } else {
      0L
    }
  }

  def save(score: Long): Unit = {
    val path = filePath
    Option(path.getParent).foreach(parent => if (!Files.exists(parent)) Files.createDirectories(parent))
    Files.write(path, score.toString.getBytes(StandardCharsets.UTF_8))
  }
}
