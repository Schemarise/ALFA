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

grammar Alfa;

import Terminals;

compilationUnit
	:
	( LANGUAGE_VERSION  alfaVersion=INT )?
	( MODEL_ID  modelVersion=STRING )?

	( include | localize )*

	( fields | typeDefs | exprconst | extensionDecl )*

    udt*

	namespaceGroup*

	EOF
	;

udt
    :
      annotationDecl
    | enumDecl
    | union
    | trait
    | record
    | key
    | entity
    | serviceDecl
    | libraryDecl
    | methodDeclaration // used for transform
    | extension
    | dataproduct
    ;

extensionDecl
     : docAndAnnotations
	   EXTENSION  extName = idOnly
	   LEFT_BRACE 
	      field*
	   RIGHT_BRACE 
	;

linkageDecl
    :
     docAndAnnotations
     LINKAGE   linkageName = idOnly  sameline_docstrings
      LEFT_BRACKET  sourceExprs=expressionSequence  RIGHT_BRACKET 
      EQUAL_ARROW 
     (
         targetType=idOrQid (opt= QUESTIONMARK )?  LEFT_BRACKET  targetExprs=expressionSequence  RIGHT_BRACKET 
         |
         isList=LIST  LESS_THAN  targetType=idOrQid  GREATER_THAN  (opt= QUESTIONMARK )?  LEFT_BRACKET  targetExprs=expressionSequence  RIGHT_BRACKET 
     )
    ;

assertDecl
    :
     docAndAnnotations
     (
        assertType =  ASSERT   assertName = idOnly  sameline_docstrings
        |
        assertType =  ASSERTALL   assertName = idOnly  LEFT_BRACKET  argName = idOnly   RIGHT_BRACKET  sameline_docstrings
     )
     block
    ;

extension
    : docAndAnnotations
    extType = idOnly fullname = idOrQid
       LEFT_BRACKET 
          ( extensionAttrib )*
       RIGHT_BRACKET 
    ;

extensionAttrib
	: docAndAnnotations name=idOnly  EQUAL  expr=expressionUnit
	;

namespaceGroup
    :
    ( docAndAnnotations  NAMESPACE  namespaceName=idOrQid sameline_docstrings )

    udt*
    ;

exprconst
    :
    CONST name=idOnly  EQUAL  expr=expressionUnit
    ;

localize:
     IMPORT  imp=idOrQid ( wildcard=( STAR | DOT_STAR ) )?
    ;

include
	:  INCLUDE  includePath=STRING ;

fields
	:  FIELDS   LEFT_BRACE 
		fieldDecl*
	 RIGHT_BRACE  ;

serviceDecl
	: docAndAnnotations
	mods=modifiers
	 SERVICE 
	  name=idOrQid versionMarker? typeParameters? optFunctionParams? sameline_docstrings
	 LEFT_BRACE 
	     ( methodSignature )*
     RIGHT_BRACE 
;

libraryDecl
	: docAndAnnotations
	mods=modifiers
	libOrtest = ( LIBRARY  |  TESTCASE  )
	  name=idOrQid sameline_docstrings
	 LEFT_BRACE 
	     ( methodDeclaration )*
     RIGHT_BRACE 
;

methodDeclaration
	: methodSignature
      block
    ;

optFunctionParams
    :  LEFT_BRACKET  ( field ( COMMA  field )* )?  RIGHT_BRACKET 
    ;

typeDefs
	:  TYPEDEFS   LEFT_BRACE 
		typeDefDecl*
	 RIGHT_BRACE  ;

typeDefDecl
	: docAndAnnotations
	newType=idOnly typeParameters?  EQUAL 
	(
	       currType=fieldType
	    |
	        NATIVE  nativeImplClass=QID
	)
	sameline_docstrings;


annotationDecl
	: docAndAnnotations
	 ANNOTATION  name = idOrQid  LEFT_BRACKET  annotationTargets?  RIGHT_BRACKET  optIncludesList
	sameline_docstrings
	 LEFT_BRACE  field*  RIGHT_BRACE  ;
	
