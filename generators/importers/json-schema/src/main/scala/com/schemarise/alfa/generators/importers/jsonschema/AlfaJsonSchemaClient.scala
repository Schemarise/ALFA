package com.schemarise.alfa.generators.importers.jsonschema

import org.everit.json.schema.loader.internal.DefaultSchemaClient

import java.io.InputStream
import java.net.URL
import java.nio.file.{Files, Path}

class AlfaJsonSchemaClient(root:Path) extends DefaultSchemaClient {
  override def get(url: String): InputStream = {
    val u = new URL(url)

    if ( u.getProtocol == "file") {
      val p = root.toString + u.getFile
      Files.newInputStream(root.resolve(p))
    }
    else {
      // If local version exists, use that. Maybe this should be configurable?
      val localVersion = root.resolve( u.getFile.split("/").last )

      if ( Files.exists(localVersion)) {
        Files.newInputStream(localVersion)
      }
      else {
        super.get(url)
      }
    }
  }
}
