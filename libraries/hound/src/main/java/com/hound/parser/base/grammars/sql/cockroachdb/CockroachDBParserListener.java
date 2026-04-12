// Generated from C:/AIDA/aida-root/libraries/hound/src/main/resources/grammars/sql/cockroachdb/CockroachDBParser.g4 by ANTLR 4.13.2
package com.hound.parser.base.grammars.sql.cockroachdb;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CockroachDBParser}.
 */
public interface CockroachDBParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#cockroachdb_file}.
	 * @param ctx the parse tree
	 */
	void enterCockroachdb_file(CockroachDBParser.Cockroachdb_fileContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#cockroachdb_file}.
	 * @param ctx the parse tree
	 */
	void exitCockroachdb_file(CockroachDBParser.Cockroachdb_fileContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#stmt_list}.
	 * @param ctx the parse tree
	 */
	void enterStmt_list(CockroachDBParser.Stmt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#stmt_list}.
	 * @param ctx the parse tree
	 */
	void exitStmt_list(CockroachDBParser.Stmt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#stmt}.
	 * @param ctx the parse tree
	 */
	void enterStmt(CockroachDBParser.StmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#stmt}.
	 * @param ctx the parse tree
	 */
	void exitStmt(CockroachDBParser.StmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#stmt_without_legacy_transaction}.
	 * @param ctx the parse tree
	 */
	void enterStmt_without_legacy_transaction(CockroachDBParser.Stmt_without_legacy_transactionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#stmt_without_legacy_transaction}.
	 * @param ctx the parse tree
	 */
	void exitStmt_without_legacy_transaction(CockroachDBParser.Stmt_without_legacy_transactionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#legacy_transaction_stmt}.
	 * @param ctx the parse tree
	 */
	void enterLegacy_transaction_stmt(CockroachDBParser.Legacy_transaction_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#legacy_transaction_stmt}.
	 * @param ctx the parse tree
	 */
	void exitLegacy_transaction_stmt(CockroachDBParser.Legacy_transaction_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#preparable_stmt}.
	 * @param ctx the parse tree
	 */
	void enterPreparable_stmt(CockroachDBParser.Preparable_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#preparable_stmt}.
	 * @param ctx the parse tree
	 */
	void exitPreparable_stmt(CockroachDBParser.Preparable_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#analyze_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAnalyze_stmt(CockroachDBParser.Analyze_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#analyze_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAnalyze_stmt(CockroachDBParser.Analyze_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#analyze_target}.
	 * @param ctx the parse tree
	 */
	void enterAnalyze_target(CockroachDBParser.Analyze_targetContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#analyze_target}.
	 * @param ctx the parse tree
	 */
	void exitAnalyze_target(CockroachDBParser.Analyze_targetContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#call_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCall_stmt(CockroachDBParser.Call_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#call_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCall_stmt(CockroachDBParser.Call_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#func_application}.
	 * @param ctx the parse tree
	 */
	void enterFunc_application(CockroachDBParser.Func_applicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#func_application}.
	 * @param ctx the parse tree
	 */
	void exitFunc_application(CockroachDBParser.Func_applicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_sort_clause_no_index}.
	 * @param ctx the parse tree
	 */
	void enterOpt_sort_clause_no_index(CockroachDBParser.Opt_sort_clause_no_indexContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_sort_clause_no_index}.
	 * @param ctx the parse tree
	 */
	void exitOpt_sort_clause_no_index(CockroachDBParser.Opt_sort_clause_no_indexContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#sort_clause_no_index}.
	 * @param ctx the parse tree
	 */
	void enterSort_clause_no_index(CockroachDBParser.Sort_clause_no_indexContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#sort_clause_no_index}.
	 * @param ctx the parse tree
	 */
	void exitSort_clause_no_index(CockroachDBParser.Sort_clause_no_indexContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#sortby_no_index_list}.
	 * @param ctx the parse tree
	 */
	void enterSortby_no_index_list(CockroachDBParser.Sortby_no_index_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#sortby_no_index_list}.
	 * @param ctx the parse tree
	 */
	void exitSortby_no_index_list(CockroachDBParser.Sortby_no_index_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#sortby}.
	 * @param ctx the parse tree
	 */
	void enterSortby(CockroachDBParser.SortbyContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#sortby}.
	 * @param ctx the parse tree
	 */
	void exitSortby(CockroachDBParser.SortbyContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_asc_desc}.
	 * @param ctx the parse tree
	 */
	void enterOpt_asc_desc(CockroachDBParser.Opt_asc_descContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_asc_desc}.
	 * @param ctx the parse tree
	 */
	void exitOpt_asc_desc(CockroachDBParser.Opt_asc_descContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_nulls_order}.
	 * @param ctx the parse tree
	 */
	void enterOpt_nulls_order(CockroachDBParser.Opt_nulls_orderContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_nulls_order}.
	 * @param ctx the parse tree
	 */
	void exitOpt_nulls_order(CockroachDBParser.Opt_nulls_orderContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#sortby_index}.
	 * @param ctx the parse tree
	 */
	void enterSortby_index(CockroachDBParser.Sortby_indexContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#sortby_index}.
	 * @param ctx the parse tree
	 */
	void exitSortby_index(CockroachDBParser.Sortby_indexContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#copy_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCopy_stmt(CockroachDBParser.Copy_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#copy_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCopy_stmt(CockroachDBParser.Copy_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_column_list}.
	 * @param ctx the parse tree
	 */
	void enterOpt_column_list(CockroachDBParser.Opt_column_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_column_list}.
	 * @param ctx the parse tree
	 */
	void exitOpt_column_list(CockroachDBParser.Opt_column_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_where_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_where_clause(CockroachDBParser.Opt_where_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_where_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_where_clause(CockroachDBParser.Opt_where_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhere_clause(CockroachDBParser.Where_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhere_clause(CockroachDBParser.Where_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_with_copy_options}.
	 * @param ctx the parse tree
	 */
	void enterOpt_with_copy_options(CockroachDBParser.Opt_with_copy_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_with_copy_options}.
	 * @param ctx the parse tree
	 */
	void exitOpt_with_copy_options(CockroachDBParser.Opt_with_copy_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_with}.
	 * @param ctx the parse tree
	 */
	void enterOpt_with(CockroachDBParser.Opt_withContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_with}.
	 * @param ctx the parse tree
	 */
	void exitOpt_with(CockroachDBParser.Opt_withContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#copy_options_list}.
	 * @param ctx the parse tree
	 */
	void enterCopy_options_list(CockroachDBParser.Copy_options_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#copy_options_list}.
	 * @param ctx the parse tree
	 */
	void exitCopy_options_list(CockroachDBParser.Copy_options_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#copy_options}.
	 * @param ctx the parse tree
	 */
	void enterCopy_options(CockroachDBParser.Copy_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#copy_options}.
	 * @param ctx the parse tree
	 */
	void exitCopy_options(CockroachDBParser.Copy_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#string_or_placeholder}.
	 * @param ctx the parse tree
	 */
	void enterString_or_placeholder(CockroachDBParser.String_or_placeholderContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#string_or_placeholder}.
	 * @param ctx the parse tree
	 */
	void exitString_or_placeholder(CockroachDBParser.String_or_placeholderContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#copy_generic_options_list}.
	 * @param ctx the parse tree
	 */
	void enterCopy_generic_options_list(CockroachDBParser.Copy_generic_options_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#copy_generic_options_list}.
	 * @param ctx the parse tree
	 */
	void exitCopy_generic_options_list(CockroachDBParser.Copy_generic_options_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#copy_generic_options}.
	 * @param ctx the parse tree
	 */
	void enterCopy_generic_options(CockroachDBParser.Copy_generic_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#copy_generic_options}.
	 * @param ctx the parse tree
	 */
	void exitCopy_generic_options(CockroachDBParser.Copy_generic_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#copy_to_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCopy_to_stmt(CockroachDBParser.Copy_to_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#copy_to_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCopy_to_stmt(CockroachDBParser.Copy_to_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#comment_stmt}.
	 * @param ctx the parse tree
	 */
	void enterComment_stmt(CockroachDBParser.Comment_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#comment_stmt}.
	 * @param ctx the parse tree
	 */
	void exitComment_stmt(CockroachDBParser.Comment_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#column_path}.
	 * @param ctx the parse tree
	 */
	void enterColumn_path(CockroachDBParser.Column_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#column_path}.
	 * @param ctx the parse tree
	 */
	void exitColumn_path(CockroachDBParser.Column_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#prefixed_column_path}.
	 * @param ctx the parse tree
	 */
	void enterPrefixed_column_path(CockroachDBParser.Prefixed_column_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#prefixed_column_path}.
	 * @param ctx the parse tree
	 */
	void exitPrefixed_column_path(CockroachDBParser.Prefixed_column_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#comment_text}.
	 * @param ctx the parse tree
	 */
	void enterComment_text(CockroachDBParser.Comment_textContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#comment_text}.
	 * @param ctx the parse tree
	 */
	void exitComment_text(CockroachDBParser.Comment_textContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#execute_stmt}.
	 * @param ctx the parse tree
	 */
	void enterExecute_stmt(CockroachDBParser.Execute_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#execute_stmt}.
	 * @param ctx the parse tree
	 */
	void exitExecute_stmt(CockroachDBParser.Execute_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_alias_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_alias_name(CockroachDBParser.Table_alias_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_alias_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_alias_name(CockroachDBParser.Table_alias_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#execute_param_clause}.
	 * @param ctx the parse tree
	 */
	void enterExecute_param_clause(CockroachDBParser.Execute_param_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#execute_param_clause}.
	 * @param ctx the parse tree
	 */
	void exitExecute_param_clause(CockroachDBParser.Execute_param_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#deallocate_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDeallocate_stmt(CockroachDBParser.Deallocate_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#deallocate_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDeallocate_stmt(CockroachDBParser.Deallocate_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#discard_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDiscard_stmt(CockroachDBParser.Discard_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#discard_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDiscard_stmt(CockroachDBParser.Discard_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#do_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDo_stmt(CockroachDBParser.Do_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#do_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDo_stmt(CockroachDBParser.Do_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#do_stmt_opt_list}.
	 * @param ctx the parse tree
	 */
	void enterDo_stmt_opt_list(CockroachDBParser.Do_stmt_opt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#do_stmt_opt_list}.
	 * @param ctx the parse tree
	 */
	void exitDo_stmt_opt_list(CockroachDBParser.Do_stmt_opt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#do_stmt_opt_item}.
	 * @param ctx the parse tree
	 */
	void enterDo_stmt_opt_item(CockroachDBParser.Do_stmt_opt_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#do_stmt_opt_item}.
	 * @param ctx the parse tree
	 */
	void exitDo_stmt_opt_item(CockroachDBParser.Do_stmt_opt_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#non_reserved_word_or_sconst}.
	 * @param ctx the parse tree
	 */
	void enterNon_reserved_word_or_sconst(CockroachDBParser.Non_reserved_word_or_sconstContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#non_reserved_word_or_sconst}.
	 * @param ctx the parse tree
	 */
	void exitNon_reserved_word_or_sconst(CockroachDBParser.Non_reserved_word_or_sconstContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#grant_stmt}.
	 * @param ctx the parse tree
	 */
	void enterGrant_stmt(CockroachDBParser.Grant_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#grant_stmt}.
	 * @param ctx the parse tree
	 */
	void exitGrant_stmt(CockroachDBParser.Grant_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#privileges}.
	 * @param ctx the parse tree
	 */
	void enterPrivileges(CockroachDBParser.PrivilegesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#privileges}.
	 * @param ctx the parse tree
	 */
	void exitPrivileges(CockroachDBParser.PrivilegesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_privilege_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_privilege_clause(CockroachDBParser.Opt_privilege_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_privilege_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_privilege_clause(CockroachDBParser.Opt_privilege_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#grant_targets}.
	 * @param ctx the parse tree
	 */
	void enterGrant_targets(CockroachDBParser.Grant_targetsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#grant_targets}.
	 * @param ctx the parse tree
	 */
	void exitGrant_targets(CockroachDBParser.Grant_targetsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_pattern}.
	 * @param ctx the parse tree
	 */
	void enterTable_pattern(CockroachDBParser.Table_patternContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_pattern}.
	 * @param ctx the parse tree
	 */
	void exitTable_pattern(CockroachDBParser.Table_patternContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_pattern_list}.
	 * @param ctx the parse tree
	 */
	void enterTable_pattern_list(CockroachDBParser.Table_pattern_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_pattern_list}.
	 * @param ctx the parse tree
	 */
	void exitTable_pattern_list(CockroachDBParser.Table_pattern_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#name_list}.
	 * @param ctx the parse tree
	 */
	void enterName_list(CockroachDBParser.Name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#name_list}.
	 * @param ctx the parse tree
	 */
	void exitName_list(CockroachDBParser.Name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#function_with_paramtypes_list}.
	 * @param ctx the parse tree
	 */
	void enterFunction_with_paramtypes_list(CockroachDBParser.Function_with_paramtypes_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#function_with_paramtypes_list}.
	 * @param ctx the parse tree
	 */
	void exitFunction_with_paramtypes_list(CockroachDBParser.Function_with_paramtypes_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#function_with_paramtypes}.
	 * @param ctx the parse tree
	 */
	void enterFunction_with_paramtypes(CockroachDBParser.Function_with_paramtypesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#function_with_paramtypes}.
	 * @param ctx the parse tree
	 */
	void exitFunction_with_paramtypes(CockroachDBParser.Function_with_paramtypesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#func_params}.
	 * @param ctx the parse tree
	 */
	void enterFunc_params(CockroachDBParser.Func_paramsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#func_params}.
	 * @param ctx the parse tree
	 */
	void exitFunc_params(CockroachDBParser.Func_paramsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#func_params_list}.
	 * @param ctx the parse tree
	 */
	void enterFunc_params_list(CockroachDBParser.Func_params_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#func_params_list}.
	 * @param ctx the parse tree
	 */
	void exitFunc_params_list(CockroachDBParser.Func_params_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#routine_param}.
	 * @param ctx the parse tree
	 */
	void enterRoutine_param(CockroachDBParser.Routine_paramContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#routine_param}.
	 * @param ctx the parse tree
	 */
	void exitRoutine_param(CockroachDBParser.Routine_paramContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#routine_param_class}.
	 * @param ctx the parse tree
	 */
	void enterRoutine_param_class(CockroachDBParser.Routine_param_classContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#routine_param_class}.
	 * @param ctx the parse tree
	 */
	void exitRoutine_param_class(CockroachDBParser.Routine_param_classContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#routine_param_type}.
	 * @param ctx the parse tree
	 */
	void enterRoutine_param_type(CockroachDBParser.Routine_param_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#routine_param_type}.
	 * @param ctx the parse tree
	 */
	void exitRoutine_param_type(CockroachDBParser.Routine_param_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#typename}.
	 * @param ctx the parse tree
	 */
	void enterTypename(CockroachDBParser.TypenameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#typename}.
	 * @param ctx the parse tree
	 */
	void exitTypename(CockroachDBParser.TypenameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#simple_typename}.
	 * @param ctx the parse tree
	 */
	void enterSimple_typename(CockroachDBParser.Simple_typenameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#simple_typename}.
	 * @param ctx the parse tree
	 */
	void exitSimple_typename(CockroachDBParser.Simple_typenameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#general_type_name}.
	 * @param ctx the parse tree
	 */
	void enterGeneral_type_name(CockroachDBParser.General_type_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#general_type_name}.
	 * @param ctx the parse tree
	 */
	void exitGeneral_type_name(CockroachDBParser.General_type_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#type_function_name_no_crdb_extra}.
	 * @param ctx the parse tree
	 */
	void enterType_function_name_no_crdb_extra(CockroachDBParser.Type_function_name_no_crdb_extraContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#type_function_name_no_crdb_extra}.
	 * @param ctx the parse tree
	 */
	void exitType_function_name_no_crdb_extra(CockroachDBParser.Type_function_name_no_crdb_extraContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#type_func_name_no_crdb_extra_keyword}.
	 * @param ctx the parse tree
	 */
	void enterType_func_name_no_crdb_extra_keyword(CockroachDBParser.Type_func_name_no_crdb_extra_keywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#type_func_name_no_crdb_extra_keyword}.
	 * @param ctx the parse tree
	 */
	void exitType_func_name_no_crdb_extra_keyword(CockroachDBParser.Type_func_name_no_crdb_extra_keywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#complex_type_name}.
	 * @param ctx the parse tree
	 */
	void enterComplex_type_name(CockroachDBParser.Complex_type_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#complex_type_name}.
	 * @param ctx the parse tree
	 */
	void exitComplex_type_name(CockroachDBParser.Complex_type_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#const_typename}.
	 * @param ctx the parse tree
	 */
	void enterConst_typename(CockroachDBParser.Const_typenameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#const_typename}.
	 * @param ctx the parse tree
	 */
	void exitConst_typename(CockroachDBParser.Const_typenameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#numeric}.
	 * @param ctx the parse tree
	 */
	void enterNumeric(CockroachDBParser.NumericContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#numeric}.
	 * @param ctx the parse tree
	 */
	void exitNumeric(CockroachDBParser.NumericContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_float}.
	 * @param ctx the parse tree
	 */
	void enterOpt_float(CockroachDBParser.Opt_floatContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_float}.
	 * @param ctx the parse tree
	 */
	void exitOpt_float(CockroachDBParser.Opt_floatContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_numeric_modifiers}.
	 * @param ctx the parse tree
	 */
	void enterOpt_numeric_modifiers(CockroachDBParser.Opt_numeric_modifiersContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_numeric_modifiers}.
	 * @param ctx the parse tree
	 */
	void exitOpt_numeric_modifiers(CockroachDBParser.Opt_numeric_modifiersContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#bit_without_length}.
	 * @param ctx the parse tree
	 */
	void enterBit_without_length(CockroachDBParser.Bit_without_lengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#bit_without_length}.
	 * @param ctx the parse tree
	 */
	void exitBit_without_length(CockroachDBParser.Bit_without_lengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#bit_with_length}.
	 * @param ctx the parse tree
	 */
	void enterBit_with_length(CockroachDBParser.Bit_with_lengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#bit_with_length}.
	 * @param ctx the parse tree
	 */
	void exitBit_with_length(CockroachDBParser.Bit_with_lengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_varying}.
	 * @param ctx the parse tree
	 */
	void enterOpt_varying(CockroachDBParser.Opt_varyingContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_varying}.
	 * @param ctx the parse tree
	 */
	void exitOpt_varying(CockroachDBParser.Opt_varyingContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#character_without_length}.
	 * @param ctx the parse tree
	 */
	void enterCharacter_without_length(CockroachDBParser.Character_without_lengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#character_without_length}.
	 * @param ctx the parse tree
	 */
	void exitCharacter_without_length(CockroachDBParser.Character_without_lengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#character_base}.
	 * @param ctx the parse tree
	 */
	void enterCharacter_base(CockroachDBParser.Character_baseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#character_base}.
	 * @param ctx the parse tree
	 */
	void exitCharacter_base(CockroachDBParser.Character_baseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#char_aliases}.
	 * @param ctx the parse tree
	 */
	void enterChar_aliases(CockroachDBParser.Char_aliasesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#char_aliases}.
	 * @param ctx the parse tree
	 */
	void exitChar_aliases(CockroachDBParser.Char_aliasesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#character_with_length}.
	 * @param ctx the parse tree
	 */
	void enterCharacter_with_length(CockroachDBParser.Character_with_lengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#character_with_length}.
	 * @param ctx the parse tree
	 */
	void exitCharacter_with_length(CockroachDBParser.Character_with_lengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#const_datetime}.
	 * @param ctx the parse tree
	 */
	void enterConst_datetime(CockroachDBParser.Const_datetimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#const_datetime}.
	 * @param ctx the parse tree
	 */
	void exitConst_datetime(CockroachDBParser.Const_datetimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_timezone}.
	 * @param ctx the parse tree
	 */
	void enterOpt_timezone(CockroachDBParser.Opt_timezoneContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_timezone}.
	 * @param ctx the parse tree
	 */
	void exitOpt_timezone(CockroachDBParser.Opt_timezoneContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#iconst_in_parentheses}.
	 * @param ctx the parse tree
	 */
	void enterIconst_in_parentheses(CockroachDBParser.Iconst_in_parenthesesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#iconst_in_parentheses}.
	 * @param ctx the parse tree
	 */
	void exitIconst_in_parentheses(CockroachDBParser.Iconst_in_parenthesesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#const_geo}.
	 * @param ctx the parse tree
	 */
	void enterConst_geo(CockroachDBParser.Const_geoContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#const_geo}.
	 * @param ctx the parse tree
	 */
	void exitConst_geo(CockroachDBParser.Const_geoContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#geo_shape_type}.
	 * @param ctx the parse tree
	 */
	void enterGeo_shape_type(CockroachDBParser.Geo_shape_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#geo_shape_type}.
	 * @param ctx the parse tree
	 */
	void exitGeo_shape_type(CockroachDBParser.Geo_shape_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#signed_iconst}.
	 * @param ctx the parse tree
	 */
	void enterSigned_iconst(CockroachDBParser.Signed_iconstContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#signed_iconst}.
	 * @param ctx the parse tree
	 */
	void exitSigned_iconst(CockroachDBParser.Signed_iconstContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#only_signed_iconst}.
	 * @param ctx the parse tree
	 */
	void enterOnly_signed_iconst(CockroachDBParser.Only_signed_iconstContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#only_signed_iconst}.
	 * @param ctx the parse tree
	 */
	void exitOnly_signed_iconst(CockroachDBParser.Only_signed_iconstContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#only_signed_fconst}.
	 * @param ctx the parse tree
	 */
	void enterOnly_signed_fconst(CockroachDBParser.Only_signed_fconstContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#only_signed_fconst}.
	 * @param ctx the parse tree
	 */
	void exitOnly_signed_fconst(CockroachDBParser.Only_signed_fconstContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#signed_iconst64}.
	 * @param ctx the parse tree
	 */
	void enterSigned_iconst64(CockroachDBParser.Signed_iconst64Context ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#signed_iconst64}.
	 * @param ctx the parse tree
	 */
	void exitSigned_iconst64(CockroachDBParser.Signed_iconst64Context ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#const_vector}.
	 * @param ctx the parse tree
	 */
	void enterConst_vector(CockroachDBParser.Const_vectorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#const_vector}.
	 * @param ctx the parse tree
	 */
	void exitConst_vector(CockroachDBParser.Const_vectorContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#interval_type}.
	 * @param ctx the parse tree
	 */
	void enterInterval_type(CockroachDBParser.Interval_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#interval_type}.
	 * @param ctx the parse tree
	 */
	void exitInterval_type(CockroachDBParser.Interval_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#interval_qualifier}.
	 * @param ctx the parse tree
	 */
	void enterInterval_qualifier(CockroachDBParser.Interval_qualifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#interval_qualifier}.
	 * @param ctx the parse tree
	 */
	void exitInterval_qualifier(CockroachDBParser.Interval_qualifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#interval_second}.
	 * @param ctx the parse tree
	 */
	void enterInterval_second(CockroachDBParser.Interval_secondContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#interval_second}.
	 * @param ctx the parse tree
	 */
	void exitInterval_second(CockroachDBParser.Interval_secondContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_array_bounds}.
	 * @param ctx the parse tree
	 */
	void enterOpt_array_bounds(CockroachDBParser.Opt_array_boundsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_array_bounds}.
	 * @param ctx the parse tree
	 */
	void exitOpt_array_bounds(CockroachDBParser.Opt_array_boundsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#param_name}.
	 * @param ctx the parse tree
	 */
	void enterParam_name(CockroachDBParser.Param_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#param_name}.
	 * @param ctx the parse tree
	 */
	void exitParam_name(CockroachDBParser.Param_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#type_function_name}.
	 * @param ctx the parse tree
	 */
	void enterType_function_name(CockroachDBParser.Type_function_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#type_function_name}.
	 * @param ctx the parse tree
	 */
	void exitType_function_name(CockroachDBParser.Type_function_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#complex_table_pattern}.
	 * @param ctx the parse tree
	 */
	void enterComplex_table_pattern(CockroachDBParser.Complex_table_patternContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#complex_table_pattern}.
	 * @param ctx the parse tree
	 */
	void exitComplex_table_pattern(CockroachDBParser.Complex_table_patternContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#target_types}.
	 * @param ctx the parse tree
	 */
	void enterTarget_types(CockroachDBParser.Target_typesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#target_types}.
	 * @param ctx the parse tree
	 */
	void exitTarget_types(CockroachDBParser.Target_typesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#type_name_list}.
	 * @param ctx the parse tree
	 */
	void enterType_name_list(CockroachDBParser.Type_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#type_name_list}.
	 * @param ctx the parse tree
	 */
	void exitType_name_list(CockroachDBParser.Type_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#schema_name_list}.
	 * @param ctx the parse tree
	 */
	void enterSchema_name_list(CockroachDBParser.Schema_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#schema_name_list}.
	 * @param ctx the parse tree
	 */
	void exitSchema_name_list(CockroachDBParser.Schema_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#role_spec_list}.
	 * @param ctx the parse tree
	 */
	void enterRole_spec_list(CockroachDBParser.Role_spec_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#role_spec_list}.
	 * @param ctx the parse tree
	 */
	void exitRole_spec_list(CockroachDBParser.Role_spec_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#role_spec}.
	 * @param ctx the parse tree
	 */
	void enterRole_spec(CockroachDBParser.Role_specContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#role_spec}.
	 * @param ctx the parse tree
	 */
	void exitRole_spec(CockroachDBParser.Role_specContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_with_grant_option}.
	 * @param ctx the parse tree
	 */
	void enterOpt_with_grant_option(CockroachDBParser.Opt_with_grant_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_with_grant_option}.
	 * @param ctx the parse tree
	 */
	void exitOpt_with_grant_option(CockroachDBParser.Opt_with_grant_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#privilege_list}.
	 * @param ctx the parse tree
	 */
	void enterPrivilege_list(CockroachDBParser.Privilege_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#privilege_list}.
	 * @param ctx the parse tree
	 */
	void exitPrivilege_list(CockroachDBParser.Privilege_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#privilege}.
	 * @param ctx the parse tree
	 */
	void enterPrivilege(CockroachDBParser.PrivilegeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#privilege}.
	 * @param ctx the parse tree
	 */
	void exitPrivilege(CockroachDBParser.PrivilegeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#prepare_stmt}.
	 * @param ctx the parse tree
	 */
	void enterPrepare_stmt(CockroachDBParser.Prepare_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#prepare_stmt}.
	 * @param ctx the parse tree
	 */
	void exitPrepare_stmt(CockroachDBParser.Prepare_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#prep_type_clause}.
	 * @param ctx the parse tree
	 */
	void enterPrep_type_clause(CockroachDBParser.Prep_type_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#prep_type_clause}.
	 * @param ctx the parse tree
	 */
	void exitPrep_type_clause(CockroachDBParser.Prep_type_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#type_list}.
	 * @param ctx the parse tree
	 */
	void enterType_list(CockroachDBParser.Type_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#type_list}.
	 * @param ctx the parse tree
	 */
	void exitType_list(CockroachDBParser.Type_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#revoke_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_stmt(CockroachDBParser.Revoke_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#revoke_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_stmt(CockroachDBParser.Revoke_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#savepoint_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSavepoint_stmt(CockroachDBParser.Savepoint_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#savepoint_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSavepoint_stmt(CockroachDBParser.Savepoint_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#reassign_owned_by_stmt}.
	 * @param ctx the parse tree
	 */
	void enterReassign_owned_by_stmt(CockroachDBParser.Reassign_owned_by_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#reassign_owned_by_stmt}.
	 * @param ctx the parse tree
	 */
	void exitReassign_owned_by_stmt(CockroachDBParser.Reassign_owned_by_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_owned_by_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_owned_by_stmt(CockroachDBParser.Drop_owned_by_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_owned_by_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_owned_by_stmt(CockroachDBParser.Drop_owned_by_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_drop_behavior}.
	 * @param ctx the parse tree
	 */
	void enterOpt_drop_behavior(CockroachDBParser.Opt_drop_behaviorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_drop_behavior}.
	 * @param ctx the parse tree
	 */
	void exitOpt_drop_behavior(CockroachDBParser.Opt_drop_behaviorContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#release_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRelease_stmt(CockroachDBParser.Release_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#release_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRelease_stmt(CockroachDBParser.Release_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#refresh_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRefresh_stmt(CockroachDBParser.Refresh_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#refresh_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRefresh_stmt(CockroachDBParser.Refresh_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_concurrently}.
	 * @param ctx the parse tree
	 */
	void enterOpt_concurrently(CockroachDBParser.Opt_concurrentlyContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_concurrently}.
	 * @param ctx the parse tree
	 */
	void exitOpt_concurrently(CockroachDBParser.Opt_concurrentlyContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_as_of_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_as_of_clause(CockroachDBParser.Opt_as_of_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_as_of_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_as_of_clause(CockroachDBParser.Opt_as_of_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#as_of_clause}.
	 * @param ctx the parse tree
	 */
	void enterAs_of_clause(CockroachDBParser.As_of_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#as_of_clause}.
	 * @param ctx the parse tree
	 */
	void exitAs_of_clause(CockroachDBParser.As_of_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_clear_data}.
	 * @param ctx the parse tree
	 */
	void enterOpt_clear_data(CockroachDBParser.Opt_clear_dataContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_clear_data}.
	 * @param ctx the parse tree
	 */
	void exitOpt_clear_data(CockroachDBParser.Opt_clear_dataContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#nonpreparable_set_stmt}.
	 * @param ctx the parse tree
	 */
	void enterNonpreparable_set_stmt(CockroachDBParser.Nonpreparable_set_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#nonpreparable_set_stmt}.
	 * @param ctx the parse tree
	 */
	void exitNonpreparable_set_stmt(CockroachDBParser.Nonpreparable_set_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#set_transaction_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSet_transaction_stmt(CockroachDBParser.Set_transaction_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#set_transaction_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSet_transaction_stmt(CockroachDBParser.Set_transaction_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#transaction_mode_list}.
	 * @param ctx the parse tree
	 */
	void enterTransaction_mode_list(CockroachDBParser.Transaction_mode_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#transaction_mode_list}.
	 * @param ctx the parse tree
	 */
	void exitTransaction_mode_list(CockroachDBParser.Transaction_mode_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#transaction_mode}.
	 * @param ctx the parse tree
	 */
	void enterTransaction_mode(CockroachDBParser.Transaction_modeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#transaction_mode}.
	 * @param ctx the parse tree
	 */
	void exitTransaction_mode(CockroachDBParser.Transaction_modeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#transaction_ise_level}.
	 * @param ctx the parse tree
	 */
	void enterTransaction_ise_level(CockroachDBParser.Transaction_ise_levelContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#transaction_ise_level}.
	 * @param ctx the parse tree
	 */
	void exitTransaction_ise_level(CockroachDBParser.Transaction_ise_levelContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#iso_level}.
	 * @param ctx the parse tree
	 */
	void enterIso_level(CockroachDBParser.Iso_levelContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#iso_level}.
	 * @param ctx the parse tree
	 */
	void exitIso_level(CockroachDBParser.Iso_levelContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#transaction_user_priority}.
	 * @param ctx the parse tree
	 */
	void enterTransaction_user_priority(CockroachDBParser.Transaction_user_priorityContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#transaction_user_priority}.
	 * @param ctx the parse tree
	 */
	void exitTransaction_user_priority(CockroachDBParser.Transaction_user_priorityContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#user_priority}.
	 * @param ctx the parse tree
	 */
	void enterUser_priority(CockroachDBParser.User_priorityContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#user_priority}.
	 * @param ctx the parse tree
	 */
	void exitUser_priority(CockroachDBParser.User_priorityContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#transaction_read_mode}.
	 * @param ctx the parse tree
	 */
	void enterTransaction_read_mode(CockroachDBParser.Transaction_read_modeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#transaction_read_mode}.
	 * @param ctx the parse tree
	 */
	void exitTransaction_read_mode(CockroachDBParser.Transaction_read_modeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#transaction_deferrable_mode}.
	 * @param ctx the parse tree
	 */
	void enterTransaction_deferrable_mode(CockroachDBParser.Transaction_deferrable_modeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#transaction_deferrable_mode}.
	 * @param ctx the parse tree
	 */
	void exitTransaction_deferrable_mode(CockroachDBParser.Transaction_deferrable_modeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#transaction_stmt}.
	 * @param ctx the parse tree
	 */
	void enterTransaction_stmt(CockroachDBParser.Transaction_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#transaction_stmt}.
	 * @param ctx the parse tree
	 */
	void exitTransaction_stmt(CockroachDBParser.Transaction_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#begin_stmt}.
	 * @param ctx the parse tree
	 */
	void enterBegin_stmt(CockroachDBParser.Begin_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#begin_stmt}.
	 * @param ctx the parse tree
	 */
	void exitBegin_stmt(CockroachDBParser.Begin_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#begin_transaction}.
	 * @param ctx the parse tree
	 */
	void enterBegin_transaction(CockroachDBParser.Begin_transactionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#begin_transaction}.
	 * @param ctx the parse tree
	 */
	void exitBegin_transaction(CockroachDBParser.Begin_transactionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#commit_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCommit_stmt(CockroachDBParser.Commit_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#commit_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCommit_stmt(CockroachDBParser.Commit_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_transaction}.
	 * @param ctx the parse tree
	 */
	void enterOpt_transaction(CockroachDBParser.Opt_transactionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_transaction}.
	 * @param ctx the parse tree
	 */
	void exitOpt_transaction(CockroachDBParser.Opt_transactionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#rollback_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRollback_stmt(CockroachDBParser.Rollback_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#rollback_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRollback_stmt(CockroachDBParser.Rollback_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#abort_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAbort_stmt(CockroachDBParser.Abort_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#abort_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAbort_stmt(CockroachDBParser.Abort_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_abort_mod}.
	 * @param ctx the parse tree
	 */
	void enterOpt_abort_mod(CockroachDBParser.Opt_abort_modContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_abort_mod}.
	 * @param ctx the parse tree
	 */
	void exitOpt_abort_mod(CockroachDBParser.Opt_abort_modContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#prepare_transaction_stmt}.
	 * @param ctx the parse tree
	 */
	void enterPrepare_transaction_stmt(CockroachDBParser.Prepare_transaction_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#prepare_transaction_stmt}.
	 * @param ctx the parse tree
	 */
	void exitPrepare_transaction_stmt(CockroachDBParser.Prepare_transaction_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#commit_prepared_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCommit_prepared_stmt(CockroachDBParser.Commit_prepared_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#commit_prepared_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCommit_prepared_stmt(CockroachDBParser.Commit_prepared_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#rollback_prepared_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRollback_prepared_stmt(CockroachDBParser.Rollback_prepared_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#rollback_prepared_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRollback_prepared_stmt(CockroachDBParser.Rollback_prepared_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#close_cursor_stmt}.
	 * @param ctx the parse tree
	 */
	void enterClose_cursor_stmt(CockroachDBParser.Close_cursor_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#close_cursor_stmt}.
	 * @param ctx the parse tree
	 */
	void exitClose_cursor_stmt(CockroachDBParser.Close_cursor_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#declare_cursor_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDeclare_cursor_stmt(CockroachDBParser.Declare_cursor_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#declare_cursor_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDeclare_cursor_stmt(CockroachDBParser.Declare_cursor_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_binary}.
	 * @param ctx the parse tree
	 */
	void enterOpt_binary(CockroachDBParser.Opt_binaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_binary}.
	 * @param ctx the parse tree
	 */
	void exitOpt_binary(CockroachDBParser.Opt_binaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_sensitivity}.
	 * @param ctx the parse tree
	 */
	void enterOpt_sensitivity(CockroachDBParser.Opt_sensitivityContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_sensitivity}.
	 * @param ctx the parse tree
	 */
	void exitOpt_sensitivity(CockroachDBParser.Opt_sensitivityContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_scroll}.
	 * @param ctx the parse tree
	 */
	void enterOpt_scroll(CockroachDBParser.Opt_scrollContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_scroll}.
	 * @param ctx the parse tree
	 */
	void exitOpt_scroll(CockroachDBParser.Opt_scrollContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_hold}.
	 * @param ctx the parse tree
	 */
	void enterOpt_hold(CockroachDBParser.Opt_holdContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_hold}.
	 * @param ctx the parse tree
	 */
	void exitOpt_hold(CockroachDBParser.Opt_holdContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#for_with_lookahead_variants}.
	 * @param ctx the parse tree
	 */
	void enterFor_with_lookahead_variants(CockroachDBParser.For_with_lookahead_variantsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#for_with_lookahead_variants}.
	 * @param ctx the parse tree
	 */
	void exitFor_with_lookahead_variants(CockroachDBParser.For_with_lookahead_variantsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#fetch_cursor_stmt}.
	 * @param ctx the parse tree
	 */
	void enterFetch_cursor_stmt(CockroachDBParser.Fetch_cursor_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#fetch_cursor_stmt}.
	 * @param ctx the parse tree
	 */
	void exitFetch_cursor_stmt(CockroachDBParser.Fetch_cursor_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#cursor_movement_specifier}.
	 * @param ctx the parse tree
	 */
	void enterCursor_movement_specifier(CockroachDBParser.Cursor_movement_specifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#cursor_movement_specifier}.
	 * @param ctx the parse tree
	 */
	void exitCursor_movement_specifier(CockroachDBParser.Cursor_movement_specifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#from_or_in}.
	 * @param ctx the parse tree
	 */
	void enterFrom_or_in(CockroachDBParser.From_or_inContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#from_or_in}.
	 * @param ctx the parse tree
	 */
	void exitFrom_or_in(CockroachDBParser.From_or_inContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#next_prior}.
	 * @param ctx the parse tree
	 */
	void enterNext_prior(CockroachDBParser.Next_priorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#next_prior}.
	 * @param ctx the parse tree
	 */
	void exitNext_prior(CockroachDBParser.Next_priorContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_from_or_in}.
	 * @param ctx the parse tree
	 */
	void enterOpt_from_or_in(CockroachDBParser.Opt_from_or_inContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_from_or_in}.
	 * @param ctx the parse tree
	 */
	void exitOpt_from_or_in(CockroachDBParser.Opt_from_or_inContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#forward_backward}.
	 * @param ctx the parse tree
	 */
	void enterForward_backward(CockroachDBParser.Forward_backwardContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#forward_backward}.
	 * @param ctx the parse tree
	 */
	void exitForward_backward(CockroachDBParser.Forward_backwardContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_forward_backward}.
	 * @param ctx the parse tree
	 */
	void enterOpt_forward_backward(CockroachDBParser.Opt_forward_backwardContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_forward_backward}.
	 * @param ctx the parse tree
	 */
	void exitOpt_forward_backward(CockroachDBParser.Opt_forward_backwardContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#move_cursor_stmt}.
	 * @param ctx the parse tree
	 */
	void enterMove_cursor_stmt(CockroachDBParser.Move_cursor_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#move_cursor_stmt}.
	 * @param ctx the parse tree
	 */
	void exitMove_cursor_stmt(CockroachDBParser.Move_cursor_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#unlisten_stmt}.
	 * @param ctx the parse tree
	 */
	void enterUnlisten_stmt(CockroachDBParser.Unlisten_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#unlisten_stmt}.
	 * @param ctx the parse tree
	 */
	void exitUnlisten_stmt(CockroachDBParser.Unlisten_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_commit_timestamp_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_commit_timestamp_stmt(CockroachDBParser.Show_commit_timestamp_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_commit_timestamp_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_commit_timestamp_stmt(CockroachDBParser.Show_commit_timestamp_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#legacy_begin_stmt}.
	 * @param ctx the parse tree
	 */
	void enterLegacy_begin_stmt(CockroachDBParser.Legacy_begin_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#legacy_begin_stmt}.
	 * @param ctx the parse tree
	 */
	void exitLegacy_begin_stmt(CockroachDBParser.Legacy_begin_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#legacy_end_stmt}.
	 * @param ctx the parse tree
	 */
	void enterLegacy_end_stmt(CockroachDBParser.Legacy_end_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#legacy_end_stmt}.
	 * @param ctx the parse tree
	 */
	void exitLegacy_end_stmt(CockroachDBParser.Legacy_end_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_stmt(CockroachDBParser.Alter_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_stmt(CockroachDBParser.Alter_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_external_connection_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_external_connection_stmt(CockroachDBParser.Alter_external_connection_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_external_connection_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_external_connection_stmt(CockroachDBParser.Alter_external_connection_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#label_spec}.
	 * @param ctx the parse tree
	 */
	void enterLabel_spec(CockroachDBParser.Label_specContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#label_spec}.
	 * @param ctx the parse tree
	 */
	void exitLabel_spec(CockroachDBParser.Label_specContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_role_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_role_stmt(CockroachDBParser.Alter_role_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_role_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_role_stmt(CockroachDBParser.Alter_role_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_in_database}.
	 * @param ctx the parse tree
	 */
	void enterOpt_in_database(CockroachDBParser.Opt_in_databaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_in_database}.
	 * @param ctx the parse tree
	 */
	void exitOpt_in_database(CockroachDBParser.Opt_in_databaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#set_or_reset_clause}.
	 * @param ctx the parse tree
	 */
	void enterSet_or_reset_clause(CockroachDBParser.Set_or_reset_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#set_or_reset_clause}.
	 * @param ctx the parse tree
	 */
	void exitSet_or_reset_clause(CockroachDBParser.Set_or_reset_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_virtual_cluster_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_virtual_cluster_stmt(CockroachDBParser.Alter_virtual_cluster_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_virtual_cluster_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_virtual_cluster_stmt(CockroachDBParser.Alter_virtual_cluster_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_virtual_cluster_replication_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_virtual_cluster_replication_stmt(CockroachDBParser.Alter_virtual_cluster_replication_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_virtual_cluster_replication_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_virtual_cluster_replication_stmt(CockroachDBParser.Alter_virtual_cluster_replication_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#virtual_cluster}.
	 * @param ctx the parse tree
	 */
	void enterVirtual_cluster(CockroachDBParser.Virtual_clusterContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#virtual_cluster}.
	 * @param ctx the parse tree
	 */
	void exitVirtual_cluster(CockroachDBParser.Virtual_clusterContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#virtual_cluster_spec}.
	 * @param ctx the parse tree
	 */
	void enterVirtual_cluster_spec(CockroachDBParser.Virtual_cluster_specContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#virtual_cluster_spec}.
	 * @param ctx the parse tree
	 */
	void exitVirtual_cluster_spec(CockroachDBParser.Virtual_cluster_specContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#replication_options_list}.
	 * @param ctx the parse tree
	 */
	void enterReplication_options_list(CockroachDBParser.Replication_options_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#replication_options_list}.
	 * @param ctx the parse tree
	 */
	void exitReplication_options_list(CockroachDBParser.Replication_options_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#replication_options}.
	 * @param ctx the parse tree
	 */
	void enterReplication_options(CockroachDBParser.Replication_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#replication_options}.
	 * @param ctx the parse tree
	 */
	void exitReplication_options(CockroachDBParser.Replication_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#source_replication_options_list}.
	 * @param ctx the parse tree
	 */
	void enterSource_replication_options_list(CockroachDBParser.Source_replication_options_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#source_replication_options_list}.
	 * @param ctx the parse tree
	 */
	void exitSource_replication_options_list(CockroachDBParser.Source_replication_options_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#source_replication_options}.
	 * @param ctx the parse tree
	 */
	void enterSource_replication_options(CockroachDBParser.Source_replication_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#source_replication_options}.
	 * @param ctx the parse tree
	 */
	void exitSource_replication_options(CockroachDBParser.Source_replication_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_with_replication_options}.
	 * @param ctx the parse tree
	 */
	void enterOpt_with_replication_options(CockroachDBParser.Opt_with_replication_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_with_replication_options}.
	 * @param ctx the parse tree
	 */
	void exitOpt_with_replication_options(CockroachDBParser.Opt_with_replication_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_virtual_cluster_capability_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_virtual_cluster_capability_stmt(CockroachDBParser.Alter_virtual_cluster_capability_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_virtual_cluster_capability_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_virtual_cluster_capability_stmt(CockroachDBParser.Alter_virtual_cluster_capability_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_virtual_cluster_rename_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_virtual_cluster_rename_stmt(CockroachDBParser.Alter_virtual_cluster_rename_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_virtual_cluster_rename_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_virtual_cluster_rename_stmt(CockroachDBParser.Alter_virtual_cluster_rename_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_virtual_cluster_service_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_virtual_cluster_service_stmt(CockroachDBParser.Alter_virtual_cluster_service_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_virtual_cluster_service_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_virtual_cluster_service_stmt(CockroachDBParser.Alter_virtual_cluster_service_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_ddl_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_ddl_stmt(CockroachDBParser.Alter_ddl_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_ddl_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_ddl_stmt(CockroachDBParser.Alter_ddl_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_stmt(CockroachDBParser.Alter_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_stmt(CockroachDBParser.Alter_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_onetable_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_onetable_stmt(CockroachDBParser.Alter_onetable_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_onetable_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_onetable_stmt(CockroachDBParser.Alter_onetable_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_table_cmds}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_cmds(CockroachDBParser.Alter_table_cmdsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_table_cmds}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_cmds(CockroachDBParser.Alter_table_cmdsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_table_cmd}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_cmd(CockroachDBParser.Alter_table_cmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_table_cmd}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_cmd(CockroachDBParser.Alter_table_cmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_column}.
	 * @param ctx the parse tree
	 */
	void enterOpt_column(CockroachDBParser.Opt_columnContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_column}.
	 * @param ctx the parse tree
	 */
	void exitOpt_column(CockroachDBParser.Opt_columnContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#column_table_def}.
	 * @param ctx the parse tree
	 */
	void enterColumn_table_def(CockroachDBParser.Column_table_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#column_table_def}.
	 * @param ctx the parse tree
	 */
	void exitColumn_table_def(CockroachDBParser.Column_table_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_constraint}.
	 * @param ctx the parse tree
	 */
	void enterTable_constraint(CockroachDBParser.Table_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_constraint}.
	 * @param ctx the parse tree
	 */
	void exitTable_constraint(CockroachDBParser.Table_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#constraint_elem}.
	 * @param ctx the parse tree
	 */
	void enterConstraint_elem(CockroachDBParser.Constraint_elemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#constraint_elem}.
	 * @param ctx the parse tree
	 */
	void exitConstraint_elem(CockroachDBParser.Constraint_elemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_storing}.
	 * @param ctx the parse tree
	 */
	void enterOpt_storing(CockroachDBParser.Opt_storingContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_storing}.
	 * @param ctx the parse tree
	 */
	void exitOpt_storing(CockroachDBParser.Opt_storingContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#storing}.
	 * @param ctx the parse tree
	 */
	void enterStoring(CockroachDBParser.StoringContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#storing}.
	 * @param ctx the parse tree
	 */
	void exitStoring(CockroachDBParser.StoringContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_partition_by_index}.
	 * @param ctx the parse tree
	 */
	void enterOpt_partition_by_index(CockroachDBParser.Opt_partition_by_indexContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_partition_by_index}.
	 * @param ctx the parse tree
	 */
	void exitOpt_partition_by_index(CockroachDBParser.Opt_partition_by_indexContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_validate_behavior}.
	 * @param ctx the parse tree
	 */
	void enterOpt_validate_behavior(CockroachDBParser.Opt_validate_behaviorContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_validate_behavior}.
	 * @param ctx the parse tree
	 */
	void exitOpt_validate_behavior(CockroachDBParser.Opt_validate_behaviorContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_column_default}.
	 * @param ctx the parse tree
	 */
	void enterAlter_column_default(CockroachDBParser.Alter_column_defaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_column_default}.
	 * @param ctx the parse tree
	 */
	void exitAlter_column_default(CockroachDBParser.Alter_column_defaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_column_on_update}.
	 * @param ctx the parse tree
	 */
	void enterAlter_column_on_update(CockroachDBParser.Alter_column_on_updateContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_column_on_update}.
	 * @param ctx the parse tree
	 */
	void exitAlter_column_on_update(CockroachDBParser.Alter_column_on_updateContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_column_visible}.
	 * @param ctx the parse tree
	 */
	void enterAlter_column_visible(CockroachDBParser.Alter_column_visibleContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_column_visible}.
	 * @param ctx the parse tree
	 */
	void exitAlter_column_visible(CockroachDBParser.Alter_column_visibleContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#identity_option_list}.
	 * @param ctx the parse tree
	 */
	void enterIdentity_option_list(CockroachDBParser.Identity_option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#identity_option_list}.
	 * @param ctx the parse tree
	 */
	void exitIdentity_option_list(CockroachDBParser.Identity_option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#identity_option_elem}.
	 * @param ctx the parse tree
	 */
	void enterIdentity_option_elem(CockroachDBParser.Identity_option_elemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#identity_option_elem}.
	 * @param ctx the parse tree
	 */
	void exitIdentity_option_elem(CockroachDBParser.Identity_option_elemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_set_data}.
	 * @param ctx the parse tree
	 */
	void enterOpt_set_data(CockroachDBParser.Opt_set_dataContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_set_data}.
	 * @param ctx the parse tree
	 */
	void exitOpt_set_data(CockroachDBParser.Opt_set_dataContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_collate}.
	 * @param ctx the parse tree
	 */
	void enterOpt_collate(CockroachDBParser.Opt_collateContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_collate}.
	 * @param ctx the parse tree
	 */
	void exitOpt_collate(CockroachDBParser.Opt_collateContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_alter_column_using}.
	 * @param ctx the parse tree
	 */
	void enterOpt_alter_column_using(CockroachDBParser.Opt_alter_column_usingContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_alter_column_using}.
	 * @param ctx the parse tree
	 */
	void exitOpt_alter_column_using(CockroachDBParser.Opt_alter_column_usingContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#col_qual_list}.
	 * @param ctx the parse tree
	 */
	void enterCol_qual_list(CockroachDBParser.Col_qual_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#col_qual_list}.
	 * @param ctx the parse tree
	 */
	void exitCol_qual_list(CockroachDBParser.Col_qual_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#col_qualification}.
	 * @param ctx the parse tree
	 */
	void enterCol_qualification(CockroachDBParser.Col_qualificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#col_qualification}.
	 * @param ctx the parse tree
	 */
	void exitCol_qualification(CockroachDBParser.Col_qualificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#col_qualification_elem}.
	 * @param ctx the parse tree
	 */
	void enterCol_qualification_elem(CockroachDBParser.Col_qualification_elemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#col_qualification_elem}.
	 * @param ctx the parse tree
	 */
	void exitCol_qualification_elem(CockroachDBParser.Col_qualification_elemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_name_parens}.
	 * @param ctx the parse tree
	 */
	void enterOpt_name_parens(CockroachDBParser.Opt_name_parensContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_name_parens}.
	 * @param ctx the parse tree
	 */
	void exitOpt_name_parens(CockroachDBParser.Opt_name_parensContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#key_match}.
	 * @param ctx the parse tree
	 */
	void enterKey_match(CockroachDBParser.Key_matchContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#key_match}.
	 * @param ctx the parse tree
	 */
	void exitKey_match(CockroachDBParser.Key_matchContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#reference_actions}.
	 * @param ctx the parse tree
	 */
	void enterReference_actions(CockroachDBParser.Reference_actionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#reference_actions}.
	 * @param ctx the parse tree
	 */
	void exitReference_actions(CockroachDBParser.Reference_actionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#reference_on_update}.
	 * @param ctx the parse tree
	 */
	void enterReference_on_update(CockroachDBParser.Reference_on_updateContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#reference_on_update}.
	 * @param ctx the parse tree
	 */
	void exitReference_on_update(CockroachDBParser.Reference_on_updateContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#reference_on_delete}.
	 * @param ctx the parse tree
	 */
	void enterReference_on_delete(CockroachDBParser.Reference_on_deleteContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#reference_on_delete}.
	 * @param ctx the parse tree
	 */
	void exitReference_on_delete(CockroachDBParser.Reference_on_deleteContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#reference_action}.
	 * @param ctx the parse tree
	 */
	void enterReference_action(CockroachDBParser.Reference_actionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#reference_action}.
	 * @param ctx the parse tree
	 */
	void exitReference_action(CockroachDBParser.Reference_actionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#generated_as}.
	 * @param ctx the parse tree
	 */
	void enterGenerated_as(CockroachDBParser.Generated_asContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#generated_as}.
	 * @param ctx the parse tree
	 */
	void exitGenerated_as(CockroachDBParser.Generated_asContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#generated_always_as}.
	 * @param ctx the parse tree
	 */
	void enterGenerated_always_as(CockroachDBParser.Generated_always_asContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#generated_always_as}.
	 * @param ctx the parse tree
	 */
	void exitGenerated_always_as(CockroachDBParser.Generated_always_asContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#generated_by_default_as}.
	 * @param ctx the parse tree
	 */
	void enterGenerated_by_default_as(CockroachDBParser.Generated_by_default_asContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#generated_by_default_as}.
	 * @param ctx the parse tree
	 */
	void exitGenerated_by_default_as(CockroachDBParser.Generated_by_default_asContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_sequence_option_list}.
	 * @param ctx the parse tree
	 */
	void enterOpt_sequence_option_list(CockroachDBParser.Opt_sequence_option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_sequence_option_list}.
	 * @param ctx the parse tree
	 */
	void exitOpt_sequence_option_list(CockroachDBParser.Opt_sequence_option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#sequence_option_list}.
	 * @param ctx the parse tree
	 */
	void enterSequence_option_list(CockroachDBParser.Sequence_option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#sequence_option_list}.
	 * @param ctx the parse tree
	 */
	void exitSequence_option_list(CockroachDBParser.Sequence_option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#sequence_option_elem}.
	 * @param ctx the parse tree
	 */
	void enterSequence_option_elem(CockroachDBParser.Sequence_option_elemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#sequence_option_elem}.
	 * @param ctx the parse tree
	 */
	void exitSequence_option_elem(CockroachDBParser.Sequence_option_elemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#set_generated_always}.
	 * @param ctx the parse tree
	 */
	void enterSet_generated_always(CockroachDBParser.Set_generated_alwaysContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#set_generated_always}.
	 * @param ctx the parse tree
	 */
	void exitSet_generated_always(CockroachDBParser.Set_generated_alwaysContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#set_generated_default}.
	 * @param ctx the parse tree
	 */
	void enterSet_generated_default(CockroachDBParser.Set_generated_defaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#set_generated_default}.
	 * @param ctx the parse tree
	 */
	void exitSet_generated_default(CockroachDBParser.Set_generated_defaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#index_params}.
	 * @param ctx the parse tree
	 */
	void enterIndex_params(CockroachDBParser.Index_paramsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#index_params}.
	 * @param ctx the parse tree
	 */
	void exitIndex_params(CockroachDBParser.Index_paramsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#index_elem}.
	 * @param ctx the parse tree
	 */
	void enterIndex_elem(CockroachDBParser.Index_elemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#index_elem}.
	 * @param ctx the parse tree
	 */
	void exitIndex_elem(CockroachDBParser.Index_elemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#index_elem_options}.
	 * @param ctx the parse tree
	 */
	void enterIndex_elem_options(CockroachDBParser.Index_elem_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#index_elem_options}.
	 * @param ctx the parse tree
	 */
	void exitIndex_elem_options(CockroachDBParser.Index_elem_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_class}.
	 * @param ctx the parse tree
	 */
	void enterOpt_class(CockroachDBParser.Opt_classContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_class}.
	 * @param ctx the parse tree
	 */
	void exitOpt_class(CockroachDBParser.Opt_classContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_hash_sharded}.
	 * @param ctx the parse tree
	 */
	void enterOpt_hash_sharded(CockroachDBParser.Opt_hash_shardedContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_hash_sharded}.
	 * @param ctx the parse tree
	 */
	void exitOpt_hash_sharded(CockroachDBParser.Opt_hash_shardedContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_hash_sharded_bucket_count}.
	 * @param ctx the parse tree
	 */
	void enterOpt_hash_sharded_bucket_count(CockroachDBParser.Opt_hash_sharded_bucket_countContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_hash_sharded_bucket_count}.
	 * @param ctx the parse tree
	 */
	void exitOpt_hash_sharded_bucket_count(CockroachDBParser.Opt_hash_sharded_bucket_countContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_with_storage_parameter_list}.
	 * @param ctx the parse tree
	 */
	void enterOpt_with_storage_parameter_list(CockroachDBParser.Opt_with_storage_parameter_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_with_storage_parameter_list}.
	 * @param ctx the parse tree
	 */
	void exitOpt_with_storage_parameter_list(CockroachDBParser.Opt_with_storage_parameter_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#audit_mode}.
	 * @param ctx the parse tree
	 */
	void enterAudit_mode(CockroachDBParser.Audit_modeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#audit_mode}.
	 * @param ctx the parse tree
	 */
	void exitAudit_mode(CockroachDBParser.Audit_modeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#partition_by_table}.
	 * @param ctx the parse tree
	 */
	void enterPartition_by_table(CockroachDBParser.Partition_by_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#partition_by_table}.
	 * @param ctx the parse tree
	 */
	void exitPartition_by_table(CockroachDBParser.Partition_by_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#partition_by}.
	 * @param ctx the parse tree
	 */
	void enterPartition_by(CockroachDBParser.Partition_byContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#partition_by}.
	 * @param ctx the parse tree
	 */
	void exitPartition_by(CockroachDBParser.Partition_byContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#partition_by_inner}.
	 * @param ctx the parse tree
	 */
	void enterPartition_by_inner(CockroachDBParser.Partition_by_innerContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#partition_by_inner}.
	 * @param ctx the parse tree
	 */
	void exitPartition_by_inner(CockroachDBParser.Partition_by_innerContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#list_partitions}.
	 * @param ctx the parse tree
	 */
	void enterList_partitions(CockroachDBParser.List_partitionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#list_partitions}.
	 * @param ctx the parse tree
	 */
	void exitList_partitions(CockroachDBParser.List_partitionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#list_partition}.
	 * @param ctx the parse tree
	 */
	void enterList_partition(CockroachDBParser.List_partitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#list_partition}.
	 * @param ctx the parse tree
	 */
	void exitList_partition(CockroachDBParser.List_partitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_partition_by}.
	 * @param ctx the parse tree
	 */
	void enterOpt_partition_by(CockroachDBParser.Opt_partition_byContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_partition_by}.
	 * @param ctx the parse tree
	 */
	void exitOpt_partition_by(CockroachDBParser.Opt_partition_byContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#range_partitions}.
	 * @param ctx the parse tree
	 */
	void enterRange_partitions(CockroachDBParser.Range_partitionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#range_partitions}.
	 * @param ctx the parse tree
	 */
	void exitRange_partitions(CockroachDBParser.Range_partitionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#range_partition}.
	 * @param ctx the parse tree
	 */
	void enterRange_partition(CockroachDBParser.Range_partitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#range_partition}.
	 * @param ctx the parse tree
	 */
	void exitRange_partition(CockroachDBParser.Range_partitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#storage_parameter_list}.
	 * @param ctx the parse tree
	 */
	void enterStorage_parameter_list(CockroachDBParser.Storage_parameter_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#storage_parameter_list}.
	 * @param ctx the parse tree
	 */
	void exitStorage_parameter_list(CockroachDBParser.Storage_parameter_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#storage_parameter}.
	 * @param ctx the parse tree
	 */
	void enterStorage_parameter(CockroachDBParser.Storage_parameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#storage_parameter}.
	 * @param ctx the parse tree
	 */
	void exitStorage_parameter(CockroachDBParser.Storage_parameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#storage_parameter_key_list}.
	 * @param ctx the parse tree
	 */
	void enterStorage_parameter_key_list(CockroachDBParser.Storage_parameter_key_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#storage_parameter_key_list}.
	 * @param ctx the parse tree
	 */
	void exitStorage_parameter_key_list(CockroachDBParser.Storage_parameter_key_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#storage_parameter_key}.
	 * @param ctx the parse tree
	 */
	void enterStorage_parameter_key(CockroachDBParser.Storage_parameter_keyContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#storage_parameter_key}.
	 * @param ctx the parse tree
	 */
	void exitStorage_parameter_key(CockroachDBParser.Storage_parameter_keyContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_rls_mode}.
	 * @param ctx the parse tree
	 */
	void enterTable_rls_mode(CockroachDBParser.Table_rls_modeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_rls_mode}.
	 * @param ctx the parse tree
	 */
	void exitTable_rls_mode(CockroachDBParser.Table_rls_modeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_split_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_split_stmt(CockroachDBParser.Alter_split_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_split_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_split_stmt(CockroachDBParser.Alter_split_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_unsplit_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_unsplit_stmt(CockroachDBParser.Alter_unsplit_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_unsplit_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_unsplit_stmt(CockroachDBParser.Alter_unsplit_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_scatter_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_scatter_stmt(CockroachDBParser.Alter_scatter_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_scatter_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_scatter_stmt(CockroachDBParser.Alter_scatter_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_zone_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_zone_table_stmt(CockroachDBParser.Alter_zone_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_zone_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_zone_table_stmt(CockroachDBParser.Alter_zone_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_rename_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_rename_table_stmt(CockroachDBParser.Alter_rename_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_rename_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_rename_table_stmt(CockroachDBParser.Alter_rename_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_table_set_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_set_schema_stmt(CockroachDBParser.Alter_table_set_schema_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_table_set_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_set_schema_stmt(CockroachDBParser.Alter_table_set_schema_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_table_locality_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_locality_stmt(CockroachDBParser.Alter_table_locality_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_table_locality_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_locality_stmt(CockroachDBParser.Alter_table_locality_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#locality}.
	 * @param ctx the parse tree
	 */
	void enterLocality(CockroachDBParser.LocalityContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#locality}.
	 * @param ctx the parse tree
	 */
	void exitLocality(CockroachDBParser.LocalityContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#region_name}.
	 * @param ctx the parse tree
	 */
	void enterRegion_name(CockroachDBParser.Region_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#region_name}.
	 * @param ctx the parse tree
	 */
	void exitRegion_name(CockroachDBParser.Region_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_table_logged_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_logged_stmt(CockroachDBParser.Alter_table_logged_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_table_logged_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_logged_stmt(CockroachDBParser.Alter_table_logged_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_table_owner_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_owner_stmt(CockroachDBParser.Alter_table_owner_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_table_owner_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_owner_stmt(CockroachDBParser.Alter_table_owner_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_index_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_index_stmt(CockroachDBParser.Alter_index_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_index_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_index_stmt(CockroachDBParser.Alter_index_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_oneindex_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_oneindex_stmt(CockroachDBParser.Alter_oneindex_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_oneindex_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_oneindex_stmt(CockroachDBParser.Alter_oneindex_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_index_cmds}.
	 * @param ctx the parse tree
	 */
	void enterAlter_index_cmds(CockroachDBParser.Alter_index_cmdsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_index_cmds}.
	 * @param ctx the parse tree
	 */
	void exitAlter_index_cmds(CockroachDBParser.Alter_index_cmdsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_index_cmd}.
	 * @param ctx the parse tree
	 */
	void enterAlter_index_cmd(CockroachDBParser.Alter_index_cmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_index_cmd}.
	 * @param ctx the parse tree
	 */
	void exitAlter_index_cmd(CockroachDBParser.Alter_index_cmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#partition_by_index}.
	 * @param ctx the parse tree
	 */
	void enterPartition_by_index(CockroachDBParser.Partition_by_indexContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#partition_by_index}.
	 * @param ctx the parse tree
	 */
	void exitPartition_by_index(CockroachDBParser.Partition_by_indexContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_split_index_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_split_index_stmt(CockroachDBParser.Alter_split_index_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_split_index_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_split_index_stmt(CockroachDBParser.Alter_split_index_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_unsplit_index_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_unsplit_index_stmt(CockroachDBParser.Alter_unsplit_index_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_unsplit_index_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_unsplit_index_stmt(CockroachDBParser.Alter_unsplit_index_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_scatter_index_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_scatter_index_stmt(CockroachDBParser.Alter_scatter_index_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_scatter_index_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_scatter_index_stmt(CockroachDBParser.Alter_scatter_index_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_rename_index_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_rename_index_stmt(CockroachDBParser.Alter_rename_index_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_rename_index_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_rename_index_stmt(CockroachDBParser.Alter_rename_index_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_zone_index_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_zone_index_stmt(CockroachDBParser.Alter_zone_index_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_zone_index_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_zone_index_stmt(CockroachDBParser.Alter_zone_index_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_index_visible_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_index_visible_stmt(CockroachDBParser.Alter_index_visible_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_index_visible_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_index_visible_stmt(CockroachDBParser.Alter_index_visible_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_index_visible}.
	 * @param ctx the parse tree
	 */
	void enterAlter_index_visible(CockroachDBParser.Alter_index_visibleContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_index_visible}.
	 * @param ctx the parse tree
	 */
	void exitAlter_index_visible(CockroachDBParser.Alter_index_visibleContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_view_stmt(CockroachDBParser.Alter_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_view_stmt(CockroachDBParser.Alter_view_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_rename_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_rename_view_stmt(CockroachDBParser.Alter_rename_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_rename_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_rename_view_stmt(CockroachDBParser.Alter_rename_view_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_view_set_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_view_set_schema_stmt(CockroachDBParser.Alter_view_set_schema_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_view_set_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_view_set_schema_stmt(CockroachDBParser.Alter_view_set_schema_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_view_owner_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_view_owner_stmt(CockroachDBParser.Alter_view_owner_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_view_owner_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_view_owner_stmt(CockroachDBParser.Alter_view_owner_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_sequence_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_sequence_stmt(CockroachDBParser.Alter_sequence_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_sequence_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_sequence_stmt(CockroachDBParser.Alter_sequence_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_rename_sequence_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_rename_sequence_stmt(CockroachDBParser.Alter_rename_sequence_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_rename_sequence_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_rename_sequence_stmt(CockroachDBParser.Alter_rename_sequence_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_sequence_options_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_sequence_options_stmt(CockroachDBParser.Alter_sequence_options_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_sequence_options_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_sequence_options_stmt(CockroachDBParser.Alter_sequence_options_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_sequence_set_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_sequence_set_schema_stmt(CockroachDBParser.Alter_sequence_set_schema_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_sequence_set_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_sequence_set_schema_stmt(CockroachDBParser.Alter_sequence_set_schema_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_sequence_owner_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_sequence_owner_stmt(CockroachDBParser.Alter_sequence_owner_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_sequence_owner_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_sequence_owner_stmt(CockroachDBParser.Alter_sequence_owner_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_database_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_stmt(CockroachDBParser.Alter_database_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_database_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_stmt(CockroachDBParser.Alter_database_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_rename_database_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_rename_database_stmt(CockroachDBParser.Alter_rename_database_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_rename_database_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_rename_database_stmt(CockroachDBParser.Alter_rename_database_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_zone_database_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_zone_database_stmt(CockroachDBParser.Alter_zone_database_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_zone_database_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_zone_database_stmt(CockroachDBParser.Alter_zone_database_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_database_owner}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_owner(CockroachDBParser.Alter_database_ownerContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_database_owner}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_owner(CockroachDBParser.Alter_database_ownerContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_database_to_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_to_schema_stmt(CockroachDBParser.Alter_database_to_schema_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_database_to_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_to_schema_stmt(CockroachDBParser.Alter_database_to_schema_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_database_add_region_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_add_region_stmt(CockroachDBParser.Alter_database_add_region_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_database_add_region_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_add_region_stmt(CockroachDBParser.Alter_database_add_region_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_database_drop_region_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_drop_region_stmt(CockroachDBParser.Alter_database_drop_region_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_database_drop_region_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_drop_region_stmt(CockroachDBParser.Alter_database_drop_region_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_database_survival_goal_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_survival_goal_stmt(CockroachDBParser.Alter_database_survival_goal_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_database_survival_goal_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_survival_goal_stmt(CockroachDBParser.Alter_database_survival_goal_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#survival_goal_clause}.
	 * @param ctx the parse tree
	 */
	void enterSurvival_goal_clause(CockroachDBParser.Survival_goal_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#survival_goal_clause}.
	 * @param ctx the parse tree
	 */
	void exitSurvival_goal_clause(CockroachDBParser.Survival_goal_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_equal}.
	 * @param ctx the parse tree
	 */
	void enterOpt_equal(CockroachDBParser.Opt_equalContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_equal}.
	 * @param ctx the parse tree
	 */
	void exitOpt_equal(CockroachDBParser.Opt_equalContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_database_primary_region_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_primary_region_stmt(CockroachDBParser.Alter_database_primary_region_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_database_primary_region_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_primary_region_stmt(CockroachDBParser.Alter_database_primary_region_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#primary_region_clause}.
	 * @param ctx the parse tree
	 */
	void enterPrimary_region_clause(CockroachDBParser.Primary_region_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#primary_region_clause}.
	 * @param ctx the parse tree
	 */
	void exitPrimary_region_clause(CockroachDBParser.Primary_region_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_database_placement_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_placement_stmt(CockroachDBParser.Alter_database_placement_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_database_placement_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_placement_stmt(CockroachDBParser.Alter_database_placement_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#placement_clause}.
	 * @param ctx the parse tree
	 */
	void enterPlacement_clause(CockroachDBParser.Placement_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#placement_clause}.
	 * @param ctx the parse tree
	 */
	void exitPlacement_clause(CockroachDBParser.Placement_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_database_set_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_set_stmt(CockroachDBParser.Alter_database_set_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_database_set_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_set_stmt(CockroachDBParser.Alter_database_set_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_database_add_super_region}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_add_super_region(CockroachDBParser.Alter_database_add_super_regionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_database_add_super_region}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_add_super_region(CockroachDBParser.Alter_database_add_super_regionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#region_name_list}.
	 * @param ctx the parse tree
	 */
	void enterRegion_name_list(CockroachDBParser.Region_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#region_name_list}.
	 * @param ctx the parse tree
	 */
	void exitRegion_name_list(CockroachDBParser.Region_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_database_alter_super_region}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_alter_super_region(CockroachDBParser.Alter_database_alter_super_regionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_database_alter_super_region}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_alter_super_region(CockroachDBParser.Alter_database_alter_super_regionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_database_drop_super_region}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_drop_super_region(CockroachDBParser.Alter_database_drop_super_regionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_database_drop_super_region}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_drop_super_region(CockroachDBParser.Alter_database_drop_super_regionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_database_set_secondary_region_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_set_secondary_region_stmt(CockroachDBParser.Alter_database_set_secondary_region_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_database_set_secondary_region_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_set_secondary_region_stmt(CockroachDBParser.Alter_database_set_secondary_region_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#secondary_region_clause}.
	 * @param ctx the parse tree
	 */
	void enterSecondary_region_clause(CockroachDBParser.Secondary_region_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#secondary_region_clause}.
	 * @param ctx the parse tree
	 */
	void exitSecondary_region_clause(CockroachDBParser.Secondary_region_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_database_drop_secondary_region}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_drop_secondary_region(CockroachDBParser.Alter_database_drop_secondary_regionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_database_drop_secondary_region}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_drop_secondary_region(CockroachDBParser.Alter_database_drop_secondary_regionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_database_set_zone_config_extension_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_set_zone_config_extension_stmt(CockroachDBParser.Alter_database_set_zone_config_extension_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_database_set_zone_config_extension_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_set_zone_config_extension_stmt(CockroachDBParser.Alter_database_set_zone_config_extension_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_range_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_range_stmt(CockroachDBParser.Alter_range_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_range_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_range_stmt(CockroachDBParser.Alter_range_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_zone_range_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_zone_range_stmt(CockroachDBParser.Alter_zone_range_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_zone_range_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_zone_range_stmt(CockroachDBParser.Alter_zone_range_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_range_relocate_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_range_relocate_stmt(CockroachDBParser.Alter_range_relocate_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_range_relocate_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_range_relocate_stmt(CockroachDBParser.Alter_range_relocate_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#relocate_kw}.
	 * @param ctx the parse tree
	 */
	void enterRelocate_kw(CockroachDBParser.Relocate_kwContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#relocate_kw}.
	 * @param ctx the parse tree
	 */
	void exitRelocate_kw(CockroachDBParser.Relocate_kwContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#lease_or_relocate_subject_nonlease}.
	 * @param ctx the parse tree
	 */
	void enterLease_or_relocate_subject_nonlease(CockroachDBParser.Lease_or_relocate_subject_nonleaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#lease_or_relocate_subject_nonlease}.
	 * @param ctx the parse tree
	 */
	void exitLease_or_relocate_subject_nonlease(CockroachDBParser.Lease_or_relocate_subject_nonleaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#relocate_subject_nonlease}.
	 * @param ctx the parse tree
	 */
	void enterRelocate_subject_nonlease(CockroachDBParser.Relocate_subject_nonleaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#relocate_subject_nonlease}.
	 * @param ctx the parse tree
	 */
	void exitRelocate_subject_nonlease(CockroachDBParser.Relocate_subject_nonleaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_partition_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_partition_stmt(CockroachDBParser.Alter_partition_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_partition_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_partition_stmt(CockroachDBParser.Alter_partition_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_zone_partition_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_zone_partition_stmt(CockroachDBParser.Alter_zone_partition_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_zone_partition_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_zone_partition_stmt(CockroachDBParser.Alter_zone_partition_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#set_zone_config}.
	 * @param ctx the parse tree
	 */
	void enterSet_zone_config(CockroachDBParser.Set_zone_configContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#set_zone_config}.
	 * @param ctx the parse tree
	 */
	void exitSet_zone_config(CockroachDBParser.Set_zone_configContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#var_set_list}.
	 * @param ctx the parse tree
	 */
	void enterVar_set_list(CockroachDBParser.Var_set_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#var_set_list}.
	 * @param ctx the parse tree
	 */
	void exitVar_set_list(CockroachDBParser.Var_set_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#var_set_item}.
	 * @param ctx the parse tree
	 */
	void enterVar_set_item(CockroachDBParser.Var_set_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#var_set_item}.
	 * @param ctx the parse tree
	 */
	void exitVar_set_item(CockroachDBParser.Var_set_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_schema_stmt(CockroachDBParser.Alter_schema_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_schema_stmt(CockroachDBParser.Alter_schema_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_type_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_type_stmt(CockroachDBParser.Alter_type_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_type_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_type_stmt(CockroachDBParser.Alter_type_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_add_val_placement}.
	 * @param ctx the parse tree
	 */
	void enterOpt_add_val_placement(CockroachDBParser.Opt_add_val_placementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_add_val_placement}.
	 * @param ctx the parse tree
	 */
	void exitOpt_add_val_placement(CockroachDBParser.Opt_add_val_placementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_default_privileges_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_default_privileges_stmt(CockroachDBParser.Alter_default_privileges_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_default_privileges_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_default_privileges_stmt(CockroachDBParser.Alter_default_privileges_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#abbreviated_grant_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAbbreviated_grant_stmt(CockroachDBParser.Abbreviated_grant_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#abbreviated_grant_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAbbreviated_grant_stmt(CockroachDBParser.Abbreviated_grant_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#target_object_type}.
	 * @param ctx the parse tree
	 */
	void enterTarget_object_type(CockroachDBParser.Target_object_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#target_object_type}.
	 * @param ctx the parse tree
	 */
	void exitTarget_object_type(CockroachDBParser.Target_object_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#abbreviated_revoke_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAbbreviated_revoke_stmt(CockroachDBParser.Abbreviated_revoke_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#abbreviated_revoke_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAbbreviated_revoke_stmt(CockroachDBParser.Abbreviated_revoke_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_in_schemas}.
	 * @param ctx the parse tree
	 */
	void enterOpt_in_schemas(CockroachDBParser.Opt_in_schemasContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_in_schemas}.
	 * @param ctx the parse tree
	 */
	void exitOpt_in_schemas(CockroachDBParser.Opt_in_schemasContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_changefeed_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_changefeed_stmt(CockroachDBParser.Alter_changefeed_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_changefeed_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_changefeed_stmt(CockroachDBParser.Alter_changefeed_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_changefeed_cmds}.
	 * @param ctx the parse tree
	 */
	void enterAlter_changefeed_cmds(CockroachDBParser.Alter_changefeed_cmdsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_changefeed_cmds}.
	 * @param ctx the parse tree
	 */
	void exitAlter_changefeed_cmds(CockroachDBParser.Alter_changefeed_cmdsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_changefeed_cmd}.
	 * @param ctx the parse tree
	 */
	void enterAlter_changefeed_cmd(CockroachDBParser.Alter_changefeed_cmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_changefeed_cmd}.
	 * @param ctx the parse tree
	 */
	void exitAlter_changefeed_cmd(CockroachDBParser.Alter_changefeed_cmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#changefeed_table_targets}.
	 * @param ctx the parse tree
	 */
	void enterChangefeed_table_targets(CockroachDBParser.Changefeed_table_targetsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#changefeed_table_targets}.
	 * @param ctx the parse tree
	 */
	void exitChangefeed_table_targets(CockroachDBParser.Changefeed_table_targetsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#changefeed_table_target}.
	 * @param ctx the parse tree
	 */
	void enterChangefeed_table_target(CockroachDBParser.Changefeed_table_targetContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#changefeed_table_target}.
	 * @param ctx the parse tree
	 */
	void exitChangefeed_table_target(CockroachDBParser.Changefeed_table_targetContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_table_prefix}.
	 * @param ctx the parse tree
	 */
	void enterOpt_table_prefix(CockroachDBParser.Opt_table_prefixContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_table_prefix}.
	 * @param ctx the parse tree
	 */
	void exitOpt_table_prefix(CockroachDBParser.Opt_table_prefixContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_chagefeed_family}.
	 * @param ctx the parse tree
	 */
	void enterOpt_chagefeed_family(CockroachDBParser.Opt_chagefeed_familyContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_chagefeed_family}.
	 * @param ctx the parse tree
	 */
	void exitOpt_chagefeed_family(CockroachDBParser.Opt_chagefeed_familyContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_backup_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_backup_stmt(CockroachDBParser.Alter_backup_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_backup_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_backup_stmt(CockroachDBParser.Alter_backup_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_backup_cmds}.
	 * @param ctx the parse tree
	 */
	void enterAlter_backup_cmds(CockroachDBParser.Alter_backup_cmdsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_backup_cmds}.
	 * @param ctx the parse tree
	 */
	void exitAlter_backup_cmds(CockroachDBParser.Alter_backup_cmdsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_backup_cmd}.
	 * @param ctx the parse tree
	 */
	void enterAlter_backup_cmd(CockroachDBParser.Alter_backup_cmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_backup_cmd}.
	 * @param ctx the parse tree
	 */
	void exitAlter_backup_cmd(CockroachDBParser.Alter_backup_cmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#backup_kms}.
	 * @param ctx the parse tree
	 */
	void enterBackup_kms(CockroachDBParser.Backup_kmsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#backup_kms}.
	 * @param ctx the parse tree
	 */
	void exitBackup_kms(CockroachDBParser.Backup_kmsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_func_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_func_stmt(CockroachDBParser.Alter_func_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_func_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_func_stmt(CockroachDBParser.Alter_func_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_func_options_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_func_options_stmt(CockroachDBParser.Alter_func_options_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_func_options_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_func_options_stmt(CockroachDBParser.Alter_func_options_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_func_opt_list}.
	 * @param ctx the parse tree
	 */
	void enterAlter_func_opt_list(CockroachDBParser.Alter_func_opt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_func_opt_list}.
	 * @param ctx the parse tree
	 */
	void exitAlter_func_opt_list(CockroachDBParser.Alter_func_opt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#common_routine_opt_item}.
	 * @param ctx the parse tree
	 */
	void enterCommon_routine_opt_item(CockroachDBParser.Common_routine_opt_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#common_routine_opt_item}.
	 * @param ctx the parse tree
	 */
	void exitCommon_routine_opt_item(CockroachDBParser.Common_routine_opt_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#ops_restrict}.
	 * @param ctx the parse tree
	 */
	void enterOps_restrict(CockroachDBParser.Ops_restrictContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#ops_restrict}.
	 * @param ctx the parse tree
	 */
	void exitOps_restrict(CockroachDBParser.Ops_restrictContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_func_rename_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_func_rename_stmt(CockroachDBParser.Alter_func_rename_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_func_rename_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_func_rename_stmt(CockroachDBParser.Alter_func_rename_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_func_owner_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_func_owner_stmt(CockroachDBParser.Alter_func_owner_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_func_owner_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_func_owner_stmt(CockroachDBParser.Alter_func_owner_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_func_set_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_func_set_schema_stmt(CockroachDBParser.Alter_func_set_schema_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_func_set_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_func_set_schema_stmt(CockroachDBParser.Alter_func_set_schema_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_func_dep_extension_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_func_dep_extension_stmt(CockroachDBParser.Alter_func_dep_extension_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_func_dep_extension_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_func_dep_extension_stmt(CockroachDBParser.Alter_func_dep_extension_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_no}.
	 * @param ctx the parse tree
	 */
	void enterOpt_no(CockroachDBParser.Opt_noContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_no}.
	 * @param ctx the parse tree
	 */
	void exitOpt_no(CockroachDBParser.Opt_noContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_proc_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_proc_stmt(CockroachDBParser.Alter_proc_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_proc_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_proc_stmt(CockroachDBParser.Alter_proc_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_proc_rename_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_proc_rename_stmt(CockroachDBParser.Alter_proc_rename_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_proc_rename_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_proc_rename_stmt(CockroachDBParser.Alter_proc_rename_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_proc_owner_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_proc_owner_stmt(CockroachDBParser.Alter_proc_owner_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_proc_owner_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_proc_owner_stmt(CockroachDBParser.Alter_proc_owner_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_proc_set_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_proc_set_schema_stmt(CockroachDBParser.Alter_proc_set_schema_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_proc_set_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_proc_set_schema_stmt(CockroachDBParser.Alter_proc_set_schema_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_backup_schedule}.
	 * @param ctx the parse tree
	 */
	void enterAlter_backup_schedule(CockroachDBParser.Alter_backup_scheduleContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_backup_schedule}.
	 * @param ctx the parse tree
	 */
	void exitAlter_backup_schedule(CockroachDBParser.Alter_backup_scheduleContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_backup_schedule_cmds}.
	 * @param ctx the parse tree
	 */
	void enterAlter_backup_schedule_cmds(CockroachDBParser.Alter_backup_schedule_cmdsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_backup_schedule_cmds}.
	 * @param ctx the parse tree
	 */
	void exitAlter_backup_schedule_cmds(CockroachDBParser.Alter_backup_schedule_cmdsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_backup_schedule_cmd}.
	 * @param ctx the parse tree
	 */
	void enterAlter_backup_schedule_cmd(CockroachDBParser.Alter_backup_schedule_cmdContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_backup_schedule_cmd}.
	 * @param ctx the parse tree
	 */
	void exitAlter_backup_schedule_cmd(CockroachDBParser.Alter_backup_schedule_cmdContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#cron_expr}.
	 * @param ctx the parse tree
	 */
	void enterCron_expr(CockroachDBParser.Cron_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#cron_expr}.
	 * @param ctx the parse tree
	 */
	void exitCron_expr(CockroachDBParser.Cron_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_policy_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_policy_stmt(CockroachDBParser.Alter_policy_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_policy_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_policy_stmt(CockroachDBParser.Alter_policy_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_policy_roles}.
	 * @param ctx the parse tree
	 */
	void enterOpt_policy_roles(CockroachDBParser.Opt_policy_rolesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_policy_roles}.
	 * @param ctx the parse tree
	 */
	void exitOpt_policy_roles(CockroachDBParser.Opt_policy_rolesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_policy_exprs}.
	 * @param ctx the parse tree
	 */
	void enterOpt_policy_exprs(CockroachDBParser.Opt_policy_exprsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_policy_exprs}.
	 * @param ctx the parse tree
	 */
	void exitOpt_policy_exprs(CockroachDBParser.Opt_policy_exprsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alter_job_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_job_stmt(CockroachDBParser.Alter_job_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alter_job_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_job_stmt(CockroachDBParser.Alter_job_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#backup_stmt}.
	 * @param ctx the parse tree
	 */
	void enterBackup_stmt(CockroachDBParser.Backup_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#backup_stmt}.
	 * @param ctx the parse tree
	 */
	void exitBackup_stmt(CockroachDBParser.Backup_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#sconst_or_placeholder}.
	 * @param ctx the parse tree
	 */
	void enterSconst_or_placeholder(CockroachDBParser.Sconst_or_placeholderContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#sconst_or_placeholder}.
	 * @param ctx the parse tree
	 */
	void exitSconst_or_placeholder(CockroachDBParser.Sconst_or_placeholderContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_backup_targets}.
	 * @param ctx the parse tree
	 */
	void enterOpt_backup_targets(CockroachDBParser.Opt_backup_targetsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_backup_targets}.
	 * @param ctx the parse tree
	 */
	void exitOpt_backup_targets(CockroachDBParser.Opt_backup_targetsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#backup_targets}.
	 * @param ctx the parse tree
	 */
	void enterBackup_targets(CockroachDBParser.Backup_targetsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#backup_targets}.
	 * @param ctx the parse tree
	 */
	void exitBackup_targets(CockroachDBParser.Backup_targetsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#string_or_placeholder_opt_list}.
	 * @param ctx the parse tree
	 */
	void enterString_or_placeholder_opt_list(CockroachDBParser.String_or_placeholder_opt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#string_or_placeholder_opt_list}.
	 * @param ctx the parse tree
	 */
	void exitString_or_placeholder_opt_list(CockroachDBParser.String_or_placeholder_opt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#string_or_placeholder_list}.
	 * @param ctx the parse tree
	 */
	void enterString_or_placeholder_list(CockroachDBParser.String_or_placeholder_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#string_or_placeholder_list}.
	 * @param ctx the parse tree
	 */
	void exitString_or_placeholder_list(CockroachDBParser.String_or_placeholder_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_with_backup_options}.
	 * @param ctx the parse tree
	 */
	void enterOpt_with_backup_options(CockroachDBParser.Opt_with_backup_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_with_backup_options}.
	 * @param ctx the parse tree
	 */
	void exitOpt_with_backup_options(CockroachDBParser.Opt_with_backup_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#backup_options}.
	 * @param ctx the parse tree
	 */
	void enterBackup_options(CockroachDBParser.Backup_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#backup_options}.
	 * @param ctx the parse tree
	 */
	void exitBackup_options(CockroachDBParser.Backup_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#backup_options_list}.
	 * @param ctx the parse tree
	 */
	void enterBackup_options_list(CockroachDBParser.Backup_options_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#backup_options_list}.
	 * @param ctx the parse tree
	 */
	void exitBackup_options_list(CockroachDBParser.Backup_options_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#cancel_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCancel_stmt(CockroachDBParser.Cancel_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#cancel_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCancel_stmt(CockroachDBParser.Cancel_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#cancel_jobs_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCancel_jobs_stmt(CockroachDBParser.Cancel_jobs_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#cancel_jobs_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCancel_jobs_stmt(CockroachDBParser.Cancel_jobs_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#cancel_queries_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCancel_queries_stmt(CockroachDBParser.Cancel_queries_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#cancel_queries_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCancel_queries_stmt(CockroachDBParser.Cancel_queries_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#cancel_sessions_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCancel_sessions_stmt(CockroachDBParser.Cancel_sessions_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#cancel_sessions_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCancel_sessions_stmt(CockroachDBParser.Cancel_sessions_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#cancel_all_jobs_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCancel_all_jobs_stmt(CockroachDBParser.Cancel_all_jobs_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#cancel_all_jobs_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCancel_all_jobs_stmt(CockroachDBParser.Cancel_all_jobs_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_stmt(CockroachDBParser.Create_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_stmt(CockroachDBParser.Create_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_stats_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_stats_stmt(CockroachDBParser.Create_stats_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_stats_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_stats_stmt(CockroachDBParser.Create_stats_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_stats_columns}.
	 * @param ctx the parse tree
	 */
	void enterOpt_stats_columns(CockroachDBParser.Opt_stats_columnsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_stats_columns}.
	 * @param ctx the parse tree
	 */
	void exitOpt_stats_columns(CockroachDBParser.Opt_stats_columnsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_stats_target}.
	 * @param ctx the parse tree
	 */
	void enterCreate_stats_target(CockroachDBParser.Create_stats_targetContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_stats_target}.
	 * @param ctx the parse tree
	 */
	void exitCreate_stats_target(CockroachDBParser.Create_stats_targetContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_create_stats_options}.
	 * @param ctx the parse tree
	 */
	void enterOpt_create_stats_options(CockroachDBParser.Opt_create_stats_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_create_stats_options}.
	 * @param ctx the parse tree
	 */
	void exitOpt_create_stats_options(CockroachDBParser.Opt_create_stats_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_stats_option_list}.
	 * @param ctx the parse tree
	 */
	void enterCreate_stats_option_list(CockroachDBParser.Create_stats_option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_stats_option_list}.
	 * @param ctx the parse tree
	 */
	void exitCreate_stats_option_list(CockroachDBParser.Create_stats_option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_stats_option}.
	 * @param ctx the parse tree
	 */
	void enterCreate_stats_option(CockroachDBParser.Create_stats_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_stats_option}.
	 * @param ctx the parse tree
	 */
	void exitCreate_stats_option(CockroachDBParser.Create_stats_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_changefeed_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_changefeed_stmt(CockroachDBParser.Create_changefeed_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_changefeed_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_changefeed_stmt(CockroachDBParser.Create_changefeed_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_changefeed_sink}.
	 * @param ctx the parse tree
	 */
	void enterOpt_changefeed_sink(CockroachDBParser.Opt_changefeed_sinkContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_changefeed_sink}.
	 * @param ctx the parse tree
	 */
	void exitOpt_changefeed_sink(CockroachDBParser.Opt_changefeed_sinkContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#changefeed_target_expr}.
	 * @param ctx the parse tree
	 */
	void enterChangefeed_target_expr(CockroachDBParser.Changefeed_target_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#changefeed_target_expr}.
	 * @param ctx the parse tree
	 */
	void exitChangefeed_target_expr(CockroachDBParser.Changefeed_target_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_extension_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_extension_stmt(CockroachDBParser.Create_extension_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_extension_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_extension_stmt(CockroachDBParser.Create_extension_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_external_connection_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_external_connection_stmt(CockroachDBParser.Create_external_connection_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_external_connection_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_external_connection_stmt(CockroachDBParser.Create_external_connection_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_logical_replication_stream_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_logical_replication_stream_stmt(CockroachDBParser.Create_logical_replication_stream_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_logical_replication_stream_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_logical_replication_stream_stmt(CockroachDBParser.Create_logical_replication_stream_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#logical_replication_resources}.
	 * @param ctx the parse tree
	 */
	void enterLogical_replication_resources(CockroachDBParser.Logical_replication_resourcesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#logical_replication_resources}.
	 * @param ctx the parse tree
	 */
	void exitLogical_replication_resources(CockroachDBParser.Logical_replication_resourcesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#logical_replication_resources_list}.
	 * @param ctx the parse tree
	 */
	void enterLogical_replication_resources_list(CockroachDBParser.Logical_replication_resources_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#logical_replication_resources_list}.
	 * @param ctx the parse tree
	 */
	void exitLogical_replication_resources_list(CockroachDBParser.Logical_replication_resources_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_logical_replication_create_table_options}.
	 * @param ctx the parse tree
	 */
	void enterOpt_logical_replication_create_table_options(CockroachDBParser.Opt_logical_replication_create_table_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_logical_replication_create_table_options}.
	 * @param ctx the parse tree
	 */
	void exitOpt_logical_replication_create_table_options(CockroachDBParser.Opt_logical_replication_create_table_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#logical_replication_create_table_options_list}.
	 * @param ctx the parse tree
	 */
	void enterLogical_replication_create_table_options_list(CockroachDBParser.Logical_replication_create_table_options_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#logical_replication_create_table_options_list}.
	 * @param ctx the parse tree
	 */
	void exitLogical_replication_create_table_options_list(CockroachDBParser.Logical_replication_create_table_options_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#logical_replication_create_table_options}.
	 * @param ctx the parse tree
	 */
	void enterLogical_replication_create_table_options(CockroachDBParser.Logical_replication_create_table_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#logical_replication_create_table_options}.
	 * @param ctx the parse tree
	 */
	void exitLogical_replication_create_table_options(CockroachDBParser.Logical_replication_create_table_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_schedule_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_schedule_stmt(CockroachDBParser.Create_schedule_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_schedule_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_schedule_stmt(CockroachDBParser.Create_schedule_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_schedule_for_changefeed_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_schedule_for_changefeed_stmt(CockroachDBParser.Create_schedule_for_changefeed_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_schedule_for_changefeed_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_schedule_for_changefeed_stmt(CockroachDBParser.Create_schedule_for_changefeed_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#schedule_label_spec}.
	 * @param ctx the parse tree
	 */
	void enterSchedule_label_spec(CockroachDBParser.Schedule_label_specContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#schedule_label_spec}.
	 * @param ctx the parse tree
	 */
	void exitSchedule_label_spec(CockroachDBParser.Schedule_label_specContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#changefeed_sink}.
	 * @param ctx the parse tree
	 */
	void enterChangefeed_sink(CockroachDBParser.Changefeed_sinkContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#changefeed_sink}.
	 * @param ctx the parse tree
	 */
	void exitChangefeed_sink(CockroachDBParser.Changefeed_sinkContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_with_schedule_options}.
	 * @param ctx the parse tree
	 */
	void enterOpt_with_schedule_options(CockroachDBParser.Opt_with_schedule_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_with_schedule_options}.
	 * @param ctx the parse tree
	 */
	void exitOpt_with_schedule_options(CockroachDBParser.Opt_with_schedule_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_schedule_for_backup_stm}.
	 * @param ctx the parse tree
	 */
	void enterCreate_schedule_for_backup_stm(CockroachDBParser.Create_schedule_for_backup_stmContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_schedule_for_backup_stm}.
	 * @param ctx the parse tree
	 */
	void exitCreate_schedule_for_backup_stm(CockroachDBParser.Create_schedule_for_backup_stmContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_full_backup_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_full_backup_clause(CockroachDBParser.Opt_full_backup_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_full_backup_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_full_backup_clause(CockroachDBParser.Opt_full_backup_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_ddl_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_ddl_stmt(CockroachDBParser.Create_ddl_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_ddl_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_ddl_stmt(CockroachDBParser.Create_ddl_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_database_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_database_stmt(CockroachDBParser.Create_database_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_database_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_database_stmt(CockroachDBParser.Create_database_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_template_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_template_clause(CockroachDBParser.Opt_template_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_template_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_template_clause(CockroachDBParser.Opt_template_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_encoding_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_encoding_clause(CockroachDBParser.Opt_encoding_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_encoding_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_encoding_clause(CockroachDBParser.Opt_encoding_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_lc_collate_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_lc_collate_clause(CockroachDBParser.Opt_lc_collate_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_lc_collate_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_lc_collate_clause(CockroachDBParser.Opt_lc_collate_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_lc_ctype_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_lc_ctype_clause(CockroachDBParser.Opt_lc_ctype_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_lc_ctype_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_lc_ctype_clause(CockroachDBParser.Opt_lc_ctype_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_connection_limit}.
	 * @param ctx the parse tree
	 */
	void enterOpt_connection_limit(CockroachDBParser.Opt_connection_limitContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_connection_limit}.
	 * @param ctx the parse tree
	 */
	void exitOpt_connection_limit(CockroachDBParser.Opt_connection_limitContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_primary_region_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_primary_region_clause(CockroachDBParser.Opt_primary_region_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_primary_region_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_primary_region_clause(CockroachDBParser.Opt_primary_region_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_regions_list}.
	 * @param ctx the parse tree
	 */
	void enterOpt_regions_list(CockroachDBParser.Opt_regions_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_regions_list}.
	 * @param ctx the parse tree
	 */
	void exitOpt_regions_list(CockroachDBParser.Opt_regions_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_survival_goal_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_survival_goal_clause(CockroachDBParser.Opt_survival_goal_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_survival_goal_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_survival_goal_clause(CockroachDBParser.Opt_survival_goal_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_placement_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_placement_clause(CockroachDBParser.Opt_placement_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_placement_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_placement_clause(CockroachDBParser.Opt_placement_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_owner_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_owner_clause(CockroachDBParser.Opt_owner_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_owner_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_owner_clause(CockroachDBParser.Opt_owner_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_super_region_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_super_region_clause(CockroachDBParser.Opt_super_region_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_super_region_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_super_region_clause(CockroachDBParser.Opt_super_region_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#super_region_clause}.
	 * @param ctx the parse tree
	 */
	void enterSuper_region_clause(CockroachDBParser.Super_region_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#super_region_clause}.
	 * @param ctx the parse tree
	 */
	void exitSuper_region_clause(CockroachDBParser.Super_region_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_secondary_region_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_secondary_region_clause(CockroachDBParser.Opt_secondary_region_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_secondary_region_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_secondary_region_clause(CockroachDBParser.Opt_secondary_region_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_index_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_index_stmt(CockroachDBParser.Create_index_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_index_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_index_stmt(CockroachDBParser.Create_index_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_index_access_method}.
	 * @param ctx the parse tree
	 */
	void enterOpt_index_access_method(CockroachDBParser.Opt_index_access_methodContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_index_access_method}.
	 * @param ctx the parse tree
	 */
	void exitOpt_index_access_method(CockroachDBParser.Opt_index_access_methodContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_schema_stmt(CockroachDBParser.Create_schema_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_schema_stmt(CockroachDBParser.Create_schema_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_schema_name}.
	 * @param ctx the parse tree
	 */
	void enterOpt_schema_name(CockroachDBParser.Opt_schema_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_schema_name}.
	 * @param ctx the parse tree
	 */
	void exitOpt_schema_name(CockroachDBParser.Opt_schema_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_stmt(CockroachDBParser.Create_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_stmt(CockroachDBParser.Create_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_table_with}.
	 * @param ctx the parse tree
	 */
	void enterOpt_table_with(CockroachDBParser.Opt_table_withContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_table_with}.
	 * @param ctx the parse tree
	 */
	void exitOpt_table_with(CockroachDBParser.Opt_table_withContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_create_table_on_commit}.
	 * @param ctx the parse tree
	 */
	void enterOpt_create_table_on_commit(CockroachDBParser.Opt_create_table_on_commitContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_create_table_on_commit}.
	 * @param ctx the parse tree
	 */
	void exitOpt_create_table_on_commit(CockroachDBParser.Opt_create_table_on_commitContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_persistence_temp_table}.
	 * @param ctx the parse tree
	 */
	void enterOpt_persistence_temp_table(CockroachDBParser.Opt_persistence_temp_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_persistence_temp_table}.
	 * @param ctx the parse tree
	 */
	void exitOpt_persistence_temp_table(CockroachDBParser.Opt_persistence_temp_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_table_elem_list}.
	 * @param ctx the parse tree
	 */
	void enterOpt_table_elem_list(CockroachDBParser.Opt_table_elem_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_table_elem_list}.
	 * @param ctx the parse tree
	 */
	void exitOpt_table_elem_list(CockroachDBParser.Opt_table_elem_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_elem_list}.
	 * @param ctx the parse tree
	 */
	void enterTable_elem_list(CockroachDBParser.Table_elem_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_elem_list}.
	 * @param ctx the parse tree
	 */
	void exitTable_elem_list(CockroachDBParser.Table_elem_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_elem}.
	 * @param ctx the parse tree
	 */
	void enterTable_elem(CockroachDBParser.Table_elemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_elem}.
	 * @param ctx the parse tree
	 */
	void exitTable_elem(CockroachDBParser.Table_elemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#index_def}.
	 * @param ctx the parse tree
	 */
	void enterIndex_def(CockroachDBParser.Index_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#index_def}.
	 * @param ctx the parse tree
	 */
	void exitIndex_def(CockroachDBParser.Index_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_index_visible}.
	 * @param ctx the parse tree
	 */
	void enterOpt_index_visible(CockroachDBParser.Opt_index_visibleContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_index_visible}.
	 * @param ctx the parse tree
	 */
	void exitOpt_index_visible(CockroachDBParser.Opt_index_visibleContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#family_def}.
	 * @param ctx the parse tree
	 */
	void enterFamily_def(CockroachDBParser.Family_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#family_def}.
	 * @param ctx the parse tree
	 */
	void exitFamily_def(CockroachDBParser.Family_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#like_table_option_list}.
	 * @param ctx the parse tree
	 */
	void enterLike_table_option_list(CockroachDBParser.Like_table_option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#like_table_option_list}.
	 * @param ctx the parse tree
	 */
	void exitLike_table_option_list(CockroachDBParser.Like_table_option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#like_table_option}.
	 * @param ctx the parse tree
	 */
	void enterLike_table_option(CockroachDBParser.Like_table_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#like_table_option}.
	 * @param ctx the parse tree
	 */
	void exitLike_table_option(CockroachDBParser.Like_table_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_table_as_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_as_stmt(CockroachDBParser.Create_table_as_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_table_as_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_as_stmt(CockroachDBParser.Create_table_as_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_as_opt_col_list}.
	 * @param ctx the parse tree
	 */
	void enterCreate_as_opt_col_list(CockroachDBParser.Create_as_opt_col_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_as_opt_col_list}.
	 * @param ctx the parse tree
	 */
	void exitCreate_as_opt_col_list(CockroachDBParser.Create_as_opt_col_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_as_table_defs}.
	 * @param ctx the parse tree
	 */
	void enterCreate_as_table_defs(CockroachDBParser.Create_as_table_defsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_as_table_defs}.
	 * @param ctx the parse tree
	 */
	void exitCreate_as_table_defs(CockroachDBParser.Create_as_table_defsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_as_col_qual_list}.
	 * @param ctx the parse tree
	 */
	void enterCreate_as_col_qual_list(CockroachDBParser.Create_as_col_qual_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_as_col_qual_list}.
	 * @param ctx the parse tree
	 */
	void exitCreate_as_col_qual_list(CockroachDBParser.Create_as_col_qual_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_as_col_qualification}.
	 * @param ctx the parse tree
	 */
	void enterCreate_as_col_qualification(CockroachDBParser.Create_as_col_qualificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_as_col_qualification}.
	 * @param ctx the parse tree
	 */
	void exitCreate_as_col_qualification(CockroachDBParser.Create_as_col_qualificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_as_col_qualification_elem}.
	 * @param ctx the parse tree
	 */
	void enterCreate_as_col_qualification_elem(CockroachDBParser.Create_as_col_qualification_elemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_as_col_qualification_elem}.
	 * @param ctx the parse tree
	 */
	void exitCreate_as_col_qualification_elem(CockroachDBParser.Create_as_col_qualification_elemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_as_constraint_def}.
	 * @param ctx the parse tree
	 */
	void enterCreate_as_constraint_def(CockroachDBParser.Create_as_constraint_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_as_constraint_def}.
	 * @param ctx the parse tree
	 */
	void exitCreate_as_constraint_def(CockroachDBParser.Create_as_constraint_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_as_constraint_elem}.
	 * @param ctx the parse tree
	 */
	void enterCreate_as_constraint_elem(CockroachDBParser.Create_as_constraint_elemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_as_constraint_elem}.
	 * @param ctx the parse tree
	 */
	void exitCreate_as_constraint_elem(CockroachDBParser.Create_as_constraint_elemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_as_params}.
	 * @param ctx the parse tree
	 */
	void enterCreate_as_params(CockroachDBParser.Create_as_paramsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_as_params}.
	 * @param ctx the parse tree
	 */
	void exitCreate_as_params(CockroachDBParser.Create_as_paramsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_as_param}.
	 * @param ctx the parse tree
	 */
	void enterCreate_as_param(CockroachDBParser.Create_as_paramContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_as_param}.
	 * @param ctx the parse tree
	 */
	void exitCreate_as_param(CockroachDBParser.Create_as_paramContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_type_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_type_stmt(CockroachDBParser.Create_type_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_type_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_type_stmt(CockroachDBParser.Create_type_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_enum_val_list}.
	 * @param ctx the parse tree
	 */
	void enterOpt_enum_val_list(CockroachDBParser.Opt_enum_val_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_enum_val_list}.
	 * @param ctx the parse tree
	 */
	void exitOpt_enum_val_list(CockroachDBParser.Opt_enum_val_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#enum_val_list}.
	 * @param ctx the parse tree
	 */
	void enterEnum_val_list(CockroachDBParser.Enum_val_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#enum_val_list}.
	 * @param ctx the parse tree
	 */
	void exitEnum_val_list(CockroachDBParser.Enum_val_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_composite_type_list}.
	 * @param ctx the parse tree
	 */
	void enterOpt_composite_type_list(CockroachDBParser.Opt_composite_type_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_composite_type_list}.
	 * @param ctx the parse tree
	 */
	void exitOpt_composite_type_list(CockroachDBParser.Opt_composite_type_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#composite_type_list}.
	 * @param ctx the parse tree
	 */
	void enterComposite_type_list(CockroachDBParser.Composite_type_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#composite_type_list}.
	 * @param ctx the parse tree
	 */
	void exitComposite_type_list(CockroachDBParser.Composite_type_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_view_stmt(CockroachDBParser.Create_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_view_stmt(CockroachDBParser.Create_view_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_with_data}.
	 * @param ctx the parse tree
	 */
	void enterOpt_with_data(CockroachDBParser.Opt_with_dataContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_with_data}.
	 * @param ctx the parse tree
	 */
	void exitOpt_with_data(CockroachDBParser.Opt_with_dataContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_sequence_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_sequence_stmt(CockroachDBParser.Create_sequence_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_sequence_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_sequence_stmt(CockroachDBParser.Create_sequence_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_temp}.
	 * @param ctx the parse tree
	 */
	void enterOpt_temp(CockroachDBParser.Opt_tempContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_temp}.
	 * @param ctx the parse tree
	 */
	void exitOpt_temp(CockroachDBParser.Opt_tempContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_func_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_func_stmt(CockroachDBParser.Create_func_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_func_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_func_stmt(CockroachDBParser.Create_func_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_return_set}.
	 * @param ctx the parse tree
	 */
	void enterOpt_return_set(CockroachDBParser.Opt_return_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_return_set}.
	 * @param ctx the parse tree
	 */
	void exitOpt_return_set(CockroachDBParser.Opt_return_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#routine_return_type}.
	 * @param ctx the parse tree
	 */
	void enterRoutine_return_type(CockroachDBParser.Routine_return_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#routine_return_type}.
	 * @param ctx the parse tree
	 */
	void exitRoutine_return_type(CockroachDBParser.Routine_return_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_func_column_list}.
	 * @param ctx the parse tree
	 */
	void enterTable_func_column_list(CockroachDBParser.Table_func_column_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_func_column_list}.
	 * @param ctx the parse tree
	 */
	void exitTable_func_column_list(CockroachDBParser.Table_func_column_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_func_column}.
	 * @param ctx the parse tree
	 */
	void enterTable_func_column(CockroachDBParser.Table_func_columnContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_func_column}.
	 * @param ctx the parse tree
	 */
	void exitTable_func_column(CockroachDBParser.Table_func_columnContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_proc_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_proc_stmt(CockroachDBParser.Create_proc_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_proc_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_proc_stmt(CockroachDBParser.Create_proc_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_routine_param_with_defaults_list}.
	 * @param ctx the parse tree
	 */
	void enterOpt_routine_param_with_defaults_list(CockroachDBParser.Opt_routine_param_with_defaults_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_routine_param_with_defaults_list}.
	 * @param ctx the parse tree
	 */
	void exitOpt_routine_param_with_defaults_list(CockroachDBParser.Opt_routine_param_with_defaults_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#routine_param_with_defaults_list}.
	 * @param ctx the parse tree
	 */
	void enterRoutine_param_with_defaults_list(CockroachDBParser.Routine_param_with_defaults_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#routine_param_with_defaults_list}.
	 * @param ctx the parse tree
	 */
	void exitRoutine_param_with_defaults_list(CockroachDBParser.Routine_param_with_defaults_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#routine_param_with_default}.
	 * @param ctx the parse tree
	 */
	void enterRoutine_param_with_default(CockroachDBParser.Routine_param_with_defaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#routine_param_with_default}.
	 * @param ctx the parse tree
	 */
	void exitRoutine_param_with_default(CockroachDBParser.Routine_param_with_defaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_create_routine_opt_list}.
	 * @param ctx the parse tree
	 */
	void enterOpt_create_routine_opt_list(CockroachDBParser.Opt_create_routine_opt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_create_routine_opt_list}.
	 * @param ctx the parse tree
	 */
	void exitOpt_create_routine_opt_list(CockroachDBParser.Opt_create_routine_opt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#routine_return_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRoutine_return_stmt(CockroachDBParser.Routine_return_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#routine_return_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRoutine_return_stmt(CockroachDBParser.Routine_return_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#routine_body_stmt_list}.
	 * @param ctx the parse tree
	 */
	void enterRoutine_body_stmt_list(CockroachDBParser.Routine_body_stmt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#routine_body_stmt_list}.
	 * @param ctx the parse tree
	 */
	void exitRoutine_body_stmt_list(CockroachDBParser.Routine_body_stmt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#routine_body_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRoutine_body_stmt(CockroachDBParser.Routine_body_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#routine_body_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRoutine_body_stmt(CockroachDBParser.Routine_body_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_routine_body}.
	 * @param ctx the parse tree
	 */
	void enterOpt_routine_body(CockroachDBParser.Opt_routine_bodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_routine_body}.
	 * @param ctx the parse tree
	 */
	void exitOpt_routine_body(CockroachDBParser.Opt_routine_bodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#or_replace}.
	 * @param ctx the parse tree
	 */
	void enterOr_replace(CockroachDBParser.Or_replaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#or_replace}.
	 * @param ctx the parse tree
	 */
	void exitOr_replace(CockroachDBParser.Or_replaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#routine_create_name}.
	 * @param ctx the parse tree
	 */
	void enterRoutine_create_name(CockroachDBParser.Routine_create_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#routine_create_name}.
	 * @param ctx the parse tree
	 */
	void exitRoutine_create_name(CockroachDBParser.Routine_create_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_trigger_stmt(CockroachDBParser.Create_trigger_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_trigger_stmt(CockroachDBParser.Create_trigger_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#trigger_action_time}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_action_time(CockroachDBParser.Trigger_action_timeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#trigger_action_time}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_action_time(CockroachDBParser.Trigger_action_timeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#trigger_event_list}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_event_list(CockroachDBParser.Trigger_event_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#trigger_event_list}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_event_list(CockroachDBParser.Trigger_event_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#trigger_event}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_event(CockroachDBParser.Trigger_eventContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#trigger_event}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_event(CockroachDBParser.Trigger_eventContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_trigger_transition_list}.
	 * @param ctx the parse tree
	 */
	void enterOpt_trigger_transition_list(CockroachDBParser.Opt_trigger_transition_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_trigger_transition_list}.
	 * @param ctx the parse tree
	 */
	void exitOpt_trigger_transition_list(CockroachDBParser.Opt_trigger_transition_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#trigger_transition_list}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_transition_list(CockroachDBParser.Trigger_transition_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#trigger_transition_list}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_transition_list(CockroachDBParser.Trigger_transition_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#trigger_transition}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_transition(CockroachDBParser.Trigger_transitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#trigger_transition}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_transition(CockroachDBParser.Trigger_transitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#transition_is_new}.
	 * @param ctx the parse tree
	 */
	void enterTransition_is_new(CockroachDBParser.Transition_is_newContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#transition_is_new}.
	 * @param ctx the parse tree
	 */
	void exitTransition_is_new(CockroachDBParser.Transition_is_newContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#transition_is_row}.
	 * @param ctx the parse tree
	 */
	void enterTransition_is_row(CockroachDBParser.Transition_is_rowContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#transition_is_row}.
	 * @param ctx the parse tree
	 */
	void exitTransition_is_row(CockroachDBParser.Transition_is_rowContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#trigger_for_each}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_for_each(CockroachDBParser.Trigger_for_eachContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#trigger_for_each}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_for_each(CockroachDBParser.Trigger_for_eachContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#trigger_for_type}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_for_type(CockroachDBParser.Trigger_for_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#trigger_for_type}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_for_type(CockroachDBParser.Trigger_for_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#trigger_when}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_when(CockroachDBParser.Trigger_whenContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#trigger_when}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_when(CockroachDBParser.Trigger_whenContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#function_or_procedure}.
	 * @param ctx the parse tree
	 */
	void enterFunction_or_procedure(CockroachDBParser.Function_or_procedureContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#function_or_procedure}.
	 * @param ctx the parse tree
	 */
	void exitFunction_or_procedure(CockroachDBParser.Function_or_procedureContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#trigger_func_args}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_func_args(CockroachDBParser.Trigger_func_argsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#trigger_func_args}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_func_args(CockroachDBParser.Trigger_func_argsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#trigger_func_arg}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_func_arg(CockroachDBParser.Trigger_func_argContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#trigger_func_arg}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_func_arg(CockroachDBParser.Trigger_func_argContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_policy_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_policy_stmt(CockroachDBParser.Create_policy_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_policy_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_policy_stmt(CockroachDBParser.Create_policy_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_policy_type}.
	 * @param ctx the parse tree
	 */
	void enterOpt_policy_type(CockroachDBParser.Opt_policy_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_policy_type}.
	 * @param ctx the parse tree
	 */
	void exitOpt_policy_type(CockroachDBParser.Opt_policy_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_policy_command}.
	 * @param ctx the parse tree
	 */
	void enterOpt_policy_command(CockroachDBParser.Opt_policy_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_policy_command}.
	 * @param ctx the parse tree
	 */
	void exitOpt_policy_command(CockroachDBParser.Opt_policy_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#create_role_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_role_stmt(CockroachDBParser.Create_role_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#create_role_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_role_stmt(CockroachDBParser.Create_role_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#if_not_exists}.
	 * @param ctx the parse tree
	 */
	void enterIf_not_exists(CockroachDBParser.If_not_existsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#if_not_exists}.
	 * @param ctx the parse tree
	 */
	void exitIf_not_exists(CockroachDBParser.If_not_existsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_role_options}.
	 * @param ctx the parse tree
	 */
	void enterOpt_role_options(CockroachDBParser.Opt_role_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_role_options}.
	 * @param ctx the parse tree
	 */
	void exitOpt_role_options(CockroachDBParser.Opt_role_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#role_options}.
	 * @param ctx the parse tree
	 */
	void enterRole_options(CockroachDBParser.Role_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#role_options}.
	 * @param ctx the parse tree
	 */
	void exitRole_options(CockroachDBParser.Role_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#role_option}.
	 * @param ctx the parse tree
	 */
	void enterRole_option(CockroachDBParser.Role_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#role_option}.
	 * @param ctx the parse tree
	 */
	void exitRole_option(CockroachDBParser.Role_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#password_clause}.
	 * @param ctx the parse tree
	 */
	void enterPassword_clause(CockroachDBParser.Password_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#password_clause}.
	 * @param ctx the parse tree
	 */
	void exitPassword_clause(CockroachDBParser.Password_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#valid_until_clause}.
	 * @param ctx the parse tree
	 */
	void enterValid_until_clause(CockroachDBParser.Valid_until_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#valid_until_clause}.
	 * @param ctx the parse tree
	 */
	void exitValid_until_clause(CockroachDBParser.Valid_until_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#provisionsrc_clause}.
	 * @param ctx the parse tree
	 */
	void enterProvisionsrc_clause(CockroachDBParser.Provisionsrc_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#provisionsrc_clause}.
	 * @param ctx the parse tree
	 */
	void exitProvisionsrc_clause(CockroachDBParser.Provisionsrc_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#check_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCheck_stmt(CockroachDBParser.Check_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#check_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCheck_stmt(CockroachDBParser.Check_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#check_external_connection_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCheck_external_connection_stmt(CockroachDBParser.Check_external_connection_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#check_external_connection_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCheck_external_connection_stmt(CockroachDBParser.Check_external_connection_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_with_check_external_connection_options_list}.
	 * @param ctx the parse tree
	 */
	void enterOpt_with_check_external_connection_options_list(CockroachDBParser.Opt_with_check_external_connection_options_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_with_check_external_connection_options_list}.
	 * @param ctx the parse tree
	 */
	void exitOpt_with_check_external_connection_options_list(CockroachDBParser.Opt_with_check_external_connection_options_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#check_external_connection_options_list}.
	 * @param ctx the parse tree
	 */
	void enterCheck_external_connection_options_list(CockroachDBParser.Check_external_connection_options_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#check_external_connection_options_list}.
	 * @param ctx the parse tree
	 */
	void exitCheck_external_connection_options_list(CockroachDBParser.Check_external_connection_options_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#check_external_connection_options}.
	 * @param ctx the parse tree
	 */
	void enterCheck_external_connection_options(CockroachDBParser.Check_external_connection_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#check_external_connection_options}.
	 * @param ctx the parse tree
	 */
	void exitCheck_external_connection_options(CockroachDBParser.Check_external_connection_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#delete_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDelete_stmt(CockroachDBParser.Delete_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#delete_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDelete_stmt(CockroachDBParser.Delete_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_with_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_with_clause(CockroachDBParser.Opt_with_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_with_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_with_clause(CockroachDBParser.Opt_with_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#with_clause}.
	 * @param ctx the parse tree
	 */
	void enterWith_clause(CockroachDBParser.With_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#with_clause}.
	 * @param ctx the parse tree
	 */
	void exitWith_clause(CockroachDBParser.With_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_batch_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_batch_clause(CockroachDBParser.Opt_batch_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_batch_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_batch_clause(CockroachDBParser.Opt_batch_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#batch_param_list}.
	 * @param ctx the parse tree
	 */
	void enterBatch_param_list(CockroachDBParser.Batch_param_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#batch_param_list}.
	 * @param ctx the parse tree
	 */
	void exitBatch_param_list(CockroachDBParser.Batch_param_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#batch_param}.
	 * @param ctx the parse tree
	 */
	void enterBatch_param(CockroachDBParser.Batch_paramContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#batch_param}.
	 * @param ctx the parse tree
	 */
	void exitBatch_param(CockroachDBParser.Batch_paramContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_using_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_using_clause(CockroachDBParser.Opt_using_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_using_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_using_clause(CockroachDBParser.Opt_using_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#cte_list}.
	 * @param ctx the parse tree
	 */
	void enterCte_list(CockroachDBParser.Cte_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#cte_list}.
	 * @param ctx the parse tree
	 */
	void exitCte_list(CockroachDBParser.Cte_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#common_table_expr}.
	 * @param ctx the parse tree
	 */
	void enterCommon_table_expr(CockroachDBParser.Common_table_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#common_table_expr}.
	 * @param ctx the parse tree
	 */
	void exitCommon_table_expr(CockroachDBParser.Common_table_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_col_def_list_no_types}.
	 * @param ctx the parse tree
	 */
	void enterOpt_col_def_list_no_types(CockroachDBParser.Opt_col_def_list_no_typesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_col_def_list_no_types}.
	 * @param ctx the parse tree
	 */
	void exitOpt_col_def_list_no_types(CockroachDBParser.Opt_col_def_list_no_typesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#col_def_list_no_types}.
	 * @param ctx the parse tree
	 */
	void enterCol_def_list_no_types(CockroachDBParser.Col_def_list_no_typesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#col_def_list_no_types}.
	 * @param ctx the parse tree
	 */
	void exitCol_def_list_no_types(CockroachDBParser.Col_def_list_no_typesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#materialized_clause}.
	 * @param ctx the parse tree
	 */
	void enterMaterialized_clause(CockroachDBParser.Materialized_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#materialized_clause}.
	 * @param ctx the parse tree
	 */
	void exitMaterialized_clause(CockroachDBParser.Materialized_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_stmt(CockroachDBParser.Drop_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_stmt(CockroachDBParser.Drop_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#explain_stmt}.
	 * @param ctx the parse tree
	 */
	void enterExplain_stmt(CockroachDBParser.Explain_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#explain_stmt}.
	 * @param ctx the parse tree
	 */
	void exitExplain_stmt(CockroachDBParser.Explain_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#explain_option_list}.
	 * @param ctx the parse tree
	 */
	void enterExplain_option_list(CockroachDBParser.Explain_option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#explain_option_list}.
	 * @param ctx the parse tree
	 */
	void exitExplain_option_list(CockroachDBParser.Explain_option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#explain_option_name}.
	 * @param ctx the parse tree
	 */
	void enterExplain_option_name(CockroachDBParser.Explain_option_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#explain_option_name}.
	 * @param ctx the parse tree
	 */
	void exitExplain_option_name(CockroachDBParser.Explain_option_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#explainable_stmt}.
	 * @param ctx the parse tree
	 */
	void enterExplainable_stmt(CockroachDBParser.Explainable_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#explainable_stmt}.
	 * @param ctx the parse tree
	 */
	void exitExplainable_stmt(CockroachDBParser.Explainable_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#import_stmt}.
	 * @param ctx the parse tree
	 */
	void enterImport_stmt(CockroachDBParser.Import_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#import_stmt}.
	 * @param ctx the parse tree
	 */
	void exitImport_stmt(CockroachDBParser.Import_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#insert_column_list}.
	 * @param ctx the parse tree
	 */
	void enterInsert_column_list(CockroachDBParser.Insert_column_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#insert_column_list}.
	 * @param ctx the parse tree
	 */
	void exitInsert_column_list(CockroachDBParser.Insert_column_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#insert_column_item}.
	 * @param ctx the parse tree
	 */
	void enterInsert_column_item(CockroachDBParser.Insert_column_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#insert_column_item}.
	 * @param ctx the parse tree
	 */
	void exitInsert_column_item(CockroachDBParser.Insert_column_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#import_format}.
	 * @param ctx the parse tree
	 */
	void enterImport_format(CockroachDBParser.Import_formatContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#import_format}.
	 * @param ctx the parse tree
	 */
	void exitImport_format(CockroachDBParser.Import_formatContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_with_options}.
	 * @param ctx the parse tree
	 */
	void enterOpt_with_options(CockroachDBParser.Opt_with_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_with_options}.
	 * @param ctx the parse tree
	 */
	void exitOpt_with_options(CockroachDBParser.Opt_with_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#kv_option_list}.
	 * @param ctx the parse tree
	 */
	void enterKv_option_list(CockroachDBParser.Kv_option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#kv_option_list}.
	 * @param ctx the parse tree
	 */
	void exitKv_option_list(CockroachDBParser.Kv_option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#kv_option}.
	 * @param ctx the parse tree
	 */
	void enterKv_option(CockroachDBParser.Kv_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#kv_option}.
	 * @param ctx the parse tree
	 */
	void exitKv_option(CockroachDBParser.Kv_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#insert_stmt}.
	 * @param ctx the parse tree
	 */
	void enterInsert_stmt(CockroachDBParser.Insert_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#insert_stmt}.
	 * @param ctx the parse tree
	 */
	void exitInsert_stmt(CockroachDBParser.Insert_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#insert_target}.
	 * @param ctx the parse tree
	 */
	void enterInsert_target(CockroachDBParser.Insert_targetContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#insert_target}.
	 * @param ctx the parse tree
	 */
	void exitInsert_target(CockroachDBParser.Insert_targetContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_name_opt_idx}.
	 * @param ctx the parse tree
	 */
	void enterTable_name_opt_idx(CockroachDBParser.Table_name_opt_idxContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_name_opt_idx}.
	 * @param ctx the parse tree
	 */
	void exitTable_name_opt_idx(CockroachDBParser.Table_name_opt_idxContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_only}.
	 * @param ctx the parse tree
	 */
	void enterOpt_only(CockroachDBParser.Opt_onlyContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_only}.
	 * @param ctx the parse tree
	 */
	void exitOpt_only(CockroachDBParser.Opt_onlyContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_index_flags}.
	 * @param ctx the parse tree
	 */
	void enterOpt_index_flags(CockroachDBParser.Opt_index_flagsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_index_flags}.
	 * @param ctx the parse tree
	 */
	void exitOpt_index_flags(CockroachDBParser.Opt_index_flagsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#index_flags_param_list}.
	 * @param ctx the parse tree
	 */
	void enterIndex_flags_param_list(CockroachDBParser.Index_flags_param_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#index_flags_param_list}.
	 * @param ctx the parse tree
	 */
	void exitIndex_flags_param_list(CockroachDBParser.Index_flags_param_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#index_flags_param}.
	 * @param ctx the parse tree
	 */
	void enterIndex_flags_param(CockroachDBParser.Index_flags_paramContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#index_flags_param}.
	 * @param ctx the parse tree
	 */
	void exitIndex_flags_param(CockroachDBParser.Index_flags_paramContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_descendant}.
	 * @param ctx the parse tree
	 */
	void enterOpt_descendant(CockroachDBParser.Opt_descendantContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_descendant}.
	 * @param ctx the parse tree
	 */
	void exitOpt_descendant(CockroachDBParser.Opt_descendantContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#insert_rest}.
	 * @param ctx the parse tree
	 */
	void enterInsert_rest(CockroachDBParser.Insert_restContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#insert_rest}.
	 * @param ctx the parse tree
	 */
	void exitInsert_rest(CockroachDBParser.Insert_restContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#on_conflict}.
	 * @param ctx the parse tree
	 */
	void enterOn_conflict(CockroachDBParser.On_conflictContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#on_conflict}.
	 * @param ctx the parse tree
	 */
	void exitOn_conflict(CockroachDBParser.On_conflictContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#set_clause_list}.
	 * @param ctx the parse tree
	 */
	void enterSet_clause_list(CockroachDBParser.Set_clause_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#set_clause_list}.
	 * @param ctx the parse tree
	 */
	void exitSet_clause_list(CockroachDBParser.Set_clause_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#set_clause}.
	 * @param ctx the parse tree
	 */
	void enterSet_clause(CockroachDBParser.Set_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#set_clause}.
	 * @param ctx the parse tree
	 */
	void exitSet_clause(CockroachDBParser.Set_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#single_set_clause}.
	 * @param ctx the parse tree
	 */
	void enterSingle_set_clause(CockroachDBParser.Single_set_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#single_set_clause}.
	 * @param ctx the parse tree
	 */
	void exitSingle_set_clause(CockroachDBParser.Single_set_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#multiple_set_clause}.
	 * @param ctx the parse tree
	 */
	void enterMultiple_set_clause(CockroachDBParser.Multiple_set_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#multiple_set_clause}.
	 * @param ctx the parse tree
	 */
	void exitMultiple_set_clause(CockroachDBParser.Multiple_set_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#returning_clause}.
	 * @param ctx the parse tree
	 */
	void enterReturning_clause(CockroachDBParser.Returning_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#returning_clause}.
	 * @param ctx the parse tree
	 */
	void exitReturning_clause(CockroachDBParser.Returning_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#target_list}.
	 * @param ctx the parse tree
	 */
	void enterTarget_list(CockroachDBParser.Target_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#target_list}.
	 * @param ctx the parse tree
	 */
	void exitTarget_list(CockroachDBParser.Target_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#target_elem}.
	 * @param ctx the parse tree
	 */
	void enterTarget_elem(CockroachDBParser.Target_elemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#target_elem}.
	 * @param ctx the parse tree
	 */
	void exitTarget_elem(CockroachDBParser.Target_elemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#target_name}.
	 * @param ctx the parse tree
	 */
	void enterTarget_name(CockroachDBParser.Target_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#target_name}.
	 * @param ctx the parse tree
	 */
	void exitTarget_name(CockroachDBParser.Target_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#bare_col_label}.
	 * @param ctx the parse tree
	 */
	void enterBare_col_label(CockroachDBParser.Bare_col_labelContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#bare_col_label}.
	 * @param ctx the parse tree
	 */
	void exitBare_col_label(CockroachDBParser.Bare_col_labelContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#bare_label_keywords}.
	 * @param ctx the parse tree
	 */
	void enterBare_label_keywords(CockroachDBParser.Bare_label_keywordsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#bare_label_keywords}.
	 * @param ctx the parse tree
	 */
	void exitBare_label_keywords(CockroachDBParser.Bare_label_keywordsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#inspect_stmt}.
	 * @param ctx the parse tree
	 */
	void enterInspect_stmt(CockroachDBParser.Inspect_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#inspect_stmt}.
	 * @param ctx the parse tree
	 */
	void exitInspect_stmt(CockroachDBParser.Inspect_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#inspect_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterInspect_table_stmt(CockroachDBParser.Inspect_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#inspect_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitInspect_table_stmt(CockroachDBParser.Inspect_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_inspect_options_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_inspect_options_clause(CockroachDBParser.Opt_inspect_options_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_inspect_options_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_inspect_options_clause(CockroachDBParser.Opt_inspect_options_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#inspect_option_list}.
	 * @param ctx the parse tree
	 */
	void enterInspect_option_list(CockroachDBParser.Inspect_option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#inspect_option_list}.
	 * @param ctx the parse tree
	 */
	void exitInspect_option_list(CockroachDBParser.Inspect_option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#inspect_option}.
	 * @param ctx the parse tree
	 */
	void enterInspect_option(CockroachDBParser.Inspect_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#inspect_option}.
	 * @param ctx the parse tree
	 */
	void exitInspect_option(CockroachDBParser.Inspect_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#inspect_database_stmt}.
	 * @param ctx the parse tree
	 */
	void enterInspect_database_stmt(CockroachDBParser.Inspect_database_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#inspect_database_stmt}.
	 * @param ctx the parse tree
	 */
	void exitInspect_database_stmt(CockroachDBParser.Inspect_database_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#pause_stmt}.
	 * @param ctx the parse tree
	 */
	void enterPause_stmt(CockroachDBParser.Pause_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#pause_stmt}.
	 * @param ctx the parse tree
	 */
	void exitPause_stmt(CockroachDBParser.Pause_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#pause_jobs_stmt}.
	 * @param ctx the parse tree
	 */
	void enterPause_jobs_stmt(CockroachDBParser.Pause_jobs_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#pause_jobs_stmt}.
	 * @param ctx the parse tree
	 */
	void exitPause_jobs_stmt(CockroachDBParser.Pause_jobs_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#with_reason}.
	 * @param ctx the parse tree
	 */
	void enterWith_reason(CockroachDBParser.With_reasonContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#with_reason}.
	 * @param ctx the parse tree
	 */
	void exitWith_reason(CockroachDBParser.With_reasonContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#pause_schedules_stmt}.
	 * @param ctx the parse tree
	 */
	void enterPause_schedules_stmt(CockroachDBParser.Pause_schedules_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#pause_schedules_stmt}.
	 * @param ctx the parse tree
	 */
	void exitPause_schedules_stmt(CockroachDBParser.Pause_schedules_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#pause_all_jobs_stmt}.
	 * @param ctx the parse tree
	 */
	void enterPause_all_jobs_stmt(CockroachDBParser.Pause_all_jobs_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#pause_all_jobs_stmt}.
	 * @param ctx the parse tree
	 */
	void exitPause_all_jobs_stmt(CockroachDBParser.Pause_all_jobs_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#reset_stmt}.
	 * @param ctx the parse tree
	 */
	void enterReset_stmt(CockroachDBParser.Reset_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#reset_stmt}.
	 * @param ctx the parse tree
	 */
	void exitReset_stmt(CockroachDBParser.Reset_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#reset_session_stmt}.
	 * @param ctx the parse tree
	 */
	void enterReset_session_stmt(CockroachDBParser.Reset_session_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#reset_session_stmt}.
	 * @param ctx the parse tree
	 */
	void exitReset_session_stmt(CockroachDBParser.Reset_session_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#session_var}.
	 * @param ctx the parse tree
	 */
	void enterSession_var(CockroachDBParser.Session_varContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#session_var}.
	 * @param ctx the parse tree
	 */
	void exitSession_var(CockroachDBParser.Session_varContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#session_var_parts}.
	 * @param ctx the parse tree
	 */
	void enterSession_var_parts(CockroachDBParser.Session_var_partsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#session_var_parts}.
	 * @param ctx the parse tree
	 */
	void exitSession_var_parts(CockroachDBParser.Session_var_partsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#reset_csetting_stmt}.
	 * @param ctx the parse tree
	 */
	void enterReset_csetting_stmt(CockroachDBParser.Reset_csetting_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#reset_csetting_stmt}.
	 * @param ctx the parse tree
	 */
	void exitReset_csetting_stmt(CockroachDBParser.Reset_csetting_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#var_name}.
	 * @param ctx the parse tree
	 */
	void enterVar_name(CockroachDBParser.Var_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#var_name}.
	 * @param ctx the parse tree
	 */
	void exitVar_name(CockroachDBParser.Var_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#attrs}.
	 * @param ctx the parse tree
	 */
	void enterAttrs(CockroachDBParser.AttrsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#attrs}.
	 * @param ctx the parse tree
	 */
	void exitAttrs(CockroachDBParser.AttrsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#restore_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRestore_stmt(CockroachDBParser.Restore_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#restore_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRestore_stmt(CockroachDBParser.Restore_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_with_restore_options}.
	 * @param ctx the parse tree
	 */
	void enterOpt_with_restore_options(CockroachDBParser.Opt_with_restore_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_with_restore_options}.
	 * @param ctx the parse tree
	 */
	void exitOpt_with_restore_options(CockroachDBParser.Opt_with_restore_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#restore_options_list}.
	 * @param ctx the parse tree
	 */
	void enterRestore_options_list(CockroachDBParser.Restore_options_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#restore_options_list}.
	 * @param ctx the parse tree
	 */
	void exitRestore_options_list(CockroachDBParser.Restore_options_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#restore_options}.
	 * @param ctx the parse tree
	 */
	void enterRestore_options(CockroachDBParser.Restore_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#restore_options}.
	 * @param ctx the parse tree
	 */
	void exitRestore_options(CockroachDBParser.Restore_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#resume_stmt}.
	 * @param ctx the parse tree
	 */
	void enterResume_stmt(CockroachDBParser.Resume_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#resume_stmt}.
	 * @param ctx the parse tree
	 */
	void exitResume_stmt(CockroachDBParser.Resume_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#resume_jobs_stmt}.
	 * @param ctx the parse tree
	 */
	void enterResume_jobs_stmt(CockroachDBParser.Resume_jobs_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#resume_jobs_stmt}.
	 * @param ctx the parse tree
	 */
	void exitResume_jobs_stmt(CockroachDBParser.Resume_jobs_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#for_schedules_clause}.
	 * @param ctx the parse tree
	 */
	void enterFor_schedules_clause(CockroachDBParser.For_schedules_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#for_schedules_clause}.
	 * @param ctx the parse tree
	 */
	void exitFor_schedules_clause(CockroachDBParser.For_schedules_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#resume_schedules_stmt}.
	 * @param ctx the parse tree
	 */
	void enterResume_schedules_stmt(CockroachDBParser.Resume_schedules_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#resume_schedules_stmt}.
	 * @param ctx the parse tree
	 */
	void exitResume_schedules_stmt(CockroachDBParser.Resume_schedules_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#resume_all_jobs_stmt}.
	 * @param ctx the parse tree
	 */
	void enterResume_all_jobs_stmt(CockroachDBParser.Resume_all_jobs_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#resume_all_jobs_stmt}.
	 * @param ctx the parse tree
	 */
	void exitResume_all_jobs_stmt(CockroachDBParser.Resume_all_jobs_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#export_stmt}.
	 * @param ctx the parse tree
	 */
	void enterExport_stmt(CockroachDBParser.Export_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#export_stmt}.
	 * @param ctx the parse tree
	 */
	void exitExport_stmt(CockroachDBParser.Export_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#scrub_stmt}.
	 * @param ctx the parse tree
	 */
	void enterScrub_stmt(CockroachDBParser.Scrub_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#scrub_stmt}.
	 * @param ctx the parse tree
	 */
	void exitScrub_stmt(CockroachDBParser.Scrub_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#scrub_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterScrub_table_stmt(CockroachDBParser.Scrub_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#scrub_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitScrub_table_stmt(CockroachDBParser.Scrub_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_scrub_options_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_scrub_options_clause(CockroachDBParser.Opt_scrub_options_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_scrub_options_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_scrub_options_clause(CockroachDBParser.Opt_scrub_options_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#scrub_option_list}.
	 * @param ctx the parse tree
	 */
	void enterScrub_option_list(CockroachDBParser.Scrub_option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#scrub_option_list}.
	 * @param ctx the parse tree
	 */
	void exitScrub_option_list(CockroachDBParser.Scrub_option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#scrub_option}.
	 * @param ctx the parse tree
	 */
	void enterScrub_option(CockroachDBParser.Scrub_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#scrub_option}.
	 * @param ctx the parse tree
	 */
	void exitScrub_option(CockroachDBParser.Scrub_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#scrub_database_stmt}.
	 * @param ctx the parse tree
	 */
	void enterScrub_database_stmt(CockroachDBParser.Scrub_database_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#scrub_database_stmt}.
	 * @param ctx the parse tree
	 */
	void exitScrub_database_stmt(CockroachDBParser.Scrub_database_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_ddl_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_ddl_stmt(CockroachDBParser.Drop_ddl_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_ddl_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_ddl_stmt(CockroachDBParser.Drop_ddl_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_role_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_role_stmt(CockroachDBParser.Drop_role_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_role_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_role_stmt(CockroachDBParser.Drop_role_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#if_exists}.
	 * @param ctx the parse tree
	 */
	void enterIf_exists(CockroachDBParser.If_existsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#if_exists}.
	 * @param ctx the parse tree
	 */
	void exitIf_exists(CockroachDBParser.If_existsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#role_or_group_or_user}.
	 * @param ctx the parse tree
	 */
	void enterRole_or_group_or_user(CockroachDBParser.Role_or_group_or_userContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#role_or_group_or_user}.
	 * @param ctx the parse tree
	 */
	void exitRole_or_group_or_user(CockroachDBParser.Role_or_group_or_userContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_schedule_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_schedule_stmt(CockroachDBParser.Drop_schedule_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_schedule_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_schedule_stmt(CockroachDBParser.Drop_schedule_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_external_connection_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_external_connection_stmt(CockroachDBParser.Drop_external_connection_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_external_connection_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_external_connection_stmt(CockroachDBParser.Drop_external_connection_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_database_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_database_stmt(CockroachDBParser.Drop_database_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_database_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_database_stmt(CockroachDBParser.Drop_database_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_index_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_index_stmt(CockroachDBParser.Drop_index_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_index_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_index_stmt(CockroachDBParser.Drop_index_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_index_name_list}.
	 * @param ctx the parse tree
	 */
	void enterTable_index_name_list(CockroachDBParser.Table_index_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_index_name_list}.
	 * @param ctx the parse tree
	 */
	void exitTable_index_name_list(CockroachDBParser.Table_index_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_table_stmt(CockroachDBParser.Drop_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_table_stmt(CockroachDBParser.Drop_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_name_list}.
	 * @param ctx the parse tree
	 */
	void enterTable_name_list(CockroachDBParser.Table_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_name_list}.
	 * @param ctx the parse tree
	 */
	void exitTable_name_list(CockroachDBParser.Table_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#db_object_name_list}.
	 * @param ctx the parse tree
	 */
	void enterDb_object_name_list(CockroachDBParser.Db_object_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#db_object_name_list}.
	 * @param ctx the parse tree
	 */
	void exitDb_object_name_list(CockroachDBParser.Db_object_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_view_stmt(CockroachDBParser.Drop_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_view_stmt(CockroachDBParser.Drop_view_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#view_name_list}.
	 * @param ctx the parse tree
	 */
	void enterView_name_list(CockroachDBParser.View_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#view_name_list}.
	 * @param ctx the parse tree
	 */
	void exitView_name_list(CockroachDBParser.View_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_sequence_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_sequence_stmt(CockroachDBParser.Drop_sequence_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_sequence_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_sequence_stmt(CockroachDBParser.Drop_sequence_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#sequence_name_list}.
	 * @param ctx the parse tree
	 */
	void enterSequence_name_list(CockroachDBParser.Sequence_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#sequence_name_list}.
	 * @param ctx the parse tree
	 */
	void exitSequence_name_list(CockroachDBParser.Sequence_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_schema_stmt(CockroachDBParser.Drop_schema_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_schema_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_schema_stmt(CockroachDBParser.Drop_schema_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_type_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_type_stmt(CockroachDBParser.Drop_type_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_type_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_type_stmt(CockroachDBParser.Drop_type_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_func_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_func_stmt(CockroachDBParser.Drop_func_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_func_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_func_stmt(CockroachDBParser.Drop_func_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_proc_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_proc_stmt(CockroachDBParser.Drop_proc_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_proc_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_proc_stmt(CockroachDBParser.Drop_proc_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_trigger_stmt(CockroachDBParser.Drop_trigger_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_trigger_stmt(CockroachDBParser.Drop_trigger_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#drop_policy_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_policy_stmt(CockroachDBParser.Drop_policy_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#drop_policy_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_policy_stmt(CockroachDBParser.Drop_policy_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#select_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSelect_stmt(CockroachDBParser.Select_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#select_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSelect_stmt(CockroachDBParser.Select_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#select_no_parens}.
	 * @param ctx the parse tree
	 */
	void enterSelect_no_parens(CockroachDBParser.Select_no_parensContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#select_no_parens}.
	 * @param ctx the parse tree
	 */
	void exitSelect_no_parens(CockroachDBParser.Select_no_parensContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#simple_select}.
	 * @param ctx the parse tree
	 */
	void enterSimple_select(CockroachDBParser.Simple_selectContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#simple_select}.
	 * @param ctx the parse tree
	 */
	void exitSimple_select(CockroachDBParser.Simple_selectContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#simple_select_clause}.
	 * @param ctx the parse tree
	 */
	void enterSimple_select_clause(CockroachDBParser.Simple_select_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#simple_select_clause}.
	 * @param ctx the parse tree
	 */
	void exitSimple_select_clause(CockroachDBParser.Simple_select_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_all_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_all_clause(CockroachDBParser.Opt_all_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_all_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_all_clause(CockroachDBParser.Opt_all_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#distinct_on_clause}.
	 * @param ctx the parse tree
	 */
	void enterDistinct_on_clause(CockroachDBParser.Distinct_on_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#distinct_on_clause}.
	 * @param ctx the parse tree
	 */
	void exitDistinct_on_clause(CockroachDBParser.Distinct_on_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#from_clause}.
	 * @param ctx the parse tree
	 */
	void enterFrom_clause(CockroachDBParser.From_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#from_clause}.
	 * @param ctx the parse tree
	 */
	void exitFrom_clause(CockroachDBParser.From_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#from_list}.
	 * @param ctx the parse tree
	 */
	void enterFrom_list(CockroachDBParser.From_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#from_list}.
	 * @param ctx the parse tree
	 */
	void exitFrom_list(CockroachDBParser.From_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#group_clause}.
	 * @param ctx the parse tree
	 */
	void enterGroup_clause(CockroachDBParser.Group_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#group_clause}.
	 * @param ctx the parse tree
	 */
	void exitGroup_clause(CockroachDBParser.Group_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#group_by_list}.
	 * @param ctx the parse tree
	 */
	void enterGroup_by_list(CockroachDBParser.Group_by_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#group_by_list}.
	 * @param ctx the parse tree
	 */
	void exitGroup_by_list(CockroachDBParser.Group_by_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#group_by_item}.
	 * @param ctx the parse tree
	 */
	void enterGroup_by_item(CockroachDBParser.Group_by_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#group_by_item}.
	 * @param ctx the parse tree
	 */
	void exitGroup_by_item(CockroachDBParser.Group_by_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#having_clause}.
	 * @param ctx the parse tree
	 */
	void enterHaving_clause(CockroachDBParser.Having_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#having_clause}.
	 * @param ctx the parse tree
	 */
	void exitHaving_clause(CockroachDBParser.Having_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#window_clause}.
	 * @param ctx the parse tree
	 */
	void enterWindow_clause(CockroachDBParser.Window_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#window_clause}.
	 * @param ctx the parse tree
	 */
	void exitWindow_clause(CockroachDBParser.Window_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#window_definition_list}.
	 * @param ctx the parse tree
	 */
	void enterWindow_definition_list(CockroachDBParser.Window_definition_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#window_definition_list}.
	 * @param ctx the parse tree
	 */
	void exitWindow_definition_list(CockroachDBParser.Window_definition_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#window_definition}.
	 * @param ctx the parse tree
	 */
	void enterWindow_definition(CockroachDBParser.Window_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#window_definition}.
	 * @param ctx the parse tree
	 */
	void exitWindow_definition(CockroachDBParser.Window_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#window_name}.
	 * @param ctx the parse tree
	 */
	void enterWindow_name(CockroachDBParser.Window_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#window_name}.
	 * @param ctx the parse tree
	 */
	void exitWindow_name(CockroachDBParser.Window_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#window_specification}.
	 * @param ctx the parse tree
	 */
	void enterWindow_specification(CockroachDBParser.Window_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#window_specification}.
	 * @param ctx the parse tree
	 */
	void exitWindow_specification(CockroachDBParser.Window_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_existing_window_name}.
	 * @param ctx the parse tree
	 */
	void enterOpt_existing_window_name(CockroachDBParser.Opt_existing_window_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_existing_window_name}.
	 * @param ctx the parse tree
	 */
	void exitOpt_existing_window_name(CockroachDBParser.Opt_existing_window_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_partition_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_partition_clause(CockroachDBParser.Opt_partition_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_partition_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_partition_clause(CockroachDBParser.Opt_partition_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_frame_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_frame_clause(CockroachDBParser.Opt_frame_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_frame_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_frame_clause(CockroachDBParser.Opt_frame_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#frame_extent}.
	 * @param ctx the parse tree
	 */
	void enterFrame_extent(CockroachDBParser.Frame_extentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#frame_extent}.
	 * @param ctx the parse tree
	 */
	void exitFrame_extent(CockroachDBParser.Frame_extentContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#frame_bound}.
	 * @param ctx the parse tree
	 */
	void enterFrame_bound(CockroachDBParser.Frame_boundContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#frame_bound}.
	 * @param ctx the parse tree
	 */
	void exitFrame_bound(CockroachDBParser.Frame_boundContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_frame_exclusion}.
	 * @param ctx the parse tree
	 */
	void enterOpt_frame_exclusion(CockroachDBParser.Opt_frame_exclusionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_frame_exclusion}.
	 * @param ctx the parse tree
	 */
	void exitOpt_frame_exclusion(CockroachDBParser.Opt_frame_exclusionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#values_clause}.
	 * @param ctx the parse tree
	 */
	void enterValues_clause(CockroachDBParser.Values_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#values_clause}.
	 * @param ctx the parse tree
	 */
	void exitValues_clause(CockroachDBParser.Values_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_clause}.
	 * @param ctx the parse tree
	 */
	void enterTable_clause(CockroachDBParser.Table_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_clause}.
	 * @param ctx the parse tree
	 */
	void exitTable_clause(CockroachDBParser.Table_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_ref}.
	 * @param ctx the parse tree
	 */
	void enterTable_ref(CockroachDBParser.Table_refContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_ref}.
	 * @param ctx the parse tree
	 */
	void exitTable_ref(CockroachDBParser.Table_refContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#row_source_extension_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRow_source_extension_stmt(CockroachDBParser.Row_source_extension_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#row_source_extension_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRow_source_extension_stmt(CockroachDBParser.Row_source_extension_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#func_table}.
	 * @param ctx the parse tree
	 */
	void enterFunc_table(CockroachDBParser.Func_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#func_table}.
	 * @param ctx the parse tree
	 */
	void exitFunc_table(CockroachDBParser.Func_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#func_expr_windowless}.
	 * @param ctx the parse tree
	 */
	void enterFunc_expr_windowless(CockroachDBParser.Func_expr_windowlessContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#func_expr_windowless}.
	 * @param ctx the parse tree
	 */
	void exitFunc_expr_windowless(CockroachDBParser.Func_expr_windowlessContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#func_expr_common_subexpr}.
	 * @param ctx the parse tree
	 */
	void enterFunc_expr_common_subexpr(CockroachDBParser.Func_expr_common_subexprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#func_expr_common_subexpr}.
	 * @param ctx the parse tree
	 */
	void exitFunc_expr_common_subexpr(CockroachDBParser.Func_expr_common_subexprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#cast_target}.
	 * @param ctx the parse tree
	 */
	void enterCast_target(CockroachDBParser.Cast_targetContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#cast_target}.
	 * @param ctx the parse tree
	 */
	void exitCast_target(CockroachDBParser.Cast_targetContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#special_function}.
	 * @param ctx the parse tree
	 */
	void enterSpecial_function(CockroachDBParser.Special_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#special_function}.
	 * @param ctx the parse tree
	 */
	void exitSpecial_function(CockroachDBParser.Special_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#extract_list}.
	 * @param ctx the parse tree
	 */
	void enterExtract_list(CockroachDBParser.Extract_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#extract_list}.
	 * @param ctx the parse tree
	 */
	void exitExtract_list(CockroachDBParser.Extract_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#extract_arg}.
	 * @param ctx the parse tree
	 */
	void enterExtract_arg(CockroachDBParser.Extract_argContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#extract_arg}.
	 * @param ctx the parse tree
	 */
	void exitExtract_arg(CockroachDBParser.Extract_argContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#overlay_list}.
	 * @param ctx the parse tree
	 */
	void enterOverlay_list(CockroachDBParser.Overlay_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#overlay_list}.
	 * @param ctx the parse tree
	 */
	void exitOverlay_list(CockroachDBParser.Overlay_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#position_list}.
	 * @param ctx the parse tree
	 */
	void enterPosition_list(CockroachDBParser.Position_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#position_list}.
	 * @param ctx the parse tree
	 */
	void exitPosition_list(CockroachDBParser.Position_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#substr_list}.
	 * @param ctx the parse tree
	 */
	void enterSubstr_list(CockroachDBParser.Substr_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#substr_list}.
	 * @param ctx the parse tree
	 */
	void exitSubstr_list(CockroachDBParser.Substr_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#trim_list}.
	 * @param ctx the parse tree
	 */
	void enterTrim_list(CockroachDBParser.Trim_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#trim_list}.
	 * @param ctx the parse tree
	 */
	void exitTrim_list(CockroachDBParser.Trim_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#overlay_placing}.
	 * @param ctx the parse tree
	 */
	void enterOverlay_placing(CockroachDBParser.Overlay_placingContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#overlay_placing}.
	 * @param ctx the parse tree
	 */
	void exitOverlay_placing(CockroachDBParser.Overlay_placingContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#substr_from}.
	 * @param ctx the parse tree
	 */
	void enterSubstr_from(CockroachDBParser.Substr_fromContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#substr_from}.
	 * @param ctx the parse tree
	 */
	void exitSubstr_from(CockroachDBParser.Substr_fromContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#substr_for}.
	 * @param ctx the parse tree
	 */
	void enterSubstr_for(CockroachDBParser.Substr_forContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#substr_for}.
	 * @param ctx the parse tree
	 */
	void exitSubstr_for(CockroachDBParser.Substr_forContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#rowsfrom_list}.
	 * @param ctx the parse tree
	 */
	void enterRowsfrom_list(CockroachDBParser.Rowsfrom_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#rowsfrom_list}.
	 * @param ctx the parse tree
	 */
	void exitRowsfrom_list(CockroachDBParser.Rowsfrom_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#rowsfrom_item}.
	 * @param ctx the parse tree
	 */
	void enterRowsfrom_item(CockroachDBParser.Rowsfrom_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#rowsfrom_item}.
	 * @param ctx the parse tree
	 */
	void exitRowsfrom_item(CockroachDBParser.Rowsfrom_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#join_type}.
	 * @param ctx the parse tree
	 */
	void enterJoin_type(CockroachDBParser.Join_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#join_type}.
	 * @param ctx the parse tree
	 */
	void exitJoin_type(CockroachDBParser.Join_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#join_outer}.
	 * @param ctx the parse tree
	 */
	void enterJoin_outer(CockroachDBParser.Join_outerContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#join_outer}.
	 * @param ctx the parse tree
	 */
	void exitJoin_outer(CockroachDBParser.Join_outerContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_join_hint}.
	 * @param ctx the parse tree
	 */
	void enterOpt_join_hint(CockroachDBParser.Opt_join_hintContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_join_hint}.
	 * @param ctx the parse tree
	 */
	void exitOpt_join_hint(CockroachDBParser.Opt_join_hintContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#join_equal}.
	 * @param ctx the parse tree
	 */
	void enterJoin_equal(CockroachDBParser.Join_equalContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#join_equal}.
	 * @param ctx the parse tree
	 */
	void exitJoin_equal(CockroachDBParser.Join_equalContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_ordinality}.
	 * @param ctx the parse tree
	 */
	void enterOpt_ordinality(CockroachDBParser.Opt_ordinalityContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_ordinality}.
	 * @param ctx the parse tree
	 */
	void exitOpt_ordinality(CockroachDBParser.Opt_ordinalityContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_alias_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_alias_clause(CockroachDBParser.Opt_alias_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_alias_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_alias_clause(CockroachDBParser.Opt_alias_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#alias_clause}.
	 * @param ctx the parse tree
	 */
	void enterAlias_clause(CockroachDBParser.Alias_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#alias_clause}.
	 * @param ctx the parse tree
	 */
	void exitAlias_clause(CockroachDBParser.Alias_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_func_alias_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_func_alias_clause(CockroachDBParser.Opt_func_alias_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_func_alias_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_func_alias_clause(CockroachDBParser.Opt_func_alias_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#func_alias_clause}.
	 * @param ctx the parse tree
	 */
	void enterFunc_alias_clause(CockroachDBParser.Func_alias_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#func_alias_clause}.
	 * @param ctx the parse tree
	 */
	void exitFunc_alias_clause(CockroachDBParser.Func_alias_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#col_def_list}.
	 * @param ctx the parse tree
	 */
	void enterCol_def_list(CockroachDBParser.Col_def_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#col_def_list}.
	 * @param ctx the parse tree
	 */
	void exitCol_def_list(CockroachDBParser.Col_def_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#col_def}.
	 * @param ctx the parse tree
	 */
	void enterCol_def(CockroachDBParser.Col_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#col_def}.
	 * @param ctx the parse tree
	 */
	void exitCol_def(CockroachDBParser.Col_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#all_or_distinct}.
	 * @param ctx the parse tree
	 */
	void enterAll_or_distinct(CockroachDBParser.All_or_distinctContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#all_or_distinct}.
	 * @param ctx the parse tree
	 */
	void exitAll_or_distinct(CockroachDBParser.All_or_distinctContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#select_limit}.
	 * @param ctx the parse tree
	 */
	void enterSelect_limit(CockroachDBParser.Select_limitContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#select_limit}.
	 * @param ctx the parse tree
	 */
	void exitSelect_limit(CockroachDBParser.Select_limitContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#limit_clause}.
	 * @param ctx the parse tree
	 */
	void enterLimit_clause(CockroachDBParser.Limit_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#limit_clause}.
	 * @param ctx the parse tree
	 */
	void exitLimit_clause(CockroachDBParser.Limit_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#first_or_next}.
	 * @param ctx the parse tree
	 */
	void enterFirst_or_next(CockroachDBParser.First_or_nextContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#first_or_next}.
	 * @param ctx the parse tree
	 */
	void exitFirst_or_next(CockroachDBParser.First_or_nextContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#select_fetch_first_value}.
	 * @param ctx the parse tree
	 */
	void enterSelect_fetch_first_value(CockroachDBParser.Select_fetch_first_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#select_fetch_first_value}.
	 * @param ctx the parse tree
	 */
	void exitSelect_fetch_first_value(CockroachDBParser.Select_fetch_first_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#row_or_rows}.
	 * @param ctx the parse tree
	 */
	void enterRow_or_rows(CockroachDBParser.Row_or_rowsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#row_or_rows}.
	 * @param ctx the parse tree
	 */
	void exitRow_or_rows(CockroachDBParser.Row_or_rowsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#offset_clause}.
	 * @param ctx the parse tree
	 */
	void enterOffset_clause(CockroachDBParser.Offset_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#offset_clause}.
	 * @param ctx the parse tree
	 */
	void exitOffset_clause(CockroachDBParser.Offset_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#sort_clause}.
	 * @param ctx the parse tree
	 */
	void enterSort_clause(CockroachDBParser.Sort_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#sort_clause}.
	 * @param ctx the parse tree
	 */
	void exitSort_clause(CockroachDBParser.Sort_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#sortby_list}.
	 * @param ctx the parse tree
	 */
	void enterSortby_list(CockroachDBParser.Sortby_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#sortby_list}.
	 * @param ctx the parse tree
	 */
	void exitSortby_list(CockroachDBParser.Sortby_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#select_with_parens}.
	 * @param ctx the parse tree
	 */
	void enterSelect_with_parens(CockroachDBParser.Select_with_parensContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#select_with_parens}.
	 * @param ctx the parse tree
	 */
	void exitSelect_with_parens(CockroachDBParser.Select_with_parensContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#for_locking_clause}.
	 * @param ctx the parse tree
	 */
	void enterFor_locking_clause(CockroachDBParser.For_locking_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#for_locking_clause}.
	 * @param ctx the parse tree
	 */
	void exitFor_locking_clause(CockroachDBParser.For_locking_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#for_locking_items}.
	 * @param ctx the parse tree
	 */
	void enterFor_locking_items(CockroachDBParser.For_locking_itemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#for_locking_items}.
	 * @param ctx the parse tree
	 */
	void exitFor_locking_items(CockroachDBParser.For_locking_itemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#for_locking_item}.
	 * @param ctx the parse tree
	 */
	void enterFor_locking_item(CockroachDBParser.For_locking_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#for_locking_item}.
	 * @param ctx the parse tree
	 */
	void exitFor_locking_item(CockroachDBParser.For_locking_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#for_locking_strength}.
	 * @param ctx the parse tree
	 */
	void enterFor_locking_strength(CockroachDBParser.For_locking_strengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#for_locking_strength}.
	 * @param ctx the parse tree
	 */
	void exitFor_locking_strength(CockroachDBParser.For_locking_strengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_locked_rels}.
	 * @param ctx the parse tree
	 */
	void enterOpt_locked_rels(CockroachDBParser.Opt_locked_relsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_locked_rels}.
	 * @param ctx the parse tree
	 */
	void exitOpt_locked_rels(CockroachDBParser.Opt_locked_relsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_nowait_or_skip}.
	 * @param ctx the parse tree
	 */
	void enterOpt_nowait_or_skip(CockroachDBParser.Opt_nowait_or_skipContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_nowait_or_skip}.
	 * @param ctx the parse tree
	 */
	void exitOpt_nowait_or_skip(CockroachDBParser.Opt_nowait_or_skipContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#preparable_set_stmt}.
	 * @param ctx the parse tree
	 */
	void enterPreparable_set_stmt(CockroachDBParser.Preparable_set_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#preparable_set_stmt}.
	 * @param ctx the parse tree
	 */
	void exitPreparable_set_stmt(CockroachDBParser.Preparable_set_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#set_session_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSet_session_stmt(CockroachDBParser.Set_session_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#set_session_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSet_session_stmt(CockroachDBParser.Set_session_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#set_rest_more}.
	 * @param ctx the parse tree
	 */
	void enterSet_rest_more(CockroachDBParser.Set_rest_moreContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#set_rest_more}.
	 * @param ctx the parse tree
	 */
	void exitSet_rest_more(CockroachDBParser.Set_rest_moreContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#set_rest}.
	 * @param ctx the parse tree
	 */
	void enterSet_rest(CockroachDBParser.Set_restContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#set_rest}.
	 * @param ctx the parse tree
	 */
	void exitSet_rest(CockroachDBParser.Set_restContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#generic_set}.
	 * @param ctx the parse tree
	 */
	void enterGeneric_set(CockroachDBParser.Generic_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#generic_set}.
	 * @param ctx the parse tree
	 */
	void exitGeneric_set(CockroachDBParser.Generic_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#var_list}.
	 * @param ctx the parse tree
	 */
	void enterVar_list(CockroachDBParser.Var_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#var_list}.
	 * @param ctx the parse tree
	 */
	void exitVar_list(CockroachDBParser.Var_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#var_value}.
	 * @param ctx the parse tree
	 */
	void enterVar_value(CockroachDBParser.Var_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#var_value}.
	 * @param ctx the parse tree
	 */
	void exitVar_value(CockroachDBParser.Var_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#extra_var_value}.
	 * @param ctx the parse tree
	 */
	void enterExtra_var_value(CockroachDBParser.Extra_var_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#extra_var_value}.
	 * @param ctx the parse tree
	 */
	void exitExtra_var_value(CockroachDBParser.Extra_var_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#to_or_eq}.
	 * @param ctx the parse tree
	 */
	void enterTo_or_eq(CockroachDBParser.To_or_eqContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#to_or_eq}.
	 * @param ctx the parse tree
	 */
	void exitTo_or_eq(CockroachDBParser.To_or_eqContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#set_local_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSet_local_stmt(CockroachDBParser.Set_local_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#set_local_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSet_local_stmt(CockroachDBParser.Set_local_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#set_csetting_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSet_csetting_stmt(CockroachDBParser.Set_csetting_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#set_csetting_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSet_csetting_stmt(CockroachDBParser.Set_csetting_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#use_stmt}.
	 * @param ctx the parse tree
	 */
	void enterUse_stmt(CockroachDBParser.Use_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#use_stmt}.
	 * @param ctx the parse tree
	 */
	void exitUse_stmt(CockroachDBParser.Use_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_stmt(CockroachDBParser.Show_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_stmt(CockroachDBParser.Show_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_backup_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_backup_stmt(CockroachDBParser.Show_backup_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_backup_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_backup_stmt(CockroachDBParser.Show_backup_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_with_show_backup_options}.
	 * @param ctx the parse tree
	 */
	void enterOpt_with_show_backup_options(CockroachDBParser.Opt_with_show_backup_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_with_show_backup_options}.
	 * @param ctx the parse tree
	 */
	void exitOpt_with_show_backup_options(CockroachDBParser.Opt_with_show_backup_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_backup_options_list}.
	 * @param ctx the parse tree
	 */
	void enterShow_backup_options_list(CockroachDBParser.Show_backup_options_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_backup_options_list}.
	 * @param ctx the parse tree
	 */
	void exitShow_backup_options_list(CockroachDBParser.Show_backup_options_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_backup_options}.
	 * @param ctx the parse tree
	 */
	void enterShow_backup_options(CockroachDBParser.Show_backup_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_backup_options}.
	 * @param ctx the parse tree
	 */
	void exitShow_backup_options(CockroachDBParser.Show_backup_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_columns_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_columns_stmt(CockroachDBParser.Show_columns_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_columns_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_columns_stmt(CockroachDBParser.Show_columns_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#with_comment}.
	 * @param ctx the parse tree
	 */
	void enterWith_comment(CockroachDBParser.With_commentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#with_comment}.
	 * @param ctx the parse tree
	 */
	void exitWith_comment(CockroachDBParser.With_commentContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_constraints_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_constraints_stmt(CockroachDBParser.Show_constraints_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_constraints_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_constraints_stmt(CockroachDBParser.Show_constraints_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_triggers_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_triggers_stmt(CockroachDBParser.Show_triggers_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_triggers_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_triggers_stmt(CockroachDBParser.Show_triggers_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_create_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_create_stmt(CockroachDBParser.Show_create_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_create_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_create_stmt(CockroachDBParser.Show_create_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_show_create_format_options}.
	 * @param ctx the parse tree
	 */
	void enterOpt_show_create_format_options(CockroachDBParser.Opt_show_create_format_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_show_create_format_options}.
	 * @param ctx the parse tree
	 */
	void exitOpt_show_create_format_options(CockroachDBParser.Opt_show_create_format_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_create_schedules_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_create_schedules_stmt(CockroachDBParser.Show_create_schedules_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_create_schedules_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_create_schedules_stmt(CockroachDBParser.Show_create_schedules_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_create_external_connections_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_create_external_connections_stmt(CockroachDBParser.Show_create_external_connections_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_create_external_connections_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_create_external_connections_stmt(CockroachDBParser.Show_create_external_connections_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_databases_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_databases_stmt(CockroachDBParser.Show_databases_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_databases_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_databases_stmt(CockroachDBParser.Show_databases_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_enums_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_enums_stmt(CockroachDBParser.Show_enums_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_enums_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_enums_stmt(CockroachDBParser.Show_enums_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_external_connections_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_external_connections_stmt(CockroachDBParser.Show_external_connections_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_external_connections_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_external_connections_stmt(CockroachDBParser.Show_external_connections_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_types_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_types_stmt(CockroachDBParser.Show_types_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_types_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_types_stmt(CockroachDBParser.Show_types_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_functions_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_functions_stmt(CockroachDBParser.Show_functions_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_functions_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_functions_stmt(CockroachDBParser.Show_functions_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_procedures_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_procedures_stmt(CockroachDBParser.Show_procedures_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_procedures_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_procedures_stmt(CockroachDBParser.Show_procedures_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_grants_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_grants_stmt(CockroachDBParser.Show_grants_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_grants_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_grants_stmt(CockroachDBParser.Show_grants_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_target_roles}.
	 * @param ctx the parse tree
	 */
	void enterOpt_target_roles(CockroachDBParser.Opt_target_rolesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_target_roles}.
	 * @param ctx the parse tree
	 */
	void exitOpt_target_roles(CockroachDBParser.Opt_target_rolesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#target_roles}.
	 * @param ctx the parse tree
	 */
	void enterTarget_roles(CockroachDBParser.Target_rolesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#target_roles}.
	 * @param ctx the parse tree
	 */
	void exitTarget_roles(CockroachDBParser.Target_rolesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#schema_wildcard}.
	 * @param ctx the parse tree
	 */
	void enterSchema_wildcard(CockroachDBParser.Schema_wildcardContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#schema_wildcard}.
	 * @param ctx the parse tree
	 */
	void exitSchema_wildcard(CockroachDBParser.Schema_wildcardContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#wildcard_pattern}.
	 * @param ctx the parse tree
	 */
	void enterWildcard_pattern(CockroachDBParser.Wildcard_patternContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#wildcard_pattern}.
	 * @param ctx the parse tree
	 */
	void exitWildcard_pattern(CockroachDBParser.Wildcard_patternContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#for_grantee_clause}.
	 * @param ctx the parse tree
	 */
	void enterFor_grantee_clause(CockroachDBParser.For_grantee_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#for_grantee_clause}.
	 * @param ctx the parse tree
	 */
	void exitFor_grantee_clause(CockroachDBParser.For_grantee_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_indexes_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_indexes_stmt(CockroachDBParser.Show_indexes_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_indexes_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_indexes_stmt(CockroachDBParser.Show_indexes_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_partitions_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_partitions_stmt(CockroachDBParser.Show_partitions_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_partitions_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_partitions_stmt(CockroachDBParser.Show_partitions_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_jobs_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_jobs_stmt(CockroachDBParser.Show_jobs_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_jobs_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_jobs_stmt(CockroachDBParser.Show_jobs_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_job_options_list}.
	 * @param ctx the parse tree
	 */
	void enterShow_job_options_list(CockroachDBParser.Show_job_options_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_job_options_list}.
	 * @param ctx the parse tree
	 */
	void exitShow_job_options_list(CockroachDBParser.Show_job_options_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_job_options}.
	 * @param ctx the parse tree
	 */
	void enterShow_job_options(CockroachDBParser.Show_job_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_job_options}.
	 * @param ctx the parse tree
	 */
	void exitShow_job_options(CockroachDBParser.Show_job_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_locality_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_locality_stmt(CockroachDBParser.Show_locality_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_locality_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_locality_stmt(CockroachDBParser.Show_locality_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_schedules_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_schedules_stmt(CockroachDBParser.Show_schedules_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_schedules_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_schedules_stmt(CockroachDBParser.Show_schedules_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#schedule_state}.
	 * @param ctx the parse tree
	 */
	void enterSchedule_state(CockroachDBParser.Schedule_stateContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#schedule_state}.
	 * @param ctx the parse tree
	 */
	void exitSchedule_state(CockroachDBParser.Schedule_stateContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_schedule_executor_type}.
	 * @param ctx the parse tree
	 */
	void enterOpt_schedule_executor_type(CockroachDBParser.Opt_schedule_executor_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_schedule_executor_type}.
	 * @param ctx the parse tree
	 */
	void exitOpt_schedule_executor_type(CockroachDBParser.Opt_schedule_executor_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_statements_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_statements_stmt(CockroachDBParser.Show_statements_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_statements_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_statements_stmt(CockroachDBParser.Show_statements_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_cluster}.
	 * @param ctx the parse tree
	 */
	void enterOpt_cluster(CockroachDBParser.Opt_clusterContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_cluster}.
	 * @param ctx the parse tree
	 */
	void exitOpt_cluster(CockroachDBParser.Opt_clusterContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#statements_or_queries}.
	 * @param ctx the parse tree
	 */
	void enterStatements_or_queries(CockroachDBParser.Statements_or_queriesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#statements_or_queries}.
	 * @param ctx the parse tree
	 */
	void exitStatements_or_queries(CockroachDBParser.Statements_or_queriesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_ranges_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_ranges_stmt(CockroachDBParser.Show_ranges_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_ranges_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_ranges_stmt(CockroachDBParser.Show_ranges_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_show_ranges_options}.
	 * @param ctx the parse tree
	 */
	void enterOpt_show_ranges_options(CockroachDBParser.Opt_show_ranges_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_show_ranges_options}.
	 * @param ctx the parse tree
	 */
	void exitOpt_show_ranges_options(CockroachDBParser.Opt_show_ranges_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_ranges_options}.
	 * @param ctx the parse tree
	 */
	void enterShow_ranges_options(CockroachDBParser.Show_ranges_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_ranges_options}.
	 * @param ctx the parse tree
	 */
	void exitShow_ranges_options(CockroachDBParser.Show_ranges_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_ranges_option}.
	 * @param ctx the parse tree
	 */
	void enterShow_ranges_option(CockroachDBParser.Show_ranges_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_ranges_option}.
	 * @param ctx the parse tree
	 */
	void exitShow_ranges_option(CockroachDBParser.Show_ranges_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_range_for_row_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_range_for_row_stmt(CockroachDBParser.Show_range_for_row_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_range_for_row_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_range_for_row_stmt(CockroachDBParser.Show_range_for_row_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_regions_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_regions_stmt(CockroachDBParser.Show_regions_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_regions_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_regions_stmt(CockroachDBParser.Show_regions_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_survival_goal_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_survival_goal_stmt(CockroachDBParser.Show_survival_goal_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_survival_goal_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_survival_goal_stmt(CockroachDBParser.Show_survival_goal_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_roles_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_roles_stmt(CockroachDBParser.Show_roles_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_roles_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_roles_stmt(CockroachDBParser.Show_roles_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_savepoint_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_savepoint_stmt(CockroachDBParser.Show_savepoint_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_savepoint_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_savepoint_stmt(CockroachDBParser.Show_savepoint_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_schemas_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_schemas_stmt(CockroachDBParser.Show_schemas_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_schemas_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_schemas_stmt(CockroachDBParser.Show_schemas_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_sequences_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_sequences_stmt(CockroachDBParser.Show_sequences_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_sequences_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_sequences_stmt(CockroachDBParser.Show_sequences_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_session_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_session_stmt(CockroachDBParser.Show_session_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_session_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_session_stmt(CockroachDBParser.Show_session_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_sessions_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_sessions_stmt(CockroachDBParser.Show_sessions_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_sessions_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_sessions_stmt(CockroachDBParser.Show_sessions_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_stats_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_stats_stmt(CockroachDBParser.Show_stats_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_stats_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_stats_stmt(CockroachDBParser.Show_stats_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_tables_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_tables_stmt(CockroachDBParser.Show_tables_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_tables_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_tables_stmt(CockroachDBParser.Show_tables_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_trace_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_trace_stmt(CockroachDBParser.Show_trace_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_trace_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_trace_stmt(CockroachDBParser.Show_trace_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_compact}.
	 * @param ctx the parse tree
	 */
	void enterOpt_compact(CockroachDBParser.Opt_compactContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_compact}.
	 * @param ctx the parse tree
	 */
	void exitOpt_compact(CockroachDBParser.Opt_compactContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_transactions_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_transactions_stmt(CockroachDBParser.Show_transactions_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_transactions_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_transactions_stmt(CockroachDBParser.Show_transactions_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_transfer_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_transfer_stmt(CockroachDBParser.Show_transfer_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_transfer_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_transfer_stmt(CockroachDBParser.Show_transfer_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_users_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_users_stmt(CockroachDBParser.Show_users_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_users_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_users_stmt(CockroachDBParser.Show_users_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_default_session_variables_for_role_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_default_session_variables_for_role_stmt(CockroachDBParser.Show_default_session_variables_for_role_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_default_session_variables_for_role_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_default_session_variables_for_role_stmt(CockroachDBParser.Show_default_session_variables_for_role_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_zone_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_zone_stmt(CockroachDBParser.Show_zone_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_zone_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_zone_stmt(CockroachDBParser.Show_zone_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#zone_name}.
	 * @param ctx the parse tree
	 */
	void enterZone_name(CockroachDBParser.Zone_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#zone_name}.
	 * @param ctx the parse tree
	 */
	void exitZone_name(CockroachDBParser.Zone_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_partition}.
	 * @param ctx the parse tree
	 */
	void enterOpt_partition(CockroachDBParser.Opt_partitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_partition}.
	 * @param ctx the parse tree
	 */
	void exitOpt_partition(CockroachDBParser.Opt_partitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#partition}.
	 * @param ctx the parse tree
	 */
	void enterPartition(CockroachDBParser.PartitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#partition}.
	 * @param ctx the parse tree
	 */
	void exitPartition(CockroachDBParser.PartitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#partition_name}.
	 * @param ctx the parse tree
	 */
	void enterPartition_name(CockroachDBParser.Partition_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#partition_name}.
	 * @param ctx the parse tree
	 */
	void exitPartition_name(CockroachDBParser.Partition_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_or_index}.
	 * @param ctx the parse tree
	 */
	void enterTable_or_index(CockroachDBParser.Table_or_indexContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_or_index}.
	 * @param ctx the parse tree
	 */
	void exitTable_or_index(CockroachDBParser.Table_or_indexContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_full_scans_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_full_scans_stmt(CockroachDBParser.Show_full_scans_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_full_scans_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_full_scans_stmt(CockroachDBParser.Show_full_scans_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_default_privileges_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_default_privileges_stmt(CockroachDBParser.Show_default_privileges_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_default_privileges_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_default_privileges_stmt(CockroachDBParser.Show_default_privileges_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_for_roles}.
	 * @param ctx the parse tree
	 */
	void enterOpt_for_roles(CockroachDBParser.Opt_for_rolesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_for_roles}.
	 * @param ctx the parse tree
	 */
	void exitOpt_for_roles(CockroachDBParser.Opt_for_rolesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_in_schema}.
	 * @param ctx the parse tree
	 */
	void enterOpt_in_schema(CockroachDBParser.Opt_in_schemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_in_schema}.
	 * @param ctx the parse tree
	 */
	void exitOpt_in_schema(CockroachDBParser.Opt_in_schemaContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#schema_name}.
	 * @param ctx the parse tree
	 */
	void enterSchema_name(CockroachDBParser.Schema_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#schema_name}.
	 * @param ctx the parse tree
	 */
	void exitSchema_name(CockroachDBParser.Schema_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#show_inspect_errors_stmt}.
	 * @param ctx the parse tree
	 */
	void enterShow_inspect_errors_stmt(CockroachDBParser.Show_inspect_errors_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#show_inspect_errors_stmt}.
	 * @param ctx the parse tree
	 */
	void exitShow_inspect_errors_stmt(CockroachDBParser.Show_inspect_errors_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_for_table_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_for_table_clause(CockroachDBParser.Opt_for_table_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_for_table_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_for_table_clause(CockroachDBParser.Opt_for_table_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_for_job_clause}.
	 * @param ctx the parse tree
	 */
	void enterOpt_for_job_clause(CockroachDBParser.Opt_for_job_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_for_job_clause}.
	 * @param ctx the parse tree
	 */
	void exitOpt_for_job_clause(CockroachDBParser.Opt_for_job_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_with_details}.
	 * @param ctx the parse tree
	 */
	void enterOpt_with_details(CockroachDBParser.Opt_with_detailsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_with_details}.
	 * @param ctx the parse tree
	 */
	void exitOpt_with_details(CockroachDBParser.Opt_with_detailsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#truncate_stmt}.
	 * @param ctx the parse tree
	 */
	void enterTruncate_stmt(CockroachDBParser.Truncate_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#truncate_stmt}.
	 * @param ctx the parse tree
	 */
	void exitTruncate_stmt(CockroachDBParser.Truncate_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#relation_expr_list}.
	 * @param ctx the parse tree
	 */
	void enterRelation_expr_list(CockroachDBParser.Relation_expr_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#relation_expr_list}.
	 * @param ctx the parse tree
	 */
	void exitRelation_expr_list(CockroachDBParser.Relation_expr_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#relation_expr}.
	 * @param ctx the parse tree
	 */
	void enterRelation_expr(CockroachDBParser.Relation_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#relation_expr}.
	 * @param ctx the parse tree
	 */
	void exitRelation_expr(CockroachDBParser.Relation_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#update_stmt}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_stmt(CockroachDBParser.Update_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#update_stmt}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_stmt(CockroachDBParser.Update_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_expr_opt_alias_idx}.
	 * @param ctx the parse tree
	 */
	void enterTable_expr_opt_alias_idx(CockroachDBParser.Table_expr_opt_alias_idxContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_expr_opt_alias_idx}.
	 * @param ctx the parse tree
	 */
	void exitTable_expr_opt_alias_idx(CockroachDBParser.Table_expr_opt_alias_idxContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_from_list}.
	 * @param ctx the parse tree
	 */
	void enterOpt_from_list(CockroachDBParser.Opt_from_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_from_list}.
	 * @param ctx the parse tree
	 */
	void exitOpt_from_list(CockroachDBParser.Opt_from_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#upsert_stmt}.
	 * @param ctx the parse tree
	 */
	void enterUpsert_stmt(CockroachDBParser.Upsert_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#upsert_stmt}.
	 * @param ctx the parse tree
	 */
	void exitUpsert_stmt(CockroachDBParser.Upsert_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#iconst}.
	 * @param ctx the parse tree
	 */
	void enterIconst(CockroachDBParser.IconstContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#iconst}.
	 * @param ctx the parse tree
	 */
	void exitIconst(CockroachDBParser.IconstContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#fconst}.
	 * @param ctx the parse tree
	 */
	void enterFconst(CockroachDBParser.FconstContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#fconst}.
	 * @param ctx the parse tree
	 */
	void exitFconst(CockroachDBParser.FconstContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#sconst}.
	 * @param ctx the parse tree
	 */
	void enterSconst(CockroachDBParser.SconstContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#sconst}.
	 * @param ctx the parse tree
	 */
	void exitSconst(CockroachDBParser.SconstContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#bconst}.
	 * @param ctx the parse tree
	 */
	void enterBconst(CockroachDBParser.BconstContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#bconst}.
	 * @param ctx the parse tree
	 */
	void exitBconst(CockroachDBParser.BconstContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#bitconst}.
	 * @param ctx the parse tree
	 */
	void enterBitconst(CockroachDBParser.BitconstContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#bitconst}.
	 * @param ctx the parse tree
	 */
	void exitBitconst(CockroachDBParser.BitconstContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#expr_list_in_parens}.
	 * @param ctx the parse tree
	 */
	void enterExpr_list_in_parens(CockroachDBParser.Expr_list_in_parensContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#expr_list_in_parens}.
	 * @param ctx the parse tree
	 */
	void exitExpr_list_in_parens(CockroachDBParser.Expr_list_in_parensContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#expr_list}.
	 * @param ctx the parse tree
	 */
	void enterExpr_list(CockroachDBParser.Expr_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#expr_list}.
	 * @param ctx the parse tree
	 */
	void exitExpr_list(CockroachDBParser.Expr_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#a_expr}.
	 * @param ctx the parse tree
	 */
	void enterA_expr(CockroachDBParser.A_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#a_expr}.
	 * @param ctx the parse tree
	 */
	void exitA_expr(CockroachDBParser.A_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#subquery_op}.
	 * @param ctx the parse tree
	 */
	void enterSubquery_op(CockroachDBParser.Subquery_opContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#subquery_op}.
	 * @param ctx the parse tree
	 */
	void exitSubquery_op(CockroachDBParser.Subquery_opContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#sub_type}.
	 * @param ctx the parse tree
	 */
	void enterSub_type(CockroachDBParser.Sub_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#sub_type}.
	 * @param ctx the parse tree
	 */
	void exitSub_type(CockroachDBParser.Sub_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#b_expr}.
	 * @param ctx the parse tree
	 */
	void enterB_expr(CockroachDBParser.B_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#b_expr}.
	 * @param ctx the parse tree
	 */
	void exitB_expr(CockroachDBParser.B_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#qual_op}.
	 * @param ctx the parse tree
	 */
	void enterQual_op(CockroachDBParser.Qual_opContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#qual_op}.
	 * @param ctx the parse tree
	 */
	void exitQual_op(CockroachDBParser.Qual_opContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#operator_op}.
	 * @param ctx the parse tree
	 */
	void enterOperator_op(CockroachDBParser.Operator_opContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#operator_op}.
	 * @param ctx the parse tree
	 */
	void exitOperator_op(CockroachDBParser.Operator_opContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#all_op}.
	 * @param ctx the parse tree
	 */
	void enterAll_op(CockroachDBParser.All_opContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#all_op}.
	 * @param ctx the parse tree
	 */
	void exitAll_op(CockroachDBParser.All_opContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#c_expr}.
	 * @param ctx the parse tree
	 */
	void enterC_expr(CockroachDBParser.C_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#c_expr}.
	 * @param ctx the parse tree
	 */
	void exitC_expr(CockroachDBParser.C_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#d_expr}.
	 * @param ctx the parse tree
	 */
	void enterD_expr(CockroachDBParser.D_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#d_expr}.
	 * @param ctx the parse tree
	 */
	void exitD_expr(CockroachDBParser.D_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#typed_literal}.
	 * @param ctx the parse tree
	 */
	void enterTyped_literal(CockroachDBParser.Typed_literalContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#typed_literal}.
	 * @param ctx the parse tree
	 */
	void exitTyped_literal(CockroachDBParser.Typed_literalContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#func_name_no_crdb_extra}.
	 * @param ctx the parse tree
	 */
	void enterFunc_name_no_crdb_extra(CockroachDBParser.Func_name_no_crdb_extraContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#func_name_no_crdb_extra}.
	 * @param ctx the parse tree
	 */
	void exitFunc_name_no_crdb_extra(CockroachDBParser.Func_name_no_crdb_extraContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#interval_value}.
	 * @param ctx the parse tree
	 */
	void enterInterval_value(CockroachDBParser.Interval_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#interval_value}.
	 * @param ctx the parse tree
	 */
	void exitInterval_value(CockroachDBParser.Interval_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#column_path_with_star}.
	 * @param ctx the parse tree
	 */
	void enterColumn_path_with_star(CockroachDBParser.Column_path_with_starContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#column_path_with_star}.
	 * @param ctx the parse tree
	 */
	void exitColumn_path_with_star(CockroachDBParser.Column_path_with_starContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#func_expr}.
	 * @param ctx the parse tree
	 */
	void enterFunc_expr(CockroachDBParser.Func_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#func_expr}.
	 * @param ctx the parse tree
	 */
	void exitFunc_expr(CockroachDBParser.Func_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#array_expr}.
	 * @param ctx the parse tree
	 */
	void enterArray_expr(CockroachDBParser.Array_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#array_expr}.
	 * @param ctx the parse tree
	 */
	void exitArray_expr(CockroachDBParser.Array_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#array_expr_list}.
	 * @param ctx the parse tree
	 */
	void enterArray_expr_list(CockroachDBParser.Array_expr_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#array_expr_list}.
	 * @param ctx the parse tree
	 */
	void exitArray_expr_list(CockroachDBParser.Array_expr_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#within_group_clause}.
	 * @param ctx the parse tree
	 */
	void enterWithin_group_clause(CockroachDBParser.Within_group_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#within_group_clause}.
	 * @param ctx the parse tree
	 */
	void exitWithin_group_clause(CockroachDBParser.Within_group_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#over_clause}.
	 * @param ctx the parse tree
	 */
	void enterOver_clause(CockroachDBParser.Over_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#over_clause}.
	 * @param ctx the parse tree
	 */
	void exitOver_clause(CockroachDBParser.Over_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#filter_clause}.
	 * @param ctx the parse tree
	 */
	void enterFilter_clause(CockroachDBParser.Filter_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#filter_clause}.
	 * @param ctx the parse tree
	 */
	void exitFilter_clause(CockroachDBParser.Filter_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#single_sort_clause}.
	 * @param ctx the parse tree
	 */
	void enterSingle_sort_clause(CockroachDBParser.Single_sort_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#single_sort_clause}.
	 * @param ctx the parse tree
	 */
	void exitSingle_sort_clause(CockroachDBParser.Single_sort_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#labeled_row}.
	 * @param ctx the parse tree
	 */
	void enterLabeled_row(CockroachDBParser.Labeled_rowContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#labeled_row}.
	 * @param ctx the parse tree
	 */
	void exitLabeled_row(CockroachDBParser.Labeled_rowContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#row}.
	 * @param ctx the parse tree
	 */
	void enterRow(CockroachDBParser.RowContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#row}.
	 * @param ctx the parse tree
	 */
	void exitRow(CockroachDBParser.RowContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#expr_tuple_unambiguous}.
	 * @param ctx the parse tree
	 */
	void enterExpr_tuple_unambiguous(CockroachDBParser.Expr_tuple_unambiguousContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#expr_tuple_unambiguous}.
	 * @param ctx the parse tree
	 */
	void exitExpr_tuple_unambiguous(CockroachDBParser.Expr_tuple_unambiguousContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#tuple1_unambiguous_values}.
	 * @param ctx the parse tree
	 */
	void enterTuple1_unambiguous_values(CockroachDBParser.Tuple1_unambiguous_valuesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#tuple1_unambiguous_values}.
	 * @param ctx the parse tree
	 */
	void exitTuple1_unambiguous_values(CockroachDBParser.Tuple1_unambiguous_valuesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#case_expr}.
	 * @param ctx the parse tree
	 */
	void enterCase_expr(CockroachDBParser.Case_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#case_expr}.
	 * @param ctx the parse tree
	 */
	void exitCase_expr(CockroachDBParser.Case_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#case_arg}.
	 * @param ctx the parse tree
	 */
	void enterCase_arg(CockroachDBParser.Case_argContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#case_arg}.
	 * @param ctx the parse tree
	 */
	void exitCase_arg(CockroachDBParser.Case_argContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#when_clause_list}.
	 * @param ctx the parse tree
	 */
	void enterWhen_clause_list(CockroachDBParser.When_clause_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#when_clause_list}.
	 * @param ctx the parse tree
	 */
	void exitWhen_clause_list(CockroachDBParser.When_clause_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#when_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhen_clause(CockroachDBParser.When_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#when_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhen_clause(CockroachDBParser.When_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#case_default}.
	 * @param ctx the parse tree
	 */
	void enterCase_default(CockroachDBParser.Case_defaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#case_default}.
	 * @param ctx the parse tree
	 */
	void exitCase_default(CockroachDBParser.Case_defaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#array_subscripts}.
	 * @param ctx the parse tree
	 */
	void enterArray_subscripts(CockroachDBParser.Array_subscriptsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#array_subscripts}.
	 * @param ctx the parse tree
	 */
	void exitArray_subscripts(CockroachDBParser.Array_subscriptsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#array_subscript}.
	 * @param ctx the parse tree
	 */
	void enterArray_subscript(CockroachDBParser.Array_subscriptContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#array_subscript}.
	 * @param ctx the parse tree
	 */
	void exitArray_subscript(CockroachDBParser.Array_subscriptContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#opt_slice_bound}.
	 * @param ctx the parse tree
	 */
	void enterOpt_slice_bound(CockroachDBParser.Opt_slice_boundContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#opt_slice_bound}.
	 * @param ctx the parse tree
	 */
	void exitOpt_slice_bound(CockroachDBParser.Opt_slice_boundContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#in_expr}.
	 * @param ctx the parse tree
	 */
	void enterIn_expr(CockroachDBParser.In_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#in_expr}.
	 * @param ctx the parse tree
	 */
	void exitIn_expr(CockroachDBParser.In_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#expr_tuple1_ambiguous}.
	 * @param ctx the parse tree
	 */
	void enterExpr_tuple1_ambiguous(CockroachDBParser.Expr_tuple1_ambiguousContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#expr_tuple1_ambiguous}.
	 * @param ctx the parse tree
	 */
	void exitExpr_tuple1_ambiguous(CockroachDBParser.Expr_tuple1_ambiguousContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#tuple1_ambiguous_values}.
	 * @param ctx the parse tree
	 */
	void enterTuple1_ambiguous_values(CockroachDBParser.Tuple1_ambiguous_valuesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#tuple1_ambiguous_values}.
	 * @param ctx the parse tree
	 */
	void exitTuple1_ambiguous_values(CockroachDBParser.Tuple1_ambiguous_valuesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#identifier_}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier_(CockroachDBParser.Identifier_Context ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#identifier_}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier_(CockroachDBParser.Identifier_Context ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(CockroachDBParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(CockroachDBParser.NameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#non_reserved_word}.
	 * @param ctx the parse tree
	 */
	void enterNon_reserved_word(CockroachDBParser.Non_reserved_wordContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#non_reserved_word}.
	 * @param ctx the parse tree
	 */
	void exitNon_reserved_word(CockroachDBParser.Non_reserved_wordContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#unreserved_keyword}.
	 * @param ctx the parse tree
	 */
	void enterUnreserved_keyword(CockroachDBParser.Unreserved_keywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#unreserved_keyword}.
	 * @param ctx the parse tree
	 */
	void exitUnreserved_keyword(CockroachDBParser.Unreserved_keywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#col_name_keyword}.
	 * @param ctx the parse tree
	 */
	void enterCol_name_keyword(CockroachDBParser.Col_name_keywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#col_name_keyword}.
	 * @param ctx the parse tree
	 */
	void exitCol_name_keyword(CockroachDBParser.Col_name_keywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#column_name}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name(CockroachDBParser.Column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#column_name}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name(CockroachDBParser.Column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#collation_name}.
	 * @param ctx the parse tree
	 */
	void enterCollation_name(CockroachDBParser.Collation_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#collation_name}.
	 * @param ctx the parse tree
	 */
	void exitCollation_name(CockroachDBParser.Collation_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#constraint_name}.
	 * @param ctx the parse tree
	 */
	void enterConstraint_name(CockroachDBParser.Constraint_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#constraint_name}.
	 * @param ctx the parse tree
	 */
	void exitConstraint_name(CockroachDBParser.Constraint_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#cursor_name}.
	 * @param ctx the parse tree
	 */
	void enterCursor_name(CockroachDBParser.Cursor_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#cursor_name}.
	 * @param ctx the parse tree
	 */
	void exitCursor_name(CockroachDBParser.Cursor_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#database_name}.
	 * @param ctx the parse tree
	 */
	void enterDatabase_name(CockroachDBParser.Database_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#database_name}.
	 * @param ctx the parse tree
	 */
	void exitDatabase_name(CockroachDBParser.Database_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#func_application_name}.
	 * @param ctx the parse tree
	 */
	void enterFunc_application_name(CockroachDBParser.Func_application_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#func_application_name}.
	 * @param ctx the parse tree
	 */
	void exitFunc_application_name(CockroachDBParser.Func_application_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#func_name}.
	 * @param ctx the parse tree
	 */
	void enterFunc_name(CockroachDBParser.Func_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#func_name}.
	 * @param ctx the parse tree
	 */
	void exitFunc_name(CockroachDBParser.Func_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#index_name}.
	 * @param ctx the parse tree
	 */
	void enterIndex_name(CockroachDBParser.Index_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#index_name}.
	 * @param ctx the parse tree
	 */
	void exitIndex_name(CockroachDBParser.Index_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#view_name}.
	 * @param ctx the parse tree
	 */
	void enterView_name(CockroachDBParser.View_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#view_name}.
	 * @param ctx the parse tree
	 */
	void exitView_name(CockroachDBParser.View_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#savepoint_name}.
	 * @param ctx the parse tree
	 */
	void enterSavepoint_name(CockroachDBParser.Savepoint_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#savepoint_name}.
	 * @param ctx the parse tree
	 */
	void exitSavepoint_name(CockroachDBParser.Savepoint_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#statistics_name}.
	 * @param ctx the parse tree
	 */
	void enterStatistics_name(CockroachDBParser.Statistics_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#statistics_name}.
	 * @param ctx the parse tree
	 */
	void exitStatistics_name(CockroachDBParser.Statistics_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#qualifiable_schema_name}.
	 * @param ctx the parse tree
	 */
	void enterQualifiable_schema_name(CockroachDBParser.Qualifiable_schema_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#qualifiable_schema_name}.
	 * @param ctx the parse tree
	 */
	void exitQualifiable_schema_name(CockroachDBParser.Qualifiable_schema_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_index_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_index_name(CockroachDBParser.Table_index_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_index_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_index_name(CockroachDBParser.Table_index_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#standalone_index_name}.
	 * @param ctx the parse tree
	 */
	void enterStandalone_index_name(CockroachDBParser.Standalone_index_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#standalone_index_name}.
	 * @param ctx the parse tree
	 */
	void exitStandalone_index_name(CockroachDBParser.Standalone_index_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#db_object_name}.
	 * @param ctx the parse tree
	 */
	void enterDb_object_name(CockroachDBParser.Db_object_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#db_object_name}.
	 * @param ctx the parse tree
	 */
	void exitDb_object_name(CockroachDBParser.Db_object_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#simple_db_object_name}.
	 * @param ctx the parse tree
	 */
	void enterSimple_db_object_name(CockroachDBParser.Simple_db_object_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#simple_db_object_name}.
	 * @param ctx the parse tree
	 */
	void exitSimple_db_object_name(CockroachDBParser.Simple_db_object_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#db_object_name_component}.
	 * @param ctx the parse tree
	 */
	void enterDb_object_name_component(CockroachDBParser.Db_object_name_componentContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#db_object_name_component}.
	 * @param ctx the parse tree
	 */
	void exitDb_object_name_component(CockroachDBParser.Db_object_name_componentContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#complex_db_object_name}.
	 * @param ctx the parse tree
	 */
	void enterComplex_db_object_name(CockroachDBParser.Complex_db_object_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#complex_db_object_name}.
	 * @param ctx the parse tree
	 */
	void exitComplex_db_object_name(CockroachDBParser.Complex_db_object_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#db_name}.
	 * @param ctx the parse tree
	 */
	void enterDb_name(CockroachDBParser.Db_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#db_name}.
	 * @param ctx the parse tree
	 */
	void exitDb_name(CockroachDBParser.Db_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#family_name}.
	 * @param ctx the parse tree
	 */
	void enterFamily_name(CockroachDBParser.Family_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#family_name}.
	 * @param ctx the parse tree
	 */
	void exitFamily_name(CockroachDBParser.Family_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#sequence_name}.
	 * @param ctx the parse tree
	 */
	void enterSequence_name(CockroachDBParser.Sequence_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#sequence_name}.
	 * @param ctx the parse tree
	 */
	void exitSequence_name(CockroachDBParser.Sequence_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#table_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_name(CockroachDBParser.Table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#table_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_name(CockroachDBParser.Table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#type_name}.
	 * @param ctx the parse tree
	 */
	void enterType_name(CockroachDBParser.Type_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#type_name}.
	 * @param ctx the parse tree
	 */
	void exitType_name(CockroachDBParser.Type_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#unrestricted_name}.
	 * @param ctx the parse tree
	 */
	void enterUnrestricted_name(CockroachDBParser.Unrestricted_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#unrestricted_name}.
	 * @param ctx the parse tree
	 */
	void exitUnrestricted_name(CockroachDBParser.Unrestricted_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#type_func_name_keyword}.
	 * @param ctx the parse tree
	 */
	void enterType_func_name_keyword(CockroachDBParser.Type_func_name_keywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#type_func_name_keyword}.
	 * @param ctx the parse tree
	 */
	void exitType_func_name_keyword(CockroachDBParser.Type_func_name_keywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#reserved_keyword}.
	 * @param ctx the parse tree
	 */
	void enterReserved_keyword(CockroachDBParser.Reserved_keywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#reserved_keyword}.
	 * @param ctx the parse tree
	 */
	void exitReserved_keyword(CockroachDBParser.Reserved_keywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link CockroachDBParser#cockroachdb_extra_reserved_keyword}.
	 * @param ctx the parse tree
	 */
	void enterCockroachdb_extra_reserved_keyword(CockroachDBParser.Cockroachdb_extra_reserved_keywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link CockroachDBParser#cockroachdb_extra_reserved_keyword}.
	 * @param ctx the parse tree
	 */
	void exitCockroachdb_extra_reserved_keyword(CockroachDBParser.Cockroachdb_extra_reserved_keywordContext ctx);
}