annotationTargets
	: idOnly ( COMMA  idOnly )*;

optExtends:
     EXTENDS  extendOrIncludeDef
;

optIncludesList
	: ( INCLUDES  extendOrIncludeDef ( COMMA  extendOrIncludeDef )* )? ;

optIncludedByList
	: ( scopedef=SCOPE ( extendOrIncludeDef ( COMMA  extendOrIncludeDef )* )? )? ;

extendOrIncludeDef
    :
    docAndAnnotations
    idOrQidWithOptTmplArgRefs
    ;

dataproduct
    : docAndAnnotations
      DATAPRODUCT name=idOrQid versionMarker? sameline_docstrings
       LEFT_BRACE 
      		(
                PUBLISH  LEFT_BRACE 
                    publishDecls*
                 RIGHT_BRACE 
      		)?

      		(
                CONSUME  LEFT_BRACE 
                    consumeDecls*
                 RIGHT_BRACE 
      		)?
       RIGHT_BRACE 
    ;

publishDecls
    : documentedVersionedIdOrQid
    ;

consumeDecls
    : consumeDPs=dpDocumentedVersionedIdOrQid
    ;

dpDocumentedVersionedIdOrQid:
    idOrQid  LEFT_BRACE 
        documentedVersionedIdOrQid*
     RIGHT_BRACE 
    ;

documentedVersionedIdOrQid:
       docAndAnnotations idOrQid versionMarker? sameline_docstrings
    ;

record
	: docAndAnnotations mods=modifiers
	   RECORD 
	  name=idOrQid versionMarker? typeParameters?
	  optExtends?
	  optIncludesList
	  sameline_docstrings
	   LEFT_BRACE 
		( vFields=field | vAssert=assertDecl | vlinkage=linkageDecl )*
	   RIGHT_BRACE  ;

union
	: docAndAnnotations mods=modifiers

  	   UNION 
	  name=idOrQid versionMarker?
	  (
          typeParameters?
  		  optExtends?
          optIncludesList sameline_docstrings
           LEFT_BRACE 
              ( vFields=field | vAssert=assertDecl )*
           RIGHT_BRACE 
	  |
          sameline_docstrings
           EQUAL 
	           untaggedTypes
	  )
;

untaggedTypes:
    fieldType ( PIPE  fieldType )*
;

key
	: docAndAnnotations mods=modifiers
	   KEY 
	  name=idOrQid typeParameters?
      optExtends?
	  optIncludesList sameline_docstrings
	   LEFT_BRACE 
	 	( vFields=field | vAssert=assertDecl | vlinkage=linkageDecl )*
	   RIGHT_BRACE  ;

trait
	: docAndAnnotations mods=modifiers
	 TRAIT 
	name=idOrQid versionMarker? typeParameters? optIncludesList optIncludedByList sameline_docstrings
	 LEFT_BRACE 
		( vFields=field | vAssert=assertDecl | vlinkage=linkageDecl )*
	 RIGHT_BRACE  ;

entity
	: docAndAnnotations mods=modifiers
	   ENTITY  (isUnion= UNION )?
	  name=idOrQid versionMarker? typeParameters?
		( 
			 KEY       
				(
					vKey=idOrQidWithOptTmplArgRefs 
					| vAnonKey = parenthsizedCommaSepFields
				)
		)?
		optExtends?
		optIncludesList sameline_docstrings
	 LEFT_BRACE 
			( field | vAssert=assertDecl | vlinkage=linkageDecl )*
	 RIGHT_BRACE  ;

parenthsizedCommaSepFields
	:  LEFT_BRACKET  ( field ( COMMA  field )* )?  RIGHT_BRACKET 
	;

enumField
	: docAndAnnotations name=idOnly ( LEFT_BRACKET  lexical=STRING  RIGHT_BRACKET  )? sameline_docstrings;

