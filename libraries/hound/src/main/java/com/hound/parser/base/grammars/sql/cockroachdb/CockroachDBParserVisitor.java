// Generated from C:/AIDA/aida-root/libraries/hound/src/main/resources/grammars/sql/cockroachdb/CockroachDBParser.g4 by ANTLR 4.13.2
package com.hound.parser.base.grammars.sql.cockroachdb;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link CockroachDBParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface CockroachDBParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#cockroachdb_file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCockroachdb_file(CockroachDBParser.Cockroachdb_fileContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#stmt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt_list(CockroachDBParser.Stmt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt(CockroachDBParser.StmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#stmt_without_legacy_transaction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt_without_legacy_transaction(CockroachDBParser.Stmt_without_legacy_transactionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#legacy_transaction_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLegacy_transaction_stmt(CockroachDBParser.Legacy_transaction_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#preparable_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreparable_stmt(CockroachDBParser.Preparable_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#analyze_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnalyze_stmt(CockroachDBParser.Analyze_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#analyze_target}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnalyze_target(CockroachDBParser.Analyze_targetContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#call_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCall_stmt(CockroachDBParser.Call_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#func_application}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_application(CockroachDBParser.Func_applicationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_sort_clause_no_index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_sort_clause_no_index(CockroachDBParser.Opt_sort_clause_no_indexContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#sort_clause_no_index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSort_clause_no_index(CockroachDBParser.Sort_clause_no_indexContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#sortby_no_index_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortby_no_index_list(CockroachDBParser.Sortby_no_index_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#sortby}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortby(CockroachDBParser.SortbyContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_asc_desc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_asc_desc(CockroachDBParser.Opt_asc_descContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_nulls_order}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_nulls_order(CockroachDBParser.Opt_nulls_orderContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#sortby_index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortby_index(CockroachDBParser.Sortby_indexContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#copy_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_stmt(CockroachDBParser.Copy_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_column_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_column_list(CockroachDBParser.Opt_column_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_where_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_where_clause(CockroachDBParser.Opt_where_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#where_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere_clause(CockroachDBParser.Where_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_with_copy_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_with_copy_options(CockroachDBParser.Opt_with_copy_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_with}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_with(CockroachDBParser.Opt_withContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#copy_options_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_options_list(CockroachDBParser.Copy_options_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#copy_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_options(CockroachDBParser.Copy_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#string_or_placeholder}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString_or_placeholder(CockroachDBParser.String_or_placeholderContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#copy_generic_options_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_generic_options_list(CockroachDBParser.Copy_generic_options_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#copy_generic_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_generic_options(CockroachDBParser.Copy_generic_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#copy_to_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_to_stmt(CockroachDBParser.Copy_to_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#comment_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment_stmt(CockroachDBParser.Comment_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#column_path}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_path(CockroachDBParser.Column_pathContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#prefixed_column_path}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrefixed_column_path(CockroachDBParser.Prefixed_column_pathContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#comment_text}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment_text(CockroachDBParser.Comment_textContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#execute_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecute_stmt(CockroachDBParser.Execute_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_alias_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_alias_name(CockroachDBParser.Table_alias_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#execute_param_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecute_param_clause(CockroachDBParser.Execute_param_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#deallocate_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeallocate_stmt(CockroachDBParser.Deallocate_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#discard_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDiscard_stmt(CockroachDBParser.Discard_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#do_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDo_stmt(CockroachDBParser.Do_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#do_stmt_opt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDo_stmt_opt_list(CockroachDBParser.Do_stmt_opt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#do_stmt_opt_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDo_stmt_opt_item(CockroachDBParser.Do_stmt_opt_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#non_reserved_word_or_sconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNon_reserved_word_or_sconst(CockroachDBParser.Non_reserved_word_or_sconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#grant_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_stmt(CockroachDBParser.Grant_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#privileges}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivileges(CockroachDBParser.PrivilegesContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_privilege_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_privilege_clause(CockroachDBParser.Opt_privilege_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#grant_targets}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_targets(CockroachDBParser.Grant_targetsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_pattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_pattern(CockroachDBParser.Table_patternContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_pattern_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_pattern_list(CockroachDBParser.Table_pattern_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName_list(CockroachDBParser.Name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#function_with_paramtypes_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_with_paramtypes_list(CockroachDBParser.Function_with_paramtypes_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#function_with_paramtypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_with_paramtypes(CockroachDBParser.Function_with_paramtypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#func_params}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_params(CockroachDBParser.Func_paramsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#func_params_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_params_list(CockroachDBParser.Func_params_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#routine_param}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutine_param(CockroachDBParser.Routine_paramContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#routine_param_class}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutine_param_class(CockroachDBParser.Routine_param_classContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#routine_param_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutine_param_type(CockroachDBParser.Routine_param_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#typename}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypename(CockroachDBParser.TypenameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#simple_typename}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_typename(CockroachDBParser.Simple_typenameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#general_type_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneral_type_name(CockroachDBParser.General_type_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#type_function_name_no_crdb_extra}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_function_name_no_crdb_extra(CockroachDBParser.Type_function_name_no_crdb_extraContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#type_func_name_no_crdb_extra_keyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_func_name_no_crdb_extra_keyword(CockroachDBParser.Type_func_name_no_crdb_extra_keywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#complex_type_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComplex_type_name(CockroachDBParser.Complex_type_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#const_typename}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConst_typename(CockroachDBParser.Const_typenameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#numeric}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumeric(CockroachDBParser.NumericContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_float}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_float(CockroachDBParser.Opt_floatContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_numeric_modifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_numeric_modifiers(CockroachDBParser.Opt_numeric_modifiersContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#bit_without_length}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBit_without_length(CockroachDBParser.Bit_without_lengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#bit_with_length}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBit_with_length(CockroachDBParser.Bit_with_lengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_varying}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_varying(CockroachDBParser.Opt_varyingContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#character_without_length}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacter_without_length(CockroachDBParser.Character_without_lengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#character_base}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacter_base(CockroachDBParser.Character_baseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#char_aliases}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChar_aliases(CockroachDBParser.Char_aliasesContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#character_with_length}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacter_with_length(CockroachDBParser.Character_with_lengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#const_datetime}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConst_datetime(CockroachDBParser.Const_datetimeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_timezone}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_timezone(CockroachDBParser.Opt_timezoneContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#iconst_in_parentheses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIconst_in_parentheses(CockroachDBParser.Iconst_in_parenthesesContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#const_geo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConst_geo(CockroachDBParser.Const_geoContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#geo_shape_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeo_shape_type(CockroachDBParser.Geo_shape_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#signed_iconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSigned_iconst(CockroachDBParser.Signed_iconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#only_signed_iconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOnly_signed_iconst(CockroachDBParser.Only_signed_iconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#only_signed_fconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOnly_signed_fconst(CockroachDBParser.Only_signed_fconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#signed_iconst64}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSigned_iconst64(CockroachDBParser.Signed_iconst64Context ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#const_vector}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConst_vector(CockroachDBParser.Const_vectorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#interval_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterval_type(CockroachDBParser.Interval_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#interval_qualifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterval_qualifier(CockroachDBParser.Interval_qualifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#interval_second}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterval_second(CockroachDBParser.Interval_secondContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_array_bounds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_array_bounds(CockroachDBParser.Opt_array_boundsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#param_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_name(CockroachDBParser.Param_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#type_function_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_function_name(CockroachDBParser.Type_function_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#complex_table_pattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComplex_table_pattern(CockroachDBParser.Complex_table_patternContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#target_types}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_types(CockroachDBParser.Target_typesContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#type_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_name_list(CockroachDBParser.Type_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#schema_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_name_list(CockroachDBParser.Schema_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#role_spec_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRole_spec_list(CockroachDBParser.Role_spec_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#role_spec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRole_spec(CockroachDBParser.Role_specContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_with_grant_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_with_grant_option(CockroachDBParser.Opt_with_grant_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#privilege_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilege_list(CockroachDBParser.Privilege_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#privilege}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilege(CockroachDBParser.PrivilegeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#prepare_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrepare_stmt(CockroachDBParser.Prepare_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#prep_type_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrep_type_clause(CockroachDBParser.Prep_type_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#type_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_list(CockroachDBParser.Type_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#revoke_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_stmt(CockroachDBParser.Revoke_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#savepoint_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSavepoint_stmt(CockroachDBParser.Savepoint_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#reassign_owned_by_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReassign_owned_by_stmt(CockroachDBParser.Reassign_owned_by_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_owned_by_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_owned_by_stmt(CockroachDBParser.Drop_owned_by_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_drop_behavior}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_drop_behavior(CockroachDBParser.Opt_drop_behaviorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#release_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelease_stmt(CockroachDBParser.Release_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#refresh_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefresh_stmt(CockroachDBParser.Refresh_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_concurrently}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_concurrently(CockroachDBParser.Opt_concurrentlyContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_as_of_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_as_of_clause(CockroachDBParser.Opt_as_of_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#as_of_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAs_of_clause(CockroachDBParser.As_of_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_clear_data}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_clear_data(CockroachDBParser.Opt_clear_dataContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#nonpreparable_set_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonpreparable_set_stmt(CockroachDBParser.Nonpreparable_set_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#set_transaction_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_transaction_stmt(CockroachDBParser.Set_transaction_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#transaction_mode_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransaction_mode_list(CockroachDBParser.Transaction_mode_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#transaction_mode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransaction_mode(CockroachDBParser.Transaction_modeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#transaction_ise_level}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransaction_ise_level(CockroachDBParser.Transaction_ise_levelContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#iso_level}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIso_level(CockroachDBParser.Iso_levelContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#transaction_user_priority}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransaction_user_priority(CockroachDBParser.Transaction_user_priorityContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#user_priority}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUser_priority(CockroachDBParser.User_priorityContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#transaction_read_mode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransaction_read_mode(CockroachDBParser.Transaction_read_modeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#transaction_deferrable_mode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransaction_deferrable_mode(CockroachDBParser.Transaction_deferrable_modeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#transaction_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransaction_stmt(CockroachDBParser.Transaction_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#begin_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBegin_stmt(CockroachDBParser.Begin_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#begin_transaction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBegin_transaction(CockroachDBParser.Begin_transactionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#commit_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommit_stmt(CockroachDBParser.Commit_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_transaction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_transaction(CockroachDBParser.Opt_transactionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#rollback_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRollback_stmt(CockroachDBParser.Rollback_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#abort_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAbort_stmt(CockroachDBParser.Abort_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_abort_mod}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_abort_mod(CockroachDBParser.Opt_abort_modContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#prepare_transaction_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrepare_transaction_stmt(CockroachDBParser.Prepare_transaction_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#commit_prepared_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommit_prepared_stmt(CockroachDBParser.Commit_prepared_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#rollback_prepared_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRollback_prepared_stmt(CockroachDBParser.Rollback_prepared_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#close_cursor_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClose_cursor_stmt(CockroachDBParser.Close_cursor_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#declare_cursor_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclare_cursor_stmt(CockroachDBParser.Declare_cursor_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_binary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_binary(CockroachDBParser.Opt_binaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_sensitivity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_sensitivity(CockroachDBParser.Opt_sensitivityContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_scroll}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_scroll(CockroachDBParser.Opt_scrollContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_hold}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_hold(CockroachDBParser.Opt_holdContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#for_with_lookahead_variants}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_with_lookahead_variants(CockroachDBParser.For_with_lookahead_variantsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#fetch_cursor_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_cursor_stmt(CockroachDBParser.Fetch_cursor_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#cursor_movement_specifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursor_movement_specifier(CockroachDBParser.Cursor_movement_specifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#from_or_in}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrom_or_in(CockroachDBParser.From_or_inContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#next_prior}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNext_prior(CockroachDBParser.Next_priorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_from_or_in}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_from_or_in(CockroachDBParser.Opt_from_or_inContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#forward_backward}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForward_backward(CockroachDBParser.Forward_backwardContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_forward_backward}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_forward_backward(CockroachDBParser.Opt_forward_backwardContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#move_cursor_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMove_cursor_stmt(CockroachDBParser.Move_cursor_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#unlisten_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnlisten_stmt(CockroachDBParser.Unlisten_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_commit_timestamp_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_commit_timestamp_stmt(CockroachDBParser.Show_commit_timestamp_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#legacy_begin_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLegacy_begin_stmt(CockroachDBParser.Legacy_begin_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#legacy_end_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLegacy_end_stmt(CockroachDBParser.Legacy_end_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_stmt(CockroachDBParser.Alter_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_external_connection_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_external_connection_stmt(CockroachDBParser.Alter_external_connection_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#label_spec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabel_spec(CockroachDBParser.Label_specContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_role_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_role_stmt(CockroachDBParser.Alter_role_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_in_database}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_in_database(CockroachDBParser.Opt_in_databaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#set_or_reset_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_or_reset_clause(CockroachDBParser.Set_or_reset_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_virtual_cluster_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_virtual_cluster_stmt(CockroachDBParser.Alter_virtual_cluster_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_virtual_cluster_replication_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_virtual_cluster_replication_stmt(CockroachDBParser.Alter_virtual_cluster_replication_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#virtual_cluster}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVirtual_cluster(CockroachDBParser.Virtual_clusterContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#virtual_cluster_spec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVirtual_cluster_spec(CockroachDBParser.Virtual_cluster_specContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#replication_options_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplication_options_list(CockroachDBParser.Replication_options_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#replication_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplication_options(CockroachDBParser.Replication_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#source_replication_options_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSource_replication_options_list(CockroachDBParser.Source_replication_options_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#source_replication_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSource_replication_options(CockroachDBParser.Source_replication_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_with_replication_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_with_replication_options(CockroachDBParser.Opt_with_replication_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_virtual_cluster_capability_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_virtual_cluster_capability_stmt(CockroachDBParser.Alter_virtual_cluster_capability_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_virtual_cluster_rename_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_virtual_cluster_rename_stmt(CockroachDBParser.Alter_virtual_cluster_rename_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_virtual_cluster_service_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_virtual_cluster_service_stmt(CockroachDBParser.Alter_virtual_cluster_service_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_ddl_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_ddl_stmt(CockroachDBParser.Alter_ddl_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_table_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_stmt(CockroachDBParser.Alter_table_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_onetable_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_onetable_stmt(CockroachDBParser.Alter_onetable_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_table_cmds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_cmds(CockroachDBParser.Alter_table_cmdsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_table_cmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_cmd(CockroachDBParser.Alter_table_cmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_column}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_column(CockroachDBParser.Opt_columnContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#column_table_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_table_def(CockroachDBParser.Column_table_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_constraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_constraint(CockroachDBParser.Table_constraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#constraint_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraint_elem(CockroachDBParser.Constraint_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_storing}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_storing(CockroachDBParser.Opt_storingContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#storing}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStoring(CockroachDBParser.StoringContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_partition_by_index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_partition_by_index(CockroachDBParser.Opt_partition_by_indexContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_validate_behavior}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_validate_behavior(CockroachDBParser.Opt_validate_behaviorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_column_default}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_column_default(CockroachDBParser.Alter_column_defaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_column_on_update}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_column_on_update(CockroachDBParser.Alter_column_on_updateContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_column_visible}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_column_visible(CockroachDBParser.Alter_column_visibleContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#identity_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentity_option_list(CockroachDBParser.Identity_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#identity_option_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentity_option_elem(CockroachDBParser.Identity_option_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_set_data}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_set_data(CockroachDBParser.Opt_set_dataContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_collate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_collate(CockroachDBParser.Opt_collateContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_alter_column_using}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_alter_column_using(CockroachDBParser.Opt_alter_column_usingContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#col_qual_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCol_qual_list(CockroachDBParser.Col_qual_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#col_qualification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCol_qualification(CockroachDBParser.Col_qualificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#col_qualification_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCol_qualification_elem(CockroachDBParser.Col_qualification_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_name_parens}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_name_parens(CockroachDBParser.Opt_name_parensContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#key_match}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKey_match(CockroachDBParser.Key_matchContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#reference_actions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReference_actions(CockroachDBParser.Reference_actionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#reference_on_update}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReference_on_update(CockroachDBParser.Reference_on_updateContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#reference_on_delete}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReference_on_delete(CockroachDBParser.Reference_on_deleteContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#reference_action}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReference_action(CockroachDBParser.Reference_actionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#generated_as}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenerated_as(CockroachDBParser.Generated_asContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#generated_always_as}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenerated_always_as(CockroachDBParser.Generated_always_asContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#generated_by_default_as}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenerated_by_default_as(CockroachDBParser.Generated_by_default_asContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_sequence_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_sequence_option_list(CockroachDBParser.Opt_sequence_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#sequence_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequence_option_list(CockroachDBParser.Sequence_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#sequence_option_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequence_option_elem(CockroachDBParser.Sequence_option_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#set_generated_always}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_generated_always(CockroachDBParser.Set_generated_alwaysContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#set_generated_default}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_generated_default(CockroachDBParser.Set_generated_defaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#index_params}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_params(CockroachDBParser.Index_paramsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#index_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_elem(CockroachDBParser.Index_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#index_elem_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_elem_options(CockroachDBParser.Index_elem_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_class}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_class(CockroachDBParser.Opt_classContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_hash_sharded}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_hash_sharded(CockroachDBParser.Opt_hash_shardedContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_hash_sharded_bucket_count}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_hash_sharded_bucket_count(CockroachDBParser.Opt_hash_sharded_bucket_countContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_with_storage_parameter_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_with_storage_parameter_list(CockroachDBParser.Opt_with_storage_parameter_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#audit_mode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAudit_mode(CockroachDBParser.Audit_modeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#partition_by_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_by_table(CockroachDBParser.Partition_by_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#partition_by}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_by(CockroachDBParser.Partition_byContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#partition_by_inner}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_by_inner(CockroachDBParser.Partition_by_innerContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#list_partitions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList_partitions(CockroachDBParser.List_partitionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#list_partition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList_partition(CockroachDBParser.List_partitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_partition_by}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_partition_by(CockroachDBParser.Opt_partition_byContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#range_partitions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRange_partitions(CockroachDBParser.Range_partitionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#range_partition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRange_partition(CockroachDBParser.Range_partitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#storage_parameter_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStorage_parameter_list(CockroachDBParser.Storage_parameter_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#storage_parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStorage_parameter(CockroachDBParser.Storage_parameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#storage_parameter_key_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStorage_parameter_key_list(CockroachDBParser.Storage_parameter_key_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#storage_parameter_key}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStorage_parameter_key(CockroachDBParser.Storage_parameter_keyContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_rls_mode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_rls_mode(CockroachDBParser.Table_rls_modeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_split_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_split_stmt(CockroachDBParser.Alter_split_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_unsplit_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_unsplit_stmt(CockroachDBParser.Alter_unsplit_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_scatter_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_scatter_stmt(CockroachDBParser.Alter_scatter_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_zone_table_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_zone_table_stmt(CockroachDBParser.Alter_zone_table_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_rename_table_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_rename_table_stmt(CockroachDBParser.Alter_rename_table_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_table_set_schema_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_set_schema_stmt(CockroachDBParser.Alter_table_set_schema_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_table_locality_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_locality_stmt(CockroachDBParser.Alter_table_locality_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#locality}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocality(CockroachDBParser.LocalityContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#region_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegion_name(CockroachDBParser.Region_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_table_logged_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_logged_stmt(CockroachDBParser.Alter_table_logged_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_table_owner_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_owner_stmt(CockroachDBParser.Alter_table_owner_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_index_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_index_stmt(CockroachDBParser.Alter_index_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_oneindex_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_oneindex_stmt(CockroachDBParser.Alter_oneindex_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_index_cmds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_index_cmds(CockroachDBParser.Alter_index_cmdsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_index_cmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_index_cmd(CockroachDBParser.Alter_index_cmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#partition_by_index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_by_index(CockroachDBParser.Partition_by_indexContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_split_index_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_split_index_stmt(CockroachDBParser.Alter_split_index_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_unsplit_index_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_unsplit_index_stmt(CockroachDBParser.Alter_unsplit_index_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_scatter_index_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_scatter_index_stmt(CockroachDBParser.Alter_scatter_index_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_rename_index_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_rename_index_stmt(CockroachDBParser.Alter_rename_index_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_zone_index_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_zone_index_stmt(CockroachDBParser.Alter_zone_index_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_index_visible_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_index_visible_stmt(CockroachDBParser.Alter_index_visible_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_index_visible}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_index_visible(CockroachDBParser.Alter_index_visibleContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_view_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_view_stmt(CockroachDBParser.Alter_view_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_rename_view_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_rename_view_stmt(CockroachDBParser.Alter_rename_view_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_view_set_schema_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_view_set_schema_stmt(CockroachDBParser.Alter_view_set_schema_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_view_owner_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_view_owner_stmt(CockroachDBParser.Alter_view_owner_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_sequence_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_sequence_stmt(CockroachDBParser.Alter_sequence_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_rename_sequence_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_rename_sequence_stmt(CockroachDBParser.Alter_rename_sequence_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_sequence_options_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_sequence_options_stmt(CockroachDBParser.Alter_sequence_options_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_sequence_set_schema_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_sequence_set_schema_stmt(CockroachDBParser.Alter_sequence_set_schema_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_sequence_owner_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_sequence_owner_stmt(CockroachDBParser.Alter_sequence_owner_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_database_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_stmt(CockroachDBParser.Alter_database_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_rename_database_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_rename_database_stmt(CockroachDBParser.Alter_rename_database_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_zone_database_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_zone_database_stmt(CockroachDBParser.Alter_zone_database_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_database_owner}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_owner(CockroachDBParser.Alter_database_ownerContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_database_to_schema_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_to_schema_stmt(CockroachDBParser.Alter_database_to_schema_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_database_add_region_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_add_region_stmt(CockroachDBParser.Alter_database_add_region_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_database_drop_region_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_drop_region_stmt(CockroachDBParser.Alter_database_drop_region_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_database_survival_goal_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_survival_goal_stmt(CockroachDBParser.Alter_database_survival_goal_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#survival_goal_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSurvival_goal_clause(CockroachDBParser.Survival_goal_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_equal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_equal(CockroachDBParser.Opt_equalContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_database_primary_region_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_primary_region_stmt(CockroachDBParser.Alter_database_primary_region_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#primary_region_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary_region_clause(CockroachDBParser.Primary_region_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_database_placement_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_placement_stmt(CockroachDBParser.Alter_database_placement_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#placement_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPlacement_clause(CockroachDBParser.Placement_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_database_set_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_set_stmt(CockroachDBParser.Alter_database_set_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_database_add_super_region}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_add_super_region(CockroachDBParser.Alter_database_add_super_regionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#region_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegion_name_list(CockroachDBParser.Region_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_database_alter_super_region}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_alter_super_region(CockroachDBParser.Alter_database_alter_super_regionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_database_drop_super_region}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_drop_super_region(CockroachDBParser.Alter_database_drop_super_regionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_database_set_secondary_region_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_set_secondary_region_stmt(CockroachDBParser.Alter_database_set_secondary_region_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#secondary_region_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSecondary_region_clause(CockroachDBParser.Secondary_region_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_database_drop_secondary_region}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_drop_secondary_region(CockroachDBParser.Alter_database_drop_secondary_regionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_database_set_zone_config_extension_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_set_zone_config_extension_stmt(CockroachDBParser.Alter_database_set_zone_config_extension_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_range_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_range_stmt(CockroachDBParser.Alter_range_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_zone_range_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_zone_range_stmt(CockroachDBParser.Alter_zone_range_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_range_relocate_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_range_relocate_stmt(CockroachDBParser.Alter_range_relocate_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#relocate_kw}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelocate_kw(CockroachDBParser.Relocate_kwContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#lease_or_relocate_subject_nonlease}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLease_or_relocate_subject_nonlease(CockroachDBParser.Lease_or_relocate_subject_nonleaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#relocate_subject_nonlease}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelocate_subject_nonlease(CockroachDBParser.Relocate_subject_nonleaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_partition_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_partition_stmt(CockroachDBParser.Alter_partition_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_zone_partition_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_zone_partition_stmt(CockroachDBParser.Alter_zone_partition_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#set_zone_config}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_zone_config(CockroachDBParser.Set_zone_configContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#var_set_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_set_list(CockroachDBParser.Var_set_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#var_set_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_set_item(CockroachDBParser.Var_set_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_schema_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_schema_stmt(CockroachDBParser.Alter_schema_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_type_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_type_stmt(CockroachDBParser.Alter_type_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_add_val_placement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_add_val_placement(CockroachDBParser.Opt_add_val_placementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_default_privileges_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_default_privileges_stmt(CockroachDBParser.Alter_default_privileges_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#abbreviated_grant_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAbbreviated_grant_stmt(CockroachDBParser.Abbreviated_grant_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#target_object_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_object_type(CockroachDBParser.Target_object_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#abbreviated_revoke_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAbbreviated_revoke_stmt(CockroachDBParser.Abbreviated_revoke_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_in_schemas}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_in_schemas(CockroachDBParser.Opt_in_schemasContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_changefeed_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_changefeed_stmt(CockroachDBParser.Alter_changefeed_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_changefeed_cmds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_changefeed_cmds(CockroachDBParser.Alter_changefeed_cmdsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_changefeed_cmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_changefeed_cmd(CockroachDBParser.Alter_changefeed_cmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#changefeed_table_targets}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChangefeed_table_targets(CockroachDBParser.Changefeed_table_targetsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#changefeed_table_target}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChangefeed_table_target(CockroachDBParser.Changefeed_table_targetContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_table_prefix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_table_prefix(CockroachDBParser.Opt_table_prefixContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_chagefeed_family}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_chagefeed_family(CockroachDBParser.Opt_chagefeed_familyContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_backup_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_backup_stmt(CockroachDBParser.Alter_backup_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_backup_cmds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_backup_cmds(CockroachDBParser.Alter_backup_cmdsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_backup_cmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_backup_cmd(CockroachDBParser.Alter_backup_cmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#backup_kms}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBackup_kms(CockroachDBParser.Backup_kmsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_func_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_func_stmt(CockroachDBParser.Alter_func_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_func_options_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_func_options_stmt(CockroachDBParser.Alter_func_options_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_func_opt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_func_opt_list(CockroachDBParser.Alter_func_opt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#common_routine_opt_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommon_routine_opt_item(CockroachDBParser.Common_routine_opt_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#ops_restrict}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOps_restrict(CockroachDBParser.Ops_restrictContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_func_rename_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_func_rename_stmt(CockroachDBParser.Alter_func_rename_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_func_owner_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_func_owner_stmt(CockroachDBParser.Alter_func_owner_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_func_set_schema_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_func_set_schema_stmt(CockroachDBParser.Alter_func_set_schema_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_func_dep_extension_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_func_dep_extension_stmt(CockroachDBParser.Alter_func_dep_extension_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_no}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_no(CockroachDBParser.Opt_noContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_proc_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_proc_stmt(CockroachDBParser.Alter_proc_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_proc_rename_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_proc_rename_stmt(CockroachDBParser.Alter_proc_rename_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_proc_owner_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_proc_owner_stmt(CockroachDBParser.Alter_proc_owner_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_proc_set_schema_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_proc_set_schema_stmt(CockroachDBParser.Alter_proc_set_schema_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_backup_schedule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_backup_schedule(CockroachDBParser.Alter_backup_scheduleContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_backup_schedule_cmds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_backup_schedule_cmds(CockroachDBParser.Alter_backup_schedule_cmdsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_backup_schedule_cmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_backup_schedule_cmd(CockroachDBParser.Alter_backup_schedule_cmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#cron_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCron_expr(CockroachDBParser.Cron_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_policy_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_policy_stmt(CockroachDBParser.Alter_policy_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_policy_roles}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_policy_roles(CockroachDBParser.Opt_policy_rolesContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_policy_exprs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_policy_exprs(CockroachDBParser.Opt_policy_exprsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alter_job_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_job_stmt(CockroachDBParser.Alter_job_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#backup_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBackup_stmt(CockroachDBParser.Backup_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#sconst_or_placeholder}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSconst_or_placeholder(CockroachDBParser.Sconst_or_placeholderContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_backup_targets}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_backup_targets(CockroachDBParser.Opt_backup_targetsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#backup_targets}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBackup_targets(CockroachDBParser.Backup_targetsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#string_or_placeholder_opt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString_or_placeholder_opt_list(CockroachDBParser.String_or_placeholder_opt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#string_or_placeholder_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString_or_placeholder_list(CockroachDBParser.String_or_placeholder_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_with_backup_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_with_backup_options(CockroachDBParser.Opt_with_backup_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#backup_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBackup_options(CockroachDBParser.Backup_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#backup_options_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBackup_options_list(CockroachDBParser.Backup_options_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#cancel_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCancel_stmt(CockroachDBParser.Cancel_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#cancel_jobs_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCancel_jobs_stmt(CockroachDBParser.Cancel_jobs_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#cancel_queries_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCancel_queries_stmt(CockroachDBParser.Cancel_queries_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#cancel_sessions_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCancel_sessions_stmt(CockroachDBParser.Cancel_sessions_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#cancel_all_jobs_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCancel_all_jobs_stmt(CockroachDBParser.Cancel_all_jobs_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_stmt(CockroachDBParser.Create_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_stats_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_stats_stmt(CockroachDBParser.Create_stats_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_stats_columns}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_stats_columns(CockroachDBParser.Opt_stats_columnsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_stats_target}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_stats_target(CockroachDBParser.Create_stats_targetContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_create_stats_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_create_stats_options(CockroachDBParser.Opt_create_stats_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_stats_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_stats_option_list(CockroachDBParser.Create_stats_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_stats_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_stats_option(CockroachDBParser.Create_stats_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_changefeed_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_changefeed_stmt(CockroachDBParser.Create_changefeed_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_changefeed_sink}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_changefeed_sink(CockroachDBParser.Opt_changefeed_sinkContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#changefeed_target_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChangefeed_target_expr(CockroachDBParser.Changefeed_target_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_extension_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_extension_stmt(CockroachDBParser.Create_extension_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_external_connection_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_external_connection_stmt(CockroachDBParser.Create_external_connection_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_logical_replication_stream_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_logical_replication_stream_stmt(CockroachDBParser.Create_logical_replication_stream_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#logical_replication_resources}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogical_replication_resources(CockroachDBParser.Logical_replication_resourcesContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#logical_replication_resources_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogical_replication_resources_list(CockroachDBParser.Logical_replication_resources_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_logical_replication_create_table_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_logical_replication_create_table_options(CockroachDBParser.Opt_logical_replication_create_table_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#logical_replication_create_table_options_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogical_replication_create_table_options_list(CockroachDBParser.Logical_replication_create_table_options_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#logical_replication_create_table_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogical_replication_create_table_options(CockroachDBParser.Logical_replication_create_table_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_schedule_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_schedule_stmt(CockroachDBParser.Create_schedule_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_schedule_for_changefeed_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_schedule_for_changefeed_stmt(CockroachDBParser.Create_schedule_for_changefeed_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#schedule_label_spec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchedule_label_spec(CockroachDBParser.Schedule_label_specContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#changefeed_sink}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChangefeed_sink(CockroachDBParser.Changefeed_sinkContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_with_schedule_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_with_schedule_options(CockroachDBParser.Opt_with_schedule_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_schedule_for_backup_stm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_schedule_for_backup_stm(CockroachDBParser.Create_schedule_for_backup_stmContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_full_backup_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_full_backup_clause(CockroachDBParser.Opt_full_backup_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_ddl_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_ddl_stmt(CockroachDBParser.Create_ddl_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_database_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_database_stmt(CockroachDBParser.Create_database_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_template_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_template_clause(CockroachDBParser.Opt_template_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_encoding_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_encoding_clause(CockroachDBParser.Opt_encoding_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_lc_collate_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_lc_collate_clause(CockroachDBParser.Opt_lc_collate_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_lc_ctype_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_lc_ctype_clause(CockroachDBParser.Opt_lc_ctype_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_connection_limit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_connection_limit(CockroachDBParser.Opt_connection_limitContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_primary_region_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_primary_region_clause(CockroachDBParser.Opt_primary_region_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_regions_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_regions_list(CockroachDBParser.Opt_regions_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_survival_goal_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_survival_goal_clause(CockroachDBParser.Opt_survival_goal_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_placement_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_placement_clause(CockroachDBParser.Opt_placement_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_owner_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_owner_clause(CockroachDBParser.Opt_owner_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_super_region_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_super_region_clause(CockroachDBParser.Opt_super_region_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#super_region_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSuper_region_clause(CockroachDBParser.Super_region_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_secondary_region_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_secondary_region_clause(CockroachDBParser.Opt_secondary_region_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_index_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_index_stmt(CockroachDBParser.Create_index_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_index_access_method}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_index_access_method(CockroachDBParser.Opt_index_access_methodContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_schema_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_schema_stmt(CockroachDBParser.Create_schema_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_schema_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_schema_name(CockroachDBParser.Opt_schema_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_table_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_table_stmt(CockroachDBParser.Create_table_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_table_with}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_table_with(CockroachDBParser.Opt_table_withContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_create_table_on_commit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_create_table_on_commit(CockroachDBParser.Opt_create_table_on_commitContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_persistence_temp_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_persistence_temp_table(CockroachDBParser.Opt_persistence_temp_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_table_elem_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_table_elem_list(CockroachDBParser.Opt_table_elem_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_elem_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_elem_list(CockroachDBParser.Table_elem_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_elem(CockroachDBParser.Table_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#index_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_def(CockroachDBParser.Index_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_index_visible}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_index_visible(CockroachDBParser.Opt_index_visibleContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#family_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFamily_def(CockroachDBParser.Family_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#like_table_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLike_table_option_list(CockroachDBParser.Like_table_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#like_table_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLike_table_option(CockroachDBParser.Like_table_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_table_as_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_table_as_stmt(CockroachDBParser.Create_table_as_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_as_opt_col_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_as_opt_col_list(CockroachDBParser.Create_as_opt_col_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_as_table_defs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_as_table_defs(CockroachDBParser.Create_as_table_defsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_as_col_qual_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_as_col_qual_list(CockroachDBParser.Create_as_col_qual_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_as_col_qualification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_as_col_qualification(CockroachDBParser.Create_as_col_qualificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_as_col_qualification_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_as_col_qualification_elem(CockroachDBParser.Create_as_col_qualification_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_as_constraint_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_as_constraint_def(CockroachDBParser.Create_as_constraint_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_as_constraint_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_as_constraint_elem(CockroachDBParser.Create_as_constraint_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_as_params}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_as_params(CockroachDBParser.Create_as_paramsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_as_param}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_as_param(CockroachDBParser.Create_as_paramContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_type_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_type_stmt(CockroachDBParser.Create_type_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_enum_val_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_enum_val_list(CockroachDBParser.Opt_enum_val_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#enum_val_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnum_val_list(CockroachDBParser.Enum_val_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_composite_type_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_composite_type_list(CockroachDBParser.Opt_composite_type_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#composite_type_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComposite_type_list(CockroachDBParser.Composite_type_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_view_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_view_stmt(CockroachDBParser.Create_view_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_with_data}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_with_data(CockroachDBParser.Opt_with_dataContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_sequence_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_sequence_stmt(CockroachDBParser.Create_sequence_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_temp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_temp(CockroachDBParser.Opt_tempContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_func_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_func_stmt(CockroachDBParser.Create_func_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_return_set}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_return_set(CockroachDBParser.Opt_return_setContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#routine_return_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutine_return_type(CockroachDBParser.Routine_return_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_func_column_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_func_column_list(CockroachDBParser.Table_func_column_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_func_column}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_func_column(CockroachDBParser.Table_func_columnContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_proc_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_proc_stmt(CockroachDBParser.Create_proc_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_routine_param_with_defaults_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_routine_param_with_defaults_list(CockroachDBParser.Opt_routine_param_with_defaults_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#routine_param_with_defaults_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutine_param_with_defaults_list(CockroachDBParser.Routine_param_with_defaults_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#routine_param_with_default}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutine_param_with_default(CockroachDBParser.Routine_param_with_defaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_create_routine_opt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_create_routine_opt_list(CockroachDBParser.Opt_create_routine_opt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#routine_return_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutine_return_stmt(CockroachDBParser.Routine_return_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#routine_body_stmt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutine_body_stmt_list(CockroachDBParser.Routine_body_stmt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#routine_body_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutine_body_stmt(CockroachDBParser.Routine_body_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_routine_body}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_routine_body(CockroachDBParser.Opt_routine_bodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#or_replace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr_replace(CockroachDBParser.Or_replaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#routine_create_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoutine_create_name(CockroachDBParser.Routine_create_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_trigger_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_trigger_stmt(CockroachDBParser.Create_trigger_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#trigger_action_time}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrigger_action_time(CockroachDBParser.Trigger_action_timeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#trigger_event_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrigger_event_list(CockroachDBParser.Trigger_event_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#trigger_event}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrigger_event(CockroachDBParser.Trigger_eventContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_trigger_transition_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_trigger_transition_list(CockroachDBParser.Opt_trigger_transition_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#trigger_transition_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrigger_transition_list(CockroachDBParser.Trigger_transition_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#trigger_transition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrigger_transition(CockroachDBParser.Trigger_transitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#transition_is_new}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransition_is_new(CockroachDBParser.Transition_is_newContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#transition_is_row}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransition_is_row(CockroachDBParser.Transition_is_rowContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#trigger_for_each}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrigger_for_each(CockroachDBParser.Trigger_for_eachContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#trigger_for_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrigger_for_type(CockroachDBParser.Trigger_for_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#trigger_when}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrigger_when(CockroachDBParser.Trigger_whenContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#function_or_procedure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_or_procedure(CockroachDBParser.Function_or_procedureContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#trigger_func_args}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrigger_func_args(CockroachDBParser.Trigger_func_argsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#trigger_func_arg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrigger_func_arg(CockroachDBParser.Trigger_func_argContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_policy_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_policy_stmt(CockroachDBParser.Create_policy_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_policy_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_policy_type(CockroachDBParser.Opt_policy_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_policy_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_policy_command(CockroachDBParser.Opt_policy_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#create_role_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_role_stmt(CockroachDBParser.Create_role_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#if_not_exists}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_not_exists(CockroachDBParser.If_not_existsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_role_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_role_options(CockroachDBParser.Opt_role_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#role_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRole_options(CockroachDBParser.Role_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#role_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRole_option(CockroachDBParser.Role_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#password_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPassword_clause(CockroachDBParser.Password_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#valid_until_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValid_until_clause(CockroachDBParser.Valid_until_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#provisionsrc_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProvisionsrc_clause(CockroachDBParser.Provisionsrc_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#check_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheck_stmt(CockroachDBParser.Check_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#check_external_connection_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheck_external_connection_stmt(CockroachDBParser.Check_external_connection_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_with_check_external_connection_options_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_with_check_external_connection_options_list(CockroachDBParser.Opt_with_check_external_connection_options_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#check_external_connection_options_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheck_external_connection_options_list(CockroachDBParser.Check_external_connection_options_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#check_external_connection_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheck_external_connection_options(CockroachDBParser.Check_external_connection_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#delete_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete_stmt(CockroachDBParser.Delete_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_with_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_with_clause(CockroachDBParser.Opt_with_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#with_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_clause(CockroachDBParser.With_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_batch_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_batch_clause(CockroachDBParser.Opt_batch_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#batch_param_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBatch_param_list(CockroachDBParser.Batch_param_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#batch_param}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBatch_param(CockroachDBParser.Batch_paramContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_using_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_using_clause(CockroachDBParser.Opt_using_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#cte_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCte_list(CockroachDBParser.Cte_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#common_table_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommon_table_expr(CockroachDBParser.Common_table_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_col_def_list_no_types}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_col_def_list_no_types(CockroachDBParser.Opt_col_def_list_no_typesContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#col_def_list_no_types}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCol_def_list_no_types(CockroachDBParser.Col_def_list_no_typesContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#materialized_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMaterialized_clause(CockroachDBParser.Materialized_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_stmt(CockroachDBParser.Drop_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#explain_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplain_stmt(CockroachDBParser.Explain_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#explain_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplain_option_list(CockroachDBParser.Explain_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#explain_option_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplain_option_name(CockroachDBParser.Explain_option_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#explainable_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplainable_stmt(CockroachDBParser.Explainable_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#import_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImport_stmt(CockroachDBParser.Import_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#insert_column_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_column_list(CockroachDBParser.Insert_column_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#insert_column_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_column_item(CockroachDBParser.Insert_column_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#import_format}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImport_format(CockroachDBParser.Import_formatContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_with_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_with_options(CockroachDBParser.Opt_with_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#kv_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKv_option_list(CockroachDBParser.Kv_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#kv_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKv_option(CockroachDBParser.Kv_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#insert_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_stmt(CockroachDBParser.Insert_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#insert_target}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_target(CockroachDBParser.Insert_targetContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_name_opt_idx}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_name_opt_idx(CockroachDBParser.Table_name_opt_idxContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_only}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_only(CockroachDBParser.Opt_onlyContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_index_flags}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_index_flags(CockroachDBParser.Opt_index_flagsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#index_flags_param_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_flags_param_list(CockroachDBParser.Index_flags_param_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#index_flags_param}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_flags_param(CockroachDBParser.Index_flags_paramContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_descendant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_descendant(CockroachDBParser.Opt_descendantContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#insert_rest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_rest(CockroachDBParser.Insert_restContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#on_conflict}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOn_conflict(CockroachDBParser.On_conflictContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#set_clause_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_clause_list(CockroachDBParser.Set_clause_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#set_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_clause(CockroachDBParser.Set_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#single_set_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingle_set_clause(CockroachDBParser.Single_set_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#multiple_set_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiple_set_clause(CockroachDBParser.Multiple_set_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#returning_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturning_clause(CockroachDBParser.Returning_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#target_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_list(CockroachDBParser.Target_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#target_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_elem(CockroachDBParser.Target_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#target_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_name(CockroachDBParser.Target_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#bare_col_label}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBare_col_label(CockroachDBParser.Bare_col_labelContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#bare_label_keywords}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBare_label_keywords(CockroachDBParser.Bare_label_keywordsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#inspect_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInspect_stmt(CockroachDBParser.Inspect_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#inspect_table_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInspect_table_stmt(CockroachDBParser.Inspect_table_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_inspect_options_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_inspect_options_clause(CockroachDBParser.Opt_inspect_options_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#inspect_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInspect_option_list(CockroachDBParser.Inspect_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#inspect_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInspect_option(CockroachDBParser.Inspect_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#inspect_database_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInspect_database_stmt(CockroachDBParser.Inspect_database_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#pause_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPause_stmt(CockroachDBParser.Pause_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#pause_jobs_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPause_jobs_stmt(CockroachDBParser.Pause_jobs_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#with_reason}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_reason(CockroachDBParser.With_reasonContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#pause_schedules_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPause_schedules_stmt(CockroachDBParser.Pause_schedules_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#pause_all_jobs_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPause_all_jobs_stmt(CockroachDBParser.Pause_all_jobs_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#reset_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReset_stmt(CockroachDBParser.Reset_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#reset_session_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReset_session_stmt(CockroachDBParser.Reset_session_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#session_var}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSession_var(CockroachDBParser.Session_varContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#session_var_parts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSession_var_parts(CockroachDBParser.Session_var_partsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#reset_csetting_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReset_csetting_stmt(CockroachDBParser.Reset_csetting_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#var_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_name(CockroachDBParser.Var_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#attrs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttrs(CockroachDBParser.AttrsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#restore_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRestore_stmt(CockroachDBParser.Restore_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_with_restore_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_with_restore_options(CockroachDBParser.Opt_with_restore_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#restore_options_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRestore_options_list(CockroachDBParser.Restore_options_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#restore_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRestore_options(CockroachDBParser.Restore_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#resume_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResume_stmt(CockroachDBParser.Resume_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#resume_jobs_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResume_jobs_stmt(CockroachDBParser.Resume_jobs_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#for_schedules_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_schedules_clause(CockroachDBParser.For_schedules_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#resume_schedules_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResume_schedules_stmt(CockroachDBParser.Resume_schedules_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#resume_all_jobs_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResume_all_jobs_stmt(CockroachDBParser.Resume_all_jobs_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#export_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExport_stmt(CockroachDBParser.Export_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#scrub_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScrub_stmt(CockroachDBParser.Scrub_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#scrub_table_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScrub_table_stmt(CockroachDBParser.Scrub_table_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_scrub_options_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_scrub_options_clause(CockroachDBParser.Opt_scrub_options_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#scrub_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScrub_option_list(CockroachDBParser.Scrub_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#scrub_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScrub_option(CockroachDBParser.Scrub_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#scrub_database_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScrub_database_stmt(CockroachDBParser.Scrub_database_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_ddl_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_ddl_stmt(CockroachDBParser.Drop_ddl_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_role_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_role_stmt(CockroachDBParser.Drop_role_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#if_exists}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_exists(CockroachDBParser.If_existsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#role_or_group_or_user}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRole_or_group_or_user(CockroachDBParser.Role_or_group_or_userContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_schedule_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_schedule_stmt(CockroachDBParser.Drop_schedule_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_external_connection_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_external_connection_stmt(CockroachDBParser.Drop_external_connection_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_database_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_database_stmt(CockroachDBParser.Drop_database_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_index_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_index_stmt(CockroachDBParser.Drop_index_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_index_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_index_name_list(CockroachDBParser.Table_index_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_table_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_table_stmt(CockroachDBParser.Drop_table_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_name_list(CockroachDBParser.Table_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#db_object_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_object_name_list(CockroachDBParser.Db_object_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_view_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_view_stmt(CockroachDBParser.Drop_view_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#view_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitView_name_list(CockroachDBParser.View_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_sequence_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_sequence_stmt(CockroachDBParser.Drop_sequence_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#sequence_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequence_name_list(CockroachDBParser.Sequence_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_schema_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_schema_stmt(CockroachDBParser.Drop_schema_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_type_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_type_stmt(CockroachDBParser.Drop_type_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_func_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_func_stmt(CockroachDBParser.Drop_func_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_proc_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_proc_stmt(CockroachDBParser.Drop_proc_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_trigger_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_trigger_stmt(CockroachDBParser.Drop_trigger_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#drop_policy_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_policy_stmt(CockroachDBParser.Drop_policy_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#select_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_stmt(CockroachDBParser.Select_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#select_no_parens}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_no_parens(CockroachDBParser.Select_no_parensContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#simple_select}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_select(CockroachDBParser.Simple_selectContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#simple_select_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_select_clause(CockroachDBParser.Simple_select_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_all_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_all_clause(CockroachDBParser.Opt_all_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#distinct_on_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistinct_on_clause(CockroachDBParser.Distinct_on_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#from_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrom_clause(CockroachDBParser.From_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#from_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrom_list(CockroachDBParser.From_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#group_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_clause(CockroachDBParser.Group_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#group_by_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_by_list(CockroachDBParser.Group_by_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#group_by_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_by_item(CockroachDBParser.Group_by_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#having_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHaving_clause(CockroachDBParser.Having_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#window_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_clause(CockroachDBParser.Window_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#window_definition_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_definition_list(CockroachDBParser.Window_definition_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#window_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_definition(CockroachDBParser.Window_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#window_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_name(CockroachDBParser.Window_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#window_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_specification(CockroachDBParser.Window_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_existing_window_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_existing_window_name(CockroachDBParser.Opt_existing_window_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_partition_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_partition_clause(CockroachDBParser.Opt_partition_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_frame_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_frame_clause(CockroachDBParser.Opt_frame_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#frame_extent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrame_extent(CockroachDBParser.Frame_extentContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#frame_bound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrame_bound(CockroachDBParser.Frame_boundContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_frame_exclusion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_frame_exclusion(CockroachDBParser.Opt_frame_exclusionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#values_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValues_clause(CockroachDBParser.Values_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_clause(CockroachDBParser.Table_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_ref}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_ref(CockroachDBParser.Table_refContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#row_source_extension_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow_source_extension_stmt(CockroachDBParser.Row_source_extension_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#func_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_table(CockroachDBParser.Func_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#func_expr_windowless}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_expr_windowless(CockroachDBParser.Func_expr_windowlessContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#func_expr_common_subexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_expr_common_subexpr(CockroachDBParser.Func_expr_common_subexprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#cast_target}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCast_target(CockroachDBParser.Cast_targetContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#special_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecial_function(CockroachDBParser.Special_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#extract_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtract_list(CockroachDBParser.Extract_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#extract_arg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtract_arg(CockroachDBParser.Extract_argContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#overlay_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverlay_list(CockroachDBParser.Overlay_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#position_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPosition_list(CockroachDBParser.Position_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#substr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstr_list(CockroachDBParser.Substr_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#trim_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrim_list(CockroachDBParser.Trim_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#overlay_placing}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverlay_placing(CockroachDBParser.Overlay_placingContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#substr_from}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstr_from(CockroachDBParser.Substr_fromContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#substr_for}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstr_for(CockroachDBParser.Substr_forContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#rowsfrom_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowsfrom_list(CockroachDBParser.Rowsfrom_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#rowsfrom_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowsfrom_item(CockroachDBParser.Rowsfrom_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#join_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin_type(CockroachDBParser.Join_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#join_outer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin_outer(CockroachDBParser.Join_outerContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_join_hint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_join_hint(CockroachDBParser.Opt_join_hintContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#join_equal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin_equal(CockroachDBParser.Join_equalContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_ordinality}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_ordinality(CockroachDBParser.Opt_ordinalityContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_alias_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_alias_clause(CockroachDBParser.Opt_alias_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#alias_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias_clause(CockroachDBParser.Alias_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_func_alias_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_func_alias_clause(CockroachDBParser.Opt_func_alias_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#func_alias_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_alias_clause(CockroachDBParser.Func_alias_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#col_def_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCol_def_list(CockroachDBParser.Col_def_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#col_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCol_def(CockroachDBParser.Col_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#all_or_distinct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAll_or_distinct(CockroachDBParser.All_or_distinctContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#select_limit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_limit(CockroachDBParser.Select_limitContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#limit_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimit_clause(CockroachDBParser.Limit_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#first_or_next}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFirst_or_next(CockroachDBParser.First_or_nextContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#select_fetch_first_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_fetch_first_value(CockroachDBParser.Select_fetch_first_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#row_or_rows}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow_or_rows(CockroachDBParser.Row_or_rowsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#offset_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOffset_clause(CockroachDBParser.Offset_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#sort_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSort_clause(CockroachDBParser.Sort_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#sortby_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortby_list(CockroachDBParser.Sortby_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#select_with_parens}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_with_parens(CockroachDBParser.Select_with_parensContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#for_locking_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_locking_clause(CockroachDBParser.For_locking_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#for_locking_items}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_locking_items(CockroachDBParser.For_locking_itemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#for_locking_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_locking_item(CockroachDBParser.For_locking_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#for_locking_strength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_locking_strength(CockroachDBParser.For_locking_strengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_locked_rels}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_locked_rels(CockroachDBParser.Opt_locked_relsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_nowait_or_skip}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_nowait_or_skip(CockroachDBParser.Opt_nowait_or_skipContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#preparable_set_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreparable_set_stmt(CockroachDBParser.Preparable_set_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#set_session_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_session_stmt(CockroachDBParser.Set_session_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#set_rest_more}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_rest_more(CockroachDBParser.Set_rest_moreContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#set_rest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_rest(CockroachDBParser.Set_restContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#generic_set}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneric_set(CockroachDBParser.Generic_setContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#var_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_list(CockroachDBParser.Var_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#var_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_value(CockroachDBParser.Var_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#extra_var_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtra_var_value(CockroachDBParser.Extra_var_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#to_or_eq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTo_or_eq(CockroachDBParser.To_or_eqContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#set_local_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_local_stmt(CockroachDBParser.Set_local_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#set_csetting_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_csetting_stmt(CockroachDBParser.Set_csetting_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#use_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUse_stmt(CockroachDBParser.Use_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_stmt(CockroachDBParser.Show_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_backup_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_backup_stmt(CockroachDBParser.Show_backup_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_with_show_backup_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_with_show_backup_options(CockroachDBParser.Opt_with_show_backup_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_backup_options_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_backup_options_list(CockroachDBParser.Show_backup_options_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_backup_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_backup_options(CockroachDBParser.Show_backup_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_columns_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_columns_stmt(CockroachDBParser.Show_columns_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#with_comment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_comment(CockroachDBParser.With_commentContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_constraints_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_constraints_stmt(CockroachDBParser.Show_constraints_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_triggers_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_triggers_stmt(CockroachDBParser.Show_triggers_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_create_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_create_stmt(CockroachDBParser.Show_create_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_show_create_format_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_show_create_format_options(CockroachDBParser.Opt_show_create_format_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_create_schedules_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_create_schedules_stmt(CockroachDBParser.Show_create_schedules_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_create_external_connections_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_create_external_connections_stmt(CockroachDBParser.Show_create_external_connections_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_databases_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_databases_stmt(CockroachDBParser.Show_databases_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_enums_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_enums_stmt(CockroachDBParser.Show_enums_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_external_connections_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_external_connections_stmt(CockroachDBParser.Show_external_connections_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_types_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_types_stmt(CockroachDBParser.Show_types_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_functions_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_functions_stmt(CockroachDBParser.Show_functions_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_procedures_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_procedures_stmt(CockroachDBParser.Show_procedures_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_grants_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_grants_stmt(CockroachDBParser.Show_grants_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_target_roles}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_target_roles(CockroachDBParser.Opt_target_rolesContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#target_roles}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_roles(CockroachDBParser.Target_rolesContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#schema_wildcard}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_wildcard(CockroachDBParser.Schema_wildcardContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#wildcard_pattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWildcard_pattern(CockroachDBParser.Wildcard_patternContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#for_grantee_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_grantee_clause(CockroachDBParser.For_grantee_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_indexes_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_indexes_stmt(CockroachDBParser.Show_indexes_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_partitions_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_partitions_stmt(CockroachDBParser.Show_partitions_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_jobs_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_jobs_stmt(CockroachDBParser.Show_jobs_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_job_options_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_job_options_list(CockroachDBParser.Show_job_options_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_job_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_job_options(CockroachDBParser.Show_job_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_locality_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_locality_stmt(CockroachDBParser.Show_locality_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_schedules_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_schedules_stmt(CockroachDBParser.Show_schedules_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#schedule_state}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchedule_state(CockroachDBParser.Schedule_stateContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_schedule_executor_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_schedule_executor_type(CockroachDBParser.Opt_schedule_executor_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_statements_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_statements_stmt(CockroachDBParser.Show_statements_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_cluster}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_cluster(CockroachDBParser.Opt_clusterContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#statements_or_queries}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatements_or_queries(CockroachDBParser.Statements_or_queriesContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_ranges_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_ranges_stmt(CockroachDBParser.Show_ranges_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_show_ranges_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_show_ranges_options(CockroachDBParser.Opt_show_ranges_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_ranges_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_ranges_options(CockroachDBParser.Show_ranges_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_ranges_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_ranges_option(CockroachDBParser.Show_ranges_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_range_for_row_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_range_for_row_stmt(CockroachDBParser.Show_range_for_row_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_regions_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_regions_stmt(CockroachDBParser.Show_regions_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_survival_goal_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_survival_goal_stmt(CockroachDBParser.Show_survival_goal_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_roles_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_roles_stmt(CockroachDBParser.Show_roles_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_savepoint_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_savepoint_stmt(CockroachDBParser.Show_savepoint_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_schemas_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_schemas_stmt(CockroachDBParser.Show_schemas_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_sequences_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_sequences_stmt(CockroachDBParser.Show_sequences_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_session_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_session_stmt(CockroachDBParser.Show_session_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_sessions_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_sessions_stmt(CockroachDBParser.Show_sessions_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_stats_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_stats_stmt(CockroachDBParser.Show_stats_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_tables_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_tables_stmt(CockroachDBParser.Show_tables_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_trace_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_trace_stmt(CockroachDBParser.Show_trace_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_compact}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_compact(CockroachDBParser.Opt_compactContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_transactions_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_transactions_stmt(CockroachDBParser.Show_transactions_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_transfer_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_transfer_stmt(CockroachDBParser.Show_transfer_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_users_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_users_stmt(CockroachDBParser.Show_users_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_default_session_variables_for_role_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_default_session_variables_for_role_stmt(CockroachDBParser.Show_default_session_variables_for_role_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_zone_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_zone_stmt(CockroachDBParser.Show_zone_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#zone_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitZone_name(CockroachDBParser.Zone_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_partition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_partition(CockroachDBParser.Opt_partitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#partition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition(CockroachDBParser.PartitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#partition_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_name(CockroachDBParser.Partition_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_or_index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_or_index(CockroachDBParser.Table_or_indexContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_full_scans_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_full_scans_stmt(CockroachDBParser.Show_full_scans_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_default_privileges_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_default_privileges_stmt(CockroachDBParser.Show_default_privileges_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_for_roles}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_for_roles(CockroachDBParser.Opt_for_rolesContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_in_schema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_in_schema(CockroachDBParser.Opt_in_schemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#schema_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_name(CockroachDBParser.Schema_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#show_inspect_errors_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_inspect_errors_stmt(CockroachDBParser.Show_inspect_errors_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_for_table_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_for_table_clause(CockroachDBParser.Opt_for_table_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_for_job_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_for_job_clause(CockroachDBParser.Opt_for_job_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_with_details}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_with_details(CockroachDBParser.Opt_with_detailsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#truncate_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTruncate_stmt(CockroachDBParser.Truncate_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#relation_expr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation_expr_list(CockroachDBParser.Relation_expr_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#relation_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation_expr(CockroachDBParser.Relation_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#update_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate_stmt(CockroachDBParser.Update_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_expr_opt_alias_idx}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_expr_opt_alias_idx(CockroachDBParser.Table_expr_opt_alias_idxContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_from_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_from_list(CockroachDBParser.Opt_from_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#upsert_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpsert_stmt(CockroachDBParser.Upsert_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#iconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIconst(CockroachDBParser.IconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#fconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFconst(CockroachDBParser.FconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#sconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSconst(CockroachDBParser.SconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#bconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBconst(CockroachDBParser.BconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#bitconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitconst(CockroachDBParser.BitconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#expr_list_in_parens}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_list_in_parens(CockroachDBParser.Expr_list_in_parensContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#expr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_list(CockroachDBParser.Expr_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#a_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr(CockroachDBParser.A_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#subquery_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubquery_op(CockroachDBParser.Subquery_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#sub_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSub_type(CockroachDBParser.Sub_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#b_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitB_expr(CockroachDBParser.B_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#qual_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQual_op(CockroachDBParser.Qual_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#operator_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperator_op(CockroachDBParser.Operator_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#all_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAll_op(CockroachDBParser.All_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#c_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitC_expr(CockroachDBParser.C_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#d_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitD_expr(CockroachDBParser.D_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#typed_literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTyped_literal(CockroachDBParser.Typed_literalContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#func_name_no_crdb_extra}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_name_no_crdb_extra(CockroachDBParser.Func_name_no_crdb_extraContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#interval_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterval_value(CockroachDBParser.Interval_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#column_path_with_star}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_path_with_star(CockroachDBParser.Column_path_with_starContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#func_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_expr(CockroachDBParser.Func_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#array_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_expr(CockroachDBParser.Array_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#array_expr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_expr_list(CockroachDBParser.Array_expr_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#within_group_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithin_group_clause(CockroachDBParser.Within_group_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#over_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOver_clause(CockroachDBParser.Over_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#filter_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilter_clause(CockroachDBParser.Filter_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#single_sort_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingle_sort_clause(CockroachDBParser.Single_sort_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#labeled_row}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabeled_row(CockroachDBParser.Labeled_rowContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#row}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow(CockroachDBParser.RowContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#expr_tuple_unambiguous}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_tuple_unambiguous(CockroachDBParser.Expr_tuple_unambiguousContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#tuple1_unambiguous_values}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTuple1_unambiguous_values(CockroachDBParser.Tuple1_unambiguous_valuesContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#case_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_expr(CockroachDBParser.Case_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#case_arg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_arg(CockroachDBParser.Case_argContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#when_clause_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhen_clause_list(CockroachDBParser.When_clause_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#when_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhen_clause(CockroachDBParser.When_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#case_default}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_default(CockroachDBParser.Case_defaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#array_subscripts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_subscripts(CockroachDBParser.Array_subscriptsContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#array_subscript}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_subscript(CockroachDBParser.Array_subscriptContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#opt_slice_bound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_slice_bound(CockroachDBParser.Opt_slice_boundContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#in_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIn_expr(CockroachDBParser.In_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#expr_tuple1_ambiguous}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_tuple1_ambiguous(CockroachDBParser.Expr_tuple1_ambiguousContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#tuple1_ambiguous_values}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTuple1_ambiguous_values(CockroachDBParser.Tuple1_ambiguous_valuesContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#identifier_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier_(CockroachDBParser.Identifier_Context ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(CockroachDBParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#non_reserved_word}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNon_reserved_word(CockroachDBParser.Non_reserved_wordContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#unreserved_keyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnreserved_keyword(CockroachDBParser.Unreserved_keywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#col_name_keyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCol_name_keyword(CockroachDBParser.Col_name_keywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_name(CockroachDBParser.Column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#collation_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollation_name(CockroachDBParser.Collation_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#constraint_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraint_name(CockroachDBParser.Constraint_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#cursor_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursor_name(CockroachDBParser.Cursor_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#database_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatabase_name(CockroachDBParser.Database_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#func_application_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_application_name(CockroachDBParser.Func_application_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#func_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_name(CockroachDBParser.Func_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#index_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_name(CockroachDBParser.Index_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#view_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitView_name(CockroachDBParser.View_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#savepoint_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSavepoint_name(CockroachDBParser.Savepoint_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#statistics_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatistics_name(CockroachDBParser.Statistics_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#qualifiable_schema_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiable_schema_name(CockroachDBParser.Qualifiable_schema_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_index_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_index_name(CockroachDBParser.Table_index_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#standalone_index_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStandalone_index_name(CockroachDBParser.Standalone_index_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#db_object_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_object_name(CockroachDBParser.Db_object_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#simple_db_object_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_db_object_name(CockroachDBParser.Simple_db_object_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#db_object_name_component}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_object_name_component(CockroachDBParser.Db_object_name_componentContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#complex_db_object_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComplex_db_object_name(CockroachDBParser.Complex_db_object_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#db_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_name(CockroachDBParser.Db_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#family_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFamily_name(CockroachDBParser.Family_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#sequence_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequence_name(CockroachDBParser.Sequence_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_name(CockroachDBParser.Table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#type_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_name(CockroachDBParser.Type_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#unrestricted_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnrestricted_name(CockroachDBParser.Unrestricted_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#type_func_name_keyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_func_name_keyword(CockroachDBParser.Type_func_name_keywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#reserved_keyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReserved_keyword(CockroachDBParser.Reserved_keywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link CockroachDBParser#cockroachdb_extra_reserved_keyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCockroachdb_extra_reserved_keyword(CockroachDBParser.Cockroachdb_extra_reserved_keywordContext ctx);
}