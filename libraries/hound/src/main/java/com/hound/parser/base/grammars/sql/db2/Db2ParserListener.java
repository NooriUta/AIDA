// Generated from C:/AIDA/Dali4/HOUND/Hound/src/main/resources/grammars/sql/db2/Db2Parser.g4 by ANTLR 4.13.1
package com.hound.parser.base.grammars.sql.db2;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link Db2Parser}.
 */
public interface Db2ParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link Db2Parser#db2_file}.
	 * @param ctx the parse tree
	 */
	void enterDb2_file(Db2Parser.Db2_fileContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#db2_file}.
	 * @param ctx the parse tree
	 */
	void exitDb2_file(Db2Parser.Db2_fileContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#batch}.
	 * @param ctx the parse tree
	 */
	void enterBatch(Db2Parser.BatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#batch}.
	 * @param ctx the parse tree
	 */
	void exitBatch(Db2Parser.BatchContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_statement}.
	 * @param ctx the parse tree
	 */
	void enterSql_statement(Db2Parser.Sql_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_statement}.
	 * @param ctx the parse tree
	 */
	void exitSql_statement(Db2Parser.Sql_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_schema_statement}.
	 * @param ctx the parse tree
	 */
	void enterSql_schema_statement(Db2Parser.Sql_schema_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_schema_statement}.
	 * @param ctx the parse tree
	 */
	void exitSql_schema_statement(Db2Parser.Sql_schema_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_data_change_statement}.
	 * @param ctx the parse tree
	 */
	void enterSql_data_change_statement(Db2Parser.Sql_data_change_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_data_change_statement}.
	 * @param ctx the parse tree
	 */
	void exitSql_data_change_statement(Db2Parser.Sql_data_change_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_data_statement}.
	 * @param ctx the parse tree
	 */
	void enterSql_data_statement(Db2Parser.Sql_data_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_data_statement}.
	 * @param ctx the parse tree
	 */
	void exitSql_data_statement(Db2Parser.Sql_data_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_transaction_statement}.
	 * @param ctx the parse tree
	 */
	void enterSql_transaction_statement(Db2Parser.Sql_transaction_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_transaction_statement}.
	 * @param ctx the parse tree
	 */
	void exitSql_transaction_statement(Db2Parser.Sql_transaction_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_connection_statement}.
	 * @param ctx the parse tree
	 */
	void enterSql_connection_statement(Db2Parser.Sql_connection_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_connection_statement}.
	 * @param ctx the parse tree
	 */
	void exitSql_connection_statement(Db2Parser.Sql_connection_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_dynamic_statement}.
	 * @param ctx the parse tree
	 */
	void enterSql_dynamic_statement(Db2Parser.Sql_dynamic_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_dynamic_statement}.
	 * @param ctx the parse tree
	 */
	void exitSql_dynamic_statement(Db2Parser.Sql_dynamic_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_session_statement}.
	 * @param ctx the parse tree
	 */
	void enterSql_session_statement(Db2Parser.Sql_session_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_session_statement}.
	 * @param ctx the parse tree
	 */
	void exitSql_session_statement(Db2Parser.Sql_session_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_embedded_host_language_statement}.
	 * @param ctx the parse tree
	 */
	void enterSql_embedded_host_language_statement(Db2Parser.Sql_embedded_host_language_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_embedded_host_language_statement}.
	 * @param ctx the parse tree
	 */
	void exitSql_embedded_host_language_statement(Db2Parser.Sql_embedded_host_language_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_constrol_statement}.
	 * @param ctx the parse tree
	 */
	void enterSql_constrol_statement(Db2Parser.Sql_constrol_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_constrol_statement}.
	 * @param ctx the parse tree
	 */
	void exitSql_constrol_statement(Db2Parser.Sql_constrol_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#select_statement}.
	 * @param ctx the parse tree
	 */
	void enterSelect_statement(Db2Parser.Select_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#select_statement}.
	 * @param ctx the parse tree
	 */
	void exitSelect_statement(Db2Parser.Select_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#read_only_clause}.
	 * @param ctx the parse tree
	 */
	void enterRead_only_clause(Db2Parser.Read_only_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#read_only_clause}.
	 * @param ctx the parse tree
	 */
	void exitRead_only_clause(Db2Parser.Read_only_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#update_clause}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_clause(Db2Parser.Update_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#update_clause}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_clause(Db2Parser.Update_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#optimize_for_clause}.
	 * @param ctx the parse tree
	 */
	void enterOptimize_for_clause(Db2Parser.Optimize_for_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#optimize_for_clause}.
	 * @param ctx the parse tree
	 */
	void exitOptimize_for_clause(Db2Parser.Optimize_for_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#concurrent_access_resolution_clause}.
	 * @param ctx the parse tree
	 */
	void enterConcurrent_access_resolution_clause(Db2Parser.Concurrent_access_resolution_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#concurrent_access_resolution_clause}.
	 * @param ctx the parse tree
	 */
	void exitConcurrent_access_resolution_clause(Db2Parser.Concurrent_access_resolution_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#delete_statement}.
	 * @param ctx the parse tree
	 */
	void enterDelete_statement(Db2Parser.Delete_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#delete_statement}.
	 * @param ctx the parse tree
	 */
	void exitDelete_statement(Db2Parser.Delete_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#delete_statement_searched_delete}.
	 * @param ctx the parse tree
	 */
	void enterDelete_statement_searched_delete(Db2Parser.Delete_statement_searched_deleteContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#delete_statement_searched_delete}.
	 * @param ctx the parse tree
	 */
	void exitDelete_statement_searched_delete(Db2Parser.Delete_statement_searched_deleteContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_or_view_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_or_view_name(Db2Parser.Table_or_view_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_or_view_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_or_view_name(Db2Parser.Table_or_view_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#delete_statement_positioned_delete}.
	 * @param ctx the parse tree
	 */
	void enterDelete_statement_positioned_delete(Db2Parser.Delete_statement_positioned_deleteContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#delete_statement_positioned_delete}.
	 * @param ctx the parse tree
	 */
	void exitDelete_statement_positioned_delete(Db2Parser.Delete_statement_positioned_deleteContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#delete_deltalake_statement}.
	 * @param ctx the parse tree
	 */
	void enterDelete_deltalake_statement(Db2Parser.Delete_deltalake_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#delete_deltalake_statement}.
	 * @param ctx the parse tree
	 */
	void exitDelete_deltalake_statement(Db2Parser.Delete_deltalake_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#insert_statement}.
	 * @param ctx the parse tree
	 */
	void enterInsert_statement(Db2Parser.Insert_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#insert_statement}.
	 * @param ctx the parse tree
	 */
	void exitInsert_statement(Db2Parser.Insert_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#insert_datalake_statement}.
	 * @param ctx the parse tree
	 */
	void enterInsert_datalake_statement(Db2Parser.Insert_datalake_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#insert_datalake_statement}.
	 * @param ctx the parse tree
	 */
	void exitInsert_datalake_statement(Db2Parser.Insert_datalake_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#values_item}.
	 * @param ctx the parse tree
	 */
	void enterValues_item(Db2Parser.Values_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#values_item}.
	 * @param ctx the parse tree
	 */
	void exitValues_item(Db2Parser.Values_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#merge_statement}.
	 * @param ctx the parse tree
	 */
	void enterMerge_statement(Db2Parser.Merge_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#merge_statement}.
	 * @param ctx the parse tree
	 */
	void exitMerge_statement(Db2Parser.Merge_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_view_fullselect}.
	 * @param ctx the parse tree
	 */
	void enterTable_view_fullselect(Db2Parser.Table_view_fullselectContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_view_fullselect}.
	 * @param ctx the parse tree
	 */
	void exitTable_view_fullselect(Db2Parser.Table_view_fullselectContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#common_table_expression_list}.
	 * @param ctx the parse tree
	 */
	void enterCommon_table_expression_list(Db2Parser.Common_table_expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#common_table_expression_list}.
	 * @param ctx the parse tree
	 */
	void exitCommon_table_expression_list(Db2Parser.Common_table_expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#matching_condition}.
	 * @param ctx the parse tree
	 */
	void enterMatching_condition(Db2Parser.Matching_conditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#matching_condition}.
	 * @param ctx the parse tree
	 */
	void exitMatching_condition(Db2Parser.Matching_conditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#modification_operation}.
	 * @param ctx the parse tree
	 */
	void enterModification_operation(Db2Parser.Modification_operationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#modification_operation}.
	 * @param ctx the parse tree
	 */
	void exitModification_operation(Db2Parser.Modification_operationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#update_operation}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_operation(Db2Parser.Update_operationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#update_operation}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_operation(Db2Parser.Update_operationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#delete_operation}.
	 * @param ctx the parse tree
	 */
	void enterDelete_operation(Db2Parser.Delete_operationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#delete_operation}.
	 * @param ctx the parse tree
	 */
	void exitDelete_operation(Db2Parser.Delete_operationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#insert_operation}.
	 * @param ctx the parse tree
	 */
	void enterInsert_operation(Db2Parser.Insert_operationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#insert_operation}.
	 * @param ctx the parse tree
	 */
	void exitInsert_operation(Db2Parser.Insert_operationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#expr_null_default_list}.
	 * @param ctx the parse tree
	 */
	void enterExpr_null_default_list(Db2Parser.Expr_null_default_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#expr_null_default_list}.
	 * @param ctx the parse tree
	 */
	void exitExpr_null_default_list(Db2Parser.Expr_null_default_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#isolation_level}.
	 * @param ctx the parse tree
	 */
	void enterIsolation_level(Db2Parser.Isolation_levelContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#isolation_level}.
	 * @param ctx the parse tree
	 */
	void exitIsolation_level(Db2Parser.Isolation_levelContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#truncate_statement}.
	 * @param ctx the parse tree
	 */
	void enterTruncate_statement(Db2Parser.Truncate_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#truncate_statement}.
	 * @param ctx the parse tree
	 */
	void exitTruncate_statement(Db2Parser.Truncate_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#update_statement}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_statement(Db2Parser.Update_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#update_statement}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_statement(Db2Parser.Update_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#update_statement_searched_update}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_statement_searched_update(Db2Parser.Update_statement_searched_updateContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#update_statement_searched_update}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_statement_searched_update(Db2Parser.Update_statement_searched_updateContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#skip_wait}.
	 * @param ctx the parse tree
	 */
	void enterSkip_wait(Db2Parser.Skip_waitContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#skip_wait}.
	 * @param ctx the parse tree
	 */
	void exitSkip_wait(Db2Parser.Skip_waitContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#update_statement_positioned_update}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_statement_positioned_update(Db2Parser.Update_statement_positioned_updateContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#update_statement_positioned_update}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_statement_positioned_update(Db2Parser.Update_statement_positioned_updateContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#include_columns}.
	 * @param ctx the parse tree
	 */
	void enterInclude_columns(Db2Parser.Include_columnsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#include_columns}.
	 * @param ctx the parse tree
	 */
	void exitInclude_columns(Db2Parser.Include_columnsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#assignment_clause}.
	 * @param ctx the parse tree
	 */
	void enterAssignment_clause(Db2Parser.Assignment_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#assignment_clause}.
	 * @param ctx the parse tree
	 */
	void exitAssignment_clause(Db2Parser.Assignment_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#assignment_item}.
	 * @param ctx the parse tree
	 */
	void enterAssignment_item(Db2Parser.Assignment_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#assignment_item}.
	 * @param ctx the parse tree
	 */
	void exitAssignment_item(Db2Parser.Assignment_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#period_clause}.
	 * @param ctx the parse tree
	 */
	void enterPeriod_clause(Db2Parser.Period_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#period_clause}.
	 * @param ctx the parse tree
	 */
	void exitPeriod_clause(Db2Parser.Period_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#time_sec}.
	 * @param ctx the parse tree
	 */
	void enterTime_sec(Db2Parser.Time_secContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#time_sec}.
	 * @param ctx the parse tree
	 */
	void exitTime_sec(Db2Parser.Time_secContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#update_datalake_statement}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_datalake_statement(Db2Parser.Update_datalake_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#update_datalake_statement}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_datalake_statement(Db2Parser.Update_datalake_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_database_authorities_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_database_authorities_statement(Db2Parser.Grant_database_authorities_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_database_authorities_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_database_authorities_statement(Db2Parser.Grant_database_authorities_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#db_privilege_list}.
	 * @param ctx the parse tree
	 */
	void enterDb_privilege_list(Db2Parser.Db_privilege_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#db_privilege_list}.
	 * @param ctx the parse tree
	 */
	void exitDb_privilege_list(Db2Parser.Db_privilege_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#db_privilege}.
	 * @param ctx the parse tree
	 */
	void enterDb_privilege(Db2Parser.Db_privilegeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#db_privilege}.
	 * @param ctx the parse tree
	 */
	void exitDb_privilege(Db2Parser.Db_privilegeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grantee}.
	 * @param ctx the parse tree
	 */
	void enterGrantee(Db2Parser.GranteeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grantee}.
	 * @param ctx the parse tree
	 */
	void exitGrantee(Db2Parser.GranteeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grantee_user_group}.
	 * @param ctx the parse tree
	 */
	void enterGrantee_user_group(Db2Parser.Grantee_user_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grantee_user_group}.
	 * @param ctx the parse tree
	 */
	void exitGrantee_user_group(Db2Parser.Grantee_user_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#user_group}.
	 * @param ctx the parse tree
	 */
	void enterUser_group(Db2Parser.User_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#user_group}.
	 * @param ctx the parse tree
	 */
	void exitUser_group(Db2Parser.User_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grantee_list}.
	 * @param ctx the parse tree
	 */
	void enterGrantee_list(Db2Parser.Grantee_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grantee_list}.
	 * @param ctx the parse tree
	 */
	void exitGrantee_list(Db2Parser.Grantee_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grantee_list_public}.
	 * @param ctx the parse tree
	 */
	void enterGrantee_list_public(Db2Parser.Grantee_list_publicContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grantee_list_public}.
	 * @param ctx the parse tree
	 */
	void exitGrantee_list_public(Db2Parser.Grantee_list_publicContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grantee_list_user_group}.
	 * @param ctx the parse tree
	 */
	void enterGrantee_list_user_group(Db2Parser.Grantee_list_user_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grantee_list_user_group}.
	 * @param ctx the parse tree
	 */
	void exitGrantee_list_user_group(Db2Parser.Grantee_list_user_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_exemption_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_exemption_statement(Db2Parser.Grant_exemption_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_exemption_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_exemption_statement(Db2Parser.Grant_exemption_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#exemption_privilege}.
	 * @param ctx the parse tree
	 */
	void enterExemption_privilege(Db2Parser.Exemption_privilegeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#exemption_privilege}.
	 * @param ctx the parse tree
	 */
	void exitExemption_privilege(Db2Parser.Exemption_privilegeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_global_variable_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_global_variable_privileges_statement(Db2Parser.Grant_global_variable_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_global_variable_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_global_variable_privileges_statement(Db2Parser.Grant_global_variable_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#variable_privilege}.
	 * @param ctx the parse tree
	 */
	void enterVariable_privilege(Db2Parser.Variable_privilegeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#variable_privilege}.
	 * @param ctx the parse tree
	 */
	void exitVariable_privilege(Db2Parser.Variable_privilegeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#read_write}.
	 * @param ctx the parse tree
	 */
	void enterRead_write(Db2Parser.Read_writeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#read_write}.
	 * @param ctx the parse tree
	 */
	void exitRead_write(Db2Parser.Read_writeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#with_grant_option}.
	 * @param ctx the parse tree
	 */
	void enterWith_grant_option(Db2Parser.With_grant_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#with_grant_option}.
	 * @param ctx the parse tree
	 */
	void exitWith_grant_option(Db2Parser.With_grant_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_index_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_index_privileges_statement(Db2Parser.Grant_index_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_index_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_index_privileges_statement(Db2Parser.Grant_index_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_module_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_module_privileges_statement(Db2Parser.Grant_module_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_module_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_module_privileges_statement(Db2Parser.Grant_module_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_package_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_package_privileges_statement(Db2Parser.Grant_package_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_package_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_package_privileges_statement(Db2Parser.Grant_package_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#package_privilege_list}.
	 * @param ctx the parse tree
	 */
	void enterPackage_privilege_list(Db2Parser.Package_privilege_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#package_privilege_list}.
	 * @param ctx the parse tree
	 */
	void exitPackage_privilege_list(Db2Parser.Package_privilege_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#package_privilege}.
	 * @param ctx the parse tree
	 */
	void enterPackage_privilege(Db2Parser.Package_privilegeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#package_privilege}.
	 * @param ctx the parse tree
	 */
	void exitPackage_privilege(Db2Parser.Package_privilegeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_role_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_role_statement(Db2Parser.Grant_role_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_role_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_role_statement(Db2Parser.Grant_role_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#role_list}.
	 * @param ctx the parse tree
	 */
	void enterRole_list(Db2Parser.Role_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#role_list}.
	 * @param ctx the parse tree
	 */
	void exitRole_list(Db2Parser.Role_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_routine_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_routine_privileges_statement(Db2Parser.Grant_routine_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_routine_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_routine_privileges_statement(Db2Parser.Grant_routine_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_schema_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_schema_privileges_statement(Db2Parser.Grant_schema_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_schema_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_schema_privileges_statement(Db2Parser.Grant_schema_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#schema_privilege_list}.
	 * @param ctx the parse tree
	 */
	void enterSchema_privilege_list(Db2Parser.Schema_privilege_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#schema_privilege_list}.
	 * @param ctx the parse tree
	 */
	void exitSchema_privilege_list(Db2Parser.Schema_privilege_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#schema_privilege}.
	 * @param ctx the parse tree
	 */
	void enterSchema_privilege(Db2Parser.Schema_privilegeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#schema_privilege}.
	 * @param ctx the parse tree
	 */
	void exitSchema_privilege(Db2Parser.Schema_privilegeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_security_label_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_security_label_statement(Db2Parser.Grant_security_label_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_security_label_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_security_label_statement(Db2Parser.Grant_security_label_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_sequence_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_sequence_privileges_statement(Db2Parser.Grant_sequence_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_sequence_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_sequence_privileges_statement(Db2Parser.Grant_sequence_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sequence_privilege_list}.
	 * @param ctx the parse tree
	 */
	void enterSequence_privilege_list(Db2Parser.Sequence_privilege_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sequence_privilege_list}.
	 * @param ctx the parse tree
	 */
	void exitSequence_privilege_list(Db2Parser.Sequence_privilege_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sequence_privilege}.
	 * @param ctx the parse tree
	 */
	void enterSequence_privilege(Db2Parser.Sequence_privilegeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sequence_privilege}.
	 * @param ctx the parse tree
	 */
	void exitSequence_privilege(Db2Parser.Sequence_privilegeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_server_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_server_privileges_statement(Db2Parser.Grant_server_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_server_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_server_privileges_statement(Db2Parser.Grant_server_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_setsessionuser_privilege_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_setsessionuser_privilege_statement(Db2Parser.Grant_setsessionuser_privilege_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_setsessionuser_privilege_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_setsessionuser_privilege_statement(Db2Parser.Grant_setsessionuser_privilege_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#user_list}.
	 * @param ctx the parse tree
	 */
	void enterUser_list(Db2Parser.User_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#user_list}.
	 * @param ctx the parse tree
	 */
	void exitUser_list(Db2Parser.User_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#user_auth}.
	 * @param ctx the parse tree
	 */
	void enterUser_auth(Db2Parser.User_authContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#user_auth}.
	 * @param ctx the parse tree
	 */
	void exitUser_auth(Db2Parser.User_authContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_table_space_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_table_space_privileges_statement(Db2Parser.Grant_table_space_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_table_space_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_table_space_privileges_statement(Db2Parser.Grant_table_space_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_table_view_or_nickname_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_table_view_or_nickname_privileges_statement(Db2Parser.Grant_table_view_or_nickname_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_table_view_or_nickname_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_table_view_or_nickname_privileges_statement(Db2Parser.Grant_table_view_or_nickname_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#tvn_privilege_list}.
	 * @param ctx the parse tree
	 */
	void enterTvn_privilege_list(Db2Parser.Tvn_privilege_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#tvn_privilege_list}.
	 * @param ctx the parse tree
	 */
	void exitTvn_privilege_list(Db2Parser.Tvn_privilege_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#tvn_privilege}.
	 * @param ctx the parse tree
	 */
	void enterTvn_privilege(Db2Parser.Tvn_privilegeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#tvn_privilege}.
	 * @param ctx the parse tree
	 */
	void exitTvn_privilege(Db2Parser.Tvn_privilegeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#column_name_list_paren}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name_list_paren(Db2Parser.Column_name_list_parenContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#column_name_list_paren}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name_list_paren(Db2Parser.Column_name_list_parenContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#column_name_list}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name_list(Db2Parser.Column_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#column_name_list}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name_list(Db2Parser.Column_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_workload_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_workload_privileges_statement(Db2Parser.Grant_workload_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_workload_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_workload_privileges_statement(Db2Parser.Grant_workload_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_xsr_object_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_xsr_object_privileges_statement(Db2Parser.Grant_xsr_object_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_xsr_object_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_xsr_object_privileges_statement(Db2Parser.Grant_xsr_object_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_database_authorities_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_database_authorities_statement(Db2Parser.Revoke_database_authorities_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_database_authorities_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_database_authorities_statement(Db2Parser.Revoke_database_authorities_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#by_all}.
	 * @param ctx the parse tree
	 */
	void enterBy_all(Db2Parser.By_allContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#by_all}.
	 * @param ctx the parse tree
	 */
	void exitBy_all(Db2Parser.By_allContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_exemption_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_exemption_statement(Db2Parser.Revoke_exemption_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_exemption_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_exemption_statement(Db2Parser.Revoke_exemption_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_global_variable_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_global_variable_privileges_statement(Db2Parser.Revoke_global_variable_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_global_variable_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_global_variable_privileges_statement(Db2Parser.Revoke_global_variable_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_index_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_index_privileges_statement(Db2Parser.Revoke_index_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_index_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_index_privileges_statement(Db2Parser.Revoke_index_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_module_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_module_privileges_statement(Db2Parser.Revoke_module_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_module_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_module_privileges_statement(Db2Parser.Revoke_module_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_package_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_package_privileges_statement(Db2Parser.Revoke_package_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_package_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_package_privileges_statement(Db2Parser.Revoke_package_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_role_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_role_statement(Db2Parser.Revoke_role_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_role_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_role_statement(Db2Parser.Revoke_role_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_routine_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_routine_privileges_statement(Db2Parser.Revoke_routine_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_routine_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_routine_privileges_statement(Db2Parser.Revoke_routine_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_schema_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_schema_privileges_statement(Db2Parser.Revoke_schema_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_schema_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_schema_privileges_statement(Db2Parser.Revoke_schema_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_security_label_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_security_label_statement(Db2Parser.Revoke_security_label_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_security_label_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_security_label_statement(Db2Parser.Revoke_security_label_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_sequence_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_sequence_privileges_statement(Db2Parser.Revoke_sequence_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_sequence_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_sequence_privileges_statement(Db2Parser.Revoke_sequence_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_server_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_server_privileges_statement(Db2Parser.Revoke_server_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_server_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_server_privileges_statement(Db2Parser.Revoke_server_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_setsessionuser_privilege_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_setsessionuser_privilege_statement(Db2Parser.Revoke_setsessionuser_privilege_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_setsessionuser_privilege_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_setsessionuser_privilege_statement(Db2Parser.Revoke_setsessionuser_privilege_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_table_space_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_table_space_privileges_statement(Db2Parser.Revoke_table_space_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_table_space_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_table_space_privileges_statement(Db2Parser.Revoke_table_space_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_table_view_or_nickname_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_table_view_or_nickname_privileges_statement(Db2Parser.Revoke_table_view_or_nickname_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_table_view_or_nickname_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_table_view_or_nickname_privileges_statement(Db2Parser.Revoke_table_view_or_nickname_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_workload_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_workload_privileges_statement(Db2Parser.Revoke_workload_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_workload_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_workload_privileges_statement(Db2Parser.Revoke_workload_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#revoke_xsr_object_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_xsr_object_privileges_statement(Db2Parser.Revoke_xsr_object_privileges_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#revoke_xsr_object_privileges_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_xsr_object_privileges_statement(Db2Parser.Revoke_xsr_object_privileges_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#user_group_role}.
	 * @param ctx the parse tree
	 */
	void enterUser_group_role(Db2Parser.User_group_roleContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#user_group_role}.
	 * @param ctx the parse tree
	 */
	void exitUser_group_role(Db2Parser.User_group_roleContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#rollback_statement}.
	 * @param ctx the parse tree
	 */
	void enterRollback_statement(Db2Parser.Rollback_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#rollback_statement}.
	 * @param ctx the parse tree
	 */
	void exitRollback_statement(Db2Parser.Rollback_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#savepoint_statement}.
	 * @param ctx the parse tree
	 */
	void enterSavepoint_statement(Db2Parser.Savepoint_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#savepoint_statement}.
	 * @param ctx the parse tree
	 */
	void exitSavepoint_statement(Db2Parser.Savepoint_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#release_savepoint_statement}.
	 * @param ctx the parse tree
	 */
	void enterRelease_savepoint_statement(Db2Parser.Release_savepoint_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#release_savepoint_statement}.
	 * @param ctx the parse tree
	 */
	void exitRelease_savepoint_statement(Db2Parser.Release_savepoint_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#allocate_cursor_statement}.
	 * @param ctx the parse tree
	 */
	void enterAllocate_cursor_statement(Db2Parser.Allocate_cursor_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#allocate_cursor_statement}.
	 * @param ctx the parse tree
	 */
	void exitAllocate_cursor_statement(Db2Parser.Allocate_cursor_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_audit_policy_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_audit_policy_statement(Db2Parser.Alter_audit_policy_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_audit_policy_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_audit_policy_statement(Db2Parser.Alter_audit_policy_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#status_spec}.
	 * @param ctx the parse tree
	 */
	void enterStatus_spec(Db2Parser.Status_specContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#status_spec}.
	 * @param ctx the parse tree
	 */
	void exitStatus_spec(Db2Parser.Status_specContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#normal_audit}.
	 * @param ctx the parse tree
	 */
	void enterNormal_audit(Db2Parser.Normal_auditContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#normal_audit}.
	 * @param ctx the parse tree
	 */
	void exitNormal_audit(Db2Parser.Normal_auditContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_bufferpool_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_bufferpool_statement(Db2Parser.Alter_bufferpool_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_bufferpool_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_bufferpool_statement(Db2Parser.Alter_bufferpool_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#immediate_deferred}.
	 * @param ctx the parse tree
	 */
	void enterImmediate_deferred(Db2Parser.Immediate_deferredContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#immediate_deferred}.
	 * @param ctx the parse tree
	 */
	void exitImmediate_deferred(Db2Parser.Immediate_deferredContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_database_partition_group_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_partition_group_statement(Db2Parser.Alter_database_partition_group_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_database_partition_group_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_partition_group_statement(Db2Parser.Alter_database_partition_group_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#db_partition_group_list_item}.
	 * @param ctx the parse tree
	 */
	void enterDb_partition_group_list_item(Db2Parser.Db_partition_group_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#db_partition_group_list_item}.
	 * @param ctx the parse tree
	 */
	void exitDb_partition_group_list_item(Db2Parser.Db_partition_group_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#db_partition_num_nums}.
	 * @param ctx the parse tree
	 */
	void enterDb_partition_num_nums(Db2Parser.Db_partition_num_numsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#db_partition_num_nums}.
	 * @param ctx the parse tree
	 */
	void exitDb_partition_num_nums(Db2Parser.Db_partition_num_numsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#db_partitions_clause}.
	 * @param ctx the parse tree
	 */
	void enterDb_partitions_clause(Db2Parser.Db_partitions_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#db_partitions_clause}.
	 * @param ctx the parse tree
	 */
	void exitDb_partitions_clause(Db2Parser.Db_partitions_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#db_partition_options}.
	 * @param ctx the parse tree
	 */
	void enterDb_partition_options(Db2Parser.Db_partition_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#db_partition_options}.
	 * @param ctx the parse tree
	 */
	void exitDb_partition_options(Db2Parser.Db_partition_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_database_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_statement(Db2Parser.Alter_database_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_database_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_statement(Db2Parser.Alter_database_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_database_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_database_opts(Db2Parser.Alter_database_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_database_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_database_opts(Db2Parser.Alter_database_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_event_monitor_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_event_monitor_statement(Db2Parser.Alter_event_monitor_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_event_monitor_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_event_monitor_statement(Db2Parser.Alter_event_monitor_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_event_monitor_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_event_monitor_opts(Db2Parser.Alter_event_monitor_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_event_monitor_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_event_monitor_opts(Db2Parser.Alter_event_monitor_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_function_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_function_statement(Db2Parser.Alter_function_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_function_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_function_statement(Db2Parser.Alter_function_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_function_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_function_opts(Db2Parser.Alter_function_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_function_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_function_opts(Db2Parser.Alter_function_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#function_designator}.
	 * @param ctx the parse tree
	 */
	void enterFunction_designator(Db2Parser.Function_designatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#function_designator}.
	 * @param ctx the parse tree
	 */
	void exitFunction_designator(Db2Parser.Function_designatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#data_type_list}.
	 * @param ctx the parse tree
	 */
	void enterData_type_list(Db2Parser.Data_type_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#data_type_list}.
	 * @param ctx the parse tree
	 */
	void exitData_type_list(Db2Parser.Data_type_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#data_type_list_paren}.
	 * @param ctx the parse tree
	 */
	void enterData_type_list_paren(Db2Parser.Data_type_list_parenContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#data_type_list_paren}.
	 * @param ctx the parse tree
	 */
	void exitData_type_list_paren(Db2Parser.Data_type_list_parenContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_histogram_template_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_histogram_template_statement(Db2Parser.Alter_histogram_template_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_histogram_template_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_histogram_template_statement(Db2Parser.Alter_histogram_template_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_index_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_index_statement(Db2Parser.Alter_index_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_index_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_index_statement(Db2Parser.Alter_index_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#yes_no}.
	 * @param ctx the parse tree
	 */
	void enterYes_no(Db2Parser.Yes_noContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#yes_no}.
	 * @param ctx the parse tree
	 */
	void exitYes_no(Db2Parser.Yes_noContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_mask_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_mask_statement(Db2Parser.Alter_mask_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_mask_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_mask_statement(Db2Parser.Alter_mask_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#enable_disable}.
	 * @param ctx the parse tree
	 */
	void enterEnable_disable(Db2Parser.Enable_disableContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#enable_disable}.
	 * @param ctx the parse tree
	 */
	void exitEnable_disable(Db2Parser.Enable_disableContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_method_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_method_statement(Db2Parser.Alter_method_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_method_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_method_statement(Db2Parser.Alter_method_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#method_designator}.
	 * @param ctx the parse tree
	 */
	void enterMethod_designator(Db2Parser.Method_designatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#method_designator}.
	 * @param ctx the parse tree
	 */
	void exitMethod_designator(Db2Parser.Method_designatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_model_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_model_statement(Db2Parser.Alter_model_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_model_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_model_statement(Db2Parser.Alter_model_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_module_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_module_statement(Db2Parser.Alter_module_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_module_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_module_statement(Db2Parser.Alter_module_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_module_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_module_opts(Db2Parser.Alter_module_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_module_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_module_opts(Db2Parser.Alter_module_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#module_function_definition}.
	 * @param ctx the parse tree
	 */
	void enterModule_function_definition(Db2Parser.Module_function_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#module_function_definition}.
	 * @param ctx the parse tree
	 */
	void exitModule_function_definition(Db2Parser.Module_function_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#module_procedure_definition}.
	 * @param ctx the parse tree
	 */
	void enterModule_procedure_definition(Db2Parser.Module_procedure_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#module_procedure_definition}.
	 * @param ctx the parse tree
	 */
	void exitModule_procedure_definition(Db2Parser.Module_procedure_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#module_type_definition}.
	 * @param ctx the parse tree
	 */
	void enterModule_type_definition(Db2Parser.Module_type_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#module_type_definition}.
	 * @param ctx the parse tree
	 */
	void exitModule_type_definition(Db2Parser.Module_type_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#module_variable_definition}.
	 * @param ctx the parse tree
	 */
	void enterModule_variable_definition(Db2Parser.Module_variable_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#module_variable_definition}.
	 * @param ctx the parse tree
	 */
	void exitModule_variable_definition(Db2Parser.Module_variable_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#module_condition_definition}.
	 * @param ctx the parse tree
	 */
	void enterModule_condition_definition(Db2Parser.Module_condition_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#module_condition_definition}.
	 * @param ctx the parse tree
	 */
	void exitModule_condition_definition(Db2Parser.Module_condition_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#module_object_identification}.
	 * @param ctx the parse tree
	 */
	void enterModule_object_identification(Db2Parser.Module_object_identificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#module_object_identification}.
	 * @param ctx the parse tree
	 */
	void exitModule_object_identification(Db2Parser.Module_object_identificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#module_function_designator}.
	 * @param ctx the parse tree
	 */
	void enterModule_function_designator(Db2Parser.Module_function_designatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#module_function_designator}.
	 * @param ctx the parse tree
	 */
	void exitModule_function_designator(Db2Parser.Module_function_designatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#module_procedure_designator}.
	 * @param ctx the parse tree
	 */
	void enterModule_procedure_designator(Db2Parser.Module_procedure_designatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#module_procedure_designator}.
	 * @param ctx the parse tree
	 */
	void exitModule_procedure_designator(Db2Parser.Module_procedure_designatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_nickname_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_nickname_statement(Db2Parser.Alter_nickname_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_nickname_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_nickname_statement(Db2Parser.Alter_nickname_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_nickname_opts_1}.
	 * @param ctx the parse tree
	 */
	void enterAlter_nickname_opts_1(Db2Parser.Alter_nickname_opts_1Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_nickname_opts_1}.
	 * @param ctx the parse tree
	 */
	void exitAlter_nickname_opts_1(Db2Parser.Alter_nickname_opts_1Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_nickname_opts_1_item}.
	 * @param ctx the parse tree
	 */
	void enterAlter_nickname_opts_1_item(Db2Parser.Alter_nickname_opts_1_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_nickname_opts_1_item}.
	 * @param ctx the parse tree
	 */
	void exitAlter_nickname_opts_1_item(Db2Parser.Alter_nickname_opts_1_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_nickname_opts_2}.
	 * @param ctx the parse tree
	 */
	void enterAlter_nickname_opts_2(Db2Parser.Alter_nickname_opts_2Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_nickname_opts_2}.
	 * @param ctx the parse tree
	 */
	void exitAlter_nickname_opts_2(Db2Parser.Alter_nickname_opts_2Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_nickname_opts_2_item}.
	 * @param ctx the parse tree
	 */
	void enterAlter_nickname_opts_2_item(Db2Parser.Alter_nickname_opts_2_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_nickname_opts_2_item}.
	 * @param ctx the parse tree
	 */
	void exitAlter_nickname_opts_2_item(Db2Parser.Alter_nickname_opts_2_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#constraint_alteration}.
	 * @param ctx the parse tree
	 */
	void enterConstraint_alteration(Db2Parser.Constraint_alterationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#constraint_alteration}.
	 * @param ctx the parse tree
	 */
	void exitConstraint_alteration(Db2Parser.Constraint_alterationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_package_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_package_statement(Db2Parser.Alter_package_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_package_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_package_statement(Db2Parser.Alter_package_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_package_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_package_opts(Db2Parser.Alter_package_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_package_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_package_opts(Db2Parser.Alter_package_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_permission_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_permission_statement(Db2Parser.Alter_permission_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_permission_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_permission_statement(Db2Parser.Alter_permission_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_procedure_external_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_procedure_external_statement(Db2Parser.Alter_procedure_external_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_procedure_external_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_procedure_external_statement(Db2Parser.Alter_procedure_external_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_procedure_external_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_procedure_external_opts(Db2Parser.Alter_procedure_external_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_procedure_external_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_procedure_external_opts(Db2Parser.Alter_procedure_external_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#procedure_designator}.
	 * @param ctx the parse tree
	 */
	void enterProcedure_designator(Db2Parser.Procedure_designatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#procedure_designator}.
	 * @param ctx the parse tree
	 */
	void exitProcedure_designator(Db2Parser.Procedure_designatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_procedure_sourced_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_procedure_sourced_statement(Db2Parser.Alter_procedure_sourced_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_procedure_sourced_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_procedure_sourced_statement(Db2Parser.Alter_procedure_sourced_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#parameter_alteration}.
	 * @param ctx the parse tree
	 */
	void enterParameter_alteration(Db2Parser.Parameter_alterationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#parameter_alteration}.
	 * @param ctx the parse tree
	 */
	void exitParameter_alteration(Db2Parser.Parameter_alterationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_procedure_sql_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_procedure_sql_statement(Db2Parser.Alter_procedure_sql_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_procedure_sql_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_procedure_sql_statement(Db2Parser.Alter_procedure_sql_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_schema_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_schema_statement(Db2Parser.Alter_schema_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_schema_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_schema_statement(Db2Parser.Alter_schema_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#none_changes}.
	 * @param ctx the parse tree
	 */
	void enterNone_changes(Db2Parser.None_changesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#none_changes}.
	 * @param ctx the parse tree
	 */
	void exitNone_changes(Db2Parser.None_changesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_security_label_component_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_security_label_component_statement(Db2Parser.Alter_security_label_component_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_security_label_component_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_security_label_component_statement(Db2Parser.Alter_security_label_component_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#add_element_clause}.
	 * @param ctx the parse tree
	 */
	void enterAdd_element_clause(Db2Parser.Add_element_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#add_element_clause}.
	 * @param ctx the parse tree
	 */
	void exitAdd_element_clause(Db2Parser.Add_element_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#array_element_clause}.
	 * @param ctx the parse tree
	 */
	void enterArray_element_clause(Db2Parser.Array_element_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#array_element_clause}.
	 * @param ctx the parse tree
	 */
	void exitArray_element_clause(Db2Parser.Array_element_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#tree_element_clause}.
	 * @param ctx the parse tree
	 */
	void enterTree_element_clause(Db2Parser.Tree_element_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#tree_element_clause}.
	 * @param ctx the parse tree
	 */
	void exitTree_element_clause(Db2Parser.Tree_element_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_security_policy_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_security_policy_statement(Db2Parser.Alter_security_policy_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_security_policy_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_security_policy_statement(Db2Parser.Alter_security_policy_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_security_policy_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_security_policy_opts(Db2Parser.Alter_security_policy_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_security_policy_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_security_policy_opts(Db2Parser.Alter_security_policy_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_sequence_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_sequence_statement(Db2Parser.Alter_sequence_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_sequence_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_sequence_statement(Db2Parser.Alter_sequence_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_sequence_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_sequence_opts(Db2Parser.Alter_sequence_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_sequence_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_sequence_opts(Db2Parser.Alter_sequence_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_server_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_server_statement(Db2Parser.Alter_server_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_server_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_server_statement(Db2Parser.Alter_server_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_server_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_server_opts(Db2Parser.Alter_server_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_server_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_server_opts(Db2Parser.Alter_server_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_service_class_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_service_class_statement(Db2Parser.Alter_service_class_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_service_class_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_service_class_statement(Db2Parser.Alter_service_class_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_service_class_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_service_class_opts(Db2Parser.Alter_service_class_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_service_class_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_service_class_opts(Db2Parser.Alter_service_class_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#default_on_off}.
	 * @param ctx the parse tree
	 */
	void enterDefault_on_off(Db2Parser.Default_on_offContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#default_on_off}.
	 * @param ctx the parse tree
	 */
	void exitDefault_on_off(Db2Parser.Default_on_offContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#default_high_medium_low}.
	 * @param ctx the parse tree
	 */
	void enterDefault_high_medium_low(Db2Parser.Default_high_medium_lowContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#default_high_medium_low}.
	 * @param ctx the parse tree
	 */
	void exitDefault_high_medium_low(Db2Parser.Default_high_medium_lowContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_stogroup_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_stogroup_statement(Db2Parser.Alter_stogroup_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_stogroup_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_stogroup_statement(Db2Parser.Alter_stogroup_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_stogroup_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_stogroup_opts(Db2Parser.Alter_stogroup_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_stogroup_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_stogroup_opts(Db2Parser.Alter_stogroup_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_statement(Db2Parser.Alter_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_statement(Db2Parser.Alter_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_table_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_opts(Db2Parser.Alter_table_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_table_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_opts(Db2Parser.Alter_table_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#null_on_off}.
	 * @param ctx the parse tree
	 */
	void enterNull_on_off(Db2Parser.Null_on_offContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#null_on_off}.
	 * @param ctx the parse tree
	 */
	void exitNull_on_off(Db2Parser.Null_on_offContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#cascade_restrict}.
	 * @param ctx the parse tree
	 */
	void enterCascade_restrict(Db2Parser.Cascade_restrictContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#cascade_restrict}.
	 * @param ctx the parse tree
	 */
	void exitCascade_restrict(Db2Parser.Cascade_restrictContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#materialized_query_definition}.
	 * @param ctx the parse tree
	 */
	void enterMaterialized_query_definition(Db2Parser.Materialized_query_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#materialized_query_definition}.
	 * @param ctx the parse tree
	 */
	void exitMaterialized_query_definition(Db2Parser.Materialized_query_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#refreshable_table_options}.
	 * @param ctx the parse tree
	 */
	void enterRefreshable_table_options(Db2Parser.Refreshable_table_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#refreshable_table_options}.
	 * @param ctx the parse tree
	 */
	void exitRefreshable_table_options(Db2Parser.Refreshable_table_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#column_alteration}.
	 * @param ctx the parse tree
	 */
	void enterColumn_alteration(Db2Parser.Column_alterationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#column_alteration}.
	 * @param ctx the parse tree
	 */
	void exitColumn_alteration(Db2Parser.Column_alterationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#generation_alteration}.
	 * @param ctx the parse tree
	 */
	void enterGeneration_alteration(Db2Parser.Generation_alterationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#generation_alteration}.
	 * @param ctx the parse tree
	 */
	void exitGeneration_alteration(Db2Parser.Generation_alterationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#identity_alteration}.
	 * @param ctx the parse tree
	 */
	void enterIdentity_alteration(Db2Parser.Identity_alterationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#identity_alteration}.
	 * @param ctx the parse tree
	 */
	void exitIdentity_alteration(Db2Parser.Identity_alterationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#generation_attribute}.
	 * @param ctx the parse tree
	 */
	void enterGeneration_attribute(Db2Parser.Generation_attributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#generation_attribute}.
	 * @param ctx the parse tree
	 */
	void exitGeneration_attribute(Db2Parser.Generation_attributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#as_identity_clause}.
	 * @param ctx the parse tree
	 */
	void enterAs_identity_clause(Db2Parser.As_identity_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#as_identity_clause}.
	 * @param ctx the parse tree
	 */
	void exitAs_identity_clause(Db2Parser.As_identity_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#as_identity_clause_opts}.
	 * @param ctx the parse tree
	 */
	void enterAs_identity_clause_opts(Db2Parser.As_identity_clause_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#as_identity_clause_opts}.
	 * @param ctx the parse tree
	 */
	void exitAs_identity_clause_opts(Db2Parser.As_identity_clause_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#period_definition_alter}.
	 * @param ctx the parse tree
	 */
	void enterPeriod_definition_alter(Db2Parser.Period_definition_alterContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#period_definition_alter}.
	 * @param ctx the parse tree
	 */
	void exitPeriod_definition_alter(Db2Parser.Period_definition_alterContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#add_partition}.
	 * @param ctx the parse tree
	 */
	void enterAdd_partition(Db2Parser.Add_partitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#add_partition}.
	 * @param ctx the parse tree
	 */
	void exitAdd_partition(Db2Parser.Add_partitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#boundary_spec_alter}.
	 * @param ctx the parse tree
	 */
	void enterBoundary_spec_alter(Db2Parser.Boundary_spec_alterContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#boundary_spec_alter}.
	 * @param ctx the parse tree
	 */
	void exitBoundary_spec_alter(Db2Parser.Boundary_spec_alterContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#attach_partition}.
	 * @param ctx the parse tree
	 */
	void enterAttach_partition(Db2Parser.Attach_partitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#attach_partition}.
	 * @param ctx the parse tree
	 */
	void exitAttach_partition(Db2Parser.Attach_partitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#activate_deactivate}.
	 * @param ctx the parse tree
	 */
	void enterActivate_deactivate(Db2Parser.Activate_deactivateContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#activate_deactivate}.
	 * @param ctx the parse tree
	 */
	void exitActivate_deactivate(Db2Parser.Activate_deactivateContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_tablespace_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_tablespace_statement(Db2Parser.Alter_tablespace_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_tablespace_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_tablespace_statement(Db2Parser.Alter_tablespace_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_tablespace_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_tablespace_opts(Db2Parser.Alter_tablespace_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_tablespace_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_tablespace_opts(Db2Parser.Alter_tablespace_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#add_clause}.
	 * @param ctx the parse tree
	 */
	void enterAdd_clause(Db2Parser.Add_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#add_clause}.
	 * @param ctx the parse tree
	 */
	void exitAdd_clause(Db2Parser.Add_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#db_container_clause}.
	 * @param ctx the parse tree
	 */
	void enterDb_container_clause(Db2Parser.Db_container_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#db_container_clause}.
	 * @param ctx the parse tree
	 */
	void exitDb_container_clause(Db2Parser.Db_container_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#db_container_clause_opts}.
	 * @param ctx the parse tree
	 */
	void enterDb_container_clause_opts(Db2Parser.Db_container_clause_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#db_container_clause_opts}.
	 * @param ctx the parse tree
	 */
	void exitDb_container_clause_opts(Db2Parser.Db_container_clause_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#drop_container_clause}.
	 * @param ctx the parse tree
	 */
	void enterDrop_container_clause(Db2Parser.Drop_container_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#drop_container_clause}.
	 * @param ctx the parse tree
	 */
	void exitDrop_container_clause(Db2Parser.Drop_container_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#file_device}.
	 * @param ctx the parse tree
	 */
	void enterFile_device(Db2Parser.File_deviceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#file_device}.
	 * @param ctx the parse tree
	 */
	void exitFile_device(Db2Parser.File_deviceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#all_containers_clause}.
	 * @param ctx the parse tree
	 */
	void enterAll_containers_clause(Db2Parser.All_containers_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#all_containers_clause}.
	 * @param ctx the parse tree
	 */
	void exitAll_containers_clause(Db2Parser.All_containers_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#system_container_clause}.
	 * @param ctx the parse tree
	 */
	void enterSystem_container_clause(Db2Parser.System_container_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#system_container_clause}.
	 * @param ctx the parse tree
	 */
	void exitSystem_container_clause(Db2Parser.System_container_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#stripeset}.
	 * @param ctx the parse tree
	 */
	void enterStripeset(Db2Parser.StripesetContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#stripeset}.
	 * @param ctx the parse tree
	 */
	void exitStripeset(Db2Parser.StripesetContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#km}.
	 * @param ctx the parse tree
	 */
	void enterKm(Db2Parser.KmContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#km}.
	 * @param ctx the parse tree
	 */
	void exitKm(Db2Parser.KmContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#kmg_percent}.
	 * @param ctx the parse tree
	 */
	void enterKmg_percent(Db2Parser.Kmg_percentContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#kmg_percent}.
	 * @param ctx the parse tree
	 */
	void exitKmg_percent(Db2Parser.Kmg_percentContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_threshold_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_threshold_statement(Db2Parser.Alter_threshold_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_threshold_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_threshold_statement(Db2Parser.Alter_threshold_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_threshold_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_threshold_opts(Db2Parser.Alter_threshold_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_threshold_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_threshold_opts(Db2Parser.Alter_threshold_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_threshold_predicate}.
	 * @param ctx the parse tree
	 */
	void enterAlter_threshold_predicate(Db2Parser.Alter_threshold_predicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_threshold_predicate}.
	 * @param ctx the parse tree
	 */
	void exitAlter_threshold_predicate(Db2Parser.Alter_threshold_predicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_threshold_exceeded_actions}.
	 * @param ctx the parse tree
	 */
	void enterAlter_threshold_exceeded_actions(Db2Parser.Alter_threshold_exceeded_actionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_threshold_exceeded_actions}.
	 * @param ctx the parse tree
	 */
	void exitAlter_threshold_exceeded_actions(Db2Parser.Alter_threshold_exceeded_actionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#dt_units}.
	 * @param ctx the parse tree
	 */
	void enterDt_units(Db2Parser.Dt_unitsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#dt_units}.
	 * @param ctx the parse tree
	 */
	void exitDt_units(Db2Parser.Dt_unitsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#dt_units_with_seconds}.
	 * @param ctx the parse tree
	 */
	void enterDt_units_with_seconds(Db2Parser.Dt_units_with_secondsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#dt_units_with_seconds}.
	 * @param ctx the parse tree
	 */
	void exitDt_units_with_seconds(Db2Parser.Dt_units_with_secondsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_trigger_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_trigger_statement(Db2Parser.Alter_trigger_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_trigger_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_trigger_statement(Db2Parser.Alter_trigger_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_trusted_context_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_trusted_context_statement(Db2Parser.Alter_trusted_context_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_trusted_context_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_trusted_context_statement(Db2Parser.Alter_trusted_context_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_trusted_context_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_trusted_context_opts(Db2Parser.Alter_trusted_context_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_trusted_context_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_trusted_context_opts(Db2Parser.Alter_trusted_context_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_trusted_context_opts_alter_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_trusted_context_opts_alter_opts(Db2Parser.Alter_trusted_context_opts_alter_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_trusted_context_opts_alter_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_trusted_context_opts_alter_opts(Db2Parser.Alter_trusted_context_opts_alter_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#addr_clause_encryption_val}.
	 * @param ctx the parse tree
	 */
	void enterAddr_clause_encryption_val(Db2Parser.Addr_clause_encryption_valContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#addr_clause_encryption_val}.
	 * @param ctx the parse tree
	 */
	void exitAddr_clause_encryption_val(Db2Parser.Addr_clause_encryption_valContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#address_clause}.
	 * @param ctx the parse tree
	 */
	void enterAddress_clause(Db2Parser.Address_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#address_clause}.
	 * @param ctx the parse tree
	 */
	void exitAddress_clause(Db2Parser.Address_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#user_clause}.
	 * @param ctx the parse tree
	 */
	void enterUser_clause(Db2Parser.User_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#user_clause}.
	 * @param ctx the parse tree
	 */
	void exitUser_clause(Db2Parser.User_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#use_for_opts}.
	 * @param ctx the parse tree
	 */
	void enterUse_for_opts(Db2Parser.Use_for_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#use_for_opts}.
	 * @param ctx the parse tree
	 */
	void exitUse_for_opts(Db2Parser.Use_for_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#use_for_opts_2}.
	 * @param ctx the parse tree
	 */
	void enterUse_for_opts_2(Db2Parser.Use_for_opts_2Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#use_for_opts_2}.
	 * @param ctx the parse tree
	 */
	void exitUse_for_opts_2(Db2Parser.Use_for_opts_2Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_type_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_type_statement(Db2Parser.Alter_type_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_type_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_type_statement(Db2Parser.Alter_type_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_type_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_type_opts(Db2Parser.Alter_type_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_type_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_type_opts(Db2Parser.Alter_type_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#method_identifier}.
	 * @param ctx the parse tree
	 */
	void enterMethod_identifier(Db2Parser.Method_identifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#method_identifier}.
	 * @param ctx the parse tree
	 */
	void exitMethod_identifier(Db2Parser.Method_identifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#method_options}.
	 * @param ctx the parse tree
	 */
	void enterMethod_options(Db2Parser.Method_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#method_options}.
	 * @param ctx the parse tree
	 */
	void exitMethod_options(Db2Parser.Method_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_usage_list_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_usage_list_statement(Db2Parser.Alter_usage_list_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_usage_list_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_usage_list_statement(Db2Parser.Alter_usage_list_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_usage_list_opts_item}.
	 * @param ctx the parse tree
	 */
	void enterAlter_usage_list_opts_item(Db2Parser.Alter_usage_list_opts_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_usage_list_opts_item}.
	 * @param ctx the parse tree
	 */
	void exitAlter_usage_list_opts_item(Db2Parser.Alter_usage_list_opts_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_user_mapping_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_user_mapping_statement(Db2Parser.Alter_user_mapping_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_user_mapping_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_user_mapping_statement(Db2Parser.Alter_user_mapping_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_user_mapping_opts_item}.
	 * @param ctx the parse tree
	 */
	void enterAlter_user_mapping_opts_item(Db2Parser.Alter_user_mapping_opts_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_user_mapping_opts_item}.
	 * @param ctx the parse tree
	 */
	void exitAlter_user_mapping_opts_item(Db2Parser.Alter_user_mapping_opts_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#add_set}.
	 * @param ctx the parse tree
	 */
	void enterAdd_set(Db2Parser.Add_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#add_set}.
	 * @param ctx the parse tree
	 */
	void exitAdd_set(Db2Parser.Add_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_view_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_view_statement(Db2Parser.Alter_view_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_view_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_view_statement(Db2Parser.Alter_view_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_view_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_view_opts(Db2Parser.Alter_view_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_view_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_view_opts(Db2Parser.Alter_view_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_work_action_set_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_work_action_set_statement(Db2Parser.Alter_work_action_set_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_work_action_set_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_work_action_set_statement(Db2Parser.Alter_work_action_set_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_work_action_set_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_work_action_set_opts(Db2Parser.Alter_work_action_set_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_work_action_set_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_work_action_set_opts(Db2Parser.Alter_work_action_set_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#work_action_alteration}.
	 * @param ctx the parse tree
	 */
	void enterWork_action_alteration(Db2Parser.Work_action_alterationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#work_action_alteration}.
	 * @param ctx the parse tree
	 */
	void exitWork_action_alteration(Db2Parser.Work_action_alterationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#work_action_alteration_opts}.
	 * @param ctx the parse tree
	 */
	void enterWork_action_alteration_opts(Db2Parser.Work_action_alteration_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#work_action_alteration_opts}.
	 * @param ctx the parse tree
	 */
	void exitWork_action_alteration_opts(Db2Parser.Work_action_alteration_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_action_types_clause}.
	 * @param ctx the parse tree
	 */
	void enterAlter_action_types_clause(Db2Parser.Alter_action_types_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_action_types_clause}.
	 * @param ctx the parse tree
	 */
	void exitAlter_action_types_clause(Db2Parser.Alter_action_types_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#threshold_predicate_clause}.
	 * @param ctx the parse tree
	 */
	void enterThreshold_predicate_clause(Db2Parser.Threshold_predicate_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#threshold_predicate_clause}.
	 * @param ctx the parse tree
	 */
	void exitThreshold_predicate_clause(Db2Parser.Threshold_predicate_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_work_class_set_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_work_class_set_statement(Db2Parser.Alter_work_class_set_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_work_class_set_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_work_class_set_statement(Db2Parser.Alter_work_class_set_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_work_class_set_opts}.
	 * @param ctx the parse tree
	 */
	void enterAlter_work_class_set_opts(Db2Parser.Alter_work_class_set_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_work_class_set_opts}.
	 * @param ctx the parse tree
	 */
	void exitAlter_work_class_set_opts(Db2Parser.Alter_work_class_set_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#work_class_alteration}.
	 * @param ctx the parse tree
	 */
	void enterWork_class_alteration(Db2Parser.Work_class_alterationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#work_class_alteration}.
	 * @param ctx the parse tree
	 */
	void exitWork_class_alteration(Db2Parser.Work_class_alterationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#work_class_alteration_opts}.
	 * @param ctx the parse tree
	 */
	void enterWork_class_alteration_opts(Db2Parser.Work_class_alteration_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#work_class_alteration_opts}.
	 * @param ctx the parse tree
	 */
	void exitWork_class_alteration_opts(Db2Parser.Work_class_alteration_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#for_from_to_alter_clause}.
	 * @param ctx the parse tree
	 */
	void enterFor_from_to_alter_clause(Db2Parser.For_from_to_alter_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#for_from_to_alter_clause}.
	 * @param ctx the parse tree
	 */
	void exitFor_from_to_alter_clause(Db2Parser.For_from_to_alter_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#schema_alter_clause}.
	 * @param ctx the parse tree
	 */
	void enterSchema_alter_clause(Db2Parser.Schema_alter_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#schema_alter_clause}.
	 * @param ctx the parse tree
	 */
	void exitSchema_alter_clause(Db2Parser.Schema_alter_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#data_tag_alter_clause}.
	 * @param ctx the parse tree
	 */
	void enterData_tag_alter_clause(Db2Parser.Data_tag_alter_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#data_tag_alter_clause}.
	 * @param ctx the parse tree
	 */
	void exitData_tag_alter_clause(Db2Parser.Data_tag_alter_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_workload_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_workload_statement(Db2Parser.Alter_workload_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_workload_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_workload_statement(Db2Parser.Alter_workload_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_workload_opts_item}.
	 * @param ctx the parse tree
	 */
	void enterAlter_workload_opts_item(Db2Parser.Alter_workload_opts_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_workload_opts_item}.
	 * @param ctx the parse tree
	 */
	void exitAlter_workload_opts_item(Db2Parser.Alter_workload_opts_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#package_executable}.
	 * @param ctx the parse tree
	 */
	void enterPackage_executable(Db2Parser.Package_executableContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#package_executable}.
	 * @param ctx the parse tree
	 */
	void exitPackage_executable(Db2Parser.Package_executableContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#base_none}.
	 * @param ctx the parse tree
	 */
	void enterBase_none(Db2Parser.Base_noneContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#base_none}.
	 * @param ctx the parse tree
	 */
	void exitBase_none(Db2Parser.Base_noneContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#extended_base_none}.
	 * @param ctx the parse tree
	 */
	void enterExtended_base_none(Db2Parser.Extended_base_noneContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#extended_base_none}.
	 * @param ctx the parse tree
	 */
	void exitExtended_base_none(Db2Parser.Extended_base_noneContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_collect_activity_data_clause}.
	 * @param ctx the parse tree
	 */
	void enterAlter_collect_activity_data_clause(Db2Parser.Alter_collect_activity_data_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_collect_activity_data_clause}.
	 * @param ctx the parse tree
	 */
	void exitAlter_collect_activity_data_clause(Db2Parser.Alter_collect_activity_data_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#with_opts}.
	 * @param ctx the parse tree
	 */
	void enterWith_opts(Db2Parser.With_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#with_opts}.
	 * @param ctx the parse tree
	 */
	void exitWith_opts(Db2Parser.With_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_collect_history_clause}.
	 * @param ctx the parse tree
	 */
	void enterAlter_collect_history_clause(Db2Parser.Alter_collect_history_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_collect_history_clause}.
	 * @param ctx the parse tree
	 */
	void exitAlter_collect_history_clause(Db2Parser.Alter_collect_history_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_collect_lock_wait_data_clause}.
	 * @param ctx the parse tree
	 */
	void enterAlter_collect_lock_wait_data_clause(Db2Parser.Alter_collect_lock_wait_data_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_collect_lock_wait_data_clause}.
	 * @param ctx the parse tree
	 */
	void exitAlter_collect_lock_wait_data_clause(Db2Parser.Alter_collect_lock_wait_data_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_wrapper_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_wrapper_statement(Db2Parser.Alter_wrapper_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_wrapper_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_wrapper_statement(Db2Parser.Alter_wrapper_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_wrapper_opts_item}.
	 * @param ctx the parse tree
	 */
	void enterAlter_wrapper_opts_item(Db2Parser.Alter_wrapper_opts_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_wrapper_opts_item}.
	 * @param ctx the parse tree
	 */
	void exitAlter_wrapper_opts_item(Db2Parser.Alter_wrapper_opts_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alter_xsrobject_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_xsrobject_statement(Db2Parser.Alter_xsrobject_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alter_xsrobject_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_xsrobject_statement(Db2Parser.Alter_xsrobject_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#string}.
	 * @param ctx the parse tree
	 */
	void enterString(Db2Parser.StringContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#string}.
	 * @param ctx the parse tree
	 */
	void exitString(Db2Parser.StringContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#string_constant}.
	 * @param ctx the parse tree
	 */
	void enterString_constant(Db2Parser.String_constantContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#string_constant}.
	 * @param ctx the parse tree
	 */
	void exitString_constant(Db2Parser.String_constantContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#numeric_constant}.
	 * @param ctx the parse tree
	 */
	void enterNumeric_constant(Db2Parser.Numeric_constantContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#numeric_constant}.
	 * @param ctx the parse tree
	 */
	void exitNumeric_constant(Db2Parser.Numeric_constantContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#data_type}.
	 * @param ctx the parse tree
	 */
	void enterData_type(Db2Parser.Data_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#data_type}.
	 * @param ctx the parse tree
	 */
	void exitData_type(Db2Parser.Data_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#anchored_data_type}.
	 * @param ctx the parse tree
	 */
	void enterAnchored_data_type(Db2Parser.Anchored_data_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#anchored_data_type}.
	 * @param ctx the parse tree
	 */
	void exitAnchored_data_type(Db2Parser.Anchored_data_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#anchored_non_row_data_type}.
	 * @param ctx the parse tree
	 */
	void enterAnchored_non_row_data_type(Db2Parser.Anchored_non_row_data_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#anchored_non_row_data_type}.
	 * @param ctx the parse tree
	 */
	void exitAnchored_non_row_data_type(Db2Parser.Anchored_non_row_data_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#anchored_row_data_type}.
	 * @param ctx the parse tree
	 */
	void enterAnchored_row_data_type(Db2Parser.Anchored_row_data_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#anchored_row_data_type}.
	 * @param ctx the parse tree
	 */
	void exitAnchored_row_data_type(Db2Parser.Anchored_row_data_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#source_data_type}.
	 * @param ctx the parse tree
	 */
	void enterSource_data_type(Db2Parser.Source_data_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#source_data_type}.
	 * @param ctx the parse tree
	 */
	void exitSource_data_type(Db2Parser.Source_data_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#data_type_constrainst}.
	 * @param ctx the parse tree
	 */
	void enterData_type_constrainst(Db2Parser.Data_type_constrainstContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#data_type_constrainst}.
	 * @param ctx the parse tree
	 */
	void exitData_type_constrainst(Db2Parser.Data_type_constrainstContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#check_condition}.
	 * @param ctx the parse tree
	 */
	void enterCheck_condition(Db2Parser.Check_conditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#check_condition}.
	 * @param ctx the parse tree
	 */
	void exitCheck_condition(Db2Parser.Check_conditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#data_type_2}.
	 * @param ctx the parse tree
	 */
	void enterData_type_2(Db2Parser.Data_type_2Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#data_type_2}.
	 * @param ctx the parse tree
	 */
	void exitData_type_2(Db2Parser.Data_type_2Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#built_in_type}.
	 * @param ctx the parse tree
	 */
	void enterBuilt_in_type(Db2Parser.Built_in_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#built_in_type}.
	 * @param ctx the parse tree
	 */
	void exitBuilt_in_type(Db2Parser.Built_in_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#integer_paren}.
	 * @param ctx the parse tree
	 */
	void enterInteger_paren(Db2Parser.Integer_parenContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#integer_paren}.
	 * @param ctx the parse tree
	 */
	void exitInteger_paren(Db2Parser.Integer_parenContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#integer_kmg_paren}.
	 * @param ctx the parse tree
	 */
	void enterInteger_kmg_paren(Db2Parser.Integer_kmg_parenContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#integer_kmg_paren}.
	 * @param ctx the parse tree
	 */
	void exitInteger_kmg_paren(Db2Parser.Integer_kmg_parenContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#char_character}.
	 * @param ctx the parse tree
	 */
	void enterChar_character(Db2Parser.Char_characterContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#char_character}.
	 * @param ctx the parse tree
	 */
	void exitChar_character(Db2Parser.Char_characterContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#octets_codeunits}.
	 * @param ctx the parse tree
	 */
	void enterOctets_codeunits(Db2Parser.Octets_codeunitsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#octets_codeunits}.
	 * @param ctx the parse tree
	 */
	void exitOctets_codeunits(Db2Parser.Octets_codeunitsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#codeunits}.
	 * @param ctx the parse tree
	 */
	void enterCodeunits(Db2Parser.CodeunitsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#codeunits}.
	 * @param ctx the parse tree
	 */
	void exitCodeunits(Db2Parser.CodeunitsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#kmg}.
	 * @param ctx the parse tree
	 */
	void enterKmg(Db2Parser.KmgContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#kmg}.
	 * @param ctx the parse tree
	 */
	void exitKmg(Db2Parser.KmgContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#rs_locator_variable}.
	 * @param ctx the parse tree
	 */
	void enterRs_locator_variable(Db2Parser.Rs_locator_variableContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#rs_locator_variable}.
	 * @param ctx the parse tree
	 */
	void exitRs_locator_variable(Db2Parser.Rs_locator_variableContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#integer_constant_list}.
	 * @param ctx the parse tree
	 */
	void enterInteger_constant_list(Db2Parser.Integer_constant_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#integer_constant_list}.
	 * @param ctx the parse tree
	 */
	void exitInteger_constant_list(Db2Parser.Integer_constant_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#integer_constant}.
	 * @param ctx the parse tree
	 */
	void enterInteger_constant(Db2Parser.Integer_constantContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#integer_constant}.
	 * @param ctx the parse tree
	 */
	void exitInteger_constant(Db2Parser.Integer_constantContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#integer_value}.
	 * @param ctx the parse tree
	 */
	void enterInteger_value(Db2Parser.Integer_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#integer_value}.
	 * @param ctx the parse tree
	 */
	void exitInteger_value(Db2Parser.Integer_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#positive_integer}.
	 * @param ctx the parse tree
	 */
	void enterPositive_integer(Db2Parser.Positive_integerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#positive_integer}.
	 * @param ctx the parse tree
	 */
	void exitPositive_integer(Db2Parser.Positive_integerContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#bigint_value}.
	 * @param ctx the parse tree
	 */
	void enterBigint_value(Db2Parser.Bigint_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#bigint_value}.
	 * @param ctx the parse tree
	 */
	void exitBigint_value(Db2Parser.Bigint_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#bigint_constant}.
	 * @param ctx the parse tree
	 */
	void enterBigint_constant(Db2Parser.Bigint_constantContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#bigint_constant}.
	 * @param ctx the parse tree
	 */
	void exitBigint_constant(Db2Parser.Bigint_constantContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#member_number}.
	 * @param ctx the parse tree
	 */
	void enterMember_number(Db2Parser.Member_numberContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#member_number}.
	 * @param ctx the parse tree
	 */
	void exitMember_number(Db2Parser.Member_numberContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#version_id}.
	 * @param ctx the parse tree
	 */
	void enterVersion_id(Db2Parser.Version_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#version_id}.
	 * @param ctx the parse tree
	 */
	void exitVersion_id(Db2Parser.Version_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#drop_statement}.
	 * @param ctx the parse tree
	 */
	void enterDrop_statement(Db2Parser.Drop_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#drop_statement}.
	 * @param ctx the parse tree
	 */
	void exitDrop_statement(Db2Parser.Drop_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alias_designator}.
	 * @param ctx the parse tree
	 */
	void enterAlias_designator(Db2Parser.Alias_designatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alias_designator}.
	 * @param ctx the parse tree
	 */
	void exitAlias_designator(Db2Parser.Alias_designatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#service_class_designator}.
	 * @param ctx the parse tree
	 */
	void enterService_class_designator(Db2Parser.Service_class_designatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#service_class_designator}.
	 * @param ctx the parse tree
	 */
	void exitService_class_designator(Db2Parser.Service_class_designatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#tablespace_name_list}.
	 * @param ctx the parse tree
	 */
	void enterTablespace_name_list(Db2Parser.Tablespace_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#tablespace_name_list}.
	 * @param ctx the parse tree
	 */
	void exitTablespace_name_list(Db2Parser.Tablespace_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#associate_locators_statement}.
	 * @param ctx the parse tree
	 */
	void enterAssociate_locators_statement(Db2Parser.Associate_locators_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#associate_locators_statement}.
	 * @param ctx the parse tree
	 */
	void exitAssociate_locators_statement(Db2Parser.Associate_locators_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#audit_statement}.
	 * @param ctx the parse tree
	 */
	void enterAudit_statement(Db2Parser.Audit_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#audit_statement}.
	 * @param ctx the parse tree
	 */
	void exitAudit_statement(Db2Parser.Audit_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#begin_declare_section_statement}.
	 * @param ctx the parse tree
	 */
	void enterBegin_declare_section_statement(Db2Parser.Begin_declare_section_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#begin_declare_section_statement}.
	 * @param ctx the parse tree
	 */
	void exitBegin_declare_section_statement(Db2Parser.Begin_declare_section_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#call_statement}.
	 * @param ctx the parse tree
	 */
	void enterCall_statement(Db2Parser.Call_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#call_statement}.
	 * @param ctx the parse tree
	 */
	void exitCall_statement(Db2Parser.Call_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#arg_list_paren}.
	 * @param ctx the parse tree
	 */
	void enterArg_list_paren(Db2Parser.Arg_list_parenContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#arg_list_paren}.
	 * @param ctx the parse tree
	 */
	void exitArg_list_paren(Db2Parser.Arg_list_parenContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#arg_list}.
	 * @param ctx the parse tree
	 */
	void enterArg_list(Db2Parser.Arg_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#arg_list}.
	 * @param ctx the parse tree
	 */
	void exitArg_list(Db2Parser.Arg_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#argument}.
	 * @param ctx the parse tree
	 */
	void enterArgument(Db2Parser.ArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#argument}.
	 * @param ctx the parse tree
	 */
	void exitArgument(Db2Parser.ArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#case_statement}.
	 * @param ctx the parse tree
	 */
	void enterCase_statement(Db2Parser.Case_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#case_statement}.
	 * @param ctx the parse tree
	 */
	void exitCase_statement(Db2Parser.Case_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#searched_case_statement_when_clause}.
	 * @param ctx the parse tree
	 */
	void enterSearched_case_statement_when_clause(Db2Parser.Searched_case_statement_when_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#searched_case_statement_when_clause}.
	 * @param ctx the parse tree
	 */
	void exitSearched_case_statement_when_clause(Db2Parser.Searched_case_statement_when_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#simple_case_statement_when_clause}.
	 * @param ctx the parse tree
	 */
	void enterSimple_case_statement_when_clause(Db2Parser.Simple_case_statement_when_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#simple_case_statement_when_clause}.
	 * @param ctx the parse tree
	 */
	void exitSimple_case_statement_when_clause(Db2Parser.Simple_case_statement_when_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#close_statement}.
	 * @param ctx the parse tree
	 */
	void enterClose_statement(Db2Parser.Close_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#close_statement}.
	 * @param ctx the parse tree
	 */
	void exitClose_statement(Db2Parser.Close_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#comment_statement}.
	 * @param ctx the parse tree
	 */
	void enterComment_statement(Db2Parser.Comment_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#comment_statement}.
	 * @param ctx the parse tree
	 */
	void exitComment_statement(Db2Parser.Comment_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#column_comment}.
	 * @param ctx the parse tree
	 */
	void enterColumn_comment(Db2Parser.Column_commentContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#column_comment}.
	 * @param ctx the parse tree
	 */
	void exitColumn_comment(Db2Parser.Column_commentContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#comment_objects}.
	 * @param ctx the parse tree
	 */
	void enterComment_objects(Db2Parser.Comment_objectsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#comment_objects}.
	 * @param ctx the parse tree
	 */
	void exitComment_objects(Db2Parser.Comment_objectsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#commit_statement}.
	 * @param ctx the parse tree
	 */
	void enterCommit_statement(Db2Parser.Commit_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#commit_statement}.
	 * @param ctx the parse tree
	 */
	void exitCommit_statement(Db2Parser.Commit_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#connect_type_1_statement}.
	 * @param ctx the parse tree
	 */
	void enterConnect_type_1_statement(Db2Parser.Connect_type_1_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#connect_type_1_statement}.
	 * @param ctx the parse tree
	 */
	void exitConnect_type_1_statement(Db2Parser.Connect_type_1_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#authorization}.
	 * @param ctx the parse tree
	 */
	void enterAuthorization(Db2Parser.AuthorizationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#authorization}.
	 * @param ctx the parse tree
	 */
	void exitAuthorization(Db2Parser.AuthorizationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#passwords}.
	 * @param ctx the parse tree
	 */
	void enterPasswords(Db2Parser.PasswordsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#passwords}.
	 * @param ctx the parse tree
	 */
	void exitPasswords(Db2Parser.PasswordsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#lock_block}.
	 * @param ctx the parse tree
	 */
	void enterLock_block(Db2Parser.Lock_blockContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#lock_block}.
	 * @param ctx the parse tree
	 */
	void exitLock_block(Db2Parser.Lock_blockContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#accesstoken}.
	 * @param ctx the parse tree
	 */
	void enterAccesstoken(Db2Parser.AccesstokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#accesstoken}.
	 * @param ctx the parse tree
	 */
	void exitAccesstoken(Db2Parser.AccesstokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#token}.
	 * @param ctx the parse tree
	 */
	void enterToken(Db2Parser.TokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#token}.
	 * @param ctx the parse tree
	 */
	void exitToken(Db2Parser.TokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#api_key}.
	 * @param ctx the parse tree
	 */
	void enterApi_key(Db2Parser.Api_keyContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#api_key}.
	 * @param ctx the parse tree
	 */
	void exitApi_key(Db2Parser.Api_keyContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#token_type}.
	 * @param ctx the parse tree
	 */
	void enterToken_type(Db2Parser.Token_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#token_type}.
	 * @param ctx the parse tree
	 */
	void exitToken_type(Db2Parser.Token_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#declare_cursor_statement}.
	 * @param ctx the parse tree
	 */
	void enterDeclare_cursor_statement(Db2Parser.Declare_cursor_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#declare_cursor_statement}.
	 * @param ctx the parse tree
	 */
	void exitDeclare_cursor_statement(Db2Parser.Declare_cursor_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#declare_global_temporary_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterDeclare_global_temporary_table_statement(Db2Parser.Declare_global_temporary_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#declare_global_temporary_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitDeclare_global_temporary_table_statement(Db2Parser.Declare_global_temporary_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#describe_statement}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_statement(Db2Parser.Describe_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#describe_statement}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_statement(Db2Parser.Describe_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#xquery_statement}.
	 * @param ctx the parse tree
	 */
	void enterXquery_statement(Db2Parser.Xquery_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#xquery_statement}.
	 * @param ctx the parse tree
	 */
	void exitXquery_statement(Db2Parser.Xquery_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#describe_input_statement}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_input_statement(Db2Parser.Describe_input_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#describe_input_statement}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_input_statement(Db2Parser.Describe_input_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#describe_output_statement}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_output_statement(Db2Parser.Describe_output_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#describe_output_statement}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_output_statement(Db2Parser.Describe_output_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#disconnect_statement}.
	 * @param ctx the parse tree
	 */
	void enterDisconnect_statement(Db2Parser.Disconnect_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#disconnect_statement}.
	 * @param ctx the parse tree
	 */
	void exitDisconnect_statement(Db2Parser.Disconnect_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#end_declare_section_statement}.
	 * @param ctx the parse tree
	 */
	void enterEnd_declare_section_statement(Db2Parser.End_declare_section_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#end_declare_section_statement}.
	 * @param ctx the parse tree
	 */
	void exitEnd_declare_section_statement(Db2Parser.End_declare_section_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#execute_statement}.
	 * @param ctx the parse tree
	 */
	void enterExecute_statement(Db2Parser.Execute_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#execute_statement}.
	 * @param ctx the parse tree
	 */
	void exitExecute_statement(Db2Parser.Execute_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#host_variable_expression}.
	 * @param ctx the parse tree
	 */
	void enterHost_variable_expression(Db2Parser.Host_variable_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#host_variable_expression}.
	 * @param ctx the parse tree
	 */
	void exitHost_variable_expression(Db2Parser.Host_variable_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#assignment_target}.
	 * @param ctx the parse tree
	 */
	void enterAssignment_target(Db2Parser.Assignment_targetContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#assignment_target}.
	 * @param ctx the parse tree
	 */
	void exitAssignment_target(Db2Parser.Assignment_targetContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#execute_immediate_statement}.
	 * @param ctx the parse tree
	 */
	void enterExecute_immediate_statement(Db2Parser.Execute_immediate_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#execute_immediate_statement}.
	 * @param ctx the parse tree
	 */
	void exitExecute_immediate_statement(Db2Parser.Execute_immediate_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#explain_statement}.
	 * @param ctx the parse tree
	 */
	void enterExplain_statement(Db2Parser.Explain_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#explain_statement}.
	 * @param ctx the parse tree
	 */
	void exitExplain_statement(Db2Parser.Explain_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#explainable_sql_statement}.
	 * @param ctx the parse tree
	 */
	void enterExplainable_sql_statement(Db2Parser.Explainable_sql_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#explainable_sql_statement}.
	 * @param ctx the parse tree
	 */
	void exitExplainable_sql_statement(Db2Parser.Explainable_sql_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#fetch_statement}.
	 * @param ctx the parse tree
	 */
	void enterFetch_statement(Db2Parser.Fetch_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#fetch_statement}.
	 * @param ctx the parse tree
	 */
	void exitFetch_statement(Db2Parser.Fetch_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#flush_bufferpools_statement}.
	 * @param ctx the parse tree
	 */
	void enterFlush_bufferpools_statement(Db2Parser.Flush_bufferpools_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#flush_bufferpools_statement}.
	 * @param ctx the parse tree
	 */
	void exitFlush_bufferpools_statement(Db2Parser.Flush_bufferpools_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#flush_event_monitor_statement}.
	 * @param ctx the parse tree
	 */
	void enterFlush_event_monitor_statement(Db2Parser.Flush_event_monitor_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#flush_event_monitor_statement}.
	 * @param ctx the parse tree
	 */
	void exitFlush_event_monitor_statement(Db2Parser.Flush_event_monitor_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#flush_federated_cache_statement}.
	 * @param ctx the parse tree
	 */
	void enterFlush_federated_cache_statement(Db2Parser.Flush_federated_cache_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#flush_federated_cache_statement}.
	 * @param ctx the parse tree
	 */
	void exitFlush_federated_cache_statement(Db2Parser.Flush_federated_cache_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#flush_optimization_profile_cache_statement}.
	 * @param ctx the parse tree
	 */
	void enterFlush_optimization_profile_cache_statement(Db2Parser.Flush_optimization_profile_cache_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#flush_optimization_profile_cache_statement}.
	 * @param ctx the parse tree
	 */
	void exitFlush_optimization_profile_cache_statement(Db2Parser.Flush_optimization_profile_cache_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#flush_package_cache_statement}.
	 * @param ctx the parse tree
	 */
	void enterFlush_package_cache_statement(Db2Parser.Flush_package_cache_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#flush_package_cache_statement}.
	 * @param ctx the parse tree
	 */
	void exitFlush_package_cache_statement(Db2Parser.Flush_package_cache_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#flush_authentication_cache_statement}.
	 * @param ctx the parse tree
	 */
	void enterFlush_authentication_cache_statement(Db2Parser.Flush_authentication_cache_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#flush_authentication_cache_statement}.
	 * @param ctx the parse tree
	 */
	void exitFlush_authentication_cache_statement(Db2Parser.Flush_authentication_cache_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#free_locator_statement}.
	 * @param ctx the parse tree
	 */
	void enterFree_locator_statement(Db2Parser.Free_locator_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#free_locator_statement}.
	 * @param ctx the parse tree
	 */
	void exitFree_locator_statement(Db2Parser.Free_locator_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#get_diagnostics_statement}.
	 * @param ctx the parse tree
	 */
	void enterGet_diagnostics_statement(Db2Parser.Get_diagnostics_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#get_diagnostics_statement}.
	 * @param ctx the parse tree
	 */
	void exitGet_diagnostics_statement(Db2Parser.Get_diagnostics_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#statement_information}.
	 * @param ctx the parse tree
	 */
	void enterStatement_information(Db2Parser.Statement_informationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#statement_information}.
	 * @param ctx the parse tree
	 */
	void exitStatement_information(Db2Parser.Statement_informationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#condition_information}.
	 * @param ctx the parse tree
	 */
	void enterCondition_information(Db2Parser.Condition_informationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#condition_information}.
	 * @param ctx the parse tree
	 */
	void exitCondition_information(Db2Parser.Condition_informationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#condition_var_assignment}.
	 * @param ctx the parse tree
	 */
	void enterCondition_var_assignment(Db2Parser.Condition_var_assignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#condition_var_assignment}.
	 * @param ctx the parse tree
	 */
	void exitCondition_var_assignment(Db2Parser.Condition_var_assignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#lock_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterLock_table_statement(Db2Parser.Lock_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#lock_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitLock_table_statement(Db2Parser.Lock_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#pipe_statement}.
	 * @param ctx the parse tree
	 */
	void enterPipe_statement(Db2Parser.Pipe_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#pipe_statement}.
	 * @param ctx the parse tree
	 */
	void exitPipe_statement(Db2Parser.Pipe_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#refresh_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterRefresh_table_statement(Db2Parser.Refresh_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#refresh_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitRefresh_table_statement(Db2Parser.Refresh_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#release_connection_statement}.
	 * @param ctx the parse tree
	 */
	void enterRelease_connection_statement(Db2Parser.Release_connection_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#release_connection_statement}.
	 * @param ctx the parse tree
	 */
	void exitRelease_connection_statement(Db2Parser.Release_connection_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#rename_statement}.
	 * @param ctx the parse tree
	 */
	void enterRename_statement(Db2Parser.Rename_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#rename_statement}.
	 * @param ctx the parse tree
	 */
	void exitRename_statement(Db2Parser.Rename_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#rename_stogroup_statement}.
	 * @param ctx the parse tree
	 */
	void enterRename_stogroup_statement(Db2Parser.Rename_stogroup_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#rename_stogroup_statement}.
	 * @param ctx the parse tree
	 */
	void exitRename_stogroup_statement(Db2Parser.Rename_stogroup_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#rename_tablespace_statement}.
	 * @param ctx the parse tree
	 */
	void enterRename_tablespace_statement(Db2Parser.Rename_tablespace_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#rename_tablespace_statement}.
	 * @param ctx the parse tree
	 */
	void exitRename_tablespace_statement(Db2Parser.Rename_tablespace_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#set_statement}.
	 * @param ctx the parse tree
	 */
	void enterSet_statement(Db2Parser.Set_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#set_statement}.
	 * @param ctx the parse tree
	 */
	void exitSet_statement(Db2Parser.Set_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#access_mode_clause}.
	 * @param ctx the parse tree
	 */
	void enterAccess_mode_clause(Db2Parser.Access_mode_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#access_mode_clause}.
	 * @param ctx the parse tree
	 */
	void exitAccess_mode_clause(Db2Parser.Access_mode_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#cascade_clause}.
	 * @param ctx the parse tree
	 */
	void enterCascade_clause(Db2Parser.Cascade_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#cascade_clause}.
	 * @param ctx the parse tree
	 */
	void exitCascade_clause(Db2Parser.Cascade_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#to_descendent_types}.
	 * @param ctx the parse tree
	 */
	void enterTo_descendent_types(Db2Parser.To_descendent_typesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#to_descendent_types}.
	 * @param ctx the parse tree
	 */
	void exitTo_descendent_types(Db2Parser.To_descendent_typesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_type_list}.
	 * @param ctx the parse tree
	 */
	void enterTable_type_list(Db2Parser.Table_type_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_type_list}.
	 * @param ctx the parse tree
	 */
	void exitTable_type_list(Db2Parser.Table_type_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_type}.
	 * @param ctx the parse tree
	 */
	void enterTable_type(Db2Parser.Table_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_type}.
	 * @param ctx the parse tree
	 */
	void exitTable_type(Db2Parser.Table_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_checked_options_list}.
	 * @param ctx the parse tree
	 */
	void enterTable_checked_options_list(Db2Parser.Table_checked_options_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_checked_options_list}.
	 * @param ctx the parse tree
	 */
	void exitTable_checked_options_list(Db2Parser.Table_checked_options_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_checked_options}.
	 * @param ctx the parse tree
	 */
	void enterTable_checked_options(Db2Parser.Table_checked_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_checked_options}.
	 * @param ctx the parse tree
	 */
	void exitTable_checked_options(Db2Parser.Table_checked_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#online_options}.
	 * @param ctx the parse tree
	 */
	void enterOnline_options(Db2Parser.Online_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#online_options}.
	 * @param ctx the parse tree
	 */
	void exitOnline_options(Db2Parser.Online_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#query_optimization_options}.
	 * @param ctx the parse tree
	 */
	void enterQuery_optimization_options(Db2Parser.Query_optimization_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#query_optimization_options}.
	 * @param ctx the parse tree
	 */
	void exitQuery_optimization_options(Db2Parser.Query_optimization_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#check_options}.
	 * @param ctx the parse tree
	 */
	void enterCheck_options(Db2Parser.Check_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#check_options}.
	 * @param ctx the parse tree
	 */
	void exitCheck_options(Db2Parser.Check_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#incremental_options}.
	 * @param ctx the parse tree
	 */
	void enterIncremental_options(Db2Parser.Incremental_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#incremental_options}.
	 * @param ctx the parse tree
	 */
	void exitIncremental_options(Db2Parser.Incremental_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#exception_clause}.
	 * @param ctx the parse tree
	 */
	void enterException_clause(Db2Parser.Exception_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#exception_clause}.
	 * @param ctx the parse tree
	 */
	void exitException_clause(Db2Parser.Exception_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#in_table_use_clause}.
	 * @param ctx the parse tree
	 */
	void enterIn_table_use_clause(Db2Parser.In_table_use_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#in_table_use_clause}.
	 * @param ctx the parse tree
	 */
	void exitIn_table_use_clause(Db2Parser.In_table_use_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_unchecked_options}.
	 * @param ctx the parse tree
	 */
	void enterTable_unchecked_options(Db2Parser.Table_unchecked_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_unchecked_options}.
	 * @param ctx the parse tree
	 */
	void exitTable_unchecked_options(Db2Parser.Table_unchecked_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#full_access}.
	 * @param ctx the parse tree
	 */
	void enterFull_access(Db2Parser.Full_accessContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#full_access}.
	 * @param ctx the parse tree
	 */
	void exitFull_access(Db2Parser.Full_accessContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#integrity_options}.
	 * @param ctx the parse tree
	 */
	void enterIntegrity_options(Db2Parser.Integrity_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#integrity_options}.
	 * @param ctx the parse tree
	 */
	void exitIntegrity_options(Db2Parser.Integrity_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#integrity_options_item}.
	 * @param ctx the parse tree
	 */
	void enterIntegrity_options_item(Db2Parser.Integrity_options_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#integrity_options_item}.
	 * @param ctx the parse tree
	 */
	void exitIntegrity_options_item(Db2Parser.Integrity_options_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#var_def_list}.
	 * @param ctx the parse tree
	 */
	void enterVar_def_list(Db2Parser.Var_def_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#var_def_list}.
	 * @param ctx the parse tree
	 */
	void exitVar_def_list(Db2Parser.Var_def_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#var_def}.
	 * @param ctx the parse tree
	 */
	void enterVar_def(Db2Parser.Var_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#var_def}.
	 * @param ctx the parse tree
	 */
	void exitVar_def(Db2Parser.Var_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#expr_null}.
	 * @param ctx the parse tree
	 */
	void enterExpr_null(Db2Parser.Expr_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#expr_null}.
	 * @param ctx the parse tree
	 */
	void exitExpr_null(Db2Parser.Expr_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#expr_null_default}.
	 * @param ctx the parse tree
	 */
	void enterExpr_null_default(Db2Parser.Expr_null_defaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#expr_null_default}.
	 * @param ctx the parse tree
	 */
	void exitExpr_null_default(Db2Parser.Expr_null_defaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#array_index}.
	 * @param ctx the parse tree
	 */
	void enterArray_index(Db2Parser.Array_indexContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#array_index}.
	 * @param ctx the parse tree
	 */
	void exitArray_index(Db2Parser.Array_indexContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#row_fullselect}.
	 * @param ctx the parse tree
	 */
	void enterRow_fullselect(Db2Parser.Row_fullselectContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#row_fullselect}.
	 * @param ctx the parse tree
	 */
	void exitRow_fullselect(Db2Parser.Row_fullselectContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#target_variable}.
	 * @param ctx the parse tree
	 */
	void enterTarget_variable(Db2Parser.Target_variableContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#target_variable}.
	 * @param ctx the parse tree
	 */
	void exitTarget_variable(Db2Parser.Target_variableContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#target_cursor_variable}.
	 * @param ctx the parse tree
	 */
	void enterTarget_cursor_variable(Db2Parser.Target_cursor_variableContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#target_cursor_variable}.
	 * @param ctx the parse tree
	 */
	void exitTarget_cursor_variable(Db2Parser.Target_cursor_variableContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#target_row_variable}.
	 * @param ctx the parse tree
	 */
	void enterTarget_row_variable(Db2Parser.Target_row_variableContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#target_row_variable}.
	 * @param ctx the parse tree
	 */
	void exitTarget_row_variable(Db2Parser.Target_row_variableContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#row_array_element_specification}.
	 * @param ctx the parse tree
	 */
	void enterRow_array_element_specification(Db2Parser.Row_array_element_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#row_array_element_specification}.
	 * @param ctx the parse tree
	 */
	void exitRow_array_element_specification(Db2Parser.Row_array_element_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#row_field_reference}.
	 * @param ctx the parse tree
	 */
	void enterRow_field_reference(Db2Parser.Row_field_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#row_field_reference}.
	 * @param ctx the parse tree
	 */
	void exitRow_field_reference(Db2Parser.Row_field_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#field_reference}.
	 * @param ctx the parse tree
	 */
	void enterField_reference(Db2Parser.Field_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#field_reference}.
	 * @param ctx the parse tree
	 */
	void exitField_reference(Db2Parser.Field_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#search_condition}.
	 * @param ctx the parse tree
	 */
	void enterSearch_condition(Db2Parser.Search_conditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#search_condition}.
	 * @param ctx the parse tree
	 */
	void exitSearch_condition(Db2Parser.Search_conditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(Db2Parser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(Db2Parser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#according_to_clause}.
	 * @param ctx the parse tree
	 */
	void enterAccording_to_clause(Db2Parser.According_to_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#according_to_clause}.
	 * @param ctx the parse tree
	 */
	void exitAccording_to_clause(Db2Parser.According_to_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#xml_schema_identification_list}.
	 * @param ctx the parse tree
	 */
	void enterXml_schema_identification_list(Db2Parser.Xml_schema_identification_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#xml_schema_identification_list}.
	 * @param ctx the parse tree
	 */
	void exitXml_schema_identification_list(Db2Parser.Xml_schema_identification_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#xml_schema_identification}.
	 * @param ctx the parse tree
	 */
	void enterXml_schema_identification(Db2Parser.Xml_schema_identificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#xml_schema_identification}.
	 * @param ctx the parse tree
	 */
	void exitXml_schema_identification(Db2Parser.Xml_schema_identificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#fullselect_in_parentheses}.
	 * @param ctx the parse tree
	 */
	void enterFullselect_in_parentheses(Db2Parser.Fullselect_in_parenthesesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#fullselect_in_parentheses}.
	 * @param ctx the parse tree
	 */
	void exitFullselect_in_parentheses(Db2Parser.Fullselect_in_parenthesesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#some_any_all}.
	 * @param ctx the parse tree
	 */
	void enterSome_any_all(Db2Parser.Some_any_allContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#some_any_all}.
	 * @param ctx the parse tree
	 */
	void exitSome_any_all(Db2Parser.Some_any_allContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#row_value_expression}.
	 * @param ctx the parse tree
	 */
	void enterRow_value_expression(Db2Parser.Row_value_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#row_value_expression}.
	 * @param ctx the parse tree
	 */
	void exitRow_value_expression(Db2Parser.Row_value_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void enterComparison_operator(Db2Parser.Comparison_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void exitComparison_operator(Db2Parser.Comparison_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#row_expression}.
	 * @param ctx the parse tree
	 */
	void enterRow_expression(Db2Parser.Row_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#row_expression}.
	 * @param ctx the parse tree
	 */
	void exitRow_expression(Db2Parser.Row_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#path_opt_list}.
	 * @param ctx the parse tree
	 */
	void enterPath_opt_list(Db2Parser.Path_opt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#path_opt_list}.
	 * @param ctx the parse tree
	 */
	void exitPath_opt_list(Db2Parser.Path_opt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#path_opt}.
	 * @param ctx the parse tree
	 */
	void enterPath_opt(Db2Parser.Path_optContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#path_opt}.
	 * @param ctx the parse tree
	 */
	void exitPath_opt(Db2Parser.Path_optContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#pkg_opt_list}.
	 * @param ctx the parse tree
	 */
	void enterPkg_opt_list(Db2Parser.Pkg_opt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#pkg_opt_list}.
	 * @param ctx the parse tree
	 */
	void exitPkg_opt_list(Db2Parser.Pkg_opt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#pkg_opt}.
	 * @param ctx the parse tree
	 */
	void enterPkg_opt(Db2Parser.Pkg_optContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#pkg_opt}.
	 * @param ctx the parse tree
	 */
	void exitPkg_opt(Db2Parser.Pkg_optContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#maintain_opt_list}.
	 * @param ctx the parse tree
	 */
	void enterMaintain_opt_list(Db2Parser.Maintain_opt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#maintain_opt_list}.
	 * @param ctx the parse tree
	 */
	void exitMaintain_opt_list(Db2Parser.Maintain_opt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#maintain_opt}.
	 * @param ctx the parse tree
	 */
	void enterMaintain_opt(Db2Parser.Maintain_optContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#maintain_opt}.
	 * @param ctx the parse tree
	 */
	void exitMaintain_opt(Db2Parser.Maintain_optContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#variable}.
	 * @param ctx the parse tree
	 */
	void enterVariable(Db2Parser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#variable}.
	 * @param ctx the parse tree
	 */
	void exitVariable(Db2Parser.VariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#host_variable}.
	 * @param ctx the parse tree
	 */
	void enterHost_variable(Db2Parser.Host_variableContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#host_variable}.
	 * @param ctx the parse tree
	 */
	void exitHost_variable(Db2Parser.Host_variableContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#set_integrity_statement}.
	 * @param ctx the parse tree
	 */
	void enterSet_integrity_statement(Db2Parser.Set_integrity_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#set_integrity_statement}.
	 * @param ctx the parse tree
	 */
	void exitSet_integrity_statement(Db2Parser.Set_integrity_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#transfer_ownership_statement}.
	 * @param ctx the parse tree
	 */
	void enterTransfer_ownership_statement(Db2Parser.Transfer_ownership_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#transfer_ownership_statement}.
	 * @param ctx the parse tree
	 */
	void exitTransfer_ownership_statement(Db2Parser.Transfer_ownership_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#objects}.
	 * @param ctx the parse tree
	 */
	void enterObjects(Db2Parser.ObjectsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#objects}.
	 * @param ctx the parse tree
	 */
	void exitObjects(Db2Parser.ObjectsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#whenever_statement}.
	 * @param ctx the parse tree
	 */
	void enterWhenever_statement(Db2Parser.Whenever_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#whenever_statement}.
	 * @param ctx the parse tree
	 */
	void exitWhenever_statement(Db2Parser.Whenever_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#for_statement}.
	 * @param ctx the parse tree
	 */
	void enterFor_statement(Db2Parser.For_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#for_statement}.
	 * @param ctx the parse tree
	 */
	void exitFor_statement(Db2Parser.For_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#goto_statement}.
	 * @param ctx the parse tree
	 */
	void enterGoto_statement(Db2Parser.Goto_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#goto_statement}.
	 * @param ctx the parse tree
	 */
	void exitGoto_statement(Db2Parser.Goto_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#if_statement}.
	 * @param ctx the parse tree
	 */
	void enterIf_statement(Db2Parser.If_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#if_statement}.
	 * @param ctx the parse tree
	 */
	void exitIf_statement(Db2Parser.If_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#include_statement}.
	 * @param ctx the parse tree
	 */
	void enterInclude_statement(Db2Parser.Include_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#include_statement}.
	 * @param ctx the parse tree
	 */
	void exitInclude_statement(Db2Parser.Include_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#resignal_statement}.
	 * @param ctx the parse tree
	 */
	void enterResignal_statement(Db2Parser.Resignal_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#resignal_statement}.
	 * @param ctx the parse tree
	 */
	void exitResignal_statement(Db2Parser.Resignal_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#signal_information}.
	 * @param ctx the parse tree
	 */
	void enterSignal_information(Db2Parser.Signal_informationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#signal_information}.
	 * @param ctx the parse tree
	 */
	void exitSignal_information(Db2Parser.Signal_informationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#diagnostic_string_constant}.
	 * @param ctx the parse tree
	 */
	void enterDiagnostic_string_constant(Db2Parser.Diagnostic_string_constantContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#diagnostic_string_constant}.
	 * @param ctx the parse tree
	 */
	void exitDiagnostic_string_constant(Db2Parser.Diagnostic_string_constantContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#signal_statement}.
	 * @param ctx the parse tree
	 */
	void enterSignal_statement(Db2Parser.Signal_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#signal_statement}.
	 * @param ctx the parse tree
	 */
	void exitSignal_statement(Db2Parser.Signal_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sqlstate_string_constant}.
	 * @param ctx the parse tree
	 */
	void enterSqlstate_string_constant(Db2Parser.Sqlstate_string_constantContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sqlstate_string_constant}.
	 * @param ctx the parse tree
	 */
	void exitSqlstate_string_constant(Db2Parser.Sqlstate_string_constantContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sqlstate_string_variable}.
	 * @param ctx the parse tree
	 */
	void enterSqlstate_string_variable(Db2Parser.Sqlstate_string_variableContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sqlstate_string_variable}.
	 * @param ctx the parse tree
	 */
	void exitSqlstate_string_variable(Db2Parser.Sqlstate_string_variableContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#signal_information_2}.
	 * @param ctx the parse tree
	 */
	void enterSignal_information_2(Db2Parser.Signal_information_2Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#signal_information_2}.
	 * @param ctx the parse tree
	 */
	void exitSignal_information_2(Db2Parser.Signal_information_2Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#diagnostic_string_expression}.
	 * @param ctx the parse tree
	 */
	void enterDiagnostic_string_expression(Db2Parser.Diagnostic_string_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#diagnostic_string_expression}.
	 * @param ctx the parse tree
	 */
	void exitDiagnostic_string_expression(Db2Parser.Diagnostic_string_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#iterate_statement}.
	 * @param ctx the parse tree
	 */
	void enterIterate_statement(Db2Parser.Iterate_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#iterate_statement}.
	 * @param ctx the parse tree
	 */
	void exitIterate_statement(Db2Parser.Iterate_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#leave_statement}.
	 * @param ctx the parse tree
	 */
	void enterLeave_statement(Db2Parser.Leave_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#leave_statement}.
	 * @param ctx the parse tree
	 */
	void exitLeave_statement(Db2Parser.Leave_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#loop_statement}.
	 * @param ctx the parse tree
	 */
	void enterLoop_statement(Db2Parser.Loop_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#loop_statement}.
	 * @param ctx the parse tree
	 */
	void exitLoop_statement(Db2Parser.Loop_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#open_statement}.
	 * @param ctx the parse tree
	 */
	void enterOpen_statement(Db2Parser.Open_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#open_statement}.
	 * @param ctx the parse tree
	 */
	void exitOpen_statement(Db2Parser.Open_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#variable_or_expression}.
	 * @param ctx the parse tree
	 */
	void enterVariable_or_expression(Db2Parser.Variable_or_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#variable_or_expression}.
	 * @param ctx the parse tree
	 */
	void exitVariable_or_expression(Db2Parser.Variable_or_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#select_into_statement}.
	 * @param ctx the parse tree
	 */
	void enterSelect_into_statement(Db2Parser.Select_into_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#select_into_statement}.
	 * @param ctx the parse tree
	 */
	void exitSelect_into_statement(Db2Parser.Select_into_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#values_into_statement}.
	 * @param ctx the parse tree
	 */
	void enterValues_into_statement(Db2Parser.Values_into_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#values_into_statement}.
	 * @param ctx the parse tree
	 */
	void exitValues_into_statement(Db2Parser.Values_into_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#prepare_statement}.
	 * @param ctx the parse tree
	 */
	void enterPrepare_statement(Db2Parser.Prepare_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#prepare_statement}.
	 * @param ctx the parse tree
	 */
	void exitPrepare_statement(Db2Parser.Prepare_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#repeat_statement}.
	 * @param ctx the parse tree
	 */
	void enterRepeat_statement(Db2Parser.Repeat_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#repeat_statement}.
	 * @param ctx the parse tree
	 */
	void exitRepeat_statement(Db2Parser.Repeat_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#return_statement}.
	 * @param ctx the parse tree
	 */
	void enterReturn_statement(Db2Parser.Return_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#return_statement}.
	 * @param ctx the parse tree
	 */
	void exitReturn_statement(Db2Parser.Return_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#while_statement}.
	 * @param ctx the parse tree
	 */
	void enterWhile_statement(Db2Parser.While_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#while_statement}.
	 * @param ctx the parse tree
	 */
	void exitWhile_statement(Db2Parser.While_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_routine_statement}.
	 * @param ctx the parse tree
	 */
	void enterSql_routine_statement(Db2Parser.Sql_routine_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_routine_statement}.
	 * @param ctx the parse tree
	 */
	void exitSql_routine_statement(Db2Parser.Sql_routine_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#common_table_expression}.
	 * @param ctx the parse tree
	 */
	void enterCommon_table_expression(Db2Parser.Common_table_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#common_table_expression}.
	 * @param ctx the parse tree
	 */
	void exitCommon_table_expression(Db2Parser.Common_table_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_alias_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_alias_statement(Db2Parser.Create_alias_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_alias_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_alias_statement(Db2Parser.Create_alias_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_alias}.
	 * @param ctx the parse tree
	 */
	void enterTable_alias(Db2Parser.Table_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_alias}.
	 * @param ctx the parse tree
	 */
	void exitTable_alias(Db2Parser.Table_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#module_alias}.
	 * @param ctx the parse tree
	 */
	void enterModule_alias(Db2Parser.Module_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#module_alias}.
	 * @param ctx the parse tree
	 */
	void exitModule_alias(Db2Parser.Module_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sequence_alias}.
	 * @param ctx the parse tree
	 */
	void enterSequence_alias(Db2Parser.Sequence_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sequence_alias}.
	 * @param ctx the parse tree
	 */
	void exitSequence_alias(Db2Parser.Sequence_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#or_replace}.
	 * @param ctx the parse tree
	 */
	void enterOr_replace(Db2Parser.Or_replaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#or_replace}.
	 * @param ctx the parse tree
	 */
	void exitOr_replace(Db2Parser.Or_replaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_audit_policy_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_audit_policy_statement(Db2Parser.Create_audit_policy_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_audit_policy_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_audit_policy_statement(Db2Parser.Create_audit_policy_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#audit_policy_opts}.
	 * @param ctx the parse tree
	 */
	void enterAudit_policy_opts(Db2Parser.Audit_policy_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#audit_policy_opts}.
	 * @param ctx the parse tree
	 */
	void exitAudit_policy_opts(Db2Parser.Audit_policy_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#audit_policy_categories_opts}.
	 * @param ctx the parse tree
	 */
	void enterAudit_policy_categories_opts(Db2Parser.Audit_policy_categories_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#audit_policy_categories_opts}.
	 * @param ctx the parse tree
	 */
	void exitAudit_policy_categories_opts(Db2Parser.Audit_policy_categories_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_bufferpool_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_bufferpool_statement(Db2Parser.Create_bufferpool_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_bufferpool_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_bufferpool_statement(Db2Parser.Create_bufferpool_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#bufferpool_opts}.
	 * @param ctx the parse tree
	 */
	void enterBufferpool_opts(Db2Parser.Bufferpool_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#bufferpool_opts}.
	 * @param ctx the parse tree
	 */
	void exitBufferpool_opts(Db2Parser.Bufferpool_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#except_clause}.
	 * @param ctx the parse tree
	 */
	void enterExcept_clause(Db2Parser.Except_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#except_clause}.
	 * @param ctx the parse tree
	 */
	void exitExcept_clause(Db2Parser.Except_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#member_list}.
	 * @param ctx the parse tree
	 */
	void enterMember_list(Db2Parser.Member_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#member_list}.
	 * @param ctx the parse tree
	 */
	void exitMember_list(Db2Parser.Member_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#member_list_item}.
	 * @param ctx the parse tree
	 */
	void enterMember_list_item(Db2Parser.Member_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#member_list_item}.
	 * @param ctx the parse tree
	 */
	void exitMember_list_item(Db2Parser.Member_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_database_partition_group_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_database_partition_group_statement(Db2Parser.Create_database_partition_group_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_database_partition_group_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_database_partition_group_statement(Db2Parser.Create_database_partition_group_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_event_monitor_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_event_monitor_statement(Db2Parser.Create_event_monitor_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_event_monitor_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_event_monitor_statement(Db2Parser.Create_event_monitor_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_event_monitor_activities_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_event_monitor_activities_statement(Db2Parser.Create_event_monitor_activities_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_event_monitor_activities_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_event_monitor_activities_statement(Db2Parser.Create_event_monitor_activities_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#formatted_event_table_info_3}.
	 * @param ctx the parse tree
	 */
	void enterFormatted_event_table_info_3(Db2Parser.Formatted_event_table_info_3Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#formatted_event_table_info_3}.
	 * @param ctx the parse tree
	 */
	void exitFormatted_event_table_info_3(Db2Parser.Formatted_event_table_info_3Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_event_monitor_change_history_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_event_monitor_change_history_statement(Db2Parser.Create_event_monitor_change_history_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_event_monitor_change_history_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_event_monitor_change_history_statement(Db2Parser.Create_event_monitor_change_history_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#event_control_list}.
	 * @param ctx the parse tree
	 */
	void enterEvent_control_list(Db2Parser.Event_control_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#event_control_list}.
	 * @param ctx the parse tree
	 */
	void exitEvent_control_list(Db2Parser.Event_control_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#event_control}.
	 * @param ctx the parse tree
	 */
	void enterEvent_control(Db2Parser.Event_controlContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#event_control}.
	 * @param ctx the parse tree
	 */
	void exitEvent_control(Db2Parser.Event_controlContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_event_monitor_locking_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_event_monitor_locking_statement(Db2Parser.Create_event_monitor_locking_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_event_monitor_locking_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_event_monitor_locking_statement(Db2Parser.Create_event_monitor_locking_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_event_monitor_package_cache_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_event_monitor_package_cache_statement(Db2Parser.Create_event_monitor_package_cache_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_event_monitor_package_cache_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_event_monitor_package_cache_statement(Db2Parser.Create_event_monitor_package_cache_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#filter_and_collection_options}.
	 * @param ctx the parse tree
	 */
	void enterFilter_and_collection_options(Db2Parser.Filter_and_collection_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#filter_and_collection_options}.
	 * @param ctx the parse tree
	 */
	void exitFilter_and_collection_options(Db2Parser.Filter_and_collection_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#event_condition}.
	 * @param ctx the parse tree
	 */
	void enterEvent_condition(Db2Parser.Event_conditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#event_condition}.
	 * @param ctx the parse tree
	 */
	void exitEvent_condition(Db2Parser.Event_conditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#event_condition_item}.
	 * @param ctx the parse tree
	 */
	void enterEvent_condition_item(Db2Parser.Event_condition_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#event_condition_item}.
	 * @param ctx the parse tree
	 */
	void exitEvent_condition_item(Db2Parser.Event_condition_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_event_monitor_statistics_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_event_monitor_statistics_statement(Db2Parser.Create_event_monitor_statistics_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_event_monitor_statistics_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_event_monitor_statistics_statement(Db2Parser.Create_event_monitor_statistics_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#event_monitor_statistics_opts}.
	 * @param ctx the parse tree
	 */
	void enterEvent_monitor_statistics_opts(Db2Parser.Event_monitor_statistics_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#event_monitor_statistics_opts}.
	 * @param ctx the parse tree
	 */
	void exitEvent_monitor_statistics_opts(Db2Parser.Event_monitor_statistics_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_event_monitor_threshold_violations_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_event_monitor_threshold_violations_statement(Db2Parser.Create_event_monitor_threshold_violations_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_event_monitor_threshold_violations_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_event_monitor_threshold_violations_statement(Db2Parser.Create_event_monitor_threshold_violations_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#formatted_event_table_info_2}.
	 * @param ctx the parse tree
	 */
	void enterFormatted_event_table_info_2(Db2Parser.Formatted_event_table_info_2Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#formatted_event_table_info_2}.
	 * @param ctx the parse tree
	 */
	void exitFormatted_event_table_info_2(Db2Parser.Formatted_event_table_info_2Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#file_options}.
	 * @param ctx the parse tree
	 */
	void enterFile_options(Db2Parser.File_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#file_options}.
	 * @param ctx the parse tree
	 */
	void exitFile_options(Db2Parser.File_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#event_monitor_threshold_opts}.
	 * @param ctx the parse tree
	 */
	void enterEvent_monitor_threshold_opts(Db2Parser.Event_monitor_threshold_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#event_monitor_threshold_opts}.
	 * @param ctx the parse tree
	 */
	void exitEvent_monitor_threshold_opts(Db2Parser.Event_monitor_threshold_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#pages}.
	 * @param ctx the parse tree
	 */
	void enterPages(Db2Parser.PagesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#pages}.
	 * @param ctx the parse tree
	 */
	void exitPages(Db2Parser.PagesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_event_monitor_unit_of_work}.
	 * @param ctx the parse tree
	 */
	void enterCreate_event_monitor_unit_of_work(Db2Parser.Create_event_monitor_unit_of_workContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_event_monitor_unit_of_work}.
	 * @param ctx the parse tree
	 */
	void exitCreate_event_monitor_unit_of_work(Db2Parser.Create_event_monitor_unit_of_workContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#formatted_event_table_info}.
	 * @param ctx the parse tree
	 */
	void enterFormatted_event_table_info(Db2Parser.Formatted_event_table_infoContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#formatted_event_table_info}.
	 * @param ctx the parse tree
	 */
	void exitFormatted_event_table_info(Db2Parser.Formatted_event_table_infoContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#autostart_manualstart}.
	 * @param ctx the parse tree
	 */
	void enterAutostart_manualstart(Db2Parser.Autostart_manualstartContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#autostart_manualstart}.
	 * @param ctx the parse tree
	 */
	void exitAutostart_manualstart(Db2Parser.Autostart_manualstartContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#evm_group}.
	 * @param ctx the parse tree
	 */
	void enterEvm_group(Db2Parser.Evm_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#evm_group}.
	 * @param ctx the parse tree
	 */
	void exitEvm_group(Db2Parser.Evm_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#target_table_options}.
	 * @param ctx the parse tree
	 */
	void enterTarget_table_options(Db2Parser.Target_table_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#target_table_options}.
	 * @param ctx the parse tree
	 */
	void exitTarget_table_options(Db2Parser.Target_table_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_external_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_external_table_statement(Db2Parser.Create_external_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_external_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_external_table_statement(Db2Parser.Create_external_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#ext_table_option}.
	 * @param ctx the parse tree
	 */
	void enterExt_table_option(Db2Parser.Ext_table_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#ext_table_option}.
	 * @param ctx the parse tree
	 */
	void exitExt_table_option(Db2Parser.Ext_table_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#ext_table_option_value}.
	 * @param ctx the parse tree
	 */
	void enterExt_table_option_value(Db2Parser.Ext_table_option_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#ext_table_option_value}.
	 * @param ctx the parse tree
	 */
	void exitExt_table_option_value(Db2Parser.Ext_table_option_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_function_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_function_statement(Db2Parser.Create_function_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_function_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_function_statement(Db2Parser.Create_function_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_function_aggregate_interface_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_function_aggregate_interface_statement(Db2Parser.Create_function_aggregate_interface_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_function_aggregate_interface_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_function_aggregate_interface_statement(Db2Parser.Create_function_aggregate_interface_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#agg_fn_param_decl}.
	 * @param ctx the parse tree
	 */
	void enterAgg_fn_param_decl(Db2Parser.Agg_fn_param_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#agg_fn_param_decl}.
	 * @param ctx the parse tree
	 */
	void exitAgg_fn_param_decl(Db2Parser.Agg_fn_param_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#agg_fn_option_list}.
	 * @param ctx the parse tree
	 */
	void enterAgg_fn_option_list(Db2Parser.Agg_fn_option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#agg_fn_option_list}.
	 * @param ctx the parse tree
	 */
	void exitAgg_fn_option_list(Db2Parser.Agg_fn_option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#state_variable_declaration}.
	 * @param ctx the parse tree
	 */
	void enterState_variable_declaration(Db2Parser.State_variable_declarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#state_variable_declaration}.
	 * @param ctx the parse tree
	 */
	void exitState_variable_declaration(Db2Parser.State_variable_declarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_function_external_scalar_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_function_external_scalar_statement(Db2Parser.Create_function_external_scalar_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_function_external_scalar_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_function_external_scalar_statement(Db2Parser.Create_function_external_scalar_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#ext_scalar_param_decl}.
	 * @param ctx the parse tree
	 */
	void enterExt_scalar_param_decl(Db2Parser.Ext_scalar_param_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#ext_scalar_param_decl}.
	 * @param ctx the parse tree
	 */
	void exitExt_scalar_param_decl(Db2Parser.Ext_scalar_param_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#ext_scalar_option_list}.
	 * @param ctx the parse tree
	 */
	void enterExt_scalar_option_list(Db2Parser.Ext_scalar_option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#ext_scalar_option_list}.
	 * @param ctx the parse tree
	 */
	void exitExt_scalar_option_list(Db2Parser.Ext_scalar_option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#ext_scalar_option_list_item}.
	 * @param ctx the parse tree
	 */
	void enterExt_scalar_option_list_item(Db2Parser.Ext_scalar_option_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#ext_scalar_option_list_item}.
	 * @param ctx the parse tree
	 */
	void exitExt_scalar_option_list_item(Db2Parser.Ext_scalar_option_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#predicate_specification}.
	 * @param ctx the parse tree
	 */
	void enterPredicate_specification(Db2Parser.Predicate_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#predicate_specification}.
	 * @param ctx the parse tree
	 */
	void exitPredicate_specification(Db2Parser.Predicate_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#data_filter}.
	 * @param ctx the parse tree
	 */
	void enterData_filter(Db2Parser.Data_filterContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#data_filter}.
	 * @param ctx the parse tree
	 */
	void exitData_filter(Db2Parser.Data_filterContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#index_exploitation}.
	 * @param ctx the parse tree
	 */
	void enterIndex_exploitation(Db2Parser.Index_exploitationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#index_exploitation}.
	 * @param ctx the parse tree
	 */
	void exitIndex_exploitation(Db2Parser.Index_exploitationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#exploitation_rule}.
	 * @param ctx the parse tree
	 */
	void enterExploitation_rule(Db2Parser.Exploitation_ruleContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#exploitation_rule}.
	 * @param ctx the parse tree
	 */
	void exitExploitation_rule(Db2Parser.Exploitation_ruleContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_function_external_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_function_external_table_statement(Db2Parser.Create_function_external_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_function_external_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_function_external_table_statement(Db2Parser.Create_function_external_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#ext_table_param_decl_list}.
	 * @param ctx the parse tree
	 */
	void enterExt_table_param_decl_list(Db2Parser.Ext_table_param_decl_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#ext_table_param_decl_list}.
	 * @param ctx the parse tree
	 */
	void exitExt_table_param_decl_list(Db2Parser.Ext_table_param_decl_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#ext_table_param_decl}.
	 * @param ctx the parse tree
	 */
	void enterExt_table_param_decl(Db2Parser.Ext_table_param_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#ext_table_param_decl}.
	 * @param ctx the parse tree
	 */
	void exitExt_table_param_decl(Db2Parser.Ext_table_param_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#ext_table_option_list}.
	 * @param ctx the parse tree
	 */
	void enterExt_table_option_list(Db2Parser.Ext_table_option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#ext_table_option_list}.
	 * @param ctx the parse tree
	 */
	void exitExt_table_option_list(Db2Parser.Ext_table_option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#ext_table_option_list_item}.
	 * @param ctx the parse tree
	 */
	void enterExt_table_option_list_item(Db2Parser.Ext_table_option_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#ext_table_option_list_item}.
	 * @param ctx the parse tree
	 */
	void exitExt_table_option_list_item(Db2Parser.Ext_table_option_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_function_old_db_external_function_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_function_old_db_external_function_statement(Db2Parser.Create_function_old_db_external_function_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_function_old_db_external_function_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_function_old_db_external_function_statement(Db2Parser.Create_function_old_db_external_function_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#oledb_option_list}.
	 * @param ctx the parse tree
	 */
	void enterOledb_option_list(Db2Parser.Oledb_option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#oledb_option_list}.
	 * @param ctx the parse tree
	 */
	void exitOledb_option_list(Db2Parser.Oledb_option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#oledb_option_list_item}.
	 * @param ctx the parse tree
	 */
	void enterOledb_option_list_item(Db2Parser.Oledb_option_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#oledb_option_list_item}.
	 * @param ctx the parse tree
	 */
	void exitOledb_option_list_item(Db2Parser.Oledb_option_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_function_sourced_or_template_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_function_sourced_or_template_statement(Db2Parser.Create_function_sourced_or_template_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_function_sourced_or_template_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_function_sourced_or_template_statement(Db2Parser.Create_function_sourced_or_template_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#fn_return_opts}.
	 * @param ctx the parse tree
	 */
	void enterFn_return_opts(Db2Parser.Fn_return_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#fn_return_opts}.
	 * @param ctx the parse tree
	 */
	void exitFn_return_opts(Db2Parser.Fn_return_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#fn_return_opts_item}.
	 * @param ctx the parse tree
	 */
	void enterFn_return_opts_item(Db2Parser.Fn_return_opts_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#fn_return_opts_item}.
	 * @param ctx the parse tree
	 */
	void exitFn_return_opts_item(Db2Parser.Fn_return_opts_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#template_opts}.
	 * @param ctx the parse tree
	 */
	void enterTemplate_opts(Db2Parser.Template_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#template_opts}.
	 * @param ctx the parse tree
	 */
	void exitTemplate_opts(Db2Parser.Template_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#template_opts_item}.
	 * @param ctx the parse tree
	 */
	void enterTemplate_opts_item(Db2Parser.Template_opts_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#template_opts_item}.
	 * @param ctx the parse tree
	 */
	void exitTemplate_opts_item(Db2Parser.Template_opts_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#ascii_unicode}.
	 * @param ctx the parse tree
	 */
	void enterAscii_unicode(Db2Parser.Ascii_unicodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#ascii_unicode}.
	 * @param ctx the parse tree
	 */
	void exitAscii_unicode(Db2Parser.Ascii_unicodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#param_decl_list_3}.
	 * @param ctx the parse tree
	 */
	void enterParam_decl_list_3(Db2Parser.Param_decl_list_3Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#param_decl_list_3}.
	 * @param ctx the parse tree
	 */
	void exitParam_decl_list_3(Db2Parser.Param_decl_list_3Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#param_decl_3}.
	 * @param ctx the parse tree
	 */
	void enterParam_decl_3(Db2Parser.Param_decl_3Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#param_decl_3}.
	 * @param ctx the parse tree
	 */
	void exitParam_decl_3(Db2Parser.Param_decl_3Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_function_sql_scalar_table_or_row_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_function_sql_scalar_table_or_row_statement(Db2Parser.Create_function_sql_scalar_table_or_row_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_function_sql_scalar_table_or_row_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_function_sql_scalar_table_or_row_statement(Db2Parser.Create_function_sql_scalar_table_or_row_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#param_decl_list_2}.
	 * @param ctx the parse tree
	 */
	void enterParam_decl_list_2(Db2Parser.Param_decl_list_2Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#param_decl_list_2}.
	 * @param ctx the parse tree
	 */
	void exitParam_decl_list_2(Db2Parser.Param_decl_list_2Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#param_decl_2}.
	 * @param ctx the parse tree
	 */
	void enterParam_decl_2(Db2Parser.Param_decl_2Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#param_decl_2}.
	 * @param ctx the parse tree
	 */
	void exitParam_decl_2(Db2Parser.Param_decl_2Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_function_body}.
	 * @param ctx the parse tree
	 */
	void enterSql_function_body(Db2Parser.Sql_function_bodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_function_body}.
	 * @param ctx the parse tree
	 */
	void exitSql_function_body(Db2Parser.Sql_function_bodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_function_mapping_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_function_mapping_statement(Db2Parser.Create_function_mapping_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_function_mapping_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_function_mapping_statement(Db2Parser.Create_function_mapping_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#function_options}.
	 * @param ctx the parse tree
	 */
	void enterFunction_options(Db2Parser.Function_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#function_options}.
	 * @param ctx the parse tree
	 */
	void exitFunction_options(Db2Parser.Function_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#function_option_name}.
	 * @param ctx the parse tree
	 */
	void enterFunction_option_name(Db2Parser.Function_option_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#function_option_name}.
	 * @param ctx the parse tree
	 */
	void exitFunction_option_name(Db2Parser.Function_option_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_global_temporary_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_global_temporary_table_statement(Db2Parser.Create_global_temporary_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_global_temporary_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_global_temporary_table_statement(Db2Parser.Create_global_temporary_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_global_temporary_table_opts}.
	 * @param ctx the parse tree
	 */
	void enterCreate_global_temporary_table_opts(Db2Parser.Create_global_temporary_table_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_global_temporary_table_opts}.
	 * @param ctx the parse tree
	 */
	void exitCreate_global_temporary_table_opts(Db2Parser.Create_global_temporary_table_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_global_temporary_table_item}.
	 * @param ctx the parse tree
	 */
	void enterCreate_global_temporary_table_item(Db2Parser.Create_global_temporary_table_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_global_temporary_table_item}.
	 * @param ctx the parse tree
	 */
	void exitCreate_global_temporary_table_item(Db2Parser.Create_global_temporary_table_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#delete_preserve}.
	 * @param ctx the parse tree
	 */
	void enterDelete_preserve(Db2Parser.Delete_preserveContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#delete_preserve}.
	 * @param ctx the parse tree
	 */
	void exitDelete_preserve(Db2Parser.Delete_preserveContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_histogram_template_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_histogram_template_statement(Db2Parser.Create_histogram_template_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_histogram_template_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_histogram_template_statement(Db2Parser.Create_histogram_template_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_index_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_index_statement(Db2Parser.Create_index_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_index_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_index_statement(Db2Parser.Create_index_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#index_col_opts}.
	 * @param ctx the parse tree
	 */
	void enterIndex_col_opts(Db2Parser.Index_col_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#index_col_opts}.
	 * @param ctx the parse tree
	 */
	void exitIndex_col_opts(Db2Parser.Index_col_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#index_col_opts_item}.
	 * @param ctx the parse tree
	 */
	void enterIndex_col_opts_item(Db2Parser.Index_col_opts_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#index_col_opts_item}.
	 * @param ctx the parse tree
	 */
	void exitIndex_col_opts_item(Db2Parser.Index_col_opts_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#key_expression}.
	 * @param ctx the parse tree
	 */
	void enterKey_expression(Db2Parser.Key_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#key_expression}.
	 * @param ctx the parse tree
	 */
	void exitKey_expression(Db2Parser.Key_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_index_extension_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_index_extension_statement(Db2Parser.Create_index_extension_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_index_extension_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_index_extension_statement(Db2Parser.Create_index_extension_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#param_list}.
	 * @param ctx the parse tree
	 */
	void enterParam_list(Db2Parser.Param_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#param_list}.
	 * @param ctx the parse tree
	 */
	void exitParam_list(Db2Parser.Param_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#index_maintenance}.
	 * @param ctx the parse tree
	 */
	void enterIndex_maintenance(Db2Parser.Index_maintenanceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#index_maintenance}.
	 * @param ctx the parse tree
	 */
	void exitIndex_maintenance(Db2Parser.Index_maintenanceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_function_invocation}.
	 * @param ctx the parse tree
	 */
	void enterTable_function_invocation(Db2Parser.Table_function_invocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_function_invocation}.
	 * @param ctx the parse tree
	 */
	void exitTable_function_invocation(Db2Parser.Table_function_invocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#index_search}.
	 * @param ctx the parse tree
	 */
	void enterIndex_search(Db2Parser.Index_searchContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#index_search}.
	 * @param ctx the parse tree
	 */
	void exitIndex_search(Db2Parser.Index_searchContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#search_method_definition}.
	 * @param ctx the parse tree
	 */
	void enterSearch_method_definition(Db2Parser.Search_method_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#search_method_definition}.
	 * @param ctx the parse tree
	 */
	void exitSearch_method_definition(Db2Parser.Search_method_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_mask_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_mask_statement(Db2Parser.Create_mask_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_mask_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_mask_statement(Db2Parser.Create_mask_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#case_expression}.
	 * @param ctx the parse tree
	 */
	void enterCase_expression(Db2Parser.Case_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#case_expression}.
	 * @param ctx the parse tree
	 */
	void exitCase_expression(Db2Parser.Case_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#range_producing_funciton_invocation}.
	 * @param ctx the parse tree
	 */
	void enterRange_producing_funciton_invocation(Db2Parser.Range_producing_funciton_invocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#range_producing_funciton_invocation}.
	 * @param ctx the parse tree
	 */
	void exitRange_producing_funciton_invocation(Db2Parser.Range_producing_funciton_invocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#index_filtering_function_invocation}.
	 * @param ctx the parse tree
	 */
	void enterIndex_filtering_function_invocation(Db2Parser.Index_filtering_function_invocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#index_filtering_function_invocation}.
	 * @param ctx the parse tree
	 */
	void exitIndex_filtering_function_invocation(Db2Parser.Index_filtering_function_invocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_method_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_method_statement(Db2Parser.Create_method_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_method_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_method_statement(Db2Parser.Create_method_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#method_opts}.
	 * @param ctx the parse tree
	 */
	void enterMethod_opts(Db2Parser.Method_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#method_opts}.
	 * @param ctx the parse tree
	 */
	void exitMethod_opts(Db2Parser.Method_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#method_opts_item}.
	 * @param ctx the parse tree
	 */
	void enterMethod_opts_item(Db2Parser.Method_opts_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#method_opts_item}.
	 * @param ctx the parse tree
	 */
	void exitMethod_opts_item(Db2Parser.Method_opts_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#method_signature}.
	 * @param ctx the parse tree
	 */
	void enterMethod_signature(Db2Parser.Method_signatureContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#method_signature}.
	 * @param ctx the parse tree
	 */
	void exitMethod_signature(Db2Parser.Method_signatureContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#method_param_list}.
	 * @param ctx the parse tree
	 */
	void enterMethod_param_list(Db2Parser.Method_param_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#method_param_list}.
	 * @param ctx the parse tree
	 */
	void exitMethod_param_list(Db2Parser.Method_param_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#data_type_3}.
	 * @param ctx the parse tree
	 */
	void enterData_type_3(Db2Parser.Data_type_3Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#data_type_3}.
	 * @param ctx the parse tree
	 */
	void exitData_type_3(Db2Parser.Data_type_3Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#data_type_4}.
	 * @param ctx the parse tree
	 */
	void enterData_type_4(Db2Parser.Data_type_4Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#data_type_4}.
	 * @param ctx the parse tree
	 */
	void exitData_type_4(Db2Parser.Data_type_4Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_method_body}.
	 * @param ctx the parse tree
	 */
	void enterSql_method_body(Db2Parser.Sql_method_bodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_method_body}.
	 * @param ctx the parse tree
	 */
	void exitSql_method_body(Db2Parser.Sql_method_bodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#compound_sql_inlined}.
	 * @param ctx the parse tree
	 */
	void enterCompound_sql_inlined(Db2Parser.Compound_sql_inlinedContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#compound_sql_inlined}.
	 * @param ctx the parse tree
	 */
	void exitCompound_sql_inlined(Db2Parser.Compound_sql_inlinedContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_statement_inlined}.
	 * @param ctx the parse tree
	 */
	void enterSql_statement_inlined(Db2Parser.Sql_statement_inlinedContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_statement_inlined}.
	 * @param ctx the parse tree
	 */
	void exitSql_statement_inlined(Db2Parser.Sql_statement_inlinedContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#compound_sql_compiled}.
	 * @param ctx the parse tree
	 */
	void enterCompound_sql_compiled(Db2Parser.Compound_sql_compiledContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#compound_sql_compiled}.
	 * @param ctx the parse tree
	 */
	void exitCompound_sql_compiled(Db2Parser.Compound_sql_compiledContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_statement_compiled}.
	 * @param ctx the parse tree
	 */
	void enterSql_statement_compiled(Db2Parser.Sql_statement_compiledContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_statement_compiled}.
	 * @param ctx the parse tree
	 */
	void exitSql_statement_compiled(Db2Parser.Sql_statement_compiledContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_module_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_module_statement(Db2Parser.Create_module_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_module_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_module_statement(Db2Parser.Create_module_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_nickname_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_nickname_statement(Db2Parser.Create_nickname_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_nickname_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_nickname_statement(Db2Parser.Create_nickname_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#nick_name_option_name}.
	 * @param ctx the parse tree
	 */
	void enterNick_name_option_name(Db2Parser.Nick_name_option_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#nick_name_option_name}.
	 * @param ctx the parse tree
	 */
	void exitNick_name_option_name(Db2Parser.Nick_name_option_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#remote_object_name}.
	 * @param ctx the parse tree
	 */
	void enterRemote_object_name(Db2Parser.Remote_object_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#remote_object_name}.
	 * @param ctx the parse tree
	 */
	void exitRemote_object_name(Db2Parser.Remote_object_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#non_relational_data_definition}.
	 * @param ctx the parse tree
	 */
	void enterNon_relational_data_definition(Db2Parser.Non_relational_data_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#non_relational_data_definition}.
	 * @param ctx the parse tree
	 */
	void exitNon_relational_data_definition(Db2Parser.Non_relational_data_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#nick_name_column_list}.
	 * @param ctx the parse tree
	 */
	void enterNick_name_column_list(Db2Parser.Nick_name_column_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#nick_name_column_list}.
	 * @param ctx the parse tree
	 */
	void exitNick_name_column_list(Db2Parser.Nick_name_column_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#nick_name_column_list_item}.
	 * @param ctx the parse tree
	 */
	void enterNick_name_column_list_item(Db2Parser.Nick_name_column_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#nick_name_column_list_item}.
	 * @param ctx the parse tree
	 */
	void exitNick_name_column_list_item(Db2Parser.Nick_name_column_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#nick_name_column_definition}.
	 * @param ctx the parse tree
	 */
	void enterNick_name_column_definition(Db2Parser.Nick_name_column_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#nick_name_column_definition}.
	 * @param ctx the parse tree
	 */
	void exitNick_name_column_definition(Db2Parser.Nick_name_column_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#nick_name_column_options}.
	 * @param ctx the parse tree
	 */
	void enterNick_name_column_options(Db2Parser.Nick_name_column_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#nick_name_column_options}.
	 * @param ctx the parse tree
	 */
	void exitNick_name_column_options(Db2Parser.Nick_name_column_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#federated_column_options}.
	 * @param ctx the parse tree
	 */
	void enterFederated_column_options(Db2Parser.Federated_column_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#federated_column_options}.
	 * @param ctx the parse tree
	 */
	void exitFederated_column_options(Db2Parser.Federated_column_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#column_option_name}.
	 * @param ctx the parse tree
	 */
	void enterColumn_option_name(Db2Parser.Column_option_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#column_option_name}.
	 * @param ctx the parse tree
	 */
	void exitColumn_option_name(Db2Parser.Column_option_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_permission_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_permission_statement(Db2Parser.Create_permission_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_permission_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_permission_statement(Db2Parser.Create_permission_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_procedure_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_procedure_statement(Db2Parser.Create_procedure_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_procedure_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_procedure_statement(Db2Parser.Create_procedure_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_procedure_external_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_procedure_external_statement(Db2Parser.Create_procedure_external_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_procedure_external_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_procedure_external_statement(Db2Parser.Create_procedure_external_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#proc_ext_param_list}.
	 * @param ctx the parse tree
	 */
	void enterProc_ext_param_list(Db2Parser.Proc_ext_param_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#proc_ext_param_list}.
	 * @param ctx the parse tree
	 */
	void exitProc_ext_param_list(Db2Parser.Proc_ext_param_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#proc_ext_param}.
	 * @param ctx the parse tree
	 */
	void enterProc_ext_param(Db2Parser.Proc_ext_paramContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#proc_ext_param}.
	 * @param ctx the parse tree
	 */
	void exitProc_ext_param(Db2Parser.Proc_ext_paramContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#option_list_2}.
	 * @param ctx the parse tree
	 */
	void enterOption_list_2(Db2Parser.Option_list_2Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#option_list_2}.
	 * @param ctx the parse tree
	 */
	void exitOption_list_2(Db2Parser.Option_list_2Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#option_list_2_item}.
	 * @param ctx the parse tree
	 */
	void enterOption_list_2_item(Db2Parser.Option_list_2_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#option_list_2_item}.
	 * @param ctx the parse tree
	 */
	void exitOption_list_2_item(Db2Parser.Option_list_2_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_procedure_sourced_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_procedure_sourced_statement(Db2Parser.Create_procedure_sourced_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_procedure_sourced_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_procedure_sourced_statement(Db2Parser.Create_procedure_sourced_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#source_procedure_clause}.
	 * @param ctx the parse tree
	 */
	void enterSource_procedure_clause(Db2Parser.Source_procedure_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#source_procedure_clause}.
	 * @param ctx the parse tree
	 */
	void exitSource_procedure_clause(Db2Parser.Source_procedure_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#source_object_name}.
	 * @param ctx the parse tree
	 */
	void enterSource_object_name(Db2Parser.Source_object_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#source_object_name}.
	 * @param ctx the parse tree
	 */
	void exitSource_object_name(Db2Parser.Source_object_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#option_list_1}.
	 * @param ctx the parse tree
	 */
	void enterOption_list_1(Db2Parser.Option_list_1Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#option_list_1}.
	 * @param ctx the parse tree
	 */
	void exitOption_list_1(Db2Parser.Option_list_1Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#option_list_1_item}.
	 * @param ctx the parse tree
	 */
	void enterOption_list_1_item(Db2Parser.Option_list_1_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#option_list_1_item}.
	 * @param ctx the parse tree
	 */
	void exitOption_list_1_item(Db2Parser.Option_list_1_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#result_set_element_number}.
	 * @param ctx the parse tree
	 */
	void enterResult_set_element_number(Db2Parser.Result_set_element_numberContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#result_set_element_number}.
	 * @param ctx the parse tree
	 */
	void exitResult_set_element_number(Db2Parser.Result_set_element_numberContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#unique_id}.
	 * @param ctx the parse tree
	 */
	void enterUnique_id(Db2Parser.Unique_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#unique_id}.
	 * @param ctx the parse tree
	 */
	void exitUnique_id(Db2Parser.Unique_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_procedure_sql_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_procedure_sql_statement(Db2Parser.Create_procedure_sql_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_procedure_sql_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_procedure_sql_statement(Db2Parser.Create_procedure_sql_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#proc_parameter_list}.
	 * @param ctx the parse tree
	 */
	void enterProc_parameter_list(Db2Parser.Proc_parameter_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#proc_parameter_list}.
	 * @param ctx the parse tree
	 */
	void exitProc_parameter_list(Db2Parser.Proc_parameter_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#proc_parameter_list_item}.
	 * @param ctx the parse tree
	 */
	void enterProc_parameter_list_item(Db2Parser.Proc_parameter_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#proc_parameter_list_item}.
	 * @param ctx the parse tree
	 */
	void exitProc_parameter_list_item(Db2Parser.Proc_parameter_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#in_out_inout}.
	 * @param ctx the parse tree
	 */
	void enterIn_out_inout(Db2Parser.In_out_inoutContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#in_out_inout}.
	 * @param ctx the parse tree
	 */
	void exitIn_out_inout(Db2Parser.In_out_inoutContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#option_list}.
	 * @param ctx the parse tree
	 */
	void enterOption_list(Db2Parser.Option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#option_list}.
	 * @param ctx the parse tree
	 */
	void exitOption_list(Db2Parser.Option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#option_list_item}.
	 * @param ctx the parse tree
	 */
	void enterOption_list_item(Db2Parser.Option_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#option_list_item}.
	 * @param ctx the parse tree
	 */
	void exitOption_list_item(Db2Parser.Option_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_procedure_body}.
	 * @param ctx the parse tree
	 */
	void enterSql_procedure_body(Db2Parser.Sql_procedure_bodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_procedure_body}.
	 * @param ctx the parse tree
	 */
	void exitSql_procedure_body(Db2Parser.Sql_procedure_bodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_role_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_role_statement(Db2Parser.Create_role_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_role_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_role_statement(Db2Parser.Create_role_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_schema_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_schema_statement(Db2Parser.Create_schema_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_schema_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_schema_statement(Db2Parser.Create_schema_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#schema_sql_statement}.
	 * @param ctx the parse tree
	 */
	void enterSchema_sql_statement(Db2Parser.Schema_sql_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#schema_sql_statement}.
	 * @param ctx the parse tree
	 */
	void exitSchema_sql_statement(Db2Parser.Schema_sql_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_security_label_component_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_security_label_component_statement(Db2Parser.Create_security_label_component_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_security_label_component_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_security_label_component_statement(Db2Parser.Create_security_label_component_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#array_clause}.
	 * @param ctx the parse tree
	 */
	void enterArray_clause(Db2Parser.Array_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#array_clause}.
	 * @param ctx the parse tree
	 */
	void exitArray_clause(Db2Parser.Array_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#set_clause}.
	 * @param ctx the parse tree
	 */
	void enterSet_clause(Db2Parser.Set_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#set_clause}.
	 * @param ctx the parse tree
	 */
	void exitSet_clause(Db2Parser.Set_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#tree_clause}.
	 * @param ctx the parse tree
	 */
	void enterTree_clause(Db2Parser.Tree_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#tree_clause}.
	 * @param ctx the parse tree
	 */
	void exitTree_clause(Db2Parser.Tree_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#tree_clause_item}.
	 * @param ctx the parse tree
	 */
	void enterTree_clause_item(Db2Parser.Tree_clause_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#tree_clause_item}.
	 * @param ctx the parse tree
	 */
	void exitTree_clause_item(Db2Parser.Tree_clause_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_security_label_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_security_label_statement(Db2Parser.Create_security_label_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_security_label_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_security_label_statement(Db2Parser.Create_security_label_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_security_label_item}.
	 * @param ctx the parse tree
	 */
	void enterCreate_security_label_item(Db2Parser.Create_security_label_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_security_label_item}.
	 * @param ctx the parse tree
	 */
	void exitCreate_security_label_item(Db2Parser.Create_security_label_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_security_policy_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_security_policy_statement(Db2Parser.Create_security_policy_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_security_policy_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_security_policy_statement(Db2Parser.Create_security_policy_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_sequence_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_sequence_statement(Db2Parser.Create_sequence_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_sequence_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_sequence_statement(Db2Parser.Create_sequence_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_sequence_opts}.
	 * @param ctx the parse tree
	 */
	void enterCreate_sequence_opts(Db2Parser.Create_sequence_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_sequence_opts}.
	 * @param ctx the parse tree
	 */
	void exitCreate_sequence_opts(Db2Parser.Create_sequence_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_sequence_opts_item}.
	 * @param ctx the parse tree
	 */
	void enterCreate_sequence_opts_item(Db2Parser.Create_sequence_opts_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_sequence_opts_item}.
	 * @param ctx the parse tree
	 */
	void exitCreate_sequence_opts_item(Db2Parser.Create_sequence_opts_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_service_class_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_service_class_statement(Db2Parser.Create_service_class_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_service_class_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_service_class_statement(Db2Parser.Create_service_class_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#high_medium_low}.
	 * @param ctx the parse tree
	 */
	void enterHigh_medium_low(Db2Parser.High_medium_lowContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#high_medium_low}.
	 * @param ctx the parse tree
	 */
	void exitHigh_medium_low(Db2Parser.High_medium_lowContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#on_off}.
	 * @param ctx the parse tree
	 */
	void enterOn_off(Db2Parser.On_offContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#on_off}.
	 * @param ctx the parse tree
	 */
	void exitOn_off(Db2Parser.On_offContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#soft_hard}.
	 * @param ctx the parse tree
	 */
	void enterSoft_hard(Db2Parser.Soft_hardContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#soft_hard}.
	 * @param ctx the parse tree
	 */
	void exitSoft_hard(Db2Parser.Soft_hardContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_server_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_server_statement(Db2Parser.Create_server_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_server_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_server_statement(Db2Parser.Create_server_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#password_}.
	 * @param ctx the parse tree
	 */
	void enterPassword_(Db2Parser.Password_Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#password_}.
	 * @param ctx the parse tree
	 */
	void exitPassword_(Db2Parser.Password_Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_stogroup_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_stogroup_statement(Db2Parser.Create_stogroup_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_stogroup_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_stogroup_statement(Db2Parser.Create_stogroup_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_stogroup_opts}.
	 * @param ctx the parse tree
	 */
	void enterCreate_stogroup_opts(Db2Parser.Create_stogroup_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_stogroup_opts}.
	 * @param ctx the parse tree
	 */
	void exitCreate_stogroup_opts(Db2Parser.Create_stogroup_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_synonym_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_synonym_statement(Db2Parser.Create_synonym_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_synonym_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_synonym_statement(Db2Parser.Create_synonym_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_statement(Db2Parser.Create_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_statement(Db2Parser.Create_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_table_opts}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_opts(Db2Parser.Create_table_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_table_opts}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_opts(Db2Parser.Create_table_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_option_list}.
	 * @param ctx the parse tree
	 */
	void enterTable_option_list(Db2Parser.Table_option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_option_list}.
	 * @param ctx the parse tree
	 */
	void exitTable_option_list(Db2Parser.Table_option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_option_list_item}.
	 * @param ctx the parse tree
	 */
	void enterTable_option_list_item(Db2Parser.Table_option_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_option_list_item}.
	 * @param ctx the parse tree
	 */
	void exitTable_option_list_item(Db2Parser.Table_option_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_option_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_option_name(Db2Parser.Table_option_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_option_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_option_name(Db2Parser.Table_option_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#element_list}.
	 * @param ctx the parse tree
	 */
	void enterElement_list(Db2Parser.Element_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#element_list}.
	 * @param ctx the parse tree
	 */
	void exitElement_list(Db2Parser.Element_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#element_list_item}.
	 * @param ctx the parse tree
	 */
	void enterElement_list_item(Db2Parser.Element_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#element_list_item}.
	 * @param ctx the parse tree
	 */
	void exitElement_list_item(Db2Parser.Element_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#column_definition}.
	 * @param ctx the parse tree
	 */
	void enterColumn_definition(Db2Parser.Column_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#column_definition}.
	 * @param ctx the parse tree
	 */
	void exitColumn_definition(Db2Parser.Column_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#period_definition}.
	 * @param ctx the parse tree
	 */
	void enterPeriod_definition(Db2Parser.Period_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#period_definition}.
	 * @param ctx the parse tree
	 */
	void exitPeriod_definition(Db2Parser.Period_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#unique_constraint}.
	 * @param ctx the parse tree
	 */
	void enterUnique_constraint(Db2Parser.Unique_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#unique_constraint}.
	 * @param ctx the parse tree
	 */
	void exitUnique_constraint(Db2Parser.Unique_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#referential_constraint}.
	 * @param ctx the parse tree
	 */
	void enterReferential_constraint(Db2Parser.Referential_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#referential_constraint}.
	 * @param ctx the parse tree
	 */
	void exitReferential_constraint(Db2Parser.Referential_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#check_constraint}.
	 * @param ctx the parse tree
	 */
	void enterCheck_constraint(Db2Parser.Check_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#check_constraint}.
	 * @param ctx the parse tree
	 */
	void exitCheck_constraint(Db2Parser.Check_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#column_options}.
	 * @param ctx the parse tree
	 */
	void enterColumn_options(Db2Parser.Column_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#column_options}.
	 * @param ctx the parse tree
	 */
	void exitColumn_options(Db2Parser.Column_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#column_options_item}.
	 * @param ctx the parse tree
	 */
	void enterColumn_options_item(Db2Parser.Column_options_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#column_options_item}.
	 * @param ctx the parse tree
	 */
	void exitColumn_options_item(Db2Parser.Column_options_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#references_clause}.
	 * @param ctx the parse tree
	 */
	void enterReferences_clause(Db2Parser.References_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#references_clause}.
	 * @param ctx the parse tree
	 */
	void exitReferences_clause(Db2Parser.References_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#rule_clause}.
	 * @param ctx the parse tree
	 */
	void enterRule_clause(Db2Parser.Rule_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#rule_clause}.
	 * @param ctx the parse tree
	 */
	void exitRule_clause(Db2Parser.Rule_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#constraint_attributes}.
	 * @param ctx the parse tree
	 */
	void enterConstraint_attributes(Db2Parser.Constraint_attributesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#constraint_attributes}.
	 * @param ctx the parse tree
	 */
	void exitConstraint_attributes(Db2Parser.Constraint_attributesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#default_clause}.
	 * @param ctx the parse tree
	 */
	void enterDefault_clause(Db2Parser.Default_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#default_clause}.
	 * @param ctx the parse tree
	 */
	void exitDefault_clause(Db2Parser.Default_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#default_values}.
	 * @param ctx the parse tree
	 */
	void enterDefault_values(Db2Parser.Default_valuesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#default_values}.
	 * @param ctx the parse tree
	 */
	void exitDefault_values(Db2Parser.Default_valuesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#generated_clause}.
	 * @param ctx the parse tree
	 */
	void enterGenerated_clause(Db2Parser.Generated_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#generated_clause}.
	 * @param ctx the parse tree
	 */
	void exitGenerated_clause(Db2Parser.Generated_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#datetime_special_register}.
	 * @param ctx the parse tree
	 */
	void enterDatetime_special_register(Db2Parser.Datetime_special_registerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#datetime_special_register}.
	 * @param ctx the parse tree
	 */
	void exitDatetime_special_register(Db2Parser.Datetime_special_registerContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#user_special_register}.
	 * @param ctx the parse tree
	 */
	void enterUser_special_register(Db2Parser.User_special_registerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#user_special_register}.
	 * @param ctx the parse tree
	 */
	void exitUser_special_register(Db2Parser.User_special_registerContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#cast_function}.
	 * @param ctx the parse tree
	 */
	void enterCast_function(Db2Parser.Cast_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#cast_function}.
	 * @param ctx the parse tree
	 */
	void exitCast_function(Db2Parser.Cast_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#identity_options}.
	 * @param ctx the parse tree
	 */
	void enterIdentity_options(Db2Parser.Identity_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#identity_options}.
	 * @param ctx the parse tree
	 */
	void exitIdentity_options(Db2Parser.Identity_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#identity_options_item}.
	 * @param ctx the parse tree
	 */
	void enterIdentity_options_item(Db2Parser.Identity_options_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#identity_options_item}.
	 * @param ctx the parse tree
	 */
	void exitIdentity_options_item(Db2Parser.Identity_options_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#as_row_change_timestamp_clause}.
	 * @param ctx the parse tree
	 */
	void enterAs_row_change_timestamp_clause(Db2Parser.As_row_change_timestamp_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#as_row_change_timestamp_clause}.
	 * @param ctx the parse tree
	 */
	void exitAs_row_change_timestamp_clause(Db2Parser.As_row_change_timestamp_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#as_generated_expression_clause}.
	 * @param ctx the parse tree
	 */
	void enterAs_generated_expression_clause(Db2Parser.As_generated_expression_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#as_generated_expression_clause}.
	 * @param ctx the parse tree
	 */
	void exitAs_generated_expression_clause(Db2Parser.As_generated_expression_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#generation_expression}.
	 * @param ctx the parse tree
	 */
	void enterGeneration_expression(Db2Parser.Generation_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#generation_expression}.
	 * @param ctx the parse tree
	 */
	void exitGeneration_expression(Db2Parser.Generation_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#as_row_transaction_timestamp_clause}.
	 * @param ctx the parse tree
	 */
	void enterAs_row_transaction_timestamp_clause(Db2Parser.As_row_transaction_timestamp_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#as_row_transaction_timestamp_clause}.
	 * @param ctx the parse tree
	 */
	void exitAs_row_transaction_timestamp_clause(Db2Parser.As_row_transaction_timestamp_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#as_row_transaction_start_id_clause}.
	 * @param ctx the parse tree
	 */
	void enterAs_row_transaction_start_id_clause(Db2Parser.As_row_transaction_start_id_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#as_row_transaction_start_id_clause}.
	 * @param ctx the parse tree
	 */
	void exitAs_row_transaction_start_id_clause(Db2Parser.As_row_transaction_start_id_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#oid_column_definition}.
	 * @param ctx the parse tree
	 */
	void enterOid_column_definition(Db2Parser.Oid_column_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#oid_column_definition}.
	 * @param ctx the parse tree
	 */
	void exitOid_column_definition(Db2Parser.Oid_column_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#range_partition_spec}.
	 * @param ctx the parse tree
	 */
	void enterRange_partition_spec(Db2Parser.Range_partition_specContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#range_partition_spec}.
	 * @param ctx the parse tree
	 */
	void exitRange_partition_spec(Db2Parser.Range_partition_specContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#partition_expression_list}.
	 * @param ctx the parse tree
	 */
	void enterPartition_expression_list(Db2Parser.Partition_expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#partition_expression_list}.
	 * @param ctx the parse tree
	 */
	void exitPartition_expression_list(Db2Parser.Partition_expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#partition_expression}.
	 * @param ctx the parse tree
	 */
	void enterPartition_expression(Db2Parser.Partition_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#partition_expression}.
	 * @param ctx the parse tree
	 */
	void exitPartition_expression(Db2Parser.Partition_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#partition_element_list}.
	 * @param ctx the parse tree
	 */
	void enterPartition_element_list(Db2Parser.Partition_element_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#partition_element_list}.
	 * @param ctx the parse tree
	 */
	void exitPartition_element_list(Db2Parser.Partition_element_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#partition_element}.
	 * @param ctx the parse tree
	 */
	void enterPartition_element(Db2Parser.Partition_elementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#partition_element}.
	 * @param ctx the parse tree
	 */
	void exitPartition_element(Db2Parser.Partition_elementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#boundary_spec}.
	 * @param ctx the parse tree
	 */
	void enterBoundary_spec(Db2Parser.Boundary_specContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#boundary_spec}.
	 * @param ctx the parse tree
	 */
	void exitBoundary_spec(Db2Parser.Boundary_specContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#partition_tablespace_options}.
	 * @param ctx the parse tree
	 */
	void enterPartition_tablespace_options(Db2Parser.Partition_tablespace_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#partition_tablespace_options}.
	 * @param ctx the parse tree
	 */
	void exitPartition_tablespace_options(Db2Parser.Partition_tablespace_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#duration_label}.
	 * @param ctx the parse tree
	 */
	void enterDuration_label(Db2Parser.Duration_labelContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#duration_label}.
	 * @param ctx the parse tree
	 */
	void exitDuration_label(Db2Parser.Duration_labelContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#starting_clause}.
	 * @param ctx the parse tree
	 */
	void enterStarting_clause(Db2Parser.Starting_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#starting_clause}.
	 * @param ctx the parse tree
	 */
	void exitStarting_clause(Db2Parser.Starting_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#const_min_max_list}.
	 * @param ctx the parse tree
	 */
	void enterConst_min_max_list(Db2Parser.Const_min_max_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#const_min_max_list}.
	 * @param ctx the parse tree
	 */
	void exitConst_min_max_list(Db2Parser.Const_min_max_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#const_min_max}.
	 * @param ctx the parse tree
	 */
	void enterConst_min_max(Db2Parser.Const_min_maxContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#const_min_max}.
	 * @param ctx the parse tree
	 */
	void exitConst_min_max(Db2Parser.Const_min_maxContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#ending_clause}.
	 * @param ctx the parse tree
	 */
	void enterEnding_clause(Db2Parser.Ending_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#ending_clause}.
	 * @param ctx the parse tree
	 */
	void exitEnding_clause(Db2Parser.Ending_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#typed_table_options}.
	 * @param ctx the parse tree
	 */
	void enterTyped_table_options(Db2Parser.Typed_table_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#typed_table_options}.
	 * @param ctx the parse tree
	 */
	void exitTyped_table_options(Db2Parser.Typed_table_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#typed_element_list}.
	 * @param ctx the parse tree
	 */
	void enterTyped_element_list(Db2Parser.Typed_element_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#typed_element_list}.
	 * @param ctx the parse tree
	 */
	void exitTyped_element_list(Db2Parser.Typed_element_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#typed_element_list_item}.
	 * @param ctx the parse tree
	 */
	void enterTyped_element_list_item(Db2Parser.Typed_element_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#typed_element_list_item}.
	 * @param ctx the parse tree
	 */
	void exitTyped_element_list_item(Db2Parser.Typed_element_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#as_result_table}.
	 * @param ctx the parse tree
	 */
	void enterAs_result_table(Db2Parser.As_result_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#as_result_table}.
	 * @param ctx the parse tree
	 */
	void exitAs_result_table(Db2Parser.As_result_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#copy_options}.
	 * @param ctx the parse tree
	 */
	void enterCopy_options(Db2Parser.Copy_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#copy_options}.
	 * @param ctx the parse tree
	 */
	void exitCopy_options(Db2Parser.Copy_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#materialized_query_options}.
	 * @param ctx the parse tree
	 */
	void enterMaterialized_query_options(Db2Parser.Materialized_query_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#materialized_query_options}.
	 * @param ctx the parse tree
	 */
	void exitMaterialized_query_options(Db2Parser.Materialized_query_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#staging_table_definition}.
	 * @param ctx the parse tree
	 */
	void enterStaging_table_definition(Db2Parser.Staging_table_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#staging_table_definition}.
	 * @param ctx the parse tree
	 */
	void exitStaging_table_definition(Db2Parser.Staging_table_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#dimensions_clause}.
	 * @param ctx the parse tree
	 */
	void enterDimensions_clause(Db2Parser.Dimensions_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#dimensions_clause}.
	 * @param ctx the parse tree
	 */
	void exitDimensions_clause(Db2Parser.Dimensions_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#col_names}.
	 * @param ctx the parse tree
	 */
	void enterCol_names(Db2Parser.Col_namesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#col_names}.
	 * @param ctx the parse tree
	 */
	void exitCol_names(Db2Parser.Col_namesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sequence_key_spec}.
	 * @param ctx the parse tree
	 */
	void enterSequence_key_spec(Db2Parser.Sequence_key_specContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sequence_key_spec}.
	 * @param ctx the parse tree
	 */
	void exitSequence_key_spec(Db2Parser.Sequence_key_specContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sequence_key_spec_list}.
	 * @param ctx the parse tree
	 */
	void enterSequence_key_spec_list(Db2Parser.Sequence_key_spec_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sequence_key_spec_list}.
	 * @param ctx the parse tree
	 */
	void exitSequence_key_spec_list(Db2Parser.Sequence_key_spec_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sequence_key_spec_list_item}.
	 * @param ctx the parse tree
	 */
	void enterSequence_key_spec_list_item(Db2Parser.Sequence_key_spec_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sequence_key_spec_list_item}.
	 * @param ctx the parse tree
	 */
	void exitSequence_key_spec_list_item(Db2Parser.Sequence_key_spec_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#tablespace_clauses}.
	 * @param ctx the parse tree
	 */
	void enterTablespace_clauses(Db2Parser.Tablespace_clausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#tablespace_clauses}.
	 * @param ctx the parse tree
	 */
	void exitTablespace_clauses(Db2Parser.Tablespace_clausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#distribution_clause}.
	 * @param ctx the parse tree
	 */
	void enterDistribution_clause(Db2Parser.Distribution_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#distribution_clause}.
	 * @param ctx the parse tree
	 */
	void exitDistribution_clause(Db2Parser.Distribution_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#partitioning_clause}.
	 * @param ctx the parse tree
	 */
	void enterPartitioning_clause(Db2Parser.Partitioning_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#partitioning_clause}.
	 * @param ctx the parse tree
	 */
	void exitPartitioning_clause(Db2Parser.Partitioning_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#if_not_exists}.
	 * @param ctx the parse tree
	 */
	void enterIf_not_exists(Db2Parser.If_not_existsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#if_not_exists}.
	 * @param ctx the parse tree
	 */
	void exitIf_not_exists(Db2Parser.If_not_existsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_tablespace_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_tablespace_statement(Db2Parser.Create_tablespace_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_tablespace_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_tablespace_statement(Db2Parser.Create_tablespace_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#storage_group}.
	 * @param ctx the parse tree
	 */
	void enterStorage_group(Db2Parser.Storage_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#storage_group}.
	 * @param ctx the parse tree
	 */
	void exitStorage_group(Db2Parser.Storage_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#size_attributes}.
	 * @param ctx the parse tree
	 */
	void enterSize_attributes(Db2Parser.Size_attributesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#size_attributes}.
	 * @param ctx the parse tree
	 */
	void exitSize_attributes(Db2Parser.Size_attributesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#system_containers}.
	 * @param ctx the parse tree
	 */
	void enterSystem_containers(Db2Parser.System_containersContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#system_containers}.
	 * @param ctx the parse tree
	 */
	void exitSystem_containers(Db2Parser.System_containersContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#container_string_list}.
	 * @param ctx the parse tree
	 */
	void enterContainer_string_list(Db2Parser.Container_string_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#container_string_list}.
	 * @param ctx the parse tree
	 */
	void exitContainer_string_list(Db2Parser.Container_string_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#database_containers}.
	 * @param ctx the parse tree
	 */
	void enterDatabase_containers(Db2Parser.Database_containersContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#database_containers}.
	 * @param ctx the parse tree
	 */
	void exitDatabase_containers(Db2Parser.Database_containersContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#container_clause}.
	 * @param ctx the parse tree
	 */
	void enterContainer_clause(Db2Parser.Container_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#container_clause}.
	 * @param ctx the parse tree
	 */
	void exitContainer_clause(Db2Parser.Container_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#container_clause_list}.
	 * @param ctx the parse tree
	 */
	void enterContainer_clause_list(Db2Parser.Container_clause_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#container_clause_list}.
	 * @param ctx the parse tree
	 */
	void exitContainer_clause_list(Db2Parser.Container_clause_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#container_clause_list_item}.
	 * @param ctx the parse tree
	 */
	void enterContainer_clause_list_item(Db2Parser.Container_clause_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#container_clause_list_item}.
	 * @param ctx the parse tree
	 */
	void exitContainer_clause_list_item(Db2Parser.Container_clause_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#on_db_partitions_clause}.
	 * @param ctx the parse tree
	 */
	void enterOn_db_partitions_clause(Db2Parser.On_db_partitions_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#on_db_partitions_clause}.
	 * @param ctx the parse tree
	 */
	void exitOn_db_partitions_clause(Db2Parser.On_db_partitions_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#db_partition_number_list}.
	 * @param ctx the parse tree
	 */
	void enterDb_partition_number_list(Db2Parser.Db_partition_number_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#db_partition_number_list}.
	 * @param ctx the parse tree
	 */
	void exitDb_partition_number_list(Db2Parser.Db_partition_number_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#db_partition_number_list_item}.
	 * @param ctx the parse tree
	 */
	void enterDb_partition_number_list_item(Db2Parser.Db_partition_number_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#db_partition_number_list_item}.
	 * @param ctx the parse tree
	 */
	void exitDb_partition_number_list_item(Db2Parser.Db_partition_number_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#db_partition_number}.
	 * @param ctx the parse tree
	 */
	void enterDb_partition_number(Db2Parser.Db_partition_numberContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#db_partition_number}.
	 * @param ctx the parse tree
	 */
	void exitDb_partition_number(Db2Parser.Db_partition_numberContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#number_of_pages}.
	 * @param ctx the parse tree
	 */
	void enterNumber_of_pages(Db2Parser.Number_of_pagesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#number_of_pages}.
	 * @param ctx the parse tree
	 */
	void exitNumber_of_pages(Db2Parser.Number_of_pagesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#number_of_files}.
	 * @param ctx the parse tree
	 */
	void enterNumber_of_files(Db2Parser.Number_of_filesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#number_of_files}.
	 * @param ctx the parse tree
	 */
	void exitNumber_of_files(Db2Parser.Number_of_filesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#number_of_milliseconds}.
	 * @param ctx the parse tree
	 */
	void enterNumber_of_milliseconds(Db2Parser.Number_of_millisecondsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#number_of_milliseconds}.
	 * @param ctx the parse tree
	 */
	void exitNumber_of_milliseconds(Db2Parser.Number_of_millisecondsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#number_megabytes_per_second}.
	 * @param ctx the parse tree
	 */
	void enterNumber_megabytes_per_second(Db2Parser.Number_megabytes_per_secondContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#number_megabytes_per_second}.
	 * @param ctx the parse tree
	 */
	void exitNumber_megabytes_per_second(Db2Parser.Number_megabytes_per_secondContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_threshold_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_threshold_statement(Db2Parser.Create_threshold_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_threshold_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_threshold_statement(Db2Parser.Create_threshold_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#threshold_domain}.
	 * @param ctx the parse tree
	 */
	void enterThreshold_domain(Db2Parser.Threshold_domainContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#threshold_domain}.
	 * @param ctx the parse tree
	 */
	void exitThreshold_domain(Db2Parser.Threshold_domainContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#statement_text}.
	 * @param ctx the parse tree
	 */
	void enterStatement_text(Db2Parser.Statement_textContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#statement_text}.
	 * @param ctx the parse tree
	 */
	void exitStatement_text(Db2Parser.Statement_textContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#executable_id}.
	 * @param ctx the parse tree
	 */
	void enterExecutable_id(Db2Parser.Executable_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#executable_id}.
	 * @param ctx the parse tree
	 */
	void exitExecutable_id(Db2Parser.Executable_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#enforcement_scope}.
	 * @param ctx the parse tree
	 */
	void enterEnforcement_scope(Db2Parser.Enforcement_scopeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#enforcement_scope}.
	 * @param ctx the parse tree
	 */
	void exitEnforcement_scope(Db2Parser.Enforcement_scopeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#threshold_predicate}.
	 * @param ctx the parse tree
	 */
	void enterThreshold_predicate(Db2Parser.Threshold_predicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#threshold_predicate}.
	 * @param ctx the parse tree
	 */
	void exitThreshold_predicate(Db2Parser.Threshold_predicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#checking_every}.
	 * @param ctx the parse tree
	 */
	void enterChecking_every(Db2Parser.Checking_everyContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#checking_every}.
	 * @param ctx the parse tree
	 */
	void exitChecking_every(Db2Parser.Checking_everyContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#hour_to_seconds}.
	 * @param ctx the parse tree
	 */
	void enterHour_to_seconds(Db2Parser.Hour_to_secondsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#hour_to_seconds}.
	 * @param ctx the parse tree
	 */
	void exitHour_to_seconds(Db2Parser.Hour_to_secondsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#day_to_minutes}.
	 * @param ctx the parse tree
	 */
	void enterDay_to_minutes(Db2Parser.Day_to_minutesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#day_to_minutes}.
	 * @param ctx the parse tree
	 */
	void exitDay_to_minutes(Db2Parser.Day_to_minutesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#day_to_seconds}.
	 * @param ctx the parse tree
	 */
	void enterDay_to_seconds(Db2Parser.Day_to_secondsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#day_to_seconds}.
	 * @param ctx the parse tree
	 */
	void exitDay_to_seconds(Db2Parser.Day_to_secondsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#threshold_exceeded_actions_2}.
	 * @param ctx the parse tree
	 */
	void enterThreshold_exceeded_actions_2(Db2Parser.Threshold_exceeded_actions_2Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#threshold_exceeded_actions_2}.
	 * @param ctx the parse tree
	 */
	void exitThreshold_exceeded_actions_2(Db2Parser.Threshold_exceeded_actions_2Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#details_section}.
	 * @param ctx the parse tree
	 */
	void enterDetails_section(Db2Parser.Details_sectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#details_section}.
	 * @param ctx the parse tree
	 */
	void exitDetails_section(Db2Parser.Details_sectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#remap_activity_action}.
	 * @param ctx the parse tree
	 */
	void enterRemap_activity_action(Db2Parser.Remap_activity_actionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#remap_activity_action}.
	 * @param ctx the parse tree
	 */
	void exitRemap_activity_action(Db2Parser.Remap_activity_actionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_transform_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_transform_statement(Db2Parser.Create_transform_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_transform_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_transform_statement(Db2Parser.Create_transform_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#tranform_list}.
	 * @param ctx the parse tree
	 */
	void enterTranform_list(Db2Parser.Tranform_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#tranform_list}.
	 * @param ctx the parse tree
	 */
	void exitTranform_list(Db2Parser.Tranform_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#tranform_list_item}.
	 * @param ctx the parse tree
	 */
	void enterTranform_list_item(Db2Parser.Tranform_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#tranform_list_item}.
	 * @param ctx the parse tree
	 */
	void exitTranform_list_item(Db2Parser.Tranform_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#transform_group_list}.
	 * @param ctx the parse tree
	 */
	void enterTransform_group_list(Db2Parser.Transform_group_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#transform_group_list}.
	 * @param ctx the parse tree
	 */
	void exitTransform_group_list(Db2Parser.Transform_group_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#transform_group_list_item}.
	 * @param ctx the parse tree
	 */
	void enterTransform_group_list_item(Db2Parser.Transform_group_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#transform_group_list_item}.
	 * @param ctx the parse tree
	 */
	void exitTransform_group_list_item(Db2Parser.Transform_group_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_trigger_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_trigger_statement(Db2Parser.Create_trigger_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_trigger_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_trigger_statement(Db2Parser.Create_trigger_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#ref_list}.
	 * @param ctx the parse tree
	 */
	void enterRef_list(Db2Parser.Ref_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#ref_list}.
	 * @param ctx the parse tree
	 */
	void exitRef_list(Db2Parser.Ref_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#ref_list_item}.
	 * @param ctx the parse tree
	 */
	void enterRef_list_item(Db2Parser.Ref_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#ref_list_item}.
	 * @param ctx the parse tree
	 */
	void exitRef_list_item(Db2Parser.Ref_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#old_new}.
	 * @param ctx the parse tree
	 */
	void enterOld_new(Db2Parser.Old_newContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#old_new}.
	 * @param ctx the parse tree
	 */
	void exitOld_new(Db2Parser.Old_newContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#correlation_name}.
	 * @param ctx the parse tree
	 */
	void enterCorrelation_name(Db2Parser.Correlation_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#correlation_name}.
	 * @param ctx the parse tree
	 */
	void exitCorrelation_name(Db2Parser.Correlation_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(Db2Parser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(Db2Parser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#trigger_event}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_event(Db2Parser.Trigger_eventContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#trigger_event}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_event(Db2Parser.Trigger_eventContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#triggered_action}.
	 * @param ctx the parse tree
	 */
	void enterTriggered_action(Db2Parser.Triggered_actionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#triggered_action}.
	 * @param ctx the parse tree
	 */
	void exitTriggered_action(Db2Parser.Triggered_actionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_procedure_statement}.
	 * @param ctx the parse tree
	 */
	void enterSql_procedure_statement(Db2Parser.Sql_procedure_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_procedure_statement}.
	 * @param ctx the parse tree
	 */
	void exitSql_procedure_statement(Db2Parser.Sql_procedure_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_function_statement}.
	 * @param ctx the parse tree
	 */
	void enterSql_function_statement(Db2Parser.Sql_function_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_function_statement}.
	 * @param ctx the parse tree
	 */
	void exitSql_function_statement(Db2Parser.Sql_function_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_trusted_context_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_trusted_context_statement(Db2Parser.Create_trusted_context_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_trusted_context_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_trusted_context_statement(Db2Parser.Create_trusted_context_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#attr_list}.
	 * @param ctx the parse tree
	 */
	void enterAttr_list(Db2Parser.Attr_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#attr_list}.
	 * @param ctx the parse tree
	 */
	void exitAttr_list(Db2Parser.Attr_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#attr_list_item}.
	 * @param ctx the parse tree
	 */
	void enterAttr_list_item(Db2Parser.Attr_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#attr_list_item}.
	 * @param ctx the parse tree
	 */
	void exitAttr_list_item(Db2Parser.Attr_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#auth_list}.
	 * @param ctx the parse tree
	 */
	void enterAuth_list(Db2Parser.Auth_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#auth_list}.
	 * @param ctx the parse tree
	 */
	void exitAuth_list(Db2Parser.Auth_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#auth_list_item}.
	 * @param ctx the parse tree
	 */
	void enterAuth_list_item(Db2Parser.Auth_list_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#auth_list_item}.
	 * @param ctx the parse tree
	 */
	void exitAuth_list_item(Db2Parser.Auth_list_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#address_value}.
	 * @param ctx the parse tree
	 */
	void enterAddress_value(Db2Parser.Address_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#address_value}.
	 * @param ctx the parse tree
	 */
	void exitAddress_value(Db2Parser.Address_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#encryption_value}.
	 * @param ctx the parse tree
	 */
	void enterEncryption_value(Db2Parser.Encryption_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#encryption_value}.
	 * @param ctx the parse tree
	 */
	void exitEncryption_value(Db2Parser.Encryption_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_type_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_type_statement(Db2Parser.Create_type_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_type_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_type_statement(Db2Parser.Create_type_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_type_array_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_type_array_statement(Db2Parser.Create_type_array_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_type_array_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_type_array_statement(Db2Parser.Create_type_array_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_type_cursor_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_type_cursor_statement(Db2Parser.Create_type_cursor_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_type_cursor_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_type_cursor_statement(Db2Parser.Create_type_cursor_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_type_distinct_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_type_distinct_statement(Db2Parser.Create_type_distinct_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_type_distinct_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_type_distinct_statement(Db2Parser.Create_type_distinct_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_type_row_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_type_row_statement(Db2Parser.Create_type_row_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_type_row_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_type_row_statement(Db2Parser.Create_type_row_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#field_definition_list_paren}.
	 * @param ctx the parse tree
	 */
	void enterField_definition_list_paren(Db2Parser.Field_definition_list_parenContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#field_definition_list_paren}.
	 * @param ctx the parse tree
	 */
	void exitField_definition_list_paren(Db2Parser.Field_definition_list_parenContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#field_definition_list}.
	 * @param ctx the parse tree
	 */
	void enterField_definition_list(Db2Parser.Field_definition_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#field_definition_list}.
	 * @param ctx the parse tree
	 */
	void exitField_definition_list(Db2Parser.Field_definition_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#field_definition}.
	 * @param ctx the parse tree
	 */
	void enterField_definition(Db2Parser.Field_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#field_definition}.
	 * @param ctx the parse tree
	 */
	void exitField_definition(Db2Parser.Field_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_type_structured_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_type_structured_statement(Db2Parser.Create_type_structured_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_type_structured_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_type_structured_statement(Db2Parser.Create_type_structured_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#structured_type_seq}.
	 * @param ctx the parse tree
	 */
	void enterStructured_type_seq(Db2Parser.Structured_type_seqContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#structured_type_seq}.
	 * @param ctx the parse tree
	 */
	void exitStructured_type_seq(Db2Parser.Structured_type_seqContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#attribute_definition_list_paren}.
	 * @param ctx the parse tree
	 */
	void enterAttribute_definition_list_paren(Db2Parser.Attribute_definition_list_parenContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#attribute_definition_list_paren}.
	 * @param ctx the parse tree
	 */
	void exitAttribute_definition_list_paren(Db2Parser.Attribute_definition_list_parenContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#attribute_definition_list}.
	 * @param ctx the parse tree
	 */
	void enterAttribute_definition_list(Db2Parser.Attribute_definition_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#attribute_definition_list}.
	 * @param ctx the parse tree
	 */
	void exitAttribute_definition_list(Db2Parser.Attribute_definition_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#attribute_definition}.
	 * @param ctx the parse tree
	 */
	void enterAttribute_definition(Db2Parser.Attribute_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#attribute_definition}.
	 * @param ctx the parse tree
	 */
	void exitAttribute_definition(Db2Parser.Attribute_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#method_specification_list}.
	 * @param ctx the parse tree
	 */
	void enterMethod_specification_list(Db2Parser.Method_specification_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#method_specification_list}.
	 * @param ctx the parse tree
	 */
	void exitMethod_specification_list(Db2Parser.Method_specification_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#method_specification}.
	 * @param ctx the parse tree
	 */
	void enterMethod_specification(Db2Parser.Method_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#method_specification}.
	 * @param ctx the parse tree
	 */
	void exitMethod_specification(Db2Parser.Method_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#method_specification_seq}.
	 * @param ctx the parse tree
	 */
	void enterMethod_specification_seq(Db2Parser.Method_specification_seqContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#method_specification_seq}.
	 * @param ctx the parse tree
	 */
	void exitMethod_specification_seq(Db2Parser.Method_specification_seqContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#as_locator}.
	 * @param ctx the parse tree
	 */
	void enterAs_locator(Db2Parser.As_locatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#as_locator}.
	 * @param ctx the parse tree
	 */
	void exitAs_locator(Db2Parser.As_locatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#param_decl_list_paren}.
	 * @param ctx the parse tree
	 */
	void enterParam_decl_list_paren(Db2Parser.Param_decl_list_parenContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#param_decl_list_paren}.
	 * @param ctx the parse tree
	 */
	void exitParam_decl_list_paren(Db2Parser.Param_decl_list_parenContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#param_decl_list}.
	 * @param ctx the parse tree
	 */
	void enterParam_decl_list(Db2Parser.Param_decl_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#param_decl_list}.
	 * @param ctx the parse tree
	 */
	void exitParam_decl_list(Db2Parser.Param_decl_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#param_decl}.
	 * @param ctx the parse tree
	 */
	void enterParam_decl(Db2Parser.Param_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#param_decl}.
	 * @param ctx the parse tree
	 */
	void exitParam_decl(Db2Parser.Param_declContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_routine_characteristics}.
	 * @param ctx the parse tree
	 */
	void enterSql_routine_characteristics(Db2Parser.Sql_routine_characteristicsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_routine_characteristics}.
	 * @param ctx the parse tree
	 */
	void exitSql_routine_characteristics(Db2Parser.Sql_routine_characteristicsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#external_routine_characteristics}.
	 * @param ctx the parse tree
	 */
	void enterExternal_routine_characteristics(Db2Parser.External_routine_characteristicsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#external_routine_characteristics}.
	 * @param ctx the parse tree
	 */
	void exitExternal_routine_characteristics(Db2Parser.External_routine_characteristicsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#length}.
	 * @param ctx the parse tree
	 */
	void enterLength(Db2Parser.LengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#length}.
	 * @param ctx the parse tree
	 */
	void exitLength(Db2Parser.LengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#rep_type}.
	 * @param ctx the parse tree
	 */
	void enterRep_type(Db2Parser.Rep_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#rep_type}.
	 * @param ctx the parse tree
	 */
	void exitRep_type(Db2Parser.Rep_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#varchars}.
	 * @param ctx the parse tree
	 */
	void enterVarchars(Db2Parser.VarcharsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#varchars}.
	 * @param ctx the parse tree
	 */
	void exitVarchars(Db2Parser.VarcharsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#varbinaries}.
	 * @param ctx the parse tree
	 */
	void enterVarbinaries(Db2Parser.VarbinariesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#varbinaries}.
	 * @param ctx the parse tree
	 */
	void exitVarbinaries(Db2Parser.VarbinariesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#for_bit_data}.
	 * @param ctx the parse tree
	 */
	void enterFor_bit_data(Db2Parser.For_bit_dataContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#for_bit_data}.
	 * @param ctx the parse tree
	 */
	void exitFor_bit_data(Db2Parser.For_bit_dataContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#lob_options}.
	 * @param ctx the parse tree
	 */
	void enterLob_options(Db2Parser.Lob_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#lob_options}.
	 * @param ctx the parse tree
	 */
	void exitLob_options(Db2Parser.Lob_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_type_mapping_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_type_mapping_statement(Db2Parser.Create_type_mapping_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_type_mapping_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_type_mapping_statement(Db2Parser.Create_type_mapping_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#for_bit_data_precision}.
	 * @param ctx the parse tree
	 */
	void enterFor_bit_data_precision(Db2Parser.For_bit_data_precisionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#for_bit_data_precision}.
	 * @param ctx the parse tree
	 */
	void exitFor_bit_data_precision(Db2Parser.For_bit_data_precisionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#precision}.
	 * @param ctx the parse tree
	 */
	void enterPrecision(Db2Parser.PrecisionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#precision}.
	 * @param ctx the parse tree
	 */
	void exitPrecision(Db2Parser.PrecisionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#scale}.
	 * @param ctx the parse tree
	 */
	void enterScale(Db2Parser.ScaleContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#scale}.
	 * @param ctx the parse tree
	 */
	void exitScale(Db2Parser.ScaleContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#precision_scale_comp}.
	 * @param ctx the parse tree
	 */
	void enterPrecision_scale_comp(Db2Parser.Precision_scale_compContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#precision_scale_comp}.
	 * @param ctx the parse tree
	 */
	void exitPrecision_scale_comp(Db2Parser.Precision_scale_compContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#from_to}.
	 * @param ctx the parse tree
	 */
	void enterFrom_to(Db2Parser.From_toContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#from_to}.
	 * @param ctx the parse tree
	 */
	void exitFrom_to(Db2Parser.From_toContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#data_source_data_type}.
	 * @param ctx the parse tree
	 */
	void enterData_source_data_type(Db2Parser.Data_source_data_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#data_source_data_type}.
	 * @param ctx the parse tree
	 */
	void exitData_source_data_type(Db2Parser.Data_source_data_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#local_data_type}.
	 * @param ctx the parse tree
	 */
	void enterLocal_data_type(Db2Parser.Local_data_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#local_data_type}.
	 * @param ctx the parse tree
	 */
	void exitLocal_data_type(Db2Parser.Local_data_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#remote_server}.
	 * @param ctx the parse tree
	 */
	void enterRemote_server(Db2Parser.Remote_serverContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#remote_server}.
	 * @param ctx the parse tree
	 */
	void exitRemote_server(Db2Parser.Remote_serverContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#server_version}.
	 * @param ctx the parse tree
	 */
	void enterServer_version(Db2Parser.Server_versionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#server_version}.
	 * @param ctx the parse tree
	 */
	void exitServer_version(Db2Parser.Server_versionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#server_type}.
	 * @param ctx the parse tree
	 */
	void enterServer_type(Db2Parser.Server_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#server_type}.
	 * @param ctx the parse tree
	 */
	void exitServer_type(Db2Parser.Server_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#version}.
	 * @param ctx the parse tree
	 */
	void enterVersion(Db2Parser.VersionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#version}.
	 * @param ctx the parse tree
	 */
	void exitVersion(Db2Parser.VersionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#release}.
	 * @param ctx the parse tree
	 */
	void enterRelease(Db2Parser.ReleaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#release}.
	 * @param ctx the parse tree
	 */
	void exitRelease(Db2Parser.ReleaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#mod}.
	 * @param ctx the parse tree
	 */
	void enterMod(Db2Parser.ModContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#mod}.
	 * @param ctx the parse tree
	 */
	void exitMod(Db2Parser.ModContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_usage_list_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_usage_list_statement(Db2Parser.Create_usage_list_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_usage_list_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_usage_list_statement(Db2Parser.Create_usage_list_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_user_mapping_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_user_mapping_statement(Db2Parser.Create_user_mapping_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_user_mapping_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_user_mapping_statement(Db2Parser.Create_user_mapping_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#user_mapping_options_paren}.
	 * @param ctx the parse tree
	 */
	void enterUser_mapping_options_paren(Db2Parser.User_mapping_options_parenContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#user_mapping_options_paren}.
	 * @param ctx the parse tree
	 */
	void exitUser_mapping_options_paren(Db2Parser.User_mapping_options_parenContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#user_mapping_options}.
	 * @param ctx the parse tree
	 */
	void enterUser_mapping_options(Db2Parser.User_mapping_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#user_mapping_options}.
	 * @param ctx the parse tree
	 */
	void exitUser_mapping_options(Db2Parser.User_mapping_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_variable_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_variable_statement(Db2Parser.Create_variable_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_variable_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_variable_statement(Db2Parser.Create_variable_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#constant_}.
	 * @param ctx the parse tree
	 */
	void enterConstant_(Db2Parser.Constant_Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#constant_}.
	 * @param ctx the parse tree
	 */
	void exitConstant_(Db2Parser.Constant_Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#special_register}.
	 * @param ctx the parse tree
	 */
	void enterSpecial_register(Db2Parser.Special_registerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#special_register}.
	 * @param ctx the parse tree
	 */
	void exitSpecial_register(Db2Parser.Special_registerContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#global_variable}.
	 * @param ctx the parse tree
	 */
	void enterGlobal_variable(Db2Parser.Global_variableContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#global_variable}.
	 * @param ctx the parse tree
	 */
	void exitGlobal_variable(Db2Parser.Global_variableContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#data_type_1}.
	 * @param ctx the parse tree
	 */
	void enterData_type_1(Db2Parser.Data_type_1Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#data_type_1}.
	 * @param ctx the parse tree
	 */
	void exitData_type_1(Db2Parser.Data_type_1Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#cursor_value_constructor}.
	 * @param ctx the parse tree
	 */
	void enterCursor_value_constructor(Db2Parser.Cursor_value_constructorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#cursor_value_constructor}.
	 * @param ctx the parse tree
	 */
	void exitCursor_value_constructor(Db2Parser.Cursor_value_constructorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#anchored_variable_data_type}.
	 * @param ctx the parse tree
	 */
	void enterAnchored_variable_data_type(Db2Parser.Anchored_variable_data_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#anchored_variable_data_type}.
	 * @param ctx the parse tree
	 */
	void exitAnchored_variable_data_type(Db2Parser.Anchored_variable_data_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#holdability}.
	 * @param ctx the parse tree
	 */
	void enterHoldability(Db2Parser.HoldabilityContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#holdability}.
	 * @param ctx the parse tree
	 */
	void exitHoldability(Db2Parser.HoldabilityContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#returnability}.
	 * @param ctx the parse tree
	 */
	void enterReturnability(Db2Parser.ReturnabilityContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#returnability}.
	 * @param ctx the parse tree
	 */
	void exitReturnability(Db2Parser.ReturnabilityContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_view_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_view_statement(Db2Parser.Create_view_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_view_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_view_statement(Db2Parser.Create_view_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_view_seq}.
	 * @param ctx the parse tree
	 */
	void enterCreate_view_seq(Db2Parser.Create_view_seqContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_view_seq}.
	 * @param ctx the parse tree
	 */
	void exitCreate_view_seq(Db2Parser.Create_view_seqContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#fullselect}.
	 * @param ctx the parse tree
	 */
	void enterFullselect(Db2Parser.FullselectContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#fullselect}.
	 * @param ctx the parse tree
	 */
	void exitFullselect(Db2Parser.FullselectContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#subselect}.
	 * @param ctx the parse tree
	 */
	void enterSubselect(Db2Parser.SubselectContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#subselect}.
	 * @param ctx the parse tree
	 */
	void exitSubselect(Db2Parser.SubselectContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#select_clause}.
	 * @param ctx the parse tree
	 */
	void enterSelect_clause(Db2Parser.Select_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#select_clause}.
	 * @param ctx the parse tree
	 */
	void exitSelect_clause(Db2Parser.Select_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#select_clause_item}.
	 * @param ctx the parse tree
	 */
	void enterSelect_clause_item(Db2Parser.Select_clause_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#select_clause_item}.
	 * @param ctx the parse tree
	 */
	void exitSelect_clause_item(Db2Parser.Select_clause_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#from_clause}.
	 * @param ctx the parse tree
	 */
	void enterFrom_clause(Db2Parser.From_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#from_clause}.
	 * @param ctx the parse tree
	 */
	void exitFrom_clause(Db2Parser.From_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_reference}.
	 * @param ctx the parse tree
	 */
	void enterTable_reference(Db2Parser.Table_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_reference}.
	 * @param ctx the parse tree
	 */
	void exitTable_reference(Db2Parser.Table_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_reference_list}.
	 * @param ctx the parse tree
	 */
	void enterTable_reference_list(Db2Parser.Table_reference_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_reference_list}.
	 * @param ctx the parse tree
	 */
	void exitTable_reference_list(Db2Parser.Table_reference_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#singles_table_reference}.
	 * @param ctx the parse tree
	 */
	void enterSingles_table_reference(Db2Parser.Singles_table_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#singles_table_reference}.
	 * @param ctx the parse tree
	 */
	void exitSingles_table_reference(Db2Parser.Singles_table_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#period_specification}.
	 * @param ctx the parse tree
	 */
	void enterPeriod_specification(Db2Parser.Period_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#period_specification}.
	 * @param ctx the parse tree
	 */
	void exitPeriod_specification(Db2Parser.Period_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(Db2Parser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(Db2Parser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#correlation_clause}.
	 * @param ctx the parse tree
	 */
	void enterCorrelation_clause(Db2Parser.Correlation_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#correlation_clause}.
	 * @param ctx the parse tree
	 */
	void exitCorrelation_clause(Db2Parser.Correlation_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#tablesample_clause}.
	 * @param ctx the parse tree
	 */
	void enterTablesample_clause(Db2Parser.Tablesample_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#tablesample_clause}.
	 * @param ctx the parse tree
	 */
	void exitTablesample_clause(Db2Parser.Tablesample_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#numeric_expression}.
	 * @param ctx the parse tree
	 */
	void enterNumeric_expression(Db2Parser.Numeric_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#numeric_expression}.
	 * @param ctx the parse tree
	 */
	void exitNumeric_expression(Db2Parser.Numeric_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#single_view_reference}.
	 * @param ctx the parse tree
	 */
	void enterSingle_view_reference(Db2Parser.Single_view_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#single_view_reference}.
	 * @param ctx the parse tree
	 */
	void exitSingle_view_reference(Db2Parser.Single_view_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#single_nickname_reference}.
	 * @param ctx the parse tree
	 */
	void enterSingle_nickname_reference(Db2Parser.Single_nickname_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#single_nickname_reference}.
	 * @param ctx the parse tree
	 */
	void exitSingle_nickname_reference(Db2Parser.Single_nickname_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#only_table_reference}.
	 * @param ctx the parse tree
	 */
	void enterOnly_table_reference(Db2Parser.Only_table_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#only_table_reference}.
	 * @param ctx the parse tree
	 */
	void exitOnly_table_reference(Db2Parser.Only_table_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#outer_table_reference}.
	 * @param ctx the parse tree
	 */
	void enterOuter_table_reference(Db2Parser.Outer_table_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#outer_table_reference}.
	 * @param ctx the parse tree
	 */
	void exitOuter_table_reference(Db2Parser.Outer_table_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#analyze_table_reference}.
	 * @param ctx the parse tree
	 */
	void enterAnalyze_table_reference(Db2Parser.Analyze_table_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#analyze_table_reference}.
	 * @param ctx the parse tree
	 */
	void exitAnalyze_table_reference(Db2Parser.Analyze_table_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#implementation_clause}.
	 * @param ctx the parse tree
	 */
	void enterImplementation_clause(Db2Parser.Implementation_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#implementation_clause}.
	 * @param ctx the parse tree
	 */
	void exitImplementation_clause(Db2Parser.Implementation_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#nested_table_reference}.
	 * @param ctx the parse tree
	 */
	void enterNested_table_reference(Db2Parser.Nested_table_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#nested_table_reference}.
	 * @param ctx the parse tree
	 */
	void exitNested_table_reference(Db2Parser.Nested_table_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#continue_handler}.
	 * @param ctx the parse tree
	 */
	void enterContinue_handler(Db2Parser.Continue_handlerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#continue_handler}.
	 * @param ctx the parse tree
	 */
	void exitContinue_handler(Db2Parser.Continue_handlerContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#specific_condition_value}.
	 * @param ctx the parse tree
	 */
	void enterSpecific_condition_value(Db2Parser.Specific_condition_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#specific_condition_value}.
	 * @param ctx the parse tree
	 */
	void exitSpecific_condition_value(Db2Parser.Specific_condition_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#data_change_table_reference}.
	 * @param ctx the parse tree
	 */
	void enterData_change_table_reference(Db2Parser.Data_change_table_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#data_change_table_reference}.
	 * @param ctx the parse tree
	 */
	void exitData_change_table_reference(Db2Parser.Data_change_table_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#searched_update_statement}.
	 * @param ctx the parse tree
	 */
	void enterSearched_update_statement(Db2Parser.Searched_update_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#searched_update_statement}.
	 * @param ctx the parse tree
	 */
	void exitSearched_update_statement(Db2Parser.Searched_update_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#searched_delete_statement}.
	 * @param ctx the parse tree
	 */
	void enterSearched_delete_statement(Db2Parser.Searched_delete_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#searched_delete_statement}.
	 * @param ctx the parse tree
	 */
	void exitSearched_delete_statement(Db2Parser.Searched_delete_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#final_new}.
	 * @param ctx the parse tree
	 */
	void enterFinal_new(Db2Parser.Final_newContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#final_new}.
	 * @param ctx the parse tree
	 */
	void exitFinal_new(Db2Parser.Final_newContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#final_new_old}.
	 * @param ctx the parse tree
	 */
	void enterFinal_new_old(Db2Parser.Final_new_oldContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#final_new_old}.
	 * @param ctx the parse tree
	 */
	void exitFinal_new_old(Db2Parser.Final_new_oldContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_function_reference}.
	 * @param ctx the parse tree
	 */
	void enterTable_function_reference(Db2Parser.Table_function_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_function_reference}.
	 * @param ctx the parse tree
	 */
	void exitTable_function_reference(Db2Parser.Table_function_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_udf_cardinality_clause}.
	 * @param ctx the parse tree
	 */
	void enterTable_udf_cardinality_clause(Db2Parser.Table_udf_cardinality_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_udf_cardinality_clause}.
	 * @param ctx the parse tree
	 */
	void exitTable_udf_cardinality_clause(Db2Parser.Table_udf_cardinality_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#typed_correlation_clause}.
	 * @param ctx the parse tree
	 */
	void enterTyped_correlation_clause(Db2Parser.Typed_correlation_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#typed_correlation_clause}.
	 * @param ctx the parse tree
	 */
	void exitTyped_correlation_clause(Db2Parser.Typed_correlation_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#column_name_data_type}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name_data_type(Db2Parser.Column_name_data_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#column_name_data_type}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name_data_type(Db2Parser.Column_name_data_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#collection_derived_table}.
	 * @param ctx the parse tree
	 */
	void enterCollection_derived_table(Db2Parser.Collection_derived_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#collection_derived_table}.
	 * @param ctx the parse tree
	 */
	void exitCollection_derived_table(Db2Parser.Collection_derived_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_function}.
	 * @param ctx the parse tree
	 */
	void enterTable_function(Db2Parser.Table_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_function}.
	 * @param ctx the parse tree
	 */
	void exitTable_function(Db2Parser.Table_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#xmltable_expression}.
	 * @param ctx the parse tree
	 */
	void enterXmltable_expression(Db2Parser.Xmltable_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#xmltable_expression}.
	 * @param ctx the parse tree
	 */
	void exitXmltable_expression(Db2Parser.Xmltable_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#xmltable_function}.
	 * @param ctx the parse tree
	 */
	void enterXmltable_function(Db2Parser.Xmltable_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#xmltable_function}.
	 * @param ctx the parse tree
	 */
	void exitXmltable_function(Db2Parser.Xmltable_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#joined_table}.
	 * @param ctx the parse tree
	 */
	void enterJoined_table(Db2Parser.Joined_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#joined_table}.
	 * @param ctx the parse tree
	 */
	void exitJoined_table(Db2Parser.Joined_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#join_condition}.
	 * @param ctx the parse tree
	 */
	void enterJoin_condition(Db2Parser.Join_conditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#join_condition}.
	 * @param ctx the parse tree
	 */
	void exitJoin_condition(Db2Parser.Join_conditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#outer}.
	 * @param ctx the parse tree
	 */
	void enterOuter(Db2Parser.OuterContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#outer}.
	 * @param ctx the parse tree
	 */
	void exitOuter(Db2Parser.OuterContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#external_table_reference}.
	 * @param ctx the parse tree
	 */
	void enterExternal_table_reference(Db2Parser.External_table_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#external_table_reference}.
	 * @param ctx the parse tree
	 */
	void exitExternal_table_reference(Db2Parser.External_table_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#column_definition_2}.
	 * @param ctx the parse tree
	 */
	void enterColumn_definition_2(Db2Parser.Column_definition_2Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#column_definition_2}.
	 * @param ctx the parse tree
	 */
	void exitColumn_definition_2(Db2Parser.Column_definition_2Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#file_name}.
	 * @param ctx the parse tree
	 */
	void enterFile_name(Db2Parser.File_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#file_name}.
	 * @param ctx the parse tree
	 */
	void exitFile_name(Db2Parser.File_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#where_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhere_clause(Db2Parser.Where_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#where_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhere_clause(Db2Parser.Where_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#group_by_clause}.
	 * @param ctx the parse tree
	 */
	void enterGroup_by_clause(Db2Parser.Group_by_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#group_by_clause}.
	 * @param ctx the parse tree
	 */
	void exitGroup_by_clause(Db2Parser.Group_by_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#group_by_clause_opts}.
	 * @param ctx the parse tree
	 */
	void enterGroup_by_clause_opts(Db2Parser.Group_by_clause_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#group_by_clause_opts}.
	 * @param ctx the parse tree
	 */
	void exitGroup_by_clause_opts(Db2Parser.Group_by_clause_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grouping_expression}.
	 * @param ctx the parse tree
	 */
	void enterGrouping_expression(Db2Parser.Grouping_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grouping_expression}.
	 * @param ctx the parse tree
	 */
	void exitGrouping_expression(Db2Parser.Grouping_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grouping_sets}.
	 * @param ctx the parse tree
	 */
	void enterGrouping_sets(Db2Parser.Grouping_setsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grouping_sets}.
	 * @param ctx the parse tree
	 */
	void exitGrouping_sets(Db2Parser.Grouping_setsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#super_groups}.
	 * @param ctx the parse tree
	 */
	void enterSuper_groups(Db2Parser.Super_groupsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#super_groups}.
	 * @param ctx the parse tree
	 */
	void exitSuper_groups(Db2Parser.Super_groupsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#grant_total}.
	 * @param ctx the parse tree
	 */
	void enterGrant_total(Db2Parser.Grant_totalContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#grant_total}.
	 * @param ctx the parse tree
	 */
	void exitGrant_total(Db2Parser.Grant_totalContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#having_clause}.
	 * @param ctx the parse tree
	 */
	void enterHaving_clause(Db2Parser.Having_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#having_clause}.
	 * @param ctx the parse tree
	 */
	void exitHaving_clause(Db2Parser.Having_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#order_by_clause}.
	 * @param ctx the parse tree
	 */
	void enterOrder_by_clause(Db2Parser.Order_by_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#order_by_clause}.
	 * @param ctx the parse tree
	 */
	void exitOrder_by_clause(Db2Parser.Order_by_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#order_by_clause_opts}.
	 * @param ctx the parse tree
	 */
	void enterOrder_by_clause_opts(Db2Parser.Order_by_clause_optsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#order_by_clause_opts}.
	 * @param ctx the parse tree
	 */
	void exitOrder_by_clause_opts(Db2Parser.Order_by_clause_optsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_designator}.
	 * @param ctx the parse tree
	 */
	void enterTable_designator(Db2Parser.Table_designatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_designator}.
	 * @param ctx the parse tree
	 */
	void exitTable_designator(Db2Parser.Table_designatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#asc_desc}.
	 * @param ctx the parse tree
	 */
	void enterAsc_desc(Db2Parser.Asc_descContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#asc_desc}.
	 * @param ctx the parse tree
	 */
	void exitAsc_desc(Db2Parser.Asc_descContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#first_last}.
	 * @param ctx the parse tree
	 */
	void enterFirst_last(Db2Parser.First_lastContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#first_last}.
	 * @param ctx the parse tree
	 */
	void exitFirst_last(Db2Parser.First_lastContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sort_key}.
	 * @param ctx the parse tree
	 */
	void enterSort_key(Db2Parser.Sort_keyContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sort_key}.
	 * @param ctx the parse tree
	 */
	void exitSort_key(Db2Parser.Sort_keyContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#simple_column_name}.
	 * @param ctx the parse tree
	 */
	void enterSimple_column_name(Db2Parser.Simple_column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#simple_column_name}.
	 * @param ctx the parse tree
	 */
	void exitSimple_column_name(Db2Parser.Simple_column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#simple_integer}.
	 * @param ctx the parse tree
	 */
	void enterSimple_integer(Db2Parser.Simple_integerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#simple_integer}.
	 * @param ctx the parse tree
	 */
	void exitSimple_integer(Db2Parser.Simple_integerContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sork_key_expression}.
	 * @param ctx the parse tree
	 */
	void enterSork_key_expression(Db2Parser.Sork_key_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sork_key_expression}.
	 * @param ctx the parse tree
	 */
	void exitSork_key_expression(Db2Parser.Sork_key_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#offset_clause}.
	 * @param ctx the parse tree
	 */
	void enterOffset_clause(Db2Parser.Offset_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#offset_clause}.
	 * @param ctx the parse tree
	 */
	void exitOffset_clause(Db2Parser.Offset_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#offset_row_count}.
	 * @param ctx the parse tree
	 */
	void enterOffset_row_count(Db2Parser.Offset_row_countContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#offset_row_count}.
	 * @param ctx the parse tree
	 */
	void exitOffset_row_count(Db2Parser.Offset_row_countContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#fetch_clause}.
	 * @param ctx the parse tree
	 */
	void enterFetch_clause(Db2Parser.Fetch_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#fetch_clause}.
	 * @param ctx the parse tree
	 */
	void exitFetch_clause(Db2Parser.Fetch_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#fetch_row_count}.
	 * @param ctx the parse tree
	 */
	void enterFetch_row_count(Db2Parser.Fetch_row_countContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#fetch_row_count}.
	 * @param ctx the parse tree
	 */
	void exitFetch_row_count(Db2Parser.Fetch_row_countContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#row_rows}.
	 * @param ctx the parse tree
	 */
	void enterRow_rows(Db2Parser.Row_rowsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#row_rows}.
	 * @param ctx the parse tree
	 */
	void exitRow_rows(Db2Parser.Row_rowsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#isolation_clause}.
	 * @param ctx the parse tree
	 */
	void enterIsolation_clause(Db2Parser.Isolation_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#isolation_clause}.
	 * @param ctx the parse tree
	 */
	void exitIsolation_clause(Db2Parser.Isolation_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#lock_request_clause}.
	 * @param ctx the parse tree
	 */
	void enterLock_request_clause(Db2Parser.Lock_request_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#lock_request_clause}.
	 * @param ctx the parse tree
	 */
	void exitLock_request_clause(Db2Parser.Lock_request_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#values_clause}.
	 * @param ctx the parse tree
	 */
	void enterValues_clause(Db2Parser.Values_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#values_clause}.
	 * @param ctx the parse tree
	 */
	void exitValues_clause(Db2Parser.Values_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#values_row}.
	 * @param ctx the parse tree
	 */
	void enterValues_row(Db2Parser.Values_rowContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#values_row}.
	 * @param ctx the parse tree
	 */
	void exitValues_row(Db2Parser.Values_rowContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#root_view_definition}.
	 * @param ctx the parse tree
	 */
	void enterRoot_view_definition(Db2Parser.Root_view_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#root_view_definition}.
	 * @param ctx the parse tree
	 */
	void exitRoot_view_definition(Db2Parser.Root_view_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#subview_definition}.
	 * @param ctx the parse tree
	 */
	void enterSubview_definition(Db2Parser.Subview_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#subview_definition}.
	 * @param ctx the parse tree
	 */
	void exitSubview_definition(Db2Parser.Subview_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#oid_column}.
	 * @param ctx the parse tree
	 */
	void enterOid_column(Db2Parser.Oid_columnContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#oid_column}.
	 * @param ctx the parse tree
	 */
	void exitOid_column(Db2Parser.Oid_columnContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#with_options}.
	 * @param ctx the parse tree
	 */
	void enterWith_options(Db2Parser.With_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#with_options}.
	 * @param ctx the parse tree
	 */
	void exitWith_options(Db2Parser.With_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#with_option_def}.
	 * @param ctx the parse tree
	 */
	void enterWith_option_def(Db2Parser.With_option_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#with_option_def}.
	 * @param ctx the parse tree
	 */
	void exitWith_option_def(Db2Parser.With_option_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#with_option_scope_def}.
	 * @param ctx the parse tree
	 */
	void enterWith_option_scope_def(Db2Parser.With_option_scope_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#with_option_scope_def}.
	 * @param ctx the parse tree
	 */
	void exitWith_option_scope_def(Db2Parser.With_option_scope_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#under_clause}.
	 * @param ctx the parse tree
	 */
	void enterUnder_clause(Db2Parser.Under_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#under_clause}.
	 * @param ctx the parse tree
	 */
	void exitUnder_clause(Db2Parser.Under_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_work_action_set_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_work_action_set_statement(Db2Parser.Create_work_action_set_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_work_action_set_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_work_action_set_statement(Db2Parser.Create_work_action_set_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#work_action_definition_list_paren}.
	 * @param ctx the parse tree
	 */
	void enterWork_action_definition_list_paren(Db2Parser.Work_action_definition_list_parenContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#work_action_definition_list_paren}.
	 * @param ctx the parse tree
	 */
	void exitWork_action_definition_list_paren(Db2Parser.Work_action_definition_list_parenContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#work_action_definition_list}.
	 * @param ctx the parse tree
	 */
	void enterWork_action_definition_list(Db2Parser.Work_action_definition_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#work_action_definition_list}.
	 * @param ctx the parse tree
	 */
	void exitWork_action_definition_list(Db2Parser.Work_action_definition_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#work_action_definition}.
	 * @param ctx the parse tree
	 */
	void enterWork_action_definition(Db2Parser.Work_action_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#work_action_definition}.
	 * @param ctx the parse tree
	 */
	void exitWork_action_definition(Db2Parser.Work_action_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#action_types_clause}.
	 * @param ctx the parse tree
	 */
	void enterAction_types_clause(Db2Parser.Action_types_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#action_types_clause}.
	 * @param ctx the parse tree
	 */
	void exitAction_types_clause(Db2Parser.Action_types_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#threshold_types_clause}.
	 * @param ctx the parse tree
	 */
	void enterThreshold_types_clause(Db2Parser.Threshold_types_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#threshold_types_clause}.
	 * @param ctx the parse tree
	 */
	void exitThreshold_types_clause(Db2Parser.Threshold_types_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#second_seconds}.
	 * @param ctx the parse tree
	 */
	void enterSecond_seconds(Db2Parser.Second_secondsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#second_seconds}.
	 * @param ctx the parse tree
	 */
	void exitSecond_seconds(Db2Parser.Second_secondsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#hours_minutes}.
	 * @param ctx the parse tree
	 */
	void enterHours_minutes(Db2Parser.Hours_minutesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#hours_minutes}.
	 * @param ctx the parse tree
	 */
	void exitHours_minutes(Db2Parser.Hours_minutesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#threshold_exceeded_actions}.
	 * @param ctx the parse tree
	 */
	void enterThreshold_exceeded_actions(Db2Parser.Threshold_exceeded_actionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#threshold_exceeded_actions}.
	 * @param ctx the parse tree
	 */
	void exitThreshold_exceeded_actions(Db2Parser.Threshold_exceeded_actionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#collect_activity_data_clause}.
	 * @param ctx the parse tree
	 */
	void enterCollect_activity_data_clause(Db2Parser.Collect_activity_data_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#collect_activity_data_clause}.
	 * @param ctx the parse tree
	 */
	void exitCollect_activity_data_clause(Db2Parser.Collect_activity_data_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#with_without}.
	 * @param ctx the parse tree
	 */
	void enterWith_without(Db2Parser.With_withoutContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#with_without}.
	 * @param ctx the parse tree
	 */
	void exitWith_without(Db2Parser.With_withoutContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#histogram_templace_clause}.
	 * @param ctx the parse tree
	 */
	void enterHistogram_templace_clause(Db2Parser.Histogram_templace_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#histogram_templace_clause}.
	 * @param ctx the parse tree
	 */
	void exitHistogram_templace_clause(Db2Parser.Histogram_templace_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_work_class_set_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_work_class_set_statement(Db2Parser.Create_work_class_set_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_work_class_set_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_work_class_set_statement(Db2Parser.Create_work_class_set_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#work_class_definition_list_paren}.
	 * @param ctx the parse tree
	 */
	void enterWork_class_definition_list_paren(Db2Parser.Work_class_definition_list_parenContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#work_class_definition_list_paren}.
	 * @param ctx the parse tree
	 */
	void exitWork_class_definition_list_paren(Db2Parser.Work_class_definition_list_parenContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#work_class_definition_list}.
	 * @param ctx the parse tree
	 */
	void enterWork_class_definition_list(Db2Parser.Work_class_definition_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#work_class_definition_list}.
	 * @param ctx the parse tree
	 */
	void exitWork_class_definition_list(Db2Parser.Work_class_definition_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#work_class_definition}.
	 * @param ctx the parse tree
	 */
	void enterWork_class_definition(Db2Parser.Work_class_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#work_class_definition}.
	 * @param ctx the parse tree
	 */
	void exitWork_class_definition(Db2Parser.Work_class_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#work_attributes}.
	 * @param ctx the parse tree
	 */
	void enterWork_attributes(Db2Parser.Work_attributesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#work_attributes}.
	 * @param ctx the parse tree
	 */
	void exitWork_attributes(Db2Parser.Work_attributesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#position_clause}.
	 * @param ctx the parse tree
	 */
	void enterPosition_clause(Db2Parser.Position_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#position_clause}.
	 * @param ctx the parse tree
	 */
	void exitPosition_clause(Db2Parser.Position_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#position_}.
	 * @param ctx the parse tree
	 */
	void enterPosition_(Db2Parser.Position_Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#position_}.
	 * @param ctx the parse tree
	 */
	void exitPosition_(Db2Parser.Position_Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#for_from_to_clause}.
	 * @param ctx the parse tree
	 */
	void enterFor_from_to_clause(Db2Parser.For_from_to_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#for_from_to_clause}.
	 * @param ctx the parse tree
	 */
	void exitFor_from_to_clause(Db2Parser.For_from_to_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#from_value}.
	 * @param ctx the parse tree
	 */
	void enterFrom_value(Db2Parser.From_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#from_value}.
	 * @param ctx the parse tree
	 */
	void exitFrom_value(Db2Parser.From_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#to_value}.
	 * @param ctx the parse tree
	 */
	void enterTo_value(Db2Parser.To_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#to_value}.
	 * @param ctx the parse tree
	 */
	void exitTo_value(Db2Parser.To_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#data_tag_clause}.
	 * @param ctx the parse tree
	 */
	void enterData_tag_clause(Db2Parser.Data_tag_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#data_tag_clause}.
	 * @param ctx the parse tree
	 */
	void exitData_tag_clause(Db2Parser.Data_tag_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#schema_clause}.
	 * @param ctx the parse tree
	 */
	void enterSchema_clause(Db2Parser.Schema_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#schema_clause}.
	 * @param ctx the parse tree
	 */
	void exitSchema_clause(Db2Parser.Schema_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_workload_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_workload_statement(Db2Parser.Create_workload_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_workload_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_workload_statement(Db2Parser.Create_workload_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#pkg_exec_seq}.
	 * @param ctx the parse tree
	 */
	void enterPkg_exec_seq(Db2Parser.Pkg_exec_seqContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#pkg_exec_seq}.
	 * @param ctx the parse tree
	 */
	void exitPkg_exec_seq(Db2Parser.Pkg_exec_seqContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#position_clause_2}.
	 * @param ctx the parse tree
	 */
	void enterPosition_clause_2(Db2Parser.Position_clause_2Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#position_clause_2}.
	 * @param ctx the parse tree
	 */
	void exitPosition_clause_2(Db2Parser.Position_clause_2Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#connection_attributes}.
	 * @param ctx the parse tree
	 */
	void enterConnection_attributes(Db2Parser.Connection_attributesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#connection_attributes}.
	 * @param ctx the parse tree
	 */
	void exitConnection_attributes(Db2Parser.Connection_attributesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#string_list}.
	 * @param ctx the parse tree
	 */
	void enterString_list(Db2Parser.String_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#string_list}.
	 * @param ctx the parse tree
	 */
	void exitString_list(Db2Parser.String_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#string_list_paren}.
	 * @param ctx the parse tree
	 */
	void enterString_list_paren(Db2Parser.String_list_parenContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#string_list_paren}.
	 * @param ctx the parse tree
	 */
	void exitString_list_paren(Db2Parser.String_list_parenContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#workload_attributes}.
	 * @param ctx the parse tree
	 */
	void enterWorkload_attributes(Db2Parser.Workload_attributesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#workload_attributes}.
	 * @param ctx the parse tree
	 */
	void exitWorkload_attributes(Db2Parser.Workload_attributesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#degree}.
	 * @param ctx the parse tree
	 */
	void enterDegree(Db2Parser.DegreeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#degree}.
	 * @param ctx the parse tree
	 */
	void exitDegree(Db2Parser.DegreeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#allow_disallow}.
	 * @param ctx the parse tree
	 */
	void enterAllow_disallow(Db2Parser.Allow_disallowContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#allow_disallow}.
	 * @param ctx the parse tree
	 */
	void exitAllow_disallow(Db2Parser.Allow_disallowContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#collect_on_clause}.
	 * @param ctx the parse tree
	 */
	void enterCollect_on_clause(Db2Parser.Collect_on_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#collect_on_clause}.
	 * @param ctx the parse tree
	 */
	void exitCollect_on_clause(Db2Parser.Collect_on_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#collect_details_clause}.
	 * @param ctx the parse tree
	 */
	void enterCollect_details_clause(Db2Parser.Collect_details_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#collect_details_clause}.
	 * @param ctx the parse tree
	 */
	void exitCollect_details_clause(Db2Parser.Collect_details_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#collect_lock_wait_options}.
	 * @param ctx the parse tree
	 */
	void enterCollect_lock_wait_options(Db2Parser.Collect_lock_wait_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#collect_lock_wait_options}.
	 * @param ctx the parse tree
	 */
	void exitCollect_lock_wait_options(Db2Parser.Collect_lock_wait_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#wait_time}.
	 * @param ctx the parse tree
	 */
	void enterWait_time(Db2Parser.Wait_timeContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#wait_time}.
	 * @param ctx the parse tree
	 */
	void exitWait_time(Db2Parser.Wait_timeContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#create_wrapper_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_wrapper_statement(Db2Parser.Create_wrapper_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#create_wrapper_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_wrapper_statement(Db2Parser.Create_wrapper_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#wrapper_option_list}.
	 * @param ctx the parse tree
	 */
	void enterWrapper_option_list(Db2Parser.Wrapper_option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#wrapper_option_list}.
	 * @param ctx the parse tree
	 */
	void exitWrapper_option_list(Db2Parser.Wrapper_option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#wrapper_option}.
	 * @param ctx the parse tree
	 */
	void enterWrapper_option(Db2Parser.Wrapper_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#wrapper_option}.
	 * @param ctx the parse tree
	 */
	void exitWrapper_option(Db2Parser.Wrapper_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(Db2Parser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(Db2Parser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#function_invocation}.
	 * @param ctx the parse tree
	 */
	void enterFunction_invocation(Db2Parser.Function_invocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#function_invocation}.
	 * @param ctx the parse tree
	 */
	void exitFunction_invocation(Db2Parser.Function_invocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#all_distinct}.
	 * @param ctx the parse tree
	 */
	void enterAll_distinct(Db2Parser.All_distinctContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#all_distinct}.
	 * @param ctx the parse tree
	 */
	void exitAll_distinct(Db2Parser.All_distinctContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#scalar_fullselect}.
	 * @param ctx the parse tree
	 */
	void enterScalar_fullselect(Db2Parser.Scalar_fullselectContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#scalar_fullselect}.
	 * @param ctx the parse tree
	 */
	void exitScalar_fullselect(Db2Parser.Scalar_fullselectContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#cast_specification}.
	 * @param ctx the parse tree
	 */
	void enterCast_specification(Db2Parser.Cast_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#cast_specification}.
	 * @param ctx the parse tree
	 */
	void exitCast_specification(Db2Parser.Cast_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#cursor_cast_specification}.
	 * @param ctx the parse tree
	 */
	void enterCursor_cast_specification(Db2Parser.Cursor_cast_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#cursor_cast_specification}.
	 * @param ctx the parse tree
	 */
	void exitCursor_cast_specification(Db2Parser.Cursor_cast_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#row_cast_specification}.
	 * @param ctx the parse tree
	 */
	void enterRow_cast_specification(Db2Parser.Row_cast_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#row_cast_specification}.
	 * @param ctx the parse tree
	 */
	void exitRow_cast_specification(Db2Parser.Row_cast_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#interval_cast_specification}.
	 * @param ctx the parse tree
	 */
	void enterInterval_cast_specification(Db2Parser.Interval_cast_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#interval_cast_specification}.
	 * @param ctx the parse tree
	 */
	void exitInterval_cast_specification(Db2Parser.Interval_cast_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#xmlcast_specification}.
	 * @param ctx the parse tree
	 */
	void enterXmlcast_specification(Db2Parser.Xmlcast_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#xmlcast_specification}.
	 * @param ctx the parse tree
	 */
	void exitXmlcast_specification(Db2Parser.Xmlcast_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#array_element_specification}.
	 * @param ctx the parse tree
	 */
	void enterArray_element_specification(Db2Parser.Array_element_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#array_element_specification}.
	 * @param ctx the parse tree
	 */
	void exitArray_element_specification(Db2Parser.Array_element_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#array_constructor}.
	 * @param ctx the parse tree
	 */
	void enterArray_constructor(Db2Parser.Array_constructorContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#array_constructor}.
	 * @param ctx the parse tree
	 */
	void exitArray_constructor(Db2Parser.Array_constructorContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#method_invocation}.
	 * @param ctx the parse tree
	 */
	void enterMethod_invocation(Db2Parser.Method_invocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#method_invocation}.
	 * @param ctx the parse tree
	 */
	void exitMethod_invocation(Db2Parser.Method_invocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#olap_specification}.
	 * @param ctx the parse tree
	 */
	void enterOlap_specification(Db2Parser.Olap_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#olap_specification}.
	 * @param ctx the parse tree
	 */
	void exitOlap_specification(Db2Parser.Olap_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#ordered_olap_specification}.
	 * @param ctx the parse tree
	 */
	void enterOrdered_olap_specification(Db2Parser.Ordered_olap_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#ordered_olap_specification}.
	 * @param ctx the parse tree
	 */
	void exitOrdered_olap_specification(Db2Parser.Ordered_olap_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#window_partition_clause}.
	 * @param ctx the parse tree
	 */
	void enterWindow_partition_clause(Db2Parser.Window_partition_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#window_partition_clause}.
	 * @param ctx the parse tree
	 */
	void exitWindow_partition_clause(Db2Parser.Window_partition_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#window_order_clause}.
	 * @param ctx the parse tree
	 */
	void enterWindow_order_clause(Db2Parser.Window_order_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#window_order_clause}.
	 * @param ctx the parse tree
	 */
	void exitWindow_order_clause(Db2Parser.Window_order_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#numbering_specification}.
	 * @param ctx the parse tree
	 */
	void enterNumbering_specification(Db2Parser.Numbering_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#numbering_specification}.
	 * @param ctx the parse tree
	 */
	void exitNumbering_specification(Db2Parser.Numbering_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#aggregation_specification}.
	 * @param ctx the parse tree
	 */
	void enterAggregation_specification(Db2Parser.Aggregation_specificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#aggregation_specification}.
	 * @param ctx the parse tree
	 */
	void exitAggregation_specification(Db2Parser.Aggregation_specificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#olap_aggregate_function}.
	 * @param ctx the parse tree
	 */
	void enterOlap_aggregate_function(Db2Parser.Olap_aggregate_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#olap_aggregate_function}.
	 * @param ctx the parse tree
	 */
	void exitOlap_aggregate_function(Db2Parser.Olap_aggregate_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#first_value_function}.
	 * @param ctx the parse tree
	 */
	void enterFirst_value_function(Db2Parser.First_value_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#first_value_function}.
	 * @param ctx the parse tree
	 */
	void exitFirst_value_function(Db2Parser.First_value_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#last_value_function}.
	 * @param ctx the parse tree
	 */
	void enterLast_value_function(Db2Parser.Last_value_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#last_value_function}.
	 * @param ctx the parse tree
	 */
	void exitLast_value_function(Db2Parser.Last_value_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#nth_value_function}.
	 * @param ctx the parse tree
	 */
	void enterNth_value_function(Db2Parser.Nth_value_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#nth_value_function}.
	 * @param ctx the parse tree
	 */
	void exitNth_value_function(Db2Parser.Nth_value_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#ratio_to_report_function}.
	 * @param ctx the parse tree
	 */
	void enterRatio_to_report_function(Db2Parser.Ratio_to_report_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#ratio_to_report_function}.
	 * @param ctx the parse tree
	 */
	void exitRatio_to_report_function(Db2Parser.Ratio_to_report_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#ignore_respect_nulls}.
	 * @param ctx the parse tree
	 */
	void enterIgnore_respect_nulls(Db2Parser.Ignore_respect_nullsContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#ignore_respect_nulls}.
	 * @param ctx the parse tree
	 */
	void exitIgnore_respect_nulls(Db2Parser.Ignore_respect_nullsContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#from_first_last}.
	 * @param ctx the parse tree
	 */
	void enterFrom_first_last(Db2Parser.From_first_lastContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#from_first_last}.
	 * @param ctx the parse tree
	 */
	void exitFrom_first_last(Db2Parser.From_first_lastContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#window_aggregation_group_clause}.
	 * @param ctx the parse tree
	 */
	void enterWindow_aggregation_group_clause(Db2Parser.Window_aggregation_group_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#window_aggregation_group_clause}.
	 * @param ctx the parse tree
	 */
	void exitWindow_aggregation_group_clause(Db2Parser.Window_aggregation_group_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#group_start}.
	 * @param ctx the parse tree
	 */
	void enterGroup_start(Db2Parser.Group_startContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#group_start}.
	 * @param ctx the parse tree
	 */
	void exitGroup_start(Db2Parser.Group_startContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#group_between}.
	 * @param ctx the parse tree
	 */
	void enterGroup_between(Db2Parser.Group_betweenContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#group_between}.
	 * @param ctx the parse tree
	 */
	void exitGroup_between(Db2Parser.Group_betweenContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#group_bound1}.
	 * @param ctx the parse tree
	 */
	void enterGroup_bound1(Db2Parser.Group_bound1Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#group_bound1}.
	 * @param ctx the parse tree
	 */
	void exitGroup_bound1(Db2Parser.Group_bound1Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#group_bound2}.
	 * @param ctx the parse tree
	 */
	void enterGroup_bound2(Db2Parser.Group_bound2Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#group_bound2}.
	 * @param ctx the parse tree
	 */
	void exitGroup_bound2(Db2Parser.Group_bound2Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#group_end}.
	 * @param ctx the parse tree
	 */
	void enterGroup_end(Db2Parser.Group_endContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#group_end}.
	 * @param ctx the parse tree
	 */
	void exitGroup_end(Db2Parser.Group_endContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#row_change_expression}.
	 * @param ctx the parse tree
	 */
	void enterRow_change_expression(Db2Parser.Row_change_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#row_change_expression}.
	 * @param ctx the parse tree
	 */
	void exitRow_change_expression(Db2Parser.Row_change_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sequence_reference}.
	 * @param ctx the parse tree
	 */
	void enterSequence_reference(Db2Parser.Sequence_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sequence_reference}.
	 * @param ctx the parse tree
	 */
	void exitSequence_reference(Db2Parser.Sequence_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#subtype_treatment}.
	 * @param ctx the parse tree
	 */
	void enterSubtype_treatment(Db2Parser.Subtype_treatmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#subtype_treatment}.
	 * @param ctx the parse tree
	 */
	void exitSubtype_treatment(Db2Parser.Subtype_treatmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#expression_list}.
	 * @param ctx the parse tree
	 */
	void enterExpression_list(Db2Parser.Expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#expression_list}.
	 * @param ctx the parse tree
	 */
	void exitExpression_list(Db2Parser.Expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#expression_list_in_parentheses}.
	 * @param ctx the parse tree
	 */
	void enterExpression_list_in_parentheses(Db2Parser.Expression_list_in_parenthesesContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#expression_list_in_parentheses}.
	 * @param ctx the parse tree
	 */
	void exitExpression_list_in_parentheses(Db2Parser.Expression_list_in_parenthesesContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#id_}.
	 * @param ctx the parse tree
	 */
	void enterId_(Db2Parser.Id_Context ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#id_}.
	 * @param ctx the parse tree
	 */
	void exitId_(Db2Parser.Id_Context ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#exposed_name}.
	 * @param ctx the parse tree
	 */
	void enterExposed_name(Db2Parser.Exposed_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#exposed_name}.
	 * @param ctx the parse tree
	 */
	void exitExposed_name(Db2Parser.Exposed_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(Db2Parser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(Db2Parser.NameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#label}.
	 * @param ctx the parse tree
	 */
	void enterLabel(Db2Parser.LabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#label}.
	 * @param ctx the parse tree
	 */
	void exitLabel(Db2Parser.LabelContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#host_label}.
	 * @param ctx the parse tree
	 */
	void enterHost_label(Db2Parser.Host_labelContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#host_label}.
	 * @param ctx the parse tree
	 */
	void exitHost_label(Db2Parser.Host_labelContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#library_name}.
	 * @param ctx the parse tree
	 */
	void enterLibrary_name(Db2Parser.Library_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#library_name}.
	 * @param ctx the parse tree
	 */
	void exitLibrary_name(Db2Parser.Library_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#array_type_name}.
	 * @param ctx the parse tree
	 */
	void enterArray_type_name(Db2Parser.Array_type_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#array_type_name}.
	 * @param ctx the parse tree
	 */
	void exitArray_type_name(Db2Parser.Array_type_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#attribute_name}.
	 * @param ctx the parse tree
	 */
	void enterAttribute_name(Db2Parser.Attribute_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#attribute_name}.
	 * @param ctx the parse tree
	 */
	void exitAttribute_name(Db2Parser.Attribute_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#row_type_name}.
	 * @param ctx the parse tree
	 */
	void enterRow_type_name(Db2Parser.Row_type_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#row_type_name}.
	 * @param ctx the parse tree
	 */
	void exitRow_type_name(Db2Parser.Row_type_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#authorization_name}.
	 * @param ctx the parse tree
	 */
	void enterAuthorization_name(Db2Parser.Authorization_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#authorization_name}.
	 * @param ctx the parse tree
	 */
	void exitAuthorization_name(Db2Parser.Authorization_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#boolean_variable_name}.
	 * @param ctx the parse tree
	 */
	void enterBoolean_variable_name(Db2Parser.Boolean_variable_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#boolean_variable_name}.
	 * @param ctx the parse tree
	 */
	void exitBoolean_variable_name(Db2Parser.Boolean_variable_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#array_variable_name}.
	 * @param ctx the parse tree
	 */
	void enterArray_variable_name(Db2Parser.Array_variable_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#array_variable_name}.
	 * @param ctx the parse tree
	 */
	void exitArray_variable_name(Db2Parser.Array_variable_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#column_name}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name(Db2Parser.Column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#column_name}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name(Db2Parser.Column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#constraint_name}.
	 * @param ctx the parse tree
	 */
	void enterConstraint_name(Db2Parser.Constraint_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#constraint_name}.
	 * @param ctx the parse tree
	 */
	void exitConstraint_name(Db2Parser.Constraint_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#descriptor_name}.
	 * @param ctx the parse tree
	 */
	void enterDescriptor_name(Db2Parser.Descriptor_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#descriptor_name}.
	 * @param ctx the parse tree
	 */
	void exitDescriptor_name(Db2Parser.Descriptor_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#distinct_type_name}.
	 * @param ctx the parse tree
	 */
	void enterDistinct_type_name(Db2Parser.Distinct_type_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#distinct_type_name}.
	 * @param ctx the parse tree
	 */
	void exitDistinct_type_name(Db2Parser.Distinct_type_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#cursor_name}.
	 * @param ctx the parse tree
	 */
	void enterCursor_name(Db2Parser.Cursor_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#cursor_name}.
	 * @param ctx the parse tree
	 */
	void exitCursor_name(Db2Parser.Cursor_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#cursor_type_name}.
	 * @param ctx the parse tree
	 */
	void enterCursor_type_name(Db2Parser.Cursor_type_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#cursor_type_name}.
	 * @param ctx the parse tree
	 */
	void exitCursor_type_name(Db2Parser.Cursor_type_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#condition_name}.
	 * @param ctx the parse tree
	 */
	void enterCondition_name(Db2Parser.Condition_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#condition_name}.
	 * @param ctx the parse tree
	 */
	void exitCondition_name(Db2Parser.Condition_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#data_source_name}.
	 * @param ctx the parse tree
	 */
	void enterData_source_name(Db2Parser.Data_source_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#data_source_name}.
	 * @param ctx the parse tree
	 */
	void exitData_source_name(Db2Parser.Data_source_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#expression_name}.
	 * @param ctx the parse tree
	 */
	void enterExpression_name(Db2Parser.Expression_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#expression_name}.
	 * @param ctx the parse tree
	 */
	void exitExpression_name(Db2Parser.Expression_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#group_name}.
	 * @param ctx the parse tree
	 */
	void enterGroup_name(Db2Parser.Group_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#group_name}.
	 * @param ctx the parse tree
	 */
	void exitGroup_name(Db2Parser.Group_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#policy_name}.
	 * @param ctx the parse tree
	 */
	void enterPolicy_name(Db2Parser.Policy_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#policy_name}.
	 * @param ctx the parse tree
	 */
	void exitPolicy_name(Db2Parser.Policy_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#bufferpool_name}.
	 * @param ctx the parse tree
	 */
	void enterBufferpool_name(Db2Parser.Bufferpool_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#bufferpool_name}.
	 * @param ctx the parse tree
	 */
	void exitBufferpool_name(Db2Parser.Bufferpool_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#db_partition_name}.
	 * @param ctx the parse tree
	 */
	void enterDb_partition_name(Db2Parser.Db_partition_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#db_partition_name}.
	 * @param ctx the parse tree
	 */
	void exitDb_partition_name(Db2Parser.Db_partition_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#database_name}.
	 * @param ctx the parse tree
	 */
	void enterDatabase_name(Db2Parser.Database_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#database_name}.
	 * @param ctx the parse tree
	 */
	void exitDatabase_name(Db2Parser.Database_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#event_monitor_name}.
	 * @param ctx the parse tree
	 */
	void enterEvent_monitor_name(Db2Parser.Event_monitor_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#event_monitor_name}.
	 * @param ctx the parse tree
	 */
	void exitEvent_monitor_name(Db2Parser.Event_monitor_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#field_name}.
	 * @param ctx the parse tree
	 */
	void enterField_name(Db2Parser.Field_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#field_name}.
	 * @param ctx the parse tree
	 */
	void exitField_name(Db2Parser.Field_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#for_loop_name}.
	 * @param ctx the parse tree
	 */
	void enterFor_loop_name(Db2Parser.For_loop_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#for_loop_name}.
	 * @param ctx the parse tree
	 */
	void exitFor_loop_name(Db2Parser.For_loop_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#function_name}.
	 * @param ctx the parse tree
	 */
	void enterFunction_name(Db2Parser.Function_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#function_name}.
	 * @param ctx the parse tree
	 */
	void exitFunction_name(Db2Parser.Function_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#function_mapping_name}.
	 * @param ctx the parse tree
	 */
	void enterFunction_mapping_name(Db2Parser.Function_mapping_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#function_mapping_name}.
	 * @param ctx the parse tree
	 */
	void exitFunction_mapping_name(Db2Parser.Function_mapping_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#global_variable_name}.
	 * @param ctx the parse tree
	 */
	void enterGlobal_variable_name(Db2Parser.Global_variable_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#global_variable_name}.
	 * @param ctx the parse tree
	 */
	void exitGlobal_variable_name(Db2Parser.Global_variable_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#hierarchy_name}.
	 * @param ctx the parse tree
	 */
	void enterHierarchy_name(Db2Parser.Hierarchy_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#hierarchy_name}.
	 * @param ctx the parse tree
	 */
	void exitHierarchy_name(Db2Parser.Hierarchy_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#host_variable_name}.
	 * @param ctx the parse tree
	 */
	void enterHost_variable_name(Db2Parser.Host_variable_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#host_variable_name}.
	 * @param ctx the parse tree
	 */
	void exitHost_variable_name(Db2Parser.Host_variable_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#parameter_marker}.
	 * @param ctx the parse tree
	 */
	void enterParameter_marker(Db2Parser.Parameter_markerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#parameter_marker}.
	 * @param ctx the parse tree
	 */
	void exitParameter_marker(Db2Parser.Parameter_markerContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#template_name}.
	 * @param ctx the parse tree
	 */
	void enterTemplate_name(Db2Parser.Template_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#template_name}.
	 * @param ctx the parse tree
	 */
	void exitTemplate_name(Db2Parser.Template_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#index_name}.
	 * @param ctx the parse tree
	 */
	void enterIndex_name(Db2Parser.Index_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#index_name}.
	 * @param ctx the parse tree
	 */
	void exitIndex_name(Db2Parser.Index_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#index_extension_name}.
	 * @param ctx the parse tree
	 */
	void enterIndex_extension_name(Db2Parser.Index_extension_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#index_extension_name}.
	 * @param ctx the parse tree
	 */
	void exitIndex_extension_name(Db2Parser.Index_extension_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#input_descriptor_name}.
	 * @param ctx the parse tree
	 */
	void enterInput_descriptor_name(Db2Parser.Input_descriptor_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#input_descriptor_name}.
	 * @param ctx the parse tree
	 */
	void exitInput_descriptor_name(Db2Parser.Input_descriptor_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#mask_name}.
	 * @param ctx the parse tree
	 */
	void enterMask_name(Db2Parser.Mask_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#mask_name}.
	 * @param ctx the parse tree
	 */
	void exitMask_name(Db2Parser.Mask_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#method_name}.
	 * @param ctx the parse tree
	 */
	void enterMethod_name(Db2Parser.Method_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#method_name}.
	 * @param ctx the parse tree
	 */
	void exitMethod_name(Db2Parser.Method_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#model_name}.
	 * @param ctx the parse tree
	 */
	void enterModel_name(Db2Parser.Model_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#model_name}.
	 * @param ctx the parse tree
	 */
	void exitModel_name(Db2Parser.Model_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#module_name}.
	 * @param ctx the parse tree
	 */
	void enterModule_name(Db2Parser.Module_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#module_name}.
	 * @param ctx the parse tree
	 */
	void exitModule_name(Db2Parser.Module_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#new_owner}.
	 * @param ctx the parse tree
	 */
	void enterNew_owner(Db2Parser.New_ownerContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#new_owner}.
	 * @param ctx the parse tree
	 */
	void exitNew_owner(Db2Parser.New_ownerContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#nick_name}.
	 * @param ctx the parse tree
	 */
	void enterNick_name(Db2Parser.Nick_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#nick_name}.
	 * @param ctx the parse tree
	 */
	void exitNick_name(Db2Parser.Nick_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#object_name}.
	 * @param ctx the parse tree
	 */
	void enterObject_name(Db2Parser.Object_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#object_name}.
	 * @param ctx the parse tree
	 */
	void exitObject_name(Db2Parser.Object_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#oid_column_name}.
	 * @param ctx the parse tree
	 */
	void enterOid_column_name(Db2Parser.Oid_column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#oid_column_name}.
	 * @param ctx the parse tree
	 */
	void exitOid_column_name(Db2Parser.Oid_column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#optimization_profile_name}.
	 * @param ctx the parse tree
	 */
	void enterOptimization_profile_name(Db2Parser.Optimization_profile_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#optimization_profile_name}.
	 * @param ctx the parse tree
	 */
	void exitOptimization_profile_name(Db2Parser.Optimization_profile_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#package_name}.
	 * @param ctx the parse tree
	 */
	void enterPackage_name(Db2Parser.Package_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#package_name}.
	 * @param ctx the parse tree
	 */
	void exitPackage_name(Db2Parser.Package_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#partition_name}.
	 * @param ctx the parse tree
	 */
	void enterPartition_name(Db2Parser.Partition_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#partition_name}.
	 * @param ctx the parse tree
	 */
	void exitPartition_name(Db2Parser.Partition_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#path_name}.
	 * @param ctx the parse tree
	 */
	void enterPath_name(Db2Parser.Path_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#path_name}.
	 * @param ctx the parse tree
	 */
	void exitPath_name(Db2Parser.Path_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#permission_name}.
	 * @param ctx the parse tree
	 */
	void enterPermission_name(Db2Parser.Permission_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#permission_name}.
	 * @param ctx the parse tree
	 */
	void exitPermission_name(Db2Parser.Permission_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#pipe_name}.
	 * @param ctx the parse tree
	 */
	void enterPipe_name(Db2Parser.Pipe_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#pipe_name}.
	 * @param ctx the parse tree
	 */
	void exitPipe_name(Db2Parser.Pipe_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#procedure_name}.
	 * @param ctx the parse tree
	 */
	void enterProcedure_name(Db2Parser.Procedure_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#procedure_name}.
	 * @param ctx the parse tree
	 */
	void exitProcedure_name(Db2Parser.Procedure_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#result_descriptor_name}.
	 * @param ctx the parse tree
	 */
	void enterResult_descriptor_name(Db2Parser.Result_descriptor_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#result_descriptor_name}.
	 * @param ctx the parse tree
	 */
	void exitResult_descriptor_name(Db2Parser.Result_descriptor_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#role_name}.
	 * @param ctx the parse tree
	 */
	void enterRole_name(Db2Parser.Role_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#role_name}.
	 * @param ctx the parse tree
	 */
	void exitRole_name(Db2Parser.Role_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#root_table_name}.
	 * @param ctx the parse tree
	 */
	void enterRoot_table_name(Db2Parser.Root_table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#root_table_name}.
	 * @param ctx the parse tree
	 */
	void exitRoot_table_name(Db2Parser.Root_table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#root_view_name}.
	 * @param ctx the parse tree
	 */
	void enterRoot_view_name(Db2Parser.Root_view_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#root_view_name}.
	 * @param ctx the parse tree
	 */
	void exitRoot_view_name(Db2Parser.Root_view_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#row_variable_name}.
	 * @param ctx the parse tree
	 */
	void enterRow_variable_name(Db2Parser.Row_variable_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#row_variable_name}.
	 * @param ctx the parse tree
	 */
	void exitRow_variable_name(Db2Parser.Row_variable_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#source_schema_name}.
	 * @param ctx the parse tree
	 */
	void enterSource_schema_name(Db2Parser.Source_schema_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#source_schema_name}.
	 * @param ctx the parse tree
	 */
	void exitSource_schema_name(Db2Parser.Source_schema_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#source_package_name}.
	 * @param ctx the parse tree
	 */
	void enterSource_package_name(Db2Parser.Source_package_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#source_package_name}.
	 * @param ctx the parse tree
	 */
	void exitSource_package_name(Db2Parser.Source_package_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#source_procedure_name}.
	 * @param ctx the parse tree
	 */
	void enterSource_procedure_name(Db2Parser.Source_procedure_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#source_procedure_name}.
	 * @param ctx the parse tree
	 */
	void exitSource_procedure_name(Db2Parser.Source_procedure_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_parameter_name}.
	 * @param ctx the parse tree
	 */
	void enterSql_parameter_name(Db2Parser.Sql_parameter_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_parameter_name}.
	 * @param ctx the parse tree
	 */
	void exitSql_parameter_name(Db2Parser.Sql_parameter_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sql_variable_name}.
	 * @param ctx the parse tree
	 */
	void enterSql_variable_name(Db2Parser.Sql_variable_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sql_variable_name}.
	 * @param ctx the parse tree
	 */
	void exitSql_variable_name(Db2Parser.Sql_variable_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#transition_variable_name}.
	 * @param ctx the parse tree
	 */
	void enterTransition_variable_name(Db2Parser.Transition_variable_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#transition_variable_name}.
	 * @param ctx the parse tree
	 */
	void exitTransition_variable_name(Db2Parser.Transition_variable_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#savepoint_name}.
	 * @param ctx the parse tree
	 */
	void enterSavepoint_name(Db2Parser.Savepoint_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#savepoint_name}.
	 * @param ctx the parse tree
	 */
	void exitSavepoint_name(Db2Parser.Savepoint_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#specific_name}.
	 * @param ctx the parse tree
	 */
	void enterSpecific_name(Db2Parser.Specific_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#specific_name}.
	 * @param ctx the parse tree
	 */
	void exitSpecific_name(Db2Parser.Specific_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#schema}.
	 * @param ctx the parse tree
	 */
	void enterSchema(Db2Parser.SchemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#schema}.
	 * @param ctx the parse tree
	 */
	void exitSchema(Db2Parser.SchemaContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#schema_name}.
	 * @param ctx the parse tree
	 */
	void enterSchema_name(Db2Parser.Schema_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#schema_name}.
	 * @param ctx the parse tree
	 */
	void exitSchema_name(Db2Parser.Schema_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#search_method_name}.
	 * @param ctx the parse tree
	 */
	void enterSearch_method_name(Db2Parser.Search_method_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#search_method_name}.
	 * @param ctx the parse tree
	 */
	void exitSearch_method_name(Db2Parser.Search_method_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#server_name}.
	 * @param ctx the parse tree
	 */
	void enterServer_name(Db2Parser.Server_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#server_name}.
	 * @param ctx the parse tree
	 */
	void exitServer_name(Db2Parser.Server_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#server_option_name}.
	 * @param ctx the parse tree
	 */
	void enterServer_option_name(Db2Parser.Server_option_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#server_option_name}.
	 * @param ctx the parse tree
	 */
	void exitServer_option_name(Db2Parser.Server_option_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#session_authorization_name}.
	 * @param ctx the parse tree
	 */
	void enterSession_authorization_name(Db2Parser.Session_authorization_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#session_authorization_name}.
	 * @param ctx the parse tree
	 */
	void exitSession_authorization_name(Db2Parser.Session_authorization_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#component_name}.
	 * @param ctx the parse tree
	 */
	void enterComponent_name(Db2Parser.Component_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#component_name}.
	 * @param ctx the parse tree
	 */
	void exitComponent_name(Db2Parser.Component_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sec_label_comp_name}.
	 * @param ctx the parse tree
	 */
	void enterSec_label_comp_name(Db2Parser.Sec_label_comp_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sec_label_comp_name}.
	 * @param ctx the parse tree
	 */
	void exitSec_label_comp_name(Db2Parser.Sec_label_comp_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#security_policy_name}.
	 * @param ctx the parse tree
	 */
	void enterSecurity_policy_name(Db2Parser.Security_policy_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#security_policy_name}.
	 * @param ctx the parse tree
	 */
	void exitSecurity_policy_name(Db2Parser.Security_policy_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#security_label_name}.
	 * @param ctx the parse tree
	 */
	void enterSecurity_label_name(Db2Parser.Security_label_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#security_label_name}.
	 * @param ctx the parse tree
	 */
	void exitSecurity_label_name(Db2Parser.Security_label_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#sequence_name}.
	 * @param ctx the parse tree
	 */
	void enterSequence_name(Db2Parser.Sequence_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#sequence_name}.
	 * @param ctx the parse tree
	 */
	void exitSequence_name(Db2Parser.Sequence_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#service_class_name}.
	 * @param ctx the parse tree
	 */
	void enterService_class_name(Db2Parser.Service_class_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#service_class_name}.
	 * @param ctx the parse tree
	 */
	void exitService_class_name(Db2Parser.Service_class_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#service_superclass_name}.
	 * @param ctx the parse tree
	 */
	void enterService_superclass_name(Db2Parser.Service_superclass_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#service_superclass_name}.
	 * @param ctx the parse tree
	 */
	void exitService_superclass_name(Db2Parser.Service_superclass_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#storagegroup_name}.
	 * @param ctx the parse tree
	 */
	void enterStoragegroup_name(Db2Parser.Storagegroup_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#storagegroup_name}.
	 * @param ctx the parse tree
	 */
	void exitStoragegroup_name(Db2Parser.Storagegroup_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#supertype_name}.
	 * @param ctx the parse tree
	 */
	void enterSupertype_name(Db2Parser.Supertype_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#supertype_name}.
	 * @param ctx the parse tree
	 */
	void exitSupertype_name(Db2Parser.Supertype_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#superview_name}.
	 * @param ctx the parse tree
	 */
	void enterSuperview_name(Db2Parser.Superview_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#superview_name}.
	 * @param ctx the parse tree
	 */
	void exitSuperview_name(Db2Parser.Superview_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#service_subclass_name}.
	 * @param ctx the parse tree
	 */
	void enterService_subclass_name(Db2Parser.Service_subclass_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#service_subclass_name}.
	 * @param ctx the parse tree
	 */
	void exitService_subclass_name(Db2Parser.Service_subclass_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#statement_name}.
	 * @param ctx the parse tree
	 */
	void enterStatement_name(Db2Parser.Statement_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#statement_name}.
	 * @param ctx the parse tree
	 */
	void exitStatement_name(Db2Parser.Statement_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#table_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_name(Db2Parser.Table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#table_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_name(Db2Parser.Table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#tablespace_name}.
	 * @param ctx the parse tree
	 */
	void enterTablespace_name(Db2Parser.Tablespace_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#tablespace_name}.
	 * @param ctx the parse tree
	 */
	void exitTablespace_name(Db2Parser.Tablespace_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#target_identifier}.
	 * @param ctx the parse tree
	 */
	void enterTarget_identifier(Db2Parser.Target_identifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#target_identifier}.
	 * @param ctx the parse tree
	 */
	void exitTarget_identifier(Db2Parser.Target_identifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#threshold_name}.
	 * @param ctx the parse tree
	 */
	void enterThreshold_name(Db2Parser.Threshold_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#threshold_name}.
	 * @param ctx the parse tree
	 */
	void exitThreshold_name(Db2Parser.Threshold_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#trigger_name}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_name(Db2Parser.Trigger_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#trigger_name}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_name(Db2Parser.Trigger_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#context_name}.
	 * @param ctx the parse tree
	 */
	void enterContext_name(Db2Parser.Context_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#context_name}.
	 * @param ctx the parse tree
	 */
	void exitContext_name(Db2Parser.Context_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#usage_list_name}.
	 * @param ctx the parse tree
	 */
	void enterUsage_list_name(Db2Parser.Usage_list_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#usage_list_name}.
	 * @param ctx the parse tree
	 */
	void exitUsage_list_name(Db2Parser.Usage_list_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#type_name}.
	 * @param ctx the parse tree
	 */
	void enterType_name(Db2Parser.Type_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#type_name}.
	 * @param ctx the parse tree
	 */
	void exitType_name(Db2Parser.Type_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#type_mapping_name}.
	 * @param ctx the parse tree
	 */
	void enterType_mapping_name(Db2Parser.Type_mapping_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#type_mapping_name}.
	 * @param ctx the parse tree
	 */
	void exitType_mapping_name(Db2Parser.Type_mapping_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#typed_table_name}.
	 * @param ctx the parse tree
	 */
	void enterTyped_table_name(Db2Parser.Typed_table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#typed_table_name}.
	 * @param ctx the parse tree
	 */
	void exitTyped_table_name(Db2Parser.Typed_table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#typed_view_name}.
	 * @param ctx the parse tree
	 */
	void enterTyped_view_name(Db2Parser.Typed_view_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#typed_view_name}.
	 * @param ctx the parse tree
	 */
	void exitTyped_view_name(Db2Parser.Typed_view_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#user_mapping_option_name}.
	 * @param ctx the parse tree
	 */
	void enterUser_mapping_option_name(Db2Parser.User_mapping_option_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#user_mapping_option_name}.
	 * @param ctx the parse tree
	 */
	void exitUser_mapping_option_name(Db2Parser.User_mapping_option_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#view_name}.
	 * @param ctx the parse tree
	 */
	void enterView_name(Db2Parser.View_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#view_name}.
	 * @param ctx the parse tree
	 */
	void exitView_name(Db2Parser.View_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#variable_name}.
	 * @param ctx the parse tree
	 */
	void enterVariable_name(Db2Parser.Variable_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#variable_name}.
	 * @param ctx the parse tree
	 */
	void exitVariable_name(Db2Parser.Variable_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#work_action_set_name}.
	 * @param ctx the parse tree
	 */
	void enterWork_action_set_name(Db2Parser.Work_action_set_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#work_action_set_name}.
	 * @param ctx the parse tree
	 */
	void exitWork_action_set_name(Db2Parser.Work_action_set_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#work_class_set_name}.
	 * @param ctx the parse tree
	 */
	void enterWork_class_set_name(Db2Parser.Work_class_set_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#work_class_set_name}.
	 * @param ctx the parse tree
	 */
	void exitWork_class_set_name(Db2Parser.Work_class_set_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#workload_name}.
	 * @param ctx the parse tree
	 */
	void enterWorkload_name(Db2Parser.Workload_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#workload_name}.
	 * @param ctx the parse tree
	 */
	void exitWorkload_name(Db2Parser.Workload_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#work_action_name}.
	 * @param ctx the parse tree
	 */
	void enterWork_action_name(Db2Parser.Work_action_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#work_action_name}.
	 * @param ctx the parse tree
	 */
	void exitWork_action_name(Db2Parser.Work_action_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#work_class_name}.
	 * @param ctx the parse tree
	 */
	void enterWork_class_name(Db2Parser.Work_class_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#work_class_name}.
	 * @param ctx the parse tree
	 */
	void exitWork_class_name(Db2Parser.Work_class_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#wrapper_name}.
	 * @param ctx the parse tree
	 */
	void enterWrapper_name(Db2Parser.Wrapper_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#wrapper_name}.
	 * @param ctx the parse tree
	 */
	void exitWrapper_name(Db2Parser.Wrapper_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#wrapper_option_name}.
	 * @param ctx the parse tree
	 */
	void enterWrapper_option_name(Db2Parser.Wrapper_option_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#wrapper_option_name}.
	 * @param ctx the parse tree
	 */
	void exitWrapper_option_name(Db2Parser.Wrapper_option_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#xsrobject_name}.
	 * @param ctx the parse tree
	 */
	void enterXsrobject_name(Db2Parser.Xsrobject_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#xsrobject_name}.
	 * @param ctx the parse tree
	 */
	void exitXsrobject_name(Db2Parser.Xsrobject_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#parameter_name}.
	 * @param ctx the parse tree
	 */
	void enterParameter_name(Db2Parser.Parameter_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#parameter_name}.
	 * @param ctx the parse tree
	 */
	void exitParameter_name(Db2Parser.Parameter_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#cursor_variable_name}.
	 * @param ctx the parse tree
	 */
	void enterCursor_variable_name(Db2Parser.Cursor_variable_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#cursor_variable_name}.
	 * @param ctx the parse tree
	 */
	void exitCursor_variable_name(Db2Parser.Cursor_variable_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#alias_name}.
	 * @param ctx the parse tree
	 */
	void enterAlias_name(Db2Parser.Alias_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#alias_name}.
	 * @param ctx the parse tree
	 */
	void exitAlias_name(Db2Parser.Alias_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#db_partition_group_name}.
	 * @param ctx the parse tree
	 */
	void enterDb_partition_group_name(Db2Parser.Db_partition_group_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#db_partition_group_name}.
	 * @param ctx the parse tree
	 */
	void exitDb_partition_group_name(Db2Parser.Db_partition_group_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#source_index_name}.
	 * @param ctx the parse tree
	 */
	void enterSource_index_name(Db2Parser.Source_index_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#source_index_name}.
	 * @param ctx the parse tree
	 */
	void exitSource_index_name(Db2Parser.Source_index_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#source_table_name}.
	 * @param ctx the parse tree
	 */
	void enterSource_table_name(Db2Parser.Source_table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#source_table_name}.
	 * @param ctx the parse tree
	 */
	void exitSource_table_name(Db2Parser.Source_table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#source_storagegroup_name}.
	 * @param ctx the parse tree
	 */
	void enterSource_storagegroup_name(Db2Parser.Source_storagegroup_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#source_storagegroup_name}.
	 * @param ctx the parse tree
	 */
	void exitSource_storagegroup_name(Db2Parser.Source_storagegroup_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#target_storagegroup_name}.
	 * @param ctx the parse tree
	 */
	void enterTarget_storagegroup_name(Db2Parser.Target_storagegroup_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#target_storagegroup_name}.
	 * @param ctx the parse tree
	 */
	void exitTarget_storagegroup_name(Db2Parser.Target_storagegroup_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#source_tablespace_name}.
	 * @param ctx the parse tree
	 */
	void enterSource_tablespace_name(Db2Parser.Source_tablespace_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#source_tablespace_name}.
	 * @param ctx the parse tree
	 */
	void exitSource_tablespace_name(Db2Parser.Source_tablespace_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#target_tablespace_name}.
	 * @param ctx the parse tree
	 */
	void enterTarget_tablespace_name(Db2Parser.Target_tablespace_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#target_tablespace_name}.
	 * @param ctx the parse tree
	 */
	void exitTarget_tablespace_name(Db2Parser.Target_tablespace_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#unqualified_function_name}.
	 * @param ctx the parse tree
	 */
	void enterUnqualified_function_name(Db2Parser.Unqualified_function_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#unqualified_function_name}.
	 * @param ctx the parse tree
	 */
	void exitUnqualified_function_name(Db2Parser.Unqualified_function_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#unqualified_procedure_name}.
	 * @param ctx the parse tree
	 */
	void enterUnqualified_procedure_name(Db2Parser.Unqualified_procedure_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#unqualified_procedure_name}.
	 * @param ctx the parse tree
	 */
	void exitUnqualified_procedure_name(Db2Parser.Unqualified_procedure_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#unqualified_specific_name}.
	 * @param ctx the parse tree
	 */
	void enterUnqualified_specific_name(Db2Parser.Unqualified_specific_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#unqualified_specific_name}.
	 * @param ctx the parse tree
	 */
	void exitUnqualified_specific_name(Db2Parser.Unqualified_specific_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#period_name}.
	 * @param ctx the parse tree
	 */
	void enterPeriod_name(Db2Parser.Period_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#period_name}.
	 * @param ctx the parse tree
	 */
	void exitPeriod_name(Db2Parser.Period_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#history_table_name}.
	 * @param ctx the parse tree
	 */
	void enterHistory_table_name(Db2Parser.History_table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#history_table_name}.
	 * @param ctx the parse tree
	 */
	void exitHistory_table_name(Db2Parser.History_table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#xml_schema_name}.
	 * @param ctx the parse tree
	 */
	void enterXml_schema_name(Db2Parser.Xml_schema_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#xml_schema_name}.
	 * @param ctx the parse tree
	 */
	void exitXml_schema_name(Db2Parser.Xml_schema_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link Db2Parser#todo}.
	 * @param ctx the parse tree
	 */
	void enterTodo(Db2Parser.TodoContext ctx);
	/**
	 * Exit a parse tree produced by {@link Db2Parser#todo}.
	 * @param ctx the parse tree
	 */
	void exitTodo(Db2Parser.TodoContext ctx);
}