enumDecl
	: docAndAnnotations mods=modifiers
	 ENUM 
	name=idOrQid versionMarker? optIncludesList sameline_docstrings
	 LEFT_BRACE 
		( enumField ( COMMA ? enumField )* )?
	 RIGHT_BRACE  ;

annotations
	: ( anns=annotation )* ;

field
	: fieldReference=idOnly
	| fieldDecl ;

methodSignature
	: docAndAnnotations
	  fname=idOnly typeParameters?
      LEFT_BRACKET  functionParams?  RIGHT_BRACKET  ( COLON  returnType = fieldType )? methodExceptions?
      sameline_docstrings
    ;

methodExceptions
    : RAISES LEFT_BRACKET idOrQidWithOptTmplArgRefs ( COMMA  idOrQidWithOptTmplArgRefs )* RIGHT_BRACKET
    ;

functionParams
    : functionParam ( COMMA  functionParam )*
    ;

functionParam
    : ( IN | OUT | INOUT )? field
    ;

fieldDecl
	: docAndAnnotations
	fieldName=idOnly  separatorColon =  COLON  isConstant=CONST? fieldType ( EQUAL  fieldValue=expressionUnit )?
	sameline_docstrings;

annotation
	:  AT  annName = idOrQid ( LEFT_BRACKET  namedExpressionSequence?  RIGHT_BRACKET  )?
	;

docAndAnnotations
    : docstrings annotations | annotations docstrings;


idWithDoc
    :
    docAndAnnotations idOnly sameline_docstrings
    ;


block
    : ( LEFT_BRACE 
  		  statement*
         RIGHT_BRACE 
      )
    ;

lambdaBlock
    : ( LEFT_BRACE 
  		  statement*
         RIGHT_BRACE 
      )
      |
      exprStmt = expressionStatement
    ;

statementList
    : ( statement )+
    ;

literal
	: vStr = ( STRING | MULTILINE_STRING )
	| vInt = INT
	| vFloat = FLOAT
	| vNaN =  NAN 
	| vInfinity =  INFINITY 
	| vNegativeInfinity =  NEGATIVE_INFINITY 
	| vTrue =  TRUE 
	| vFalse =  FALSE 
	| vQid = QID
	| vOptQid = OPTQID
	| vQidCompletion = ID_COMPLETION
	| vId = idOnly
	| vDollarId = DOLLARID // reference to constant
	;

valueMap:
	valueMapEntry ( COMMA  valueMapEntry )*;

valueMapEntry
	: entrykey=expressionUnit  COLON  entryvalue=expressionUnit ;


statement
    : (
        letStatement
      | varStatement
      | assignmentStatement
      | expressionStatement
      | returnStatement
      | tripleQuestionmarkStatement
     )
    ;

expressionStatement
    : docstrings expressionUnit
    ;

letStatement
    : docstrings  LET  localAssignmentDeclaration
    ;

varStatement
    : docstrings  VAR  name=idOnly ( COLON  dtype=fieldType )? ( EQUAL  expr=expressionUnit )?
    ;

assignmentStatement
    :
      docstrings path=idOrQid  EQUAL  expr=expressionUnit
    ;

localAssignmentDeclaration
    : name=idOnly ( COLON  dtype=fieldType )?  EQUAL  expr=expressionUnit
    ;

tripleQuestionmarkStatement
    : docstrings  TRIPLE_QUESTIONMARK 
    ;

returnStatement
    : docstrings  RETURN  expressionUnit?
    ;

expressionSequence
    : expressionUnit ( COMMA  expressionUnit )* ( COMMA )?
    ;

namedExpressionSequence
    : namedExpression ( COMMA  namedExpression )* ( COMMA )?
    ;

namedExpression
	: ( name=idOnly  EQUAL  )? expr=expressionUnit ( COMMA )?
	;

decisionExpression
    : decision=decisionUnitExpression
    |  LEFT_BRACKET  decision=decisionUnitExpression ( COMMA  decision=decisionUnitExpression )*  RIGHT_BRACKET 
    ;

