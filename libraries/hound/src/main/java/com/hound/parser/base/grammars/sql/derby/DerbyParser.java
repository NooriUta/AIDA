// Generated from C:/AIDA/aida-root/libraries/hound/src/main/resources/grammars/sql/derby/DerbyParser.g4 by ANTLR 4.13.2
package com.hound.parser.base.grammars.sql.derby;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class DerbyParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		ABS=1, ABSVAL=2, ACOS=3, ACTION=4, ADD=5, AGGREGATE=6, ALL=7, ALLOCATE=8, 
		ALTER=9, ALWAYS=10, AND=11, ANY=12, ARE=13, AS=14, ASC=15, ASIN=16, ASSERTION=17, 
		AT=18, ATAN2=19, ATAN=20, AUTHORIZATION=21, AVG=22, BEGIN=23, BETWEEN=24, 
		BIGINT=25, BINARY=26, BIT=27, BLOB=28, BOOLEAN=29, BOTH=30, BY=31, CALL=32, 
		CALLED=33, CASCADE=34, CASCADED=35, CASE=36, CAST=37, CEIL=38, CEILING=39, 
		CHAR=40, CHARACTER=41, CHARACTER_LENGTH=42, CHECK=43, CLOB=44, CLOSE=45, 
		COALESCE=46, COLLATE=47, COLLATION=48, COLUMN=49, COMMIT=50, COMMITTED=51, 
		CONNECT=52, CONNECTION=53, CONSTRAINT=54, CONSTRAINTS=55, CONTAINS=56, 
		CONTINUE=57, CONVERT=58, CORRESPONDING=59, COS=60, COUNT=61, CREATE=62, 
		CROSS=63, CS=64, CURRENT=65, CURRENT_DATE=66, CURRENT_ROLE=67, CURRENT_TIME=68, 
		CURRENT_TIMESTAMP=69, CURRENT_USER=70, CURSOR=71, CYCLE=72, DATA=73, DATE=74, 
		DAY=75, DEALLOCATE=76, DEC=77, DECIMAL=78, DECLARE=79, DEFAULT=80, DEFERRABLE=81, 
		DEFERRED=82, DEFINER=83, DEGREES=84, DELETE=85, DERBY=86, DERBY_JDBC_RESULT_SET=87, 
		DESC=88, DESCRIBE=89, DETERMINISTIC=90, DIAGNOSTICS=91, DIRTY=92, DISCONNECT=93, 
		DISTINCT=94, DOUBLE=95, DROP=96, DYNAMIC=97, ELSE=98, END=99, END_EXEC=100, 
		ESCAPE=101, EXCEPT=102, EXCEPTION=103, EXCLUSIVE=104, EXEC=105, EXECUTE=106, 
		EXISTS=107, EXP=108, EXPLAIN=109, EXTERNAL=110, FALSE=111, FETCH=112, 
		FIRST=113, FLOAT=114, FLOOR=115, FOR=116, FOREIGN=117, FOUND=118, FROM=119, 
		FULL=120, FUNCTION=121, GENERATED=122, GET=123, GETCURRENTCONNECTION=124, 
		GIGA=125, GLOBAL=126, GO=127, GOTO=128, GRANT=129, GROUP=130, HAVING=131, 
		HOUR=132, IDENTITY=133, IDENTITY_VAL_LOCAL=134, IMMEDIATE=135, IN=136, 
		INCREMENT=137, INDEX=138, INDICATOR=139, INITIALLY=140, INNER=141, INOUT=142, 
		INPUT=143, INSENSITIVE=144, INSERT=145, INT=146, INTEGER=147, INTERSECT=148, 
		INTO=149, INVOKER=150, IS=151, ISOLATION=152, JAVA=153, JOIN=154, KEY=155, 
		KILO=156, LANGUAGE=157, LARGE=158, LAST=159, LCASE=160, LEADING=161, LEFT=162, 
		LENGTH=163, LIKE=164, LN=165, LOCATE=166, LOCK=167, LOCKSIZE=168, LOG10=169, 
		LOG=170, LOGGED=171, LONG=172, LOWER=173, LTRIM=174, MATCH=175, MATCHED=176, 
		MAX=177, MAXVALUE=178, MEGA=179, MERGE=180, MIN=181, MINUTE=182, MINVALUE=183, 
		MOD=184, MODE=185, MODIFIES=186, MONTH=187, NAME=188, NATIONAL=189, NATURAL=190, 
		NCHAR=191, NEXT=192, NO=193, NONE=194, NOT=195, NULL_=196, NULLIF=197, 
		NULLS=198, NUMERIC=199, NVARCHAR=200, OBJECT=201, OF=202, OFFSET=203, 
		ON=204, ONLY=205, OPEN=206, OPTION=207, OR=208, ORDER=209, OUT=210, OUTER=211, 
		OUTPUT=212, OVERLAPS=213, PAD=214, PARAMETER=215, PARTIAL=216, PI=217, 
		PRECISION=218, PREPARE=219, PRESERVE=220, PRIMARY=221, PRIOR=222, PRIVILEGES=223, 
		PROCEDURE=224, PUBLIC=225, RADIANS=226, READ=227, READS=228, REAL=229, 
		REFERENCES=230, RELATIVE=231, RENAME=232, REPEATABLE=233, RESET=234, RESTART=235, 
		RESTRICT=236, RESULT=237, RETURNS=238, REVOKE=239, RIGHT=240, ROLE=241, 
		ROLLBACK=242, ROLLUP=243, ROW=244, ROWS=245, RR=246, RS=247, RTRIM=248, 
		SCHEMA=249, SCROLL=250, SECOND=251, SECURITY=252, SELECT=253, SEQUENCE=254, 
		SERIALIZABLE=255, SESSION_USER=256, SET=257, SETS=258, SHARE=259, SIN=260, 
		SMALLINT=261, SOME=262, SPACE=263, SQL=264, SQLCODE=265, SQLERROR=266, 
		SQLID=267, SQLSTATE=268, SQRT=269, STABILITY=270, START=271, STDDEV_POP=272, 
		STDDEV_SAMP=273, STYLE=274, SUBSTR=275, SUBSTRING=276, SUM=277, SYNONYM=278, 
		SYSTEM_USER=279, TABLE=280, TAN=281, TEMPORARY=282, THEN=283, TIME=284, 
		TIMESTAMP=285, TIMEZONE_HOUR=286, TIMEZONE_MINUTE=287, TO=288, TRANSACTION=289, 
		TRANSLATE=290, TRANSLATION=291, TRIGGER=292, TRIM=293, TRUE=294, TRUNCATE=295, 
		TYPE=296, UCASE=297, UNCOMMITTED=298, UNION=299, UNIQUE=300, UNKNOWN=301, 
		UPDATE=302, UPPER=303, UR=304, USAGE=305, USER=306, USING=307, VALUES=308, 
		VAR_POP=309, VAR_SAMP=310, VARCHAR=311, VARYING=312, VIEW=313, WHEN=314, 
		WHENEVER=315, WHERE=316, WINDOW=317, WITH=318, WORK=319, WRITE=320, XML=321, 
		XMLEXISTS=322, XMLPARSE=323, XMLQUERY=324, XMLSERIALIZE=325, YEAR=326, 
		WHITE_SPACE=327, SQL_COMMENT=328, LINE_COMMENT=329, DOUBLE_QUOTE_ID=330, 
		SINGLE_QUOTE=331, ID=332, STRING_LITERAL=333, DECIMAL_LITERAL=334, FLOAT_LITERAL=335, 
		REAL_LITERAL=336, CHAR_LITERAL=337, NE=338, LTGT=339, EQ=340, GT=341, 
		GE=342, LT=343, LE=344, EXCLAMATION=345, PIPE_PIPE=346, DOT=347, UNDERLINE=348, 
		LR_BRACKET=349, RR_BRACKET=350, COMMA=351, SEMI=352, STAR=353, DIVIDE=354, 
		MODULE=355, PLUS=356, MINUS=357, PLACEHOLDER=358;
	public static final int
		RULE_derby_file = 0, RULE_batch = 1, RULE_sql_command = 2, RULE_ddl_command = 3, 
		RULE_dml_command = 4, RULE_keyword = 5, RULE_insert_statement = 6, RULE_offset_clause = 7, 
		RULE_fetch_clause = 8, RULE_first_next = 9, RULE_row_rows = 10, RULE_integer_literal = 11, 
		RULE_merge_statement = 12, RULE_merge_when_clause = 13, RULE_merge_when_matched = 14, 
		RULE_merge_when_not_matched = 15, RULE_merge_update = 16, RULE_merge_insert = 17, 
		RULE_search_condition = 18, RULE_match_refinement = 19, RULE_update_statement = 20, 
		RULE_where_clause = 21, RULE_boolean_expression = 22, RULE_table_subquery = 23, 
		RULE_character_expression = 24, RULE_comparison_operator = 25, RULE_value = 26, 
		RULE_delete_statement = 27, RULE_other_command = 28, RULE_grantees = 29, 
		RULE_grantee = 30, RULE_isolation_level = 31, RULE_constraint_name_list = 32, 
		RULE_column_definition = 33, RULE_with_default = 34, RULE_default_constant_expression = 35, 
		RULE_generated_column_spec = 36, RULE_generation_clause = 37, RULE_column_level_constraint = 38, 
		RULE_table_level_constraint = 39, RULE_references_clause = 40, RULE_no_action_restrict = 41, 
		RULE_constraint_characteristics = 42, RULE_constraint_check_time = 43, 
		RULE_simple_column_name_list = 44, RULE_truncate_table = 45, RULE_privilege_type = 46, 
		RULE_privilege_list = 47, RULE_table_privilege = 48, RULE_column_list = 49, 
		RULE_column_identifier = 50, RULE_revokees = 51, RULE_revokee = 52, RULE_authorization_identifier = 53, 
		RULE_alter_command = 54, RULE_alter_table = 55, RULE_constraint_clause = 56, 
		RULE_cascade_restrict = 57, RULE_row_table = 58, RULE_column_alteration = 59, 
		RULE_integer = 60, RULE_integer_constant = 61, RULE_defaultValue = 62, 
		RULE_constant_expression_null = 63, RULE_always_by_default = 64, RULE_set_drop = 65, 
		RULE_with_set = 66, RULE_create_command = 67, RULE_create_derby_aggregate = 68, 
		RULE_value_data_type = 69, RULE_create_function = 70, RULE_function_parameter = 71, 
		RULE_return_data_type = 72, RULE_table_type = 73, RULE_column_element = 74, 
		RULE_function_element = 75, RULE_create_index = 76, RULE_create_procedure = 77, 
		RULE_procedure_parameter = 78, RULE_data_type = 79, RULE_procedure_element = 80, 
		RULE_create_role = 81, RULE_create_schema = 82, RULE_create_sequence = 83, 
		RULE_sequence_element = 84, RULE_signedInteger = 85, RULE_create_synonym = 86, 
		RULE_create_table = 87, RULE_create_trigger = 88, RULE_create_type = 89, 
		RULE_single_quoted_string = 90, RULE_create_view = 91, RULE_query = 92, 
		RULE_select_expression = 93, RULE_select_item = 94, RULE_simple_column_name = 95, 
		RULE_drop_command = 96, RULE_drop_derby_aggregate = 97, RULE_drop_function = 98, 
		RULE_drop_index = 99, RULE_drop_procedure = 100, RULE_drop_role = 101, 
		RULE_drop_schema = 102, RULE_drop_sequence = 103, RULE_drop_synonym = 104, 
		RULE_drop_table = 105, RULE_drop_trigger = 106, RULE_drop_type = 107, 
		RULE_drop_view = 108, RULE_string = 109, RULE_table_name = 110, RULE_schema_name = 111, 
		RULE_role_name = 112, RULE_aggregate_name = 113, RULE_constraint_name = 114, 
		RULE_column_name = 115, RULE_correlation_name = 116, RULE_function_name = 117, 
		RULE_index_name = 118, RULE_procedure_name = 119, RULE_parameter_name = 120, 
		RULE_sequence_name = 121, RULE_synonym_name = 122, RULE_trigger_name = 123, 
		RULE_type_name = 124, RULE_user_name = 125, RULE_view_name = 126, RULE_cursor_nName = 127, 
		RULE_id_ = 128, RULE_num = 129, RULE_expr_list = 130, RULE_expression = 131, 
		RULE_primitive_expression = 132, RULE_function_call = 133, RULE_literal = 134, 
		RULE_sign = 135, RULE_case_expression = 136, RULE_searched_case = 137, 
		RULE_simple_case = 138, RULE_extended_case = 139, RULE_when_operand = 140, 
		RULE_value_expression = 141, RULE_standard_built_in_function = 142, RULE_aggreagate_built_in_function = 143, 
		RULE_select_statement = 144, RULE_for_update_clause = 145, RULE_all_distinct = 146, 
		RULE_from_clause = 147, RULE_table_expression = 148, RULE_join_operation = 149, 
		RULE_table_view_or_function_expression = 150, RULE_using_clause = 151, 
		RULE_correlation_clause = 152, RULE_table_function_invocation = 153, RULE_function_arg = 154, 
		RULE_group_by_clause = 155, RULE_having_clause = 156, RULE_order_by_clause = 157, 
		RULE_order_by_item = 158, RULE_asc_desc = 159, RULE_columnPosition = 160, 
		RULE_values_expression = 161;
	private static String[] makeRuleNames() {
		return new String[] {
			"derby_file", "batch", "sql_command", "ddl_command", "dml_command", "keyword", 
			"insert_statement", "offset_clause", "fetch_clause", "first_next", "row_rows", 
			"integer_literal", "merge_statement", "merge_when_clause", "merge_when_matched", 
			"merge_when_not_matched", "merge_update", "merge_insert", "search_condition", 
			"match_refinement", "update_statement", "where_clause", "boolean_expression", 
			"table_subquery", "character_expression", "comparison_operator", "value", 
			"delete_statement", "other_command", "grantees", "grantee", "isolation_level", 
			"constraint_name_list", "column_definition", "with_default", "default_constant_expression", 
			"generated_column_spec", "generation_clause", "column_level_constraint", 
			"table_level_constraint", "references_clause", "no_action_restrict", 
			"constraint_characteristics", "constraint_check_time", "simple_column_name_list", 
			"truncate_table", "privilege_type", "privilege_list", "table_privilege", 
			"column_list", "column_identifier", "revokees", "revokee", "authorization_identifier", 
			"alter_command", "alter_table", "constraint_clause", "cascade_restrict", 
			"row_table", "column_alteration", "integer", "integer_constant", "defaultValue", 
			"constant_expression_null", "always_by_default", "set_drop", "with_set", 
			"create_command", "create_derby_aggregate", "value_data_type", "create_function", 
			"function_parameter", "return_data_type", "table_type", "column_element", 
			"function_element", "create_index", "create_procedure", "procedure_parameter", 
			"data_type", "procedure_element", "create_role", "create_schema", "create_sequence", 
			"sequence_element", "signedInteger", "create_synonym", "create_table", 
			"create_trigger", "create_type", "single_quoted_string", "create_view", 
			"query", "select_expression", "select_item", "simple_column_name", "drop_command", 
			"drop_derby_aggregate", "drop_function", "drop_index", "drop_procedure", 
			"drop_role", "drop_schema", "drop_sequence", "drop_synonym", "drop_table", 
			"drop_trigger", "drop_type", "drop_view", "string", "table_name", "schema_name", 
			"role_name", "aggregate_name", "constraint_name", "column_name", "correlation_name", 
			"function_name", "index_name", "procedure_name", "parameter_name", "sequence_name", 
			"synonym_name", "trigger_name", "type_name", "user_name", "view_name", 
			"cursor_nName", "id_", "num", "expr_list", "expression", "primitive_expression", 
			"function_call", "literal", "sign", "case_expression", "searched_case", 
			"simple_case", "extended_case", "when_operand", "value_expression", "standard_built_in_function", 
			"aggreagate_built_in_function", "select_statement", "for_update_clause", 
			"all_distinct", "from_clause", "table_expression", "join_operation", 
			"table_view_or_function_expression", "using_clause", "correlation_clause", 
			"table_function_invocation", "function_arg", "group_by_clause", "having_clause", 
			"order_by_clause", "order_by_item", "asc_desc", "columnPosition", "values_expression"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'ABS'", "'ABSVAL'", "'ACOS'", "'ACTION'", "'ADD'", "'AGGREGATE'", 
			"'ALL'", "'ALLOCATE'", "'ALTER'", "'ALWAYS'", "'AND'", "'ANY'", "'ARE'", 
			"'AS'", "'ASC'", "'ASIN'", "'ASSERTION'", "'AT'", "'ATAN2'", "'ATAN'", 
			"'AUTHORIZATION'", "'AVG'", "'BEGIN'", "'BETWEEN'", "'BIGINT'", "'BINARY'", 
			"'BIT'", "'BLOB'", "'BOOLEAN'", "'BOTH'", "'BY'", "'CALL'", "'CALLED'", 
			"'CASCADE'", "'CASCADED'", "'CASE'", "'CAST'", "'CEIL'", "'CEILING'", 
			"'CHAR'", "'CHARACTER'", "'CHARACTER_LENGTH'", "'CHECK'", "'CLOB'", "'CLOSE'", 
			"'COALESCE'", "'COLLATE'", "'COLLATION'", "'COLUMN'", "'COMMIT'", "'COMMITTED'", 
			"'CONNECT'", "'CONNECTION'", "'CONSTRAINT'", "'CONSTRAINTS'", "'CONTAINS'", 
			"'CONTINUE'", "'CONVERT'", "'CORRESPONDING'", "'COS'", "'COUNT'", "'CREATE'", 
			"'CROSS'", "'CS'", "'CURRENT'", "'CURRENT_DATE'", "'CURRENT_ROLE'", "'CURRENT_TIME'", 
			"'CURRENT_TIMESTAMP'", "'CURRENT_USER'", "'CURSOR'", "'CYCLE'", "'DATA'", 
			"'DATE'", "'DAY'", "'DEALLOCATE'", "'DEC'", "'DECIMAL'", "'DECLARE'", 
			"'DEFAULT'", "'DEFERRABLE'", "'DEFERRED'", "'DEFINER'", "'DEGREES'", 
			"'DELETE'", "'DERBY'", "'DERBY_JDBC_RESULT_SET'", "'DESC'", "'DESCRIBE'", 
			"'DETERMINISTIC'", "'DIAGNOSTICS'", "'DIRTY'", "'DISCONNECT'", "'DISTINCT'", 
			"'DOUBLE'", "'DROP'", "'DYNAMIC'", "'ELSE'", "'END'", "'END_EXEC'", "'ESCAPE'", 
			"'EXCEPT'", "'EXCEPTION'", "'EXCLUSIVE'", "'EXEC'", null, "'EXISTS'", 
			"'EXP'", "'EXPLAIN'", "'EXTERNAL'", "'FALSE'", "'FETCH'", "'FIRST'", 
			"'FLOAT'", "'FLOOR'", "'FOR'", "'FOREIGN'", "'FOUND'", "'FROM'", "'FULL'", 
			"'FUNCTION'", "'GENERATED'", "'GET'", "'GETCURRENTCONNECTION'", "'G'", 
			"'GLOBAL'", "'GO'", "'GOTO'", "'GRANT'", "'GROUP'", "'HAVING'", "'HOUR'", 
			"'IDENTITY'", "'IDENTITY_VAL_LOCAL'", "'IMMEDIATE'", "'IN'", "'INCREMENT'", 
			"'INDEX'", "'INDICATOR'", "'INITIALLY'", "'INNER'", "'INOUT'", "'INPUT'", 
			"'INSENSITIVE'", "'INSERT'", "'INT'", "'INTEGER'", "'INTERSECT'", "'INTO'", 
			"'INVOKER'", "'IS'", "'ISOLATION'", "'JAVA'", "'JOIN'", "'KEY'", "'K'", 
			"'LANGUAGE'", "'LARGE'", "'LAST'", "'LCASE'", "'LEADING'", "'LEFT'", 
			"'LENGTH'", "'LIKE'", "'LN'", "'LOCATE'", "'LOCK'", "'LOCKSIZE'", "'LOG10'", 
			"'LOG'", "'LOGGED'", "'LONG'", "'LOWER'", "'LTRIM'", "'MATCH'", "'MATCHED'", 
			"'MAX'", "'MAXVALUE'", "'M'", "'MERGE'", "'MIN'", "'MINUTE'", "'MINVALUE'", 
			"'MOD'", "'MODE'", "'MODIFIES'", "'MONTH'", "'NAME'", "'NATIONAL'", "'NATURAL'", 
			"'NCHAR'", "'NEXT'", "'NO'", "'NONE'", "'NOT'", "'NULL'", "'NULLIF'", 
			"'NULLS'", "'NUMERIC'", "'NVARCHAR'", "'OBJECT'", "'OF'", "'OFFSET'", 
			"'ON'", "'ONLY'", "'OPEN'", "'OPTION'", "'OR'", "'ORDER'", "'OUT'", "'OUTER'", 
			"'OUTPUT'", "'OVERLAPS'", "'PAD'", "'PARAMETER'", "'PARTIAL'", "'PI'", 
			"'PRECISION'", "'PREPARE'", "'PRESERVE'", "'PRIMARY'", "'PRIOR'", "'PRIVILEGES'", 
			"'PROCEDURE'", "'PUBLIC'", "'RADIANS'", "'READ'", "'READS'", "'REAL'", 
			"'REFERENCES'", "'RELATIVE'", "'RENAME'", "'REPEATABLE'", "'RESET'", 
			"'RESTART'", "'RESTRICT'", "'RESULT'", "'RETURNS'", "'REVOKE'", "'RIGHT'", 
			"'ROLE'", "'ROLLBACK'", "'ROLLUP'", "'ROW'", "'ROWS'", "'RR'", "'RS'", 
			"'RTRIM'", "'SCHEMA'", "'SCROLL'", "'SECOND'", "'SECURITY'", "'SELECT'", 
			"'SEQUENCE'", "'SERIALIZABLE'", "'SESSION_USER'", "'SET'", "'SETS'", 
			"'SHARE'", "'SIN'", "'SMALLINT'", "'SOME'", "'SPACE'", "'SQL'", "'SQLCODE'", 
			"'SQLERROR'", "'SQLID'", "'SQLSTATE'", "'SQRT'", "'STABILITY'", "'START'", 
			"'STDDEV_POP'", "'STDDEV_SAMP'", "'STYLE'", "'SUBSTR'", "'SUBSTRING'", 
			"'SUM'", "'SYNONYM'", "'SYSTEM_USER'", "'TABLE'", "'TAN'", "'TEMPORARY'", 
			"'THEN'", "'TIME'", "'TIMESTAMP'", "'TIMEZONE_HOUR'", "'TIMEZONE_MINUTE'", 
			"'TO'", "'TRANSACTION'", "'TRANSLATE'", "'TRANSLATION'", "'TRIGGER'", 
			"'TRIM'", "'TRUE'", "'TRUNCATE'", "'TYPE'", "'UCASE'", "'UNCOMMITTED'", 
			"'UNION'", "'UNIQUE'", "'UNKNOWN'", "'UPDATE'", "'UPPER'", "'UR'", "'USAGE'", 
			"'USER'", "'USING'", "'VALUES'", "'VAR_POP'", "'VAR_SAMP'", "'VARCHAR'", 
			"'VARYING'", "'VIEW'", "'WHEN'", "'WHENEVER'", "'WHERE'", "'WINDOW'", 
			"'WITH'", "'WORK'", "'WRITE'", "'XML'", "'XMLEXISTS'", "'XMLPARSE'", 
			"'XMLQUERY'", "'XMLSERIALIZE'", "'YEAR'", null, null, null, null, "'''", 
			null, null, null, null, null, null, "'!='", "'<>'", "'='", "'>'", "'>='", 
			"'<'", "'<='", "'!'", "'||'", "'.'", "'_'", "'('", "')'", "','", "';'", 
			"'*'", "'/'", "'%'", "'+'", "'-'", "'?'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ABS", "ABSVAL", "ACOS", "ACTION", "ADD", "AGGREGATE", "ALL", "ALLOCATE", 
			"ALTER", "ALWAYS", "AND", "ANY", "ARE", "AS", "ASC", "ASIN", "ASSERTION", 
			"AT", "ATAN2", "ATAN", "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", "BIGINT", 
			"BINARY", "BIT", "BLOB", "BOOLEAN", "BOTH", "BY", "CALL", "CALLED", "CASCADE", 
			"CASCADED", "CASE", "CAST", "CEIL", "CEILING", "CHAR", "CHARACTER", "CHARACTER_LENGTH", 
			"CHECK", "CLOB", "CLOSE", "COALESCE", "COLLATE", "COLLATION", "COLUMN", 
			"COMMIT", "COMMITTED", "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS", 
			"CONTAINS", "CONTINUE", "CONVERT", "CORRESPONDING", "COS", "COUNT", "CREATE", 
			"CROSS", "CS", "CURRENT", "CURRENT_DATE", "CURRENT_ROLE", "CURRENT_TIME", 
			"CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "CYCLE", "DATA", "DATE", 
			"DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE", 
			"DEFERRED", "DEFINER", "DEGREES", "DELETE", "DERBY", "DERBY_JDBC_RESULT_SET", 
			"DESC", "DESCRIBE", "DETERMINISTIC", "DIAGNOSTICS", "DIRTY", "DISCONNECT", 
			"DISTINCT", "DOUBLE", "DROP", "DYNAMIC", "ELSE", "END", "END_EXEC", "ESCAPE", 
			"EXCEPT", "EXCEPTION", "EXCLUSIVE", "EXEC", "EXECUTE", "EXISTS", "EXP", 
			"EXPLAIN", "EXTERNAL", "FALSE", "FETCH", "FIRST", "FLOAT", "FLOOR", "FOR", 
			"FOREIGN", "FOUND", "FROM", "FULL", "FUNCTION", "GENERATED", "GET", "GETCURRENTCONNECTION", 
			"GIGA", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "HAVING", "HOUR", "IDENTITY", 
			"IDENTITY_VAL_LOCAL", "IMMEDIATE", "IN", "INCREMENT", "INDEX", "INDICATOR", 
			"INITIALLY", "INNER", "INOUT", "INPUT", "INSENSITIVE", "INSERT", "INT", 
			"INTEGER", "INTERSECT", "INTO", "INVOKER", "IS", "ISOLATION", "JAVA", 
			"JOIN", "KEY", "KILO", "LANGUAGE", "LARGE", "LAST", "LCASE", "LEADING", 
			"LEFT", "LENGTH", "LIKE", "LN", "LOCATE", "LOCK", "LOCKSIZE", "LOG10", 
			"LOG", "LOGGED", "LONG", "LOWER", "LTRIM", "MATCH", "MATCHED", "MAX", 
			"MAXVALUE", "MEGA", "MERGE", "MIN", "MINUTE", "MINVALUE", "MOD", "MODE", 
			"MODIFIES", "MONTH", "NAME", "NATIONAL", "NATURAL", "NCHAR", "NEXT", 
			"NO", "NONE", "NOT", "NULL_", "NULLIF", "NULLS", "NUMERIC", "NVARCHAR", 
			"OBJECT", "OF", "OFFSET", "ON", "ONLY", "OPEN", "OPTION", "OR", "ORDER", 
			"OUT", "OUTER", "OUTPUT", "OVERLAPS", "PAD", "PARAMETER", "PARTIAL", 
			"PI", "PRECISION", "PREPARE", "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", 
			"PROCEDURE", "PUBLIC", "RADIANS", "READ", "READS", "REAL", "REFERENCES", 
			"RELATIVE", "RENAME", "REPEATABLE", "RESET", "RESTART", "RESTRICT", "RESULT", 
			"RETURNS", "REVOKE", "RIGHT", "ROLE", "ROLLBACK", "ROLLUP", "ROW", "ROWS", 
			"RR", "RS", "RTRIM", "SCHEMA", "SCROLL", "SECOND", "SECURITY", "SELECT", 
			"SEQUENCE", "SERIALIZABLE", "SESSION_USER", "SET", "SETS", "SHARE", "SIN", 
			"SMALLINT", "SOME", "SPACE", "SQL", "SQLCODE", "SQLERROR", "SQLID", "SQLSTATE", 
			"SQRT", "STABILITY", "START", "STDDEV_POP", "STDDEV_SAMP", "STYLE", "SUBSTR", 
			"SUBSTRING", "SUM", "SYNONYM", "SYSTEM_USER", "TABLE", "TAN", "TEMPORARY", 
			"THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", 
			"TRANSACTION", "TRANSLATE", "TRANSLATION", "TRIGGER", "TRIM", "TRUE", 
			"TRUNCATE", "TYPE", "UCASE", "UNCOMMITTED", "UNION", "UNIQUE", "UNKNOWN", 
			"UPDATE", "UPPER", "UR", "USAGE", "USER", "USING", "VALUES", "VAR_POP", 
			"VAR_SAMP", "VARCHAR", "VARYING", "VIEW", "WHEN", "WHENEVER", "WHERE", 
			"WINDOW", "WITH", "WORK", "WRITE", "XML", "XMLEXISTS", "XMLPARSE", "XMLQUERY", 
			"XMLSERIALIZE", "YEAR", "WHITE_SPACE", "SQL_COMMENT", "LINE_COMMENT", 
			"DOUBLE_QUOTE_ID", "SINGLE_QUOTE", "ID", "STRING_LITERAL", "DECIMAL_LITERAL", 
			"FLOAT_LITERAL", "REAL_LITERAL", "CHAR_LITERAL", "NE", "LTGT", "EQ", 
			"GT", "GE", "LT", "LE", "EXCLAMATION", "PIPE_PIPE", "DOT", "UNDERLINE", 
			"LR_BRACKET", "RR_BRACKET", "COMMA", "SEMI", "STAR", "DIVIDE", "MODULE", 
			"PLUS", "MINUS", "PLACEHOLDER"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "DerbyParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public DerbyParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Derby_fileContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(DerbyParser.EOF, 0); }
		public List<BatchContext> batch() {
			return getRuleContexts(BatchContext.class);
		}
		public BatchContext batch(int i) {
			return getRuleContext(BatchContext.class,i);
		}
		public Derby_fileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_derby_file; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDerby_file(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDerby_file(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDerby_file(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Derby_fileContext derby_file() throws RecognitionException {
		Derby_fileContext _localctx = new Derby_fileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_derby_file);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(327);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(324);
					batch();
					}
					} 
				}
				setState(329);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			setState(330);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BatchContext extends ParserRuleContext {
		public Sql_commandContext sql_command() {
			return getRuleContext(Sql_commandContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(DerbyParser.SEMI, 0); }
		public BatchContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_batch; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterBatch(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitBatch(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitBatch(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BatchContext batch() throws RecognitionException {
		BatchContext _localctx = new BatchContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_batch);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(332);
			sql_command();
			setState(334);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(333);
				match(SEMI);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Sql_commandContext extends ParserRuleContext {
		public Ddl_commandContext ddl_command() {
			return getRuleContext(Ddl_commandContext.class,0);
		}
		public Dml_commandContext dml_command() {
			return getRuleContext(Dml_commandContext.class,0);
		}
		public Other_commandContext other_command() {
			return getRuleContext(Other_commandContext.class,0);
		}
		public Sql_commandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sql_command; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSql_command(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSql_command(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSql_command(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Sql_commandContext sql_command() throws RecognitionException {
		Sql_commandContext _localctx = new Sql_commandContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_sql_command);
		try {
			setState(339);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(336);
				ddl_command();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(337);
				dml_command();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(338);
				other_command();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Ddl_commandContext extends ParserRuleContext {
		public Alter_commandContext alter_command() {
			return getRuleContext(Alter_commandContext.class,0);
		}
		public Create_commandContext create_command() {
			return getRuleContext(Create_commandContext.class,0);
		}
		public Drop_commandContext drop_command() {
			return getRuleContext(Drop_commandContext.class,0);
		}
		public Ddl_commandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ddl_command; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDdl_command(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDdl_command(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDdl_command(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Ddl_commandContext ddl_command() throws RecognitionException {
		Ddl_commandContext _localctx = new Ddl_commandContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_ddl_command);
		try {
			setState(344);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(341);
				alter_command();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(342);
				create_command();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(343);
				drop_command();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Dml_commandContext extends ParserRuleContext {
		public Select_statementContext select_statement() {
			return getRuleContext(Select_statementContext.class,0);
		}
		public Insert_statementContext insert_statement() {
			return getRuleContext(Insert_statementContext.class,0);
		}
		public Update_statementContext update_statement() {
			return getRuleContext(Update_statementContext.class,0);
		}
		public Delete_statementContext delete_statement() {
			return getRuleContext(Delete_statementContext.class,0);
		}
		public Merge_statementContext merge_statement() {
			return getRuleContext(Merge_statementContext.class,0);
		}
		public Truncate_tableContext truncate_table() {
			return getRuleContext(Truncate_tableContext.class,0);
		}
		public Dml_commandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dml_command; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDml_command(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDml_command(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDml_command(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Dml_commandContext dml_command() throws RecognitionException {
		Dml_commandContext _localctx = new Dml_commandContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_dml_command);
		try {
			setState(352);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(346);
				select_statement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(347);
				insert_statement();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(348);
				update_statement();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(349);
				delete_statement();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(350);
				merge_statement();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(351);
				truncate_table();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class KeywordContext extends ParserRuleContext {
		public TerminalNode ADD() { return getToken(DerbyParser.ADD, 0); }
		public TerminalNode ALL() { return getToken(DerbyParser.ALL, 0); }
		public TerminalNode ALLOCATE() { return getToken(DerbyParser.ALLOCATE, 0); }
		public TerminalNode ALTER() { return getToken(DerbyParser.ALTER, 0); }
		public TerminalNode AND() { return getToken(DerbyParser.AND, 0); }
		public TerminalNode ANY() { return getToken(DerbyParser.ANY, 0); }
		public TerminalNode ARE() { return getToken(DerbyParser.ARE, 0); }
		public TerminalNode AS() { return getToken(DerbyParser.AS, 0); }
		public TerminalNode ASC() { return getToken(DerbyParser.ASC, 0); }
		public TerminalNode ASSERTION() { return getToken(DerbyParser.ASSERTION, 0); }
		public TerminalNode AT() { return getToken(DerbyParser.AT, 0); }
		public TerminalNode AUTHORIZATION() { return getToken(DerbyParser.AUTHORIZATION, 0); }
		public TerminalNode AVG() { return getToken(DerbyParser.AVG, 0); }
		public TerminalNode BEGIN() { return getToken(DerbyParser.BEGIN, 0); }
		public TerminalNode BETWEEN() { return getToken(DerbyParser.BETWEEN, 0); }
		public TerminalNode BIGINT() { return getToken(DerbyParser.BIGINT, 0); }
		public TerminalNode BIT() { return getToken(DerbyParser.BIT, 0); }
		public TerminalNode BOOLEAN() { return getToken(DerbyParser.BOOLEAN, 0); }
		public TerminalNode BOTH() { return getToken(DerbyParser.BOTH, 0); }
		public TerminalNode BY() { return getToken(DerbyParser.BY, 0); }
		public TerminalNode CALL() { return getToken(DerbyParser.CALL, 0); }
		public TerminalNode CASCADE() { return getToken(DerbyParser.CASCADE, 0); }
		public TerminalNode CASCADED() { return getToken(DerbyParser.CASCADED, 0); }
		public TerminalNode CASE() { return getToken(DerbyParser.CASE, 0); }
		public TerminalNode CAST() { return getToken(DerbyParser.CAST, 0); }
		public TerminalNode CHAR() { return getToken(DerbyParser.CHAR, 0); }
		public TerminalNode CHARACTER() { return getToken(DerbyParser.CHARACTER, 0); }
		public TerminalNode CHARACTER_LENGTH() { return getToken(DerbyParser.CHARACTER_LENGTH, 0); }
		public TerminalNode CHECK() { return getToken(DerbyParser.CHECK, 0); }
		public TerminalNode CLOSE() { return getToken(DerbyParser.CLOSE, 0); }
		public TerminalNode COALESCE() { return getToken(DerbyParser.COALESCE, 0); }
		public TerminalNode COLLATE() { return getToken(DerbyParser.COLLATE, 0); }
		public TerminalNode COLLATION() { return getToken(DerbyParser.COLLATION, 0); }
		public TerminalNode COLUMN() { return getToken(DerbyParser.COLUMN, 0); }
		public TerminalNode COMMIT() { return getToken(DerbyParser.COMMIT, 0); }
		public TerminalNode CONNECT() { return getToken(DerbyParser.CONNECT, 0); }
		public TerminalNode CONNECTION() { return getToken(DerbyParser.CONNECTION, 0); }
		public TerminalNode CONSTRAINT() { return getToken(DerbyParser.CONSTRAINT, 0); }
		public TerminalNode CONSTRAINTS() { return getToken(DerbyParser.CONSTRAINTS, 0); }
		public TerminalNode CONTINUE() { return getToken(DerbyParser.CONTINUE, 0); }
		public TerminalNode CONVERT() { return getToken(DerbyParser.CONVERT, 0); }
		public TerminalNode CORRESPONDING() { return getToken(DerbyParser.CORRESPONDING, 0); }
		public TerminalNode CREATE() { return getToken(DerbyParser.CREATE, 0); }
		public TerminalNode CROSS() { return getToken(DerbyParser.CROSS, 0); }
		public TerminalNode CURRENT() { return getToken(DerbyParser.CURRENT, 0); }
		public TerminalNode CURRENT_DATE() { return getToken(DerbyParser.CURRENT_DATE, 0); }
		public TerminalNode CURRENT_ROLE() { return getToken(DerbyParser.CURRENT_ROLE, 0); }
		public TerminalNode CURRENT_TIME() { return getToken(DerbyParser.CURRENT_TIME, 0); }
		public TerminalNode CURRENT_TIMESTAMP() { return getToken(DerbyParser.CURRENT_TIMESTAMP, 0); }
		public TerminalNode CURRENT_USER() { return getToken(DerbyParser.CURRENT_USER, 0); }
		public TerminalNode CURSOR() { return getToken(DerbyParser.CURSOR, 0); }
		public TerminalNode DEALLOCATE() { return getToken(DerbyParser.DEALLOCATE, 0); }
		public TerminalNode DEC() { return getToken(DerbyParser.DEC, 0); }
		public TerminalNode DECIMAL() { return getToken(DerbyParser.DECIMAL, 0); }
		public TerminalNode DECLARE() { return getToken(DerbyParser.DECLARE, 0); }
		public TerminalNode DEFAULT() { return getToken(DerbyParser.DEFAULT, 0); }
		public TerminalNode DEFERRABLE() { return getToken(DerbyParser.DEFERRABLE, 0); }
		public TerminalNode DEFERRED() { return getToken(DerbyParser.DEFERRED, 0); }
		public TerminalNode DELETE() { return getToken(DerbyParser.DELETE, 0); }
		public TerminalNode DESC() { return getToken(DerbyParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(DerbyParser.DESCRIBE, 0); }
		public TerminalNode DIAGNOSTICS() { return getToken(DerbyParser.DIAGNOSTICS, 0); }
		public TerminalNode DISCONNECT() { return getToken(DerbyParser.DISCONNECT, 0); }
		public TerminalNode DISTINCT() { return getToken(DerbyParser.DISTINCT, 0); }
		public TerminalNode DOUBLE() { return getToken(DerbyParser.DOUBLE, 0); }
		public TerminalNode DROP() { return getToken(DerbyParser.DROP, 0); }
		public TerminalNode ELSE() { return getToken(DerbyParser.ELSE, 0); }
		public TerminalNode END() { return getToken(DerbyParser.END, 0); }
		public TerminalNode END_EXEC() { return getToken(DerbyParser.END_EXEC, 0); }
		public TerminalNode ESCAPE() { return getToken(DerbyParser.ESCAPE, 0); }
		public TerminalNode EXCEPT() { return getToken(DerbyParser.EXCEPT, 0); }
		public TerminalNode EXCEPTION() { return getToken(DerbyParser.EXCEPTION, 0); }
		public TerminalNode EXEC() { return getToken(DerbyParser.EXEC, 0); }
		public TerminalNode EXECUTE() { return getToken(DerbyParser.EXECUTE, 0); }
		public TerminalNode EXISTS() { return getToken(DerbyParser.EXISTS, 0); }
		public TerminalNode EXPLAIN() { return getToken(DerbyParser.EXPLAIN, 0); }
		public TerminalNode EXTERNAL() { return getToken(DerbyParser.EXTERNAL, 0); }
		public TerminalNode FALSE() { return getToken(DerbyParser.FALSE, 0); }
		public TerminalNode FETCH() { return getToken(DerbyParser.FETCH, 0); }
		public TerminalNode FIRST() { return getToken(DerbyParser.FIRST, 0); }
		public TerminalNode FLOAT() { return getToken(DerbyParser.FLOAT, 0); }
		public TerminalNode FOR() { return getToken(DerbyParser.FOR, 0); }
		public TerminalNode FOREIGN() { return getToken(DerbyParser.FOREIGN, 0); }
		public TerminalNode FOUND() { return getToken(DerbyParser.FOUND, 0); }
		public TerminalNode FROM() { return getToken(DerbyParser.FROM, 0); }
		public TerminalNode FULL() { return getToken(DerbyParser.FULL, 0); }
		public TerminalNode FUNCTION() { return getToken(DerbyParser.FUNCTION, 0); }
		public TerminalNode GET() { return getToken(DerbyParser.GET, 0); }
		public TerminalNode GETCURRENTCONNECTION() { return getToken(DerbyParser.GETCURRENTCONNECTION, 0); }
		public TerminalNode GLOBAL() { return getToken(DerbyParser.GLOBAL, 0); }
		public TerminalNode GO() { return getToken(DerbyParser.GO, 0); }
		public TerminalNode GOTO() { return getToken(DerbyParser.GOTO, 0); }
		public TerminalNode GRANT() { return getToken(DerbyParser.GRANT, 0); }
		public TerminalNode GROUP() { return getToken(DerbyParser.GROUP, 0); }
		public TerminalNode HAVING() { return getToken(DerbyParser.HAVING, 0); }
		public TerminalNode HOUR() { return getToken(DerbyParser.HOUR, 0); }
		public TerminalNode IDENTITY() { return getToken(DerbyParser.IDENTITY, 0); }
		public TerminalNode IMMEDIATE() { return getToken(DerbyParser.IMMEDIATE, 0); }
		public TerminalNode IN() { return getToken(DerbyParser.IN, 0); }
		public TerminalNode INDICATOR() { return getToken(DerbyParser.INDICATOR, 0); }
		public TerminalNode INITIALLY() { return getToken(DerbyParser.INITIALLY, 0); }
		public TerminalNode INNER() { return getToken(DerbyParser.INNER, 0); }
		public TerminalNode INOUT() { return getToken(DerbyParser.INOUT, 0); }
		public TerminalNode INPUT() { return getToken(DerbyParser.INPUT, 0); }
		public TerminalNode INSENSITIVE() { return getToken(DerbyParser.INSENSITIVE, 0); }
		public TerminalNode INSERT() { return getToken(DerbyParser.INSERT, 0); }
		public TerminalNode INT() { return getToken(DerbyParser.INT, 0); }
		public TerminalNode INTEGER() { return getToken(DerbyParser.INTEGER, 0); }
		public TerminalNode INTERSECT() { return getToken(DerbyParser.INTERSECT, 0); }
		public TerminalNode INTO() { return getToken(DerbyParser.INTO, 0); }
		public TerminalNode IS() { return getToken(DerbyParser.IS, 0); }
		public TerminalNode ISOLATION() { return getToken(DerbyParser.ISOLATION, 0); }
		public TerminalNode JOIN() { return getToken(DerbyParser.JOIN, 0); }
		public TerminalNode KEY() { return getToken(DerbyParser.KEY, 0); }
		public TerminalNode LAST() { return getToken(DerbyParser.LAST, 0); }
		public TerminalNode LEADING() { return getToken(DerbyParser.LEADING, 0); }
		public TerminalNode LEFT() { return getToken(DerbyParser.LEFT, 0); }
		public TerminalNode LIKE() { return getToken(DerbyParser.LIKE, 0); }
		public TerminalNode LOWER() { return getToken(DerbyParser.LOWER, 0); }
		public TerminalNode LTRIM() { return getToken(DerbyParser.LTRIM, 0); }
		public TerminalNode MATCH() { return getToken(DerbyParser.MATCH, 0); }
		public TerminalNode MAX() { return getToken(DerbyParser.MAX, 0); }
		public TerminalNode MIN() { return getToken(DerbyParser.MIN, 0); }
		public TerminalNode MINUTE() { return getToken(DerbyParser.MINUTE, 0); }
		public TerminalNode NATIONAL() { return getToken(DerbyParser.NATIONAL, 0); }
		public TerminalNode NATURAL() { return getToken(DerbyParser.NATURAL, 0); }
		public TerminalNode NCHAR() { return getToken(DerbyParser.NCHAR, 0); }
		public TerminalNode NVARCHAR() { return getToken(DerbyParser.NVARCHAR, 0); }
		public TerminalNode NEXT() { return getToken(DerbyParser.NEXT, 0); }
		public TerminalNode NO() { return getToken(DerbyParser.NO, 0); }
		public TerminalNode NONE() { return getToken(DerbyParser.NONE, 0); }
		public TerminalNode NOT() { return getToken(DerbyParser.NOT, 0); }
		public TerminalNode NULL_() { return getToken(DerbyParser.NULL_, 0); }
		public TerminalNode NULLIF() { return getToken(DerbyParser.NULLIF, 0); }
		public TerminalNode NUMERIC() { return getToken(DerbyParser.NUMERIC, 0); }
		public TerminalNode OF() { return getToken(DerbyParser.OF, 0); }
		public TerminalNode ON() { return getToken(DerbyParser.ON, 0); }
		public TerminalNode ONLY() { return getToken(DerbyParser.ONLY, 0); }
		public TerminalNode OPEN() { return getToken(DerbyParser.OPEN, 0); }
		public TerminalNode OPTION() { return getToken(DerbyParser.OPTION, 0); }
		public TerminalNode OR() { return getToken(DerbyParser.OR, 0); }
		public TerminalNode ORDER() { return getToken(DerbyParser.ORDER, 0); }
		public TerminalNode OUTER() { return getToken(DerbyParser.OUTER, 0); }
		public TerminalNode OUTPUT() { return getToken(DerbyParser.OUTPUT, 0); }
		public TerminalNode OVERLAPS() { return getToken(DerbyParser.OVERLAPS, 0); }
		public TerminalNode PAD() { return getToken(DerbyParser.PAD, 0); }
		public TerminalNode PARTIAL() { return getToken(DerbyParser.PARTIAL, 0); }
		public TerminalNode PREPARE() { return getToken(DerbyParser.PREPARE, 0); }
		public TerminalNode PRESERVE() { return getToken(DerbyParser.PRESERVE, 0); }
		public TerminalNode PRIMARY() { return getToken(DerbyParser.PRIMARY, 0); }
		public TerminalNode PRIOR() { return getToken(DerbyParser.PRIOR, 0); }
		public TerminalNode PRIVILEGES() { return getToken(DerbyParser.PRIVILEGES, 0); }
		public TerminalNode PROCEDURE() { return getToken(DerbyParser.PROCEDURE, 0); }
		public TerminalNode PUBLIC() { return getToken(DerbyParser.PUBLIC, 0); }
		public TerminalNode READ() { return getToken(DerbyParser.READ, 0); }
		public TerminalNode REAL() { return getToken(DerbyParser.REAL, 0); }
		public TerminalNode REFERENCES() { return getToken(DerbyParser.REFERENCES, 0); }
		public TerminalNode RELATIVE() { return getToken(DerbyParser.RELATIVE, 0); }
		public TerminalNode RESTRICT() { return getToken(DerbyParser.RESTRICT, 0); }
		public TerminalNode REVOKE() { return getToken(DerbyParser.REVOKE, 0); }
		public TerminalNode RIGHT() { return getToken(DerbyParser.RIGHT, 0); }
		public TerminalNode ROLLBACK() { return getToken(DerbyParser.ROLLBACK, 0); }
		public TerminalNode ROWS() { return getToken(DerbyParser.ROWS, 0); }
		public TerminalNode RTRIM() { return getToken(DerbyParser.RTRIM, 0); }
		public TerminalNode SCHEMA() { return getToken(DerbyParser.SCHEMA, 0); }
		public TerminalNode SCROLL() { return getToken(DerbyParser.SCROLL, 0); }
		public TerminalNode SECOND() { return getToken(DerbyParser.SECOND, 0); }
		public TerminalNode SELECT() { return getToken(DerbyParser.SELECT, 0); }
		public TerminalNode SESSION_USER() { return getToken(DerbyParser.SESSION_USER, 0); }
		public TerminalNode SET() { return getToken(DerbyParser.SET, 0); }
		public TerminalNode SMALLINT() { return getToken(DerbyParser.SMALLINT, 0); }
		public TerminalNode SOME() { return getToken(DerbyParser.SOME, 0); }
		public TerminalNode SPACE() { return getToken(DerbyParser.SPACE, 0); }
		public TerminalNode SQL() { return getToken(DerbyParser.SQL, 0); }
		public TerminalNode SQLCODE() { return getToken(DerbyParser.SQLCODE, 0); }
		public TerminalNode SQLERROR() { return getToken(DerbyParser.SQLERROR, 0); }
		public TerminalNode SQLSTATE() { return getToken(DerbyParser.SQLSTATE, 0); }
		public TerminalNode SUBSTR() { return getToken(DerbyParser.SUBSTR, 0); }
		public TerminalNode SUBSTRING() { return getToken(DerbyParser.SUBSTRING, 0); }
		public TerminalNode SUM() { return getToken(DerbyParser.SUM, 0); }
		public TerminalNode SYSTEM_USER() { return getToken(DerbyParser.SYSTEM_USER, 0); }
		public TerminalNode TABLE() { return getToken(DerbyParser.TABLE, 0); }
		public TerminalNode TEMPORARY() { return getToken(DerbyParser.TEMPORARY, 0); }
		public TerminalNode TIMEZONE_HOUR() { return getToken(DerbyParser.TIMEZONE_HOUR, 0); }
		public TerminalNode TIMEZONE_MINUTE() { return getToken(DerbyParser.TIMEZONE_MINUTE, 0); }
		public TerminalNode TO() { return getToken(DerbyParser.TO, 0); }
		public TerminalNode TRANSACTION() { return getToken(DerbyParser.TRANSACTION, 0); }
		public TerminalNode TRANSLATE() { return getToken(DerbyParser.TRANSLATE, 0); }
		public TerminalNode TRANSLATION() { return getToken(DerbyParser.TRANSLATION, 0); }
		public TerminalNode TRIM() { return getToken(DerbyParser.TRIM, 0); }
		public TerminalNode TRUE() { return getToken(DerbyParser.TRUE, 0); }
		public TerminalNode UNION() { return getToken(DerbyParser.UNION, 0); }
		public TerminalNode UNIQUE() { return getToken(DerbyParser.UNIQUE, 0); }
		public TerminalNode UNKNOWN() { return getToken(DerbyParser.UNKNOWN, 0); }
		public TerminalNode UPDATE() { return getToken(DerbyParser.UPDATE, 0); }
		public TerminalNode UPPER() { return getToken(DerbyParser.UPPER, 0); }
		public TerminalNode USER() { return getToken(DerbyParser.USER, 0); }
		public TerminalNode USING() { return getToken(DerbyParser.USING, 0); }
		public TerminalNode VALUES() { return getToken(DerbyParser.VALUES, 0); }
		public TerminalNode VARCHAR() { return getToken(DerbyParser.VARCHAR, 0); }
		public TerminalNode VARYING() { return getToken(DerbyParser.VARYING, 0); }
		public TerminalNode VIEW() { return getToken(DerbyParser.VIEW, 0); }
		public TerminalNode WHENEVER() { return getToken(DerbyParser.WHENEVER, 0); }
		public TerminalNode WHERE() { return getToken(DerbyParser.WHERE, 0); }
		public TerminalNode WINDOW() { return getToken(DerbyParser.WINDOW, 0); }
		public TerminalNode WITH() { return getToken(DerbyParser.WITH, 0); }
		public TerminalNode WORK() { return getToken(DerbyParser.WORK, 0); }
		public TerminalNode WRITE() { return getToken(DerbyParser.WRITE, 0); }
		public TerminalNode XML() { return getToken(DerbyParser.XML, 0); }
		public TerminalNode XMLEXISTS() { return getToken(DerbyParser.XMLEXISTS, 0); }
		public TerminalNode XMLPARSE() { return getToken(DerbyParser.XMLPARSE, 0); }
		public TerminalNode XMLQUERY() { return getToken(DerbyParser.XMLQUERY, 0); }
		public TerminalNode XMLSERIALIZE() { return getToken(DerbyParser.XMLSERIALIZE, 0); }
		public TerminalNode YEAR() { return getToken(DerbyParser.YEAR, 0); }
		public KeywordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyword; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterKeyword(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitKeyword(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitKeyword(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeywordContext keyword() throws RecognitionException {
		KeywordContext _localctx = new KeywordContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_keyword);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(354);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & -3533092333419037792L) != 0) || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & -1298171942908200833L) != 0) || ((((_la - 129)) & ~0x3f) == 0 && ((1L << (_la - 129)) & -1139006037011989281L) != 0) || ((((_la - 193)) & ~0x3f) == 0 && ((1L << (_la - 193)) & -7524731618305639713L) != 0) || ((((_la - 257)) & ~0x3f) == 0 && ((1L << (_la - 257)) & -158052357440271375L) != 0) || ((((_la - 321)) & ~0x3f) == 0 && ((1L << (_la - 321)) & 63L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Insert_statementContext extends ParserRuleContext {
		public TerminalNode INSERT() { return getToken(DerbyParser.INSERT, 0); }
		public TerminalNode INTO() { return getToken(DerbyParser.INTO, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public List<TerminalNode> LR_BRACKET() { return getTokens(DerbyParser.LR_BRACKET); }
		public TerminalNode LR_BRACKET(int i) {
			return getToken(DerbyParser.LR_BRACKET, i);
		}
		public List<Simple_column_nameContext> simple_column_name() {
			return getRuleContexts(Simple_column_nameContext.class);
		}
		public Simple_column_nameContext simple_column_name(int i) {
			return getRuleContext(Simple_column_nameContext.class,i);
		}
		public List<TerminalNode> RR_BRACKET() { return getTokens(DerbyParser.RR_BRACKET); }
		public TerminalNode RR_BRACKET(int i) {
			return getToken(DerbyParser.RR_BRACKET, i);
		}
		public Order_by_clauseContext order_by_clause() {
			return getRuleContext(Order_by_clauseContext.class,0);
		}
		public Offset_clauseContext offset_clause() {
			return getRuleContext(Offset_clauseContext.class,0);
		}
		public Fetch_clauseContext fetch_clause() {
			return getRuleContext(Fetch_clauseContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Insert_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insert_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterInsert_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitInsert_statement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitInsert_statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Insert_statementContext insert_statement() throws RecognitionException {
		Insert_statementContext _localctx = new Insert_statementContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_insert_statement);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(356);
			match(INSERT);
			setState(357);
			match(INTO);
			setState(358);
			table_name();
			setState(372);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(359);
					match(LR_BRACKET);
					setState(360);
					simple_column_name();
					setState(365);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(361);
							match(COMMA);
							setState(362);
							simple_column_name();
							}
							} 
						}
						setState(367);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
					}
					setState(368);
					match(RR_BRACKET);
					}
					} 
				}
				setState(374);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			}
			setState(375);
			query(0);
			setState(377);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				{
				setState(376);
				order_by_clause();
				}
				break;
			}
			setState(380);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				{
				setState(379);
				offset_clause();
				}
				break;
			}
			setState(383);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				{
				setState(382);
				fetch_clause();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Offset_clauseContext extends ParserRuleContext {
		public TerminalNode OFFSET() { return getToken(DerbyParser.OFFSET, 0); }
		public Row_rowsContext row_rows() {
			return getRuleContext(Row_rowsContext.class,0);
		}
		public Integer_literalContext integer_literal() {
			return getRuleContext(Integer_literalContext.class,0);
		}
		public TerminalNode PLACEHOLDER() { return getToken(DerbyParser.PLACEHOLDER, 0); }
		public Offset_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_offset_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterOffset_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitOffset_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitOffset_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Offset_clauseContext offset_clause() throws RecognitionException {
		Offset_clauseContext _localctx = new Offset_clauseContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_offset_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(385);
			match(OFFSET);
			setState(388);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				{
				setState(386);
				integer_literal();
				}
				break;
			case 2:
				{
				setState(387);
				match(PLACEHOLDER);
				}
				break;
			}
			setState(390);
			row_rows();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Fetch_clauseContext extends ParserRuleContext {
		public TerminalNode FETCH() { return getToken(DerbyParser.FETCH, 0); }
		public First_nextContext first_next() {
			return getRuleContext(First_nextContext.class,0);
		}
		public Row_rowsContext row_rows() {
			return getRuleContext(Row_rowsContext.class,0);
		}
		public TerminalNode ONLY() { return getToken(DerbyParser.ONLY, 0); }
		public Integer_literalContext integer_literal() {
			return getRuleContext(Integer_literalContext.class,0);
		}
		public TerminalNode PLACEHOLDER() { return getToken(DerbyParser.PLACEHOLDER, 0); }
		public Fetch_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fetch_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterFetch_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitFetch_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitFetch_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Fetch_clauseContext fetch_clause() throws RecognitionException {
		Fetch_clauseContext _localctx = new Fetch_clauseContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_fetch_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(392);
			match(FETCH);
			setState(393);
			first_next();
			setState(396);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				{
				setState(394);
				integer_literal();
				}
				break;
			case 2:
				{
				setState(395);
				match(PLACEHOLDER);
				}
				break;
			}
			setState(398);
			row_rows();
			setState(399);
			match(ONLY);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class First_nextContext extends ParserRuleContext {
		public TerminalNode FIRST() { return getToken(DerbyParser.FIRST, 0); }
		public TerminalNode NEXT() { return getToken(DerbyParser.NEXT, 0); }
		public First_nextContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_first_next; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterFirst_next(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitFirst_next(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitFirst_next(this);
			else return visitor.visitChildren(this);
		}
	}

	public final First_nextContext first_next() throws RecognitionException {
		First_nextContext _localctx = new First_nextContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_first_next);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(401);
			_la = _input.LA(1);
			if ( !(_la==FIRST || _la==NEXT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Row_rowsContext extends ParserRuleContext {
		public TerminalNode ROW() { return getToken(DerbyParser.ROW, 0); }
		public TerminalNode ROWS() { return getToken(DerbyParser.ROWS, 0); }
		public Row_rowsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_row_rows; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterRow_rows(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitRow_rows(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitRow_rows(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Row_rowsContext row_rows() throws RecognitionException {
		Row_rowsContext _localctx = new Row_rowsContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_row_rows);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(403);
			_la = _input.LA(1);
			if ( !(_la==ROW || _la==ROWS) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Integer_literalContext extends ParserRuleContext {
		public NumContext num() {
			return getRuleContext(NumContext.class,0);
		}
		public Integer_literalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterInteger_literal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitInteger_literal(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitInteger_literal(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Integer_literalContext integer_literal() throws RecognitionException {
		Integer_literalContext _localctx = new Integer_literalContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_integer_literal);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(405);
			num();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Merge_statementContext extends ParserRuleContext {
		public TerminalNode MERGE() { return getToken(DerbyParser.MERGE, 0); }
		public TerminalNode INTO() { return getToken(DerbyParser.INTO, 0); }
		public List<Table_nameContext> table_name() {
			return getRuleContexts(Table_nameContext.class);
		}
		public Table_nameContext table_name(int i) {
			return getRuleContext(Table_nameContext.class,i);
		}
		public TerminalNode USING() { return getToken(DerbyParser.USING, 0); }
		public TerminalNode ON() { return getToken(DerbyParser.ON, 0); }
		public Search_conditionContext search_condition() {
			return getRuleContext(Search_conditionContext.class,0);
		}
		public List<Correlation_nameContext> correlation_name() {
			return getRuleContexts(Correlation_nameContext.class);
		}
		public Correlation_nameContext correlation_name(int i) {
			return getRuleContext(Correlation_nameContext.class,i);
		}
		public List<Merge_when_clauseContext> merge_when_clause() {
			return getRuleContexts(Merge_when_clauseContext.class);
		}
		public Merge_when_clauseContext merge_when_clause(int i) {
			return getRuleContext(Merge_when_clauseContext.class,i);
		}
		public List<TerminalNode> AS() { return getTokens(DerbyParser.AS); }
		public TerminalNode AS(int i) {
			return getToken(DerbyParser.AS, i);
		}
		public Merge_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_merge_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterMerge_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitMerge_statement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitMerge_statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Merge_statementContext merge_statement() throws RecognitionException {
		Merge_statementContext _localctx = new Merge_statementContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_merge_statement);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(407);
			match(MERGE);
			setState(408);
			match(INTO);
			setState(409);
			table_name();
			setState(414);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				{
				setState(411);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
				case 1:
					{
					setState(410);
					match(AS);
					}
					break;
				}
				setState(413);
				correlation_name();
				}
				break;
			}
			setState(416);
			match(USING);
			setState(417);
			table_name();
			setState(422);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				{
				setState(419);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
				case 1:
					{
					setState(418);
					match(AS);
					}
					break;
				}
				setState(421);
				correlation_name();
				}
				break;
			}
			setState(424);
			match(ON);
			setState(425);
			search_condition();
			setState(429);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(426);
					merge_when_clause();
					}
					} 
				}
				setState(431);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Merge_when_clauseContext extends ParserRuleContext {
		public Merge_when_matchedContext merge_when_matched() {
			return getRuleContext(Merge_when_matchedContext.class,0);
		}
		public Merge_when_not_matchedContext merge_when_not_matched() {
			return getRuleContext(Merge_when_not_matchedContext.class,0);
		}
		public Merge_when_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_merge_when_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterMerge_when_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitMerge_when_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitMerge_when_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Merge_when_clauseContext merge_when_clause() throws RecognitionException {
		Merge_when_clauseContext _localctx = new Merge_when_clauseContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_merge_when_clause);
		try {
			setState(434);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(432);
				merge_when_matched();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(433);
				merge_when_not_matched();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Merge_when_matchedContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(DerbyParser.WHEN, 0); }
		public TerminalNode MATCHED() { return getToken(DerbyParser.MATCHED, 0); }
		public TerminalNode THEN() { return getToken(DerbyParser.THEN, 0); }
		public Merge_updateContext merge_update() {
			return getRuleContext(Merge_updateContext.class,0);
		}
		public TerminalNode DELETE() { return getToken(DerbyParser.DELETE, 0); }
		public TerminalNode AND() { return getToken(DerbyParser.AND, 0); }
		public Match_refinementContext match_refinement() {
			return getRuleContext(Match_refinementContext.class,0);
		}
		public Merge_when_matchedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_merge_when_matched; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterMerge_when_matched(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitMerge_when_matched(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitMerge_when_matched(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Merge_when_matchedContext merge_when_matched() throws RecognitionException {
		Merge_when_matchedContext _localctx = new Merge_when_matchedContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_merge_when_matched);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(436);
			match(WHEN);
			setState(437);
			match(MATCHED);
			setState(440);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				{
				setState(438);
				match(AND);
				setState(439);
				match_refinement();
				}
				break;
			}
			setState(442);
			match(THEN);
			setState(445);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				{
				setState(443);
				merge_update();
				}
				break;
			case 2:
				{
				setState(444);
				match(DELETE);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Merge_when_not_matchedContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(DerbyParser.WHEN, 0); }
		public TerminalNode NOT() { return getToken(DerbyParser.NOT, 0); }
		public TerminalNode MATCHED() { return getToken(DerbyParser.MATCHED, 0); }
		public TerminalNode THEN() { return getToken(DerbyParser.THEN, 0); }
		public Merge_insertContext merge_insert() {
			return getRuleContext(Merge_insertContext.class,0);
		}
		public TerminalNode AND() { return getToken(DerbyParser.AND, 0); }
		public Match_refinementContext match_refinement() {
			return getRuleContext(Match_refinementContext.class,0);
		}
		public Merge_when_not_matchedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_merge_when_not_matched; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterMerge_when_not_matched(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitMerge_when_not_matched(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitMerge_when_not_matched(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Merge_when_not_matchedContext merge_when_not_matched() throws RecognitionException {
		Merge_when_not_matchedContext _localctx = new Merge_when_not_matchedContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_merge_when_not_matched);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(447);
			match(WHEN);
			setState(448);
			match(NOT);
			setState(449);
			match(MATCHED);
			{
			setState(450);
			match(AND);
			setState(451);
			match_refinement();
			}
			setState(453);
			match(THEN);
			setState(454);
			merge_insert();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Merge_updateContext extends ParserRuleContext {
		public TerminalNode UPDATE() { return getToken(DerbyParser.UPDATE, 0); }
		public TerminalNode SET() { return getToken(DerbyParser.SET, 0); }
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public List<TerminalNode> EQ() { return getTokens(DerbyParser.EQ); }
		public TerminalNode EQ(int i) {
			return getToken(DerbyParser.EQ, i);
		}
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Merge_updateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_merge_update; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterMerge_update(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitMerge_update(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitMerge_update(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Merge_updateContext merge_update() throws RecognitionException {
		Merge_updateContext _localctx = new Merge_updateContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_merge_update);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(456);
			match(UPDATE);
			setState(457);
			match(SET);
			setState(458);
			column_name();
			setState(459);
			match(EQ);
			setState(460);
			value();
			setState(468);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(461);
					match(COMMA);
					setState(462);
					column_name();
					setState(463);
					match(EQ);
					setState(464);
					value();
					}
					} 
				}
				setState(470);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Merge_insertContext extends ParserRuleContext {
		public TerminalNode INSERT() { return getToken(DerbyParser.INSERT, 0); }
		public TerminalNode VALUES() { return getToken(DerbyParser.VALUES, 0); }
		public List<TerminalNode> LR_BRACKET() { return getTokens(DerbyParser.LR_BRACKET); }
		public TerminalNode LR_BRACKET(int i) {
			return getToken(DerbyParser.LR_BRACKET, i);
		}
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public List<TerminalNode> RR_BRACKET() { return getTokens(DerbyParser.RR_BRACKET); }
		public TerminalNode RR_BRACKET(int i) {
			return getToken(DerbyParser.RR_BRACKET, i);
		}
		public List<Simple_column_nameContext> simple_column_name() {
			return getRuleContexts(Simple_column_nameContext.class);
		}
		public Simple_column_nameContext simple_column_name(int i) {
			return getRuleContext(Simple_column_nameContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Merge_insertContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_merge_insert; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterMerge_insert(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitMerge_insert(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitMerge_insert(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Merge_insertContext merge_insert() throws RecognitionException {
		Merge_insertContext _localctx = new Merge_insertContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_merge_insert);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(471);
			match(INSERT);
			setState(483);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				{
				setState(472);
				match(LR_BRACKET);
				setState(473);
				simple_column_name();
				setState(478);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(474);
						match(COMMA);
						setState(475);
						simple_column_name();
						}
						} 
					}
					setState(480);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
				}
				setState(481);
				match(RR_BRACKET);
				}
				break;
			}
			setState(485);
			match(VALUES);
			setState(486);
			match(LR_BRACKET);
			setState(487);
			value();
			setState(492);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(488);
					match(COMMA);
					setState(489);
					value();
					}
					} 
				}
				setState(494);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
			}
			setState(495);
			match(RR_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Search_conditionContext extends ParserRuleContext {
		public Boolean_expressionContext boolean_expression() {
			return getRuleContext(Boolean_expressionContext.class,0);
		}
		public Search_conditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_search_condition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSearch_condition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSearch_condition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSearch_condition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Search_conditionContext search_condition() throws RecognitionException {
		Search_conditionContext _localctx = new Search_conditionContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_search_condition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(497);
			boolean_expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Match_refinementContext extends ParserRuleContext {
		public Boolean_expressionContext boolean_expression() {
			return getRuleContext(Boolean_expressionContext.class,0);
		}
		public Match_refinementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_match_refinement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterMatch_refinement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitMatch_refinement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitMatch_refinement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Match_refinementContext match_refinement() throws RecognitionException {
		Match_refinementContext _localctx = new Match_refinementContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_match_refinement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(499);
			boolean_expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Update_statementContext extends ParserRuleContext {
		public TerminalNode UPDATE() { return getToken(DerbyParser.UPDATE, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public TerminalNode SET() { return getToken(DerbyParser.SET, 0); }
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public List<TerminalNode> EQ() { return getTokens(DerbyParser.EQ); }
		public TerminalNode EQ(int i) {
			return getToken(DerbyParser.EQ, i);
		}
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public Correlation_nameContext correlation_name() {
			return getRuleContext(Correlation_nameContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Where_clauseContext where_clause() {
			return getRuleContext(Where_clauseContext.class,0);
		}
		public TerminalNode AS() { return getToken(DerbyParser.AS, 0); }
		public TerminalNode WHERE() { return getToken(DerbyParser.WHERE, 0); }
		public TerminalNode CURRENT() { return getToken(DerbyParser.CURRENT, 0); }
		public TerminalNode OF() { return getToken(DerbyParser.OF, 0); }
		public Cursor_nNameContext cursor_nName() {
			return getRuleContext(Cursor_nNameContext.class,0);
		}
		public Update_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_update_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterUpdate_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitUpdate_statement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitUpdate_statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Update_statementContext update_statement() throws RecognitionException {
		Update_statementContext _localctx = new Update_statementContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_update_statement);
		try {
			int _alt;
			setState(547);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(501);
				match(UPDATE);
				setState(502);
				table_name();
				setState(507);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
				case 1:
					{
					setState(504);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
					case 1:
						{
						setState(503);
						match(AS);
						}
						break;
					}
					setState(506);
					correlation_name();
					}
					break;
				}
				setState(509);
				match(SET);
				setState(510);
				column_name();
				setState(511);
				match(EQ);
				setState(512);
				value();
				setState(520);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(513);
						match(COMMA);
						setState(514);
						column_name();
						setState(515);
						match(EQ);
						setState(516);
						value();
						}
						} 
					}
					setState(522);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
				}
				setState(524);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
				case 1:
					{
					setState(523);
					where_clause();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(526);
				match(UPDATE);
				setState(527);
				table_name();
				setState(528);
				match(SET);
				setState(529);
				column_name();
				setState(530);
				match(EQ);
				setState(531);
				value();
				setState(539);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(532);
						match(COMMA);
						setState(533);
						column_name();
						setState(534);
						match(EQ);
						setState(535);
						value();
						}
						} 
					}
					setState(541);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
				}
				setState(542);
				match(WHERE);
				setState(543);
				match(CURRENT);
				setState(544);
				match(OF);
				setState(545);
				cursor_nName();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Where_clauseContext extends ParserRuleContext {
		public TerminalNode WHERE() { return getToken(DerbyParser.WHERE, 0); }
		public Boolean_expressionContext boolean_expression() {
			return getRuleContext(Boolean_expressionContext.class,0);
		}
		public Where_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_where_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterWhere_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitWhere_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitWhere_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Where_clauseContext where_clause() throws RecognitionException {
		Where_clauseContext _localctx = new Where_clauseContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_where_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(549);
			match(WHERE);
			setState(550);
			boolean_expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Boolean_expressionContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode AND() { return getToken(DerbyParser.AND, 0); }
		public TerminalNode OR() { return getToken(DerbyParser.OR, 0); }
		public TerminalNode NOT() { return getToken(DerbyParser.NOT, 0); }
		public Comparison_operatorContext comparison_operator() {
			return getRuleContext(Comparison_operatorContext.class,0);
		}
		public TerminalNode IS() { return getToken(DerbyParser.IS, 0); }
		public TerminalNode NULL_() { return getToken(DerbyParser.NULL_, 0); }
		public List<Character_expressionContext> character_expression() {
			return getRuleContexts(Character_expressionContext.class);
		}
		public Character_expressionContext character_expression(int i) {
			return getRuleContext(Character_expressionContext.class,i);
		}
		public TerminalNode LIKE() { return getToken(DerbyParser.LIKE, 0); }
		public TerminalNode ESCAPE() { return getToken(DerbyParser.ESCAPE, 0); }
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public TerminalNode BETWEEN() { return getToken(DerbyParser.BETWEEN, 0); }
		public TerminalNode IN() { return getToken(DerbyParser.IN, 0); }
		public Table_subqueryContext table_subquery() {
			return getRuleContext(Table_subqueryContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public TerminalNode EXISTS() { return getToken(DerbyParser.EXISTS, 0); }
		public TerminalNode ALL() { return getToken(DerbyParser.ALL, 0); }
		public TerminalNode ANY() { return getToken(DerbyParser.ANY, 0); }
		public TerminalNode SOME() { return getToken(DerbyParser.SOME, 0); }
		public Boolean_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boolean_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterBoolean_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitBoolean_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitBoolean_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Boolean_expressionContext boolean_expression() throws RecognitionException {
		Boolean_expressionContext _localctx = new Boolean_expressionContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_boolean_expression);
		int _la;
		try {
			int _alt;
			setState(625);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(552);
				expression(0);
				setState(553);
				match(AND);
				setState(554);
				expression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(556);
				expression(0);
				setState(557);
				match(OR);
				setState(558);
				expression(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(560);
				match(NOT);
				setState(561);
				expression(0);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(562);
				expression(0);
				setState(563);
				comparison_operator();
				setState(564);
				expression(0);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(566);
				expression(0);
				setState(567);
				match(IS);
				setState(569);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
				case 1:
					{
					setState(568);
					match(NOT);
					}
					break;
				}
				setState(571);
				match(NULL_);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(573);
				character_expression();
				setState(575);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
				case 1:
					{
					setState(574);
					match(NOT);
					}
					break;
				}
				setState(577);
				match(LIKE);
				setState(578);
				character_expression();
				setState(581);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
				case 1:
					{
					setState(579);
					match(ESCAPE);
					setState(580);
					string();
					}
					break;
				}
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(583);
				expression(0);
				setState(585);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
				case 1:
					{
					setState(584);
					match(NOT);
					}
					break;
				}
				setState(587);
				match(BETWEEN);
				setState(588);
				expression(0);
				setState(589);
				match(AND);
				setState(590);
				expression(0);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(592);
				expression(0);
				setState(594);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
				case 1:
					{
					setState(593);
					match(NOT);
					}
					break;
				}
				setState(596);
				match(IN);
				setState(597);
				table_subquery();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(599);
				expression(0);
				setState(601);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
				case 1:
					{
					setState(600);
					match(NOT);
					}
					break;
				}
				setState(603);
				match(IN);
				setState(604);
				match(LR_BRACKET);
				setState(605);
				expression(0);
				setState(610);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,36,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(606);
						match(COMMA);
						setState(607);
						expression(0);
						}
						} 
					}
					setState(612);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,36,_ctx);
				}
				setState(613);
				match(RR_BRACKET);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(616);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
				case 1:
					{
					setState(615);
					match(NOT);
					}
					break;
				}
				setState(618);
				match(EXISTS);
				setState(619);
				table_subquery();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(620);
				expression(0);
				setState(621);
				comparison_operator();
				setState(622);
				_la = _input.LA(1);
				if ( !(_la==ALL || _la==ANY || _la==SOME) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(623);
				table_subquery();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Table_subqueryContext extends ParserRuleContext {
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public Order_by_clauseContext order_by_clause() {
			return getRuleContext(Order_by_clauseContext.class,0);
		}
		public Offset_clauseContext offset_clause() {
			return getRuleContext(Offset_clauseContext.class,0);
		}
		public Fetch_clauseContext fetch_clause() {
			return getRuleContext(Fetch_clauseContext.class,0);
		}
		public Table_subqueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_subquery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterTable_subquery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitTable_subquery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitTable_subquery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Table_subqueryContext table_subquery() throws RecognitionException {
		Table_subqueryContext _localctx = new Table_subqueryContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_table_subquery);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(627);
			match(LR_BRACKET);
			setState(628);
			query(0);
			setState(630);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				{
				setState(629);
				order_by_clause();
				}
				break;
			}
			setState(633);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
			case 1:
				{
				setState(632);
				offset_clause();
				}
				break;
			}
			setState(636);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				{
				setState(635);
				fetch_clause();
				}
				break;
			}
			setState(638);
			match(RR_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Character_expressionContext extends ParserRuleContext {
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public Character_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_character_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCharacter_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCharacter_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCharacter_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Character_expressionContext character_expression() throws RecognitionException {
		Character_expressionContext _localctx = new Character_expressionContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_character_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(640);
			string();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Comparison_operatorContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(DerbyParser.LT, 0); }
		public TerminalNode EQ() { return getToken(DerbyParser.EQ, 0); }
		public TerminalNode GT() { return getToken(DerbyParser.GT, 0); }
		public TerminalNode LE() { return getToken(DerbyParser.LE, 0); }
		public TerminalNode GE() { return getToken(DerbyParser.GE, 0); }
		public TerminalNode LTGT() { return getToken(DerbyParser.LTGT, 0); }
		public Comparison_operatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparison_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterComparison_operator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitComparison_operator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitComparison_operator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Comparison_operatorContext comparison_operator() throws RecognitionException {
		Comparison_operatorContext _localctx = new Comparison_operatorContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_comparison_operator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(642);
			_la = _input.LA(1);
			if ( !(((((_la - 339)) & ~0x3f) == 0 && ((1L << (_la - 339)) & 63L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ValueContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(DerbyParser.DEFAULT, 0); }
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_value);
		try {
			setState(646);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(644);
				expression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(645);
				match(DEFAULT);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Delete_statementContext extends ParserRuleContext {
		public TerminalNode DELETE() { return getToken(DerbyParser.DELETE, 0); }
		public TerminalNode FROM() { return getToken(DerbyParser.FROM, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public Correlation_nameContext correlation_name() {
			return getRuleContext(Correlation_nameContext.class,0);
		}
		public Where_clauseContext where_clause() {
			return getRuleContext(Where_clauseContext.class,0);
		}
		public TerminalNode AS() { return getToken(DerbyParser.AS, 0); }
		public TerminalNode WHERE() { return getToken(DerbyParser.WHERE, 0); }
		public TerminalNode CURRENT() { return getToken(DerbyParser.CURRENT, 0); }
		public TerminalNode OF() { return getToken(DerbyParser.OF, 0); }
		public Cursor_nNameContext cursor_nName() {
			return getRuleContext(Cursor_nNameContext.class,0);
		}
		public Delete_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_delete_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDelete_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDelete_statement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDelete_statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Delete_statementContext delete_statement() throws RecognitionException {
		Delete_statementContext _localctx = new Delete_statementContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_delete_statement);
		try {
			setState(668);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(648);
				match(DELETE);
				setState(649);
				match(FROM);
				setState(650);
				table_name();
				setState(655);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
				case 1:
					{
					setState(652);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
					case 1:
						{
						setState(651);
						match(AS);
						}
						break;
					}
					setState(654);
					correlation_name();
					}
					break;
				}
				setState(658);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
				case 1:
					{
					setState(657);
					where_clause();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(660);
				match(DELETE);
				setState(661);
				match(FROM);
				setState(662);
				table_name();
				setState(663);
				match(WHERE);
				setState(664);
				match(CURRENT);
				setState(665);
				match(OF);
				setState(666);
				cursor_nName();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Other_commandContext extends ParserRuleContext {
		public TerminalNode CALL() { return getToken(DerbyParser.CALL, 0); }
		public Procedure_nameContext procedure_name() {
			return getRuleContext(Procedure_nameContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public Expr_listContext expr_list() {
			return getRuleContext(Expr_listContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public TerminalNode DECLARE() { return getToken(DerbyParser.DECLARE, 0); }
		public TerminalNode GLOBAL() { return getToken(DerbyParser.GLOBAL, 0); }
		public TerminalNode TEMPORARY() { return getToken(DerbyParser.TEMPORARY, 0); }
		public List<TerminalNode> TABLE() { return getTokens(DerbyParser.TABLE); }
		public TerminalNode TABLE(int i) {
			return getToken(DerbyParser.TABLE, i);
		}
		public List<Table_nameContext> table_name() {
			return getRuleContexts(Table_nameContext.class);
		}
		public Table_nameContext table_name(int i) {
			return getRuleContext(Table_nameContext.class,i);
		}
		public List<Column_definitionContext> column_definition() {
			return getRuleContexts(Column_definitionContext.class);
		}
		public Column_definitionContext column_definition(int i) {
			return getRuleContext(Column_definitionContext.class,i);
		}
		public TerminalNode NOT() { return getToken(DerbyParser.NOT, 0); }
		public TerminalNode LOGGED() { return getToken(DerbyParser.LOGGED, 0); }
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public List<TerminalNode> ON() { return getTokens(DerbyParser.ON); }
		public TerminalNode ON(int i) {
			return getToken(DerbyParser.ON, i);
		}
		public TerminalNode COMMIT() { return getToken(DerbyParser.COMMIT, 0); }
		public List<TerminalNode> ROWS() { return getTokens(DerbyParser.ROWS); }
		public TerminalNode ROWS(int i) {
			return getToken(DerbyParser.ROWS, i);
		}
		public TerminalNode ROLLBACK() { return getToken(DerbyParser.ROLLBACK, 0); }
		public List<TerminalNode> DELETE() { return getTokens(DerbyParser.DELETE); }
		public TerminalNode DELETE(int i) {
			return getToken(DerbyParser.DELETE, i);
		}
		public TerminalNode PRESERVE() { return getToken(DerbyParser.PRESERVE, 0); }
		public TerminalNode GRANT() { return getToken(DerbyParser.GRANT, 0); }
		public Privilege_typeContext privilege_type() {
			return getRuleContext(Privilege_typeContext.class,0);
		}
		public TerminalNode TO() { return getToken(DerbyParser.TO, 0); }
		public GranteesContext grantees() {
			return getRuleContext(GranteesContext.class,0);
		}
		public View_nameContext view_name() {
			return getRuleContext(View_nameContext.class,0);
		}
		public TerminalNode EXECUTE() { return getToken(DerbyParser.EXECUTE, 0); }
		public TerminalNode FUNCTION() { return getToken(DerbyParser.FUNCTION, 0); }
		public Function_nameContext function_name() {
			return getRuleContext(Function_nameContext.class,0);
		}
		public TerminalNode PROCEDURE() { return getToken(DerbyParser.PROCEDURE, 0); }
		public TerminalNode USAGE() { return getToken(DerbyParser.USAGE, 0); }
		public TerminalNode SEQUENCE() { return getToken(DerbyParser.SEQUENCE, 0); }
		public Sequence_nameContext sequence_name() {
			return getRuleContext(Sequence_nameContext.class,0);
		}
		public TerminalNode TYPE() { return getToken(DerbyParser.TYPE, 0); }
		public Type_nameContext type_name() {
			return getRuleContext(Type_nameContext.class,0);
		}
		public TerminalNode DERBY() { return getToken(DerbyParser.DERBY, 0); }
		public TerminalNode AGGREGATE() { return getToken(DerbyParser.AGGREGATE, 0); }
		public Aggregate_nameContext aggregate_name() {
			return getRuleContext(Aggregate_nameContext.class,0);
		}
		public List<Role_nameContext> role_name() {
			return getRuleContexts(Role_nameContext.class);
		}
		public Role_nameContext role_name(int i) {
			return getRuleContext(Role_nameContext.class,i);
		}
		public TerminalNode LOCK() { return getToken(DerbyParser.LOCK, 0); }
		public TerminalNode IN() { return getToken(DerbyParser.IN, 0); }
		public TerminalNode MODE() { return getToken(DerbyParser.MODE, 0); }
		public TerminalNode SHARE() { return getToken(DerbyParser.SHARE, 0); }
		public TerminalNode EXCLUSIVE() { return getToken(DerbyParser.EXCLUSIVE, 0); }
		public List<TerminalNode> RENAME() { return getTokens(DerbyParser.RENAME); }
		public TerminalNode RENAME(int i) {
			return getToken(DerbyParser.RENAME, i);
		}
		public TerminalNode COLUMN() { return getToken(DerbyParser.COLUMN, 0); }
		public TerminalNode DOT() { return getToken(DerbyParser.DOT, 0); }
		public List<Simple_column_nameContext> simple_column_name() {
			return getRuleContexts(Simple_column_nameContext.class);
		}
		public Simple_column_nameContext simple_column_name(int i) {
			return getRuleContext(Simple_column_nameContext.class,i);
		}
		public TerminalNode INDEX() { return getToken(DerbyParser.INDEX, 0); }
		public List<Index_nameContext> index_name() {
			return getRuleContexts(Index_nameContext.class);
		}
		public Index_nameContext index_name(int i) {
			return getRuleContext(Index_nameContext.class,i);
		}
		public TerminalNode REVOKE() { return getToken(DerbyParser.REVOKE, 0); }
		public TerminalNode FROM() { return getToken(DerbyParser.FROM, 0); }
		public RevokeesContext revokees() {
			return getRuleContext(RevokeesContext.class,0);
		}
		public TerminalNode RESTRICT() { return getToken(DerbyParser.RESTRICT, 0); }
		public TerminalNode SET() { return getToken(DerbyParser.SET, 0); }
		public TerminalNode CONSTRAINTS() { return getToken(DerbyParser.CONSTRAINTS, 0); }
		public Constraint_name_listContext constraint_name_list() {
			return getRuleContext(Constraint_name_listContext.class,0);
		}
		public TerminalNode DEFERRED() { return getToken(DerbyParser.DEFERRED, 0); }
		public TerminalNode IMMEDIATE() { return getToken(DerbyParser.IMMEDIATE, 0); }
		public TerminalNode ISOLATION() { return getToken(DerbyParser.ISOLATION, 0); }
		public Isolation_levelContext isolation_level() {
			return getRuleContext(Isolation_levelContext.class,0);
		}
		public TerminalNode CURRENT() { return getToken(DerbyParser.CURRENT, 0); }
		public TerminalNode EQ() { return getToken(DerbyParser.EQ, 0); }
		public TerminalNode ROLE() { return getToken(DerbyParser.ROLE, 0); }
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public TerminalNode PLACEHOLDER() { return getToken(DerbyParser.PLACEHOLDER, 0); }
		public TerminalNode NONE() { return getToken(DerbyParser.NONE, 0); }
		public TerminalNode SCHEMA() { return getToken(DerbyParser.SCHEMA, 0); }
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode USER() { return getToken(DerbyParser.USER, 0); }
		public TerminalNode SQLID() { return getToken(DerbyParser.SQLID, 0); }
		public Other_commandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_other_command; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterOther_command(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitOther_command(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitOther_command(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Other_commandContext other_command() throws RecognitionException {
		Other_commandContext _localctx = new Other_commandContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_other_command);
		int _la;
		try {
			int _alt;
			setState(917);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,64,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(670);
				match(CALL);
				setState(671);
				procedure_name();
				setState(672);
				match(LR_BRACKET);
				setState(673);
				expr_list();
				setState(674);
				match(RR_BRACKET);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(676);
				match(DECLARE);
				setState(677);
				match(GLOBAL);
				setState(678);
				match(TEMPORARY);
				setState(679);
				match(TABLE);
				setState(680);
				table_name();
				setState(681);
				column_definition();
				setState(686);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,47,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(682);
						match(COMMA);
						setState(683);
						column_definition();
						}
						} 
					}
					setState(688);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,47,_ctx);
				}
				setState(693);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
				case 1:
					{
					setState(689);
					match(ON);
					setState(690);
					match(COMMIT);
					setState(691);
					_la = _input.LA(1);
					if ( !(_la==DELETE || _la==PRESERVE) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(692);
					match(ROWS);
					}
					break;
				}
				setState(695);
				match(NOT);
				setState(696);
				match(LOGGED);
				setState(701);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,49,_ctx) ) {
				case 1:
					{
					setState(697);
					match(ON);
					setState(698);
					match(ROLLBACK);
					setState(699);
					match(DELETE);
					setState(700);
					match(ROWS);
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(703);
				match(GRANT);
				setState(704);
				privilege_type();
				setState(705);
				match(ON);
				setState(707);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
				case 1:
					{
					setState(706);
					match(TABLE);
					}
					break;
				}
				setState(711);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,51,_ctx) ) {
				case 1:
					{
					setState(709);
					table_name();
					}
					break;
				case 2:
					{
					setState(710);
					view_name();
					}
					break;
				}
				setState(713);
				match(TO);
				setState(714);
				grantees();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(716);
				match(GRANT);
				setState(717);
				match(EXECUTE);
				setState(718);
				match(ON);
				setState(719);
				match(FUNCTION);
				setState(720);
				function_name();
				setState(721);
				match(TO);
				setState(722);
				grantees();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(724);
				match(GRANT);
				setState(725);
				match(EXECUTE);
				setState(726);
				match(ON);
				setState(727);
				match(PROCEDURE);
				setState(728);
				procedure_name();
				setState(729);
				match(TO);
				setState(730);
				grantees();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(732);
				match(GRANT);
				setState(733);
				match(USAGE);
				setState(734);
				match(ON);
				setState(735);
				match(SEQUENCE);
				setState(736);
				sequence_name();
				setState(737);
				match(TO);
				setState(738);
				grantees();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(740);
				match(GRANT);
				setState(741);
				match(USAGE);
				setState(742);
				match(ON);
				setState(743);
				match(TYPE);
				setState(744);
				type_name();
				setState(745);
				match(TO);
				setState(746);
				grantees();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(748);
				match(GRANT);
				setState(749);
				match(USAGE);
				setState(750);
				match(ON);
				setState(751);
				match(DERBY);
				setState(752);
				match(AGGREGATE);
				setState(753);
				aggregate_name();
				setState(754);
				match(TO);
				setState(755);
				grantees();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(757);
				match(GRANT);
				setState(758);
				role_name();
				setState(763);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,52,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(759);
						match(COMMA);
						setState(760);
						role_name();
						}
						} 
					}
					setState(765);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,52,_ctx);
				}
				setState(766);
				match(TO);
				setState(767);
				grantees();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(769);
				match(LOCK);
				setState(770);
				match(TABLE);
				setState(771);
				table_name();
				setState(772);
				match(IN);
				setState(773);
				_la = _input.LA(1);
				if ( !(_la==EXCLUSIVE || _la==SHARE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(774);
				match(MODE);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(776);
				match(RENAME);
				setState(777);
				match(COLUMN);
				setState(778);
				table_name();
				setState(779);
				match(DOT);
				setState(780);
				simple_column_name();
				setState(781);
				match(TO);
				setState(782);
				simple_column_name();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(784);
				match(RENAME);
				setState(785);
				match(INDEX);
				setState(786);
				index_name();
				setState(787);
				match(TO);
				setState(788);
				index_name();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(790);
				match(RENAME);
				setState(791);
				match(TABLE);
				setState(792);
				match(RENAME);
				setState(793);
				match(TABLE);
				setState(794);
				table_name();
				setState(795);
				match(TO);
				setState(796);
				table_name();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(798);
				match(REVOKE);
				setState(799);
				privilege_type();
				setState(800);
				match(ON);
				setState(802);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,53,_ctx) ) {
				case 1:
					{
					setState(801);
					match(TABLE);
					}
					break;
				}
				setState(806);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,54,_ctx) ) {
				case 1:
					{
					setState(804);
					table_name();
					}
					break;
				case 2:
					{
					setState(805);
					view_name();
					}
					break;
				}
				setState(808);
				match(FROM);
				setState(809);
				revokees();
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(811);
				match(REVOKE);
				setState(812);
				match(EXECUTE);
				setState(813);
				match(ON);
				setState(814);
				match(FUNCTION);
				setState(815);
				function_name();
				setState(816);
				match(FROM);
				setState(817);
				revokees();
				setState(818);
				match(RESTRICT);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(820);
				match(REVOKE);
				setState(821);
				match(EXECUTE);
				setState(822);
				match(ON);
				setState(823);
				match(PROCEDURE);
				setState(824);
				procedure_name();
				setState(825);
				match(FROM);
				setState(826);
				revokees();
				setState(827);
				match(RESTRICT);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(829);
				match(REVOKE);
				setState(830);
				match(USAGE);
				setState(831);
				match(ON);
				setState(832);
				match(SEQUENCE);
				setState(833);
				sequence_name();
				setState(834);
				match(FROM);
				setState(835);
				revokees();
				setState(836);
				match(RESTRICT);
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(838);
				match(REVOKE);
				setState(839);
				match(USAGE);
				setState(840);
				match(ON);
				setState(841);
				match(TYPE);
				setState(842);
				type_name();
				setState(843);
				match(FROM);
				setState(844);
				revokees();
				setState(845);
				match(RESTRICT);
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(847);
				match(REVOKE);
				setState(848);
				match(USAGE);
				setState(849);
				match(ON);
				setState(850);
				match(DERBY);
				setState(851);
				match(AGGREGATE);
				setState(852);
				aggregate_name();
				setState(853);
				match(FROM);
				setState(854);
				revokees();
				setState(855);
				match(RESTRICT);
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(857);
				match(REVOKE);
				setState(858);
				role_name();
				setState(863);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,55,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(859);
						match(COMMA);
						setState(860);
						role_name();
						}
						} 
					}
					setState(865);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,55,_ctx);
				}
				setState(866);
				match(FROM);
				setState(867);
				revokees();
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 21);
				{
				setState(869);
				match(SET);
				setState(870);
				match(CONSTRAINTS);
				setState(871);
				constraint_name_list();
				setState(872);
				_la = _input.LA(1);
				if ( !(_la==DEFERRED || _la==IMMEDIATE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 22);
				{
				setState(874);
				match(SET);
				setState(876);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
				case 1:
					{
					setState(875);
					match(CURRENT);
					}
					break;
				}
				setState(878);
				match(ISOLATION);
				setState(880);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,57,_ctx) ) {
				case 1:
					{
					setState(879);
					match(EQ);
					}
					break;
				}
				setState(882);
				isolation_level();
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 23);
				{
				setState(883);
				match(SET);
				setState(884);
				match(ROLE);
				setState(889);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,58,_ctx) ) {
				case 1:
					{
					setState(885);
					role_name();
					}
					break;
				case 2:
					{
					setState(886);
					string();
					}
					break;
				case 3:
					{
					setState(887);
					match(PLACEHOLDER);
					}
					break;
				case 4:
					{
					setState(888);
					match(NONE);
					}
					break;
				}
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 24);
				{
				setState(891);
				match(SET);
				setState(893);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
				case 1:
					{
					setState(892);
					match(CURRENT);
					}
					break;
				}
				setState(895);
				match(SCHEMA);
				setState(897);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,60,_ctx) ) {
				case 1:
					{
					setState(896);
					match(EQ);
					}
					break;
				}
				setState(903);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,61,_ctx) ) {
				case 1:
					{
					setState(899);
					schema_name();
					}
					break;
				case 2:
					{
					setState(900);
					match(USER);
					}
					break;
				case 3:
					{
					setState(901);
					match(PLACEHOLDER);
					}
					break;
				case 4:
					{
					setState(902);
					string();
					}
					break;
				}
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 25);
				{
				setState(905);
				match(SET);
				setState(906);
				match(CURRENT);
				setState(907);
				match(SQLID);
				setState(909);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,62,_ctx) ) {
				case 1:
					{
					setState(908);
					match(EQ);
					}
					break;
				}
				setState(915);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,63,_ctx) ) {
				case 1:
					{
					setState(911);
					schema_name();
					}
					break;
				case 2:
					{
					setState(912);
					match(USER);
					}
					break;
				case 3:
					{
					setState(913);
					match(PLACEHOLDER);
					}
					break;
				case 4:
					{
					setState(914);
					string();
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GranteesContext extends ParserRuleContext {
		public List<GranteeContext> grantee() {
			return getRuleContexts(GranteeContext.class);
		}
		public GranteeContext grantee(int i) {
			return getRuleContext(GranteeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public GranteesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_grantees; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterGrantees(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitGrantees(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitGrantees(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GranteesContext grantees() throws RecognitionException {
		GranteesContext _localctx = new GranteesContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_grantees);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(919);
			grantee();
			setState(924);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,65,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(920);
					match(COMMA);
					setState(921);
					grantee();
					}
					} 
				}
				setState(926);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,65,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GranteeContext extends ParserRuleContext {
		public Authorization_identifierContext authorization_identifier() {
			return getRuleContext(Authorization_identifierContext.class,0);
		}
		public Role_nameContext role_name() {
			return getRuleContext(Role_nameContext.class,0);
		}
		public TerminalNode PUBLIC() { return getToken(DerbyParser.PUBLIC, 0); }
		public GranteeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_grantee; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterGrantee(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitGrantee(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitGrantee(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GranteeContext grantee() throws RecognitionException {
		GranteeContext _localctx = new GranteeContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_grantee);
		try {
			setState(930);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,66,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(927);
				authorization_identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(928);
				role_name();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(929);
				match(PUBLIC);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Isolation_levelContext extends ParserRuleContext {
		public TerminalNode UR() { return getToken(DerbyParser.UR, 0); }
		public TerminalNode DIRTY() { return getToken(DerbyParser.DIRTY, 0); }
		public TerminalNode READ() { return getToken(DerbyParser.READ, 0); }
		public TerminalNode UNCOMMITTED() { return getToken(DerbyParser.UNCOMMITTED, 0); }
		public TerminalNode CS() { return getToken(DerbyParser.CS, 0); }
		public TerminalNode COMMITTED() { return getToken(DerbyParser.COMMITTED, 0); }
		public TerminalNode CURSOR() { return getToken(DerbyParser.CURSOR, 0); }
		public TerminalNode STABILITY() { return getToken(DerbyParser.STABILITY, 0); }
		public TerminalNode RS() { return getToken(DerbyParser.RS, 0); }
		public TerminalNode RR() { return getToken(DerbyParser.RR, 0); }
		public TerminalNode REPEATABLE() { return getToken(DerbyParser.REPEATABLE, 0); }
		public TerminalNode SERIALIZABLE() { return getToken(DerbyParser.SERIALIZABLE, 0); }
		public TerminalNode RESET() { return getToken(DerbyParser.RESET, 0); }
		public Isolation_levelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_isolation_level; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterIsolation_level(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitIsolation_level(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitIsolation_level(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Isolation_levelContext isolation_level() throws RecognitionException {
		Isolation_levelContext _localctx = new Isolation_levelContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_isolation_level);
		try {
			setState(948);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,67,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(932);
				match(UR);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(933);
				match(DIRTY);
				setState(934);
				match(READ);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(935);
				match(READ);
				setState(936);
				match(UNCOMMITTED);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(937);
				match(CS);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(938);
				match(READ);
				setState(939);
				match(COMMITTED);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(940);
				match(CURSOR);
				setState(941);
				match(STABILITY);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(942);
				match(RS);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(943);
				match(RR);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(944);
				match(REPEATABLE);
				setState(945);
				match(READ);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(946);
				match(SERIALIZABLE);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(947);
				match(RESET);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Constraint_name_listContext extends ParserRuleContext {
		public TerminalNode ALL() { return getToken(DerbyParser.ALL, 0); }
		public List<Constraint_nameContext> constraint_name() {
			return getRuleContexts(Constraint_nameContext.class);
		}
		public Constraint_nameContext constraint_name(int i) {
			return getRuleContext(Constraint_nameContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Constraint_name_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraint_name_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterConstraint_name_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitConstraint_name_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitConstraint_name_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Constraint_name_listContext constraint_name_list() throws RecognitionException {
		Constraint_name_listContext _localctx = new Constraint_name_listContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_constraint_name_list);
		try {
			int _alt;
			setState(959);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,69,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(950);
				match(ALL);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(951);
				constraint_name();
				setState(956);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,68,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(952);
						match(COMMA);
						setState(953);
						constraint_name();
						}
						} 
					}
					setState(958);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,68,_ctx);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Column_definitionContext extends ParserRuleContext {
		public Simple_column_nameContext simple_column_name() {
			return getRuleContext(Simple_column_nameContext.class,0);
		}
		public Data_typeContext data_type() {
			return getRuleContext(Data_typeContext.class,0);
		}
		public List<With_defaultContext> with_default() {
			return getRuleContexts(With_defaultContext.class);
		}
		public With_defaultContext with_default(int i) {
			return getRuleContext(With_defaultContext.class,i);
		}
		public Column_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterColumn_definition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitColumn_definition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitColumn_definition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_definitionContext column_definition() throws RecognitionException {
		Column_definitionContext _localctx = new Column_definitionContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_column_definition);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(961);
			simple_column_name();
			setState(963);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,70,_ctx) ) {
			case 1:
				{
				setState(962);
				data_type();
				}
				break;
			}
			setState(968);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,71,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(965);
					with_default();
					}
					} 
				}
				setState(970);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,71,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class With_defaultContext extends ParserRuleContext {
		public TerminalNode DEFAULT() { return getToken(DerbyParser.DEFAULT, 0); }
		public Default_constant_expressionContext default_constant_expression() {
			return getRuleContext(Default_constant_expressionContext.class,0);
		}
		public TerminalNode WITH() { return getToken(DerbyParser.WITH, 0); }
		public Generated_column_specContext generated_column_spec() {
			return getRuleContext(Generated_column_specContext.class,0);
		}
		public Generation_clauseContext generation_clause() {
			return getRuleContext(Generation_clauseContext.class,0);
		}
		public With_defaultContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_with_default; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterWith_default(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitWith_default(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitWith_default(this);
			else return visitor.visitChildren(this);
		}
	}

	public final With_defaultContext with_default() throws RecognitionException {
		With_defaultContext _localctx = new With_defaultContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_with_default);
		try {
			setState(978);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,73,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(972);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,72,_ctx) ) {
				case 1:
					{
					setState(971);
					match(WITH);
					}
					break;
				}
				setState(974);
				match(DEFAULT);
				setState(975);
				default_constant_expression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(976);
				generated_column_spec();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(977);
				generation_clause();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Default_constant_expressionContext extends ParserRuleContext {
		public TerminalNode NULL_() { return getToken(DerbyParser.NULL_, 0); }
		public TerminalNode CURRENT() { return getToken(DerbyParser.CURRENT, 0); }
		public TerminalNode SCHEMA() { return getToken(DerbyParser.SCHEMA, 0); }
		public TerminalNode SQLID() { return getToken(DerbyParser.SQLID, 0); }
		public TerminalNode USER() { return getToken(DerbyParser.USER, 0); }
		public TerminalNode CURRENT_USER() { return getToken(DerbyParser.CURRENT_USER, 0); }
		public TerminalNode SESSION_USER() { return getToken(DerbyParser.SESSION_USER, 0); }
		public TerminalNode CURRENT_ROLE() { return getToken(DerbyParser.CURRENT_ROLE, 0); }
		public TerminalNode DATE() { return getToken(DerbyParser.DATE, 0); }
		public TerminalNode TIME() { return getToken(DerbyParser.TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(DerbyParser.TIMESTAMP, 0); }
		public TerminalNode CURRENT_DATE() { return getToken(DerbyParser.CURRENT_DATE, 0); }
		public TerminalNode CURRENT_TIME() { return getToken(DerbyParser.CURRENT_TIME, 0); }
		public TerminalNode CURRENT_TIMESTAMP() { return getToken(DerbyParser.CURRENT_TIMESTAMP, 0); }
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public Default_constant_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_default_constant_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDefault_constant_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDefault_constant_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDefault_constant_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Default_constant_expressionContext default_constant_expression() throws RecognitionException {
		Default_constant_expressionContext _localctx = new Default_constant_expressionContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_default_constant_expression);
		int _la;
		try {
			setState(1000);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,74,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(980);
				match(NULL_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(981);
				match(CURRENT);
				setState(982);
				_la = _input.LA(1);
				if ( !(_la==SCHEMA || _la==SQLID) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(983);
				match(USER);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(984);
				match(CURRENT_USER);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(985);
				match(SESSION_USER);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(986);
				match(CURRENT_ROLE);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(987);
				match(DATE);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(988);
				match(TIME);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(989);
				match(TIMESTAMP);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(990);
				match(CURRENT);
				setState(991);
				match(DATE);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(992);
				match(CURRENT_DATE);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(993);
				match(CURRENT);
				setState(994);
				match(TIME);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(995);
				match(CURRENT_TIME);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(996);
				match(CURRENT);
				setState(997);
				match(TIMESTAMP);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(998);
				match(CURRENT_TIMESTAMP);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(999);
				literal();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Generated_column_specContext extends ParserRuleContext {
		public TerminalNode GENERATED() { return getToken(DerbyParser.GENERATED, 0); }
		public Always_by_defaultContext always_by_default() {
			return getRuleContext(Always_by_defaultContext.class,0);
		}
		public TerminalNode AS() { return getToken(DerbyParser.AS, 0); }
		public TerminalNode IDENTITY() { return getToken(DerbyParser.IDENTITY, 0); }
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public TerminalNode START() { return getToken(DerbyParser.START, 0); }
		public TerminalNode WITH() { return getToken(DerbyParser.WITH, 0); }
		public Integer_constantContext integer_constant() {
			return getRuleContext(Integer_constantContext.class,0);
		}
		public TerminalNode INCREMENT() { return getToken(DerbyParser.INCREMENT, 0); }
		public TerminalNode BY() { return getToken(DerbyParser.BY, 0); }
		public TerminalNode CYCLE() { return getToken(DerbyParser.CYCLE, 0); }
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public TerminalNode NO() { return getToken(DerbyParser.NO, 0); }
		public Generated_column_specContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_generated_column_spec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterGenerated_column_spec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitGenerated_column_spec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitGenerated_column_spec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Generated_column_specContext generated_column_spec() throws RecognitionException {
		Generated_column_specContext _localctx = new Generated_column_specContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_generated_column_spec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1002);
			match(GENERATED);
			setState(1003);
			always_by_default();
			setState(1004);
			match(AS);
			setState(1005);
			match(IDENTITY);
			setState(1018);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,76,_ctx) ) {
			case 1:
				{
				setState(1006);
				match(LR_BRACKET);
				setState(1007);
				match(START);
				setState(1008);
				match(WITH);
				setState(1009);
				integer_constant();
				}
				break;
			case 2:
				{
				setState(1010);
				match(INCREMENT);
				setState(1011);
				match(BY);
				setState(1012);
				integer_constant();
				}
				break;
			case 3:
				{
				setState(1014);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,75,_ctx) ) {
				case 1:
					{
					setState(1013);
					match(NO);
					}
					break;
				}
				setState(1016);
				match(CYCLE);
				setState(1017);
				match(RR_BRACKET);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Generation_clauseContext extends ParserRuleContext {
		public TerminalNode GENERATED() { return getToken(DerbyParser.GENERATED, 0); }
		public TerminalNode ALWAYS() { return getToken(DerbyParser.ALWAYS, 0); }
		public TerminalNode AS() { return getToken(DerbyParser.AS, 0); }
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public Values_expressionContext values_expression() {
			return getRuleContext(Values_expressionContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public Generation_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_generation_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterGeneration_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitGeneration_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitGeneration_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Generation_clauseContext generation_clause() throws RecognitionException {
		Generation_clauseContext _localctx = new Generation_clauseContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_generation_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1020);
			match(GENERATED);
			setState(1021);
			match(ALWAYS);
			setState(1022);
			match(AS);
			setState(1023);
			match(LR_BRACKET);
			setState(1024);
			values_expression();
			setState(1025);
			match(RR_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Column_level_constraintContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(DerbyParser.NOT, 0); }
		public TerminalNode NULL_() { return getToken(DerbyParser.NULL_, 0); }
		public TerminalNode CHECK() { return getToken(DerbyParser.CHECK, 0); }
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public Search_conditionContext search_condition() {
			return getRuleContext(Search_conditionContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public TerminalNode PRIMARY() { return getToken(DerbyParser.PRIMARY, 0); }
		public TerminalNode KEY() { return getToken(DerbyParser.KEY, 0); }
		public TerminalNode UNIQUE() { return getToken(DerbyParser.UNIQUE, 0); }
		public References_clauseContext references_clause() {
			return getRuleContext(References_clauseContext.class,0);
		}
		public TerminalNode CONSTRAINT() { return getToken(DerbyParser.CONSTRAINT, 0); }
		public Constraint_nameContext constraint_name() {
			return getRuleContext(Constraint_nameContext.class,0);
		}
		public Constraint_characteristicsContext constraint_characteristics() {
			return getRuleContext(Constraint_characteristicsContext.class,0);
		}
		public Column_level_constraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_level_constraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterColumn_level_constraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitColumn_level_constraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitColumn_level_constraint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_level_constraintContext column_level_constraint() throws RecognitionException {
		Column_level_constraintContext _localctx = new Column_level_constraintContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_column_level_constraint);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1029);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,77,_ctx) ) {
			case 1:
				{
				setState(1027);
				match(CONSTRAINT);
				setState(1028);
				constraint_name();
				}
				break;
			}
			setState(1042);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
			case 1:
				{
				setState(1031);
				match(NOT);
				setState(1032);
				match(NULL_);
				}
				break;
			case 2:
				{
				setState(1033);
				match(CHECK);
				setState(1034);
				match(LR_BRACKET);
				setState(1035);
				search_condition();
				setState(1036);
				match(RR_BRACKET);
				}
				break;
			case 3:
				{
				setState(1038);
				match(PRIMARY);
				setState(1039);
				match(KEY);
				}
				break;
			case 4:
				{
				setState(1040);
				match(UNIQUE);
				}
				break;
			case 5:
				{
				setState(1041);
				references_clause();
				}
				break;
			}
			setState(1045);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,79,_ctx) ) {
			case 1:
				{
				setState(1044);
				constraint_characteristics();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Table_level_constraintContext extends ParserRuleContext {
		public TerminalNode CHECK() { return getToken(DerbyParser.CHECK, 0); }
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public Search_conditionContext search_condition() {
			return getRuleContext(Search_conditionContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public TerminalNode CONSTRAINT() { return getToken(DerbyParser.CONSTRAINT, 0); }
		public Constraint_nameContext constraint_name() {
			return getRuleContext(Constraint_nameContext.class,0);
		}
		public Constraint_characteristicsContext constraint_characteristics() {
			return getRuleContext(Constraint_characteristicsContext.class,0);
		}
		public TerminalNode PRIMARY() { return getToken(DerbyParser.PRIMARY, 0); }
		public TerminalNode KEY() { return getToken(DerbyParser.KEY, 0); }
		public Simple_column_name_listContext simple_column_name_list() {
			return getRuleContext(Simple_column_name_listContext.class,0);
		}
		public TerminalNode UNIQUE() { return getToken(DerbyParser.UNIQUE, 0); }
		public TerminalNode FOREIGN() { return getToken(DerbyParser.FOREIGN, 0); }
		public References_clauseContext references_clause() {
			return getRuleContext(References_clauseContext.class,0);
		}
		public Table_level_constraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_level_constraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterTable_level_constraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitTable_level_constraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitTable_level_constraint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Table_level_constraintContext table_level_constraint() throws RecognitionException {
		Table_level_constraintContext _localctx = new Table_level_constraintContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_table_level_constraint);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1049);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,80,_ctx) ) {
			case 1:
				{
				setState(1047);
				match(CONSTRAINT);
				setState(1048);
				constraint_name();
				}
				break;
			}
			setState(1076);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
			case 1:
				{
				setState(1051);
				match(CHECK);
				setState(1052);
				match(LR_BRACKET);
				setState(1053);
				search_condition();
				setState(1054);
				match(RR_BRACKET);
				}
				break;
			case 2:
				{
				setState(1074);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,81,_ctx) ) {
				case 1:
					{
					setState(1056);
					match(PRIMARY);
					setState(1057);
					match(KEY);
					setState(1058);
					match(LR_BRACKET);
					setState(1059);
					simple_column_name_list();
					setState(1060);
					match(RR_BRACKET);
					}
					break;
				case 2:
					{
					setState(1062);
					match(UNIQUE);
					setState(1063);
					match(LR_BRACKET);
					setState(1064);
					simple_column_name_list();
					setState(1065);
					match(RR_BRACKET);
					}
					break;
				case 3:
					{
					setState(1067);
					match(FOREIGN);
					setState(1068);
					match(KEY);
					setState(1069);
					match(LR_BRACKET);
					setState(1070);
					simple_column_name_list();
					setState(1071);
					match(RR_BRACKET);
					setState(1072);
					references_clause();
					}
					break;
				}
				}
				break;
			}
			setState(1079);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,83,_ctx) ) {
			case 1:
				{
				setState(1078);
				constraint_characteristics();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class References_clauseContext extends ParserRuleContext {
		public TerminalNode REFERENCES() { return getToken(DerbyParser.REFERENCES, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public Simple_column_name_listContext simple_column_name_list() {
			return getRuleContext(Simple_column_name_listContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public List<TerminalNode> ON() { return getTokens(DerbyParser.ON); }
		public TerminalNode ON(int i) {
			return getToken(DerbyParser.ON, i);
		}
		public TerminalNode DELETE() { return getToken(DerbyParser.DELETE, 0); }
		public TerminalNode UPDATE() { return getToken(DerbyParser.UPDATE, 0); }
		public No_action_restrictContext no_action_restrict() {
			return getRuleContext(No_action_restrictContext.class,0);
		}
		public TerminalNode NO() { return getToken(DerbyParser.NO, 0); }
		public TerminalNode ACTION() { return getToken(DerbyParser.ACTION, 0); }
		public TerminalNode RESTRICT() { return getToken(DerbyParser.RESTRICT, 0); }
		public TerminalNode CASCADE() { return getToken(DerbyParser.CASCADE, 0); }
		public TerminalNode SET() { return getToken(DerbyParser.SET, 0); }
		public TerminalNode NULL_() { return getToken(DerbyParser.NULL_, 0); }
		public References_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_references_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterReferences_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitReferences_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitReferences_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final References_clauseContext references_clause() throws RecognitionException {
		References_clauseContext _localctx = new References_clauseContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_references_clause);
		try {
			setState(1123);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1081);
				match(REFERENCES);
				setState(1082);
				table_name();
				setState(1087);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,84,_ctx) ) {
				case 1:
					{
					setState(1083);
					match(LR_BRACKET);
					setState(1084);
					simple_column_name_list();
					setState(1085);
					match(RR_BRACKET);
					}
					break;
				}
				setState(1099);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
				case 1:
					{
					setState(1089);
					match(ON);
					setState(1090);
					match(DELETE);
					setState(1097);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,85,_ctx) ) {
					case 1:
						{
						setState(1091);
						match(NO);
						setState(1092);
						match(ACTION);
						}
						break;
					case 2:
						{
						setState(1093);
						match(RESTRICT);
						}
						break;
					case 3:
						{
						setState(1094);
						match(CASCADE);
						}
						break;
					case 4:
						{
						setState(1095);
						match(SET);
						setState(1096);
						match(NULL_);
						}
						break;
					}
					}
					break;
				}
				setState(1104);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,87,_ctx) ) {
				case 1:
					{
					setState(1101);
					match(ON);
					setState(1102);
					match(UPDATE);
					setState(1103);
					no_action_restrict();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1109);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,88,_ctx) ) {
				case 1:
					{
					setState(1106);
					match(ON);
					setState(1107);
					match(UPDATE);
					setState(1108);
					no_action_restrict();
					}
					break;
				}
				setState(1121);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,90,_ctx) ) {
				case 1:
					{
					setState(1111);
					match(ON);
					setState(1112);
					match(DELETE);
					setState(1119);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,89,_ctx) ) {
					case 1:
						{
						setState(1113);
						match(NO);
						setState(1114);
						match(ACTION);
						}
						break;
					case 2:
						{
						setState(1115);
						match(RESTRICT);
						}
						break;
					case 3:
						{
						setState(1116);
						match(CASCADE);
						}
						break;
					case 4:
						{
						setState(1117);
						match(SET);
						setState(1118);
						match(NULL_);
						}
						break;
					}
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class No_action_restrictContext extends ParserRuleContext {
		public TerminalNode NO() { return getToken(DerbyParser.NO, 0); }
		public TerminalNode ACTION() { return getToken(DerbyParser.ACTION, 0); }
		public TerminalNode RESTRICT() { return getToken(DerbyParser.RESTRICT, 0); }
		public No_action_restrictContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_no_action_restrict; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterNo_action_restrict(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitNo_action_restrict(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitNo_action_restrict(this);
			else return visitor.visitChildren(this);
		}
	}

	public final No_action_restrictContext no_action_restrict() throws RecognitionException {
		No_action_restrictContext _localctx = new No_action_restrictContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_no_action_restrict);
		try {
			setState(1128);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,92,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1125);
				match(NO);
				setState(1126);
				match(ACTION);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1127);
				match(RESTRICT);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Constraint_characteristicsContext extends ParserRuleContext {
		public Constraint_check_timeContext constraint_check_time() {
			return getRuleContext(Constraint_check_timeContext.class,0);
		}
		public TerminalNode DEFERRABLE() { return getToken(DerbyParser.DEFERRABLE, 0); }
		public TerminalNode NOT() { return getToken(DerbyParser.NOT, 0); }
		public Constraint_characteristicsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraint_characteristics; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterConstraint_characteristics(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitConstraint_characteristics(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitConstraint_characteristics(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Constraint_characteristicsContext constraint_characteristics() throws RecognitionException {
		Constraint_characteristicsContext _localctx = new Constraint_characteristicsContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_constraint_characteristics);
		try {
			setState(1144);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,97,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1130);
				constraint_check_time();
				setState(1135);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,94,_ctx) ) {
				case 1:
					{
					setState(1132);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,93,_ctx) ) {
					case 1:
						{
						setState(1131);
						match(NOT);
						}
						break;
					}
					setState(1134);
					match(DEFERRABLE);
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1138);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,95,_ctx) ) {
				case 1:
					{
					setState(1137);
					match(NOT);
					}
					break;
				}
				setState(1140);
				match(DEFERRABLE);
				setState(1142);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,96,_ctx) ) {
				case 1:
					{
					setState(1141);
					constraint_check_time();
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Constraint_check_timeContext extends ParserRuleContext {
		public TerminalNode INITIALLY() { return getToken(DerbyParser.INITIALLY, 0); }
		public TerminalNode DEFERRED() { return getToken(DerbyParser.DEFERRED, 0); }
		public TerminalNode IMMEDIATE() { return getToken(DerbyParser.IMMEDIATE, 0); }
		public Constraint_check_timeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraint_check_time; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterConstraint_check_time(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitConstraint_check_time(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitConstraint_check_time(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Constraint_check_timeContext constraint_check_time() throws RecognitionException {
		Constraint_check_timeContext _localctx = new Constraint_check_timeContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_constraint_check_time);
		try {
			setState(1150);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,98,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1146);
				match(INITIALLY);
				setState(1147);
				match(DEFERRED);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1148);
				match(INITIALLY);
				setState(1149);
				match(IMMEDIATE);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Simple_column_name_listContext extends ParserRuleContext {
		public List<Simple_column_nameContext> simple_column_name() {
			return getRuleContexts(Simple_column_nameContext.class);
		}
		public Simple_column_nameContext simple_column_name(int i) {
			return getRuleContext(Simple_column_nameContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Simple_column_name_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simple_column_name_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSimple_column_name_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSimple_column_name_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSimple_column_name_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Simple_column_name_listContext simple_column_name_list() throws RecognitionException {
		Simple_column_name_listContext _localctx = new Simple_column_name_listContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_simple_column_name_list);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1152);
			simple_column_name();
			setState(1157);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,99,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1153);
					match(COMMA);
					setState(1154);
					simple_column_name();
					}
					} 
				}
				setState(1159);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,99,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Truncate_tableContext extends ParserRuleContext {
		public TerminalNode TRUNCATE() { return getToken(DerbyParser.TRUNCATE, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public TerminalNode TABLE() { return getToken(DerbyParser.TABLE, 0); }
		public Truncate_tableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_truncate_table; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterTruncate_table(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitTruncate_table(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitTruncate_table(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Truncate_tableContext truncate_table() throws RecognitionException {
		Truncate_tableContext _localctx = new Truncate_tableContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_truncate_table);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1160);
			match(TRUNCATE);
			setState(1162);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,100,_ctx) ) {
			case 1:
				{
				setState(1161);
				match(TABLE);
				}
				break;
			}
			setState(1164);
			table_name();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Privilege_typeContext extends ParserRuleContext {
		public TerminalNode ALL() { return getToken(DerbyParser.ALL, 0); }
		public TerminalNode PRIVILEGES() { return getToken(DerbyParser.PRIVILEGES, 0); }
		public Privilege_listContext privilege_list() {
			return getRuleContext(Privilege_listContext.class,0);
		}
		public Privilege_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_privilege_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterPrivilege_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitPrivilege_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitPrivilege_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Privilege_typeContext privilege_type() throws RecognitionException {
		Privilege_typeContext _localctx = new Privilege_typeContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_privilege_type);
		try {
			setState(1169);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,101,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1166);
				match(ALL);
				setState(1167);
				match(PRIVILEGES);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1168);
				privilege_list();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Privilege_listContext extends ParserRuleContext {
		public List<Table_privilegeContext> table_privilege() {
			return getRuleContexts(Table_privilegeContext.class);
		}
		public Table_privilegeContext table_privilege(int i) {
			return getRuleContext(Table_privilegeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Privilege_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_privilege_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterPrivilege_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitPrivilege_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitPrivilege_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Privilege_listContext privilege_list() throws RecognitionException {
		Privilege_listContext _localctx = new Privilege_listContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_privilege_list);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1171);
			table_privilege();
			setState(1176);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,102,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1172);
					match(COMMA);
					setState(1173);
					table_privilege();
					}
					} 
				}
				setState(1178);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,102,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Table_privilegeContext extends ParserRuleContext {
		public TerminalNode DELETE() { return getToken(DerbyParser.DELETE, 0); }
		public TerminalNode INSERT() { return getToken(DerbyParser.INSERT, 0); }
		public TerminalNode REFERENCES() { return getToken(DerbyParser.REFERENCES, 0); }
		public Column_listContext column_list() {
			return getRuleContext(Column_listContext.class,0);
		}
		public TerminalNode SELECT() { return getToken(DerbyParser.SELECT, 0); }
		public TerminalNode TRIGGER() { return getToken(DerbyParser.TRIGGER, 0); }
		public TerminalNode UPDATE() { return getToken(DerbyParser.UPDATE, 0); }
		public Table_privilegeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_privilege; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterTable_privilege(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitTable_privilege(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitTable_privilege(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Table_privilegeContext table_privilege() throws RecognitionException {
		Table_privilegeContext _localctx = new Table_privilegeContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_table_privilege);
		try {
			setState(1194);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,106,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1179);
				match(DELETE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1180);
				match(INSERT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1181);
				match(REFERENCES);
				setState(1183);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,103,_ctx) ) {
				case 1:
					{
					setState(1182);
					column_list();
					}
					break;
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1185);
				match(SELECT);
				setState(1187);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,104,_ctx) ) {
				case 1:
					{
					setState(1186);
					column_list();
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1189);
				match(TRIGGER);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1190);
				match(UPDATE);
				setState(1192);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,105,_ctx) ) {
				case 1:
					{
					setState(1191);
					column_list();
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Column_listContext extends ParserRuleContext {
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public List<Column_identifierContext> column_identifier() {
			return getRuleContexts(Column_identifierContext.class);
		}
		public Column_identifierContext column_identifier(int i) {
			return getRuleContext(Column_identifierContext.class,i);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Column_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterColumn_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitColumn_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitColumn_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_listContext column_list() throws RecognitionException {
		Column_listContext _localctx = new Column_listContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_column_list);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1196);
			match(LR_BRACKET);
			setState(1197);
			column_identifier();
			setState(1202);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,107,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1198);
					match(COMMA);
					setState(1199);
					column_identifier();
					}
					} 
				}
				setState(1204);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,107,_ctx);
			}
			setState(1205);
			match(RR_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Column_identifierContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Column_identifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterColumn_identifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitColumn_identifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitColumn_identifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_identifierContext column_identifier() throws RecognitionException {
		Column_identifierContext _localctx = new Column_identifierContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_column_identifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1207);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RevokeesContext extends ParserRuleContext {
		public List<RevokeeContext> revokee() {
			return getRuleContexts(RevokeeContext.class);
		}
		public RevokeeContext revokee(int i) {
			return getRuleContext(RevokeeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public RevokeesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_revokees; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterRevokees(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitRevokees(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitRevokees(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RevokeesContext revokees() throws RecognitionException {
		RevokeesContext _localctx = new RevokeesContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_revokees);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1209);
			revokee();
			setState(1214);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,108,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1210);
					match(COMMA);
					setState(1211);
					revokee();
					}
					} 
				}
				setState(1216);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,108,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RevokeeContext extends ParserRuleContext {
		public Authorization_identifierContext authorization_identifier() {
			return getRuleContext(Authorization_identifierContext.class,0);
		}
		public Role_nameContext role_name() {
			return getRuleContext(Role_nameContext.class,0);
		}
		public TerminalNode PUBLIC() { return getToken(DerbyParser.PUBLIC, 0); }
		public RevokeeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_revokee; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterRevokee(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitRevokee(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitRevokee(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RevokeeContext revokee() throws RecognitionException {
		RevokeeContext _localctx = new RevokeeContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_revokee);
		try {
			setState(1220);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,109,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1217);
				authorization_identifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1218);
				role_name();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1219);
				match(PUBLIC);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Authorization_identifierContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Authorization_identifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_authorization_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterAuthorization_identifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitAuthorization_identifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitAuthorization_identifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Authorization_identifierContext authorization_identifier() throws RecognitionException {
		Authorization_identifierContext _localctx = new Authorization_identifierContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_authorization_identifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1222);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Alter_commandContext extends ParserRuleContext {
		public Alter_tableContext alter_table() {
			return getRuleContext(Alter_tableContext.class,0);
		}
		public Alter_commandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alter_command; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterAlter_command(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitAlter_command(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitAlter_command(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Alter_commandContext alter_command() throws RecognitionException {
		Alter_commandContext _localctx = new Alter_commandContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_alter_command);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1224);
			alter_table();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Alter_tableContext extends ParserRuleContext {
		public TerminalNode ALTER() { return getToken(DerbyParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(DerbyParser.TABLE, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public TerminalNode ADD() { return getToken(DerbyParser.ADD, 0); }
		public TerminalNode COLUMN() { return getToken(DerbyParser.COLUMN, 0); }
		public Column_definitionContext column_definition() {
			return getRuleContext(Column_definitionContext.class,0);
		}
		public Constraint_clauseContext constraint_clause() {
			return getRuleContext(Constraint_clauseContext.class,0);
		}
		public TerminalNode DROP() { return getToken(DerbyParser.DROP, 0); }
		public Column_nameContext column_name() {
			return getRuleContext(Column_nameContext.class,0);
		}
		public Cascade_restrictContext cascade_restrict() {
			return getRuleContext(Cascade_restrictContext.class,0);
		}
		public TerminalNode PRIMARY() { return getToken(DerbyParser.PRIMARY, 0); }
		public TerminalNode KEY() { return getToken(DerbyParser.KEY, 0); }
		public TerminalNode FOREIGN() { return getToken(DerbyParser.FOREIGN, 0); }
		public Constraint_nameContext constraint_name() {
			return getRuleContext(Constraint_nameContext.class,0);
		}
		public TerminalNode UNIQUE() { return getToken(DerbyParser.UNIQUE, 0); }
		public TerminalNode CHECK() { return getToken(DerbyParser.CHECK, 0); }
		public TerminalNode CONSTRAINT() { return getToken(DerbyParser.CONSTRAINT, 0); }
		public Column_alterationContext column_alteration() {
			return getRuleContext(Column_alterationContext.class,0);
		}
		public TerminalNode LOCKSIZE() { return getToken(DerbyParser.LOCKSIZE, 0); }
		public Row_tableContext row_table() {
			return getRuleContext(Row_tableContext.class,0);
		}
		public Alter_tableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alter_table; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterAlter_table(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitAlter_table(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitAlter_table(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Alter_tableContext alter_table() throws RecognitionException {
		Alter_tableContext _localctx = new Alter_tableContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_alter_table);
		try {
			setState(1264);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,114,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1226);
				match(ALTER);
				setState(1227);
				match(TABLE);
				setState(1228);
				table_name();
				setState(1229);
				match(ADD);
				setState(1230);
				match(COLUMN);
				setState(1231);
				column_definition();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1233);
				match(ADD);
				setState(1234);
				constraint_clause();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1235);
				match(DROP);
				setState(1237);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,110,_ctx) ) {
				case 1:
					{
					setState(1236);
					match(COLUMN);
					}
					break;
				}
				setState(1239);
				column_name();
				setState(1241);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,111,_ctx) ) {
				case 1:
					{
					setState(1240);
					cascade_restrict();
					}
					break;
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1243);
				match(DROP);
				setState(1255);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,112,_ctx) ) {
				case 1:
					{
					setState(1244);
					match(PRIMARY);
					setState(1245);
					match(KEY);
					}
					break;
				case 2:
					{
					setState(1246);
					match(FOREIGN);
					setState(1247);
					match(KEY);
					setState(1248);
					constraint_name();
					}
					break;
				case 3:
					{
					setState(1249);
					match(UNIQUE);
					setState(1250);
					constraint_name();
					}
					break;
				case 4:
					{
					setState(1251);
					match(CHECK);
					setState(1252);
					constraint_name();
					}
					break;
				case 5:
					{
					setState(1253);
					match(CONSTRAINT);
					setState(1254);
					constraint_name();
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1257);
				match(ALTER);
				setState(1259);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,113,_ctx) ) {
				case 1:
					{
					setState(1258);
					match(COLUMN);
					}
					break;
				}
				setState(1261);
				column_alteration();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1262);
				match(LOCKSIZE);
				setState(1263);
				row_table();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Constraint_clauseContext extends ParserRuleContext {
		public Column_level_constraintContext column_level_constraint() {
			return getRuleContext(Column_level_constraintContext.class,0);
		}
		public Table_level_constraintContext table_level_constraint() {
			return getRuleContext(Table_level_constraintContext.class,0);
		}
		public Constraint_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraint_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterConstraint_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitConstraint_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitConstraint_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Constraint_clauseContext constraint_clause() throws RecognitionException {
		Constraint_clauseContext _localctx = new Constraint_clauseContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_constraint_clause);
		try {
			setState(1268);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,115,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1266);
				column_level_constraint();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1267);
				table_level_constraint();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Cascade_restrictContext extends ParserRuleContext {
		public TerminalNode CASCADE() { return getToken(DerbyParser.CASCADE, 0); }
		public TerminalNode RESTRICT() { return getToken(DerbyParser.RESTRICT, 0); }
		public Cascade_restrictContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cascade_restrict; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCascade_restrict(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCascade_restrict(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCascade_restrict(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Cascade_restrictContext cascade_restrict() throws RecognitionException {
		Cascade_restrictContext _localctx = new Cascade_restrictContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_cascade_restrict);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1270);
			_la = _input.LA(1);
			if ( !(_la==CASCADE || _la==RESTRICT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Row_tableContext extends ParserRuleContext {
		public TerminalNode ROW() { return getToken(DerbyParser.ROW, 0); }
		public TerminalNode TABLE() { return getToken(DerbyParser.TABLE, 0); }
		public Row_tableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_row_table; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterRow_table(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitRow_table(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitRow_table(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Row_tableContext row_table() throws RecognitionException {
		Row_tableContext _localctx = new Row_tableContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_row_table);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1272);
			_la = _input.LA(1);
			if ( !(_la==ROW || _la==TABLE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Column_alterationContext extends ParserRuleContext {
		public Column_nameContext column_name() {
			return getRuleContext(Column_nameContext.class,0);
		}
		public TerminalNode SET() { return getToken(DerbyParser.SET, 0); }
		public List<TerminalNode> DATA() { return getTokens(DerbyParser.DATA); }
		public TerminalNode DATA(int i) {
			return getToken(DerbyParser.DATA, i);
		}
		public TerminalNode TYPE() { return getToken(DerbyParser.TYPE, 0); }
		public TerminalNode BLOB() { return getToken(DerbyParser.BLOB, 0); }
		public IntegerContext integer() {
			return getRuleContext(IntegerContext.class,0);
		}
		public TerminalNode CLOB() { return getToken(DerbyParser.CLOB, 0); }
		public TerminalNode VARCHAR() { return getToken(DerbyParser.VARCHAR, 0); }
		public TerminalNode FOR() { return getToken(DerbyParser.FOR, 0); }
		public TerminalNode BIT() { return getToken(DerbyParser.BIT, 0); }
		public TerminalNode INCREMENT() { return getToken(DerbyParser.INCREMENT, 0); }
		public TerminalNode BY() { return getToken(DerbyParser.BY, 0); }
		public Integer_constantContext integer_constant() {
			return getRuleContext(Integer_constantContext.class,0);
		}
		public TerminalNode RESTART() { return getToken(DerbyParser.RESTART, 0); }
		public TerminalNode WITH() { return getToken(DerbyParser.WITH, 0); }
		public TerminalNode GENERATED() { return getToken(DerbyParser.GENERATED, 0); }
		public Always_by_defaultContext always_by_default() {
			return getRuleContext(Always_by_defaultContext.class,0);
		}
		public Set_dropContext set_drop() {
			return getRuleContext(Set_dropContext.class,0);
		}
		public TerminalNode NOT() { return getToken(DerbyParser.NOT, 0); }
		public TerminalNode NULL_() { return getToken(DerbyParser.NULL_, 0); }
		public TerminalNode DEFAULT() { return getToken(DerbyParser.DEFAULT, 0); }
		public DefaultValueContext defaultValue() {
			return getRuleContext(DefaultValueContext.class,0);
		}
		public With_setContext with_set() {
			return getRuleContext(With_setContext.class,0);
		}
		public TerminalNode CYCLE() { return getToken(DerbyParser.CYCLE, 0); }
		public TerminalNode NO() { return getToken(DerbyParser.NO, 0); }
		public TerminalNode DROP() { return getToken(DerbyParser.DROP, 0); }
		public Column_alterationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_alteration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterColumn_alteration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitColumn_alteration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitColumn_alteration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_alterationContext column_alteration() throws RecognitionException {
		Column_alterationContext _localctx = new Column_alterationContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_column_alteration);
		try {
			setState(1350);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,119,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1274);
				column_name();
				setState(1275);
				match(SET);
				setState(1276);
				match(DATA);
				setState(1277);
				match(TYPE);
				setState(1278);
				match(BLOB);
				{
				setState(1279);
				integer();
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1281);
				column_name();
				setState(1282);
				match(SET);
				setState(1283);
				match(DATA);
				setState(1284);
				match(TYPE);
				setState(1285);
				match(CLOB);
				{
				setState(1286);
				integer();
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1288);
				column_name();
				setState(1289);
				match(SET);
				setState(1290);
				match(DATA);
				setState(1291);
				match(TYPE);
				setState(1292);
				match(VARCHAR);
				{
				setState(1293);
				integer();
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1295);
				column_name();
				setState(1296);
				match(SET);
				setState(1297);
				match(DATA);
				setState(1298);
				match(TYPE);
				setState(1299);
				match(VARCHAR);
				{
				setState(1300);
				integer();
				}
				setState(1301);
				match(FOR);
				setState(1302);
				match(BIT);
				setState(1303);
				match(DATA);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1305);
				column_name();
				setState(1306);
				match(SET);
				setState(1307);
				match(INCREMENT);
				setState(1308);
				match(BY);
				setState(1309);
				integer_constant();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1311);
				column_name();
				setState(1312);
				match(RESTART);
				setState(1313);
				match(WITH);
				setState(1314);
				integer_constant();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1316);
				column_name();
				setState(1317);
				match(SET);
				setState(1318);
				match(GENERATED);
				setState(1319);
				always_by_default();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1321);
				column_name();
				setState(1322);
				set_drop();
				setState(1323);
				match(NOT);
				setState(1324);
				match(NULL_);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1326);
				column_name();
				setState(1328);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,116,_ctx) ) {
				case 1:
					{
					setState(1327);
					match(NOT);
					}
					break;
				}
				setState(1330);
				match(NULL_);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1332);
				column_name();
				setState(1334);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,117,_ctx) ) {
				case 1:
					{
					setState(1333);
					with_set();
					}
					break;
				}
				setState(1336);
				match(DEFAULT);
				setState(1337);
				defaultValue();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(1339);
				column_name();
				setState(1340);
				match(SET);
				setState(1342);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,118,_ctx) ) {
				case 1:
					{
					setState(1341);
					match(NO);
					}
					break;
				}
				setState(1344);
				match(CYCLE);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(1346);
				column_name();
				setState(1347);
				match(DROP);
				setState(1348);
				match(DEFAULT);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IntegerContext extends ParserRuleContext {
		public NumContext num() {
			return getRuleContext(NumContext.class,0);
		}
		public IntegerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterInteger(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitInteger(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitInteger(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntegerContext integer() throws RecognitionException {
		IntegerContext _localctx = new IntegerContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_integer);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1352);
			num();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Integer_constantContext extends ParserRuleContext {
		public NumContext num() {
			return getRuleContext(NumContext.class,0);
		}
		public Integer_constantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer_constant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterInteger_constant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitInteger_constant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitInteger_constant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Integer_constantContext integer_constant() throws RecognitionException {
		Integer_constantContext _localctx = new Integer_constantContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_integer_constant);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1354);
			num();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DefaultValueContext extends ParserRuleContext {
		public Constant_expression_nullContext constant_expression_null() {
			return getRuleContext(Constant_expression_nullContext.class,0);
		}
		public DefaultValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defaultValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDefaultValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDefaultValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDefaultValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefaultValueContext defaultValue() throws RecognitionException {
		DefaultValueContext _localctx = new DefaultValueContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_defaultValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1356);
			constant_expression_null();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Constant_expression_nullContext extends ParserRuleContext {
		public NumContext num() {
			return getRuleContext(NumContext.class,0);
		}
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public TerminalNode NULL_() { return getToken(DerbyParser.NULL_, 0); }
		public Constant_expression_nullContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant_expression_null; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterConstant_expression_null(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitConstant_expression_null(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitConstant_expression_null(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Constant_expression_nullContext constant_expression_null() throws RecognitionException {
		Constant_expression_nullContext _localctx = new Constant_expression_nullContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_constant_expression_null);
		try {
			setState(1361);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,120,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1358);
				num();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1359);
				string();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1360);
				match(NULL_);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Always_by_defaultContext extends ParserRuleContext {
		public TerminalNode ALWAYS() { return getToken(DerbyParser.ALWAYS, 0); }
		public TerminalNode BY() { return getToken(DerbyParser.BY, 0); }
		public TerminalNode DEFAULT() { return getToken(DerbyParser.DEFAULT, 0); }
		public Always_by_defaultContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_always_by_default; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterAlways_by_default(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitAlways_by_default(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitAlways_by_default(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Always_by_defaultContext always_by_default() throws RecognitionException {
		Always_by_defaultContext _localctx = new Always_by_defaultContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_always_by_default);
		try {
			setState(1366);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,121,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1363);
				match(ALWAYS);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1364);
				match(BY);
				setState(1365);
				match(DEFAULT);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Set_dropContext extends ParserRuleContext {
		public TerminalNode SET() { return getToken(DerbyParser.SET, 0); }
		public TerminalNode DROP() { return getToken(DerbyParser.DROP, 0); }
		public Set_dropContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_set_drop; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSet_drop(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSet_drop(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSet_drop(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Set_dropContext set_drop() throws RecognitionException {
		Set_dropContext _localctx = new Set_dropContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_set_drop);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1368);
			_la = _input.LA(1);
			if ( !(_la==DROP || _la==SET) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class With_setContext extends ParserRuleContext {
		public TerminalNode WITH() { return getToken(DerbyParser.WITH, 0); }
		public TerminalNode SET() { return getToken(DerbyParser.SET, 0); }
		public With_setContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_with_set; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterWith_set(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitWith_set(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitWith_set(this);
			else return visitor.visitChildren(this);
		}
	}

	public final With_setContext with_set() throws RecognitionException {
		With_setContext _localctx = new With_setContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_with_set);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1370);
			_la = _input.LA(1);
			if ( !(_la==SET || _la==WITH) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Create_commandContext extends ParserRuleContext {
		public Create_derby_aggregateContext create_derby_aggregate() {
			return getRuleContext(Create_derby_aggregateContext.class,0);
		}
		public Create_functionContext create_function() {
			return getRuleContext(Create_functionContext.class,0);
		}
		public Create_indexContext create_index() {
			return getRuleContext(Create_indexContext.class,0);
		}
		public Create_procedureContext create_procedure() {
			return getRuleContext(Create_procedureContext.class,0);
		}
		public Create_roleContext create_role() {
			return getRuleContext(Create_roleContext.class,0);
		}
		public Create_schemaContext create_schema() {
			return getRuleContext(Create_schemaContext.class,0);
		}
		public Create_sequenceContext create_sequence() {
			return getRuleContext(Create_sequenceContext.class,0);
		}
		public Create_synonymContext create_synonym() {
			return getRuleContext(Create_synonymContext.class,0);
		}
		public Create_tableContext create_table() {
			return getRuleContext(Create_tableContext.class,0);
		}
		public Create_triggerContext create_trigger() {
			return getRuleContext(Create_triggerContext.class,0);
		}
		public Create_typeContext create_type() {
			return getRuleContext(Create_typeContext.class,0);
		}
		public Create_viewContext create_view() {
			return getRuleContext(Create_viewContext.class,0);
		}
		public Create_commandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_command; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCreate_command(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCreate_command(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCreate_command(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_commandContext create_command() throws RecognitionException {
		Create_commandContext _localctx = new Create_commandContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_create_command);
		try {
			setState(1384);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,122,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1372);
				create_derby_aggregate();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1373);
				create_function();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1374);
				create_index();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1375);
				create_procedure();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1376);
				create_role();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1377);
				create_schema();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1378);
				create_sequence();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1379);
				create_synonym();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1380);
				create_table();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1381);
				create_trigger();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(1382);
				create_type();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(1383);
				create_view();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Create_derby_aggregateContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DerbyParser.CREATE, 0); }
		public TerminalNode DERBY() { return getToken(DerbyParser.DERBY, 0); }
		public TerminalNode AGGREGATE() { return getToken(DerbyParser.AGGREGATE, 0); }
		public Aggregate_nameContext aggregate_name() {
			return getRuleContext(Aggregate_nameContext.class,0);
		}
		public TerminalNode FOR() { return getToken(DerbyParser.FOR, 0); }
		public List<Value_data_typeContext> value_data_type() {
			return getRuleContexts(Value_data_typeContext.class);
		}
		public Value_data_typeContext value_data_type(int i) {
			return getRuleContext(Value_data_typeContext.class,i);
		}
		public TerminalNode EXTERNAL() { return getToken(DerbyParser.EXTERNAL, 0); }
		public TerminalNode NAME() { return getToken(DerbyParser.NAME, 0); }
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public TerminalNode RETURNS() { return getToken(DerbyParser.RETURNS, 0); }
		public Create_derby_aggregateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_derby_aggregate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCreate_derby_aggregate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCreate_derby_aggregate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCreate_derby_aggregate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_derby_aggregateContext create_derby_aggregate() throws RecognitionException {
		Create_derby_aggregateContext _localctx = new Create_derby_aggregateContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_create_derby_aggregate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1386);
			match(CREATE);
			setState(1387);
			match(DERBY);
			setState(1388);
			match(AGGREGATE);
			setState(1389);
			aggregate_name();
			setState(1390);
			match(FOR);
			setState(1391);
			value_data_type();
			{
			setState(1392);
			match(RETURNS);
			setState(1393);
			value_data_type();
			}
			setState(1395);
			match(EXTERNAL);
			setState(1396);
			match(NAME);
			setState(1397);
			string();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Value_data_typeContext extends ParserRuleContext {
		public Data_typeContext data_type() {
			return getRuleContext(Data_typeContext.class,0);
		}
		public Value_data_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value_data_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterValue_data_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitValue_data_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitValue_data_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Value_data_typeContext value_data_type() throws RecognitionException {
		Value_data_typeContext _localctx = new Value_data_typeContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_value_data_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1399);
			data_type();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Create_functionContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DerbyParser.CREATE, 0); }
		public TerminalNode FUNCTION() { return getToken(DerbyParser.FUNCTION, 0); }
		public Function_nameContext function_name() {
			return getRuleContext(Function_nameContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public List<Function_parameterContext> function_parameter() {
			return getRuleContexts(Function_parameterContext.class);
		}
		public Function_parameterContext function_parameter(int i) {
			return getRuleContext(Function_parameterContext.class,i);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public TerminalNode RETURNS() { return getToken(DerbyParser.RETURNS, 0); }
		public Return_data_typeContext return_data_type() {
			return getRuleContext(Return_data_typeContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public List<Function_elementContext> function_element() {
			return getRuleContexts(Function_elementContext.class);
		}
		public Function_elementContext function_element(int i) {
			return getRuleContext(Function_elementContext.class,i);
		}
		public Create_functionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_function; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCreate_function(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCreate_function(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCreate_function(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_functionContext create_function() throws RecognitionException {
		Create_functionContext _localctx = new Create_functionContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_create_function);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1401);
			match(CREATE);
			setState(1402);
			match(FUNCTION);
			setState(1403);
			function_name();
			setState(1404);
			match(LR_BRACKET);
			setState(1405);
			function_parameter();
			setState(1410);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,123,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1406);
					match(COMMA);
					setState(1407);
					function_parameter();
					}
					} 
				}
				setState(1412);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,123,_ctx);
			}
			setState(1413);
			match(RR_BRACKET);
			setState(1414);
			match(RETURNS);
			setState(1415);
			return_data_type();
			setState(1419);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,124,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1416);
					function_element();
					}
					} 
				}
				setState(1421);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,124,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Function_parameterContext extends ParserRuleContext {
		public Data_typeContext data_type() {
			return getRuleContext(Data_typeContext.class,0);
		}
		public Parameter_nameContext parameter_name() {
			return getRuleContext(Parameter_nameContext.class,0);
		}
		public Function_parameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function_parameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterFunction_parameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitFunction_parameter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitFunction_parameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Function_parameterContext function_parameter() throws RecognitionException {
		Function_parameterContext _localctx = new Function_parameterContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_function_parameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1423);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,125,_ctx) ) {
			case 1:
				{
				setState(1422);
				parameter_name();
				}
				break;
			}
			setState(1425);
			data_type();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Return_data_typeContext extends ParserRuleContext {
		public Table_typeContext table_type() {
			return getRuleContext(Table_typeContext.class,0);
		}
		public Data_typeContext data_type() {
			return getRuleContext(Data_typeContext.class,0);
		}
		public Return_data_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_return_data_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterReturn_data_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitReturn_data_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitReturn_data_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Return_data_typeContext return_data_type() throws RecognitionException {
		Return_data_typeContext _localctx = new Return_data_typeContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_return_data_type);
		try {
			setState(1429);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,126,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1427);
				table_type();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1428);
				data_type();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Table_typeContext extends ParserRuleContext {
		public TerminalNode TABLE() { return getToken(DerbyParser.TABLE, 0); }
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public List<Column_elementContext> column_element() {
			return getRuleContexts(Column_elementContext.class);
		}
		public Column_elementContext column_element(int i) {
			return getRuleContext(Column_elementContext.class,i);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public TerminalNode COMMA() { return getToken(DerbyParser.COMMA, 0); }
		public Table_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterTable_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitTable_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitTable_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Table_typeContext table_type() throws RecognitionException {
		Table_typeContext _localctx = new Table_typeContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_table_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1431);
			match(TABLE);
			setState(1432);
			match(LR_BRACKET);
			setState(1433);
			column_element();
			{
			setState(1434);
			match(COMMA);
			setState(1435);
			column_element();
			}
			setState(1437);
			match(RR_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Column_elementContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Data_typeContext data_type() {
			return getRuleContext(Data_typeContext.class,0);
		}
		public Column_elementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_element; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterColumn_element(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitColumn_element(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitColumn_element(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_elementContext column_element() throws RecognitionException {
		Column_elementContext _localctx = new Column_elementContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_column_element);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1439);
			id_();
			setState(1440);
			data_type();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Function_elementContext extends ParserRuleContext {
		public TerminalNode LANGUAGE() { return getToken(DerbyParser.LANGUAGE, 0); }
		public TerminalNode JAVA() { return getToken(DerbyParser.JAVA, 0); }
		public TerminalNode DETERMINISTIC() { return getToken(DerbyParser.DETERMINISTIC, 0); }
		public TerminalNode NOT() { return getToken(DerbyParser.NOT, 0); }
		public TerminalNode EXTERNAL() { return getToken(DerbyParser.EXTERNAL, 0); }
		public TerminalNode NAME() { return getToken(DerbyParser.NAME, 0); }
		public Single_quoted_stringContext single_quoted_string() {
			return getRuleContext(Single_quoted_stringContext.class,0);
		}
		public TerminalNode PARAMETER() { return getToken(DerbyParser.PARAMETER, 0); }
		public TerminalNode STYLE() { return getToken(DerbyParser.STYLE, 0); }
		public TerminalNode DERBY_JDBC_RESULT_SET() { return getToken(DerbyParser.DERBY_JDBC_RESULT_SET, 0); }
		public TerminalNode DERBY() { return getToken(DerbyParser.DERBY, 0); }
		public TerminalNode SECURITY() { return getToken(DerbyParser.SECURITY, 0); }
		public TerminalNode DEFINER() { return getToken(DerbyParser.DEFINER, 0); }
		public TerminalNode INVOKER() { return getToken(DerbyParser.INVOKER, 0); }
		public TerminalNode NO() { return getToken(DerbyParser.NO, 0); }
		public TerminalNode SQL() { return getToken(DerbyParser.SQL, 0); }
		public TerminalNode CONTAINS() { return getToken(DerbyParser.CONTAINS, 0); }
		public TerminalNode READS() { return getToken(DerbyParser.READS, 0); }
		public TerminalNode DATA() { return getToken(DerbyParser.DATA, 0); }
		public TerminalNode RETURNS() { return getToken(DerbyParser.RETURNS, 0); }
		public List<TerminalNode> NULL_() { return getTokens(DerbyParser.NULL_); }
		public TerminalNode NULL_(int i) {
			return getToken(DerbyParser.NULL_, i);
		}
		public TerminalNode ON() { return getToken(DerbyParser.ON, 0); }
		public TerminalNode INPUT() { return getToken(DerbyParser.INPUT, 0); }
		public TerminalNode CALLED() { return getToken(DerbyParser.CALLED, 0); }
		public Function_elementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function_element; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterFunction_element(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitFunction_element(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitFunction_element(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Function_elementContext function_element() throws RecognitionException {
		Function_elementContext _localctx = new Function_elementContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_function_element);
		int _la;
		try {
			setState(1478);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,130,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1442);
				match(LANGUAGE);
				setState(1443);
				match(JAVA);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1447);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,127,_ctx) ) {
				case 1:
					{
					setState(1444);
					match(DETERMINISTIC);
					}
					break;
				case 2:
					{
					setState(1445);
					match(NOT);
					setState(1446);
					match(DETERMINISTIC);
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1449);
				match(EXTERNAL);
				setState(1450);
				match(NAME);
				setState(1451);
				single_quoted_string();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1452);
				match(PARAMETER);
				setState(1453);
				match(STYLE);
				setState(1454);
				_la = _input.LA(1);
				if ( !(_la==DERBY || _la==DERBY_JDBC_RESULT_SET || _la==JAVA) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1455);
				match(EXTERNAL);
				setState(1456);
				match(SECURITY);
				setState(1457);
				_la = _input.LA(1);
				if ( !(_la==DEFINER || _la==INVOKER) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1465);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,128,_ctx) ) {
				case 1:
					{
					setState(1458);
					match(NO);
					setState(1459);
					match(SQL);
					}
					break;
				case 2:
					{
					setState(1460);
					match(CONTAINS);
					setState(1461);
					match(SQL);
					}
					break;
				case 3:
					{
					setState(1462);
					match(READS);
					setState(1463);
					match(SQL);
					setState(1464);
					match(DATA);
					}
					break;
				}
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1476);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,129,_ctx) ) {
				case 1:
					{
					setState(1467);
					match(RETURNS);
					setState(1468);
					match(NULL_);
					setState(1469);
					match(ON);
					setState(1470);
					match(NULL_);
					setState(1471);
					match(INPUT);
					}
					break;
				case 2:
					{
					setState(1472);
					match(CALLED);
					setState(1473);
					match(ON);
					setState(1474);
					match(NULL_);
					setState(1475);
					match(INPUT);
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Create_indexContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DerbyParser.CREATE, 0); }
		public TerminalNode INDEX() { return getToken(DerbyParser.INDEX, 0); }
		public Index_nameContext index_name() {
			return getRuleContext(Index_nameContext.class,0);
		}
		public TerminalNode ON() { return getToken(DerbyParser.ON, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public List<Simple_column_nameContext> simple_column_name() {
			return getRuleContexts(Simple_column_nameContext.class);
		}
		public Simple_column_nameContext simple_column_name(int i) {
			return getRuleContext(Simple_column_nameContext.class,i);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public TerminalNode UNIQUE() { return getToken(DerbyParser.UNIQUE, 0); }
		public List<Asc_descContext> asc_desc() {
			return getRuleContexts(Asc_descContext.class);
		}
		public Asc_descContext asc_desc(int i) {
			return getRuleContext(Asc_descContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Create_indexContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_index; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCreate_index(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCreate_index(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCreate_index(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_indexContext create_index() throws RecognitionException {
		Create_indexContext _localctx = new Create_indexContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_create_index);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1480);
			match(CREATE);
			setState(1482);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,131,_ctx) ) {
			case 1:
				{
				setState(1481);
				match(UNIQUE);
				}
				break;
			}
			setState(1484);
			match(INDEX);
			setState(1485);
			index_name();
			setState(1486);
			match(ON);
			setState(1487);
			table_name();
			setState(1488);
			match(LR_BRACKET);
			setState(1489);
			simple_column_name();
			setState(1491);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,132,_ctx) ) {
			case 1:
				{
				setState(1490);
				asc_desc();
				}
				break;
			}
			setState(1500);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,134,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1493);
					match(COMMA);
					setState(1494);
					simple_column_name();
					setState(1496);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,133,_ctx) ) {
					case 1:
						{
						setState(1495);
						asc_desc();
						}
						break;
					}
					}
					} 
				}
				setState(1502);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,134,_ctx);
			}
			setState(1503);
			match(RR_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Create_procedureContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DerbyParser.CREATE, 0); }
		public TerminalNode PROCEDURE() { return getToken(DerbyParser.PROCEDURE, 0); }
		public Procedure_nameContext procedure_name() {
			return getRuleContext(Procedure_nameContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public List<Procedure_parameterContext> procedure_parameter() {
			return getRuleContexts(Procedure_parameterContext.class);
		}
		public Procedure_parameterContext procedure_parameter(int i) {
			return getRuleContext(Procedure_parameterContext.class,i);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public List<Procedure_elementContext> procedure_element() {
			return getRuleContexts(Procedure_elementContext.class);
		}
		public Procedure_elementContext procedure_element(int i) {
			return getRuleContext(Procedure_elementContext.class,i);
		}
		public Create_procedureContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_procedure; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCreate_procedure(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCreate_procedure(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCreate_procedure(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_procedureContext create_procedure() throws RecognitionException {
		Create_procedureContext _localctx = new Create_procedureContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_create_procedure);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1505);
			match(CREATE);
			setState(1506);
			match(PROCEDURE);
			setState(1507);
			procedure_name();
			setState(1508);
			match(LR_BRACKET);
			setState(1509);
			procedure_parameter();
			setState(1514);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,135,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1510);
					match(COMMA);
					setState(1511);
					procedure_parameter();
					}
					} 
				}
				setState(1516);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,135,_ctx);
			}
			setState(1517);
			match(RR_BRACKET);
			setState(1521);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,136,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1518);
					procedure_element();
					}
					} 
				}
				setState(1523);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,136,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Procedure_parameterContext extends ParserRuleContext {
		public Data_typeContext data_type() {
			return getRuleContext(Data_typeContext.class,0);
		}
		public Parameter_nameContext parameter_name() {
			return getRuleContext(Parameter_nameContext.class,0);
		}
		public TerminalNode IN() { return getToken(DerbyParser.IN, 0); }
		public TerminalNode OUT() { return getToken(DerbyParser.OUT, 0); }
		public TerminalNode INOUT() { return getToken(DerbyParser.INOUT, 0); }
		public Procedure_parameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_procedure_parameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterProcedure_parameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitProcedure_parameter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitProcedure_parameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Procedure_parameterContext procedure_parameter() throws RecognitionException {
		Procedure_parameterContext _localctx = new Procedure_parameterContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_procedure_parameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1525);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,137,_ctx) ) {
			case 1:
				{
				setState(1524);
				_la = _input.LA(1);
				if ( !(_la==IN || _la==INOUT || _la==OUT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			}
			setState(1528);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,138,_ctx) ) {
			case 1:
				{
				setState(1527);
				parameter_name();
				}
				break;
			}
			setState(1530);
			data_type();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Data_typeContext extends ParserRuleContext {
		public TerminalNode BIGINT() { return getToken(DerbyParser.BIGINT, 0); }
		public TerminalNode BLOB() { return getToken(DerbyParser.BLOB, 0); }
		public TerminalNode BINARY() { return getToken(DerbyParser.BINARY, 0); }
		public TerminalNode LARGE() { return getToken(DerbyParser.LARGE, 0); }
		public TerminalNode OBJECT() { return getToken(DerbyParser.OBJECT, 0); }
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public List<NumContext> num() {
			return getRuleContexts(NumContext.class);
		}
		public NumContext num(int i) {
			return getRuleContext(NumContext.class,i);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public TerminalNode KILO() { return getToken(DerbyParser.KILO, 0); }
		public TerminalNode MEGA() { return getToken(DerbyParser.MEGA, 0); }
		public TerminalNode GIGA() { return getToken(DerbyParser.GIGA, 0); }
		public TerminalNode BOOLEAN() { return getToken(DerbyParser.BOOLEAN, 0); }
		public TerminalNode CHAR() { return getToken(DerbyParser.CHAR, 0); }
		public TerminalNode CHARACTER() { return getToken(DerbyParser.CHARACTER, 0); }
		public TerminalNode CLOB() { return getToken(DerbyParser.CLOB, 0); }
		public TerminalNode DATE() { return getToken(DerbyParser.DATE, 0); }
		public TerminalNode DECIMAL() { return getToken(DerbyParser.DECIMAL, 0); }
		public TerminalNode DEC() { return getToken(DerbyParser.DEC, 0); }
		public TerminalNode NUMERIC() { return getToken(DerbyParser.NUMERIC, 0); }
		public TerminalNode COMMA() { return getToken(DerbyParser.COMMA, 0); }
		public TerminalNode DOUBLE() { return getToken(DerbyParser.DOUBLE, 0); }
		public TerminalNode PRECISION() { return getToken(DerbyParser.PRECISION, 0); }
		public TerminalNode FLOAT() { return getToken(DerbyParser.FLOAT, 0); }
		public TerminalNode INT() { return getToken(DerbyParser.INT, 0); }
		public TerminalNode INTEGER() { return getToken(DerbyParser.INTEGER, 0); }
		public TerminalNode LONG() { return getToken(DerbyParser.LONG, 0); }
		public TerminalNode VARCHAR() { return getToken(DerbyParser.VARCHAR, 0); }
		public TerminalNode FOR() { return getToken(DerbyParser.FOR, 0); }
		public TerminalNode BIT() { return getToken(DerbyParser.BIT, 0); }
		public TerminalNode DATA() { return getToken(DerbyParser.DATA, 0); }
		public TerminalNode REAL() { return getToken(DerbyParser.REAL, 0); }
		public TerminalNode SMALLINT() { return getToken(DerbyParser.SMALLINT, 0); }
		public TerminalNode TIME() { return getToken(DerbyParser.TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(DerbyParser.TIMESTAMP, 0); }
		public TerminalNode VARYING() { return getToken(DerbyParser.VARYING, 0); }
		public TerminalNode XML() { return getToken(DerbyParser.XML, 0); }
		public Data_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_data_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterData_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitData_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitData_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Data_typeContext data_type() throws RecognitionException {
		Data_typeContext _localctx = new Data_typeContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_data_type);
		int _la;
		try {
			setState(1619);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,151,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1532);
				match(BIGINT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1537);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,139,_ctx) ) {
				case 1:
					{
					setState(1533);
					match(BLOB);
					}
					break;
				case 2:
					{
					setState(1534);
					match(BINARY);
					setState(1535);
					match(LARGE);
					setState(1536);
					match(OBJECT);
					}
					break;
				}
				setState(1546);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,141,_ctx) ) {
				case 1:
					{
					setState(1539);
					match(LR_BRACKET);
					setState(1540);
					num();
					setState(1542);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,140,_ctx) ) {
					case 1:
						{
						setState(1541);
						_la = _input.LA(1);
						if ( !(((((_la - 125)) & ~0x3f) == 0 && ((1L << (_la - 125)) & 18014400656965633L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
						break;
					}
					setState(1544);
					match(RR_BRACKET);
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1548);
				match(BOOLEAN);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1549);
				_la = _input.LA(1);
				if ( !(_la==CHAR || _la==CHARACTER) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1554);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,142,_ctx) ) {
				case 1:
					{
					setState(1550);
					match(LR_BRACKET);
					setState(1551);
					num();
					setState(1552);
					match(RR_BRACKET);
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1560);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,143,_ctx) ) {
				case 1:
					{
					setState(1556);
					match(CLOB);
					}
					break;
				case 2:
					{
					setState(1557);
					match(CHARACTER);
					setState(1558);
					match(LARGE);
					setState(1559);
					match(OBJECT);
					}
					break;
				}
				setState(1569);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,145,_ctx) ) {
				case 1:
					{
					setState(1562);
					match(LR_BRACKET);
					setState(1563);
					num();
					setState(1565);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,144,_ctx) ) {
					case 1:
						{
						setState(1564);
						_la = _input.LA(1);
						if ( !(((((_la - 125)) & ~0x3f) == 0 && ((1L << (_la - 125)) & 18014400656965633L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
						break;
					}
					setState(1567);
					match(RR_BRACKET);
					}
					break;
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1571);
				match(DATE);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1572);
				_la = _input.LA(1);
				if ( !(_la==DEC || _la==DECIMAL || _la==NUMERIC) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1581);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,147,_ctx) ) {
				case 1:
					{
					setState(1573);
					match(LR_BRACKET);
					setState(1574);
					num();
					setState(1577);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,146,_ctx) ) {
					case 1:
						{
						setState(1575);
						match(COMMA);
						setState(1576);
						num();
						}
						break;
					}
					setState(1579);
					match(RR_BRACKET);
					}
					break;
				}
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1583);
				match(DOUBLE);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1584);
				match(DOUBLE);
				setState(1585);
				match(PRECISION);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1586);
				match(FLOAT);
				setState(1591);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,148,_ctx) ) {
				case 1:
					{
					setState(1587);
					match(LR_BRACKET);
					setState(1588);
					num();
					setState(1589);
					match(RR_BRACKET);
					}
					break;
				}
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(1593);
				_la = _input.LA(1);
				if ( !(_la==INT || _la==INTEGER) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(1594);
				match(LONG);
				setState(1595);
				match(VARCHAR);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(1596);
				match(LONG);
				setState(1597);
				match(VARCHAR);
				setState(1598);
				match(FOR);
				setState(1599);
				match(BIT);
				setState(1600);
				match(DATA);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(1601);
				match(REAL);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(1602);
				match(SMALLINT);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(1603);
				match(TIME);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(1604);
				match(TIMESTAMP);
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(1610);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,149,_ctx) ) {
				case 1:
					{
					setState(1605);
					match(VARCHAR);
					}
					break;
				case 2:
					{
					setState(1606);
					match(CHAR);
					setState(1607);
					match(VARYING);
					}
					break;
				case 3:
					{
					setState(1608);
					match(CHARACTER);
					setState(1609);
					match(VARYING);
					}
					break;
				}
				setState(1616);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,150,_ctx) ) {
				case 1:
					{
					setState(1612);
					match(LR_BRACKET);
					setState(1613);
					num();
					setState(1614);
					match(RR_BRACKET);
					}
					break;
				}
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(1618);
				match(XML);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Procedure_elementContext extends ParserRuleContext {
		public TerminalNode RESULT() { return getToken(DerbyParser.RESULT, 0); }
		public TerminalNode SETS() { return getToken(DerbyParser.SETS, 0); }
		public IntegerContext integer() {
			return getRuleContext(IntegerContext.class,0);
		}
		public TerminalNode DYNAMIC() { return getToken(DerbyParser.DYNAMIC, 0); }
		public TerminalNode LANGUAGE() { return getToken(DerbyParser.LANGUAGE, 0); }
		public TerminalNode JAVA() { return getToken(DerbyParser.JAVA, 0); }
		public TerminalNode DETERMINISTIC() { return getToken(DerbyParser.DETERMINISTIC, 0); }
		public TerminalNode NOT() { return getToken(DerbyParser.NOT, 0); }
		public TerminalNode EXTERNAL() { return getToken(DerbyParser.EXTERNAL, 0); }
		public TerminalNode NAME() { return getToken(DerbyParser.NAME, 0); }
		public Single_quoted_stringContext single_quoted_string() {
			return getRuleContext(Single_quoted_stringContext.class,0);
		}
		public TerminalNode PARAMETER() { return getToken(DerbyParser.PARAMETER, 0); }
		public TerminalNode STYLE() { return getToken(DerbyParser.STYLE, 0); }
		public TerminalNode DERBY() { return getToken(DerbyParser.DERBY, 0); }
		public TerminalNode SECURITY() { return getToken(DerbyParser.SECURITY, 0); }
		public TerminalNode DEFINER() { return getToken(DerbyParser.DEFINER, 0); }
		public TerminalNode INVOKER() { return getToken(DerbyParser.INVOKER, 0); }
		public TerminalNode NO() { return getToken(DerbyParser.NO, 0); }
		public TerminalNode SQL() { return getToken(DerbyParser.SQL, 0); }
		public TerminalNode MODIFIES() { return getToken(DerbyParser.MODIFIES, 0); }
		public TerminalNode DATA() { return getToken(DerbyParser.DATA, 0); }
		public TerminalNode CONTAINS() { return getToken(DerbyParser.CONTAINS, 0); }
		public TerminalNode READS() { return getToken(DerbyParser.READS, 0); }
		public Procedure_elementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_procedure_element; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterProcedure_element(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitProcedure_element(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitProcedure_element(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Procedure_elementContext procedure_element() throws RecognitionException {
		Procedure_elementContext _localctx = new Procedure_elementContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_procedure_element);
		int _la;
		try {
			setState(1655);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,155,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1622);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,152,_ctx) ) {
				case 1:
					{
					setState(1621);
					match(DYNAMIC);
					}
					break;
				}
				setState(1624);
				match(RESULT);
				setState(1625);
				match(SETS);
				setState(1626);
				integer();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1627);
				match(LANGUAGE);
				setState(1628);
				match(JAVA);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1632);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,153,_ctx) ) {
				case 1:
					{
					setState(1629);
					match(DETERMINISTIC);
					}
					break;
				case 2:
					{
					setState(1630);
					match(NOT);
					setState(1631);
					match(DETERMINISTIC);
					}
					break;
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1634);
				match(EXTERNAL);
				setState(1635);
				match(NAME);
				setState(1636);
				single_quoted_string();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1637);
				match(PARAMETER);
				setState(1638);
				match(STYLE);
				setState(1639);
				_la = _input.LA(1);
				if ( !(_la==DERBY || _la==JAVA) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1640);
				match(EXTERNAL);
				setState(1641);
				match(SECURITY);
				setState(1642);
				_la = _input.LA(1);
				if ( !(_la==DEFINER || _la==INVOKER) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1653);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,154,_ctx) ) {
				case 1:
					{
					setState(1643);
					match(NO);
					setState(1644);
					match(SQL);
					}
					break;
				case 2:
					{
					setState(1645);
					match(MODIFIES);
					setState(1646);
					match(SQL);
					setState(1647);
					match(DATA);
					}
					break;
				case 3:
					{
					setState(1648);
					match(CONTAINS);
					setState(1649);
					match(SQL);
					}
					break;
				case 4:
					{
					setState(1650);
					match(READS);
					setState(1651);
					match(SQL);
					setState(1652);
					match(DATA);
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Create_roleContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DerbyParser.CREATE, 0); }
		public TerminalNode ROLE() { return getToken(DerbyParser.ROLE, 0); }
		public Role_nameContext role_name() {
			return getRuleContext(Role_nameContext.class,0);
		}
		public Create_roleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_role; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCreate_role(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCreate_role(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCreate_role(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_roleContext create_role() throws RecognitionException {
		Create_roleContext _localctx = new Create_roleContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_create_role);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1657);
			match(CREATE);
			setState(1658);
			match(ROLE);
			setState(1659);
			role_name();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Create_schemaContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DerbyParser.CREATE, 0); }
		public TerminalNode SCHEMA() { return getToken(DerbyParser.SCHEMA, 0); }
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode AUTHORIZATION() { return getToken(DerbyParser.AUTHORIZATION, 0); }
		public User_nameContext user_name() {
			return getRuleContext(User_nameContext.class,0);
		}
		public Create_schemaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_schema; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCreate_schema(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCreate_schema(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCreate_schema(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_schemaContext create_schema() throws RecognitionException {
		Create_schemaContext _localctx = new Create_schemaContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_create_schema);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1661);
			match(CREATE);
			setState(1662);
			match(SCHEMA);
			setState(1670);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,156,_ctx) ) {
			case 1:
				{
				setState(1663);
				schema_name();
				setState(1664);
				match(AUTHORIZATION);
				setState(1665);
				user_name();
				}
				break;
			case 2:
				{
				setState(1667);
				schema_name();
				}
				break;
			case 3:
				{
				setState(1668);
				match(AUTHORIZATION);
				setState(1669);
				user_name();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Create_sequenceContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DerbyParser.CREATE, 0); }
		public TerminalNode SEQUENCE() { return getToken(DerbyParser.SEQUENCE, 0); }
		public Sequence_nameContext sequence_name() {
			return getRuleContext(Sequence_nameContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public List<Sequence_elementContext> sequence_element() {
			return getRuleContexts(Sequence_elementContext.class);
		}
		public Sequence_elementContext sequence_element(int i) {
			return getRuleContext(Sequence_elementContext.class,i);
		}
		public Create_sequenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_sequence; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCreate_sequence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCreate_sequence(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCreate_sequence(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_sequenceContext create_sequence() throws RecognitionException {
		Create_sequenceContext _localctx = new Create_sequenceContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_create_sequence);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1672);
			match(CREATE);
			setState(1673);
			match(SEQUENCE);
			setState(1674);
			sequence_name();
			setState(1679);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,157,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1675);
					match(COMMA);
					setState(1676);
					sequence_element();
					}
					} 
				}
				setState(1681);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,157,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Sequence_elementContext extends ParserRuleContext {
		public TerminalNode AS() { return getToken(DerbyParser.AS, 0); }
		public Data_typeContext data_type() {
			return getRuleContext(Data_typeContext.class,0);
		}
		public TerminalNode START() { return getToken(DerbyParser.START, 0); }
		public TerminalNode WITH() { return getToken(DerbyParser.WITH, 0); }
		public SignedIntegerContext signedInteger() {
			return getRuleContext(SignedIntegerContext.class,0);
		}
		public TerminalNode INCREMENT() { return getToken(DerbyParser.INCREMENT, 0); }
		public TerminalNode BY() { return getToken(DerbyParser.BY, 0); }
		public TerminalNode MAXVALUE() { return getToken(DerbyParser.MAXVALUE, 0); }
		public TerminalNode NO() { return getToken(DerbyParser.NO, 0); }
		public TerminalNode MINVALUE() { return getToken(DerbyParser.MINVALUE, 0); }
		public TerminalNode CYCLE() { return getToken(DerbyParser.CYCLE, 0); }
		public Sequence_elementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sequence_element; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSequence_element(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSequence_element(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSequence_element(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Sequence_elementContext sequence_element() throws RecognitionException {
		Sequence_elementContext _localctx = new Sequence_elementContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_sequence_element);
		try {
			setState(1701);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,158,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1682);
				match(AS);
				setState(1683);
				data_type();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1684);
				match(START);
				setState(1685);
				match(WITH);
				setState(1686);
				signedInteger();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1687);
				match(INCREMENT);
				setState(1688);
				match(BY);
				setState(1689);
				signedInteger();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1690);
				match(MAXVALUE);
				setState(1691);
				signedInteger();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1692);
				match(NO);
				setState(1693);
				match(MAXVALUE);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1694);
				match(MINVALUE);
				setState(1695);
				signedInteger();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1696);
				match(NO);
				setState(1697);
				match(MINVALUE);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1698);
				match(CYCLE);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1699);
				match(NO);
				setState(1700);
				match(CYCLE);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SignedIntegerContext extends ParserRuleContext {
		public NumContext num() {
			return getRuleContext(NumContext.class,0);
		}
		public SignedIntegerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_signedInteger; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSignedInteger(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSignedInteger(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSignedInteger(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SignedIntegerContext signedInteger() throws RecognitionException {
		SignedIntegerContext _localctx = new SignedIntegerContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_signedInteger);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1703);
			num();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Create_synonymContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DerbyParser.CREATE, 0); }
		public TerminalNode SYNONYM() { return getToken(DerbyParser.SYNONYM, 0); }
		public Synonym_nameContext synonym_name() {
			return getRuleContext(Synonym_nameContext.class,0);
		}
		public TerminalNode FOR() { return getToken(DerbyParser.FOR, 0); }
		public View_nameContext view_name() {
			return getRuleContext(View_nameContext.class,0);
		}
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public Create_synonymContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_synonym; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCreate_synonym(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCreate_synonym(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCreate_synonym(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_synonymContext create_synonym() throws RecognitionException {
		Create_synonymContext _localctx = new Create_synonymContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_create_synonym);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1705);
			match(CREATE);
			setState(1706);
			match(SYNONYM);
			setState(1707);
			synonym_name();
			setState(1708);
			match(FOR);
			setState(1711);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,159,_ctx) ) {
			case 1:
				{
				setState(1709);
				view_name();
				}
				break;
			case 2:
				{
				setState(1710);
				table_name();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Create_tableContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DerbyParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(DerbyParser.TABLE, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public TerminalNode WITH() { return getToken(DerbyParser.WITH, 0); }
		public TerminalNode NO() { return getToken(DerbyParser.NO, 0); }
		public TerminalNode DATA() { return getToken(DerbyParser.DATA, 0); }
		public Create_tableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_table; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCreate_table(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCreate_table(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCreate_table(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_tableContext create_table() throws RecognitionException {
		Create_tableContext _localctx = new Create_tableContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_create_table);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1713);
			match(CREATE);
			setState(1714);
			match(TABLE);
			setState(1715);
			table_name();
			setState(1716);
			match(WITH);
			setState(1717);
			match(NO);
			setState(1718);
			match(DATA);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Create_triggerContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DerbyParser.CREATE, 0); }
		public TerminalNode TRIGGER() { return getToken(DerbyParser.TRIGGER, 0); }
		public Trigger_nameContext trigger_name() {
			return getRuleContext(Trigger_nameContext.class,0);
		}
		public Create_triggerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_trigger; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCreate_trigger(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCreate_trigger(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCreate_trigger(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_triggerContext create_trigger() throws RecognitionException {
		Create_triggerContext _localctx = new Create_triggerContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_create_trigger);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1720);
			match(CREATE);
			setState(1721);
			match(TRIGGER);
			setState(1722);
			trigger_name();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Create_typeContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DerbyParser.CREATE, 0); }
		public TerminalNode TYPE() { return getToken(DerbyParser.TYPE, 0); }
		public Type_nameContext type_name() {
			return getRuleContext(Type_nameContext.class,0);
		}
		public TerminalNode EXTERNAL() { return getToken(DerbyParser.EXTERNAL, 0); }
		public TerminalNode NAME() { return getToken(DerbyParser.NAME, 0); }
		public Single_quoted_stringContext single_quoted_string() {
			return getRuleContext(Single_quoted_stringContext.class,0);
		}
		public TerminalNode LANGUAGE() { return getToken(DerbyParser.LANGUAGE, 0); }
		public TerminalNode JAVA() { return getToken(DerbyParser.JAVA, 0); }
		public Create_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCreate_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCreate_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCreate_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_typeContext create_type() throws RecognitionException {
		Create_typeContext _localctx = new Create_typeContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_create_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1724);
			match(CREATE);
			setState(1725);
			match(TYPE);
			setState(1726);
			type_name();
			setState(1727);
			match(EXTERNAL);
			setState(1728);
			match(NAME);
			setState(1729);
			single_quoted_string();
			setState(1730);
			match(LANGUAGE);
			setState(1731);
			match(JAVA);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Single_quoted_stringContext extends ParserRuleContext {
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public Single_quoted_stringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_single_quoted_string; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSingle_quoted_string(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSingle_quoted_string(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSingle_quoted_string(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Single_quoted_stringContext single_quoted_string() throws RecognitionException {
		Single_quoted_stringContext _localctx = new Single_quoted_stringContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_single_quoted_string);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1733);
			string();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Create_viewContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(DerbyParser.CREATE, 0); }
		public TerminalNode VIEW() { return getToken(DerbyParser.VIEW, 0); }
		public View_nameContext view_name() {
			return getRuleContext(View_nameContext.class,0);
		}
		public TerminalNode AS() { return getToken(DerbyParser.AS, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public List<Simple_column_nameContext> simple_column_name() {
			return getRuleContexts(Simple_column_nameContext.class);
		}
		public Simple_column_nameContext simple_column_name(int i) {
			return getRuleContext(Simple_column_nameContext.class,i);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public Order_by_clauseContext order_by_clause() {
			return getRuleContext(Order_by_clauseContext.class,0);
		}
		public Offset_clauseContext offset_clause() {
			return getRuleContext(Offset_clauseContext.class,0);
		}
		public Fetch_clauseContext fetch_clause() {
			return getRuleContext(Fetch_clauseContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Create_viewContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_view; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCreate_view(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCreate_view(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCreate_view(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_viewContext create_view() throws RecognitionException {
		Create_viewContext _localctx = new Create_viewContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_create_view);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1735);
			match(CREATE);
			setState(1736);
			match(VIEW);
			setState(1737);
			view_name();
			setState(1749);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,161,_ctx) ) {
			case 1:
				{
				setState(1738);
				match(LR_BRACKET);
				setState(1739);
				simple_column_name();
				setState(1744);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,160,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1740);
						match(COMMA);
						setState(1741);
						simple_column_name();
						}
						} 
					}
					setState(1746);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,160,_ctx);
				}
				setState(1747);
				match(RR_BRACKET);
				}
				break;
			}
			setState(1751);
			match(AS);
			setState(1752);
			query(0);
			setState(1754);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,162,_ctx) ) {
			case 1:
				{
				setState(1753);
				order_by_clause();
				}
				break;
			}
			setState(1757);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,163,_ctx) ) {
			case 1:
				{
				setState(1756);
				offset_clause();
				}
				break;
			}
			setState(1760);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,164,_ctx) ) {
			case 1:
				{
				setState(1759);
				fetch_clause();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class QueryContext extends ParserRuleContext {
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public List<QueryContext> query() {
			return getRuleContexts(QueryContext.class);
		}
		public QueryContext query(int i) {
			return getRuleContext(QueryContext.class,i);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public Order_by_clauseContext order_by_clause() {
			return getRuleContext(Order_by_clauseContext.class,0);
		}
		public Offset_clauseContext offset_clause() {
			return getRuleContext(Offset_clauseContext.class,0);
		}
		public Fetch_clauseContext fetch_clause() {
			return getRuleContext(Fetch_clauseContext.class,0);
		}
		public Select_expressionContext select_expression() {
			return getRuleContext(Select_expressionContext.class,0);
		}
		public Values_expressionContext values_expression() {
			return getRuleContext(Values_expressionContext.class,0);
		}
		public TerminalNode INTERSECT() { return getToken(DerbyParser.INTERSECT, 0); }
		public All_distinctContext all_distinct() {
			return getRuleContext(All_distinctContext.class,0);
		}
		public TerminalNode EXCEPT() { return getToken(DerbyParser.EXCEPT, 0); }
		public TerminalNode UNION() { return getToken(DerbyParser.UNION, 0); }
		public QueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitQuery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		return query(0);
	}

	private QueryContext query(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		QueryContext _localctx = new QueryContext(_ctx, _parentState);
		QueryContext _prevctx = _localctx;
		int _startState = 184;
		enterRecursionRule(_localctx, 184, RULE_query, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1778);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,168,_ctx) ) {
			case 1:
				{
				setState(1763);
				match(LR_BRACKET);
				setState(1764);
				query(0);
				setState(1766);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,165,_ctx) ) {
				case 1:
					{
					setState(1765);
					order_by_clause();
					}
					break;
				}
				setState(1769);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,166,_ctx) ) {
				case 1:
					{
					setState(1768);
					offset_clause();
					}
					break;
				}
				setState(1772);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,167,_ctx) ) {
				case 1:
					{
					setState(1771);
					fetch_clause();
					}
					break;
				}
				setState(1774);
				match(RR_BRACKET);
				}
				break;
			case 2:
				{
				setState(1776);
				select_expression();
				}
				break;
			case 3:
				{
				setState(1777);
				values_expression();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(1800);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,173,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1798);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,172,_ctx) ) {
					case 1:
						{
						_localctx = new QueryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_query);
						setState(1780);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(1781);
						match(INTERSECT);
						setState(1783);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,169,_ctx) ) {
						case 1:
							{
							setState(1782);
							all_distinct();
							}
							break;
						}
						setState(1785);
						query(6);
						}
						break;
					case 2:
						{
						_localctx = new QueryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_query);
						setState(1786);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(1787);
						match(EXCEPT);
						setState(1789);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,170,_ctx) ) {
						case 1:
							{
							setState(1788);
							all_distinct();
							}
							break;
						}
						setState(1791);
						query(5);
						}
						break;
					case 3:
						{
						_localctx = new QueryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_query);
						setState(1792);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(1793);
						match(UNION);
						setState(1795);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,171,_ctx) ) {
						case 1:
							{
							setState(1794);
							all_distinct();
							}
							break;
						}
						setState(1797);
						query(4);
						}
						break;
					}
					} 
				}
				setState(1802);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,173,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Select_expressionContext extends ParserRuleContext {
		public TerminalNode SELECT() { return getToken(DerbyParser.SELECT, 0); }
		public List<Select_itemContext> select_item() {
			return getRuleContexts(Select_itemContext.class);
		}
		public Select_itemContext select_item(int i) {
			return getRuleContext(Select_itemContext.class,i);
		}
		public From_clauseContext from_clause() {
			return getRuleContext(From_clauseContext.class,0);
		}
		public All_distinctContext all_distinct() {
			return getRuleContext(All_distinctContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Where_clauseContext where_clause() {
			return getRuleContext(Where_clauseContext.class,0);
		}
		public Group_by_clauseContext group_by_clause() {
			return getRuleContext(Group_by_clauseContext.class,0);
		}
		public Having_clauseContext having_clause() {
			return getRuleContext(Having_clauseContext.class,0);
		}
		public Order_by_clauseContext order_by_clause() {
			return getRuleContext(Order_by_clauseContext.class,0);
		}
		public Offset_clauseContext offset_clause() {
			return getRuleContext(Offset_clauseContext.class,0);
		}
		public Fetch_clauseContext fetch_clause() {
			return getRuleContext(Fetch_clauseContext.class,0);
		}
		public Select_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSelect_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSelect_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSelect_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Select_expressionContext select_expression() throws RecognitionException {
		Select_expressionContext _localctx = new Select_expressionContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_select_expression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1803);
			match(SELECT);
			setState(1805);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,174,_ctx) ) {
			case 1:
				{
				setState(1804);
				all_distinct();
				}
				break;
			}
			setState(1807);
			select_item();
			setState(1812);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,175,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1808);
					match(COMMA);
					setState(1809);
					select_item();
					}
					} 
				}
				setState(1814);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,175,_ctx);
			}
			setState(1815);
			from_clause();
			setState(1817);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,176,_ctx) ) {
			case 1:
				{
				setState(1816);
				where_clause();
				}
				break;
			}
			setState(1820);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,177,_ctx) ) {
			case 1:
				{
				setState(1819);
				group_by_clause();
				}
				break;
			}
			setState(1823);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,178,_ctx) ) {
			case 1:
				{
				setState(1822);
				having_clause();
				}
				break;
			}
			setState(1826);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,179,_ctx) ) {
			case 1:
				{
				setState(1825);
				order_by_clause();
				}
				break;
			}
			setState(1829);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,180,_ctx) ) {
			case 1:
				{
				setState(1828);
				offset_clause();
				}
				break;
			}
			setState(1832);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,181,_ctx) ) {
			case 1:
				{
				setState(1831);
				fetch_clause();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Select_itemContext extends ParserRuleContext {
		public TerminalNode STAR() { return getToken(DerbyParser.STAR, 0); }
		public TerminalNode DOT() { return getToken(DerbyParser.DOT, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public Correlation_nameContext correlation_name() {
			return getRuleContext(Correlation_nameContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode AS() { return getToken(DerbyParser.AS, 0); }
		public Simple_column_nameContext simple_column_name() {
			return getRuleContext(Simple_column_nameContext.class,0);
		}
		public Select_itemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSelect_item(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSelect_item(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSelect_item(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Select_itemContext select_item() throws RecognitionException {
		Select_itemContext _localctx = new Select_itemContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_select_item);
		try {
			setState(1847);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,184,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1834);
				match(STAR);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1837);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,182,_ctx) ) {
				case 1:
					{
					setState(1835);
					table_name();
					}
					break;
				case 2:
					{
					setState(1836);
					correlation_name();
					}
					break;
				}
				setState(1839);
				match(DOT);
				setState(1840);
				match(STAR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1842);
				expression(0);
				setState(1845);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,183,_ctx) ) {
				case 1:
					{
					setState(1843);
					match(AS);
					setState(1844);
					simple_column_name();
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Simple_column_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Simple_column_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simple_column_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSimple_column_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSimple_column_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSimple_column_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Simple_column_nameContext simple_column_name() throws RecognitionException {
		Simple_column_nameContext _localctx = new Simple_column_nameContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_simple_column_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1849);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Drop_commandContext extends ParserRuleContext {
		public Drop_derby_aggregateContext drop_derby_aggregate() {
			return getRuleContext(Drop_derby_aggregateContext.class,0);
		}
		public Drop_functionContext drop_function() {
			return getRuleContext(Drop_functionContext.class,0);
		}
		public Drop_indexContext drop_index() {
			return getRuleContext(Drop_indexContext.class,0);
		}
		public Drop_procedureContext drop_procedure() {
			return getRuleContext(Drop_procedureContext.class,0);
		}
		public Drop_roleContext drop_role() {
			return getRuleContext(Drop_roleContext.class,0);
		}
		public Drop_schemaContext drop_schema() {
			return getRuleContext(Drop_schemaContext.class,0);
		}
		public Drop_sequenceContext drop_sequence() {
			return getRuleContext(Drop_sequenceContext.class,0);
		}
		public Drop_synonymContext drop_synonym() {
			return getRuleContext(Drop_synonymContext.class,0);
		}
		public Drop_tableContext drop_table() {
			return getRuleContext(Drop_tableContext.class,0);
		}
		public Drop_triggerContext drop_trigger() {
			return getRuleContext(Drop_triggerContext.class,0);
		}
		public Drop_typeContext drop_type() {
			return getRuleContext(Drop_typeContext.class,0);
		}
		public Drop_viewContext drop_view() {
			return getRuleContext(Drop_viewContext.class,0);
		}
		public Drop_commandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_command; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDrop_command(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDrop_command(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDrop_command(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Drop_commandContext drop_command() throws RecognitionException {
		Drop_commandContext _localctx = new Drop_commandContext(_ctx, getState());
		enterRule(_localctx, 192, RULE_drop_command);
		try {
			setState(1863);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,185,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1851);
				drop_derby_aggregate();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1852);
				drop_function();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1853);
				drop_index();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1854);
				drop_procedure();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1855);
				drop_role();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1856);
				drop_schema();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1857);
				drop_sequence();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(1858);
				drop_synonym();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(1859);
				drop_table();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(1860);
				drop_trigger();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(1861);
				drop_type();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(1862);
				drop_view();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Drop_derby_aggregateContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(DerbyParser.DROP, 0); }
		public TerminalNode DERBY() { return getToken(DerbyParser.DERBY, 0); }
		public TerminalNode AGGREGATE() { return getToken(DerbyParser.AGGREGATE, 0); }
		public Aggregate_nameContext aggregate_name() {
			return getRuleContext(Aggregate_nameContext.class,0);
		}
		public TerminalNode RESTRICT() { return getToken(DerbyParser.RESTRICT, 0); }
		public Drop_derby_aggregateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_derby_aggregate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDrop_derby_aggregate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDrop_derby_aggregate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDrop_derby_aggregate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Drop_derby_aggregateContext drop_derby_aggregate() throws RecognitionException {
		Drop_derby_aggregateContext _localctx = new Drop_derby_aggregateContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_drop_derby_aggregate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1865);
			match(DROP);
			setState(1866);
			match(DERBY);
			setState(1867);
			match(AGGREGATE);
			setState(1868);
			aggregate_name();
			setState(1869);
			match(RESTRICT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Drop_functionContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(DerbyParser.DROP, 0); }
		public TerminalNode FUNCTION() { return getToken(DerbyParser.FUNCTION, 0); }
		public Function_nameContext function_name() {
			return getRuleContext(Function_nameContext.class,0);
		}
		public Drop_functionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_function; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDrop_function(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDrop_function(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDrop_function(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Drop_functionContext drop_function() throws RecognitionException {
		Drop_functionContext _localctx = new Drop_functionContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_drop_function);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1871);
			match(DROP);
			setState(1872);
			match(FUNCTION);
			setState(1873);
			function_name();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Drop_indexContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(DerbyParser.DROP, 0); }
		public TerminalNode INDEX() { return getToken(DerbyParser.INDEX, 0); }
		public Index_nameContext index_name() {
			return getRuleContext(Index_nameContext.class,0);
		}
		public Drop_indexContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_index; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDrop_index(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDrop_index(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDrop_index(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Drop_indexContext drop_index() throws RecognitionException {
		Drop_indexContext _localctx = new Drop_indexContext(_ctx, getState());
		enterRule(_localctx, 198, RULE_drop_index);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1875);
			match(DROP);
			setState(1876);
			match(INDEX);
			setState(1877);
			index_name();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Drop_procedureContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(DerbyParser.DROP, 0); }
		public TerminalNode PROCEDURE() { return getToken(DerbyParser.PROCEDURE, 0); }
		public Procedure_nameContext procedure_name() {
			return getRuleContext(Procedure_nameContext.class,0);
		}
		public Drop_procedureContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_procedure; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDrop_procedure(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDrop_procedure(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDrop_procedure(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Drop_procedureContext drop_procedure() throws RecognitionException {
		Drop_procedureContext _localctx = new Drop_procedureContext(_ctx, getState());
		enterRule(_localctx, 200, RULE_drop_procedure);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1879);
			match(DROP);
			setState(1880);
			match(PROCEDURE);
			setState(1881);
			procedure_name();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Drop_roleContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(DerbyParser.DROP, 0); }
		public TerminalNode ROLE() { return getToken(DerbyParser.ROLE, 0); }
		public Role_nameContext role_name() {
			return getRuleContext(Role_nameContext.class,0);
		}
		public Drop_roleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_role; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDrop_role(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDrop_role(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDrop_role(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Drop_roleContext drop_role() throws RecognitionException {
		Drop_roleContext _localctx = new Drop_roleContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_drop_role);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1883);
			match(DROP);
			setState(1884);
			match(ROLE);
			setState(1885);
			role_name();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Drop_schemaContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(DerbyParser.DROP, 0); }
		public TerminalNode SCHEMA() { return getToken(DerbyParser.SCHEMA, 0); }
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode RESTRICT() { return getToken(DerbyParser.RESTRICT, 0); }
		public Drop_schemaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_schema; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDrop_schema(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDrop_schema(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDrop_schema(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Drop_schemaContext drop_schema() throws RecognitionException {
		Drop_schemaContext _localctx = new Drop_schemaContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_drop_schema);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1887);
			match(DROP);
			setState(1888);
			match(SCHEMA);
			setState(1889);
			schema_name();
			setState(1890);
			match(RESTRICT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Drop_sequenceContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(DerbyParser.DROP, 0); }
		public TerminalNode SEQUENCE() { return getToken(DerbyParser.SEQUENCE, 0); }
		public Sequence_nameContext sequence_name() {
			return getRuleContext(Sequence_nameContext.class,0);
		}
		public TerminalNode RESTRICT() { return getToken(DerbyParser.RESTRICT, 0); }
		public Drop_sequenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_sequence; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDrop_sequence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDrop_sequence(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDrop_sequence(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Drop_sequenceContext drop_sequence() throws RecognitionException {
		Drop_sequenceContext _localctx = new Drop_sequenceContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_drop_sequence);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1892);
			match(DROP);
			setState(1893);
			match(SEQUENCE);
			setState(1894);
			sequence_name();
			setState(1895);
			match(RESTRICT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Drop_synonymContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(DerbyParser.DROP, 0); }
		public TerminalNode SYNONYM() { return getToken(DerbyParser.SYNONYM, 0); }
		public Synonym_nameContext synonym_name() {
			return getRuleContext(Synonym_nameContext.class,0);
		}
		public Drop_synonymContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_synonym; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDrop_synonym(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDrop_synonym(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDrop_synonym(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Drop_synonymContext drop_synonym() throws RecognitionException {
		Drop_synonymContext _localctx = new Drop_synonymContext(_ctx, getState());
		enterRule(_localctx, 208, RULE_drop_synonym);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1897);
			match(DROP);
			setState(1898);
			match(SYNONYM);
			setState(1899);
			synonym_name();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Drop_tableContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(DerbyParser.DROP, 0); }
		public TerminalNode TABLE() { return getToken(DerbyParser.TABLE, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public Drop_tableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_table; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDrop_table(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDrop_table(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDrop_table(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Drop_tableContext drop_table() throws RecognitionException {
		Drop_tableContext _localctx = new Drop_tableContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_drop_table);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1901);
			match(DROP);
			setState(1902);
			match(TABLE);
			setState(1903);
			table_name();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Drop_triggerContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(DerbyParser.DROP, 0); }
		public TerminalNode TRIGGER() { return getToken(DerbyParser.TRIGGER, 0); }
		public Trigger_nameContext trigger_name() {
			return getRuleContext(Trigger_nameContext.class,0);
		}
		public Drop_triggerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_trigger; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDrop_trigger(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDrop_trigger(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDrop_trigger(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Drop_triggerContext drop_trigger() throws RecognitionException {
		Drop_triggerContext _localctx = new Drop_triggerContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_drop_trigger);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1905);
			match(DROP);
			setState(1906);
			match(TRIGGER);
			setState(1907);
			trigger_name();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Drop_typeContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(DerbyParser.DROP, 0); }
		public TerminalNode TYPE() { return getToken(DerbyParser.TYPE, 0); }
		public Type_nameContext type_name() {
			return getRuleContext(Type_nameContext.class,0);
		}
		public TerminalNode RESTRICT() { return getToken(DerbyParser.RESTRICT, 0); }
		public Drop_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDrop_type(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDrop_type(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDrop_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Drop_typeContext drop_type() throws RecognitionException {
		Drop_typeContext _localctx = new Drop_typeContext(_ctx, getState());
		enterRule(_localctx, 214, RULE_drop_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1909);
			match(DROP);
			setState(1910);
			match(TYPE);
			setState(1911);
			type_name();
			setState(1912);
			match(RESTRICT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Drop_viewContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(DerbyParser.DROP, 0); }
		public TerminalNode VIEW() { return getToken(DerbyParser.VIEW, 0); }
		public View_nameContext view_name() {
			return getRuleContext(View_nameContext.class,0);
		}
		public Drop_viewContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_view; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterDrop_view(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitDrop_view(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitDrop_view(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Drop_viewContext drop_view() throws RecognitionException {
		Drop_viewContext _localctx = new Drop_viewContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_drop_view);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1914);
			match(DROP);
			setState(1915);
			match(VIEW);
			setState(1916);
			view_name();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StringContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(DerbyParser.STRING_LITERAL, 0); }
		public StringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitString(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitString(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringContext string() throws RecognitionException {
		StringContext _localctx = new StringContext(_ctx, getState());
		enterRule(_localctx, 218, RULE_string);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1918);
			match(STRING_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Table_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(DerbyParser.DOT, 0); }
		public Table_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterTable_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitTable_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitTable_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Table_nameContext table_name() throws RecognitionException {
		Table_nameContext _localctx = new Table_nameContext(_ctx, getState());
		enterRule(_localctx, 220, RULE_table_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1923);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,186,_ctx) ) {
			case 1:
				{
				setState(1920);
				schema_name();
				setState(1921);
				match(DOT);
				}
				break;
			}
			setState(1925);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Schema_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Schema_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_schema_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSchema_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSchema_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSchema_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Schema_nameContext schema_name() throws RecognitionException {
		Schema_nameContext _localctx = new Schema_nameContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_schema_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1927);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Role_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Role_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_role_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterRole_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitRole_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitRole_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Role_nameContext role_name() throws RecognitionException {
		Role_nameContext _localctx = new Role_nameContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_role_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1929);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Aggregate_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(DerbyParser.DOT, 0); }
		public Aggregate_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregate_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterAggregate_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitAggregate_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitAggregate_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Aggregate_nameContext aggregate_name() throws RecognitionException {
		Aggregate_nameContext _localctx = new Aggregate_nameContext(_ctx, getState());
		enterRule(_localctx, 226, RULE_aggregate_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1934);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,187,_ctx) ) {
			case 1:
				{
				setState(1931);
				schema_name();
				setState(1932);
				match(DOT);
				}
				break;
			}
			setState(1936);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Constraint_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(DerbyParser.DOT, 0); }
		public Constraint_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraint_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterConstraint_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitConstraint_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitConstraint_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Constraint_nameContext constraint_name() throws RecognitionException {
		Constraint_nameContext _localctx = new Constraint_nameContext(_ctx, getState());
		enterRule(_localctx, 228, RULE_constraint_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1941);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,188,_ctx) ) {
			case 1:
				{
				setState(1938);
				schema_name();
				setState(1939);
				match(DOT);
				}
				break;
			}
			setState(1943);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Column_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public TerminalNode DOT() { return getToken(DerbyParser.DOT, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public Correlation_nameContext correlation_name() {
			return getRuleContext(Correlation_nameContext.class,0);
		}
		public Column_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterColumn_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitColumn_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitColumn_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_nameContext column_name() throws RecognitionException {
		Column_nameContext _localctx = new Column_nameContext(_ctx, getState());
		enterRule(_localctx, 230, RULE_column_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1951);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,190,_ctx) ) {
			case 1:
				{
				setState(1947);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,189,_ctx) ) {
				case 1:
					{
					setState(1945);
					table_name();
					}
					break;
				case 2:
					{
					setState(1946);
					correlation_name();
					}
					break;
				}
				setState(1949);
				match(DOT);
				}
				break;
			}
			setState(1953);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Correlation_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Correlation_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_correlation_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCorrelation_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCorrelation_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCorrelation_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Correlation_nameContext correlation_name() throws RecognitionException {
		Correlation_nameContext _localctx = new Correlation_nameContext(_ctx, getState());
		enterRule(_localctx, 232, RULE_correlation_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1955);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Function_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(DerbyParser.DOT, 0); }
		public Function_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterFunction_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitFunction_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitFunction_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Function_nameContext function_name() throws RecognitionException {
		Function_nameContext _localctx = new Function_nameContext(_ctx, getState());
		enterRule(_localctx, 234, RULE_function_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1960);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,191,_ctx) ) {
			case 1:
				{
				setState(1957);
				schema_name();
				setState(1958);
				match(DOT);
				}
				break;
			}
			setState(1962);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Index_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(DerbyParser.DOT, 0); }
		public Index_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_index_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterIndex_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitIndex_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitIndex_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Index_nameContext index_name() throws RecognitionException {
		Index_nameContext _localctx = new Index_nameContext(_ctx, getState());
		enterRule(_localctx, 236, RULE_index_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1967);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,192,_ctx) ) {
			case 1:
				{
				setState(1964);
				schema_name();
				setState(1965);
				match(DOT);
				}
				break;
			}
			setState(1969);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Procedure_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(DerbyParser.DOT, 0); }
		public Procedure_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_procedure_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterProcedure_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitProcedure_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitProcedure_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Procedure_nameContext procedure_name() throws RecognitionException {
		Procedure_nameContext _localctx = new Procedure_nameContext(_ctx, getState());
		enterRule(_localctx, 238, RULE_procedure_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1974);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,193,_ctx) ) {
			case 1:
				{
				setState(1971);
				schema_name();
				setState(1972);
				match(DOT);
				}
				break;
			}
			setState(1976);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Parameter_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Parameter_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameter_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterParameter_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitParameter_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitParameter_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Parameter_nameContext parameter_name() throws RecognitionException {
		Parameter_nameContext _localctx = new Parameter_nameContext(_ctx, getState());
		enterRule(_localctx, 240, RULE_parameter_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1978);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Sequence_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(DerbyParser.DOT, 0); }
		public Sequence_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sequence_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSequence_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSequence_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSequence_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Sequence_nameContext sequence_name() throws RecognitionException {
		Sequence_nameContext _localctx = new Sequence_nameContext(_ctx, getState());
		enterRule(_localctx, 242, RULE_sequence_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1983);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,194,_ctx) ) {
			case 1:
				{
				setState(1980);
				schema_name();
				setState(1981);
				match(DOT);
				}
				break;
			}
			setState(1985);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Synonym_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(DerbyParser.DOT, 0); }
		public Synonym_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_synonym_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSynonym_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSynonym_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSynonym_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Synonym_nameContext synonym_name() throws RecognitionException {
		Synonym_nameContext _localctx = new Synonym_nameContext(_ctx, getState());
		enterRule(_localctx, 244, RULE_synonym_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1990);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,195,_ctx) ) {
			case 1:
				{
				setState(1987);
				schema_name();
				setState(1988);
				match(DOT);
				}
				break;
			}
			setState(1992);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Trigger_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(DerbyParser.DOT, 0); }
		public Trigger_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_trigger_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterTrigger_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitTrigger_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitTrigger_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Trigger_nameContext trigger_name() throws RecognitionException {
		Trigger_nameContext _localctx = new Trigger_nameContext(_ctx, getState());
		enterRule(_localctx, 246, RULE_trigger_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1997);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,196,_ctx) ) {
			case 1:
				{
				setState(1994);
				schema_name();
				setState(1995);
				match(DOT);
				}
				break;
			}
			setState(1999);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Type_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(DerbyParser.DOT, 0); }
		public Type_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterType_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitType_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitType_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Type_nameContext type_name() throws RecognitionException {
		Type_nameContext _localctx = new Type_nameContext(_ctx, getState());
		enterRule(_localctx, 248, RULE_type_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2004);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,197,_ctx) ) {
			case 1:
				{
				setState(2001);
				schema_name();
				setState(2002);
				match(DOT);
				}
				break;
			}
			setState(2006);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class User_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public User_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_user_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterUser_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitUser_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitUser_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final User_nameContext user_name() throws RecognitionException {
		User_nameContext _localctx = new User_nameContext(_ctx, getState());
		enterRule(_localctx, 250, RULE_user_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2008);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class View_nameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(DerbyParser.DOT, 0); }
		public View_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_view_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterView_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitView_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitView_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final View_nameContext view_name() throws RecognitionException {
		View_nameContext _localctx = new View_nameContext(_ctx, getState());
		enterRule(_localctx, 252, RULE_view_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2013);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,198,_ctx) ) {
			case 1:
				{
				setState(2010);
				schema_name();
				setState(2011);
				match(DOT);
				}
				break;
			}
			setState(2015);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Cursor_nNameContext extends ParserRuleContext {
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Cursor_nNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cursor_nName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCursor_nName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCursor_nName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCursor_nName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Cursor_nNameContext cursor_nName() throws RecognitionException {
		Cursor_nNameContext _localctx = new Cursor_nNameContext(_ctx, getState());
		enterRule(_localctx, 254, RULE_cursor_nName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2017);
			id_();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Id_Context extends ParserRuleContext {
		public TerminalNode ID() { return getToken(DerbyParser.ID, 0); }
		public TerminalNode DOUBLE_QUOTE_ID() { return getToken(DerbyParser.DOUBLE_QUOTE_ID, 0); }
		public Id_Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id_; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterId_(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitId_(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitId_(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Id_Context id_() throws RecognitionException {
		Id_Context _localctx = new Id_Context(_ctx, getState());
		enterRule(_localctx, 256, RULE_id_);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2019);
			_la = _input.LA(1);
			if ( !(_la==DOUBLE_QUOTE_ID || _la==ID) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NumContext extends ParserRuleContext {
		public TerminalNode DECIMAL_LITERAL() { return getToken(DerbyParser.DECIMAL_LITERAL, 0); }
		public NumContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_num; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterNum(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitNum(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitNum(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumContext num() throws RecognitionException {
		NumContext _localctx = new NumContext(_ctx, getState());
		enterRule(_localctx, 258, RULE_num);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2021);
			match(DECIMAL_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Expr_listContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Expr_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterExpr_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitExpr_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitExpr_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expr_listContext expr_list() throws RecognitionException {
		Expr_listContext _localctx = new Expr_listContext(_ctx, getState());
		enterRule(_localctx, 260, RULE_expr_list);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2023);
			expression(0);
			setState(2028);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,199,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2024);
					match(COMMA);
					setState(2025);
					expression(0);
					}
					} 
				}
				setState(2030);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,199,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public Token op;
		public Primitive_expressionContext primitive_expression() {
			return getRuleContext(Primitive_expressionContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public Table_subqueryContext table_subquery() {
			return getRuleContext(Table_subqueryContext.class,0);
		}
		public Function_callContext function_call() {
			return getRuleContext(Function_callContext.class,0);
		}
		public Case_expressionContext case_expression() {
			return getRuleContext(Case_expressionContext.class,0);
		}
		public TerminalNode PLUS() { return getToken(DerbyParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(DerbyParser.MINUS, 0); }
		public TerminalNode NOT() { return getToken(DerbyParser.NOT, 0); }
		public TerminalNode CAST() { return getToken(DerbyParser.CAST, 0); }
		public TerminalNode AS() { return getToken(DerbyParser.AS, 0); }
		public Data_typeContext data_type() {
			return getRuleContext(Data_typeContext.class,0);
		}
		public TerminalNode STAR() { return getToken(DerbyParser.STAR, 0); }
		public TerminalNode DIVIDE() { return getToken(DerbyParser.DIVIDE, 0); }
		public TerminalNode MODULE() { return getToken(DerbyParser.MODULE, 0); }
		public TerminalNode DOT() { return getToken(DerbyParser.DOT, 0); }
		public Comparison_operatorContext comparison_operator() {
			return getRuleContext(Comparison_operatorContext.class,0);
		}
		public TerminalNode AND() { return getToken(DerbyParser.AND, 0); }
		public TerminalNode OR() { return getToken(DerbyParser.OR, 0); }
		public TerminalNode IN() { return getToken(DerbyParser.IN, 0); }
		public Expr_listContext expr_list() {
			return getRuleContext(Expr_listContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 262;
		enterRecursionRule(_localctx, 262, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2051);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,200,_ctx) ) {
			case 1:
				{
				setState(2032);
				primitive_expression();
				}
				break;
			case 2:
				{
				setState(2033);
				match(LR_BRACKET);
				setState(2034);
				expression(0);
				setState(2035);
				match(RR_BRACKET);
				}
				break;
			case 3:
				{
				setState(2037);
				table_subquery();
				}
				break;
			case 4:
				{
				setState(2038);
				function_call();
				}
				break;
			case 5:
				{
				setState(2039);
				case_expression();
				}
				break;
			case 6:
				{
				setState(2040);
				((ExpressionContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==PLUS || _la==MINUS) ) {
					((ExpressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2041);
				expression(10);
				}
				break;
			case 7:
				{
				setState(2042);
				match(NOT);
				setState(2043);
				expression(3);
				}
				break;
			case 8:
				{
				setState(2044);
				match(CAST);
				setState(2045);
				match(LR_BRACKET);
				setState(2046);
				expression(0);
				setState(2047);
				match(AS);
				setState(2048);
				data_type();
				setState(2049);
				match(RR_BRACKET);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(2083);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,203,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(2081);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,202,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(2053);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(2054);
						((ExpressionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 353)) & ~0x3f) == 0 && ((1L << (_la - 353)) & 7L) != 0)) ) {
							((ExpressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(2055);
						expression(10);
						}
						break;
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(2056);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(2057);
						((ExpressionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==PLUS || _la==MINUS) ) {
							((ExpressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(2058);
						expression(9);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(2059);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(2060);
						match(DOT);
						setState(2061);
						expression(8);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(2062);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(2063);
						comparison_operator();
						setState(2064);
						expression(7);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(2066);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(2067);
						match(AND);
						setState(2068);
						expression(6);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(2069);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(2070);
						match(OR);
						setState(2071);
						expression(5);
						}
						break;
					case 7:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(2072);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(2074);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,201,_ctx) ) {
						case 1:
							{
							setState(2073);
							match(NOT);
							}
							break;
						}
						setState(2076);
						match(IN);
						setState(2077);
						match(LR_BRACKET);
						setState(2078);
						expr_list();
						setState(2079);
						match(RR_BRACKET);
						}
						break;
					}
					} 
				}
				setState(2085);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,203,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Primitive_expressionContext extends ParserRuleContext {
		public TerminalNode DEFAULT() { return getToken(DerbyParser.DEFAULT, 0); }
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public Id_Context id_() {
			return getRuleContext(Id_Context.class,0);
		}
		public Primitive_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primitive_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterPrimitive_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitPrimitive_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitPrimitive_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Primitive_expressionContext primitive_expression() throws RecognitionException {
		Primitive_expressionContext _localctx = new Primitive_expressionContext(_ctx, getState());
		enterRule(_localctx, 264, RULE_primitive_expression);
		try {
			setState(2089);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,204,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2086);
				match(DEFAULT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2087);
				literal();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2088);
				id_();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Function_callContext extends ParserRuleContext {
		public Function_nameContext function_name() {
			return getRuleContext(Function_nameContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public Expr_listContext expr_list() {
			return getRuleContext(Expr_listContext.class,0);
		}
		public Standard_built_in_functionContext standard_built_in_function() {
			return getRuleContext(Standard_built_in_functionContext.class,0);
		}
		public Aggreagate_built_in_functionContext aggreagate_built_in_function() {
			return getRuleContext(Aggreagate_built_in_functionContext.class,0);
		}
		public Function_callContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function_call; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterFunction_call(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitFunction_call(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitFunction_call(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Function_callContext function_call() throws RecognitionException {
		Function_callContext _localctx = new Function_callContext(_ctx, getState());
		enterRule(_localctx, 266, RULE_function_call);
		try {
			setState(2112);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,208,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2091);
				function_name();
				setState(2092);
				match(LR_BRACKET);
				setState(2094);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,205,_ctx) ) {
				case 1:
					{
					setState(2093);
					expr_list();
					}
					break;
				}
				setState(2096);
				match(RR_BRACKET);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2098);
				standard_built_in_function();
				setState(2099);
				match(LR_BRACKET);
				setState(2101);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,206,_ctx) ) {
				case 1:
					{
					setState(2100);
					expr_list();
					}
					break;
				}
				setState(2103);
				match(RR_BRACKET);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2105);
				aggreagate_built_in_function();
				setState(2106);
				match(LR_BRACKET);
				setState(2108);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,207,_ctx) ) {
				case 1:
					{
					setState(2107);
					expr_list();
					}
					break;
				}
				setState(2110);
				match(RR_BRACKET);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LiteralContext extends ParserRuleContext {
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public TerminalNode DECIMAL_LITERAL() { return getToken(DerbyParser.DECIMAL_LITERAL, 0); }
		public SignContext sign() {
			return getRuleContext(SignContext.class,0);
		}
		public TerminalNode REAL_LITERAL() { return getToken(DerbyParser.REAL_LITERAL, 0); }
		public TerminalNode FLOAT_LITERAL() { return getToken(DerbyParser.FLOAT_LITERAL, 0); }
		public TerminalNode TRUE() { return getToken(DerbyParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(DerbyParser.FALSE, 0); }
		public TerminalNode NULL_() { return getToken(DerbyParser.NULL_, 0); }
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 268, RULE_literal);
		int _la;
		try {
			setState(2126);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,211,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2114);
				string();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2116);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,209,_ctx) ) {
				case 1:
					{
					setState(2115);
					sign();
					}
					break;
				}
				setState(2118);
				match(DECIMAL_LITERAL);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2120);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,210,_ctx) ) {
				case 1:
					{
					setState(2119);
					sign();
					}
					break;
				}
				setState(2122);
				_la = _input.LA(1);
				if ( !(_la==FLOAT_LITERAL || _la==REAL_LITERAL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2123);
				match(TRUE);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2124);
				match(FALSE);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2125);
				match(NULL_);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SignContext extends ParserRuleContext {
		public TerminalNode PLUS() { return getToken(DerbyParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(DerbyParser.MINUS, 0); }
		public SignContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sign; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSign(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSign(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSign(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SignContext sign() throws RecognitionException {
		SignContext _localctx = new SignContext(_ctx, getState());
		enterRule(_localctx, 270, RULE_sign);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2128);
			_la = _input.LA(1);
			if ( !(_la==PLUS || _la==MINUS) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Case_expressionContext extends ParserRuleContext {
		public Searched_caseContext searched_case() {
			return getRuleContext(Searched_caseContext.class,0);
		}
		public Simple_caseContext simple_case() {
			return getRuleContext(Simple_caseContext.class,0);
		}
		public Extended_caseContext extended_case() {
			return getRuleContext(Extended_caseContext.class,0);
		}
		public Case_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_case_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCase_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCase_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCase_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Case_expressionContext case_expression() throws RecognitionException {
		Case_expressionContext _localctx = new Case_expressionContext(_ctx, getState());
		enterRule(_localctx, 272, RULE_case_expression);
		try {
			setState(2133);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,212,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2130);
				searched_case();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2131);
				simple_case();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2132);
				extended_case();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Searched_caseContext extends ParserRuleContext {
		public TerminalNode CASE() { return getToken(DerbyParser.CASE, 0); }
		public List<TerminalNode> WHEN() { return getTokens(DerbyParser.WHEN); }
		public TerminalNode WHEN(int i) {
			return getToken(DerbyParser.WHEN, i);
		}
		public List<Boolean_expressionContext> boolean_expression() {
			return getRuleContexts(Boolean_expressionContext.class);
		}
		public Boolean_expressionContext boolean_expression(int i) {
			return getRuleContext(Boolean_expressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(DerbyParser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(DerbyParser.THEN, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode END() { return getToken(DerbyParser.END, 0); }
		public TerminalNode ELSE() { return getToken(DerbyParser.ELSE, 0); }
		public Searched_caseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_searched_case; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSearched_case(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSearched_case(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSearched_case(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Searched_caseContext searched_case() throws RecognitionException {
		Searched_caseContext _localctx = new Searched_caseContext(_ctx, getState());
		enterRule(_localctx, 274, RULE_searched_case);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2135);
			match(CASE);
			setState(2136);
			match(WHEN);
			setState(2137);
			boolean_expression();
			setState(2138);
			match(THEN);
			setState(2139);
			expression(0);
			setState(2147);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,213,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2140);
					match(WHEN);
					setState(2141);
					boolean_expression();
					setState(2142);
					match(THEN);
					setState(2143);
					expression(0);
					}
					} 
				}
				setState(2149);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,213,_ctx);
			}
			setState(2152);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,214,_ctx) ) {
			case 1:
				{
				setState(2150);
				match(ELSE);
				setState(2151);
				expression(0);
				}
				break;
			}
			setState(2154);
			match(END);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Simple_caseContext extends ParserRuleContext {
		public TerminalNode CASE() { return getToken(DerbyParser.CASE, 0); }
		public List<Value_expressionContext> value_expression() {
			return getRuleContexts(Value_expressionContext.class);
		}
		public Value_expressionContext value_expression(int i) {
			return getRuleContext(Value_expressionContext.class,i);
		}
		public List<TerminalNode> WHEN() { return getTokens(DerbyParser.WHEN); }
		public TerminalNode WHEN(int i) {
			return getToken(DerbyParser.WHEN, i);
		}
		public List<TerminalNode> THEN() { return getTokens(DerbyParser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(DerbyParser.THEN, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode END() { return getToken(DerbyParser.END, 0); }
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public TerminalNode ELSE() { return getToken(DerbyParser.ELSE, 0); }
		public Simple_caseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simple_case; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSimple_case(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSimple_case(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSimple_case(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Simple_caseContext simple_case() throws RecognitionException {
		Simple_caseContext _localctx = new Simple_caseContext(_ctx, getState());
		enterRule(_localctx, 276, RULE_simple_case);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2156);
			match(CASE);
			setState(2157);
			value_expression();
			setState(2158);
			match(WHEN);
			setState(2159);
			value_expression();
			setState(2164);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,215,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2160);
					match(COMMA);
					setState(2161);
					value_expression();
					}
					} 
				}
				setState(2166);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,215,_ctx);
			}
			setState(2167);
			match(THEN);
			setState(2168);
			expression(0);
			setState(2183);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,217,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2169);
					match(WHEN);
					setState(2170);
					value_expression();
					setState(2175);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,216,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(2171);
							match(COMMA);
							setState(2172);
							value_expression();
							}
							} 
						}
						setState(2177);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,216,_ctx);
					}
					setState(2178);
					match(THEN);
					setState(2179);
					expression(0);
					}
					} 
				}
				setState(2185);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,217,_ctx);
			}
			setState(2188);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,218,_ctx) ) {
			case 1:
				{
				setState(2186);
				match(ELSE);
				setState(2187);
				expression(0);
				}
				break;
			}
			setState(2190);
			match(END);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Extended_caseContext extends ParserRuleContext {
		public TerminalNode CASE() { return getToken(DerbyParser.CASE, 0); }
		public Value_expressionContext value_expression() {
			return getRuleContext(Value_expressionContext.class,0);
		}
		public List<TerminalNode> WHEN() { return getTokens(DerbyParser.WHEN); }
		public TerminalNode WHEN(int i) {
			return getToken(DerbyParser.WHEN, i);
		}
		public List<When_operandContext> when_operand() {
			return getRuleContexts(When_operandContext.class);
		}
		public When_operandContext when_operand(int i) {
			return getRuleContext(When_operandContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(DerbyParser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(DerbyParser.THEN, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode END() { return getToken(DerbyParser.END, 0); }
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public TerminalNode ELSE() { return getToken(DerbyParser.ELSE, 0); }
		public Extended_caseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extended_case; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterExtended_case(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitExtended_case(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitExtended_case(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Extended_caseContext extended_case() throws RecognitionException {
		Extended_caseContext _localctx = new Extended_caseContext(_ctx, getState());
		enterRule(_localctx, 278, RULE_extended_case);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2192);
			match(CASE);
			setState(2193);
			value_expression();
			setState(2194);
			match(WHEN);
			setState(2195);
			when_operand();
			setState(2200);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,219,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2196);
					match(COMMA);
					setState(2197);
					when_operand();
					}
					} 
				}
				setState(2202);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,219,_ctx);
			}
			setState(2203);
			match(THEN);
			setState(2204);
			expression(0);
			setState(2219);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,221,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2205);
					match(WHEN);
					setState(2206);
					when_operand();
					setState(2211);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,220,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(2207);
							match(COMMA);
							setState(2208);
							when_operand();
							}
							} 
						}
						setState(2213);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,220,_ctx);
					}
					setState(2214);
					match(THEN);
					setState(2215);
					expression(0);
					}
					} 
				}
				setState(2221);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,221,_ctx);
			}
			setState(2224);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,222,_ctx) ) {
			case 1:
				{
				setState(2222);
				match(ELSE);
				setState(2223);
				expression(0);
				}
				break;
			}
			setState(2226);
			match(END);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class When_operandContext extends ParserRuleContext {
		public Value_expressionContext value_expression() {
			return getRuleContext(Value_expressionContext.class,0);
		}
		public Comparison_operatorContext comparison_operator() {
			return getRuleContext(Comparison_operatorContext.class,0);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode IS() { return getToken(DerbyParser.IS, 0); }
		public TerminalNode NULL_() { return getToken(DerbyParser.NULL_, 0); }
		public TerminalNode NOT() { return getToken(DerbyParser.NOT, 0); }
		public TerminalNode LIKE() { return getToken(DerbyParser.LIKE, 0); }
		public List<StringContext> string() {
			return getRuleContexts(StringContext.class);
		}
		public StringContext string(int i) {
			return getRuleContext(StringContext.class,i);
		}
		public TerminalNode ESCAPE() { return getToken(DerbyParser.ESCAPE, 0); }
		public TerminalNode BETWEEN() { return getToken(DerbyParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(DerbyParser.AND, 0); }
		public TerminalNode IN() { return getToken(DerbyParser.IN, 0); }
		public Table_subqueryContext table_subquery() {
			return getRuleContext(Table_subqueryContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public Expr_listContext expr_list() {
			return getRuleContext(Expr_listContext.class,0);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public TerminalNode ALL() { return getToken(DerbyParser.ALL, 0); }
		public TerminalNode ANY() { return getToken(DerbyParser.ANY, 0); }
		public TerminalNode SOME() { return getToken(DerbyParser.SOME, 0); }
		public When_operandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_when_operand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterWhen_operand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitWhen_operand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitWhen_operand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final When_operandContext when_operand() throws RecognitionException {
		When_operandContext _localctx = new When_operandContext(_ctx, getState());
		enterRule(_localctx, 280, RULE_when_operand);
		int _la;
		try {
			setState(2271);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,229,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2228);
				value_expression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2229);
				comparison_operator();
				setState(2230);
				expression(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2232);
				match(IS);
				setState(2234);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,223,_ctx) ) {
				case 1:
					{
					setState(2233);
					match(NOT);
					}
					break;
				}
				setState(2236);
				match(NULL_);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2238);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,224,_ctx) ) {
				case 1:
					{
					setState(2237);
					match(NOT);
					}
					break;
				}
				setState(2240);
				match(LIKE);
				setState(2241);
				string();
				setState(2244);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,225,_ctx) ) {
				case 1:
					{
					setState(2242);
					match(ESCAPE);
					setState(2243);
					string();
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2247);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,226,_ctx) ) {
				case 1:
					{
					setState(2246);
					match(NOT);
					}
					break;
				}
				setState(2249);
				match(BETWEEN);
				setState(2250);
				expression(0);
				setState(2251);
				match(AND);
				setState(2252);
				expression(0);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2255);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,227,_ctx) ) {
				case 1:
					{
					setState(2254);
					match(NOT);
					}
					break;
				}
				setState(2257);
				match(IN);
				setState(2258);
				table_subquery();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2260);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,228,_ctx) ) {
				case 1:
					{
					setState(2259);
					match(NOT);
					}
					break;
				}
				setState(2262);
				match(IN);
				setState(2263);
				match(LR_BRACKET);
				setState(2264);
				expr_list();
				setState(2265);
				match(RR_BRACKET);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(2267);
				comparison_operator();
				setState(2268);
				_la = _input.LA(1);
				if ( !(_la==ALL || _la==ANY || _la==SOME) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(2269);
				table_subquery();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Value_expressionContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Value_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterValue_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitValue_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitValue_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Value_expressionContext value_expression() throws RecognitionException {
		Value_expressionContext _localctx = new Value_expressionContext(_ctx, getState());
		enterRule(_localctx, 282, RULE_value_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2273);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Standard_built_in_functionContext extends ParserRuleContext {
		public TerminalNode ABS() { return getToken(DerbyParser.ABS, 0); }
		public TerminalNode ABSVAL() { return getToken(DerbyParser.ABSVAL, 0); }
		public TerminalNode ACOS() { return getToken(DerbyParser.ACOS, 0); }
		public TerminalNode ASIN() { return getToken(DerbyParser.ASIN, 0); }
		public TerminalNode ATAN() { return getToken(DerbyParser.ATAN, 0); }
		public TerminalNode ATAN2() { return getToken(DerbyParser.ATAN2, 0); }
		public TerminalNode BIGINT() { return getToken(DerbyParser.BIGINT, 0); }
		public TerminalNode CAST() { return getToken(DerbyParser.CAST, 0); }
		public TerminalNode CEIL() { return getToken(DerbyParser.CEIL, 0); }
		public TerminalNode CEILING() { return getToken(DerbyParser.CEILING, 0); }
		public TerminalNode CHAR() { return getToken(DerbyParser.CHAR, 0); }
		public TerminalNode PIPE_PIPE() { return getToken(DerbyParser.PIPE_PIPE, 0); }
		public TerminalNode COS() { return getToken(DerbyParser.COS, 0); }
		public TerminalNode NULLIF() { return getToken(DerbyParser.NULLIF, 0); }
		public TerminalNode CURRENT_DATE() { return getToken(DerbyParser.CURRENT_DATE, 0); }
		public TerminalNode CURRENT() { return getToken(DerbyParser.CURRENT, 0); }
		public TerminalNode ISOLATION() { return getToken(DerbyParser.ISOLATION, 0); }
		public TerminalNode CURRENT_TIME() { return getToken(DerbyParser.CURRENT_TIME, 0); }
		public TerminalNode CURRENT_TIMESTAMP() { return getToken(DerbyParser.CURRENT_TIMESTAMP, 0); }
		public TerminalNode CURRENT_USER() { return getToken(DerbyParser.CURRENT_USER, 0); }
		public TerminalNode DATE() { return getToken(DerbyParser.DATE, 0); }
		public TerminalNode DAY() { return getToken(DerbyParser.DAY, 0); }
		public TerminalNode DEGREES() { return getToken(DerbyParser.DEGREES, 0); }
		public TerminalNode DOUBLE() { return getToken(DerbyParser.DOUBLE, 0); }
		public TerminalNode EXP() { return getToken(DerbyParser.EXP, 0); }
		public TerminalNode FLOOR() { return getToken(DerbyParser.FLOOR, 0); }
		public TerminalNode HOUR() { return getToken(DerbyParser.HOUR, 0); }
		public TerminalNode IDENTITY_VAL_LOCAL() { return getToken(DerbyParser.IDENTITY_VAL_LOCAL, 0); }
		public TerminalNode INT() { return getToken(DerbyParser.INT, 0); }
		public TerminalNode INTEGER() { return getToken(DerbyParser.INTEGER, 0); }
		public TerminalNode LENGTH() { return getToken(DerbyParser.LENGTH, 0); }
		public TerminalNode LN() { return getToken(DerbyParser.LN, 0); }
		public TerminalNode LOG() { return getToken(DerbyParser.LOG, 0); }
		public TerminalNode LOG10() { return getToken(DerbyParser.LOG10, 0); }
		public TerminalNode LOCATE() { return getToken(DerbyParser.LOCATE, 0); }
		public TerminalNode LCASE() { return getToken(DerbyParser.LCASE, 0); }
		public TerminalNode LOWER() { return getToken(DerbyParser.LOWER, 0); }
		public TerminalNode LTRIM() { return getToken(DerbyParser.LTRIM, 0); }
		public TerminalNode MINUTE() { return getToken(DerbyParser.MINUTE, 0); }
		public TerminalNode MOD() { return getToken(DerbyParser.MOD, 0); }
		public TerminalNode MONTH() { return getToken(DerbyParser.MONTH, 0); }
		public TerminalNode PI() { return getToken(DerbyParser.PI, 0); }
		public TerminalNode RADIANS() { return getToken(DerbyParser.RADIANS, 0); }
		public TerminalNode RTRIM() { return getToken(DerbyParser.RTRIM, 0); }
		public TerminalNode SECOND() { return getToken(DerbyParser.SECOND, 0); }
		public TerminalNode SESSION_USER() { return getToken(DerbyParser.SESSION_USER, 0); }
		public TerminalNode SIN() { return getToken(DerbyParser.SIN, 0); }
		public TerminalNode SMALLINT() { return getToken(DerbyParser.SMALLINT, 0); }
		public TerminalNode SQRT() { return getToken(DerbyParser.SQRT, 0); }
		public TerminalNode SUBSTR() { return getToken(DerbyParser.SUBSTR, 0); }
		public TerminalNode TAN() { return getToken(DerbyParser.TAN, 0); }
		public TerminalNode TIME() { return getToken(DerbyParser.TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(DerbyParser.TIMESTAMP, 0); }
		public TerminalNode TRIM() { return getToken(DerbyParser.TRIM, 0); }
		public TerminalNode UCASE() { return getToken(DerbyParser.UCASE, 0); }
		public TerminalNode UPPER() { return getToken(DerbyParser.UPPER, 0); }
		public TerminalNode USER() { return getToken(DerbyParser.USER, 0); }
		public TerminalNode VARCHAR() { return getToken(DerbyParser.VARCHAR, 0); }
		public TerminalNode YEAR() { return getToken(DerbyParser.YEAR, 0); }
		public Standard_built_in_functionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_standard_built_in_function; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterStandard_built_in_function(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitStandard_built_in_function(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitStandard_built_in_function(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Standard_built_in_functionContext standard_built_in_function() throws RecognitionException {
		Standard_built_in_functionContext _localctx = new Standard_built_in_functionContext(_ctx, getState());
		enterRule(_localctx, 284, RULE_standard_built_in_function);
		try {
			setState(2334);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,230,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2275);
				match(ABS);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2276);
				match(ABSVAL);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2277);
				match(ACOS);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2278);
				match(ASIN);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2279);
				match(ATAN);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(2280);
				match(ATAN2);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(2281);
				match(BIGINT);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(2282);
				match(CAST);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(2283);
				match(CEIL);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(2284);
				match(CEILING);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(2285);
				match(CHAR);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(2286);
				match(PIPE_PIPE);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(2287);
				match(COS);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(2288);
				match(NULLIF);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(2289);
				match(CURRENT_DATE);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(2290);
				match(CURRENT);
				setState(2291);
				match(ISOLATION);
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(2292);
				match(CURRENT_TIME);
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(2293);
				match(CURRENT_TIMESTAMP);
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(2294);
				match(CURRENT_USER);
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(2295);
				match(DATE);
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 21);
				{
				setState(2296);
				match(DAY);
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 22);
				{
				setState(2297);
				match(DEGREES);
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 23);
				{
				setState(2298);
				match(DOUBLE);
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 24);
				{
				setState(2299);
				match(EXP);
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 25);
				{
				setState(2300);
				match(FLOOR);
				}
				break;
			case 26:
				enterOuterAlt(_localctx, 26);
				{
				setState(2301);
				match(HOUR);
				}
				break;
			case 27:
				enterOuterAlt(_localctx, 27);
				{
				setState(2302);
				match(IDENTITY_VAL_LOCAL);
				}
				break;
			case 28:
				enterOuterAlt(_localctx, 28);
				{
				setState(2303);
				match(INT);
				}
				break;
			case 29:
				enterOuterAlt(_localctx, 29);
				{
				setState(2304);
				match(INTEGER);
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 30);
				{
				setState(2305);
				match(LENGTH);
				}
				break;
			case 31:
				enterOuterAlt(_localctx, 31);
				{
				setState(2306);
				match(LN);
				}
				break;
			case 32:
				enterOuterAlt(_localctx, 32);
				{
				setState(2307);
				match(LOG);
				}
				break;
			case 33:
				enterOuterAlt(_localctx, 33);
				{
				setState(2308);
				match(LOG10);
				}
				break;
			case 34:
				enterOuterAlt(_localctx, 34);
				{
				setState(2309);
				match(LOCATE);
				}
				break;
			case 35:
				enterOuterAlt(_localctx, 35);
				{
				setState(2310);
				match(LCASE);
				}
				break;
			case 36:
				enterOuterAlt(_localctx, 36);
				{
				setState(2311);
				match(LOWER);
				}
				break;
			case 37:
				enterOuterAlt(_localctx, 37);
				{
				setState(2312);
				match(LTRIM);
				}
				break;
			case 38:
				enterOuterAlt(_localctx, 38);
				{
				setState(2313);
				match(MINUTE);
				}
				break;
			case 39:
				enterOuterAlt(_localctx, 39);
				{
				setState(2314);
				match(MOD);
				}
				break;
			case 40:
				enterOuterAlt(_localctx, 40);
				{
				setState(2315);
				match(MONTH);
				}
				break;
			case 41:
				enterOuterAlt(_localctx, 41);
				{
				setState(2316);
				match(PI);
				}
				break;
			case 42:
				enterOuterAlt(_localctx, 42);
				{
				setState(2317);
				match(RADIANS);
				}
				break;
			case 43:
				enterOuterAlt(_localctx, 43);
				{
				setState(2318);
				match(RTRIM);
				}
				break;
			case 44:
				enterOuterAlt(_localctx, 44);
				{
				setState(2319);
				match(SECOND);
				}
				break;
			case 45:
				enterOuterAlt(_localctx, 45);
				{
				setState(2320);
				match(SESSION_USER);
				}
				break;
			case 46:
				enterOuterAlt(_localctx, 46);
				{
				setState(2321);
				match(SIN);
				}
				break;
			case 47:
				enterOuterAlt(_localctx, 47);
				{
				setState(2322);
				match(SMALLINT);
				}
				break;
			case 48:
				enterOuterAlt(_localctx, 48);
				{
				setState(2323);
				match(SQRT);
				}
				break;
			case 49:
				enterOuterAlt(_localctx, 49);
				{
				setState(2324);
				match(SUBSTR);
				}
				break;
			case 50:
				enterOuterAlt(_localctx, 50);
				{
				setState(2325);
				match(TAN);
				}
				break;
			case 51:
				enterOuterAlt(_localctx, 51);
				{
				setState(2326);
				match(TIME);
				}
				break;
			case 52:
				enterOuterAlt(_localctx, 52);
				{
				setState(2327);
				match(TIMESTAMP);
				}
				break;
			case 53:
				enterOuterAlt(_localctx, 53);
				{
				setState(2328);
				match(TRIM);
				}
				break;
			case 54:
				enterOuterAlt(_localctx, 54);
				{
				setState(2329);
				match(UCASE);
				}
				break;
			case 55:
				enterOuterAlt(_localctx, 55);
				{
				setState(2330);
				match(UPPER);
				}
				break;
			case 56:
				enterOuterAlt(_localctx, 56);
				{
				setState(2331);
				match(USER);
				}
				break;
			case 57:
				enterOuterAlt(_localctx, 57);
				{
				setState(2332);
				match(VARCHAR);
				}
				break;
			case 58:
				enterOuterAlt(_localctx, 58);
				{
				setState(2333);
				match(YEAR);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Aggreagate_built_in_functionContext extends ParserRuleContext {
		public TerminalNode AVG() { return getToken(DerbyParser.AVG, 0); }
		public TerminalNode COUNT() { return getToken(DerbyParser.COUNT, 0); }
		public TerminalNode MAX() { return getToken(DerbyParser.MAX, 0); }
		public TerminalNode MIN() { return getToken(DerbyParser.MIN, 0); }
		public TerminalNode STDDEV_POP() { return getToken(DerbyParser.STDDEV_POP, 0); }
		public TerminalNode STDDEV_SAMP() { return getToken(DerbyParser.STDDEV_SAMP, 0); }
		public TerminalNode SUM() { return getToken(DerbyParser.SUM, 0); }
		public TerminalNode VAR_POP() { return getToken(DerbyParser.VAR_POP, 0); }
		public TerminalNode VAR_SAMP() { return getToken(DerbyParser.VAR_SAMP, 0); }
		public Aggreagate_built_in_functionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggreagate_built_in_function; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterAggreagate_built_in_function(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitAggreagate_built_in_function(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitAggreagate_built_in_function(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Aggreagate_built_in_functionContext aggreagate_built_in_function() throws RecognitionException {
		Aggreagate_built_in_functionContext _localctx = new Aggreagate_built_in_functionContext(_ctx, getState());
		enterRule(_localctx, 286, RULE_aggreagate_built_in_function);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2336);
			_la = _input.LA(1);
			if ( !(_la==AVG || _la==COUNT || _la==MAX || _la==MIN || ((((_la - 272)) & ~0x3f) == 0 && ((1L << (_la - 272)) & 412316860451L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Select_statementContext extends ParserRuleContext {
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public Order_by_clauseContext order_by_clause() {
			return getRuleContext(Order_by_clauseContext.class,0);
		}
		public Offset_clauseContext offset_clause() {
			return getRuleContext(Offset_clauseContext.class,0);
		}
		public Fetch_clauseContext fetch_clause() {
			return getRuleContext(Fetch_clauseContext.class,0);
		}
		public For_update_clauseContext for_update_clause() {
			return getRuleContext(For_update_clauseContext.class,0);
		}
		public TerminalNode WITH() { return getToken(DerbyParser.WITH, 0); }
		public TerminalNode RR() { return getToken(DerbyParser.RR, 0); }
		public TerminalNode RS() { return getToken(DerbyParser.RS, 0); }
		public TerminalNode CS() { return getToken(DerbyParser.CS, 0); }
		public TerminalNode UR() { return getToken(DerbyParser.UR, 0); }
		public Select_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterSelect_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitSelect_statement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitSelect_statement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Select_statementContext select_statement() throws RecognitionException {
		Select_statementContext _localctx = new Select_statementContext(_ctx, getState());
		enterRule(_localctx, 288, RULE_select_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2338);
			query(0);
			setState(2340);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,231,_ctx) ) {
			case 1:
				{
				setState(2339);
				order_by_clause();
				}
				break;
			}
			setState(2343);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,232,_ctx) ) {
			case 1:
				{
				setState(2342);
				offset_clause();
				}
				break;
			}
			setState(2346);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,233,_ctx) ) {
			case 1:
				{
				setState(2345);
				fetch_clause();
				}
				break;
			}
			setState(2349);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,234,_ctx) ) {
			case 1:
				{
				setState(2348);
				for_update_clause();
				}
				break;
			}
			setState(2353);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,235,_ctx) ) {
			case 1:
				{
				setState(2351);
				match(WITH);
				setState(2352);
				_la = _input.LA(1);
				if ( !(_la==CS || ((((_la - 246)) & ~0x3f) == 0 && ((1L << (_la - 246)) & 288230376151711747L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class For_update_clauseContext extends ParserRuleContext {
		public TerminalNode FOR() { return getToken(DerbyParser.FOR, 0); }
		public TerminalNode READ() { return getToken(DerbyParser.READ, 0); }
		public TerminalNode ONLY() { return getToken(DerbyParser.ONLY, 0); }
		public TerminalNode FETCH() { return getToken(DerbyParser.FETCH, 0); }
		public TerminalNode UPDATE() { return getToken(DerbyParser.UPDATE, 0); }
		public TerminalNode OF() { return getToken(DerbyParser.OF, 0); }
		public List<Simple_column_nameContext> simple_column_name() {
			return getRuleContexts(Simple_column_nameContext.class);
		}
		public Simple_column_nameContext simple_column_name(int i) {
			return getRuleContext(Simple_column_nameContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public For_update_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_for_update_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterFor_update_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitFor_update_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitFor_update_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final For_update_clauseContext for_update_clause() throws RecognitionException {
		For_update_clauseContext _localctx = new For_update_clauseContext(_ctx, getState());
		enterRule(_localctx, 290, RULE_for_update_clause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2355);
			match(FOR);
			setState(2372);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,238,_ctx) ) {
			case 1:
				{
				setState(2356);
				match(READ);
				setState(2357);
				match(ONLY);
				}
				break;
			case 2:
				{
				setState(2358);
				match(FETCH);
				setState(2359);
				match(ONLY);
				}
				break;
			case 3:
				{
				setState(2360);
				match(UPDATE);
				setState(2370);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,237,_ctx) ) {
				case 1:
					{
					setState(2361);
					match(OF);
					setState(2362);
					simple_column_name();
					setState(2367);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,236,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(2363);
							match(COMMA);
							setState(2364);
							simple_column_name();
							}
							} 
						}
						setState(2369);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,236,_ctx);
					}
					}
					break;
				}
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class All_distinctContext extends ParserRuleContext {
		public TerminalNode ALL() { return getToken(DerbyParser.ALL, 0); }
		public TerminalNode DISTINCT() { return getToken(DerbyParser.DISTINCT, 0); }
		public All_distinctContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_all_distinct; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterAll_distinct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitAll_distinct(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitAll_distinct(this);
			else return visitor.visitChildren(this);
		}
	}

	public final All_distinctContext all_distinct() throws RecognitionException {
		All_distinctContext _localctx = new All_distinctContext(_ctx, getState());
		enterRule(_localctx, 292, RULE_all_distinct);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2374);
			_la = _input.LA(1);
			if ( !(_la==ALL || _la==DISTINCT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class From_clauseContext extends ParserRuleContext {
		public TerminalNode FROM() { return getToken(DerbyParser.FROM, 0); }
		public List<Table_expressionContext> table_expression() {
			return getRuleContexts(Table_expressionContext.class);
		}
		public Table_expressionContext table_expression(int i) {
			return getRuleContext(Table_expressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public From_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_from_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterFrom_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitFrom_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitFrom_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final From_clauseContext from_clause() throws RecognitionException {
		From_clauseContext _localctx = new From_clauseContext(_ctx, getState());
		enterRule(_localctx, 294, RULE_from_clause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2376);
			match(FROM);
			setState(2377);
			table_expression(0);
			setState(2382);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,239,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2378);
					match(COMMA);
					setState(2379);
					table_expression(0);
					}
					} 
				}
				setState(2384);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,239,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Table_expressionContext extends ParserRuleContext {
		public Table_view_or_function_expressionContext table_view_or_function_expression() {
			return getRuleContext(Table_view_or_function_expressionContext.class,0);
		}
		public Table_expressionContext table_expression() {
			return getRuleContext(Table_expressionContext.class,0);
		}
		public Join_operationContext join_operation() {
			return getRuleContext(Join_operationContext.class,0);
		}
		public Table_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterTable_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitTable_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitTable_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Table_expressionContext table_expression() throws RecognitionException {
		return table_expression(0);
	}

	private Table_expressionContext table_expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Table_expressionContext _localctx = new Table_expressionContext(_ctx, _parentState);
		Table_expressionContext _prevctx = _localctx;
		int _startState = 296;
		enterRecursionRule(_localctx, 296, RULE_table_expression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(2386);
			table_view_or_function_expression();
			}
			_ctx.stop = _input.LT(-1);
			setState(2392);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,240,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Table_expressionContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_table_expression);
					setState(2388);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(2389);
					join_operation();
					}
					} 
				}
				setState(2394);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,240,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Join_operationContext extends ParserRuleContext {
		public TerminalNode JOIN() { return getToken(DerbyParser.JOIN, 0); }
		public Table_expressionContext table_expression() {
			return getRuleContext(Table_expressionContext.class,0);
		}
		public TerminalNode ON() { return getToken(DerbyParser.ON, 0); }
		public Boolean_expressionContext boolean_expression() {
			return getRuleContext(Boolean_expressionContext.class,0);
		}
		public Using_clauseContext using_clause() {
			return getRuleContext(Using_clauseContext.class,0);
		}
		public TerminalNode INNER() { return getToken(DerbyParser.INNER, 0); }
		public TerminalNode LEFT() { return getToken(DerbyParser.LEFT, 0); }
		public TerminalNode OUTER() { return getToken(DerbyParser.OUTER, 0); }
		public TerminalNode RIGHT() { return getToken(DerbyParser.RIGHT, 0); }
		public TerminalNode CROSS() { return getToken(DerbyParser.CROSS, 0); }
		public Table_view_or_function_expressionContext table_view_or_function_expression() {
			return getRuleContext(Table_view_or_function_expressionContext.class,0);
		}
		public TerminalNode NATURAL() { return getToken(DerbyParser.NATURAL, 0); }
		public Join_operationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_join_operation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterJoin_operation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitJoin_operation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitJoin_operation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Join_operationContext join_operation() throws RecognitionException {
		Join_operationContext _localctx = new Join_operationContext(_ctx, getState());
		enterRule(_localctx, 298, RULE_join_operation);
		int _la;
		try {
			setState(2446);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,251,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2396);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,241,_ctx) ) {
				case 1:
					{
					setState(2395);
					match(INNER);
					}
					break;
				}
				setState(2398);
				match(JOIN);
				setState(2399);
				table_expression(0);
				setState(2403);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,242,_ctx) ) {
				case 1:
					{
					setState(2400);
					match(ON);
					setState(2401);
					boolean_expression();
					}
					break;
				case 2:
					{
					setState(2402);
					using_clause();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2405);
				match(LEFT);
				setState(2407);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,243,_ctx) ) {
				case 1:
					{
					setState(2406);
					match(OUTER);
					}
					break;
				}
				setState(2409);
				match(JOIN);
				setState(2410);
				table_expression(0);
				setState(2414);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,244,_ctx) ) {
				case 1:
					{
					setState(2411);
					match(ON);
					setState(2412);
					boolean_expression();
					}
					break;
				case 2:
					{
					setState(2413);
					using_clause();
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(2416);
				match(RIGHT);
				setState(2418);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,245,_ctx) ) {
				case 1:
					{
					setState(2417);
					match(OUTER);
					}
					break;
				}
				setState(2420);
				match(JOIN);
				setState(2421);
				table_expression(0);
				setState(2425);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,246,_ctx) ) {
				case 1:
					{
					setState(2422);
					match(ON);
					setState(2423);
					boolean_expression();
					}
					break;
				case 2:
					{
					setState(2424);
					using_clause();
					}
					break;
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(2427);
				match(CROSS);
				setState(2428);
				match(JOIN);
				setState(2431);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,247,_ctx) ) {
				case 1:
					{
					setState(2429);
					table_view_or_function_expression();
					}
					break;
				case 2:
					{
					setState(2430);
					table_expression(0);
					}
					break;
				}
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(2433);
				match(NATURAL);
				setState(2439);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,249,_ctx) ) {
				case 1:
					{
					setState(2434);
					_la = _input.LA(1);
					if ( !(_la==LEFT || _la==RIGHT) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(2436);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,248,_ctx) ) {
					case 1:
						{
						setState(2435);
						match(OUTER);
						}
						break;
					}
					}
					break;
				case 2:
					{
					setState(2438);
					match(INNER);
					}
					break;
				}
				setState(2441);
				match(JOIN);
				setState(2444);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,250,_ctx) ) {
				case 1:
					{
					setState(2442);
					table_view_or_function_expression();
					}
					break;
				case 2:
					{
					setState(2443);
					table_expression(0);
					}
					break;
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Table_view_or_function_expressionContext extends ParserRuleContext {
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public View_nameContext view_name() {
			return getRuleContext(View_nameContext.class,0);
		}
		public Correlation_clauseContext correlation_clause() {
			return getRuleContext(Correlation_clauseContext.class,0);
		}
		public Table_subqueryContext table_subquery() {
			return getRuleContext(Table_subqueryContext.class,0);
		}
		public Table_function_invocationContext table_function_invocation() {
			return getRuleContext(Table_function_invocationContext.class,0);
		}
		public Table_view_or_function_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_view_or_function_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterTable_view_or_function_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitTable_view_or_function_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitTable_view_or_function_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Table_view_or_function_expressionContext table_view_or_function_expression() throws RecognitionException {
		Table_view_or_function_expressionContext _localctx = new Table_view_or_function_expressionContext(_ctx, getState());
		enterRule(_localctx, 300, RULE_table_view_or_function_expression);
		try {
			setState(2461);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,255,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(2450);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,252,_ctx) ) {
				case 1:
					{
					setState(2448);
					table_name();
					}
					break;
				case 2:
					{
					setState(2449);
					view_name();
					}
					break;
				}
				setState(2453);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,253,_ctx) ) {
				case 1:
					{
					setState(2452);
					correlation_clause();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(2457);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,254,_ctx) ) {
				case 1:
					{
					setState(2455);
					table_subquery();
					}
					break;
				case 2:
					{
					setState(2456);
					table_function_invocation();
					}
					break;
				}
				setState(2459);
				correlation_clause();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Using_clauseContext extends ParserRuleContext {
		public TerminalNode USING() { return getToken(DerbyParser.USING, 0); }
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public List<Simple_column_nameContext> simple_column_name() {
			return getRuleContexts(Simple_column_nameContext.class);
		}
		public Simple_column_nameContext simple_column_name(int i) {
			return getRuleContext(Simple_column_nameContext.class,i);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Using_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_using_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterUsing_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitUsing_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitUsing_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Using_clauseContext using_clause() throws RecognitionException {
		Using_clauseContext _localctx = new Using_clauseContext(_ctx, getState());
		enterRule(_localctx, 302, RULE_using_clause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2463);
			match(USING);
			setState(2464);
			match(LR_BRACKET);
			setState(2465);
			simple_column_name();
			setState(2470);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,256,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2466);
					match(COMMA);
					setState(2467);
					simple_column_name();
					}
					} 
				}
				setState(2472);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,256,_ctx);
			}
			setState(2473);
			match(RR_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Correlation_clauseContext extends ParserRuleContext {
		public Correlation_nameContext correlation_name() {
			return getRuleContext(Correlation_nameContext.class,0);
		}
		public TerminalNode AS() { return getToken(DerbyParser.AS, 0); }
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public List<Simple_column_nameContext> simple_column_name() {
			return getRuleContexts(Simple_column_nameContext.class);
		}
		public Simple_column_nameContext simple_column_name(int i) {
			return getRuleContext(Simple_column_nameContext.class,i);
		}
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Correlation_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_correlation_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterCorrelation_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitCorrelation_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitCorrelation_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Correlation_clauseContext correlation_clause() throws RecognitionException {
		Correlation_clauseContext _localctx = new Correlation_clauseContext(_ctx, getState());
		enterRule(_localctx, 304, RULE_correlation_clause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2476);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,257,_ctx) ) {
			case 1:
				{
				setState(2475);
				match(AS);
				}
				break;
			}
			setState(2478);
			correlation_name();
			setState(2490);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,259,_ctx) ) {
			case 1:
				{
				setState(2479);
				match(LR_BRACKET);
				setState(2480);
				simple_column_name();
				setState(2485);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,258,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2481);
						match(COMMA);
						setState(2482);
						simple_column_name();
						}
						} 
					}
					setState(2487);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,258,_ctx);
				}
				setState(2488);
				match(RR_BRACKET);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Table_function_invocationContext extends ParserRuleContext {
		public TerminalNode TABLE() { return getToken(DerbyParser.TABLE, 0); }
		public Function_nameContext function_name() {
			return getRuleContext(Function_nameContext.class,0);
		}
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public List<Function_argContext> function_arg() {
			return getRuleContexts(Function_argContext.class);
		}
		public Function_argContext function_arg(int i) {
			return getRuleContext(Function_argContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Table_function_invocationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_function_invocation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterTable_function_invocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitTable_function_invocation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitTable_function_invocation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Table_function_invocationContext table_function_invocation() throws RecognitionException {
		Table_function_invocationContext _localctx = new Table_function_invocationContext(_ctx, getState());
		enterRule(_localctx, 306, RULE_table_function_invocation);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2492);
			match(TABLE);
			setState(2493);
			function_name();
			setState(2494);
			match(LR_BRACKET);
			setState(2503);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,261,_ctx) ) {
			case 1:
				{
				setState(2495);
				function_arg();
				setState(2500);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,260,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2496);
						match(COMMA);
						setState(2497);
						function_arg();
						}
						} 
					}
					setState(2502);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,260,_ctx);
				}
				}
				break;
			}
			setState(2505);
			match(RR_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Function_argContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Function_argContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function_arg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterFunction_arg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitFunction_arg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitFunction_arg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Function_argContext function_arg() throws RecognitionException {
		Function_argContext _localctx = new Function_argContext(_ctx, getState());
		enterRule(_localctx, 308, RULE_function_arg);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2507);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Group_by_clauseContext extends ParserRuleContext {
		public TerminalNode GROUP() { return getToken(DerbyParser.GROUP, 0); }
		public TerminalNode BY() { return getToken(DerbyParser.BY, 0); }
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public TerminalNode ROLLUP() { return getToken(DerbyParser.ROLLUP, 0); }
		public TerminalNode LR_BRACKET() { return getToken(DerbyParser.LR_BRACKET, 0); }
		public TerminalNode RR_BRACKET() { return getToken(DerbyParser.RR_BRACKET, 0); }
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Group_by_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_group_by_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterGroup_by_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitGroup_by_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitGroup_by_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Group_by_clauseContext group_by_clause() throws RecognitionException {
		Group_by_clauseContext _localctx = new Group_by_clauseContext(_ctx, getState());
		enterRule(_localctx, 310, RULE_group_by_clause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2509);
			match(GROUP);
			setState(2510);
			match(BY);
			setState(2531);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,264,_ctx) ) {
			case 1:
				{
				setState(2511);
				column_name();
				setState(2516);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,262,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2512);
						match(COMMA);
						setState(2513);
						column_name();
						}
						} 
					}
					setState(2518);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,262,_ctx);
				}
				}
				break;
			case 2:
				{
				setState(2519);
				match(ROLLUP);
				setState(2520);
				match(LR_BRACKET);
				setState(2521);
				column_name();
				setState(2526);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,263,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2522);
						match(COMMA);
						setState(2523);
						column_name();
						}
						} 
					}
					setState(2528);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,263,_ctx);
				}
				setState(2529);
				match(RR_BRACKET);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Having_clauseContext extends ParserRuleContext {
		public TerminalNode HAVING() { return getToken(DerbyParser.HAVING, 0); }
		public Boolean_expressionContext boolean_expression() {
			return getRuleContext(Boolean_expressionContext.class,0);
		}
		public Having_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_having_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterHaving_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitHaving_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitHaving_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Having_clauseContext having_clause() throws RecognitionException {
		Having_clauseContext _localctx = new Having_clauseContext(_ctx, getState());
		enterRule(_localctx, 312, RULE_having_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2533);
			match(HAVING);
			setState(2534);
			boolean_expression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Order_by_clauseContext extends ParserRuleContext {
		public TerminalNode ORDER() { return getToken(DerbyParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(DerbyParser.BY, 0); }
		public List<Order_by_itemContext> order_by_item() {
			return getRuleContexts(Order_by_itemContext.class);
		}
		public Order_by_itemContext order_by_item(int i) {
			return getRuleContext(Order_by_itemContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Order_by_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_order_by_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterOrder_by_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitOrder_by_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitOrder_by_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Order_by_clauseContext order_by_clause() throws RecognitionException {
		Order_by_clauseContext _localctx = new Order_by_clauseContext(_ctx, getState());
		enterRule(_localctx, 314, RULE_order_by_clause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2536);
			match(ORDER);
			setState(2537);
			match(BY);
			setState(2538);
			order_by_item();
			setState(2543);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,265,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(2539);
					match(COMMA);
					setState(2540);
					order_by_item();
					}
					} 
				}
				setState(2545);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,265,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Order_by_itemContext extends ParserRuleContext {
		public Column_nameContext column_name() {
			return getRuleContext(Column_nameContext.class,0);
		}
		public ColumnPositionContext columnPosition() {
			return getRuleContext(ColumnPositionContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Asc_descContext asc_desc() {
			return getRuleContext(Asc_descContext.class,0);
		}
		public TerminalNode NULLS() { return getToken(DerbyParser.NULLS, 0); }
		public TerminalNode FIRST() { return getToken(DerbyParser.FIRST, 0); }
		public TerminalNode LAST() { return getToken(DerbyParser.LAST, 0); }
		public Order_by_itemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_order_by_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterOrder_by_item(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitOrder_by_item(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitOrder_by_item(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Order_by_itemContext order_by_item() throws RecognitionException {
		Order_by_itemContext _localctx = new Order_by_itemContext(_ctx, getState());
		enterRule(_localctx, 316, RULE_order_by_item);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2549);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,266,_ctx) ) {
			case 1:
				{
				setState(2546);
				column_name();
				}
				break;
			case 2:
				{
				setState(2547);
				columnPosition();
				}
				break;
			case 3:
				{
				setState(2548);
				expression(0);
				}
				break;
			}
			setState(2552);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,267,_ctx) ) {
			case 1:
				{
				setState(2551);
				asc_desc();
				}
				break;
			}
			setState(2558);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,268,_ctx) ) {
			case 1:
				{
				setState(2554);
				match(NULLS);
				setState(2555);
				match(FIRST);
				}
				break;
			case 2:
				{
				setState(2556);
				match(NULLS);
				setState(2557);
				match(LAST);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Asc_descContext extends ParserRuleContext {
		public TerminalNode ASC() { return getToken(DerbyParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(DerbyParser.DESC, 0); }
		public Asc_descContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asc_desc; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterAsc_desc(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitAsc_desc(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitAsc_desc(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Asc_descContext asc_desc() throws RecognitionException {
		Asc_descContext _localctx = new Asc_descContext(_ctx, getState());
		enterRule(_localctx, 318, RULE_asc_desc);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2560);
			_la = _input.LA(1);
			if ( !(_la==ASC || _la==DESC) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ColumnPositionContext extends ParserRuleContext {
		public NumContext num() {
			return getRuleContext(NumContext.class,0);
		}
		public ColumnPositionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnPosition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterColumnPosition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitColumnPosition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitColumnPosition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnPositionContext columnPosition() throws RecognitionException {
		ColumnPositionContext _localctx = new ColumnPositionContext(_ctx, getState());
		enterRule(_localctx, 320, RULE_columnPosition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(2562);
			num();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Values_expressionContext extends ParserRuleContext {
		public TerminalNode VALUES() { return getToken(DerbyParser.VALUES, 0); }
		public List<TerminalNode> LR_BRACKET() { return getTokens(DerbyParser.LR_BRACKET); }
		public TerminalNode LR_BRACKET(int i) {
			return getToken(DerbyParser.LR_BRACKET, i);
		}
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public List<TerminalNode> RR_BRACKET() { return getTokens(DerbyParser.RR_BRACKET); }
		public TerminalNode RR_BRACKET(int i) {
			return getToken(DerbyParser.RR_BRACKET, i);
		}
		public Order_by_clauseContext order_by_clause() {
			return getRuleContext(Order_by_clauseContext.class,0);
		}
		public Offset_clauseContext offset_clause() {
			return getRuleContext(Offset_clauseContext.class,0);
		}
		public Fetch_clauseContext fetch_clause() {
			return getRuleContext(Fetch_clauseContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(DerbyParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(DerbyParser.COMMA, i);
		}
		public Values_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_values_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).enterValues_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DerbyParserListener ) ((DerbyParserListener)listener).exitValues_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DerbyParserVisitor ) return ((DerbyParserVisitor<? extends T>)visitor).visitValues_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Values_expressionContext values_expression() throws RecognitionException {
		Values_expressionContext _localctx = new Values_expressionContext(_ctx, getState());
		enterRule(_localctx, 322, RULE_values_expression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(2601);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,273,_ctx) ) {
			case 1:
				{
				setState(2564);
				match(VALUES);
				setState(2565);
				match(LR_BRACKET);
				setState(2566);
				value();
				setState(2571);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,269,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2567);
						match(COMMA);
						setState(2568);
						value();
						}
						} 
					}
					setState(2573);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,269,_ctx);
				}
				setState(2574);
				match(RR_BRACKET);
				setState(2589);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,271,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2575);
						match(COMMA);
						setState(2576);
						match(LR_BRACKET);
						setState(2577);
						value();
						setState(2582);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,270,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(2578);
								match(COMMA);
								setState(2579);
								value();
								}
								} 
							}
							setState(2584);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,270,_ctx);
						}
						setState(2585);
						match(RR_BRACKET);
						}
						} 
					}
					setState(2591);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,271,_ctx);
				}
				}
				break;
			case 2:
				{
				setState(2592);
				match(VALUES);
				setState(2593);
				value();
				setState(2598);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,272,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(2594);
						match(COMMA);
						setState(2595);
						value();
						}
						} 
					}
					setState(2600);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,272,_ctx);
				}
				}
				break;
			}
			setState(2604);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,274,_ctx) ) {
			case 1:
				{
				setState(2603);
				order_by_clause();
				}
				break;
			}
			setState(2607);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,275,_ctx) ) {
			case 1:
				{
				setState(2606);
				offset_clause();
				}
				break;
			}
			setState(2610);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,276,_ctx) ) {
			case 1:
				{
				setState(2609);
				fetch_clause();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 92:
			return query_sempred((QueryContext)_localctx, predIndex);
		case 131:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		case 148:
			return table_expression_sempred((Table_expressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean query_sempred(QueryContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 5);
		case 1:
			return precpred(_ctx, 4);
		case 2:
			return precpred(_ctx, 3);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3:
			return precpred(_ctx, 9);
		case 4:
			return precpred(_ctx, 8);
		case 5:
			return precpred(_ctx, 7);
		case 6:
			return precpred(_ctx, 6);
		case 7:
			return precpred(_ctx, 5);
		case 8:
			return precpred(_ctx, 4);
		case 9:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean table_expression_sempred(Table_expressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 10:
			return precpred(_ctx, 1);
		}
		return true;
	}

	private static final String _serializedATNSegment0 =
		"\u0004\u0001\u0166\u0a35\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007"+
		"\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007"+
		"\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007"+
		"\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007"+
		"\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007"+
		"\u001e\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007"+
		"\"\u0002#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007"+
		"\'\u0002(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007"+
		",\u0002-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u00021\u0007"+
		"1\u00022\u00072\u00023\u00073\u00024\u00074\u00025\u00075\u00026\u0007"+
		"6\u00027\u00077\u00028\u00078\u00029\u00079\u0002:\u0007:\u0002;\u0007"+
		";\u0002<\u0007<\u0002=\u0007=\u0002>\u0007>\u0002?\u0007?\u0002@\u0007"+
		"@\u0002A\u0007A\u0002B\u0007B\u0002C\u0007C\u0002D\u0007D\u0002E\u0007"+
		"E\u0002F\u0007F\u0002G\u0007G\u0002H\u0007H\u0002I\u0007I\u0002J\u0007"+
		"J\u0002K\u0007K\u0002L\u0007L\u0002M\u0007M\u0002N\u0007N\u0002O\u0007"+
		"O\u0002P\u0007P\u0002Q\u0007Q\u0002R\u0007R\u0002S\u0007S\u0002T\u0007"+
		"T\u0002U\u0007U\u0002V\u0007V\u0002W\u0007W\u0002X\u0007X\u0002Y\u0007"+
		"Y\u0002Z\u0007Z\u0002[\u0007[\u0002\\\u0007\\\u0002]\u0007]\u0002^\u0007"+
		"^\u0002_\u0007_\u0002`\u0007`\u0002a\u0007a\u0002b\u0007b\u0002c\u0007"+
		"c\u0002d\u0007d\u0002e\u0007e\u0002f\u0007f\u0002g\u0007g\u0002h\u0007"+
		"h\u0002i\u0007i\u0002j\u0007j\u0002k\u0007k\u0002l\u0007l\u0002m\u0007"+
		"m\u0002n\u0007n\u0002o\u0007o\u0002p\u0007p\u0002q\u0007q\u0002r\u0007"+
		"r\u0002s\u0007s\u0002t\u0007t\u0002u\u0007u\u0002v\u0007v\u0002w\u0007"+
		"w\u0002x\u0007x\u0002y\u0007y\u0002z\u0007z\u0002{\u0007{\u0002|\u0007"+
		"|\u0002}\u0007}\u0002~\u0007~\u0002\u007f\u0007\u007f\u0002\u0080\u0007"+
		"\u0080\u0002\u0081\u0007\u0081\u0002\u0082\u0007\u0082\u0002\u0083\u0007"+
		"\u0083\u0002\u0084\u0007\u0084\u0002\u0085\u0007\u0085\u0002\u0086\u0007"+
		"\u0086\u0002\u0087\u0007\u0087\u0002\u0088\u0007\u0088\u0002\u0089\u0007"+
		"\u0089\u0002\u008a\u0007\u008a\u0002\u008b\u0007\u008b\u0002\u008c\u0007"+
		"\u008c\u0002\u008d\u0007\u008d\u0002\u008e\u0007\u008e\u0002\u008f\u0007"+
		"\u008f\u0002\u0090\u0007\u0090\u0002\u0091\u0007\u0091\u0002\u0092\u0007"+
		"\u0092\u0002\u0093\u0007\u0093\u0002\u0094\u0007\u0094\u0002\u0095\u0007"+
		"\u0095\u0002\u0096\u0007\u0096\u0002\u0097\u0007\u0097\u0002\u0098\u0007"+
		"\u0098\u0002\u0099\u0007\u0099\u0002\u009a\u0007\u009a\u0002\u009b\u0007"+
		"\u009b\u0002\u009c\u0007\u009c\u0002\u009d\u0007\u009d\u0002\u009e\u0007"+
		"\u009e\u0002\u009f\u0007\u009f\u0002\u00a0\u0007\u00a0\u0002\u00a1\u0007"+
		"\u00a1\u0001\u0000\u0005\u0000\u0146\b\u0000\n\u0000\f\u0000\u0149\t\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0003\u0001\u014f\b\u0001"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002\u0154\b\u0002\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0003\u0003\u0159\b\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004\u0161\b\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006\u016c\b\u0006\n\u0006"+
		"\f\u0006\u016f\t\u0006\u0001\u0006\u0001\u0006\u0005\u0006\u0173\b\u0006"+
		"\n\u0006\f\u0006\u0176\t\u0006\u0001\u0006\u0001\u0006\u0003\u0006\u017a"+
		"\b\u0006\u0001\u0006\u0003\u0006\u017d\b\u0006\u0001\u0006\u0003\u0006"+
		"\u0180\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0185\b"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0003\b"+
		"\u018d\b\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001\n\u0001"+
		"\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u019c\b\f\u0001"+
		"\f\u0003\f\u019f\b\f\u0001\f\u0001\f\u0001\f\u0003\f\u01a4\b\f\u0001\f"+
		"\u0003\f\u01a7\b\f\u0001\f\u0001\f\u0001\f\u0005\f\u01ac\b\f\n\f\f\f\u01af"+
		"\t\f\u0001\r\u0001\r\u0003\r\u01b3\b\r\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0003\u000e\u01b9\b\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0003\u000e\u01be\b\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0005\u0010\u01d3\b\u0010\n\u0010"+
		"\f\u0010\u01d6\t\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0005\u0011\u01dd\b\u0011\n\u0011\f\u0011\u01e0\t\u0011\u0001"+
		"\u0011\u0001\u0011\u0003\u0011\u01e4\b\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u01eb\b\u0011\n\u0011\f\u0011"+
		"\u01ee\t\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0013"+
		"\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u01f9\b\u0014"+
		"\u0001\u0014\u0003\u0014\u01fc\b\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0005\u0014\u0207\b\u0014\n\u0014\f\u0014\u020a\t\u0014\u0001\u0014\u0003"+
		"\u0014\u020d\b\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0005\u0014\u021a\b\u0014\n\u0014\f\u0014\u021d\t\u0014\u0001\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u0224\b\u0014"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0003\u0016\u023a\b\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0003\u0016\u0240\b\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0003\u0016\u0246\b\u0016\u0001\u0016\u0001\u0016"+
		"\u0003\u0016\u024a\b\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u0253\b\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u025a\b\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0005\u0016"+
		"\u0261\b\u0016\n\u0016\f\u0016\u0264\t\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0003\u0016\u0269\b\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u0272\b\u0016\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u0277\b\u0017\u0001\u0017\u0003"+
		"\u0017\u027a\b\u0017\u0001\u0017\u0003\u0017\u027d\b\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u001a"+
		"\u0001\u001a\u0003\u001a\u0287\b\u001a\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0003\u001b\u028d\b\u001b\u0001\u001b\u0003\u001b\u0290\b"+
		"\u001b\u0001\u001b\u0003\u001b\u0293\b\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0003"+
		"\u001b\u029d\b\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0005\u001c\u02ad\b\u001c\n"+
		"\u001c\f\u001c\u02b0\t\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0003\u001c\u02b6\b\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0003\u001c\u02be\b\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0003\u001c\u02c4\b\u001c\u0001\u001c\u0001"+
		"\u001c\u0003\u001c\u02c8\b\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0005\u001c\u02fa\b\u001c\n\u001c\f\u001c"+
		"\u02fd\t\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0003\u001c\u0323\b\u001c\u0001\u001c\u0001\u001c\u0003\u001c"+
		"\u0327\b\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0005\u001c\u035e\b\u001c\n\u001c\f\u001c\u0361\t\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0003\u001c\u036d\b\u001c\u0001\u001c\u0001"+
		"\u001c\u0003\u001c\u0371\b\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0003\u001c\u037a\b\u001c\u0001"+
		"\u001c\u0001\u001c\u0003\u001c\u037e\b\u001c\u0001\u001c\u0001\u001c\u0003"+
		"\u001c\u0382\b\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0003"+
		"\u001c\u0388\b\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0003"+
		"\u001c\u038e\b\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0003"+
		"\u001c\u0394\b\u001c\u0003\u001c\u0396\b\u001c\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0005\u001d\u039b\b\u001d\n\u001d\f\u001d\u039e\t\u001d\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0003\u001e\u03a3\b\u001e\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0003\u001f\u03b5\b\u001f\u0001 \u0001"+
		" \u0001 \u0001 \u0005 \u03bb\b \n \f \u03be\t \u0003 \u03c0\b \u0001!"+
		"\u0001!\u0003!\u03c4\b!\u0001!\u0005!\u03c7\b!\n!\f!\u03ca\t!\u0001\""+
		"\u0003\"\u03cd\b\"\u0001\"\u0001\"\u0001\"\u0001\"\u0003\"\u03d3\b\"\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0003"+
		"#\u03e9\b#\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001"+
		"$\u0001$\u0001$\u0001$\u0003$\u03f7\b$\u0001$\u0001$\u0003$\u03fb\b$\u0001"+
		"%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001&\u0001&\u0003&\u0406"+
		"\b&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001&\u0001"+
		"&\u0001&\u0003&\u0413\b&\u0001&\u0003&\u0416\b&\u0001\'\u0001\'\u0003"+
		"\'\u041a\b\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001"+
		"\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001"+
		"\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0001\'\u0003\'\u0433\b\'\u0003"+
		"\'\u0435\b\'\u0001\'\u0003\'\u0438\b\'\u0001(\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0003(\u0440\b(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0003(\u044a\b(\u0003(\u044c\b(\u0001(\u0001(\u0001(\u0003(\u0451"+
		"\b(\u0001(\u0001(\u0001(\u0003(\u0456\b(\u0001(\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0003(\u0460\b(\u0003(\u0462\b(\u0003(\u0464\b"+
		"(\u0001)\u0001)\u0001)\u0003)\u0469\b)\u0001*\u0001*\u0003*\u046d\b*\u0001"+
		"*\u0003*\u0470\b*\u0001*\u0003*\u0473\b*\u0001*\u0001*\u0003*\u0477\b"+
		"*\u0003*\u0479\b*\u0001+\u0001+\u0001+\u0001+\u0003+\u047f\b+\u0001,\u0001"+
		",\u0001,\u0005,\u0484\b,\n,\f,\u0487\t,\u0001-\u0001-\u0003-\u048b\b-"+
		"\u0001-\u0001-\u0001.\u0001.\u0001.\u0003.\u0492\b.\u0001/\u0001/\u0001"+
		"/\u0005/\u0497\b/\n/\f/\u049a\t/\u00010\u00010\u00010\u00010\u00030\u04a0"+
		"\b0\u00010\u00010\u00030\u04a4\b0\u00010\u00010\u00010\u00030\u04a9\b"+
		"0\u00030\u04ab\b0\u00011\u00011\u00011\u00011\u00051\u04b1\b1\n1\f1\u04b4"+
		"\t1\u00011\u00011\u00012\u00012\u00013\u00013\u00013\u00053\u04bd\b3\n"+
		"3\f3\u04c0\t3\u00014\u00014\u00014\u00034\u04c5\b4\u00015\u00015\u0001"+
		"6\u00016\u00017\u00017\u00017\u00017\u00017\u00017\u00017\u00017\u0001"+
		"7\u00017\u00017\u00037\u04d6\b7\u00017\u00017\u00037\u04da\b7\u00017\u0001"+
		"7\u00017\u00017\u00017\u00017\u00017\u00017\u00017\u00017\u00017\u0001"+
		"7\u00037\u04e8\b7\u00017\u00017\u00037\u04ec\b7\u00017\u00017\u00017\u0003"+
		"7\u04f1\b7\u00018\u00018\u00038\u04f5\b8\u00019\u00019\u0001:\u0001:\u0001"+
		";\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001"+
		";\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001"+
		";\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001"+
		";\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001"+
		";\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001"+
		";\u0001;\u0001;\u0001;\u0003;\u0531\b;\u0001;\u0001;\u0001;\u0001;\u0003"+
		";\u0537\b;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0003;\u053f\b;\u0001"+
		";\u0001;\u0001;\u0001;\u0001;\u0001;\u0003;\u0547\b;\u0001<\u0001<\u0001"+
		"=\u0001=\u0001>\u0001>\u0001?\u0001?\u0001?\u0003?\u0552\b?\u0001@\u0001"+
		"@\u0001@\u0003@\u0557\b@\u0001A\u0001A\u0001B\u0001B\u0001C\u0001C\u0001"+
		"C\u0001C\u0001C\u0001C\u0001C\u0001C\u0001C\u0001C\u0001C\u0001C\u0003"+
		"C\u0569\bC\u0001D\u0001D\u0001D\u0001D\u0001D\u0001D\u0001D\u0001D\u0001"+
		"D\u0001D\u0001D\u0001D\u0001D\u0001E\u0001E\u0001F\u0001F\u0001F\u0001"+
		"F\u0001F\u0001F\u0001F\u0005F\u0581\bF\nF\fF\u0584\tF\u0001F\u0001F\u0001"+
		"F\u0001F\u0005F\u058a\bF\nF\fF\u058d\tF\u0001G\u0003G\u0590\bG\u0001G"+
		"\u0001G\u0001H\u0001H\u0003H\u0596\bH\u0001I\u0001I\u0001I\u0001I\u0001"+
		"I\u0001I\u0001I\u0001I\u0001J\u0001J\u0001J\u0001K\u0001K\u0001K\u0001"+
		"K\u0001K\u0003K\u05a8\bK\u0001K\u0001K\u0001K\u0001K\u0001K\u0001K\u0001"+
		"K\u0001K\u0001K\u0001K\u0001K\u0001K\u0001K\u0001K\u0001K\u0001K\u0003"+
		"K\u05ba\bK\u0001K\u0001K\u0001K\u0001K\u0001K\u0001K\u0001K\u0001K\u0001"+
		"K\u0003K\u05c5\bK\u0003K\u05c7\bK\u0001L\u0001L\u0003L\u05cb\bL\u0001"+
		"L\u0001L\u0001L\u0001L\u0001L\u0001L\u0001L\u0003L\u05d4\bL\u0001L\u0001"+
		"L\u0001L\u0003L\u05d9\bL\u0005L\u05db\bL\nL\fL\u05de\tL\u0001L\u0001L"+
		"\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0001M\u0005M\u05e9\bM\nM\f"+
		"M\u05ec\tM\u0001M\u0001M\u0005M\u05f0\bM\nM\fM\u05f3\tM\u0001N\u0003N"+
		"\u05f6\bN\u0001N\u0003N\u05f9\bN\u0001N\u0001N\u0001O\u0001O\u0001O\u0001"+
		"O\u0001O\u0003O\u0602\bO\u0001O\u0001O\u0001O\u0003O\u0607\bO\u0001O\u0001"+
		"O\u0003O\u060b\bO\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0003O\u0613"+
		"\bO\u0001O\u0001O\u0001O\u0001O\u0003O\u0619\bO\u0001O\u0001O\u0001O\u0003"+
		"O\u061e\bO\u0001O\u0001O\u0003O\u0622\bO\u0001O\u0001O\u0001O\u0001O\u0001"+
		"O\u0001O\u0003O\u062a\bO\u0001O\u0001O\u0003O\u062e\bO\u0001O\u0001O\u0001"+
		"O\u0001O\u0001O\u0001O\u0001O\u0001O\u0003O\u0638\bO\u0001O\u0001O\u0001"+
		"O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001"+
		"O\u0001O\u0001O\u0001O\u0001O\u0003O\u064b\bO\u0001O\u0001O\u0001O\u0001"+
		"O\u0003O\u0651\bO\u0001O\u0003O\u0654\bO\u0001P\u0003P\u0657\bP\u0001"+
		"P\u0001P\u0001P\u0001P\u0001P\u0001P\u0001P\u0001P\u0003P\u0661\bP\u0001"+
		"P\u0001P\u0001P\u0001P\u0001P\u0001P\u0001P\u0001P\u0001P\u0001P\u0001"+
		"P\u0001P\u0001P\u0001P\u0001P\u0001P\u0001P\u0001P\u0001P\u0003P\u0676"+
		"\bP\u0003P\u0678\bP\u0001Q\u0001Q\u0001Q\u0001Q\u0001R\u0001R\u0001R\u0001"+
		"R\u0001R\u0001R\u0001R\u0001R\u0001R\u0003R\u0687\bR\u0001S\u0001S\u0001"+
		"S\u0001S\u0001S\u0005S\u068e\bS\nS\fS\u0691\tS\u0001T\u0001T\u0001T\u0001"+
		"T\u0001T\u0001T\u0001T\u0001T\u0001T\u0001T\u0001T\u0001T\u0001T\u0001"+
		"T\u0001T\u0001T\u0001T\u0001T\u0001T\u0003T\u06a6\bT\u0001U\u0001U\u0001"+
		"V\u0001V\u0001V\u0001V\u0001V\u0001V\u0003V\u06b0\bV\u0001W\u0001W\u0001"+
		"W\u0001W\u0001W\u0001W\u0001W\u0001X\u0001X\u0001X\u0001X\u0001Y\u0001"+
		"Y\u0001Y\u0001Y\u0001Y\u0001Y\u0001Y\u0001Y\u0001Y\u0001Z\u0001Z\u0001"+
		"[\u0001[\u0001[\u0001[\u0001[\u0001[\u0001[\u0005[\u06cf\b[\n[\f[\u06d2"+
		"\t[\u0001[\u0001[\u0003[\u06d6\b[\u0001[\u0001[\u0001[\u0003[\u06db\b"+
		"[\u0001[\u0003[\u06de\b[\u0001[\u0003[\u06e1\b[\u0001\\\u0001\\\u0001"+
		"\\\u0001\\\u0003\\\u06e7\b\\\u0001\\\u0003\\\u06ea\b\\\u0001\\\u0003\\"+
		"\u06ed\b\\\u0001\\\u0001\\\u0001\\\u0001\\\u0003\\\u06f3\b\\\u0001\\\u0001"+
		"\\\u0001\\\u0003\\\u06f8\b\\\u0001\\\u0001\\\u0001\\\u0001\\\u0003\\\u06fe"+
		"\b\\\u0001\\\u0001\\\u0001\\\u0001\\\u0003\\\u0704\b\\\u0001\\\u0005\\"+
		"\u0707\b\\\n\\\f\\\u070a\t\\\u0001]\u0001]\u0003]\u070e\b]\u0001]\u0001"+
		"]\u0001]\u0005]\u0713\b]\n]\f]\u0716\t]\u0001]\u0001]\u0003]\u071a\b]"+
		"\u0001]\u0003]\u071d\b]\u0001]\u0003]\u0720\b]\u0001]\u0003]\u0723\b]"+
		"\u0001]\u0003]\u0726\b]\u0001]\u0003]\u0729\b]\u0001^\u0001^\u0001^\u0003"+
		"^\u072e\b^\u0001^\u0001^\u0001^\u0001^\u0001^\u0001^\u0003^\u0736\b^\u0003"+
		"^\u0738\b^\u0001_\u0001_\u0001`\u0001`\u0001`\u0001`\u0001`\u0001`\u0001"+
		"`\u0001`\u0001`\u0001`\u0001`\u0001`\u0003`\u0748\b`\u0001a\u0001a\u0001"+
		"a\u0001a\u0001a\u0001a\u0001b\u0001b\u0001b\u0001b\u0001c\u0001c\u0001"+
		"c\u0001c\u0001d\u0001d\u0001d\u0001d\u0001e\u0001e\u0001e\u0001e\u0001"+
		"f\u0001f\u0001f\u0001f\u0001f\u0001g\u0001g\u0001g\u0001g\u0001g\u0001"+
		"h\u0001h\u0001h\u0001h\u0001i\u0001i\u0001i\u0001i\u0001j\u0001j\u0001"+
		"j\u0001j\u0001k\u0001k\u0001k\u0001k\u0001k\u0001l\u0001l\u0001l\u0001"+
		"l\u0001m\u0001m\u0001n\u0001n\u0001n\u0003n\u0784\bn\u0001n\u0001n\u0001"+
		"o\u0001o\u0001p\u0001p\u0001q\u0001q\u0001q\u0003q\u078f\bq\u0001q\u0001"+
		"q\u0001r\u0001r\u0001r\u0003r\u0796\br\u0001r\u0001r\u0001s\u0001s\u0003"+
		"s\u079c\bs\u0001s\u0001s\u0003s\u07a0\bs\u0001s\u0001s\u0001t\u0001t\u0001"+
		"u\u0001u\u0001u\u0003u\u07a9\bu\u0001u\u0001u\u0001v\u0001v\u0001v\u0003"+
		"v\u07b0\bv\u0001v\u0001v\u0001w\u0001w\u0001w\u0003w\u07b7\bw\u0001w\u0001"+
		"w\u0001x\u0001x\u0001y\u0001y\u0001y\u0003y\u07c0\by\u0001y\u0001y\u0001"+
		"z\u0001z\u0001z\u0003z\u07c7\bz\u0001z\u0001z\u0001{\u0001{\u0001{\u0003"+
		"{\u07ce\b{\u0001{\u0001{\u0001|\u0001|\u0001|\u0003|\u07d5\b|\u0001|\u0001"+
		"|\u0001}\u0001}\u0001~\u0001~\u0001~\u0003~\u07de\b~\u0001~\u0001~\u0001"+
		"\u007f\u0001\u007f\u0001\u0080\u0001\u0080\u0001\u0081\u0001\u0081\u0001"+
		"\u0082\u0001\u0082\u0001\u0082\u0005\u0082\u07eb\b\u0082\n\u0082\f\u0082"+
		"\u07ee\t\u0082\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083"+
		"\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083"+
		"\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083"+
		"\u0001\u0083\u0001\u0083\u0001\u0083\u0003\u0083\u0804\b\u0083\u0001\u0083"+
		"\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083"+
		"\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083"+
		"\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083\u0001\u0083"+
		"\u0001\u0083\u0001\u0083\u0003\u0083\u081b\b\u0083\u0001\u0083\u0001\u0083"+
		"\u0001\u0083\u0001\u0083\u0001\u0083\u0005\u0083\u0822\b\u0083\n\u0083"+
		"\f\u0083\u0825\t\u0083\u0001\u0084\u0001\u0084\u0001\u0084\u0003\u0084"+
		"\u082a\b\u0084\u0001\u0085\u0001\u0085\u0001\u0085\u0003\u0085\u082f\b"+
		"\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0003"+
		"\u0085\u0836\b\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0001\u0085\u0001"+
		"\u0085\u0003\u0085\u083d\b\u0085\u0001\u0085\u0001\u0085\u0003\u0085\u0841"+
		"\b\u0085\u0001\u0086\u0001\u0086\u0003\u0086\u0845\b\u0086\u0001\u0086"+
		"\u0001\u0086\u0003\u0086\u0849\b\u0086\u0001\u0086\u0001\u0086\u0001\u0086"+
		"\u0001\u0086\u0003\u0086\u084f\b\u0086\u0001\u0087\u0001\u0087\u0001\u0088"+
		"\u0001\u0088\u0001\u0088\u0003\u0088\u0856\b\u0088\u0001\u0089\u0001\u0089"+
		"\u0001\u0089\u0001\u0089\u0001\u0089\u0001\u0089\u0001\u0089\u0001\u0089"+
		"\u0001\u0089\u0001\u0089\u0005\u0089\u0862\b\u0089\n\u0089\f\u0089\u0865"+
		"\t\u0089\u0001\u0089\u0001\u0089\u0003\u0089\u0869\b\u0089\u0001\u0089"+
		"\u0001\u0089\u0001\u008a\u0001\u008a\u0001\u008a\u0001\u008a\u0001\u008a"+
		"\u0001\u008a\u0005\u008a\u0873\b\u008a\n\u008a\f\u008a\u0876\t\u008a\u0001"+
		"\u008a\u0001\u008a\u0001\u008a\u0001\u008a\u0001\u008a\u0001\u008a\u0005"+
		"\u008a\u087e\b\u008a\n\u008a\f\u008a\u0881\t\u008a\u0001\u008a\u0001\u008a"+
		"\u0001\u008a\u0005\u008a\u0886\b\u008a\n\u008a\f\u008a\u0889\t\u008a\u0001"+
		"\u008a\u0001\u008a\u0003\u008a\u088d\b\u008a\u0001\u008a\u0001\u008a\u0001"+
		"\u008b\u0001\u008b\u0001\u008b\u0001\u008b\u0001\u008b\u0001\u008b\u0005"+
		"\u008b\u0897\b\u008b\n\u008b\f\u008b\u089a\t\u008b\u0001\u008b\u0001\u008b"+
		"\u0001\u008b\u0001\u008b\u0001\u008b\u0001\u008b\u0005\u008b\u08a2\b\u008b"+
		"\n\u008b\f\u008b\u08a5\t\u008b\u0001\u008b\u0001\u008b\u0001\u008b\u0005"+
		"\u008b\u08aa\b\u008b\n\u008b\f\u008b\u08ad\t\u008b\u0001\u008b\u0001\u008b"+
		"\u0003\u008b\u08b1\b\u008b\u0001\u008b\u0001\u008b\u0001\u008c\u0001\u008c"+
		"\u0001\u008c\u0001\u008c\u0001\u008c\u0001\u008c\u0003\u008c\u08bb\b\u008c"+
		"\u0001\u008c\u0001\u008c\u0003\u008c\u08bf\b\u008c\u0001\u008c\u0001\u008c"+
		"\u0001\u008c\u0001\u008c\u0003\u008c\u08c5\b\u008c\u0001\u008c\u0003\u008c"+
		"\u08c8\b\u008c\u0001\u008c\u0001\u008c\u0001\u008c\u0001\u008c\u0001\u008c"+
		"\u0001\u008c\u0003\u008c\u08d0\b\u008c\u0001\u008c\u0001\u008c\u0001\u008c"+
		"\u0003\u008c\u08d5\b\u008c\u0001\u008c\u0001\u008c\u0001\u008c\u0001\u008c"+
		"\u0001\u008c\u0001\u008c\u0001\u008c\u0001\u008c\u0001\u008c\u0003\u008c"+
		"\u08e0\b\u008c\u0001\u008d\u0001\u008d\u0001\u008e\u0001\u008e\u0001\u008e"+
		"\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e"+
		"\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e"+
		"\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e"+
		"\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e"+
		"\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e"+
		"\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e"+
		"\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e"+
		"\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e"+
		"\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e\u0001\u008e"+
		"\u0001\u008e\u0001\u008e\u0003\u008e\u091f\b\u008e\u0001\u008f\u0001\u008f"+
		"\u0001\u0090\u0001\u0090\u0003\u0090\u0925\b\u0090\u0001\u0090\u0003\u0090"+
		"\u0928\b\u0090\u0001\u0090\u0003\u0090\u092b\b\u0090\u0001\u0090\u0003"+
		"\u0090\u092e\b\u0090\u0001\u0090\u0001\u0090\u0003\u0090\u0932\b\u0090"+
		"\u0001\u0091\u0001\u0091\u0001\u0091\u0001\u0091\u0001\u0091\u0001\u0091"+
		"\u0001\u0091\u0001\u0091\u0001\u0091\u0001\u0091\u0005\u0091\u093e\b\u0091"+
		"\n\u0091\f\u0091\u0941\t\u0091\u0003\u0091\u0943\b\u0091\u0003\u0091\u0945"+
		"\b\u0091\u0001\u0092\u0001\u0092\u0001\u0093\u0001\u0093\u0001\u0093\u0001"+
		"\u0093\u0005\u0093\u094d\b\u0093\n\u0093\f\u0093\u0950\t\u0093\u0001\u0094"+
		"\u0001\u0094\u0001\u0094\u0001\u0094\u0001\u0094\u0005\u0094\u0957\b\u0094"+
		"\n\u0094\f\u0094\u095a\t\u0094\u0001\u0095\u0003\u0095\u095d\b\u0095\u0001"+
		"\u0095\u0001\u0095\u0001\u0095\u0001\u0095\u0001\u0095\u0003\u0095\u0964"+
		"\b\u0095\u0001\u0095\u0001\u0095\u0003\u0095\u0968\b\u0095\u0001\u0095"+
		"\u0001\u0095\u0001\u0095\u0001\u0095\u0001\u0095\u0003\u0095\u096f\b\u0095"+
		"\u0001\u0095\u0001\u0095\u0003\u0095\u0973\b\u0095\u0001\u0095\u0001\u0095"+
		"\u0001\u0095\u0001\u0095\u0001\u0095\u0003\u0095\u097a\b\u0095\u0001\u0095"+
		"\u0001\u0095\u0001\u0095\u0001\u0095\u0003\u0095\u0980\b\u0095\u0001\u0095"+
		"\u0001\u0095\u0001\u0095\u0003\u0095\u0985\b\u0095\u0001\u0095\u0003\u0095"+
		"\u0988\b\u0095\u0001\u0095\u0001\u0095\u0001\u0095\u0003\u0095\u098d\b"+
		"\u0095\u0003\u0095\u098f\b\u0095\u0001\u0096\u0001\u0096\u0003\u0096\u0993"+
		"\b\u0096\u0001\u0096\u0003\u0096\u0996\b\u0096\u0001\u0096\u0001\u0096"+
		"\u0003\u0096\u099a\b\u0096\u0001\u0096\u0001\u0096\u0003\u0096\u099e\b"+
		"\u0096\u0001\u0097\u0001\u0097\u0001\u0097\u0001\u0097\u0001\u0097\u0005"+
		"\u0097\u09a5\b\u0097\n\u0097\f\u0097\u09a8\t\u0097\u0001\u0097\u0001\u0097"+
		"\u0001\u0098\u0003\u0098\u09ad\b\u0098\u0001\u0098\u0001\u0098\u0001\u0098"+
		"\u0001\u0098\u0001\u0098\u0005\u0098\u09b4\b\u0098\n\u0098\f\u0098\u09b7"+
		"\t\u0098\u0001\u0098\u0001\u0098\u0003\u0098\u09bb\b\u0098\u0001\u0099"+
		"\u0001\u0099\u0001\u0099\u0001\u0099\u0001\u0099\u0001\u0099\u0005\u0099"+
		"\u09c3\b\u0099\n\u0099\f\u0099\u09c6\t\u0099\u0003\u0099\u09c8\b\u0099"+
		"\u0001\u0099\u0001\u0099\u0001\u009a\u0001\u009a\u0001\u009b\u0001\u009b"+
		"\u0001\u009b\u0001\u009b\u0001\u009b\u0005\u009b\u09d3\b\u009b\n\u009b"+
		"\f\u009b\u09d6\t\u009b\u0001\u009b\u0001\u009b\u0001\u009b\u0001\u009b"+
		"\u0001\u009b\u0005\u009b\u09dd\b\u009b\n\u009b\f\u009b\u09e0\t\u009b\u0001"+
		"\u009b\u0001\u009b\u0003\u009b\u09e4\b\u009b\u0001\u009c\u0001\u009c\u0001"+
		"\u009c\u0001\u009d\u0001\u009d\u0001\u009d\u0001\u009d\u0001\u009d\u0005"+
		"\u009d\u09ee\b\u009d\n\u009d\f\u009d\u09f1\t\u009d\u0001\u009e\u0001\u009e"+
		"\u0001\u009e\u0003\u009e\u09f6\b\u009e\u0001\u009e\u0003\u009e\u09f9\b"+
		"\u009e\u0001\u009e\u0001\u009e\u0001\u009e\u0001\u009e\u0003\u009e\u09ff"+
		"\b\u009e\u0001\u009f\u0001\u009f\u0001\u00a0\u0001\u00a0\u0001\u00a1\u0001"+
		"\u00a1\u0001\u00a1\u0001\u00a1\u0001\u00a1\u0005\u00a1\u0a0a\b\u00a1\n"+
		"\u00a1\f\u00a1\u0a0d\t\u00a1\u0001\u00a1\u0001\u00a1\u0001\u00a1\u0001"+
		"\u00a1\u0001\u00a1\u0001\u00a1\u0005\u00a1\u0a15\b\u00a1\n\u00a1\f\u00a1"+
		"\u0a18\t\u00a1\u0001\u00a1\u0001\u00a1\u0005\u00a1\u0a1c\b\u00a1\n\u00a1"+
		"\f\u00a1\u0a1f\t\u00a1\u0001\u00a1\u0001\u00a1\u0001\u00a1\u0001\u00a1"+
		"\u0005\u00a1\u0a25\b\u00a1\n\u00a1\f\u00a1\u0a28\t\u00a1\u0003\u00a1\u0a2a"+
		"\b\u00a1\u0001\u00a1\u0003\u00a1\u0a2d\b\u00a1\u0001\u00a1\u0003\u00a1"+
		"\u0a30\b\u00a1\u0001\u00a1\u0003\u00a1\u0a33\b\u00a1\u0001\u00a1\u0000"+
		"\u0003\u00b8\u0106\u0128\u00a2\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010"+
		"\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPR"+
		"TVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e"+
		"\u0090\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6"+
		"\u00a8\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8\u00ba\u00bc\u00be"+
		"\u00c0\u00c2\u00c4\u00c6\u00c8\u00ca\u00cc\u00ce\u00d0\u00d2\u00d4\u00d6"+
		"\u00d8\u00da\u00dc\u00de\u00e0\u00e2\u00e4\u00e6\u00e8\u00ea\u00ec\u00ee"+
		"\u00f0\u00f2\u00f4\u00f6\u00f8\u00fa\u00fc\u00fe\u0100\u0102\u0104\u0106"+
		"\u0108\u010a\u010c\u010e\u0110\u0112\u0114\u0116\u0118\u011a\u011c\u011e"+
		"\u0120\u0122\u0124\u0126\u0128\u012a\u012c\u012e\u0130\u0132\u0134\u0136"+
		"\u0138\u013a\u013c\u013e\u0140\u0142\u0000\u001e>\u0000\u0005\u0005\u0007"+
		"\t\u000b\u000f\u0011\u0012\u0015\u0019\u001b\u001b\u001d \"%(+-2479;>"+
		"?AGLRUUXY[[]`bgikmrty{|~\u0085\u0087\u0088\u008b\u0095\u0097\u0098\u009a"+
		"\u009b\u009f\u009f\u00a1\u00a2\u00a4\u00a4\u00ad\u00af\u00b1\u00b1\u00b5"+
		"\u00b6\u00bd\u00c5\u00c7\u00c8\u00ca\u00ca\u00cc\u00d1\u00d3\u00d6\u00d8"+
		"\u00d8\u00db\u00e1\u00e3\u00e3\u00e5\u00e7\u00ec\u00ec\u00ef\u00f0\u00f2"+
		"\u00f2\u00f5\u00f5\u00f8\u00fb\u00fd\u00fd\u0100\u0101\u0105\u010a\u010c"+
		"\u010c\u0113\u0115\u0117\u0118\u011a\u011a\u011e\u0123\u0125\u0126\u012b"+
		"\u012f\u0132\u0134\u0137\u0139\u013b\u0146\u0002\u0000qq\u00c0\u00c0\u0001"+
		"\u0000\u00f4\u00f5\u0003\u0000\u0007\u0007\f\f\u0106\u0106\u0001\u0000"+
		"\u0153\u0158\u0002\u0000UU\u00dc\u00dc\u0002\u0000hh\u0103\u0103\u0002"+
		"\u0000RR\u0087\u0087\u0002\u0000\u00f9\u00f9\u010b\u010b\u0002\u0000\""+
		"\"\u00ec\u00ec\u0002\u0000\u00f4\u00f4\u0118\u0118\u0002\u0000``\u0101"+
		"\u0101\u0002\u0000\u0101\u0101\u013e\u013e\u0002\u0000VW\u0099\u0099\u0002"+
		"\u0000SS\u0096\u0096\u0003\u0000\u0088\u0088\u008e\u008e\u00d2\u00d2\u0003"+
		"\u0000}}\u009c\u009c\u00b3\u00b3\u0001\u0000()\u0002\u0000MN\u00c7\u00c7"+
		"\u0001\u0000\u0092\u0093\u0002\u0000VV\u0099\u0099\u0002\u0000\u014a\u014a"+
		"\u014c\u014c\u0001\u0000\u0164\u0165\u0001\u0000\u0161\u0163\u0001\u0000"+
		"\u014f\u0150\u0007\u0000\u0016\u0016==\u00b1\u00b1\u00b5\u00b5\u0110\u0111"+
		"\u0115\u0115\u0135\u0136\u0003\u0000@@\u00f6\u00f7\u0130\u0130\u0002\u0000"+
		"\u0007\u0007^^\u0002\u0000\u00a2\u00a2\u00f0\u00f0\u0002\u0000\u000f\u000f"+
		"XX\u0ba2\u0000\u0147\u0001\u0000\u0000\u0000\u0002\u014c\u0001\u0000\u0000"+
		"\u0000\u0004\u0153\u0001\u0000\u0000\u0000\u0006\u0158\u0001\u0000\u0000"+
		"\u0000\b\u0160\u0001\u0000\u0000\u0000\n\u0162\u0001\u0000\u0000\u0000"+
		"\f\u0164\u0001\u0000\u0000\u0000\u000e\u0181\u0001\u0000\u0000\u0000\u0010"+
		"\u0188\u0001\u0000\u0000\u0000\u0012\u0191\u0001\u0000\u0000\u0000\u0014"+
		"\u0193\u0001\u0000\u0000\u0000\u0016\u0195\u0001\u0000\u0000\u0000\u0018"+
		"\u0197\u0001\u0000\u0000\u0000\u001a\u01b2\u0001\u0000\u0000\u0000\u001c"+
		"\u01b4\u0001\u0000\u0000\u0000\u001e\u01bf\u0001\u0000\u0000\u0000 \u01c8"+
		"\u0001\u0000\u0000\u0000\"\u01d7\u0001\u0000\u0000\u0000$\u01f1\u0001"+
		"\u0000\u0000\u0000&\u01f3\u0001\u0000\u0000\u0000(\u0223\u0001\u0000\u0000"+
		"\u0000*\u0225\u0001\u0000\u0000\u0000,\u0271\u0001\u0000\u0000\u0000."+
		"\u0273\u0001\u0000\u0000\u00000\u0280\u0001\u0000\u0000\u00002\u0282\u0001"+
		"\u0000\u0000\u00004\u0286\u0001\u0000\u0000\u00006\u029c\u0001\u0000\u0000"+
		"\u00008\u0395\u0001\u0000\u0000\u0000:\u0397\u0001\u0000\u0000\u0000<"+
		"\u03a2\u0001\u0000\u0000\u0000>\u03b4\u0001\u0000\u0000\u0000@\u03bf\u0001"+
		"\u0000\u0000\u0000B\u03c1\u0001\u0000\u0000\u0000D\u03d2\u0001\u0000\u0000"+
		"\u0000F\u03e8\u0001\u0000\u0000\u0000H\u03ea\u0001\u0000\u0000\u0000J"+
		"\u03fc\u0001\u0000\u0000\u0000L\u0405\u0001\u0000\u0000\u0000N\u0419\u0001"+
		"\u0000\u0000\u0000P\u0463\u0001\u0000\u0000\u0000R\u0468\u0001\u0000\u0000"+
		"\u0000T\u0478\u0001\u0000\u0000\u0000V\u047e\u0001\u0000\u0000\u0000X"+
		"\u0480\u0001\u0000\u0000\u0000Z\u0488\u0001\u0000\u0000\u0000\\\u0491"+
		"\u0001\u0000\u0000\u0000^\u0493\u0001\u0000\u0000\u0000`\u04aa\u0001\u0000"+
		"\u0000\u0000b\u04ac\u0001\u0000\u0000\u0000d\u04b7\u0001\u0000\u0000\u0000"+
		"f\u04b9\u0001\u0000\u0000\u0000h\u04c4\u0001\u0000\u0000\u0000j\u04c6"+
		"\u0001\u0000\u0000\u0000l\u04c8\u0001\u0000\u0000\u0000n\u04f0\u0001\u0000"+
		"\u0000\u0000p\u04f4\u0001\u0000\u0000\u0000r\u04f6\u0001\u0000\u0000\u0000"+
		"t\u04f8\u0001\u0000\u0000\u0000v\u0546\u0001\u0000\u0000\u0000x\u0548"+
		"\u0001\u0000\u0000\u0000z\u054a\u0001\u0000\u0000\u0000|\u054c\u0001\u0000"+
		"\u0000\u0000~\u0551\u0001\u0000\u0000\u0000\u0080\u0556\u0001\u0000\u0000"+
		"\u0000\u0082\u0558\u0001\u0000\u0000\u0000\u0084\u055a\u0001\u0000\u0000"+
		"\u0000\u0086\u0568\u0001\u0000\u0000\u0000\u0088\u056a\u0001\u0000\u0000"+
		"\u0000\u008a\u0577\u0001\u0000\u0000\u0000\u008c\u0579\u0001\u0000\u0000"+
		"\u0000\u008e\u058f\u0001\u0000\u0000\u0000\u0090\u0595\u0001\u0000\u0000"+
		"\u0000\u0092\u0597\u0001\u0000\u0000\u0000\u0094\u059f\u0001\u0000\u0000"+
		"\u0000\u0096\u05c6\u0001\u0000\u0000\u0000\u0098\u05c8\u0001\u0000\u0000"+
		"\u0000\u009a\u05e1\u0001\u0000\u0000\u0000\u009c\u05f5\u0001\u0000\u0000"+
		"\u0000\u009e\u0653\u0001\u0000\u0000\u0000\u00a0\u0677\u0001\u0000\u0000"+
		"\u0000\u00a2\u0679\u0001\u0000\u0000\u0000\u00a4\u067d\u0001\u0000\u0000"+
		"\u0000\u00a6\u0688\u0001\u0000\u0000\u0000\u00a8\u06a5\u0001\u0000\u0000"+
		"\u0000\u00aa\u06a7\u0001\u0000\u0000\u0000\u00ac\u06a9\u0001\u0000\u0000"+
		"\u0000\u00ae\u06b1\u0001\u0000\u0000\u0000\u00b0\u06b8\u0001\u0000\u0000"+
		"\u0000\u00b2\u06bc\u0001\u0000\u0000\u0000\u00b4\u06c5\u0001\u0000\u0000"+
		"\u0000\u00b6\u06c7\u0001\u0000\u0000\u0000\u00b8\u06f2\u0001\u0000\u0000"+
		"\u0000\u00ba\u070b\u0001\u0000\u0000\u0000\u00bc\u0737\u0001\u0000\u0000"+
		"\u0000\u00be\u0739\u0001\u0000\u0000\u0000\u00c0\u0747\u0001\u0000\u0000"+
		"\u0000\u00c2\u0749\u0001\u0000\u0000\u0000\u00c4\u074f\u0001\u0000\u0000"+
		"\u0000\u00c6\u0753\u0001\u0000\u0000\u0000\u00c8\u0757\u0001\u0000\u0000"+
		"\u0000\u00ca\u075b\u0001\u0000\u0000\u0000\u00cc\u075f\u0001\u0000\u0000"+
		"\u0000\u00ce\u0764\u0001\u0000\u0000\u0000\u00d0\u0769\u0001\u0000\u0000"+
		"\u0000\u00d2\u076d\u0001\u0000\u0000\u0000\u00d4\u0771\u0001\u0000\u0000"+
		"\u0000\u00d6\u0775\u0001\u0000\u0000\u0000\u00d8\u077a\u0001\u0000\u0000"+
		"\u0000\u00da\u077e\u0001\u0000\u0000\u0000\u00dc\u0783\u0001\u0000\u0000"+
		"\u0000\u00de\u0787\u0001\u0000\u0000\u0000\u00e0\u0789\u0001\u0000\u0000"+
		"\u0000\u00e2\u078e\u0001\u0000\u0000\u0000\u00e4\u0795\u0001\u0000\u0000"+
		"\u0000\u00e6\u079f\u0001\u0000\u0000\u0000\u00e8\u07a3\u0001\u0000\u0000"+
		"\u0000\u00ea\u07a8\u0001\u0000\u0000\u0000\u00ec\u07af\u0001\u0000\u0000"+
		"\u0000\u00ee\u07b6\u0001\u0000\u0000\u0000\u00f0\u07ba\u0001\u0000\u0000"+
		"\u0000\u00f2\u07bf\u0001\u0000\u0000\u0000\u00f4\u07c6\u0001\u0000\u0000"+
		"\u0000\u00f6\u07cd\u0001\u0000\u0000\u0000\u00f8\u07d4\u0001\u0000\u0000"+
		"\u0000\u00fa\u07d8\u0001\u0000\u0000\u0000\u00fc\u07dd\u0001\u0000\u0000"+
		"\u0000\u00fe\u07e1\u0001\u0000\u0000\u0000\u0100\u07e3\u0001\u0000\u0000"+
		"\u0000\u0102\u07e5\u0001\u0000\u0000\u0000\u0104\u07e7\u0001\u0000\u0000"+
		"\u0000\u0106\u0803\u0001\u0000\u0000\u0000\u0108\u0829\u0001\u0000\u0000"+
		"\u0000\u010a\u0840\u0001\u0000\u0000\u0000\u010c\u084e\u0001\u0000\u0000"+
		"\u0000\u010e\u0850\u0001\u0000\u0000\u0000\u0110\u0855\u0001\u0000\u0000"+
		"\u0000\u0112\u0857\u0001\u0000\u0000\u0000\u0114\u086c\u0001\u0000\u0000"+
		"\u0000\u0116\u0890\u0001\u0000\u0000\u0000\u0118\u08df\u0001\u0000\u0000"+
		"\u0000\u011a\u08e1\u0001\u0000\u0000\u0000\u011c\u091e\u0001\u0000\u0000"+
		"\u0000\u011e\u0920\u0001\u0000\u0000\u0000\u0120\u0922\u0001\u0000\u0000"+
		"\u0000\u0122\u0933\u0001\u0000\u0000\u0000\u0124\u0946\u0001\u0000\u0000"+
		"\u0000\u0126\u0948\u0001\u0000\u0000\u0000\u0128\u0951\u0001\u0000\u0000"+
		"\u0000\u012a\u098e\u0001\u0000\u0000\u0000\u012c\u099d\u0001\u0000\u0000"+
		"\u0000\u012e\u099f\u0001\u0000\u0000\u0000\u0130\u09ac\u0001\u0000\u0000"+
		"\u0000\u0132\u09bc\u0001\u0000\u0000\u0000\u0134\u09cb\u0001\u0000\u0000"+
		"\u0000\u0136\u09cd\u0001\u0000\u0000\u0000\u0138\u09e5\u0001\u0000\u0000"+
		"\u0000\u013a\u09e8\u0001\u0000\u0000\u0000\u013c\u09f5\u0001\u0000\u0000"+
		"\u0000\u013e\u0a00\u0001\u0000\u0000\u0000\u0140\u0a02\u0001\u0000\u0000"+
		"\u0000\u0142\u0a29\u0001\u0000\u0000\u0000\u0144\u0146\u0003\u0002\u0001"+
		"\u0000\u0145\u0144\u0001\u0000\u0000\u0000\u0146\u0149\u0001\u0000\u0000"+
		"\u0000\u0147\u0145\u0001\u0000\u0000\u0000\u0147\u0148\u0001\u0000\u0000"+
		"\u0000\u0148\u014a\u0001\u0000\u0000\u0000\u0149\u0147\u0001\u0000\u0000"+
		"\u0000\u014a\u014b\u0005\u0000\u0000\u0001\u014b\u0001\u0001\u0000\u0000"+
		"\u0000\u014c\u014e\u0003\u0004\u0002\u0000\u014d\u014f\u0005\u0160\u0000"+
		"\u0000\u014e\u014d\u0001\u0000\u0000\u0000\u014e\u014f\u0001\u0000\u0000"+
		"\u0000\u014f\u0003\u0001\u0000\u0000\u0000\u0150\u0154\u0003\u0006\u0003"+
		"\u0000\u0151\u0154\u0003\b\u0004\u0000\u0152\u0154\u00038\u001c\u0000"+
		"\u0153\u0150\u0001\u0000\u0000\u0000\u0153\u0151\u0001\u0000\u0000\u0000"+
		"\u0153\u0152\u0001\u0000\u0000\u0000\u0154\u0005\u0001\u0000\u0000\u0000"+
		"\u0155\u0159\u0003l6\u0000\u0156\u0159\u0003\u0086C\u0000\u0157\u0159"+
		"\u0003\u00c0`\u0000\u0158\u0155\u0001\u0000\u0000\u0000\u0158\u0156\u0001"+
		"\u0000\u0000\u0000\u0158\u0157\u0001\u0000\u0000\u0000\u0159\u0007\u0001"+
		"\u0000\u0000\u0000\u015a\u0161\u0003\u0120\u0090\u0000\u015b\u0161\u0003"+
		"\f\u0006\u0000\u015c\u0161\u0003(\u0014\u0000\u015d\u0161\u00036\u001b"+
		"\u0000\u015e\u0161\u0003\u0018\f\u0000\u015f\u0161\u0003Z-\u0000\u0160"+
		"\u015a\u0001\u0000\u0000\u0000\u0160\u015b\u0001\u0000\u0000\u0000\u0160"+
		"\u015c\u0001\u0000\u0000\u0000\u0160\u015d\u0001\u0000\u0000\u0000\u0160"+
		"\u015e\u0001\u0000\u0000\u0000\u0160\u015f\u0001\u0000\u0000\u0000\u0161"+
		"\t\u0001\u0000\u0000\u0000\u0162\u0163\u0007\u0000\u0000\u0000\u0163\u000b"+
		"\u0001\u0000\u0000\u0000\u0164\u0165\u0005\u0091\u0000\u0000\u0165\u0166"+
		"\u0005\u0095\u0000\u0000\u0166\u0174\u0003\u00dcn\u0000\u0167\u0168\u0005"+
		"\u015d\u0000\u0000\u0168\u016d\u0003\u00be_\u0000\u0169\u016a\u0005\u015f"+
		"\u0000\u0000\u016a\u016c\u0003\u00be_\u0000\u016b\u0169\u0001\u0000\u0000"+
		"\u0000\u016c\u016f\u0001\u0000\u0000\u0000\u016d\u016b\u0001\u0000\u0000"+
		"\u0000\u016d\u016e\u0001\u0000\u0000\u0000\u016e\u0170\u0001\u0000\u0000"+
		"\u0000\u016f\u016d\u0001\u0000\u0000\u0000\u0170\u0171\u0005\u015e\u0000"+
		"\u0000\u0171\u0173\u0001\u0000\u0000\u0000\u0172\u0167\u0001\u0000\u0000"+
		"\u0000\u0173\u0176\u0001\u0000\u0000\u0000\u0174\u0172\u0001\u0000\u0000"+
		"\u0000\u0174\u0175\u0001\u0000\u0000\u0000\u0175\u0177\u0001\u0000\u0000"+
		"\u0000\u0176\u0174\u0001\u0000\u0000\u0000\u0177\u0179\u0003\u00b8\\\u0000"+
		"\u0178\u017a\u0003\u013a\u009d\u0000\u0179\u0178\u0001\u0000\u0000\u0000"+
		"\u0179\u017a\u0001\u0000\u0000\u0000\u017a\u017c\u0001\u0000\u0000\u0000"+
		"\u017b\u017d\u0003\u000e\u0007\u0000\u017c\u017b\u0001\u0000\u0000\u0000"+
		"\u017c\u017d\u0001\u0000\u0000\u0000\u017d\u017f\u0001\u0000\u0000\u0000"+
		"\u017e\u0180\u0003\u0010\b\u0000\u017f\u017e\u0001\u0000\u0000\u0000\u017f"+
		"\u0180\u0001\u0000\u0000\u0000\u0180\r\u0001\u0000\u0000\u0000\u0181\u0184"+
		"\u0005\u00cb\u0000\u0000\u0182\u0185\u0003\u0016\u000b\u0000\u0183\u0185"+
		"\u0005\u0166\u0000\u0000\u0184\u0182\u0001\u0000\u0000\u0000\u0184\u0183"+
		"\u0001\u0000\u0000\u0000\u0185\u0186\u0001\u0000\u0000\u0000\u0186\u0187"+
		"\u0003\u0014\n\u0000\u0187\u000f\u0001\u0000\u0000\u0000\u0188\u0189\u0005"+
		"p\u0000\u0000\u0189\u018c\u0003\u0012\t\u0000\u018a\u018d\u0003\u0016"+
		"\u000b\u0000\u018b\u018d\u0005\u0166\u0000\u0000\u018c\u018a\u0001\u0000"+
		"\u0000\u0000\u018c\u018b\u0001\u0000\u0000\u0000\u018d\u018e\u0001\u0000"+
		"\u0000\u0000\u018e\u018f\u0003\u0014\n\u0000\u018f\u0190\u0005\u00cd\u0000"+
		"\u0000\u0190\u0011\u0001\u0000\u0000\u0000\u0191\u0192\u0007\u0001\u0000"+
		"\u0000\u0192\u0013\u0001\u0000\u0000\u0000\u0193\u0194\u0007\u0002\u0000"+
		"\u0000\u0194\u0015\u0001\u0000\u0000\u0000\u0195\u0196\u0003\u0102\u0081"+
		"\u0000\u0196\u0017\u0001\u0000\u0000\u0000\u0197\u0198\u0005\u00b4\u0000"+
		"\u0000\u0198\u0199\u0005\u0095\u0000\u0000\u0199\u019e\u0003\u00dcn\u0000"+
		"\u019a\u019c\u0005\u000e\u0000\u0000\u019b\u019a\u0001\u0000\u0000\u0000"+
		"\u019b\u019c\u0001\u0000\u0000\u0000\u019c\u019d\u0001\u0000\u0000\u0000"+
		"\u019d\u019f\u0003\u00e8t\u0000\u019e\u019b\u0001\u0000\u0000\u0000\u019e"+
		"\u019f\u0001\u0000\u0000\u0000\u019f\u01a0\u0001\u0000\u0000\u0000\u01a0"+
		"\u01a1\u0005\u0133\u0000\u0000\u01a1\u01a6\u0003\u00dcn\u0000\u01a2\u01a4"+
		"\u0005\u000e\u0000\u0000\u01a3\u01a2\u0001\u0000\u0000\u0000\u01a3\u01a4"+
		"\u0001\u0000\u0000\u0000\u01a4\u01a5\u0001\u0000\u0000\u0000\u01a5\u01a7"+
		"\u0003\u00e8t\u0000\u01a6\u01a3\u0001\u0000\u0000\u0000\u01a6\u01a7\u0001"+
		"\u0000\u0000\u0000\u01a7\u01a8\u0001\u0000\u0000\u0000\u01a8\u01a9\u0005"+
		"\u00cc\u0000\u0000\u01a9\u01ad\u0003$\u0012\u0000\u01aa\u01ac\u0003\u001a"+
		"\r\u0000\u01ab\u01aa\u0001\u0000\u0000\u0000\u01ac\u01af\u0001\u0000\u0000"+
		"\u0000\u01ad\u01ab\u0001\u0000\u0000\u0000\u01ad\u01ae\u0001\u0000\u0000"+
		"\u0000\u01ae\u0019\u0001\u0000\u0000\u0000\u01af\u01ad\u0001\u0000\u0000"+
		"\u0000\u01b0\u01b3\u0003\u001c\u000e\u0000\u01b1\u01b3\u0003\u001e\u000f"+
		"\u0000\u01b2\u01b0\u0001\u0000\u0000\u0000\u01b2\u01b1\u0001\u0000\u0000"+
		"\u0000\u01b3\u001b\u0001\u0000\u0000\u0000\u01b4\u01b5\u0005\u013a\u0000"+
		"\u0000\u01b5\u01b8\u0005\u00b0\u0000\u0000\u01b6\u01b7\u0005\u000b\u0000"+
		"\u0000\u01b7\u01b9\u0003&\u0013\u0000\u01b8\u01b6\u0001\u0000\u0000\u0000"+
		"\u01b8\u01b9\u0001\u0000\u0000\u0000\u01b9\u01ba\u0001\u0000\u0000\u0000"+
		"\u01ba\u01bd\u0005\u011b\u0000\u0000\u01bb\u01be\u0003 \u0010\u0000\u01bc"+
		"\u01be\u0005U\u0000\u0000\u01bd\u01bb\u0001\u0000\u0000\u0000\u01bd\u01bc"+
		"\u0001\u0000\u0000\u0000\u01be\u001d\u0001\u0000\u0000\u0000\u01bf\u01c0"+
		"\u0005\u013a\u0000\u0000\u01c0\u01c1\u0005\u00c3\u0000\u0000\u01c1\u01c2"+
		"\u0005\u00b0\u0000\u0000\u01c2\u01c3\u0005\u000b\u0000\u0000\u01c3\u01c4"+
		"\u0003&\u0013\u0000\u01c4\u01c5\u0001\u0000\u0000\u0000\u01c5\u01c6\u0005"+
		"\u011b\u0000\u0000\u01c6\u01c7\u0003\"\u0011\u0000\u01c7\u001f\u0001\u0000"+
		"\u0000\u0000\u01c8\u01c9\u0005\u012e\u0000\u0000\u01c9\u01ca\u0005\u0101"+
		"\u0000\u0000\u01ca\u01cb\u0003\u00e6s\u0000\u01cb\u01cc\u0005\u0154\u0000"+
		"\u0000\u01cc\u01d4\u00034\u001a\u0000\u01cd\u01ce\u0005\u015f\u0000\u0000"+
		"\u01ce\u01cf\u0003\u00e6s\u0000\u01cf\u01d0\u0005\u0154\u0000\u0000\u01d0"+
		"\u01d1\u00034\u001a\u0000\u01d1\u01d3\u0001\u0000\u0000\u0000\u01d2\u01cd"+
		"\u0001\u0000\u0000\u0000\u01d3\u01d6\u0001\u0000\u0000\u0000\u01d4\u01d2"+
		"\u0001\u0000\u0000\u0000\u01d4\u01d5\u0001\u0000\u0000\u0000\u01d5!\u0001"+
		"\u0000\u0000\u0000\u01d6\u01d4\u0001\u0000\u0000\u0000\u01d7\u01e3\u0005"+
		"\u0091\u0000\u0000\u01d8\u01d9\u0005\u015d\u0000\u0000\u01d9\u01de\u0003"+
		"\u00be_\u0000\u01da\u01db\u0005\u015f\u0000\u0000\u01db\u01dd\u0003\u00be"+
		"_\u0000\u01dc\u01da\u0001\u0000\u0000\u0000\u01dd\u01e0\u0001\u0000\u0000"+
		"\u0000\u01de\u01dc\u0001\u0000\u0000\u0000\u01de\u01df\u0001\u0000\u0000"+
		"\u0000\u01df\u01e1\u0001\u0000\u0000\u0000\u01e0\u01de\u0001\u0000\u0000"+
		"\u0000\u01e1\u01e2\u0005\u015e\u0000\u0000\u01e2\u01e4\u0001\u0000\u0000"+
		"\u0000\u01e3\u01d8\u0001\u0000\u0000\u0000\u01e3\u01e4\u0001\u0000\u0000"+
		"\u0000\u01e4\u01e5\u0001\u0000\u0000\u0000\u01e5\u01e6\u0005\u0134\u0000"+
		"\u0000\u01e6\u01e7\u0005\u015d\u0000\u0000\u01e7\u01ec\u00034\u001a\u0000"+
		"\u01e8\u01e9\u0005\u015f\u0000\u0000\u01e9\u01eb\u00034\u001a\u0000\u01ea"+
		"\u01e8\u0001\u0000\u0000\u0000\u01eb\u01ee\u0001\u0000\u0000\u0000\u01ec"+
		"\u01ea\u0001\u0000\u0000\u0000\u01ec\u01ed\u0001\u0000\u0000\u0000\u01ed"+
		"\u01ef\u0001\u0000\u0000\u0000\u01ee\u01ec\u0001\u0000\u0000\u0000\u01ef"+
		"\u01f0\u0005\u015e\u0000\u0000\u01f0#\u0001\u0000\u0000\u0000\u01f1\u01f2"+
		"\u0003,\u0016\u0000\u01f2%\u0001\u0000\u0000\u0000\u01f3\u01f4\u0003,"+
		"\u0016\u0000\u01f4\'\u0001\u0000\u0000\u0000\u01f5\u01f6\u0005\u012e\u0000"+
		"\u0000\u01f6\u01fb\u0003\u00dcn\u0000\u01f7\u01f9\u0005\u000e\u0000\u0000"+
		"\u01f8\u01f7\u0001\u0000\u0000\u0000\u01f8\u01f9\u0001\u0000\u0000\u0000"+
		"\u01f9\u01fa\u0001\u0000\u0000\u0000\u01fa\u01fc\u0003\u00e8t\u0000\u01fb"+
		"\u01f8\u0001\u0000\u0000\u0000\u01fb\u01fc\u0001\u0000\u0000\u0000\u01fc"+
		"\u01fd\u0001\u0000\u0000\u0000\u01fd\u01fe\u0005\u0101\u0000\u0000\u01fe"+
		"\u01ff\u0003\u00e6s\u0000\u01ff\u0200\u0005\u0154\u0000\u0000\u0200\u0208"+
		"\u00034\u001a\u0000\u0201\u0202\u0005\u015f\u0000\u0000\u0202\u0203\u0003"+
		"\u00e6s\u0000\u0203\u0204\u0005\u0154\u0000\u0000\u0204\u0205\u00034\u001a"+
		"\u0000\u0205\u0207\u0001\u0000\u0000\u0000\u0206\u0201\u0001\u0000\u0000"+
		"\u0000\u0207\u020a\u0001\u0000\u0000\u0000\u0208\u0206\u0001\u0000\u0000"+
		"\u0000\u0208\u0209\u0001\u0000\u0000\u0000\u0209\u020c\u0001\u0000\u0000"+
		"\u0000\u020a\u0208\u0001\u0000\u0000\u0000\u020b\u020d\u0003*\u0015\u0000"+
		"\u020c\u020b\u0001\u0000\u0000\u0000\u020c\u020d\u0001\u0000\u0000\u0000"+
		"\u020d\u0224\u0001\u0000\u0000\u0000\u020e\u020f\u0005\u012e\u0000\u0000"+
		"\u020f\u0210\u0003\u00dcn\u0000\u0210\u0211\u0005\u0101\u0000\u0000\u0211"+
		"\u0212\u0003\u00e6s\u0000\u0212\u0213\u0005\u0154\u0000\u0000\u0213\u021b"+
		"\u00034\u001a\u0000\u0214\u0215\u0005\u015f\u0000\u0000\u0215\u0216\u0003"+
		"\u00e6s\u0000\u0216\u0217\u0005\u0154\u0000\u0000\u0217\u0218\u00034\u001a"+
		"\u0000\u0218\u021a\u0001\u0000\u0000\u0000\u0219\u0214\u0001\u0000\u0000"+
		"\u0000\u021a\u021d\u0001\u0000\u0000\u0000\u021b\u0219\u0001\u0000\u0000"+
		"\u0000\u021b\u021c\u0001\u0000\u0000\u0000\u021c\u021e\u0001\u0000\u0000"+
		"\u0000\u021d\u021b\u0001\u0000\u0000\u0000\u021e\u021f\u0005\u013c\u0000"+
		"\u0000\u021f\u0220\u0005A\u0000\u0000\u0220\u0221\u0005\u00ca\u0000\u0000"+
		"\u0221\u0222\u0003\u00fe\u007f\u0000\u0222\u0224\u0001\u0000\u0000\u0000"+
		"\u0223\u01f5\u0001\u0000\u0000\u0000\u0223\u020e\u0001\u0000\u0000\u0000"+
		"\u0224)\u0001\u0000\u0000\u0000\u0225\u0226\u0005\u013c\u0000\u0000\u0226"+
		"\u0227\u0003,\u0016\u0000\u0227+\u0001\u0000\u0000\u0000\u0228\u0229\u0003"+
		"\u0106\u0083\u0000\u0229\u022a\u0005\u000b\u0000\u0000\u022a\u022b\u0003"+
		"\u0106\u0083\u0000\u022b\u0272\u0001\u0000\u0000\u0000\u022c\u022d\u0003"+
		"\u0106\u0083\u0000\u022d\u022e\u0005\u00d0\u0000\u0000\u022e\u022f\u0003"+
		"\u0106\u0083\u0000\u022f\u0272\u0001\u0000\u0000\u0000\u0230\u0231\u0005"+
		"\u00c3\u0000\u0000\u0231\u0272\u0003\u0106\u0083\u0000\u0232\u0233\u0003"+
		"\u0106\u0083\u0000\u0233\u0234\u00032\u0019\u0000\u0234\u0235\u0003\u0106"+
		"\u0083\u0000\u0235\u0272\u0001\u0000\u0000\u0000\u0236\u0237\u0003\u0106"+
		"\u0083\u0000\u0237\u0239\u0005\u0097\u0000\u0000\u0238\u023a\u0005\u00c3"+
		"\u0000\u0000\u0239\u0238\u0001\u0000\u0000\u0000\u0239\u023a\u0001\u0000"+
		"\u0000\u0000\u023a\u023b\u0001\u0000\u0000\u0000\u023b\u023c\u0005\u00c4"+
		"\u0000\u0000\u023c\u0272\u0001\u0000\u0000\u0000\u023d\u023f\u00030\u0018"+
		"\u0000\u023e\u0240\u0005\u00c3\u0000\u0000\u023f\u023e\u0001\u0000\u0000"+
		"\u0000\u023f\u0240\u0001\u0000\u0000\u0000\u0240\u0241\u0001\u0000\u0000"+
		"\u0000\u0241\u0242\u0005\u00a4\u0000\u0000\u0242\u0245\u00030\u0018\u0000"+
		"\u0243\u0244\u0005e\u0000\u0000\u0244\u0246\u0003\u00dam\u0000\u0245\u0243"+
		"\u0001\u0000\u0000\u0000\u0245\u0246\u0001\u0000\u0000\u0000\u0246\u0272"+
		"\u0001\u0000\u0000\u0000\u0247\u0249\u0003\u0106\u0083\u0000\u0248\u024a"+
		"\u0005\u00c3\u0000\u0000\u0249\u0248\u0001\u0000\u0000\u0000\u0249\u024a"+
		"\u0001\u0000\u0000\u0000\u024a\u024b\u0001\u0000\u0000\u0000\u024b\u024c"+
		"\u0005\u0018\u0000\u0000\u024c\u024d\u0003\u0106\u0083\u0000\u024d\u024e"+
		"\u0005\u000b\u0000\u0000\u024e\u024f\u0003\u0106\u0083\u0000\u024f\u0272"+
		"\u0001\u0000\u0000\u0000\u0250\u0252\u0003\u0106\u0083\u0000\u0251\u0253"+
		"\u0005\u00c3\u0000\u0000\u0252\u0251\u0001\u0000\u0000\u0000\u0252\u0253"+
		"\u0001\u0000\u0000\u0000\u0253\u0254\u0001\u0000\u0000\u0000\u0254\u0255"+
		"\u0005\u0088\u0000\u0000\u0255\u0256\u0003.\u0017\u0000\u0256\u0272\u0001"+
		"\u0000\u0000\u0000\u0257\u0259\u0003\u0106\u0083\u0000\u0258\u025a\u0005"+
		"\u00c3\u0000\u0000\u0259\u0258\u0001\u0000\u0000\u0000\u0259\u025a\u0001"+
		"\u0000\u0000\u0000\u025a\u025b\u0001\u0000\u0000\u0000\u025b\u025c\u0005"+
		"\u0088\u0000\u0000\u025c\u025d\u0005\u015d\u0000\u0000\u025d\u0262\u0003"+
		"\u0106\u0083\u0000\u025e\u025f\u0005\u015f\u0000\u0000\u025f\u0261\u0003"+
		"\u0106\u0083\u0000\u0260\u025e\u0001\u0000\u0000\u0000\u0261\u0264\u0001"+
		"\u0000\u0000\u0000\u0262\u0260\u0001\u0000\u0000\u0000\u0262\u0263\u0001"+
		"\u0000\u0000\u0000\u0263\u0265\u0001\u0000\u0000\u0000\u0264\u0262\u0001"+
		"\u0000\u0000\u0000\u0265\u0266\u0005\u015e\u0000\u0000\u0266\u0272\u0001"+
		"\u0000\u0000\u0000\u0267\u0269\u0005\u00c3\u0000\u0000\u0268\u0267\u0001"+
		"\u0000\u0000\u0000\u0268\u0269\u0001\u0000\u0000\u0000\u0269\u026a\u0001"+
		"\u0000\u0000\u0000\u026a\u026b\u0005k\u0000\u0000\u026b\u0272\u0003.\u0017"+
		"\u0000\u026c\u026d\u0003\u0106\u0083\u0000\u026d\u026e\u00032\u0019\u0000"+
		"\u026e\u026f\u0007\u0003\u0000\u0000\u026f\u0270\u0003.\u0017\u0000\u0270"+
		"\u0272\u0001\u0000\u0000\u0000\u0271\u0228\u0001\u0000\u0000\u0000\u0271"+
		"\u022c\u0001\u0000\u0000\u0000\u0271\u0230\u0001\u0000\u0000\u0000\u0271"+
		"\u0232\u0001\u0000\u0000\u0000\u0271\u0236\u0001\u0000\u0000\u0000\u0271"+
		"\u023d\u0001\u0000\u0000\u0000\u0271\u0247\u0001\u0000\u0000\u0000\u0271"+
		"\u0250\u0001\u0000\u0000\u0000\u0271\u0257\u0001\u0000\u0000\u0000\u0271"+
		"\u0268\u0001\u0000\u0000\u0000\u0271\u026c\u0001\u0000\u0000\u0000\u0272"+
		"-\u0001\u0000\u0000\u0000\u0273\u0274\u0005\u015d\u0000\u0000\u0274\u0276"+
		"\u0003\u00b8\\\u0000\u0275\u0277\u0003\u013a\u009d\u0000\u0276\u0275\u0001"+
		"\u0000\u0000\u0000\u0276\u0277\u0001\u0000\u0000\u0000\u0277\u0279\u0001"+
		"\u0000\u0000\u0000\u0278\u027a\u0003\u000e\u0007\u0000\u0279\u0278\u0001"+
		"\u0000\u0000\u0000\u0279\u027a\u0001\u0000\u0000\u0000\u027a\u027c\u0001"+
		"\u0000\u0000\u0000\u027b\u027d\u0003\u0010\b\u0000\u027c\u027b\u0001\u0000"+
		"\u0000\u0000\u027c\u027d\u0001\u0000\u0000\u0000\u027d\u027e\u0001\u0000"+
		"\u0000\u0000\u027e\u027f\u0005\u015e\u0000\u0000\u027f/\u0001\u0000\u0000"+
		"\u0000\u0280\u0281\u0003\u00dam\u0000\u02811\u0001\u0000\u0000\u0000\u0282"+
		"\u0283\u0007\u0004\u0000\u0000\u02833\u0001\u0000\u0000\u0000\u0284\u0287"+
		"\u0003\u0106\u0083\u0000\u0285\u0287\u0005P\u0000\u0000\u0286\u0284\u0001"+
		"\u0000\u0000\u0000\u0286\u0285\u0001\u0000\u0000\u0000\u02875\u0001\u0000"+
		"\u0000\u0000\u0288\u0289\u0005U\u0000\u0000\u0289\u028a\u0005w\u0000\u0000"+
		"\u028a\u028f\u0003\u00dcn\u0000\u028b\u028d\u0005\u000e\u0000\u0000\u028c"+
		"\u028b\u0001\u0000\u0000\u0000\u028c\u028d\u0001\u0000\u0000\u0000\u028d"+
		"\u028e\u0001\u0000\u0000\u0000\u028e\u0290\u0003\u00e8t\u0000\u028f\u028c"+
		"\u0001\u0000\u0000\u0000\u028f\u0290\u0001\u0000\u0000\u0000\u0290\u0292"+
		"\u0001\u0000\u0000\u0000\u0291\u0293\u0003*\u0015\u0000\u0292\u0291\u0001"+
		"\u0000\u0000\u0000\u0292\u0293\u0001\u0000\u0000\u0000\u0293\u029d\u0001"+
		"\u0000\u0000\u0000\u0294\u0295\u0005U\u0000\u0000\u0295\u0296\u0005w\u0000"+
		"\u0000\u0296\u0297\u0003\u00dcn\u0000\u0297\u0298\u0005\u013c\u0000\u0000"+
		"\u0298\u0299\u0005A\u0000\u0000\u0299\u029a\u0005\u00ca\u0000\u0000\u029a"+
		"\u029b\u0003\u00fe\u007f\u0000\u029b\u029d\u0001\u0000\u0000\u0000\u029c"+
		"\u0288\u0001\u0000\u0000\u0000\u029c\u0294\u0001\u0000\u0000\u0000\u029d"+
		"7\u0001\u0000\u0000\u0000\u029e\u029f\u0005 \u0000\u0000\u029f\u02a0\u0003"+
		"\u00eew\u0000\u02a0\u02a1\u0005\u015d\u0000\u0000\u02a1\u02a2\u0003\u0104"+
		"\u0082\u0000\u02a2\u02a3\u0005\u015e\u0000\u0000\u02a3\u0396\u0001\u0000"+
		"\u0000\u0000\u02a4\u02a5\u0005O\u0000\u0000\u02a5\u02a6\u0005~\u0000\u0000"+
		"\u02a6\u02a7\u0005\u011a\u0000\u0000\u02a7\u02a8\u0005\u0118\u0000\u0000"+
		"\u02a8\u02a9\u0003\u00dcn\u0000\u02a9\u02ae\u0003B!\u0000\u02aa\u02ab"+
		"\u0005\u015f\u0000\u0000\u02ab\u02ad\u0003B!\u0000\u02ac\u02aa\u0001\u0000"+
		"\u0000\u0000\u02ad\u02b0\u0001\u0000\u0000\u0000\u02ae\u02ac\u0001\u0000"+
		"\u0000\u0000\u02ae\u02af\u0001\u0000\u0000\u0000\u02af\u02b5\u0001\u0000"+
		"\u0000\u0000\u02b0\u02ae\u0001\u0000\u0000\u0000\u02b1\u02b2\u0005\u00cc"+
		"\u0000\u0000\u02b2\u02b3\u00052\u0000\u0000\u02b3\u02b4\u0007\u0005\u0000"+
		"\u0000\u02b4\u02b6\u0005\u00f5\u0000\u0000\u02b5\u02b1\u0001\u0000\u0000"+
		"\u0000\u02b5\u02b6\u0001\u0000\u0000\u0000\u02b6\u02b7\u0001\u0000\u0000"+
		"\u0000\u02b7\u02b8\u0005\u00c3\u0000\u0000\u02b8\u02bd\u0005\u00ab\u0000"+
		"\u0000\u02b9\u02ba\u0005\u00cc\u0000\u0000\u02ba\u02bb\u0005\u00f2\u0000"+
		"\u0000\u02bb\u02bc\u0005U\u0000\u0000\u02bc\u02be\u0005\u00f5\u0000\u0000"+
		"\u02bd\u02b9\u0001\u0000\u0000\u0000\u02bd\u02be\u0001\u0000\u0000\u0000"+
		"\u02be\u0396\u0001\u0000\u0000\u0000\u02bf\u02c0\u0005\u0081\u0000\u0000"+
		"\u02c0\u02c1\u0003\\.\u0000\u02c1\u02c3\u0005\u00cc\u0000\u0000\u02c2"+
		"\u02c4\u0005\u0118\u0000\u0000\u02c3\u02c2\u0001\u0000\u0000\u0000\u02c3"+
		"\u02c4\u0001\u0000\u0000\u0000\u02c4\u02c7\u0001\u0000\u0000\u0000\u02c5"+
		"\u02c8\u0003\u00dcn\u0000\u02c6\u02c8\u0003\u00fc~\u0000\u02c7\u02c5\u0001"+
		"\u0000\u0000\u0000\u02c7\u02c6\u0001\u0000\u0000\u0000\u02c8\u02c9\u0001"+
		"\u0000\u0000\u0000\u02c9\u02ca\u0005\u0120\u0000\u0000\u02ca\u02cb\u0003"+
		":\u001d\u0000\u02cb\u0396\u0001\u0000\u0000\u0000\u02cc\u02cd\u0005\u0081"+
		"\u0000\u0000\u02cd\u02ce\u0005j\u0000\u0000\u02ce\u02cf\u0005\u00cc\u0000"+
		"\u0000\u02cf\u02d0\u0005y\u0000\u0000\u02d0\u02d1\u0003\u00eau\u0000\u02d1"+
		"\u02d2\u0005\u0120\u0000\u0000\u02d2\u02d3\u0003:\u001d\u0000\u02d3\u0396"+
		"\u0001\u0000\u0000\u0000\u02d4\u02d5\u0005\u0081\u0000\u0000\u02d5\u02d6"+
		"\u0005j\u0000\u0000\u02d6\u02d7\u0005\u00cc\u0000\u0000\u02d7\u02d8\u0005"+
		"\u00e0\u0000\u0000\u02d8\u02d9\u0003\u00eew\u0000\u02d9\u02da\u0005\u0120"+
		"\u0000\u0000\u02da\u02db\u0003:\u001d\u0000\u02db\u0396\u0001\u0000\u0000"+
		"\u0000\u02dc\u02dd\u0005\u0081\u0000\u0000\u02dd\u02de\u0005\u0131\u0000"+
		"\u0000\u02de\u02df\u0005\u00cc\u0000\u0000\u02df\u02e0\u0005\u00fe\u0000"+
		"\u0000\u02e0\u02e1\u0003\u00f2y\u0000\u02e1\u02e2\u0005\u0120\u0000\u0000"+
		"\u02e2\u02e3\u0003:\u001d\u0000\u02e3\u0396\u0001\u0000\u0000\u0000\u02e4"+
		"\u02e5\u0005\u0081\u0000\u0000\u02e5\u02e6\u0005\u0131\u0000\u0000\u02e6"+
		"\u02e7\u0005\u00cc\u0000\u0000\u02e7\u02e8\u0005\u0128\u0000\u0000\u02e8"+
		"\u02e9\u0003\u00f8|\u0000\u02e9\u02ea\u0005\u0120\u0000\u0000\u02ea\u02eb"+
		"\u0003:\u001d\u0000\u02eb\u0396\u0001\u0000\u0000\u0000\u02ec\u02ed\u0005"+
		"\u0081\u0000\u0000\u02ed\u02ee\u0005\u0131\u0000\u0000\u02ee\u02ef\u0005"+
		"\u00cc\u0000\u0000\u02ef\u02f0\u0005V\u0000\u0000\u02f0\u02f1\u0005\u0006"+
		"\u0000\u0000\u02f1\u02f2\u0003\u00e2q\u0000\u02f2\u02f3\u0005\u0120\u0000"+
		"\u0000\u02f3\u02f4\u0003:\u001d\u0000\u02f4\u0396\u0001\u0000\u0000\u0000"+
		"\u02f5\u02f6\u0005\u0081\u0000\u0000\u02f6\u02fb\u0003\u00e0p\u0000\u02f7"+
		"\u02f8\u0005\u015f\u0000\u0000\u02f8\u02fa\u0003\u00e0p\u0000\u02f9\u02f7"+
		"\u0001\u0000\u0000\u0000\u02fa\u02fd\u0001\u0000\u0000\u0000\u02fb\u02f9"+
		"\u0001\u0000\u0000\u0000\u02fb\u02fc\u0001\u0000\u0000\u0000\u02fc\u02fe"+
		"\u0001\u0000\u0000\u0000\u02fd\u02fb\u0001\u0000\u0000\u0000\u02fe\u02ff"+
		"\u0005\u0120\u0000\u0000\u02ff\u0300\u0003:\u001d\u0000\u0300\u0396\u0001"+
		"\u0000\u0000\u0000\u0301\u0302\u0005\u00a7\u0000\u0000\u0302\u0303\u0005"+
		"\u0118\u0000\u0000\u0303\u0304\u0003\u00dcn\u0000\u0304\u0305\u0005\u0088"+
		"\u0000\u0000\u0305\u0306\u0007\u0006\u0000\u0000\u0306\u0307\u0005\u00b9"+
		"\u0000\u0000\u0307\u0396\u0001\u0000\u0000\u0000\u0308\u0309\u0005\u00e8"+
		"\u0000\u0000\u0309\u030a\u00051\u0000\u0000\u030a\u030b\u0003\u00dcn\u0000"+
		"\u030b\u030c\u0005\u015b\u0000\u0000\u030c\u030d\u0003\u00be_\u0000\u030d"+
		"\u030e\u0005\u0120\u0000\u0000\u030e\u030f\u0003\u00be_\u0000\u030f\u0396"+
		"\u0001\u0000\u0000\u0000\u0310\u0311\u0005\u00e8\u0000\u0000\u0311\u0312"+
		"\u0005\u008a\u0000\u0000\u0312\u0313\u0003\u00ecv\u0000\u0313\u0314\u0005"+
		"\u0120\u0000\u0000\u0314\u0315\u0003\u00ecv\u0000\u0315\u0396\u0001\u0000"+
		"\u0000\u0000\u0316\u0317\u0005\u00e8\u0000\u0000\u0317\u0318\u0005\u0118"+
		"\u0000\u0000\u0318\u0319\u0005\u00e8\u0000\u0000\u0319\u031a\u0005\u0118"+
		"\u0000\u0000\u031a\u031b\u0003\u00dcn\u0000\u031b\u031c\u0005\u0120\u0000"+
		"\u0000\u031c\u031d\u0003\u00dcn\u0000\u031d\u0396\u0001\u0000\u0000\u0000"+
		"\u031e\u031f\u0005\u00ef\u0000\u0000\u031f\u0320\u0003\\.\u0000\u0320"+
		"\u0322\u0005\u00cc\u0000\u0000\u0321\u0323\u0005\u0118\u0000\u0000\u0322"+
		"\u0321\u0001\u0000\u0000\u0000\u0322\u0323\u0001\u0000\u0000\u0000\u0323"+
		"\u0326\u0001\u0000\u0000\u0000\u0324\u0327\u0003\u00dcn\u0000\u0325\u0327"+
		"\u0003\u00fc~\u0000\u0326\u0324\u0001\u0000\u0000\u0000\u0326\u0325\u0001"+
		"\u0000\u0000\u0000\u0327\u0328\u0001\u0000\u0000\u0000\u0328\u0329\u0005"+
		"w\u0000\u0000\u0329\u032a\u0003f3\u0000\u032a\u0396\u0001\u0000\u0000"+
		"\u0000\u032b\u032c\u0005\u00ef\u0000\u0000\u032c\u032d\u0005j\u0000\u0000"+
		"\u032d\u032e\u0005\u00cc\u0000\u0000\u032e\u032f\u0005y\u0000\u0000\u032f"+
		"\u0330\u0003\u00eau\u0000\u0330\u0331\u0005w\u0000\u0000\u0331\u0332\u0003"+
		"f3\u0000\u0332\u0333\u0005\u00ec\u0000\u0000\u0333\u0396\u0001\u0000\u0000"+
		"\u0000\u0334\u0335\u0005\u00ef\u0000\u0000\u0335\u0336\u0005j\u0000\u0000"+
		"\u0336\u0337\u0005\u00cc\u0000\u0000\u0337\u0338\u0005\u00e0\u0000\u0000"+
		"\u0338\u0339\u0003\u00eew\u0000\u0339\u033a\u0005w\u0000\u0000\u033a\u033b"+
		"\u0003f3\u0000\u033b\u033c\u0005\u00ec\u0000\u0000\u033c\u0396\u0001\u0000"+
		"\u0000\u0000\u033d\u033e\u0005\u00ef\u0000\u0000\u033e\u033f\u0005\u0131"+
		"\u0000\u0000\u033f\u0340\u0005\u00cc\u0000\u0000\u0340\u0341\u0005\u00fe"+
		"\u0000\u0000\u0341\u0342\u0003\u00f2y\u0000\u0342\u0343\u0005w\u0000\u0000"+
		"\u0343\u0344\u0003f3\u0000\u0344\u0345\u0005\u00ec\u0000\u0000\u0345\u0396"+
		"\u0001\u0000\u0000\u0000\u0346\u0347\u0005\u00ef\u0000\u0000\u0347\u0348"+
		"\u0005\u0131\u0000\u0000\u0348\u0349\u0005\u00cc\u0000\u0000\u0349\u034a"+
		"\u0005\u0128\u0000\u0000\u034a\u034b\u0003\u00f8|\u0000\u034b\u034c\u0005"+
		"w\u0000\u0000\u034c\u034d\u0003f3\u0000\u034d\u034e\u0005\u00ec\u0000"+
		"\u0000\u034e\u0396\u0001\u0000\u0000\u0000\u034f\u0350\u0005\u00ef\u0000"+
		"\u0000\u0350\u0351\u0005\u0131\u0000\u0000\u0351\u0352\u0005\u00cc\u0000"+
		"\u0000\u0352\u0353\u0005V\u0000\u0000\u0353\u0354\u0005\u0006\u0000\u0000"+
		"\u0354\u0355\u0003\u00e2q\u0000\u0355\u0356\u0005w\u0000\u0000\u0356\u0357"+
		"\u0003f3\u0000\u0357\u0358\u0005\u00ec\u0000\u0000\u0358\u0396\u0001\u0000"+
		"\u0000\u0000\u0359\u035a\u0005\u00ef\u0000\u0000\u035a\u035f\u0003\u00e0"+
		"p\u0000\u035b\u035c\u0005\u015f\u0000\u0000\u035c\u035e\u0003\u00e0p\u0000"+
		"\u035d\u035b\u0001\u0000\u0000\u0000\u035e\u0361\u0001\u0000\u0000\u0000"+
		"\u035f\u035d\u0001\u0000\u0000\u0000\u035f\u0360\u0001\u0000\u0000\u0000"+
		"\u0360\u0362\u0001\u0000\u0000\u0000\u0361\u035f\u0001\u0000\u0000\u0000"+
		"\u0362\u0363\u0005w\u0000\u0000\u0363\u0364\u0003f3\u0000\u0364\u0396"+
		"\u0001\u0000\u0000\u0000\u0365\u0366\u0005\u0101\u0000\u0000\u0366\u0367"+
		"\u00057\u0000\u0000\u0367\u0368\u0003@ \u0000\u0368\u0369\u0007\u0007"+
		"\u0000\u0000\u0369\u0396\u0001\u0000\u0000\u0000\u036a\u036c\u0005\u0101"+
		"\u0000\u0000\u036b\u036d\u0005A\u0000\u0000\u036c\u036b\u0001\u0000\u0000"+
		"\u0000\u036c\u036d\u0001\u0000\u0000\u0000\u036d\u036e\u0001\u0000\u0000"+
		"\u0000\u036e\u0370\u0005\u0098\u0000\u0000\u036f\u0371\u0005\u0154\u0000"+
		"\u0000\u0370\u036f\u0001\u0000\u0000\u0000\u0370\u0371\u0001\u0000\u0000"+
		"\u0000\u0371\u0372\u0001\u0000\u0000\u0000\u0372\u0396\u0003>\u001f\u0000"+
		"\u0373\u0374\u0005\u0101\u0000\u0000\u0374\u0379\u0005\u00f1\u0000\u0000"+
		"\u0375\u037a\u0003\u00e0p\u0000\u0376\u037a\u0003\u00dam\u0000\u0377\u037a"+
		"\u0005\u0166\u0000\u0000\u0378\u037a\u0005\u00c2\u0000\u0000\u0379\u0375"+
		"\u0001\u0000\u0000\u0000\u0379\u0376\u0001\u0000\u0000\u0000\u0379\u0377"+
		"\u0001\u0000\u0000\u0000\u0379\u0378\u0001\u0000\u0000\u0000\u037a\u0396"+
		"\u0001\u0000\u0000\u0000\u037b\u037d\u0005\u0101\u0000\u0000\u037c\u037e"+
		"\u0005A\u0000\u0000\u037d\u037c\u0001\u0000\u0000\u0000\u037d\u037e\u0001"+
		"\u0000\u0000\u0000\u037e\u037f\u0001\u0000\u0000\u0000\u037f\u0381\u0005"+
		"\u00f9\u0000\u0000\u0380\u0382\u0005\u0154\u0000\u0000\u0381\u0380\u0001"+
		"\u0000\u0000\u0000\u0381\u0382\u0001\u0000\u0000\u0000\u0382\u0387\u0001"+
		"\u0000\u0000\u0000\u0383\u0388\u0003\u00deo\u0000\u0384\u0388\u0005\u0132"+
		"\u0000\u0000\u0385\u0388\u0005\u0166\u0000\u0000\u0386\u0388\u0003\u00da"+
		"m\u0000\u0387\u0383\u0001\u0000\u0000\u0000\u0387\u0384\u0001\u0000\u0000"+
		"\u0000\u0387\u0385\u0001\u0000\u0000\u0000\u0387\u0386\u0001\u0000\u0000"+
		"\u0000\u0388\u0396\u0001\u0000\u0000\u0000\u0389\u038a\u0005\u0101\u0000"+
		"\u0000\u038a\u038b\u0005A\u0000\u0000\u038b\u038d\u0005\u010b\u0000\u0000"+
		"\u038c\u038e\u0005\u0154\u0000\u0000\u038d\u038c\u0001\u0000\u0000\u0000"+
		"\u038d\u038e\u0001\u0000\u0000\u0000\u038e\u0393\u0001\u0000\u0000\u0000"+
		"\u038f\u0394\u0003\u00deo\u0000\u0390\u0394\u0005\u0132\u0000\u0000\u0391"+
		"\u0394\u0005\u0166\u0000\u0000\u0392\u0394\u0003\u00dam\u0000\u0393\u038f"+
		"\u0001\u0000\u0000\u0000\u0393\u0390\u0001\u0000\u0000\u0000\u0393\u0391"+
		"\u0001\u0000\u0000\u0000\u0393\u0392\u0001\u0000\u0000\u0000\u0394\u0396"+
		"\u0001\u0000\u0000\u0000\u0395\u029e\u0001\u0000\u0000\u0000\u0395\u02a4"+
		"\u0001\u0000\u0000\u0000\u0395\u02bf\u0001\u0000\u0000\u0000\u0395\u02cc"+
		"\u0001\u0000\u0000\u0000\u0395\u02d4\u0001\u0000\u0000\u0000\u0395\u02dc"+
		"\u0001\u0000\u0000\u0000\u0395\u02e4\u0001\u0000\u0000\u0000\u0395\u02ec"+
		"\u0001\u0000\u0000\u0000\u0395\u02f5\u0001\u0000\u0000\u0000\u0395\u0301"+
		"\u0001\u0000\u0000\u0000\u0395\u0308\u0001\u0000\u0000\u0000\u0395\u0310"+
		"\u0001\u0000\u0000\u0000\u0395\u0316\u0001\u0000\u0000\u0000\u0395\u031e"+
		"\u0001\u0000\u0000\u0000\u0395\u032b\u0001\u0000\u0000\u0000\u0395\u0334"+
		"\u0001\u0000\u0000\u0000\u0395\u033d\u0001\u0000\u0000\u0000\u0395\u0346"+
		"\u0001\u0000\u0000\u0000\u0395\u034f\u0001\u0000\u0000\u0000\u0395\u0359"+
		"\u0001\u0000\u0000\u0000\u0395\u0365\u0001\u0000\u0000\u0000\u0395\u036a"+
		"\u0001\u0000\u0000\u0000\u0395\u0373\u0001\u0000\u0000\u0000\u0395\u037b"+
		"\u0001\u0000\u0000\u0000\u0395\u0389\u0001\u0000\u0000\u0000\u03969\u0001"+
		"\u0000\u0000\u0000\u0397\u039c\u0003<\u001e\u0000\u0398\u0399\u0005\u015f"+
		"\u0000\u0000\u0399\u039b\u0003<\u001e\u0000\u039a\u0398\u0001\u0000\u0000"+
		"\u0000\u039b\u039e\u0001\u0000\u0000\u0000\u039c\u039a\u0001\u0000\u0000"+
		"\u0000\u039c\u039d\u0001\u0000\u0000\u0000\u039d;\u0001\u0000\u0000\u0000"+
		"\u039e\u039c\u0001\u0000\u0000\u0000\u039f\u03a3\u0003j5\u0000\u03a0\u03a3"+
		"\u0003\u00e0p\u0000\u03a1\u03a3\u0005\u00e1\u0000\u0000\u03a2\u039f\u0001"+
		"\u0000\u0000\u0000\u03a2\u03a0\u0001\u0000\u0000\u0000\u03a2\u03a1\u0001"+
		"\u0000\u0000\u0000\u03a3=\u0001\u0000\u0000\u0000\u03a4\u03b5\u0005\u0130"+
		"\u0000\u0000\u03a5\u03a6\u0005\\\u0000\u0000\u03a6\u03b5\u0005\u00e3\u0000"+
		"\u0000\u03a7\u03a8\u0005\u00e3\u0000\u0000\u03a8\u03b5\u0005\u012a\u0000"+
		"\u0000\u03a9\u03b5\u0005@\u0000\u0000\u03aa\u03ab\u0005\u00e3\u0000\u0000"+
		"\u03ab\u03b5\u00053\u0000\u0000\u03ac\u03ad\u0005G\u0000\u0000\u03ad\u03b5"+
		"\u0005\u010e\u0000\u0000\u03ae\u03b5\u0005\u00f7\u0000\u0000\u03af\u03b5"+
		"\u0005\u00f6\u0000\u0000\u03b0\u03b1\u0005\u00e9\u0000\u0000\u03b1\u03b5"+
		"\u0005\u00e3\u0000\u0000\u03b2\u03b5\u0005\u00ff\u0000\u0000\u03b3\u03b5"+
		"\u0005\u00ea\u0000\u0000\u03b4\u03a4\u0001\u0000\u0000\u0000\u03b4\u03a5"+
		"\u0001\u0000\u0000\u0000\u03b4\u03a7\u0001\u0000\u0000\u0000\u03b4\u03a9"+
		"\u0001\u0000\u0000\u0000\u03b4\u03aa\u0001\u0000\u0000\u0000\u03b4\u03ac"+
		"\u0001\u0000\u0000\u0000\u03b4\u03ae\u0001\u0000\u0000\u0000\u03b4\u03af"+
		"\u0001\u0000\u0000\u0000\u03b4\u03b0\u0001\u0000\u0000\u0000\u03b4\u03b2"+
		"\u0001\u0000\u0000\u0000\u03b4\u03b3\u0001\u0000\u0000\u0000\u03b5?\u0001"+
		"\u0000\u0000\u0000\u03b6\u03c0\u0005\u0007\u0000\u0000\u03b7\u03bc\u0003"+
		"\u00e4r\u0000\u03b8\u03b9\u0005\u015f\u0000\u0000\u03b9\u03bb\u0003\u00e4"+
		"r\u0000\u03ba\u03b8\u0001\u0000\u0000\u0000\u03bb\u03be\u0001\u0000\u0000"+
		"\u0000\u03bc\u03ba\u0001\u0000\u0000\u0000\u03bc\u03bd\u0001\u0000\u0000"+
		"\u0000\u03bd\u03c0\u0001\u0000\u0000\u0000\u03be\u03bc\u0001\u0000\u0000"+
		"\u0000\u03bf\u03b6\u0001\u0000\u0000\u0000\u03bf\u03b7\u0001\u0000\u0000"+
		"\u0000\u03c0A\u0001\u0000\u0000\u0000\u03c1\u03c3\u0003\u00be_\u0000\u03c2"+
		"\u03c4\u0003\u009eO\u0000\u03c3\u03c2\u0001\u0000\u0000\u0000\u03c3\u03c4"+
		"\u0001\u0000\u0000\u0000\u03c4\u03c8\u0001\u0000\u0000\u0000\u03c5\u03c7"+
		"\u0003D\"\u0000\u03c6\u03c5\u0001\u0000\u0000\u0000\u03c7\u03ca\u0001"+
		"\u0000\u0000\u0000\u03c8\u03c6\u0001\u0000\u0000\u0000\u03c8\u03c9\u0001"+
		"\u0000\u0000\u0000\u03c9C\u0001\u0000\u0000\u0000\u03ca\u03c8\u0001\u0000"+
		"\u0000\u0000\u03cb\u03cd\u0005\u013e\u0000\u0000\u03cc\u03cb\u0001\u0000"+
		"\u0000\u0000\u03cc\u03cd\u0001\u0000\u0000\u0000\u03cd\u03ce\u0001\u0000"+
		"\u0000\u0000\u03ce\u03cf\u0005P\u0000\u0000\u03cf\u03d3\u0003F#\u0000"+
		"\u03d0\u03d3\u0003H$\u0000\u03d1\u03d3\u0003J%\u0000\u03d2\u03cc\u0001"+
		"\u0000\u0000\u0000\u03d2\u03d0\u0001\u0000\u0000\u0000\u03d2\u03d1\u0001"+
		"\u0000\u0000\u0000\u03d3E\u0001\u0000\u0000\u0000\u03d4\u03e9\u0005\u00c4"+
		"\u0000\u0000\u03d5\u03d6\u0005A\u0000\u0000\u03d6\u03e9\u0007\b\u0000"+
		"\u0000\u03d7\u03e9\u0005\u0132\u0000\u0000\u03d8\u03e9\u0005F\u0000\u0000"+
		"\u03d9\u03e9\u0005\u0100\u0000\u0000\u03da\u03e9\u0005C\u0000\u0000\u03db"+
		"\u03e9\u0005J\u0000\u0000\u03dc\u03e9\u0005\u011c\u0000\u0000\u03dd\u03e9"+
		"\u0005\u011d\u0000\u0000\u03de\u03df\u0005A\u0000\u0000\u03df\u03e9\u0005"+
		"J\u0000\u0000\u03e0\u03e9\u0005B\u0000\u0000\u03e1\u03e2\u0005A\u0000"+
		"\u0000\u03e2\u03e9\u0005\u011c\u0000\u0000\u03e3\u03e9\u0005D\u0000\u0000"+
		"\u03e4\u03e5\u0005A\u0000\u0000\u03e5\u03e9\u0005\u011d\u0000\u0000\u03e6"+
		"\u03e9\u0005E\u0000\u0000\u03e7\u03e9\u0003\u010c\u0086\u0000\u03e8\u03d4"+
		"\u0001\u0000\u0000\u0000\u03e8\u03d5\u0001\u0000\u0000\u0000\u03e8\u03d7"+
		"\u0001\u0000\u0000\u0000\u03e8\u03d8\u0001\u0000\u0000\u0000\u03e8\u03d9"+
		"\u0001\u0000\u0000\u0000\u03e8\u03da\u0001\u0000\u0000\u0000\u03e8\u03db"+
		"\u0001\u0000\u0000\u0000\u03e8\u03dc\u0001\u0000\u0000\u0000\u03e8\u03dd"+
		"\u0001\u0000\u0000\u0000\u03e8\u03de\u0001\u0000\u0000\u0000\u03e8\u03e0"+
		"\u0001\u0000\u0000\u0000\u03e8\u03e1\u0001\u0000\u0000\u0000\u03e8\u03e3"+
		"\u0001\u0000\u0000\u0000\u03e8\u03e4\u0001\u0000\u0000\u0000\u03e8\u03e6"+
		"\u0001\u0000\u0000\u0000\u03e8\u03e7\u0001\u0000\u0000\u0000\u03e9G\u0001"+
		"\u0000\u0000\u0000\u03ea\u03eb\u0005z\u0000\u0000\u03eb\u03ec\u0003\u0080"+
		"@\u0000\u03ec\u03ed\u0005\u000e\u0000\u0000\u03ed\u03fa\u0005\u0085\u0000"+
		"\u0000\u03ee\u03ef\u0005\u015d\u0000\u0000\u03ef\u03f0\u0005\u010f\u0000"+
		"\u0000\u03f0\u03f1\u0005\u013e\u0000\u0000\u03f1\u03fb\u0003z=\u0000\u03f2"+
		"\u03f3\u0005\u0089\u0000\u0000\u03f3\u03f4\u0005\u001f\u0000\u0000\u03f4"+
		"\u03fb\u0003z=\u0000\u03f5\u03f7\u0005\u00c1\u0000\u0000\u03f6\u03f5\u0001"+
		"\u0000\u0000\u0000\u03f6\u03f7\u0001\u0000\u0000\u0000\u03f7\u03f8\u0001"+
		"\u0000\u0000\u0000\u03f8\u03f9\u0005H\u0000\u0000\u03f9\u03fb\u0005\u015e"+
		"\u0000\u0000\u03fa\u03ee\u0001\u0000\u0000\u0000\u03fa\u03f2\u0001\u0000"+
		"\u0000\u0000\u03fa\u03f6\u0001\u0000\u0000\u0000\u03fa\u03fb\u0001\u0000"+
		"\u0000\u0000\u03fbI\u0001\u0000\u0000\u0000\u03fc\u03fd\u0005z\u0000\u0000"+
		"\u03fd\u03fe\u0005\n\u0000\u0000\u03fe\u03ff\u0005\u000e\u0000\u0000\u03ff"+
		"\u0400\u0005\u015d\u0000\u0000\u0400\u0401\u0003\u0142\u00a1\u0000\u0401"+
		"\u0402\u0005\u015e\u0000\u0000\u0402K\u0001\u0000\u0000\u0000\u0403\u0404"+
		"\u00056\u0000\u0000\u0404\u0406\u0003\u00e4r\u0000\u0405\u0403\u0001\u0000"+
		"\u0000\u0000\u0405\u0406\u0001\u0000\u0000\u0000\u0406\u0412\u0001\u0000"+
		"\u0000\u0000\u0407\u0408\u0005\u00c3\u0000\u0000\u0408\u0413\u0005\u00c4"+
		"\u0000\u0000\u0409\u040a\u0005+\u0000\u0000\u040a\u040b\u0005\u015d\u0000"+
		"\u0000\u040b\u040c\u0003$\u0012\u0000\u040c\u040d\u0005\u015e\u0000\u0000"+
		"\u040d\u0413\u0001\u0000\u0000\u0000\u040e\u040f\u0005\u00dd\u0000\u0000"+
		"\u040f\u0413\u0005\u009b\u0000\u0000\u0410\u0413\u0005\u012c\u0000\u0000"+
		"\u0411\u0413\u0003P(\u0000\u0412\u0407\u0001\u0000\u0000\u0000\u0412\u0409"+
		"\u0001\u0000\u0000\u0000\u0412\u040e\u0001\u0000\u0000\u0000\u0412\u0410"+
		"\u0001\u0000\u0000\u0000\u0412\u0411\u0001\u0000\u0000\u0000\u0413\u0415"+
		"\u0001\u0000\u0000\u0000\u0414\u0416\u0003T*\u0000\u0415\u0414\u0001\u0000"+
		"\u0000\u0000\u0415\u0416\u0001\u0000\u0000\u0000\u0416M\u0001\u0000\u0000"+
		"\u0000\u0417\u0418\u00056\u0000\u0000\u0418\u041a\u0003\u00e4r\u0000\u0419"+
		"\u0417\u0001\u0000\u0000\u0000\u0419\u041a\u0001\u0000\u0000\u0000\u041a"+
		"\u0434\u0001\u0000\u0000\u0000\u041b\u041c\u0005+\u0000\u0000\u041c\u041d"+
		"\u0005\u015d\u0000\u0000\u041d\u041e\u0003$\u0012\u0000\u041e\u041f\u0005"+
		"\u015e\u0000\u0000\u041f\u0435\u0001\u0000\u0000\u0000\u0420\u0421\u0005"+
		"\u00dd\u0000\u0000\u0421\u0422\u0005\u009b\u0000\u0000\u0422\u0423\u0005"+
		"\u015d\u0000\u0000\u0423\u0424\u0003X,\u0000\u0424\u0425\u0005\u015e\u0000"+
		"\u0000\u0425\u0433\u0001\u0000\u0000\u0000\u0426\u0427\u0005\u012c\u0000"+
		"\u0000\u0427\u0428\u0005\u015d\u0000\u0000\u0428\u0429\u0003X,\u0000\u0429"+
		"\u042a\u0005\u015e\u0000\u0000\u042a\u0433\u0001\u0000\u0000\u0000\u042b"+
		"\u042c\u0005u\u0000\u0000\u042c\u042d\u0005\u009b\u0000\u0000\u042d\u042e"+
		"\u0005\u015d\u0000\u0000\u042e\u042f\u0003X,\u0000\u042f\u0430\u0005\u015e"+
		"\u0000\u0000\u0430\u0431\u0003P(\u0000\u0431\u0433\u0001\u0000\u0000\u0000"+
		"\u0432\u0420\u0001\u0000\u0000\u0000\u0432\u0426\u0001\u0000\u0000\u0000"+
		"\u0432\u042b\u0001\u0000\u0000\u0000\u0433\u0435\u0001\u0000\u0000\u0000"+
		"\u0434\u041b\u0001\u0000\u0000\u0000\u0434\u0432\u0001\u0000\u0000\u0000"+
		"\u0435\u0437\u0001\u0000\u0000\u0000\u0436\u0438\u0003T*\u0000\u0437\u0436"+
		"\u0001\u0000\u0000\u0000\u0437\u0438\u0001\u0000\u0000\u0000\u0438O\u0001"+
		"\u0000\u0000\u0000\u0439\u043a\u0005\u00e6\u0000\u0000\u043a\u043f\u0003"+
		"\u00dcn\u0000\u043b\u043c\u0005\u015d\u0000\u0000\u043c\u043d\u0003X,"+
		"\u0000\u043d\u043e\u0005\u015e\u0000\u0000\u043e\u0440\u0001\u0000\u0000"+
		"\u0000\u043f\u043b\u0001\u0000\u0000\u0000\u043f\u0440\u0001\u0000\u0000"+
		"\u0000\u0440\u044b\u0001\u0000\u0000\u0000\u0441\u0442\u0005\u00cc\u0000"+
		"\u0000\u0442\u0449\u0005U\u0000\u0000\u0443\u0444\u0005\u00c1\u0000\u0000"+
		"\u0444\u044a\u0005\u0004\u0000\u0000\u0445\u044a\u0005\u00ec\u0000\u0000"+
		"\u0446\u044a\u0005\"\u0000\u0000\u0447\u0448\u0005\u0101\u0000\u0000\u0448"+
		"\u044a\u0005\u00c4\u0000\u0000\u0449\u0443\u0001\u0000\u0000\u0000\u0449"+
		"\u0445\u0001\u0000\u0000\u0000\u0449\u0446\u0001\u0000\u0000\u0000\u0449"+
		"\u0447\u0001\u0000\u0000\u0000\u044a\u044c\u0001\u0000\u0000\u0000\u044b"+
		"\u0441\u0001\u0000\u0000\u0000\u044b\u044c\u0001\u0000\u0000\u0000\u044c"+
		"\u0450\u0001\u0000\u0000\u0000\u044d\u044e\u0005\u00cc\u0000\u0000\u044e"+
		"\u044f\u0005\u012e\u0000\u0000\u044f\u0451\u0003R)\u0000\u0450\u044d\u0001"+
		"\u0000\u0000\u0000\u0450\u0451\u0001\u0000\u0000\u0000\u0451\u0464\u0001"+
		"\u0000\u0000\u0000\u0452\u0453\u0005\u00cc\u0000\u0000\u0453\u0454\u0005"+
		"\u012e\u0000\u0000\u0454\u0456\u0003R)\u0000\u0455\u0452\u0001\u0000\u0000"+
		"\u0000\u0455\u0456\u0001\u0000\u0000\u0000\u0456\u0461\u0001\u0000\u0000"+
		"\u0000\u0457\u0458\u0005\u00cc\u0000\u0000\u0458\u045f\u0005U\u0000\u0000"+
		"\u0459\u045a\u0005\u00c1\u0000\u0000\u045a\u0460\u0005\u0004\u0000\u0000"+
		"\u045b\u0460\u0005\u00ec\u0000\u0000\u045c\u0460\u0005\"\u0000\u0000\u045d"+
		"\u045e\u0005\u0101\u0000\u0000\u045e\u0460\u0005\u00c4\u0000\u0000\u045f"+
		"\u0459\u0001\u0000\u0000\u0000\u045f\u045b\u0001\u0000\u0000\u0000\u045f"+
		"\u045c\u0001\u0000\u0000\u0000\u045f\u045d\u0001\u0000\u0000\u0000\u0460"+
		"\u0462\u0001\u0000\u0000\u0000\u0461\u0457\u0001\u0000\u0000\u0000\u0461"+
		"\u0462\u0001\u0000\u0000\u0000\u0462\u0464\u0001\u0000\u0000\u0000\u0463"+
		"\u0439\u0001\u0000\u0000\u0000\u0463\u0455\u0001\u0000\u0000\u0000\u0464"+
		"Q\u0001\u0000\u0000\u0000\u0465\u0466\u0005\u00c1\u0000\u0000\u0466\u0469"+
		"\u0005\u0004\u0000\u0000\u0467\u0469\u0005\u00ec\u0000\u0000\u0468\u0465"+
		"\u0001\u0000\u0000\u0000\u0468\u0467\u0001\u0000\u0000\u0000\u0469S\u0001"+
		"\u0000\u0000\u0000\u046a\u046f\u0003V+\u0000\u046b\u046d\u0005\u00c3\u0000"+
		"\u0000\u046c\u046b\u0001\u0000\u0000\u0000\u046c\u046d\u0001\u0000\u0000"+
		"\u0000\u046d\u046e\u0001\u0000\u0000\u0000\u046e\u0470\u0005Q\u0000\u0000"+
		"\u046f\u046c\u0001\u0000\u0000\u0000\u046f\u0470\u0001\u0000\u0000\u0000"+
		"\u0470\u0479\u0001\u0000\u0000\u0000\u0471\u0473\u0005\u00c3\u0000\u0000"+
		"\u0472\u0471\u0001\u0000\u0000\u0000\u0472\u0473\u0001\u0000\u0000\u0000"+
		"\u0473\u0474\u0001\u0000\u0000\u0000\u0474\u0476\u0005Q\u0000\u0000\u0475"+
		"\u0477\u0003V+\u0000\u0476\u0475\u0001\u0000\u0000\u0000\u0476\u0477\u0001"+
		"\u0000\u0000\u0000\u0477\u0479\u0001\u0000\u0000\u0000\u0478\u046a\u0001"+
		"\u0000\u0000\u0000\u0478\u0472\u0001\u0000\u0000\u0000\u0479U\u0001\u0000"+
		"\u0000\u0000\u047a\u047b\u0005\u008c\u0000\u0000\u047b\u047f\u0005R\u0000"+
		"\u0000\u047c\u047d\u0005\u008c\u0000\u0000\u047d\u047f\u0005\u0087\u0000"+
		"\u0000\u047e\u047a\u0001\u0000\u0000\u0000\u047e\u047c\u0001\u0000\u0000"+
		"\u0000\u047fW\u0001\u0000\u0000\u0000\u0480\u0485\u0003\u00be_\u0000\u0481"+
		"\u0482\u0005\u015f\u0000\u0000\u0482\u0484\u0003\u00be_\u0000\u0483\u0481"+
		"\u0001\u0000\u0000\u0000\u0484\u0487\u0001\u0000\u0000\u0000\u0485\u0483"+
		"\u0001\u0000\u0000\u0000\u0485\u0486\u0001\u0000\u0000\u0000\u0486Y\u0001"+
		"\u0000\u0000\u0000\u0487\u0485\u0001\u0000\u0000\u0000\u0488\u048a\u0005"+
		"\u0127\u0000\u0000\u0489\u048b\u0005\u0118\u0000\u0000\u048a\u0489\u0001"+
		"\u0000\u0000\u0000\u048a\u048b\u0001\u0000\u0000\u0000\u048b\u048c\u0001"+
		"\u0000\u0000\u0000\u048c\u048d\u0003\u00dcn\u0000\u048d[\u0001\u0000\u0000"+
		"\u0000\u048e\u048f\u0005\u0007\u0000\u0000\u048f\u0492\u0005\u00df\u0000"+
		"\u0000\u0490\u0492\u0003^/\u0000\u0491\u048e\u0001\u0000\u0000\u0000\u0491"+
		"\u0490\u0001\u0000\u0000\u0000\u0492]\u0001\u0000\u0000\u0000\u0493\u0498"+
		"\u0003`0\u0000\u0494\u0495\u0005\u015f\u0000\u0000\u0495\u0497\u0003`"+
		"0\u0000\u0496\u0494\u0001\u0000\u0000\u0000\u0497\u049a\u0001\u0000\u0000"+
		"\u0000\u0498\u0496\u0001\u0000\u0000\u0000\u0498\u0499\u0001\u0000\u0000"+
		"\u0000\u0499_\u0001\u0000\u0000\u0000\u049a\u0498\u0001\u0000\u0000\u0000"+
		"\u049b\u04ab\u0005U\u0000\u0000\u049c\u04ab\u0005\u0091\u0000\u0000\u049d"+
		"\u049f\u0005\u00e6\u0000\u0000\u049e\u04a0\u0003b1\u0000\u049f\u049e\u0001"+
		"\u0000\u0000\u0000\u049f\u04a0\u0001\u0000\u0000\u0000\u04a0\u04ab\u0001"+
		"\u0000\u0000\u0000\u04a1\u04a3\u0005\u00fd\u0000\u0000\u04a2\u04a4\u0003"+
		"b1\u0000\u04a3\u04a2\u0001\u0000\u0000\u0000\u04a3\u04a4\u0001\u0000\u0000"+
		"\u0000\u04a4\u04ab\u0001\u0000\u0000\u0000\u04a5\u04ab\u0005\u0124\u0000"+
		"\u0000\u04a6\u04a8\u0005\u012e\u0000\u0000\u04a7\u04a9\u0003b1\u0000\u04a8"+
		"\u04a7\u0001\u0000\u0000\u0000\u04a8\u04a9\u0001\u0000\u0000\u0000\u04a9"+
		"\u04ab\u0001\u0000\u0000\u0000\u04aa\u049b\u0001\u0000\u0000\u0000\u04aa"+
		"\u049c\u0001\u0000\u0000\u0000\u04aa\u049d\u0001\u0000\u0000\u0000\u04aa"+
		"\u04a1\u0001\u0000\u0000\u0000\u04aa\u04a5\u0001\u0000\u0000\u0000\u04aa"+
		"\u04a6\u0001\u0000\u0000\u0000\u04aba\u0001\u0000\u0000\u0000\u04ac\u04ad"+
		"\u0005\u015d\u0000\u0000\u04ad\u04b2\u0003d2\u0000\u04ae\u04af\u0005\u015f"+
		"\u0000\u0000\u04af\u04b1\u0003d2\u0000\u04b0\u04ae\u0001\u0000\u0000\u0000"+
		"\u04b1\u04b4\u0001\u0000\u0000\u0000\u04b2\u04b0\u0001\u0000\u0000\u0000"+
		"\u04b2\u04b3\u0001\u0000\u0000\u0000\u04b3\u04b5\u0001\u0000\u0000\u0000"+
		"\u04b4\u04b2\u0001\u0000\u0000\u0000\u04b5\u04b6\u0005\u015e\u0000\u0000"+
		"\u04b6c\u0001\u0000\u0000\u0000\u04b7\u04b8\u0003\u0100\u0080\u0000\u04b8"+
		"e\u0001\u0000\u0000\u0000\u04b9\u04be\u0003h4\u0000\u04ba\u04bb\u0005"+
		"\u015f\u0000\u0000\u04bb\u04bd\u0003h4\u0000\u04bc\u04ba\u0001\u0000\u0000"+
		"\u0000\u04bd\u04c0\u0001\u0000\u0000\u0000\u04be\u04bc\u0001\u0000\u0000"+
		"\u0000\u04be\u04bf\u0001\u0000\u0000\u0000\u04bfg\u0001\u0000\u0000\u0000"+
		"\u04c0\u04be\u0001\u0000\u0000\u0000\u04c1\u04c5\u0003j5\u0000\u04c2\u04c5"+
		"\u0003\u00e0p\u0000\u04c3\u04c5\u0005\u00e1\u0000\u0000\u04c4\u04c1\u0001"+
		"\u0000\u0000\u0000\u04c4\u04c2\u0001\u0000\u0000\u0000\u04c4\u04c3\u0001"+
		"\u0000\u0000\u0000\u04c5i\u0001\u0000\u0000\u0000\u04c6\u04c7\u0003\u0100"+
		"\u0080\u0000\u04c7k\u0001\u0000\u0000\u0000\u04c8\u04c9\u0003n7\u0000"+
		"\u04c9m\u0001\u0000\u0000\u0000\u04ca\u04cb\u0005\t\u0000\u0000\u04cb"+
		"\u04cc\u0005\u0118\u0000\u0000\u04cc\u04cd\u0003\u00dcn\u0000\u04cd\u04ce"+
		"\u0005\u0005\u0000\u0000\u04ce\u04cf\u00051\u0000\u0000\u04cf\u04d0\u0003"+
		"B!\u0000\u04d0\u04f1\u0001\u0000\u0000\u0000\u04d1\u04d2\u0005\u0005\u0000"+
		"\u0000\u04d2\u04f1\u0003p8\u0000\u04d3\u04d5\u0005`\u0000\u0000\u04d4"+
		"\u04d6\u00051\u0000\u0000\u04d5\u04d4\u0001\u0000\u0000\u0000\u04d5\u04d6"+
		"\u0001\u0000\u0000\u0000\u04d6\u04d7\u0001\u0000\u0000\u0000\u04d7\u04d9"+
		"\u0003\u00e6s\u0000\u04d8\u04da\u0003r9\u0000\u04d9\u04d8\u0001\u0000"+
		"\u0000\u0000\u04d9\u04da\u0001\u0000\u0000\u0000\u04da\u04f1\u0001\u0000"+
		"\u0000\u0000\u04db\u04e7\u0005`\u0000\u0000\u04dc\u04dd\u0005\u00dd\u0000"+
		"\u0000\u04dd\u04e8\u0005\u009b\u0000\u0000\u04de\u04df\u0005u\u0000\u0000"+
		"\u04df\u04e0\u0005\u009b\u0000\u0000\u04e0\u04e8\u0003\u00e4r\u0000\u04e1"+
		"\u04e2\u0005\u012c\u0000\u0000\u04e2\u04e8\u0003\u00e4r\u0000\u04e3\u04e4"+
		"\u0005+\u0000\u0000\u04e4\u04e8\u0003\u00e4r\u0000\u04e5\u04e6\u00056"+
		"\u0000\u0000\u04e6\u04e8\u0003\u00e4r\u0000\u04e7\u04dc\u0001\u0000\u0000"+
		"\u0000\u04e7\u04de\u0001\u0000\u0000\u0000\u04e7\u04e1\u0001\u0000\u0000"+
		"\u0000\u04e7\u04e3\u0001\u0000\u0000\u0000\u04e7\u04e5\u0001\u0000\u0000"+
		"\u0000\u04e8\u04f1\u0001\u0000\u0000\u0000\u04e9\u04eb\u0005\t\u0000\u0000"+
		"\u04ea\u04ec\u00051\u0000\u0000\u04eb\u04ea\u0001\u0000\u0000\u0000\u04eb"+
		"\u04ec\u0001\u0000\u0000\u0000\u04ec\u04ed\u0001\u0000\u0000\u0000\u04ed"+
		"\u04f1\u0003v;\u0000\u04ee\u04ef\u0005\u00a8\u0000\u0000\u04ef\u04f1\u0003"+
		"t:\u0000\u04f0\u04ca\u0001\u0000\u0000\u0000\u04f0\u04d1\u0001\u0000\u0000"+
		"\u0000\u04f0\u04d3\u0001\u0000\u0000\u0000\u04f0\u04db\u0001\u0000\u0000"+
		"\u0000\u04f0\u04e9\u0001\u0000\u0000\u0000\u04f0\u04ee\u0001\u0000\u0000"+
		"\u0000\u04f1o\u0001\u0000\u0000\u0000\u04f2\u04f5\u0003L&\u0000\u04f3"+
		"\u04f5\u0003N\'\u0000\u04f4\u04f2\u0001\u0000\u0000\u0000\u04f4\u04f3"+
		"\u0001\u0000\u0000\u0000\u04f5q\u0001\u0000\u0000\u0000\u04f6\u04f7\u0007"+
		"\t\u0000\u0000\u04f7s\u0001\u0000\u0000\u0000\u04f8\u04f9\u0007\n\u0000"+
		"\u0000\u04f9u\u0001\u0000\u0000\u0000\u04fa\u04fb\u0003\u00e6s\u0000\u04fb"+
		"\u04fc\u0005\u0101\u0000\u0000\u04fc\u04fd\u0005I\u0000\u0000\u04fd\u04fe"+
		"\u0005\u0128\u0000\u0000\u04fe\u04ff\u0005\u001c\u0000\u0000\u04ff\u0500"+
		"\u0003x<\u0000\u0500\u0547\u0001\u0000\u0000\u0000\u0501\u0502\u0003\u00e6"+
		"s\u0000\u0502\u0503\u0005\u0101\u0000\u0000\u0503\u0504\u0005I\u0000\u0000"+
		"\u0504\u0505\u0005\u0128\u0000\u0000\u0505\u0506\u0005,\u0000\u0000\u0506"+
		"\u0507\u0003x<\u0000\u0507\u0547\u0001\u0000\u0000\u0000\u0508\u0509\u0003"+
		"\u00e6s\u0000\u0509\u050a\u0005\u0101\u0000\u0000\u050a\u050b\u0005I\u0000"+
		"\u0000\u050b\u050c\u0005\u0128\u0000\u0000\u050c\u050d\u0005\u0137\u0000"+
		"\u0000\u050d\u050e\u0003x<\u0000\u050e\u0547\u0001\u0000\u0000\u0000\u050f"+
		"\u0510\u0003\u00e6s\u0000\u0510\u0511\u0005\u0101\u0000\u0000\u0511\u0512"+
		"\u0005I\u0000\u0000\u0512\u0513\u0005\u0128\u0000\u0000\u0513\u0514\u0005"+
		"\u0137\u0000\u0000\u0514\u0515\u0003x<\u0000\u0515\u0516\u0005t\u0000"+
		"\u0000\u0516\u0517\u0005\u001b\u0000\u0000\u0517\u0518\u0005I\u0000\u0000"+
		"\u0518\u0547\u0001\u0000\u0000\u0000\u0519\u051a\u0003\u00e6s\u0000\u051a"+
		"\u051b\u0005\u0101\u0000\u0000\u051b\u051c\u0005\u0089\u0000\u0000\u051c"+
		"\u051d\u0005\u001f\u0000\u0000\u051d\u051e\u0003z=\u0000\u051e\u0547\u0001"+
		"\u0000\u0000\u0000\u051f\u0520\u0003\u00e6s\u0000\u0520\u0521\u0005\u00eb"+
		"\u0000\u0000\u0521\u0522\u0005\u013e\u0000\u0000\u0522\u0523\u0003z=\u0000"+
		"\u0523\u0547\u0001\u0000\u0000\u0000\u0524\u0525\u0003\u00e6s\u0000\u0525"+
		"\u0526\u0005\u0101\u0000\u0000\u0526\u0527\u0005z\u0000\u0000\u0527\u0528"+
		"\u0003\u0080@\u0000\u0528\u0547\u0001\u0000\u0000\u0000\u0529\u052a\u0003"+
		"\u00e6s\u0000\u052a\u052b\u0003\u0082A\u0000\u052b\u052c\u0005\u00c3\u0000"+
		"\u0000\u052c\u052d\u0005\u00c4\u0000\u0000\u052d\u0547\u0001\u0000\u0000"+
		"\u0000\u052e\u0530\u0003\u00e6s\u0000\u052f\u0531\u0005\u00c3\u0000\u0000"+
		"\u0530\u052f\u0001\u0000\u0000\u0000\u0530\u0531\u0001\u0000\u0000\u0000"+
		"\u0531\u0532\u0001\u0000\u0000\u0000\u0532\u0533\u0005\u00c4\u0000\u0000"+
		"\u0533\u0547\u0001\u0000\u0000\u0000\u0534\u0536\u0003\u00e6s\u0000\u0535"+
		"\u0537\u0003\u0084B\u0000\u0536\u0535\u0001\u0000\u0000\u0000\u0536\u0537"+
		"\u0001\u0000\u0000\u0000\u0537\u0538\u0001\u0000\u0000\u0000\u0538\u0539"+
		"\u0005P\u0000\u0000\u0539\u053a\u0003|>\u0000\u053a\u0547\u0001\u0000"+
		"\u0000\u0000\u053b\u053c\u0003\u00e6s\u0000\u053c\u053e\u0005\u0101\u0000"+
		"\u0000\u053d\u053f\u0005\u00c1\u0000\u0000\u053e\u053d\u0001\u0000\u0000"+
		"\u0000\u053e\u053f\u0001\u0000\u0000\u0000\u053f\u0540\u0001\u0000\u0000"+
		"\u0000\u0540\u0541\u0005H\u0000\u0000\u0541\u0547\u0001\u0000\u0000\u0000"+
		"\u0542\u0543\u0003\u00e6s\u0000\u0543\u0544\u0005`\u0000\u0000\u0544\u0545"+
		"\u0005P\u0000\u0000\u0545\u0547\u0001\u0000\u0000\u0000\u0546\u04fa\u0001"+
		"\u0000\u0000\u0000\u0546\u0501\u0001\u0000\u0000\u0000\u0546\u0508\u0001"+
		"\u0000\u0000\u0000\u0546\u050f\u0001\u0000\u0000\u0000\u0546\u0519\u0001"+
		"\u0000\u0000\u0000\u0546\u051f\u0001\u0000\u0000\u0000\u0546\u0524\u0001"+
		"\u0000\u0000\u0000\u0546\u0529\u0001\u0000\u0000\u0000\u0546\u052e\u0001"+
		"\u0000\u0000\u0000\u0546\u0534\u0001\u0000\u0000\u0000\u0546\u053b\u0001"+
		"\u0000\u0000\u0000\u0546\u0542\u0001\u0000\u0000\u0000\u0547w\u0001\u0000"+
		"\u0000\u0000\u0548\u0549\u0003\u0102\u0081\u0000\u0549y\u0001\u0000\u0000"+
		"\u0000\u054a\u054b\u0003\u0102\u0081\u0000\u054b{\u0001\u0000\u0000\u0000"+
		"\u054c\u054d\u0003~?\u0000\u054d}\u0001\u0000\u0000\u0000\u054e\u0552"+
		"\u0003\u0102\u0081\u0000\u054f\u0552\u0003\u00dam\u0000\u0550\u0552\u0005"+
		"\u00c4\u0000\u0000\u0551\u054e\u0001\u0000\u0000\u0000\u0551\u054f\u0001"+
		"\u0000\u0000\u0000\u0551\u0550\u0001\u0000\u0000\u0000\u0552\u007f\u0001"+
		"\u0000\u0000\u0000\u0553\u0557\u0005\n\u0000\u0000\u0554\u0555\u0005\u001f"+
		"\u0000\u0000\u0555\u0557\u0005P\u0000\u0000\u0556\u0553\u0001\u0000\u0000"+
		"\u0000\u0556\u0554\u0001\u0000\u0000\u0000\u0557\u0081\u0001\u0000\u0000"+
		"\u0000\u0558\u0559\u0007\u000b\u0000\u0000\u0559\u0083\u0001\u0000\u0000"+
		"\u0000\u055a\u055b\u0007\f\u0000\u0000\u055b\u0085\u0001\u0000\u0000\u0000"+
		"\u055c\u0569\u0003\u0088D\u0000\u055d\u0569\u0003\u008cF\u0000\u055e\u0569"+
		"\u0003\u0098L\u0000\u055f\u0569\u0003\u009aM\u0000\u0560\u0569\u0003\u00a2"+
		"Q\u0000\u0561\u0569\u0003\u00a4R\u0000\u0562\u0569\u0003\u00a6S\u0000"+
		"\u0563\u0569\u0003\u00acV\u0000\u0564\u0569\u0003\u00aeW\u0000\u0565\u0569"+
		"\u0003\u00b0X\u0000\u0566\u0569\u0003\u00b2Y\u0000\u0567\u0569\u0003\u00b6"+
		"[\u0000\u0568\u055c\u0001\u0000\u0000\u0000\u0568\u055d\u0001\u0000\u0000"+
		"\u0000\u0568\u055e\u0001\u0000\u0000\u0000\u0568\u055f\u0001\u0000\u0000"+
		"\u0000\u0568\u0560\u0001\u0000\u0000\u0000\u0568\u0561\u0001\u0000\u0000"+
		"\u0000\u0568\u0562\u0001\u0000\u0000\u0000\u0568\u0563\u0001\u0000\u0000"+
		"\u0000\u0568\u0564\u0001\u0000\u0000\u0000\u0568\u0565\u0001\u0000\u0000"+
		"\u0000\u0568\u0566\u0001\u0000\u0000\u0000\u0568\u0567\u0001\u0000\u0000"+
		"\u0000\u0569\u0087\u0001\u0000\u0000\u0000\u056a\u056b\u0005>\u0000\u0000"+
		"\u056b\u056c\u0005V\u0000\u0000\u056c\u056d\u0005\u0006\u0000\u0000\u056d"+
		"\u056e\u0003\u00e2q\u0000\u056e\u056f\u0005t\u0000\u0000\u056f\u0570\u0003"+
		"\u008aE\u0000\u0570\u0571\u0005\u00ee\u0000\u0000\u0571\u0572\u0003\u008a"+
		"E\u0000\u0572\u0573\u0001\u0000\u0000\u0000\u0573\u0574\u0005n\u0000\u0000"+
		"\u0574\u0575\u0005\u00bc\u0000\u0000\u0575\u0576\u0003\u00dam\u0000\u0576"+
		"\u0089\u0001\u0000\u0000\u0000\u0577\u0578\u0003\u009eO\u0000\u0578\u008b"+
		"\u0001\u0000\u0000\u0000\u0579\u057a\u0005>\u0000\u0000\u057a\u057b\u0005"+
		"y\u0000\u0000\u057b\u057c\u0003\u00eau\u0000\u057c\u057d\u0005\u015d\u0000"+
		"\u0000\u057d\u0582\u0003\u008eG\u0000\u057e\u057f\u0005\u015f\u0000\u0000"+
		"\u057f\u0581\u0003\u008eG\u0000\u0580\u057e\u0001\u0000\u0000\u0000\u0581"+
		"\u0584\u0001\u0000\u0000\u0000\u0582\u0580\u0001\u0000\u0000\u0000\u0582"+
		"\u0583\u0001\u0000\u0000\u0000\u0583\u0585\u0001\u0000\u0000\u0000\u0584"+
		"\u0582\u0001\u0000\u0000\u0000\u0585\u0586\u0005\u015e\u0000\u0000\u0586"+
		"\u0587\u0005\u00ee\u0000\u0000\u0587\u058b\u0003\u0090H\u0000\u0588\u058a"+
		"\u0003\u0096K\u0000\u0589\u0588\u0001\u0000\u0000\u0000\u058a\u058d\u0001"+
		"\u0000\u0000\u0000\u058b\u0589\u0001\u0000\u0000\u0000\u058b\u058c\u0001"+
		"\u0000\u0000\u0000\u058c\u008d\u0001\u0000\u0000\u0000\u058d\u058b\u0001"+
		"\u0000\u0000\u0000\u058e\u0590\u0003\u00f0x\u0000\u058f\u058e\u0001\u0000"+
		"\u0000\u0000\u058f\u0590\u0001\u0000\u0000\u0000\u0590\u0591\u0001\u0000"+
		"\u0000\u0000\u0591\u0592\u0003\u009eO\u0000\u0592\u008f\u0001\u0000\u0000"+
		"\u0000\u0593\u0596\u0003\u0092I\u0000\u0594\u0596\u0003\u009eO\u0000\u0595"+
		"\u0593\u0001\u0000\u0000\u0000\u0595\u0594\u0001\u0000\u0000\u0000\u0596"+
		"\u0091\u0001\u0000\u0000\u0000\u0597\u0598\u0005\u0118\u0000\u0000\u0598"+
		"\u0599\u0005\u015d\u0000\u0000\u0599\u059a\u0003\u0094J\u0000\u059a\u059b"+
		"\u0005\u015f\u0000\u0000\u059b\u059c\u0003\u0094J\u0000\u059c\u059d\u0001"+
		"\u0000\u0000\u0000\u059d\u059e\u0005\u015e\u0000\u0000\u059e\u0093\u0001"+
		"\u0000\u0000\u0000\u059f\u05a0\u0003\u0100\u0080\u0000\u05a0\u05a1\u0003"+
		"\u009eO\u0000\u05a1\u0095\u0001\u0000\u0000\u0000\u05a2\u05a3\u0005\u009d"+
		"\u0000\u0000\u05a3\u05c7\u0005\u0099\u0000\u0000\u05a4\u05a8\u0005Z\u0000"+
		"\u0000\u05a5\u05a6\u0005\u00c3\u0000\u0000\u05a6\u05a8\u0005Z\u0000\u0000"+
		"\u05a7\u05a4\u0001\u0000\u0000\u0000\u05a7\u05a5\u0001\u0000\u0000\u0000"+
		"\u05a8\u05c7\u0001\u0000\u0000\u0000\u05a9\u05aa\u0005n\u0000\u0000\u05aa"+
		"\u05ab\u0005\u00bc\u0000\u0000\u05ab\u05c7\u0003\u00b4Z\u0000\u05ac\u05ad"+
		"\u0005\u00d7\u0000\u0000\u05ad\u05ae\u0005\u0112\u0000\u0000\u05ae\u05c7"+
		"\u0007\r\u0000\u0000\u05af\u05b0\u0005n\u0000\u0000\u05b0\u05b1\u0005"+
		"\u00fc\u0000\u0000\u05b1\u05c7\u0007\u000e\u0000\u0000\u05b2\u05b3\u0005"+
		"\u00c1\u0000\u0000\u05b3\u05ba\u0005\u0108\u0000\u0000\u05b4\u05b5\u0005"+
		"8\u0000\u0000\u05b5\u05ba\u0005\u0108\u0000\u0000\u05b6\u05b7\u0005\u00e4"+
		"\u0000\u0000\u05b7\u05b8\u0005\u0108\u0000\u0000\u05b8\u05ba\u0005I\u0000"+
		"\u0000\u05b9\u05b2\u0001\u0000\u0000\u0000\u05b9\u05b4\u0001\u0000\u0000"+
		"\u0000\u05b9\u05b6\u0001\u0000\u0000\u0000\u05ba\u05c7\u0001\u0000\u0000"+
		"\u0000\u05bb\u05bc\u0005\u00ee\u0000\u0000\u05bc\u05bd\u0005\u00c4\u0000"+
		"\u0000\u05bd\u05be\u0005\u00cc\u0000\u0000\u05be\u05bf\u0005\u00c4\u0000"+
		"\u0000\u05bf\u05c5\u0005\u008f\u0000\u0000\u05c0\u05c1\u0005!\u0000\u0000"+
		"\u05c1\u05c2\u0005\u00cc\u0000\u0000\u05c2\u05c3\u0005\u00c4\u0000\u0000"+
		"\u05c3\u05c5\u0005\u008f\u0000\u0000\u05c4\u05bb\u0001\u0000\u0000\u0000"+
		"\u05c4\u05c0\u0001\u0000\u0000\u0000\u05c5\u05c7\u0001\u0000\u0000\u0000"+
		"\u05c6\u05a2\u0001\u0000\u0000\u0000\u05c6\u05a7\u0001\u0000\u0000\u0000"+
		"\u05c6\u05a9\u0001\u0000\u0000\u0000\u05c6\u05ac\u0001\u0000\u0000\u0000"+
		"\u05c6\u05af\u0001\u0000\u0000\u0000\u05c6\u05b9\u0001\u0000\u0000\u0000"+
		"\u05c6\u05c4\u0001\u0000\u0000\u0000\u05c7\u0097\u0001\u0000\u0000\u0000"+
		"\u05c8\u05ca\u0005>\u0000\u0000\u05c9\u05cb\u0005\u012c\u0000\u0000\u05ca"+
		"\u05c9\u0001\u0000\u0000\u0000\u05ca\u05cb\u0001\u0000\u0000\u0000\u05cb"+
		"\u05cc\u0001\u0000\u0000\u0000\u05cc\u05cd\u0005\u008a\u0000\u0000\u05cd"+
		"\u05ce\u0003\u00ecv\u0000\u05ce\u05cf\u0005\u00cc\u0000\u0000\u05cf\u05d0"+
		"\u0003\u00dcn\u0000\u05d0\u05d1\u0005\u015d\u0000\u0000\u05d1\u05d3\u0003"+
		"\u00be_\u0000\u05d2\u05d4\u0003\u013e\u009f\u0000\u05d3\u05d2\u0001\u0000"+
		"\u0000\u0000\u05d3\u05d4\u0001\u0000\u0000\u0000\u05d4\u05dc\u0001\u0000"+
		"\u0000\u0000\u05d5\u05d6\u0005\u015f\u0000\u0000\u05d6\u05d8\u0003\u00be"+
		"_\u0000\u05d7\u05d9\u0003\u013e\u009f\u0000\u05d8\u05d7\u0001\u0000\u0000"+
		"\u0000\u05d8\u05d9\u0001\u0000\u0000\u0000\u05d9\u05db\u0001\u0000\u0000"+
		"\u0000\u05da\u05d5\u0001\u0000\u0000\u0000\u05db\u05de\u0001\u0000\u0000"+
		"\u0000\u05dc\u05da\u0001\u0000\u0000\u0000\u05dc\u05dd\u0001\u0000\u0000"+
		"\u0000\u05dd\u05df\u0001\u0000\u0000\u0000\u05de\u05dc\u0001\u0000\u0000"+
		"\u0000\u05df\u05e0\u0005\u015e\u0000\u0000\u05e0\u0099\u0001\u0000\u0000"+
		"\u0000\u05e1\u05e2\u0005>\u0000\u0000\u05e2\u05e3\u0005\u00e0\u0000\u0000"+
		"\u05e3\u05e4\u0003\u00eew\u0000\u05e4\u05e5\u0005\u015d\u0000\u0000\u05e5"+
		"\u05ea\u0003\u009cN\u0000\u05e6\u05e7\u0005\u015f\u0000\u0000\u05e7\u05e9"+
		"\u0003\u009cN\u0000\u05e8\u05e6\u0001\u0000\u0000\u0000\u05e9\u05ec\u0001"+
		"\u0000\u0000\u0000\u05ea\u05e8\u0001\u0000\u0000\u0000\u05ea\u05eb\u0001"+
		"\u0000\u0000\u0000\u05eb\u05ed\u0001\u0000\u0000\u0000\u05ec\u05ea\u0001"+
		"\u0000\u0000\u0000\u05ed\u05f1\u0005\u015e\u0000\u0000\u05ee\u05f0\u0003"+
		"\u00a0P\u0000\u05ef\u05ee\u0001\u0000\u0000\u0000\u05f0\u05f3\u0001\u0000"+
		"\u0000\u0000\u05f1\u05ef\u0001\u0000\u0000\u0000\u05f1\u05f2\u0001\u0000"+
		"\u0000\u0000\u05f2\u009b\u0001\u0000\u0000\u0000\u05f3\u05f1\u0001\u0000"+
		"\u0000\u0000\u05f4\u05f6\u0007\u000f\u0000\u0000\u05f5\u05f4\u0001\u0000"+
		"\u0000\u0000\u05f5\u05f6\u0001\u0000\u0000\u0000\u05f6\u05f8\u0001\u0000"+
		"\u0000\u0000\u05f7\u05f9\u0003\u00f0x\u0000\u05f8\u05f7\u0001\u0000\u0000"+
		"\u0000\u05f8\u05f9\u0001\u0000\u0000\u0000\u05f9\u05fa\u0001\u0000\u0000"+
		"\u0000\u05fa\u05fb\u0003\u009eO\u0000\u05fb\u009d\u0001\u0000\u0000\u0000"+
		"\u05fc\u0654\u0005\u0019\u0000\u0000\u05fd\u0602\u0005\u001c\u0000\u0000"+
		"\u05fe\u05ff\u0005\u001a\u0000\u0000\u05ff\u0600\u0005\u009e\u0000\u0000"+
		"\u0600\u0602\u0005\u00c9\u0000\u0000\u0601\u05fd\u0001\u0000\u0000\u0000"+
		"\u0601\u05fe\u0001\u0000\u0000\u0000\u0602\u060a\u0001\u0000\u0000\u0000"+
		"\u0603\u0604\u0005\u015d\u0000\u0000\u0604\u0606\u0003\u0102\u0081\u0000"+
		"\u0605\u0607\u0007\u0010\u0000\u0000\u0606\u0605\u0001\u0000\u0000\u0000"+
		"\u0606\u0607\u0001\u0000\u0000\u0000\u0607\u0608\u0001\u0000\u0000\u0000"+
		"\u0608\u0609\u0005\u015e\u0000\u0000\u0609\u060b\u0001\u0000\u0000\u0000"+
		"\u060a\u0603\u0001\u0000\u0000\u0000\u060a\u060b\u0001\u0000\u0000\u0000"+
		"\u060b\u0654\u0001\u0000\u0000\u0000\u060c\u0654\u0005\u001d\u0000\u0000"+
		"\u060d\u0612\u0007\u0011\u0000\u0000\u060e\u060f\u0005\u015d\u0000\u0000"+
		"\u060f\u0610\u0003\u0102\u0081\u0000\u0610\u0611\u0005\u015e\u0000\u0000"+
		"\u0611\u0613\u0001\u0000\u0000\u0000\u0612\u060e\u0001\u0000\u0000\u0000"+
		"\u0612\u0613\u0001\u0000\u0000\u0000\u0613\u0654\u0001\u0000\u0000\u0000"+
		"\u0614\u0619\u0005,\u0000\u0000\u0615\u0616\u0005)\u0000\u0000\u0616\u0617"+
		"\u0005\u009e\u0000\u0000\u0617\u0619\u0005\u00c9\u0000\u0000\u0618\u0614"+
		"\u0001\u0000\u0000\u0000\u0618\u0615\u0001\u0000\u0000\u0000\u0619\u0621"+
		"\u0001\u0000\u0000\u0000\u061a\u061b\u0005\u015d\u0000\u0000\u061b\u061d"+
		"\u0003\u0102\u0081\u0000\u061c\u061e\u0007\u0010\u0000\u0000\u061d\u061c"+
		"\u0001\u0000\u0000\u0000\u061d\u061e\u0001\u0000\u0000\u0000\u061e\u061f"+
		"\u0001\u0000\u0000\u0000\u061f\u0620\u0005\u015e\u0000\u0000\u0620\u0622"+
		"\u0001\u0000\u0000\u0000\u0621\u061a\u0001\u0000\u0000\u0000\u0621\u0622"+
		"\u0001\u0000\u0000\u0000\u0622\u0654\u0001\u0000\u0000\u0000\u0623\u0654"+
		"\u0005J\u0000\u0000\u0624\u062d\u0007\u0012\u0000\u0000\u0625\u0626\u0005"+
		"\u015d\u0000\u0000\u0626\u0629\u0003\u0102\u0081\u0000\u0627\u0628\u0005"+
		"\u015f\u0000\u0000\u0628\u062a\u0003\u0102\u0081\u0000\u0629\u0627\u0001"+
		"\u0000\u0000\u0000\u0629\u062a\u0001\u0000\u0000\u0000\u062a\u062b\u0001"+
		"\u0000\u0000\u0000\u062b\u062c\u0005\u015e\u0000\u0000\u062c\u062e\u0001"+
		"\u0000\u0000\u0000\u062d\u0625\u0001\u0000\u0000\u0000\u062d\u062e\u0001"+
		"\u0000\u0000\u0000\u062e\u0654\u0001\u0000\u0000\u0000\u062f\u0654\u0005"+
		"_\u0000\u0000\u0630\u0631\u0005_\u0000\u0000\u0631\u0654\u0005\u00da\u0000"+
		"\u0000\u0632\u0637\u0005r\u0000\u0000\u0633\u0634\u0005\u015d\u0000\u0000"+
		"\u0634\u0635\u0003\u0102\u0081\u0000\u0635\u0636\u0005\u015e\u0000\u0000"+
		"\u0636\u0638\u0001\u0000\u0000\u0000\u0637\u0633\u0001\u0000\u0000\u0000"+
		"\u0637\u0638\u0001\u0000\u0000\u0000\u0638\u0654\u0001\u0000\u0000\u0000"+
		"\u0639\u0654\u0007\u0013\u0000\u0000\u063a\u063b\u0005\u00ac\u0000\u0000"+
		"\u063b\u0654\u0005\u0137\u0000\u0000\u063c\u063d\u0005\u00ac\u0000\u0000"+
		"\u063d\u063e\u0005\u0137\u0000\u0000\u063e\u063f\u0005t\u0000\u0000\u063f"+
		"\u0640\u0005\u001b\u0000\u0000\u0640\u0654\u0005I\u0000\u0000\u0641\u0654"+
		"\u0005\u00e5\u0000\u0000\u0642\u0654\u0005\u0105\u0000\u0000\u0643\u0654"+
		"\u0005\u011c\u0000\u0000\u0644\u0654\u0005\u011d\u0000\u0000\u0645\u064b"+
		"\u0005\u0137\u0000\u0000\u0646\u0647\u0005(\u0000\u0000\u0647\u064b\u0005"+
		"\u0138\u0000\u0000\u0648\u0649\u0005)\u0000\u0000\u0649\u064b\u0005\u0138"+
		"\u0000\u0000\u064a\u0645\u0001\u0000\u0000\u0000\u064a\u0646\u0001\u0000"+
		"\u0000\u0000\u064a\u0648\u0001\u0000\u0000\u0000\u064b\u0650\u0001\u0000"+
		"\u0000\u0000\u064c\u064d\u0005\u015d\u0000\u0000\u064d\u064e\u0003\u0102"+
		"\u0081\u0000\u064e\u064f\u0005\u015e\u0000\u0000\u064f\u0651\u0001\u0000"+
		"\u0000\u0000\u0650\u064c\u0001\u0000\u0000\u0000\u0650\u0651\u0001\u0000"+
		"\u0000\u0000\u0651\u0654\u0001\u0000\u0000\u0000\u0652\u0654\u0005\u0141"+
		"\u0000\u0000\u0653\u05fc\u0001\u0000\u0000\u0000\u0653\u0601\u0001\u0000"+
		"\u0000\u0000\u0653\u060c\u0001\u0000\u0000\u0000\u0653\u060d\u0001\u0000"+
		"\u0000\u0000\u0653\u0618\u0001\u0000\u0000\u0000\u0653\u0623\u0001\u0000"+
		"\u0000\u0000\u0653\u0624\u0001\u0000\u0000\u0000\u0653\u062f\u0001\u0000"+
		"\u0000\u0000\u0653\u0630\u0001\u0000\u0000\u0000\u0653\u0632\u0001\u0000"+
		"\u0000\u0000\u0653\u0639\u0001\u0000\u0000\u0000\u0653\u063a\u0001\u0000"+
		"\u0000\u0000\u0653\u063c\u0001\u0000\u0000\u0000\u0653\u0641\u0001\u0000"+
		"\u0000\u0000\u0653\u0642\u0001\u0000\u0000\u0000\u0653\u0643\u0001\u0000"+
		"\u0000\u0000\u0653\u0644\u0001\u0000\u0000\u0000\u0653\u064a\u0001\u0000"+
		"\u0000\u0000\u0653\u0652\u0001\u0000\u0000\u0000\u0654\u009f\u0001\u0000"+
		"\u0000\u0000\u0655\u0657\u0005a\u0000\u0000\u0656\u0655\u0001\u0000\u0000"+
		"\u0000\u0656\u0657\u0001\u0000\u0000\u0000\u0657\u0658\u0001\u0000\u0000"+
		"\u0000\u0658\u0659\u0005\u00ed\u0000\u0000\u0659\u065a\u0005\u0102\u0000"+
		"\u0000\u065a\u0678\u0003x<\u0000\u065b\u065c\u0005\u009d\u0000\u0000\u065c"+
		"\u0678\u0005\u0099\u0000\u0000\u065d\u0661\u0005Z\u0000\u0000\u065e\u065f"+
		"\u0005\u00c3\u0000\u0000\u065f\u0661\u0005Z\u0000\u0000\u0660\u065d\u0001"+
		"\u0000\u0000\u0000\u0660\u065e\u0001\u0000\u0000\u0000\u0661\u0678\u0001"+
		"\u0000\u0000\u0000\u0662\u0663\u0005n\u0000\u0000\u0663\u0664\u0005\u00bc"+
		"\u0000\u0000\u0664\u0678\u0003\u00b4Z\u0000\u0665\u0666\u0005\u00d7\u0000"+
		"\u0000\u0666\u0667\u0005\u0112\u0000\u0000\u0667\u0678\u0007\u0014\u0000"+
		"\u0000\u0668\u0669\u0005n\u0000\u0000\u0669\u066a\u0005\u00fc\u0000\u0000"+
		"\u066a\u0678\u0007\u000e\u0000\u0000\u066b\u066c\u0005\u00c1\u0000\u0000"+
		"\u066c\u0676\u0005\u0108\u0000\u0000\u066d\u066e\u0005\u00ba\u0000\u0000"+
		"\u066e\u066f\u0005\u0108\u0000\u0000\u066f\u0676\u0005I\u0000\u0000\u0670"+
		"\u0671\u00058\u0000\u0000\u0671\u0676\u0005\u0108\u0000\u0000\u0672\u0673"+
		"\u0005\u00e4\u0000\u0000\u0673\u0674\u0005\u0108\u0000\u0000\u0674\u0676"+
		"\u0005I\u0000\u0000\u0675\u066b\u0001\u0000\u0000\u0000\u0675\u066d\u0001"+
		"\u0000\u0000\u0000\u0675\u0670\u0001\u0000\u0000\u0000\u0675\u0672\u0001"+
		"\u0000\u0000\u0000\u0676\u0678\u0001\u0000\u0000\u0000\u0677\u0656\u0001"+
		"\u0000\u0000\u0000\u0677\u065b\u0001\u0000\u0000\u0000\u0677\u0660\u0001"+
		"\u0000\u0000\u0000\u0677\u0662\u0001\u0000\u0000\u0000\u0677\u0665\u0001"+
		"\u0000\u0000\u0000\u0677\u0668\u0001\u0000\u0000\u0000\u0677\u0675\u0001"+
		"\u0000\u0000\u0000\u0678\u00a1\u0001\u0000\u0000\u0000\u0679\u067a\u0005"+
		">\u0000\u0000\u067a\u067b\u0005\u00f1\u0000\u0000\u067b\u067c\u0003\u00e0"+
		"p\u0000\u067c\u00a3\u0001\u0000\u0000\u0000\u067d\u067e\u0005>\u0000\u0000"+
		"\u067e\u0686\u0005\u00f9\u0000\u0000\u067f\u0680\u0003\u00deo\u0000\u0680"+
		"\u0681\u0005\u0015\u0000\u0000\u0681\u0682\u0003\u00fa}\u0000\u0682\u0687"+
		"\u0001\u0000\u0000\u0000\u0683\u0687\u0003\u00deo\u0000\u0684\u0685\u0005"+
		"\u0015\u0000\u0000\u0685\u0687\u0003\u00fa}\u0000\u0686\u067f\u0001\u0000"+
		"\u0000\u0000\u0686\u0683\u0001\u0000\u0000\u0000\u0686\u0684\u0001\u0000"+
		"\u0000\u0000\u0687\u00a5\u0001\u0000\u0000\u0000\u0688\u0689\u0005>\u0000"+
		"\u0000\u0689\u068a\u0005\u00fe\u0000\u0000\u068a\u068f\u0003\u00f2y\u0000"+
		"\u068b\u068c\u0005\u015f\u0000\u0000\u068c\u068e\u0003\u00a8T\u0000\u068d"+
		"\u068b\u0001\u0000\u0000\u0000\u068e\u0691\u0001\u0000\u0000\u0000\u068f"+
		"\u068d\u0001\u0000\u0000\u0000\u068f\u0690\u0001\u0000\u0000\u0000\u0690"+
		"\u00a7\u0001\u0000\u0000\u0000\u0691\u068f\u0001\u0000\u0000\u0000\u0692"+
		"\u0693\u0005\u000e\u0000\u0000\u0693\u06a6\u0003\u009eO\u0000\u0694\u0695"+
		"\u0005\u010f\u0000\u0000\u0695\u0696\u0005\u013e\u0000\u0000\u0696\u06a6"+
		"\u0003\u00aaU\u0000\u0697\u0698\u0005\u0089\u0000\u0000\u0698\u0699\u0005"+
		"\u001f\u0000\u0000\u0699\u06a6\u0003\u00aaU\u0000\u069a\u069b\u0005\u00b2"+
		"\u0000\u0000\u069b\u06a6\u0003\u00aaU\u0000\u069c\u069d\u0005\u00c1\u0000"+
		"\u0000\u069d\u06a6\u0005\u00b2\u0000\u0000\u069e\u069f\u0005\u00b7\u0000"+
		"\u0000\u069f\u06a6\u0003\u00aaU\u0000\u06a0\u06a1\u0005\u00c1\u0000\u0000"+
		"\u06a1\u06a6\u0005\u00b7\u0000\u0000\u06a2\u06a6\u0005H\u0000\u0000\u06a3"+
		"\u06a4\u0005\u00c1\u0000\u0000\u06a4\u06a6\u0005H\u0000\u0000\u06a5\u0692"+
		"\u0001\u0000\u0000\u0000\u06a5\u0694\u0001\u0000\u0000\u0000\u06a5\u0697"+
		"\u0001\u0000\u0000\u0000\u06a5\u069a\u0001\u0000\u0000\u0000\u06a5\u069c"+
		"\u0001\u0000\u0000\u0000\u06a5\u069e\u0001\u0000\u0000\u0000\u06a5\u06a0"+
		"\u0001\u0000\u0000\u0000\u06a5\u06a2\u0001\u0000\u0000\u0000\u06a5\u06a3"+
		"\u0001\u0000\u0000\u0000\u06a6\u00a9\u0001\u0000\u0000\u0000\u06a7\u06a8"+
		"\u0003\u0102\u0081\u0000\u06a8\u00ab\u0001\u0000\u0000\u0000\u06a9\u06aa"+
		"\u0005>\u0000\u0000\u06aa\u06ab\u0005\u0116\u0000\u0000\u06ab\u06ac\u0003"+
		"\u00f4z\u0000\u06ac\u06af\u0005t\u0000\u0000\u06ad\u06b0\u0003\u00fc~"+
		"\u0000\u06ae\u06b0\u0003\u00dcn\u0000\u06af\u06ad\u0001\u0000\u0000\u0000"+
		"\u06af\u06ae\u0001\u0000\u0000\u0000\u06b0\u00ad\u0001\u0000\u0000\u0000"+
		"\u06b1\u06b2\u0005>\u0000\u0000\u06b2\u06b3\u0005\u0118\u0000\u0000\u06b3"+
		"\u06b4\u0003\u00dcn\u0000\u06b4\u06b5\u0005\u013e\u0000\u0000\u06b5\u06b6"+
		"\u0005\u00c1\u0000\u0000\u06b6\u06b7\u0005I\u0000\u0000\u06b7\u00af\u0001"+
		"\u0000\u0000\u0000\u06b8\u06b9\u0005>\u0000\u0000\u06b9\u06ba\u0005\u0124"+
		"\u0000\u0000\u06ba\u06bb\u0003\u00f6{\u0000\u06bb\u00b1\u0001\u0000\u0000"+
		"\u0000\u06bc\u06bd\u0005>\u0000\u0000\u06bd\u06be\u0005\u0128\u0000\u0000"+
		"\u06be\u06bf\u0003\u00f8|\u0000\u06bf\u06c0\u0005n\u0000\u0000\u06c0\u06c1"+
		"\u0005\u00bc\u0000\u0000\u06c1\u06c2\u0003\u00b4Z\u0000\u06c2\u06c3\u0005"+
		"\u009d\u0000\u0000\u06c3\u06c4\u0005\u0099\u0000\u0000\u06c4\u00b3\u0001"+
		"\u0000\u0000\u0000\u06c5\u06c6\u0003\u00dam\u0000\u06c6\u00b5\u0001\u0000"+
		"\u0000\u0000\u06c7\u06c8\u0005>\u0000\u0000\u06c8\u06c9\u0005\u0139\u0000"+
		"\u0000\u06c9\u06d5\u0003\u00fc~\u0000\u06ca\u06cb\u0005\u015d\u0000\u0000"+
		"\u06cb\u06d0\u0003\u00be_\u0000\u06cc\u06cd\u0005\u015f\u0000\u0000\u06cd"+
		"\u06cf\u0003\u00be_\u0000\u06ce\u06cc\u0001\u0000\u0000\u0000\u06cf\u06d2"+
		"\u0001\u0000\u0000\u0000\u06d0\u06ce\u0001\u0000\u0000\u0000\u06d0\u06d1"+
		"\u0001\u0000\u0000\u0000\u06d1\u06d3\u0001\u0000\u0000\u0000\u06d2\u06d0"+
		"\u0001\u0000\u0000\u0000\u06d3\u06d4\u0005\u015e\u0000\u0000\u06d4\u06d6"+
		"\u0001\u0000\u0000\u0000\u06d5\u06ca\u0001\u0000\u0000\u0000\u06d5\u06d6"+
		"\u0001\u0000\u0000\u0000\u06d6\u06d7\u0001\u0000\u0000\u0000\u06d7\u06d8"+
		"\u0005\u000e\u0000\u0000\u06d8\u06da\u0003\u00b8\\\u0000\u06d9\u06db\u0003"+
		"\u013a\u009d\u0000\u06da\u06d9\u0001\u0000\u0000\u0000\u06da\u06db\u0001"+
		"\u0000\u0000\u0000\u06db\u06dd\u0001\u0000\u0000\u0000\u06dc\u06de\u0003"+
		"\u000e\u0007\u0000\u06dd\u06dc\u0001\u0000\u0000\u0000\u06dd\u06de\u0001"+
		"\u0000\u0000\u0000\u06de\u06e0\u0001\u0000\u0000\u0000\u06df\u06e1\u0003"+
		"\u0010\b\u0000\u06e0\u06df\u0001\u0000\u0000\u0000\u06e0\u06e1\u0001\u0000"+
		"\u0000\u0000\u06e1\u00b7\u0001\u0000\u0000\u0000\u06e2\u06e3\u0006\\\uffff"+
		"\uffff\u0000\u06e3\u06e4\u0005\u015d\u0000\u0000\u06e4\u06e6\u0003\u00b8"+
		"\\\u0000\u06e5\u06e7\u0003\u013a\u009d\u0000\u06e6\u06e5\u0001\u0000\u0000"+
		"\u0000\u06e6\u06e7\u0001\u0000\u0000\u0000\u06e7\u06e9\u0001\u0000\u0000"+
		"\u0000\u06e8\u06ea\u0003\u000e\u0007\u0000\u06e9\u06e8\u0001\u0000\u0000"+
		"\u0000\u06e9\u06ea\u0001\u0000\u0000\u0000\u06ea\u06ec\u0001\u0000\u0000"+
		"\u0000\u06eb\u06ed\u0003\u0010\b\u0000\u06ec\u06eb\u0001\u0000\u0000\u0000"+
		"\u06ec\u06ed\u0001\u0000\u0000\u0000\u06ed\u06ee\u0001\u0000\u0000\u0000"+
		"\u06ee\u06ef\u0005\u015e\u0000\u0000\u06ef\u06f3\u0001\u0000\u0000\u0000"+
		"\u06f0\u06f3\u0003\u00ba]\u0000\u06f1\u06f3\u0003\u0142\u00a1\u0000\u06f2"+
		"\u06e2\u0001\u0000\u0000\u0000\u06f2\u06f0\u0001\u0000\u0000\u0000\u06f2"+
		"\u06f1\u0001\u0000\u0000\u0000\u06f3\u0708\u0001\u0000\u0000\u0000\u06f4"+
		"\u06f5\n\u0005\u0000\u0000\u06f5\u06f7\u0005\u0094\u0000\u0000\u06f6\u06f8"+
		"\u0003\u0124\u0092\u0000\u06f7\u06f6\u0001\u0000\u0000\u0000\u06f7\u06f8"+
		"\u0001\u0000\u0000\u0000\u06f8\u06f9\u0001\u0000\u0000\u0000\u06f9\u0707"+
		"\u0003\u00b8\\\u0006\u06fa\u06fb\n\u0004\u0000\u0000\u06fb\u06fd\u0005"+
		"f\u0000\u0000\u06fc\u06fe\u0003\u0124\u0092\u0000\u06fd\u06fc\u0001\u0000"+
		"\u0000\u0000\u06fd\u06fe\u0001\u0000\u0000\u0000\u06fe\u06ff\u0001\u0000"+
		"\u0000\u0000\u06ff\u0707\u0003\u00b8\\\u0005\u0700\u0701\n\u0003\u0000"+
		"\u0000\u0701\u0703\u0005\u012b\u0000\u0000\u0702\u0704\u0003\u0124\u0092"+
		"\u0000\u0703\u0702\u0001\u0000\u0000\u0000\u0703\u0704\u0001\u0000\u0000"+
		"\u0000\u0704\u0705\u0001\u0000\u0000\u0000\u0705\u0707\u0003\u00b8\\\u0004"+
		"\u0706\u06f4\u0001\u0000\u0000\u0000\u0706\u06fa\u0001\u0000\u0000\u0000"+
		"\u0706\u0700\u0001\u0000\u0000\u0000\u0707\u070a\u0001\u0000\u0000\u0000"+
		"\u0708\u0706\u0001\u0000\u0000\u0000\u0708\u0709\u0001\u0000\u0000\u0000"+
		"\u0709\u00b9\u0001\u0000\u0000\u0000\u070a\u0708\u0001\u0000\u0000\u0000"+
		"\u070b\u070d\u0005\u00fd\u0000\u0000\u070c\u070e\u0003\u0124\u0092\u0000"+
		"\u070d\u070c\u0001\u0000\u0000\u0000\u070d\u070e\u0001\u0000\u0000\u0000"+
		"\u070e\u070f\u0001\u0000\u0000\u0000\u070f\u0714\u0003\u00bc^\u0000\u0710"+
		"\u0711\u0005\u015f\u0000\u0000\u0711\u0713\u0003\u00bc^\u0000\u0712\u0710"+
		"\u0001\u0000\u0000\u0000\u0713\u0716\u0001\u0000\u0000\u0000\u0714\u0712"+
		"\u0001\u0000\u0000\u0000\u0714\u0715\u0001\u0000\u0000\u0000\u0715\u0717"+
		"\u0001\u0000\u0000\u0000\u0716\u0714\u0001\u0000\u0000\u0000\u0717\u0719"+
		"\u0003\u0126\u0093\u0000\u0718\u071a\u0003*\u0015\u0000\u0719\u0718\u0001"+
		"\u0000\u0000\u0000\u0719\u071a\u0001\u0000\u0000\u0000\u071a\u071c\u0001"+
		"\u0000\u0000\u0000\u071b\u071d\u0003\u0136\u009b\u0000\u071c\u071b\u0001"+
		"\u0000\u0000\u0000\u071c\u071d\u0001\u0000\u0000\u0000\u071d\u071f\u0001"+
		"\u0000\u0000\u0000\u071e\u0720\u0003\u0138\u009c\u0000\u071f\u071e\u0001"+
		"\u0000\u0000\u0000\u071f\u0720\u0001\u0000\u0000\u0000\u0720\u0722\u0001"+
		"\u0000\u0000\u0000\u0721\u0723\u0003\u013a\u009d\u0000\u0722\u0721\u0001"+
		"\u0000\u0000\u0000\u0722\u0723\u0001\u0000\u0000\u0000\u0723\u0725\u0001"+
		"\u0000\u0000\u0000\u0724\u0726\u0003\u000e\u0007\u0000\u0725\u0724\u0001"+
		"\u0000\u0000\u0000\u0725\u0726\u0001\u0000\u0000\u0000\u0726\u0728\u0001"+
		"\u0000\u0000\u0000\u0727\u0729\u0003\u0010\b\u0000\u0728\u0727\u0001\u0000"+
		"\u0000\u0000\u0728\u0729\u0001\u0000\u0000\u0000\u0729\u00bb\u0001\u0000"+
		"\u0000\u0000\u072a\u0738\u0005\u0161\u0000\u0000\u072b\u072e\u0003\u00dc"+
		"n\u0000\u072c\u072e\u0003\u00e8t\u0000\u072d\u072b\u0001\u0000\u0000\u0000"+
		"\u072d\u072c\u0001\u0000\u0000\u0000\u072e\u072f\u0001\u0000\u0000\u0000"+
		"\u072f\u0730\u0005\u015b\u0000\u0000\u0730\u0731\u0005\u0161\u0000\u0000"+
		"\u0731\u0738\u0001\u0000\u0000\u0000\u0732\u0735\u0003\u0106\u0083\u0000"+
		"\u0733\u0734\u0005\u000e\u0000\u0000\u0734\u0736\u0003\u00be_\u0000\u0735"+
		"\u0733\u0001\u0000\u0000\u0000\u0735\u0736\u0001\u0000\u0000\u0000\u0736"+
		"\u0738\u0001\u0000\u0000\u0000\u0737\u072a\u0001\u0000\u0000\u0000\u0737"+
		"\u072d\u0001\u0000\u0000\u0000\u0737\u0732\u0001\u0000\u0000\u0000\u0738"+
		"\u00bd\u0001\u0000\u0000\u0000\u0739\u073a\u0003\u0100\u0080\u0000\u073a"+
		"\u00bf\u0001\u0000\u0000\u0000\u073b\u0748\u0003\u00c2a\u0000\u073c\u0748"+
		"\u0003\u00c4b\u0000\u073d\u0748\u0003\u00c6c\u0000\u073e\u0748\u0003\u00c8"+
		"d\u0000\u073f\u0748\u0003\u00cae\u0000\u0740\u0748\u0003\u00ccf\u0000"+
		"\u0741\u0748\u0003\u00ceg\u0000\u0742\u0748\u0003\u00d0h\u0000\u0743\u0748"+
		"\u0003\u00d2i\u0000\u0744\u0748\u0003\u00d4j\u0000\u0745\u0748\u0003\u00d6"+
		"k\u0000\u0746\u0748\u0003\u00d8l\u0000\u0747\u073b\u0001\u0000\u0000\u0000"+
		"\u0747\u073c\u0001\u0000\u0000\u0000\u0747\u073d\u0001\u0000\u0000\u0000"+
		"\u0747\u073e\u0001\u0000\u0000\u0000\u0747\u073f\u0001\u0000\u0000\u0000"+
		"\u0747\u0740\u0001\u0000\u0000\u0000\u0747\u0741\u0001\u0000\u0000\u0000"+
		"\u0747\u0742\u0001\u0000\u0000\u0000\u0747\u0743\u0001\u0000\u0000\u0000"+
		"\u0747\u0744\u0001\u0000\u0000\u0000\u0747\u0745\u0001\u0000\u0000\u0000"+
		"\u0747\u0746\u0001\u0000\u0000\u0000\u0748\u00c1\u0001\u0000\u0000\u0000"+
		"\u0749\u074a\u0005`\u0000\u0000\u074a\u074b\u0005V\u0000\u0000\u074b\u074c"+
		"\u0005\u0006\u0000\u0000\u074c\u074d\u0003\u00e2q\u0000\u074d\u074e\u0005"+
		"\u00ec\u0000\u0000\u074e\u00c3\u0001\u0000\u0000\u0000\u074f\u0750\u0005"+
		"`\u0000\u0000\u0750\u0751\u0005y\u0000\u0000\u0751\u0752\u0003\u00eau"+
		"\u0000\u0752\u00c5\u0001\u0000\u0000\u0000\u0753\u0754\u0005`\u0000\u0000"+
		"\u0754\u0755\u0005\u008a\u0000\u0000\u0755\u0756\u0003\u00ecv\u0000\u0756"+
		"\u00c7\u0001\u0000\u0000\u0000\u0757\u0758\u0005`\u0000\u0000\u0758\u0759"+
		"\u0005\u00e0\u0000\u0000\u0759\u075a\u0003\u00eew\u0000\u075a\u00c9\u0001"+
		"\u0000\u0000\u0000\u075b\u075c\u0005`\u0000\u0000\u075c\u075d\u0005\u00f1"+
		"\u0000\u0000\u075d\u075e\u0003\u00e0p\u0000\u075e\u00cb\u0001\u0000\u0000"+
		"\u0000\u075f\u0760\u0005`\u0000\u0000\u0760\u0761\u0005\u00f9\u0000\u0000"+
		"\u0761\u0762\u0003\u00deo\u0000\u0762\u0763\u0005\u00ec\u0000\u0000\u0763"+
		"\u00cd\u0001\u0000\u0000\u0000\u0764\u0765\u0005`\u0000\u0000\u0765\u0766"+
		"\u0005\u00fe\u0000\u0000\u0766\u0767\u0003\u00f2y\u0000\u0767\u0768\u0005"+
		"\u00ec\u0000\u0000\u0768\u00cf\u0001\u0000\u0000\u0000\u0769\u076a\u0005"+
		"`\u0000\u0000\u076a\u076b\u0005\u0116\u0000\u0000\u076b\u076c\u0003\u00f4"+
		"z\u0000\u076c\u00d1\u0001\u0000\u0000\u0000\u076d\u076e\u0005`\u0000\u0000"+
		"\u076e\u076f\u0005\u0118\u0000\u0000\u076f\u0770\u0003\u00dcn\u0000\u0770"+
		"\u00d3\u0001\u0000\u0000\u0000\u0771\u0772\u0005`\u0000\u0000\u0772\u0773"+
		"\u0005\u0124\u0000\u0000\u0773\u0774\u0003\u00f6{\u0000\u0774\u00d5\u0001"+
		"\u0000\u0000\u0000\u0775\u0776\u0005`\u0000\u0000\u0776\u0777\u0005\u0128"+
		"\u0000\u0000\u0777\u0778\u0003\u00f8|\u0000\u0778\u0779\u0005\u00ec\u0000"+
		"\u0000\u0779\u00d7\u0001\u0000\u0000\u0000\u077a\u077b\u0005`\u0000\u0000"+
		"\u077b\u077c\u0005\u0139\u0000\u0000\u077c\u077d\u0003\u00fc~\u0000\u077d"+
		"\u00d9\u0001\u0000\u0000\u0000\u077e\u077f\u0005\u014d\u0000\u0000\u077f"+
		"\u00db\u0001\u0000\u0000\u0000\u0780\u0781\u0003\u00deo\u0000\u0781\u0782"+
		"\u0005\u015b\u0000\u0000\u0782\u0784\u0001\u0000\u0000\u0000\u0783\u0780"+
		"\u0001\u0000\u0000\u0000\u0783\u0784\u0001\u0000\u0000\u0000\u0784\u0785"+
		"\u0001\u0000\u0000\u0000\u0785\u0786\u0003\u0100\u0080\u0000\u0786\u00dd"+
		"\u0001\u0000\u0000\u0000\u0787\u0788\u0003\u0100\u0080\u0000\u0788\u00df"+
		"\u0001\u0000\u0000\u0000\u0789\u078a\u0003\u0100\u0080\u0000\u078a\u00e1"+
		"\u0001\u0000\u0000\u0000\u078b\u078c\u0003\u00deo\u0000\u078c\u078d\u0005"+
		"\u015b\u0000\u0000\u078d\u078f\u0001\u0000\u0000\u0000\u078e\u078b\u0001"+
		"\u0000\u0000\u0000\u078e\u078f\u0001\u0000\u0000\u0000\u078f\u0790\u0001"+
		"\u0000\u0000\u0000\u0790\u0791\u0003\u0100\u0080\u0000\u0791\u00e3\u0001"+
		"\u0000\u0000\u0000\u0792\u0793\u0003\u00deo\u0000\u0793\u0794\u0005\u015b"+
		"\u0000\u0000\u0794\u0796\u0001\u0000\u0000\u0000\u0795\u0792\u0001\u0000"+
		"\u0000\u0000\u0795\u0796\u0001\u0000\u0000\u0000\u0796\u0797\u0001\u0000"+
		"\u0000\u0000\u0797\u0798\u0003\u0100\u0080\u0000\u0798\u00e5\u0001\u0000"+
		"\u0000\u0000\u0799\u079c\u0003\u00dcn\u0000\u079a\u079c\u0003\u00e8t\u0000"+
		"\u079b\u0799\u0001\u0000\u0000\u0000\u079b\u079a\u0001\u0000\u0000\u0000"+
		"\u079c\u079d\u0001\u0000\u0000\u0000\u079d\u079e\u0005\u015b\u0000\u0000"+
		"\u079e\u07a0\u0001\u0000\u0000\u0000\u079f\u079b\u0001\u0000\u0000\u0000"+
		"\u079f\u07a0\u0001\u0000\u0000\u0000\u07a0\u07a1\u0001\u0000\u0000\u0000"+
		"\u07a1\u07a2\u0003\u0100\u0080\u0000\u07a2\u00e7\u0001\u0000\u0000\u0000"+
		"\u07a3\u07a4\u0003\u0100\u0080\u0000\u07a4\u00e9\u0001\u0000\u0000\u0000"+
		"\u07a5\u07a6\u0003\u00deo\u0000\u07a6\u07a7\u0005\u015b\u0000\u0000\u07a7"+
		"\u07a9\u0001\u0000\u0000\u0000\u07a8\u07a5\u0001\u0000\u0000\u0000\u07a8"+
		"\u07a9\u0001\u0000\u0000\u0000\u07a9\u07aa\u0001\u0000\u0000\u0000\u07aa"+
		"\u07ab\u0003\u0100\u0080\u0000\u07ab\u00eb\u0001\u0000\u0000\u0000\u07ac"+
		"\u07ad\u0003\u00deo\u0000\u07ad\u07ae\u0005\u015b\u0000\u0000\u07ae\u07b0"+
		"\u0001\u0000\u0000\u0000\u07af\u07ac\u0001\u0000\u0000\u0000\u07af\u07b0"+
		"\u0001\u0000\u0000\u0000\u07b0\u07b1\u0001\u0000\u0000\u0000\u07b1\u07b2"+
		"\u0003\u0100\u0080\u0000\u07b2\u00ed\u0001\u0000\u0000\u0000\u07b3\u07b4"+
		"\u0003\u00deo\u0000\u07b4\u07b5\u0005\u015b\u0000\u0000\u07b5\u07b7\u0001"+
		"\u0000\u0000\u0000\u07b6\u07b3\u0001\u0000\u0000\u0000\u07b6\u07b7\u0001"+
		"\u0000\u0000\u0000\u07b7\u07b8\u0001\u0000\u0000\u0000\u07b8\u07b9\u0003"+
		"\u0100\u0080\u0000\u07b9\u00ef\u0001\u0000\u0000\u0000\u07ba\u07bb\u0003"+
		"\u0100\u0080\u0000\u07bb\u00f1\u0001\u0000\u0000\u0000\u07bc\u07bd\u0003"+
		"\u00deo\u0000\u07bd\u07be\u0005\u015b\u0000\u0000\u07be\u07c0\u0001\u0000"+
		"\u0000\u0000\u07bf\u07bc\u0001\u0000\u0000\u0000\u07bf\u07c0\u0001\u0000"+
		"\u0000\u0000\u07c0\u07c1\u0001\u0000\u0000\u0000\u07c1\u07c2\u0003\u0100"+
		"\u0080\u0000\u07c2\u00f3\u0001\u0000\u0000\u0000\u07c3\u07c4\u0003\u00de"+
		"o\u0000\u07c4\u07c5\u0005\u015b\u0000\u0000\u07c5\u07c7\u0001\u0000\u0000"+
		"\u0000\u07c6\u07c3\u0001\u0000\u0000\u0000\u07c6\u07c7\u0001\u0000\u0000"+
		"\u0000\u07c7\u07c8\u0001\u0000\u0000\u0000\u07c8\u07c9\u0003\u0100\u0080"+
		"\u0000\u07c9\u00f5\u0001\u0000\u0000\u0000\u07ca\u07cb\u0003\u00deo\u0000"+
		"\u07cb\u07cc\u0005\u015b\u0000\u0000\u07cc\u07ce\u0001\u0000\u0000\u0000"+
		"\u07cd\u07ca\u0001\u0000\u0000\u0000\u07cd\u07ce\u0001\u0000\u0000\u0000"+
		"\u07ce\u07cf\u0001\u0000\u0000\u0000\u07cf\u07d0\u0003\u0100\u0080\u0000"+
		"\u07d0\u00f7\u0001\u0000\u0000\u0000\u07d1\u07d2\u0003\u00deo\u0000\u07d2"+
		"\u07d3\u0005\u015b\u0000\u0000\u07d3\u07d5\u0001\u0000\u0000\u0000\u07d4"+
		"\u07d1\u0001\u0000\u0000\u0000\u07d4\u07d5\u0001\u0000\u0000\u0000\u07d5"+
		"\u07d6\u0001\u0000\u0000\u0000\u07d6\u07d7\u0003\u0100\u0080\u0000\u07d7"+
		"\u00f9\u0001\u0000\u0000\u0000\u07d8\u07d9\u0003\u0100\u0080\u0000\u07d9"+
		"\u00fb\u0001\u0000\u0000\u0000\u07da\u07db\u0003\u00deo\u0000\u07db\u07dc"+
		"\u0005\u015b\u0000\u0000\u07dc\u07de\u0001\u0000\u0000\u0000\u07dd\u07da"+
		"\u0001\u0000\u0000\u0000\u07dd\u07de\u0001\u0000\u0000\u0000\u07de\u07df"+
		"\u0001\u0000\u0000\u0000\u07df\u07e0\u0003\u0100\u0080\u0000\u07e0\u00fd"+
		"\u0001\u0000\u0000\u0000\u07e1\u07e2\u0003\u0100\u0080\u0000\u07e2\u00ff"+
		"\u0001\u0000\u0000\u0000\u07e3\u07e4\u0007\u0015\u0000\u0000\u07e4\u0101"+
		"\u0001\u0000\u0000\u0000\u07e5\u07e6\u0005\u014e\u0000\u0000\u07e6\u0103"+
		"\u0001\u0000\u0000\u0000\u07e7\u07ec\u0003\u0106\u0083\u0000\u07e8\u07e9"+
		"\u0005\u015f\u0000\u0000\u07e9\u07eb\u0003\u0106\u0083\u0000\u07ea\u07e8"+
		"\u0001\u0000\u0000\u0000\u07eb\u07ee\u0001\u0000\u0000\u0000\u07ec\u07ea"+
		"\u0001\u0000\u0000\u0000\u07ec\u07ed\u0001\u0000\u0000\u0000\u07ed\u0105"+
		"\u0001\u0000\u0000\u0000\u07ee\u07ec\u0001\u0000\u0000\u0000\u07ef\u07f0"+
		"\u0006\u0083\uffff\uffff\u0000\u07f0\u0804\u0003\u0108\u0084\u0000\u07f1"+
		"\u07f2\u0005\u015d\u0000\u0000\u07f2\u07f3\u0003\u0106\u0083\u0000\u07f3"+
		"\u07f4\u0005\u015e\u0000\u0000\u07f4\u0804\u0001\u0000\u0000\u0000\u07f5"+
		"\u0804\u0003.\u0017\u0000\u07f6\u0804\u0003\u010a\u0085\u0000\u07f7\u0804"+
		"\u0003\u0110\u0088\u0000\u07f8\u07f9\u0007\u0016\u0000\u0000\u07f9\u0804"+
		"\u0003\u0106\u0083\n\u07fa\u07fb\u0005\u00c3\u0000\u0000\u07fb\u0804\u0003"+
		"\u0106\u0083\u0003\u07fc\u07fd\u0005%\u0000\u0000\u07fd\u07fe\u0005\u015d"+
		"\u0000\u0000\u07fe\u07ff\u0003\u0106\u0083\u0000\u07ff\u0800\u0005\u000e"+
		"\u0000\u0000\u0800\u0801\u0003\u009eO\u0000\u0801\u0802\u0005\u015e\u0000"+
		"\u0000\u0802\u0804\u0001\u0000\u0000\u0000\u0803\u07ef\u0001\u0000\u0000"+
		"\u0000\u0803\u07f1\u0001\u0000\u0000\u0000\u0803\u07f5\u0001\u0000\u0000"+
		"\u0000\u0803\u07f6\u0001\u0000\u0000\u0000\u0803\u07f7\u0001\u0000\u0000"+
		"\u0000\u0803\u07f8\u0001\u0000\u0000\u0000\u0803\u07fa\u0001\u0000\u0000"+
		"\u0000\u0803\u07fc\u0001\u0000\u0000\u0000\u0804\u0823\u0001\u0000\u0000"+
		"\u0000\u0805\u0806\n\t\u0000\u0000\u0806\u0807\u0007\u0017\u0000\u0000"+
		"\u0807\u0822\u0003\u0106\u0083\n\u0808\u0809\n\b\u0000\u0000\u0809\u080a"+
		"\u0007\u0016\u0000\u0000\u080a\u0822\u0003\u0106\u0083\t\u080b\u080c\n"+
		"\u0007\u0000\u0000\u080c\u080d\u0005\u015b\u0000\u0000\u080d\u0822\u0003"+
		"\u0106\u0083\b\u080e\u080f\n\u0006\u0000\u0000\u080f\u0810\u00032\u0019"+
		"\u0000\u0810\u0811\u0003\u0106\u0083\u0007\u0811\u0822\u0001\u0000\u0000"+
		"\u0000\u0812\u0813\n\u0005\u0000\u0000\u0813\u0814\u0005\u000b\u0000\u0000"+
		"\u0814\u0822\u0003\u0106\u0083\u0006\u0815\u0816\n\u0004\u0000\u0000\u0816"+
		"\u0817\u0005\u00d0\u0000\u0000\u0817\u0822\u0003\u0106\u0083\u0005\u0818"+
		"\u081a\n\u0002\u0000\u0000\u0819\u081b\u0005\u00c3\u0000\u0000\u081a\u0819"+
		"\u0001\u0000\u0000\u0000\u081a\u081b\u0001\u0000\u0000\u0000\u081b\u081c"+
		"\u0001\u0000\u0000\u0000\u081c\u081d\u0005\u0088\u0000\u0000\u081d\u081e"+
		"\u0005\u015d\u0000\u0000\u081e\u081f\u0003\u0104\u0082\u0000\u081f\u0820"+
		"\u0005\u015e\u0000\u0000\u0820\u0822\u0001\u0000\u0000\u0000\u0821\u0805"+
		"\u0001\u0000\u0000\u0000\u0821\u0808\u0001\u0000\u0000\u0000\u0821\u080b"+
		"\u0001\u0000\u0000\u0000\u0821\u080e\u0001\u0000\u0000\u0000\u0821\u0812"+
		"\u0001\u0000\u0000\u0000\u0821\u0815\u0001\u0000\u0000\u0000\u0821\u0818"+
		"\u0001\u0000\u0000\u0000\u0822\u0825\u0001\u0000\u0000\u0000\u0823\u0821"+
		"\u0001\u0000\u0000\u0000\u0823\u0824\u0001\u0000\u0000\u0000\u0824\u0107"+
		"\u0001\u0000\u0000\u0000\u0825\u0823\u0001\u0000\u0000\u0000\u0826\u082a"+
		"\u0005P\u0000\u0000\u0827\u082a\u0003\u010c\u0086\u0000\u0828\u082a\u0003"+
		"\u0100\u0080\u0000\u0829\u0826\u0001\u0000\u0000\u0000\u0829\u0827\u0001"+
		"\u0000\u0000\u0000\u0829\u0828\u0001\u0000\u0000\u0000\u082a\u0109\u0001"+
		"\u0000\u0000\u0000\u082b\u082c\u0003\u00eau\u0000\u082c\u082e\u0005\u015d"+
		"\u0000\u0000\u082d\u082f\u0003\u0104\u0082\u0000\u082e\u082d\u0001\u0000"+
		"\u0000\u0000\u082e\u082f\u0001\u0000\u0000\u0000\u082f\u0830\u0001\u0000"+
		"\u0000\u0000\u0830\u0831\u0005\u015e\u0000\u0000\u0831\u0841\u0001\u0000"+
		"\u0000\u0000\u0832\u0833\u0003\u011c\u008e\u0000\u0833\u0835\u0005\u015d"+
		"\u0000\u0000\u0834\u0836\u0003\u0104\u0082\u0000\u0835\u0834\u0001\u0000"+
		"\u0000\u0000\u0835\u0836\u0001\u0000\u0000\u0000\u0836\u0837\u0001\u0000"+
		"\u0000\u0000\u0837\u0838\u0005\u015e\u0000\u0000\u0838\u0841\u0001\u0000"+
		"\u0000\u0000\u0839\u083a\u0003\u011e\u008f\u0000\u083a\u083c\u0005\u015d"+
		"\u0000\u0000\u083b\u083d\u0003\u0104\u0082\u0000\u083c\u083b\u0001\u0000"+
		"\u0000\u0000\u083c\u083d\u0001\u0000\u0000\u0000\u083d\u083e\u0001\u0000"+
		"\u0000\u0000\u083e\u083f\u0005\u015e\u0000\u0000\u083f\u0841\u0001\u0000"+
		"\u0000\u0000\u0840\u082b\u0001\u0000\u0000\u0000\u0840\u0832\u0001\u0000"+
		"\u0000\u0000\u0840\u0839\u0001\u0000\u0000\u0000\u0841\u010b\u0001\u0000"+
		"\u0000\u0000\u0842\u084f\u0003\u00dam\u0000\u0843\u0845\u0003\u010e\u0087"+
		"\u0000\u0844\u0843\u0001\u0000\u0000\u0000\u0844\u0845\u0001\u0000\u0000"+
		"\u0000\u0845\u0846\u0001\u0000\u0000\u0000\u0846\u084f\u0005\u014e\u0000"+
		"\u0000\u0847\u0849\u0003\u010e\u0087\u0000\u0848\u0847\u0001\u0000\u0000"+
		"\u0000\u0848\u0849\u0001\u0000\u0000\u0000\u0849\u084a\u0001\u0000\u0000"+
		"\u0000\u084a\u084f\u0007\u0018\u0000\u0000\u084b\u084f\u0005\u0126\u0000"+
		"\u0000\u084c\u084f\u0005o\u0000\u0000\u084d\u084f\u0005\u00c4\u0000\u0000"+
		"\u084e\u0842\u0001\u0000\u0000\u0000\u084e\u0844\u0001\u0000\u0000\u0000"+
		"\u084e\u0848\u0001\u0000\u0000\u0000\u084e\u084b\u0001\u0000\u0000\u0000"+
		"\u084e\u084c\u0001\u0000\u0000\u0000\u084e\u084d\u0001\u0000\u0000\u0000"+
		"\u084f\u010d\u0001\u0000\u0000\u0000\u0850\u0851\u0007\u0016\u0000\u0000"+
		"\u0851\u010f\u0001\u0000\u0000\u0000\u0852\u0856\u0003\u0112\u0089\u0000"+
		"\u0853\u0856\u0003\u0114\u008a\u0000\u0854\u0856\u0003\u0116\u008b\u0000"+
		"\u0855\u0852\u0001\u0000\u0000\u0000\u0855\u0853\u0001\u0000\u0000\u0000"+
		"\u0855\u0854\u0001\u0000\u0000\u0000\u0856\u0111\u0001\u0000\u0000\u0000"+
		"\u0857\u0858\u0005$\u0000\u0000\u0858\u0859\u0005\u013a\u0000\u0000\u0859"+
		"\u085a\u0003,\u0016\u0000\u085a\u085b\u0005\u011b\u0000\u0000\u085b\u0863"+
		"\u0003\u0106\u0083\u0000\u085c\u085d\u0005\u013a\u0000\u0000\u085d\u085e"+
		"\u0003,\u0016\u0000\u085e\u085f\u0005\u011b\u0000\u0000\u085f\u0860\u0003"+
		"\u0106\u0083\u0000\u0860\u0862\u0001\u0000\u0000\u0000\u0861\u085c\u0001"+
		"\u0000\u0000\u0000\u0862\u0865\u0001\u0000\u0000\u0000\u0863\u0861\u0001"+
		"\u0000\u0000\u0000\u0863\u0864\u0001\u0000\u0000\u0000\u0864\u0868\u0001"+
		"\u0000\u0000\u0000\u0865\u0863\u0001\u0000\u0000\u0000\u0866\u0867\u0005"+
		"b\u0000\u0000\u0867\u0869\u0003\u0106\u0083\u0000\u0868\u0866\u0001\u0000"+
		"\u0000\u0000\u0868\u0869\u0001\u0000\u0000\u0000\u0869\u086a\u0001\u0000"+
		"\u0000\u0000\u086a\u086b\u0005c\u0000\u0000\u086b\u0113\u0001\u0000\u0000"+
		"\u0000\u086c\u086d\u0005$\u0000\u0000\u086d\u086e\u0003\u011a\u008d\u0000"+
		"\u086e\u086f\u0005\u013a\u0000\u0000\u086f\u0874\u0003\u011a\u008d\u0000"+
		"\u0870\u0871\u0005\u015f\u0000\u0000\u0871\u0873\u0003\u011a\u008d\u0000"+
		"\u0872\u0870\u0001\u0000\u0000\u0000\u0873\u0876\u0001\u0000\u0000\u0000"+
		"\u0874\u0872\u0001\u0000\u0000\u0000\u0874\u0875\u0001\u0000\u0000\u0000"+
		"\u0875\u0877\u0001\u0000\u0000\u0000\u0876\u0874\u0001\u0000\u0000\u0000"+
		"\u0877\u0878\u0005\u011b\u0000\u0000\u0878\u0887\u0003\u0106\u0083\u0000"+
		"\u0879\u087a\u0005\u013a\u0000\u0000\u087a\u087f\u0003\u011a\u008d\u0000"+
		"\u087b\u087c\u0005\u015f\u0000\u0000\u087c\u087e\u0003\u011a\u008d\u0000"+
		"\u087d\u087b\u0001\u0000\u0000\u0000\u087e\u0881\u0001\u0000\u0000\u0000"+
		"\u087f\u087d\u0001\u0000\u0000\u0000\u087f\u0880\u0001\u0000\u0000\u0000"+
		"\u0880\u0882\u0001\u0000\u0000\u0000\u0881\u087f\u0001\u0000\u0000\u0000"+
		"\u0882\u0883\u0005\u011b\u0000\u0000\u0883\u0884\u0003\u0106\u0083\u0000"+
		"\u0884\u0886\u0001\u0000\u0000\u0000\u0885\u0879\u0001\u0000\u0000\u0000"+
		"\u0886\u0889\u0001\u0000\u0000\u0000\u0887\u0885\u0001\u0000\u0000\u0000"+
		"\u0887\u0888\u0001\u0000\u0000\u0000\u0888\u088c\u0001\u0000\u0000\u0000"+
		"\u0889\u0887\u0001\u0000\u0000\u0000\u088a\u088b\u0005b\u0000\u0000\u088b"+
		"\u088d\u0003\u0106\u0083\u0000\u088c\u088a\u0001\u0000\u0000\u0000\u088c"+
		"\u088d\u0001\u0000\u0000\u0000\u088d\u088e\u0001\u0000\u0000\u0000\u088e"+
		"\u088f\u0005c\u0000\u0000\u088f\u0115\u0001\u0000\u0000\u0000\u0890\u0891"+
		"\u0005$\u0000\u0000\u0891\u0892\u0003\u011a\u008d\u0000\u0892\u0893\u0005"+
		"\u013a\u0000\u0000\u0893\u0898\u0003\u0118\u008c\u0000\u0894\u0895\u0005"+
		"\u015f\u0000\u0000\u0895\u0897\u0003\u0118\u008c\u0000\u0896\u0894\u0001"+
		"\u0000\u0000\u0000\u0897\u089a\u0001\u0000\u0000\u0000\u0898\u0896\u0001"+
		"\u0000\u0000\u0000\u0898\u0899\u0001\u0000\u0000\u0000\u0899\u089b\u0001"+
		"\u0000\u0000\u0000\u089a\u0898\u0001\u0000\u0000\u0000\u089b\u089c\u0005"+
		"\u011b\u0000\u0000\u089c\u08ab\u0003\u0106\u0083\u0000\u089d\u089e\u0005"+
		"\u013a\u0000\u0000\u089e\u08a3\u0003\u0118\u008c\u0000\u089f\u08a0\u0005"+
		"\u015f\u0000\u0000\u08a0\u08a2\u0003\u0118\u008c\u0000\u08a1\u089f\u0001"+
		"\u0000\u0000\u0000\u08a2\u08a5\u0001\u0000\u0000\u0000\u08a3\u08a1\u0001"+
		"\u0000\u0000\u0000\u08a3\u08a4\u0001\u0000\u0000\u0000\u08a4\u08a6\u0001"+
		"\u0000\u0000\u0000\u08a5\u08a3\u0001\u0000\u0000\u0000\u08a6\u08a7\u0005"+
		"\u011b\u0000\u0000\u08a7\u08a8\u0003\u0106\u0083\u0000\u08a8\u08aa\u0001"+
		"\u0000\u0000\u0000\u08a9\u089d\u0001\u0000\u0000\u0000\u08aa\u08ad\u0001"+
		"\u0000\u0000\u0000\u08ab\u08a9\u0001\u0000\u0000\u0000\u08ab\u08ac\u0001"+
		"\u0000\u0000\u0000\u08ac\u08b0\u0001\u0000\u0000\u0000\u08ad\u08ab\u0001"+
		"\u0000\u0000\u0000\u08ae\u08af\u0005b\u0000\u0000\u08af\u08b1\u0003\u0106"+
		"\u0083\u0000\u08b0\u08ae\u0001\u0000\u0000\u0000\u08b0\u08b1\u0001\u0000"+
		"\u0000\u0000\u08b1\u08b2\u0001\u0000\u0000\u0000\u08b2\u08b3\u0005c\u0000"+
		"\u0000\u08b3\u0117\u0001\u0000\u0000\u0000\u08b4\u08e0\u0003\u011a\u008d"+
		"\u0000\u08b5\u08b6\u00032\u0019\u0000\u08b6\u08b7\u0003\u0106\u0083\u0000"+
		"\u08b7\u08e0\u0001\u0000\u0000\u0000\u08b8\u08ba\u0005\u0097\u0000\u0000"+
		"\u08b9\u08bb\u0005\u00c3\u0000\u0000\u08ba\u08b9\u0001\u0000\u0000\u0000"+
		"\u08ba\u08bb\u0001\u0000\u0000\u0000\u08bb\u08bc\u0001\u0000\u0000\u0000"+
		"\u08bc\u08e0\u0005\u00c4\u0000\u0000\u08bd\u08bf\u0005\u00c3\u0000\u0000"+
		"\u08be\u08bd\u0001\u0000\u0000\u0000\u08be\u08bf\u0001\u0000\u0000\u0000"+
		"\u08bf\u08c0\u0001\u0000\u0000\u0000\u08c0\u08c1\u0005\u00a4\u0000\u0000"+
		"\u08c1\u08c4\u0003\u00dam\u0000\u08c2\u08c3\u0005e\u0000\u0000\u08c3\u08c5"+
		"\u0003\u00dam\u0000\u08c4\u08c2\u0001\u0000\u0000\u0000\u08c4\u08c5\u0001"+
		"\u0000\u0000\u0000\u08c5\u08e0\u0001\u0000\u0000\u0000\u08c6\u08c8\u0005"+
		"\u00c3\u0000\u0000\u08c7\u08c6\u0001\u0000\u0000\u0000\u08c7\u08c8\u0001"+
		"\u0000\u0000\u0000\u08c8\u08c9\u0001\u0000\u0000\u0000\u08c9\u08ca\u0005"+
		"\u0018\u0000\u0000\u08ca\u08cb\u0003\u0106\u0083\u0000\u08cb\u08cc\u0005"+
		"\u000b\u0000\u0000\u08cc\u08cd\u0003\u0106\u0083\u0000\u08cd\u08e0\u0001"+
		"\u0000\u0000\u0000\u08ce\u08d0\u0005\u00c3\u0000\u0000\u08cf\u08ce\u0001"+
		"\u0000\u0000\u0000\u08cf\u08d0\u0001\u0000\u0000\u0000\u08d0\u08d1\u0001"+
		"\u0000\u0000\u0000\u08d1\u08d2\u0005\u0088\u0000\u0000\u08d2\u08e0\u0003"+
		".\u0017\u0000\u08d3\u08d5\u0005\u00c3\u0000\u0000\u08d4\u08d3\u0001\u0000"+
		"\u0000\u0000\u08d4\u08d5\u0001\u0000\u0000\u0000\u08d5\u08d6\u0001\u0000"+
		"\u0000\u0000\u08d6\u08d7\u0005\u0088\u0000\u0000\u08d7\u08d8\u0005\u015d"+
		"\u0000\u0000\u08d8\u08d9\u0003\u0104\u0082\u0000\u08d9\u08da\u0005\u015e"+
		"\u0000\u0000\u08da\u08e0\u0001\u0000\u0000\u0000\u08db\u08dc\u00032\u0019"+
		"\u0000\u08dc\u08dd\u0007\u0003\u0000\u0000\u08dd\u08de\u0003.\u0017\u0000"+
		"\u08de\u08e0\u0001\u0000\u0000\u0000\u08df\u08b4\u0001\u0000\u0000\u0000"+
		"\u08df\u08b5\u0001\u0000\u0000\u0000\u08df\u08b8\u0001\u0000\u0000\u0000"+
		"\u08df\u08be\u0001\u0000\u0000\u0000\u08df\u08c7\u0001\u0000\u0000\u0000"+
		"\u08df\u08cf\u0001\u0000\u0000\u0000\u08df\u08d4\u0001\u0000\u0000\u0000"+
		"\u08df\u08db\u0001\u0000\u0000\u0000\u08e0\u0119\u0001\u0000\u0000\u0000"+
		"\u08e1\u08e2\u0003\u0106\u0083\u0000\u08e2\u011b\u0001\u0000\u0000\u0000"+
		"\u08e3\u091f\u0005\u0001\u0000\u0000\u08e4\u091f\u0005\u0002\u0000\u0000"+
		"\u08e5\u091f\u0005\u0003\u0000\u0000\u08e6\u091f\u0005\u0010\u0000\u0000"+
		"\u08e7\u091f\u0005\u0014\u0000\u0000\u08e8\u091f\u0005\u0013\u0000\u0000"+
		"\u08e9\u091f\u0005\u0019\u0000\u0000\u08ea\u091f\u0005%\u0000\u0000\u08eb"+
		"\u091f\u0005&\u0000\u0000\u08ec\u091f\u0005\'\u0000\u0000\u08ed\u091f"+
		"\u0005(\u0000\u0000\u08ee\u091f\u0005\u015a\u0000\u0000\u08ef\u091f\u0005"+
		"<\u0000\u0000\u08f0\u091f\u0005\u00c5\u0000\u0000\u08f1\u091f\u0005B\u0000"+
		"\u0000\u08f2\u08f3\u0005A\u0000\u0000\u08f3\u091f\u0005\u0098\u0000\u0000"+
		"\u08f4\u091f\u0005D\u0000\u0000\u08f5\u091f\u0005E\u0000\u0000\u08f6\u091f"+
		"\u0005F\u0000\u0000\u08f7\u091f\u0005J\u0000\u0000\u08f8\u091f\u0005K"+
		"\u0000\u0000\u08f9\u091f\u0005T\u0000\u0000\u08fa\u091f\u0005_\u0000\u0000"+
		"\u08fb\u091f\u0005l\u0000\u0000\u08fc\u091f\u0005s\u0000\u0000\u08fd\u091f"+
		"\u0005\u0084\u0000\u0000\u08fe\u091f\u0005\u0086\u0000\u0000\u08ff\u091f"+
		"\u0005\u0092\u0000\u0000\u0900\u091f\u0005\u0093\u0000\u0000\u0901\u091f"+
		"\u0005\u00a3\u0000\u0000\u0902\u091f\u0005\u00a5\u0000\u0000\u0903\u091f"+
		"\u0005\u00aa\u0000\u0000\u0904\u091f\u0005\u00a9\u0000\u0000\u0905\u091f"+
		"\u0005\u00a6\u0000\u0000\u0906\u091f\u0005\u00a0\u0000\u0000\u0907\u091f"+
		"\u0005\u00ad\u0000\u0000\u0908\u091f\u0005\u00ae\u0000\u0000\u0909\u091f"+
		"\u0005\u00b6\u0000\u0000\u090a\u091f\u0005\u00b8\u0000\u0000\u090b\u091f"+
		"\u0005\u00bb\u0000\u0000\u090c\u091f\u0005\u00d9\u0000\u0000\u090d\u091f"+
		"\u0005\u00e2\u0000\u0000\u090e\u091f\u0005\u00f8\u0000\u0000\u090f\u091f"+
		"\u0005\u00fb\u0000\u0000\u0910\u091f\u0005\u0100\u0000\u0000\u0911\u091f"+
		"\u0005\u0104\u0000\u0000\u0912\u091f\u0005\u0105\u0000\u0000\u0913\u091f"+
		"\u0005\u010d\u0000\u0000\u0914\u091f\u0005\u0113\u0000\u0000\u0915\u091f"+
		"\u0005\u0119\u0000\u0000\u0916\u091f\u0005\u011c\u0000\u0000\u0917\u091f"+
		"\u0005\u011d\u0000\u0000\u0918\u091f\u0005\u0125\u0000\u0000\u0919\u091f"+
		"\u0005\u0129\u0000\u0000\u091a\u091f\u0005\u012f\u0000\u0000\u091b\u091f"+
		"\u0005\u0132\u0000\u0000\u091c\u091f\u0005\u0137\u0000\u0000\u091d\u091f"+
		"\u0005\u0146\u0000\u0000\u091e\u08e3\u0001\u0000\u0000\u0000\u091e\u08e4"+
		"\u0001\u0000\u0000\u0000\u091e\u08e5\u0001\u0000\u0000\u0000\u091e\u08e6"+
		"\u0001\u0000\u0000\u0000\u091e\u08e7\u0001\u0000\u0000\u0000\u091e\u08e8"+
		"\u0001\u0000\u0000\u0000\u091e\u08e9\u0001\u0000\u0000\u0000\u091e\u08ea"+
		"\u0001\u0000\u0000\u0000\u091e\u08eb\u0001\u0000\u0000\u0000\u091e\u08ec"+
		"\u0001\u0000\u0000\u0000\u091e\u08ed\u0001\u0000\u0000\u0000\u091e\u08ee"+
		"\u0001\u0000\u0000\u0000\u091e\u08ef\u0001\u0000\u0000\u0000\u091e\u08f0"+
		"\u0001\u0000\u0000\u0000\u091e\u08f1\u0001\u0000\u0000\u0000\u091e\u08f2"+
		"\u0001\u0000\u0000\u0000\u091e\u08f4\u0001\u0000\u0000\u0000\u091e\u08f5"+
		"\u0001\u0000\u0000\u0000\u091e\u08f6\u0001\u0000\u0000\u0000\u091e\u08f7"+
		"\u0001\u0000\u0000\u0000\u091e\u08f8\u0001\u0000\u0000\u0000\u091e\u08f9"+
		"\u0001\u0000\u0000\u0000\u091e\u08fa\u0001\u0000\u0000\u0000\u091e\u08fb"+
		"\u0001\u0000\u0000\u0000\u091e\u08fc\u0001\u0000\u0000\u0000\u091e\u08fd"+
		"\u0001\u0000\u0000\u0000\u091e\u08fe\u0001\u0000\u0000\u0000\u091e\u08ff"+
		"\u0001\u0000\u0000\u0000\u091e\u0900\u0001\u0000\u0000\u0000\u091e\u0901"+
		"\u0001\u0000\u0000\u0000\u091e\u0902\u0001\u0000\u0000\u0000\u091e\u0903"+
		"\u0001\u0000\u0000\u0000\u091e\u0904\u0001\u0000\u0000\u0000\u091e\u0905"+
		"\u0001\u0000\u0000\u0000\u091e\u0906\u0001\u0000\u0000\u0000\u091e\u0907"+
		"\u0001\u0000\u0000\u0000\u091e\u0908\u0001\u0000\u0000\u0000\u091e\u0909"+
		"\u0001\u0000\u0000\u0000\u091e\u090a\u0001\u0000\u0000\u0000\u091e\u090b"+
		"\u0001\u0000\u0000\u0000\u091e\u090c\u0001\u0000\u0000\u0000\u091e\u090d"+
		"\u0001\u0000\u0000\u0000\u091e\u090e\u0001\u0000\u0000\u0000\u091e\u090f"+
		"\u0001\u0000\u0000\u0000\u091e\u0910\u0001\u0000\u0000\u0000\u091e\u0911";
	private static final String _serializedATNSegment1 =
		"\u0001\u0000\u0000\u0000\u091e\u0912\u0001\u0000\u0000\u0000\u091e\u0913"+
		"\u0001\u0000\u0000\u0000\u091e\u0914\u0001\u0000\u0000\u0000\u091e\u0915"+
		"\u0001\u0000\u0000\u0000\u091e\u0916\u0001\u0000\u0000\u0000\u091e\u0917"+
		"\u0001\u0000\u0000\u0000\u091e\u0918\u0001\u0000\u0000\u0000\u091e\u0919"+
		"\u0001\u0000\u0000\u0000\u091e\u091a\u0001\u0000\u0000\u0000\u091e\u091b"+
		"\u0001\u0000\u0000\u0000\u091e\u091c\u0001\u0000\u0000\u0000\u091e\u091d"+
		"\u0001\u0000\u0000\u0000\u091f\u011d\u0001\u0000\u0000\u0000\u0920\u0921"+
		"\u0007\u0019\u0000\u0000\u0921\u011f\u0001\u0000\u0000\u0000\u0922\u0924"+
		"\u0003\u00b8\\\u0000\u0923\u0925\u0003\u013a\u009d\u0000\u0924\u0923\u0001"+
		"\u0000\u0000\u0000\u0924\u0925\u0001\u0000\u0000\u0000\u0925\u0927\u0001"+
		"\u0000\u0000\u0000\u0926\u0928\u0003\u000e\u0007\u0000\u0927\u0926\u0001"+
		"\u0000\u0000\u0000\u0927\u0928\u0001\u0000\u0000\u0000\u0928\u092a\u0001"+
		"\u0000\u0000\u0000\u0929\u092b\u0003\u0010\b\u0000\u092a\u0929\u0001\u0000"+
		"\u0000\u0000\u092a\u092b\u0001\u0000\u0000\u0000\u092b\u092d\u0001\u0000"+
		"\u0000\u0000\u092c\u092e\u0003\u0122\u0091\u0000\u092d\u092c\u0001\u0000"+
		"\u0000\u0000\u092d\u092e\u0001\u0000\u0000\u0000\u092e\u0931\u0001\u0000"+
		"\u0000\u0000\u092f\u0930\u0005\u013e\u0000\u0000\u0930\u0932\u0007\u001a"+
		"\u0000\u0000\u0931\u092f\u0001\u0000\u0000\u0000\u0931\u0932\u0001\u0000"+
		"\u0000\u0000\u0932\u0121\u0001\u0000\u0000\u0000\u0933\u0944\u0005t\u0000"+
		"\u0000\u0934\u0935\u0005\u00e3\u0000\u0000\u0935\u0945\u0005\u00cd\u0000"+
		"\u0000\u0936\u0937\u0005p\u0000\u0000\u0937\u0945\u0005\u00cd\u0000\u0000"+
		"\u0938\u0942\u0005\u012e\u0000\u0000\u0939\u093a\u0005\u00ca\u0000\u0000"+
		"\u093a\u093f\u0003\u00be_\u0000\u093b\u093c\u0005\u015f\u0000\u0000\u093c"+
		"\u093e\u0003\u00be_\u0000\u093d\u093b\u0001\u0000\u0000\u0000\u093e\u0941"+
		"\u0001\u0000\u0000\u0000\u093f\u093d\u0001\u0000\u0000\u0000\u093f\u0940"+
		"\u0001\u0000\u0000\u0000\u0940\u0943\u0001\u0000\u0000\u0000\u0941\u093f"+
		"\u0001\u0000\u0000\u0000\u0942\u0939\u0001\u0000\u0000\u0000\u0942\u0943"+
		"\u0001\u0000\u0000\u0000\u0943\u0945\u0001\u0000\u0000\u0000\u0944\u0934"+
		"\u0001\u0000\u0000\u0000\u0944\u0936\u0001\u0000\u0000\u0000\u0944\u0938"+
		"\u0001\u0000\u0000\u0000\u0945\u0123\u0001\u0000\u0000\u0000\u0946\u0947"+
		"\u0007\u001b\u0000\u0000\u0947\u0125\u0001\u0000\u0000\u0000\u0948\u0949"+
		"\u0005w\u0000\u0000\u0949\u094e\u0003\u0128\u0094\u0000\u094a\u094b\u0005"+
		"\u015f\u0000\u0000\u094b\u094d\u0003\u0128\u0094\u0000\u094c\u094a\u0001"+
		"\u0000\u0000\u0000\u094d\u0950\u0001\u0000\u0000\u0000\u094e\u094c\u0001"+
		"\u0000\u0000\u0000\u094e\u094f\u0001\u0000\u0000\u0000\u094f\u0127\u0001"+
		"\u0000\u0000\u0000\u0950\u094e\u0001\u0000\u0000\u0000\u0951\u0952\u0006"+
		"\u0094\uffff\uffff\u0000\u0952\u0953\u0003\u012c\u0096\u0000\u0953\u0958"+
		"\u0001\u0000\u0000\u0000\u0954\u0955\n\u0001\u0000\u0000\u0955\u0957\u0003"+
		"\u012a\u0095\u0000\u0956\u0954\u0001\u0000\u0000\u0000\u0957\u095a\u0001"+
		"\u0000\u0000\u0000\u0958\u0956\u0001\u0000\u0000\u0000\u0958\u0959\u0001"+
		"\u0000\u0000\u0000\u0959\u0129\u0001\u0000\u0000\u0000\u095a\u0958\u0001"+
		"\u0000\u0000\u0000\u095b\u095d\u0005\u008d\u0000\u0000\u095c\u095b\u0001"+
		"\u0000\u0000\u0000\u095c\u095d\u0001\u0000\u0000\u0000\u095d\u095e\u0001"+
		"\u0000\u0000\u0000\u095e\u095f\u0005\u009a\u0000\u0000\u095f\u0963\u0003"+
		"\u0128\u0094\u0000\u0960\u0961\u0005\u00cc\u0000\u0000\u0961\u0964\u0003"+
		",\u0016\u0000\u0962\u0964\u0003\u012e\u0097\u0000\u0963\u0960\u0001\u0000"+
		"\u0000\u0000\u0963\u0962\u0001\u0000\u0000\u0000\u0964\u098f\u0001\u0000"+
		"\u0000\u0000\u0965\u0967\u0005\u00a2\u0000\u0000\u0966\u0968\u0005\u00d3"+
		"\u0000\u0000\u0967\u0966\u0001\u0000\u0000\u0000\u0967\u0968\u0001\u0000"+
		"\u0000\u0000\u0968\u0969\u0001\u0000\u0000\u0000\u0969\u096a\u0005\u009a"+
		"\u0000\u0000\u096a\u096e\u0003\u0128\u0094\u0000\u096b\u096c\u0005\u00cc"+
		"\u0000\u0000\u096c\u096f\u0003,\u0016\u0000\u096d\u096f\u0003\u012e\u0097"+
		"\u0000\u096e\u096b\u0001\u0000\u0000\u0000\u096e\u096d\u0001\u0000\u0000"+
		"\u0000\u096f\u098f\u0001\u0000\u0000\u0000\u0970\u0972\u0005\u00f0\u0000"+
		"\u0000\u0971\u0973\u0005\u00d3\u0000\u0000\u0972\u0971\u0001\u0000\u0000"+
		"\u0000\u0972\u0973\u0001\u0000\u0000\u0000\u0973\u0974\u0001\u0000\u0000"+
		"\u0000\u0974\u0975\u0005\u009a\u0000\u0000\u0975\u0979\u0003\u0128\u0094"+
		"\u0000\u0976\u0977\u0005\u00cc\u0000\u0000\u0977\u097a\u0003,\u0016\u0000"+
		"\u0978\u097a\u0003\u012e\u0097\u0000\u0979\u0976\u0001\u0000\u0000\u0000"+
		"\u0979\u0978\u0001\u0000\u0000\u0000\u097a\u098f\u0001\u0000\u0000\u0000"+
		"\u097b\u097c\u0005?\u0000\u0000\u097c\u097f\u0005\u009a\u0000\u0000\u097d"+
		"\u0980\u0003\u012c\u0096\u0000\u097e\u0980\u0003\u0128\u0094\u0000\u097f"+
		"\u097d\u0001\u0000\u0000\u0000\u097f\u097e\u0001\u0000\u0000\u0000\u0980"+
		"\u098f\u0001\u0000\u0000\u0000\u0981\u0987\u0005\u00be\u0000\u0000\u0982"+
		"\u0984\u0007\u001c\u0000\u0000\u0983\u0985\u0005\u00d3\u0000\u0000\u0984"+
		"\u0983\u0001\u0000\u0000\u0000\u0984\u0985\u0001\u0000\u0000\u0000\u0985"+
		"\u0988\u0001\u0000\u0000\u0000\u0986\u0988\u0005\u008d\u0000\u0000\u0987"+
		"\u0982\u0001\u0000\u0000\u0000\u0987\u0986\u0001\u0000\u0000\u0000\u0987"+
		"\u0988\u0001\u0000\u0000\u0000\u0988\u0989\u0001\u0000\u0000\u0000\u0989"+
		"\u098c\u0005\u009a\u0000\u0000\u098a\u098d\u0003\u012c\u0096\u0000\u098b"+
		"\u098d\u0003\u0128\u0094\u0000\u098c\u098a\u0001\u0000\u0000\u0000\u098c"+
		"\u098b\u0001\u0000\u0000\u0000\u098d\u098f\u0001\u0000\u0000\u0000\u098e"+
		"\u095c\u0001\u0000\u0000\u0000\u098e\u0965\u0001\u0000\u0000\u0000\u098e"+
		"\u0970\u0001\u0000\u0000\u0000\u098e\u097b\u0001\u0000\u0000\u0000\u098e"+
		"\u0981\u0001\u0000\u0000\u0000\u098f\u012b\u0001\u0000\u0000\u0000\u0990"+
		"\u0993\u0003\u00dcn\u0000\u0991\u0993\u0003\u00fc~\u0000\u0992\u0990\u0001"+
		"\u0000\u0000\u0000\u0992\u0991\u0001\u0000\u0000\u0000\u0993\u0995\u0001"+
		"\u0000\u0000\u0000\u0994\u0996\u0003\u0130\u0098\u0000\u0995\u0994\u0001"+
		"\u0000\u0000\u0000\u0995\u0996\u0001\u0000\u0000\u0000\u0996\u099e\u0001"+
		"\u0000\u0000\u0000\u0997\u099a\u0003.\u0017\u0000\u0998\u099a\u0003\u0132"+
		"\u0099\u0000\u0999\u0997\u0001\u0000\u0000\u0000\u0999\u0998\u0001\u0000"+
		"\u0000\u0000\u099a\u099b\u0001\u0000\u0000\u0000\u099b\u099c\u0003\u0130"+
		"\u0098\u0000\u099c\u099e\u0001\u0000\u0000\u0000\u099d\u0992\u0001\u0000"+
		"\u0000\u0000\u099d\u0999\u0001\u0000\u0000\u0000\u099e\u012d\u0001\u0000"+
		"\u0000\u0000\u099f\u09a0\u0005\u0133\u0000\u0000\u09a0\u09a1\u0005\u015d"+
		"\u0000\u0000\u09a1\u09a6\u0003\u00be_\u0000\u09a2\u09a3\u0005\u015f\u0000"+
		"\u0000\u09a3\u09a5\u0003\u00be_\u0000\u09a4\u09a2\u0001\u0000\u0000\u0000"+
		"\u09a5\u09a8\u0001\u0000\u0000\u0000\u09a6\u09a4\u0001\u0000\u0000\u0000"+
		"\u09a6\u09a7\u0001\u0000\u0000\u0000\u09a7\u09a9\u0001\u0000\u0000\u0000"+
		"\u09a8\u09a6\u0001\u0000\u0000\u0000\u09a9\u09aa\u0005\u015e\u0000\u0000"+
		"\u09aa\u012f\u0001\u0000\u0000\u0000\u09ab\u09ad\u0005\u000e\u0000\u0000"+
		"\u09ac\u09ab\u0001\u0000\u0000\u0000\u09ac\u09ad\u0001\u0000\u0000\u0000"+
		"\u09ad\u09ae\u0001\u0000\u0000\u0000\u09ae\u09ba\u0003\u00e8t\u0000\u09af"+
		"\u09b0\u0005\u015d\u0000\u0000\u09b0\u09b5\u0003\u00be_\u0000\u09b1\u09b2"+
		"\u0005\u015f\u0000\u0000\u09b2\u09b4\u0003\u00be_\u0000\u09b3\u09b1\u0001"+
		"\u0000\u0000\u0000\u09b4\u09b7\u0001\u0000\u0000\u0000\u09b5\u09b3\u0001"+
		"\u0000\u0000\u0000\u09b5\u09b6\u0001\u0000\u0000\u0000\u09b6\u09b8\u0001"+
		"\u0000\u0000\u0000\u09b7\u09b5\u0001\u0000\u0000\u0000\u09b8\u09b9\u0005"+
		"\u015e\u0000\u0000\u09b9\u09bb\u0001\u0000\u0000\u0000\u09ba\u09af\u0001"+
		"\u0000\u0000\u0000\u09ba\u09bb\u0001\u0000\u0000\u0000\u09bb\u0131\u0001"+
		"\u0000\u0000\u0000\u09bc\u09bd\u0005\u0118\u0000\u0000\u09bd\u09be\u0003"+
		"\u00eau\u0000\u09be\u09c7\u0005\u015d\u0000\u0000\u09bf\u09c4\u0003\u0134"+
		"\u009a\u0000\u09c0\u09c1\u0005\u015f\u0000\u0000\u09c1\u09c3\u0003\u0134"+
		"\u009a\u0000\u09c2\u09c0\u0001\u0000\u0000\u0000\u09c3\u09c6\u0001\u0000"+
		"\u0000\u0000\u09c4\u09c2\u0001\u0000\u0000\u0000\u09c4\u09c5\u0001\u0000"+
		"\u0000\u0000\u09c5\u09c8\u0001\u0000\u0000\u0000\u09c6\u09c4\u0001\u0000"+
		"\u0000\u0000\u09c7\u09bf\u0001\u0000\u0000\u0000\u09c7\u09c8\u0001\u0000"+
		"\u0000\u0000\u09c8\u09c9\u0001\u0000\u0000\u0000\u09c9\u09ca\u0005\u015e"+
		"\u0000\u0000\u09ca\u0133\u0001\u0000\u0000\u0000\u09cb\u09cc\u0003\u0106"+
		"\u0083\u0000\u09cc\u0135\u0001\u0000\u0000\u0000\u09cd\u09ce\u0005\u0082"+
		"\u0000\u0000\u09ce\u09e3\u0005\u001f\u0000\u0000\u09cf\u09d4\u0003\u00e6"+
		"s\u0000\u09d0\u09d1\u0005\u015f\u0000\u0000\u09d1\u09d3\u0003\u00e6s\u0000"+
		"\u09d2\u09d0\u0001\u0000\u0000\u0000\u09d3\u09d6\u0001\u0000\u0000\u0000"+
		"\u09d4\u09d2\u0001\u0000\u0000\u0000\u09d4\u09d5\u0001\u0000\u0000\u0000"+
		"\u09d5\u09e4\u0001\u0000\u0000\u0000\u09d6\u09d4\u0001\u0000\u0000\u0000"+
		"\u09d7\u09d8\u0005\u00f3\u0000\u0000\u09d8\u09d9\u0005\u015d\u0000\u0000"+
		"\u09d9\u09de\u0003\u00e6s\u0000\u09da\u09db\u0005\u015f\u0000\u0000\u09db"+
		"\u09dd\u0003\u00e6s\u0000\u09dc\u09da\u0001\u0000\u0000\u0000\u09dd\u09e0"+
		"\u0001\u0000\u0000\u0000\u09de\u09dc\u0001\u0000\u0000\u0000\u09de\u09df"+
		"\u0001\u0000\u0000\u0000\u09df\u09e1\u0001\u0000\u0000\u0000\u09e0\u09de"+
		"\u0001\u0000\u0000\u0000\u09e1\u09e2\u0005\u015e\u0000\u0000\u09e2\u09e4"+
		"\u0001\u0000\u0000\u0000\u09e3\u09cf\u0001\u0000\u0000\u0000\u09e3\u09d7"+
		"\u0001\u0000\u0000\u0000\u09e4\u0137\u0001\u0000\u0000\u0000\u09e5\u09e6"+
		"\u0005\u0083\u0000\u0000\u09e6\u09e7\u0003,\u0016\u0000\u09e7\u0139\u0001"+
		"\u0000\u0000\u0000\u09e8\u09e9\u0005\u00d1\u0000\u0000\u09e9\u09ea\u0005"+
		"\u001f\u0000\u0000\u09ea\u09ef\u0003\u013c\u009e\u0000\u09eb\u09ec\u0005"+
		"\u015f\u0000\u0000\u09ec\u09ee\u0003\u013c\u009e\u0000\u09ed\u09eb\u0001"+
		"\u0000\u0000\u0000\u09ee\u09f1\u0001\u0000\u0000\u0000\u09ef\u09ed\u0001"+
		"\u0000\u0000\u0000\u09ef\u09f0\u0001\u0000\u0000\u0000\u09f0\u013b\u0001"+
		"\u0000\u0000\u0000\u09f1\u09ef\u0001\u0000\u0000\u0000\u09f2\u09f6\u0003"+
		"\u00e6s\u0000\u09f3\u09f6\u0003\u0140\u00a0\u0000\u09f4\u09f6\u0003\u0106"+
		"\u0083\u0000\u09f5\u09f2\u0001\u0000\u0000\u0000\u09f5\u09f3\u0001\u0000"+
		"\u0000\u0000\u09f5\u09f4\u0001\u0000\u0000\u0000\u09f6\u09f8\u0001\u0000"+
		"\u0000\u0000\u09f7\u09f9\u0003\u013e\u009f\u0000\u09f8\u09f7\u0001\u0000"+
		"\u0000\u0000\u09f8\u09f9\u0001\u0000\u0000\u0000\u09f9\u09fe\u0001\u0000"+
		"\u0000\u0000\u09fa\u09fb\u0005\u00c6\u0000\u0000\u09fb\u09ff\u0005q\u0000"+
		"\u0000\u09fc\u09fd\u0005\u00c6\u0000\u0000\u09fd\u09ff\u0005\u009f\u0000"+
		"\u0000\u09fe\u09fa\u0001\u0000\u0000\u0000\u09fe\u09fc\u0001\u0000\u0000"+
		"\u0000\u09fe\u09ff\u0001\u0000\u0000\u0000\u09ff\u013d\u0001\u0000\u0000"+
		"\u0000\u0a00\u0a01\u0007\u001d\u0000\u0000\u0a01\u013f\u0001\u0000\u0000"+
		"\u0000\u0a02\u0a03\u0003\u0102\u0081\u0000\u0a03\u0141\u0001\u0000\u0000"+
		"\u0000\u0a04\u0a05\u0005\u0134\u0000\u0000\u0a05\u0a06\u0005\u015d\u0000"+
		"\u0000\u0a06\u0a0b\u00034\u001a\u0000\u0a07\u0a08\u0005\u015f\u0000\u0000"+
		"\u0a08\u0a0a\u00034\u001a\u0000\u0a09\u0a07\u0001\u0000\u0000\u0000\u0a0a"+
		"\u0a0d\u0001\u0000\u0000\u0000\u0a0b\u0a09\u0001\u0000\u0000\u0000\u0a0b"+
		"\u0a0c\u0001\u0000\u0000\u0000\u0a0c\u0a0e\u0001\u0000\u0000\u0000\u0a0d"+
		"\u0a0b\u0001\u0000\u0000\u0000\u0a0e\u0a1d\u0005\u015e\u0000\u0000\u0a0f"+
		"\u0a10\u0005\u015f\u0000\u0000\u0a10\u0a11\u0005\u015d\u0000\u0000\u0a11"+
		"\u0a16\u00034\u001a\u0000\u0a12\u0a13\u0005\u015f\u0000\u0000\u0a13\u0a15"+
		"\u00034\u001a\u0000\u0a14\u0a12\u0001\u0000\u0000\u0000\u0a15\u0a18\u0001"+
		"\u0000\u0000\u0000\u0a16\u0a14\u0001\u0000\u0000\u0000\u0a16\u0a17\u0001"+
		"\u0000\u0000\u0000\u0a17\u0a19\u0001\u0000\u0000\u0000\u0a18\u0a16\u0001"+
		"\u0000\u0000\u0000\u0a19\u0a1a\u0005\u015e\u0000\u0000\u0a1a\u0a1c\u0001"+
		"\u0000\u0000\u0000\u0a1b\u0a0f\u0001\u0000\u0000\u0000\u0a1c\u0a1f\u0001"+
		"\u0000\u0000\u0000\u0a1d\u0a1b\u0001\u0000\u0000\u0000\u0a1d\u0a1e\u0001"+
		"\u0000\u0000\u0000\u0a1e\u0a2a\u0001\u0000\u0000\u0000\u0a1f\u0a1d\u0001"+
		"\u0000\u0000\u0000\u0a20\u0a21\u0005\u0134\u0000\u0000\u0a21\u0a26\u0003"+
		"4\u001a\u0000\u0a22\u0a23\u0005\u015f\u0000\u0000\u0a23\u0a25\u00034\u001a"+
		"\u0000\u0a24\u0a22\u0001\u0000\u0000\u0000\u0a25\u0a28\u0001\u0000\u0000"+
		"\u0000\u0a26\u0a24\u0001\u0000\u0000\u0000\u0a26\u0a27\u0001\u0000\u0000"+
		"\u0000\u0a27\u0a2a\u0001\u0000\u0000\u0000\u0a28\u0a26\u0001\u0000\u0000"+
		"\u0000\u0a29\u0a04\u0001\u0000\u0000\u0000\u0a29\u0a20\u0001\u0000\u0000"+
		"\u0000\u0a2a\u0a2c\u0001\u0000\u0000\u0000\u0a2b\u0a2d\u0003\u013a\u009d"+
		"\u0000\u0a2c\u0a2b\u0001\u0000\u0000\u0000\u0a2c\u0a2d\u0001\u0000\u0000"+
		"\u0000\u0a2d\u0a2f\u0001\u0000\u0000\u0000\u0a2e\u0a30\u0003\u000e\u0007"+
		"\u0000\u0a2f\u0a2e\u0001\u0000\u0000\u0000\u0a2f\u0a30\u0001\u0000\u0000"+
		"\u0000\u0a30\u0a32\u0001\u0000\u0000\u0000\u0a31\u0a33\u0003\u0010\b\u0000"+
		"\u0a32\u0a31\u0001\u0000\u0000\u0000\u0a32\u0a33\u0001\u0000\u0000\u0000"+
		"\u0a33\u0143\u0001\u0000\u0000\u0000\u0115\u0147\u014e\u0153\u0158\u0160"+
		"\u016d\u0174\u0179\u017c\u017f\u0184\u018c\u019b\u019e\u01a3\u01a6\u01ad"+
		"\u01b2\u01b8\u01bd\u01d4\u01de\u01e3\u01ec\u01f8\u01fb\u0208\u020c\u021b"+
		"\u0223\u0239\u023f\u0245\u0249\u0252\u0259\u0262\u0268\u0271\u0276\u0279"+
		"\u027c\u0286\u028c\u028f\u0292\u029c\u02ae\u02b5\u02bd\u02c3\u02c7\u02fb"+
		"\u0322\u0326\u035f\u036c\u0370\u0379\u037d\u0381\u0387\u038d\u0393\u0395"+
		"\u039c\u03a2\u03b4\u03bc\u03bf\u03c3\u03c8\u03cc\u03d2\u03e8\u03f6\u03fa"+
		"\u0405\u0412\u0415\u0419\u0432\u0434\u0437\u043f\u0449\u044b\u0450\u0455"+
		"\u045f\u0461\u0463\u0468\u046c\u046f\u0472\u0476\u0478\u047e\u0485\u048a"+
		"\u0491\u0498\u049f\u04a3\u04a8\u04aa\u04b2\u04be\u04c4\u04d5\u04d9\u04e7"+
		"\u04eb\u04f0\u04f4\u0530\u0536\u053e\u0546\u0551\u0556\u0568\u0582\u058b"+
		"\u058f\u0595\u05a7\u05b9\u05c4\u05c6\u05ca\u05d3\u05d8\u05dc\u05ea\u05f1"+
		"\u05f5\u05f8\u0601\u0606\u060a\u0612\u0618\u061d\u0621\u0629\u062d\u0637"+
		"\u064a\u0650\u0653\u0656\u0660\u0675\u0677\u0686\u068f\u06a5\u06af\u06d0"+
		"\u06d5\u06da\u06dd\u06e0\u06e6\u06e9\u06ec\u06f2\u06f7\u06fd\u0703\u0706"+
		"\u0708\u070d\u0714\u0719\u071c\u071f\u0722\u0725\u0728\u072d\u0735\u0737"+
		"\u0747\u0783\u078e\u0795\u079b\u079f\u07a8\u07af\u07b6\u07bf\u07c6\u07cd"+
		"\u07d4\u07dd\u07ec\u0803\u081a\u0821\u0823\u0829\u082e\u0835\u083c\u0840"+
		"\u0844\u0848\u084e\u0855\u0863\u0868\u0874\u087f\u0887\u088c\u0898\u08a3"+
		"\u08ab\u08b0\u08ba\u08be\u08c4\u08c7\u08cf\u08d4\u08df\u091e\u0924\u0927"+
		"\u092a\u092d\u0931\u093f\u0942\u0944\u094e\u0958\u095c\u0963\u0967\u096e"+
		"\u0972\u0979\u097f\u0984\u0987\u098c\u098e\u0992\u0995\u0999\u099d\u09a6"+
		"\u09ac\u09b5\u09ba\u09c4\u09c7\u09d4\u09de\u09e3\u09ef\u09f5\u09f8\u09fe"+
		"\u0a0b\u0a16\u0a1d\u0a26\u0a29\u0a2c\u0a2f\u0a32";
	public static final String _serializedATN = Utils.join(
		new String[] {
			_serializedATNSegment0,
			_serializedATNSegment1
		},
		""
	);
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}