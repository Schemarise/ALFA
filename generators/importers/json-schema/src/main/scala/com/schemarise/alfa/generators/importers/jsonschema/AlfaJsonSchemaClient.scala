package com.schemarise.alfa.generators.importers.jsonschema

import org.everit.json.schema.loader.internal.DefaultSchemaClient

import java.io.{File, FileInputStream, InputStream}
import java.net.URL
import java.nio.file.{Files, Path}

class AlfaJsonSchemaClient(root:Path) extends DefaultSchemaClient {
  override def get(url: String): InputStream = {
    val u = new URL(url)

    if ( u.getProtocol == "file") {
      val p = root.toString + u.getFile
      Files.newInputStream(root.resolve(p))
//      new FileInputStream(new File(p))
    }
    else {
      super.get(url)
    }
  }
}
