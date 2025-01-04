package com.schemarise.alfa.generators.common

import java.io.File
import java.net.URI
import java.nio.file._
import java.util


object AlfaPath {
  def get(p: String) = {
    if (p.startsWith("http://") || p.startsWith("https://"))
      new AlfaPath(p)
    else
      Paths.get(p)
  }
}

class AlfaPath(uri: String) extends Path {

  override def getFileSystem: FileSystem = ???

  override def isAbsolute: Boolean = ???

  override def getRoot: Path = ???

  override def getFileName: Path = ???

  override def getParent: Path = {
    val i = uri.lastIndexOf("/")
    new AlfaPath(uri.substring(0, i + 1))
  }

  override def getNameCount: Int = ???

  override def getName(index: Int): Path = ???

  override def subpath(beginIndex: Int, endIndex: Int): Path = ???

  override def startsWith(other: Path): Boolean = ???

  override def startsWith(other: String): Boolean = ???

  override def endsWith(other: Path): Boolean = ???

  override def endsWith(other: String): Boolean = ???

  override def normalize(): Path = ???

  override def resolve(other: Path): Path = ???

  override def resolve(other: String): Path = new AlfaPath(uri + other)

  override def resolveSibling(other: Path): Path = ???

  override def resolveSibling(other: String): Path = ???

  override def relativize(other: Path): Path = ???

  override def toUri: URI = URI.create(uri)

  override def toAbsolutePath: Path = ???

  override def toRealPath(options: LinkOption*): Path = ???

  override def toFile: File = ???

  override def register(watcher: WatchService, events: Array[WatchEvent.Kind[_]], modifiers: WatchEvent.Modifier*): WatchKey = ???

  override def register(watcher: WatchService, events: WatchEvent.Kind[_]*): WatchKey = ???

  override def iterator(): util.Iterator[Path] = ???

  override def compareTo(other: Path): Int = ???

  override def toString: String = uri
}
