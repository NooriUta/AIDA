// Generated from C:/AIDA/Dali4/HOUND/Hound/src/main/resources/grammars/sql/sqlite/SQLiteParser.g4 by ANTLR 4.13.1
package com.hound.parser.base.grammars.sql.sqlite;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SQLiteParser}.
 */
public interface SQLiteParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#parse}.
	 * @param ctx the parse tree
	 */
	void enterParse(SQLiteParser.ParseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#parse}.
	 * @param ctx the parse tree
	 */
	void exitParse(SQLiteParser.ParseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#sql_stmt_list}.
	 * @param ctx the parse tree
	 */
	void enterSql_stmt_list(SQLiteParser.Sql_stmt_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#sql_stmt_list}.
	 * @param ctx the parse tree
	 */
	void exitSql_stmt_list(SQLiteParser.Sql_stmt_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#sql_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSql_stmt(SQLiteParser.Sql_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#sql_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSql_stmt(SQLiteParser.Sql_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#alter_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_stmt(SQLiteParser.Alter_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#alter_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_stmt(SQLiteParser.Alter_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#analyze_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAnalyze_stmt(SQLiteParser.Analyze_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#analyze_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAnalyze_stmt(SQLiteParser.Analyze_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#attach_stmt}.
	 * @param ctx the parse tree
	 */
	void enterAttach_stmt(SQLiteParser.Attach_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#attach_stmt}.
	 * @param ctx the parse tree
	 */
	void exitAttach_stmt(SQLiteParser.Attach_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#begin_stmt}.
	 * @param ctx the parse tree
	 */
	void enterBegin_stmt(SQLiteParser.Begin_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#begin_stmt}.
	 * @param ctx the parse tree
	 */
	void exitBegin_stmt(SQLiteParser.Begin_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#commit_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCommit_stmt(SQLiteParser.Commit_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#commit_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCommit_stmt(SQLiteParser.Commit_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#rollback_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRollback_stmt(SQLiteParser.Rollback_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#rollback_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRollback_stmt(SQLiteParser.Rollback_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#savepoint_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSavepoint_stmt(SQLiteParser.Savepoint_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#savepoint_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSavepoint_stmt(SQLiteParser.Savepoint_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#release_stmt}.
	 * @param ctx the parse tree
	 */
	void enterRelease_stmt(SQLiteParser.Release_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#release_stmt}.
	 * @param ctx the parse tree
	 */
	void exitRelease_stmt(SQLiteParser.Release_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#create_index_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_index_stmt(SQLiteParser.Create_index_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#create_index_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_index_stmt(SQLiteParser.Create_index_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#indexed_column}.
	 * @param ctx the parse tree
	 */
	void enterIndexed_column(SQLiteParser.Indexed_columnContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#indexed_column}.
	 * @param ctx the parse tree
	 */
	void exitIndexed_column(SQLiteParser.Indexed_columnContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#create_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_stmt(SQLiteParser.Create_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#create_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_stmt(SQLiteParser.Create_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#table_options}.
	 * @param ctx the parse tree
	 */
	void enterTable_options(SQLiteParser.Table_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#table_options}.
	 * @param ctx the parse tree
	 */
	void exitTable_options(SQLiteParser.Table_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#column_def}.
	 * @param ctx the parse tree
	 */
	void enterColumn_def(SQLiteParser.Column_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#column_def}.
	 * @param ctx the parse tree
	 */
	void exitColumn_def(SQLiteParser.Column_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#type_name}.
	 * @param ctx the parse tree
	 */
	void enterType_name(SQLiteParser.Type_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#type_name}.
	 * @param ctx the parse tree
	 */
	void exitType_name(SQLiteParser.Type_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#column_constraint}.
	 * @param ctx the parse tree
	 */
	void enterColumn_constraint(SQLiteParser.Column_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#column_constraint}.
	 * @param ctx the parse tree
	 */
	void exitColumn_constraint(SQLiteParser.Column_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#signed_number}.
	 * @param ctx the parse tree
	 */
	void enterSigned_number(SQLiteParser.Signed_numberContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#signed_number}.
	 * @param ctx the parse tree
	 */
	void exitSigned_number(SQLiteParser.Signed_numberContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#table_constraint}.
	 * @param ctx the parse tree
	 */
	void enterTable_constraint(SQLiteParser.Table_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#table_constraint}.
	 * @param ctx the parse tree
	 */
	void exitTable_constraint(SQLiteParser.Table_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#foreign_key_clause}.
	 * @param ctx the parse tree
	 */
	void enterForeign_key_clause(SQLiteParser.Foreign_key_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#foreign_key_clause}.
	 * @param ctx the parse tree
	 */
	void exitForeign_key_clause(SQLiteParser.Foreign_key_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#conflict_clause}.
	 * @param ctx the parse tree
	 */
	void enterConflict_clause(SQLiteParser.Conflict_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#conflict_clause}.
	 * @param ctx the parse tree
	 */
	void exitConflict_clause(SQLiteParser.Conflict_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#create_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_trigger_stmt(SQLiteParser.Create_trigger_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#create_trigger_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_trigger_stmt(SQLiteParser.Create_trigger_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#create_view_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_view_stmt(SQLiteParser.Create_view_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#create_view_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_view_stmt(SQLiteParser.Create_view_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#create_virtual_table_stmt}.
	 * @param ctx the parse tree
	 */
	void enterCreate_virtual_table_stmt(SQLiteParser.Create_virtual_table_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#create_virtual_table_stmt}.
	 * @param ctx the parse tree
	 */
	void exitCreate_virtual_table_stmt(SQLiteParser.Create_virtual_table_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#with_clause}.
	 * @param ctx the parse tree
	 */
	void enterWith_clause(SQLiteParser.With_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#with_clause}.
	 * @param ctx the parse tree
	 */
	void exitWith_clause(SQLiteParser.With_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#common_table_expression}.
	 * @param ctx the parse tree
	 */
	void enterCommon_table_expression(SQLiteParser.Common_table_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#common_table_expression}.
	 * @param ctx the parse tree
	 */
	void exitCommon_table_expression(SQLiteParser.Common_table_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#cte_table_name}.
	 * @param ctx the parse tree
	 */
	void enterCte_table_name(SQLiteParser.Cte_table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#cte_table_name}.
	 * @param ctx the parse tree
	 */
	void exitCte_table_name(SQLiteParser.Cte_table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#delete_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDelete_stmt(SQLiteParser.Delete_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#delete_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDelete_stmt(SQLiteParser.Delete_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#detach_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDetach_stmt(SQLiteParser.Detach_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#detach_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDetach_stmt(SQLiteParser.Detach_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#drop_stmt}.
	 * @param ctx the parse tree
	 */
	void enterDrop_stmt(SQLiteParser.Drop_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#drop_stmt}.
	 * @param ctx the parse tree
	 */
	void exitDrop_stmt(SQLiteParser.Drop_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(SQLiteParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(SQLiteParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#expr_or}.
	 * @param ctx the parse tree
	 */
	void enterExpr_or(SQLiteParser.Expr_orContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#expr_or}.
	 * @param ctx the parse tree
	 */
	void exitExpr_or(SQLiteParser.Expr_orContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#expr_and}.
	 * @param ctx the parse tree
	 */
	void enterExpr_and(SQLiteParser.Expr_andContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#expr_and}.
	 * @param ctx the parse tree
	 */
	void exitExpr_and(SQLiteParser.Expr_andContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#expr_not}.
	 * @param ctx the parse tree
	 */
	void enterExpr_not(SQLiteParser.Expr_notContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#expr_not}.
	 * @param ctx the parse tree
	 */
	void exitExpr_not(SQLiteParser.Expr_notContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#expr_binary}.
	 * @param ctx the parse tree
	 */
	void enterExpr_binary(SQLiteParser.Expr_binaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#expr_binary}.
	 * @param ctx the parse tree
	 */
	void exitExpr_binary(SQLiteParser.Expr_binaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#expr_comparison}.
	 * @param ctx the parse tree
	 */
	void enterExpr_comparison(SQLiteParser.Expr_comparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#expr_comparison}.
	 * @param ctx the parse tree
	 */
	void exitExpr_comparison(SQLiteParser.Expr_comparisonContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#expr_bitwise}.
	 * @param ctx the parse tree
	 */
	void enterExpr_bitwise(SQLiteParser.Expr_bitwiseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#expr_bitwise}.
	 * @param ctx the parse tree
	 */
	void exitExpr_bitwise(SQLiteParser.Expr_bitwiseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#expr_addition}.
	 * @param ctx the parse tree
	 */
	void enterExpr_addition(SQLiteParser.Expr_additionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#expr_addition}.
	 * @param ctx the parse tree
	 */
	void exitExpr_addition(SQLiteParser.Expr_additionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#expr_multiplication}.
	 * @param ctx the parse tree
	 */
	void enterExpr_multiplication(SQLiteParser.Expr_multiplicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#expr_multiplication}.
	 * @param ctx the parse tree
	 */
	void exitExpr_multiplication(SQLiteParser.Expr_multiplicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#expr_string}.
	 * @param ctx the parse tree
	 */
	void enterExpr_string(SQLiteParser.Expr_stringContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#expr_string}.
	 * @param ctx the parse tree
	 */
	void exitExpr_string(SQLiteParser.Expr_stringContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#expr_collate}.
	 * @param ctx the parse tree
	 */
	void enterExpr_collate(SQLiteParser.Expr_collateContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#expr_collate}.
	 * @param ctx the parse tree
	 */
	void exitExpr_collate(SQLiteParser.Expr_collateContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#expr_unary}.
	 * @param ctx the parse tree
	 */
	void enterExpr_unary(SQLiteParser.Expr_unaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#expr_unary}.
	 * @param ctx the parse tree
	 */
	void exitExpr_unary(SQLiteParser.Expr_unaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#expr_base}.
	 * @param ctx the parse tree
	 */
	void enterExpr_base(SQLiteParser.Expr_baseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#expr_base}.
	 * @param ctx the parse tree
	 */
	void exitExpr_base(SQLiteParser.Expr_baseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#expr_recursive}.
	 * @param ctx the parse tree
	 */
	void enterExpr_recursive(SQLiteParser.Expr_recursiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#expr_recursive}.
	 * @param ctx the parse tree
	 */
	void exitExpr_recursive(SQLiteParser.Expr_recursiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#raise_function}.
	 * @param ctx the parse tree
	 */
	void enterRaise_function(SQLiteParser.Raise_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#raise_function}.
	 * @param ctx the parse tree
	 */
	void exitRaise_function(SQLiteParser.Raise_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#literal_value}.
	 * @param ctx the parse tree
	 */
	void enterLiteral_value(SQLiteParser.Literal_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#literal_value}.
	 * @param ctx the parse tree
	 */
	void exitLiteral_value(SQLiteParser.Literal_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#percentile_clause}.
	 * @param ctx the parse tree
	 */
	void enterPercentile_clause(SQLiteParser.Percentile_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#percentile_clause}.
	 * @param ctx the parse tree
	 */
	void exitPercentile_clause(SQLiteParser.Percentile_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#value_row}.
	 * @param ctx the parse tree
	 */
	void enterValue_row(SQLiteParser.Value_rowContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#value_row}.
	 * @param ctx the parse tree
	 */
	void exitValue_row(SQLiteParser.Value_rowContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#values_clause}.
	 * @param ctx the parse tree
	 */
	void enterValues_clause(SQLiteParser.Values_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#values_clause}.
	 * @param ctx the parse tree
	 */
	void exitValues_clause(SQLiteParser.Values_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#insert_stmt}.
	 * @param ctx the parse tree
	 */
	void enterInsert_stmt(SQLiteParser.Insert_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#insert_stmt}.
	 * @param ctx the parse tree
	 */
	void exitInsert_stmt(SQLiteParser.Insert_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#returning_clause}.
	 * @param ctx the parse tree
	 */
	void enterReturning_clause(SQLiteParser.Returning_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#returning_clause}.
	 * @param ctx the parse tree
	 */
	void exitReturning_clause(SQLiteParser.Returning_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#upsert_clause}.
	 * @param ctx the parse tree
	 */
	void enterUpsert_clause(SQLiteParser.Upsert_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#upsert_clause}.
	 * @param ctx the parse tree
	 */
	void exitUpsert_clause(SQLiteParser.Upsert_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#pragma_stmt}.
	 * @param ctx the parse tree
	 */
	void enterPragma_stmt(SQLiteParser.Pragma_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#pragma_stmt}.
	 * @param ctx the parse tree
	 */
	void exitPragma_stmt(SQLiteParser.Pragma_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#pragma_value}.
	 * @param ctx the parse tree
	 */
	void enterPragma_value(SQLiteParser.Pragma_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#pragma_value}.
	 * @param ctx the parse tree
	 */
	void exitPragma_value(SQLiteParser.Pragma_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#reindex_stmt}.
	 * @param ctx the parse tree
	 */
	void enterReindex_stmt(SQLiteParser.Reindex_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#reindex_stmt}.
	 * @param ctx the parse tree
	 */
	void exitReindex_stmt(SQLiteParser.Reindex_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#select_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSelect_stmt(SQLiteParser.Select_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#select_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSelect_stmt(SQLiteParser.Select_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#join_clause}.
	 * @param ctx the parse tree
	 */
	void enterJoin_clause(SQLiteParser.Join_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#join_clause}.
	 * @param ctx the parse tree
	 */
	void exitJoin_clause(SQLiteParser.Join_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#select_core}.
	 * @param ctx the parse tree
	 */
	void enterSelect_core(SQLiteParser.Select_coreContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#select_core}.
	 * @param ctx the parse tree
	 */
	void exitSelect_core(SQLiteParser.Select_coreContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#table_or_subquery}.
	 * @param ctx the parse tree
	 */
	void enterTable_or_subquery(SQLiteParser.Table_or_subqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#table_or_subquery}.
	 * @param ctx the parse tree
	 */
	void exitTable_or_subquery(SQLiteParser.Table_or_subqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#result_column}.
	 * @param ctx the parse tree
	 */
	void enterResult_column(SQLiteParser.Result_columnContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#result_column}.
	 * @param ctx the parse tree
	 */
	void exitResult_column(SQLiteParser.Result_columnContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#join_operator}.
	 * @param ctx the parse tree
	 */
	void enterJoin_operator(SQLiteParser.Join_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#join_operator}.
	 * @param ctx the parse tree
	 */
	void exitJoin_operator(SQLiteParser.Join_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#join_constraint}.
	 * @param ctx the parse tree
	 */
	void enterJoin_constraint(SQLiteParser.Join_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#join_constraint}.
	 * @param ctx the parse tree
	 */
	void exitJoin_constraint(SQLiteParser.Join_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#compound_operator}.
	 * @param ctx the parse tree
	 */
	void enterCompound_operator(SQLiteParser.Compound_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#compound_operator}.
	 * @param ctx the parse tree
	 */
	void exitCompound_operator(SQLiteParser.Compound_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#update_stmt}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_stmt(SQLiteParser.Update_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#update_stmt}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_stmt(SQLiteParser.Update_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#column_name_list}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name_list(SQLiteParser.Column_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#column_name_list}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name_list(SQLiteParser.Column_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#qualified_table_name}.
	 * @param ctx the parse tree
	 */
	void enterQualified_table_name(SQLiteParser.Qualified_table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#qualified_table_name}.
	 * @param ctx the parse tree
	 */
	void exitQualified_table_name(SQLiteParser.Qualified_table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#vacuum_stmt}.
	 * @param ctx the parse tree
	 */
	void enterVacuum_stmt(SQLiteParser.Vacuum_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#vacuum_stmt}.
	 * @param ctx the parse tree
	 */
	void exitVacuum_stmt(SQLiteParser.Vacuum_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#filter_clause}.
	 * @param ctx the parse tree
	 */
	void enterFilter_clause(SQLiteParser.Filter_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#filter_clause}.
	 * @param ctx the parse tree
	 */
	void exitFilter_clause(SQLiteParser.Filter_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#window_defn}.
	 * @param ctx the parse tree
	 */
	void enterWindow_defn(SQLiteParser.Window_defnContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#window_defn}.
	 * @param ctx the parse tree
	 */
	void exitWindow_defn(SQLiteParser.Window_defnContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#over_clause}.
	 * @param ctx the parse tree
	 */
	void enterOver_clause(SQLiteParser.Over_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#over_clause}.
	 * @param ctx the parse tree
	 */
	void exitOver_clause(SQLiteParser.Over_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#frame_spec}.
	 * @param ctx the parse tree
	 */
	void enterFrame_spec(SQLiteParser.Frame_specContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#frame_spec}.
	 * @param ctx the parse tree
	 */
	void exitFrame_spec(SQLiteParser.Frame_specContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#frame_clause}.
	 * @param ctx the parse tree
	 */
	void enterFrame_clause(SQLiteParser.Frame_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#frame_clause}.
	 * @param ctx the parse tree
	 */
	void exitFrame_clause(SQLiteParser.Frame_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#order_clause}.
	 * @param ctx the parse tree
	 */
	void enterOrder_clause(SQLiteParser.Order_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#order_clause}.
	 * @param ctx the parse tree
	 */
	void exitOrder_clause(SQLiteParser.Order_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#limit_clause}.
	 * @param ctx the parse tree
	 */
	void enterLimit_clause(SQLiteParser.Limit_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#limit_clause}.
	 * @param ctx the parse tree
	 */
	void exitLimit_clause(SQLiteParser.Limit_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#ordering_term}.
	 * @param ctx the parse tree
	 */
	void enterOrdering_term(SQLiteParser.Ordering_termContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#ordering_term}.
	 * @param ctx the parse tree
	 */
	void exitOrdering_term(SQLiteParser.Ordering_termContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#asc_desc}.
	 * @param ctx the parse tree
	 */
	void enterAsc_desc(SQLiteParser.Asc_descContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#asc_desc}.
	 * @param ctx the parse tree
	 */
	void exitAsc_desc(SQLiteParser.Asc_descContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#frame_left}.
	 * @param ctx the parse tree
	 */
	void enterFrame_left(SQLiteParser.Frame_leftContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#frame_left}.
	 * @param ctx the parse tree
	 */
	void exitFrame_left(SQLiteParser.Frame_leftContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#frame_right}.
	 * @param ctx the parse tree
	 */
	void enterFrame_right(SQLiteParser.Frame_rightContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#frame_right}.
	 * @param ctx the parse tree
	 */
	void exitFrame_right(SQLiteParser.Frame_rightContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#frame_single}.
	 * @param ctx the parse tree
	 */
	void enterFrame_single(SQLiteParser.Frame_singleContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#frame_single}.
	 * @param ctx the parse tree
	 */
	void exitFrame_single(SQLiteParser.Frame_singleContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#error_message}.
	 * @param ctx the parse tree
	 */
	void enterError_message(SQLiteParser.Error_messageContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#error_message}.
	 * @param ctx the parse tree
	 */
	void exitError_message(SQLiteParser.Error_messageContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#filename}.
	 * @param ctx the parse tree
	 */
	void enterFilename(SQLiteParser.FilenameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#filename}.
	 * @param ctx the parse tree
	 */
	void exitFilename(SQLiteParser.FilenameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#module_argument}.
	 * @param ctx the parse tree
	 */
	void enterModule_argument(SQLiteParser.Module_argumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#module_argument}.
	 * @param ctx the parse tree
	 */
	void exitModule_argument(SQLiteParser.Module_argumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#module_argument_outer}.
	 * @param ctx the parse tree
	 */
	void enterModule_argument_outer(SQLiteParser.Module_argument_outerContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#module_argument_outer}.
	 * @param ctx the parse tree
	 */
	void exitModule_argument_outer(SQLiteParser.Module_argument_outerContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#module_argument_inner}.
	 * @param ctx the parse tree
	 */
	void enterModule_argument_inner(SQLiteParser.Module_argument_innerContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#module_argument_inner}.
	 * @param ctx the parse tree
	 */
	void exitModule_argument_inner(SQLiteParser.Module_argument_innerContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#fallback_excluding_conflicts}.
	 * @param ctx the parse tree
	 */
	void enterFallback_excluding_conflicts(SQLiteParser.Fallback_excluding_conflictsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#fallback_excluding_conflicts}.
	 * @param ctx the parse tree
	 */
	void exitFallback_excluding_conflicts(SQLiteParser.Fallback_excluding_conflictsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#join_keyword}.
	 * @param ctx the parse tree
	 */
	void enterJoin_keyword(SQLiteParser.Join_keywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#join_keyword}.
	 * @param ctx the parse tree
	 */
	void exitJoin_keyword(SQLiteParser.Join_keywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#fallback}.
	 * @param ctx the parse tree
	 */
	void enterFallback(SQLiteParser.FallbackContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#fallback}.
	 * @param ctx the parse tree
	 */
	void exitFallback(SQLiteParser.FallbackContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(SQLiteParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(SQLiteParser.NameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#function_name}.
	 * @param ctx the parse tree
	 */
	void enterFunction_name(SQLiteParser.Function_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#function_name}.
	 * @param ctx the parse tree
	 */
	void exitFunction_name(SQLiteParser.Function_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#schema_name}.
	 * @param ctx the parse tree
	 */
	void enterSchema_name(SQLiteParser.Schema_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#schema_name}.
	 * @param ctx the parse tree
	 */
	void exitSchema_name(SQLiteParser.Schema_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#table_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_name(SQLiteParser.Table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#table_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_name(SQLiteParser.Table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#table_or_index_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_or_index_name(SQLiteParser.Table_or_index_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#table_or_index_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_or_index_name(SQLiteParser.Table_or_index_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#column_name}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name(SQLiteParser.Column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#column_name}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name(SQLiteParser.Column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#column_name_excluding_string}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name_excluding_string(SQLiteParser.Column_name_excluding_stringContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#column_name_excluding_string}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name_excluding_string(SQLiteParser.Column_name_excluding_stringContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#column_alias}.
	 * @param ctx the parse tree
	 */
	void enterColumn_alias(SQLiteParser.Column_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#column_alias}.
	 * @param ctx the parse tree
	 */
	void exitColumn_alias(SQLiteParser.Column_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#collation_name}.
	 * @param ctx the parse tree
	 */
	void enterCollation_name(SQLiteParser.Collation_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#collation_name}.
	 * @param ctx the parse tree
	 */
	void exitCollation_name(SQLiteParser.Collation_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#foreign_table}.
	 * @param ctx the parse tree
	 */
	void enterForeign_table(SQLiteParser.Foreign_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#foreign_table}.
	 * @param ctx the parse tree
	 */
	void exitForeign_table(SQLiteParser.Foreign_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#index_name}.
	 * @param ctx the parse tree
	 */
	void enterIndex_name(SQLiteParser.Index_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#index_name}.
	 * @param ctx the parse tree
	 */
	void exitIndex_name(SQLiteParser.Index_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#trigger_name}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_name(SQLiteParser.Trigger_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#trigger_name}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_name(SQLiteParser.Trigger_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#view_name}.
	 * @param ctx the parse tree
	 */
	void enterView_name(SQLiteParser.View_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#view_name}.
	 * @param ctx the parse tree
	 */
	void exitView_name(SQLiteParser.View_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#module_name}.
	 * @param ctx the parse tree
	 */
	void enterModule_name(SQLiteParser.Module_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#module_name}.
	 * @param ctx the parse tree
	 */
	void exitModule_name(SQLiteParser.Module_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#pragma_name}.
	 * @param ctx the parse tree
	 */
	void enterPragma_name(SQLiteParser.Pragma_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#pragma_name}.
	 * @param ctx the parse tree
	 */
	void exitPragma_name(SQLiteParser.Pragma_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#savepoint_name}.
	 * @param ctx the parse tree
	 */
	void enterSavepoint_name(SQLiteParser.Savepoint_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#savepoint_name}.
	 * @param ctx the parse tree
	 */
	void exitSavepoint_name(SQLiteParser.Savepoint_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#table_alias}.
	 * @param ctx the parse tree
	 */
	void enterTable_alias(SQLiteParser.Table_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#table_alias}.
	 * @param ctx the parse tree
	 */
	void exitTable_alias(SQLiteParser.Table_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#table_alias_excluding_joins}.
	 * @param ctx the parse tree
	 */
	void enterTable_alias_excluding_joins(SQLiteParser.Table_alias_excluding_joinsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#table_alias_excluding_joins}.
	 * @param ctx the parse tree
	 */
	void exitTable_alias_excluding_joins(SQLiteParser.Table_alias_excluding_joinsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#window_name}.
	 * @param ctx the parse tree
	 */
	void enterWindow_name(SQLiteParser.Window_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#window_name}.
	 * @param ctx the parse tree
	 */
	void exitWindow_name(SQLiteParser.Window_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(SQLiteParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(SQLiteParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#base_window_name}.
	 * @param ctx the parse tree
	 */
	void enterBase_window_name(SQLiteParser.Base_window_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#base_window_name}.
	 * @param ctx the parse tree
	 */
	void exitBase_window_name(SQLiteParser.Base_window_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#table_function_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_function_name(SQLiteParser.Table_function_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#table_function_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_function_name(SQLiteParser.Table_function_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#any_name_excluding_raise}.
	 * @param ctx the parse tree
	 */
	void enterAny_name_excluding_raise(SQLiteParser.Any_name_excluding_raiseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#any_name_excluding_raise}.
	 * @param ctx the parse tree
	 */
	void exitAny_name_excluding_raise(SQLiteParser.Any_name_excluding_raiseContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#any_name_excluding_joins}.
	 * @param ctx the parse tree
	 */
	void enterAny_name_excluding_joins(SQLiteParser.Any_name_excluding_joinsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#any_name_excluding_joins}.
	 * @param ctx the parse tree
	 */
	void exitAny_name_excluding_joins(SQLiteParser.Any_name_excluding_joinsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#any_name_excluding_string}.
	 * @param ctx the parse tree
	 */
	void enterAny_name_excluding_string(SQLiteParser.Any_name_excluding_stringContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#any_name_excluding_string}.
	 * @param ctx the parse tree
	 */
	void exitAny_name_excluding_string(SQLiteParser.Any_name_excluding_stringContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLiteParser#any_name}.
	 * @param ctx the parse tree
	 */
	void enterAny_name(SQLiteParser.Any_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLiteParser#any_name}.
	 * @param ctx the parse tree
	 */
	void exitAny_name(SQLiteParser.Any_nameContext ctx);
}