package com.schemarise.alfa.utils.cli

import com.schemarise.alfa.compiler.utils.{StdoutLogger, VFS}
import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import java.nio.file.{FileSystem, Files, Path, Paths}
import java.util.regex.Pattern

class AlfaCliTest extends AnyFunSuite {
  val logger = new StdoutLogger()

  val targetDir = new File(getClass.getResource("/").getPath + "../").getCanonicalPath + "/"
  val testDir = new File(targetDir, "../src/test/resources/").getCanonicalPath + "/"

  test("regex") {
    val pat = Pattern.compile("(\\d+|ACT)\\/(\\d+)")
    val m = pat.matcher("ACT/360")
    val x = m.matches()

    println(x)

  }


  test("Test Alfa Main Compile") {

    val fs = VFS.create()
    val path = fs.getPath("customer.alfa")

    VFS.write(logger, path,
      """
        |entity com.acme.Customer key ( Id : uuid ) {
        |   Name : string
        |}
      """.stripMargin)

    val smt = new AlfaCli(compileFlag = true, path = Some(path))
    smt.run()
  }

  test("Test Alfa Main Compile and Generate") {

    val fs = VFS.create()
    val path = fs.getPath("customer.alfa")
    val output = fs.getPath("/gen/")

    VFS.write(logger, path,
      """
        |entity com.acme.Customer key ( Id : uuid ) {
        |   Name : string
        |}
      """.stripMargin)

    val smt = new AlfaCli(compileFlag = true, exporters = Some("java"),
      path = Some(path), outputDir = Some(output))
    smt.run()

    assert(Files.exists(fs.getPath("/gen/com/acme/Customer.java")))
    assert(Files.exists(fs.getPath("/gen/com/acme/CustomerKey.java")))

    //    VFS.printFileSystemContents(output)
  }

  private def makeCustomersPackage(fs: FileSystem): Path = {
    val modulesPath = fs.getPath("modules")
    VFS.mkdir(modulesPath)

    val settingsPath = fs.getPath("src/customers/settings.alfa-proj.yaml")

    VFS.write(logger, settingsPath,
      """
        |name: com.acme:customers
        |
      """.stripMargin)

    VFS.write(logger, fs.getPath("src/customers/customer.alfa"),
      """
        |entity com.acme.Customer key ( Id : uuid ) {
        |   Name : string
        |}
      """.stripMargin)

    val smt = new AlfaCli(compileFlag = true, installFlag = true,
      modulePaths = Some(List(modulesPath)), path = Some(settingsPath))

    smt.run()

    fs.getPath("modules/com.acme:customers.zip")
  }

  private def makeOrdersPackage(fs: FileSystem): Path = {
    val modulesPath = fs.getPath("modules")
    VFS.mkdir(modulesPath)

    val settingsPath = fs.getPath("src/orders/settings.alfa-proj.yaml")

    VFS.write(logger, settingsPath,
      """
        |name: com.acme:orders
        |dependencies: ['com.acme:customers']
        |
      """.stripMargin)

    VFS.write(logger, fs.getPath("src/orders/orders.alfa"),
      """
        |entity com.acme.Order key ( Id : uuid ) {
        |}
      """.stripMargin)

    val smt = new AlfaCli(compileFlag = true, installFlag = true,
      modulePaths = Some(List(modulesPath)), path = Some(settingsPath))
    smt.run()

    fs.getPath("modules/com.acme:orders.zip")
  }

  test("Test SMT Main Install") {
    val fs = VFS.create()
    val pkg1 = makeCustomersPackage(fs)
    assert(Files.exists(fs.getPath("modules/com.acme-customers.zip")))
  }

  test("Test Main Module Dependency") {
    val fs = VFS.create()
    val pkg1 = makeCustomersPackage(fs)
    val pkg2 = makeOrdersPackage(fs)
  }
}

