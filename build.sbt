ThisBuild / scalaVersion := "3.3.1"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"

// Set a specific JavaFX version
val javafxVersion = "21"

// Function to determine the OS-specific classifier for JavaFX
val osName = sys.props("os.name").toLowerCase
val javafxClassifier = osName match {
  case n if n.contains("linux") => "linux"
  case n if n.contains("mac") => "mac"
  case n if n.contains("win") => "win"
  case _ => throw new RuntimeException(s"Unsupported operating system: $osName")
}

lazy val root = (project in file("."))
  .settings(
    name := "TetrisScala",
    // Add the ScalaFX dependency
    libraryDependencies += "org.scalafx" %% "scalafx" % "21.0.0-R32",
    // Add the platform-specific JavaFX dependencies
    libraryDependencies ++= Seq(
      "org.openjfx" % "javafx-base"    % javafxVersion classifier javafxClassifier,
      "org.openjfx" % "javafx-controls"  % javafxVersion classifier javafxClassifier,
      "org.openjfx" % "javafx-graphics"  % javafxVersion classifier javafxClassifier,
      "org.scalatest" %% "scalatest" % "3.2.18" % Test
    ),
    Compile / mainClass := Some("tetris.Main"),
    fork := true
  )
