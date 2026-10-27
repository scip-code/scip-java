package com.sourcegraph.scip_java.buildtools

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties

import scala.util.Using

import com.sourcegraph.io.AutoDeletedFile
import com.sourcegraph.scip_java.BuildInfo
import com.sourcegraph.scip_java.commands.IndexCommand

class SbtBuildTool(index: IndexCommand) extends BuildTool("sbt", index) {
  override def usedInCurrentDirectory(): Boolean = {
    Files.isRegularFile(index.workingDirectory.resolve("build.sbt")) ||
    sbtVersion().isDefined
  }

  override def generateScip(): Int = {
    sbtVersion() match {
      case Some(version) =>
        if (isSupportedSbtVersion(version)) {
          unconditionallyGenerateScip()
        } else {
          failFast(
            s"Unsupported sbt version '$version'. " +
              s"To fix this problem, upgrade to sbt ${BuildInfo
                  .sbtVersion} and try again."
          )
        }
      case None =>
        failFast(
          s"No sbt version detected. " +
            s"To fix this problem, run the following command and try again: " +
            s"echo 'sbt.version=${BuildInfo
                .sbtVersion}' >> project/build.properties"
        )
    }
  }

  private def failFast(message: String): Int = {
    index.app.error(message)
    1
  }

  private def unconditionallyGenerateScip(): Int =
    Using.resource(sourcegraphSbtPluginFile()) { _ =>
      val buildCommand = index.finalBuildCommand(List("sourcegraphScip"))

      val sourcegraphScip = index.process(
        List("sbt", "sourcegraphEnable") ++ buildCommand
      )

      val inputDump = index
        .workingDirectory
        .resolve("target")
        .resolve("sbt-sourcegraph")
        .resolve("index.scip")

      if (sourcegraphScip.exitCode == 0 && Files.isRegularFile(inputDump)) {
        val outputDump = index.workingDirectory.resolve(index.output)
        Files.copy(inputDump, outputDump, StandardCopyOption.REPLACE_EXISTING)
        index.app.info(outputDump.toString)
      }
      sourcegraphScip.exitCode
    }

  private def isSupportedSbtVersion(version: String): Boolean = {
    SbtBuildTool.isSupportedSbtVersion(version) match {
      case Left(message) =>
        index.app.error(message)
        false
      case Right(value) =>
        value
    }
  }

  private def sbtVersion(): Option[String] = {
    val buildProperties = index
      .workingDirectory
      .resolve("project")
      .resolve("build.properties")
    if (Files.isRegularFile(buildProperties)) {
      val props = new Properties()
      val in = Files.newInputStream(buildProperties)
      try props.load(in)
      finally in.close()
      Option(props.getProperty("sbt.version"))
    } else {
      None
    }
  }

  private def sourcegraphSbtPluginFile(): AutoDeletedFile = {
    val addSbtPluginFile = index
      .workingDirectory
      .resolve("project")
      .resolve("sourcegraph_generated.sbt")
    val version = SbtBuildTool.sbtSourcegraphVersion(sbtVersion())
    AutoDeletedFile.fromPath(
      addSbtPluginFile,
      s"""addSbtPlugin("com.sourcegraph" % "sbt-sourcegraph" % "$version")
         |""".stripMargin
    )
  }
}

object SbtBuildTool {
  // sbt-sourcegraph 0.4.4 is published to Maven Central only under the
  // Maven-compatible file names that sbt 1.9 introduced, so earlier sbt
  // versions cannot download it. 0.4.3, the version that scip-java 0.11.x
  // used, is also published under the legacy names.
  def sbtSourcegraphVersion(sbtVersion: Option[String]): String =
    sbtVersion.map(SbtVersionParser.versionSegments) match {
      case Some(1 :: minor :: _) if minor < 9 =>
        "0.4.3"
      case _ =>
        BuildInfo.sbtSourcegraphVersion
    }

  def isSupportedSbtVersion(version: String): Either[String, Boolean] = {
    SbtVersionParser.versionSegments(version) match {
      case major :: minor :: _ :: _ =>
        Right(major == 1 && minor >= 2)

      case _ =>
        Left(
          s"Failed to parse SBT version: [$version]. Only SBT 1.2+ is supported"
        )

    }
  }
}

object SbtVersionParser {
  def versionSegments(raw: String) = raw
    .takeWhile(c => c.isDigit || c == '.')
    .split("\\.", 3)
    .toList
    .flatMap(_.toIntOption)
}
