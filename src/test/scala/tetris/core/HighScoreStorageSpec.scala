package tetris.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

class HighScoreStorageSpec extends AnyFunSuite with Matchers {

  private def withTempPath[A](body: Path => A): A = {
    val dir = Files.createTempDirectory("tetris-highscore-storage-test")
    val file = dir.resolve("highscore.dat")
    try body(file)
    finally {
      Files.deleteIfExists(file)
      Files.deleteIfExists(dir)
    }
  }

  test("load returns 0 when the file does not exist") {
    withTempPath { path =>
      HighScoreStorage.withCustomPath(path) {
        HighScoreStorage.load() shouldEqual 0L
      }
    }
  }

  test("save persists the value and load reads it back") {
    withTempPath { path =>
      HighScoreStorage.withCustomPath(path) {
        HighScoreStorage.save(1234L)
        new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim shouldEqual "1234"
        HighScoreStorage.load() shouldEqual 1234L
      }
    }
  }

  test("load handles corrupt files by returning 0") {
    withTempPath { path =>
      Files.writeString(path, "not-a-number", StandardCharsets.UTF_8)
      HighScoreStorage.withCustomPath(path) {
        HighScoreStorage.load() shouldEqual 0L
      }
    }
  }
}
