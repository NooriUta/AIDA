// Generated from C:/AIDA/Dali4/HOUND/Hound/src/main/resources/grammars/sql/databricks/DatabricksParser.g4 by ANTLR 4.13.1
package com.hound.parser.base.grammars.sql.databricks;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DatabricksParser}.
 */
public interface DatabricksParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#databricks_file}.
	 * @param ctx the parse tree
	 */
	void enterDatabricks_file(DatabricksParser.Databricks_fileContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#databricks_file}.
	 * @param ctx the parse tree
	 */
	void exitDatabricks_file(DatabricksParser.Databricks_fileContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#statement_list}.
	 * @param ctx the parse tree
	 */
	void enterStatement_list(DatabricksParser.Statement_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#statement_list}.
	 * @param ctx the parse tree
	 */
	void exitStatement_list(DatabricksParser.Statement_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(DatabricksParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(DatabricksParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#ddl_statement}.
	 * @param ctx the parse tree
	 */
	void enterDdl_statement(DatabricksParser.Ddl_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#ddl_statement}.
	 * @param ctx the parse tree
	 */
	void exitDdl_statement(DatabricksParser.Ddl_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#dml_statement}.
	 * @param ctx the parse tree
	 */
	void enterDml_statement(DatabricksParser.Dml_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#dml_statement}.
	 * @param ctx the parse tree
	 */
	void exitDml_statement(DatabricksParser.Dml_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#data_retrieval_statement}.
	 * @param ctx the parse tree
	 */
	void enterData_retrieval_statement(DatabricksParser.Data_retrieval_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#data_retrieval_statement}.
	 * @param ctx the parse tree
	 */
	void exitData_retrieval_statement(DatabricksParser.Data_retrieval_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_statement}.
	 * @param ctx the parse tree
	 */
	void enterShow_statement(DatabricksParser.Show_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_statement}.
	 * @param ctx the parse tree
	 */
	void exitShow_statement(DatabricksParser.Show_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#describe_statement}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_statement(DatabricksParser.Describe_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#describe_statement}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_statement(DatabricksParser.Describe_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#misc_statement}.
	 * @param ctx the parse tree
	 */
	void enterMisc_statement(DatabricksParser.Misc_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#misc_statement}.
	 * @param ctx the parse tree
	 */
	void exitMisc_statement(DatabricksParser.Misc_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#resource_management_statement}.
	 * @param ctx the parse tree
	 */
	void enterResource_management_statement(DatabricksParser.Resource_management_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#resource_management_statement}.
	 * @param ctx the parse tree
	 */
	void exitResource_management_statement(DatabricksParser.Resource_management_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#security_statement}.
	 * @param ctx the parse tree
	 */
	void enterSecurity_statement(DatabricksParser.Security_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#security_statement}.
	 * @param ctx the parse tree
	 */
	void exitSecurity_statement(DatabricksParser.Security_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#insert_statement}.
	 * @param ctx the parse tree
	 */
	void enterInsert_statement(DatabricksParser.Insert_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#insert_statement}.
	 * @param ctx the parse tree
	 */
	void exitInsert_statement(DatabricksParser.Insert_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#insert_overwrite_directory_statement}.
	 * @param ctx the parse tree
	 */
	void enterInsert_overwrite_directory_statement(DatabricksParser.Insert_overwrite_directory_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#insert_overwrite_directory_statement}.
	 * @param ctx the parse tree
	 */
	void exitInsert_overwrite_directory_statement(DatabricksParser.Insert_overwrite_directory_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#insert_overwrite_directory_hive_format_statement}.
	 * @param ctx the parse tree
	 */
	void enterInsert_overwrite_directory_hive_format_statement(DatabricksParser.Insert_overwrite_directory_hive_format_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#insert_overwrite_directory_hive_format_statement}.
	 * @param ctx the parse tree
	 */
	void exitInsert_overwrite_directory_hive_format_statement(DatabricksParser.Insert_overwrite_directory_hive_format_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#load_data_statement}.
	 * @param ctx the parse tree
	 */
	void enterLoad_data_statement(DatabricksParser.Load_data_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#load_data_statement}.
	 * @param ctx the parse tree
	 */
	void exitLoad_data_statement(DatabricksParser.Load_data_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#merge_into_statement}.
	 * @param ctx the parse tree
	 */
	void enterMerge_into_statement(DatabricksParser.Merge_into_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#merge_into_statement}.
	 * @param ctx the parse tree
	 */
	void exitMerge_into_statement(DatabricksParser.Merge_into_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#update_statement}.
	 * @param ctx the parse tree
	 */
	void enterUpdate_statement(DatabricksParser.Update_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#update_statement}.
	 * @param ctx the parse tree
	 */
	void exitUpdate_statement(DatabricksParser.Update_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#copy_into_statement}.
	 * @param ctx the parse tree
	 */
	void enterCopy_into_statement(DatabricksParser.Copy_into_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#copy_into_statement}.
	 * @param ctx the parse tree
	 */
	void exitCopy_into_statement(DatabricksParser.Copy_into_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#delete_statement}.
	 * @param ctx the parse tree
	 */
	void enterDelete_statement(DatabricksParser.Delete_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#delete_statement}.
	 * @param ctx the parse tree
	 */
	void exitDelete_statement(DatabricksParser.Delete_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#comment_on_statement}.
	 * @param ctx the parse tree
	 */
	void enterComment_on_statement(DatabricksParser.Comment_on_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#comment_on_statement}.
	 * @param ctx the parse tree
	 */
	void exitComment_on_statement(DatabricksParser.Comment_on_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#declare_variable}.
	 * @param ctx the parse tree
	 */
	void enterDeclare_variable(DatabricksParser.Declare_variableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#declare_variable}.
	 * @param ctx the parse tree
	 */
	void exitDeclare_variable(DatabricksParser.Declare_variableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#msck_repair_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterMsck_repair_table_statement(DatabricksParser.Msck_repair_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#msck_repair_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitMsck_repair_table_statement(DatabricksParser.Msck_repair_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#refresh_statement}.
	 * @param ctx the parse tree
	 */
	void enterRefresh_statement(DatabricksParser.Refresh_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#refresh_statement}.
	 * @param ctx the parse tree
	 */
	void exitRefresh_statement(DatabricksParser.Refresh_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#set_tag_statement}.
	 * @param ctx the parse tree
	 */
	void enterSet_tag_statement(DatabricksParser.Set_tag_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#set_tag_statement}.
	 * @param ctx the parse tree
	 */
	void exitSet_tag_statement(DatabricksParser.Set_tag_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#sync_statement}.
	 * @param ctx the parse tree
	 */
	void enterSync_statement(DatabricksParser.Sync_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#sync_statement}.
	 * @param ctx the parse tree
	 */
	void exitSync_statement(DatabricksParser.Sync_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#undrop_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterUndrop_table_statement(DatabricksParser.Undrop_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#undrop_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitUndrop_table_statement(DatabricksParser.Undrop_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#unset_tag_statement}.
	 * @param ctx the parse tree
	 */
	void enterUnset_tag_statement(DatabricksParser.Unset_tag_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#unset_tag_statement}.
	 * @param ctx the parse tree
	 */
	void exitUnset_tag_statement(DatabricksParser.Unset_tag_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#execute_immediate}.
	 * @param ctx the parse tree
	 */
	void enterExecute_immediate(DatabricksParser.Execute_immediateContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#execute_immediate}.
	 * @param ctx the parse tree
	 */
	void exitExecute_immediate(DatabricksParser.Execute_immediateContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#variable_name_list}.
	 * @param ctx the parse tree
	 */
	void enterVariable_name_list(DatabricksParser.Variable_name_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#variable_name_list}.
	 * @param ctx the parse tree
	 */
	void exitVariable_name_list(DatabricksParser.Variable_name_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#reset_statement}.
	 * @param ctx the parse tree
	 */
	void enterReset_statement(DatabricksParser.Reset_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#reset_statement}.
	 * @param ctx the parse tree
	 */
	void exitReset_statement(DatabricksParser.Reset_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#set_recipient}.
	 * @param ctx the parse tree
	 */
	void enterSet_recipient(DatabricksParser.Set_recipientContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#set_recipient}.
	 * @param ctx the parse tree
	 */
	void exitSet_recipient(DatabricksParser.Set_recipientContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#set_timezone}.
	 * @param ctx the parse tree
	 */
	void enterSet_timezone(DatabricksParser.Set_timezoneContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#set_timezone}.
	 * @param ctx the parse tree
	 */
	void exitSet_timezone(DatabricksParser.Set_timezoneContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#set_variable}.
	 * @param ctx the parse tree
	 */
	void enterSet_variable(DatabricksParser.Set_variableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#set_variable}.
	 * @param ctx the parse tree
	 */
	void exitSet_variable(DatabricksParser.Set_variableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#use_catalog}.
	 * @param ctx the parse tree
	 */
	void enterUse_catalog(DatabricksParser.Use_catalogContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#use_catalog}.
	 * @param ctx the parse tree
	 */
	void exitUse_catalog(DatabricksParser.Use_catalogContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#set}.
	 * @param ctx the parse tree
	 */
	void enterSet(DatabricksParser.SetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#set}.
	 * @param ctx the parse tree
	 */
	void exitSet(DatabricksParser.SetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#truncate_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterTruncate_table_statement(DatabricksParser.Truncate_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#truncate_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitTruncate_table_statement(DatabricksParser.Truncate_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#partition_clause}.
	 * @param ctx the parse tree
	 */
	void enterPartition_clause(DatabricksParser.Partition_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#partition_clause}.
	 * @param ctx the parse tree
	 */
	void exitPartition_clause(DatabricksParser.Partition_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#partition_column_list}.
	 * @param ctx the parse tree
	 */
	void enterPartition_column_list(DatabricksParser.Partition_column_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#partition_column_list}.
	 * @param ctx the parse tree
	 */
	void exitPartition_column_list(DatabricksParser.Partition_column_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#partition_value}.
	 * @param ctx the parse tree
	 */
	void enterPartition_value(DatabricksParser.Partition_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#partition_value}.
	 * @param ctx the parse tree
	 */
	void exitPartition_value(DatabricksParser.Partition_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_statement(DatabricksParser.Alter_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#alter_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_statement(DatabricksParser.Alter_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#catalog_name}.
	 * @param ctx the parse tree
	 */
	void enterCatalog_name(DatabricksParser.Catalog_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#catalog_name}.
	 * @param ctx the parse tree
	 */
	void exitCatalog_name(DatabricksParser.Catalog_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#default_collation_name}.
	 * @param ctx the parse tree
	 */
	void enterDefault_collation_name(DatabricksParser.Default_collation_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#default_collation_name}.
	 * @param ctx the parse tree
	 */
	void exitDefault_collation_name(DatabricksParser.Default_collation_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#location_name}.
	 * @param ctx the parse tree
	 */
	void enterLocation_name(DatabricksParser.Location_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#location_name}.
	 * @param ctx the parse tree
	 */
	void exitLocation_name(DatabricksParser.Location_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#principal}.
	 * @param ctx the parse tree
	 */
	void enterPrincipal(DatabricksParser.PrincipalContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#principal}.
	 * @param ctx the parse tree
	 */
	void exitPrincipal(DatabricksParser.PrincipalContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#connection_name}.
	 * @param ctx the parse tree
	 */
	void enterConnection_name(DatabricksParser.Connection_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#connection_name}.
	 * @param ctx the parse tree
	 */
	void exitConnection_name(DatabricksParser.Connection_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#clean_room_name}.
	 * @param ctx the parse tree
	 */
	void enterClean_room_name(DatabricksParser.Clean_room_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#clean_room_name}.
	 * @param ctx the parse tree
	 */
	void exitClean_room_name(DatabricksParser.Clean_room_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#credential_name}.
	 * @param ctx the parse tree
	 */
	void enterCredential_name(DatabricksParser.Credential_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#credential_name}.
	 * @param ctx the parse tree
	 */
	void exitCredential_name(DatabricksParser.Credential_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#file_name}.
	 * @param ctx the parse tree
	 */
	void enterFile_name(DatabricksParser.File_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#file_name}.
	 * @param ctx the parse tree
	 */
	void exitFile_name(DatabricksParser.File_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#partition_column}.
	 * @param ctx the parse tree
	 */
	void enterPartition_column(DatabricksParser.Partition_columnContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#partition_column}.
	 * @param ctx the parse tree
	 */
	void exitPartition_column(DatabricksParser.Partition_columnContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#resource_name}.
	 * @param ctx the parse tree
	 */
	void enterResource_name(DatabricksParser.Resource_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#resource_name}.
	 * @param ctx the parse tree
	 */
	void exitResource_name(DatabricksParser.Resource_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#function_name}.
	 * @param ctx the parse tree
	 */
	void enterFunction_name(DatabricksParser.Function_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#function_name}.
	 * @param ctx the parse tree
	 */
	void exitFunction_name(DatabricksParser.Function_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#metadata_name}.
	 * @param ctx the parse tree
	 */
	void enterMetadata_name(DatabricksParser.Metadata_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#metadata_name}.
	 * @param ctx the parse tree
	 */
	void exitMetadata_name(DatabricksParser.Metadata_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#procedure_name}.
	 * @param ctx the parse tree
	 */
	void enterProcedure_name(DatabricksParser.Procedure_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#procedure_name}.
	 * @param ctx the parse tree
	 */
	void exitProcedure_name(DatabricksParser.Procedure_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#provider_name}.
	 * @param ctx the parse tree
	 */
	void enterProvider_name(DatabricksParser.Provider_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#provider_name}.
	 * @param ctx the parse tree
	 */
	void exitProvider_name(DatabricksParser.Provider_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#recipient_name}.
	 * @param ctx the parse tree
	 */
	void enterRecipient_name(DatabricksParser.Recipient_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#recipient_name}.
	 * @param ctx the parse tree
	 */
	void exitRecipient_name(DatabricksParser.Recipient_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#relation_id}.
	 * @param ctx the parse tree
	 */
	void enterRelation_id(DatabricksParser.Relation_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#relation_id}.
	 * @param ctx the parse tree
	 */
	void exitRelation_id(DatabricksParser.Relation_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#relation_name}.
	 * @param ctx the parse tree
	 */
	void enterRelation_name(DatabricksParser.Relation_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#relation_name}.
	 * @param ctx the parse tree
	 */
	void exitRelation_name(DatabricksParser.Relation_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#share_name}.
	 * @param ctx the parse tree
	 */
	void enterShare_name(DatabricksParser.Share_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#share_name}.
	 * @param ctx the parse tree
	 */
	void exitShare_name(DatabricksParser.Share_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#table_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_name(DatabricksParser.Table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#table_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_name(DatabricksParser.Table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#variable_name}.
	 * @param ctx the parse tree
	 */
	void enterVariable_name(DatabricksParser.Variable_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#variable_name}.
	 * @param ctx the parse tree
	 */
	void exitVariable_name(DatabricksParser.Variable_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#view_name}.
	 * @param ctx the parse tree
	 */
	void enterView_name(DatabricksParser.View_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#view_name}.
	 * @param ctx the parse tree
	 */
	void exitView_name(DatabricksParser.View_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#volume_name}.
	 * @param ctx the parse tree
	 */
	void enterVolume_name(DatabricksParser.Volume_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#volume_name}.
	 * @param ctx the parse tree
	 */
	void exitVolume_name(DatabricksParser.Volume_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#alter_catalog}.
	 * @param ctx the parse tree
	 */
	void enterAlter_catalog(DatabricksParser.Alter_catalogContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#alter_catalog}.
	 * @param ctx the parse tree
	 */
	void exitAlter_catalog(DatabricksParser.Alter_catalogContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#alter_connection}.
	 * @param ctx the parse tree
	 */
	void enterAlter_connection(DatabricksParser.Alter_connectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#alter_connection}.
	 * @param ctx the parse tree
	 */
	void exitAlter_connection(DatabricksParser.Alter_connectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#option_list}.
	 * @param ctx the parse tree
	 */
	void enterOption_list(DatabricksParser.Option_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#option_list}.
	 * @param ctx the parse tree
	 */
	void exitOption_list(DatabricksParser.Option_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#alter_credential}.
	 * @param ctx the parse tree
	 */
	void enterAlter_credential(DatabricksParser.Alter_credentialContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#alter_credential}.
	 * @param ctx the parse tree
	 */
	void exitAlter_credential(DatabricksParser.Alter_credentialContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#data_type_list}.
	 * @param ctx the parse tree
	 */
	void enterData_type_list(DatabricksParser.Data_type_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#data_type_list}.
	 * @param ctx the parse tree
	 */
	void exitData_type_list(DatabricksParser.Data_type_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#alter_location}.
	 * @param ctx the parse tree
	 */
	void enterAlter_location(DatabricksParser.Alter_locationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#alter_location}.
	 * @param ctx the parse tree
	 */
	void exitAlter_location(DatabricksParser.Alter_locationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#alter_materialized_view}.
	 * @param ctx the parse tree
	 */
	void enterAlter_materialized_view(DatabricksParser.Alter_materialized_viewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#alter_materialized_view}.
	 * @param ctx the parse tree
	 */
	void exitAlter_materialized_view(DatabricksParser.Alter_materialized_viewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#alter_provider}.
	 * @param ctx the parse tree
	 */
	void enterAlter_provider(DatabricksParser.Alter_providerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#alter_provider}.
	 * @param ctx the parse tree
	 */
	void exitAlter_provider(DatabricksParser.Alter_providerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#alter_recipient}.
	 * @param ctx the parse tree
	 */
	void enterAlter_recipient(DatabricksParser.Alter_recipientContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#alter_recipient}.
	 * @param ctx the parse tree
	 */
	void exitAlter_recipient(DatabricksParser.Alter_recipientContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#property_key_list}.
	 * @param ctx the parse tree
	 */
	void enterProperty_key_list(DatabricksParser.Property_key_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#property_key_list}.
	 * @param ctx the parse tree
	 */
	void exitProperty_key_list(DatabricksParser.Property_key_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#property_key_value_list}.
	 * @param ctx the parse tree
	 */
	void enterProperty_key_value_list(DatabricksParser.Property_key_value_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#property_key_value_list}.
	 * @param ctx the parse tree
	 */
	void exitProperty_key_value_list(DatabricksParser.Property_key_value_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#property_key_value}.
	 * @param ctx the parse tree
	 */
	void enterProperty_key_value(DatabricksParser.Property_key_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#property_key_value}.
	 * @param ctx the parse tree
	 */
	void exitProperty_key_value(DatabricksParser.Property_key_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#property_key}.
	 * @param ctx the parse tree
	 */
	void enterProperty_key(DatabricksParser.Property_keyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#property_key}.
	 * @param ctx the parse tree
	 */
	void exitProperty_key(DatabricksParser.Property_keyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#property_value}.
	 * @param ctx the parse tree
	 */
	void enterProperty_value(DatabricksParser.Property_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#property_value}.
	 * @param ctx the parse tree
	 */
	void exitProperty_value(DatabricksParser.Property_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#alter_schema}.
	 * @param ctx the parse tree
	 */
	void enterAlter_schema(DatabricksParser.Alter_schemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#alter_schema}.
	 * @param ctx the parse tree
	 */
	void exitAlter_schema(DatabricksParser.Alter_schemaContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#alter_share}.
	 * @param ctx the parse tree
	 */
	void enterAlter_share(DatabricksParser.Alter_shareContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#alter_share}.
	 * @param ctx the parse tree
	 */
	void exitAlter_share(DatabricksParser.Alter_shareContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#alter_streaming_table}.
	 * @param ctx the parse tree
	 */
	void enterAlter_streaming_table(DatabricksParser.Alter_streaming_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#alter_streaming_table}.
	 * @param ctx the parse tree
	 */
	void exitAlter_streaming_table(DatabricksParser.Alter_streaming_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#alter_table}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table(DatabricksParser.Alter_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#alter_table}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table(DatabricksParser.Alter_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#alter_view}.
	 * @param ctx the parse tree
	 */
	void enterAlter_view(DatabricksParser.Alter_viewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#alter_view}.
	 * @param ctx the parse tree
	 */
	void exitAlter_view(DatabricksParser.Alter_viewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#alter_volume}.
	 * @param ctx the parse tree
	 */
	void enterAlter_volume(DatabricksParser.Alter_volumeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#alter_volume}.
	 * @param ctx the parse tree
	 */
	void exitAlter_volume(DatabricksParser.Alter_volumeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#tag_list}.
	 * @param ctx the parse tree
	 */
	void enterTag_list(DatabricksParser.Tag_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#tag_list}.
	 * @param ctx the parse tree
	 */
	void exitTag_list(DatabricksParser.Tag_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_statement(DatabricksParser.Create_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_statement(DatabricksParser.Create_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_bloomfilter_index}.
	 * @param ctx the parse tree
	 */
	void enterCreate_bloomfilter_index(DatabricksParser.Create_bloomfilter_indexContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_bloomfilter_index}.
	 * @param ctx the parse tree
	 */
	void exitCreate_bloomfilter_index(DatabricksParser.Create_bloomfilter_indexContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_catalog}.
	 * @param ctx the parse tree
	 */
	void enterCreate_catalog(DatabricksParser.Create_catalogContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_catalog}.
	 * @param ctx the parse tree
	 */
	void exitCreate_catalog(DatabricksParser.Create_catalogContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#location_path}.
	 * @param ctx the parse tree
	 */
	void enterLocation_path(DatabricksParser.Location_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#location_path}.
	 * @param ctx the parse tree
	 */
	void exitLocation_path(DatabricksParser.Location_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#comment}.
	 * @param ctx the parse tree
	 */
	void enterComment(DatabricksParser.CommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#comment}.
	 * @param ctx the parse tree
	 */
	void exitComment(DatabricksParser.CommentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_connection}.
	 * @param ctx the parse tree
	 */
	void enterCreate_connection(DatabricksParser.Create_connectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_connection}.
	 * @param ctx the parse tree
	 */
	void exitCreate_connection(DatabricksParser.Create_connectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_function}.
	 * @param ctx the parse tree
	 */
	void enterCreate_function(DatabricksParser.Create_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_function}.
	 * @param ctx the parse tree
	 */
	void exitCreate_function(DatabricksParser.Create_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_location}.
	 * @param ctx the parse tree
	 */
	void enterCreate_location(DatabricksParser.Create_locationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_location}.
	 * @param ctx the parse tree
	 */
	void exitCreate_location(DatabricksParser.Create_locationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#column_list_in_parentheses}.
	 * @param ctx the parse tree
	 */
	void enterColumn_list_in_parentheses(DatabricksParser.Column_list_in_parenthesesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#column_list_in_parentheses}.
	 * @param ctx the parse tree
	 */
	void exitColumn_list_in_parentheses(DatabricksParser.Column_list_in_parenthesesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_materialized_view}.
	 * @param ctx the parse tree
	 */
	void enterCreate_materialized_view(DatabricksParser.Create_materialized_viewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_materialized_view}.
	 * @param ctx the parse tree
	 */
	void exitCreate_materialized_view(DatabricksParser.Create_materialized_viewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_procedure}.
	 * @param ctx the parse tree
	 */
	void enterCreate_procedure(DatabricksParser.Create_procedureContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_procedure}.
	 * @param ctx the parse tree
	 */
	void exitCreate_procedure(DatabricksParser.Create_procedureContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_recipient}.
	 * @param ctx the parse tree
	 */
	void enterCreate_recipient(DatabricksParser.Create_recipientContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_recipient}.
	 * @param ctx the parse tree
	 */
	void exitCreate_recipient(DatabricksParser.Create_recipientContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_schema}.
	 * @param ctx the parse tree
	 */
	void enterCreate_schema(DatabricksParser.Create_schemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_schema}.
	 * @param ctx the parse tree
	 */
	void exitCreate_schema(DatabricksParser.Create_schemaContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_share}.
	 * @param ctx the parse tree
	 */
	void enterCreate_share(DatabricksParser.Create_shareContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_share}.
	 * @param ctx the parse tree
	 */
	void exitCreate_share(DatabricksParser.Create_shareContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#true_false}.
	 * @param ctx the parse tree
	 */
	void enterTrue_false(DatabricksParser.True_falseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#true_false}.
	 * @param ctx the parse tree
	 */
	void exitTrue_false(DatabricksParser.True_falseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_streaming_table}.
	 * @param ctx the parse tree
	 */
	void enterCreate_streaming_table(DatabricksParser.Create_streaming_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_streaming_table}.
	 * @param ctx the parse tree
	 */
	void exitCreate_streaming_table(DatabricksParser.Create_streaming_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_table}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table(DatabricksParser.Create_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_table}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table(DatabricksParser.Create_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_table_using}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_using(DatabricksParser.Create_table_usingContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_table_using}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_using(DatabricksParser.Create_table_usingContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_table_like}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_like(DatabricksParser.Create_table_likeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_table_like}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_like(DatabricksParser.Create_table_likeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_table_clone}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_clone(DatabricksParser.Create_table_cloneContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_table_clone}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_clone(DatabricksParser.Create_table_cloneContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_table_hive_format}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_hive_format(DatabricksParser.Create_table_hive_formatContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_table_hive_format}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_hive_format(DatabricksParser.Create_table_hive_formatContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#call}.
	 * @param ctx the parse tree
	 */
	void enterCall(DatabricksParser.CallContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#call}.
	 * @param ctx the parse tree
	 */
	void exitCall(DatabricksParser.CallContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#argument_list}.
	 * @param ctx the parse tree
	 */
	void enterArgument_list(DatabricksParser.Argument_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#argument_list}.
	 * @param ctx the parse tree
	 */
	void exitArgument_list(DatabricksParser.Argument_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#named_argument_list}.
	 * @param ctx the parse tree
	 */
	void enterNamed_argument_list(DatabricksParser.Named_argument_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#named_argument_list}.
	 * @param ctx the parse tree
	 */
	void exitNamed_argument_list(DatabricksParser.Named_argument_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_view}.
	 * @param ctx the parse tree
	 */
	void enterCreate_view(DatabricksParser.Create_viewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_view}.
	 * @param ctx the parse tree
	 */
	void exitCreate_view(DatabricksParser.Create_viewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_volume}.
	 * @param ctx the parse tree
	 */
	void enterCreate_volume(DatabricksParser.Create_volumeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_volume}.
	 * @param ctx the parse tree
	 */
	void exitCreate_volume(DatabricksParser.Create_volumeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#object_type_plural}.
	 * @param ctx the parse tree
	 */
	void enterObject_type_plural(DatabricksParser.Object_type_pluralContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#object_type_plural}.
	 * @param ctx the parse tree
	 */
	void exitObject_type_plural(DatabricksParser.Object_type_pluralContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_statement}.
	 * @param ctx the parse tree
	 */
	void enterDrop_statement(DatabricksParser.Drop_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_statement}.
	 * @param ctx the parse tree
	 */
	void exitDrop_statement(DatabricksParser.Drop_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_bloomfilter_index}.
	 * @param ctx the parse tree
	 */
	void enterDrop_bloomfilter_index(DatabricksParser.Drop_bloomfilter_indexContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_bloomfilter_index}.
	 * @param ctx the parse tree
	 */
	void exitDrop_bloomfilter_index(DatabricksParser.Drop_bloomfilter_indexContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_catalog}.
	 * @param ctx the parse tree
	 */
	void enterDrop_catalog(DatabricksParser.Drop_catalogContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_catalog}.
	 * @param ctx the parse tree
	 */
	void exitDrop_catalog(DatabricksParser.Drop_catalogContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_connection}.
	 * @param ctx the parse tree
	 */
	void enterDrop_connection(DatabricksParser.Drop_connectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_connection}.
	 * @param ctx the parse tree
	 */
	void exitDrop_connection(DatabricksParser.Drop_connectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_credential}.
	 * @param ctx the parse tree
	 */
	void enterDrop_credential(DatabricksParser.Drop_credentialContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_credential}.
	 * @param ctx the parse tree
	 */
	void exitDrop_credential(DatabricksParser.Drop_credentialContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_database}.
	 * @param ctx the parse tree
	 */
	void enterDrop_database(DatabricksParser.Drop_databaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_database}.
	 * @param ctx the parse tree
	 */
	void exitDrop_database(DatabricksParser.Drop_databaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_function}.
	 * @param ctx the parse tree
	 */
	void enterDrop_function(DatabricksParser.Drop_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_function}.
	 * @param ctx the parse tree
	 */
	void exitDrop_function(DatabricksParser.Drop_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_location}.
	 * @param ctx the parse tree
	 */
	void enterDrop_location(DatabricksParser.Drop_locationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_location}.
	 * @param ctx the parse tree
	 */
	void exitDrop_location(DatabricksParser.Drop_locationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_procedure}.
	 * @param ctx the parse tree
	 */
	void enterDrop_procedure(DatabricksParser.Drop_procedureContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_procedure}.
	 * @param ctx the parse tree
	 */
	void exitDrop_procedure(DatabricksParser.Drop_procedureContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_provider}.
	 * @param ctx the parse tree
	 */
	void enterDrop_provider(DatabricksParser.Drop_providerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_provider}.
	 * @param ctx the parse tree
	 */
	void exitDrop_provider(DatabricksParser.Drop_providerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_recipient}.
	 * @param ctx the parse tree
	 */
	void enterDrop_recipient(DatabricksParser.Drop_recipientContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_recipient}.
	 * @param ctx the parse tree
	 */
	void exitDrop_recipient(DatabricksParser.Drop_recipientContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_schema}.
	 * @param ctx the parse tree
	 */
	void enterDrop_schema(DatabricksParser.Drop_schemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_schema}.
	 * @param ctx the parse tree
	 */
	void exitDrop_schema(DatabricksParser.Drop_schemaContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_share}.
	 * @param ctx the parse tree
	 */
	void enterDrop_share(DatabricksParser.Drop_shareContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_share}.
	 * @param ctx the parse tree
	 */
	void exitDrop_share(DatabricksParser.Drop_shareContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_table}.
	 * @param ctx the parse tree
	 */
	void enterDrop_table(DatabricksParser.Drop_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_table}.
	 * @param ctx the parse tree
	 */
	void exitDrop_table(DatabricksParser.Drop_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_variable}.
	 * @param ctx the parse tree
	 */
	void enterDrop_variable(DatabricksParser.Drop_variableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_variable}.
	 * @param ctx the parse tree
	 */
	void exitDrop_variable(DatabricksParser.Drop_variableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_view}.
	 * @param ctx the parse tree
	 */
	void enterDrop_view(DatabricksParser.Drop_viewContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_view}.
	 * @param ctx the parse tree
	 */
	void exitDrop_view(DatabricksParser.Drop_viewContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_volume}.
	 * @param ctx the parse tree
	 */
	void enterDrop_volume(DatabricksParser.Drop_volumeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_volume}.
	 * @param ctx the parse tree
	 */
	void exitDrop_volume(DatabricksParser.Drop_volumeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#cascade_restrict}.
	 * @param ctx the parse tree
	 */
	void enterCascade_restrict(DatabricksParser.Cascade_restrictContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#cascade_restrict}.
	 * @param ctx the parse tree
	 */
	void exitCascade_restrict(DatabricksParser.Cascade_restrictContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#arg_types}.
	 * @param ctx the parse tree
	 */
	void enterArg_types(DatabricksParser.Arg_typesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#arg_types}.
	 * @param ctx the parse tree
	 */
	void exitArg_types(DatabricksParser.Arg_typesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#use_schema}.
	 * @param ctx the parse tree
	 */
	void enterUse_schema(DatabricksParser.Use_schemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#use_schema}.
	 * @param ctx the parse tree
	 */
	void exitUse_schema(DatabricksParser.Use_schemaContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#add_archive}.
	 * @param ctx the parse tree
	 */
	void enterAdd_archive(DatabricksParser.Add_archiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#add_archive}.
	 * @param ctx the parse tree
	 */
	void exitAdd_archive(DatabricksParser.Add_archiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#add_file}.
	 * @param ctx the parse tree
	 */
	void enterAdd_file(DatabricksParser.Add_fileContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#add_file}.
	 * @param ctx the parse tree
	 */
	void exitAdd_file(DatabricksParser.Add_fileContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#add_jar}.
	 * @param ctx the parse tree
	 */
	void enterAdd_jar(DatabricksParser.Add_jarContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#add_jar}.
	 * @param ctx the parse tree
	 */
	void exitAdd_jar(DatabricksParser.Add_jarContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#list_archive}.
	 * @param ctx the parse tree
	 */
	void enterList_archive(DatabricksParser.List_archiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#list_archive}.
	 * @param ctx the parse tree
	 */
	void exitList_archive(DatabricksParser.List_archiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#list_file}.
	 * @param ctx the parse tree
	 */
	void enterList_file(DatabricksParser.List_fileContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#list_file}.
	 * @param ctx the parse tree
	 */
	void exitList_file(DatabricksParser.List_fileContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#list_jar}.
	 * @param ctx the parse tree
	 */
	void enterList_jar(DatabricksParser.List_jarContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#list_jar}.
	 * @param ctx the parse tree
	 */
	void exitList_jar(DatabricksParser.List_jarContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#get}.
	 * @param ctx the parse tree
	 */
	void enterGet(DatabricksParser.GetContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#get}.
	 * @param ctx the parse tree
	 */
	void exitGet(DatabricksParser.GetContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#put_into}.
	 * @param ctx the parse tree
	 */
	void enterPut_into(DatabricksParser.Put_intoContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#put_into}.
	 * @param ctx the parse tree
	 */
	void exitPut_into(DatabricksParser.Put_intoContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#remove}.
	 * @param ctx the parse tree
	 */
	void enterRemove(DatabricksParser.RemoveContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#remove}.
	 * @param ctx the parse tree
	 */
	void exitRemove(DatabricksParser.RemoveContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#alter_group}.
	 * @param ctx the parse tree
	 */
	void enterAlter_group(DatabricksParser.Alter_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#alter_group}.
	 * @param ctx the parse tree
	 */
	void exitAlter_group(DatabricksParser.Alter_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#user_group}.
	 * @param ctx the parse tree
	 */
	void enterUser_group(DatabricksParser.User_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#user_group}.
	 * @param ctx the parse tree
	 */
	void exitUser_group(DatabricksParser.User_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#principal_list}.
	 * @param ctx the parse tree
	 */
	void enterPrincipal_list(DatabricksParser.Principal_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#principal_list}.
	 * @param ctx the parse tree
	 */
	void exitPrincipal_list(DatabricksParser.Principal_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#create_group}.
	 * @param ctx the parse tree
	 */
	void enterCreate_group(DatabricksParser.Create_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#create_group}.
	 * @param ctx the parse tree
	 */
	void exitCreate_group(DatabricksParser.Create_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#deny}.
	 * @param ctx the parse tree
	 */
	void enterDeny(DatabricksParser.DenyContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#deny}.
	 * @param ctx the parse tree
	 */
	void exitDeny(DatabricksParser.DenyContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#drop_group}.
	 * @param ctx the parse tree
	 */
	void enterDrop_group(DatabricksParser.Drop_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#drop_group}.
	 * @param ctx the parse tree
	 */
	void exitDrop_group(DatabricksParser.Drop_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#grant}.
	 * @param ctx the parse tree
	 */
	void enterGrant(DatabricksParser.GrantContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#grant}.
	 * @param ctx the parse tree
	 */
	void exitGrant(DatabricksParser.GrantContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#privilege_types}.
	 * @param ctx the parse tree
	 */
	void enterPrivilege_types(DatabricksParser.Privilege_typesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#privilege_types}.
	 * @param ctx the parse tree
	 */
	void exitPrivilege_types(DatabricksParser.Privilege_typesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#privilege_type_list}.
	 * @param ctx the parse tree
	 */
	void enterPrivilege_type_list(DatabricksParser.Privilege_type_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#privilege_type_list}.
	 * @param ctx the parse tree
	 */
	void exitPrivilege_type_list(DatabricksParser.Privilege_type_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#privilege_type}.
	 * @param ctx the parse tree
	 */
	void enterPrivilege_type(DatabricksParser.Privilege_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#privilege_type}.
	 * @param ctx the parse tree
	 */
	void exitPrivilege_type(DatabricksParser.Privilege_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#grant_share}.
	 * @param ctx the parse tree
	 */
	void enterGrant_share(DatabricksParser.Grant_shareContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#grant_share}.
	 * @param ctx the parse tree
	 */
	void exitGrant_share(DatabricksParser.Grant_shareContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#repair_privileges}.
	 * @param ctx the parse tree
	 */
	void enterRepair_privileges(DatabricksParser.Repair_privilegesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#repair_privileges}.
	 * @param ctx the parse tree
	 */
	void exitRepair_privileges(DatabricksParser.Repair_privilegesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#schema_database}.
	 * @param ctx the parse tree
	 */
	void enterSchema_database(DatabricksParser.Schema_databaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#schema_database}.
	 * @param ctx the parse tree
	 */
	void exitSchema_database(DatabricksParser.Schema_databaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#object}.
	 * @param ctx the parse tree
	 */
	void enterObject(DatabricksParser.ObjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#object}.
	 * @param ctx the parse tree
	 */
	void exitObject(DatabricksParser.ObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#revoke}.
	 * @param ctx the parse tree
	 */
	void enterRevoke(DatabricksParser.RevokeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#revoke}.
	 * @param ctx the parse tree
	 */
	void exitRevoke(DatabricksParser.RevokeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#revoke_share}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_share(DatabricksParser.Revoke_shareContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#revoke_share}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_share(DatabricksParser.Revoke_shareContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_grants}.
	 * @param ctx the parse tree
	 */
	void enterShow_grants(DatabricksParser.Show_grantsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_grants}.
	 * @param ctx the parse tree
	 */
	void exitShow_grants(DatabricksParser.Show_grantsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#securable_object}.
	 * @param ctx the parse tree
	 */
	void enterSecurable_object(DatabricksParser.Securable_objectContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#securable_object}.
	 * @param ctx the parse tree
	 */
	void exitSecurable_object(DatabricksParser.Securable_objectContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#storage_service}.
	 * @param ctx the parse tree
	 */
	void enterStorage_service(DatabricksParser.Storage_serviceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#storage_service}.
	 * @param ctx the parse tree
	 */
	void exitStorage_service(DatabricksParser.Storage_serviceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_grants_on_share}.
	 * @param ctx the parse tree
	 */
	void enterShow_grants_on_share(DatabricksParser.Show_grants_on_shareContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_grants_on_share}.
	 * @param ctx the parse tree
	 */
	void exitShow_grants_on_share(DatabricksParser.Show_grants_on_shareContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_grants_to_recipient}.
	 * @param ctx the parse tree
	 */
	void enterShow_grants_to_recipient(DatabricksParser.Show_grants_to_recipientContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_grants_to_recipient}.
	 * @param ctx the parse tree
	 */
	void exitShow_grants_to_recipient(DatabricksParser.Show_grants_to_recipientContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#comment_clause}.
	 * @param ctx the parse tree
	 */
	void enterComment_clause(DatabricksParser.Comment_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#comment_clause}.
	 * @param ctx the parse tree
	 */
	void exitComment_clause(DatabricksParser.Comment_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#inline_comment_clause}.
	 * @param ctx the parse tree
	 */
	void enterInline_comment_clause(DatabricksParser.Inline_comment_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#inline_comment_clause}.
	 * @param ctx the parse tree
	 */
	void exitInline_comment_clause(DatabricksParser.Inline_comment_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#if_exists}.
	 * @param ctx the parse tree
	 */
	void enterIf_exists(DatabricksParser.If_existsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#if_exists}.
	 * @param ctx the parse tree
	 */
	void exitIf_exists(DatabricksParser.If_existsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#if_not_exists}.
	 * @param ctx the parse tree
	 */
	void enterIf_not_exists(DatabricksParser.If_not_existsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#if_not_exists}.
	 * @param ctx the parse tree
	 */
	void exitIf_not_exists(DatabricksParser.If_not_existsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#or_replace}.
	 * @param ctx the parse tree
	 */
	void enterOr_replace(DatabricksParser.Or_replaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#or_replace}.
	 * @param ctx the parse tree
	 */
	void exitOr_replace(DatabricksParser.Or_replaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#describe}.
	 * @param ctx the parse tree
	 */
	void enterDescribe(DatabricksParser.DescribeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#describe}.
	 * @param ctx the parse tree
	 */
	void exitDescribe(DatabricksParser.DescribeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#describe_catalog}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_catalog(DatabricksParser.Describe_catalogContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#describe_catalog}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_catalog(DatabricksParser.Describe_catalogContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#describe_connection}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_connection(DatabricksParser.Describe_connectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#describe_connection}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_connection(DatabricksParser.Describe_connectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#describe_credential}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_credential(DatabricksParser.Describe_credentialContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#describe_credential}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_credential(DatabricksParser.Describe_credentialContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#describe_database}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_database(DatabricksParser.Describe_databaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#describe_database}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_database(DatabricksParser.Describe_databaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#describe_function}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_function(DatabricksParser.Describe_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#describe_function}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_function(DatabricksParser.Describe_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#describe_location}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_location(DatabricksParser.Describe_locationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#describe_location}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_location(DatabricksParser.Describe_locationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#describe_procedure}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_procedure(DatabricksParser.Describe_procedureContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#describe_procedure}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_procedure(DatabricksParser.Describe_procedureContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#describe_provider}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_provider(DatabricksParser.Describe_providerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#describe_provider}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_provider(DatabricksParser.Describe_providerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#describe_query}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_query(DatabricksParser.Describe_queryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#describe_query}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_query(DatabricksParser.Describe_queryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#describe_recipient}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_recipient(DatabricksParser.Describe_recipientContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#describe_recipient}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_recipient(DatabricksParser.Describe_recipientContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#describe_schema}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_schema(DatabricksParser.Describe_schemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#describe_schema}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_schema(DatabricksParser.Describe_schemaContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#describe_share}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_share(DatabricksParser.Describe_shareContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#describe_share}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_share(DatabricksParser.Describe_shareContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#describe_table}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_table(DatabricksParser.Describe_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#describe_table}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_table(DatabricksParser.Describe_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#describe_volume}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_volume(DatabricksParser.Describe_volumeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#describe_volume}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_volume(DatabricksParser.Describe_volumeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#list}.
	 * @param ctx the parse tree
	 */
	void enterList(DatabricksParser.ListContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#list}.
	 * @param ctx the parse tree
	 */
	void exitList(DatabricksParser.ListContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_all_in_share}.
	 * @param ctx the parse tree
	 */
	void enterShow_all_in_share(DatabricksParser.Show_all_in_shareContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_all_in_share}.
	 * @param ctx the parse tree
	 */
	void exitShow_all_in_share(DatabricksParser.Show_all_in_shareContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_catalogs}.
	 * @param ctx the parse tree
	 */
	void enterShow_catalogs(DatabricksParser.Show_catalogsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_catalogs}.
	 * @param ctx the parse tree
	 */
	void exitShow_catalogs(DatabricksParser.Show_catalogsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_columns}.
	 * @param ctx the parse tree
	 */
	void enterShow_columns(DatabricksParser.Show_columnsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_columns}.
	 * @param ctx the parse tree
	 */
	void exitShow_columns(DatabricksParser.Show_columnsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#in_from}.
	 * @param ctx the parse tree
	 */
	void enterIn_from(DatabricksParser.In_fromContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#in_from}.
	 * @param ctx the parse tree
	 */
	void exitIn_from(DatabricksParser.In_fromContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_connections}.
	 * @param ctx the parse tree
	 */
	void enterShow_connections(DatabricksParser.Show_connectionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_connections}.
	 * @param ctx the parse tree
	 */
	void exitShow_connections(DatabricksParser.Show_connectionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_create_table}.
	 * @param ctx the parse tree
	 */
	void enterShow_create_table(DatabricksParser.Show_create_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_create_table}.
	 * @param ctx the parse tree
	 */
	void exitShow_create_table(DatabricksParser.Show_create_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_credentials}.
	 * @param ctx the parse tree
	 */
	void enterShow_credentials(DatabricksParser.Show_credentialsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_credentials}.
	 * @param ctx the parse tree
	 */
	void exitShow_credentials(DatabricksParser.Show_credentialsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_databases}.
	 * @param ctx the parse tree
	 */
	void enterShow_databases(DatabricksParser.Show_databasesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_databases}.
	 * @param ctx the parse tree
	 */
	void exitShow_databases(DatabricksParser.Show_databasesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_functions}.
	 * @param ctx the parse tree
	 */
	void enterShow_functions(DatabricksParser.Show_functionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_functions}.
	 * @param ctx the parse tree
	 */
	void exitShow_functions(DatabricksParser.Show_functionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#function_kind}.
	 * @param ctx the parse tree
	 */
	void enterFunction_kind(DatabricksParser.Function_kindContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#function_kind}.
	 * @param ctx the parse tree
	 */
	void exitFunction_kind(DatabricksParser.Function_kindContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_groups}.
	 * @param ctx the parse tree
	 */
	void enterShow_groups(DatabricksParser.Show_groupsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_groups}.
	 * @param ctx the parse tree
	 */
	void exitShow_groups(DatabricksParser.Show_groupsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_locations}.
	 * @param ctx the parse tree
	 */
	void enterShow_locations(DatabricksParser.Show_locationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_locations}.
	 * @param ctx the parse tree
	 */
	void exitShow_locations(DatabricksParser.Show_locationsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_partitions}.
	 * @param ctx the parse tree
	 */
	void enterShow_partitions(DatabricksParser.Show_partitionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_partitions}.
	 * @param ctx the parse tree
	 */
	void exitShow_partitions(DatabricksParser.Show_partitionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_procedures}.
	 * @param ctx the parse tree
	 */
	void enterShow_procedures(DatabricksParser.Show_proceduresContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_procedures}.
	 * @param ctx the parse tree
	 */
	void exitShow_procedures(DatabricksParser.Show_proceduresContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_providers}.
	 * @param ctx the parse tree
	 */
	void enterShow_providers(DatabricksParser.Show_providersContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_providers}.
	 * @param ctx the parse tree
	 */
	void exitShow_providers(DatabricksParser.Show_providersContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_recipients}.
	 * @param ctx the parse tree
	 */
	void enterShow_recipients(DatabricksParser.Show_recipientsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_recipients}.
	 * @param ctx the parse tree
	 */
	void exitShow_recipients(DatabricksParser.Show_recipientsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_schemas}.
	 * @param ctx the parse tree
	 */
	void enterShow_schemas(DatabricksParser.Show_schemasContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_schemas}.
	 * @param ctx the parse tree
	 */
	void exitShow_schemas(DatabricksParser.Show_schemasContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_shares}.
	 * @param ctx the parse tree
	 */
	void enterShow_shares(DatabricksParser.Show_sharesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_shares}.
	 * @param ctx the parse tree
	 */
	void exitShow_shares(DatabricksParser.Show_sharesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_shares_in_provider}.
	 * @param ctx the parse tree
	 */
	void enterShow_shares_in_provider(DatabricksParser.Show_shares_in_providerContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_shares_in_provider}.
	 * @param ctx the parse tree
	 */
	void exitShow_shares_in_provider(DatabricksParser.Show_shares_in_providerContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_table}.
	 * @param ctx the parse tree
	 */
	void enterShow_table(DatabricksParser.Show_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_table}.
	 * @param ctx the parse tree
	 */
	void exitShow_table(DatabricksParser.Show_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_tables}.
	 * @param ctx the parse tree
	 */
	void enterShow_tables(DatabricksParser.Show_tablesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_tables}.
	 * @param ctx the parse tree
	 */
	void exitShow_tables(DatabricksParser.Show_tablesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_tables_dropped}.
	 * @param ctx the parse tree
	 */
	void enterShow_tables_dropped(DatabricksParser.Show_tables_droppedContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_tables_dropped}.
	 * @param ctx the parse tree
	 */
	void exitShow_tables_dropped(DatabricksParser.Show_tables_droppedContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_tblproperties}.
	 * @param ctx the parse tree
	 */
	void enterShow_tblproperties(DatabricksParser.Show_tblpropertiesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_tblproperties}.
	 * @param ctx the parse tree
	 */
	void exitShow_tblproperties(DatabricksParser.Show_tblpropertiesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_users}.
	 * @param ctx the parse tree
	 */
	void enterShow_users(DatabricksParser.Show_usersContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_users}.
	 * @param ctx the parse tree
	 */
	void exitShow_users(DatabricksParser.Show_usersContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_views}.
	 * @param ctx the parse tree
	 */
	void enterShow_views(DatabricksParser.Show_viewsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_views}.
	 * @param ctx the parse tree
	 */
	void exitShow_views(DatabricksParser.Show_viewsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#show_volumes}.
	 * @param ctx the parse tree
	 */
	void enterShow_volumes(DatabricksParser.Show_volumesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#show_volumes}.
	 * @param ctx the parse tree
	 */
	void exitShow_volumes(DatabricksParser.Show_volumesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#like_pattern}.
	 * @param ctx the parse tree
	 */
	void enterLike_pattern(DatabricksParser.Like_patternContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#like_pattern}.
	 * @param ctx the parse tree
	 */
	void exitLike_pattern(DatabricksParser.Like_patternContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#schema_name}.
	 * @param ctx the parse tree
	 */
	void enterSchema_name(DatabricksParser.Schema_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#schema_name}.
	 * @param ctx the parse tree
	 */
	void exitSchema_name(DatabricksParser.Schema_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#id_clause}.
	 * @param ctx the parse tree
	 */
	void enterId_clause(DatabricksParser.Id_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#id_clause}.
	 * @param ctx the parse tree
	 */
	void exitId_clause(DatabricksParser.Id_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#string}.
	 * @param ctx the parse tree
	 */
	void enterString(DatabricksParser.StringContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#string}.
	 * @param ctx the parse tree
	 */
	void exitString(DatabricksParser.StringContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#id_}.
	 * @param ctx the parse tree
	 */
	void enterId_(DatabricksParser.Id_Context ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#id_}.
	 * @param ctx the parse tree
	 */
	void exitId_(DatabricksParser.Id_Context ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#keyword}.
	 * @param ctx the parse tree
	 */
	void enterKeyword(DatabricksParser.KeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#keyword}.
	 * @param ctx the parse tree
	 */
	void exitKeyword(DatabricksParser.KeywordContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#non_reserved_words}.
	 * @param ctx the parse tree
	 */
	void enterNon_reserved_words(DatabricksParser.Non_reserved_wordsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#non_reserved_words}.
	 * @param ctx the parse tree
	 */
	void exitNon_reserved_words(DatabricksParser.Non_reserved_wordsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#column_name}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name(DatabricksParser.Column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#column_name}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name(DatabricksParser.Column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#column_list}.
	 * @param ctx the parse tree
	 */
	void enterColumn_list(DatabricksParser.Column_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#column_list}.
	 * @param ctx the parse tree
	 */
	void exitColumn_list(DatabricksParser.Column_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#object_name}.
	 * @param ctx the parse tree
	 */
	void enterObject_name(DatabricksParser.Object_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#object_name}.
	 * @param ctx the parse tree
	 */
	void exitObject_name(DatabricksParser.Object_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#num}.
	 * @param ctx the parse tree
	 */
	void enterNum(DatabricksParser.NumContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#num}.
	 * @param ctx the parse tree
	 */
	void exitNum(DatabricksParser.NumContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#expr_list}.
	 * @param ctx the parse tree
	 */
	void enterExpr_list(DatabricksParser.Expr_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#expr_list}.
	 * @param ctx the parse tree
	 */
	void exitExpr_list(DatabricksParser.Expr_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#expr_list_in_parentheses}.
	 * @param ctx the parse tree
	 */
	void enterExpr_list_in_parentheses(DatabricksParser.Expr_list_in_parenthesesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#expr_list_in_parentheses}.
	 * @param ctx the parse tree
	 */
	void exitExpr_list_in_parentheses(DatabricksParser.Expr_list_in_parenthesesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(DatabricksParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(DatabricksParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#cast_expr}.
	 * @param ctx the parse tree
	 */
	void enterCast_expr(DatabricksParser.Cast_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#cast_expr}.
	 * @param ctx the parse tree
	 */
	void exitCast_expr(DatabricksParser.Cast_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#data_type_size}.
	 * @param ctx the parse tree
	 */
	void enterData_type_size(DatabricksParser.Data_type_sizeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#data_type_size}.
	 * @param ctx the parse tree
	 */
	void exitData_type_size(DatabricksParser.Data_type_sizeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#data_type}.
	 * @param ctx the parse tree
	 */
	void enterData_type(DatabricksParser.Data_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#data_type}.
	 * @param ctx the parse tree
	 */
	void exitData_type(DatabricksParser.Data_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#primitive_expression}.
	 * @param ctx the parse tree
	 */
	void enterPrimitive_expression(DatabricksParser.Primitive_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#primitive_expression}.
	 * @param ctx the parse tree
	 */
	void exitPrimitive_expression(DatabricksParser.Primitive_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#function_call}.
	 * @param ctx the parse tree
	 */
	void enterFunction_call(DatabricksParser.Function_callContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#function_call}.
	 * @param ctx the parse tree
	 */
	void exitFunction_call(DatabricksParser.Function_callContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#param_assoc_list}.
	 * @param ctx the parse tree
	 */
	void enterParam_assoc_list(DatabricksParser.Param_assoc_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#param_assoc_list}.
	 * @param ctx the parse tree
	 */
	void exitParam_assoc_list(DatabricksParser.Param_assoc_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#param_assoc}.
	 * @param ctx the parse tree
	 */
	void enterParam_assoc(DatabricksParser.Param_assocContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#param_assoc}.
	 * @param ctx the parse tree
	 */
	void exitParam_assoc(DatabricksParser.Param_assocContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(DatabricksParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(DatabricksParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#sign}.
	 * @param ctx the parse tree
	 */
	void enterSign(DatabricksParser.SignContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#sign}.
	 * @param ctx the parse tree
	 */
	void exitSign(DatabricksParser.SignContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#full_column_name}.
	 * @param ctx the parse tree
	 */
	void enterFull_column_name(DatabricksParser.Full_column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#full_column_name}.
	 * @param ctx the parse tree
	 */
	void exitFull_column_name(DatabricksParser.Full_column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#bracket_expression}.
	 * @param ctx the parse tree
	 */
	void enterBracket_expression(DatabricksParser.Bracket_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#bracket_expression}.
	 * @param ctx the parse tree
	 */
	void exitBracket_expression(DatabricksParser.Bracket_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#case_expression}.
	 * @param ctx the parse tree
	 */
	void enterCase_expression(DatabricksParser.Case_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#case_expression}.
	 * @param ctx the parse tree
	 */
	void exitCase_expression(DatabricksParser.Case_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#switch_search_condition_section}.
	 * @param ctx the parse tree
	 */
	void enterSwitch_search_condition_section(DatabricksParser.Switch_search_condition_sectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#switch_search_condition_section}.
	 * @param ctx the parse tree
	 */
	void exitSwitch_search_condition_section(DatabricksParser.Switch_search_condition_sectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#switch_section}.
	 * @param ctx the parse tree
	 */
	void enterSwitch_section(DatabricksParser.Switch_sectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#switch_section}.
	 * @param ctx the parse tree
	 */
	void exitSwitch_section(DatabricksParser.Switch_sectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#query_statement}.
	 * @param ctx the parse tree
	 */
	void enterQuery_statement(DatabricksParser.Query_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#query_statement}.
	 * @param ctx the parse tree
	 */
	void exitQuery_statement(DatabricksParser.Query_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#with_expression}.
	 * @param ctx the parse tree
	 */
	void enterWith_expression(DatabricksParser.With_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#with_expression}.
	 * @param ctx the parse tree
	 */
	void exitWith_expression(DatabricksParser.With_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#common_table_expression}.
	 * @param ctx the parse tree
	 */
	void enterCommon_table_expression(DatabricksParser.Common_table_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#common_table_expression}.
	 * @param ctx the parse tree
	 */
	void exitCommon_table_expression(DatabricksParser.Common_table_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#recursion_limit}.
	 * @param ctx the parse tree
	 */
	void enterRecursion_limit(DatabricksParser.Recursion_limitContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#recursion_limit}.
	 * @param ctx the parse tree
	 */
	void exitRecursion_limit(DatabricksParser.Recursion_limitContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#recursive_query}.
	 * @param ctx the parse tree
	 */
	void enterRecursive_query(DatabricksParser.Recursive_queryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#recursive_query}.
	 * @param ctx the parse tree
	 */
	void exitRecursive_query(DatabricksParser.Recursive_queryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#subquery}.
	 * @param ctx the parse tree
	 */
	void enterSubquery(DatabricksParser.SubqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#subquery}.
	 * @param ctx the parse tree
	 */
	void exitSubquery(DatabricksParser.SubqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#set_operator}.
	 * @param ctx the parse tree
	 */
	void enterSet_operator(DatabricksParser.Set_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#set_operator}.
	 * @param ctx the parse tree
	 */
	void exitSet_operator(DatabricksParser.Set_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#order_by_clause}.
	 * @param ctx the parse tree
	 */
	void enterOrder_by_clause(DatabricksParser.Order_by_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#order_by_clause}.
	 * @param ctx the parse tree
	 */
	void exitOrder_by_clause(DatabricksParser.Order_by_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#order_by_item}.
	 * @param ctx the parse tree
	 */
	void enterOrder_by_item(DatabricksParser.Order_by_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#order_by_item}.
	 * @param ctx the parse tree
	 */
	void exitOrder_by_item(DatabricksParser.Order_by_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#sort_direction}.
	 * @param ctx the parse tree
	 */
	void enterSort_direction(DatabricksParser.Sort_directionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#sort_direction}.
	 * @param ctx the parse tree
	 */
	void exitSort_direction(DatabricksParser.Sort_directionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#nulls_sort_order}.
	 * @param ctx the parse tree
	 */
	void enterNulls_sort_order(DatabricksParser.Nulls_sort_orderContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#nulls_sort_order}.
	 * @param ctx the parse tree
	 */
	void exitNulls_sort_order(DatabricksParser.Nulls_sort_orderContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#distribute_by_clause}.
	 * @param ctx the parse tree
	 */
	void enterDistribute_by_clause(DatabricksParser.Distribute_by_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#distribute_by_clause}.
	 * @param ctx the parse tree
	 */
	void exitDistribute_by_clause(DatabricksParser.Distribute_by_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#sort_by_clause}.
	 * @param ctx the parse tree
	 */
	void enterSort_by_clause(DatabricksParser.Sort_by_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#sort_by_clause}.
	 * @param ctx the parse tree
	 */
	void exitSort_by_clause(DatabricksParser.Sort_by_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#sort_by_item}.
	 * @param ctx the parse tree
	 */
	void enterSort_by_item(DatabricksParser.Sort_by_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#sort_by_item}.
	 * @param ctx the parse tree
	 */
	void exitSort_by_item(DatabricksParser.Sort_by_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#cluster_by_clause}.
	 * @param ctx the parse tree
	 */
	void enterCluster_by_clause(DatabricksParser.Cluster_by_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#cluster_by_clause}.
	 * @param ctx the parse tree
	 */
	void exitCluster_by_clause(DatabricksParser.Cluster_by_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#window_clause}.
	 * @param ctx the parse tree
	 */
	void enterWindow_clause(DatabricksParser.Window_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#window_clause}.
	 * @param ctx the parse tree
	 */
	void exitWindow_clause(DatabricksParser.Window_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#window_name}.
	 * @param ctx the parse tree
	 */
	void enterWindow_name(DatabricksParser.Window_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#window_name}.
	 * @param ctx the parse tree
	 */
	void exitWindow_name(DatabricksParser.Window_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#window_spec}.
	 * @param ctx the parse tree
	 */
	void enterWindow_spec(DatabricksParser.Window_specContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#window_spec}.
	 * @param ctx the parse tree
	 */
	void exitWindow_spec(DatabricksParser.Window_specContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#limit_clause}.
	 * @param ctx the parse tree
	 */
	void enterLimit_clause(DatabricksParser.Limit_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#limit_clause}.
	 * @param ctx the parse tree
	 */
	void exitLimit_clause(DatabricksParser.Limit_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#offset_clause}.
	 * @param ctx the parse tree
	 */
	void enterOffset_clause(DatabricksParser.Offset_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#offset_clause}.
	 * @param ctx the parse tree
	 */
	void exitOffset_clause(DatabricksParser.Offset_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#select_statement}.
	 * @param ctx the parse tree
	 */
	void enterSelect_statement(DatabricksParser.Select_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#select_statement}.
	 * @param ctx the parse tree
	 */
	void exitSelect_statement(DatabricksParser.Select_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#select_clause}.
	 * @param ctx the parse tree
	 */
	void enterSelect_clause(DatabricksParser.Select_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#select_clause}.
	 * @param ctx the parse tree
	 */
	void exitSelect_clause(DatabricksParser.Select_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#hints}.
	 * @param ctx the parse tree
	 */
	void enterHints(DatabricksParser.HintsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#hints}.
	 * @param ctx the parse tree
	 */
	void exitHints(DatabricksParser.HintsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#select_item}.
	 * @param ctx the parse tree
	 */
	void enterSelect_item(DatabricksParser.Select_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#select_item}.
	 * @param ctx the parse tree
	 */
	void exitSelect_item(DatabricksParser.Select_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#named_expression}.
	 * @param ctx the parse tree
	 */
	void enterNamed_expression(DatabricksParser.Named_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#named_expression}.
	 * @param ctx the parse tree
	 */
	void exitNamed_expression(DatabricksParser.Named_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#star_clause}.
	 * @param ctx the parse tree
	 */
	void enterStar_clause(DatabricksParser.Star_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#star_clause}.
	 * @param ctx the parse tree
	 */
	void exitStar_clause(DatabricksParser.Star_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#except_clause}.
	 * @param ctx the parse tree
	 */
	void enterExcept_clause(DatabricksParser.Except_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#except_clause}.
	 * @param ctx the parse tree
	 */
	void exitExcept_clause(DatabricksParser.Except_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#table_reference}.
	 * @param ctx the parse tree
	 */
	void enterTable_reference(DatabricksParser.Table_referenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#table_reference}.
	 * @param ctx the parse tree
	 */
	void exitTable_reference(DatabricksParser.Table_referenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#join_clause}.
	 * @param ctx the parse tree
	 */
	void enterJoin_clause(DatabricksParser.Join_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#join_clause}.
	 * @param ctx the parse tree
	 */
	void exitJoin_clause(DatabricksParser.Join_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#join_type}.
	 * @param ctx the parse tree
	 */
	void enterJoin_type(DatabricksParser.Join_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#join_type}.
	 * @param ctx the parse tree
	 */
	void exitJoin_type(DatabricksParser.Join_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#pivot_clause}.
	 * @param ctx the parse tree
	 */
	void enterPivot_clause(DatabricksParser.Pivot_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#pivot_clause}.
	 * @param ctx the parse tree
	 */
	void exitPivot_clause(DatabricksParser.Pivot_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#expr_list_with_alias}.
	 * @param ctx the parse tree
	 */
	void enterExpr_list_with_alias(DatabricksParser.Expr_list_with_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#expr_list_with_alias}.
	 * @param ctx the parse tree
	 */
	void exitExpr_list_with_alias(DatabricksParser.Expr_list_with_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#unpivot_clause}.
	 * @param ctx the parse tree
	 */
	void enterUnpivot_clause(DatabricksParser.Unpivot_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#unpivot_clause}.
	 * @param ctx the parse tree
	 */
	void exitUnpivot_clause(DatabricksParser.Unpivot_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#single_value}.
	 * @param ctx the parse tree
	 */
	void enterSingle_value(DatabricksParser.Single_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#single_value}.
	 * @param ctx the parse tree
	 */
	void exitSingle_value(DatabricksParser.Single_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#multi_value}.
	 * @param ctx the parse tree
	 */
	void enterMulti_value(DatabricksParser.Multi_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#multi_value}.
	 * @param ctx the parse tree
	 */
	void exitMulti_value(DatabricksParser.Multi_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#column_list_with_aliases}.
	 * @param ctx the parse tree
	 */
	void enterColumn_list_with_aliases(DatabricksParser.Column_list_with_aliasesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#column_list_with_aliases}.
	 * @param ctx the parse tree
	 */
	void exitColumn_list_with_aliases(DatabricksParser.Column_list_with_aliasesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#include_exclude}.
	 * @param ctx the parse tree
	 */
	void enterInclude_exclude(DatabricksParser.Include_excludeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#include_exclude}.
	 * @param ctx the parse tree
	 */
	void exitInclude_exclude(DatabricksParser.Include_excludeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#table_valued_function}.
	 * @param ctx the parse tree
	 */
	void enterTable_valued_function(DatabricksParser.Table_valued_functionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#table_valued_function}.
	 * @param ctx the parse tree
	 */
	void exitTable_valued_function(DatabricksParser.Table_valued_functionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#table_name_with_optional_parentheses}.
	 * @param ctx the parse tree
	 */
	void enterTable_name_with_optional_parentheses(DatabricksParser.Table_name_with_optional_parenthesesContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#table_name_with_optional_parentheses}.
	 * @param ctx the parse tree
	 */
	void exitTable_name_with_optional_parentheses(DatabricksParser.Table_name_with_optional_parenthesesContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#tablesample_clause}.
	 * @param ctx the parse tree
	 */
	void enterTablesample_clause(DatabricksParser.Tablesample_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#tablesample_clause}.
	 * @param ctx the parse tree
	 */
	void exitTablesample_clause(DatabricksParser.Tablesample_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#table_alias}.
	 * @param ctx the parse tree
	 */
	void enterTable_alias(DatabricksParser.Table_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#table_alias}.
	 * @param ctx the parse tree
	 */
	void exitTable_alias(DatabricksParser.Table_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#lateral_view_clause}.
	 * @param ctx the parse tree
	 */
	void enterLateral_view_clause(DatabricksParser.Lateral_view_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#lateral_view_clause}.
	 * @param ctx the parse tree
	 */
	void exitLateral_view_clause(DatabricksParser.Lateral_view_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#table_identifier}.
	 * @param ctx the parse tree
	 */
	void enterTable_identifier(DatabricksParser.Table_identifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#table_identifier}.
	 * @param ctx the parse tree
	 */
	void exitTable_identifier(DatabricksParser.Table_identifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhere_clause(DatabricksParser.Where_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhere_clause(DatabricksParser.Where_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#group_by_clause}.
	 * @param ctx the parse tree
	 */
	void enterGroup_by_clause(DatabricksParser.Group_by_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#group_by_clause}.
	 * @param ctx the parse tree
	 */
	void exitGroup_by_clause(DatabricksParser.Group_by_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#group_by_item_list}.
	 * @param ctx the parse tree
	 */
	void enterGroup_by_item_list(DatabricksParser.Group_by_item_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#group_by_item_list}.
	 * @param ctx the parse tree
	 */
	void exitGroup_by_item_list(DatabricksParser.Group_by_item_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#group_by_item}.
	 * @param ctx the parse tree
	 */
	void enterGroup_by_item(DatabricksParser.Group_by_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#group_by_item}.
	 * @param ctx the parse tree
	 */
	void exitGroup_by_item(DatabricksParser.Group_by_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#grouping_set}.
	 * @param ctx the parse tree
	 */
	void enterGrouping_set(DatabricksParser.Grouping_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#grouping_set}.
	 * @param ctx the parse tree
	 */
	void exitGrouping_set(DatabricksParser.Grouping_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#having_clause}.
	 * @param ctx the parse tree
	 */
	void enterHaving_clause(DatabricksParser.Having_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#having_clause}.
	 * @param ctx the parse tree
	 */
	void exitHaving_clause(DatabricksParser.Having_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#qualify_clause}.
	 * @param ctx the parse tree
	 */
	void enterQualify_clause(DatabricksParser.Qualify_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#qualify_clause}.
	 * @param ctx the parse tree
	 */
	void exitQualify_clause(DatabricksParser.Qualify_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#values_statement}.
	 * @param ctx the parse tree
	 */
	void enterValues_statement(DatabricksParser.Values_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#values_statement}.
	 * @param ctx the parse tree
	 */
	void exitValues_statement(DatabricksParser.Values_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#values_item}.
	 * @param ctx the parse tree
	 */
	void enterValues_item(DatabricksParser.Values_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#values_item}.
	 * @param ctx the parse tree
	 */
	void exitValues_item(DatabricksParser.Values_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#sql_pipeline}.
	 * @param ctx the parse tree
	 */
	void enterSql_pipeline(DatabricksParser.Sql_pipelineContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#sql_pipeline}.
	 * @param ctx the parse tree
	 */
	void exitSql_pipeline(DatabricksParser.Sql_pipelineContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#explain_statement}.
	 * @param ctx the parse tree
	 */
	void enterExplain_statement(DatabricksParser.Explain_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#explain_statement}.
	 * @param ctx the parse tree
	 */
	void exitExplain_statement(DatabricksParser.Explain_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#as_alias}.
	 * @param ctx the parse tree
	 */
	void enterAs_alias(DatabricksParser.As_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#as_alias}.
	 * @param ctx the parse tree
	 */
	void exitAs_alias(DatabricksParser.As_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(DatabricksParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(DatabricksParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatabricksParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void enterComparison_operator(DatabricksParser.Comparison_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatabricksParser#comparison_operator}.
	 * @param ctx the parse tree
	 */
	void exitComparison_operator(DatabricksParser.Comparison_operatorContext ctx);
}