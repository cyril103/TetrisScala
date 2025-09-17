package tetris.core

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

class ConfigStorageSpec extends AnyFunSuite with Matchers {

  private def withTempPath[A](body: Path => A): A = {
    val dir = Files.createTempDirectory("tetris-config-test")
    val file = dir.resolve("config.properties")
    try body(file)
    finally {
      Files.deleteIfExists(file)
      Files.deleteIfExists(dir)
    }
  }

  test("load returns defaults when file missing") {
    withTempPath { path =>
      ConfigStorage.withCustomPath(path) {
        ConfigStorage.load() shouldEqual GameConfig()
      }
    }
  }

  test("load parses valid properties") {
    withTempPath { path =>
      val content = """startingLevel=4
volume=75
darkMode=false
resetHighScoreOnStart=true
"""
      Files.writeString(path, content, StandardCharsets.UTF_8)

      ConfigStorage.withCustomPath(path) {
        ConfigStorage.load() shouldEqual GameConfig(startingLevel = 4, volume = 75, darkMode = false, resetHighScoreOnStart = true)
      }
    }
  }

  test("load ignores invalid values and falls back to defaults") {
    withTempPath { path =>
      val content = """startingLevel=not-a-number
volume=500
darkMode=maybe
resetHighScoreOnStart=perhaps
"""
      Files.writeString(path, content, StandardCharsets.UTF_8)

      ConfigStorage.withCustomPath(path) {
        ConfigStorage.load() shouldEqual GameConfig(volume = 100)
      }
    }
  }

  test("save writes the configuration to disk") {
    withTempPath { path =>
      ConfigStorage.withCustomPath(path) {
        val config = GameConfig(startingLevel = 7, volume = 60, darkMode = false, resetHighScoreOnStart = true)
        ConfigStorage.save(config)

        val raw = Files.readString(path, StandardCharsets.UTF_8)
        raw.contains("startingLevel=7") shouldBe true
        raw.contains("volume=60") shouldBe true
        raw.contains("darkMode=false") shouldBe true
        raw.contains("resetHighScoreOnStart=true") shouldBe true
      }
    }
  }
}
