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
package com.schemarise.alfa.compiler.err

import com.schemarise.alfa.compiler.types.AnnotationTargetType

sealed trait ErrorCode {
  val description: String
}

case object IncludedByTypeDoesNotIncludesThisTrait extends ErrorCode {
  override val description: String = "Type used in scope '%s' does not include '%s'"
}


case object IncludeTypeNotIncludedInIncludedByList extends ErrorCode {
  override val description: String = "Cannot use '%s' as includes unless '%s' is listed in scope of '%s'"
}

case object InternalTypeReferenced extends ErrorCode {
  override val description: String = "Internal type '%s' referenced from non-internal type '%s'"
}


case object IncludedByNeedsToBeTransitive extends ErrorCode {
  override val description: String = "Includes of trait '%s' which defines scope, requires '%s' also to define scope"
}


case object ServiceReferencesTraits extends ErrorCode {
  override val description: String = "Service '%s' references trait '%s' which does not define a scope"
}

case object ServiceReferencesMetaTypes extends ErrorCode {
  override val description: String = "Service '%s' references meta types through method arguments or return types"
}

case object DataProductUsingInternalType extends ErrorCode {
  override val description: String = "Data products cannot use internal types - '%s'"
}

case object InvalidKeyUsage extends ErrorCode {
  override val description: String = "Invalid key<> usage - %s"
}

case object DuplicateUnionType extends ErrorCode {
  override val description: String = "Duplicate union component type %s. Conflicts with %s."
}

case object ImplicitCallToGet extends ErrorCode {
  override val description: String = "Optional reference. Will automatically wrap with get()"
}

case object CyclicDeclaration extends ErrorCode {
  override val description: String = "Cyclic declaration between %s and %s - if intentional, nice try ;)"
}

case object NotPermittedAsMapKey extends ErrorCode {
  override val description: String = "The type '%s' is not permitted in a map key. %s"
}


case object IncompletePath extends ErrorCode {
  override val description: String = "Incomplete path %s"
}

case object SetCannotContainCollection extends ErrorCode {
  override val description: String = "Set cannot contain another vector type."
}

case object AvoidCharsInName extends ErrorCode {
  override val description: String = "%s in name '%s' should be avoided as it may conflict with generated code"
}

case object InternalErrorMsg extends ErrorCode {
  override val description: String = "%s"
}

case object DocTagFormatError extends ErrorCode {
  override val description: String = "%s"
}

case object NumberTooLargeForInt extends ErrorCode {
  override val description: String = "Number is too large for an 'int'. Use 'long'"
}


case object TopologicalTraversalCycles extends ErrorCode {
  override val description: String = "Cannot traverse topologically, user defined types have cycles: - %s"
}

case object FieldCycle extends ErrorCode {
  override val description: String = "Field expressions create a cyclic dependency '%s'"
}

case object MultipleDeclsFromDependencies extends ErrorCode {
  override val description: String = "Multiple definitions found for %s in %s"
}

case object NameComponentPotentialConflict extends ErrorCode {
  override val description: String = "Use of word '%s' can conflict with a keyword or %s code generation"
}

case object KeysShouldNotBeOptional extends ErrorCode {
  override val description: String = "A key (%s) having an optional field - %s, is not permitted"
}

case object CompilerSettingError extends ErrorCode {
  override val description: String = "Compiler setting error: %s"
}

case object FileIncludeError extends ErrorCode {
  override val description: String = "Included file '%s' not found. Expected to find in path '%s'"
}

case object TypeParametersMissing extends ErrorCode {
  override val description: String = "Type parameters missing for declaration %s - %s required %s specified."
}

case object TypeParametersMismatch extends ErrorCode {
  override val description: String = "Type parameters mismatch for %s - %s required %s specified."
}

case object DuplicateEntry extends ErrorCode {
  override val description: String = "Duplicate %s '%s'"
}

case object DuplicateAnnotationEntry extends ErrorCode {
  override val description: String = "Annotation %s already assigned against an ancestor trait"
}

case object IncludesCauseDuplicateEntry extends ErrorCode {
  override val description: String = "Includes cause incompatible types for field '%s' with types '%s' and '%s'."
}

case object IncludesCauseFieldHiding extends ErrorCode {
  override val description: String = "Include defines field '%s' %s times with different descriptions. It will be collapsed into 1 definition"
}

