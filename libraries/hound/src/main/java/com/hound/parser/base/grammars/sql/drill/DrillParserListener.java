// Generated from C:/AIDA/aida-root/libraries/hound/src/main/resources/grammars/sql/drill/DrillParser.g4 by ANTLR 4.13.2
package com.hound.parser.base.grammars.sql.drill;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DrillParser}.
 */
public interface DrillParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DrillParser#drill_file}.
	 * @param ctx the parse tree
	 */
	void enterDrill_file(DrillParser.Drill_fileContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#drill_file}.
	 * @param ctx the parse tree
	 */
	void exitDrill_file(DrillParser.Drill_fileContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#batch}.
	 * @param ctx the parse tree
	 */
	void enterBatch(DrillParser.BatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#batch}.
	 * @param ctx the parse tree
	 */
	void exitBatch(DrillParser.BatchContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#sql_command}.
	 * @param ctx the parse tree
	 */
	void enterSql_command(DrillParser.Sql_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#sql_command}.
	 * @param ctx the parse tree
	 */
	void exitSql_command(DrillParser.Sql_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#ddl_command}.
	 * @param ctx the parse tree
	 */
	void enterDdl_command(DrillParser.Ddl_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#ddl_command}.
	 * @param ctx the parse tree
	 */
	void exitDdl_command(DrillParser.Ddl_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#create_command}.
	 * @param ctx the parse tree
	 */
	void enterCreate_command(DrillParser.Create_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#create_command}.
	 * @param ctx the parse tree
	 */
	void exitCreate_command(DrillParser.Create_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#create_schema}.
	 * @param ctx the parse tree
	 */
	void enterCreate_schema(DrillParser.Create_schemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#create_schema}.
	 * @param ctx the parse tree
	 */
	void exitCreate_schema(DrillParser.Create_schemaContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#column_definition}.
	 * @param ctx the parse tree
	 */
	void enterColumn_definition(DrillParser.Column_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#column_definition}.
	 * @param ctx the parse tree
	 */
	void exitColumn_definition(DrillParser.Column_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#kv_list}.
	 * @param ctx the parse tree
	 */
	void enterKv_list(DrillParser.Kv_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#kv_list}.
	 * @param ctx the parse tree
	 */
	void exitKv_list(DrillParser.Kv_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#kv_pair}.
	 * @param ctx the parse tree
	 */
	void enterKv_pair(DrillParser.Kv_pairContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#kv_pair}.
	 * @param ctx the parse tree
	 */
	void exitKv_pair(DrillParser.Kv_pairContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#create_table}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table(DrillParser.Create_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#create_table}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table(DrillParser.Create_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#column_list_paren}.
	 * @param ctx the parse tree
	 */
	void enterColumn_list_paren(DrillParser.Column_list_parenContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#column_list_paren}.
	 * @param ctx the parse tree
	 */
	void exitColumn_list_paren(DrillParser.Column_list_parenContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#column_list}.
	 * @param ctx the parse tree
	 */
	void enterColumn_list(DrillParser.Column_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#column_list}.
	 * @param ctx the parse tree
	 */
	void exitColumn_list(DrillParser.Column_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#create_temp_table}.
	 * @param ctx the parse tree
	 */
	void enterCreate_temp_table(DrillParser.Create_temp_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#create_temp_table}.
	 * @param ctx the parse tree
	 */
	void exitCreate_temp_table(DrillParser.Create_temp_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#partition_by_clause}.
	 * @param ctx the parse tree
	 */
	void enterPartition_by_clause(DrillParser.Partition_by_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#partition_by_clause}.
	 * @param ctx the parse tree
	 */
	void exitPartition_by_clause(DrillParser.Partition_by_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#create_function}.
	 * @param ctx the parse tree
	 */
	void enterCreate_function(DrillParser.Create_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#create_function}.
	 * @param ctx the parse tree
	 */
	void exitCreate_function(DrillParser.Create_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#create_view}.
	 * @param ctx the parse tree
	 */
	void enterCreate_view(DrillParser.Create_viewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#create_view}.
	 * @param ctx the parse tree
	 */
	void exitCreate_view(DrillParser.Create_viewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#alter_command}.
	 * @param ctx the parse tree
	 */
	void enterAlter_command(DrillParser.Alter_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#alter_command}.
	 * @param ctx the parse tree
	 */
	void exitAlter_command(DrillParser.Alter_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#alter_system}.
	 * @param ctx the parse tree
	 */
	void enterAlter_system(DrillParser.Alter_systemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#alter_system}.
	 * @param ctx the parse tree
	 */
	void exitAlter_system(DrillParser.Alter_systemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#option_name}.
	 * @param ctx the parse tree
	 */
	void enterOption_name(DrillParser.Option_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#option_name}.
	 * @param ctx the parse tree
	 */
	void exitOption_name(DrillParser.Option_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#drop_command}.
	 * @param ctx the parse tree
	 */
	void enterDrop_command(DrillParser.Drop_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#drop_command}.
	 * @param ctx the parse tree
	 */
	void exitDrop_command(DrillParser.Drop_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#drop_table}.
	 * @param ctx the parse tree
	 */
	void enterDrop_table(DrillParser.Drop_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#drop_table}.
	 * @param ctx the parse tree
	 */
	void exitDrop_table(DrillParser.Drop_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#drop_view}.
	 * @param ctx the parse tree
	 */
	void enterDrop_view(DrillParser.Drop_viewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#drop_view}.
	 * @param ctx the parse tree
	 */
	void exitDrop_view(DrillParser.Drop_viewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#drop_function}.
	 * @param ctx the parse tree
	 */
	void enterDrop_function(DrillParser.Drop_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#drop_function}.
	 * @param ctx the parse tree
	 */
	void exitDrop_function(DrillParser.Drop_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#other_command}.
	 * @param ctx the parse tree
	 */
	void enterOther_command(DrillParser.Other_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#other_command}.
	 * @param ctx the parse tree
	 */
	void exitOther_command(DrillParser.Other_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#set_command}.
	 * @param ctx the parse tree
	 */
	void enterSet_command(DrillParser.Set_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#set_command}.
	 * @param ctx the parse tree
	 */
	void exitSet_command(DrillParser.Set_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#reset_command}.
	 * @param ctx the parse tree
	 */
	void enterReset_command(DrillParser.Reset_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#reset_command}.
	 * @param ctx the parse tree
	 */
	void exitReset_command(DrillParser.Reset_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#refresh_table_metadata}.
	 * @param ctx the parse tree
	 */
	void enterRefresh_table_metadata(DrillParser.Refresh_table_metadataContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#refresh_table_metadata}.
	 * @param ctx the parse tree
	 */
	void exitRefresh_table_metadata(DrillParser.Refresh_table_metadataContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#analyze_command}.
	 * @param ctx the parse tree
	 */
	void enterAnalyze_command(DrillParser.Analyze_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#analyze_command}.
	 * @param ctx the parse tree
	 */
	void exitAnalyze_command(DrillParser.Analyze_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#param_list}.
	 * @param ctx the parse tree
	 */
	void enterParam_list(DrillParser.Param_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#param_list}.
	 * @param ctx the parse tree
	 */
	void exitParam_list(DrillParser.Param_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#describe_command}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_command(DrillParser.Describe_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#describe_command}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_command(DrillParser.Describe_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#show_command}.
	 * @param ctx the parse tree
	 */
	void enterShow_command(DrillParser.Show_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#show_command}.
	 * @param ctx the parse tree
	 */
	void exitShow_command(DrillParser.Show_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#use_command}.
	 * @param ctx the parse tree
	 */
	void enterUse_command(DrillParser.Use_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#use_command}.
	 * @param ctx the parse tree
	 */
	void exitUse_command(DrillParser.Use_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#select_stmt}.
	 * @param ctx the parse tree
	 */
	void enterSelect_stmt(DrillParser.Select_stmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#select_stmt}.
	 * @param ctx the parse tree
	 */
	void exitSelect_stmt(DrillParser.Select_stmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#with_clause}.
	 * @param ctx the parse tree
	 */
	void enterWith_clause(DrillParser.With_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#with_clause}.
	 * @param ctx the parse tree
	 */
	void exitWith_clause(DrillParser.With_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#with_item}.
	 * @param ctx the parse tree
	 */
	void enterWith_item(DrillParser.With_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#with_item}.
	 * @param ctx the parse tree
	 */
	void exitWith_item(DrillParser.With_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#select_clause}.
	 * @param ctx the parse tree
	 */
	void enterSelect_clause(DrillParser.Select_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#select_clause}.
	 * @param ctx the parse tree
	 */
	void exitSelect_clause(DrillParser.Select_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#select_item}.
	 * @param ctx the parse tree
	 */
	void enterSelect_item(DrillParser.Select_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#select_item}.
	 * @param ctx the parse tree
	 */
	void exitSelect_item(DrillParser.Select_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#from_clause}.
	 * @param ctx the parse tree
	 */
	void enterFrom_clause(DrillParser.From_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#from_clause}.
	 * @param ctx the parse tree
	 */
	void exitFrom_clause(DrillParser.From_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#table_expression}.
	 * @param ctx the parse tree
	 */
	void enterTable_expression(DrillParser.Table_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#table_expression}.
	 * @param ctx the parse tree
	 */
	void exitTable_expression(DrillParser.Table_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#lateral_join_type}.
	 * @param ctx the parse tree
	 */
	void enterLateral_join_type(DrillParser.Lateral_join_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#lateral_join_type}.
	 * @param ctx the parse tree
	 */
	void exitLateral_join_type(DrillParser.Lateral_join_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#lateral_subquery}.
	 * @param ctx the parse tree
	 */
	void enterLateral_subquery(DrillParser.Lateral_subqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#lateral_subquery}.
	 * @param ctx the parse tree
	 */
	void exitLateral_subquery(DrillParser.Lateral_subqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#join_clause}.
	 * @param ctx the parse tree
	 */
	void enterJoin_clause(DrillParser.Join_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#join_clause}.
	 * @param ctx the parse tree
	 */
	void exitJoin_clause(DrillParser.Join_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#join_type}.
	 * @param ctx the parse tree
	 */
	void enterJoin_type(DrillParser.Join_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#join_type}.
	 * @param ctx the parse tree
	 */
	void exitJoin_type(DrillParser.Join_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#table_reference}.
	 * @param ctx the parse tree
	 */
	void enterTable_reference(DrillParser.Table_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#table_reference}.
	 * @param ctx the parse tree
	 */
	void exitTable_reference(DrillParser.Table_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#unnest_table_expr}.
	 * @param ctx the parse tree
	 */
	void enterUnnest_table_expr(DrillParser.Unnest_table_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#unnest_table_expr}.
	 * @param ctx the parse tree
	 */
	void exitUnnest_table_expr(DrillParser.Unnest_table_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#correlation_clause}.
	 * @param ctx the parse tree
	 */
	void enterCorrelation_clause(DrillParser.Correlation_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#correlation_clause}.
	 * @param ctx the parse tree
	 */
	void exitCorrelation_clause(DrillParser.Correlation_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhere_clause(DrillParser.Where_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhere_clause(DrillParser.Where_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#boolean_expression}.
	 * @param ctx the parse tree
	 */
	void enterBoolean_expression(DrillParser.Boolean_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#boolean_expression}.
	 * @param ctx the parse tree
	 */
	void exitBoolean_expression(DrillParser.Boolean_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#table_subquery}.
	 * @param ctx the parse tree
	 */
	void enterTable_subquery(DrillParser.Table_subqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#table_subquery}.
	 * @param ctx the parse tree
	 */
	void exitTable_subquery(DrillParser.Table_subqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(DrillParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(DrillParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#primitive_expression}.
	 * @param ctx the parse tree
	 */
	void enterPrimitive_expression(DrillParser.Primitive_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#primitive_expression}.
	 * @param ctx the parse tree
	 */
	void exitPrimitive_expression(DrillParser.Primitive_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(DrillParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(DrillParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#function_call}.
	 * @param ctx the parse tree
	 */
	void enterFunction_call(DrillParser.Function_callContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#function_call}.
	 * @param ctx the parse tree
	 */
	void exitFunction_call(DrillParser.Function_callContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void enterComparison_operator(DrillParser.Comparison_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void exitComparison_operator(DrillParser.Comparison_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#expr_list}.
	 * @param ctx the parse tree
	 */
	void enterExpr_list(DrillParser.Expr_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#expr_list}.
	 * @param ctx the parse tree
	 */
	void exitExpr_list(DrillParser.Expr_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#group_by_clause}.
	 * @param ctx the parse tree
	 */
	void enterGroup_by_clause(DrillParser.Group_by_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#group_by_clause}.
	 * @param ctx the parse tree
	 */
	void exitGroup_by_clause(DrillParser.Group_by_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#having_clause}.
	 * @param ctx the parse tree
	 */
	void enterHaving_clause(DrillParser.Having_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#having_clause}.
	 * @param ctx the parse tree
	 */
	void exitHaving_clause(DrillParser.Having_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#order_by_clause}.
	 * @param ctx the parse tree
	 */
	void enterOrder_by_clause(DrillParser.Order_by_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#order_by_clause}.
	 * @param ctx the parse tree
	 */
	void exitOrder_by_clause(DrillParser.Order_by_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#limit_clause}.
	 * @param ctx the parse tree
	 */
	void enterLimit_clause(DrillParser.Limit_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#limit_clause}.
	 * @param ctx the parse tree
	 */
	void exitLimit_clause(DrillParser.Limit_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#offset_clause}.
	 * @param ctx the parse tree
	 */
	void enterOffset_clause(DrillParser.Offset_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#offset_clause}.
	 * @param ctx the parse tree
	 */
	void exitOffset_clause(DrillParser.Offset_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(DrillParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(DrillParser.NumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(DrillParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(DrillParser.QueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#select_expression}.
	 * @param ctx the parse tree
	 */
	void enterSelect_expression(DrillParser.Select_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#select_expression}.
	 * @param ctx the parse tree
	 */
	void exitSelect_expression(DrillParser.Select_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#data_type}.
	 * @param ctx the parse tree
	 */
	void enterData_type(DrillParser.Data_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#data_type}.
	 * @param ctx the parse tree
	 */
	void exitData_type(DrillParser.Data_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#default_clause}.
	 * @param ctx the parse tree
	 */
	void enterDefault_clause(DrillParser.Default_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#default_clause}.
	 * @param ctx the parse tree
	 */
	void exitDefault_clause(DrillParser.Default_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#nullability}.
	 * @param ctx the parse tree
	 */
	void enterNullability(DrillParser.NullabilityContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#nullability}.
	 * @param ctx the parse tree
	 */
	void exitNullability(DrillParser.NullabilityContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#format_clause}.
	 * @param ctx the parse tree
	 */
	void enterFormat_clause(DrillParser.Format_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#format_clause}.
	 * @param ctx the parse tree
	 */
	void exitFormat_clause(DrillParser.Format_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#properties_clause}.
	 * @param ctx the parse tree
	 */
	void enterProperties_clause(DrillParser.Properties_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#properties_clause}.
	 * @param ctx the parse tree
	 */
	void exitProperties_clause(DrillParser.Properties_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#or_replace}.
	 * @param ctx the parse tree
	 */
	void enterOr_replace(DrillParser.Or_replaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#or_replace}.
	 * @param ctx the parse tree
	 */
	void exitOr_replace(DrillParser.Or_replaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#if_exists}.
	 * @param ctx the parse tree
	 */
	void enterIf_exists(DrillParser.If_existsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#if_exists}.
	 * @param ctx the parse tree
	 */
	void exitIf_exists(DrillParser.If_existsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#id_}.
	 * @param ctx the parse tree
	 */
	void enterId_(DrillParser.Id_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#id_}.
	 * @param ctx the parse tree
	 */
	void exitId_(DrillParser.Id_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#string}.
	 * @param ctx the parse tree
	 */
	void enterString(DrillParser.StringContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#string}.
	 * @param ctx the parse tree
	 */
	void exitString(DrillParser.StringContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#workspace}.
	 * @param ctx the parse tree
	 */
	void enterWorkspace(DrillParser.WorkspaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#workspace}.
	 * @param ctx the parse tree
	 */
	void exitWorkspace(DrillParser.WorkspaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(DrillParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(DrillParser.NameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#schema_name}.
	 * @param ctx the parse tree
	 */
	void enterSchema_name(DrillParser.Schema_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#schema_name}.
	 * @param ctx the parse tree
	 */
	void exitSchema_name(DrillParser.Schema_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#table_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_name(DrillParser.Table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#table_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_name(DrillParser.Table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#view_name}.
	 * @param ctx the parse tree
	 */
	void enterView_name(DrillParser.View_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#view_name}.
	 * @param ctx the parse tree
	 */
	void exitView_name(DrillParser.View_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#correlation_name}.
	 * @param ctx the parse tree
	 */
	void enterCorrelation_name(DrillParser.Correlation_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#correlation_name}.
	 * @param ctx the parse tree
	 */
	void exitCorrelation_name(DrillParser.Correlation_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#column_name}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name(DrillParser.Column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#column_name}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name(DrillParser.Column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#function_name}.
	 * @param ctx the parse tree
	 */
	void enterFunction_name(DrillParser.Function_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#function_name}.
	 * @param ctx the parse tree
	 */
	void exitFunction_name(DrillParser.Function_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#column_alias}.
	 * @param ctx the parse tree
	 */
	void enterColumn_alias(DrillParser.Column_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#column_alias}.
	 * @param ctx the parse tree
	 */
	void exitColumn_alias(DrillParser.Column_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#table_path}.
	 * @param ctx the parse tree
	 */
	void enterTable_path(DrillParser.Table_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#table_path}.
	 * @param ctx the parse tree
	 */
	void exitTable_path(DrillParser.Table_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link DrillParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(DrillParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DrillParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(DrillParser.ValueContext ctx);
}