decisionUnitExpression
    : literal                                                                                               # DecisionLiteralExpression
    |  STAR                                                                                                 # DecisionWildcardExpression
    | ( LEFT_BRACKET dtype=fieldType RIGHT_BRACKET )?  NONE                                                 # DecisionNoneExpression
    | (notIn= NOT_IN )?  LEFT_SQUARE_BRACKET  from=literal  TWO_DOTS  to=literal  RIGHT_SQUARE_BRACKET      # RangeExpression
    | op=( LESS_THAN  |  GREATER_THAN  |  LESS_THAN_EQUAL  |  GREATER_THAN_EQUAL ) rhs=expressionUnit       # PartialRelativeExpression
    | op=( DOUBLE_EQUAL  |  NOT_EQUAL  ) rhs=expressionUnit                                                 # PartialEqualityExpression
    ;

decisionRule
    : input = decisionExpression  EQUAL_ARROW  output = expressionUnit
    ;

functionCall
    : ( library=idOrQid   DOUBLE_COLON  )? methodName=idOnly ( LESS_THAN  methodResultType=fieldType  GREATER_THAN )?   LEFT_BRACKET  args=namedExpressionSequence?  RIGHT_BRACKET
    ;

expressionUnit
    : ( NEW  |  AT  ) udtName=idOrQid   LEFT_BRACKET  args=namedExpressionSequence?  RIGHT_BRACKET  ( WITH  with=idOnly )?    # NewExpression
    |  RAISE  raiseType=idOnly  LEFT_BRACKET  (category=idOnly  COMMA )? message=expressionUnit  RIGHT_BRACKET                # RaiseExpression
    |  PARTIAL  udtName=idOrQid   LEFT_BRACKET  args=namedExpressionSequence?  RIGHT_BRACKET                                  # FragmentExpression
    | (negate= EXCLAMATION )? functionCall ( DOT  literal )?                                                                  # MethodCallExpression
    | lhs=expressionUnit ( PIPE  funcChains = functionCall )+                                                                 # ChainedMethodCallExpression
    | lhs=expressionUnit op=( SLASH  |  STAR  |  PERCENTAGE  |  PLUS  |  MINUS ) rhs=expressionUnit                           # MathExpression
    | lhs=expressionUnit op=( LESS_THAN  |  GREATER_THAN  |  LESS_THAN_EQUAL  |  GREATER_THAN_EQUAL ) rhs=expressionUnit      # RelativeExpression
    | lhs=expressionUnit op=( DOUBLE_EQUAL  |  NOT_EQUAL  ) rhs=expressionUnit                                                # EqualityExpression
    | lhs=expressionUnit op=( OR_CONDITION   |  AND_CONDITION  |  CARET ) rhs=expressionUnit                                  # LogicalExpression
    | cond= IF   LEFT_BRACKET  ifExp=expressionUnit  RIGHT_BRACKET
                    truedoc=docstrings thenExp=expressionUnit
                    ( ELSE  falsedoc=docstrings elseExp=expressionUnit )?                                                     # IfElseExpression
    | ( LEFT_BRACKET dtype=fieldType RIGHT_BRACKET )?  NONE                                                                   # NoneExpression
    |  THIS                                                                                                                   # ThisExpression
    | (negate= EXCLAMATION )? literal                                                                                         # LiteralExpression
    | ( LEFT_BRACKET castType=fieldType RIGHT_BRACKET )?  LEFT_SQUARE_BRACKET  expressionSequence?  RIGHT_SQUARE_BRACKET      # ListExpression
    | ( LEFT_BRACKET castType=fieldType RIGHT_BRACKET )?  LEFT_BRACE  (vSet=expressionSequence|vMap=valueMap)?  RIGHT_BRACE   # BracesExpression
    | (negate= EXCLAMATION )?   LEFT_BRACKET  namedExpressionSequence  RIGHT_BRACKET                                          # ParenthesisExpression
    | lambdaParameters?  EQUAL_ARROW  lambdaBlock                                                                             # LambdaExpression

    |  LEFT_BRACKET  criteria = expressionSequence  RIGHT_BRACKET   MATCH  (hitPolicy = idOnly)?
       LEFT_BRACE
           ( ruleLine = decisionRule )+
       RIGHT_BRACE                                                                                                            # DecisionTblExpression
    ;

