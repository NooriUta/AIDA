// Generated from C:/AIDA/Dali4/HOUND/Hound/src/main/resources/grammars/sql/postgresql/PostgreSQLParser.g4 by ANTLR 4.13.1
package com.hound.parser.base.grammars.sql.postgresql;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PostgreSQLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PostgreSQLParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#root}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoot(PostgreSQLParser.RootContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#stmtblock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmtblock(PostgreSQLParser.StmtblockContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#stmtmulti}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmtmulti(PostgreSQLParser.StmtmultiContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt(PostgreSQLParser.StmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#callstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallstmt(PostgreSQLParser.CallstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createrolestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreaterolestmt(PostgreSQLParser.CreaterolestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#with_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_(PostgreSQLParser.With_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#optrolelist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptrolelist(PostgreSQLParser.OptrolelistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alteroptrolelist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlteroptrolelist(PostgreSQLParser.AlteroptrolelistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alteroptroleelem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlteroptroleelem(PostgreSQLParser.AlteroptroleelemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createoptroleelem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateoptroleelem(PostgreSQLParser.CreateoptroleelemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createuserstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateuserstmt(PostgreSQLParser.CreateuserstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterrolestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterrolestmt(PostgreSQLParser.AlterrolestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#in_database_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIn_database_(PostgreSQLParser.In_database_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterrolesetstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterrolesetstmt(PostgreSQLParser.AlterrolesetstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#droprolestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDroprolestmt(PostgreSQLParser.DroprolestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#creategroupstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreategroupstmt(PostgreSQLParser.CreategroupstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#altergroupstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAltergroupstmt(PostgreSQLParser.AltergroupstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#add_drop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdd_drop(PostgreSQLParser.Add_dropContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createschemastmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateschemastmt(PostgreSQLParser.CreateschemastmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#optschemaname}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptschemaname(PostgreSQLParser.OptschemanameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#optschemaeltlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptschemaeltlist(PostgreSQLParser.OptschemaeltlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#schema_stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_stmt(PostgreSQLParser.Schema_stmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#variablesetstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariablesetstmt(PostgreSQLParser.VariablesetstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#set_rest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_rest(PostgreSQLParser.Set_restContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#generic_set}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneric_set(PostgreSQLParser.Generic_setContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#set_rest_more}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_rest_more(PostgreSQLParser.Set_rest_moreContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#var_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_name(PostgreSQLParser.Var_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#var_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_list(PostgreSQLParser.Var_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#var_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar_value(PostgreSQLParser.Var_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#iso_level}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIso_level(PostgreSQLParser.Iso_levelContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#boolean_or_string_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolean_or_string_(PostgreSQLParser.Boolean_or_string_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#zone_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitZone_value(PostgreSQLParser.Zone_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#encoding_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEncoding_(PostgreSQLParser.Encoding_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#nonreservedword_or_sconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonreservedword_or_sconst(PostgreSQLParser.Nonreservedword_or_sconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#variableresetstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableresetstmt(PostgreSQLParser.VariableresetstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#reset_rest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReset_rest(PostgreSQLParser.Reset_restContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#generic_reset}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneric_reset(PostgreSQLParser.Generic_resetContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#setresetclause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetresetclause(PostgreSQLParser.SetresetclauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#functionsetresetclause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionsetresetclause(PostgreSQLParser.FunctionsetresetclauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#variableshowstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableshowstmt(PostgreSQLParser.VariableshowstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#constraintssetstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintssetstmt(PostgreSQLParser.ConstraintssetstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#constraints_set_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraints_set_list(PostgreSQLParser.Constraints_set_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#constraints_set_mode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraints_set_mode(PostgreSQLParser.Constraints_set_modeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#checkpointstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheckpointstmt(PostgreSQLParser.CheckpointstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#discardstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDiscardstmt(PostgreSQLParser.DiscardstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#altertablestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAltertablestmt(PostgreSQLParser.AltertablestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alter_table_cmds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_cmds(PostgreSQLParser.Alter_table_cmdsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#partition_cmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_cmd(PostgreSQLParser.Partition_cmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#index_partition_cmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_partition_cmd(PostgreSQLParser.Index_partition_cmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alter_table_cmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table_cmd(PostgreSQLParser.Alter_table_cmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alter_column_default}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_column_default(PostgreSQLParser.Alter_column_defaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#drop_behavior_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_behavior_(PostgreSQLParser.Drop_behavior_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#collate_clause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollate_clause_(PostgreSQLParser.Collate_clause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alter_using}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_using(PostgreSQLParser.Alter_usingContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#replica_identity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplica_identity(PostgreSQLParser.Replica_identityContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#reloptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReloptions(PostgreSQLParser.ReloptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#reloptions_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReloptions_(PostgreSQLParser.Reloptions_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#reloption_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReloption_list(PostgreSQLParser.Reloption_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#reloption_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReloption_elem(PostgreSQLParser.Reloption_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alter_identity_column_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_identity_column_option_list(PostgreSQLParser.Alter_identity_column_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alter_identity_column_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_identity_column_option(PostgreSQLParser.Alter_identity_column_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#partitionboundspec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionboundspec(PostgreSQLParser.PartitionboundspecContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#hash_partbound_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHash_partbound_elem(PostgreSQLParser.Hash_partbound_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#hash_partbound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHash_partbound(PostgreSQLParser.Hash_partboundContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#altercompositetypestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAltercompositetypestmt(PostgreSQLParser.AltercompositetypestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alter_type_cmds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_type_cmds(PostgreSQLParser.Alter_type_cmdsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alter_type_cmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_type_cmd(PostgreSQLParser.Alter_type_cmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#closeportalstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCloseportalstmt(PostgreSQLParser.CloseportalstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#copystmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopystmt(PostgreSQLParser.CopystmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#copy_from}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_from(PostgreSQLParser.Copy_fromContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#program_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram_(PostgreSQLParser.Program_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#copy_file_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_file_name(PostgreSQLParser.Copy_file_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#copy_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_options(PostgreSQLParser.Copy_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#copy_opt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_opt_list(PostgreSQLParser.Copy_opt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#copy_opt_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_opt_item(PostgreSQLParser.Copy_opt_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#binary_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinary_(PostgreSQLParser.Binary_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#copy_delimiter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_delimiter(PostgreSQLParser.Copy_delimiterContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#using_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUsing_(PostgreSQLParser.Using_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#copy_generic_opt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_generic_opt_list(PostgreSQLParser.Copy_generic_opt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#copy_generic_opt_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_generic_opt_elem(PostgreSQLParser.Copy_generic_opt_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#copy_generic_opt_arg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_generic_opt_arg(PostgreSQLParser.Copy_generic_opt_argContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#copy_generic_opt_arg_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_generic_opt_arg_list(PostgreSQLParser.Copy_generic_opt_arg_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#copy_generic_opt_arg_list_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_generic_opt_arg_list_item(PostgreSQLParser.Copy_generic_opt_arg_list_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatestmt(PostgreSQLParser.CreatestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#opttemp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpttemp(PostgreSQLParser.OpttempContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#opttableelementlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpttableelementlist(PostgreSQLParser.OpttableelementlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#opttypedtableelementlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpttypedtableelementlist(PostgreSQLParser.OpttypedtableelementlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#tableelementlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableelementlist(PostgreSQLParser.TableelementlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#typedtableelementlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypedtableelementlist(PostgreSQLParser.TypedtableelementlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#tableelement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableelement(PostgreSQLParser.TableelementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#typedtableelement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypedtableelement(PostgreSQLParser.TypedtableelementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#columnDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnDef(PostgreSQLParser.ColumnDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#columnOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnOptions(PostgreSQLParser.ColumnOptionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#colquallist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColquallist(PostgreSQLParser.ColquallistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#colconstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColconstraint(PostgreSQLParser.ColconstraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#colconstraintelem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColconstraintelem(PostgreSQLParser.ColconstraintelemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#generated_when}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenerated_when(PostgreSQLParser.Generated_whenContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#constraintattr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintattr(PostgreSQLParser.ConstraintattrContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#tablelikeclause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablelikeclause(PostgreSQLParser.TablelikeclauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#tablelikeoptionlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablelikeoptionlist(PostgreSQLParser.TablelikeoptionlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#tablelikeoption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablelikeoption(PostgreSQLParser.TablelikeoptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#tableconstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableconstraint(PostgreSQLParser.TableconstraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#constraintelem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintelem(PostgreSQLParser.ConstraintelemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#no_inherit_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNo_inherit_(PostgreSQLParser.No_inherit_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#column_list_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_list_(PostgreSQLParser.Column_list_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#columnlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnlist(PostgreSQLParser.ColumnlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#columnElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnElem(PostgreSQLParser.ColumnElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#c_include_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitC_include_(PostgreSQLParser.C_include_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#key_match}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKey_match(PostgreSQLParser.Key_matchContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#exclusionconstraintlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExclusionconstraintlist(PostgreSQLParser.ExclusionconstraintlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#exclusionconstraintelem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExclusionconstraintelem(PostgreSQLParser.ExclusionconstraintelemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#exclusionwhereclause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExclusionwhereclause(PostgreSQLParser.ExclusionwhereclauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#key_actions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKey_actions(PostgreSQLParser.Key_actionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#key_update}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKey_update(PostgreSQLParser.Key_updateContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#key_delete}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKey_delete(PostgreSQLParser.Key_deleteContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#key_action}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKey_action(PostgreSQLParser.Key_actionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#optinherit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptinherit(PostgreSQLParser.OptinheritContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#optpartitionspec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptpartitionspec(PostgreSQLParser.OptpartitionspecContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#partitionspec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionspec(PostgreSQLParser.PartitionspecContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#part_params}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPart_params(PostgreSQLParser.Part_paramsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#part_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPart_elem(PostgreSQLParser.Part_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#table_access_method_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_access_method_clause(PostgreSQLParser.Table_access_method_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#optwith}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptwith(PostgreSQLParser.OptwithContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#oncommitoption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOncommitoption(PostgreSQLParser.OncommitoptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#opttablespace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpttablespace(PostgreSQLParser.OpttablespaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#optconstablespace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptconstablespace(PostgreSQLParser.OptconstablespaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#existingindex}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExistingindex(PostgreSQLParser.ExistingindexContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createstatsstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatestatsstmt(PostgreSQLParser.CreatestatsstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterstatsstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterstatsstmt(PostgreSQLParser.AlterstatsstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createasstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateasstmt(PostgreSQLParser.CreateasstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#create_as_target}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_as_target(PostgreSQLParser.Create_as_targetContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#with_data_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_data_(PostgreSQLParser.With_data_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#creatematviewstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatematviewstmt(PostgreSQLParser.CreatematviewstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#create_mv_target}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_mv_target(PostgreSQLParser.Create_mv_targetContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#optnolog}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptnolog(PostgreSQLParser.OptnologContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#refreshmatviewstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefreshmatviewstmt(PostgreSQLParser.RefreshmatviewstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createseqstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateseqstmt(PostgreSQLParser.CreateseqstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterseqstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterseqstmt(PostgreSQLParser.AlterseqstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#optseqoptlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptseqoptlist(PostgreSQLParser.OptseqoptlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#optparenthesizedseqoptlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptparenthesizedseqoptlist(PostgreSQLParser.OptparenthesizedseqoptlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#seqoptlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeqoptlist(PostgreSQLParser.SeqoptlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#seqoptelem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeqoptelem(PostgreSQLParser.SeqoptelemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#by_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBy_(PostgreSQLParser.By_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#numericonly}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericonly(PostgreSQLParser.NumericonlyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#numericonly_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericonly_list(PostgreSQLParser.Numericonly_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createplangstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateplangstmt(PostgreSQLParser.CreateplangstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#trusted_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrusted_(PostgreSQLParser.Trusted_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#handler_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandler_name(PostgreSQLParser.Handler_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#inline_handler_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInline_handler_(PostgreSQLParser.Inline_handler_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#validator_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValidator_clause(PostgreSQLParser.Validator_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#validator_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValidator_(PostgreSQLParser.Validator_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#procedural_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedural_(PostgreSQLParser.Procedural_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createtablespacestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatetablespacestmt(PostgreSQLParser.CreatetablespacestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#opttablespaceowner}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpttablespaceowner(PostgreSQLParser.OpttablespaceownerContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#droptablespacestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDroptablespacestmt(PostgreSQLParser.DroptablespacestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createextensionstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateextensionstmt(PostgreSQLParser.CreateextensionstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#create_extension_opt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_extension_opt_list(PostgreSQLParser.Create_extension_opt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#create_extension_opt_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_extension_opt_item(PostgreSQLParser.Create_extension_opt_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterextensionstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterextensionstmt(PostgreSQLParser.AlterextensionstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alter_extension_opt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_extension_opt_list(PostgreSQLParser.Alter_extension_opt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alter_extension_opt_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_extension_opt_item(PostgreSQLParser.Alter_extension_opt_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterextensioncontentsstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterextensioncontentsstmt(PostgreSQLParser.AlterextensioncontentsstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createfdwstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatefdwstmt(PostgreSQLParser.CreatefdwstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#fdw_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFdw_option(PostgreSQLParser.Fdw_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#fdw_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFdw_options(PostgreSQLParser.Fdw_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#fdw_options_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFdw_options_(PostgreSQLParser.Fdw_options_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterfdwstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterfdwstmt(PostgreSQLParser.AlterfdwstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#create_generic_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_generic_options(PostgreSQLParser.Create_generic_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#generic_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneric_option_list(PostgreSQLParser.Generic_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alter_generic_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_generic_options(PostgreSQLParser.Alter_generic_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alter_generic_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_generic_option_list(PostgreSQLParser.Alter_generic_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alter_generic_option_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_generic_option_elem(PostgreSQLParser.Alter_generic_option_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#generic_option_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneric_option_elem(PostgreSQLParser.Generic_option_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#generic_option_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneric_option_name(PostgreSQLParser.Generic_option_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#generic_option_arg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneric_option_arg(PostgreSQLParser.Generic_option_argContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createforeignserverstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateforeignserverstmt(PostgreSQLParser.CreateforeignserverstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#type_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_(PostgreSQLParser.Type_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#foreign_server_version}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForeign_server_version(PostgreSQLParser.Foreign_server_versionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#foreign_server_version_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForeign_server_version_(PostgreSQLParser.Foreign_server_version_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterforeignserverstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterforeignserverstmt(PostgreSQLParser.AlterforeignserverstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createforeigntablestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateforeigntablestmt(PostgreSQLParser.CreateforeigntablestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#importforeignschemastmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportforeignschemastmt(PostgreSQLParser.ImportforeignschemastmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#import_qualification_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImport_qualification_type(PostgreSQLParser.Import_qualification_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#import_qualification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImport_qualification(PostgreSQLParser.Import_qualificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createusermappingstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateusermappingstmt(PostgreSQLParser.CreateusermappingstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#auth_ident}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAuth_ident(PostgreSQLParser.Auth_identContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#dropusermappingstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropusermappingstmt(PostgreSQLParser.DropusermappingstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterusermappingstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterusermappingstmt(PostgreSQLParser.AlterusermappingstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createpolicystmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatepolicystmt(PostgreSQLParser.CreatepolicystmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterpolicystmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterpolicystmt(PostgreSQLParser.AlterpolicystmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#rowsecurityoptionalexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowsecurityoptionalexpr(PostgreSQLParser.RowsecurityoptionalexprContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#rowsecurityoptionalwithcheck}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowsecurityoptionalwithcheck(PostgreSQLParser.RowsecurityoptionalwithcheckContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#rowsecuritydefaulttorole}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowsecuritydefaulttorole(PostgreSQLParser.RowsecuritydefaulttoroleContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#rowsecurityoptionaltorole}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowsecurityoptionaltorole(PostgreSQLParser.RowsecurityoptionaltoroleContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#rowsecuritydefaultpermissive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowsecuritydefaultpermissive(PostgreSQLParser.RowsecuritydefaultpermissiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#rowsecuritydefaultforcmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowsecuritydefaultforcmd(PostgreSQLParser.RowsecuritydefaultforcmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#row_security_cmd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow_security_cmd(PostgreSQLParser.Row_security_cmdContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createamstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateamstmt(PostgreSQLParser.CreateamstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#am_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAm_type(PostgreSQLParser.Am_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createtrigstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatetrigstmt(PostgreSQLParser.CreatetrigstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#triggeractiontime}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggeractiontime(PostgreSQLParser.TriggeractiontimeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#triggerevents}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerevents(PostgreSQLParser.TriggereventsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#triggeroneevent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggeroneevent(PostgreSQLParser.TriggeroneeventContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#triggerreferencing}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerreferencing(PostgreSQLParser.TriggerreferencingContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#triggertransitions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggertransitions(PostgreSQLParser.TriggertransitionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#triggertransition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggertransition(PostgreSQLParser.TriggertransitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#transitionoldornew}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransitionoldornew(PostgreSQLParser.TransitionoldornewContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#transitionrowortable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransitionrowortable(PostgreSQLParser.TransitionrowortableContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#transitionrelname}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransitionrelname(PostgreSQLParser.TransitionrelnameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#triggerforspec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerforspec(PostgreSQLParser.TriggerforspecContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#triggerforopteach}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerforopteach(PostgreSQLParser.TriggerforopteachContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#triggerfortype}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerfortype(PostgreSQLParser.TriggerfortypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#triggerwhen}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerwhen(PostgreSQLParser.TriggerwhenContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#function_or_procedure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_or_procedure(PostgreSQLParser.Function_or_procedureContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#triggerfuncargs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerfuncargs(PostgreSQLParser.TriggerfuncargsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#triggerfuncarg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerfuncarg(PostgreSQLParser.TriggerfuncargContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#optconstrfromtable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOptconstrfromtable(PostgreSQLParser.OptconstrfromtableContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#constraintattributespec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintattributespec(PostgreSQLParser.ConstraintattributespecContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#constraintattributeElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstraintattributeElem(PostgreSQLParser.ConstraintattributeElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createeventtrigstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateeventtrigstmt(PostgreSQLParser.CreateeventtrigstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#event_trigger_when_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvent_trigger_when_list(PostgreSQLParser.Event_trigger_when_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#event_trigger_when_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvent_trigger_when_item(PostgreSQLParser.Event_trigger_when_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#event_trigger_value_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvent_trigger_value_list(PostgreSQLParser.Event_trigger_value_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#altereventtrigstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAltereventtrigstmt(PostgreSQLParser.AltereventtrigstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#enable_trigger}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnable_trigger(PostgreSQLParser.Enable_triggerContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createassertionstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateassertionstmt(PostgreSQLParser.CreateassertionstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#definestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinestmt(PostgreSQLParser.DefinestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinition(PostgreSQLParser.DefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#def_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDef_list(PostgreSQLParser.Def_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#def_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDef_elem(PostgreSQLParser.Def_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#def_arg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDef_arg(PostgreSQLParser.Def_argContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#old_aggr_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOld_aggr_definition(PostgreSQLParser.Old_aggr_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#old_aggr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOld_aggr_list(PostgreSQLParser.Old_aggr_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#old_aggr_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOld_aggr_elem(PostgreSQLParser.Old_aggr_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#enum_val_list_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnum_val_list_(PostgreSQLParser.Enum_val_list_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#enum_val_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnum_val_list(PostgreSQLParser.Enum_val_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterenumstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterenumstmt(PostgreSQLParser.AlterenumstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#if_not_exists_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_not_exists_(PostgreSQLParser.If_not_exists_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createopclassstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateopclassstmt(PostgreSQLParser.CreateopclassstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#opclass_item_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpclass_item_list(PostgreSQLParser.Opclass_item_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#opclass_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpclass_item(PostgreSQLParser.Opclass_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#default_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefault_(PostgreSQLParser.Default_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#opfamily_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpfamily_(PostgreSQLParser.Opfamily_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#opclass_purpose}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpclass_purpose(PostgreSQLParser.Opclass_purposeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#recheck_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecheck_(PostgreSQLParser.Recheck_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createopfamilystmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateopfamilystmt(PostgreSQLParser.CreateopfamilystmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alteropfamilystmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlteropfamilystmt(PostgreSQLParser.AlteropfamilystmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#opclass_drop_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpclass_drop_list(PostgreSQLParser.Opclass_drop_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#opclass_drop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpclass_drop(PostgreSQLParser.Opclass_dropContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#dropopclassstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropopclassstmt(PostgreSQLParser.DropopclassstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#dropopfamilystmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropopfamilystmt(PostgreSQLParser.DropopfamilystmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#dropownedstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropownedstmt(PostgreSQLParser.DropownedstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#reassignownedstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReassignownedstmt(PostgreSQLParser.ReassignownedstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#dropstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropstmt(PostgreSQLParser.DropstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#object_type_any_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject_type_any_name(PostgreSQLParser.Object_type_any_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#object_type_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject_type_name(PostgreSQLParser.Object_type_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#drop_type_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_type_name(PostgreSQLParser.Drop_type_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#object_type_name_on_any_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject_type_name_on_any_name(PostgreSQLParser.Object_type_name_on_any_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#any_name_list_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAny_name_list_(PostgreSQLParser.Any_name_list_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#any_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAny_name(PostgreSQLParser.Any_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#attrs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttrs(PostgreSQLParser.AttrsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#type_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_name_list(PostgreSQLParser.Type_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#truncatestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTruncatestmt(PostgreSQLParser.TruncatestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#restart_seqs_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRestart_seqs_(PostgreSQLParser.Restart_seqs_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#commentstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentstmt(PostgreSQLParser.CommentstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#comment_text}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment_text(PostgreSQLParser.Comment_textContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#seclabelstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSeclabelstmt(PostgreSQLParser.SeclabelstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#provider_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProvider_(PostgreSQLParser.Provider_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#security_label}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSecurity_label(PostgreSQLParser.Security_labelContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#fetchstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetchstmt(PostgreSQLParser.FetchstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#fetch_args}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetch_args(PostgreSQLParser.Fetch_argsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#from_in}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrom_in(PostgreSQLParser.From_inContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#from_in_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrom_in_(PostgreSQLParser.From_in_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#grantstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantstmt(PostgreSQLParser.GrantstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#revokestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevokestmt(PostgreSQLParser.RevokestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#privileges}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivileges(PostgreSQLParser.PrivilegesContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#privilege_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilege_list(PostgreSQLParser.Privilege_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#privilege}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilege(PostgreSQLParser.PrivilegeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#privilege_target}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilege_target(PostgreSQLParser.Privilege_targetContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#grantee_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantee_list(PostgreSQLParser.Grantee_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#grantee}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantee(PostgreSQLParser.GranteeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#grant_grant_option_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_grant_option_(PostgreSQLParser.Grant_grant_option_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#grantrolestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrantrolestmt(PostgreSQLParser.GrantrolestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#revokerolestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevokerolestmt(PostgreSQLParser.RevokerolestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#grant_admin_option_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_admin_option_(PostgreSQLParser.Grant_admin_option_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#granted_by_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGranted_by_(PostgreSQLParser.Granted_by_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterdefaultprivilegesstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterdefaultprivilegesstmt(PostgreSQLParser.AlterdefaultprivilegesstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#defacloptionlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefacloptionlist(PostgreSQLParser.DefacloptionlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#defacloption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefacloption(PostgreSQLParser.DefacloptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#defaclaction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaclaction(PostgreSQLParser.DefaclactionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#defacl_privilege_target}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefacl_privilege_target(PostgreSQLParser.Defacl_privilege_targetContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#indexstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexstmt(PostgreSQLParser.IndexstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#unique_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnique_(PostgreSQLParser.Unique_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#nulls_distinct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNulls_distinct(PostgreSQLParser.Nulls_distinctContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#single_name_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingle_name_(PostgreSQLParser.Single_name_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#concurrently_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConcurrently_(PostgreSQLParser.Concurrently_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#index_name_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_name_(PostgreSQLParser.Index_name_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#access_method_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAccess_method_clause(PostgreSQLParser.Access_method_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#index_params}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_params(PostgreSQLParser.Index_paramsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#index_elem_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_elem_options(PostgreSQLParser.Index_elem_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#index_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_elem(PostgreSQLParser.Index_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#include_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInclude_(PostgreSQLParser.Include_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#index_including_params}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex_including_params(PostgreSQLParser.Index_including_paramsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#collate_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCollate_(PostgreSQLParser.Collate_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#class_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClass_(PostgreSQLParser.Class_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#asc_desc_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsc_desc_(PostgreSQLParser.Asc_desc_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#nulls_order_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNulls_order_(PostgreSQLParser.Nulls_order_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createfunctionstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatefunctionstmt(PostgreSQLParser.CreatefunctionstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#or_replace_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr_replace_(PostgreSQLParser.Or_replace_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_args}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_args(PostgreSQLParser.Func_argsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_args_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_args_list(PostgreSQLParser.Func_args_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#function_with_argtypes_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_with_argtypes_list(PostgreSQLParser.Function_with_argtypes_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#function_with_argtypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_with_argtypes(PostgreSQLParser.Function_with_argtypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_args_with_defaults}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_args_with_defaults(PostgreSQLParser.Func_args_with_defaultsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_args_with_defaults_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_args_with_defaults_list(PostgreSQLParser.Func_args_with_defaults_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_arg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_arg(PostgreSQLParser.Func_argContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#arg_class}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArg_class(PostgreSQLParser.Arg_classContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#param_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_name(PostgreSQLParser.Param_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_return}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_return(PostgreSQLParser.Func_returnContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_type(PostgreSQLParser.Func_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_arg_with_default}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_arg_with_default(PostgreSQLParser.Func_arg_with_defaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#aggr_arg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggr_arg(PostgreSQLParser.Aggr_argContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#aggr_args}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggr_args(PostgreSQLParser.Aggr_argsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#aggr_args_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggr_args_list(PostgreSQLParser.Aggr_args_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#aggregate_with_argtypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregate_with_argtypes(PostgreSQLParser.Aggregate_with_argtypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#aggregate_with_argtypes_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregate_with_argtypes_list(PostgreSQLParser.Aggregate_with_argtypes_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createfunc_opt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatefunc_opt_list(PostgreSQLParser.Createfunc_opt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#common_func_opt_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommon_func_opt_item(PostgreSQLParser.Common_func_opt_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createfunc_opt_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatefunc_opt_item(PostgreSQLParser.Createfunc_opt_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_as}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_as(PostgreSQLParser.Func_asContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#transform_type_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_type_list(PostgreSQLParser.Transform_type_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#definition_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinition_(PostgreSQLParser.Definition_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#table_func_column}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_func_column(PostgreSQLParser.Table_func_columnContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#table_func_column_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_func_column_list(PostgreSQLParser.Table_func_column_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterfunctionstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterfunctionstmt(PostgreSQLParser.AlterfunctionstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterfunc_opt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterfunc_opt_list(PostgreSQLParser.Alterfunc_opt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#restrict_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRestrict_(PostgreSQLParser.Restrict_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#removefuncstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemovefuncstmt(PostgreSQLParser.RemovefuncstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#removeaggrstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemoveaggrstmt(PostgreSQLParser.RemoveaggrstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#removeoperstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemoveoperstmt(PostgreSQLParser.RemoveoperstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#oper_argtypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOper_argtypes(PostgreSQLParser.Oper_argtypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#any_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAny_operator(PostgreSQLParser.Any_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#operator_with_argtypes_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperator_with_argtypes_list(PostgreSQLParser.Operator_with_argtypes_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#operator_with_argtypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperator_with_argtypes(PostgreSQLParser.Operator_with_argtypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#dostmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDostmt(PostgreSQLParser.DostmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#dostmt_opt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDostmt_opt_list(PostgreSQLParser.Dostmt_opt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#dostmt_opt_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDostmt_opt_item(PostgreSQLParser.Dostmt_opt_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createcaststmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatecaststmt(PostgreSQLParser.CreatecaststmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#cast_context}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCast_context(PostgreSQLParser.Cast_contextContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#dropcaststmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropcaststmt(PostgreSQLParser.DropcaststmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#if_exists_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_exists_(PostgreSQLParser.If_exists_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createtransformstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatetransformstmt(PostgreSQLParser.CreatetransformstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#transform_element_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransform_element_list(PostgreSQLParser.Transform_element_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#droptransformstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDroptransformstmt(PostgreSQLParser.DroptransformstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#reindexstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReindexstmt(PostgreSQLParser.ReindexstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#reindex_target_relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReindex_target_relation(PostgreSQLParser.Reindex_target_relationContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#reindex_target_all}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReindex_target_all(PostgreSQLParser.Reindex_target_allContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#reindex_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReindex_option_list(PostgreSQLParser.Reindex_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#altertblspcstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAltertblspcstmt(PostgreSQLParser.AltertblspcstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#renamestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRenamestmt(PostgreSQLParser.RenamestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#column_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_(PostgreSQLParser.Column_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#set_data_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_data_(PostgreSQLParser.Set_data_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterobjectdependsstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterobjectdependsstmt(PostgreSQLParser.AlterobjectdependsstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#no_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNo_(PostgreSQLParser.No_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterobjectschemastmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterobjectschemastmt(PostgreSQLParser.AlterobjectschemastmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alteroperatorstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlteroperatorstmt(PostgreSQLParser.AlteroperatorstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#operator_def_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperator_def_list(PostgreSQLParser.Operator_def_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#operator_def_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperator_def_elem(PostgreSQLParser.Operator_def_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#operator_def_arg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperator_def_arg(PostgreSQLParser.Operator_def_argContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#altertypestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAltertypestmt(PostgreSQLParser.AltertypestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterownerstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterownerstmt(PostgreSQLParser.AlterownerstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createpublicationstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatepublicationstmt(PostgreSQLParser.CreatepublicationstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#publication_for_tables_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPublication_for_tables_(PostgreSQLParser.Publication_for_tables_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#publication_for_tables}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPublication_for_tables(PostgreSQLParser.Publication_for_tablesContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterpublicationstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterpublicationstmt(PostgreSQLParser.AlterpublicationstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createsubscriptionstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatesubscriptionstmt(PostgreSQLParser.CreatesubscriptionstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#publication_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPublication_name_list(PostgreSQLParser.Publication_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#publication_name_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPublication_name_item(PostgreSQLParser.Publication_name_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#altersubscriptionstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAltersubscriptionstmt(PostgreSQLParser.AltersubscriptionstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#dropsubscriptionstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropsubscriptionstmt(PostgreSQLParser.DropsubscriptionstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#rulestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRulestmt(PostgreSQLParser.RulestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#ruleactionlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRuleactionlist(PostgreSQLParser.RuleactionlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#ruleactionmulti}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRuleactionmulti(PostgreSQLParser.RuleactionmultiContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#ruleactionstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRuleactionstmt(PostgreSQLParser.RuleactionstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#ruleactionstmtOrEmpty}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRuleactionstmtOrEmpty(PostgreSQLParser.RuleactionstmtOrEmptyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#event}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvent(PostgreSQLParser.EventContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#instead_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstead_(PostgreSQLParser.Instead_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#notifystmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotifystmt(PostgreSQLParser.NotifystmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#notify_payload}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotify_payload(PostgreSQLParser.Notify_payloadContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#listenstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListenstmt(PostgreSQLParser.ListenstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#unlistenstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnlistenstmt(PostgreSQLParser.UnlistenstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#transactionstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransactionstmt(PostgreSQLParser.TransactionstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#transaction_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransaction_(PostgreSQLParser.Transaction_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#transaction_mode_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransaction_mode_item(PostgreSQLParser.Transaction_mode_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#transaction_mode_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransaction_mode_list(PostgreSQLParser.Transaction_mode_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#transaction_mode_list_or_empty}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransaction_mode_list_or_empty(PostgreSQLParser.Transaction_mode_list_or_emptyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#transaction_chain_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransaction_chain_(PostgreSQLParser.Transaction_chain_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#viewstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitViewstmt(PostgreSQLParser.ViewstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#check_option_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheck_option_(PostgreSQLParser.Check_option_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#loadstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoadstmt(PostgreSQLParser.LoadstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createdbstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatedbstmt(PostgreSQLParser.CreatedbstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createdb_opt_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatedb_opt_list(PostgreSQLParser.Createdb_opt_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createdb_opt_items}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatedb_opt_items(PostgreSQLParser.Createdb_opt_itemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createdb_opt_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatedb_opt_item(PostgreSQLParser.Createdb_opt_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createdb_opt_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatedb_opt_name(PostgreSQLParser.Createdb_opt_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#equal_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqual_(PostgreSQLParser.Equal_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterdatabasestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterdatabasestmt(PostgreSQLParser.AlterdatabasestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterdatabasesetstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterdatabasesetstmt(PostgreSQLParser.AlterdatabasesetstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#dropdbstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropdbstmt(PostgreSQLParser.DropdbstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#drop_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_option_list(PostgreSQLParser.Drop_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#drop_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_option(PostgreSQLParser.Drop_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#altercollationstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAltercollationstmt(PostgreSQLParser.AltercollationstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#altersystemstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAltersystemstmt(PostgreSQLParser.AltersystemstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createdomainstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatedomainstmt(PostgreSQLParser.CreatedomainstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alterdomainstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterdomainstmt(PostgreSQLParser.AlterdomainstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#as_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAs_(PostgreSQLParser.As_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#altertsdictionarystmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAltertsdictionarystmt(PostgreSQLParser.AltertsdictionarystmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#altertsconfigurationstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAltertsconfigurationstmt(PostgreSQLParser.AltertsconfigurationstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#any_with}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAny_with(PostgreSQLParser.Any_withContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#createconversionstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateconversionstmt(PostgreSQLParser.CreateconversionstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#clusterstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClusterstmt(PostgreSQLParser.ClusterstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#cluster_index_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCluster_index_specification(PostgreSQLParser.Cluster_index_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#vacuumstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVacuumstmt(PostgreSQLParser.VacuumstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#analyzestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnalyzestmt(PostgreSQLParser.AnalyzestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#utility_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUtility_option_list(PostgreSQLParser.Utility_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#vac_analyze_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVac_analyze_option_list(PostgreSQLParser.Vac_analyze_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#analyze_keyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnalyze_keyword(PostgreSQLParser.Analyze_keywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#utility_option_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUtility_option_elem(PostgreSQLParser.Utility_option_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#utility_option_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUtility_option_name(PostgreSQLParser.Utility_option_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#utility_option_arg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUtility_option_arg(PostgreSQLParser.Utility_option_argContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#vac_analyze_option_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVac_analyze_option_elem(PostgreSQLParser.Vac_analyze_option_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#vac_analyze_option_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVac_analyze_option_name(PostgreSQLParser.Vac_analyze_option_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#vac_analyze_option_arg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVac_analyze_option_arg(PostgreSQLParser.Vac_analyze_option_argContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#analyze_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnalyze_(PostgreSQLParser.Analyze_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#verbose_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVerbose_(PostgreSQLParser.Verbose_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#full_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFull_(PostgreSQLParser.Full_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#freeze_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFreeze_(PostgreSQLParser.Freeze_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#name_list_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName_list_(PostgreSQLParser.Name_list_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#vacuum_relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVacuum_relation(PostgreSQLParser.Vacuum_relationContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#vacuum_relation_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVacuum_relation_list(PostgreSQLParser.Vacuum_relation_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#vacuum_relation_list_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVacuum_relation_list_(PostgreSQLParser.Vacuum_relation_list_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#explainstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplainstmt(PostgreSQLParser.ExplainstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#explainablestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplainablestmt(PostgreSQLParser.ExplainablestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#explain_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplain_option_list(PostgreSQLParser.Explain_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#explain_option_elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplain_option_elem(PostgreSQLParser.Explain_option_elemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#explain_option_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplain_option_name(PostgreSQLParser.Explain_option_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#explain_option_arg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplain_option_arg(PostgreSQLParser.Explain_option_argContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#preparestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreparestmt(PostgreSQLParser.PreparestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#prep_type_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrep_type_clause(PostgreSQLParser.Prep_type_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#preparablestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPreparablestmt(PostgreSQLParser.PreparablestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#executestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecutestmt(PostgreSQLParser.ExecutestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#execute_param_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecute_param_clause(PostgreSQLParser.Execute_param_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#deallocatestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeallocatestmt(PostgreSQLParser.DeallocatestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#insertstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertstmt(PostgreSQLParser.InsertstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#insert_target}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_target(PostgreSQLParser.Insert_targetContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#insert_rest}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_rest(PostgreSQLParser.Insert_restContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#override_kind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverride_kind(PostgreSQLParser.Override_kindContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#insert_column_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_column_list(PostgreSQLParser.Insert_column_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#insert_column_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_column_item(PostgreSQLParser.Insert_column_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#on_conflict_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOn_conflict_(PostgreSQLParser.On_conflict_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#conf_expr_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConf_expr_(PostgreSQLParser.Conf_expr_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#returning_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturning_clause(PostgreSQLParser.Returning_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#mergestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMergestmt(PostgreSQLParser.MergestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#merge_insert_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMerge_insert_clause(PostgreSQLParser.Merge_insert_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#merge_update_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMerge_update_clause(PostgreSQLParser.Merge_update_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#merge_delete_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMerge_delete_clause(PostgreSQLParser.Merge_delete_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#deletestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeletestmt(PostgreSQLParser.DeletestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#using_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUsing_clause(PostgreSQLParser.Using_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#lockstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLockstmt(PostgreSQLParser.LockstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#lock_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLock_(PostgreSQLParser.Lock_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#lock_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLock_type(PostgreSQLParser.Lock_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#nowait_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNowait_(PostgreSQLParser.Nowait_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#nowait_or_skip_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNowait_or_skip_(PostgreSQLParser.Nowait_or_skip_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#updatestmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdatestmt(PostgreSQLParser.UpdatestmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#set_clause_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_clause_list(PostgreSQLParser.Set_clause_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#set_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_clause(PostgreSQLParser.Set_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#set_target}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_target(PostgreSQLParser.Set_targetContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#set_target_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_target_list(PostgreSQLParser.Set_target_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#declarecursorstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarecursorstmt(PostgreSQLParser.DeclarecursorstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#cursor_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursor_name(PostgreSQLParser.Cursor_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#cursor_options}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCursor_options(PostgreSQLParser.Cursor_optionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#hold_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHold_(PostgreSQLParser.Hold_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#selectstmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectstmt(PostgreSQLParser.SelectstmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#select_with_parens}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_with_parens(PostgreSQLParser.Select_with_parensContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#select_no_parens}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_no_parens(PostgreSQLParser.Select_no_parensContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#select_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_clause(PostgreSQLParser.Select_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#simple_select_intersect}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_select_intersect(PostgreSQLParser.Simple_select_intersectContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#simple_select_pramary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimple_select_pramary(PostgreSQLParser.Simple_select_pramaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#with_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_clause(PostgreSQLParser.With_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#cte_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCte_list(PostgreSQLParser.Cte_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#common_table_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommon_table_expr(PostgreSQLParser.Common_table_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#materialized_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMaterialized_(PostgreSQLParser.Materialized_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#with_clause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_clause_(PostgreSQLParser.With_clause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#into_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInto_clause(PostgreSQLParser.Into_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#strict_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStrict_(PostgreSQLParser.Strict_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#opttempTableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpttempTableName(PostgreSQLParser.OpttempTableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#table_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_(PostgreSQLParser.Table_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#all_or_distinct}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAll_or_distinct(PostgreSQLParser.All_or_distinctContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#distinct_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistinct_clause(PostgreSQLParser.Distinct_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#all_clause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAll_clause_(PostgreSQLParser.All_clause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#sort_clause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSort_clause_(PostgreSQLParser.Sort_clause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#sort_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSort_clause(PostgreSQLParser.Sort_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#sortby_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortby_list(PostgreSQLParser.Sortby_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#sortby}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSortby(PostgreSQLParser.SortbyContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#select_limit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_limit(PostgreSQLParser.Select_limitContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#select_limit_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_limit_(PostgreSQLParser.Select_limit_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#limit_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimit_clause(PostgreSQLParser.Limit_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#offset_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOffset_clause(PostgreSQLParser.Offset_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#select_limit_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_limit_value(PostgreSQLParser.Select_limit_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#select_offset_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_offset_value(PostgreSQLParser.Select_offset_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#select_fetch_first_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_fetch_first_value(PostgreSQLParser.Select_fetch_first_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#i_or_f_const}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitI_or_f_const(PostgreSQLParser.I_or_f_constContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#row_or_rows}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow_or_rows(PostgreSQLParser.Row_or_rowsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#first_or_next}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFirst_or_next(PostgreSQLParser.First_or_nextContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#group_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_clause(PostgreSQLParser.Group_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#group_by_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_by_list(PostgreSQLParser.Group_by_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#group_by_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_by_item(PostgreSQLParser.Group_by_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#empty_grouping_set}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmpty_grouping_set(PostgreSQLParser.Empty_grouping_setContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#rollup_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRollup_clause(PostgreSQLParser.Rollup_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#cube_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCube_clause(PostgreSQLParser.Cube_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#grouping_sets_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrouping_sets_clause(PostgreSQLParser.Grouping_sets_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#having_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHaving_clause(PostgreSQLParser.Having_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#for_locking_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_locking_clause(PostgreSQLParser.For_locking_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#for_locking_clause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_locking_clause_(PostgreSQLParser.For_locking_clause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#for_locking_items}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_locking_items(PostgreSQLParser.For_locking_itemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#for_locking_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_locking_item(PostgreSQLParser.For_locking_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#for_locking_strength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_locking_strength(PostgreSQLParser.For_locking_strengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#locked_rels_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocked_rels_list(PostgreSQLParser.Locked_rels_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#values_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValues_clause(PostgreSQLParser.Values_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#from_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrom_clause(PostgreSQLParser.From_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#from_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrom_list(PostgreSQLParser.From_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#table_ref}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_ref(PostgreSQLParser.Table_refContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#alias_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias_clause(PostgreSQLParser.Alias_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_alias_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_alias_clause(PostgreSQLParser.Func_alias_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#join_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin_type(PostgreSQLParser.Join_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#join_qual}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin_qual(PostgreSQLParser.Join_qualContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#relation_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation_expr(PostgreSQLParser.Relation_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#relation_expr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation_expr_list(PostgreSQLParser.Relation_expr_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#relation_expr_opt_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation_expr_opt_alias(PostgreSQLParser.Relation_expr_opt_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#tablesample_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablesample_clause(PostgreSQLParser.Tablesample_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#repeatable_clause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRepeatable_clause_(PostgreSQLParser.Repeatable_clause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_table(PostgreSQLParser.Func_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#rowsfrom_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowsfrom_item(PostgreSQLParser.Rowsfrom_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#rowsfrom_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRowsfrom_list(PostgreSQLParser.Rowsfrom_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#col_def_list_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCol_def_list_(PostgreSQLParser.Col_def_list_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#ordinality_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrdinality_(PostgreSQLParser.Ordinality_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#where_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere_clause(PostgreSQLParser.Where_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#where_or_current_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere_or_current_clause(PostgreSQLParser.Where_or_current_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#opttablefuncelementlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpttablefuncelementlist(PostgreSQLParser.OpttablefuncelementlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#tablefuncelementlist}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablefuncelementlist(PostgreSQLParser.TablefuncelementlistContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#tablefuncelement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablefuncelement(PostgreSQLParser.TablefuncelementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#xmltable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmltable(PostgreSQLParser.XmltableContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#xmltable_column_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmltable_column_list(PostgreSQLParser.Xmltable_column_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#xmltable_column_el}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmltable_column_el(PostgreSQLParser.Xmltable_column_elContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#xmltable_column_option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmltable_column_option_list(PostgreSQLParser.Xmltable_column_option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#xmltable_column_option_el}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmltable_column_option_el(PostgreSQLParser.Xmltable_column_option_elContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#xml_namespace_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXml_namespace_list(PostgreSQLParser.Xml_namespace_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#xml_namespace_el}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXml_namespace_el(PostgreSQLParser.Xml_namespace_elContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#typename}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypename(PostgreSQLParser.TypenameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#opt_array_bounds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_array_bounds(PostgreSQLParser.Opt_array_boundsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#simpletypename}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpletypename(PostgreSQLParser.SimpletypenameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#consttypename}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConsttypename(PostgreSQLParser.ConsttypenameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#generictype}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGenerictype(PostgreSQLParser.GenerictypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#type_modifiers_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_modifiers_(PostgreSQLParser.Type_modifiers_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#numeric}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumeric(PostgreSQLParser.NumericContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#float_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFloat_(PostgreSQLParser.Float_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#bit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBit(PostgreSQLParser.BitContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#constbit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstbit(PostgreSQLParser.ConstbitContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#bitwithlength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitwithlength(PostgreSQLParser.BitwithlengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#bitwithoutlength}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitwithoutlength(PostgreSQLParser.BitwithoutlengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#character}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacter(PostgreSQLParser.CharacterContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#constcharacter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstcharacter(PostgreSQLParser.ConstcharacterContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#character_c}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharacter_c(PostgreSQLParser.Character_cContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#varying_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarying_(PostgreSQLParser.Varying_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#constdatetime}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstdatetime(PostgreSQLParser.ConstdatetimeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#constinterval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstinterval(PostgreSQLParser.ConstintervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#timezone_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimezone_(PostgreSQLParser.Timezone_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#interval_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterval_(PostgreSQLParser.Interval_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#interval_second}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterval_second(PostgreSQLParser.Interval_secondContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#jsonType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonType(PostgreSQLParser.JsonTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#escape_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEscape_(PostgreSQLParser.Escape_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr(PostgreSQLParser.A_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_qual}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_qual(PostgreSQLParser.A_expr_qualContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_lessless}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_lessless(PostgreSQLParser.A_expr_lesslessContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_or}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_or(PostgreSQLParser.A_expr_orContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_and}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_and(PostgreSQLParser.A_expr_andContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_between}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_between(PostgreSQLParser.A_expr_betweenContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_in}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_in(PostgreSQLParser.A_expr_inContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_unary_not}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_unary_not(PostgreSQLParser.A_expr_unary_notContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_isnull}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_isnull(PostgreSQLParser.A_expr_isnullContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_is_not}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_is_not(PostgreSQLParser.A_expr_is_notContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_compare}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_compare(PostgreSQLParser.A_expr_compareContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_like}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_like(PostgreSQLParser.A_expr_likeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_qual_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_qual_op(PostgreSQLParser.A_expr_qual_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_unary_qualop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_unary_qualop(PostgreSQLParser.A_expr_unary_qualopContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_add}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_add(PostgreSQLParser.A_expr_addContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_mul}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_mul(PostgreSQLParser.A_expr_mulContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_caret}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_caret(PostgreSQLParser.A_expr_caretContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_unary_sign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_unary_sign(PostgreSQLParser.A_expr_unary_signContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_at_time_zone}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_at_time_zone(PostgreSQLParser.A_expr_at_time_zoneContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_collate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_collate(PostgreSQLParser.A_expr_collateContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#a_expr_typecast}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitA_expr_typecast(PostgreSQLParser.A_expr_typecastContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#b_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitB_expr(PostgreSQLParser.B_exprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code c_expr_exists}
	 * labeled alternative in {@link PostgreSQLParser#c_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitC_expr_exists(PostgreSQLParser.C_expr_existsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code c_expr_expr}
	 * labeled alternative in {@link PostgreSQLParser#c_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitC_expr_expr(PostgreSQLParser.C_expr_exprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code c_expr_case}
	 * labeled alternative in {@link PostgreSQLParser#c_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitC_expr_case(PostgreSQLParser.C_expr_caseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#plsqlvariablename}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPlsqlvariablename(PostgreSQLParser.PlsqlvariablenameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_application}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_application(PostgreSQLParser.Func_applicationContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_expr(PostgreSQLParser.Func_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_expr_windowless}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_expr_windowless(PostgreSQLParser.Func_expr_windowlessContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_expr_common_subexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_expr_common_subexpr(PostgreSQLParser.Func_expr_common_subexprContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#xml_root_version}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXml_root_version(PostgreSQLParser.Xml_root_versionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#xml_root_standalone_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXml_root_standalone_(PostgreSQLParser.Xml_root_standalone_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#xml_attributes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXml_attributes(PostgreSQLParser.Xml_attributesContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#xml_attribute_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXml_attribute_list(PostgreSQLParser.Xml_attribute_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#xml_attribute_el}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXml_attribute_el(PostgreSQLParser.Xml_attribute_elContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#document_or_content}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocument_or_content(PostgreSQLParser.Document_or_contentContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#xml_whitespace_option}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXml_whitespace_option(PostgreSQLParser.Xml_whitespace_optionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#xmlexists_argument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlexists_argument(PostgreSQLParser.Xmlexists_argumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#xml_passing_mech}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXml_passing_mech(PostgreSQLParser.Xml_passing_mechContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#within_group_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithin_group_clause(PostgreSQLParser.Within_group_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#filter_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilter_clause(PostgreSQLParser.Filter_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#window_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_clause(PostgreSQLParser.Window_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#window_definition_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_definition_list(PostgreSQLParser.Window_definition_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#window_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_definition(PostgreSQLParser.Window_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#over_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOver_clause(PostgreSQLParser.Over_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#window_specification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_specification(PostgreSQLParser.Window_specificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#existing_window_name_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExisting_window_name_(PostgreSQLParser.Existing_window_name_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#partition_clause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_clause_(PostgreSQLParser.Partition_clause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#frame_clause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrame_clause_(PostgreSQLParser.Frame_clause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#frame_extent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrame_extent(PostgreSQLParser.Frame_extentContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#frame_bound}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrame_bound(PostgreSQLParser.Frame_boundContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#window_exclusion_clause_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_exclusion_clause_(PostgreSQLParser.Window_exclusion_clause_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#row}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRow(PostgreSQLParser.RowContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#explicit_row}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplicit_row(PostgreSQLParser.Explicit_rowContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#implicit_row}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImplicit_row(PostgreSQLParser.Implicit_rowContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#sub_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSub_type(PostgreSQLParser.Sub_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#all_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAll_op(PostgreSQLParser.All_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#mathop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathop(PostgreSQLParser.MathopContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#qual_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQual_op(PostgreSQLParser.Qual_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#qual_all_op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQual_all_op(PostgreSQLParser.Qual_all_opContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#subquery_Op}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubquery_Op(PostgreSQLParser.Subquery_OpContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#expr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_list(PostgreSQLParser.Expr_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_arg_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_arg_list(PostgreSQLParser.Func_arg_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_arg_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_arg_expr(PostgreSQLParser.Func_arg_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#type_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_list(PostgreSQLParser.Type_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#array_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_expr(PostgreSQLParser.Array_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#array_expr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_expr_list(PostgreSQLParser.Array_expr_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#extract_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtract_list(PostgreSQLParser.Extract_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#extract_arg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExtract_arg(PostgreSQLParser.Extract_argContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#unicode_normal_form}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnicode_normal_form(PostgreSQLParser.Unicode_normal_formContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#overlay_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverlay_list(PostgreSQLParser.Overlay_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#position_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPosition_list(PostgreSQLParser.Position_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#substr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstr_list(PostgreSQLParser.Substr_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#trim_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrim_list(PostgreSQLParser.Trim_listContext ctx);
	/**
	 * Visit a parse tree produced by the {@code in_expr_select}
	 * labeled alternative in {@link PostgreSQLParser#in_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIn_expr_select(PostgreSQLParser.In_expr_selectContext ctx);
	/**
	 * Visit a parse tree produced by the {@code in_expr_list}
	 * labeled alternative in {@link PostgreSQLParser#in_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIn_expr_list(PostgreSQLParser.In_expr_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#case_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_expr(PostgreSQLParser.Case_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#when_clause_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhen_clause_list(PostgreSQLParser.When_clause_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#when_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhen_clause(PostgreSQLParser.When_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#case_default}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_default(PostgreSQLParser.Case_defaultContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#case_arg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_arg(PostgreSQLParser.Case_argContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#columnref}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnref(PostgreSQLParser.ColumnrefContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#indirection_el}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndirection_el(PostgreSQLParser.Indirection_elContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#slice_bound_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSlice_bound_(PostgreSQLParser.Slice_bound_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#indirection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndirection(PostgreSQLParser.IndirectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#opt_indirection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpt_indirection(PostgreSQLParser.Opt_indirectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_passing_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_passing_clause(PostgreSQLParser.Json_passing_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_arguments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_arguments(PostgreSQLParser.Json_argumentsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_argument}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_argument(PostgreSQLParser.Json_argumentContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_wrapper_behavior}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_wrapper_behavior(PostgreSQLParser.Json_wrapper_behaviorContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_behavior}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_behavior(PostgreSQLParser.Json_behaviorContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_behavior_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_behavior_type(PostgreSQLParser.Json_behavior_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_behavior_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_behavior_clause(PostgreSQLParser.Json_behavior_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_on_error_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_on_error_clause(PostgreSQLParser.Json_on_error_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_value_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_value_expr(PostgreSQLParser.Json_value_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_format_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_format_clause(PostgreSQLParser.Json_format_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_quotes_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_quotes_clause(PostgreSQLParser.Json_quotes_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_returning_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_returning_clause(PostgreSQLParser.Json_returning_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_predicate_type_constraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_predicate_type_constraint(PostgreSQLParser.Json_predicate_type_constraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_key_uniqueness_constraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_key_uniqueness_constraint(PostgreSQLParser.Json_key_uniqueness_constraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_name_and_value_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_name_and_value_list(PostgreSQLParser.Json_name_and_value_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_name_and_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_name_and_value(PostgreSQLParser.Json_name_and_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_object_constructor_null_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_object_constructor_null_clause(PostgreSQLParser.Json_object_constructor_null_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_array_constructor_null_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_array_constructor_null_clause(PostgreSQLParser.Json_array_constructor_null_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_value_expr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_value_expr_list(PostgreSQLParser.Json_value_expr_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_aggregate_func}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_aggregate_func(PostgreSQLParser.Json_aggregate_funcContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#json_array_aggregate_order_by_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJson_array_aggregate_order_by_clause(PostgreSQLParser.Json_array_aggregate_order_by_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#target_list_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_list_(PostgreSQLParser.Target_list_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#target_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_list(PostgreSQLParser.Target_listContext ctx);
	/**
	 * Visit a parse tree produced by the {@code target_label}
	 * labeled alternative in {@link PostgreSQLParser#target_el}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_label(PostgreSQLParser.Target_labelContext ctx);
	/**
	 * Visit a parse tree produced by the {@code target_star}
	 * labeled alternative in {@link PostgreSQLParser#target_el}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTarget_star(PostgreSQLParser.Target_starContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#qualified_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualified_name_list(PostgreSQLParser.Qualified_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#qualified_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualified_name(PostgreSQLParser.Qualified_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName_list(PostgreSQLParser.Name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(PostgreSQLParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#attr_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttr_name(PostgreSQLParser.Attr_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#file_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFile_name(PostgreSQLParser.File_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#func_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_name(PostgreSQLParser.Func_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#aexprconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAexprconst(PostgreSQLParser.AexprconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#xconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXconst(PostgreSQLParser.XconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#bconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBconst(PostgreSQLParser.BconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#fconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFconst(PostgreSQLParser.FconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#iconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIconst(PostgreSQLParser.IconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#sconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSconst(PostgreSQLParser.SconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#anysconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnysconst(PostgreSQLParser.AnysconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#uescape_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUescape_(PostgreSQLParser.Uescape_Context ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#signediconst}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignediconst(PostgreSQLParser.SignediconstContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#roleid}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoleid(PostgreSQLParser.RoleidContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#rolespec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRolespec(PostgreSQLParser.RolespecContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#role_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRole_list(PostgreSQLParser.Role_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#colid}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColid(PostgreSQLParser.ColidContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#type_function_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_function_name(PostgreSQLParser.Type_function_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#nonreservedword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonreservedword(PostgreSQLParser.NonreservedwordContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#colLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColLabel(PostgreSQLParser.ColLabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#bareColLabel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBareColLabel(PostgreSQLParser.BareColLabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#unreserved_keyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnreserved_keyword(PostgreSQLParser.Unreserved_keywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#col_name_keyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCol_name_keyword(PostgreSQLParser.Col_name_keywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#type_func_name_keyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_func_name_keyword(PostgreSQLParser.Type_func_name_keywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#reserved_keyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReserved_keyword(PostgreSQLParser.Reserved_keywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#bare_label_keyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBare_label_keyword(PostgreSQLParser.Bare_label_keywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#any_identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAny_identifier(PostgreSQLParser.Any_identifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link PostgreSQLParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(PostgreSQLParser.IdentifierContext ctx);
}