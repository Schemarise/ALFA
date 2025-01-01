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
package com.schemarise.alfa.compiler.test.lang.datatypes

import com.schemarise.alfa.compiler.test.lang.AlfaCoreFunSuite
import com.schemarise.alfa.compiler.tools.tabular.UdtFlattener
import com.schemarise.alfa.compiler.utils.TestCompiler

class TableFlattenTest extends AlfaCoreFunSuite {
  test("Table with meta cols") {
    val s =
      """
        |namespace demo
        |
        |record MetaInfo {
        |    RecName : $recordName
        |    Obj : $record
        |    Numbers : list< int >
        |    Dict : map< ID : int, DAT : string >
        |}
        |""".stripMargin

    val cua = TestCompiler.compileValidScript(s)
    val mi = cua.getUdt("demo.MetaInfo").get

    val f = new UdtFlattener(cua, mi)
    val t = f.table

    assertEqualsIgnoringWhitespace(t.toString,
      """
        |tablular demo.MetaInfo {
        |  RecName : string
        |  Obj : string
        |  Numbers$Idx : long
        |  Numbers : int
        |  ID : int
        |  DAT : string
        |}
        |""".stripMargin)
  }

  test("Table with udts cols") {
    val s =
      """
        |namespace demo
        |
        |entity Customer key(cusId:uuid) includes Person {
        |    Accounts : list< string(8,8) >      ## List of accounts held
        |    CustType : CustomerType             ## Type of customer based on annual spend
        |    PassCodeToken : string(5,*)         ## Security Token
        |    LinkedCustomer : uuid?
        |}
        |
        |trait Person scope Employee, Customer {
        |    FirstName : string          ## Legal first and middle name
        |    LastName : string           ## Legal last name
        |    DateOfBirth : date          ## Date of birth
        |}
        |
        |enum CustomerType {
        |    Standard                    ## Less than $1000 spend annually
        |    Gold                        ## Between $1000 to $10,000 spend annually
        |    Platinum                    ## Over $10,000 annual spend
        |}
        |
        |entity Employee key(id:uuid) includes Person {
        |    # Official job title
        |    JobTitle : string
        |
        |    # Annual salary
        |    Salary : double
        |}
        |
        |""".stripMargin

    val cua = TestCompiler.compileValidScript(s)
    val mi = cua.getUdt("demo.Customer").get

    val f = new UdtFlattener(cua, mi)
    val t = f.table
    println(t)
  }


  // TODO Need more tests


}
