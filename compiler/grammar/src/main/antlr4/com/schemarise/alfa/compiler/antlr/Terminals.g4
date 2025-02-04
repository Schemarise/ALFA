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

grammar Terminals;

DOLLAR_ANNOTATION : '$annotation';
DOLLAR_ANY : '$any';
DOLLAR_ENTITY : '$entity';
DOLLAR_ENTITYNAME : '$entityName';
DOLLAR_ENUM : '$enum';
DOLLAR_ENUMNAME : '$enumName';
DOLLAR_FIELDNAME : '$fieldName';
DOLLAR_KEY : '$key';
DOLLAR_KEYNAME : '$keyName';
DOLLAR_RECORD : '$record';
DOLLAR_RECORDNAME : '$recordName';
DOLLAR_SERVICE : '$service';
DOLLAR_SERVICENAME : '$serviceName';
DOLLAR_TRAIT : '$trait';
DOLLAR_TRAITNAME : '$traitName';
DOLLAR_UDT : '$udt';
DOLLAR_UDTNAME : '$udtName';
DOLLAR_UNION : '$union';
DOLLAR_UNIONNAME : '$unionName';

ANNOTATION : 'annotation';
ASSERT : 'assert';
ASSERTALL : 'assertAll';
BINARY : 'binary';
BOOLEAN : 'boolean';
COMPRESSED : 'compressed';
CONST : 'const';
CONSUME : 'consume';
DATAPRODUCT : 'dataproduct';
DATE : 'date';
DATETIME : 'datetime';
DATETIMETZ : 'datetimetz';
DECIMAL : 'decimal';
DOUBLE : 'double';
DURATION : 'duration';
EITHER : 'either';
ELSE : 'else';
ENCRYPTED : 'encrypted';
ENTITY : 'entity';
ENUM : 'enum';
EXTENDS : 'extends';
EXTENSION : 'extension';
FALSE : 'false';
FIELD : 'field';
FIELDS : 'fields';
FRAGMENT : 'fragment';
FUNC : 'func';
FUTURE : 'future';
IF : 'if';
IMPORT : 'import';
INCLUDE : 'include';
INCLUDES : 'includes';
INFINITY : 'Infinity';
INT_TYPE : 'int';
INTERNAL : 'internal';
KEY : 'key';
LANGUAGE_VERSION : 'language-version';
LET : 'let';
LIBRARY : 'library';
LINKAGE : 'linkage';
LIST : 'list';
LONG : 'long';
MAP : 'map';
MATCH : 'match';
METHOD : 'method';
MODEL_ID : 'model-id';
NAMESPACE : 'namespace';
NAN : 'NaN';
EXTERNAL : 'external';
NEW : 'new';
NONE : 'none';
NOT_IN : 'not in';
PAIR : 'pair';
PARTIAL : 'partial';
PERIOD : 'period';
PUBLISH : 'publish';
RAISE : 'raise';
RAISES : 'raises';
RECORD : 'record';
RETURN : 'return';
SCOPE : 'scope';
SERVICE : 'service';
SET : 'set';
SHORT : 'short';
STREAM : 'stream';
STRING_TYPE : 'string';
TABLE : 'table';
TESTCASE : 'testcase';
THIS : 'this';
TIME : 'time';
TRAIT : 'trait';
TRUE : 'true';
TRY : 'try';
TUPLE : 'tuple';
TYPEDEFS : 'typedefs';
UNION : 'union';
UUID : 'uuid';
VAR : 'var';
VOID : 'void';
WITH : 'with';

// Symbolic

EXCLAMATION : '!';
NOT_EQUAL : '!=';
LEFT_BRACE : '{';
PIPE : '|';
OR_CONDITION : '||';
RIGHT_BRACE : '}';
PERCENTAGE : '%';
AND_CONDITION : '&&';
LEFT_BRACKET : '(';
RIGHT_BRACKET : ')';
STAR : '*';
PLUS : '+';
COMMA : ',';
MINUS : '-';
NEGATIVE_INFINITY : '-Infinity';
DOT : '.';
DOT_STAR : '.*';
TWO_DOTS : '..';
SLASH : '/';
COLON : ':';
DOUBLE_COLON : '::';
LESS_THAN : '<';
LESS_THAN_EQUAL : '<=';
EQUAL : '=';
DOUBLE_EQUAL : '==';
EQUAL_ARROW : '=>';
GREATER_THAN : '>';
GREATER_THAN_EQUAL : '>=';
QUESTIONMARK : '?';
TRIPLE_QUESTIONMARK : '???';
AT : '@';

IN : 'in';
INOUT : 'inout';
OUT : 'out';

