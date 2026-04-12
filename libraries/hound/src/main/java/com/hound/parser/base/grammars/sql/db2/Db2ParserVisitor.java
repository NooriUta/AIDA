// Generated from C:/AIDA/Dali4/HOUND/Hound/src/main/resources/grammars/sql/db2/Db2Parser.g4 by ANTLR 4.13.1
package com.hound.parser.base.grammars.sql.db2;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link Db2Parser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface Db2ParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link Db2Parser#db2_file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb2_file(Db2Parser.Db2_fileContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#batch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBatch(Db2Parser.BatchContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_statement(Db2Parser.Sql_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_schema_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_schema_statement(Db2Parser.Sql_schema_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_data_change_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_data_change_statement(Db2Parser.Sql_data_change_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_data_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_data_statement(Db2Parser.Sql_data_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_transaction_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_transaction_statement(Db2Parser.Sql_transaction_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_connection_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_connection_statement(Db2Parser.Sql_connection_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_dynamic_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_dynamic_statement(Db2Parser.Sql_dynamic_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_session_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_session_statement(Db2Parser.Sql_session_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_embedded_host_language_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_embedded_host_language_statement(Db2Parser.Sql_embedded_host_language_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_constrol_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_constrol_statement(Db2Parser.Sql_constrol_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#select_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_statement(Db2Parser.Select_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#read_only_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRead_only_clause(Db2Parser.Read_only_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#update_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate_clause(Db2Parser.Update_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#optimize_for_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptimize_for_clause(Db2Parser.Optimize_for_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#concurrent_access_resolution_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConcurrent_access_resolution_clause(Db2Parser.Concurrent_access_resolution_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#delete_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete_statement(Db2Parser.Delete_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#delete_statement_searched_delete}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete_statement_searched_delete(Db2Parser.Delete_statement_searched_deleteContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_or_view_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_or_view_name(Db2Parser.Table_or_view_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#delete_statement_positioned_delete}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete_statement_positioned_delete(Db2Parser.Delete_statement_positioned_deleteContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#delete_deltalake_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete_deltalake_statement(Db2Parser.Delete_deltalake_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#insert_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_statement(Db2Parser.Insert_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#insert_datalake_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_datalake_statement(Db2Parser.Insert_datalake_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#values_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValues_item(Db2Parser.Values_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#merge_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMerge_statement(Db2Parser.Merge_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_view_fullselect}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_view_fullselect(Db2Parser.Table_view_fullselectContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#common_table_expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommon_table_expression_list(Db2Parser.Common_table_expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#matching_condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatching_condition(Db2Parser.Matching_conditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#modification_operation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModification_operation(Db2Parser.Modification_operationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#update_operation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate_operation(Db2Parser.Update_operationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#delete_operation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete_operation(Db2Parser.Delete_operationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#insert_operation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_operation(Db2Parser.Insert_operationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#expr_null_default_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_null_default_list(Db2Parser.Expr_null_default_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#isolation_level}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsolation_level(Db2Parser.Isolation_levelContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#truncate_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTruncate_statement(Db2Parser.Truncate_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#update_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate_statement(Db2Parser.Update_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#update_statement_searched_update}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate_statement_searched_update(Db2Parser.Update_statement_searched_updateContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#skip_wait}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSkip_wait(Db2Parser.Skip_waitContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#update_statement_positioned_update}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate_statement_positioned_update(Db2Parser.Update_statement_positioned_updateContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#include_columns}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInclude_columns(Db2Parser.Include_columnsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#assignment_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment_clause(Db2Parser.Assignment_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#assignment_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment_item(Db2Parser.Assignment_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#period_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPeriod_clause(Db2Parser.Period_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#time_sec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTime_sec(Db2Parser.Time_secContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#update_datalake_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate_datalake_statement(Db2Parser.Update_datalake_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_database_authorities_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_database_authorities_statement(Db2Parser.Grant_database_authorities_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#db_privilege_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_privilege_list(Db2Parser.Db_privilege_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#db_privilege}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_privilege(Db2Parser.Db_privilegeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grantee}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantee(Db2Parser.GranteeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grantee_user_group}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantee_user_group(Db2Parser.Grantee_user_groupContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#user_group}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUser_group(Db2Parser.User_groupContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grantee_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantee_list(Db2Parser.Grantee_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grantee_list_public}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantee_list_public(Db2Parser.Grantee_list_publicContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grantee_list_user_group}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantee_list_user_group(Db2Parser.Grantee_list_user_groupContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_exemption_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_exemption_statement(Db2Parser.Grant_exemption_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#exemption_privilege}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExemption_privilege(Db2Parser.Exemption_privilegeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_global_variable_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_global_variable_privileges_statement(Db2Parser.Grant_global_variable_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#variable_privilege}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable_privilege(Db2Parser.Variable_privilegeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#read_write}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRead_write(Db2Parser.Read_writeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#with_grant_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_grant_option(Db2Parser.With_grant_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_index_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_index_privileges_statement(Db2Parser.Grant_index_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_module_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_module_privileges_statement(Db2Parser.Grant_module_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_package_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_package_privileges_statement(Db2Parser.Grant_package_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#package_privilege_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackage_privilege_list(Db2Parser.Package_privilege_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#package_privilege}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackage_privilege(Db2Parser.Package_privilegeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_role_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_role_statement(Db2Parser.Grant_role_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#role_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRole_list(Db2Parser.Role_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_routine_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_routine_privileges_statement(Db2Parser.Grant_routine_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_schema_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_schema_privileges_statement(Db2Parser.Grant_schema_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#schema_privilege_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_privilege_list(Db2Parser.Schema_privilege_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#schema_privilege}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_privilege(Db2Parser.Schema_privilegeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_security_label_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_security_label_statement(Db2Parser.Grant_security_label_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_sequence_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_sequence_privileges_statement(Db2Parser.Grant_sequence_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sequence_privilege_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequence_privilege_list(Db2Parser.Sequence_privilege_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sequence_privilege}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequence_privilege(Db2Parser.Sequence_privilegeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_server_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_server_privileges_statement(Db2Parser.Grant_server_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_setsessionuser_privilege_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_setsessionuser_privilege_statement(Db2Parser.Grant_setsessionuser_privilege_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#user_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUser_list(Db2Parser.User_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#user_auth}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUser_auth(Db2Parser.User_authContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_table_space_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_table_space_privileges_statement(Db2Parser.Grant_table_space_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_table_view_or_nickname_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_table_view_or_nickname_privileges_statement(Db2Parser.Grant_table_view_or_nickname_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#tvn_privilege_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTvn_privilege_list(Db2Parser.Tvn_privilege_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#tvn_privilege}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTvn_privilege(Db2Parser.Tvn_privilegeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#column_name_list_paren}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_name_list_paren(Db2Parser.Column_name_list_parenContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#column_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_name_list(Db2Parser.Column_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_workload_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_workload_privileges_statement(Db2Parser.Grant_workload_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_xsr_object_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_xsr_object_privileges_statement(Db2Parser.Grant_xsr_object_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_database_authorities_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_database_authorities_statement(Db2Parser.Revoke_database_authorities_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#by_all}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBy_all(Db2Parser.By_allContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_exemption_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_exemption_statement(Db2Parser.Revoke_exemption_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_global_variable_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_global_variable_privileges_statement(Db2Parser.Revoke_global_variable_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_index_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_index_privileges_statement(Db2Parser.Revoke_index_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_module_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_module_privileges_statement(Db2Parser.Revoke_module_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_package_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_package_privileges_statement(Db2Parser.Revoke_package_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_role_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_role_statement(Db2Parser.Revoke_role_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_routine_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_routine_privileges_statement(Db2Parser.Revoke_routine_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_schema_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_schema_privileges_statement(Db2Parser.Revoke_schema_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_security_label_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_security_label_statement(Db2Parser.Revoke_security_label_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_sequence_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_sequence_privileges_statement(Db2Parser.Revoke_sequence_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_server_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_server_privileges_statement(Db2Parser.Revoke_server_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_setsessionuser_privilege_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_setsessionuser_privilege_statement(Db2Parser.Revoke_setsessionuser_privilege_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_table_space_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_table_space_privileges_statement(Db2Parser.Revoke_table_space_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_table_view_or_nickname_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_table_view_or_nickname_privileges_statement(Db2Parser.Revoke_table_view_or_nickname_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_workload_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_workload_privileges_statement(Db2Parser.Revoke_workload_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#revoke_xsr_object_privileges_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_xsr_object_privileges_statement(Db2Parser.Revoke_xsr_object_privileges_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#user_group_role}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUser_group_role(Db2Parser.User_group_roleContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#rollback_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRollback_statement(Db2Parser.Rollback_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#savepoint_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSavepoint_statement(Db2Parser.Savepoint_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#release_savepoint_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelease_savepoint_statement(Db2Parser.Release_savepoint_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#allocate_cursor_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllocate_cursor_statement(Db2Parser.Allocate_cursor_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_audit_policy_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_audit_policy_statement(Db2Parser.Alter_audit_policy_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#status_spec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatus_spec(Db2Parser.Status_specContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#normal_audit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNormal_audit(Db2Parser.Normal_auditContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_bufferpool_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_bufferpool_statement(Db2Parser.Alter_bufferpool_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#immediate_deferred}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImmediate_deferred(Db2Parser.Immediate_deferredContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_database_partition_group_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_partition_group_statement(Db2Parser.Alter_database_partition_group_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#db_partition_group_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_partition_group_list_item(Db2Parser.Db_partition_group_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#db_partition_num_nums}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_partition_num_nums(Db2Parser.Db_partition_num_numsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#db_partitions_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_partitions_clause(Db2Parser.Db_partitions_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#db_partition_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_partition_options(Db2Parser.Db_partition_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_database_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_statement(Db2Parser.Alter_database_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_database_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_database_opts(Db2Parser.Alter_database_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_event_monitor_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_event_monitor_statement(Db2Parser.Alter_event_monitor_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_event_monitor_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_event_monitor_opts(Db2Parser.Alter_event_monitor_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_function_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_function_statement(Db2Parser.Alter_function_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_function_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_function_opts(Db2Parser.Alter_function_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#function_designator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_designator(Db2Parser.Function_designatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#data_type_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_type_list(Db2Parser.Data_type_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#data_type_list_paren}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_type_list_paren(Db2Parser.Data_type_list_parenContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_histogram_template_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_histogram_template_statement(Db2Parser.Alter_histogram_template_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_index_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_index_statement(Db2Parser.Alter_index_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#yes_no}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYes_no(Db2Parser.Yes_noContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_mask_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_mask_statement(Db2Parser.Alter_mask_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#enable_disable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnable_disable(Db2Parser.Enable_disableContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_method_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_method_statement(Db2Parser.Alter_method_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#method_designator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_designator(Db2Parser.Method_designatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_model_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_model_statement(Db2Parser.Alter_model_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_module_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_module_statement(Db2Parser.Alter_module_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_module_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_module_opts(Db2Parser.Alter_module_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#module_function_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModule_function_definition(Db2Parser.Module_function_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#module_procedure_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModule_procedure_definition(Db2Parser.Module_procedure_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#module_type_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModule_type_definition(Db2Parser.Module_type_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#module_variable_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModule_variable_definition(Db2Parser.Module_variable_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#module_condition_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModule_condition_definition(Db2Parser.Module_condition_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#module_object_identification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModule_object_identification(Db2Parser.Module_object_identificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#module_function_designator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModule_function_designator(Db2Parser.Module_function_designatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#module_procedure_designator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModule_procedure_designator(Db2Parser.Module_procedure_designatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_nickname_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_nickname_statement(Db2Parser.Alter_nickname_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_nickname_opts_1}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_nickname_opts_1(Db2Parser.Alter_nickname_opts_1Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_nickname_opts_1_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_nickname_opts_1_item(Db2Parser.Alter_nickname_opts_1_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_nickname_opts_2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_nickname_opts_2(Db2Parser.Alter_nickname_opts_2Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_nickname_opts_2_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_nickname_opts_2_item(Db2Parser.Alter_nickname_opts_2_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#constraint_alteration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraint_alteration(Db2Parser.Constraint_alterationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_package_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_package_statement(Db2Parser.Alter_package_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_package_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_package_opts(Db2Parser.Alter_package_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_permission_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_permission_statement(Db2Parser.Alter_permission_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_procedure_external_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_procedure_external_statement(Db2Parser.Alter_procedure_external_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_procedure_external_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_procedure_external_opts(Db2Parser.Alter_procedure_external_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#procedure_designator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedure_designator(Db2Parser.Procedure_designatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_procedure_sourced_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_procedure_sourced_statement(Db2Parser.Alter_procedure_sourced_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#parameter_alteration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameter_alteration(Db2Parser.Parameter_alterationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_procedure_sql_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_procedure_sql_statement(Db2Parser.Alter_procedure_sql_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_schema_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_schema_statement(Db2Parser.Alter_schema_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#none_changes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNone_changes(Db2Parser.None_changesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_security_label_component_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_security_label_component_statement(Db2Parser.Alter_security_label_component_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#add_element_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdd_element_clause(Db2Parser.Add_element_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#array_element_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_element_clause(Db2Parser.Array_element_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#tree_element_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTree_element_clause(Db2Parser.Tree_element_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_security_policy_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_security_policy_statement(Db2Parser.Alter_security_policy_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_security_policy_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_security_policy_opts(Db2Parser.Alter_security_policy_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_sequence_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_sequence_statement(Db2Parser.Alter_sequence_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_sequence_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_sequence_opts(Db2Parser.Alter_sequence_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_server_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_server_statement(Db2Parser.Alter_server_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_server_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_server_opts(Db2Parser.Alter_server_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_service_class_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_service_class_statement(Db2Parser.Alter_service_class_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_service_class_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_service_class_opts(Db2Parser.Alter_service_class_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#default_on_off}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefault_on_off(Db2Parser.Default_on_offContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#default_high_medium_low}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefault_high_medium_low(Db2Parser.Default_high_medium_lowContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_stogroup_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_stogroup_statement(Db2Parser.Alter_stogroup_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_stogroup_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_stogroup_opts(Db2Parser.Alter_stogroup_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_statement(Db2Parser.Alter_table_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_table_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_opts(Db2Parser.Alter_table_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#null_on_off}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNull_on_off(Db2Parser.Null_on_offContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#cascade_restrict}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCascade_restrict(Db2Parser.Cascade_restrictContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#materialized_query_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMaterialized_query_definition(Db2Parser.Materialized_query_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#refreshable_table_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefreshable_table_options(Db2Parser.Refreshable_table_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#column_alteration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_alteration(Db2Parser.Column_alterationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#generation_alteration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneration_alteration(Db2Parser.Generation_alterationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#identity_alteration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentity_alteration(Db2Parser.Identity_alterationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#generation_attribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneration_attribute(Db2Parser.Generation_attributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#as_identity_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAs_identity_clause(Db2Parser.As_identity_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#as_identity_clause_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAs_identity_clause_opts(Db2Parser.As_identity_clause_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#period_definition_alter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPeriod_definition_alter(Db2Parser.Period_definition_alterContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#add_partition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdd_partition(Db2Parser.Add_partitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#boundary_spec_alter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoundary_spec_alter(Db2Parser.Boundary_spec_alterContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#attach_partition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttach_partition(Db2Parser.Attach_partitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#activate_deactivate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActivate_deactivate(Db2Parser.Activate_deactivateContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_tablespace_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_tablespace_statement(Db2Parser.Alter_tablespace_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_tablespace_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_tablespace_opts(Db2Parser.Alter_tablespace_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#add_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdd_clause(Db2Parser.Add_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#db_container_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_container_clause(Db2Parser.Db_container_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#db_container_clause_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_container_clause_opts(Db2Parser.Db_container_clause_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#drop_container_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_container_clause(Db2Parser.Drop_container_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#file_device}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFile_device(Db2Parser.File_deviceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#all_containers_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAll_containers_clause(Db2Parser.All_containers_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#system_container_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSystem_container_clause(Db2Parser.System_container_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#stripeset}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStripeset(Db2Parser.StripesetContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#km}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKm(Db2Parser.KmContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#kmg_percent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKmg_percent(Db2Parser.Kmg_percentContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_threshold_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_threshold_statement(Db2Parser.Alter_threshold_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_threshold_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_threshold_opts(Db2Parser.Alter_threshold_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_threshold_predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_threshold_predicate(Db2Parser.Alter_threshold_predicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_threshold_exceeded_actions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_threshold_exceeded_actions(Db2Parser.Alter_threshold_exceeded_actionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#dt_units}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_units(Db2Parser.Dt_unitsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#dt_units_with_seconds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDt_units_with_seconds(Db2Parser.Dt_units_with_secondsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_trigger_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_trigger_statement(Db2Parser.Alter_trigger_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_trusted_context_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_trusted_context_statement(Db2Parser.Alter_trusted_context_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_trusted_context_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_trusted_context_opts(Db2Parser.Alter_trusted_context_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_trusted_context_opts_alter_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_trusted_context_opts_alter_opts(Db2Parser.Alter_trusted_context_opts_alter_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#addr_clause_encryption_val}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddr_clause_encryption_val(Db2Parser.Addr_clause_encryption_valContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#address_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddress_clause(Db2Parser.Address_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#user_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUser_clause(Db2Parser.User_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#use_for_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUse_for_opts(Db2Parser.Use_for_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#use_for_opts_2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUse_for_opts_2(Db2Parser.Use_for_opts_2Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_type_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_type_statement(Db2Parser.Alter_type_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_type_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_type_opts(Db2Parser.Alter_type_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#method_identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_identifier(Db2Parser.Method_identifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#method_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_options(Db2Parser.Method_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_usage_list_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_usage_list_statement(Db2Parser.Alter_usage_list_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_usage_list_opts_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_usage_list_opts_item(Db2Parser.Alter_usage_list_opts_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_user_mapping_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_user_mapping_statement(Db2Parser.Alter_user_mapping_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_user_mapping_opts_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_user_mapping_opts_item(Db2Parser.Alter_user_mapping_opts_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#add_set}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdd_set(Db2Parser.Add_setContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_view_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_view_statement(Db2Parser.Alter_view_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_view_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_view_opts(Db2Parser.Alter_view_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_work_action_set_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_work_action_set_statement(Db2Parser.Alter_work_action_set_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_work_action_set_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_work_action_set_opts(Db2Parser.Alter_work_action_set_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#work_action_alteration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWork_action_alteration(Db2Parser.Work_action_alterationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#work_action_alteration_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWork_action_alteration_opts(Db2Parser.Work_action_alteration_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_action_types_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_action_types_clause(Db2Parser.Alter_action_types_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#threshold_predicate_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThreshold_predicate_clause(Db2Parser.Threshold_predicate_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_work_class_set_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_work_class_set_statement(Db2Parser.Alter_work_class_set_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_work_class_set_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_work_class_set_opts(Db2Parser.Alter_work_class_set_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#work_class_alteration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWork_class_alteration(Db2Parser.Work_class_alterationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#work_class_alteration_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWork_class_alteration_opts(Db2Parser.Work_class_alteration_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#for_from_to_alter_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_from_to_alter_clause(Db2Parser.For_from_to_alter_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#schema_alter_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_alter_clause(Db2Parser.Schema_alter_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#data_tag_alter_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_tag_alter_clause(Db2Parser.Data_tag_alter_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_workload_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_workload_statement(Db2Parser.Alter_workload_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_workload_opts_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_workload_opts_item(Db2Parser.Alter_workload_opts_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#package_executable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackage_executable(Db2Parser.Package_executableContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#base_none}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBase_none(Db2Parser.Base_noneContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#extended_base_none}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtended_base_none(Db2Parser.Extended_base_noneContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_collect_activity_data_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_collect_activity_data_clause(Db2Parser.Alter_collect_activity_data_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#with_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_opts(Db2Parser.With_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_collect_history_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_collect_history_clause(Db2Parser.Alter_collect_history_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_collect_lock_wait_data_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_collect_lock_wait_data_clause(Db2Parser.Alter_collect_lock_wait_data_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_wrapper_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_wrapper_statement(Db2Parser.Alter_wrapper_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_wrapper_opts_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_wrapper_opts_item(Db2Parser.Alter_wrapper_opts_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alter_xsrobject_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_xsrobject_statement(Db2Parser.Alter_xsrobject_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#string}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString(Db2Parser.StringContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#string_constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString_constant(Db2Parser.String_constantContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#numeric_constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumeric_constant(Db2Parser.Numeric_constantContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#data_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_type(Db2Parser.Data_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#anchored_data_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnchored_data_type(Db2Parser.Anchored_data_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#anchored_non_row_data_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnchored_non_row_data_type(Db2Parser.Anchored_non_row_data_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#anchored_row_data_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnchored_row_data_type(Db2Parser.Anchored_row_data_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#source_data_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSource_data_type(Db2Parser.Source_data_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#data_type_constrainst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_type_constrainst(Db2Parser.Data_type_constrainstContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#check_condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheck_condition(Db2Parser.Check_conditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#data_type_2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_type_2(Db2Parser.Data_type_2Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#built_in_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBuilt_in_type(Db2Parser.Built_in_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#integer_paren}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger_paren(Db2Parser.Integer_parenContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#integer_kmg_paren}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger_kmg_paren(Db2Parser.Integer_kmg_parenContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#char_character}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChar_character(Db2Parser.Char_characterContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#octets_codeunits}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOctets_codeunits(Db2Parser.Octets_codeunitsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#codeunits}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCodeunits(Db2Parser.CodeunitsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#kmg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKmg(Db2Parser.KmgContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#rs_locator_variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRs_locator_variable(Db2Parser.Rs_locator_variableContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#integer_constant_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger_constant_list(Db2Parser.Integer_constant_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#integer_constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger_constant(Db2Parser.Integer_constantContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#integer_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger_value(Db2Parser.Integer_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#positive_integer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositive_integer(Db2Parser.Positive_integerContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#bigint_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBigint_value(Db2Parser.Bigint_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#bigint_constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBigint_constant(Db2Parser.Bigint_constantContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#member_number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMember_number(Db2Parser.Member_numberContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#version_id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVersion_id(Db2Parser.Version_idContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#drop_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_statement(Db2Parser.Drop_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alias_designator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias_designator(Db2Parser.Alias_designatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#service_class_designator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitService_class_designator(Db2Parser.Service_class_designatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#tablespace_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablespace_name_list(Db2Parser.Tablespace_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#associate_locators_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssociate_locators_statement(Db2Parser.Associate_locators_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#audit_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAudit_statement(Db2Parser.Audit_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#begin_declare_section_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBegin_declare_section_statement(Db2Parser.Begin_declare_section_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#call_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCall_statement(Db2Parser.Call_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#arg_list_paren}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArg_list_paren(Db2Parser.Arg_list_parenContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#arg_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArg_list(Db2Parser.Arg_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#argument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgument(Db2Parser.ArgumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#case_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_statement(Db2Parser.Case_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#searched_case_statement_when_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearched_case_statement_when_clause(Db2Parser.Searched_case_statement_when_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#simple_case_statement_when_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_case_statement_when_clause(Db2Parser.Simple_case_statement_when_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#close_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClose_statement(Db2Parser.Close_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#comment_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment_statement(Db2Parser.Comment_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#column_comment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_comment(Db2Parser.Column_commentContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#comment_objects}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment_objects(Db2Parser.Comment_objectsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#commit_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommit_statement(Db2Parser.Commit_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#connect_type_1_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConnect_type_1_statement(Db2Parser.Connect_type_1_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#authorization}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAuthorization(Db2Parser.AuthorizationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#passwords}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPasswords(Db2Parser.PasswordsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#lock_block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLock_block(Db2Parser.Lock_blockContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#accesstoken}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAccesstoken(Db2Parser.AccesstokenContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#token}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitToken(Db2Parser.TokenContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#api_key}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitApi_key(Db2Parser.Api_keyContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#token_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitToken_type(Db2Parser.Token_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#declare_cursor_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclare_cursor_statement(Db2Parser.Declare_cursor_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#declare_global_temporary_table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclare_global_temporary_table_statement(Db2Parser.Declare_global_temporary_table_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#describe_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_statement(Db2Parser.Describe_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#xquery_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXquery_statement(Db2Parser.Xquery_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#describe_input_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_input_statement(Db2Parser.Describe_input_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#describe_output_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_output_statement(Db2Parser.Describe_output_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#disconnect_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDisconnect_statement(Db2Parser.Disconnect_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#end_declare_section_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnd_declare_section_statement(Db2Parser.End_declare_section_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#execute_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecute_statement(Db2Parser.Execute_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#host_variable_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHost_variable_expression(Db2Parser.Host_variable_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#assignment_target}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment_target(Db2Parser.Assignment_targetContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#execute_immediate_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecute_immediate_statement(Db2Parser.Execute_immediate_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#explain_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplain_statement(Db2Parser.Explain_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#explainable_sql_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplainable_sql_statement(Db2Parser.Explainable_sql_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#fetch_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_statement(Db2Parser.Fetch_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#flush_bufferpools_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlush_bufferpools_statement(Db2Parser.Flush_bufferpools_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#flush_event_monitor_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlush_event_monitor_statement(Db2Parser.Flush_event_monitor_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#flush_federated_cache_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlush_federated_cache_statement(Db2Parser.Flush_federated_cache_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#flush_optimization_profile_cache_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlush_optimization_profile_cache_statement(Db2Parser.Flush_optimization_profile_cache_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#flush_package_cache_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlush_package_cache_statement(Db2Parser.Flush_package_cache_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#flush_authentication_cache_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlush_authentication_cache_statement(Db2Parser.Flush_authentication_cache_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#free_locator_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFree_locator_statement(Db2Parser.Free_locator_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#get_diagnostics_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGet_diagnostics_statement(Db2Parser.Get_diagnostics_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#statement_information}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement_information(Db2Parser.Statement_informationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#condition_information}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondition_information(Db2Parser.Condition_informationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#condition_var_assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondition_var_assignment(Db2Parser.Condition_var_assignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#lock_table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLock_table_statement(Db2Parser.Lock_table_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#pipe_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPipe_statement(Db2Parser.Pipe_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#refresh_table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefresh_table_statement(Db2Parser.Refresh_table_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#release_connection_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelease_connection_statement(Db2Parser.Release_connection_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#rename_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRename_statement(Db2Parser.Rename_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#rename_stogroup_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRename_stogroup_statement(Db2Parser.Rename_stogroup_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#rename_tablespace_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRename_tablespace_statement(Db2Parser.Rename_tablespace_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#set_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_statement(Db2Parser.Set_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#access_mode_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAccess_mode_clause(Db2Parser.Access_mode_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#cascade_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCascade_clause(Db2Parser.Cascade_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#to_descendent_types}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTo_descendent_types(Db2Parser.To_descendent_typesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_type_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_type_list(Db2Parser.Table_type_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_type(Db2Parser.Table_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_checked_options_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_checked_options_list(Db2Parser.Table_checked_options_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_checked_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_checked_options(Db2Parser.Table_checked_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#online_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOnline_options(Db2Parser.Online_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#query_optimization_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuery_optimization_options(Db2Parser.Query_optimization_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#check_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheck_options(Db2Parser.Check_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#incremental_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIncremental_options(Db2Parser.Incremental_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#exception_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitException_clause(Db2Parser.Exception_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#in_table_use_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIn_table_use_clause(Db2Parser.In_table_use_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_unchecked_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_unchecked_options(Db2Parser.Table_unchecked_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#full_access}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFull_access(Db2Parser.Full_accessContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#integrity_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegrity_options(Db2Parser.Integrity_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#integrity_options_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegrity_options_item(Db2Parser.Integrity_options_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#var_def_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_def_list(Db2Parser.Var_def_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#var_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_def(Db2Parser.Var_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#expr_null}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_null(Db2Parser.Expr_nullContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#expr_null_default}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_null_default(Db2Parser.Expr_null_defaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#array_index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_index(Db2Parser.Array_indexContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#row_fullselect}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow_fullselect(Db2Parser.Row_fullselectContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#target_variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_variable(Db2Parser.Target_variableContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#target_cursor_variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_cursor_variable(Db2Parser.Target_cursor_variableContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#target_row_variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_row_variable(Db2Parser.Target_row_variableContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#row_array_element_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow_array_element_specification(Db2Parser.Row_array_element_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#row_field_reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow_field_reference(Db2Parser.Row_field_referenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#field_reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField_reference(Db2Parser.Field_referenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#search_condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_condition(Db2Parser.Search_conditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(Db2Parser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#according_to_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAccording_to_clause(Db2Parser.According_to_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#xml_schema_identification_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXml_schema_identification_list(Db2Parser.Xml_schema_identification_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#xml_schema_identification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXml_schema_identification(Db2Parser.Xml_schema_identificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#fullselect_in_parentheses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFullselect_in_parentheses(Db2Parser.Fullselect_in_parenthesesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#some_any_all}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSome_any_all(Db2Parser.Some_any_allContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#row_value_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow_value_expression(Db2Parser.Row_value_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#comparison_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison_operator(Db2Parser.Comparison_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#row_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow_expression(Db2Parser.Row_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#path_opt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath_opt_list(Db2Parser.Path_opt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#path_opt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath_opt(Db2Parser.Path_optContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#pkg_opt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPkg_opt_list(Db2Parser.Pkg_opt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#pkg_opt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPkg_opt(Db2Parser.Pkg_optContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#maintain_opt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMaintain_opt_list(Db2Parser.Maintain_opt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#maintain_opt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMaintain_opt(Db2Parser.Maintain_optContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(Db2Parser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#host_variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHost_variable(Db2Parser.Host_variableContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#set_integrity_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_integrity_statement(Db2Parser.Set_integrity_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#transfer_ownership_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransfer_ownership_statement(Db2Parser.Transfer_ownership_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#objects}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjects(Db2Parser.ObjectsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#whenever_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenever_statement(Db2Parser.Whenever_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#for_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_statement(Db2Parser.For_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#goto_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGoto_statement(Db2Parser.Goto_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#if_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_statement(Db2Parser.If_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#include_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInclude_statement(Db2Parser.Include_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#resignal_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResignal_statement(Db2Parser.Resignal_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#signal_information}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignal_information(Db2Parser.Signal_informationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#diagnostic_string_constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDiagnostic_string_constant(Db2Parser.Diagnostic_string_constantContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#signal_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignal_statement(Db2Parser.Signal_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sqlstate_string_constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSqlstate_string_constant(Db2Parser.Sqlstate_string_constantContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sqlstate_string_variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSqlstate_string_variable(Db2Parser.Sqlstate_string_variableContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#signal_information_2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignal_information_2(Db2Parser.Signal_information_2Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#diagnostic_string_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDiagnostic_string_expression(Db2Parser.Diagnostic_string_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#iterate_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIterate_statement(Db2Parser.Iterate_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#leave_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLeave_statement(Db2Parser.Leave_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#loop_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoop_statement(Db2Parser.Loop_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#open_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpen_statement(Db2Parser.Open_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#variable_or_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable_or_expression(Db2Parser.Variable_or_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#select_into_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_into_statement(Db2Parser.Select_into_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#values_into_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValues_into_statement(Db2Parser.Values_into_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#prepare_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrepare_statement(Db2Parser.Prepare_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#repeat_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRepeat_statement(Db2Parser.Repeat_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#return_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturn_statement(Db2Parser.Return_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#while_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhile_statement(Db2Parser.While_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_routine_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_routine_statement(Db2Parser.Sql_routine_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#common_table_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommon_table_expression(Db2Parser.Common_table_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_alias_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_alias_statement(Db2Parser.Create_alias_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_alias(Db2Parser.Table_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#module_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModule_alias(Db2Parser.Module_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sequence_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequence_alias(Db2Parser.Sequence_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#or_replace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr_replace(Db2Parser.Or_replaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_audit_policy_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_audit_policy_statement(Db2Parser.Create_audit_policy_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#audit_policy_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAudit_policy_opts(Db2Parser.Audit_policy_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#audit_policy_categories_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAudit_policy_categories_opts(Db2Parser.Audit_policy_categories_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_bufferpool_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_bufferpool_statement(Db2Parser.Create_bufferpool_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#bufferpool_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBufferpool_opts(Db2Parser.Bufferpool_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#except_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExcept_clause(Db2Parser.Except_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#member_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMember_list(Db2Parser.Member_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#member_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMember_list_item(Db2Parser.Member_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_database_partition_group_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_database_partition_group_statement(Db2Parser.Create_database_partition_group_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_event_monitor_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_event_monitor_statement(Db2Parser.Create_event_monitor_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_event_monitor_activities_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_event_monitor_activities_statement(Db2Parser.Create_event_monitor_activities_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#formatted_event_table_info_3}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormatted_event_table_info_3(Db2Parser.Formatted_event_table_info_3Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_event_monitor_change_history_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_event_monitor_change_history_statement(Db2Parser.Create_event_monitor_change_history_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#event_control_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvent_control_list(Db2Parser.Event_control_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#event_control}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvent_control(Db2Parser.Event_controlContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_event_monitor_locking_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_event_monitor_locking_statement(Db2Parser.Create_event_monitor_locking_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_event_monitor_package_cache_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_event_monitor_package_cache_statement(Db2Parser.Create_event_monitor_package_cache_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#filter_and_collection_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilter_and_collection_options(Db2Parser.Filter_and_collection_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#event_condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvent_condition(Db2Parser.Event_conditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#event_condition_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvent_condition_item(Db2Parser.Event_condition_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_event_monitor_statistics_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_event_monitor_statistics_statement(Db2Parser.Create_event_monitor_statistics_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#event_monitor_statistics_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvent_monitor_statistics_opts(Db2Parser.Event_monitor_statistics_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_event_monitor_threshold_violations_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_event_monitor_threshold_violations_statement(Db2Parser.Create_event_monitor_threshold_violations_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#formatted_event_table_info_2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormatted_event_table_info_2(Db2Parser.Formatted_event_table_info_2Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#file_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFile_options(Db2Parser.File_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#event_monitor_threshold_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvent_monitor_threshold_opts(Db2Parser.Event_monitor_threshold_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#pages}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPages(Db2Parser.PagesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_event_monitor_unit_of_work}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_event_monitor_unit_of_work(Db2Parser.Create_event_monitor_unit_of_workContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#formatted_event_table_info}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormatted_event_table_info(Db2Parser.Formatted_event_table_infoContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#autostart_manualstart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAutostart_manualstart(Db2Parser.Autostart_manualstartContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#evm_group}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvm_group(Db2Parser.Evm_groupContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#target_table_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_table_options(Db2Parser.Target_table_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_external_table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_external_table_statement(Db2Parser.Create_external_table_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#ext_table_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExt_table_option(Db2Parser.Ext_table_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#ext_table_option_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExt_table_option_value(Db2Parser.Ext_table_option_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_function_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_function_statement(Db2Parser.Create_function_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_function_aggregate_interface_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_function_aggregate_interface_statement(Db2Parser.Create_function_aggregate_interface_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#agg_fn_param_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAgg_fn_param_decl(Db2Parser.Agg_fn_param_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#agg_fn_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAgg_fn_option_list(Db2Parser.Agg_fn_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#state_variable_declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitState_variable_declaration(Db2Parser.State_variable_declarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_function_external_scalar_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_function_external_scalar_statement(Db2Parser.Create_function_external_scalar_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#ext_scalar_param_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExt_scalar_param_decl(Db2Parser.Ext_scalar_param_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#ext_scalar_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExt_scalar_option_list(Db2Parser.Ext_scalar_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#ext_scalar_option_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExt_scalar_option_list_item(Db2Parser.Ext_scalar_option_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#predicate_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate_specification(Db2Parser.Predicate_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#data_filter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_filter(Db2Parser.Data_filterContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#index_exploitation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_exploitation(Db2Parser.Index_exploitationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#exploitation_rule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExploitation_rule(Db2Parser.Exploitation_ruleContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_function_external_table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_function_external_table_statement(Db2Parser.Create_function_external_table_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#ext_table_param_decl_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExt_table_param_decl_list(Db2Parser.Ext_table_param_decl_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#ext_table_param_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExt_table_param_decl(Db2Parser.Ext_table_param_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#ext_table_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExt_table_option_list(Db2Parser.Ext_table_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#ext_table_option_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExt_table_option_list_item(Db2Parser.Ext_table_option_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_function_old_db_external_function_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_function_old_db_external_function_statement(Db2Parser.Create_function_old_db_external_function_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#oledb_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOledb_option_list(Db2Parser.Oledb_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#oledb_option_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOledb_option_list_item(Db2Parser.Oledb_option_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_function_sourced_or_template_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_function_sourced_or_template_statement(Db2Parser.Create_function_sourced_or_template_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#fn_return_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFn_return_opts(Db2Parser.Fn_return_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#fn_return_opts_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFn_return_opts_item(Db2Parser.Fn_return_opts_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#template_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemplate_opts(Db2Parser.Template_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#template_opts_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemplate_opts_item(Db2Parser.Template_opts_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#ascii_unicode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAscii_unicode(Db2Parser.Ascii_unicodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#param_decl_list_3}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_decl_list_3(Db2Parser.Param_decl_list_3Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#param_decl_3}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_decl_3(Db2Parser.Param_decl_3Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_function_sql_scalar_table_or_row_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_function_sql_scalar_table_or_row_statement(Db2Parser.Create_function_sql_scalar_table_or_row_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#param_decl_list_2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_decl_list_2(Db2Parser.Param_decl_list_2Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#param_decl_2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_decl_2(Db2Parser.Param_decl_2Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_function_body}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_function_body(Db2Parser.Sql_function_bodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_function_mapping_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_function_mapping_statement(Db2Parser.Create_function_mapping_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#function_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_options(Db2Parser.Function_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#function_option_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_option_name(Db2Parser.Function_option_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_global_temporary_table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_global_temporary_table_statement(Db2Parser.Create_global_temporary_table_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_global_temporary_table_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_global_temporary_table_opts(Db2Parser.Create_global_temporary_table_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_global_temporary_table_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_global_temporary_table_item(Db2Parser.Create_global_temporary_table_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#delete_preserve}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete_preserve(Db2Parser.Delete_preserveContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_histogram_template_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_histogram_template_statement(Db2Parser.Create_histogram_template_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_index_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_index_statement(Db2Parser.Create_index_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#index_col_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_col_opts(Db2Parser.Index_col_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#index_col_opts_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_col_opts_item(Db2Parser.Index_col_opts_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#key_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKey_expression(Db2Parser.Key_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_index_extension_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_index_extension_statement(Db2Parser.Create_index_extension_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#param_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_list(Db2Parser.Param_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#index_maintenance}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_maintenance(Db2Parser.Index_maintenanceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_function_invocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_function_invocation(Db2Parser.Table_function_invocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#index_search}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_search(Db2Parser.Index_searchContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#search_method_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_method_definition(Db2Parser.Search_method_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_mask_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_mask_statement(Db2Parser.Create_mask_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#case_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_expression(Db2Parser.Case_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#range_producing_funciton_invocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRange_producing_funciton_invocation(Db2Parser.Range_producing_funciton_invocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#index_filtering_function_invocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_filtering_function_invocation(Db2Parser.Index_filtering_function_invocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_method_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_method_statement(Db2Parser.Create_method_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#method_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_opts(Db2Parser.Method_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#method_opts_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_opts_item(Db2Parser.Method_opts_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#method_signature}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_signature(Db2Parser.Method_signatureContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#method_param_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_param_list(Db2Parser.Method_param_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#data_type_3}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_type_3(Db2Parser.Data_type_3Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#data_type_4}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_type_4(Db2Parser.Data_type_4Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_method_body}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_method_body(Db2Parser.Sql_method_bodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#compound_sql_inlined}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompound_sql_inlined(Db2Parser.Compound_sql_inlinedContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_statement_inlined}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_statement_inlined(Db2Parser.Sql_statement_inlinedContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#compound_sql_compiled}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompound_sql_compiled(Db2Parser.Compound_sql_compiledContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_statement_compiled}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_statement_compiled(Db2Parser.Sql_statement_compiledContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_module_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_module_statement(Db2Parser.Create_module_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_nickname_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_nickname_statement(Db2Parser.Create_nickname_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#nick_name_option_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNick_name_option_name(Db2Parser.Nick_name_option_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#remote_object_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemote_object_name(Db2Parser.Remote_object_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#non_relational_data_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNon_relational_data_definition(Db2Parser.Non_relational_data_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#nick_name_column_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNick_name_column_list(Db2Parser.Nick_name_column_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#nick_name_column_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNick_name_column_list_item(Db2Parser.Nick_name_column_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#nick_name_column_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNick_name_column_definition(Db2Parser.Nick_name_column_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#nick_name_column_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNick_name_column_options(Db2Parser.Nick_name_column_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#federated_column_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFederated_column_options(Db2Parser.Federated_column_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#column_option_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_option_name(Db2Parser.Column_option_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_permission_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_permission_statement(Db2Parser.Create_permission_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_procedure_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_procedure_statement(Db2Parser.Create_procedure_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_procedure_external_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_procedure_external_statement(Db2Parser.Create_procedure_external_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#proc_ext_param_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProc_ext_param_list(Db2Parser.Proc_ext_param_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#proc_ext_param}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProc_ext_param(Db2Parser.Proc_ext_paramContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#option_list_2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOption_list_2(Db2Parser.Option_list_2Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#option_list_2_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOption_list_2_item(Db2Parser.Option_list_2_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_procedure_sourced_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_procedure_sourced_statement(Db2Parser.Create_procedure_sourced_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#source_procedure_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSource_procedure_clause(Db2Parser.Source_procedure_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#source_object_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSource_object_name(Db2Parser.Source_object_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#option_list_1}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOption_list_1(Db2Parser.Option_list_1Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#option_list_1_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOption_list_1_item(Db2Parser.Option_list_1_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#result_set_element_number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResult_set_element_number(Db2Parser.Result_set_element_numberContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#unique_id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnique_id(Db2Parser.Unique_idContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_procedure_sql_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_procedure_sql_statement(Db2Parser.Create_procedure_sql_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#proc_parameter_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProc_parameter_list(Db2Parser.Proc_parameter_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#proc_parameter_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProc_parameter_list_item(Db2Parser.Proc_parameter_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#in_out_inout}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIn_out_inout(Db2Parser.In_out_inoutContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOption_list(Db2Parser.Option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#option_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOption_list_item(Db2Parser.Option_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_procedure_body}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_procedure_body(Db2Parser.Sql_procedure_bodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_role_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_role_statement(Db2Parser.Create_role_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_schema_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_schema_statement(Db2Parser.Create_schema_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#schema_sql_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_sql_statement(Db2Parser.Schema_sql_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_security_label_component_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_security_label_component_statement(Db2Parser.Create_security_label_component_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#array_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_clause(Db2Parser.Array_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#set_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_clause(Db2Parser.Set_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#tree_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTree_clause(Db2Parser.Tree_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#tree_clause_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTree_clause_item(Db2Parser.Tree_clause_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_security_label_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_security_label_statement(Db2Parser.Create_security_label_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_security_label_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_security_label_item(Db2Parser.Create_security_label_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_security_policy_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_security_policy_statement(Db2Parser.Create_security_policy_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_sequence_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_sequence_statement(Db2Parser.Create_sequence_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_sequence_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_sequence_opts(Db2Parser.Create_sequence_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_sequence_opts_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_sequence_opts_item(Db2Parser.Create_sequence_opts_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_service_class_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_service_class_statement(Db2Parser.Create_service_class_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#high_medium_low}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHigh_medium_low(Db2Parser.High_medium_lowContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#on_off}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOn_off(Db2Parser.On_offContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#soft_hard}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSoft_hard(Db2Parser.Soft_hardContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_server_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_server_statement(Db2Parser.Create_server_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#password_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPassword_(Db2Parser.Password_Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_stogroup_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_stogroup_statement(Db2Parser.Create_stogroup_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_stogroup_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_stogroup_opts(Db2Parser.Create_stogroup_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_synonym_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_synonym_statement(Db2Parser.Create_synonym_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_table_statement(Db2Parser.Create_table_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_table_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_table_opts(Db2Parser.Create_table_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_option_list(Db2Parser.Table_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_option_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_option_list_item(Db2Parser.Table_option_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_option_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_option_name(Db2Parser.Table_option_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#element_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElement_list(Db2Parser.Element_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#element_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElement_list_item(Db2Parser.Element_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#column_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_definition(Db2Parser.Column_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#period_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPeriod_definition(Db2Parser.Period_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#unique_constraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnique_constraint(Db2Parser.Unique_constraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#referential_constraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferential_constraint(Db2Parser.Referential_constraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#check_constraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheck_constraint(Db2Parser.Check_constraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#column_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_options(Db2Parser.Column_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#column_options_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_options_item(Db2Parser.Column_options_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#references_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferences_clause(Db2Parser.References_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#rule_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRule_clause(Db2Parser.Rule_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#constraint_attributes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraint_attributes(Db2Parser.Constraint_attributesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#default_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefault_clause(Db2Parser.Default_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#default_values}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefault_values(Db2Parser.Default_valuesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#generated_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenerated_clause(Db2Parser.Generated_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#datetime_special_register}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatetime_special_register(Db2Parser.Datetime_special_registerContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#user_special_register}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUser_special_register(Db2Parser.User_special_registerContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#cast_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCast_function(Db2Parser.Cast_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#identity_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentity_options(Db2Parser.Identity_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#identity_options_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentity_options_item(Db2Parser.Identity_options_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#as_row_change_timestamp_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAs_row_change_timestamp_clause(Db2Parser.As_row_change_timestamp_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#as_generated_expression_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAs_generated_expression_clause(Db2Parser.As_generated_expression_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#generation_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneration_expression(Db2Parser.Generation_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#as_row_transaction_timestamp_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAs_row_transaction_timestamp_clause(Db2Parser.As_row_transaction_timestamp_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#as_row_transaction_start_id_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAs_row_transaction_start_id_clause(Db2Parser.As_row_transaction_start_id_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#oid_column_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOid_column_definition(Db2Parser.Oid_column_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#range_partition_spec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRange_partition_spec(Db2Parser.Range_partition_specContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#partition_expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_expression_list(Db2Parser.Partition_expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#partition_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_expression(Db2Parser.Partition_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#partition_element_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_element_list(Db2Parser.Partition_element_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#partition_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_element(Db2Parser.Partition_elementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#boundary_spec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoundary_spec(Db2Parser.Boundary_specContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#partition_tablespace_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_tablespace_options(Db2Parser.Partition_tablespace_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#duration_label}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDuration_label(Db2Parser.Duration_labelContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#starting_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStarting_clause(Db2Parser.Starting_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#const_min_max_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConst_min_max_list(Db2Parser.Const_min_max_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#const_min_max}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConst_min_max(Db2Parser.Const_min_maxContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#ending_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnding_clause(Db2Parser.Ending_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#typed_table_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTyped_table_options(Db2Parser.Typed_table_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#typed_element_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTyped_element_list(Db2Parser.Typed_element_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#typed_element_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTyped_element_list_item(Db2Parser.Typed_element_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#as_result_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAs_result_table(Db2Parser.As_result_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#copy_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_options(Db2Parser.Copy_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#materialized_query_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMaterialized_query_options(Db2Parser.Materialized_query_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#staging_table_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStaging_table_definition(Db2Parser.Staging_table_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#dimensions_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDimensions_clause(Db2Parser.Dimensions_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#col_names}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCol_names(Db2Parser.Col_namesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sequence_key_spec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequence_key_spec(Db2Parser.Sequence_key_specContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sequence_key_spec_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequence_key_spec_list(Db2Parser.Sequence_key_spec_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sequence_key_spec_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequence_key_spec_list_item(Db2Parser.Sequence_key_spec_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#tablespace_clauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablespace_clauses(Db2Parser.Tablespace_clausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#distribution_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistribution_clause(Db2Parser.Distribution_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#partitioning_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitioning_clause(Db2Parser.Partitioning_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#if_not_exists}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_not_exists(Db2Parser.If_not_existsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_tablespace_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_tablespace_statement(Db2Parser.Create_tablespace_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#storage_group}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStorage_group(Db2Parser.Storage_groupContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#size_attributes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSize_attributes(Db2Parser.Size_attributesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#system_containers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSystem_containers(Db2Parser.System_containersContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#container_string_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContainer_string_list(Db2Parser.Container_string_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#database_containers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatabase_containers(Db2Parser.Database_containersContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#container_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContainer_clause(Db2Parser.Container_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#container_clause_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContainer_clause_list(Db2Parser.Container_clause_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#container_clause_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContainer_clause_list_item(Db2Parser.Container_clause_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#on_db_partitions_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOn_db_partitions_clause(Db2Parser.On_db_partitions_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#db_partition_number_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_partition_number_list(Db2Parser.Db_partition_number_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#db_partition_number_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_partition_number_list_item(Db2Parser.Db_partition_number_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#db_partition_number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_partition_number(Db2Parser.Db_partition_numberContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#number_of_pages}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber_of_pages(Db2Parser.Number_of_pagesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#number_of_files}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber_of_files(Db2Parser.Number_of_filesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#number_of_milliseconds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber_of_milliseconds(Db2Parser.Number_of_millisecondsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#number_megabytes_per_second}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber_megabytes_per_second(Db2Parser.Number_megabytes_per_secondContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_threshold_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_threshold_statement(Db2Parser.Create_threshold_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#threshold_domain}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThreshold_domain(Db2Parser.Threshold_domainContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#statement_text}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement_text(Db2Parser.Statement_textContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#executable_id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecutable_id(Db2Parser.Executable_idContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#enforcement_scope}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnforcement_scope(Db2Parser.Enforcement_scopeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#threshold_predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThreshold_predicate(Db2Parser.Threshold_predicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#checking_every}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChecking_every(Db2Parser.Checking_everyContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#hour_to_seconds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHour_to_seconds(Db2Parser.Hour_to_secondsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#day_to_minutes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDay_to_minutes(Db2Parser.Day_to_minutesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#day_to_seconds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDay_to_seconds(Db2Parser.Day_to_secondsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#threshold_exceeded_actions_2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThreshold_exceeded_actions_2(Db2Parser.Threshold_exceeded_actions_2Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#details_section}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDetails_section(Db2Parser.Details_sectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#remap_activity_action}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemap_activity_action(Db2Parser.Remap_activity_actionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_transform_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_transform_statement(Db2Parser.Create_transform_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#tranform_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTranform_list(Db2Parser.Tranform_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#tranform_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTranform_list_item(Db2Parser.Tranform_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#transform_group_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_group_list(Db2Parser.Transform_group_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#transform_group_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_group_list_item(Db2Parser.Transform_group_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_trigger_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_trigger_statement(Db2Parser.Create_trigger_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#ref_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRef_list(Db2Parser.Ref_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#ref_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRef_list_item(Db2Parser.Ref_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#old_new}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOld_new(Db2Parser.Old_newContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#correlation_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCorrelation_name(Db2Parser.Correlation_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(Db2Parser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#trigger_event}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrigger_event(Db2Parser.Trigger_eventContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#triggered_action}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggered_action(Db2Parser.Triggered_actionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_procedure_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_procedure_statement(Db2Parser.Sql_procedure_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_function_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_function_statement(Db2Parser.Sql_function_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_trusted_context_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_trusted_context_statement(Db2Parser.Create_trusted_context_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#attr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttr_list(Db2Parser.Attr_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#attr_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttr_list_item(Db2Parser.Attr_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#auth_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAuth_list(Db2Parser.Auth_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#auth_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAuth_list_item(Db2Parser.Auth_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#address_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddress_value(Db2Parser.Address_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#encryption_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEncryption_value(Db2Parser.Encryption_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_type_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_type_statement(Db2Parser.Create_type_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_type_array_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_type_array_statement(Db2Parser.Create_type_array_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_type_cursor_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_type_cursor_statement(Db2Parser.Create_type_cursor_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_type_distinct_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_type_distinct_statement(Db2Parser.Create_type_distinct_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_type_row_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_type_row_statement(Db2Parser.Create_type_row_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#field_definition_list_paren}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField_definition_list_paren(Db2Parser.Field_definition_list_parenContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#field_definition_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField_definition_list(Db2Parser.Field_definition_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#field_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField_definition(Db2Parser.Field_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_type_structured_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_type_structured_statement(Db2Parser.Create_type_structured_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#structured_type_seq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructured_type_seq(Db2Parser.Structured_type_seqContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#attribute_definition_list_paren}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttribute_definition_list_paren(Db2Parser.Attribute_definition_list_parenContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#attribute_definition_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttribute_definition_list(Db2Parser.Attribute_definition_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#attribute_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttribute_definition(Db2Parser.Attribute_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#method_specification_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_specification_list(Db2Parser.Method_specification_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#method_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_specification(Db2Parser.Method_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#method_specification_seq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_specification_seq(Db2Parser.Method_specification_seqContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#as_locator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAs_locator(Db2Parser.As_locatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#param_decl_list_paren}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_decl_list_paren(Db2Parser.Param_decl_list_parenContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#param_decl_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_decl_list(Db2Parser.Param_decl_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#param_decl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_decl(Db2Parser.Param_declContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_routine_characteristics}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_routine_characteristics(Db2Parser.Sql_routine_characteristicsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#external_routine_characteristics}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExternal_routine_characteristics(Db2Parser.External_routine_characteristicsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#length}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLength(Db2Parser.LengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#rep_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRep_type(Db2Parser.Rep_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#varchars}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarchars(Db2Parser.VarcharsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#varbinaries}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarbinaries(Db2Parser.VarbinariesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#for_bit_data}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_bit_data(Db2Parser.For_bit_dataContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#lob_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLob_options(Db2Parser.Lob_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_type_mapping_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_type_mapping_statement(Db2Parser.Create_type_mapping_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#for_bit_data_precision}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_bit_data_precision(Db2Parser.For_bit_data_precisionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#precision}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision(Db2Parser.PrecisionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#scale}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScale(Db2Parser.ScaleContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#precision_scale_comp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrecision_scale_comp(Db2Parser.Precision_scale_compContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#from_to}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrom_to(Db2Parser.From_toContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#data_source_data_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_source_data_type(Db2Parser.Data_source_data_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#local_data_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocal_data_type(Db2Parser.Local_data_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#remote_server}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemote_server(Db2Parser.Remote_serverContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#server_version}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitServer_version(Db2Parser.Server_versionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#server_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitServer_type(Db2Parser.Server_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#version}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVersion(Db2Parser.VersionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#release}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelease(Db2Parser.ReleaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#mod}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMod(Db2Parser.ModContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_usage_list_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_usage_list_statement(Db2Parser.Create_usage_list_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_user_mapping_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_user_mapping_statement(Db2Parser.Create_user_mapping_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#user_mapping_options_paren}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUser_mapping_options_paren(Db2Parser.User_mapping_options_parenContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#user_mapping_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUser_mapping_options(Db2Parser.User_mapping_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_variable_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_variable_statement(Db2Parser.Create_variable_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#constant_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant_(Db2Parser.Constant_Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#special_register}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecial_register(Db2Parser.Special_registerContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#global_variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGlobal_variable(Db2Parser.Global_variableContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#data_type_1}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_type_1(Db2Parser.Data_type_1Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#cursor_value_constructor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursor_value_constructor(Db2Parser.Cursor_value_constructorContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#anchored_variable_data_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnchored_variable_data_type(Db2Parser.Anchored_variable_data_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#holdability}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHoldability(Db2Parser.HoldabilityContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#returnability}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnability(Db2Parser.ReturnabilityContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_view_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_view_statement(Db2Parser.Create_view_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_view_seq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_view_seq(Db2Parser.Create_view_seqContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#fullselect}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFullselect(Db2Parser.FullselectContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#subselect}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubselect(Db2Parser.SubselectContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#select_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_clause(Db2Parser.Select_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#select_clause_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_clause_item(Db2Parser.Select_clause_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#from_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrom_clause(Db2Parser.From_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_reference(Db2Parser.Table_referenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_reference_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_reference_list(Db2Parser.Table_reference_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#singles_table_reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingles_table_reference(Db2Parser.Singles_table_referenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#period_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPeriod_specification(Db2Parser.Period_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(Db2Parser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#correlation_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCorrelation_clause(Db2Parser.Correlation_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#tablesample_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablesample_clause(Db2Parser.Tablesample_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#numeric_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumeric_expression(Db2Parser.Numeric_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#single_view_reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingle_view_reference(Db2Parser.Single_view_referenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#single_nickname_reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingle_nickname_reference(Db2Parser.Single_nickname_referenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#only_table_reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOnly_table_reference(Db2Parser.Only_table_referenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#outer_table_reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOuter_table_reference(Db2Parser.Outer_table_referenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#analyze_table_reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnalyze_table_reference(Db2Parser.Analyze_table_referenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#implementation_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImplementation_clause(Db2Parser.Implementation_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#nested_table_reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNested_table_reference(Db2Parser.Nested_table_referenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#continue_handler}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinue_handler(Db2Parser.Continue_handlerContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#specific_condition_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecific_condition_value(Db2Parser.Specific_condition_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#data_change_table_reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_change_table_reference(Db2Parser.Data_change_table_referenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#searched_update_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearched_update_statement(Db2Parser.Searched_update_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#searched_delete_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearched_delete_statement(Db2Parser.Searched_delete_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#final_new}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFinal_new(Db2Parser.Final_newContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#final_new_old}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFinal_new_old(Db2Parser.Final_new_oldContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_function_reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_function_reference(Db2Parser.Table_function_referenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_udf_cardinality_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_udf_cardinality_clause(Db2Parser.Table_udf_cardinality_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#typed_correlation_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTyped_correlation_clause(Db2Parser.Typed_correlation_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#column_name_data_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_name_data_type(Db2Parser.Column_name_data_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#collection_derived_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollection_derived_table(Db2Parser.Collection_derived_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_function(Db2Parser.Table_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#xmltable_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmltable_expression(Db2Parser.Xmltable_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#xmltable_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmltable_function(Db2Parser.Xmltable_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#joined_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoined_table(Db2Parser.Joined_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#join_condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin_condition(Db2Parser.Join_conditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#outer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOuter(Db2Parser.OuterContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#external_table_reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExternal_table_reference(Db2Parser.External_table_referenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#column_definition_2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_definition_2(Db2Parser.Column_definition_2Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#file_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFile_name(Db2Parser.File_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#where_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere_clause(Db2Parser.Where_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#group_by_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_by_clause(Db2Parser.Group_by_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#group_by_clause_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_by_clause_opts(Db2Parser.Group_by_clause_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grouping_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrouping_expression(Db2Parser.Grouping_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grouping_sets}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrouping_sets(Db2Parser.Grouping_setsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#super_groups}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSuper_groups(Db2Parser.Super_groupsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#grant_total}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_total(Db2Parser.Grant_totalContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#having_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHaving_clause(Db2Parser.Having_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#order_by_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrder_by_clause(Db2Parser.Order_by_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#order_by_clause_opts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrder_by_clause_opts(Db2Parser.Order_by_clause_optsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_designator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_designator(Db2Parser.Table_designatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#asc_desc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsc_desc(Db2Parser.Asc_descContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#first_last}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFirst_last(Db2Parser.First_lastContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sort_key}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSort_key(Db2Parser.Sort_keyContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#simple_column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_column_name(Db2Parser.Simple_column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#simple_integer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_integer(Db2Parser.Simple_integerContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sork_key_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSork_key_expression(Db2Parser.Sork_key_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#offset_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOffset_clause(Db2Parser.Offset_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#offset_row_count}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOffset_row_count(Db2Parser.Offset_row_countContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#fetch_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_clause(Db2Parser.Fetch_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#fetch_row_count}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_row_count(Db2Parser.Fetch_row_countContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#row_rows}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow_rows(Db2Parser.Row_rowsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#isolation_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsolation_clause(Db2Parser.Isolation_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#lock_request_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLock_request_clause(Db2Parser.Lock_request_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#values_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValues_clause(Db2Parser.Values_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#values_row}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValues_row(Db2Parser.Values_rowContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#root_view_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoot_view_definition(Db2Parser.Root_view_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#subview_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubview_definition(Db2Parser.Subview_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#oid_column}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOid_column(Db2Parser.Oid_columnContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#with_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_options(Db2Parser.With_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#with_option_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_option_def(Db2Parser.With_option_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#with_option_scope_def}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_option_scope_def(Db2Parser.With_option_scope_defContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#under_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnder_clause(Db2Parser.Under_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_work_action_set_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_work_action_set_statement(Db2Parser.Create_work_action_set_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#work_action_definition_list_paren}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWork_action_definition_list_paren(Db2Parser.Work_action_definition_list_parenContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#work_action_definition_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWork_action_definition_list(Db2Parser.Work_action_definition_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#work_action_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWork_action_definition(Db2Parser.Work_action_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#action_types_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAction_types_clause(Db2Parser.Action_types_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#threshold_types_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThreshold_types_clause(Db2Parser.Threshold_types_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#second_seconds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSecond_seconds(Db2Parser.Second_secondsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#hours_minutes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHours_minutes(Db2Parser.Hours_minutesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#threshold_exceeded_actions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThreshold_exceeded_actions(Db2Parser.Threshold_exceeded_actionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#collect_activity_data_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollect_activity_data_clause(Db2Parser.Collect_activity_data_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#with_without}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_without(Db2Parser.With_withoutContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#histogram_templace_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHistogram_templace_clause(Db2Parser.Histogram_templace_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_work_class_set_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_work_class_set_statement(Db2Parser.Create_work_class_set_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#work_class_definition_list_paren}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWork_class_definition_list_paren(Db2Parser.Work_class_definition_list_parenContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#work_class_definition_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWork_class_definition_list(Db2Parser.Work_class_definition_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#work_class_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWork_class_definition(Db2Parser.Work_class_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#work_attributes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWork_attributes(Db2Parser.Work_attributesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#position_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPosition_clause(Db2Parser.Position_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#position_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPosition_(Db2Parser.Position_Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#for_from_to_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_from_to_clause(Db2Parser.For_from_to_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#from_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrom_value(Db2Parser.From_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#to_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTo_value(Db2Parser.To_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#data_tag_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_tag_clause(Db2Parser.Data_tag_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#schema_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_clause(Db2Parser.Schema_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_workload_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_workload_statement(Db2Parser.Create_workload_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#pkg_exec_seq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPkg_exec_seq(Db2Parser.Pkg_exec_seqContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#position_clause_2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPosition_clause_2(Db2Parser.Position_clause_2Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#connection_attributes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConnection_attributes(Db2Parser.Connection_attributesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#string_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString_list(Db2Parser.String_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#string_list_paren}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString_list_paren(Db2Parser.String_list_parenContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#workload_attributes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorkload_attributes(Db2Parser.Workload_attributesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#degree}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDegree(Db2Parser.DegreeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#allow_disallow}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllow_disallow(Db2Parser.Allow_disallowContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#collect_on_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollect_on_clause(Db2Parser.Collect_on_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#collect_details_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollect_details_clause(Db2Parser.Collect_details_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#collect_lock_wait_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollect_lock_wait_options(Db2Parser.Collect_lock_wait_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#wait_time}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWait_time(Db2Parser.Wait_timeContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#create_wrapper_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_wrapper_statement(Db2Parser.Create_wrapper_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#wrapper_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWrapper_option_list(Db2Parser.Wrapper_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#wrapper_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWrapper_option(Db2Parser.Wrapper_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(Db2Parser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#function_invocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_invocation(Db2Parser.Function_invocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#all_distinct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAll_distinct(Db2Parser.All_distinctContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#scalar_fullselect}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScalar_fullselect(Db2Parser.Scalar_fullselectContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#cast_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCast_specification(Db2Parser.Cast_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#cursor_cast_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursor_cast_specification(Db2Parser.Cursor_cast_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#row_cast_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow_cast_specification(Db2Parser.Row_cast_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#interval_cast_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterval_cast_specification(Db2Parser.Interval_cast_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#xmlcast_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlcast_specification(Db2Parser.Xmlcast_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#array_element_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_element_specification(Db2Parser.Array_element_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#array_constructor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_constructor(Db2Parser.Array_constructorContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#method_invocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_invocation(Db2Parser.Method_invocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#olap_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOlap_specification(Db2Parser.Olap_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#ordered_olap_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrdered_olap_specification(Db2Parser.Ordered_olap_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#window_partition_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_partition_clause(Db2Parser.Window_partition_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#window_order_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_order_clause(Db2Parser.Window_order_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#numbering_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumbering_specification(Db2Parser.Numbering_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#aggregation_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregation_specification(Db2Parser.Aggregation_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#olap_aggregate_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOlap_aggregate_function(Db2Parser.Olap_aggregate_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#first_value_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFirst_value_function(Db2Parser.First_value_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#last_value_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLast_value_function(Db2Parser.Last_value_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#nth_value_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNth_value_function(Db2Parser.Nth_value_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#ratio_to_report_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRatio_to_report_function(Db2Parser.Ratio_to_report_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#ignore_respect_nulls}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIgnore_respect_nulls(Db2Parser.Ignore_respect_nullsContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#from_first_last}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrom_first_last(Db2Parser.From_first_lastContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#window_aggregation_group_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_aggregation_group_clause(Db2Parser.Window_aggregation_group_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#group_start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_start(Db2Parser.Group_startContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#group_between}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_between(Db2Parser.Group_betweenContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#group_bound1}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_bound1(Db2Parser.Group_bound1Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#group_bound2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_bound2(Db2Parser.Group_bound2Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#group_end}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_end(Db2Parser.Group_endContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#row_change_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow_change_expression(Db2Parser.Row_change_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sequence_reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequence_reference(Db2Parser.Sequence_referenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#subtype_treatment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubtype_treatment(Db2Parser.Subtype_treatmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_list(Db2Parser.Expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#expression_list_in_parentheses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_list_in_parentheses(Db2Parser.Expression_list_in_parenthesesContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#id_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId_(Db2Parser.Id_Context ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#exposed_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExposed_name(Db2Parser.Exposed_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(Db2Parser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#label}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabel(Db2Parser.LabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#host_label}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHost_label(Db2Parser.Host_labelContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#library_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLibrary_name(Db2Parser.Library_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#array_type_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_type_name(Db2Parser.Array_type_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#attribute_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttribute_name(Db2Parser.Attribute_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#row_type_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow_type_name(Db2Parser.Row_type_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#authorization_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAuthorization_name(Db2Parser.Authorization_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#boolean_variable_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolean_variable_name(Db2Parser.Boolean_variable_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#array_variable_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_variable_name(Db2Parser.Array_variable_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_name(Db2Parser.Column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#constraint_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraint_name(Db2Parser.Constraint_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#descriptor_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescriptor_name(Db2Parser.Descriptor_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#distinct_type_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistinct_type_name(Db2Parser.Distinct_type_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#cursor_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursor_name(Db2Parser.Cursor_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#cursor_type_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursor_type_name(Db2Parser.Cursor_type_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#condition_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondition_name(Db2Parser.Condition_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#data_source_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_source_name(Db2Parser.Data_source_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#expression_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_name(Db2Parser.Expression_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#group_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_name(Db2Parser.Group_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#policy_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPolicy_name(Db2Parser.Policy_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#bufferpool_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBufferpool_name(Db2Parser.Bufferpool_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#db_partition_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_partition_name(Db2Parser.Db_partition_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#database_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatabase_name(Db2Parser.Database_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#event_monitor_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvent_monitor_name(Db2Parser.Event_monitor_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#field_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField_name(Db2Parser.Field_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#for_loop_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_loop_name(Db2Parser.For_loop_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#function_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_name(Db2Parser.Function_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#function_mapping_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_mapping_name(Db2Parser.Function_mapping_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#global_variable_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGlobal_variable_name(Db2Parser.Global_variable_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#hierarchy_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHierarchy_name(Db2Parser.Hierarchy_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#host_variable_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHost_variable_name(Db2Parser.Host_variable_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#parameter_marker}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameter_marker(Db2Parser.Parameter_markerContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#template_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemplate_name(Db2Parser.Template_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#index_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_name(Db2Parser.Index_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#index_extension_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_extension_name(Db2Parser.Index_extension_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#input_descriptor_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInput_descriptor_name(Db2Parser.Input_descriptor_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#mask_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMask_name(Db2Parser.Mask_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#method_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethod_name(Db2Parser.Method_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#model_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModel_name(Db2Parser.Model_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#module_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModule_name(Db2Parser.Module_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#new_owner}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNew_owner(Db2Parser.New_ownerContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#nick_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNick_name(Db2Parser.Nick_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#object_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject_name(Db2Parser.Object_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#oid_column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOid_column_name(Db2Parser.Oid_column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#optimization_profile_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptimization_profile_name(Db2Parser.Optimization_profile_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#package_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackage_name(Db2Parser.Package_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#partition_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_name(Db2Parser.Partition_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#path_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath_name(Db2Parser.Path_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#permission_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPermission_name(Db2Parser.Permission_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#pipe_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPipe_name(Db2Parser.Pipe_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#procedure_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedure_name(Db2Parser.Procedure_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#result_descriptor_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResult_descriptor_name(Db2Parser.Result_descriptor_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#role_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRole_name(Db2Parser.Role_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#root_table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoot_table_name(Db2Parser.Root_table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#root_view_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoot_view_name(Db2Parser.Root_view_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#row_variable_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow_variable_name(Db2Parser.Row_variable_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#source_schema_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSource_schema_name(Db2Parser.Source_schema_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#source_package_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSource_package_name(Db2Parser.Source_package_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#source_procedure_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSource_procedure_name(Db2Parser.Source_procedure_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_parameter_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_parameter_name(Db2Parser.Sql_parameter_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sql_variable_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_variable_name(Db2Parser.Sql_variable_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#transition_variable_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransition_variable_name(Db2Parser.Transition_variable_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#savepoint_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSavepoint_name(Db2Parser.Savepoint_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#specific_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecific_name(Db2Parser.Specific_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#schema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema(Db2Parser.SchemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#schema_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_name(Db2Parser.Schema_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#search_method_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_method_name(Db2Parser.Search_method_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#server_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitServer_name(Db2Parser.Server_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#server_option_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitServer_option_name(Db2Parser.Server_option_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#session_authorization_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSession_authorization_name(Db2Parser.Session_authorization_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#component_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComponent_name(Db2Parser.Component_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sec_label_comp_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSec_label_comp_name(Db2Parser.Sec_label_comp_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#security_policy_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSecurity_policy_name(Db2Parser.Security_policy_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#security_label_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSecurity_label_name(Db2Parser.Security_label_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#sequence_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequence_name(Db2Parser.Sequence_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#service_class_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitService_class_name(Db2Parser.Service_class_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#service_superclass_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitService_superclass_name(Db2Parser.Service_superclass_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#storagegroup_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStoragegroup_name(Db2Parser.Storagegroup_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#supertype_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSupertype_name(Db2Parser.Supertype_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#superview_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSuperview_name(Db2Parser.Superview_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#service_subclass_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitService_subclass_name(Db2Parser.Service_subclass_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#statement_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement_name(Db2Parser.Statement_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_name(Db2Parser.Table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#tablespace_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablespace_name(Db2Parser.Tablespace_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#target_identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_identifier(Db2Parser.Target_identifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#threshold_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThreshold_name(Db2Parser.Threshold_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#trigger_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrigger_name(Db2Parser.Trigger_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#context_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContext_name(Db2Parser.Context_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#usage_list_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUsage_list_name(Db2Parser.Usage_list_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#type_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_name(Db2Parser.Type_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#type_mapping_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_mapping_name(Db2Parser.Type_mapping_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#typed_table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTyped_table_name(Db2Parser.Typed_table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#typed_view_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTyped_view_name(Db2Parser.Typed_view_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#user_mapping_option_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUser_mapping_option_name(Db2Parser.User_mapping_option_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#view_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitView_name(Db2Parser.View_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#variable_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable_name(Db2Parser.Variable_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#work_action_set_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWork_action_set_name(Db2Parser.Work_action_set_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#work_class_set_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWork_class_set_name(Db2Parser.Work_class_set_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#workload_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorkload_name(Db2Parser.Workload_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#work_action_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWork_action_name(Db2Parser.Work_action_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#work_class_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWork_class_name(Db2Parser.Work_class_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#wrapper_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWrapper_name(Db2Parser.Wrapper_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#wrapper_option_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWrapper_option_name(Db2Parser.Wrapper_option_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#xsrobject_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXsrobject_name(Db2Parser.Xsrobject_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#parameter_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameter_name(Db2Parser.Parameter_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#cursor_variable_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursor_variable_name(Db2Parser.Cursor_variable_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#alias_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias_name(Db2Parser.Alias_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#db_partition_group_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDb_partition_group_name(Db2Parser.Db_partition_group_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#source_index_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSource_index_name(Db2Parser.Source_index_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#source_table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSource_table_name(Db2Parser.Source_table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#source_storagegroup_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSource_storagegroup_name(Db2Parser.Source_storagegroup_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#target_storagegroup_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_storagegroup_name(Db2Parser.Target_storagegroup_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#source_tablespace_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSource_tablespace_name(Db2Parser.Source_tablespace_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#target_tablespace_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_tablespace_name(Db2Parser.Target_tablespace_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#unqualified_function_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnqualified_function_name(Db2Parser.Unqualified_function_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#unqualified_procedure_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnqualified_procedure_name(Db2Parser.Unqualified_procedure_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#unqualified_specific_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnqualified_specific_name(Db2Parser.Unqualified_specific_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#period_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPeriod_name(Db2Parser.Period_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#history_table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHistory_table_name(Db2Parser.History_table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#xml_schema_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXml_schema_name(Db2Parser.Xml_schema_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link Db2Parser#todo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTodo(Db2Parser.TodoContext ctx);
}