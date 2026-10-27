package tests

import com.sourcegraph.scip_java.BuildInfo
import com.sourcegraph.scip_java.buildtools.SbtBuildTool
import com.sourcegraph.scip_java.buildtools.SbtVersionParser

class SbtVersionParserSuite extends munit.FunSuite {
  test("parsing sbt versions") {
    import SbtVersionParser.{versionSegments => parse}
    assertEquals(parse("1.9.7"), List(1, 9, 7))
    assertEquals(parse("1.10.0"), List(1, 10, 0))
    assertEquals(parse("1.10.0-RC1"), List(1, 10, 0))
    assertEquals(parse("0.13.17"), List(0, 13, 17))
    assertEquals(parse("0.13"), List(0, 13))
  }

  test("supported sbt versions") {
    import SbtBuildTool.{isSupportedSbtVersion => check}

    def checkSupported(version: String) = {
      assert(check(version).contains(true), check(version))
    }

    def checkUnsupported(version: String) = {
      assert(check(version).contains(false), check(version))
    }

    def checkFailed(version: String) = {
      assert(check(version).isLeft, check(version))
    }

    checkSupported("1.10.0-RC1")
    checkSupported("1.5.6")
    checkSupported("1.9.7")
    checkSupported("1.2.0")

    checkUnsupported("1.0.0-RC1")
    checkUnsupported("1.1.6")
    checkUnsupported("0.13.16")

    checkFailed("1.0-RC1")
    checkFailed("0.13")
    checkFailed("BLA")
    checkFailed("")
  }

  test("sbt-sourcegraph version") {
    import SbtBuildTool.{sbtSourcegraphVersion => pick}
    assertEquals(pick(Some("1.3.3")), "0.4.3")
    assertEquals(pick(Some("1.8.3")), "0.4.3")
    assertEquals(pick(Some("1.9.0")), BuildInfo.sbtSourcegraphVersion)
    assertEquals(pick(Some("1.10.0-RC1")), BuildInfo.sbtSourcegraphVersion)
    assertEquals(pick(None), BuildInfo.sbtSourcegraphVersion)
  }
}
