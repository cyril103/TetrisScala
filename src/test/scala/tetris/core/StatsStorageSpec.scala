package tetris.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

class StatsStorageSpec extends AnyFunSuite with Matchers {

  private def withTempStats[A](body: Path => A): A = {
    val dir = Files.createTempDirectory("tetris-stats-test")
    val file = dir.resolve("stats.dat")
    try body(file)
    finally {
      Files.deleteIfExists(file)
      Files.deleteIfExists(dir)
    }
  }

  test("load returns defaults when file missing") {
    withTempStats { path =>
      StatsStorage.withCustomPath(path) {
        StatsStorage.load() shouldEqual LastStats(0L, 0)
      }
    }
  }

  test("load parses valid stats") {
    withTempStats { path =>
      Files.writeString(path, "12345;12", StandardCharsets.UTF_8)
      StatsStorage.withCustomPath(path) {
        StatsStorage.load() shouldEqual LastStats(12345L, 12)
      }
    }
  }

  test("load sanitises invalid content") {
    withTempStats { path =>
      Files.writeString(path, "oops;nan", StandardCharsets.UTF_8)
      StatsStorage.withCustomPath(path) {
        StatsStorage.load() shouldEqual LastStats(0L, 0)
      }
    }
  }

  test("save persists the latest stats") {
    withTempStats { path =>
      StatsStorage.withCustomPath(path) {
        val stats = LastStats(score = 9876L, lines = 27)
        StatsStorage.save(stats)
        Files.readString(path, StandardCharsets.UTF_8) shouldEqual "9876;27"
      }
    }
  }
}
