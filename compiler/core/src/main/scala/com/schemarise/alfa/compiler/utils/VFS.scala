/**
 * Copyright 2024 Schemarise Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.schemarise.alfa.compiler.utils

import com.google.common.jimfs.{Configuration, Jimfs}
import org.apache.commons.io.FileUtils
import org.fusesource.jansi.Ansi.ansi

import java.io.{BufferedReader, InputStreamReader}
import java.net.{URI, URL}
import java.nio.file.attribute.{BasicFileAttributes, PosixFilePermission}
import java.nio.file._
import java.util.stream.Collectors
import java.util.zip.{ZipEntry, ZipInputStream}
import scala.collection.JavaConverters._

/**
 * Virtual File System
 */
object VFS {
  def mkdir(p: Path): Path = {
    mkdir(p.getFileSystem, p.toString)
  }

  def clearFiles(p: Path): Unit = {
    try {
      FileUtils.deleteDirectory(p.toFile)
    } catch {
      case e: Throwable =>
        System.err.println("WARN: failed to clear directory " + p)
    }
  }


  def listFileForExtension(p: Path, ext: String): List[Path] = {
    Files.walk(p).
      collect(Collectors.toList[Path]).
      asScala.
      filter(_.toString.endsWith(ext)).toList
  }

  def mkdir(fs: FileSystem, dir: String): Path = {
    val p = fs.getPath(dir).toAbsolutePath
    if (!Files.exists(p))
      Files.createDirectories(p)
    p
  }

  def printFileSystemContents(fs: FileSystem): Unit = printFileSystemContents(fs.getPath("/"))

  def printFileSystemContents(p: Path): Unit = {
    println("Files:")
    Files.walk(p).filter(e => !Files.isDirectory(e)).forEach(e => println("  " + e))

    println("\nContents:")

    Files.walk(p).filter(e => !Files.isDirectory(e)).forEach(e => {

      val s = "---- " + p.relativize(e) + " -------------------------------------------------------------------------"

      println(ansi().fg(org.fusesource.jansi.Ansi.Color.BLUE).a(s.substring(0, 70)).reset())
      if (e.toString.endsWith(".zip")) {
        val zis = new ZipInputStream(Files.newInputStream(e))
        var ze: ZipEntry = zis.getNextEntry()

        val buffer = new Array[Byte](1024 * 32)

        while (ze != null) {
          println("\n--- " + ze.getName() + " :")
          val len = zis.read(buffer)
          if (len >= 0)
            print(new String(buffer, 0, len))
          else
            println("*** File " + ze.getName() + " length " + len)

          ze = zis.getNextEntry()
        }

        zis.closeEntry()
        zis.close()
      }
      else {
        println(new String(Files.readAllBytes(e)))
      }
    })
  }


  def ls(p: Path): Unit = {

    if (Files.isDirectory(p))
      println(p.toAbsolutePath)

    if (p.toString.endsWith(".zip")) {
      println("ZIP:" + p.toAbsolutePath)

      val zis = new ZipInputStream(Files.newInputStream(p))
      var ze: ZipEntry = zis.getNextEntry()

      val buffer = new Array[Byte](1024 * 32)


      while (ze != null) {
        println("\t" + ze.getName)

        ze = zis.getNextEntry()
      }

      zis.closeEntry()
      zis.close()
    }

    Files.walk(p).filter(e => Files.isDirectory(e)).forEach(e => ls(e))
  }

  def traverseContents(fs: FileSystem): Iterator[FileAndContents] = {
    val p = fs.getPath("/")
    Files.walk(p).
      filter(e => !Files.isDirectory(e)).
      iterator().
      asScala.
      map(e => FileAndContents(e, new String(Files.readAllBytes(e))))
  }

  def append(f: Path, contents: String): Unit = {
    Files.write(f, contents.getBytes(), StandardOpenOption.APPEND)
  }

