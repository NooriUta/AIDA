// Generated from C:/AIDA/Dali4/HOUND/Hound/src/main/resources/grammars/sql/derby/DerbyParser.g4 by ANTLR 4.13.1
package com.hound.parser.base.grammars.sql.derby;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DerbyParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DerbyParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DerbyParser#derby_file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDerby_file(DerbyParser.Derby_fileContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#batch}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBatch(DerbyParser.BatchContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#sql_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_command(DerbyParser.Sql_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#ddl_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdl_command(DerbyParser.Ddl_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#dml_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDml_command(DerbyParser.Dml_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#keyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyword(DerbyParser.KeywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#insert_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_statement(DerbyParser.Insert_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#offset_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOffset_clause(DerbyParser.Offset_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#fetch_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_clause(DerbyParser.Fetch_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#first_next}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFirst_next(DerbyParser.First_nextContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#row_rows}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow_rows(DerbyParser.Row_rowsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#integer_literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger_literal(DerbyParser.Integer_literalContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#merge_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMerge_statement(DerbyParser.Merge_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#merge_when_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMerge_when_clause(DerbyParser.Merge_when_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#merge_when_matched}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMerge_when_matched(DerbyParser.Merge_when_matchedContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#merge_when_not_matched}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMerge_when_not_matched(DerbyParser.Merge_when_not_matchedContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#merge_update}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMerge_update(DerbyParser.Merge_updateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#merge_insert}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMerge_insert(DerbyParser.Merge_insertContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#search_condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearch_condition(DerbyParser.Search_conditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#match_refinement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatch_refinement(DerbyParser.Match_refinementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#update_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate_statement(DerbyParser.Update_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#where_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere_clause(DerbyParser.Where_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#boolean_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolean_expression(DerbyParser.Boolean_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#table_subquery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_subquery(DerbyParser.Table_subqueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#character_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacter_expression(DerbyParser.Character_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#comparison_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison_operator(DerbyParser.Comparison_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(DerbyParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#delete_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete_statement(DerbyParser.Delete_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#other_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOther_command(DerbyParser.Other_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#grantees}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantees(DerbyParser.GranteesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#grantee}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantee(DerbyParser.GranteeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#isolation_level}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsolation_level(DerbyParser.Isolation_levelContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#constraint_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraint_name_list(DerbyParser.Constraint_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#column_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_definition(DerbyParser.Column_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#with_default}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_default(DerbyParser.With_defaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#default_constant_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefault_constant_expression(DerbyParser.Default_constant_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#generated_column_spec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenerated_column_spec(DerbyParser.Generated_column_specContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#generation_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneration_clause(DerbyParser.Generation_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#column_level_constraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_level_constraint(DerbyParser.Column_level_constraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#table_level_constraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_level_constraint(DerbyParser.Table_level_constraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#references_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferences_clause(DerbyParser.References_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#no_action_restrict}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNo_action_restrict(DerbyParser.No_action_restrictContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#constraint_characteristics}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraint_characteristics(DerbyParser.Constraint_characteristicsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#constraint_check_time}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraint_check_time(DerbyParser.Constraint_check_timeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#simple_column_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_column_name_list(DerbyParser.Simple_column_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#truncate_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTruncate_table(DerbyParser.Truncate_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#privilege_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilege_type(DerbyParser.Privilege_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#privilege_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilege_list(DerbyParser.Privilege_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#table_privilege}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_privilege(DerbyParser.Table_privilegeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#column_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_list(DerbyParser.Column_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#column_identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_identifier(DerbyParser.Column_identifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#revokees}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevokees(DerbyParser.RevokeesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#revokee}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevokee(DerbyParser.RevokeeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#authorization_identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAuthorization_identifier(DerbyParser.Authorization_identifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#alter_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_command(DerbyParser.Alter_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#alter_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table(DerbyParser.Alter_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#constraint_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraint_clause(DerbyParser.Constraint_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#cascade_restrict}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCascade_restrict(DerbyParser.Cascade_restrictContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#row_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow_table(DerbyParser.Row_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#column_alteration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_alteration(DerbyParser.Column_alterationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#integer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger(DerbyParser.IntegerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#integer_constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger_constant(DerbyParser.Integer_constantContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#defaultValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaultValue(DerbyParser.DefaultValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#constant_expression_null}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant_expression_null(DerbyParser.Constant_expression_nullContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#always_by_default}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlways_by_default(DerbyParser.Always_by_defaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#set_drop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_drop(DerbyParser.Set_dropContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#with_set}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_set(DerbyParser.With_setContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#create_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_command(DerbyParser.Create_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#create_derby_aggregate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_derby_aggregate(DerbyParser.Create_derby_aggregateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#value_data_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_data_type(DerbyParser.Value_data_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#create_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_function(DerbyParser.Create_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#function_parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_parameter(DerbyParser.Function_parameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#return_data_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturn_data_type(DerbyParser.Return_data_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#table_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_type(DerbyParser.Table_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#column_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_element(DerbyParser.Column_elementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#function_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_element(DerbyParser.Function_elementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#create_index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_index(DerbyParser.Create_indexContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#create_procedure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_procedure(DerbyParser.Create_procedureContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#procedure_parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedure_parameter(DerbyParser.Procedure_parameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#data_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_type(DerbyParser.Data_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#procedure_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedure_element(DerbyParser.Procedure_elementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#create_role}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_role(DerbyParser.Create_roleContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#create_schema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_schema(DerbyParser.Create_schemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#create_sequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_sequence(DerbyParser.Create_sequenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#sequence_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequence_element(DerbyParser.Sequence_elementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#signedInteger}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignedInteger(DerbyParser.SignedIntegerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#create_synonym}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_synonym(DerbyParser.Create_synonymContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#create_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_table(DerbyParser.Create_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#create_trigger}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_trigger(DerbyParser.Create_triggerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#create_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_type(DerbyParser.Create_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#single_quoted_string}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingle_quoted_string(DerbyParser.Single_quoted_stringContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#create_view}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_view(DerbyParser.Create_viewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuery(DerbyParser.QueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#select_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_expression(DerbyParser.Select_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#select_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_item(DerbyParser.Select_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#simple_column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_column_name(DerbyParser.Simple_column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#drop_command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_command(DerbyParser.Drop_commandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#drop_derby_aggregate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_derby_aggregate(DerbyParser.Drop_derby_aggregateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#drop_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_function(DerbyParser.Drop_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#drop_index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_index(DerbyParser.Drop_indexContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#drop_procedure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_procedure(DerbyParser.Drop_procedureContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#drop_role}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_role(DerbyParser.Drop_roleContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#drop_schema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_schema(DerbyParser.Drop_schemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#drop_sequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_sequence(DerbyParser.Drop_sequenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#drop_synonym}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_synonym(DerbyParser.Drop_synonymContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#drop_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_table(DerbyParser.Drop_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#drop_trigger}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_trigger(DerbyParser.Drop_triggerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#drop_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_type(DerbyParser.Drop_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#drop_view}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_view(DerbyParser.Drop_viewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#string}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString(DerbyParser.StringContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_name(DerbyParser.Table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#schema_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_name(DerbyParser.Schema_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#role_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRole_name(DerbyParser.Role_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#aggregate_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregate_name(DerbyParser.Aggregate_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#constraint_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraint_name(DerbyParser.Constraint_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_name(DerbyParser.Column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#correlation_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCorrelation_name(DerbyParser.Correlation_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#function_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_name(DerbyParser.Function_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#index_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_name(DerbyParser.Index_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#procedure_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedure_name(DerbyParser.Procedure_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#parameter_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameter_name(DerbyParser.Parameter_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#sequence_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSequence_name(DerbyParser.Sequence_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#synonym_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSynonym_name(DerbyParser.Synonym_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#trigger_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrigger_name(DerbyParser.Trigger_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#type_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_name(DerbyParser.Type_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#user_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUser_name(DerbyParser.User_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#view_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitView_name(DerbyParser.View_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#cursor_nName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursor_nName(DerbyParser.Cursor_nNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#id_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId_(DerbyParser.Id_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#num}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNum(DerbyParser.NumContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#expr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_list(DerbyParser.Expr_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(DerbyParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#primitive_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitive_expression(DerbyParser.Primitive_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#function_call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_call(DerbyParser.Function_callContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(DerbyParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#sign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSign(DerbyParser.SignContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#case_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_expression(DerbyParser.Case_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#searched_case}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearched_case(DerbyParser.Searched_caseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#simple_case}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_case(DerbyParser.Simple_caseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#extended_case}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtended_case(DerbyParser.Extended_caseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#when_operand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhen_operand(DerbyParser.When_operandContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#value_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue_expression(DerbyParser.Value_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#standard_built_in_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStandard_built_in_function(DerbyParser.Standard_built_in_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#aggreagate_built_in_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggreagate_built_in_function(DerbyParser.Aggreagate_built_in_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#select_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_statement(DerbyParser.Select_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#for_update_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_update_clause(DerbyParser.For_update_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#all_distinct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAll_distinct(DerbyParser.All_distinctContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#from_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrom_clause(DerbyParser.From_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#table_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_expression(DerbyParser.Table_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#join_operation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin_operation(DerbyParser.Join_operationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#table_view_or_function_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_view_or_function_expression(DerbyParser.Table_view_or_function_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#using_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUsing_clause(DerbyParser.Using_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#correlation_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCorrelation_clause(DerbyParser.Correlation_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#table_function_invocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_function_invocation(DerbyParser.Table_function_invocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#function_arg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_arg(DerbyParser.Function_argContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#group_by_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_by_clause(DerbyParser.Group_by_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#having_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHaving_clause(DerbyParser.Having_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#order_by_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrder_by_clause(DerbyParser.Order_by_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#order_by_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrder_by_item(DerbyParser.Order_by_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#asc_desc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsc_desc(DerbyParser.Asc_descContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#columnPosition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnPosition(DerbyParser.ColumnPositionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DerbyParser#values_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValues_expression(DerbyParser.Values_expressionContext ctx);
}