case object MissingVersionForUdt extends ErrorCode {
  override val description: String = "Version %s for %s '%s' has been declared, when version %s is missing. Versions are required to be consecutive."
}

case object MissingNonVersionForUdt extends ErrorCode {
  override val description: String = "Version %s for %s '%s' has been declared, when a unversioned declaration is missing. An unversioned definition ( latest ) is always required."
}

case object NameConflictsWithANamespace extends ErrorCode {
  override val description: String = "The user defined type '%s' conflicts with a namespace by the same name"
}

case object FragmentHasNoMatchingUdt extends ErrorCode {
  override val description: String = "Fragment has no matching user-defined type %s"
}

case object FragmentMergeMultipleExtends extends ErrorCode {
  override val description: String = "Fragment merge results in multiple extends for %s"
}

case object TemplateParamConflictsWithUdt extends ErrorCode {
  override val description: String = "Template parameter '%s' conflicts with a user defined type with the same name at %s"
}

case object ReferenceToAlfatheticDefinition extends ErrorCode {
  override val description: String = "Cannot reference synthetic declaration %s"
}

//case object InvalidKeyUsage extends ErrorCode {
//  override val description: String = "key<T> can only reference a key of an entity not '%s'"
//}

case object TabularDataTypeOnlyUDTs extends ErrorCode {
  override val description: String = "Table datatype can only be parameterized with user-defined-types"
}

case object InvalidEnclosedType extends ErrorCode {
  override val description: String = "Invalid enclosed object. %s of %s is not supported."
}

case object EntityIsSingleton extends ErrorCode {
  override val description: String = "Entity does not have a key, its a singleton - '%s'"
}

case object TupleNamesSizeMismatch extends ErrorCode {
  override val description: String = "Either all tuple entries should be named or none - '%s'"
}

//case object AnonymousUnionNamesSizeMismatch extends ErrorCode {
//  override val description: String = "Either all anonymous union entries should be named or none - '%s'"
//}

case object AnonymousUnionTypesMustBeUnique extends ErrorCode {
  override val description: String = "Anonymous union types must be unique"
}

case object ParserError extends ErrorCode {
  override val description: String = "Parsing failed - %s"
}

case object ValueParseError extends ErrorCode {
  override val description: String = "Failed to parse %s value expected in format %s. %s"
}

case object NumberRangeError extends ErrorCode {
  override val description: String = "Start of range %s is not smaller than or equal to end of range %s"
}


case object NumberWithinRangeError extends ErrorCode {
  override val description: String = "Numbers are not within range of %s and %s"
}


case object UnknownConst extends ErrorCode {
  override val description: String = "Unknown const '%s'"
}


case object UnknownType extends ErrorCode {
  override val description: String = "Unknown type '%s'"
}


case object UnknownDataproduct extends ErrorCode {
  override val description: String = "Unknown dataproduct '%s'"
}

case object TypeArgsMismatchWarning extends ErrorCode {
  override val description: String = "Matching type name '%s' found, but type arguments do not match"
}

case object IncorrectParametersToType extends ErrorCode {
  override val description: String = "Incorrect parameters to %s. %s."
}


case object NoAnnotationTargets extends ErrorCode {
  override val description: String = "No targets defined for annotation '%s'"
}

case object UnknownAnnotation extends ErrorCode {
  override val description: String = "Unknown annotation '%s'"
}

case object UnknownAnnotationTarget extends ErrorCode {
  override val description: String = s"Unknown annotation target '%s'. Supported targets are ${AnnotationTargetType.names}."
}

case object UnknownExtension extends ErrorCode {
  override val description: String = "Unknown top level declaration or extension '%s'"
}

case object AnnotationNotPermitted extends ErrorCode {
  override val description: String = "Annotation @%s cannot be set on a %s. It can be set on %s"
}

case object TemplatingError extends ErrorCode {
  override val description: String = "Templating error : '%s'"
}

case object GlobalFieldNotFound extends ErrorCode {
  override val description: String = "Global field '%s' not found"
}

case object IncludesOnlyTraits extends ErrorCode {
  override val description: String = "Only traits can be included, '%s' is a %s"
}

case object ExtendOnlySameType extends ErrorCode {
  override val description: String = "%s %s cannot extend a %s - %s"
}

