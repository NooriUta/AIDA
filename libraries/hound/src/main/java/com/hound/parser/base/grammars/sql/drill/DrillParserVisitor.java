// Generated from C:/AIDA/Dali4/HOUND/Hound/src/main/resources/grammars/sql/drill/DrillParser.g4 by ANTLR 4.13.1
package com.hound.parser.base.grammars.sql.drill;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DrillParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DrillParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DrillParser#drill_file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrill_file(DrillParser.Drill_fileContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#batch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBatch(DrillParser.BatchContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#sql_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_command(DrillParser.Sql_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#ddl_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdl_command(DrillParser.Ddl_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#create_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_command(DrillParser.Create_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#create_schema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_schema(DrillParser.Create_schemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#column_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_definition(DrillParser.Column_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#kv_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKv_list(DrillParser.Kv_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#kv_pair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKv_pair(DrillParser.Kv_pairContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#create_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_table(DrillParser.Create_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#column_list_paren}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_list_paren(DrillParser.Column_list_parenContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#column_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_list(DrillParser.Column_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#create_temp_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_temp_table(DrillParser.Create_temp_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#partition_by_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_by_clause(DrillParser.Partition_by_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#create_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_function(DrillParser.Create_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#create_view}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_view(DrillParser.Create_viewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#alter_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_command(DrillParser.Alter_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#alter_system}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_system(DrillParser.Alter_systemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#option_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOption_name(DrillParser.Option_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#drop_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_command(DrillParser.Drop_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#drop_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_table(DrillParser.Drop_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#drop_view}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_view(DrillParser.Drop_viewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#drop_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_function(DrillParser.Drop_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#other_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOther_command(DrillParser.Other_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#set_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_command(DrillParser.Set_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#reset_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReset_command(DrillParser.Reset_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#refresh_table_metadata}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefresh_table_metadata(DrillParser.Refresh_table_metadataContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#analyze_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnalyze_command(DrillParser.Analyze_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#param_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_list(DrillParser.Param_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#describe_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_command(DrillParser.Describe_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#show_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_command(DrillParser.Show_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#use_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUse_command(DrillParser.Use_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#select_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_stmt(DrillParser.Select_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#with_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_clause(DrillParser.With_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#with_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_item(DrillParser.With_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#select_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_clause(DrillParser.Select_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#select_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_item(DrillParser.Select_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#from_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrom_clause(DrillParser.From_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#table_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_expression(DrillParser.Table_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#lateral_join_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLateral_join_type(DrillParser.Lateral_join_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#lateral_subquery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLateral_subquery(DrillParser.Lateral_subqueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#join_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin_clause(DrillParser.Join_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#join_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin_type(DrillParser.Join_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#table_reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_reference(DrillParser.Table_referenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#unnest_table_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnnest_table_expr(DrillParser.Unnest_table_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#correlation_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCorrelation_clause(DrillParser.Correlation_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#where_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere_clause(DrillParser.Where_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#boolean_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolean_expression(DrillParser.Boolean_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#table_subquery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_subquery(DrillParser.Table_subqueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(DrillParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#primitive_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitive_expression(DrillParser.Primitive_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(DrillParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#function_call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_call(DrillParser.Function_callContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#comparison_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison_operator(DrillParser.Comparison_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#expr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_list(DrillParser.Expr_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#group_by_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_by_clause(DrillParser.Group_by_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#having_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHaving_clause(DrillParser.Having_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#order_by_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrder_by_clause(DrillParser.Order_by_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#limit_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimit_clause(DrillParser.Limit_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#offset_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOffset_clause(DrillParser.Offset_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber(DrillParser.NumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuery(DrillParser.QueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#select_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_expression(DrillParser.Select_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#data_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_type(DrillParser.Data_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#default_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefault_clause(DrillParser.Default_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#nullability}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullability(DrillParser.NullabilityContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#format_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormat_clause(DrillParser.Format_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#properties_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperties_clause(DrillParser.Properties_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#or_replace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr_replace(DrillParser.Or_replaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#if_exists}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_exists(DrillParser.If_existsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#id_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId_(DrillParser.Id_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#string}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString(DrillParser.StringContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#workspace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorkspace(DrillParser.WorkspaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(DrillParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#schema_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_name(DrillParser.Schema_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_name(DrillParser.Table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#view_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitView_name(DrillParser.View_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#correlation_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCorrelation_name(DrillParser.Correlation_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_name(DrillParser.Column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#function_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_name(DrillParser.Function_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#column_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_alias(DrillParser.Column_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#table_path}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_path(DrillParser.Table_pathContext ctx);
	/**
	 * Visit a parse tree produced by {@link DrillParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(DrillParser.ValueContext ctx);
}