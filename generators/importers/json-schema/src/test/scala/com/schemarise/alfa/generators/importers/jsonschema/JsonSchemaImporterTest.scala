package com.schemarise.alfa.generators.importers.jsonschema

import com.schemarise.alfa.compiler.utils.{StdoutLogger, VFS}
import com.schemarise.alfa.generators.common.AlfaImporterParams
import com.schemarise.alfa.utils.testing.AlfaFunSuite
import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import java.nio.file.{Path, Paths}
import java.util

class JsonSchemaImporterTest extends AlfaFunSuite {

  test("Test json-schema model 1") {
    //<editor-fold desc="...">
    val schemaStr =
      """
        |{
        |    "$id": "https://gitpod.io/gitpod.schema.json",
        |    "$schema": "http://json-schema.org/draft-07/schema#",
        |    "title": "Gitpod Config",
        |    "type": "object",
        |    "properties": {
        |        "ide": {
        |          "anyOf": [
        |            {
        |              "type": "string"
        |            },
        |            {
        |              "type": "string",
        |              "enum": [
        |                "theia",
        |                "code"
        |              ]
        |            }
        |          ],
        |          "default": "theia",
        |          "description": "Controls what ide should be used for a workspace.",
        |          "deprecationMessage": "The 'ide' property is an experimental feature and enabled only for some users."
        |        },
        |        "ports": {
        |            "type": "array",
        |            "description": "List of exposed ports.",
        |            "items": {
        |                "type": "object",
        |                "required": [
        |                    "port"
        |                ],
        |                "properties": {
        |                    "port": {
        |                        "type": ["number", "string"],
        |                        "pattern": "^\\d+[:-]\\d+$",
        |                        "description": "The port number (e.g. 1337) or range (e.g. 3000-3999) to expose."
        |                    },
        |                    "onOpen": {
        |                        "type": "string",
        |                        "enum": [
        |                            "open-browser",
        |                            "open-preview",
        |                            "notify",
        |                            "ignore"
        |                        ],
        |                        "description": "What to do when a service on this port was detected. 'notify' (default) will show a notification asking the user what to do. 'open-browser' will open a new browser tab. 'open-preview' will open in the preview on the right of the IDE. 'ignore' will do nothing."
        |                    },
        |                    "visibility": {
        |                        "type": "string",
        |                        "enum": [
        |                            "private",
        |                            "public"
        |                        ],
        |                        "default": "public",
        |                        "description": "Whether the port visibility should be private or public. 'public' (default) will allow everyone with the port URL to access the port. 'private' will only allow users with workspace access to access the port."
        |                    },
        |                    "name": {
        |                        "type": "string",
        |                        "deprecationMessage": "The 'name' property is deprecated.",
        |                        "description": "Port name (deprecated)."
        |                    },
        |                    "protocol": {
        |                        "type": "string",
        |                        "enum": [
        |                            "http",
        |                            "TCP",
        |                            "UDP"
        |                        ],
        |                        "deprecationMessage": "The 'protocol' property is deprecated.",
        |                        "description": "The protocol to be used. (deprecated)"
        |                    }
        |                },
        |                "additionalProperties": false
        |            }
        |        },
        |        "tasks": {
        |            "type": "array",
        |            "description": "List of tasks to run on start. Each task will open a terminal in the IDE.",
        |            "items": {
        |                "type": "object",
        |                "properties": {
        |                    "name": {
        |                        "type": "string",
        |                        "description": "Name of the task. Shown on the tab of the opened terminal."
        |                    },
        |                    "before": {
        |                        "type": "string",
        |                        "description": "A shell command to run before `init` and the main `command`. This command is executed on every start and is expected to terminate. If it fails, the following commands will not be executed."
        |                    },
        |                    "init": {
        |                        "type": "string",
        |                        "description": "A shell command to run between `before` and the main `command`. This command is executed only on after initializing a workspace with a fresh clone, but not on restarts and snapshots. This command is expected to terminate. If it fails, the `command` property will not be executed."
        |                    },
        |                    "prebuild": {
        |                        "type": "string",
        |                        "description": "A shell command to run after `before`. This command is executed only on during workspace prebuilds. This command is expected to terminate. If it fails, the workspace build fails."
        |                    },
        |                    "command": {
        |                        "type": "string",
        |                        "description": "The main shell command to run after `before` and `init`. This command is executed last on every start and doesn't have to terminate."
        |                    },
        |                    "env": {
        |                        "type": "object",
        |                        "description": "Environment variables to set."
        |                    },
        |                    "openIn": {
        |                        "type": "string",
        |                        "enum": [
        |                            "bottom",
        |                            "main",
        |                            "left",
        |                            "right"
        |                        ],
        |                        "description": "The panel/area where to open the terminal. Default is 'bottom' panel."
        |                    },
        |                    "openMode": {
        |                        "type": "string",
        |                        "enum": [
        |                            "split-top",
        |                            "split-left",
        |                            "split-right",
        |                            "split-bottom",
        |                            "tab-before",
        |                            "tab-after"
        |                        ],
        |                        "description": "The opening mode. Default is 'tab-after'."
        |                    }
        |                },
        |                "additionalProperties": false
        |            }
        |        },
        |        "image": {
        |            "type": [
        |                "object",
        |                "string"
        |            ],
        |            "description": "The Docker image to run your workspace in.",
        |            "default": "gitpod/workspace-full",
        |            "required": [
        |                "file"
        |            ],
        |            "properties": {
        |                "file": {
        |                    "type": "string",
        |                    "description": "Relative path to a docker file."
        |                },
        |                "context": {
        |                    "type": "string",
        |                    "description": "Relative path to the context path (optional). Should only be set if you need to copy files into the image."
        |                }
        |            },
        |            "additionalProperties": false
        |        },
        |        "checkoutLocation": {
        |            "type": "string",
        |            "description": "Path to where the repository should be checked out."
        |        },
        |        "workspaceLocation": {
        |            "type": "string",
        |            "description": "Path to where the IDE's workspace should be opened."
        |        },
        |        "gitConfig": {
        |            "type": [
        |                "object"
        |            ],
        |            "description": "Git config values should be provided in pairs. E.g. `core.autocrlf: input`. See https://git-scm.com/docs/git-config#_values.",
        |            "additionalProperties": {
        |                "type": "string"
        |            }
        |        },
        |        "github": {
        |            "type": "object",
        |            "description": "Configures Gitpod's GitHub app",
        |            "properties": {
        |                "prebuilds": {
        |                    "type": [
        |                        "boolean",
        |                        "object"
        |                    ],
        |                    "description": "Set to true to enable workspace prebuilds, false to disable them. Defaults to true.",
        |                    "properties": {
        |                        "master": {
        |                            "type": "boolean",
        |                            "description": "Enable prebuilds for the default branch (typically master). Defaults to true."
        |                        },
        |                        "branches": {
        |                            "type": "boolean",
        |                            "description": "Enable prebuilds for all branches. Defaults to false."
        |                        },
        |                        "pullRequests": {
        |                            "type": "boolean",
        |                            "description": "Enable prebuilds for pull-requests from the original repo. Defaults to true."
        |                        },
        |                        "pullRequestsFromForks": {
        |                            "type": "boolean",
        |                            "description": "Enable prebuilds for pull-requests from any repo (e.g. from forks). Defaults to false."
        |                        },
        |                        "addBadge": {
        |                            "type": "boolean",
        |                            "description": "Add a Review in Gitpod badge to pull requests. Defaults to true."
        |                        },
        |                        "addLabel": {
        |                            "type": [
        |                                "boolean",
        |                                "string"
        |                            ],
        |                            "description": "Add a label to a PR when it's prebuilt. Set to true to use the default label (prebuilt-in-gitpod) or set to a string to use a different label name. This is a beta feature and may be unreliable. Defaults to false."
        |                        }
        |                    }
        |                }
        |            },
        |            "additionalProperties": false
        |        },
        |        "vscode": {
        |            "type": "object",
        |            "description": "Configure VS Code integration",
        |            "additionalProperties": false,
        |            "properties": {
        |                "extensions": {
        |                    "type": "array",
        |                    "description": "List of extensions which should be installed for users of this workspace. The identifier of an extension is always '${publisher}.${name}'. For example: 'vscode.csharp'.",
        |                    "items": {
        |                        "type": "string"
        |                    }
        |                }
        |            }
        |        }
        |    },
        |    "additionalProperties": false
        |}
        |
      """.stripMargin
    //</editor-fold>

    JsonSchemaImporterTest.convertJsonSchemaToAlfa(schemaStr, "gitpod-model")
  }

