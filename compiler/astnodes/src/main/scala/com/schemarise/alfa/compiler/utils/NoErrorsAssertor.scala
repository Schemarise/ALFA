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

import com.schemarise.alfa.compiler.ast.ResolvableNode
import com.schemarise.alfa.compiler.ast.model.{IDataproduct, ITransform, NodeVisitMode}
import com.schemarise.alfa.compiler.ast.model.NodeVisitMode.Mode

class NoErrorsAssertor extends NodeMethodApplyVisitor {

  private var _foundErrors = false

  def foundErrors = _foundErrors

  def apply(u: Any): Mode = {
    if (u.isInstanceOf[ResolvableNode]) {
      val e = u.asInstanceOf[ResolvableNode]
      if (e.hasLocalNodeErrors) {
        _foundErrors = true
        return NodeVisitMode.Break
      }
    }

    NodeVisitMode.Continue
  }
}