case object CycleIncludesDetected extends ErrorCode {
  override val description: String = "Cycle detected in Trait included from %s   %s"
}

case object CycleExtendsDetected extends ErrorCode {
  override val description: String = "Cycle detected in extends from %s %s"
}


case object UnsupportedVersion extends ErrorCode {
  override val description: String = "Unsupported version specified - %s. ALFA Compiler in use supports up-to %s"
}

case object ExtendedEntityAlreadyDefinesKey extends ErrorCode {
  override val description: String = "The entity '%s' cannot declare a key as its ancestor ( %s ) defines a key in %s"
}

case object EntityKeyAndBodyFieldsDuplicate extends ErrorCode {
  override val description: String = "Following fields are duplicated in the key and body - %s"
}

case object ExtendSimilarDefinitionsOnly extends ErrorCode {
  override val description: String = "The %s %s cannot extend a %s"
}

case object InvalidKeyComponentType extends ErrorCode {
  override val description: String = "Invalid key component type %"
}

case object InvalidScalarArgs extends ErrorCode {
  override val description: String = "Invalid scalar arguments : %s."
}

case object InvalidPattern extends ErrorCode {
  override val description: String = "Invalid pattern format '%s'. %s."
}

case object InvalidUsageOfVoid extends ErrorCode {
  override val description: String = "Void can only be a field type of a union field or a return type of a service method"
}

case object InvalidLambdaArgs extends ErrorCode {
  override val description: String = "Expected %s lambda args"
}

case object ResultTypeMismatch extends ErrorCode {
  override val description: String = "Result with type '%s' does not match expected type '%s'"
}

case object TypeParameterCountMismatch extends ErrorCode {
  override val description: String = "Type %s expects %s type parameters, but %s arguments specified"
}

case object AlreadyDeclared extends ErrorCode {
  override val description: String = "The declaration of '%s' in %s was previously declared in %s"
}

case object IdentifierAlreadyDeclared extends ErrorCode {
  override val description: String = "The declaration of %s was previously declared in %s"
}

case object MultipleProjectConfigurationsFound extends ErrorCode {
  override val description: String = "Multiple project configuration found in directory %s."
}

case object DependencyZipNotFound extends ErrorCode {
  override val description: String = "Did not find expected dependency %s."
}

case object SingleLineDocCommentsOnSameLine extends ErrorCode {
  override val description: String = "Summary documentation starting # need to be on same line as the documented object. Eg. Name : string # Person's name"
}

case object UnexpectedExpressionType extends ErrorCode {
  override val description: String = "Expression needs to be of type %s, however it is %s."
}

case object ExpressionDoesNotResolveToType extends ErrorCode {
  override val description: String = "From '%s', No '%s' field available in type '%s'"
}

case object QualifiedExpressionWithoutOptionalChaining extends ErrorCode {
  override val description: String = "Qualified expression '%s' accesses optional type '%s'. Use optional chaining - '?.' syntax."
}

case object CannotInferType extends ErrorCode {
  override val description: String = "Cannot infer type of %s. %s"
}

case object LossOfPrecision extends ErrorCode {
  override val description: String = "Loss of precision. Expression needs to be of type %s, however value is %s."
}

case object MultipleExtends extends ErrorCode {
  override val description: String = "Multiple extends defined for '%s' via fragments"
}

case object UnsupportedNamespace extends ErrorCode {
  override val description: String = "Unsupported namespace %s"
}

case object ExpressionError extends ErrorCode {
  override val description: String = "Failed in expression. %s"
}

case object FieldRedeclared extends ErrorCode {
  override val description: String = "Field '%s' being re-declared"
}

case object TopLevelKeyNameConflictWithEntityKey extends ErrorCode {
  override val description: String = "The defined key %s conflicts with the implied key for entity %s"
}


case object FieldAlreadyAnnotated extends ErrorCode {
  override val description: String = "Field '%s' is already defined against another list of annotations"
}

case object TransformerError extends ErrorCode {
  override val description: String = "Failed in transformer. %s"
}

case object MadatoryFieldsNotSpecified extends ErrorCode {
  override val description: String = "Mandatory fields not specified - %s"
}


case object UnknownIdentifier extends ErrorCode {
  override val description: String = "Unknown identifier %s"
}