  def load(logger: ILogger, sourcePath: Path, targetPath: Path): Unit = {
    if (Files.isDirectory(sourcePath)) {
      logger.error(s"Loading from directory not supported ($sourcePath). Specify a file path.")
    }
    else {
      val contents = VFS.read(sourcePath)
      val fname = sourcePath.getFileName.toString

      VFS.write(targetPath.resolve(fname), contents)
    }
  }

  def writeInNewFs(filePath: String, contents: String) = {
    val fs = VFS.create()
    val root = VFS.mkdir(fs, "/")
    val sp = root.resolve(filePath)
    write(sp, contents)
    sp
  }

  def write(f: Path, contents: String): Unit = {
    write(new StdoutLogger(), f, contents, false)
  }

  def read(u : URL ) : String = {
    val in = new BufferedReader(new InputStreamReader(u.openStream()))

    val buf = new StringBuilder()

    var inputLine = ""
    while ((inputLine = in.readLine()) != null) {
      buf.append(inputLine)
    }

    in.close()
    buf.toString()
  }

  def read(f: Path): String = {
    new String(Files.readAllBytes(f))
  }

  def write(logger: ILogger, unnormalized: Path, contents: String,
            skipIdentical: Boolean = true, makeShExtensionExecutable: Boolean = true): Unit = {
    val f = unnormalized.normalize()

    if (f.getParent != null && !Files.exists(f.getParent))
      mkdir(f.getFileSystem, f.getParent.toString)

    if (skipIdentical &&
      Files.exists(f) &&
      Files.size(f) == contents.length &&
      new String(Files.readAllBytes(f)).equals(contents)) {
      logger.debug("Skipped writing identical file contents " + f)
    }
    else {
      val opt = if (Files.exists(f)) Array(StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE) else Array(StandardOpenOption.CREATE_NEW)
      Files.write(f, contents.getBytes, opt: _*)

      if (makeShExtensionExecutable && unnormalized.toString.endsWith(".sh")) {
        val views = f.getFileSystem.supportedFileAttributeViews()
        if (views.contains("posix")) {
          val permissions = Files.getPosixFilePermissions(f)
          permissions.add(PosixFilePermission.OWNER_EXECUTE)
          Files.setPosixFilePermissions(f, permissions)
        }
      }
    }

  }

  def create(): FileSystem = {
    val config = Configuration.unix().toBuilder()
      .setWorkingDirectory("/")
      .build();

    val fs = Jimfs.newFileSystem(config)
    fs
  }

  def createAndGetPath(): Path = {
    val fs = create()
    fs.getPath("/")
  }

  def createAndCopyFrom(src: Path, includeFilter: Function[Path, Boolean] = (p) => true): FileSystem = {
    val fs = create()
    Files.walkFileTree(src, new DirCopyVisitor(src, fs, includeFilter))
    fs
  }

  def createFromZIP(zipFile: Path): FileSystem = {
    val uri = URI.create("jar:file:" + zipFile.toString)

    try {
      FileSystems.getFileSystem(uri)
    } catch {
      case fne: FileSystemNotFoundException =>
        FileSystems.newFileSystem(uri, java.util.Collections.emptyMap[String, Object](), null.asInstanceOf[ClassLoader])
    }
  }
}

case class FileAndContents(p: Path, contents: String) {}

class DirCopyVisitor(var src: Path, var tgtfs: FileSystem, includeFilter: Function[Path, Boolean] = (p) => true) extends SimpleFileVisitor[Path] {
  override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
    val srcFile = src.relativize(file)
    val srcParent = srcFile.getParent
    val tgtFile = tgtfs.getPath(srcFile.toString)

    if (srcParent != null) {
      val tgtParent = tgtFile.getParent
      if (Files.notExists(tgtParent))
        VFS.mkdir(tgtParent)
    }

    if (!Files.isDirectory(file) &&
      !Files.exists(tgtFile) &&
      includeFilter.apply(tgtFile)) {
      Files.copy(file, tgtFile)
    }

    FileVisitResult.CONTINUE
  }
}
