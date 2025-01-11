package com.schemarise.alfa.runtime.utils

import java.util.{Collections, Optional}
import com.schemarise.alfa.runtime.{Alfa, AlfaObject, JsonCodec, ValidationCollectingListener}
import schemarise.alfa.runtime.model.asserts.ConstraintType
import com.schemarise.alfa.runtime.codec.CodecConfig
import com.schemarise.alfa.runtime.codec.json.JsonCodecConfig
import com.schemarise.alfa.runtime.codec.table.TableCodec
import flattentest.FlatObj
import nestedtuple.TupleStrLen
import org.scalatest.funsuite.AnyFunSuite

class NestedTupleValidationTest extends AnyFunSuite {

  val json1 =
    """{
      |    "payload" : {
      |        "level1" : "10",
      |        "nested1" : {
      |            "level2" : "dfg"
      |        },
      |        "nested2" : [
      |            {
      |                "level3" : "dskl"
      |            },
      |            {
      |                "level3" : ""
      |            }
      |        ],
      |        "nestedOpt1" : {
      |            "level4" : "dfg"
      |        },
      |        "nestedOpt2" : [
      |            {
      |                "level5" : "dskl"
      |            },
      |            {
      |                "level5" : ""
      |            }
      |        ]
      |    }
      |}
    """.stripMargin

  test("validate nested 1") {

    val cc = JsonCodecConfig.builder().
      setAssignableToClass(classOf[TupleStrLen]).
      setShouldValidateOnBuild(false).
      setAssertListener(new ValidationCollectingListener() ).
      build()

    val d: TupleStrLen = Alfa.jsonCodec.fromJsonString(cc, json1)
    d.validate(cc)

    val vr = cc.getAssertListener.getValidationReport.build()

    assert(vr.getAlerts.size() == 2)
    assert(vr.getAlerts.get(0).getViolatedConstraint.get() == ConstraintType.OutsidePermittedRange)
  }

  test("Perf tuple reading") {

    var i = 0
    while (i < 1000) {
      runTest()
      i += 1
    }

  }

  def runTest(): Unit = {
    val cc = JsonCodecConfig.builder().
      setAssignableToClass(classOf[TupleStrLen]).
      setShouldValidateOnBuild(false).
      setAssertListener(new ValidationCollectingListener() ).
      build()

    val d: TupleStrLen = Alfa.jsonCodec.fromJsonString(cc, json1)
    d.validate(cc)

  }
}