LEFT_SQUARE_BRACKET : '[';
RIGHT_SQUARE_BRACKET : ']';
CARET : '^';


idOrQid
	: idOnly
	| QID
	| ID_COMPLETION
	;

idOnly
	: id = ( ID | INCLUDES | MAP | LIST | SCOPE | FUNC | SET | TUPLE | STREAM | NAMESPACE | RECORD | ENTITY | TRAIT | KEY | CONST | INTERNAL | FRAGMENT |
	         DOLLAR_KEY | ENUM | UNION | FIELD | EXTENSION | SERVICE | LIBRARY | METHOD | ANNOTATION | PUBLISH | CONSUME | DATAPRODUCT | EXTERNAL |
	         INCLUDE | PERIOD | IN | OUT | INOUT )
	;

ID
	:     ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
	| '`' ~( '`' | '\r' | '\n' )+ '`'
	;

DOLLARID
    :
       '$' ID
    ;

QID
	: ( ID | INCLUDES | MAP | LIST | FUNC | SET | TUPLE | STREAM | NAMESPACE | RECORD | ENTITY | TRAIT | KEY | DOLLAR_KEY | ENUM | UNION | FIELD | SERVICE | METHOD | ANNOTATION | IN | OUT | INOUT | EXTERNAL )
	    '.'
	  ( ID | INCLUDES | MAP | LIST | FUNC | SET | TUPLE | STREAM | NAMESPACE | RECORD | ENTITY | TRAIT | KEY | DOLLAR_KEY | ENUM | UNION | FIELD | SERVICE | METHOD | ANNOTATION | IN | OUT | INOUT | EXTERNAL )
	  ( '.'
	  ( ID | INCLUDES | MAP | LIST | FUNC | SET | TUPLE | STREAM | NAMESPACE | RECORD | ENTITY | TRAIT | KEY | DOLLAR_KEY | ENUM | UNION | FIELD | SERVICE | METHOD | ANNOTATION | IN | OUT | INOUT | EXTERNAL )
	  )*;

ID_COMPLETION
    :
        ( ID | QID )  '.'
    ;

OPTQID
	: ( ID | INCLUDES | MAP | LIST | FUNC | SET | TUPLE | STREAM | NAMESPACE | RECORD | ENTITY | TRAIT | KEY | DOLLAR_KEY | ENUM | UNION | FIELD | SERVICE | METHOD | ANNOTATION | IN | OUT | INOUT | EXTERNAL )
	    '?.'
	  ( ID | INCLUDES | MAP | LIST | FUNC | SET | TUPLE | STREAM | NAMESPACE | RECORD | ENTITY | TRAIT | KEY | DOLLAR_KEY | ENUM | UNION | FIELD | SERVICE | METHOD | ANNOTATION | IN | OUT | INOUT | EXTERNAL )
	  ( '?.'
	  ( ID | INCLUDES | MAP | LIST | FUNC | SET | TUPLE | STREAM | NAMESPACE | RECORD | ENTITY | TRAIT | KEY | DOLLAR_KEY | ENUM | UNION | FIELD | SERVICE | METHOD | ANNOTATION | IN | OUT | INOUT | EXTERNAL )
	  )*;

INT
	: ('-')? '0'..'9'+ ('k'|'m'|'b'|'t'|'L')?;

FLOAT
	:
	  ('-')? ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
	| ('-')? '.' ('0'..'9')+ EXPONENT?
	;


COMMENT
	: ( '//' ~('\n'|'\r')* ( '\r'? '\n' | EOF ) |   '/*' .*? '*/' ) -> channel(HIDDEN) ;

DOCSTRING
	: '#' ~('#')  ~('\n'|'\r')* '\r'? '\n'
	| '/#' .*? '#/';

SAMELINE_DOCSTRING
	: '##' ~('\n'|'\r')* '\r'? '\n';

WS
	: ( ' '
        | '\t'
        | NL
        ) -> channel(HIDDEN) ;

NL
    : '\r' | '\n'
    ;

MULTILINE_STRING
	:
	'"""' .*? '"""'
	;

STRING
	: '"' ( ESC_SEQ | ~('\\'|'"') )* '"' ;

CHAR
	: '\'' ( ESC_SEQ | ~('\''|'\\') ) '\'' ;

fragment EXPONENT
	: ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment HEX_DIGIT
	: ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment ESC_SEQ
	:
	'\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')
	| UNICODE_ESC
	| OCTAL_ESC ;

fragment OCTAL_ESC
	: '\\' ('0'..'3') ('0'..'7') ('0'..'7')
	| '\\' ('0'..'7') ('0'..'7')
	| '\\' ('0'..'7') ;

fragment UNICODE_ESC
	: '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT ;