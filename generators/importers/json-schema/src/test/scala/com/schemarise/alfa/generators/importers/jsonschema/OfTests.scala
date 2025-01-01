package com.schemarise.alfa.generators.importers.jsonschema

import org.everit.json.schema.loader.SchemaLoader
import org.everit.json.schema.{CombinedSchema, ObjectSchema, Schema}
import org.json.{JSONObject, JSONTokener}
import org.scalatest.funsuite.AnyFunSuite

class OfTests extends AnyFunSuite {

  val sample1 =
    """
      |{
      |    "description" : "schema validating people and vehicles",
      |    "type" : "object",
      |    "oneOf" : [
      |       {
      |        "type" : "object",
      |        "properties" : {
      |            "firstName" : {
      |                "type" : "string"
      |            },
      |            "lastName" : {
      |                "type" : "string"
      |            },
      |            "sport" : {
      |                "type" : "string"
      |            }
      |          }
      |      },
      |      {
      |        "type" : "object",
      |        "properties" : {
      |            "vehicle" : {
      |                "type" : "string"
      |            },
      |            "price" : {
      |                "type" : "integer"
      |            }
      |        },
      |        "additionalProperties":false
      |      }
      |    ]
      |}
    """.stripMargin

  val sample2 =
    """
      |{
      |    "$schema": "http://json-schema.org/draft-04/schema#",
      |    "type": "object",
      |    "title": "My schema",
      |    "additionalProperties": true,
      |    "properties": {
      |        "AddressLine1": { "type": "string" },
      |        "AddressLine2": { "type": "string" },
      |        "City":         { "type": "string" }
      |    },
      |    "required": [ "AddressLine1" ],
      |    "oneOf": [
      |        {
      |            "type": "object",
      |            "properties": {
      |                "State":   { "type": "string" },
      |                "ZipCode": { "type": "string" }
      |            },
      |            "required": [ "ZipCode" ]
      |        },
      |        {
      |            "type": "object",
      |            "properties": {
      |                "County":   { "type": "string" },
      |                "PostCode": { "type": "string" }
      |            },
      |            "required": [ "PostCode" ]
      |        }
      |    ]
      |}
    """.stripMargin

  val sample3 =
    """
      |{
      |    "$schema": "http://json-schema.org/draft-04/schema#",
      |    "type": "object",
      |    "title": "My schema",
      |    "properties": {
      |        "Address": { "type": "string" },
      |    },
      |    "required": [ "Address" ],
      |    "anyOf": [
      |        {
      |          "allOf":[
      |               {
      |                  "type": "object",
      |                  "properties": {
      |                      "State":   { "type": "string" },
      |                      "City":   { "type": "string" },
      |                      "ZipCoded": { "type": "string" }
      |                  },
      |               },
      |               {
      |                  "required": [ "State", "City", "ZipCoded" ]
      |               }
      |             ]
      |        },
      |        {
      |          "allOf":[
      |               {
      |                  "type": "object",
      |                  "properties": {
      |                      "County":   { "type": "string" },
      |                      "PostCode": { "type": "string" }
      |                  },
      |               },
      |               {
      |                  "required": [ "County", "PostCode" ]
      |               }
      |            ]
      |        }
      |    ]
      |}
    """.stripMargin


  val sample4 =
    """
      |{
      |    "description" : "schema validating people and vehicles",
      |    "type" : "object",
      |    "properties" : {
      |        "name" : {
      |            "type" : "string"
      |        },
      |        "email" : {
      |            "type" : "string"
      |        },
      |        "phone" : {
      |            "type" : "string"
      |        }
      |      },
      |      "additionalProperties":false,
      |      "oneOf" : [
      |        {
      |           "required" : [ "name", "email" ],
      |        },
      |        {
      |           "required" : [ "name", "phone" ],
      |        }
      |      ]
      |}
    """.stripMargin


  test("Sample1") {
    JsonSchemaImporterTest.convertJsonSchemaToAlfa(sample1, "sample1")
  }

  test("Sample2") {
    JsonSchemaImporterTest.convertJsonSchemaToAlfa(sample2, "sample2")
  }

  test("Sample3") {
    JsonSchemaImporterTest.convertJsonSchemaToAlfa(sample3, "sample3")
  }

  test("Sample4") {
    JsonSchemaImporterTest.convertJsonSchemaToAlfa(sample4, "sample4")
  }

  test("Everit") {
    val rawSchema = new JSONObject(new JSONTokener(sample3))

    val schema: Schema = SchemaLoader.load(rawSchema)

    val cs = schema.asInstanceOf[CombinedSchema]

    val jsm = new CombinedModel(cs, "")
    val os: ObjectSchema = jsm.outputSchema

  }

  test("oneOf") {

    val schema =
      """
        |{
        |    "$schema": "http://json-schema.org/draft-04/schema#",
        |    "type": "object",
        |    "title": "My schema",
        |    "additionalProperties": true,
        |    "properties": {
        |        "AddressLine1": { "type": "string" },
        |        "AddressLine2": { "type": "string" },
        |        "City":         { "type": "string" }
        |    },
        |    "required": [ "AddressLine1" ],
        |    "oneOf": [
        |        {
        |            "type": "object",
        |            "properties": {
        |                "State":   { "type": "string" },
        |                "ZipCode": { "type": "string" }
        |            },
        |            "required": [ "ZipCode" ]
        |        },
        |        {
        |            "type": "object",
        |            "properties": {
        |                "County":   { "type": "string" },
        |                "PostCode": { "type": "string" }
        |            },
        |            "required": [ "PostCode" ]
        |        }
        |    ]
        |}
        |
        |/*
        |{
        |  "$schema": "http://json-schema.org/draft-07/schema#",
        |  "type": "object",
        |  "additionalItems": false,
        |  "properties": {
        |    "sample": {
        |      "oneOf": [
        |        {
        |          "type": "object",
        |          "anyOf": [
        |            {
        |              "required": [
        |                "a"
        |              ]
        |            },
        |            {
        |              "required": [
        |                "b"
        |              ]
        |            }
        |          ],
        |          "additionalProperties": false,
        |          "properties": {
        |            "a": {
        |              "type": "string"
        |            },
        |            "b": {
        |              "type": "string"
        |            }
        |          }
        |        },
        |        {
        |          "type": "object",
        |          "additionalProperties": false,
        |          "properties": {
        |            "c": {
        |              "type": "string"
        |            }
        |          }
        |        },
        |        {
        |          "type": "object",
        |          "additionalProperties": false,
        |          "properties": {
        |            "d": {
        |              "type": "string"
        |            }
        |          }
        |        }
        |      ]
        |    }
        |  }
        |}
        |*/
        |
      """.stripMargin


    JsonSchemaImporterTest.convertJsonSchemaToAlfa(schema, "sample5")

  }
}

