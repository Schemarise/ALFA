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
package com.schemarise.alfa.compiler.settings

//case class PackageSettings(val OutputPath : Option[Path] = None ) {
//
//  override def toString: String = {
//    val sb = new mutable.StringBuilder()
//
//    if ( OutputPath.isDefined )
//      sb.append("  " + BuildSettingsFlags.OutputPath.toString + ": " +
//        OutputPath.get.toString )
//
//    if ( sb.length > 0 )
//      sb.insert(0, "build:\n")
//
//    sb.toString
//  }
//}
//
//object PackageSettings {
//  def fromConfig(settings : Map[String,Object] ): PackageSettings = {
//
//    settings.keySet.foreach( k => {
//      if ( ! BuildSettingsFlags.withNameOpt(k).isDefined ) {
//        throw new CompilerSettingsException("Unknown package setting specified '" + k + "'" )
//      }
//    } )
//
//    val op = settings.get( BuildSettingsFlags.OutputPath.toString )
//    val p = if ( op.isDefined ) Paths.get(op.get.toString ) else Paths.get("build")
//
//    PackageSettings(Some(p))
//  }
//}
//
//object BuildSettingsFlags extends com.schemarise.alfa.compiler.SearchableEnumeration {
//  type FlagSettings = Value
//
//  val OutputPath = Value("OutputPath")
//
//  def withNameOpt(s: String): Option[Value] = values.find(_.toString == s)
//}