  test("Test json-schema model 2") {
    //<editor-fold desc="...">
    // https://cswr.github.io/JsonSchema/spec/definitions_references/
    val str =
      """
        |{
        |  "$schema": "https://json-schema.org/draft-07/schema",
        |    "definitions": {
        |        "person": {
        |            "type": "object",
        |            "required": ["first_name", "last_name", "age"],
        |            "properties": {
        |                "first_name": {"type": "string"},
        |                "last_name": {"type": "string"},
        |                "age": {"type": "integer"}
        |            }
        |        },
        |        "football_team": {
        |            "type": "object",
        |            "required": ["name", "league"],
        |            "properties": {
        |                "name": {"type": "string"},
        |                "league": {"type": "string"},
        |                "year_founded": {"type": "integer"}
        |            }
        |        }
        |
        |    },
        |    "allOf": [
        |        {"$ref": "#/definitions/person"},
        |        {"$ref": "#/definitions/football_team"}
        |    ]
        |}
      """.stripMargin
    //</editor-fold>

    JsonSchemaImporterTest.convertJsonSchemaToAlfa(str, "football-team-model")
  }

  test("Test json-schema model 3") {
    //<editor-fold desc="...">
    val schemaStr =
      """
        |{
        |  "$schema": "https://json-schema.org/draft-07/schema",
        |  "$id": "https://example.com/product.schema.json",
        |  "title": "Product",
        |  "description": "A product from Acme's catalog",
        |  "type": "object",
        |  "properties": {
        |    "productId": {
        |      "description": "The unique identifier for a product",
        |      "type": "integer"
        |    },
        |    "productName": {
        |      "description": "Name of the product",
        |      "type": [ "string", "integer", "null" ]
        |    },
        |    "price": {
        |      "description": "The price of the product",
        |      "type": "number",
        |      "exclusiveMinimum": 0
        |    }
        |  },
        |  "required": [ "productId", "productName", "price" ]
        |}
        |
      """.stripMargin
    //</editor-fold>

    JsonSchemaImporterTest.convertJsonSchemaToAlfa(schemaStr, "product-model")
  }

  test("Test json-schema model 4") {
    //<editor-fold desc="...">
    val schemaStr =
      """
        |{
        |  "$schema": "https://json-schema.org/draft-07/schema",
        |  "$id": "https://example.com/product.schema.json",
        |  "title": "Product",
        |  "description": "A product from Acme's catalog",
        |  "type": "object",
        |  "properties": {
        |    "productId": {
        |      "description": "The unique identifier for a product",
        |      "type": "integer"
        |    },
        |    "productName": {
        |      "description": "Name of the product",
        |      "type": "string"
        |    },
        |    "price": {
        |      "description": "The price of the product",
        |      "type": "number",
        |      "exclusiveMinimum": 0
        |    },
        |     "status": {
        |       "enum": ["Created", "In-progress", "Completed", "Closed"]
        |     },
        |    "tags": {
        |      "description": "Tags for the product",
        |      "type": "array",
        |      "items": {
        |        "type": "string"
        |      },
        |      "minItems": 1,
        |      "uniqueItems": true
        |    }
        |  },
        |  "required": [ "productId", "productName", "price" ]
        |}
        |
      """.stripMargin
    //</editor-fold>

    JsonSchemaImporterTest.convertJsonSchemaToAlfa(schemaStr, "another-product-model")
  }


  test("Test json-schema model 5 - allOf") {
    val s =
      """
        |{
        |  "$schema": "http://json-schema.org/draft-07/schema#",
        |  "title": "bugtest",
        |  "description": "everit bug",
        |  "type": "object",
        |  "additionalProperties": false,
        |  "dependencies": {},
        |  "properties": {
        |    "arrayPropEnabled": {
        |      "type": "boolean"
        |    },
        |    "arrayProp": {
        |      "type": "array",
        |      "items": {
        |        "type": "number"
        |      },
        |      "default": [0,1,2,3]
        |    }
        |  },
        |  "allOf": [
        |    {
        |      "if": {
        |        "properties": {
        |          "arrayPropEnabled": true
        |        }
        |      },
        |      "then": {
        |        "dependencies": {
        |          "arrayPropEnabled": [
        |            "arrayProp"
        |          ]
        |        }
        |      }
        |    }
        |  ]
        |}
      """.stripMargin

    JsonSchemaImporterTest.convertJsonSchemaToAlfa(s, "conditional-schema.json")

  }

}

object JsonSchemaImporterTest {
  val targetDir = new File(getClass.getResource("/").getPath + "../").getCanonicalPath + "/"

  def convertJsonSchemaToAlfa(jsonSchemaStr: String, targetDir: String) = {

    val outDir = Paths.get(new AlfaFunSuite().targetGeneratedTestSources(targetDir))

    val f = VFS.create()
    val jsonSchemaFile = f.getPath("/test-schema.json")
    VFS.write(jsonSchemaFile, jsonSchemaStr)

    val m = new util.HashMap[String, Object]()
    m.put("namespace", "demo")
    val jsi = new JsonSchemaImporter( new AlfaImporterParams( new StdoutLogger(), jsonSchemaFile, outDir, m) )
  }

}