case object UnknownIdentifierSuggestions extends ErrorCode {
  override val description: String = "Unknown identifier %s. Did you mean to use %s?"
}

case object UnionExpressionMultiFieldsSet extends ErrorCode {
  override val description: String = "A union object can have only 1 field assigned"
}

case object ExpressionNotAssignableToVariable extends ErrorCode {
  override val description: String = "Expression of type %s cannot be assigned to variable typed %s"
}

case object TraitFieldsCannotBeAssignedAnObject extends ErrorCode {
  override val description: String = "Trait fields cannot be assigned a value, unless it is cast to a concrete datatype"
}

case object UnknownField extends ErrorCode {
  override val description: String = "Unknown field '%s'. %s"
}

case object ReturnTypeNeededOnExpr extends ErrorCode {
  override val description: String = "Function %s needs return type"
}

case object AssertResultError extends ErrorCode {
  override val description: String = "Assert needs to return a value of type 'string?'"
}

case object ParenthesisExprExpectedOneExpr extends ErrorCode {
  override val description: String = "Parenthesis expression encountered multiple (%s)  expressions %s"
}

case object OperandsHaveDifferentTypes extends ErrorCode {
  override val description: String = "Operands '%s' and '%s' have incompatible data-types (%s and %s), and cannot be used with '%s'"
}

case object IncompatibleArg extends ErrorCode {
  override val description: String = "Invalid argument to function '%s'. Argument at position %s should be type %s instead of %s"
}

case object UnableToInferArgType extends ErrorCode {
  override val description: String = "Unable to infer type of argument"
}

case object ModifyingImmutableField extends ErrorCode {
  override val description: String = "Method '%s' will modify immutable field '%s'"
}

case object UnionCannotHaveOptionalFields extends ErrorCode {
  override val description: String = "Union cannot have an optional field - '%s'"
}

case object KeyDefinedForKeylessEntity extends ErrorCode {
  override val description: String = "Key defined for keyless entity"
}

case object SizeNeedsToBeGreaterThan extends ErrorCode {
  override val description: String = "%s size needs to be greater than %s, instead of %s"
}

case object ArgumentTypeMismatchForOperation extends ErrorCode {
  override val description: String = "Values of '%s' and '%s' cannot be used with operator '%s'"
}

case object SizeNeedsToBeSmallerThan extends ErrorCode {
  override val description: String = "%s size needs to be smaller than %s, instead of %s"
}

case object UnknownEnumConstant extends ErrorCode {
  override val description: String = "Unknown enum constant %s. Did you mean %s?"
}

case object OnlyLocalMethodsAllowed extends ErrorCode {
  override val description: String = "Only simple local method calls supported - %s"
}

case object MethodInvalidNoOfArgs extends ErrorCode {
  override val description: String = "Incorrect number of args to %s, expected %s"
}

case object NoneNeedsType extends ErrorCode {
  override val description: String = "Cannot infer type of none. Please specify using 'none(<type name>)'. E.g. none(int)"
}

case object FunctionArgsIsNotExpectedType extends ErrorCode {
  override val description: String = "Argument %s to function %s, needs to be a %s, not %s"
}

case object LambdaArgsParametersMismatch extends ErrorCode {
  override val description: String = "Argument names to the 2 lambdas needs to match - %s and %s do not"
}

case object InvalidStringForFormat extends ErrorCode {
  override val description: String = "Invalid string '%s' for '%s' type. Refer to documentation for correct format. %s"
}

case object ExpressionNotAssignable extends ErrorCode {
  override val description: String = "Expression %s of type %s specified, when type %s is expected"
}

case object ExpressionNotAssignableUseConvertTo extends ErrorCode {
  override val description: String = "Use convertTo() to change expression type %s to expected type %s"
}

case object TransformerExists extends ErrorCode {
  override val description: String = "Duplicate transformer. One already declared at %s"
}

case object NoMatchingTypesForImport extends ErrorCode {
  override val description: String = "No types found for import %s"
}

case object DecisionTableError extends ErrorCode {
  override val description: String = "Decision table error - %s"
}

case object TestCaseNamingError extends ErrorCode {
  override val description: String = "Testcase %s : %s"
}

case object TypeNotExpectedForFunction extends ErrorCode {
  override val description: String = "Function %s does not accept a type parameter %s"
}
