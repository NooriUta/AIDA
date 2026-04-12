// Generated from C:/AIDA/aida-root/libraries/hound/src/main/resources/grammars/sql/derby/DerbyParser.g4 by ANTLR 4.13.2
package com.hound.parser.base.grammars.sql.derby;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DerbyParser}.
 */
public interface DerbyParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DerbyParser#derby_file}.
	 * @param ctx the parse tree
	 */
	void enterDerby_file(DerbyParser.Derby_fileContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#derby_file}.
	 * @param ctx the parse tree
	 */
	void exitDerby_file(DerbyParser.Derby_fileContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#batch}.
	 * @param ctx the parse tree
	 */
	void enterBatch(DerbyParser.BatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#batch}.
	 * @param ctx the parse tree
	 */
	void exitBatch(DerbyParser.BatchContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#sql_command}.
	 * @param ctx the parse tree
	 */
	void enterSql_command(DerbyParser.Sql_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#sql_command}.
	 * @param ctx the parse tree
	 */
	void exitSql_command(DerbyParser.Sql_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#ddl_command}.
	 * @param ctx the parse tree
	 */
	void enterDdl_command(DerbyParser.Ddl_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#ddl_command}.
	 * @param ctx the parse tree
	 */
	void exitDdl_command(DerbyParser.Ddl_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#dml_command}.
	 * @param ctx the parse tree
	 */
	void enterDml_command(DerbyParser.Dml_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#dml_command}.
	 * @param ctx the parse tree
	 */
	void exitDml_command(DerbyParser.Dml_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#keyword}.
	 * @param ctx the parse tree
	 */
	void enterKeyword(DerbyParser.KeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#keyword}.
	 * @param ctx the parse tree
	 */
	void exitKeyword(DerbyParser.KeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#insert_statement}.
	 * @param ctx the parse tree
	 */
	void enterInsert_statement(DerbyParser.Insert_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#insert_statement}.
	 * @param ctx the parse tree
	 */
	void exitInsert_statement(DerbyParser.Insert_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#offset_clause}.
	 * @param ctx the parse tree
	 */
	void enterOffset_clause(DerbyParser.Offset_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#offset_clause}.
	 * @param ctx the parse tree
	 */
	void exitOffset_clause(DerbyParser.Offset_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#fetch_clause}.
	 * @param ctx the parse tree
	 */
	void enterFetch_clause(DerbyParser.Fetch_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#fetch_clause}.
	 * @param ctx the parse tree
	 */
	void exitFetch_clause(DerbyParser.Fetch_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#first_next}.
	 * @param ctx the parse tree
	 */
	void enterFirst_next(DerbyParser.First_nextContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#first_next}.
	 * @param ctx the parse tree
	 */
	void exitFirst_next(DerbyParser.First_nextContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#row_rows}.
	 * @param ctx the parse tree
	 */
	void enterRow_rows(DerbyParser.Row_rowsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#row_rows}.
	 * @param ctx the parse tree
	 */
	void exitRow_rows(DerbyParser.Row_rowsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#integer_literal}.
	 * @param ctx the parse tree
	 */
	void enterInteger_literal(DerbyParser.Integer_literalContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#integer_literal}.
	 * @param ctx the parse tree
	 */
	void exitInteger_literal(DerbyParser.Integer_literalContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#merge_statement}.
	 * @param ctx the parse tree
	 */
	void enterMerge_statement(DerbyParser.Merge_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#merge_statement}.
	 * @param ctx the parse tree
	 */
	void exitMerge_statement(DerbyParser.Merge_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#merge_when_clause}.
	 * @param ctx the parse tree
	 */
	void enterMerge_when_clause(DerbyParser.Merge_when_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#merge_when_clause}.
	 * @param ctx the parse tree
	 */
	void exitMerge_when_clause(DerbyParser.Merge_when_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#merge_when_matched}.
	 * @param ctx the parse tree
	 */
	void enterMerge_when_matched(DerbyParser.Merge_when_matchedContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#merge_when_matched}.
	 * @param ctx the parse tree
	 */
	void exitMerge_when_matched(DerbyParser.Merge_when_matchedContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#merge_when_not_matched}.
	 * @param ctx the parse tree
	 */
	void enterMerge_when_not_matched(DerbyParser.Merge_when_not_matchedContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#merge_when_not_matched}.
	 * @param ctx the parse tree
	 */
	void exitMerge_when_not_matched(DerbyParser.Merge_when_not_matchedContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#merge_update}.
	 * @param ctx the parse tree
	 */
	void enterMerge_update(DerbyParser.Merge_updateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#merge_update}.
	 * @param ctx the parse tree
	 */
	void exitMerge_update(DerbyParser.Merge_updateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#merge_insert}.
	 * @param ctx the parse tree
	 */
	void enterMerge_insert(DerbyParser.Merge_insertContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#merge_insert}.
	 * @param ctx the parse tree
	 */
	void exitMerge_insert(DerbyParser.Merge_insertContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#search_condition}.
	 * @param ctx the parse tree
	 */
	void enterSearch_condition(DerbyParser.Search_conditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#search_condition}.
	 * @param ctx the parse tree
	 */
	void exitSearch_condition(DerbyParser.Search_conditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#match_refinement}.
	 * @param ctx the parse tree
	 */
	void enterMatch_refinement(DerbyParser.Match_refinementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#match_refinement}.
	 * @param ctx the parse tree
	 */
	void exitMatch_refinement(DerbyParser.Match_refinementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#update_statement}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_statement(DerbyParser.Update_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#update_statement}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_statement(DerbyParser.Update_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhere_clause(DerbyParser.Where_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhere_clause(DerbyParser.Where_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#boolean_expression}.
	 * @param ctx the parse tree
	 */
	void enterBoolean_expression(DerbyParser.Boolean_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#boolean_expression}.
	 * @param ctx the parse tree
	 */
	void exitBoolean_expression(DerbyParser.Boolean_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#table_subquery}.
	 * @param ctx the parse tree
	 */
	void enterTable_subquery(DerbyParser.Table_subqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#table_subquery}.
	 * @param ctx the parse tree
	 */
	void exitTable_subquery(DerbyParser.Table_subqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#character_expression}.
	 * @param ctx the parse tree
	 */
	void enterCharacter_expression(DerbyParser.Character_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#character_expression}.
	 * @param ctx the parse tree
	 */
	void exitCharacter_expression(DerbyParser.Character_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void enterComparison_operator(DerbyParser.Comparison_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void exitComparison_operator(DerbyParser.Comparison_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(DerbyParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(DerbyParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#delete_statement}.
	 * @param ctx the parse tree
	 */
	void enterDelete_statement(DerbyParser.Delete_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#delete_statement}.
	 * @param ctx the parse tree
	 */
	void exitDelete_statement(DerbyParser.Delete_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#other_command}.
	 * @param ctx the parse tree
	 */
	void enterOther_command(DerbyParser.Other_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#other_command}.
	 * @param ctx the parse tree
	 */
	void exitOther_command(DerbyParser.Other_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#grantees}.
	 * @param ctx the parse tree
	 */
	void enterGrantees(DerbyParser.GranteesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#grantees}.
	 * @param ctx the parse tree
	 */
	void exitGrantees(DerbyParser.GranteesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#grantee}.
	 * @param ctx the parse tree
	 */
	void enterGrantee(DerbyParser.GranteeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#grantee}.
	 * @param ctx the parse tree
	 */
	void exitGrantee(DerbyParser.GranteeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#isolation_level}.
	 * @param ctx the parse tree
	 */
	void enterIsolation_level(DerbyParser.Isolation_levelContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#isolation_level}.
	 * @param ctx the parse tree
	 */
	void exitIsolation_level(DerbyParser.Isolation_levelContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#constraint_name_list}.
	 * @param ctx the parse tree
	 */
	void enterConstraint_name_list(DerbyParser.Constraint_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#constraint_name_list}.
	 * @param ctx the parse tree
	 */
	void exitConstraint_name_list(DerbyParser.Constraint_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#column_definition}.
	 * @param ctx the parse tree
	 */
	void enterColumn_definition(DerbyParser.Column_definitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#column_definition}.
	 * @param ctx the parse tree
	 */
	void exitColumn_definition(DerbyParser.Column_definitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#with_default}.
	 * @param ctx the parse tree
	 */
	void enterWith_default(DerbyParser.With_defaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#with_default}.
	 * @param ctx the parse tree
	 */
	void exitWith_default(DerbyParser.With_defaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#default_constant_expression}.
	 * @param ctx the parse tree
	 */
	void enterDefault_constant_expression(DerbyParser.Default_constant_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#default_constant_expression}.
	 * @param ctx the parse tree
	 */
	void exitDefault_constant_expression(DerbyParser.Default_constant_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#generated_column_spec}.
	 * @param ctx the parse tree
	 */
	void enterGenerated_column_spec(DerbyParser.Generated_column_specContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#generated_column_spec}.
	 * @param ctx the parse tree
	 */
	void exitGenerated_column_spec(DerbyParser.Generated_column_specContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#generation_clause}.
	 * @param ctx the parse tree
	 */
	void enterGeneration_clause(DerbyParser.Generation_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#generation_clause}.
	 * @param ctx the parse tree
	 */
	void exitGeneration_clause(DerbyParser.Generation_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#column_level_constraint}.
	 * @param ctx the parse tree
	 */
	void enterColumn_level_constraint(DerbyParser.Column_level_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#column_level_constraint}.
	 * @param ctx the parse tree
	 */
	void exitColumn_level_constraint(DerbyParser.Column_level_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#table_level_constraint}.
	 * @param ctx the parse tree
	 */
	void enterTable_level_constraint(DerbyParser.Table_level_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#table_level_constraint}.
	 * @param ctx the parse tree
	 */
	void exitTable_level_constraint(DerbyParser.Table_level_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#references_clause}.
	 * @param ctx the parse tree
	 */
	void enterReferences_clause(DerbyParser.References_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#references_clause}.
	 * @param ctx the parse tree
	 */
	void exitReferences_clause(DerbyParser.References_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#no_action_restrict}.
	 * @param ctx the parse tree
	 */
	void enterNo_action_restrict(DerbyParser.No_action_restrictContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#no_action_restrict}.
	 * @param ctx the parse tree
	 */
	void exitNo_action_restrict(DerbyParser.No_action_restrictContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#constraint_characteristics}.
	 * @param ctx the parse tree
	 */
	void enterConstraint_characteristics(DerbyParser.Constraint_characteristicsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#constraint_characteristics}.
	 * @param ctx the parse tree
	 */
	void exitConstraint_characteristics(DerbyParser.Constraint_characteristicsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#constraint_check_time}.
	 * @param ctx the parse tree
	 */
	void enterConstraint_check_time(DerbyParser.Constraint_check_timeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#constraint_check_time}.
	 * @param ctx the parse tree
	 */
	void exitConstraint_check_time(DerbyParser.Constraint_check_timeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#simple_column_name_list}.
	 * @param ctx the parse tree
	 */
	void enterSimple_column_name_list(DerbyParser.Simple_column_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#simple_column_name_list}.
	 * @param ctx the parse tree
	 */
	void exitSimple_column_name_list(DerbyParser.Simple_column_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#truncate_table}.
	 * @param ctx the parse tree
	 */
	void enterTruncate_table(DerbyParser.Truncate_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#truncate_table}.
	 * @param ctx the parse tree
	 */
	void exitTruncate_table(DerbyParser.Truncate_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#privilege_type}.
	 * @param ctx the parse tree
	 */
	void enterPrivilege_type(DerbyParser.Privilege_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#privilege_type}.
	 * @param ctx the parse tree
	 */
	void exitPrivilege_type(DerbyParser.Privilege_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#privilege_list}.
	 * @param ctx the parse tree
	 */
	void enterPrivilege_list(DerbyParser.Privilege_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#privilege_list}.
	 * @param ctx the parse tree
	 */
	void exitPrivilege_list(DerbyParser.Privilege_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#table_privilege}.
	 * @param ctx the parse tree
	 */
	void enterTable_privilege(DerbyParser.Table_privilegeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#table_privilege}.
	 * @param ctx the parse tree
	 */
	void exitTable_privilege(DerbyParser.Table_privilegeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#column_list}.
	 * @param ctx the parse tree
	 */
	void enterColumn_list(DerbyParser.Column_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#column_list}.
	 * @param ctx the parse tree
	 */
	void exitColumn_list(DerbyParser.Column_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#column_identifier}.
	 * @param ctx the parse tree
	 */
	void enterColumn_identifier(DerbyParser.Column_identifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#column_identifier}.
	 * @param ctx the parse tree
	 */
	void exitColumn_identifier(DerbyParser.Column_identifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#revokees}.
	 * @param ctx the parse tree
	 */
	void enterRevokees(DerbyParser.RevokeesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#revokees}.
	 * @param ctx the parse tree
	 */
	void exitRevokees(DerbyParser.RevokeesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#revokee}.
	 * @param ctx the parse tree
	 */
	void enterRevokee(DerbyParser.RevokeeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#revokee}.
	 * @param ctx the parse tree
	 */
	void exitRevokee(DerbyParser.RevokeeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#authorization_identifier}.
	 * @param ctx the parse tree
	 */
	void enterAuthorization_identifier(DerbyParser.Authorization_identifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#authorization_identifier}.
	 * @param ctx the parse tree
	 */
	void exitAuthorization_identifier(DerbyParser.Authorization_identifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#alter_command}.
	 * @param ctx the parse tree
	 */
	void enterAlter_command(DerbyParser.Alter_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#alter_command}.
	 * @param ctx the parse tree
	 */
	void exitAlter_command(DerbyParser.Alter_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#alter_table}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table(DerbyParser.Alter_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#alter_table}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table(DerbyParser.Alter_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#constraint_clause}.
	 * @param ctx the parse tree
	 */
	void enterConstraint_clause(DerbyParser.Constraint_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#constraint_clause}.
	 * @param ctx the parse tree
	 */
	void exitConstraint_clause(DerbyParser.Constraint_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#cascade_restrict}.
	 * @param ctx the parse tree
	 */
	void enterCascade_restrict(DerbyParser.Cascade_restrictContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#cascade_restrict}.
	 * @param ctx the parse tree
	 */
	void exitCascade_restrict(DerbyParser.Cascade_restrictContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#row_table}.
	 * @param ctx the parse tree
	 */
	void enterRow_table(DerbyParser.Row_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#row_table}.
	 * @param ctx the parse tree
	 */
	void exitRow_table(DerbyParser.Row_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#column_alteration}.
	 * @param ctx the parse tree
	 */
	void enterColumn_alteration(DerbyParser.Column_alterationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#column_alteration}.
	 * @param ctx the parse tree
	 */
	void exitColumn_alteration(DerbyParser.Column_alterationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#integer}.
	 * @param ctx the parse tree
	 */
	void enterInteger(DerbyParser.IntegerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#integer}.
	 * @param ctx the parse tree
	 */
	void exitInteger(DerbyParser.IntegerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#integer_constant}.
	 * @param ctx the parse tree
	 */
	void enterInteger_constant(DerbyParser.Integer_constantContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#integer_constant}.
	 * @param ctx the parse tree
	 */
	void exitInteger_constant(DerbyParser.Integer_constantContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#defaultValue}.
	 * @param ctx the parse tree
	 */
	void enterDefaultValue(DerbyParser.DefaultValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#defaultValue}.
	 * @param ctx the parse tree
	 */
	void exitDefaultValue(DerbyParser.DefaultValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#constant_expression_null}.
	 * @param ctx the parse tree
	 */
	void enterConstant_expression_null(DerbyParser.Constant_expression_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#constant_expression_null}.
	 * @param ctx the parse tree
	 */
	void exitConstant_expression_null(DerbyParser.Constant_expression_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#always_by_default}.
	 * @param ctx the parse tree
	 */
	void enterAlways_by_default(DerbyParser.Always_by_defaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#always_by_default}.
	 * @param ctx the parse tree
	 */
	void exitAlways_by_default(DerbyParser.Always_by_defaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#set_drop}.
	 * @param ctx the parse tree
	 */
	void enterSet_drop(DerbyParser.Set_dropContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#set_drop}.
	 * @param ctx the parse tree
	 */
	void exitSet_drop(DerbyParser.Set_dropContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#with_set}.
	 * @param ctx the parse tree
	 */
	void enterWith_set(DerbyParser.With_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#with_set}.
	 * @param ctx the parse tree
	 */
	void exitWith_set(DerbyParser.With_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#create_command}.
	 * @param ctx the parse tree
	 */
	void enterCreate_command(DerbyParser.Create_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#create_command}.
	 * @param ctx the parse tree
	 */
	void exitCreate_command(DerbyParser.Create_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#create_derby_aggregate}.
	 * @param ctx the parse tree
	 */
	void enterCreate_derby_aggregate(DerbyParser.Create_derby_aggregateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#create_derby_aggregate}.
	 * @param ctx the parse tree
	 */
	void exitCreate_derby_aggregate(DerbyParser.Create_derby_aggregateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#value_data_type}.
	 * @param ctx the parse tree
	 */
	void enterValue_data_type(DerbyParser.Value_data_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#value_data_type}.
	 * @param ctx the parse tree
	 */
	void exitValue_data_type(DerbyParser.Value_data_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#create_function}.
	 * @param ctx the parse tree
	 */
	void enterCreate_function(DerbyParser.Create_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#create_function}.
	 * @param ctx the parse tree
	 */
	void exitCreate_function(DerbyParser.Create_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#function_parameter}.
	 * @param ctx the parse tree
	 */
	void enterFunction_parameter(DerbyParser.Function_parameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#function_parameter}.
	 * @param ctx the parse tree
	 */
	void exitFunction_parameter(DerbyParser.Function_parameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#return_data_type}.
	 * @param ctx the parse tree
	 */
	void enterReturn_data_type(DerbyParser.Return_data_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#return_data_type}.
	 * @param ctx the parse tree
	 */
	void exitReturn_data_type(DerbyParser.Return_data_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#table_type}.
	 * @param ctx the parse tree
	 */
	void enterTable_type(DerbyParser.Table_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#table_type}.
	 * @param ctx the parse tree
	 */
	void exitTable_type(DerbyParser.Table_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#column_element}.
	 * @param ctx the parse tree
	 */
	void enterColumn_element(DerbyParser.Column_elementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#column_element}.
	 * @param ctx the parse tree
	 */
	void exitColumn_element(DerbyParser.Column_elementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#function_element}.
	 * @param ctx the parse tree
	 */
	void enterFunction_element(DerbyParser.Function_elementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#function_element}.
	 * @param ctx the parse tree
	 */
	void exitFunction_element(DerbyParser.Function_elementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#create_index}.
	 * @param ctx the parse tree
	 */
	void enterCreate_index(DerbyParser.Create_indexContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#create_index}.
	 * @param ctx the parse tree
	 */
	void exitCreate_index(DerbyParser.Create_indexContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#create_procedure}.
	 * @param ctx the parse tree
	 */
	void enterCreate_procedure(DerbyParser.Create_procedureContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#create_procedure}.
	 * @param ctx the parse tree
	 */
	void exitCreate_procedure(DerbyParser.Create_procedureContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#procedure_parameter}.
	 * @param ctx the parse tree
	 */
	void enterProcedure_parameter(DerbyParser.Procedure_parameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#procedure_parameter}.
	 * @param ctx the parse tree
	 */
	void exitProcedure_parameter(DerbyParser.Procedure_parameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#data_type}.
	 * @param ctx the parse tree
	 */
	void enterData_type(DerbyParser.Data_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#data_type}.
	 * @param ctx the parse tree
	 */
	void exitData_type(DerbyParser.Data_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#procedure_element}.
	 * @param ctx the parse tree
	 */
	void enterProcedure_element(DerbyParser.Procedure_elementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#procedure_element}.
	 * @param ctx the parse tree
	 */
	void exitProcedure_element(DerbyParser.Procedure_elementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#create_role}.
	 * @param ctx the parse tree
	 */
	void enterCreate_role(DerbyParser.Create_roleContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#create_role}.
	 * @param ctx the parse tree
	 */
	void exitCreate_role(DerbyParser.Create_roleContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#create_schema}.
	 * @param ctx the parse tree
	 */
	void enterCreate_schema(DerbyParser.Create_schemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#create_schema}.
	 * @param ctx the parse tree
	 */
	void exitCreate_schema(DerbyParser.Create_schemaContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#create_sequence}.
	 * @param ctx the parse tree
	 */
	void enterCreate_sequence(DerbyParser.Create_sequenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#create_sequence}.
	 * @param ctx the parse tree
	 */
	void exitCreate_sequence(DerbyParser.Create_sequenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#sequence_element}.
	 * @param ctx the parse tree
	 */
	void enterSequence_element(DerbyParser.Sequence_elementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#sequence_element}.
	 * @param ctx the parse tree
	 */
	void exitSequence_element(DerbyParser.Sequence_elementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#signedInteger}.
	 * @param ctx the parse tree
	 */
	void enterSignedInteger(DerbyParser.SignedIntegerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#signedInteger}.
	 * @param ctx the parse tree
	 */
	void exitSignedInteger(DerbyParser.SignedIntegerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#create_synonym}.
	 * @param ctx the parse tree
	 */
	void enterCreate_synonym(DerbyParser.Create_synonymContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#create_synonym}.
	 * @param ctx the parse tree
	 */
	void exitCreate_synonym(DerbyParser.Create_synonymContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#create_table}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table(DerbyParser.Create_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#create_table}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table(DerbyParser.Create_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#create_trigger}.
	 * @param ctx the parse tree
	 */
	void enterCreate_trigger(DerbyParser.Create_triggerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#create_trigger}.
	 * @param ctx the parse tree
	 */
	void exitCreate_trigger(DerbyParser.Create_triggerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#create_type}.
	 * @param ctx the parse tree
	 */
	void enterCreate_type(DerbyParser.Create_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#create_type}.
	 * @param ctx the parse tree
	 */
	void exitCreate_type(DerbyParser.Create_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#single_quoted_string}.
	 * @param ctx the parse tree
	 */
	void enterSingle_quoted_string(DerbyParser.Single_quoted_stringContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#single_quoted_string}.
	 * @param ctx the parse tree
	 */
	void exitSingle_quoted_string(DerbyParser.Single_quoted_stringContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#create_view}.
	 * @param ctx the parse tree
	 */
	void enterCreate_view(DerbyParser.Create_viewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#create_view}.
	 * @param ctx the parse tree
	 */
	void exitCreate_view(DerbyParser.Create_viewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(DerbyParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(DerbyParser.QueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#select_expression}.
	 * @param ctx the parse tree
	 */
	void enterSelect_expression(DerbyParser.Select_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#select_expression}.
	 * @param ctx the parse tree
	 */
	void exitSelect_expression(DerbyParser.Select_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#select_item}.
	 * @param ctx the parse tree
	 */
	void enterSelect_item(DerbyParser.Select_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#select_item}.
	 * @param ctx the parse tree
	 */
	void exitSelect_item(DerbyParser.Select_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#simple_column_name}.
	 * @param ctx the parse tree
	 */
	void enterSimple_column_name(DerbyParser.Simple_column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#simple_column_name}.
	 * @param ctx the parse tree
	 */
	void exitSimple_column_name(DerbyParser.Simple_column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#drop_command}.
	 * @param ctx the parse tree
	 */
	void enterDrop_command(DerbyParser.Drop_commandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#drop_command}.
	 * @param ctx the parse tree
	 */
	void exitDrop_command(DerbyParser.Drop_commandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#drop_derby_aggregate}.
	 * @param ctx the parse tree
	 */
	void enterDrop_derby_aggregate(DerbyParser.Drop_derby_aggregateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#drop_derby_aggregate}.
	 * @param ctx the parse tree
	 */
	void exitDrop_derby_aggregate(DerbyParser.Drop_derby_aggregateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#drop_function}.
	 * @param ctx the parse tree
	 */
	void enterDrop_function(DerbyParser.Drop_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#drop_function}.
	 * @param ctx the parse tree
	 */
	void exitDrop_function(DerbyParser.Drop_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#drop_index}.
	 * @param ctx the parse tree
	 */
	void enterDrop_index(DerbyParser.Drop_indexContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#drop_index}.
	 * @param ctx the parse tree
	 */
	void exitDrop_index(DerbyParser.Drop_indexContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#drop_procedure}.
	 * @param ctx the parse tree
	 */
	void enterDrop_procedure(DerbyParser.Drop_procedureContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#drop_procedure}.
	 * @param ctx the parse tree
	 */
	void exitDrop_procedure(DerbyParser.Drop_procedureContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#drop_role}.
	 * @param ctx the parse tree
	 */
	void enterDrop_role(DerbyParser.Drop_roleContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#drop_role}.
	 * @param ctx the parse tree
	 */
	void exitDrop_role(DerbyParser.Drop_roleContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#drop_schema}.
	 * @param ctx the parse tree
	 */
	void enterDrop_schema(DerbyParser.Drop_schemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#drop_schema}.
	 * @param ctx the parse tree
	 */
	void exitDrop_schema(DerbyParser.Drop_schemaContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#drop_sequence}.
	 * @param ctx the parse tree
	 */
	void enterDrop_sequence(DerbyParser.Drop_sequenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#drop_sequence}.
	 * @param ctx the parse tree
	 */
	void exitDrop_sequence(DerbyParser.Drop_sequenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#drop_synonym}.
	 * @param ctx the parse tree
	 */
	void enterDrop_synonym(DerbyParser.Drop_synonymContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#drop_synonym}.
	 * @param ctx the parse tree
	 */
	void exitDrop_synonym(DerbyParser.Drop_synonymContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#drop_table}.
	 * @param ctx the parse tree
	 */
	void enterDrop_table(DerbyParser.Drop_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#drop_table}.
	 * @param ctx the parse tree
	 */
	void exitDrop_table(DerbyParser.Drop_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#drop_trigger}.
	 * @param ctx the parse tree
	 */
	void enterDrop_trigger(DerbyParser.Drop_triggerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#drop_trigger}.
	 * @param ctx the parse tree
	 */
	void exitDrop_trigger(DerbyParser.Drop_triggerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#drop_type}.
	 * @param ctx the parse tree
	 */
	void enterDrop_type(DerbyParser.Drop_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#drop_type}.
	 * @param ctx the parse tree
	 */
	void exitDrop_type(DerbyParser.Drop_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#drop_view}.
	 * @param ctx the parse tree
	 */
	void enterDrop_view(DerbyParser.Drop_viewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#drop_view}.
	 * @param ctx the parse tree
	 */
	void exitDrop_view(DerbyParser.Drop_viewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#string}.
	 * @param ctx the parse tree
	 */
	void enterString(DerbyParser.StringContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#string}.
	 * @param ctx the parse tree
	 */
	void exitString(DerbyParser.StringContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#table_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_name(DerbyParser.Table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#table_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_name(DerbyParser.Table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#schema_name}.
	 * @param ctx the parse tree
	 */
	void enterSchema_name(DerbyParser.Schema_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#schema_name}.
	 * @param ctx the parse tree
	 */
	void exitSchema_name(DerbyParser.Schema_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#role_name}.
	 * @param ctx the parse tree
	 */
	void enterRole_name(DerbyParser.Role_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#role_name}.
	 * @param ctx the parse tree
	 */
	void exitRole_name(DerbyParser.Role_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#aggregate_name}.
	 * @param ctx the parse tree
	 */
	void enterAggregate_name(DerbyParser.Aggregate_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#aggregate_name}.
	 * @param ctx the parse tree
	 */
	void exitAggregate_name(DerbyParser.Aggregate_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#constraint_name}.
	 * @param ctx the parse tree
	 */
	void enterConstraint_name(DerbyParser.Constraint_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#constraint_name}.
	 * @param ctx the parse tree
	 */
	void exitConstraint_name(DerbyParser.Constraint_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#column_name}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name(DerbyParser.Column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#column_name}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name(DerbyParser.Column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#correlation_name}.
	 * @param ctx the parse tree
	 */
	void enterCorrelation_name(DerbyParser.Correlation_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#correlation_name}.
	 * @param ctx the parse tree
	 */
	void exitCorrelation_name(DerbyParser.Correlation_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#function_name}.
	 * @param ctx the parse tree
	 */
	void enterFunction_name(DerbyParser.Function_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#function_name}.
	 * @param ctx the parse tree
	 */
	void exitFunction_name(DerbyParser.Function_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#index_name}.
	 * @param ctx the parse tree
	 */
	void enterIndex_name(DerbyParser.Index_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#index_name}.
	 * @param ctx the parse tree
	 */
	void exitIndex_name(DerbyParser.Index_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#procedure_name}.
	 * @param ctx the parse tree
	 */
	void enterProcedure_name(DerbyParser.Procedure_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#procedure_name}.
	 * @param ctx the parse tree
	 */
	void exitProcedure_name(DerbyParser.Procedure_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#parameter_name}.
	 * @param ctx the parse tree
	 */
	void enterParameter_name(DerbyParser.Parameter_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#parameter_name}.
	 * @param ctx the parse tree
	 */
	void exitParameter_name(DerbyParser.Parameter_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#sequence_name}.
	 * @param ctx the parse tree
	 */
	void enterSequence_name(DerbyParser.Sequence_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#sequence_name}.
	 * @param ctx the parse tree
	 */
	void exitSequence_name(DerbyParser.Sequence_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#synonym_name}.
	 * @param ctx the parse tree
	 */
	void enterSynonym_name(DerbyParser.Synonym_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#synonym_name}.
	 * @param ctx the parse tree
	 */
	void exitSynonym_name(DerbyParser.Synonym_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#trigger_name}.
	 * @param ctx the parse tree
	 */
	void enterTrigger_name(DerbyParser.Trigger_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#trigger_name}.
	 * @param ctx the parse tree
	 */
	void exitTrigger_name(DerbyParser.Trigger_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#type_name}.
	 * @param ctx the parse tree
	 */
	void enterType_name(DerbyParser.Type_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#type_name}.
	 * @param ctx the parse tree
	 */
	void exitType_name(DerbyParser.Type_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#user_name}.
	 * @param ctx the parse tree
	 */
	void enterUser_name(DerbyParser.User_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#user_name}.
	 * @param ctx the parse tree
	 */
	void exitUser_name(DerbyParser.User_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#view_name}.
	 * @param ctx the parse tree
	 */
	void enterView_name(DerbyParser.View_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#view_name}.
	 * @param ctx the parse tree
	 */
	void exitView_name(DerbyParser.View_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#cursor_nName}.
	 * @param ctx the parse tree
	 */
	void enterCursor_nName(DerbyParser.Cursor_nNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#cursor_nName}.
	 * @param ctx the parse tree
	 */
	void exitCursor_nName(DerbyParser.Cursor_nNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#id_}.
	 * @param ctx the parse tree
	 */
	void enterId_(DerbyParser.Id_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#id_}.
	 * @param ctx the parse tree
	 */
	void exitId_(DerbyParser.Id_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#num}.
	 * @param ctx the parse tree
	 */
	void enterNum(DerbyParser.NumContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#num}.
	 * @param ctx the parse tree
	 */
	void exitNum(DerbyParser.NumContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#expr_list}.
	 * @param ctx the parse tree
	 */
	void enterExpr_list(DerbyParser.Expr_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#expr_list}.
	 * @param ctx the parse tree
	 */
	void exitExpr_list(DerbyParser.Expr_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(DerbyParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(DerbyParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#primitive_expression}.
	 * @param ctx the parse tree
	 */
	void enterPrimitive_expression(DerbyParser.Primitive_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#primitive_expression}.
	 * @param ctx the parse tree
	 */
	void exitPrimitive_expression(DerbyParser.Primitive_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#function_call}.
	 * @param ctx the parse tree
	 */
	void enterFunction_call(DerbyParser.Function_callContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#function_call}.
	 * @param ctx the parse tree
	 */
	void exitFunction_call(DerbyParser.Function_callContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(DerbyParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(DerbyParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#sign}.
	 * @param ctx the parse tree
	 */
	void enterSign(DerbyParser.SignContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#sign}.
	 * @param ctx the parse tree
	 */
	void exitSign(DerbyParser.SignContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#case_expression}.
	 * @param ctx the parse tree
	 */
	void enterCase_expression(DerbyParser.Case_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#case_expression}.
	 * @param ctx the parse tree
	 */
	void exitCase_expression(DerbyParser.Case_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#searched_case}.
	 * @param ctx the parse tree
	 */
	void enterSearched_case(DerbyParser.Searched_caseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#searched_case}.
	 * @param ctx the parse tree
	 */
	void exitSearched_case(DerbyParser.Searched_caseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#simple_case}.
	 * @param ctx the parse tree
	 */
	void enterSimple_case(DerbyParser.Simple_caseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#simple_case}.
	 * @param ctx the parse tree
	 */
	void exitSimple_case(DerbyParser.Simple_caseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#extended_case}.
	 * @param ctx the parse tree
	 */
	void enterExtended_case(DerbyParser.Extended_caseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#extended_case}.
	 * @param ctx the parse tree
	 */
	void exitExtended_case(DerbyParser.Extended_caseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#when_operand}.
	 * @param ctx the parse tree
	 */
	void enterWhen_operand(DerbyParser.When_operandContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#when_operand}.
	 * @param ctx the parse tree
	 */
	void exitWhen_operand(DerbyParser.When_operandContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#value_expression}.
	 * @param ctx the parse tree
	 */
	void enterValue_expression(DerbyParser.Value_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#value_expression}.
	 * @param ctx the parse tree
	 */
	void exitValue_expression(DerbyParser.Value_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#standard_built_in_function}.
	 * @param ctx the parse tree
	 */
	void enterStandard_built_in_function(DerbyParser.Standard_built_in_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#standard_built_in_function}.
	 * @param ctx the parse tree
	 */
	void exitStandard_built_in_function(DerbyParser.Standard_built_in_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#aggreagate_built_in_function}.
	 * @param ctx the parse tree
	 */
	void enterAggreagate_built_in_function(DerbyParser.Aggreagate_built_in_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#aggreagate_built_in_function}.
	 * @param ctx the parse tree
	 */
	void exitAggreagate_built_in_function(DerbyParser.Aggreagate_built_in_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#select_statement}.
	 * @param ctx the parse tree
	 */
	void enterSelect_statement(DerbyParser.Select_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#select_statement}.
	 * @param ctx the parse tree
	 */
	void exitSelect_statement(DerbyParser.Select_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#for_update_clause}.
	 * @param ctx the parse tree
	 */
	void enterFor_update_clause(DerbyParser.For_update_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#for_update_clause}.
	 * @param ctx the parse tree
	 */
	void exitFor_update_clause(DerbyParser.For_update_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#all_distinct}.
	 * @param ctx the parse tree
	 */
	void enterAll_distinct(DerbyParser.All_distinctContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#all_distinct}.
	 * @param ctx the parse tree
	 */
	void exitAll_distinct(DerbyParser.All_distinctContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#from_clause}.
	 * @param ctx the parse tree
	 */
	void enterFrom_clause(DerbyParser.From_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#from_clause}.
	 * @param ctx the parse tree
	 */
	void exitFrom_clause(DerbyParser.From_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#table_expression}.
	 * @param ctx the parse tree
	 */
	void enterTable_expression(DerbyParser.Table_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#table_expression}.
	 * @param ctx the parse tree
	 */
	void exitTable_expression(DerbyParser.Table_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#join_operation}.
	 * @param ctx the parse tree
	 */
	void enterJoin_operation(DerbyParser.Join_operationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#join_operation}.
	 * @param ctx the parse tree
	 */
	void exitJoin_operation(DerbyParser.Join_operationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#table_view_or_function_expression}.
	 * @param ctx the parse tree
	 */
	void enterTable_view_or_function_expression(DerbyParser.Table_view_or_function_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#table_view_or_function_expression}.
	 * @param ctx the parse tree
	 */
	void exitTable_view_or_function_expression(DerbyParser.Table_view_or_function_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#using_clause}.
	 * @param ctx the parse tree
	 */
	void enterUsing_clause(DerbyParser.Using_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#using_clause}.
	 * @param ctx the parse tree
	 */
	void exitUsing_clause(DerbyParser.Using_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#correlation_clause}.
	 * @param ctx the parse tree
	 */
	void enterCorrelation_clause(DerbyParser.Correlation_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#correlation_clause}.
	 * @param ctx the parse tree
	 */
	void exitCorrelation_clause(DerbyParser.Correlation_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#table_function_invocation}.
	 * @param ctx the parse tree
	 */
	void enterTable_function_invocation(DerbyParser.Table_function_invocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#table_function_invocation}.
	 * @param ctx the parse tree
	 */
	void exitTable_function_invocation(DerbyParser.Table_function_invocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#function_arg}.
	 * @param ctx the parse tree
	 */
	void enterFunction_arg(DerbyParser.Function_argContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#function_arg}.
	 * @param ctx the parse tree
	 */
	void exitFunction_arg(DerbyParser.Function_argContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#group_by_clause}.
	 * @param ctx the parse tree
	 */
	void enterGroup_by_clause(DerbyParser.Group_by_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#group_by_clause}.
	 * @param ctx the parse tree
	 */
	void exitGroup_by_clause(DerbyParser.Group_by_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#having_clause}.
	 * @param ctx the parse tree
	 */
	void enterHaving_clause(DerbyParser.Having_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#having_clause}.
	 * @param ctx the parse tree
	 */
	void exitHaving_clause(DerbyParser.Having_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#order_by_clause}.
	 * @param ctx the parse tree
	 */
	void enterOrder_by_clause(DerbyParser.Order_by_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#order_by_clause}.
	 * @param ctx the parse tree
	 */
	void exitOrder_by_clause(DerbyParser.Order_by_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#order_by_item}.
	 * @param ctx the parse tree
	 */
	void enterOrder_by_item(DerbyParser.Order_by_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#order_by_item}.
	 * @param ctx the parse tree
	 */
	void exitOrder_by_item(DerbyParser.Order_by_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#asc_desc}.
	 * @param ctx the parse tree
	 */
	void enterAsc_desc(DerbyParser.Asc_descContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#asc_desc}.
	 * @param ctx the parse tree
	 */
	void exitAsc_desc(DerbyParser.Asc_descContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#columnPosition}.
	 * @param ctx the parse tree
	 */
	void enterColumnPosition(DerbyParser.ColumnPositionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#columnPosition}.
	 * @param ctx the parse tree
	 */
	void exitColumnPosition(DerbyParser.ColumnPositionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DerbyParser#values_expression}.
	 * @param ctx the parse tree
	 */
	void enterValues_expression(DerbyParser.Values_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DerbyParser#values_expression}.
	 * @param ctx the parse tree
	 */
	void exitValues_expression(DerbyParser.Values_expressionContext ctx);
}