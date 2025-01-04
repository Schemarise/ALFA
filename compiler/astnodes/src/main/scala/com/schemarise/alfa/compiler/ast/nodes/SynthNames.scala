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
package com.schemarise.alfa.compiler.ast.nodes

import com.schemarise.alfa.compiler.Context
import com.schemarise.alfa.compiler.ast.model.IToken
import com.schemarise.alfa.compiler.err.{AvoidCharsInName, ResolutionMessage}

object SynthNames {
  val TupleField = "TupleField__"
  val Union = "Union__"

  private val namePrefixes = Set(TupleField, Union)

  def assertNameWarnings(ctx: Context, loc: IToken, name: String): Unit = {
    val warn =
      if (name.indexOf("___") < 0)
        false
      else {
        val t = name.substring(0, Math.min(name.length, TupleField.size))
        val u = name.substring(0, Math.min(name.length, Union.size))

        !namePrefixes.contains(t) && !namePrefixes.contains(u)
      }

    // TODO UNDO
    if (warn)
      ctx.addResolutionWarning(new ResolutionMessage(loc, AvoidCharsInName)(None, List.empty, "Triple underscore", name))

    if (name.startsWith("$"))
      ctx.addResolutionWarning(new ResolutionMessage(loc, AvoidCharsInName)(None, List.empty, "Dollar", name))
  }
}