lambdaParameters
    : oneArg=formalParameterArg
    |  LEFT_BRACKET  argList=formalParameterList?  RIGHT_BRACKET 
    ;

formalParameterList
    : formalParameterArg ( COMMA  formalParameterArg)*
    ;

formalParameterArg
    : idOnly
    ;

typeParameters
	:  LESS_THAN  typeParam ( COMMA  typeParam )*  GREATER_THAN  ;

typeParam
	: paramName=idOnly ( COLON  derrivedFromType = fieldType )?
	;

fieldType
	:
	(
        scalarType
        | vectorType
        | enclosedType
        | lambdaType
        | metaType
        | anyType= DOLLAR_ANY
        | idOrQidWithOptTmplArgRefs
	)
	opt= QUESTIONMARK ?
	;

modifiers
	: modifier*
	;

modifier
	: FRAGMENT | INTERNAL
	;

versionMarker
	: AT  versionNo=INT
	;

idOrQidWithOptTmplArgRefs
	:  versionedIdOrQid typeArguments?
	;

versionedIdOrQid
    :  idOrQid versionMarker?
    ;

typeArguments
	:  LESS_THAN fieldType ( COMMA  fieldType  )*  GREATER_THAN  ;

enclosedType
	: encType= STREAM       LESS_THAN  ft=fieldType  GREATER_THAN
	| encType= FUTURE       LESS_THAN  ft=fieldType  GREATER_THAN
	| encType= TRY          LESS_THAN  ft=fieldType  GREATER_THAN
	| encType=KEY           LESS_THAN  ft=fieldType  GREATER_THAN
	| encType= EITHER       LESS_THAN  ft=fieldType  COMMA  right=fieldType  GREATER_THAN
	| encType= PAIR         LESS_THAN  ft=fieldType  COMMA  right=fieldType  GREATER_THAN
	| encType= TABLE        LESS_THAN  ft=fieldType  GREATER_THAN   ( LEFT_BRACKET  optargs=namedExpressionSequence?  RIGHT_BRACKET  )?
	| encType= ENCRYPTED    LESS_THAN  ft=fieldType  GREATER_THAN   ( LEFT_BRACKET  optargs=namedExpressionSequence?  RIGHT_BRACKET  )?
	| encType= COMPRESSED   LESS_THAN  ft=fieldType  GREATER_THAN   ( LEFT_BRACKET  optargs=namedExpressionSequence?  RIGHT_BRACKET  )?
	;

metaType
	: mType= DOLLAR_ENTITYNAME 
	| mType= DOLLAR_RECORDNAME 
	| mType= DOLLAR_SERVICENAME 
	| mType= DOLLAR_KEYNAME 
	| mType= DOLLAR_UNIONNAME 
	| mType= DOLLAR_ENUMNAME 
	| mType= DOLLAR_TRAITNAME 
	| mType= DOLLAR_UDTNAME 
	| mType= DOLLAR_ENTITY 
	| mType= DOLLAR_SERVICE 
    | mType= DOLLAR_RECORD 
    | mType= DOLLAR_ANNOTATION 
    | mType= DOLLAR_KEY 
    | mType= DOLLAR_UNION 
    | mType= DOLLAR_ENUM 
    | mType= DOLLAR_TRAIT 
    | mType= DOLLAR_UDT 
    | mType= DOLLAR_FIELDNAME 
	;

