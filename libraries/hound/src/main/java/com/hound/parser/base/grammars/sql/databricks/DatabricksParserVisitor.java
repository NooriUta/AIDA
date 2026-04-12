// Generated from C:/AIDA/aida-root/libraries/hound/src/main/resources/grammars/sql/databricks/DatabricksParser.g4 by ANTLR 4.13.2
package com.hound.parser.base.grammars.sql.databricks;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DatabricksParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DatabricksParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#databricks_file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatabricks_file(DatabricksParser.Databricks_fileContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#statement_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement_list(DatabricksParser.Statement_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(DatabricksParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#ddl_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdl_statement(DatabricksParser.Ddl_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#dml_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDml_statement(DatabricksParser.Dml_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#data_retrieval_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_retrieval_statement(DatabricksParser.Data_retrieval_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_statement(DatabricksParser.Show_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#describe_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_statement(DatabricksParser.Describe_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#misc_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMisc_statement(DatabricksParser.Misc_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#resource_management_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResource_management_statement(DatabricksParser.Resource_management_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#security_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSecurity_statement(DatabricksParser.Security_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#insert_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_statement(DatabricksParser.Insert_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#insert_overwrite_directory_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_overwrite_directory_statement(DatabricksParser.Insert_overwrite_directory_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#insert_overwrite_directory_hive_format_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_overwrite_directory_hive_format_statement(DatabricksParser.Insert_overwrite_directory_hive_format_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#load_data_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoad_data_statement(DatabricksParser.Load_data_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#merge_into_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMerge_into_statement(DatabricksParser.Merge_into_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#update_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate_statement(DatabricksParser.Update_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#copy_into_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCopy_into_statement(DatabricksParser.Copy_into_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#delete_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDelete_statement(DatabricksParser.Delete_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#comment_on_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment_on_statement(DatabricksParser.Comment_on_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#declare_variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclare_variable(DatabricksParser.Declare_variableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#msck_repair_table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMsck_repair_table_statement(DatabricksParser.Msck_repair_table_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#refresh_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefresh_statement(DatabricksParser.Refresh_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#set_tag_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_tag_statement(DatabricksParser.Set_tag_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#sync_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSync_statement(DatabricksParser.Sync_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#undrop_table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUndrop_table_statement(DatabricksParser.Undrop_table_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#unset_tag_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnset_tag_statement(DatabricksParser.Unset_tag_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#execute_immediate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecute_immediate(DatabricksParser.Execute_immediateContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#variable_name_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable_name_list(DatabricksParser.Variable_name_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#reset_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReset_statement(DatabricksParser.Reset_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#set_recipient}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_recipient(DatabricksParser.Set_recipientContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#set_timezone}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_timezone(DatabricksParser.Set_timezoneContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#set_variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_variable(DatabricksParser.Set_variableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#use_catalog}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUse_catalog(DatabricksParser.Use_catalogContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#set}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet(DatabricksParser.SetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#truncate_table_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTruncate_table_statement(DatabricksParser.Truncate_table_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#partition_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_clause(DatabricksParser.Partition_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#partition_column_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_column_list(DatabricksParser.Partition_column_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#partition_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_value(DatabricksParser.Partition_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#alter_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_statement(DatabricksParser.Alter_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#catalog_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatalog_name(DatabricksParser.Catalog_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#default_collation_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefault_collation_name(DatabricksParser.Default_collation_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#location_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocation_name(DatabricksParser.Location_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#principal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrincipal(DatabricksParser.PrincipalContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#connection_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConnection_name(DatabricksParser.Connection_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#clean_room_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClean_room_name(DatabricksParser.Clean_room_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#credential_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCredential_name(DatabricksParser.Credential_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#file_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFile_name(DatabricksParser.File_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#partition_column}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartition_column(DatabricksParser.Partition_columnContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#resource_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResource_name(DatabricksParser.Resource_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#function_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_name(DatabricksParser.Function_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#metadata_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMetadata_name(DatabricksParser.Metadata_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#procedure_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcedure_name(DatabricksParser.Procedure_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#provider_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProvider_name(DatabricksParser.Provider_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#recipient_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecipient_name(DatabricksParser.Recipient_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#relation_id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation_id(DatabricksParser.Relation_idContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#relation_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation_name(DatabricksParser.Relation_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#share_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShare_name(DatabricksParser.Share_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_name(DatabricksParser.Table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#variable_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable_name(DatabricksParser.Variable_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#view_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitView_name(DatabricksParser.View_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#volume_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVolume_name(DatabricksParser.Volume_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#alter_catalog}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_catalog(DatabricksParser.Alter_catalogContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#alter_connection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_connection(DatabricksParser.Alter_connectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#option_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOption_list(DatabricksParser.Option_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#alter_credential}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_credential(DatabricksParser.Alter_credentialContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#data_type_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_type_list(DatabricksParser.Data_type_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#alter_location}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_location(DatabricksParser.Alter_locationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#alter_materialized_view}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_materialized_view(DatabricksParser.Alter_materialized_viewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#alter_provider}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_provider(DatabricksParser.Alter_providerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#alter_recipient}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_recipient(DatabricksParser.Alter_recipientContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#property_key_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty_key_list(DatabricksParser.Property_key_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#property_key_value_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty_key_value_list(DatabricksParser.Property_key_value_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#property_key_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty_key_value(DatabricksParser.Property_key_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#property_key}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty_key(DatabricksParser.Property_keyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#property_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProperty_value(DatabricksParser.Property_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#alter_schema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_schema(DatabricksParser.Alter_schemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#alter_share}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_share(DatabricksParser.Alter_shareContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#alter_streaming_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_streaming_table(DatabricksParser.Alter_streaming_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#alter_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_table(DatabricksParser.Alter_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#alter_view}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_view(DatabricksParser.Alter_viewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#alter_volume}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_volume(DatabricksParser.Alter_volumeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#tag_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTag_list(DatabricksParser.Tag_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_statement(DatabricksParser.Create_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_bloomfilter_index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_bloomfilter_index(DatabricksParser.Create_bloomfilter_indexContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_catalog}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_catalog(DatabricksParser.Create_catalogContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#location_path}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLocation_path(DatabricksParser.Location_pathContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#comment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment(DatabricksParser.CommentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_connection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_connection(DatabricksParser.Create_connectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_function(DatabricksParser.Create_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_location}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_location(DatabricksParser.Create_locationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#column_list_in_parentheses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_list_in_parentheses(DatabricksParser.Column_list_in_parenthesesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_materialized_view}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_materialized_view(DatabricksParser.Create_materialized_viewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_procedure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_procedure(DatabricksParser.Create_procedureContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_recipient}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_recipient(DatabricksParser.Create_recipientContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_schema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_schema(DatabricksParser.Create_schemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_share}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_share(DatabricksParser.Create_shareContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#true_false}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrue_false(DatabricksParser.True_falseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_streaming_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_streaming_table(DatabricksParser.Create_streaming_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_table(DatabricksParser.Create_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_table_using}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_table_using(DatabricksParser.Create_table_usingContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_table_like}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_table_like(DatabricksParser.Create_table_likeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_table_clone}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_table_clone(DatabricksParser.Create_table_cloneContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_table_hive_format}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_table_hive_format(DatabricksParser.Create_table_hive_formatContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCall(DatabricksParser.CallContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#argument_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgument_list(DatabricksParser.Argument_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#named_argument_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamed_argument_list(DatabricksParser.Named_argument_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_view}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_view(DatabricksParser.Create_viewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_volume}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_volume(DatabricksParser.Create_volumeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#object_type_plural}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject_type_plural(DatabricksParser.Object_type_pluralContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_statement(DatabricksParser.Drop_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_bloomfilter_index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_bloomfilter_index(DatabricksParser.Drop_bloomfilter_indexContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_catalog}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_catalog(DatabricksParser.Drop_catalogContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_connection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_connection(DatabricksParser.Drop_connectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_credential}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_credential(DatabricksParser.Drop_credentialContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_database}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_database(DatabricksParser.Drop_databaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_function(DatabricksParser.Drop_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_location}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_location(DatabricksParser.Drop_locationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_procedure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_procedure(DatabricksParser.Drop_procedureContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_provider}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_provider(DatabricksParser.Drop_providerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_recipient}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_recipient(DatabricksParser.Drop_recipientContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_schema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_schema(DatabricksParser.Drop_schemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_share}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_share(DatabricksParser.Drop_shareContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_table(DatabricksParser.Drop_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_variable(DatabricksParser.Drop_variableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_view}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_view(DatabricksParser.Drop_viewContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_volume}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_volume(DatabricksParser.Drop_volumeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#cascade_restrict}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCascade_restrict(DatabricksParser.Cascade_restrictContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#arg_types}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArg_types(DatabricksParser.Arg_typesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#use_schema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUse_schema(DatabricksParser.Use_schemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#add_archive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdd_archive(DatabricksParser.Add_archiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#add_file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdd_file(DatabricksParser.Add_fileContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#add_jar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdd_jar(DatabricksParser.Add_jarContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#list_archive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList_archive(DatabricksParser.List_archiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#list_file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList_file(DatabricksParser.List_fileContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#list_jar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList_jar(DatabricksParser.List_jarContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#get}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGet(DatabricksParser.GetContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#put_into}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPut_into(DatabricksParser.Put_intoContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#remove}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRemove(DatabricksParser.RemoveContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#alter_group}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlter_group(DatabricksParser.Alter_groupContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#user_group}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUser_group(DatabricksParser.User_groupContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#principal_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrincipal_list(DatabricksParser.Principal_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#create_group}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreate_group(DatabricksParser.Create_groupContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#deny}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeny(DatabricksParser.DenyContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#drop_group}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrop_group(DatabricksParser.Drop_groupContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#grant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant(DatabricksParser.GrantContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#privilege_types}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilege_types(DatabricksParser.Privilege_typesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#privilege_type_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilege_type_list(DatabricksParser.Privilege_type_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#privilege_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivilege_type(DatabricksParser.Privilege_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#grant_share}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrant_share(DatabricksParser.Grant_shareContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#repair_privileges}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRepair_privileges(DatabricksParser.Repair_privilegesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#schema_database}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_database(DatabricksParser.Schema_databaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#object}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject(DatabricksParser.ObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#revoke}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke(DatabricksParser.RevokeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#revoke_share}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRevoke_share(DatabricksParser.Revoke_shareContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_grants}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_grants(DatabricksParser.Show_grantsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#securable_object}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSecurable_object(DatabricksParser.Securable_objectContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#storage_service}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStorage_service(DatabricksParser.Storage_serviceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_grants_on_share}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_grants_on_share(DatabricksParser.Show_grants_on_shareContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_grants_to_recipient}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_grants_to_recipient(DatabricksParser.Show_grants_to_recipientContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#comment_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment_clause(DatabricksParser.Comment_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#inline_comment_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInline_comment_clause(DatabricksParser.Inline_comment_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#if_exists}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_exists(DatabricksParser.If_existsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#if_not_exists}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_not_exists(DatabricksParser.If_not_existsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#or_replace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr_replace(DatabricksParser.Or_replaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#describe}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe(DatabricksParser.DescribeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#describe_catalog}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_catalog(DatabricksParser.Describe_catalogContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#describe_connection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_connection(DatabricksParser.Describe_connectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#describe_credential}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_credential(DatabricksParser.Describe_credentialContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#describe_database}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_database(DatabricksParser.Describe_databaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#describe_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_function(DatabricksParser.Describe_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#describe_location}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_location(DatabricksParser.Describe_locationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#describe_procedure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_procedure(DatabricksParser.Describe_procedureContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#describe_provider}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_provider(DatabricksParser.Describe_providerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#describe_query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_query(DatabricksParser.Describe_queryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#describe_recipient}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_recipient(DatabricksParser.Describe_recipientContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#describe_schema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_schema(DatabricksParser.Describe_schemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#describe_share}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_share(DatabricksParser.Describe_shareContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#describe_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_table(DatabricksParser.Describe_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#describe_volume}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescribe_volume(DatabricksParser.Describe_volumeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList(DatabricksParser.ListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_all_in_share}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_all_in_share(DatabricksParser.Show_all_in_shareContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_catalogs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_catalogs(DatabricksParser.Show_catalogsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_columns}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_columns(DatabricksParser.Show_columnsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#in_from}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIn_from(DatabricksParser.In_fromContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_connections}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_connections(DatabricksParser.Show_connectionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_create_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_create_table(DatabricksParser.Show_create_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_credentials}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_credentials(DatabricksParser.Show_credentialsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_databases}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_databases(DatabricksParser.Show_databasesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_functions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_functions(DatabricksParser.Show_functionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#function_kind}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_kind(DatabricksParser.Function_kindContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_groups}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_groups(DatabricksParser.Show_groupsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_locations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_locations(DatabricksParser.Show_locationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_partitions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_partitions(DatabricksParser.Show_partitionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_procedures}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_procedures(DatabricksParser.Show_proceduresContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_providers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_providers(DatabricksParser.Show_providersContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_recipients}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_recipients(DatabricksParser.Show_recipientsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_schemas}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_schemas(DatabricksParser.Show_schemasContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_shares}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_shares(DatabricksParser.Show_sharesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_shares_in_provider}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_shares_in_provider(DatabricksParser.Show_shares_in_providerContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_table(DatabricksParser.Show_tableContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_tables}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_tables(DatabricksParser.Show_tablesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_tables_dropped}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_tables_dropped(DatabricksParser.Show_tables_droppedContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_tblproperties}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_tblproperties(DatabricksParser.Show_tblpropertiesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_users}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_users(DatabricksParser.Show_usersContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_views}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_views(DatabricksParser.Show_viewsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#show_volumes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShow_volumes(DatabricksParser.Show_volumesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#like_pattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLike_pattern(DatabricksParser.Like_patternContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#schema_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema_name(DatabricksParser.Schema_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#id_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId_clause(DatabricksParser.Id_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#string}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString(DatabricksParser.StringContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#id_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId_(DatabricksParser.Id_Context ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#keyword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyword(DatabricksParser.KeywordContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#non_reserved_words}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNon_reserved_words(DatabricksParser.Non_reserved_wordsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_name(DatabricksParser.Column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#column_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_list(DatabricksParser.Column_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#object_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject_name(DatabricksParser.Object_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#num}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNum(DatabricksParser.NumContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#expr_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_list(DatabricksParser.Expr_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#expr_list_in_parentheses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_list_in_parentheses(DatabricksParser.Expr_list_in_parenthesesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(DatabricksParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#cast_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCast_expr(DatabricksParser.Cast_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#data_type_size}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_type_size(DatabricksParser.Data_type_sizeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#data_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitData_type(DatabricksParser.Data_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#primitive_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitive_expression(DatabricksParser.Primitive_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#function_call}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction_call(DatabricksParser.Function_callContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#param_assoc_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_assoc_list(DatabricksParser.Param_assoc_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#param_assoc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam_assoc(DatabricksParser.Param_assocContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(DatabricksParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#sign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSign(DatabricksParser.SignContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#full_column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFull_column_name(DatabricksParser.Full_column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#bracket_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBracket_expression(DatabricksParser.Bracket_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#case_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCase_expression(DatabricksParser.Case_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#switch_search_condition_section}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitch_search_condition_section(DatabricksParser.Switch_search_condition_sectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#switch_section}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSwitch_section(DatabricksParser.Switch_sectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#query_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuery_statement(DatabricksParser.Query_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#with_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWith_expression(DatabricksParser.With_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#common_table_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommon_table_expression(DatabricksParser.Common_table_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#recursion_limit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecursion_limit(DatabricksParser.Recursion_limitContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#recursive_query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecursive_query(DatabricksParser.Recursive_queryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#subquery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubquery(DatabricksParser.SubqueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#set_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_operator(DatabricksParser.Set_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#order_by_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrder_by_clause(DatabricksParser.Order_by_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#order_by_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrder_by_item(DatabricksParser.Order_by_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#sort_direction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSort_direction(DatabricksParser.Sort_directionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#nulls_sort_order}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNulls_sort_order(DatabricksParser.Nulls_sort_orderContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#distribute_by_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistribute_by_clause(DatabricksParser.Distribute_by_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#sort_by_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSort_by_clause(DatabricksParser.Sort_by_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#sort_by_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSort_by_item(DatabricksParser.Sort_by_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#cluster_by_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCluster_by_clause(DatabricksParser.Cluster_by_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#window_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_clause(DatabricksParser.Window_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#window_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_name(DatabricksParser.Window_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#window_spec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindow_spec(DatabricksParser.Window_specContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#limit_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimit_clause(DatabricksParser.Limit_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#offset_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOffset_clause(DatabricksParser.Offset_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#select_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_statement(DatabricksParser.Select_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#select_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_clause(DatabricksParser.Select_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#hints}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHints(DatabricksParser.HintsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#select_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelect_item(DatabricksParser.Select_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#named_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamed_expression(DatabricksParser.Named_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#star_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStar_clause(DatabricksParser.Star_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#except_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExcept_clause(DatabricksParser.Except_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#table_reference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_reference(DatabricksParser.Table_referenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#join_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin_clause(DatabricksParser.Join_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#join_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoin_type(DatabricksParser.Join_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#pivot_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPivot_clause(DatabricksParser.Pivot_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#expr_list_with_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_list_with_alias(DatabricksParser.Expr_list_with_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#unpivot_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnpivot_clause(DatabricksParser.Unpivot_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#single_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingle_value(DatabricksParser.Single_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#multi_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMulti_value(DatabricksParser.Multi_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#column_list_with_aliases}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_list_with_aliases(DatabricksParser.Column_list_with_aliasesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#include_exclude}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInclude_exclude(DatabricksParser.Include_excludeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#table_valued_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_valued_function(DatabricksParser.Table_valued_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#table_name_with_optional_parentheses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_name_with_optional_parentheses(DatabricksParser.Table_name_with_optional_parenthesesContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#tablesample_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablesample_clause(DatabricksParser.Tablesample_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#table_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_alias(DatabricksParser.Table_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#lateral_view_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLateral_view_clause(DatabricksParser.Lateral_view_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#table_identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_identifier(DatabricksParser.Table_identifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#where_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhere_clause(DatabricksParser.Where_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#group_by_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_by_clause(DatabricksParser.Group_by_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#group_by_item_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_by_item_list(DatabricksParser.Group_by_item_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#group_by_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup_by_item(DatabricksParser.Group_by_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#grouping_set}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGrouping_set(DatabricksParser.Grouping_setContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#having_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHaving_clause(DatabricksParser.Having_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#qualify_clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualify_clause(DatabricksParser.Qualify_clauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#values_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValues_statement(DatabricksParser.Values_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#values_item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValues_item(DatabricksParser.Values_itemContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#sql_pipeline}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSql_pipeline(DatabricksParser.Sql_pipelineContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#explain_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExplain_statement(DatabricksParser.Explain_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#as_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAs_alias(DatabricksParser.As_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(DatabricksParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link DatabricksParser#comparison_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison_operator(DatabricksParser.Comparison_operatorContext ctx);
}