scalarType
	: name= STRING_TYPE    ( intRangeParams |  LEFT_BRACKET  format= (STRING | MULTILINE_STRING)  RIGHT_BRACKET  )?
	| name= SHORT       intRangeParams?
	| name= INT_TYPE         intRangeParams?
	| name= LONG        intRangeParams?
	| name= DECIMAL     ( LEFT_BRACKET  precision=(INT|STAR)  COMMA  scale=(INT|STAR) ( COMMA  doubleRangeParams )?  RIGHT_BRACKET  )?
	| name= BOOLEAN 
	| name= DATE        ( LEFT_BRACKET  stringRangeParams ( COMMA  format=STRING )?  RIGHT_BRACKET  )?
	| name= DATETIME    ( LEFT_BRACKET  stringRangeParams ( COMMA  format=STRING )?  RIGHT_BRACKET  )?
	| name= DATETIMETZ  ( LEFT_BRACKET  stringRangeParams ( COMMA  format=STRING )?  RIGHT_BRACKET  )?
	| name= TIME        ( LEFT_BRACKET  stringRangeParams ( COMMA  format=STRING )?  RIGHT_BRACKET  )?
	| name= DURATION    ( LEFT_BRACKET  stringRangeParams  RIGHT_BRACKET  )?
	| name= PERIOD      ( LEFT_BRACKET  stringRangeParams  RIGHT_BRACKET  )?
	| name= DOUBLE      ( LEFT_BRACKET  doubleRangeParams  RIGHT_BRACKET  )?
	| name= BINARY      intRangeParams?
	| name= VOID 
	| name= UUID 
	;

intRangeParams
    : LEFT_BRACKET
        (minExclusive= GREATER_THAN )? from=sizeIntParam  COMMA  (maxExclusive= LESS_THAN )? to=sizeIntParam
      RIGHT_BRACKET
    ;

stringRangeParams
    : from=rangeStringParam  COMMA  to=rangeStringParam
    ;

rangeStringParam
    :     STRING | STAR
    ;

sizeIntParam
    :     INT | STAR
    ;

doubleRangeParams
    : (minExclusive= GREATER_THAN )? from=sizeDoubleParam  COMMA  (maxExclusive= LESS_THAN )? to=sizeDoubleParam
    ;

sizeDoubleParam
    :     INT | FLOAT | STAR
    ;


lambdaType
    : FUNC  LESS_THAN  lambdaArgs  COMMA  resultType=fieldType  GREATER_THAN 
    ;

lambdaArgs
    : LEFT_BRACKET  (fieldType ( COMMA  fieldType )* )?  RIGHT_BRACKET
    | fieldType
    ;

vectorType
	: vtName=MAP      LESS_THAN  keyType=namedMapComponent  COMMA  valType=namedMapComponent  GREATER_THAN  intRangeParams?
	| vtName=LIST     LESS_THAN  ft1=fieldType  GREATER_THAN  intRangeParams?
	| vtName=SET      LESS_THAN  ft1=fieldType ( COMMA  setUniqueFields )?  GREATER_THAN  intRangeParams?
	| docAndAnnotations vtName=TUPLE  LESS_THAN  ( embeddedField ( COMMA ? embeddedField )* )?   GREATER_THAN
	| vtName=UNION    LESS_THAN  untaggedTypes  GREATER_THAN 
	| vtName=UNION    LESS_THAN  embeddedField  COMMA  embeddedField ( COMMA ? embeddedField )*  GREATER_THAN 
	| vtName=ENUM     LESS_THAN  idWithDoc ( COMMA ? idWithDoc )*  GREATER_THAN 
	;

setUniqueFields
	:  LEFT_SQUARE_BRACKET  idOnly ( COMMA  idOnly )*  RIGHT_SQUARE_BRACKET
	;

namedMapComponent
	: ( name=idOnly  COLON  )? type=fieldType
	;

embeddedField
	:
	    docstrings name=idOnly  COLON  type=fieldType
	  | name=idOnly
	;

docstrings
	: docs=DOCSTRING* ;

sameline_docstrings
	: docs=SAMELINE_DOCSTRING? ;

