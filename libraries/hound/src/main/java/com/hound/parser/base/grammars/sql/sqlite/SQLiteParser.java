// Generated from C:/AIDA/aida-root/libraries/hound/src/main/resources/grammars/sql/sqlite/SQLiteParser.g4 by ANTLR 4.13.2
package com.hound.parser.base.grammars.sql.sqlite;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class SQLiteParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		SCOL=1, DOT=2, OPEN_PAR=3, CLOSE_PAR=4, COMMA=5, ASSIGN=6, STAR=7, PLUS=8, 
		MINUS=9, TILDE=10, PIPE2=11, DIV=12, MOD=13, LT2=14, GT2=15, AMP=16, PIPE=17, 
		LT=18, LT_EQ=19, GT=20, GT_EQ=21, EQ=22, NOT_EQ1=23, NOT_EQ2=24, JPTR=25, 
		JPTR2=26, ABORT_=27, ACTION_=28, ADD_=29, AFTER_=30, ALL_=31, ALTER_=32, 
		ALWAYS_=33, ANALYZE_=34, AND_=35, AS_=36, ASC_=37, ATTACH_=38, AUTOINCREMENT_=39, 
		BEFORE_=40, BEGIN_=41, BETWEEN_=42, BY_=43, CASCADE_=44, CASE_=45, CAST_=46, 
		CHECK_=47, COLLATE_=48, COLUMN_=49, COMMIT_=50, CONFLICT_=51, CONSTRAINT_=52, 
		CREATE_=53, CROSS_=54, CURRENT_=55, CURRENT_DATE_=56, CURRENT_TIME_=57, 
		CURRENT_TIMESTAMP_=58, DATABASE_=59, DEFAULT_=60, DEFERRABLE_=61, DEFERRED_=62, 
		DELETE_=63, DESC_=64, DETACH_=65, DISTINCT_=66, DO_=67, DROP_=68, EACH_=69, 
		ELSE_=70, END_=71, ESCAPE_=72, EXCEPT_=73, EXCLUDE_=74, EXCLUSIVE_=75, 
		EXISTS_=76, EXPLAIN_=77, FAIL_=78, FALSE_=79, FILTER_=80, FIRST_=81, FOLLOWING_=82, 
		FOR_=83, FOREIGN_=84, FROM_=85, FULL_=86, GENERATED_=87, GLOB_=88, GROUP_=89, 
		GROUPS_=90, HAVING_=91, IF_=92, IGNORE_=93, IMMEDIATE_=94, IN_=95, INDEX_=96, 
		INDEXED_=97, INITIALLY_=98, INNER_=99, INSERT_=100, INSTEAD_=101, INTERSECT_=102, 
		INTO_=103, IS_=104, ISNULL_=105, JOIN_=106, KEY_=107, LAST_=108, LEFT_=109, 
		LIKE_=110, LIMIT_=111, MATCH_=112, MATERIALIZED_=113, NATURAL_=114, NO_=115, 
		NOT_=116, NOTHING_=117, NOTNULL_=118, NULL_=119, NULLS_=120, OF_=121, 
		OFFSET_=122, ON_=123, OR_=124, ORDER_=125, OTHERS_=126, OUTER_=127, OVER_=128, 
		PARTITION_=129, PLAN_=130, PRAGMA_=131, PRECEDING_=132, PRIMARY_=133, 
		QUERY_=134, RAISE_=135, RANGE_=136, RECURSIVE_=137, REFERENCES_=138, REGEXP_=139, 
		REINDEX_=140, RELEASE_=141, RENAME_=142, REPLACE_=143, RESTRICT_=144, 
		RETURNING_=145, RIGHT_=146, ROLLBACK_=147, ROW_=148, ROWID_=149, ROWS_=150, 
		SAVEPOINT_=151, SELECT_=152, SET_=153, STORED_=154, STRICT_=155, TABLE_=156, 
		TEMP_=157, TEMPORARY_=158, THEN_=159, TIES_=160, TO_=161, TRANSACTION_=162, 
		TRIGGER_=163, TRUE_=164, UNBOUNDED_=165, UNION_=166, UNIQUE_=167, UPDATE_=168, 
		USING_=169, VACUUM_=170, VALUES_=171, VIEW_=172, VIRTUAL_=173, WHEN_=174, 
		WHERE_=175, WINDOW_=176, WITH_=177, WITHIN_=178, WITHOUT_=179, IDENTIFIER=180, 
		NUMERIC_LITERAL=181, BIND_PARAMETER=182, STRING_LITERAL=183, BLOB_LITERAL=184, 
		SINGLE_LINE_COMMENT=185, MULTILINE_COMMENT=186, SPACES=187, UNEXPECTED_CHAR=188;
	public static final int
		RULE_parse = 0, RULE_sql_stmt_list = 1, RULE_sql_stmt = 2, RULE_alter_table_stmt = 3, 
		RULE_analyze_stmt = 4, RULE_attach_stmt = 5, RULE_begin_stmt = 6, RULE_commit_stmt = 7, 
		RULE_rollback_stmt = 8, RULE_savepoint_stmt = 9, RULE_release_stmt = 10, 
		RULE_create_index_stmt = 11, RULE_indexed_column = 12, RULE_create_table_stmt = 13, 
		RULE_table_options = 14, RULE_column_def = 15, RULE_type_name = 16, RULE_column_constraint = 17, 
		RULE_signed_number = 18, RULE_table_constraint = 19, RULE_foreign_key_clause = 20, 
		RULE_conflict_clause = 21, RULE_create_trigger_stmt = 22, RULE_create_view_stmt = 23, 
		RULE_create_virtual_table_stmt = 24, RULE_with_clause = 25, RULE_common_table_expression = 26, 
		RULE_cte_table_name = 27, RULE_delete_stmt = 28, RULE_detach_stmt = 29, 
		RULE_drop_stmt = 30, RULE_expr = 31, RULE_expr_or = 32, RULE_expr_and = 33, 
		RULE_expr_not = 34, RULE_expr_binary = 35, RULE_expr_comparison = 36, 
		RULE_expr_bitwise = 37, RULE_expr_addition = 38, RULE_expr_multiplication = 39, 
		RULE_expr_string = 40, RULE_expr_collate = 41, RULE_expr_unary = 42, RULE_expr_base = 43, 
		RULE_expr_recursive = 44, RULE_raise_function = 45, RULE_literal_value = 46, 
		RULE_percentile_clause = 47, RULE_value_row = 48, RULE_values_clause = 49, 
		RULE_insert_stmt = 50, RULE_returning_clause = 51, RULE_upsert_clause = 52, 
		RULE_pragma_stmt = 53, RULE_pragma_value = 54, RULE_reindex_stmt = 55, 
		RULE_select_stmt = 56, RULE_join_clause = 57, RULE_select_core = 58, RULE_table_or_subquery = 59, 
		RULE_result_column = 60, RULE_join_operator = 61, RULE_join_constraint = 62, 
		RULE_compound_operator = 63, RULE_update_stmt = 64, RULE_column_name_list = 65, 
		RULE_qualified_table_name = 66, RULE_vacuum_stmt = 67, RULE_filter_clause = 68, 
		RULE_window_defn = 69, RULE_over_clause = 70, RULE_frame_spec = 71, RULE_frame_clause = 72, 
		RULE_order_clause = 73, RULE_limit_clause = 74, RULE_ordering_term = 75, 
		RULE_asc_desc = 76, RULE_frame_left = 77, RULE_frame_right = 78, RULE_frame_single = 79, 
		RULE_error_message = 80, RULE_filename = 81, RULE_module_argument = 82, 
		RULE_module_argument_outer = 83, RULE_module_argument_inner = 84, RULE_fallback_excluding_conflicts = 85, 
		RULE_join_keyword = 86, RULE_fallback = 87, RULE_name = 88, RULE_function_name = 89, 
		RULE_schema_name = 90, RULE_table_name = 91, RULE_table_or_index_name = 92, 
		RULE_column_name = 93, RULE_column_name_excluding_string = 94, RULE_column_alias = 95, 
		RULE_collation_name = 96, RULE_foreign_table = 97, RULE_index_name = 98, 
		RULE_trigger_name = 99, RULE_view_name = 100, RULE_module_name = 101, 
		RULE_pragma_name = 102, RULE_savepoint_name = 103, RULE_table_alias = 104, 
		RULE_table_alias_excluding_joins = 105, RULE_window_name = 106, RULE_alias = 107, 
		RULE_base_window_name = 108, RULE_table_function_name = 109, RULE_any_name_excluding_raise = 110, 
		RULE_any_name_excluding_joins = 111, RULE_any_name_excluding_string = 112, 
		RULE_any_name = 113;
	private static String[] makeRuleNames() {
		return new String[] {
			"parse", "sql_stmt_list", "sql_stmt", "alter_table_stmt", "analyze_stmt", 
			"attach_stmt", "begin_stmt", "commit_stmt", "rollback_stmt", "savepoint_stmt", 
			"release_stmt", "create_index_stmt", "indexed_column", "create_table_stmt", 
			"table_options", "column_def", "type_name", "column_constraint", "signed_number", 
			"table_constraint", "foreign_key_clause", "conflict_clause", "create_trigger_stmt", 
			"create_view_stmt", "create_virtual_table_stmt", "with_clause", "common_table_expression", 
			"cte_table_name", "delete_stmt", "detach_stmt", "drop_stmt", "expr", 
			"expr_or", "expr_and", "expr_not", "expr_binary", "expr_comparison", 
			"expr_bitwise", "expr_addition", "expr_multiplication", "expr_string", 
			"expr_collate", "expr_unary", "expr_base", "expr_recursive", "raise_function", 
			"literal_value", "percentile_clause", "value_row", "values_clause", "insert_stmt", 
			"returning_clause", "upsert_clause", "pragma_stmt", "pragma_value", "reindex_stmt", 
			"select_stmt", "join_clause", "select_core", "table_or_subquery", "result_column", 
			"join_operator", "join_constraint", "compound_operator", "update_stmt", 
			"column_name_list", "qualified_table_name", "vacuum_stmt", "filter_clause", 
			"window_defn", "over_clause", "frame_spec", "frame_clause", "order_clause", 
			"limit_clause", "ordering_term", "asc_desc", "frame_left", "frame_right", 
			"frame_single", "error_message", "filename", "module_argument", "module_argument_outer", 
			"module_argument_inner", "fallback_excluding_conflicts", "join_keyword", 
			"fallback", "name", "function_name", "schema_name", "table_name", "table_or_index_name", 
			"column_name", "column_name_excluding_string", "column_alias", "collation_name", 
			"foreign_table", "index_name", "trigger_name", "view_name", "module_name", 
			"pragma_name", "savepoint_name", "table_alias", "table_alias_excluding_joins", 
			"window_name", "alias", "base_window_name", "table_function_name", "any_name_excluding_raise", 
			"any_name_excluding_joins", "any_name_excluding_string", "any_name"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "';'", "'.'", "'('", "')'", "','", "'='", "'*'", "'+'", "'-'", 
			"'~'", "'||'", "'/'", "'%'", "'<<'", "'>>'", "'&'", "'|'", "'<'", "'<='", 
			"'>'", "'>='", "'=='", "'!='", "'<>'", "'->'", "'->>'", "'ABORT'", "'ACTION'", 
			"'ADD'", "'AFTER'", "'ALL'", "'ALTER'", "'ALWAYS'", "'ANALYZE'", "'AND'", 
			"'AS'", "'ASC'", "'ATTACH'", "'AUTOINCREMENT'", "'BEFORE'", "'BEGIN'", 
			"'BETWEEN'", "'BY'", "'CASCADE'", "'CASE'", "'CAST'", "'CHECK'", "'COLLATE'", 
			"'COLUMN'", "'COMMIT'", "'CONFLICT'", "'CONSTRAINT'", "'CREATE'", "'CROSS'", 
			"'CURRENT'", "'CURRENT_DATE'", "'CURRENT_TIME'", "'CURRENT_TIMESTAMP'", 
			"'DATABASE'", "'DEFAULT'", "'DEFERRABLE'", "'DEFERRED'", "'DELETE'", 
			"'DESC'", "'DETACH'", "'DISTINCT'", "'DO'", "'DROP'", "'EACH'", "'ELSE'", 
			"'END'", "'ESCAPE'", "'EXCEPT'", "'EXCLUDE'", "'EXCLUSIVE'", "'EXISTS'", 
			"'EXPLAIN'", "'FAIL'", "'FALSE'", "'FILTER'", "'FIRST'", "'FOLLOWING'", 
			"'FOR'", "'FOREIGN'", "'FROM'", "'FULL'", "'GENERATED'", "'GLOB'", "'GROUP'", 
			"'GROUPS'", "'HAVING'", "'IF'", "'IGNORE'", "'IMMEDIATE'", "'IN'", "'INDEX'", 
			"'INDEXED'", "'INITIALLY'", "'INNER'", "'INSERT'", "'INSTEAD'", "'INTERSECT'", 
			"'INTO'", "'IS'", "'ISNULL'", "'JOIN'", "'KEY'", "'LAST'", "'LEFT'", 
			"'LIKE'", "'LIMIT'", "'MATCH'", "'MATERIALIZED'", "'NATURAL'", "'NO'", 
			"'NOT'", "'NOTHING'", "'NOTNULL'", "'NULL'", "'NULLS'", "'OF'", "'OFFSET'", 
			"'ON'", "'OR'", "'ORDER'", "'OTHERS'", "'OUTER'", "'OVER'", "'PARTITION'", 
			"'PLAN'", "'PRAGMA'", "'PRECEDING'", "'PRIMARY'", "'QUERY'", "'RAISE'", 
			"'RANGE'", "'RECURSIVE'", "'REFERENCES'", "'REGEXP'", "'REINDEX'", "'RELEASE'", 
			"'RENAME'", "'REPLACE'", "'RESTRICT'", "'RETURNING'", "'RIGHT'", "'ROLLBACK'", 
			"'ROW'", "'ROWID'", "'ROWS'", "'SAVEPOINT'", "'SELECT'", "'SET'", "'STORED'", 
			"'STRICT'", "'TABLE'", "'TEMP'", "'TEMPORARY'", "'THEN'", "'TIES'", "'TO'", 
			"'TRANSACTION'", "'TRIGGER'", "'TRUE'", "'UNBOUNDED'", "'UNION'", "'UNIQUE'", 
			"'UPDATE'", "'USING'", "'VACUUM'", "'VALUES'", "'VIEW'", "'VIRTUAL'", 
			"'WHEN'", "'WHERE'", "'WINDOW'", "'WITH'", "'WITHIN'", "'WITHOUT'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "SCOL", "DOT", "OPEN_PAR", "CLOSE_PAR", "COMMA", "ASSIGN", "STAR", 
			"PLUS", "MINUS", "TILDE", "PIPE2", "DIV", "MOD", "LT2", "GT2", "AMP", 
			"PIPE", "LT", "LT_EQ", "GT", "GT_EQ", "EQ", "NOT_EQ1", "NOT_EQ2", "JPTR", 
			"JPTR2", "ABORT_", "ACTION_", "ADD_", "AFTER_", "ALL_", "ALTER_", "ALWAYS_", 
			"ANALYZE_", "AND_", "AS_", "ASC_", "ATTACH_", "AUTOINCREMENT_", "BEFORE_", 
			"BEGIN_", "BETWEEN_", "BY_", "CASCADE_", "CASE_", "CAST_", "CHECK_", 
			"COLLATE_", "COLUMN_", "COMMIT_", "CONFLICT_", "CONSTRAINT_", "CREATE_", 
			"CROSS_", "CURRENT_", "CURRENT_DATE_", "CURRENT_TIME_", "CURRENT_TIMESTAMP_", 
			"DATABASE_", "DEFAULT_", "DEFERRABLE_", "DEFERRED_", "DELETE_", "DESC_", 
			"DETACH_", "DISTINCT_", "DO_", "DROP_", "EACH_", "ELSE_", "END_", "ESCAPE_", 
			"EXCEPT_", "EXCLUDE_", "EXCLUSIVE_", "EXISTS_", "EXPLAIN_", "FAIL_", 
			"FALSE_", "FILTER_", "FIRST_", "FOLLOWING_", "FOR_", "FOREIGN_", "FROM_", 
			"FULL_", "GENERATED_", "GLOB_", "GROUP_", "GROUPS_", "HAVING_", "IF_", 
			"IGNORE_", "IMMEDIATE_", "IN_", "INDEX_", "INDEXED_", "INITIALLY_", "INNER_", 
			"INSERT_", "INSTEAD_", "INTERSECT_", "INTO_", "IS_", "ISNULL_", "JOIN_", 
			"KEY_", "LAST_", "LEFT_", "LIKE_", "LIMIT_", "MATCH_", "MATERIALIZED_", 
			"NATURAL_", "NO_", "NOT_", "NOTHING_", "NOTNULL_", "NULL_", "NULLS_", 
			"OF_", "OFFSET_", "ON_", "OR_", "ORDER_", "OTHERS_", "OUTER_", "OVER_", 
			"PARTITION_", "PLAN_", "PRAGMA_", "PRECEDING_", "PRIMARY_", "QUERY_", 
			"RAISE_", "RANGE_", "RECURSIVE_", "REFERENCES_", "REGEXP_", "REINDEX_", 
			"RELEASE_", "RENAME_", "REPLACE_", "RESTRICT_", "RETURNING_", "RIGHT_", 
			"ROLLBACK_", "ROW_", "ROWID_", "ROWS_", "SAVEPOINT_", "SELECT_", "SET_", 
			"STORED_", "STRICT_", "TABLE_", "TEMP_", "TEMPORARY_", "THEN_", "TIES_", 
			"TO_", "TRANSACTION_", "TRIGGER_", "TRUE_", "UNBOUNDED_", "UNION_", "UNIQUE_", 
			"UPDATE_", "USING_", "VACUUM_", "VALUES_", "VIEW_", "VIRTUAL_", "WHEN_", 
			"WHERE_", "WINDOW_", "WITH_", "WITHIN_", "WITHOUT_", "IDENTIFIER", "NUMERIC_LITERAL", 
			"BIND_PARAMETER", "STRING_LITERAL", "BLOB_LITERAL", "SINGLE_LINE_COMMENT", 
			"MULTILINE_COMMENT", "SPACES", "UNEXPECTED_CHAR"
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
	public String getGrammarFileName() { return "SQLiteParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SQLiteParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParseContext extends ParserRuleContext {
		public Sql_stmt_listContext sql_stmt_list() {
			return getRuleContext(Sql_stmt_listContext.class,0);
		}
		public TerminalNode EOF() { return getToken(SQLiteParser.EOF, 0); }
		public ParseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parse; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterParse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitParse(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitParse(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParseContext parse() throws RecognitionException {
		ParseContext _localctx = new ParseContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_parse);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(228);
			sql_stmt_list();
			setState(229);
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
	public static class Sql_stmt_listContext extends ParserRuleContext {
		public List<Sql_stmtContext> sql_stmt() {
			return getRuleContexts(Sql_stmtContext.class);
		}
		public Sql_stmtContext sql_stmt(int i) {
			return getRuleContext(Sql_stmtContext.class,i);
		}
		public List<TerminalNode> SCOL() { return getTokens(SQLiteParser.SCOL); }
		public TerminalNode SCOL(int i) {
			return getToken(SQLiteParser.SCOL, i);
		}
		public Sql_stmt_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sql_stmt_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterSql_stmt_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitSql_stmt_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitSql_stmt_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Sql_stmt_listContext sql_stmt_list() throws RecognitionException {
		Sql_stmt_listContext _localctx = new Sql_stmt_listContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_sql_stmt_list);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(232);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(231);
				sql_stmt();
				}
				break;
			}
			setState(240);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(234);
					match(SCOL);
					setState(236);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
					case 1:
						{
						setState(235);
						sql_stmt();
						}
						break;
					}
					}
					} 
				}
				setState(242);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
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
	public static class Sql_stmtContext extends ParserRuleContext {
		public Alter_table_stmtContext alter_table_stmt() {
			return getRuleContext(Alter_table_stmtContext.class,0);
		}
		public Analyze_stmtContext analyze_stmt() {
			return getRuleContext(Analyze_stmtContext.class,0);
		}
		public Attach_stmtContext attach_stmt() {
			return getRuleContext(Attach_stmtContext.class,0);
		}
		public Begin_stmtContext begin_stmt() {
			return getRuleContext(Begin_stmtContext.class,0);
		}
		public Commit_stmtContext commit_stmt() {
			return getRuleContext(Commit_stmtContext.class,0);
		}
		public Create_index_stmtContext create_index_stmt() {
			return getRuleContext(Create_index_stmtContext.class,0);
		}
		public Create_table_stmtContext create_table_stmt() {
			return getRuleContext(Create_table_stmtContext.class,0);
		}
		public Create_trigger_stmtContext create_trigger_stmt() {
			return getRuleContext(Create_trigger_stmtContext.class,0);
		}
		public Create_view_stmtContext create_view_stmt() {
			return getRuleContext(Create_view_stmtContext.class,0);
		}
		public Create_virtual_table_stmtContext create_virtual_table_stmt() {
			return getRuleContext(Create_virtual_table_stmtContext.class,0);
		}
		public Delete_stmtContext delete_stmt() {
			return getRuleContext(Delete_stmtContext.class,0);
		}
		public Detach_stmtContext detach_stmt() {
			return getRuleContext(Detach_stmtContext.class,0);
		}
		public Drop_stmtContext drop_stmt() {
			return getRuleContext(Drop_stmtContext.class,0);
		}
		public Insert_stmtContext insert_stmt() {
			return getRuleContext(Insert_stmtContext.class,0);
		}
		public Pragma_stmtContext pragma_stmt() {
			return getRuleContext(Pragma_stmtContext.class,0);
		}
		public Reindex_stmtContext reindex_stmt() {
			return getRuleContext(Reindex_stmtContext.class,0);
		}
		public Release_stmtContext release_stmt() {
			return getRuleContext(Release_stmtContext.class,0);
		}
		public Rollback_stmtContext rollback_stmt() {
			return getRuleContext(Rollback_stmtContext.class,0);
		}
		public Savepoint_stmtContext savepoint_stmt() {
			return getRuleContext(Savepoint_stmtContext.class,0);
		}
		public Select_stmtContext select_stmt() {
			return getRuleContext(Select_stmtContext.class,0);
		}
		public Update_stmtContext update_stmt() {
			return getRuleContext(Update_stmtContext.class,0);
		}
		public Vacuum_stmtContext vacuum_stmt() {
			return getRuleContext(Vacuum_stmtContext.class,0);
		}
		public TerminalNode EXPLAIN_() { return getToken(SQLiteParser.EXPLAIN_, 0); }
		public TerminalNode QUERY_() { return getToken(SQLiteParser.QUERY_, 0); }
		public TerminalNode PLAN_() { return getToken(SQLiteParser.PLAN_, 0); }
		public Sql_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sql_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterSql_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitSql_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitSql_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Sql_stmtContext sql_stmt() throws RecognitionException {
		Sql_stmtContext _localctx = new Sql_stmtContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_sql_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(248);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(243);
				match(EXPLAIN_);
				setState(246);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
				case 1:
					{
					setState(244);
					match(QUERY_);
					setState(245);
					match(PLAN_);
					}
					break;
				}
				}
				break;
			}
			setState(272);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				{
				setState(250);
				alter_table_stmt();
				}
				break;
			case 2:
				{
				setState(251);
				analyze_stmt();
				}
				break;
			case 3:
				{
				setState(252);
				attach_stmt();
				}
				break;
			case 4:
				{
				setState(253);
				begin_stmt();
				}
				break;
			case 5:
				{
				setState(254);
				commit_stmt();
				}
				break;
			case 6:
				{
				setState(255);
				create_index_stmt();
				}
				break;
			case 7:
				{
				setState(256);
				create_table_stmt();
				}
				break;
			case 8:
				{
				setState(257);
				create_trigger_stmt();
				}
				break;
			case 9:
				{
				setState(258);
				create_view_stmt();
				}
				break;
			case 10:
				{
				setState(259);
				create_virtual_table_stmt();
				}
				break;
			case 11:
				{
				setState(260);
				delete_stmt();
				}
				break;
			case 12:
				{
				setState(261);
				detach_stmt();
				}
				break;
			case 13:
				{
				setState(262);
				drop_stmt();
				}
				break;
			case 14:
				{
				setState(263);
				insert_stmt();
				}
				break;
			case 15:
				{
				setState(264);
				pragma_stmt();
				}
				break;
			case 16:
				{
				setState(265);
				reindex_stmt();
				}
				break;
			case 17:
				{
				setState(266);
				release_stmt();
				}
				break;
			case 18:
				{
				setState(267);
				rollback_stmt();
				}
				break;
			case 19:
				{
				setState(268);
				savepoint_stmt();
				}
				break;
			case 20:
				{
				setState(269);
				select_stmt();
				}
				break;
			case 21:
				{
				setState(270);
				update_stmt();
				}
				break;
			case 22:
				{
				setState(271);
				vacuum_stmt();
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
	public static class Alter_table_stmtContext extends ParserRuleContext {
		public Table_nameContext new_table_name;
		public Column_nameContext old_column_name;
		public Column_nameContext new_column_name;
		public TerminalNode ALTER_() { return getToken(SQLiteParser.ALTER_, 0); }
		public TerminalNode TABLE_() { return getToken(SQLiteParser.TABLE_, 0); }
		public List<Table_nameContext> table_name() {
			return getRuleContexts(Table_nameContext.class);
		}
		public Table_nameContext table_name(int i) {
			return getRuleContext(Table_nameContext.class,i);
		}
		public TerminalNode RENAME_() { return getToken(SQLiteParser.RENAME_, 0); }
		public TerminalNode ADD_() { return getToken(SQLiteParser.ADD_, 0); }
		public Column_defContext column_def() {
			return getRuleContext(Column_defContext.class,0);
		}
		public TerminalNode DROP_() { return getToken(SQLiteParser.DROP_, 0); }
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(SQLiteParser.DOT, 0); }
		public TerminalNode TO_() { return getToken(SQLiteParser.TO_, 0); }
		public TerminalNode COLUMN_() { return getToken(SQLiteParser.COLUMN_, 0); }
		public Alter_table_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alter_table_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterAlter_table_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitAlter_table_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitAlter_table_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Alter_table_stmtContext alter_table_stmt() throws RecognitionException {
		Alter_table_stmtContext _localctx = new Alter_table_stmtContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_alter_table_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(274);
			match(ALTER_);
			setState(275);
			match(TABLE_);
			setState(279);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				{
				setState(276);
				schema_name();
				setState(277);
				match(DOT);
				}
				break;
			}
			setState(281);
			table_name();
			setState(304);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				{
				setState(282);
				match(RENAME_);
				setState(292);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
				case 1:
					{
					setState(283);
					match(TO_);
					setState(284);
					((Alter_table_stmtContext)_localctx).new_table_name = table_name();
					}
					break;
				case 2:
					{
					setState(286);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
					case 1:
						{
						setState(285);
						match(COLUMN_);
						}
						break;
					}
					setState(288);
					((Alter_table_stmtContext)_localctx).old_column_name = column_name();
					setState(289);
					match(TO_);
					setState(290);
					((Alter_table_stmtContext)_localctx).new_column_name = column_name();
					}
					break;
				}
				}
				break;
			case 2:
				{
				setState(294);
				match(ADD_);
				setState(296);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
				case 1:
					{
					setState(295);
					match(COLUMN_);
					}
					break;
				}
				setState(298);
				column_def();
				}
				break;
			case 3:
				{
				setState(299);
				match(DROP_);
				setState(301);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
				case 1:
					{
					setState(300);
					match(COLUMN_);
					}
					break;
				}
				setState(303);
				column_name();
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
	public static class Analyze_stmtContext extends ParserRuleContext {
		public TerminalNode ANALYZE_() { return getToken(SQLiteParser.ANALYZE_, 0); }
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public Table_or_index_nameContext table_or_index_name() {
			return getRuleContext(Table_or_index_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(SQLiteParser.DOT, 0); }
		public Analyze_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_analyze_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterAnalyze_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitAnalyze_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitAnalyze_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Analyze_stmtContext analyze_stmt() throws RecognitionException {
		Analyze_stmtContext _localctx = new Analyze_stmtContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_analyze_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(306);
			match(ANALYZE_);
			setState(314);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				{
				setState(307);
				schema_name();
				}
				break;
			case 2:
				{
				setState(311);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
				case 1:
					{
					setState(308);
					schema_name();
					setState(309);
					match(DOT);
					}
					break;
				}
				setState(313);
				table_or_index_name();
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
	public static class Attach_stmtContext extends ParserRuleContext {
		public TerminalNode ATTACH_() { return getToken(SQLiteParser.ATTACH_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode AS_() { return getToken(SQLiteParser.AS_, 0); }
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DATABASE_() { return getToken(SQLiteParser.DATABASE_, 0); }
		public Attach_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attach_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterAttach_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitAttach_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitAttach_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Attach_stmtContext attach_stmt() throws RecognitionException {
		Attach_stmtContext _localctx = new Attach_stmtContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_attach_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(316);
			match(ATTACH_);
			setState(318);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				{
				setState(317);
				match(DATABASE_);
				}
				break;
			}
			setState(320);
			expr();
			setState(321);
			match(AS_);
			setState(322);
			schema_name();
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
	public static class Begin_stmtContext extends ParserRuleContext {
		public TerminalNode BEGIN_() { return getToken(SQLiteParser.BEGIN_, 0); }
		public TerminalNode TRANSACTION_() { return getToken(SQLiteParser.TRANSACTION_, 0); }
		public TerminalNode DEFERRED_() { return getToken(SQLiteParser.DEFERRED_, 0); }
		public TerminalNode IMMEDIATE_() { return getToken(SQLiteParser.IMMEDIATE_, 0); }
		public TerminalNode EXCLUSIVE_() { return getToken(SQLiteParser.EXCLUSIVE_, 0); }
		public Begin_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_begin_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterBegin_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitBegin_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitBegin_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Begin_stmtContext begin_stmt() throws RecognitionException {
		Begin_stmtContext _localctx = new Begin_stmtContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_begin_stmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(324);
			match(BEGIN_);
			setState(326);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				{
				setState(325);
				_la = _input.LA(1);
				if ( !(((((_la - 62)) & ~0x3f) == 0 && ((1L << (_la - 62)) & 4294975489L) != 0)) ) {
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
			setState(329);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				{
				setState(328);
				match(TRANSACTION_);
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
	public static class Commit_stmtContext extends ParserRuleContext {
		public TerminalNode COMMIT_() { return getToken(SQLiteParser.COMMIT_, 0); }
		public TerminalNode END_() { return getToken(SQLiteParser.END_, 0); }
		public TerminalNode TRANSACTION_() { return getToken(SQLiteParser.TRANSACTION_, 0); }
		public Commit_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commit_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterCommit_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitCommit_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitCommit_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Commit_stmtContext commit_stmt() throws RecognitionException {
		Commit_stmtContext _localctx = new Commit_stmtContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_commit_stmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(331);
			_la = _input.LA(1);
			if ( !(_la==COMMIT_ || _la==END_) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(333);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				{
				setState(332);
				match(TRANSACTION_);
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
	public static class Rollback_stmtContext extends ParserRuleContext {
		public TerminalNode ROLLBACK_() { return getToken(SQLiteParser.ROLLBACK_, 0); }
		public TerminalNode TRANSACTION_() { return getToken(SQLiteParser.TRANSACTION_, 0); }
		public TerminalNode TO_() { return getToken(SQLiteParser.TO_, 0); }
		public Savepoint_nameContext savepoint_name() {
			return getRuleContext(Savepoint_nameContext.class,0);
		}
		public TerminalNode SAVEPOINT_() { return getToken(SQLiteParser.SAVEPOINT_, 0); }
		public Rollback_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rollback_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterRollback_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitRollback_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitRollback_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Rollback_stmtContext rollback_stmt() throws RecognitionException {
		Rollback_stmtContext _localctx = new Rollback_stmtContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_rollback_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(335);
			match(ROLLBACK_);
			setState(337);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				{
				setState(336);
				match(TRANSACTION_);
				}
				break;
			}
			setState(344);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				{
				setState(339);
				match(TO_);
				setState(341);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
				case 1:
					{
					setState(340);
					match(SAVEPOINT_);
					}
					break;
				}
				setState(343);
				savepoint_name();
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
	public static class Savepoint_stmtContext extends ParserRuleContext {
		public TerminalNode SAVEPOINT_() { return getToken(SQLiteParser.SAVEPOINT_, 0); }
		public Savepoint_nameContext savepoint_name() {
			return getRuleContext(Savepoint_nameContext.class,0);
		}
		public Savepoint_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_savepoint_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterSavepoint_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitSavepoint_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitSavepoint_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Savepoint_stmtContext savepoint_stmt() throws RecognitionException {
		Savepoint_stmtContext _localctx = new Savepoint_stmtContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_savepoint_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(346);
			match(SAVEPOINT_);
			setState(347);
			savepoint_name();
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
	public static class Release_stmtContext extends ParserRuleContext {
		public TerminalNode RELEASE_() { return getToken(SQLiteParser.RELEASE_, 0); }
		public Savepoint_nameContext savepoint_name() {
			return getRuleContext(Savepoint_nameContext.class,0);
		}
		public TerminalNode SAVEPOINT_() { return getToken(SQLiteParser.SAVEPOINT_, 0); }
		public Release_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_release_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterRelease_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitRelease_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitRelease_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Release_stmtContext release_stmt() throws RecognitionException {
		Release_stmtContext _localctx = new Release_stmtContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_release_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(349);
			match(RELEASE_);
			setState(351);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				{
				setState(350);
				match(SAVEPOINT_);
				}
				break;
			}
			setState(353);
			savepoint_name();
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
	public static class Create_index_stmtContext extends ParserRuleContext {
		public TerminalNode CREATE_() { return getToken(SQLiteParser.CREATE_, 0); }
		public TerminalNode INDEX_() { return getToken(SQLiteParser.INDEX_, 0); }
		public Index_nameContext index_name() {
			return getRuleContext(Index_nameContext.class,0);
		}
		public TerminalNode ON_() { return getToken(SQLiteParser.ON_, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public List<Indexed_columnContext> indexed_column() {
			return getRuleContexts(Indexed_columnContext.class);
		}
		public Indexed_columnContext indexed_column(int i) {
			return getRuleContext(Indexed_columnContext.class,i);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public TerminalNode UNIQUE_() { return getToken(SQLiteParser.UNIQUE_, 0); }
		public TerminalNode IF_() { return getToken(SQLiteParser.IF_, 0); }
		public TerminalNode NOT_() { return getToken(SQLiteParser.NOT_, 0); }
		public TerminalNode EXISTS_() { return getToken(SQLiteParser.EXISTS_, 0); }
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(SQLiteParser.DOT, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public TerminalNode WHERE_() { return getToken(SQLiteParser.WHERE_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public Create_index_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_index_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterCreate_index_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitCreate_index_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitCreate_index_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_index_stmtContext create_index_stmt() throws RecognitionException {
		Create_index_stmtContext _localctx = new Create_index_stmtContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_create_index_stmt);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(355);
			match(CREATE_);
			setState(357);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				{
				setState(356);
				match(UNIQUE_);
				}
				break;
			}
			setState(359);
			match(INDEX_);
			setState(363);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				{
				setState(360);
				match(IF_);
				setState(361);
				match(NOT_);
				setState(362);
				match(EXISTS_);
				}
				break;
			}
			setState(368);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				{
				setState(365);
				schema_name();
				setState(366);
				match(DOT);
				}
				break;
			}
			setState(370);
			index_name();
			setState(371);
			match(ON_);
			setState(372);
			table_name();
			setState(373);
			match(OPEN_PAR);
			setState(374);
			indexed_column();
			setState(379);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(375);
					match(COMMA);
					setState(376);
					indexed_column();
					}
					} 
				}
				setState(381);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			}
			setState(382);
			match(CLOSE_PAR);
			setState(385);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				{
				setState(383);
				match(WHERE_);
				setState(384);
				expr();
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
	public static class Indexed_columnContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode COLLATE_() { return getToken(SQLiteParser.COLLATE_, 0); }
		public Collation_nameContext collation_name() {
			return getRuleContext(Collation_nameContext.class,0);
		}
		public Asc_descContext asc_desc() {
			return getRuleContext(Asc_descContext.class,0);
		}
		public Indexed_columnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexed_column; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterIndexed_column(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitIndexed_column(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitIndexed_column(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Indexed_columnContext indexed_column() throws RecognitionException {
		Indexed_columnContext _localctx = new Indexed_columnContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_indexed_column);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(387);
			expr();
			setState(390);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				{
				setState(388);
				match(COLLATE_);
				setState(389);
				collation_name();
				}
				break;
			}
			setState(393);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				{
				setState(392);
				asc_desc();
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
	public static class Create_table_stmtContext extends ParserRuleContext {
		public TerminalNode CREATE_() { return getToken(SQLiteParser.CREATE_, 0); }
		public TerminalNode TABLE_() { return getToken(SQLiteParser.TABLE_, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public List<Column_defContext> column_def() {
			return getRuleContexts(Column_defContext.class);
		}
		public Column_defContext column_def(int i) {
			return getRuleContext(Column_defContext.class,i);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public TerminalNode AS_() { return getToken(SQLiteParser.AS_, 0); }
		public Select_stmtContext select_stmt() {
			return getRuleContext(Select_stmtContext.class,0);
		}
		public TerminalNode IF_() { return getToken(SQLiteParser.IF_, 0); }
		public TerminalNode NOT_() { return getToken(SQLiteParser.NOT_, 0); }
		public TerminalNode EXISTS_() { return getToken(SQLiteParser.EXISTS_, 0); }
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(SQLiteParser.DOT, 0); }
		public TerminalNode TEMP_() { return getToken(SQLiteParser.TEMP_, 0); }
		public TerminalNode TEMPORARY_() { return getToken(SQLiteParser.TEMPORARY_, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public List<Table_constraintContext> table_constraint() {
			return getRuleContexts(Table_constraintContext.class);
		}
		public Table_constraintContext table_constraint(int i) {
			return getRuleContext(Table_constraintContext.class,i);
		}
		public Table_optionsContext table_options() {
			return getRuleContext(Table_optionsContext.class,0);
		}
		public Create_table_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_table_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterCreate_table_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitCreate_table_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitCreate_table_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_table_stmtContext create_table_stmt() throws RecognitionException {
		Create_table_stmtContext _localctx = new Create_table_stmtContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_create_table_stmt);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(395);
			match(CREATE_);
			setState(397);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
			case 1:
				{
				setState(396);
				_la = _input.LA(1);
				if ( !(_la==TEMP_ || _la==TEMPORARY_) ) {
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
			setState(399);
			match(TABLE_);
			setState(403);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				{
				setState(400);
				match(IF_);
				setState(401);
				match(NOT_);
				setState(402);
				match(EXISTS_);
				}
				break;
			}
			setState(408);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				{
				setState(405);
				schema_name();
				setState(406);
				match(DOT);
				}
				break;
			}
			setState(410);
			table_name();
			setState(433);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				{
				setState(411);
				match(OPEN_PAR);
				setState(412);
				column_def();
				setState(417);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(413);
						match(COMMA);
						setState(414);
						column_def();
						}
						} 
					}
					setState(419);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
				}
				setState(424);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(420);
						match(COMMA);
						setState(421);
						table_constraint();
						}
						} 
					}
					setState(426);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
				}
				setState(427);
				match(CLOSE_PAR);
				setState(429);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
				case 1:
					{
					setState(428);
					table_options();
					}
					break;
				}
				}
				break;
			case 2:
				{
				setState(431);
				match(AS_);
				setState(432);
				select_stmt();
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
	public static class Table_optionsContext extends ParserRuleContext {
		public List<TerminalNode> WITHOUT_() { return getTokens(SQLiteParser.WITHOUT_); }
		public TerminalNode WITHOUT_(int i) {
			return getToken(SQLiteParser.WITHOUT_, i);
		}
		public List<TerminalNode> ROWID_() { return getTokens(SQLiteParser.ROWID_); }
		public TerminalNode ROWID_(int i) {
			return getToken(SQLiteParser.ROWID_, i);
		}
		public List<TerminalNode> STRICT_() { return getTokens(SQLiteParser.STRICT_); }
		public TerminalNode STRICT_(int i) {
			return getToken(SQLiteParser.STRICT_, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Table_optionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_options; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterTable_options(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitTable_options(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitTable_options(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Table_optionsContext table_options() throws RecognitionException {
		Table_optionsContext _localctx = new Table_optionsContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_table_options);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(438);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				{
				setState(435);
				match(WITHOUT_);
				setState(436);
				match(ROWID_);
				}
				break;
			case 2:
				{
				setState(437);
				match(STRICT_);
				}
				break;
			}
			setState(448);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,38,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(440);
					match(COMMA);
					setState(444);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
					case 1:
						{
						setState(441);
						match(WITHOUT_);
						setState(442);
						match(ROWID_);
						}
						break;
					case 2:
						{
						setState(443);
						match(STRICT_);
						}
						break;
					}
					}
					} 
				}
				setState(450);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,38,_ctx);
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
	public static class Column_defContext extends ParserRuleContext {
		public Column_nameContext column_name() {
			return getRuleContext(Column_nameContext.class,0);
		}
		public Type_nameContext type_name() {
			return getRuleContext(Type_nameContext.class,0);
		}
		public List<Column_constraintContext> column_constraint() {
			return getRuleContexts(Column_constraintContext.class);
		}
		public Column_constraintContext column_constraint(int i) {
			return getRuleContext(Column_constraintContext.class,i);
		}
		public Column_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterColumn_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitColumn_def(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitColumn_def(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_defContext column_def() throws RecognitionException {
		Column_defContext _localctx = new Column_defContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_column_def);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(451);
			column_name();
			setState(453);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				{
				setState(452);
				type_name();
				}
				break;
			}
			setState(458);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(455);
					column_constraint();
					}
					} 
				}
				setState(460);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
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
	public static class Type_nameContext extends ParserRuleContext {
		public List<NameContext> name() {
			return getRuleContexts(NameContext.class);
		}
		public NameContext name(int i) {
			return getRuleContext(NameContext.class,i);
		}
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public List<Signed_numberContext> signed_number() {
			return getRuleContexts(Signed_numberContext.class);
		}
		public Signed_numberContext signed_number(int i) {
			return getRuleContext(Signed_numberContext.class,i);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public TerminalNode COMMA() { return getToken(SQLiteParser.COMMA, 0); }
		public Type_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterType_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitType_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitType_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Type_nameContext type_name() throws RecognitionException {
		Type_nameContext _localctx = new Type_nameContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_type_name);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(462); 
			_errHandler.sync(this);
			_alt = 1+1;
			do {
				switch (_alt) {
				case 1+1:
					{
					{
					setState(461);
					name();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(464); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,41,_ctx);
			} while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(476);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				{
				setState(466);
				match(OPEN_PAR);
				setState(467);
				signed_number();
				setState(468);
				match(CLOSE_PAR);
				}
				break;
			case 2:
				{
				setState(470);
				match(OPEN_PAR);
				setState(471);
				signed_number();
				setState(472);
				match(COMMA);
				setState(473);
				signed_number();
				setState(474);
				match(CLOSE_PAR);
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
	public static class Column_constraintContext extends ParserRuleContext {
		public TerminalNode PRIMARY_() { return getToken(SQLiteParser.PRIMARY_, 0); }
		public TerminalNode KEY_() { return getToken(SQLiteParser.KEY_, 0); }
		public TerminalNode CHECK_() { return getToken(SQLiteParser.CHECK_, 0); }
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public TerminalNode DEFAULT_() { return getToken(SQLiteParser.DEFAULT_, 0); }
		public TerminalNode COLLATE_() { return getToken(SQLiteParser.COLLATE_, 0); }
		public Collation_nameContext collation_name() {
			return getRuleContext(Collation_nameContext.class,0);
		}
		public Foreign_key_clauseContext foreign_key_clause() {
			return getRuleContext(Foreign_key_clauseContext.class,0);
		}
		public TerminalNode AS_() { return getToken(SQLiteParser.AS_, 0); }
		public TerminalNode CONSTRAINT_() { return getToken(SQLiteParser.CONSTRAINT_, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TerminalNode NULL_() { return getToken(SQLiteParser.NULL_, 0); }
		public TerminalNode UNIQUE_() { return getToken(SQLiteParser.UNIQUE_, 0); }
		public Signed_numberContext signed_number() {
			return getRuleContext(Signed_numberContext.class,0);
		}
		public Literal_valueContext literal_value() {
			return getRuleContext(Literal_valueContext.class,0);
		}
		public Asc_descContext asc_desc() {
			return getRuleContext(Asc_descContext.class,0);
		}
		public Conflict_clauseContext conflict_clause() {
			return getRuleContext(Conflict_clauseContext.class,0);
		}
		public TerminalNode AUTOINCREMENT_() { return getToken(SQLiteParser.AUTOINCREMENT_, 0); }
		public TerminalNode GENERATED_() { return getToken(SQLiteParser.GENERATED_, 0); }
		public TerminalNode ALWAYS_() { return getToken(SQLiteParser.ALWAYS_, 0); }
		public TerminalNode STORED_() { return getToken(SQLiteParser.STORED_, 0); }
		public TerminalNode VIRTUAL_() { return getToken(SQLiteParser.VIRTUAL_, 0); }
		public TerminalNode NOT_() { return getToken(SQLiteParser.NOT_, 0); }
		public Column_constraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_constraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterColumn_constraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitColumn_constraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitColumn_constraint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_constraintContext column_constraint() throws RecognitionException {
		Column_constraintContext _localctx = new Column_constraintContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_column_constraint);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(480);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
			case 1:
				{
				setState(478);
				match(CONSTRAINT_);
				setState(479);
				name();
				}
				break;
			}
			setState(531);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,53,_ctx) ) {
			case 1:
				{
				setState(482);
				match(PRIMARY_);
				setState(483);
				match(KEY_);
				setState(485);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
				case 1:
					{
					setState(484);
					asc_desc();
					}
					break;
				}
				setState(488);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
				case 1:
					{
					setState(487);
					conflict_clause();
					}
					break;
				}
				setState(491);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
				case 1:
					{
					setState(490);
					match(AUTOINCREMENT_);
					}
					break;
				}
				}
				break;
			case 2:
				{
				setState(498);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
				case 1:
					{
					setState(494);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,47,_ctx) ) {
					case 1:
						{
						setState(493);
						match(NOT_);
						}
						break;
					}
					setState(496);
					match(NULL_);
					}
					break;
				case 2:
					{
					setState(497);
					match(UNIQUE_);
					}
					break;
				}
				setState(501);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,49,_ctx) ) {
				case 1:
					{
					setState(500);
					conflict_clause();
					}
					break;
				}
				}
				break;
			case 3:
				{
				setState(503);
				match(CHECK_);
				setState(504);
				match(OPEN_PAR);
				setState(505);
				expr();
				setState(506);
				match(CLOSE_PAR);
				}
				break;
			case 4:
				{
				setState(508);
				match(DEFAULT_);
				setState(515);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
				case 1:
					{
					setState(509);
					signed_number();
					}
					break;
				case 2:
					{
					setState(510);
					literal_value();
					}
					break;
				case 3:
					{
					setState(511);
					match(OPEN_PAR);
					setState(512);
					expr();
					setState(513);
					match(CLOSE_PAR);
					}
					break;
				}
				}
				break;
			case 5:
				{
				setState(517);
				match(COLLATE_);
				setState(518);
				collation_name();
				}
				break;
			case 6:
				{
				setState(519);
				foreign_key_clause();
				}
				break;
			case 7:
				{
				setState(522);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,51,_ctx) ) {
				case 1:
					{
					setState(520);
					match(GENERATED_);
					setState(521);
					match(ALWAYS_);
					}
					break;
				}
				setState(524);
				match(AS_);
				setState(525);
				match(OPEN_PAR);
				setState(526);
				expr();
				setState(527);
				match(CLOSE_PAR);
				setState(529);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,52,_ctx) ) {
				case 1:
					{
					setState(528);
					_la = _input.LA(1);
					if ( !(_la==STORED_ || _la==VIRTUAL_) ) {
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
	public static class Signed_numberContext extends ParserRuleContext {
		public TerminalNode NUMERIC_LITERAL() { return getToken(SQLiteParser.NUMERIC_LITERAL, 0); }
		public TerminalNode PLUS() { return getToken(SQLiteParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(SQLiteParser.MINUS, 0); }
		public Signed_numberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_signed_number; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterSigned_number(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitSigned_number(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitSigned_number(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Signed_numberContext signed_number() throws RecognitionException {
		Signed_numberContext _localctx = new Signed_numberContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_signed_number);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(534);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,54,_ctx) ) {
			case 1:
				{
				setState(533);
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
				break;
			}
			setState(536);
			match(NUMERIC_LITERAL);
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
	public static class Table_constraintContext extends ParserRuleContext {
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public List<Indexed_columnContext> indexed_column() {
			return getRuleContexts(Indexed_columnContext.class);
		}
		public Indexed_columnContext indexed_column(int i) {
			return getRuleContext(Indexed_columnContext.class,i);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public TerminalNode CHECK_() { return getToken(SQLiteParser.CHECK_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode FOREIGN_() { return getToken(SQLiteParser.FOREIGN_, 0); }
		public TerminalNode KEY_() { return getToken(SQLiteParser.KEY_, 0); }
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public Foreign_key_clauseContext foreign_key_clause() {
			return getRuleContext(Foreign_key_clauseContext.class,0);
		}
		public TerminalNode CONSTRAINT_() { return getToken(SQLiteParser.CONSTRAINT_, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TerminalNode PRIMARY_() { return getToken(SQLiteParser.PRIMARY_, 0); }
		public TerminalNode UNIQUE_() { return getToken(SQLiteParser.UNIQUE_, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Conflict_clauseContext conflict_clause() {
			return getRuleContext(Conflict_clauseContext.class,0);
		}
		public Table_constraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_constraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterTable_constraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitTable_constraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitTable_constraint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Table_constraintContext table_constraint() throws RecognitionException {
		Table_constraintContext _localctx = new Table_constraintContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_table_constraint);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(540);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,55,_ctx) ) {
			case 1:
				{
				setState(538);
				match(CONSTRAINT_);
				setState(539);
				name();
				}
				break;
			}
			setState(579);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,60,_ctx) ) {
			case 1:
				{
				setState(545);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
				case 1:
					{
					setState(542);
					match(PRIMARY_);
					setState(543);
					match(KEY_);
					}
					break;
				case 2:
					{
					setState(544);
					match(UNIQUE_);
					}
					break;
				}
				setState(547);
				match(OPEN_PAR);
				setState(548);
				indexed_column();
				setState(553);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,57,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(549);
						match(COMMA);
						setState(550);
						indexed_column();
						}
						} 
					}
					setState(555);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,57,_ctx);
				}
				setState(556);
				match(CLOSE_PAR);
				setState(558);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,58,_ctx) ) {
				case 1:
					{
					setState(557);
					conflict_clause();
					}
					break;
				}
				}
				break;
			case 2:
				{
				setState(560);
				match(CHECK_);
				setState(561);
				match(OPEN_PAR);
				setState(562);
				expr();
				setState(563);
				match(CLOSE_PAR);
				}
				break;
			case 3:
				{
				setState(565);
				match(FOREIGN_);
				setState(566);
				match(KEY_);
				setState(567);
				match(OPEN_PAR);
				setState(568);
				column_name();
				setState(573);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,59,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(569);
						match(COMMA);
						setState(570);
						column_name();
						}
						} 
					}
					setState(575);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,59,_ctx);
				}
				setState(576);
				match(CLOSE_PAR);
				setState(577);
				foreign_key_clause();
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
	public static class Foreign_key_clauseContext extends ParserRuleContext {
		public TerminalNode REFERENCES_() { return getToken(SQLiteParser.REFERENCES_, 0); }
		public Foreign_tableContext foreign_table() {
			return getRuleContext(Foreign_tableContext.class,0);
		}
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public List<TerminalNode> ON_() { return getTokens(SQLiteParser.ON_); }
		public TerminalNode ON_(int i) {
			return getToken(SQLiteParser.ON_, i);
		}
		public List<TerminalNode> MATCH_() { return getTokens(SQLiteParser.MATCH_); }
		public TerminalNode MATCH_(int i) {
			return getToken(SQLiteParser.MATCH_, i);
		}
		public List<NameContext> name() {
			return getRuleContexts(NameContext.class);
		}
		public NameContext name(int i) {
			return getRuleContext(NameContext.class,i);
		}
		public TerminalNode DEFERRABLE_() { return getToken(SQLiteParser.DEFERRABLE_, 0); }
		public List<TerminalNode> DELETE_() { return getTokens(SQLiteParser.DELETE_); }
		public TerminalNode DELETE_(int i) {
			return getToken(SQLiteParser.DELETE_, i);
		}
		public List<TerminalNode> UPDATE_() { return getTokens(SQLiteParser.UPDATE_); }
		public TerminalNode UPDATE_(int i) {
			return getToken(SQLiteParser.UPDATE_, i);
		}
		public List<TerminalNode> SET_() { return getTokens(SQLiteParser.SET_); }
		public TerminalNode SET_(int i) {
			return getToken(SQLiteParser.SET_, i);
		}
		public List<TerminalNode> CASCADE_() { return getTokens(SQLiteParser.CASCADE_); }
		public TerminalNode CASCADE_(int i) {
			return getToken(SQLiteParser.CASCADE_, i);
		}
		public List<TerminalNode> RESTRICT_() { return getTokens(SQLiteParser.RESTRICT_); }
		public TerminalNode RESTRICT_(int i) {
			return getToken(SQLiteParser.RESTRICT_, i);
		}
		public List<TerminalNode> NO_() { return getTokens(SQLiteParser.NO_); }
		public TerminalNode NO_(int i) {
			return getToken(SQLiteParser.NO_, i);
		}
		public List<TerminalNode> ACTION_() { return getTokens(SQLiteParser.ACTION_); }
		public TerminalNode ACTION_(int i) {
			return getToken(SQLiteParser.ACTION_, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public List<TerminalNode> NULL_() { return getTokens(SQLiteParser.NULL_); }
		public TerminalNode NULL_(int i) {
			return getToken(SQLiteParser.NULL_, i);
		}
		public List<TerminalNode> DEFAULT_() { return getTokens(SQLiteParser.DEFAULT_); }
		public TerminalNode DEFAULT_(int i) {
			return getToken(SQLiteParser.DEFAULT_, i);
		}
		public TerminalNode NOT_() { return getToken(SQLiteParser.NOT_, 0); }
		public TerminalNode INITIALLY_() { return getToken(SQLiteParser.INITIALLY_, 0); }
		public TerminalNode DEFERRED_() { return getToken(SQLiteParser.DEFERRED_, 0); }
		public TerminalNode IMMEDIATE_() { return getToken(SQLiteParser.IMMEDIATE_, 0); }
		public Foreign_key_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_foreign_key_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterForeign_key_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitForeign_key_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitForeign_key_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Foreign_key_clauseContext foreign_key_clause() throws RecognitionException {
		Foreign_key_clauseContext _localctx = new Foreign_key_clauseContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_foreign_key_clause);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(581);
			match(REFERENCES_);
			setState(582);
			foreign_table();
			setState(594);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,62,_ctx) ) {
			case 1:
				{
				setState(583);
				match(OPEN_PAR);
				setState(584);
				column_name();
				setState(589);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(585);
						match(COMMA);
						setState(586);
						column_name();
						}
						} 
					}
					setState(591);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,61,_ctx);
				}
				setState(592);
				match(CLOSE_PAR);
				}
				break;
			}
			setState(610);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,65,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					setState(608);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,64,_ctx) ) {
					case 1:
						{
						setState(596);
						match(ON_);
						setState(597);
						_la = _input.LA(1);
						if ( !(_la==DELETE_ || _la==UPDATE_) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(604);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,63,_ctx) ) {
						case 1:
							{
							setState(598);
							match(SET_);
							setState(599);
							_la = _input.LA(1);
							if ( !(_la==DEFAULT_ || _la==NULL_) ) {
							_errHandler.recoverInline(this);
							}
							else {
								if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
								_errHandler.reportMatch(this);
								consume();
							}
							}
							break;
						case 2:
							{
							setState(600);
							match(CASCADE_);
							}
							break;
						case 3:
							{
							setState(601);
							match(RESTRICT_);
							}
							break;
						case 4:
							{
							setState(602);
							match(NO_);
							setState(603);
							match(ACTION_);
							}
							break;
						}
						}
						break;
					case 2:
						{
						setState(606);
						match(MATCH_);
						setState(607);
						name();
						}
						break;
					}
					} 
				}
				setState(612);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,65,_ctx);
			}
			setState(621);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,68,_ctx) ) {
			case 1:
				{
				setState(614);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,66,_ctx) ) {
				case 1:
					{
					setState(613);
					match(NOT_);
					}
					break;
				}
				setState(616);
				match(DEFERRABLE_);
				setState(619);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,67,_ctx) ) {
				case 1:
					{
					setState(617);
					match(INITIALLY_);
					setState(618);
					_la = _input.LA(1);
					if ( !(_la==DEFERRED_ || _la==IMMEDIATE_) ) {
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
	public static class Conflict_clauseContext extends ParserRuleContext {
		public TerminalNode ON_() { return getToken(SQLiteParser.ON_, 0); }
		public TerminalNode CONFLICT_() { return getToken(SQLiteParser.CONFLICT_, 0); }
		public TerminalNode ROLLBACK_() { return getToken(SQLiteParser.ROLLBACK_, 0); }
		public TerminalNode ABORT_() { return getToken(SQLiteParser.ABORT_, 0); }
		public TerminalNode FAIL_() { return getToken(SQLiteParser.FAIL_, 0); }
		public TerminalNode IGNORE_() { return getToken(SQLiteParser.IGNORE_, 0); }
		public TerminalNode REPLACE_() { return getToken(SQLiteParser.REPLACE_, 0); }
		public Conflict_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conflict_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterConflict_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitConflict_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitConflict_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Conflict_clauseContext conflict_clause() throws RecognitionException {
		Conflict_clauseContext _localctx = new Conflict_clauseContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_conflict_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(623);
			match(ON_);
			setState(624);
			match(CONFLICT_);
			setState(625);
			_la = _input.LA(1);
			if ( !(_la==ABORT_ || _la==FAIL_ || ((((_la - 93)) & ~0x3f) == 0 && ((1L << (_la - 93)) & 19140298416324609L) != 0)) ) {
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
	public static class Create_trigger_stmtContext extends ParserRuleContext {
		public TerminalNode CREATE_() { return getToken(SQLiteParser.CREATE_, 0); }
		public TerminalNode TRIGGER_() { return getToken(SQLiteParser.TRIGGER_, 0); }
		public Trigger_nameContext trigger_name() {
			return getRuleContext(Trigger_nameContext.class,0);
		}
		public TerminalNode ON_() { return getToken(SQLiteParser.ON_, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public TerminalNode BEGIN_() { return getToken(SQLiteParser.BEGIN_, 0); }
		public TerminalNode END_() { return getToken(SQLiteParser.END_, 0); }
		public TerminalNode DELETE_() { return getToken(SQLiteParser.DELETE_, 0); }
		public TerminalNode INSERT_() { return getToken(SQLiteParser.INSERT_, 0); }
		public TerminalNode UPDATE_() { return getToken(SQLiteParser.UPDATE_, 0); }
		public TerminalNode IF_() { return getToken(SQLiteParser.IF_, 0); }
		public TerminalNode NOT_() { return getToken(SQLiteParser.NOT_, 0); }
		public TerminalNode EXISTS_() { return getToken(SQLiteParser.EXISTS_, 0); }
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(SQLiteParser.DOT, 0); }
		public TerminalNode BEFORE_() { return getToken(SQLiteParser.BEFORE_, 0); }
		public TerminalNode AFTER_() { return getToken(SQLiteParser.AFTER_, 0); }
		public TerminalNode INSTEAD_() { return getToken(SQLiteParser.INSTEAD_, 0); }
		public List<TerminalNode> OF_() { return getTokens(SQLiteParser.OF_); }
		public TerminalNode OF_(int i) {
			return getToken(SQLiteParser.OF_, i);
		}
		public TerminalNode FOR_() { return getToken(SQLiteParser.FOR_, 0); }
		public TerminalNode EACH_() { return getToken(SQLiteParser.EACH_, 0); }
		public TerminalNode ROW_() { return getToken(SQLiteParser.ROW_, 0); }
		public TerminalNode WHEN_() { return getToken(SQLiteParser.WHEN_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public List<TerminalNode> SCOL() { return getTokens(SQLiteParser.SCOL); }
		public TerminalNode SCOL(int i) {
			return getToken(SQLiteParser.SCOL, i);
		}
		public TerminalNode TEMP_() { return getToken(SQLiteParser.TEMP_, 0); }
		public TerminalNode TEMPORARY_() { return getToken(SQLiteParser.TEMPORARY_, 0); }
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public List<Update_stmtContext> update_stmt() {
			return getRuleContexts(Update_stmtContext.class);
		}
		public Update_stmtContext update_stmt(int i) {
			return getRuleContext(Update_stmtContext.class,i);
		}
		public List<Insert_stmtContext> insert_stmt() {
			return getRuleContexts(Insert_stmtContext.class);
		}
		public Insert_stmtContext insert_stmt(int i) {
			return getRuleContext(Insert_stmtContext.class,i);
		}
		public List<Delete_stmtContext> delete_stmt() {
			return getRuleContexts(Delete_stmtContext.class);
		}
		public Delete_stmtContext delete_stmt(int i) {
			return getRuleContext(Delete_stmtContext.class,i);
		}
		public List<Select_stmtContext> select_stmt() {
			return getRuleContexts(Select_stmtContext.class);
		}
		public Select_stmtContext select_stmt(int i) {
			return getRuleContext(Select_stmtContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Create_trigger_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_trigger_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterCreate_trigger_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitCreate_trigger_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitCreate_trigger_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_trigger_stmtContext create_trigger_stmt() throws RecognitionException {
		Create_trigger_stmtContext _localctx = new Create_trigger_stmtContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_create_trigger_stmt);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(627);
			match(CREATE_);
			setState(629);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,69,_ctx) ) {
			case 1:
				{
				setState(628);
				_la = _input.LA(1);
				if ( !(_la==TEMP_ || _la==TEMPORARY_) ) {
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
			setState(631);
			match(TRIGGER_);
			setState(635);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,70,_ctx) ) {
			case 1:
				{
				setState(632);
				match(IF_);
				setState(633);
				match(NOT_);
				setState(634);
				match(EXISTS_);
				}
				break;
			}
			setState(640);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,71,_ctx) ) {
			case 1:
				{
				setState(637);
				schema_name();
				setState(638);
				match(DOT);
				}
				break;
			}
			setState(642);
			trigger_name();
			setState(647);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,72,_ctx) ) {
			case 1:
				{
				setState(643);
				match(BEFORE_);
				}
				break;
			case 2:
				{
				setState(644);
				match(AFTER_);
				}
				break;
			case 3:
				{
				setState(645);
				match(INSTEAD_);
				setState(646);
				match(OF_);
				}
				break;
			}
			setState(663);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,75,_ctx) ) {
			case 1:
				{
				setState(649);
				match(DELETE_);
				}
				break;
			case 2:
				{
				setState(650);
				match(INSERT_);
				}
				break;
			case 3:
				{
				setState(651);
				match(UPDATE_);
				setState(661);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,74,_ctx) ) {
				case 1:
					{
					setState(652);
					match(OF_);
					setState(653);
					column_name();
					setState(658);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,73,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(654);
							match(COMMA);
							setState(655);
							column_name();
							}
							} 
						}
						setState(660);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,73,_ctx);
					}
					}
					break;
				}
				}
				break;
			}
			setState(665);
			match(ON_);
			setState(666);
			table_name();
			setState(670);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,76,_ctx) ) {
			case 1:
				{
				setState(667);
				match(FOR_);
				setState(668);
				match(EACH_);
				setState(669);
				match(ROW_);
				}
				break;
			}
			setState(674);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,77,_ctx) ) {
			case 1:
				{
				setState(672);
				match(WHEN_);
				setState(673);
				expr();
				}
				break;
			}
			setState(676);
			match(BEGIN_);
			setState(685); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(681);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
					case 1:
						{
						setState(677);
						update_stmt();
						}
						break;
					case 2:
						{
						setState(678);
						insert_stmt();
						}
						break;
					case 3:
						{
						setState(679);
						delete_stmt();
						}
						break;
					case 4:
						{
						setState(680);
						select_stmt();
						}
						break;
					}
					setState(683);
					match(SCOL);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(687); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,79,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(689);
			match(END_);
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
	public static class Create_view_stmtContext extends ParserRuleContext {
		public TerminalNode CREATE_() { return getToken(SQLiteParser.CREATE_, 0); }
		public TerminalNode VIEW_() { return getToken(SQLiteParser.VIEW_, 0); }
		public View_nameContext view_name() {
			return getRuleContext(View_nameContext.class,0);
		}
		public TerminalNode AS_() { return getToken(SQLiteParser.AS_, 0); }
		public Select_stmtContext select_stmt() {
			return getRuleContext(Select_stmtContext.class,0);
		}
		public TerminalNode IF_() { return getToken(SQLiteParser.IF_, 0); }
		public TerminalNode NOT_() { return getToken(SQLiteParser.NOT_, 0); }
		public TerminalNode EXISTS_() { return getToken(SQLiteParser.EXISTS_, 0); }
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(SQLiteParser.DOT, 0); }
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public TerminalNode TEMP_() { return getToken(SQLiteParser.TEMP_, 0); }
		public TerminalNode TEMPORARY_() { return getToken(SQLiteParser.TEMPORARY_, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Create_view_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_view_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterCreate_view_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitCreate_view_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitCreate_view_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_view_stmtContext create_view_stmt() throws RecognitionException {
		Create_view_stmtContext _localctx = new Create_view_stmtContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_create_view_stmt);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(691);
			match(CREATE_);
			setState(693);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,80,_ctx) ) {
			case 1:
				{
				setState(692);
				_la = _input.LA(1);
				if ( !(_la==TEMP_ || _la==TEMPORARY_) ) {
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
			setState(695);
			match(VIEW_);
			setState(699);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,81,_ctx) ) {
			case 1:
				{
				setState(696);
				match(IF_);
				setState(697);
				match(NOT_);
				setState(698);
				match(EXISTS_);
				}
				break;
			}
			setState(704);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
			case 1:
				{
				setState(701);
				schema_name();
				setState(702);
				match(DOT);
				}
				break;
			}
			setState(706);
			view_name();
			setState(718);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,84,_ctx) ) {
			case 1:
				{
				setState(707);
				match(OPEN_PAR);
				setState(708);
				column_name();
				setState(713);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,83,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(709);
						match(COMMA);
						setState(710);
						column_name();
						}
						} 
					}
					setState(715);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,83,_ctx);
				}
				setState(716);
				match(CLOSE_PAR);
				}
				break;
			}
			setState(720);
			match(AS_);
			setState(721);
			select_stmt();
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
	public static class Create_virtual_table_stmtContext extends ParserRuleContext {
		public TerminalNode CREATE_() { return getToken(SQLiteParser.CREATE_, 0); }
		public TerminalNode VIRTUAL_() { return getToken(SQLiteParser.VIRTUAL_, 0); }
		public TerminalNode TABLE_() { return getToken(SQLiteParser.TABLE_, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public TerminalNode USING_() { return getToken(SQLiteParser.USING_, 0); }
		public Module_nameContext module_name() {
			return getRuleContext(Module_nameContext.class,0);
		}
		public TerminalNode IF_() { return getToken(SQLiteParser.IF_, 0); }
		public TerminalNode NOT_() { return getToken(SQLiteParser.NOT_, 0); }
		public TerminalNode EXISTS_() { return getToken(SQLiteParser.EXISTS_, 0); }
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(SQLiteParser.DOT, 0); }
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public List<Module_argumentContext> module_argument() {
			return getRuleContexts(Module_argumentContext.class);
		}
		public Module_argumentContext module_argument(int i) {
			return getRuleContext(Module_argumentContext.class,i);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Create_virtual_table_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_virtual_table_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterCreate_virtual_table_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitCreate_virtual_table_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitCreate_virtual_table_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Create_virtual_table_stmtContext create_virtual_table_stmt() throws RecognitionException {
		Create_virtual_table_stmtContext _localctx = new Create_virtual_table_stmtContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_create_virtual_table_stmt);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(723);
			match(CREATE_);
			setState(724);
			match(VIRTUAL_);
			setState(725);
			match(TABLE_);
			setState(729);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,85,_ctx) ) {
			case 1:
				{
				setState(726);
				match(IF_);
				setState(727);
				match(NOT_);
				setState(728);
				match(EXISTS_);
				}
				break;
			}
			setState(734);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,86,_ctx) ) {
			case 1:
				{
				setState(731);
				schema_name();
				setState(732);
				match(DOT);
				}
				break;
			}
			setState(736);
			table_name();
			setState(737);
			match(USING_);
			setState(738);
			module_name();
			setState(750);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,88,_ctx) ) {
			case 1:
				{
				setState(739);
				match(OPEN_PAR);
				setState(740);
				module_argument();
				setState(745);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,87,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(741);
						match(COMMA);
						setState(742);
						module_argument();
						}
						} 
					}
					setState(747);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,87,_ctx);
				}
				setState(748);
				match(CLOSE_PAR);
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
	public static class With_clauseContext extends ParserRuleContext {
		public TerminalNode WITH_() { return getToken(SQLiteParser.WITH_, 0); }
		public List<Common_table_expressionContext> common_table_expression() {
			return getRuleContexts(Common_table_expressionContext.class);
		}
		public Common_table_expressionContext common_table_expression(int i) {
			return getRuleContext(Common_table_expressionContext.class,i);
		}
		public TerminalNode RECURSIVE_() { return getToken(SQLiteParser.RECURSIVE_, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public With_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_with_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterWith_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitWith_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitWith_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final With_clauseContext with_clause() throws RecognitionException {
		With_clauseContext _localctx = new With_clauseContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_with_clause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(752);
			match(WITH_);
			setState(754);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,89,_ctx) ) {
			case 1:
				{
				setState(753);
				match(RECURSIVE_);
				}
				break;
			}
			setState(756);
			common_table_expression();
			setState(761);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,90,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(757);
					match(COMMA);
					setState(758);
					common_table_expression();
					}
					} 
				}
				setState(763);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,90,_ctx);
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
	public static class Common_table_expressionContext extends ParserRuleContext {
		public Cte_table_nameContext cte_table_name() {
			return getRuleContext(Cte_table_nameContext.class,0);
		}
		public TerminalNode AS_() { return getToken(SQLiteParser.AS_, 0); }
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public Select_stmtContext select_stmt() {
			return getRuleContext(Select_stmtContext.class,0);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public TerminalNode MATERIALIZED_() { return getToken(SQLiteParser.MATERIALIZED_, 0); }
		public TerminalNode NOT_() { return getToken(SQLiteParser.NOT_, 0); }
		public Common_table_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_common_table_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterCommon_table_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitCommon_table_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitCommon_table_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Common_table_expressionContext common_table_expression() throws RecognitionException {
		Common_table_expressionContext _localctx = new Common_table_expressionContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_common_table_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(764);
			cte_table_name();
			setState(765);
			match(AS_);
			setState(770);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,92,_ctx) ) {
			case 1:
				{
				setState(767);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,91,_ctx) ) {
				case 1:
					{
					setState(766);
					match(NOT_);
					}
					break;
				}
				setState(769);
				match(MATERIALIZED_);
				}
				break;
			}
			setState(772);
			match(OPEN_PAR);
			setState(773);
			select_stmt();
			setState(774);
			match(CLOSE_PAR);
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
	public static class Cte_table_nameContext extends ParserRuleContext {
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Cte_table_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cte_table_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterCte_table_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitCte_table_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitCte_table_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Cte_table_nameContext cte_table_name() throws RecognitionException {
		Cte_table_nameContext _localctx = new Cte_table_nameContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_cte_table_name);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(776);
			table_name();
			setState(788);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,94,_ctx) ) {
			case 1:
				{
				setState(777);
				match(OPEN_PAR);
				setState(778);
				column_name();
				setState(783);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,93,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(779);
						match(COMMA);
						setState(780);
						column_name();
						}
						} 
					}
					setState(785);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,93,_ctx);
				}
				setState(786);
				match(CLOSE_PAR);
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
	public static class Delete_stmtContext extends ParserRuleContext {
		public TerminalNode DELETE_() { return getToken(SQLiteParser.DELETE_, 0); }
		public TerminalNode FROM_() { return getToken(SQLiteParser.FROM_, 0); }
		public Qualified_table_nameContext qualified_table_name() {
			return getRuleContext(Qualified_table_nameContext.class,0);
		}
		public With_clauseContext with_clause() {
			return getRuleContext(With_clauseContext.class,0);
		}
		public TerminalNode WHERE_() { return getToken(SQLiteParser.WHERE_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public Returning_clauseContext returning_clause() {
			return getRuleContext(Returning_clauseContext.class,0);
		}
		public Order_clauseContext order_clause() {
			return getRuleContext(Order_clauseContext.class,0);
		}
		public Limit_clauseContext limit_clause() {
			return getRuleContext(Limit_clauseContext.class,0);
		}
		public Delete_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_delete_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterDelete_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitDelete_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitDelete_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Delete_stmtContext delete_stmt() throws RecognitionException {
		Delete_stmtContext _localctx = new Delete_stmtContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_delete_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(791);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,95,_ctx) ) {
			case 1:
				{
				setState(790);
				with_clause();
				}
				break;
			}
			setState(793);
			match(DELETE_);
			setState(794);
			match(FROM_);
			setState(795);
			qualified_table_name();
			setState(798);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,96,_ctx) ) {
			case 1:
				{
				setState(796);
				match(WHERE_);
				setState(797);
				expr();
				}
				break;
			}
			setState(801);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,97,_ctx) ) {
			case 1:
				{
				setState(800);
				returning_clause();
				}
				break;
			}
			setState(804);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,98,_ctx) ) {
			case 1:
				{
				setState(803);
				order_clause();
				}
				break;
			}
			setState(807);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,99,_ctx) ) {
			case 1:
				{
				setState(806);
				limit_clause();
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
	public static class Detach_stmtContext extends ParserRuleContext {
		public TerminalNode DETACH_() { return getToken(SQLiteParser.DETACH_, 0); }
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DATABASE_() { return getToken(SQLiteParser.DATABASE_, 0); }
		public Detach_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_detach_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterDetach_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitDetach_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitDetach_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Detach_stmtContext detach_stmt() throws RecognitionException {
		Detach_stmtContext _localctx = new Detach_stmtContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_detach_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(809);
			match(DETACH_);
			setState(811);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,100,_ctx) ) {
			case 1:
				{
				setState(810);
				match(DATABASE_);
				}
				break;
			}
			setState(813);
			schema_name();
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
	public static class Drop_stmtContext extends ParserRuleContext {
		public Token object;
		public TerminalNode DROP_() { return getToken(SQLiteParser.DROP_, 0); }
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public TerminalNode INDEX_() { return getToken(SQLiteParser.INDEX_, 0); }
		public TerminalNode TABLE_() { return getToken(SQLiteParser.TABLE_, 0); }
		public TerminalNode TRIGGER_() { return getToken(SQLiteParser.TRIGGER_, 0); }
		public TerminalNode VIEW_() { return getToken(SQLiteParser.VIEW_, 0); }
		public TerminalNode IF_() { return getToken(SQLiteParser.IF_, 0); }
		public TerminalNode EXISTS_() { return getToken(SQLiteParser.EXISTS_, 0); }
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(SQLiteParser.DOT, 0); }
		public Drop_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterDrop_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitDrop_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitDrop_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Drop_stmtContext drop_stmt() throws RecognitionException {
		Drop_stmtContext _localctx = new Drop_stmtContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_drop_stmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(815);
			match(DROP_);
			setState(816);
			((Drop_stmtContext)_localctx).object = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==INDEX_ || _la==TABLE_ || _la==TRIGGER_ || _la==VIEW_) ) {
				((Drop_stmtContext)_localctx).object = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(819);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,101,_ctx) ) {
			case 1:
				{
				setState(817);
				match(IF_);
				setState(818);
				match(EXISTS_);
				}
				break;
			}
			setState(824);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,102,_ctx) ) {
			case 1:
				{
				setState(821);
				schema_name();
				setState(822);
				match(DOT);
				}
				break;
			}
			setState(826);
			any_name();
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
	public static class ExprContext extends ParserRuleContext {
		public Expr_orContext expr_or() {
			return getRuleContext(Expr_orContext.class,0);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(828);
			expr_or();
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
	public static class Expr_orContext extends ParserRuleContext {
		public List<Expr_andContext> expr_and() {
			return getRuleContexts(Expr_andContext.class);
		}
		public Expr_andContext expr_and(int i) {
			return getRuleContext(Expr_andContext.class,i);
		}
		public List<TerminalNode> OR_() { return getTokens(SQLiteParser.OR_); }
		public TerminalNode OR_(int i) {
			return getToken(SQLiteParser.OR_, i);
		}
		public Expr_orContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr_or; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterExpr_or(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitExpr_or(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitExpr_or(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expr_orContext expr_or() throws RecognitionException {
		Expr_orContext _localctx = new Expr_orContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_expr_or);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(830);
			expr_and();
			setState(835);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,103,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(831);
					match(OR_);
					setState(832);
					expr_and();
					}
					} 
				}
				setState(837);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,103,_ctx);
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
	public static class Expr_andContext extends ParserRuleContext {
		public List<Expr_notContext> expr_not() {
			return getRuleContexts(Expr_notContext.class);
		}
		public Expr_notContext expr_not(int i) {
			return getRuleContext(Expr_notContext.class,i);
		}
		public List<TerminalNode> AND_() { return getTokens(SQLiteParser.AND_); }
		public TerminalNode AND_(int i) {
			return getToken(SQLiteParser.AND_, i);
		}
		public Expr_andContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr_and; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterExpr_and(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitExpr_and(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitExpr_and(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expr_andContext expr_and() throws RecognitionException {
		Expr_andContext _localctx = new Expr_andContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_expr_and);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(838);
			expr_not();
			setState(843);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,104,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(839);
					match(AND_);
					setState(840);
					expr_not();
					}
					} 
				}
				setState(845);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,104,_ctx);
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
	public static class Expr_notContext extends ParserRuleContext {
		public Expr_binaryContext expr_binary() {
			return getRuleContext(Expr_binaryContext.class,0);
		}
		public List<TerminalNode> NOT_() { return getTokens(SQLiteParser.NOT_); }
		public TerminalNode NOT_(int i) {
			return getToken(SQLiteParser.NOT_, i);
		}
		public Expr_notContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr_not; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterExpr_not(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitExpr_not(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitExpr_not(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expr_notContext expr_not() throws RecognitionException {
		Expr_notContext _localctx = new Expr_notContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_expr_not);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(849);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,105,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(846);
					match(NOT_);
					}
					} 
				}
				setState(851);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,105,_ctx);
			}
			setState(852);
			expr_binary();
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
	public static class Expr_binaryContext extends ParserRuleContext {
		public List<Expr_comparisonContext> expr_comparison() {
			return getRuleContexts(Expr_comparisonContext.class);
		}
		public Expr_comparisonContext expr_comparison(int i) {
			return getRuleContext(Expr_comparisonContext.class,i);
		}
		public List<TerminalNode> IS_() { return getTokens(SQLiteParser.IS_); }
		public TerminalNode IS_(int i) {
			return getToken(SQLiteParser.IS_, i);
		}
		public List<TerminalNode> BETWEEN_() { return getTokens(SQLiteParser.BETWEEN_); }
		public TerminalNode BETWEEN_(int i) {
			return getToken(SQLiteParser.BETWEEN_, i);
		}
		public List<TerminalNode> AND_() { return getTokens(SQLiteParser.AND_); }
		public TerminalNode AND_(int i) {
			return getToken(SQLiteParser.AND_, i);
		}
		public List<TerminalNode> IN_() { return getTokens(SQLiteParser.IN_); }
		public TerminalNode IN_(int i) {
			return getToken(SQLiteParser.IN_, i);
		}
		public List<TerminalNode> ISNULL_() { return getTokens(SQLiteParser.ISNULL_); }
		public TerminalNode ISNULL_(int i) {
			return getToken(SQLiteParser.ISNULL_, i);
		}
		public List<TerminalNode> NOTNULL_() { return getTokens(SQLiteParser.NOTNULL_); }
		public TerminalNode NOTNULL_(int i) {
			return getToken(SQLiteParser.NOTNULL_, i);
		}
		public List<TerminalNode> NOT_() { return getTokens(SQLiteParser.NOT_); }
		public TerminalNode NOT_(int i) {
			return getToken(SQLiteParser.NOT_, i);
		}
		public List<TerminalNode> NULL_() { return getTokens(SQLiteParser.NULL_); }
		public TerminalNode NULL_(int i) {
			return getToken(SQLiteParser.NULL_, i);
		}
		public List<TerminalNode> ASSIGN() { return getTokens(SQLiteParser.ASSIGN); }
		public TerminalNode ASSIGN(int i) {
			return getToken(SQLiteParser.ASSIGN, i);
		}
		public List<TerminalNode> EQ() { return getTokens(SQLiteParser.EQ); }
		public TerminalNode EQ(int i) {
			return getToken(SQLiteParser.EQ, i);
		}
		public List<TerminalNode> NOT_EQ1() { return getTokens(SQLiteParser.NOT_EQ1); }
		public TerminalNode NOT_EQ1(int i) {
			return getToken(SQLiteParser.NOT_EQ1, i);
		}
		public List<TerminalNode> NOT_EQ2() { return getTokens(SQLiteParser.NOT_EQ2); }
		public TerminalNode NOT_EQ2(int i) {
			return getToken(SQLiteParser.NOT_EQ2, i);
		}
		public List<TerminalNode> OPEN_PAR() { return getTokens(SQLiteParser.OPEN_PAR); }
		public TerminalNode OPEN_PAR(int i) {
			return getToken(SQLiteParser.OPEN_PAR, i);
		}
		public List<TerminalNode> CLOSE_PAR() { return getTokens(SQLiteParser.CLOSE_PAR); }
		public TerminalNode CLOSE_PAR(int i) {
			return getToken(SQLiteParser.CLOSE_PAR, i);
		}
		public List<Table_nameContext> table_name() {
			return getRuleContexts(Table_nameContext.class);
		}
		public Table_nameContext table_name(int i) {
			return getRuleContext(Table_nameContext.class,i);
		}
		public List<Table_function_nameContext> table_function_name() {
			return getRuleContexts(Table_function_nameContext.class);
		}
		public Table_function_nameContext table_function_name(int i) {
			return getRuleContext(Table_function_nameContext.class,i);
		}
		public List<TerminalNode> LIKE_() { return getTokens(SQLiteParser.LIKE_); }
		public TerminalNode LIKE_(int i) {
			return getToken(SQLiteParser.LIKE_, i);
		}
		public List<TerminalNode> DISTINCT_() { return getTokens(SQLiteParser.DISTINCT_); }
		public TerminalNode DISTINCT_(int i) {
			return getToken(SQLiteParser.DISTINCT_, i);
		}
		public List<TerminalNode> FROM_() { return getTokens(SQLiteParser.FROM_); }
		public TerminalNode FROM_(int i) {
			return getToken(SQLiteParser.FROM_, i);
		}
		public List<TerminalNode> GLOB_() { return getTokens(SQLiteParser.GLOB_); }
		public TerminalNode GLOB_(int i) {
			return getToken(SQLiteParser.GLOB_, i);
		}
		public List<TerminalNode> REGEXP_() { return getTokens(SQLiteParser.REGEXP_); }
		public TerminalNode REGEXP_(int i) {
			return getToken(SQLiteParser.REGEXP_, i);
		}
		public List<TerminalNode> MATCH_() { return getTokens(SQLiteParser.MATCH_); }
		public TerminalNode MATCH_(int i) {
			return getToken(SQLiteParser.MATCH_, i);
		}
		public List<Select_stmtContext> select_stmt() {
			return getRuleContexts(Select_stmtContext.class);
		}
		public Select_stmtContext select_stmt(int i) {
			return getRuleContext(Select_stmtContext.class,i);
		}
		public List<Schema_nameContext> schema_name() {
			return getRuleContexts(Schema_nameContext.class);
		}
		public Schema_nameContext schema_name(int i) {
			return getRuleContext(Schema_nameContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(SQLiteParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(SQLiteParser.DOT, i);
		}
		public List<TerminalNode> ESCAPE_() { return getTokens(SQLiteParser.ESCAPE_); }
		public TerminalNode ESCAPE_(int i) {
			return getToken(SQLiteParser.ESCAPE_, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Expr_binaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr_binary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterExpr_binary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitExpr_binary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitExpr_binary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expr_binaryContext expr_binary() throws RecognitionException {
		Expr_binaryContext _localctx = new Expr_binaryContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_expr_binary);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(854);
			expr_comparison();
			setState(937);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,121,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					setState(935);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,120,_ctx) ) {
					case 1:
						{
						setState(855);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 29360192L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(856);
						expr_comparison();
						}
						break;
					case 2:
						{
						setState(857);
						match(IS_);
						setState(859);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,106,_ctx) ) {
						case 1:
							{
							setState(858);
							match(NOT_);
							}
							break;
						}
						setState(863);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,107,_ctx) ) {
						case 1:
							{
							setState(861);
							match(DISTINCT_);
							setState(862);
							match(FROM_);
							}
							break;
						}
						setState(865);
						expr_comparison();
						}
						break;
					case 3:
						{
						setState(867);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,108,_ctx) ) {
						case 1:
							{
							setState(866);
							match(NOT_);
							}
							break;
						}
						setState(869);
						match(BETWEEN_);
						setState(870);
						expr_comparison();
						setState(871);
						match(AND_);
						setState(872);
						expr_comparison();
						}
						break;
					case 4:
						{
						setState(875);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,109,_ctx) ) {
						case 1:
							{
							setState(874);
							match(NOT_);
							}
							break;
						}
						setState(877);
						match(IN_);
						setState(916);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,116,_ctx) ) {
						case 1:
							{
							setState(878);
							match(OPEN_PAR);
							setState(888);
							_errHandler.sync(this);
							switch ( getInterpreter().adaptivePredict(_input,111,_ctx) ) {
							case 1:
								{
								setState(879);
								select_stmt();
								}
								break;
							case 2:
								{
								setState(880);
								expr_comparison();
								setState(885);
								_errHandler.sync(this);
								_alt = getInterpreter().adaptivePredict(_input,110,_ctx);
								while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
									if ( _alt==1 ) {
										{
										{
										setState(881);
										match(COMMA);
										setState(882);
										expr_comparison();
										}
										} 
									}
									setState(887);
									_errHandler.sync(this);
									_alt = getInterpreter().adaptivePredict(_input,110,_ctx);
								}
								}
								break;
							}
							setState(890);
							match(CLOSE_PAR);
							}
							break;
						case 2:
							{
							setState(894);
							_errHandler.sync(this);
							switch ( getInterpreter().adaptivePredict(_input,112,_ctx) ) {
							case 1:
								{
								setState(891);
								schema_name();
								setState(892);
								match(DOT);
								}
								break;
							}
							setState(896);
							table_name();
							}
							break;
						case 3:
							{
							setState(900);
							_errHandler.sync(this);
							switch ( getInterpreter().adaptivePredict(_input,113,_ctx) ) {
							case 1:
								{
								setState(897);
								schema_name();
								setState(898);
								match(DOT);
								}
								break;
							}
							setState(902);
							table_function_name();
							setState(903);
							match(OPEN_PAR);
							setState(912);
							_errHandler.sync(this);
							switch ( getInterpreter().adaptivePredict(_input,115,_ctx) ) {
							case 1:
								{
								setState(904);
								expr_comparison();
								setState(909);
								_errHandler.sync(this);
								_alt = getInterpreter().adaptivePredict(_input,114,_ctx);
								while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
									if ( _alt==1 ) {
										{
										{
										setState(905);
										match(COMMA);
										setState(906);
										expr_comparison();
										}
										} 
									}
									setState(911);
									_errHandler.sync(this);
									_alt = getInterpreter().adaptivePredict(_input,114,_ctx);
								}
								}
								break;
							}
							setState(914);
							match(CLOSE_PAR);
							}
							break;
						}
						}
						break;
					case 5:
						{
						setState(919);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,117,_ctx) ) {
						case 1:
							{
							setState(918);
							match(NOT_);
							}
							break;
						}
						setState(929);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,119,_ctx) ) {
						case 1:
							{
							setState(921);
							match(LIKE_);
							setState(922);
							expr_comparison();
							setState(925);
							_errHandler.sync(this);
							switch ( getInterpreter().adaptivePredict(_input,118,_ctx) ) {
							case 1:
								{
								setState(923);
								match(ESCAPE_);
								setState(924);
								expr_comparison();
								}
								break;
							}
							}
							break;
						case 2:
							{
							setState(927);
							_la = _input.LA(1);
							if ( !(((((_la - 88)) & ~0x3f) == 0 && ((1L << (_la - 88)) & 2251799830462465L) != 0)) ) {
							_errHandler.recoverInline(this);
							}
							else {
								if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
								_errHandler.reportMatch(this);
								consume();
							}
							setState(928);
							expr_comparison();
							}
							break;
						}
						}
						break;
					case 6:
						{
						setState(931);
						match(ISNULL_);
						}
						break;
					case 7:
						{
						setState(932);
						match(NOTNULL_);
						}
						break;
					case 8:
						{
						setState(933);
						match(NOT_);
						setState(934);
						match(NULL_);
						}
						break;
					}
					} 
				}
				setState(939);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,121,_ctx);
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
	public static class Expr_comparisonContext extends ParserRuleContext {
		public List<Expr_bitwiseContext> expr_bitwise() {
			return getRuleContexts(Expr_bitwiseContext.class);
		}
		public Expr_bitwiseContext expr_bitwise(int i) {
			return getRuleContext(Expr_bitwiseContext.class,i);
		}
		public List<TerminalNode> LT() { return getTokens(SQLiteParser.LT); }
		public TerminalNode LT(int i) {
			return getToken(SQLiteParser.LT, i);
		}
		public List<TerminalNode> LT_EQ() { return getTokens(SQLiteParser.LT_EQ); }
		public TerminalNode LT_EQ(int i) {
			return getToken(SQLiteParser.LT_EQ, i);
		}
		public List<TerminalNode> GT() { return getTokens(SQLiteParser.GT); }
		public TerminalNode GT(int i) {
			return getToken(SQLiteParser.GT, i);
		}
		public List<TerminalNode> GT_EQ() { return getTokens(SQLiteParser.GT_EQ); }
		public TerminalNode GT_EQ(int i) {
			return getToken(SQLiteParser.GT_EQ, i);
		}
		public Expr_comparisonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr_comparison; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterExpr_comparison(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitExpr_comparison(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitExpr_comparison(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expr_comparisonContext expr_comparison() throws RecognitionException {
		Expr_comparisonContext _localctx = new Expr_comparisonContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_expr_comparison);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(940);
			expr_bitwise();
			setState(945);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,122,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(941);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 3932160L) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(942);
					expr_bitwise();
					}
					} 
				}
				setState(947);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,122,_ctx);
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
	public static class Expr_bitwiseContext extends ParserRuleContext {
		public List<Expr_additionContext> expr_addition() {
			return getRuleContexts(Expr_additionContext.class);
		}
		public Expr_additionContext expr_addition(int i) {
			return getRuleContext(Expr_additionContext.class,i);
		}
		public List<TerminalNode> LT2() { return getTokens(SQLiteParser.LT2); }
		public TerminalNode LT2(int i) {
			return getToken(SQLiteParser.LT2, i);
		}
		public List<TerminalNode> GT2() { return getTokens(SQLiteParser.GT2); }
		public TerminalNode GT2(int i) {
			return getToken(SQLiteParser.GT2, i);
		}
		public List<TerminalNode> AMP() { return getTokens(SQLiteParser.AMP); }
		public TerminalNode AMP(int i) {
			return getToken(SQLiteParser.AMP, i);
		}
		public List<TerminalNode> PIPE() { return getTokens(SQLiteParser.PIPE); }
		public TerminalNode PIPE(int i) {
			return getToken(SQLiteParser.PIPE, i);
		}
		public Expr_bitwiseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr_bitwise; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterExpr_bitwise(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitExpr_bitwise(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitExpr_bitwise(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expr_bitwiseContext expr_bitwise() throws RecognitionException {
		Expr_bitwiseContext _localctx = new Expr_bitwiseContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_expr_bitwise);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(948);
			expr_addition();
			setState(953);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,123,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(949);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 245760L) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(950);
					expr_addition();
					}
					} 
				}
				setState(955);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,123,_ctx);
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
	public static class Expr_additionContext extends ParserRuleContext {
		public List<Expr_multiplicationContext> expr_multiplication() {
			return getRuleContexts(Expr_multiplicationContext.class);
		}
		public Expr_multiplicationContext expr_multiplication(int i) {
			return getRuleContext(Expr_multiplicationContext.class,i);
		}
		public List<TerminalNode> PLUS() { return getTokens(SQLiteParser.PLUS); }
		public TerminalNode PLUS(int i) {
			return getToken(SQLiteParser.PLUS, i);
		}
		public List<TerminalNode> MINUS() { return getTokens(SQLiteParser.MINUS); }
		public TerminalNode MINUS(int i) {
			return getToken(SQLiteParser.MINUS, i);
		}
		public Expr_additionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr_addition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterExpr_addition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitExpr_addition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitExpr_addition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expr_additionContext expr_addition() throws RecognitionException {
		Expr_additionContext _localctx = new Expr_additionContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_expr_addition);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(956);
			expr_multiplication();
			setState(961);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,124,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(957);
					_la = _input.LA(1);
					if ( !(_la==PLUS || _la==MINUS) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(958);
					expr_multiplication();
					}
					} 
				}
				setState(963);
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
	public static class Expr_multiplicationContext extends ParserRuleContext {
		public List<Expr_stringContext> expr_string() {
			return getRuleContexts(Expr_stringContext.class);
		}
		public Expr_stringContext expr_string(int i) {
			return getRuleContext(Expr_stringContext.class,i);
		}
		public List<TerminalNode> STAR() { return getTokens(SQLiteParser.STAR); }
		public TerminalNode STAR(int i) {
			return getToken(SQLiteParser.STAR, i);
		}
		public List<TerminalNode> DIV() { return getTokens(SQLiteParser.DIV); }
		public TerminalNode DIV(int i) {
			return getToken(SQLiteParser.DIV, i);
		}
		public List<TerminalNode> MOD() { return getTokens(SQLiteParser.MOD); }
		public TerminalNode MOD(int i) {
			return getToken(SQLiteParser.MOD, i);
		}
		public Expr_multiplicationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr_multiplication; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterExpr_multiplication(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitExpr_multiplication(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitExpr_multiplication(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expr_multiplicationContext expr_multiplication() throws RecognitionException {
		Expr_multiplicationContext _localctx = new Expr_multiplicationContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_expr_multiplication);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(964);
			expr_string();
			setState(969);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,125,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(965);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 12416L) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(966);
					expr_string();
					}
					} 
				}
				setState(971);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,125,_ctx);
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
	public static class Expr_stringContext extends ParserRuleContext {
		public List<Expr_collateContext> expr_collate() {
			return getRuleContexts(Expr_collateContext.class);
		}
		public Expr_collateContext expr_collate(int i) {
			return getRuleContext(Expr_collateContext.class,i);
		}
		public List<TerminalNode> PIPE2() { return getTokens(SQLiteParser.PIPE2); }
		public TerminalNode PIPE2(int i) {
			return getToken(SQLiteParser.PIPE2, i);
		}
		public List<TerminalNode> JPTR() { return getTokens(SQLiteParser.JPTR); }
		public TerminalNode JPTR(int i) {
			return getToken(SQLiteParser.JPTR, i);
		}
		public List<TerminalNode> JPTR2() { return getTokens(SQLiteParser.JPTR2); }
		public TerminalNode JPTR2(int i) {
			return getToken(SQLiteParser.JPTR2, i);
		}
		public Expr_stringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr_string; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterExpr_string(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitExpr_string(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitExpr_string(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expr_stringContext expr_string() throws RecognitionException {
		Expr_stringContext _localctx = new Expr_stringContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_expr_string);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(972);
			expr_collate();
			setState(977);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,126,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(973);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 100665344L) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(974);
					expr_collate();
					}
					} 
				}
				setState(979);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,126,_ctx);
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
	public static class Expr_collateContext extends ParserRuleContext {
		public Expr_unaryContext expr_unary() {
			return getRuleContext(Expr_unaryContext.class,0);
		}
		public List<TerminalNode> COLLATE_() { return getTokens(SQLiteParser.COLLATE_); }
		public TerminalNode COLLATE_(int i) {
			return getToken(SQLiteParser.COLLATE_, i);
		}
		public List<Collation_nameContext> collation_name() {
			return getRuleContexts(Collation_nameContext.class);
		}
		public Collation_nameContext collation_name(int i) {
			return getRuleContext(Collation_nameContext.class,i);
		}
		public Expr_collateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr_collate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterExpr_collate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitExpr_collate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitExpr_collate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expr_collateContext expr_collate() throws RecognitionException {
		Expr_collateContext _localctx = new Expr_collateContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_expr_collate);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(980);
			expr_unary();
			setState(985);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,127,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(981);
					match(COLLATE_);
					setState(982);
					collation_name();
					}
					} 
				}
				setState(987);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,127,_ctx);
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
	public static class Expr_unaryContext extends ParserRuleContext {
		public Expr_baseContext expr_base() {
			return getRuleContext(Expr_baseContext.class,0);
		}
		public List<TerminalNode> MINUS() { return getTokens(SQLiteParser.MINUS); }
		public TerminalNode MINUS(int i) {
			return getToken(SQLiteParser.MINUS, i);
		}
		public List<TerminalNode> PLUS() { return getTokens(SQLiteParser.PLUS); }
		public TerminalNode PLUS(int i) {
			return getToken(SQLiteParser.PLUS, i);
		}
		public List<TerminalNode> TILDE() { return getTokens(SQLiteParser.TILDE); }
		public TerminalNode TILDE(int i) {
			return getToken(SQLiteParser.TILDE, i);
		}
		public Expr_unaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr_unary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterExpr_unary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitExpr_unary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitExpr_unary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expr_unaryContext expr_unary() throws RecognitionException {
		Expr_unaryContext _localctx = new Expr_unaryContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_expr_unary);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(991);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,128,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(988);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1792L) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
					} 
				}
				setState(993);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,128,_ctx);
			}
			setState(994);
			expr_base();
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
	public static class Expr_baseContext extends ParserRuleContext {
		public Literal_valueContext literal_value() {
			return getRuleContext(Literal_valueContext.class,0);
		}
		public TerminalNode BIND_PARAMETER() { return getToken(SQLiteParser.BIND_PARAMETER, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public List<TerminalNode> DOT() { return getTokens(SQLiteParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(SQLiteParser.DOT, i);
		}
		public Column_nameContext column_name() {
			return getRuleContext(Column_nameContext.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public Column_name_excluding_stringContext column_name_excluding_string() {
			return getRuleContext(Column_name_excluding_stringContext.class,0);
		}
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public Select_stmtContext select_stmt() {
			return getRuleContext(Select_stmtContext.class,0);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public TerminalNode EXISTS_() { return getToken(SQLiteParser.EXISTS_, 0); }
		public TerminalNode NOT_() { return getToken(SQLiteParser.NOT_, 0); }
		public Raise_functionContext raise_function() {
			return getRuleContext(Raise_functionContext.class,0);
		}
		public Expr_recursiveContext expr_recursive() {
			return getRuleContext(Expr_recursiveContext.class,0);
		}
		public Expr_baseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr_base; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterExpr_base(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitExpr_base(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitExpr_base(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expr_baseContext expr_base() throws RecognitionException {
		Expr_baseContext _localctx = new Expr_baseContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_expr_base);
		try {
			setState(1020);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,132,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(996);
				literal_value();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(997);
				match(BIND_PARAMETER);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1001);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,129,_ctx) ) {
				case 1:
					{
					setState(998);
					schema_name();
					setState(999);
					match(DOT);
					}
					break;
				}
				setState(1003);
				table_name();
				setState(1004);
				match(DOT);
				setState(1005);
				column_name();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1007);
				column_name_excluding_string();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1012);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,131,_ctx) ) {
				case 1:
					{
					setState(1009);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,130,_ctx) ) {
					case 1:
						{
						setState(1008);
						match(NOT_);
						}
						break;
					}
					setState(1011);
					match(EXISTS_);
					}
					break;
				}
				setState(1014);
				match(OPEN_PAR);
				setState(1015);
				select_stmt();
				setState(1016);
				match(CLOSE_PAR);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1018);
				raise_function();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1019);
				expr_recursive();
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
	public static class Expr_recursiveContext extends ParserRuleContext {
		public Function_nameContext function_name() {
			return getRuleContext(Function_nameContext.class,0);
		}
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode STAR() { return getToken(SQLiteParser.STAR, 0); }
		public Percentile_clauseContext percentile_clause() {
			return getRuleContext(Percentile_clauseContext.class,0);
		}
		public Filter_clauseContext filter_clause() {
			return getRuleContext(Filter_clauseContext.class,0);
		}
		public Over_clauseContext over_clause() {
			return getRuleContext(Over_clauseContext.class,0);
		}
		public TerminalNode DISTINCT_() { return getToken(SQLiteParser.DISTINCT_, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Order_clauseContext order_clause() {
			return getRuleContext(Order_clauseContext.class,0);
		}
		public TerminalNode CAST_() { return getToken(SQLiteParser.CAST_, 0); }
		public TerminalNode AS_() { return getToken(SQLiteParser.AS_, 0); }
		public Type_nameContext type_name() {
			return getRuleContext(Type_nameContext.class,0);
		}
		public TerminalNode CASE_() { return getToken(SQLiteParser.CASE_, 0); }
		public TerminalNode END_() { return getToken(SQLiteParser.END_, 0); }
		public List<TerminalNode> WHEN_() { return getTokens(SQLiteParser.WHEN_); }
		public TerminalNode WHEN_(int i) {
			return getToken(SQLiteParser.WHEN_, i);
		}
		public List<TerminalNode> THEN_() { return getTokens(SQLiteParser.THEN_); }
		public TerminalNode THEN_(int i) {
			return getToken(SQLiteParser.THEN_, i);
		}
		public TerminalNode ELSE_() { return getToken(SQLiteParser.ELSE_, 0); }
		public Expr_recursiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr_recursive; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterExpr_recursive(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitExpr_recursive(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitExpr_recursive(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expr_recursiveContext expr_recursive() throws RecognitionException {
		Expr_recursiveContext _localctx = new Expr_recursiveContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_expr_recursive);
		try {
			int _alt;
			setState(1088);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,144,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1022);
				function_name();
				setState(1023);
				match(OPEN_PAR);
				setState(1039);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,136,_ctx) ) {
				case 1:
					{
					setState(1025);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,133,_ctx) ) {
					case 1:
						{
						setState(1024);
						match(DISTINCT_);
						}
						break;
					}
					setState(1027);
					expr();
					setState(1032);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,134,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(1028);
							match(COMMA);
							setState(1029);
							expr();
							}
							} 
						}
						setState(1034);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,134,_ctx);
					}
					setState(1036);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,135,_ctx) ) {
					case 1:
						{
						setState(1035);
						order_clause();
						}
						break;
					}
					}
					break;
				case 2:
					{
					setState(1038);
					match(STAR);
					}
					break;
				}
				setState(1041);
				match(CLOSE_PAR);
				setState(1043);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,137,_ctx) ) {
				case 1:
					{
					setState(1042);
					percentile_clause();
					}
					break;
				}
				setState(1046);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,138,_ctx) ) {
				case 1:
					{
					setState(1045);
					filter_clause();
					}
					break;
				}
				setState(1049);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,139,_ctx) ) {
				case 1:
					{
					setState(1048);
					over_clause();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1051);
				match(OPEN_PAR);
				setState(1052);
				expr();
				setState(1057);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,140,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1053);
						match(COMMA);
						setState(1054);
						expr();
						}
						} 
					}
					setState(1059);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,140,_ctx);
				}
				setState(1060);
				match(CLOSE_PAR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1062);
				match(CAST_);
				setState(1063);
				match(OPEN_PAR);
				setState(1064);
				expr();
				setState(1065);
				match(AS_);
				setState(1066);
				type_name();
				setState(1067);
				match(CLOSE_PAR);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1069);
				match(CASE_);
				setState(1071);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,141,_ctx) ) {
				case 1:
					{
					setState(1070);
					expr();
					}
					break;
				}
				setState(1078); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(1073);
						match(WHEN_);
						setState(1074);
						expr();
						setState(1075);
						match(THEN_);
						setState(1076);
						expr();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(1080); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,142,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(1084);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,143,_ctx) ) {
				case 1:
					{
					setState(1082);
					match(ELSE_);
					setState(1083);
					expr();
					}
					break;
				}
				setState(1086);
				match(END_);
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
	public static class Raise_functionContext extends ParserRuleContext {
		public TerminalNode RAISE_() { return getToken(SQLiteParser.RAISE_, 0); }
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public TerminalNode IGNORE_() { return getToken(SQLiteParser.IGNORE_, 0); }
		public TerminalNode COMMA() { return getToken(SQLiteParser.COMMA, 0); }
		public Error_messageContext error_message() {
			return getRuleContext(Error_messageContext.class,0);
		}
		public TerminalNode ROLLBACK_() { return getToken(SQLiteParser.ROLLBACK_, 0); }
		public TerminalNode ABORT_() { return getToken(SQLiteParser.ABORT_, 0); }
		public TerminalNode FAIL_() { return getToken(SQLiteParser.FAIL_, 0); }
		public Raise_functionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_raise_function; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterRaise_function(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitRaise_function(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitRaise_function(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Raise_functionContext raise_function() throws RecognitionException {
		Raise_functionContext _localctx = new Raise_functionContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_raise_function);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1090);
			match(RAISE_);
			setState(1091);
			match(OPEN_PAR);
			setState(1096);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,145,_ctx) ) {
			case 1:
				{
				setState(1092);
				match(IGNORE_);
				}
				break;
			case 2:
				{
				setState(1093);
				_la = _input.LA(1);
				if ( !(_la==ABORT_ || _la==FAIL_ || _la==ROLLBACK_) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1094);
				match(COMMA);
				setState(1095);
				error_message();
				}
				break;
			}
			setState(1098);
			match(CLOSE_PAR);
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
	public static class Literal_valueContext extends ParserRuleContext {
		public TerminalNode NUMERIC_LITERAL() { return getToken(SQLiteParser.NUMERIC_LITERAL, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(SQLiteParser.STRING_LITERAL, 0); }
		public TerminalNode BLOB_LITERAL() { return getToken(SQLiteParser.BLOB_LITERAL, 0); }
		public TerminalNode NULL_() { return getToken(SQLiteParser.NULL_, 0); }
		public TerminalNode TRUE_() { return getToken(SQLiteParser.TRUE_, 0); }
		public TerminalNode FALSE_() { return getToken(SQLiteParser.FALSE_, 0); }
		public TerminalNode CURRENT_TIME_() { return getToken(SQLiteParser.CURRENT_TIME_, 0); }
		public TerminalNode CURRENT_DATE_() { return getToken(SQLiteParser.CURRENT_DATE_, 0); }
		public TerminalNode CURRENT_TIMESTAMP_() { return getToken(SQLiteParser.CURRENT_TIMESTAMP_, 0); }
		public Literal_valueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterLiteral_value(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitLiteral_value(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitLiteral_value(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Literal_valueContext literal_value() throws RecognitionException {
		Literal_valueContext _localctx = new Literal_valueContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_literal_value);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1100);
			_la = _input.LA(1);
			if ( !(((((_la - 56)) & ~0x3f) == 0 && ((1L << (_la - 56)) & -9223372036846387193L) != 0) || ((((_la - 164)) & ~0x3f) == 0 && ((1L << (_la - 164)) & 1703937L) != 0)) ) {
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
	public static class Percentile_clauseContext extends ParserRuleContext {
		public TerminalNode WITHIN_() { return getToken(SQLiteParser.WITHIN_, 0); }
		public TerminalNode GROUP_() { return getToken(SQLiteParser.GROUP_, 0); }
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public TerminalNode ORDER_() { return getToken(SQLiteParser.ORDER_, 0); }
		public TerminalNode BY_() { return getToken(SQLiteParser.BY_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public Percentile_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_percentile_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterPercentile_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitPercentile_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitPercentile_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Percentile_clauseContext percentile_clause() throws RecognitionException {
		Percentile_clauseContext _localctx = new Percentile_clauseContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_percentile_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1102);
			match(WITHIN_);
			setState(1103);
			match(GROUP_);
			setState(1104);
			match(OPEN_PAR);
			setState(1105);
			match(ORDER_);
			setState(1106);
			match(BY_);
			setState(1107);
			expr();
			setState(1108);
			match(CLOSE_PAR);
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
	public static class Value_rowContext extends ParserRuleContext {
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Value_rowContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value_row; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterValue_row(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitValue_row(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitValue_row(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Value_rowContext value_row() throws RecognitionException {
		Value_rowContext _localctx = new Value_rowContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_value_row);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1110);
			match(OPEN_PAR);
			setState(1111);
			expr();
			setState(1116);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,146,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1112);
					match(COMMA);
					setState(1113);
					expr();
					}
					} 
				}
				setState(1118);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,146,_ctx);
			}
			setState(1119);
			match(CLOSE_PAR);
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
	public static class Values_clauseContext extends ParserRuleContext {
		public TerminalNode VALUES_() { return getToken(SQLiteParser.VALUES_, 0); }
		public List<Value_rowContext> value_row() {
			return getRuleContexts(Value_rowContext.class);
		}
		public Value_rowContext value_row(int i) {
			return getRuleContext(Value_rowContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Values_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_values_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterValues_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitValues_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitValues_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Values_clauseContext values_clause() throws RecognitionException {
		Values_clauseContext _localctx = new Values_clauseContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_values_clause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1121);
			match(VALUES_);
			setState(1122);
			value_row();
			setState(1127);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,147,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1123);
					match(COMMA);
					setState(1124);
					value_row();
					}
					} 
				}
				setState(1129);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,147,_ctx);
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
	public static class Insert_stmtContext extends ParserRuleContext {
		public TerminalNode INTO_() { return getToken(SQLiteParser.INTO_, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public TerminalNode INSERT_() { return getToken(SQLiteParser.INSERT_, 0); }
		public TerminalNode REPLACE_() { return getToken(SQLiteParser.REPLACE_, 0); }
		public TerminalNode OR_() { return getToken(SQLiteParser.OR_, 0); }
		public Select_stmtContext select_stmt() {
			return getRuleContext(Select_stmtContext.class,0);
		}
		public TerminalNode DEFAULT_() { return getToken(SQLiteParser.DEFAULT_, 0); }
		public TerminalNode VALUES_() { return getToken(SQLiteParser.VALUES_, 0); }
		public With_clauseContext with_clause() {
			return getRuleContext(With_clauseContext.class,0);
		}
		public TerminalNode ROLLBACK_() { return getToken(SQLiteParser.ROLLBACK_, 0); }
		public TerminalNode ABORT_() { return getToken(SQLiteParser.ABORT_, 0); }
		public TerminalNode FAIL_() { return getToken(SQLiteParser.FAIL_, 0); }
		public TerminalNode IGNORE_() { return getToken(SQLiteParser.IGNORE_, 0); }
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(SQLiteParser.DOT, 0); }
		public TerminalNode AS_() { return getToken(SQLiteParser.AS_, 0); }
		public Table_aliasContext table_alias() {
			return getRuleContext(Table_aliasContext.class,0);
		}
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public Returning_clauseContext returning_clause() {
			return getRuleContext(Returning_clauseContext.class,0);
		}
		public List<Upsert_clauseContext> upsert_clause() {
			return getRuleContexts(Upsert_clauseContext.class);
		}
		public Upsert_clauseContext upsert_clause(int i) {
			return getRuleContext(Upsert_clauseContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Insert_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insert_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterInsert_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitInsert_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitInsert_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Insert_stmtContext insert_stmt() throws RecognitionException {
		Insert_stmtContext _localctx = new Insert_stmtContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_insert_stmt);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1131);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,148,_ctx) ) {
			case 1:
				{
				setState(1130);
				with_clause();
				}
				break;
			}
			setState(1138);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,149,_ctx) ) {
			case 1:
				{
				setState(1133);
				match(INSERT_);
				}
				break;
			case 2:
				{
				setState(1134);
				match(REPLACE_);
				}
				break;
			case 3:
				{
				setState(1135);
				match(INSERT_);
				setState(1136);
				match(OR_);
				setState(1137);
				_la = _input.LA(1);
				if ( !(_la==ABORT_ || _la==FAIL_ || ((((_la - 93)) & ~0x3f) == 0 && ((1L << (_la - 93)) & 19140298416324609L) != 0)) ) {
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
			setState(1140);
			match(INTO_);
			setState(1144);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,150,_ctx) ) {
			case 1:
				{
				setState(1141);
				schema_name();
				setState(1142);
				match(DOT);
				}
				break;
			}
			setState(1146);
			table_name();
			setState(1149);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,151,_ctx) ) {
			case 1:
				{
				setState(1147);
				match(AS_);
				setState(1148);
				table_alias();
				}
				break;
			}
			setState(1162);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,153,_ctx) ) {
			case 1:
				{
				setState(1151);
				match(OPEN_PAR);
				setState(1152);
				column_name();
				setState(1157);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,152,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1153);
						match(COMMA);
						setState(1154);
						column_name();
						}
						} 
					}
					setState(1159);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,152,_ctx);
				}
				setState(1160);
				match(CLOSE_PAR);
				}
				break;
			}
			setState(1173);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,155,_ctx) ) {
			case 1:
				{
				setState(1164);
				select_stmt();
				setState(1168);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,154,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1165);
						upsert_clause();
						}
						} 
					}
					setState(1170);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,154,_ctx);
				}
				}
				break;
			case 2:
				{
				setState(1171);
				match(DEFAULT_);
				setState(1172);
				match(VALUES_);
				}
				break;
			}
			setState(1176);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,156,_ctx) ) {
			case 1:
				{
				setState(1175);
				returning_clause();
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
	public static class Returning_clauseContext extends ParserRuleContext {
		public TerminalNode RETURNING_() { return getToken(SQLiteParser.RETURNING_, 0); }
		public List<TerminalNode> STAR() { return getTokens(SQLiteParser.STAR); }
		public TerminalNode STAR(int i) {
			return getToken(SQLiteParser.STAR, i);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public List<Column_aliasContext> column_alias() {
			return getRuleContexts(Column_aliasContext.class);
		}
		public Column_aliasContext column_alias(int i) {
			return getRuleContext(Column_aliasContext.class,i);
		}
		public List<TerminalNode> AS_() { return getTokens(SQLiteParser.AS_); }
		public TerminalNode AS_(int i) {
			return getToken(SQLiteParser.AS_, i);
		}
		public Returning_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_returning_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterReturning_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitReturning_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitReturning_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Returning_clauseContext returning_clause() throws RecognitionException {
		Returning_clauseContext _localctx = new Returning_clauseContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_returning_clause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1178);
			match(RETURNING_);
			setState(1187);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,159,_ctx) ) {
			case 1:
				{
				setState(1179);
				match(STAR);
				}
				break;
			case 2:
				{
				setState(1180);
				expr();
				setState(1185);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,158,_ctx) ) {
				case 1:
					{
					setState(1182);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,157,_ctx) ) {
					case 1:
						{
						setState(1181);
						match(AS_);
						}
						break;
					}
					setState(1184);
					column_alias();
					}
					break;
				}
				}
				break;
			}
			setState(1202);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,163,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1189);
					match(COMMA);
					setState(1198);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,162,_ctx) ) {
					case 1:
						{
						setState(1190);
						match(STAR);
						}
						break;
					case 2:
						{
						setState(1191);
						expr();
						setState(1196);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,161,_ctx) ) {
						case 1:
							{
							setState(1193);
							_errHandler.sync(this);
							switch ( getInterpreter().adaptivePredict(_input,160,_ctx) ) {
							case 1:
								{
								setState(1192);
								match(AS_);
								}
								break;
							}
							setState(1195);
							column_alias();
							}
							break;
						}
						}
						break;
					}
					}
					} 
				}
				setState(1204);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,163,_ctx);
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
	public static class Upsert_clauseContext extends ParserRuleContext {
		public TerminalNode ON_() { return getToken(SQLiteParser.ON_, 0); }
		public TerminalNode CONFLICT_() { return getToken(SQLiteParser.CONFLICT_, 0); }
		public TerminalNode DO_() { return getToken(SQLiteParser.DO_, 0); }
		public TerminalNode NOTHING_() { return getToken(SQLiteParser.NOTHING_, 0); }
		public TerminalNode UPDATE_() { return getToken(SQLiteParser.UPDATE_, 0); }
		public TerminalNode SET_() { return getToken(SQLiteParser.SET_, 0); }
		public List<TerminalNode> ASSIGN() { return getTokens(SQLiteParser.ASSIGN); }
		public TerminalNode ASSIGN(int i) {
			return getToken(SQLiteParser.ASSIGN, i);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public List<Indexed_columnContext> indexed_column() {
			return getRuleContexts(Indexed_columnContext.class);
		}
		public Indexed_columnContext indexed_column(int i) {
			return getRuleContext(Indexed_columnContext.class,i);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public List<Column_name_listContext> column_name_list() {
			return getRuleContexts(Column_name_listContext.class);
		}
		public Column_name_listContext column_name_list(int i) {
			return getRuleContext(Column_name_listContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public List<TerminalNode> WHERE_() { return getTokens(SQLiteParser.WHERE_); }
		public TerminalNode WHERE_(int i) {
			return getToken(SQLiteParser.WHERE_, i);
		}
		public Upsert_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_upsert_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterUpsert_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitUpsert_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitUpsert_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Upsert_clauseContext upsert_clause() throws RecognitionException {
		Upsert_clauseContext _localctx = new Upsert_clauseContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_upsert_clause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1205);
			match(ON_);
			setState(1206);
			match(CONFLICT_);
			setState(1221);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,166,_ctx) ) {
			case 1:
				{
				setState(1207);
				match(OPEN_PAR);
				setState(1208);
				indexed_column();
				setState(1213);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,164,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1209);
						match(COMMA);
						setState(1210);
						indexed_column();
						}
						} 
					}
					setState(1215);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,164,_ctx);
				}
				setState(1216);
				match(CLOSE_PAR);
				setState(1219);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,165,_ctx) ) {
				case 1:
					{
					setState(1217);
					match(WHERE_);
					setState(1218);
					expr();
					}
					break;
				}
				}
				break;
			}
			setState(1223);
			match(DO_);
			setState(1250);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,171,_ctx) ) {
			case 1:
				{
				setState(1224);
				match(NOTHING_);
				}
				break;
			case 2:
				{
				setState(1225);
				match(UPDATE_);
				setState(1226);
				match(SET_);
				setState(1229);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,167,_ctx) ) {
				case 1:
					{
					setState(1227);
					column_name();
					}
					break;
				case 2:
					{
					setState(1228);
					column_name_list();
					}
					break;
				}
				setState(1231);
				match(ASSIGN);
				setState(1232);
				expr();
				setState(1243);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,169,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1233);
						match(COMMA);
						setState(1236);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,168,_ctx) ) {
						case 1:
							{
							setState(1234);
							column_name();
							}
							break;
						case 2:
							{
							setState(1235);
							column_name_list();
							}
							break;
						}
						setState(1238);
						match(ASSIGN);
						setState(1239);
						expr();
						}
						} 
					}
					setState(1245);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,169,_ctx);
				}
				setState(1248);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,170,_ctx) ) {
				case 1:
					{
					setState(1246);
					match(WHERE_);
					setState(1247);
					expr();
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
	public static class Pragma_stmtContext extends ParserRuleContext {
		public TerminalNode PRAGMA_() { return getToken(SQLiteParser.PRAGMA_, 0); }
		public Pragma_nameContext pragma_name() {
			return getRuleContext(Pragma_nameContext.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(SQLiteParser.DOT, 0); }
		public TerminalNode ASSIGN() { return getToken(SQLiteParser.ASSIGN, 0); }
		public Pragma_valueContext pragma_value() {
			return getRuleContext(Pragma_valueContext.class,0);
		}
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public Pragma_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pragma_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterPragma_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitPragma_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitPragma_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Pragma_stmtContext pragma_stmt() throws RecognitionException {
		Pragma_stmtContext _localctx = new Pragma_stmtContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_pragma_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1252);
			match(PRAGMA_);
			setState(1256);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,172,_ctx) ) {
			case 1:
				{
				setState(1253);
				schema_name();
				setState(1254);
				match(DOT);
				}
				break;
			}
			setState(1258);
			pragma_name();
			setState(1265);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,173,_ctx) ) {
			case 1:
				{
				setState(1259);
				match(ASSIGN);
				setState(1260);
				pragma_value();
				}
				break;
			case 2:
				{
				setState(1261);
				match(OPEN_PAR);
				setState(1262);
				pragma_value();
				setState(1263);
				match(CLOSE_PAR);
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
	public static class Pragma_valueContext extends ParserRuleContext {
		public Signed_numberContext signed_number() {
			return getRuleContext(Signed_numberContext.class,0);
		}
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TerminalNode STRING_LITERAL() { return getToken(SQLiteParser.STRING_LITERAL, 0); }
		public Pragma_valueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pragma_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterPragma_value(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitPragma_value(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitPragma_value(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Pragma_valueContext pragma_value() throws RecognitionException {
		Pragma_valueContext _localctx = new Pragma_valueContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_pragma_value);
		try {
			setState(1270);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,174,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1267);
				signed_number();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1268);
				name();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1269);
				match(STRING_LITERAL);
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
	public static class Reindex_stmtContext extends ParserRuleContext {
		public TerminalNode REINDEX_() { return getToken(SQLiteParser.REINDEX_, 0); }
		public Collation_nameContext collation_name() {
			return getRuleContext(Collation_nameContext.class,0);
		}
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public Index_nameContext index_name() {
			return getRuleContext(Index_nameContext.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(SQLiteParser.DOT, 0); }
		public Reindex_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reindex_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterReindex_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitReindex_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitReindex_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Reindex_stmtContext reindex_stmt() throws RecognitionException {
		Reindex_stmtContext _localctx = new Reindex_stmtContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_reindex_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1272);
			match(REINDEX_);
			setState(1283);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,177,_ctx) ) {
			case 1:
				{
				setState(1273);
				collation_name();
				}
				break;
			case 2:
				{
				setState(1277);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,175,_ctx) ) {
				case 1:
					{
					setState(1274);
					schema_name();
					setState(1275);
					match(DOT);
					}
					break;
				}
				setState(1281);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,176,_ctx) ) {
				case 1:
					{
					setState(1279);
					table_name();
					}
					break;
				case 2:
					{
					setState(1280);
					index_name();
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
	public static class Select_stmtContext extends ParserRuleContext {
		public List<Select_coreContext> select_core() {
			return getRuleContexts(Select_coreContext.class);
		}
		public Select_coreContext select_core(int i) {
			return getRuleContext(Select_coreContext.class,i);
		}
		public With_clauseContext with_clause() {
			return getRuleContext(With_clauseContext.class,0);
		}
		public List<Compound_operatorContext> compound_operator() {
			return getRuleContexts(Compound_operatorContext.class);
		}
		public Compound_operatorContext compound_operator(int i) {
			return getRuleContext(Compound_operatorContext.class,i);
		}
		public Order_clauseContext order_clause() {
			return getRuleContext(Order_clauseContext.class,0);
		}
		public Limit_clauseContext limit_clause() {
			return getRuleContext(Limit_clauseContext.class,0);
		}
		public Select_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterSelect_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitSelect_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitSelect_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Select_stmtContext select_stmt() throws RecognitionException {
		Select_stmtContext _localctx = new Select_stmtContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_select_stmt);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1286);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,178,_ctx) ) {
			case 1:
				{
				setState(1285);
				with_clause();
				}
				break;
			}
			setState(1288);
			select_core();
			setState(1294);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,179,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1289);
					compound_operator();
					setState(1290);
					select_core();
					}
					} 
				}
				setState(1296);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,179,_ctx);
			}
			setState(1298);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,180,_ctx) ) {
			case 1:
				{
				setState(1297);
				order_clause();
				}
				break;
			}
			setState(1301);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,181,_ctx) ) {
			case 1:
				{
				setState(1300);
				limit_clause();
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
	public static class Join_clauseContext extends ParserRuleContext {
		public List<Table_or_subqueryContext> table_or_subquery() {
			return getRuleContexts(Table_or_subqueryContext.class);
		}
		public Table_or_subqueryContext table_or_subquery(int i) {
			return getRuleContext(Table_or_subqueryContext.class,i);
		}
		public List<Join_operatorContext> join_operator() {
			return getRuleContexts(Join_operatorContext.class);
		}
		public Join_operatorContext join_operator(int i) {
			return getRuleContext(Join_operatorContext.class,i);
		}
		public List<Join_constraintContext> join_constraint() {
			return getRuleContexts(Join_constraintContext.class);
		}
		public Join_constraintContext join_constraint(int i) {
			return getRuleContext(Join_constraintContext.class,i);
		}
		public Join_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_join_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterJoin_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitJoin_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitJoin_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Join_clauseContext join_clause() throws RecognitionException {
		Join_clauseContext _localctx = new Join_clauseContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_join_clause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1303);
			table_or_subquery();
			setState(1311);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,183,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1304);
					join_operator();
					setState(1305);
					table_or_subquery();
					setState(1307);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,182,_ctx) ) {
					case 1:
						{
						setState(1306);
						join_constraint();
						}
						break;
					}
					}
					} 
				}
				setState(1313);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,183,_ctx);
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
	public static class Select_coreContext extends ParserRuleContext {
		public ExprContext where_expr;
		public ExprContext expr;
		public List<ExprContext> group_by_expr = new ArrayList<ExprContext>();
		public ExprContext having_expr;
		public TerminalNode SELECT_() { return getToken(SQLiteParser.SELECT_, 0); }
		public List<Result_columnContext> result_column() {
			return getRuleContexts(Result_columnContext.class);
		}
		public Result_columnContext result_column(int i) {
			return getRuleContext(Result_columnContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public TerminalNode FROM_() { return getToken(SQLiteParser.FROM_, 0); }
		public Join_clauseContext join_clause() {
			return getRuleContext(Join_clauseContext.class,0);
		}
		public TerminalNode WHERE_() { return getToken(SQLiteParser.WHERE_, 0); }
		public TerminalNode GROUP_() { return getToken(SQLiteParser.GROUP_, 0); }
		public TerminalNode BY_() { return getToken(SQLiteParser.BY_, 0); }
		public TerminalNode WINDOW_() { return getToken(SQLiteParser.WINDOW_, 0); }
		public List<Window_nameContext> window_name() {
			return getRuleContexts(Window_nameContext.class);
		}
		public Window_nameContext window_name(int i) {
			return getRuleContext(Window_nameContext.class,i);
		}
		public List<TerminalNode> AS_() { return getTokens(SQLiteParser.AS_); }
		public TerminalNode AS_(int i) {
			return getToken(SQLiteParser.AS_, i);
		}
		public List<Window_defnContext> window_defn() {
			return getRuleContexts(Window_defnContext.class);
		}
		public Window_defnContext window_defn(int i) {
			return getRuleContext(Window_defnContext.class,i);
		}
		public TerminalNode DISTINCT_() { return getToken(SQLiteParser.DISTINCT_, 0); }
		public TerminalNode ALL_() { return getToken(SQLiteParser.ALL_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode HAVING_() { return getToken(SQLiteParser.HAVING_, 0); }
		public Values_clauseContext values_clause() {
			return getRuleContext(Values_clauseContext.class,0);
		}
		public Select_coreContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select_core; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterSelect_core(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitSelect_core(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitSelect_core(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Select_coreContext select_core() throws RecognitionException {
		Select_coreContext _localctx = new Select_coreContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_select_core);
		int _la;
		try {
			int _alt;
			setState(1367);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,193,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1314);
				match(SELECT_);
				setState(1316);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,184,_ctx) ) {
				case 1:
					{
					setState(1315);
					_la = _input.LA(1);
					if ( !(_la==ALL_ || _la==DISTINCT_) ) {
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
				setState(1318);
				result_column();
				setState(1323);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,185,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1319);
						match(COMMA);
						setState(1320);
						result_column();
						}
						} 
					}
					setState(1325);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,185,_ctx);
				}
				setState(1328);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,186,_ctx) ) {
				case 1:
					{
					setState(1326);
					match(FROM_);
					setState(1327);
					join_clause();
					}
					break;
				}
				setState(1332);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,187,_ctx) ) {
				case 1:
					{
					setState(1330);
					match(WHERE_);
					setState(1331);
					((Select_coreContext)_localctx).where_expr = expr();
					}
					break;
				}
				setState(1348);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,190,_ctx) ) {
				case 1:
					{
					setState(1334);
					match(GROUP_);
					setState(1335);
					match(BY_);
					setState(1336);
					((Select_coreContext)_localctx).expr = expr();
					((Select_coreContext)_localctx).group_by_expr.add(((Select_coreContext)_localctx).expr);
					setState(1341);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,188,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(1337);
							match(COMMA);
							setState(1338);
							((Select_coreContext)_localctx).expr = expr();
							((Select_coreContext)_localctx).group_by_expr.add(((Select_coreContext)_localctx).expr);
							}
							} 
						}
						setState(1343);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,188,_ctx);
					}
					setState(1346);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,189,_ctx) ) {
					case 1:
						{
						setState(1344);
						match(HAVING_);
						setState(1345);
						((Select_coreContext)_localctx).having_expr = expr();
						}
						break;
					}
					}
					break;
				}
				setState(1364);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,192,_ctx) ) {
				case 1:
					{
					setState(1350);
					match(WINDOW_);
					setState(1351);
					window_name();
					setState(1352);
					match(AS_);
					setState(1353);
					window_defn();
					setState(1361);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,191,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(1354);
							match(COMMA);
							setState(1355);
							window_name();
							setState(1356);
							match(AS_);
							setState(1357);
							window_defn();
							}
							} 
						}
						setState(1363);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,191,_ctx);
					}
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1366);
				values_clause();
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
	public static class Table_or_subqueryContext extends ParserRuleContext {
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(SQLiteParser.DOT, 0); }
		public TerminalNode AS_() { return getToken(SQLiteParser.AS_, 0); }
		public Table_aliasContext table_alias() {
			return getRuleContext(Table_aliasContext.class,0);
		}
		public Table_alias_excluding_joinsContext table_alias_excluding_joins() {
			return getRuleContext(Table_alias_excluding_joinsContext.class,0);
		}
		public TerminalNode INDEXED_() { return getToken(SQLiteParser.INDEXED_, 0); }
		public TerminalNode BY_() { return getToken(SQLiteParser.BY_, 0); }
		public Index_nameContext index_name() {
			return getRuleContext(Index_nameContext.class,0);
		}
		public TerminalNode NOT_() { return getToken(SQLiteParser.NOT_, 0); }
		public Table_function_nameContext table_function_name() {
			return getRuleContext(Table_function_nameContext.class,0);
		}
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Join_clauseContext join_clause() {
			return getRuleContext(Join_clauseContext.class,0);
		}
		public Select_stmtContext select_stmt() {
			return getRuleContext(Select_stmtContext.class,0);
		}
		public Table_or_subqueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_or_subquery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterTable_or_subquery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitTable_or_subquery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitTable_or_subquery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Table_or_subqueryContext table_or_subquery() throws RecognitionException {
		Table_or_subqueryContext _localctx = new Table_or_subqueryContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_table_or_subquery);
		try {
			int _alt;
			setState(1422);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,203,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1372);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,194,_ctx) ) {
				case 1:
					{
					setState(1369);
					schema_name();
					setState(1370);
					match(DOT);
					}
					break;
				}
				setState(1374);
				table_name();
				setState(1378);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,195,_ctx) ) {
				case 1:
					{
					setState(1375);
					match(AS_);
					setState(1376);
					table_alias();
					}
					break;
				case 2:
					{
					setState(1377);
					table_alias_excluding_joins();
					}
					break;
				}
				setState(1385);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,196,_ctx) ) {
				case 1:
					{
					setState(1380);
					match(INDEXED_);
					setState(1381);
					match(BY_);
					setState(1382);
					index_name();
					}
					break;
				case 2:
					{
					setState(1383);
					match(NOT_);
					setState(1384);
					match(INDEXED_);
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1390);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,197,_ctx) ) {
				case 1:
					{
					setState(1387);
					schema_name();
					setState(1388);
					match(DOT);
					}
					break;
				}
				setState(1392);
				table_function_name();
				setState(1393);
				match(OPEN_PAR);
				setState(1394);
				expr();
				setState(1399);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,198,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1395);
						match(COMMA);
						setState(1396);
						expr();
						}
						} 
					}
					setState(1401);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,198,_ctx);
				}
				setState(1402);
				match(CLOSE_PAR);
				setState(1407);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,200,_ctx) ) {
				case 1:
					{
					setState(1404);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,199,_ctx) ) {
					case 1:
						{
						setState(1403);
						match(AS_);
						}
						break;
					}
					setState(1406);
					table_alias();
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1409);
				match(OPEN_PAR);
				setState(1410);
				join_clause();
				setState(1411);
				match(CLOSE_PAR);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1413);
				match(OPEN_PAR);
				setState(1414);
				select_stmt();
				setState(1415);
				match(CLOSE_PAR);
				setState(1420);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,202,_ctx) ) {
				case 1:
					{
					setState(1417);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,201,_ctx) ) {
					case 1:
						{
						setState(1416);
						match(AS_);
						}
						break;
					}
					setState(1419);
					table_alias();
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
	public static class Result_columnContext extends ParserRuleContext {
		public TerminalNode STAR() { return getToken(SQLiteParser.STAR, 0); }
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(SQLiteParser.DOT, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public Column_aliasContext column_alias() {
			return getRuleContext(Column_aliasContext.class,0);
		}
		public TerminalNode AS_() { return getToken(SQLiteParser.AS_, 0); }
		public Result_columnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_result_column; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterResult_column(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitResult_column(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitResult_column(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Result_columnContext result_column() throws RecognitionException {
		Result_columnContext _localctx = new Result_columnContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_result_column);
		try {
			setState(1436);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,206,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1424);
				match(STAR);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1425);
				table_name();
				setState(1426);
				match(DOT);
				setState(1427);
				match(STAR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1429);
				expr();
				setState(1434);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,205,_ctx) ) {
				case 1:
					{
					setState(1431);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,204,_ctx) ) {
					case 1:
						{
						setState(1430);
						match(AS_);
						}
						break;
					}
					setState(1433);
					column_alias();
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
	public static class Join_operatorContext extends ParserRuleContext {
		public TerminalNode COMMA() { return getToken(SQLiteParser.COMMA, 0); }
		public TerminalNode JOIN_() { return getToken(SQLiteParser.JOIN_, 0); }
		public TerminalNode NATURAL_() { return getToken(SQLiteParser.NATURAL_, 0); }
		public TerminalNode INNER_() { return getToken(SQLiteParser.INNER_, 0); }
		public TerminalNode CROSS_() { return getToken(SQLiteParser.CROSS_, 0); }
		public TerminalNode LEFT_() { return getToken(SQLiteParser.LEFT_, 0); }
		public TerminalNode RIGHT_() { return getToken(SQLiteParser.RIGHT_, 0); }
		public TerminalNode FULL_() { return getToken(SQLiteParser.FULL_, 0); }
		public TerminalNode OUTER_() { return getToken(SQLiteParser.OUTER_, 0); }
		public Join_operatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_join_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterJoin_operator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitJoin_operator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitJoin_operator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Join_operatorContext join_operator() throws RecognitionException {
		Join_operatorContext _localctx = new Join_operatorContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_join_operator);
		int _la;
		try {
			setState(1451);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,210,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1438);
				match(COMMA);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1440);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,207,_ctx) ) {
				case 1:
					{
					setState(1439);
					match(NATURAL_);
					}
					break;
				}
				setState(1448);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,209,_ctx) ) {
				case 1:
					{
					setState(1442);
					_la = _input.LA(1);
					if ( !(((((_la - 86)) & ~0x3f) == 0 && ((1L << (_la - 86)) & 1152921504615235585L) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(1444);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,208,_ctx) ) {
					case 1:
						{
						setState(1443);
						match(OUTER_);
						}
						break;
					}
					}
					break;
				case 2:
					{
					setState(1446);
					match(INNER_);
					}
					break;
				case 3:
					{
					setState(1447);
					match(CROSS_);
					}
					break;
				}
				setState(1450);
				match(JOIN_);
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
	public static class Join_constraintContext extends ParserRuleContext {
		public TerminalNode ON_() { return getToken(SQLiteParser.ON_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode USING_() { return getToken(SQLiteParser.USING_, 0); }
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Join_constraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_join_constraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterJoin_constraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitJoin_constraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitJoin_constraint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Join_constraintContext join_constraint() throws RecognitionException {
		Join_constraintContext _localctx = new Join_constraintContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_join_constraint);
		try {
			int _alt;
			setState(1467);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,212,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1453);
				match(ON_);
				setState(1454);
				expr();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1455);
				match(USING_);
				setState(1456);
				match(OPEN_PAR);
				setState(1457);
				column_name();
				setState(1462);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,211,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1458);
						match(COMMA);
						setState(1459);
						column_name();
						}
						} 
					}
					setState(1464);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,211,_ctx);
				}
				setState(1465);
				match(CLOSE_PAR);
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
	public static class Compound_operatorContext extends ParserRuleContext {
		public TerminalNode UNION_() { return getToken(SQLiteParser.UNION_, 0); }
		public TerminalNode ALL_() { return getToken(SQLiteParser.ALL_, 0); }
		public TerminalNode INTERSECT_() { return getToken(SQLiteParser.INTERSECT_, 0); }
		public TerminalNode EXCEPT_() { return getToken(SQLiteParser.EXCEPT_, 0); }
		public Compound_operatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compound_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterCompound_operator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitCompound_operator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitCompound_operator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Compound_operatorContext compound_operator() throws RecognitionException {
		Compound_operatorContext _localctx = new Compound_operatorContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_compound_operator);
		try {
			setState(1475);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,214,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1469);
				match(UNION_);
				setState(1471);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,213,_ctx) ) {
				case 1:
					{
					setState(1470);
					match(ALL_);
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1473);
				match(INTERSECT_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1474);
				match(EXCEPT_);
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
	public static class Update_stmtContext extends ParserRuleContext {
		public TerminalNode UPDATE_() { return getToken(SQLiteParser.UPDATE_, 0); }
		public Qualified_table_nameContext qualified_table_name() {
			return getRuleContext(Qualified_table_nameContext.class,0);
		}
		public TerminalNode SET_() { return getToken(SQLiteParser.SET_, 0); }
		public List<TerminalNode> ASSIGN() { return getTokens(SQLiteParser.ASSIGN); }
		public TerminalNode ASSIGN(int i) {
			return getToken(SQLiteParser.ASSIGN, i);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public List<Column_name_listContext> column_name_list() {
			return getRuleContexts(Column_name_listContext.class);
		}
		public Column_name_listContext column_name_list(int i) {
			return getRuleContext(Column_name_listContext.class,i);
		}
		public With_clauseContext with_clause() {
			return getRuleContext(With_clauseContext.class,0);
		}
		public TerminalNode OR_() { return getToken(SQLiteParser.OR_, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public TerminalNode FROM_() { return getToken(SQLiteParser.FROM_, 0); }
		public Join_clauseContext join_clause() {
			return getRuleContext(Join_clauseContext.class,0);
		}
		public TerminalNode WHERE_() { return getToken(SQLiteParser.WHERE_, 0); }
		public Returning_clauseContext returning_clause() {
			return getRuleContext(Returning_clauseContext.class,0);
		}
		public Order_clauseContext order_clause() {
			return getRuleContext(Order_clauseContext.class,0);
		}
		public Limit_clauseContext limit_clause() {
			return getRuleContext(Limit_clauseContext.class,0);
		}
		public TerminalNode ROLLBACK_() { return getToken(SQLiteParser.ROLLBACK_, 0); }
		public TerminalNode ABORT_() { return getToken(SQLiteParser.ABORT_, 0); }
		public TerminalNode REPLACE_() { return getToken(SQLiteParser.REPLACE_, 0); }
		public TerminalNode FAIL_() { return getToken(SQLiteParser.FAIL_, 0); }
		public TerminalNode IGNORE_() { return getToken(SQLiteParser.IGNORE_, 0); }
		public Update_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_update_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterUpdate_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitUpdate_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitUpdate_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Update_stmtContext update_stmt() throws RecognitionException {
		Update_stmtContext _localctx = new Update_stmtContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_update_stmt);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1478);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,215,_ctx) ) {
			case 1:
				{
				setState(1477);
				with_clause();
				}
				break;
			}
			setState(1480);
			match(UPDATE_);
			setState(1483);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,216,_ctx) ) {
			case 1:
				{
				setState(1481);
				match(OR_);
				setState(1482);
				_la = _input.LA(1);
				if ( !(_la==ABORT_ || _la==FAIL_ || ((((_la - 93)) & ~0x3f) == 0 && ((1L << (_la - 93)) & 19140298416324609L) != 0)) ) {
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
			setState(1485);
			qualified_table_name();
			setState(1486);
			match(SET_);
			setState(1489);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,217,_ctx) ) {
			case 1:
				{
				setState(1487);
				column_name();
				}
				break;
			case 2:
				{
				setState(1488);
				column_name_list();
				}
				break;
			}
			setState(1491);
			match(ASSIGN);
			setState(1492);
			expr();
			setState(1503);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,219,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1493);
					match(COMMA);
					setState(1496);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,218,_ctx) ) {
					case 1:
						{
						setState(1494);
						column_name();
						}
						break;
					case 2:
						{
						setState(1495);
						column_name_list();
						}
						break;
					}
					setState(1498);
					match(ASSIGN);
					setState(1499);
					expr();
					}
					} 
				}
				setState(1505);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,219,_ctx);
			}
			setState(1508);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,220,_ctx) ) {
			case 1:
				{
				setState(1506);
				match(FROM_);
				setState(1507);
				join_clause();
				}
				break;
			}
			setState(1512);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,221,_ctx) ) {
			case 1:
				{
				setState(1510);
				match(WHERE_);
				setState(1511);
				expr();
				}
				break;
			}
			setState(1515);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,222,_ctx) ) {
			case 1:
				{
				setState(1514);
				returning_clause();
				}
				break;
			}
			setState(1518);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,223,_ctx) ) {
			case 1:
				{
				setState(1517);
				order_clause();
				}
				break;
			}
			setState(1521);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,224,_ctx) ) {
			case 1:
				{
				setState(1520);
				limit_clause();
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
	public static class Column_name_listContext extends ParserRuleContext {
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public List<Column_nameContext> column_name() {
			return getRuleContexts(Column_nameContext.class);
		}
		public Column_nameContext column_name(int i) {
			return getRuleContext(Column_nameContext.class,i);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Column_name_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_name_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterColumn_name_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitColumn_name_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitColumn_name_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_name_listContext column_name_list() throws RecognitionException {
		Column_name_listContext _localctx = new Column_name_listContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_column_name_list);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1523);
			match(OPEN_PAR);
			setState(1524);
			column_name();
			setState(1529);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,225,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1525);
					match(COMMA);
					setState(1526);
					column_name();
					}
					} 
				}
				setState(1531);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,225,_ctx);
			}
			setState(1532);
			match(CLOSE_PAR);
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
	public static class Qualified_table_nameContext extends ParserRuleContext {
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode DOT() { return getToken(SQLiteParser.DOT, 0); }
		public TerminalNode AS_() { return getToken(SQLiteParser.AS_, 0); }
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public TerminalNode INDEXED_() { return getToken(SQLiteParser.INDEXED_, 0); }
		public TerminalNode BY_() { return getToken(SQLiteParser.BY_, 0); }
		public Index_nameContext index_name() {
			return getRuleContext(Index_nameContext.class,0);
		}
		public TerminalNode NOT_() { return getToken(SQLiteParser.NOT_, 0); }
		public Qualified_table_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualified_table_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterQualified_table_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitQualified_table_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitQualified_table_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Qualified_table_nameContext qualified_table_name() throws RecognitionException {
		Qualified_table_nameContext _localctx = new Qualified_table_nameContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_qualified_table_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1537);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,226,_ctx) ) {
			case 1:
				{
				setState(1534);
				schema_name();
				setState(1535);
				match(DOT);
				}
				break;
			}
			setState(1539);
			table_name();
			setState(1542);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,227,_ctx) ) {
			case 1:
				{
				setState(1540);
				match(AS_);
				setState(1541);
				alias();
				}
				break;
			}
			setState(1549);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,228,_ctx) ) {
			case 1:
				{
				setState(1544);
				match(INDEXED_);
				setState(1545);
				match(BY_);
				setState(1546);
				index_name();
				}
				break;
			case 2:
				{
				setState(1547);
				match(NOT_);
				setState(1548);
				match(INDEXED_);
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
	public static class Vacuum_stmtContext extends ParserRuleContext {
		public TerminalNode VACUUM_() { return getToken(SQLiteParser.VACUUM_, 0); }
		public Schema_nameContext schema_name() {
			return getRuleContext(Schema_nameContext.class,0);
		}
		public TerminalNode INTO_() { return getToken(SQLiteParser.INTO_, 0); }
		public FilenameContext filename() {
			return getRuleContext(FilenameContext.class,0);
		}
		public Vacuum_stmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vacuum_stmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterVacuum_stmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitVacuum_stmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitVacuum_stmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Vacuum_stmtContext vacuum_stmt() throws RecognitionException {
		Vacuum_stmtContext _localctx = new Vacuum_stmtContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_vacuum_stmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1551);
			match(VACUUM_);
			setState(1553);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,229,_ctx) ) {
			case 1:
				{
				setState(1552);
				schema_name();
				}
				break;
			}
			setState(1557);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,230,_ctx) ) {
			case 1:
				{
				setState(1555);
				match(INTO_);
				setState(1556);
				filename();
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
	public static class Filter_clauseContext extends ParserRuleContext {
		public TerminalNode FILTER_() { return getToken(SQLiteParser.FILTER_, 0); }
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public TerminalNode WHERE_() { return getToken(SQLiteParser.WHERE_, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public Filter_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filter_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterFilter_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitFilter_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitFilter_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Filter_clauseContext filter_clause() throws RecognitionException {
		Filter_clauseContext _localctx = new Filter_clauseContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_filter_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1559);
			match(FILTER_);
			setState(1560);
			match(OPEN_PAR);
			setState(1561);
			match(WHERE_);
			setState(1562);
			expr();
			setState(1563);
			match(CLOSE_PAR);
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
	public static class Window_defnContext extends ParserRuleContext {
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public Base_window_nameContext base_window_name() {
			return getRuleContext(Base_window_nameContext.class,0);
		}
		public TerminalNode PARTITION_() { return getToken(SQLiteParser.PARTITION_, 0); }
		public TerminalNode BY_() { return getToken(SQLiteParser.BY_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public Order_clauseContext order_clause() {
			return getRuleContext(Order_clauseContext.class,0);
		}
		public Frame_specContext frame_spec() {
			return getRuleContext(Frame_specContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Window_defnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_window_defn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterWindow_defn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitWindow_defn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitWindow_defn(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Window_defnContext window_defn() throws RecognitionException {
		Window_defnContext _localctx = new Window_defnContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_window_defn);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1565);
			match(OPEN_PAR);
			setState(1567);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,231,_ctx) ) {
			case 1:
				{
				setState(1566);
				base_window_name();
				}
				break;
			}
			setState(1579);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,233,_ctx) ) {
			case 1:
				{
				setState(1569);
				match(PARTITION_);
				setState(1570);
				match(BY_);
				setState(1571);
				expr();
				setState(1576);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,232,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1572);
						match(COMMA);
						setState(1573);
						expr();
						}
						} 
					}
					setState(1578);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,232,_ctx);
				}
				}
				break;
			}
			setState(1582);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,234,_ctx) ) {
			case 1:
				{
				setState(1581);
				order_clause();
				}
				break;
			}
			setState(1585);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,235,_ctx) ) {
			case 1:
				{
				setState(1584);
				frame_spec();
				}
				break;
			}
			setState(1587);
			match(CLOSE_PAR);
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
	public static class Over_clauseContext extends ParserRuleContext {
		public TerminalNode OVER_() { return getToken(SQLiteParser.OVER_, 0); }
		public Window_nameContext window_name() {
			return getRuleContext(Window_nameContext.class,0);
		}
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public Base_window_nameContext base_window_name() {
			return getRuleContext(Base_window_nameContext.class,0);
		}
		public TerminalNode PARTITION_() { return getToken(SQLiteParser.PARTITION_, 0); }
		public TerminalNode BY_() { return getToken(SQLiteParser.BY_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public Order_clauseContext order_clause() {
			return getRuleContext(Order_clauseContext.class,0);
		}
		public Frame_specContext frame_spec() {
			return getRuleContext(Frame_specContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Over_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_over_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterOver_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitOver_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitOver_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Over_clauseContext over_clause() throws RecognitionException {
		Over_clauseContext _localctx = new Over_clauseContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_over_clause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1589);
			match(OVER_);
			setState(1614);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,241,_ctx) ) {
			case 1:
				{
				setState(1590);
				window_name();
				}
				break;
			case 2:
				{
				setState(1591);
				match(OPEN_PAR);
				setState(1593);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,236,_ctx) ) {
				case 1:
					{
					setState(1592);
					base_window_name();
					}
					break;
				}
				setState(1605);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,238,_ctx) ) {
				case 1:
					{
					setState(1595);
					match(PARTITION_);
					setState(1596);
					match(BY_);
					setState(1597);
					expr();
					setState(1602);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,237,_ctx);
					while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
						if ( _alt==1 ) {
							{
							{
							setState(1598);
							match(COMMA);
							setState(1599);
							expr();
							}
							} 
						}
						setState(1604);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,237,_ctx);
					}
					}
					break;
				}
				setState(1608);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,239,_ctx) ) {
				case 1:
					{
					setState(1607);
					order_clause();
					}
					break;
				}
				setState(1611);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,240,_ctx) ) {
				case 1:
					{
					setState(1610);
					frame_spec();
					}
					break;
				}
				setState(1613);
				match(CLOSE_PAR);
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
	public static class Frame_specContext extends ParserRuleContext {
		public Frame_clauseContext frame_clause() {
			return getRuleContext(Frame_clauseContext.class,0);
		}
		public TerminalNode EXCLUDE_() { return getToken(SQLiteParser.EXCLUDE_, 0); }
		public TerminalNode NO_() { return getToken(SQLiteParser.NO_, 0); }
		public TerminalNode OTHERS_() { return getToken(SQLiteParser.OTHERS_, 0); }
		public TerminalNode CURRENT_() { return getToken(SQLiteParser.CURRENT_, 0); }
		public TerminalNode ROW_() { return getToken(SQLiteParser.ROW_, 0); }
		public TerminalNode GROUP_() { return getToken(SQLiteParser.GROUP_, 0); }
		public TerminalNode TIES_() { return getToken(SQLiteParser.TIES_, 0); }
		public Frame_specContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frame_spec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterFrame_spec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitFrame_spec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitFrame_spec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Frame_specContext frame_spec() throws RecognitionException {
		Frame_specContext _localctx = new Frame_specContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_frame_spec);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1616);
			frame_clause();
			setState(1626);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,243,_ctx) ) {
			case 1:
				{
				setState(1617);
				match(EXCLUDE_);
				setState(1624);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,242,_ctx) ) {
				case 1:
					{
					setState(1618);
					match(NO_);
					setState(1619);
					match(OTHERS_);
					}
					break;
				case 2:
					{
					setState(1620);
					match(CURRENT_);
					setState(1621);
					match(ROW_);
					}
					break;
				case 3:
					{
					setState(1622);
					match(GROUP_);
					}
					break;
				case 4:
					{
					setState(1623);
					match(TIES_);
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
	public static class Frame_clauseContext extends ParserRuleContext {
		public TerminalNode RANGE_() { return getToken(SQLiteParser.RANGE_, 0); }
		public TerminalNode ROWS_() { return getToken(SQLiteParser.ROWS_, 0); }
		public TerminalNode GROUPS_() { return getToken(SQLiteParser.GROUPS_, 0); }
		public Frame_singleContext frame_single() {
			return getRuleContext(Frame_singleContext.class,0);
		}
		public TerminalNode BETWEEN_() { return getToken(SQLiteParser.BETWEEN_, 0); }
		public Frame_leftContext frame_left() {
			return getRuleContext(Frame_leftContext.class,0);
		}
		public TerminalNode AND_() { return getToken(SQLiteParser.AND_, 0); }
		public Frame_rightContext frame_right() {
			return getRuleContext(Frame_rightContext.class,0);
		}
		public Frame_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frame_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterFrame_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitFrame_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitFrame_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Frame_clauseContext frame_clause() throws RecognitionException {
		Frame_clauseContext _localctx = new Frame_clauseContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_frame_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1628);
			_la = _input.LA(1);
			if ( !(((((_la - 90)) & ~0x3f) == 0 && ((1L << (_la - 90)) & 1152991873351024641L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(1635);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,244,_ctx) ) {
			case 1:
				{
				setState(1629);
				frame_single();
				}
				break;
			case 2:
				{
				setState(1630);
				match(BETWEEN_);
				setState(1631);
				frame_left();
				setState(1632);
				match(AND_);
				setState(1633);
				frame_right();
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
	public static class Order_clauseContext extends ParserRuleContext {
		public TerminalNode ORDER_() { return getToken(SQLiteParser.ORDER_, 0); }
		public TerminalNode BY_() { return getToken(SQLiteParser.BY_, 0); }
		public List<Ordering_termContext> ordering_term() {
			return getRuleContexts(Ordering_termContext.class);
		}
		public Ordering_termContext ordering_term(int i) {
			return getRuleContext(Ordering_termContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SQLiteParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SQLiteParser.COMMA, i);
		}
		public Order_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_order_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterOrder_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitOrder_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitOrder_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Order_clauseContext order_clause() throws RecognitionException {
		Order_clauseContext _localctx = new Order_clauseContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_order_clause);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1637);
			match(ORDER_);
			setState(1638);
			match(BY_);
			setState(1639);
			ordering_term();
			setState(1644);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,245,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1640);
					match(COMMA);
					setState(1641);
					ordering_term();
					}
					} 
				}
				setState(1646);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,245,_ctx);
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
	public static class Limit_clauseContext extends ParserRuleContext {
		public TerminalNode LIMIT_() { return getToken(SQLiteParser.LIMIT_, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode OFFSET_() { return getToken(SQLiteParser.OFFSET_, 0); }
		public TerminalNode COMMA() { return getToken(SQLiteParser.COMMA, 0); }
		public Limit_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_limit_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterLimit_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitLimit_clause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitLimit_clause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Limit_clauseContext limit_clause() throws RecognitionException {
		Limit_clauseContext _localctx = new Limit_clauseContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_limit_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1647);
			match(LIMIT_);
			setState(1648);
			expr();
			setState(1651);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,246,_ctx) ) {
			case 1:
				{
				setState(1649);
				_la = _input.LA(1);
				if ( !(_la==COMMA || _la==OFFSET_) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1650);
				expr();
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
	public static class Ordering_termContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode COLLATE_() { return getToken(SQLiteParser.COLLATE_, 0); }
		public Collation_nameContext collation_name() {
			return getRuleContext(Collation_nameContext.class,0);
		}
		public Asc_descContext asc_desc() {
			return getRuleContext(Asc_descContext.class,0);
		}
		public TerminalNode NULLS_() { return getToken(SQLiteParser.NULLS_, 0); }
		public TerminalNode FIRST_() { return getToken(SQLiteParser.FIRST_, 0); }
		public TerminalNode LAST_() { return getToken(SQLiteParser.LAST_, 0); }
		public Ordering_termContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ordering_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterOrdering_term(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitOrdering_term(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitOrdering_term(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Ordering_termContext ordering_term() throws RecognitionException {
		Ordering_termContext _localctx = new Ordering_termContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_ordering_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1653);
			expr();
			setState(1656);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,247,_ctx) ) {
			case 1:
				{
				setState(1654);
				match(COLLATE_);
				setState(1655);
				collation_name();
				}
				break;
			}
			setState(1659);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,248,_ctx) ) {
			case 1:
				{
				setState(1658);
				asc_desc();
				}
				break;
			}
			setState(1663);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,249,_ctx) ) {
			case 1:
				{
				setState(1661);
				match(NULLS_);
				setState(1662);
				_la = _input.LA(1);
				if ( !(_la==FIRST_ || _la==LAST_) ) {
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
	public static class Asc_descContext extends ParserRuleContext {
		public TerminalNode ASC_() { return getToken(SQLiteParser.ASC_, 0); }
		public TerminalNode DESC_() { return getToken(SQLiteParser.DESC_, 0); }
		public Asc_descContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asc_desc; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterAsc_desc(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitAsc_desc(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitAsc_desc(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Asc_descContext asc_desc() throws RecognitionException {
		Asc_descContext _localctx = new Asc_descContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_asc_desc);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1665);
			_la = _input.LA(1);
			if ( !(_la==ASC_ || _la==DESC_) ) {
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
	public static class Frame_leftContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode PRECEDING_() { return getToken(SQLiteParser.PRECEDING_, 0); }
		public TerminalNode FOLLOWING_() { return getToken(SQLiteParser.FOLLOWING_, 0); }
		public TerminalNode CURRENT_() { return getToken(SQLiteParser.CURRENT_, 0); }
		public TerminalNode ROW_() { return getToken(SQLiteParser.ROW_, 0); }
		public Frame_leftContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frame_left; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterFrame_left(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitFrame_left(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitFrame_left(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Frame_leftContext frame_left() throws RecognitionException {
		Frame_leftContext _localctx = new Frame_leftContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_frame_left);
		try {
			setState(1675);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,250,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1667);
				expr();
				setState(1668);
				match(PRECEDING_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1670);
				expr();
				setState(1671);
				match(FOLLOWING_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1673);
				match(CURRENT_);
				setState(1674);
				match(ROW_);
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
	public static class Frame_rightContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode PRECEDING_() { return getToken(SQLiteParser.PRECEDING_, 0); }
		public TerminalNode FOLLOWING_() { return getToken(SQLiteParser.FOLLOWING_, 0); }
		public TerminalNode CURRENT_() { return getToken(SQLiteParser.CURRENT_, 0); }
		public TerminalNode ROW_() { return getToken(SQLiteParser.ROW_, 0); }
		public Frame_rightContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frame_right; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterFrame_right(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitFrame_right(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitFrame_right(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Frame_rightContext frame_right() throws RecognitionException {
		Frame_rightContext _localctx = new Frame_rightContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_frame_right);
		try {
			setState(1685);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,251,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1677);
				expr();
				setState(1678);
				match(PRECEDING_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1680);
				expr();
				setState(1681);
				match(FOLLOWING_);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1683);
				match(CURRENT_);
				setState(1684);
				match(ROW_);
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
	public static class Frame_singleContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode PRECEDING_() { return getToken(SQLiteParser.PRECEDING_, 0); }
		public TerminalNode CURRENT_() { return getToken(SQLiteParser.CURRENT_, 0); }
		public TerminalNode ROW_() { return getToken(SQLiteParser.ROW_, 0); }
		public Frame_singleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frame_single; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterFrame_single(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitFrame_single(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitFrame_single(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Frame_singleContext frame_single() throws RecognitionException {
		Frame_singleContext _localctx = new Frame_singleContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_frame_single);
		try {
			setState(1692);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,252,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1687);
				expr();
				setState(1688);
				match(PRECEDING_);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1690);
				match(CURRENT_);
				setState(1691);
				match(ROW_);
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
	public static class Error_messageContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public Error_messageContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_error_message; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterError_message(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitError_message(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitError_message(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Error_messageContext error_message() throws RecognitionException {
		Error_messageContext _localctx = new Error_messageContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_error_message);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1694);
			expr();
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
	public static class FilenameContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public FilenameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filename; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterFilename(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitFilename(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitFilename(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilenameContext filename() throws RecognitionException {
		FilenameContext _localctx = new FilenameContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_filename);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1696);
			expr();
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
	public static class Module_argumentContext extends ParserRuleContext {
		public List<Module_argument_outerContext> module_argument_outer() {
			return getRuleContexts(Module_argument_outerContext.class);
		}
		public Module_argument_outerContext module_argument_outer(int i) {
			return getRuleContext(Module_argument_outerContext.class,i);
		}
		public Module_argumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_module_argument; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterModule_argument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitModule_argument(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitModule_argument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Module_argumentContext module_argument() throws RecognitionException {
		Module_argumentContext _localctx = new Module_argumentContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_module_argument);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1701);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,253,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1698);
					module_argument_outer();
					}
					} 
				}
				setState(1703);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,253,_ctx);
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
	public static class Module_argument_outerContext extends ParserRuleContext {
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public TerminalNode UNEXPECTED_CHAR() { return getToken(SQLiteParser.UNEXPECTED_CHAR, 0); }
		public TerminalNode COMMA() { return getToken(SQLiteParser.COMMA, 0); }
		public List<Module_argument_innerContext> module_argument_inner() {
			return getRuleContexts(Module_argument_innerContext.class);
		}
		public Module_argument_innerContext module_argument_inner(int i) {
			return getRuleContext(Module_argument_innerContext.class,i);
		}
		public Module_argument_outerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_module_argument_outer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterModule_argument_outer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitModule_argument_outer(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitModule_argument_outer(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Module_argument_outerContext module_argument_outer() throws RecognitionException {
		Module_argument_outerContext _localctx = new Module_argument_outerContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_module_argument_outer);
		int _la;
		try {
			int _alt;
			setState(1713);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,255,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1704);
				_la = _input.LA(1);
				if ( _la <= 0 || ((((_la) & ~0x3f) == 0 && ((1L << _la) & 56L) != 0) || _la==UNEXPECTED_CHAR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1705);
				match(OPEN_PAR);
				setState(1709);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,254,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1706);
						module_argument_inner();
						}
						} 
					}
					setState(1711);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,254,_ctx);
				}
				setState(1712);
				match(CLOSE_PAR);
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
	public static class Module_argument_innerContext extends ParserRuleContext {
		public TerminalNode OPEN_PAR() { return getToken(SQLiteParser.OPEN_PAR, 0); }
		public TerminalNode CLOSE_PAR() { return getToken(SQLiteParser.CLOSE_PAR, 0); }
		public TerminalNode UNEXPECTED_CHAR() { return getToken(SQLiteParser.UNEXPECTED_CHAR, 0); }
		public List<Module_argument_innerContext> module_argument_inner() {
			return getRuleContexts(Module_argument_innerContext.class);
		}
		public Module_argument_innerContext module_argument_inner(int i) {
			return getRuleContext(Module_argument_innerContext.class,i);
		}
		public Module_argument_innerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_module_argument_inner; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterModule_argument_inner(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitModule_argument_inner(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitModule_argument_inner(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Module_argument_innerContext module_argument_inner() throws RecognitionException {
		Module_argument_innerContext _localctx = new Module_argument_innerContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_module_argument_inner);
		int _la;
		try {
			int _alt;
			setState(1724);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,257,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1715);
				_la = _input.LA(1);
				if ( _la <= 0 || (_la==OPEN_PAR || _la==CLOSE_PAR || _la==UNEXPECTED_CHAR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1716);
				match(OPEN_PAR);
				setState(1720);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,256,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(1717);
						module_argument_inner();
						}
						} 
					}
					setState(1722);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,256,_ctx);
				}
				setState(1723);
				match(CLOSE_PAR);
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
	public static class Fallback_excluding_conflictsContext extends ParserRuleContext {
		public TerminalNode ABORT_() { return getToken(SQLiteParser.ABORT_, 0); }
		public TerminalNode ACTION_() { return getToken(SQLiteParser.ACTION_, 0); }
		public TerminalNode AFTER_() { return getToken(SQLiteParser.AFTER_, 0); }
		public TerminalNode ALWAYS_() { return getToken(SQLiteParser.ALWAYS_, 0); }
		public TerminalNode ANALYZE_() { return getToken(SQLiteParser.ANALYZE_, 0); }
		public TerminalNode ASC_() { return getToken(SQLiteParser.ASC_, 0); }
		public TerminalNode ATTACH_() { return getToken(SQLiteParser.ATTACH_, 0); }
		public TerminalNode BEFORE_() { return getToken(SQLiteParser.BEFORE_, 0); }
		public TerminalNode BEGIN_() { return getToken(SQLiteParser.BEGIN_, 0); }
		public TerminalNode BY_() { return getToken(SQLiteParser.BY_, 0); }
		public TerminalNode CASCADE_() { return getToken(SQLiteParser.CASCADE_, 0); }
		public TerminalNode CAST_() { return getToken(SQLiteParser.CAST_, 0); }
		public TerminalNode COLUMN_() { return getToken(SQLiteParser.COLUMN_, 0); }
		public TerminalNode CONFLICT_() { return getToken(SQLiteParser.CONFLICT_, 0); }
		public TerminalNode CURRENT_() { return getToken(SQLiteParser.CURRENT_, 0); }
		public TerminalNode CURRENT_DATE_() { return getToken(SQLiteParser.CURRENT_DATE_, 0); }
		public TerminalNode CURRENT_TIME_() { return getToken(SQLiteParser.CURRENT_TIME_, 0); }
		public TerminalNode CURRENT_TIMESTAMP_() { return getToken(SQLiteParser.CURRENT_TIMESTAMP_, 0); }
		public TerminalNode DATABASE_() { return getToken(SQLiteParser.DATABASE_, 0); }
		public TerminalNode DEFERRED_() { return getToken(SQLiteParser.DEFERRED_, 0); }
		public TerminalNode DESC_() { return getToken(SQLiteParser.DESC_, 0); }
		public TerminalNode DETACH_() { return getToken(SQLiteParser.DETACH_, 0); }
		public TerminalNode DO_() { return getToken(SQLiteParser.DO_, 0); }
		public TerminalNode EACH_() { return getToken(SQLiteParser.EACH_, 0); }
		public TerminalNode END_() { return getToken(SQLiteParser.END_, 0); }
		public TerminalNode EXCEPT_() { return getToken(SQLiteParser.EXCEPT_, 0); }
		public TerminalNode EXCLUDE_() { return getToken(SQLiteParser.EXCLUDE_, 0); }
		public TerminalNode EXCLUSIVE_() { return getToken(SQLiteParser.EXCLUSIVE_, 0); }
		public TerminalNode EXPLAIN_() { return getToken(SQLiteParser.EXPLAIN_, 0); }
		public TerminalNode FAIL_() { return getToken(SQLiteParser.FAIL_, 0); }
		public TerminalNode FALSE_() { return getToken(SQLiteParser.FALSE_, 0); }
		public TerminalNode FIRST_() { return getToken(SQLiteParser.FIRST_, 0); }
		public TerminalNode FOLLOWING_() { return getToken(SQLiteParser.FOLLOWING_, 0); }
		public TerminalNode FOR_() { return getToken(SQLiteParser.FOR_, 0); }
		public TerminalNode GENERATED_() { return getToken(SQLiteParser.GENERATED_, 0); }
		public TerminalNode GLOB_() { return getToken(SQLiteParser.GLOB_, 0); }
		public TerminalNode GROUPS_() { return getToken(SQLiteParser.GROUPS_, 0); }
		public TerminalNode IF_() { return getToken(SQLiteParser.IF_, 0); }
		public TerminalNode IGNORE_() { return getToken(SQLiteParser.IGNORE_, 0); }
		public TerminalNode IMMEDIATE_() { return getToken(SQLiteParser.IMMEDIATE_, 0); }
		public TerminalNode INITIALLY_() { return getToken(SQLiteParser.INITIALLY_, 0); }
		public TerminalNode INSTEAD_() { return getToken(SQLiteParser.INSTEAD_, 0); }
		public TerminalNode INTERSECT_() { return getToken(SQLiteParser.INTERSECT_, 0); }
		public TerminalNode KEY_() { return getToken(SQLiteParser.KEY_, 0); }
		public TerminalNode LAST_() { return getToken(SQLiteParser.LAST_, 0); }
		public TerminalNode LIKE_() { return getToken(SQLiteParser.LIKE_, 0); }
		public TerminalNode MATCH_() { return getToken(SQLiteParser.MATCH_, 0); }
		public TerminalNode MATERIALIZED_() { return getToken(SQLiteParser.MATERIALIZED_, 0); }
		public TerminalNode NO_() { return getToken(SQLiteParser.NO_, 0); }
		public TerminalNode NULLS_() { return getToken(SQLiteParser.NULLS_, 0); }
		public TerminalNode OF_() { return getToken(SQLiteParser.OF_, 0); }
		public TerminalNode OFFSET_() { return getToken(SQLiteParser.OFFSET_, 0); }
		public TerminalNode OTHERS_() { return getToken(SQLiteParser.OTHERS_, 0); }
		public TerminalNode PARTITION_() { return getToken(SQLiteParser.PARTITION_, 0); }
		public TerminalNode PLAN_() { return getToken(SQLiteParser.PLAN_, 0); }
		public TerminalNode PRAGMA_() { return getToken(SQLiteParser.PRAGMA_, 0); }
		public TerminalNode PRECEDING_() { return getToken(SQLiteParser.PRECEDING_, 0); }
		public TerminalNode QUERY_() { return getToken(SQLiteParser.QUERY_, 0); }
		public TerminalNode RANGE_() { return getToken(SQLiteParser.RANGE_, 0); }
		public TerminalNode RECURSIVE_() { return getToken(SQLiteParser.RECURSIVE_, 0); }
		public TerminalNode REGEXP_() { return getToken(SQLiteParser.REGEXP_, 0); }
		public TerminalNode REINDEX_() { return getToken(SQLiteParser.REINDEX_, 0); }
		public TerminalNode RELEASE_() { return getToken(SQLiteParser.RELEASE_, 0); }
		public TerminalNode RENAME_() { return getToken(SQLiteParser.RENAME_, 0); }
		public TerminalNode REPLACE_() { return getToken(SQLiteParser.REPLACE_, 0); }
		public TerminalNode RESTRICT_() { return getToken(SQLiteParser.RESTRICT_, 0); }
		public TerminalNode ROLLBACK_() { return getToken(SQLiteParser.ROLLBACK_, 0); }
		public TerminalNode ROW_() { return getToken(SQLiteParser.ROW_, 0); }
		public TerminalNode ROWID_() { return getToken(SQLiteParser.ROWID_, 0); }
		public TerminalNode ROWS_() { return getToken(SQLiteParser.ROWS_, 0); }
		public TerminalNode SAVEPOINT_() { return getToken(SQLiteParser.SAVEPOINT_, 0); }
		public TerminalNode STORED_() { return getToken(SQLiteParser.STORED_, 0); }
		public TerminalNode STRICT_() { return getToken(SQLiteParser.STRICT_, 0); }
		public TerminalNode TEMP_() { return getToken(SQLiteParser.TEMP_, 0); }
		public TerminalNode TEMPORARY_() { return getToken(SQLiteParser.TEMPORARY_, 0); }
		public TerminalNode TIES_() { return getToken(SQLiteParser.TIES_, 0); }
		public TerminalNode TRIGGER_() { return getToken(SQLiteParser.TRIGGER_, 0); }
		public TerminalNode TRUE_() { return getToken(SQLiteParser.TRUE_, 0); }
		public TerminalNode UNBOUNDED_() { return getToken(SQLiteParser.UNBOUNDED_, 0); }
		public TerminalNode UNION_() { return getToken(SQLiteParser.UNION_, 0); }
		public TerminalNode VACUUM_() { return getToken(SQLiteParser.VACUUM_, 0); }
		public TerminalNode VIEW_() { return getToken(SQLiteParser.VIEW_, 0); }
		public TerminalNode VIRTUAL_() { return getToken(SQLiteParser.VIRTUAL_, 0); }
		public TerminalNode WITH_() { return getToken(SQLiteParser.WITH_, 0); }
		public TerminalNode WITHIN_() { return getToken(SQLiteParser.WITHIN_, 0); }
		public TerminalNode WITHOUT_() { return getToken(SQLiteParser.WITHOUT_, 0); }
		public Fallback_excluding_conflictsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fallback_excluding_conflicts; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterFallback_excluding_conflicts(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitFallback_excluding_conflicts(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitFallback_excluding_conflicts(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Fallback_excluding_conflictsContext fallback_excluding_conflicts() throws RecognitionException {
		Fallback_excluding_conflictsContext _localctx = new Fallback_excluding_conflictsContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_fallback_excluding_conflicts);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1726);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 5731493970903564288L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 5119282589928976043L) != 0) || ((((_la - 129)) & ~0x3f) == 0 && ((1L << (_la - 129)) & 1999172898979247L) != 0)) ) {
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
	public static class Join_keywordContext extends ParserRuleContext {
		public TerminalNode CROSS_() { return getToken(SQLiteParser.CROSS_, 0); }
		public TerminalNode FULL_() { return getToken(SQLiteParser.FULL_, 0); }
		public TerminalNode INDEXED_() { return getToken(SQLiteParser.INDEXED_, 0); }
		public TerminalNode INNER_() { return getToken(SQLiteParser.INNER_, 0); }
		public TerminalNode LEFT_() { return getToken(SQLiteParser.LEFT_, 0); }
		public TerminalNode NATURAL_() { return getToken(SQLiteParser.NATURAL_, 0); }
		public TerminalNode OUTER_() { return getToken(SQLiteParser.OUTER_, 0); }
		public TerminalNode RIGHT_() { return getToken(SQLiteParser.RIGHT_, 0); }
		public Join_keywordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_join_keyword; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterJoin_keyword(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitJoin_keyword(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitJoin_keyword(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Join_keywordContext join_keyword() throws RecognitionException {
		Join_keywordContext _localctx = new Join_keywordContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_join_keyword);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1728);
			_la = _input.LA(1);
			if ( !(_la==CROSS_ || ((((_la - 86)) & ~0x3f) == 0 && ((1L << (_la - 86)) & 1152923703906936833L) != 0)) ) {
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
	public static class FallbackContext extends ParserRuleContext {
		public Fallback_excluding_conflictsContext fallback_excluding_conflicts() {
			return getRuleContext(Fallback_excluding_conflictsContext.class,0);
		}
		public Join_keywordContext join_keyword() {
			return getRuleContext(Join_keywordContext.class,0);
		}
		public TerminalNode RAISE_() { return getToken(SQLiteParser.RAISE_, 0); }
		public FallbackContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fallback; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterFallback(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitFallback(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitFallback(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FallbackContext fallback() throws RecognitionException {
		FallbackContext _localctx = new FallbackContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_fallback);
		try {
			setState(1733);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,258,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1730);
				fallback_excluding_conflicts();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1731);
				join_keyword();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1732);
				match(RAISE_);
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
	public static class NameContext extends ParserRuleContext {
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public NameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NameContext name() throws RecognitionException {
		NameContext _localctx = new NameContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1735);
			any_name();
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
		public Any_name_excluding_raiseContext any_name_excluding_raise() {
			return getRuleContext(Any_name_excluding_raiseContext.class,0);
		}
		public Function_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterFunction_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitFunction_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitFunction_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Function_nameContext function_name() throws RecognitionException {
		Function_nameContext _localctx = new Function_nameContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_function_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1737);
			any_name_excluding_raise();
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
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public Schema_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_schema_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterSchema_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitSchema_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitSchema_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Schema_nameContext schema_name() throws RecognitionException {
		Schema_nameContext _localctx = new Schema_nameContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_schema_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1739);
			any_name();
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
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public Table_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterTable_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitTable_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitTable_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Table_nameContext table_name() throws RecognitionException {
		Table_nameContext _localctx = new Table_nameContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_table_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1741);
			any_name();
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
	public static class Table_or_index_nameContext extends ParserRuleContext {
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public Table_or_index_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_or_index_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterTable_or_index_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitTable_or_index_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitTable_or_index_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Table_or_index_nameContext table_or_index_name() throws RecognitionException {
		Table_or_index_nameContext _localctx = new Table_or_index_nameContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_table_or_index_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1743);
			any_name();
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
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public Column_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterColumn_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitColumn_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitColumn_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_nameContext column_name() throws RecognitionException {
		Column_nameContext _localctx = new Column_nameContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_column_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1745);
			any_name();
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
	public static class Column_name_excluding_stringContext extends ParserRuleContext {
		public Any_name_excluding_stringContext any_name_excluding_string() {
			return getRuleContext(Any_name_excluding_stringContext.class,0);
		}
		public Column_name_excluding_stringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_name_excluding_string; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterColumn_name_excluding_string(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitColumn_name_excluding_string(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitColumn_name_excluding_string(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_name_excluding_stringContext column_name_excluding_string() throws RecognitionException {
		Column_name_excluding_stringContext _localctx = new Column_name_excluding_stringContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_column_name_excluding_string);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1747);
			any_name_excluding_string();
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
	public static class Column_aliasContext extends ParserRuleContext {
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public Column_aliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterColumn_alias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitColumn_alias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitColumn_alias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Column_aliasContext column_alias() throws RecognitionException {
		Column_aliasContext _localctx = new Column_aliasContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_column_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1749);
			any_name();
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
	public static class Collation_nameContext extends ParserRuleContext {
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public Collation_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_collation_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterCollation_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitCollation_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitCollation_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Collation_nameContext collation_name() throws RecognitionException {
		Collation_nameContext _localctx = new Collation_nameContext(_ctx, getState());
		enterRule(_localctx, 192, RULE_collation_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1751);
			any_name();
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
	public static class Foreign_tableContext extends ParserRuleContext {
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public Foreign_tableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_foreign_table; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterForeign_table(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitForeign_table(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitForeign_table(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Foreign_tableContext foreign_table() throws RecognitionException {
		Foreign_tableContext _localctx = new Foreign_tableContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_foreign_table);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1753);
			any_name();
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
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public Index_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_index_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterIndex_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitIndex_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitIndex_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Index_nameContext index_name() throws RecognitionException {
		Index_nameContext _localctx = new Index_nameContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_index_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1755);
			any_name();
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
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public Trigger_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_trigger_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterTrigger_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitTrigger_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitTrigger_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Trigger_nameContext trigger_name() throws RecognitionException {
		Trigger_nameContext _localctx = new Trigger_nameContext(_ctx, getState());
		enterRule(_localctx, 198, RULE_trigger_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1757);
			any_name();
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
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public View_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_view_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterView_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitView_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitView_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final View_nameContext view_name() throws RecognitionException {
		View_nameContext _localctx = new View_nameContext(_ctx, getState());
		enterRule(_localctx, 200, RULE_view_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1759);
			any_name();
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
	public static class Module_nameContext extends ParserRuleContext {
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public Module_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_module_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterModule_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitModule_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitModule_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Module_nameContext module_name() throws RecognitionException {
		Module_nameContext _localctx = new Module_nameContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_module_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1761);
			any_name();
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
	public static class Pragma_nameContext extends ParserRuleContext {
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public Pragma_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pragma_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterPragma_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitPragma_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitPragma_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Pragma_nameContext pragma_name() throws RecognitionException {
		Pragma_nameContext _localctx = new Pragma_nameContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_pragma_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1763);
			any_name();
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
	public static class Savepoint_nameContext extends ParserRuleContext {
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public Savepoint_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_savepoint_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterSavepoint_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitSavepoint_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitSavepoint_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Savepoint_nameContext savepoint_name() throws RecognitionException {
		Savepoint_nameContext _localctx = new Savepoint_nameContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_savepoint_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1765);
			any_name();
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
	public static class Table_aliasContext extends ParserRuleContext {
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public Table_aliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterTable_alias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitTable_alias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitTable_alias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Table_aliasContext table_alias() throws RecognitionException {
		Table_aliasContext _localctx = new Table_aliasContext(_ctx, getState());
		enterRule(_localctx, 208, RULE_table_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1767);
			any_name();
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
	public static class Table_alias_excluding_joinsContext extends ParserRuleContext {
		public Any_name_excluding_joinsContext any_name_excluding_joins() {
			return getRuleContext(Any_name_excluding_joinsContext.class,0);
		}
		public Table_alias_excluding_joinsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_alias_excluding_joins; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterTable_alias_excluding_joins(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitTable_alias_excluding_joins(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitTable_alias_excluding_joins(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Table_alias_excluding_joinsContext table_alias_excluding_joins() throws RecognitionException {
		Table_alias_excluding_joinsContext _localctx = new Table_alias_excluding_joinsContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_table_alias_excluding_joins);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1769);
			any_name_excluding_joins();
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
	public static class Window_nameContext extends ParserRuleContext {
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public Window_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_window_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterWindow_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitWindow_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitWindow_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Window_nameContext window_name() throws RecognitionException {
		Window_nameContext _localctx = new Window_nameContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_window_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1771);
			any_name();
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
	public static class AliasContext extends ParserRuleContext {
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public AliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitAlias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitAlias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AliasContext alias() throws RecognitionException {
		AliasContext _localctx = new AliasContext(_ctx, getState());
		enterRule(_localctx, 214, RULE_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1773);
			any_name();
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
	public static class Base_window_nameContext extends ParserRuleContext {
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public Base_window_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_base_window_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterBase_window_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitBase_window_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitBase_window_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Base_window_nameContext base_window_name() throws RecognitionException {
		Base_window_nameContext _localctx = new Base_window_nameContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_base_window_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1775);
			any_name();
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
	public static class Table_function_nameContext extends ParserRuleContext {
		public Any_nameContext any_name() {
			return getRuleContext(Any_nameContext.class,0);
		}
		public Table_function_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_function_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterTable_function_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitTable_function_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitTable_function_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Table_function_nameContext table_function_name() throws RecognitionException {
		Table_function_nameContext _localctx = new Table_function_nameContext(_ctx, getState());
		enterRule(_localctx, 218, RULE_table_function_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1777);
			any_name();
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
	public static class Any_name_excluding_raiseContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(SQLiteParser.IDENTIFIER, 0); }
		public Fallback_excluding_conflictsContext fallback_excluding_conflicts() {
			return getRuleContext(Fallback_excluding_conflictsContext.class,0);
		}
		public Join_keywordContext join_keyword() {
			return getRuleContext(Join_keywordContext.class,0);
		}
		public TerminalNode STRING_LITERAL() { return getToken(SQLiteParser.STRING_LITERAL, 0); }
		public Any_name_excluding_raiseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_any_name_excluding_raise; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterAny_name_excluding_raise(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitAny_name_excluding_raise(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitAny_name_excluding_raise(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Any_name_excluding_raiseContext any_name_excluding_raise() throws RecognitionException {
		Any_name_excluding_raiseContext _localctx = new Any_name_excluding_raiseContext(_ctx, getState());
		enterRule(_localctx, 220, RULE_any_name_excluding_raise);
		try {
			setState(1783);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,259,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1779);
				match(IDENTIFIER);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1780);
				fallback_excluding_conflicts();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1781);
				join_keyword();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1782);
				match(STRING_LITERAL);
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
	public static class Any_name_excluding_joinsContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(SQLiteParser.IDENTIFIER, 0); }
		public Fallback_excluding_conflictsContext fallback_excluding_conflicts() {
			return getRuleContext(Fallback_excluding_conflictsContext.class,0);
		}
		public TerminalNode RAISE_() { return getToken(SQLiteParser.RAISE_, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(SQLiteParser.STRING_LITERAL, 0); }
		public Any_name_excluding_joinsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_any_name_excluding_joins; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterAny_name_excluding_joins(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitAny_name_excluding_joins(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitAny_name_excluding_joins(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Any_name_excluding_joinsContext any_name_excluding_joins() throws RecognitionException {
		Any_name_excluding_joinsContext _localctx = new Any_name_excluding_joinsContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_any_name_excluding_joins);
		try {
			setState(1789);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,260,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1785);
				match(IDENTIFIER);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1786);
				fallback_excluding_conflicts();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1787);
				match(RAISE_);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1788);
				match(STRING_LITERAL);
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
	public static class Any_name_excluding_stringContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(SQLiteParser.IDENTIFIER, 0); }
		public FallbackContext fallback() {
			return getRuleContext(FallbackContext.class,0);
		}
		public Any_name_excluding_stringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_any_name_excluding_string; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterAny_name_excluding_string(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitAny_name_excluding_string(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitAny_name_excluding_string(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Any_name_excluding_stringContext any_name_excluding_string() throws RecognitionException {
		Any_name_excluding_stringContext _localctx = new Any_name_excluding_stringContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_any_name_excluding_string);
		try {
			setState(1793);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,261,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1791);
				match(IDENTIFIER);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1792);
				fallback();
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
	public static class Any_nameContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(SQLiteParser.IDENTIFIER, 0); }
		public FallbackContext fallback() {
			return getRuleContext(FallbackContext.class,0);
		}
		public TerminalNode STRING_LITERAL() { return getToken(SQLiteParser.STRING_LITERAL, 0); }
		public Any_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_any_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).enterAny_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SQLiteParserListener ) ((SQLiteParserListener)listener).exitAny_name(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SQLiteParserVisitor ) return ((SQLiteParserVisitor<? extends T>)visitor).visitAny_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Any_nameContext any_name() throws RecognitionException {
		Any_nameContext _localctx = new Any_nameContext(_ctx, getState());
		enterRule(_localctx, 226, RULE_any_name);
		try {
			setState(1798);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,262,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1795);
				match(IDENTIFIER);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1796);
				fallback();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1797);
				match(STRING_LITERAL);
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

	public static final String _serializedATN =
		"\u0004\u0001\u00bc\u0709\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
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
		"m\u0002n\u0007n\u0002o\u0007o\u0002p\u0007p\u0002q\u0007q\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0001\u0003\u0001\u00e9\b\u0001\u0001\u0001"+
		"\u0001\u0001\u0003\u0001\u00ed\b\u0001\u0005\u0001\u00ef\b\u0001\n\u0001"+
		"\f\u0001\u00f2\t\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002"+
		"\u00f7\b\u0002\u0003\u0002\u00f9\b\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0003\u0002\u0111\b\u0002\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0003\u0003\u0118\b\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003\u011f\b\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003\u0125\b\u0003\u0001"+
		"\u0003\u0001\u0003\u0003\u0003\u0129\b\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0003\u0003\u012e\b\u0003\u0001\u0003\u0003\u0003\u0131\b\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004"+
		"\u0138\b\u0004\u0001\u0004\u0003\u0004\u013b\b\u0004\u0001\u0005\u0001"+
		"\u0005\u0003\u0005\u013f\b\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0006\u0001\u0006\u0003\u0006\u0147\b\u0006\u0001\u0006\u0003"+
		"\u0006\u014a\b\u0006\u0001\u0007\u0001\u0007\u0003\u0007\u014e\b\u0007"+
		"\u0001\b\u0001\b\u0003\b\u0152\b\b\u0001\b\u0001\b\u0003\b\u0156\b\b\u0001"+
		"\b\u0003\b\u0159\b\b\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0003\n\u0160"+
		"\b\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0003\u000b\u0166\b\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u016c\b\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u0171\b\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0005\u000b\u017a\b\u000b\n\u000b\f\u000b\u017d\t\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0003\u000b\u0182\b\u000b\u0001\f\u0001\f\u0001\f\u0003"+
		"\f\u0187\b\f\u0001\f\u0003\f\u018a\b\f\u0001\r\u0001\r\u0003\r\u018e\b"+
		"\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003\r\u0194\b\r\u0001\r\u0001\r\u0001"+
		"\r\u0003\r\u0199\b\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0005\r\u01a0"+
		"\b\r\n\r\f\r\u01a3\t\r\u0001\r\u0001\r\u0005\r\u01a7\b\r\n\r\f\r\u01aa"+
		"\t\r\u0001\r\u0001\r\u0003\r\u01ae\b\r\u0001\r\u0001\r\u0003\r\u01b2\b"+
		"\r\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u01b7\b\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u01bd\b\u000e\u0005"+
		"\u000e\u01bf\b\u000e\n\u000e\f\u000e\u01c2\t\u000e\u0001\u000f\u0001\u000f"+
		"\u0003\u000f\u01c6\b\u000f\u0001\u000f\u0005\u000f\u01c9\b\u000f\n\u000f"+
		"\f\u000f\u01cc\t\u000f\u0001\u0010\u0004\u0010\u01cf\b\u0010\u000b\u0010"+
		"\f\u0010\u01d0\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010"+
		"\u01dd\b\u0010\u0001\u0011\u0001\u0011\u0003\u0011\u01e1\b\u0011\u0001"+
		"\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u01e6\b\u0011\u0001\u0011\u0003"+
		"\u0011\u01e9\b\u0011\u0001\u0011\u0003\u0011\u01ec\b\u0011\u0001\u0011"+
		"\u0003\u0011\u01ef\b\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u01f3\b"+
		"\u0011\u0001\u0011\u0003\u0011\u01f6\b\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u0204\b\u0011\u0001"+
		"\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u020b"+
		"\b\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0003"+
		"\u0011\u0212\b\u0011\u0003\u0011\u0214\b\u0011\u0001\u0012\u0003\u0012"+
		"\u0217\b\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0003\u0013"+
		"\u021d\b\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0003\u0013\u0222\b"+
		"\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u0228"+
		"\b\u0013\n\u0013\f\u0013\u022b\t\u0013\u0001\u0013\u0001\u0013\u0003\u0013"+
		"\u022f\b\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0005\u0013\u023c\b\u0013\n\u0013\f\u0013\u023f\t\u0013\u0001\u0013\u0001"+
		"\u0013\u0001\u0013\u0003\u0013\u0244\b\u0013\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0005\u0014\u024c\b\u0014\n"+
		"\u0014\f\u0014\u024f\t\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u0253"+
		"\b\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u025d\b\u0014\u0001\u0014\u0001"+
		"\u0014\u0005\u0014\u0261\b\u0014\n\u0014\f\u0014\u0264\t\u0014\u0001\u0014"+
		"\u0003\u0014\u0267\b\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014"+
		"\u026c\b\u0014\u0003\u0014\u026e\b\u0014\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0003\u0016\u0276\b\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u027c\b\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u0281\b\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u0288\b\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0005\u0016\u0291\b\u0016\n\u0016\f\u0016\u0294\t\u0016\u0003\u0016"+
		"\u0296\b\u0016\u0003\u0016\u0298\b\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u029f\b\u0016\u0001\u0016\u0001"+
		"\u0016\u0003\u0016\u02a3\b\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0003\u0016\u02aa\b\u0016\u0001\u0016\u0001\u0016\u0004"+
		"\u0016\u02ae\b\u0016\u000b\u0016\f\u0016\u02af\u0001\u0016\u0001\u0016"+
		"\u0001\u0017\u0001\u0017\u0003\u0017\u02b6\b\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0003\u0017\u02bc\b\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0003\u0017\u02c1\b\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0005\u0017\u02c8\b\u0017\n\u0017\f\u0017\u02cb"+
		"\t\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u02cf\b\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0003\u0018\u02da\b\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0003\u0018\u02df\b\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018\u02e8\b\u0018"+
		"\n\u0018\f\u0018\u02eb\t\u0018\u0001\u0018\u0001\u0018\u0003\u0018\u02ef"+
		"\b\u0018\u0001\u0019\u0001\u0019\u0003\u0019\u02f3\b\u0019\u0001\u0019"+
		"\u0001\u0019\u0001\u0019\u0005\u0019\u02f8\b\u0019\n\u0019\f\u0019\u02fb"+
		"\t\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0003\u001a\u0300\b\u001a"+
		"\u0001\u001a\u0003\u001a\u0303\b\u001a\u0001\u001a\u0001\u001a\u0001\u001a"+
		"\u0001\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0005\u001b\u030e\b\u001b\n\u001b\f\u001b\u0311\t\u001b\u0001\u001b\u0001"+
		"\u001b\u0003\u001b\u0315\b\u001b\u0001\u001c\u0003\u001c\u0318\b\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0003\u001c"+
		"\u031f\b\u001c\u0001\u001c\u0003\u001c\u0322\b\u001c\u0001\u001c\u0003"+
		"\u001c\u0325\b\u001c\u0001\u001c\u0003\u001c\u0328\b\u001c\u0001\u001d"+
		"\u0001\u001d\u0003\u001d\u032c\b\u001d\u0001\u001d\u0001\u001d\u0001\u001e"+
		"\u0001\u001e\u0001\u001e\u0001\u001e\u0003\u001e\u0334\b\u001e\u0001\u001e"+
		"\u0001\u001e\u0001\u001e\u0003\u001e\u0339\b\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001f\u0001\u001f\u0001 \u0001 \u0001 \u0005 \u0342\b \n \f \u0345"+
		"\t \u0001!\u0001!\u0001!\u0005!\u034a\b!\n!\f!\u034d\t!\u0001\"\u0005"+
		"\"\u0350\b\"\n\"\f\"\u0353\t\"\u0001\"\u0001\"\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0003#\u035c\b#\u0001#\u0001#\u0003#\u0360\b#\u0001#\u0001#\u0003"+
		"#\u0364\b#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0003#\u036c\b#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0005#\u0374\b#\n#\f#\u0377\t#\u0003"+
		"#\u0379\b#\u0001#\u0001#\u0001#\u0001#\u0003#\u037f\b#\u0001#\u0001#\u0001"+
		"#\u0001#\u0003#\u0385\b#\u0001#\u0001#\u0001#\u0001#\u0001#\u0005#\u038c"+
		"\b#\n#\f#\u038f\t#\u0003#\u0391\b#\u0001#\u0001#\u0003#\u0395\b#\u0001"+
		"#\u0003#\u0398\b#\u0001#\u0001#\u0001#\u0001#\u0003#\u039e\b#\u0001#\u0001"+
		"#\u0003#\u03a2\b#\u0001#\u0001#\u0001#\u0001#\u0005#\u03a8\b#\n#\f#\u03ab"+
		"\t#\u0001$\u0001$\u0001$\u0005$\u03b0\b$\n$\f$\u03b3\t$\u0001%\u0001%"+
		"\u0001%\u0005%\u03b8\b%\n%\f%\u03bb\t%\u0001&\u0001&\u0001&\u0005&\u03c0"+
		"\b&\n&\f&\u03c3\t&\u0001\'\u0001\'\u0001\'\u0005\'\u03c8\b\'\n\'\f\'\u03cb"+
		"\t\'\u0001(\u0001(\u0001(\u0005(\u03d0\b(\n(\f(\u03d3\t(\u0001)\u0001"+
		")\u0001)\u0005)\u03d8\b)\n)\f)\u03db\t)\u0001*\u0005*\u03de\b*\n*\f*\u03e1"+
		"\t*\u0001*\u0001*\u0001+\u0001+\u0001+\u0001+\u0001+\u0003+\u03ea\b+\u0001"+
		"+\u0001+\u0001+\u0001+\u0001+\u0001+\u0003+\u03f2\b+\u0001+\u0003+\u03f5"+
		"\b+\u0001+\u0001+\u0001+\u0001+\u0001+\u0001+\u0003+\u03fd\b+\u0001,\u0001"+
		",\u0001,\u0003,\u0402\b,\u0001,\u0001,\u0001,\u0005,\u0407\b,\n,\f,\u040a"+
		"\t,\u0001,\u0003,\u040d\b,\u0001,\u0003,\u0410\b,\u0001,\u0001,\u0003"+
		",\u0414\b,\u0001,\u0003,\u0417\b,\u0001,\u0003,\u041a\b,\u0001,\u0001"+
		",\u0001,\u0001,\u0005,\u0420\b,\n,\f,\u0423\t,\u0001,\u0001,\u0001,\u0001"+
		",\u0001,\u0001,\u0001,\u0001,\u0001,\u0001,\u0001,\u0003,\u0430\b,\u0001"+
		",\u0001,\u0001,\u0001,\u0001,\u0004,\u0437\b,\u000b,\f,\u0438\u0001,\u0001"+
		",\u0003,\u043d\b,\u0001,\u0001,\u0003,\u0441\b,\u0001-\u0001-\u0001-\u0001"+
		"-\u0001-\u0001-\u0003-\u0449\b-\u0001-\u0001-\u0001.\u0001.\u0001/\u0001"+
		"/\u0001/\u0001/\u0001/\u0001/\u0001/\u0001/\u00010\u00010\u00010\u0001"+
		"0\u00050\u045b\b0\n0\f0\u045e\t0\u00010\u00010\u00011\u00011\u00011\u0001"+
		"1\u00051\u0466\b1\n1\f1\u0469\t1\u00012\u00032\u046c\b2\u00012\u00012"+
		"\u00012\u00012\u00012\u00032\u0473\b2\u00012\u00012\u00012\u00012\u0003"+
		"2\u0479\b2\u00012\u00012\u00012\u00032\u047e\b2\u00012\u00012\u00012\u0001"+
		"2\u00052\u0484\b2\n2\f2\u0487\t2\u00012\u00012\u00032\u048b\b2\u00012"+
		"\u00012\u00052\u048f\b2\n2\f2\u0492\t2\u00012\u00012\u00032\u0496\b2\u0001"+
		"2\u00032\u0499\b2\u00013\u00013\u00013\u00013\u00033\u049f\b3\u00013\u0003"+
		"3\u04a2\b3\u00033\u04a4\b3\u00013\u00013\u00013\u00013\u00033\u04aa\b"+
		"3\u00013\u00033\u04ad\b3\u00033\u04af\b3\u00053\u04b1\b3\n3\f3\u04b4\t"+
		"3\u00014\u00014\u00014\u00014\u00014\u00014\u00054\u04bc\b4\n4\f4\u04bf"+
		"\t4\u00014\u00014\u00014\u00034\u04c4\b4\u00034\u04c6\b4\u00014\u0001"+
		"4\u00014\u00014\u00014\u00014\u00034\u04ce\b4\u00014\u00014\u00014\u0001"+
		"4\u00014\u00034\u04d5\b4\u00014\u00014\u00014\u00054\u04da\b4\n4\f4\u04dd"+
		"\t4\u00014\u00014\u00034\u04e1\b4\u00034\u04e3\b4\u00015\u00015\u0001"+
		"5\u00015\u00035\u04e9\b5\u00015\u00015\u00015\u00015\u00015\u00015\u0001"+
		"5\u00035\u04f2\b5\u00016\u00016\u00016\u00036\u04f7\b6\u00017\u00017\u0001"+
		"7\u00017\u00017\u00037\u04fe\b7\u00017\u00017\u00037\u0502\b7\u00037\u0504"+
		"\b7\u00018\u00038\u0507\b8\u00018\u00018\u00018\u00018\u00058\u050d\b"+
		"8\n8\f8\u0510\t8\u00018\u00038\u0513\b8\u00018\u00038\u0516\b8\u00019"+
		"\u00019\u00019\u00019\u00039\u051c\b9\u00059\u051e\b9\n9\f9\u0521\t9\u0001"+
		":\u0001:\u0003:\u0525\b:\u0001:\u0001:\u0001:\u0005:\u052a\b:\n:\f:\u052d"+
		"\t:\u0001:\u0001:\u0003:\u0531\b:\u0001:\u0001:\u0003:\u0535\b:\u0001"+
		":\u0001:\u0001:\u0001:\u0001:\u0005:\u053c\b:\n:\f:\u053f\t:\u0001:\u0001"+
		":\u0003:\u0543\b:\u0003:\u0545\b:\u0001:\u0001:\u0001:\u0001:\u0001:\u0001"+
		":\u0001:\u0001:\u0001:\u0005:\u0550\b:\n:\f:\u0553\t:\u0003:\u0555\b:"+
		"\u0001:\u0003:\u0558\b:\u0001;\u0001;\u0001;\u0003;\u055d\b;\u0001;\u0001"+
		";\u0001;\u0001;\u0003;\u0563\b;\u0001;\u0001;\u0001;\u0001;\u0001;\u0003"+
		";\u056a\b;\u0001;\u0001;\u0001;\u0003;\u056f\b;\u0001;\u0001;\u0001;\u0001"+
		";\u0001;\u0005;\u0576\b;\n;\f;\u0579\t;\u0001;\u0001;\u0003;\u057d\b;"+
		"\u0001;\u0003;\u0580\b;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001;\u0001"+
		";\u0001;\u0003;\u058a\b;\u0001;\u0003;\u058d\b;\u0003;\u058f\b;\u0001"+
		"<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0003<\u0598\b<\u0001<\u0003"+
		"<\u059b\b<\u0003<\u059d\b<\u0001=\u0001=\u0003=\u05a1\b=\u0001=\u0001"+
		"=\u0003=\u05a5\b=\u0001=\u0001=\u0003=\u05a9\b=\u0001=\u0003=\u05ac\b"+
		"=\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0005>\u05b5\b>\n>"+
		"\f>\u05b8\t>\u0001>\u0001>\u0003>\u05bc\b>\u0001?\u0001?\u0003?\u05c0"+
		"\b?\u0001?\u0001?\u0003?\u05c4\b?\u0001@\u0003@\u05c7\b@\u0001@\u0001"+
		"@\u0001@\u0003@\u05cc\b@\u0001@\u0001@\u0001@\u0001@\u0003@\u05d2\b@\u0001"+
		"@\u0001@\u0001@\u0001@\u0001@\u0003@\u05d9\b@\u0001@\u0001@\u0001@\u0005"+
		"@\u05de\b@\n@\f@\u05e1\t@\u0001@\u0001@\u0003@\u05e5\b@\u0001@\u0001@"+
		"\u0003@\u05e9\b@\u0001@\u0003@\u05ec\b@\u0001@\u0003@\u05ef\b@\u0001@"+
		"\u0003@\u05f2\b@\u0001A\u0001A\u0001A\u0001A\u0005A\u05f8\bA\nA\fA\u05fb"+
		"\tA\u0001A\u0001A\u0001B\u0001B\u0001B\u0003B\u0602\bB\u0001B\u0001B\u0001"+
		"B\u0003B\u0607\bB\u0001B\u0001B\u0001B\u0001B\u0001B\u0003B\u060e\bB\u0001"+
		"C\u0001C\u0003C\u0612\bC\u0001C\u0001C\u0003C\u0616\bC\u0001D\u0001D\u0001"+
		"D\u0001D\u0001D\u0001D\u0001E\u0001E\u0003E\u0620\bE\u0001E\u0001E\u0001"+
		"E\u0001E\u0001E\u0005E\u0627\bE\nE\fE\u062a\tE\u0003E\u062c\bE\u0001E"+
		"\u0003E\u062f\bE\u0001E\u0003E\u0632\bE\u0001E\u0001E\u0001F\u0001F\u0001"+
		"F\u0001F\u0003F\u063a\bF\u0001F\u0001F\u0001F\u0001F\u0001F\u0005F\u0641"+
		"\bF\nF\fF\u0644\tF\u0003F\u0646\bF\u0001F\u0003F\u0649\bF\u0001F\u0003"+
		"F\u064c\bF\u0001F\u0003F\u064f\bF\u0001G\u0001G\u0001G\u0001G\u0001G\u0001"+
		"G\u0001G\u0001G\u0003G\u0659\bG\u0003G\u065b\bG\u0001H\u0001H\u0001H\u0001"+
		"H\u0001H\u0001H\u0001H\u0003H\u0664\bH\u0001I\u0001I\u0001I\u0001I\u0001"+
		"I\u0005I\u066b\bI\nI\fI\u066e\tI\u0001J\u0001J\u0001J\u0001J\u0003J\u0674"+
		"\bJ\u0001K\u0001K\u0001K\u0003K\u0679\bK\u0001K\u0003K\u067c\bK\u0001"+
		"K\u0001K\u0003K\u0680\bK\u0001L\u0001L\u0001M\u0001M\u0001M\u0001M\u0001"+
		"M\u0001M\u0001M\u0001M\u0003M\u068c\bM\u0001N\u0001N\u0001N\u0001N\u0001"+
		"N\u0001N\u0001N\u0001N\u0003N\u0696\bN\u0001O\u0001O\u0001O\u0001O\u0001"+
		"O\u0003O\u069d\bO\u0001P\u0001P\u0001Q\u0001Q\u0001R\u0005R\u06a4\bR\n"+
		"R\fR\u06a7\tR\u0001S\u0001S\u0001S\u0005S\u06ac\bS\nS\fS\u06af\tS\u0001"+
		"S\u0003S\u06b2\bS\u0001T\u0001T\u0001T\u0005T\u06b7\bT\nT\fT\u06ba\tT"+
		"\u0001T\u0003T\u06bd\bT\u0001U\u0001U\u0001V\u0001V\u0001W\u0001W\u0001"+
		"W\u0003W\u06c6\bW\u0001X\u0001X\u0001Y\u0001Y\u0001Z\u0001Z\u0001[\u0001"+
		"[\u0001\\\u0001\\\u0001]\u0001]\u0001^\u0001^\u0001_\u0001_\u0001`\u0001"+
		"`\u0001a\u0001a\u0001b\u0001b\u0001c\u0001c\u0001d\u0001d\u0001e\u0001"+
		"e\u0001f\u0001f\u0001g\u0001g\u0001h\u0001h\u0001i\u0001i\u0001j\u0001"+
		"j\u0001k\u0001k\u0001l\u0001l\u0001m\u0001m\u0001n\u0001n\u0001n\u0001"+
		"n\u0003n\u06f8\bn\u0001o\u0001o\u0001o\u0001o\u0003o\u06fe\bo\u0001p\u0001"+
		"p\u0003p\u0702\bp\u0001q\u0001q\u0001q\u0003q\u0707\bq\u0001q\u0002\u01a1"+
		"\u01d0\u0000r\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016"+
		"\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprt"+
		"vxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090\u0092\u0094"+
		"\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u00a8\u00aa\u00ac"+
		"\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8\u00ba\u00bc\u00be\u00c0\u00c2\u00c4"+
		"\u00c6\u00c8\u00ca\u00cc\u00ce\u00d0\u00d2\u00d4\u00d6\u00d8\u00da\u00dc"+
		"\u00de\u00e0\u00e2\u0000\u001d\u0003\u0000>>KK^^\u0002\u000022GG\u0001"+
		"\u0000\u009d\u009e\u0002\u0000\u009a\u009a\u00ad\u00ad\u0001\u0000\b\t"+
		"\u0002\u0000??\u00a8\u00a8\u0002\u0000<<ww\u0002\u0000>>^^\u0005\u0000"+
		"\u001b\u001bNN]]\u008f\u008f\u0093\u0093\u0004\u0000``\u009c\u009c\u00a3"+
		"\u00a3\u00ac\u00ac\u0002\u0000\u0006\u0006\u0016\u0018\u0003\u0000XXp"+
		"p\u008b\u008b\u0001\u0000\u0012\u0015\u0001\u0000\u000e\u0011\u0002\u0000"+
		"\u0007\u0007\f\r\u0002\u0000\u000b\u000b\u0019\u001a\u0001\u0000\b\n\u0003"+
		"\u0000\u001b\u001bNN\u0093\u0093\u0006\u00008:OOww\u00a4\u00a4\u00b5\u00b5"+
		"\u00b7\u00b8\u0002\u0000\u001f\u001fBB\u0003\u0000VVmm\u0092\u0092\u0003"+
		"\u0000ZZ\u0088\u0088\u0096\u0096\u0002\u0000\u0005\u0005zz\u0002\u0000"+
		"QQll\u0002\u0000%%@@\u0002\u0000\u0003\u0005\u00bc\u00bc\u0002\u0000\u0003"+
		"\u0004\u00bc\u00bc)\u0000\u001b\u001c\u001e\u001e!\"%&()+,..11337;>>@"+
		"ACCEEGGIKMOQSWXZZ\\^bbefklnnpqssxz~~\u0081\u0084\u0086\u0086\u0088\u0089"+
		"\u008b\u0090\u0093\u0097\u009a\u009b\u009d\u009e\u00a0\u00a0\u00a3\u00a6"+
		"\u00aa\u00aa\u00ac\u00ad\u00b1\u00b3\b\u000066VVaaccmmrr\u007f\u007f\u0092"+
		"\u0092\u07e9\u0000\u00e4\u0001\u0000\u0000\u0000\u0002\u00e8\u0001\u0000"+
		"\u0000\u0000\u0004\u00f8\u0001\u0000\u0000\u0000\u0006\u0112\u0001\u0000"+
		"\u0000\u0000\b\u0132\u0001\u0000\u0000\u0000\n\u013c\u0001\u0000\u0000"+
		"\u0000\f\u0144\u0001\u0000\u0000\u0000\u000e\u014b\u0001\u0000\u0000\u0000"+
		"\u0010\u014f\u0001\u0000\u0000\u0000\u0012\u015a\u0001\u0000\u0000\u0000"+
		"\u0014\u015d\u0001\u0000\u0000\u0000\u0016\u0163\u0001\u0000\u0000\u0000"+
		"\u0018\u0183\u0001\u0000\u0000\u0000\u001a\u018b\u0001\u0000\u0000\u0000"+
		"\u001c\u01b6\u0001\u0000\u0000\u0000\u001e\u01c3\u0001\u0000\u0000\u0000"+
		" \u01ce\u0001\u0000\u0000\u0000\"\u01e0\u0001\u0000\u0000\u0000$\u0216"+
		"\u0001\u0000\u0000\u0000&\u021c\u0001\u0000\u0000\u0000(\u0245\u0001\u0000"+
		"\u0000\u0000*\u026f\u0001\u0000\u0000\u0000,\u0273\u0001\u0000\u0000\u0000"+
		".\u02b3\u0001\u0000\u0000\u00000\u02d3\u0001\u0000\u0000\u00002\u02f0"+
		"\u0001\u0000\u0000\u00004\u02fc\u0001\u0000\u0000\u00006\u0308\u0001\u0000"+
		"\u0000\u00008\u0317\u0001\u0000\u0000\u0000:\u0329\u0001\u0000\u0000\u0000"+
		"<\u032f\u0001\u0000\u0000\u0000>\u033c\u0001\u0000\u0000\u0000@\u033e"+
		"\u0001\u0000\u0000\u0000B\u0346\u0001\u0000\u0000\u0000D\u0351\u0001\u0000"+
		"\u0000\u0000F\u0356\u0001\u0000\u0000\u0000H\u03ac\u0001\u0000\u0000\u0000"+
		"J\u03b4\u0001\u0000\u0000\u0000L\u03bc\u0001\u0000\u0000\u0000N\u03c4"+
		"\u0001\u0000\u0000\u0000P\u03cc\u0001\u0000\u0000\u0000R\u03d4\u0001\u0000"+
		"\u0000\u0000T\u03df\u0001\u0000\u0000\u0000V\u03fc\u0001\u0000\u0000\u0000"+
		"X\u0440\u0001\u0000\u0000\u0000Z\u0442\u0001\u0000\u0000\u0000\\\u044c"+
		"\u0001\u0000\u0000\u0000^\u044e\u0001\u0000\u0000\u0000`\u0456\u0001\u0000"+
		"\u0000\u0000b\u0461\u0001\u0000\u0000\u0000d\u046b\u0001\u0000\u0000\u0000"+
		"f\u049a\u0001\u0000\u0000\u0000h\u04b5\u0001\u0000\u0000\u0000j\u04e4"+
		"\u0001\u0000\u0000\u0000l\u04f6\u0001\u0000\u0000\u0000n\u04f8\u0001\u0000"+
		"\u0000\u0000p\u0506\u0001\u0000\u0000\u0000r\u0517\u0001\u0000\u0000\u0000"+
		"t\u0557\u0001\u0000\u0000\u0000v\u058e\u0001\u0000\u0000\u0000x\u059c"+
		"\u0001\u0000\u0000\u0000z\u05ab\u0001\u0000\u0000\u0000|\u05bb\u0001\u0000"+
		"\u0000\u0000~\u05c3\u0001\u0000\u0000\u0000\u0080\u05c6\u0001\u0000\u0000"+
		"\u0000\u0082\u05f3\u0001\u0000\u0000\u0000\u0084\u0601\u0001\u0000\u0000"+
		"\u0000\u0086\u060f\u0001\u0000\u0000\u0000\u0088\u0617\u0001\u0000\u0000"+
		"\u0000\u008a\u061d\u0001\u0000\u0000\u0000\u008c\u0635\u0001\u0000\u0000"+
		"\u0000\u008e\u0650\u0001\u0000\u0000\u0000\u0090\u065c\u0001\u0000\u0000"+
		"\u0000\u0092\u0665\u0001\u0000\u0000\u0000\u0094\u066f\u0001\u0000\u0000"+
		"\u0000\u0096\u0675\u0001\u0000\u0000\u0000\u0098\u0681\u0001\u0000\u0000"+
		"\u0000\u009a\u068b\u0001\u0000\u0000\u0000\u009c\u0695\u0001\u0000\u0000"+
		"\u0000\u009e\u069c\u0001\u0000\u0000\u0000\u00a0\u069e\u0001\u0000\u0000"+
		"\u0000\u00a2\u06a0\u0001\u0000\u0000\u0000\u00a4\u06a5\u0001\u0000\u0000"+
		"\u0000\u00a6\u06b1\u0001\u0000\u0000\u0000\u00a8\u06bc\u0001\u0000\u0000"+
		"\u0000\u00aa\u06be\u0001\u0000\u0000\u0000\u00ac\u06c0\u0001\u0000\u0000"+
		"\u0000\u00ae\u06c5\u0001\u0000\u0000\u0000\u00b0\u06c7\u0001\u0000\u0000"+
		"\u0000\u00b2\u06c9\u0001\u0000\u0000\u0000\u00b4\u06cb\u0001\u0000\u0000"+
		"\u0000\u00b6\u06cd\u0001\u0000\u0000\u0000\u00b8\u06cf\u0001\u0000\u0000"+
		"\u0000\u00ba\u06d1\u0001\u0000\u0000\u0000\u00bc\u06d3\u0001\u0000\u0000"+
		"\u0000\u00be\u06d5\u0001\u0000\u0000\u0000\u00c0\u06d7\u0001\u0000\u0000"+
		"\u0000\u00c2\u06d9\u0001\u0000\u0000\u0000\u00c4\u06db\u0001\u0000\u0000"+
		"\u0000\u00c6\u06dd\u0001\u0000\u0000\u0000\u00c8\u06df\u0001\u0000\u0000"+
		"\u0000\u00ca\u06e1\u0001\u0000\u0000\u0000\u00cc\u06e3\u0001\u0000\u0000"+
		"\u0000\u00ce\u06e5\u0001\u0000\u0000\u0000\u00d0\u06e7\u0001\u0000\u0000"+
		"\u0000\u00d2\u06e9\u0001\u0000\u0000\u0000\u00d4\u06eb\u0001\u0000\u0000"+
		"\u0000\u00d6\u06ed\u0001\u0000\u0000\u0000\u00d8\u06ef\u0001\u0000\u0000"+
		"\u0000\u00da\u06f1\u0001\u0000\u0000\u0000\u00dc\u06f7\u0001\u0000\u0000"+
		"\u0000\u00de\u06fd\u0001\u0000\u0000\u0000\u00e0\u0701\u0001\u0000\u0000"+
		"\u0000\u00e2\u0706\u0001\u0000\u0000\u0000\u00e4\u00e5\u0003\u0002\u0001"+
		"\u0000\u00e5\u00e6\u0005\u0000\u0000\u0001\u00e6\u0001\u0001\u0000\u0000"+
		"\u0000\u00e7\u00e9\u0003\u0004\u0002\u0000\u00e8\u00e7\u0001\u0000\u0000"+
		"\u0000\u00e8\u00e9\u0001\u0000\u0000\u0000\u00e9\u00f0\u0001\u0000\u0000"+
		"\u0000\u00ea\u00ec\u0005\u0001\u0000\u0000\u00eb\u00ed\u0003\u0004\u0002"+
		"\u0000\u00ec\u00eb\u0001\u0000\u0000\u0000\u00ec\u00ed\u0001\u0000\u0000"+
		"\u0000\u00ed\u00ef\u0001\u0000\u0000\u0000\u00ee\u00ea\u0001\u0000\u0000"+
		"\u0000\u00ef\u00f2\u0001\u0000\u0000\u0000\u00f0\u00ee\u0001\u0000\u0000"+
		"\u0000\u00f0\u00f1\u0001\u0000\u0000\u0000\u00f1\u0003\u0001\u0000\u0000"+
		"\u0000\u00f2\u00f0\u0001\u0000\u0000\u0000\u00f3\u00f6\u0005M\u0000\u0000"+
		"\u00f4\u00f5\u0005\u0086\u0000\u0000\u00f5\u00f7\u0005\u0082\u0000\u0000"+
		"\u00f6\u00f4\u0001\u0000\u0000\u0000\u00f6\u00f7\u0001\u0000\u0000\u0000"+
		"\u00f7\u00f9\u0001\u0000\u0000\u0000\u00f8\u00f3\u0001\u0000\u0000\u0000"+
		"\u00f8\u00f9\u0001\u0000\u0000\u0000\u00f9\u0110\u0001\u0000\u0000\u0000"+
		"\u00fa\u0111\u0003\u0006\u0003\u0000\u00fb\u0111\u0003\b\u0004\u0000\u00fc"+
		"\u0111\u0003\n\u0005\u0000\u00fd\u0111\u0003\f\u0006\u0000\u00fe\u0111"+
		"\u0003\u000e\u0007\u0000\u00ff\u0111\u0003\u0016\u000b\u0000\u0100\u0111"+
		"\u0003\u001a\r\u0000\u0101\u0111\u0003,\u0016\u0000\u0102\u0111\u0003"+
		".\u0017\u0000\u0103\u0111\u00030\u0018\u0000\u0104\u0111\u00038\u001c"+
		"\u0000\u0105\u0111\u0003:\u001d\u0000\u0106\u0111\u0003<\u001e\u0000\u0107"+
		"\u0111\u0003d2\u0000\u0108\u0111\u0003j5\u0000\u0109\u0111\u0003n7\u0000"+
		"\u010a\u0111\u0003\u0014\n\u0000\u010b\u0111\u0003\u0010\b\u0000\u010c"+
		"\u0111\u0003\u0012\t\u0000\u010d\u0111\u0003p8\u0000\u010e\u0111\u0003"+
		"\u0080@\u0000\u010f\u0111\u0003\u0086C\u0000\u0110\u00fa\u0001\u0000\u0000"+
		"\u0000\u0110\u00fb\u0001\u0000\u0000\u0000\u0110\u00fc\u0001\u0000\u0000"+
		"\u0000\u0110\u00fd\u0001\u0000\u0000\u0000\u0110\u00fe\u0001\u0000\u0000"+
		"\u0000\u0110\u00ff\u0001\u0000\u0000\u0000\u0110\u0100\u0001\u0000\u0000"+
		"\u0000\u0110\u0101\u0001\u0000\u0000\u0000\u0110\u0102\u0001\u0000\u0000"+
		"\u0000\u0110\u0103\u0001\u0000\u0000\u0000\u0110\u0104\u0001\u0000\u0000"+
		"\u0000\u0110\u0105\u0001\u0000\u0000\u0000\u0110\u0106\u0001\u0000\u0000"+
		"\u0000\u0110\u0107\u0001\u0000\u0000\u0000\u0110\u0108\u0001\u0000\u0000"+
		"\u0000\u0110\u0109\u0001\u0000\u0000\u0000\u0110\u010a\u0001\u0000\u0000"+
		"\u0000\u0110\u010b\u0001\u0000\u0000\u0000\u0110\u010c\u0001\u0000\u0000"+
		"\u0000\u0110\u010d\u0001\u0000\u0000\u0000\u0110\u010e\u0001\u0000\u0000"+
		"\u0000\u0110\u010f\u0001\u0000\u0000\u0000\u0111\u0005\u0001\u0000\u0000"+
		"\u0000\u0112\u0113\u0005 \u0000\u0000\u0113\u0117\u0005\u009c\u0000\u0000"+
		"\u0114\u0115\u0003\u00b4Z\u0000\u0115\u0116\u0005\u0002\u0000\u0000\u0116"+
		"\u0118\u0001\u0000\u0000\u0000\u0117\u0114\u0001\u0000\u0000\u0000\u0117"+
		"\u0118\u0001\u0000\u0000\u0000\u0118\u0119\u0001\u0000\u0000\u0000\u0119"+
		"\u0130\u0003\u00b6[\u0000\u011a\u0124\u0005\u008e\u0000\u0000\u011b\u011c"+
		"\u0005\u00a1\u0000\u0000\u011c\u0125\u0003\u00b6[\u0000\u011d\u011f\u0005"+
		"1\u0000\u0000\u011e\u011d\u0001\u0000\u0000\u0000\u011e\u011f\u0001\u0000"+
		"\u0000\u0000\u011f\u0120\u0001\u0000\u0000\u0000\u0120\u0121\u0003\u00ba"+
		"]\u0000\u0121\u0122\u0005\u00a1\u0000\u0000\u0122\u0123\u0003\u00ba]\u0000"+
		"\u0123\u0125\u0001\u0000\u0000\u0000\u0124\u011b\u0001\u0000\u0000\u0000"+
		"\u0124\u011e\u0001\u0000\u0000\u0000\u0125\u0131\u0001\u0000\u0000\u0000"+
		"\u0126\u0128\u0005\u001d\u0000\u0000\u0127\u0129\u00051\u0000\u0000\u0128"+
		"\u0127\u0001\u0000\u0000\u0000\u0128\u0129\u0001\u0000\u0000\u0000\u0129"+
		"\u012a\u0001\u0000\u0000\u0000\u012a\u0131\u0003\u001e\u000f\u0000\u012b"+
		"\u012d\u0005D\u0000\u0000\u012c\u012e\u00051\u0000\u0000\u012d\u012c\u0001"+
		"\u0000\u0000\u0000\u012d\u012e\u0001\u0000\u0000\u0000\u012e\u012f\u0001"+
		"\u0000\u0000\u0000\u012f\u0131\u0003\u00ba]\u0000\u0130\u011a\u0001\u0000"+
		"\u0000\u0000\u0130\u0126\u0001\u0000\u0000\u0000\u0130\u012b\u0001\u0000"+
		"\u0000\u0000\u0131\u0007\u0001\u0000\u0000\u0000\u0132\u013a\u0005\"\u0000"+
		"\u0000\u0133\u013b\u0003\u00b4Z\u0000\u0134\u0135\u0003\u00b4Z\u0000\u0135"+
		"\u0136\u0005\u0002\u0000\u0000\u0136\u0138\u0001\u0000\u0000\u0000\u0137"+
		"\u0134\u0001\u0000\u0000\u0000\u0137\u0138\u0001\u0000\u0000\u0000\u0138"+
		"\u0139\u0001\u0000\u0000\u0000\u0139\u013b\u0003\u00b8\\\u0000\u013a\u0133"+
		"\u0001\u0000\u0000\u0000\u013a\u0137\u0001\u0000\u0000\u0000\u013a\u013b"+
		"\u0001\u0000\u0000\u0000\u013b\t\u0001\u0000\u0000\u0000\u013c\u013e\u0005"+
		"&\u0000\u0000\u013d\u013f\u0005;\u0000\u0000\u013e\u013d\u0001\u0000\u0000"+
		"\u0000\u013e\u013f\u0001\u0000\u0000\u0000\u013f\u0140\u0001\u0000\u0000"+
		"\u0000\u0140\u0141\u0003>\u001f\u0000\u0141\u0142\u0005$\u0000\u0000\u0142"+
		"\u0143\u0003\u00b4Z\u0000\u0143\u000b\u0001\u0000\u0000\u0000\u0144\u0146"+
		"\u0005)\u0000\u0000\u0145\u0147\u0007\u0000\u0000\u0000\u0146\u0145\u0001"+
		"\u0000\u0000\u0000\u0146\u0147\u0001\u0000\u0000\u0000\u0147\u0149\u0001"+
		"\u0000\u0000\u0000\u0148\u014a\u0005\u00a2\u0000\u0000\u0149\u0148\u0001"+
		"\u0000\u0000\u0000\u0149\u014a\u0001\u0000\u0000\u0000\u014a\r\u0001\u0000"+
		"\u0000\u0000\u014b\u014d\u0007\u0001\u0000\u0000\u014c\u014e\u0005\u00a2"+
		"\u0000\u0000\u014d\u014c\u0001\u0000\u0000\u0000\u014d\u014e\u0001\u0000"+
		"\u0000\u0000\u014e\u000f\u0001\u0000\u0000\u0000\u014f\u0151\u0005\u0093"+
		"\u0000\u0000\u0150\u0152\u0005\u00a2\u0000\u0000\u0151\u0150\u0001\u0000"+
		"\u0000\u0000\u0151\u0152\u0001\u0000\u0000\u0000\u0152\u0158\u0001\u0000"+
		"\u0000\u0000\u0153\u0155\u0005\u00a1\u0000\u0000\u0154\u0156\u0005\u0097"+
		"\u0000\u0000\u0155\u0154\u0001\u0000\u0000\u0000\u0155\u0156\u0001\u0000"+
		"\u0000\u0000\u0156\u0157\u0001\u0000\u0000\u0000\u0157\u0159\u0003\u00ce"+
		"g\u0000\u0158\u0153\u0001\u0000\u0000\u0000\u0158\u0159\u0001\u0000\u0000"+
		"\u0000\u0159\u0011\u0001\u0000\u0000\u0000\u015a\u015b\u0005\u0097\u0000"+
		"\u0000\u015b\u015c\u0003\u00ceg\u0000\u015c\u0013\u0001\u0000\u0000\u0000"+
		"\u015d\u015f\u0005\u008d\u0000\u0000\u015e\u0160\u0005\u0097\u0000\u0000"+
		"\u015f\u015e\u0001\u0000\u0000\u0000\u015f\u0160\u0001\u0000\u0000\u0000"+
		"\u0160\u0161\u0001\u0000\u0000\u0000\u0161\u0162\u0003\u00ceg\u0000\u0162"+
		"\u0015\u0001\u0000\u0000\u0000\u0163\u0165\u00055\u0000\u0000\u0164\u0166"+
		"\u0005\u00a7\u0000\u0000\u0165\u0164\u0001\u0000\u0000\u0000\u0165\u0166"+
		"\u0001\u0000\u0000\u0000\u0166\u0167\u0001\u0000\u0000\u0000\u0167\u016b"+
		"\u0005`\u0000\u0000\u0168\u0169\u0005\\\u0000\u0000\u0169\u016a\u0005"+
		"t\u0000\u0000\u016a\u016c\u0005L\u0000\u0000\u016b\u0168\u0001\u0000\u0000"+
		"\u0000\u016b\u016c\u0001\u0000\u0000\u0000\u016c\u0170\u0001\u0000\u0000"+
		"\u0000\u016d\u016e\u0003\u00b4Z\u0000\u016e\u016f\u0005\u0002\u0000\u0000"+
		"\u016f\u0171\u0001\u0000\u0000\u0000\u0170\u016d\u0001\u0000\u0000\u0000"+
		"\u0170\u0171\u0001\u0000\u0000\u0000\u0171\u0172\u0001\u0000\u0000\u0000"+
		"\u0172\u0173\u0003\u00c4b\u0000\u0173\u0174\u0005{\u0000\u0000\u0174\u0175"+
		"\u0003\u00b6[\u0000\u0175\u0176\u0005\u0003\u0000\u0000\u0176\u017b\u0003"+
		"\u0018\f\u0000\u0177\u0178\u0005\u0005\u0000\u0000\u0178\u017a\u0003\u0018"+
		"\f\u0000\u0179\u0177\u0001\u0000\u0000\u0000\u017a\u017d\u0001\u0000\u0000"+
		"\u0000\u017b\u0179\u0001\u0000\u0000\u0000\u017b\u017c\u0001\u0000\u0000"+
		"\u0000\u017c\u017e\u0001\u0000\u0000\u0000\u017d\u017b\u0001\u0000\u0000"+
		"\u0000\u017e\u0181\u0005\u0004\u0000\u0000\u017f\u0180\u0005\u00af\u0000"+
		"\u0000\u0180\u0182\u0003>\u001f\u0000\u0181\u017f\u0001\u0000\u0000\u0000"+
		"\u0181\u0182\u0001\u0000\u0000\u0000\u0182\u0017\u0001\u0000\u0000\u0000"+
		"\u0183\u0186\u0003>\u001f\u0000\u0184\u0185\u00050\u0000\u0000\u0185\u0187"+
		"\u0003\u00c0`\u0000\u0186\u0184\u0001\u0000\u0000\u0000\u0186\u0187\u0001"+
		"\u0000\u0000\u0000\u0187\u0189\u0001\u0000\u0000\u0000\u0188\u018a\u0003"+
		"\u0098L\u0000\u0189\u0188\u0001\u0000\u0000\u0000\u0189\u018a\u0001\u0000"+
		"\u0000\u0000\u018a\u0019\u0001\u0000\u0000\u0000\u018b\u018d\u00055\u0000"+
		"\u0000\u018c\u018e\u0007\u0002\u0000\u0000\u018d\u018c\u0001\u0000\u0000"+
		"\u0000\u018d\u018e\u0001\u0000\u0000\u0000\u018e\u018f\u0001\u0000\u0000"+
		"\u0000\u018f\u0193\u0005\u009c\u0000\u0000\u0190\u0191\u0005\\\u0000\u0000"+
		"\u0191\u0192\u0005t\u0000\u0000\u0192\u0194\u0005L\u0000\u0000\u0193\u0190"+
		"\u0001\u0000\u0000\u0000\u0193\u0194\u0001\u0000\u0000\u0000\u0194\u0198"+
		"\u0001\u0000\u0000\u0000\u0195\u0196\u0003\u00b4Z\u0000\u0196\u0197\u0005"+
		"\u0002\u0000\u0000\u0197\u0199\u0001\u0000\u0000\u0000\u0198\u0195\u0001"+
		"\u0000\u0000\u0000\u0198\u0199\u0001\u0000\u0000\u0000\u0199\u019a\u0001"+
		"\u0000\u0000\u0000\u019a\u01b1\u0003\u00b6[\u0000\u019b\u019c\u0005\u0003"+
		"\u0000\u0000\u019c\u01a1\u0003\u001e\u000f\u0000\u019d\u019e\u0005\u0005"+
		"\u0000\u0000\u019e\u01a0\u0003\u001e\u000f\u0000\u019f\u019d\u0001\u0000"+
		"\u0000\u0000\u01a0\u01a3\u0001\u0000\u0000\u0000\u01a1\u01a2\u0001\u0000"+
		"\u0000\u0000\u01a1\u019f\u0001\u0000\u0000\u0000\u01a2\u01a8\u0001\u0000"+
		"\u0000\u0000\u01a3\u01a1\u0001\u0000\u0000\u0000\u01a4\u01a5\u0005\u0005"+
		"\u0000\u0000\u01a5\u01a7\u0003&\u0013\u0000\u01a6\u01a4\u0001\u0000\u0000"+
		"\u0000\u01a7\u01aa\u0001\u0000\u0000\u0000\u01a8\u01a6\u0001\u0000\u0000"+
		"\u0000\u01a8\u01a9\u0001\u0000\u0000\u0000\u01a9\u01ab\u0001\u0000\u0000"+
		"\u0000\u01aa\u01a8\u0001\u0000\u0000\u0000\u01ab\u01ad\u0005\u0004\u0000"+
		"\u0000\u01ac\u01ae\u0003\u001c\u000e\u0000\u01ad\u01ac\u0001\u0000\u0000"+
		"\u0000\u01ad\u01ae\u0001\u0000\u0000\u0000\u01ae\u01b2\u0001\u0000\u0000"+
		"\u0000\u01af\u01b0\u0005$\u0000\u0000\u01b0\u01b2\u0003p8\u0000\u01b1"+
		"\u019b\u0001\u0000\u0000\u0000\u01b1\u01af\u0001\u0000\u0000\u0000\u01b2"+
		"\u001b\u0001\u0000\u0000\u0000\u01b3\u01b4\u0005\u00b3\u0000\u0000\u01b4"+
		"\u01b7\u0005\u0095\u0000\u0000\u01b5\u01b7\u0005\u009b\u0000\u0000\u01b6"+
		"\u01b3\u0001\u0000\u0000\u0000\u01b6\u01b5\u0001\u0000\u0000\u0000\u01b7"+
		"\u01c0\u0001\u0000\u0000\u0000\u01b8\u01bc\u0005\u0005\u0000\u0000\u01b9"+
		"\u01ba\u0005\u00b3\u0000\u0000\u01ba\u01bd\u0005\u0095\u0000\u0000\u01bb"+
		"\u01bd\u0005\u009b\u0000\u0000\u01bc\u01b9\u0001\u0000\u0000\u0000\u01bc"+
		"\u01bb\u0001\u0000\u0000\u0000\u01bd\u01bf\u0001\u0000\u0000\u0000\u01be"+
		"\u01b8\u0001\u0000\u0000\u0000\u01bf\u01c2\u0001\u0000\u0000\u0000\u01c0"+
		"\u01be\u0001\u0000\u0000\u0000\u01c0\u01c1\u0001\u0000\u0000\u0000\u01c1"+
		"\u001d\u0001\u0000\u0000\u0000\u01c2\u01c0\u0001\u0000\u0000\u0000\u01c3"+
		"\u01c5\u0003\u00ba]\u0000\u01c4\u01c6\u0003 \u0010\u0000\u01c5\u01c4\u0001"+
		"\u0000\u0000\u0000\u01c5\u01c6\u0001\u0000\u0000\u0000\u01c6\u01ca\u0001"+
		"\u0000\u0000\u0000\u01c7\u01c9\u0003\"\u0011\u0000\u01c8\u01c7\u0001\u0000"+
		"\u0000\u0000\u01c9\u01cc\u0001\u0000\u0000\u0000\u01ca\u01c8\u0001\u0000"+
		"\u0000\u0000\u01ca\u01cb\u0001\u0000\u0000\u0000\u01cb\u001f\u0001\u0000"+
		"\u0000\u0000\u01cc\u01ca\u0001\u0000\u0000\u0000\u01cd\u01cf\u0003\u00b0"+
		"X\u0000\u01ce\u01cd\u0001\u0000\u0000\u0000\u01cf\u01d0\u0001\u0000\u0000"+
		"\u0000\u01d0\u01d1\u0001\u0000\u0000\u0000\u01d0\u01ce\u0001\u0000\u0000"+
		"\u0000\u01d1\u01dc\u0001\u0000\u0000\u0000\u01d2\u01d3\u0005\u0003\u0000"+
		"\u0000\u01d3\u01d4\u0003$\u0012\u0000\u01d4\u01d5\u0005\u0004\u0000\u0000"+
		"\u01d5\u01dd\u0001\u0000\u0000\u0000\u01d6\u01d7\u0005\u0003\u0000\u0000"+
		"\u01d7\u01d8\u0003$\u0012\u0000\u01d8\u01d9\u0005\u0005\u0000\u0000\u01d9"+
		"\u01da\u0003$\u0012\u0000\u01da\u01db\u0005\u0004\u0000\u0000\u01db\u01dd"+
		"\u0001\u0000\u0000\u0000\u01dc\u01d2\u0001\u0000\u0000\u0000\u01dc\u01d6"+
		"\u0001\u0000\u0000\u0000\u01dc\u01dd\u0001\u0000\u0000\u0000\u01dd!\u0001"+
		"\u0000\u0000\u0000\u01de\u01df\u00054\u0000\u0000\u01df\u01e1\u0003\u00b0"+
		"X\u0000\u01e0\u01de\u0001\u0000\u0000\u0000\u01e0\u01e1\u0001\u0000\u0000"+
		"\u0000\u01e1\u0213\u0001\u0000\u0000\u0000\u01e2\u01e3\u0005\u0085\u0000"+
		"\u0000\u01e3\u01e5\u0005k\u0000\u0000\u01e4\u01e6\u0003\u0098L\u0000\u01e5"+
		"\u01e4\u0001\u0000\u0000\u0000\u01e5\u01e6\u0001\u0000\u0000\u0000\u01e6"+
		"\u01e8\u0001\u0000\u0000\u0000\u01e7\u01e9\u0003*\u0015\u0000\u01e8\u01e7"+
		"\u0001\u0000\u0000\u0000\u01e8\u01e9\u0001\u0000\u0000\u0000\u01e9\u01eb"+
		"\u0001\u0000\u0000\u0000\u01ea\u01ec\u0005\'\u0000\u0000\u01eb\u01ea\u0001"+
		"\u0000\u0000\u0000\u01eb\u01ec\u0001\u0000\u0000\u0000\u01ec\u0214\u0001"+
		"\u0000\u0000\u0000\u01ed\u01ef\u0005t\u0000\u0000\u01ee\u01ed\u0001\u0000"+
		"\u0000\u0000\u01ee\u01ef\u0001\u0000\u0000\u0000\u01ef\u01f0\u0001\u0000"+
		"\u0000\u0000\u01f0\u01f3\u0005w\u0000\u0000\u01f1\u01f3\u0005\u00a7\u0000"+
		"\u0000\u01f2\u01ee\u0001\u0000\u0000\u0000\u01f2\u01f1\u0001\u0000\u0000"+
		"\u0000\u01f3\u01f5\u0001\u0000\u0000\u0000\u01f4\u01f6\u0003*\u0015\u0000"+
		"\u01f5\u01f4\u0001\u0000\u0000\u0000\u01f5\u01f6\u0001\u0000\u0000\u0000"+
		"\u01f6\u0214\u0001\u0000\u0000\u0000\u01f7\u01f8\u0005/\u0000\u0000\u01f8"+
		"\u01f9\u0005\u0003\u0000\u0000\u01f9\u01fa\u0003>\u001f\u0000\u01fa\u01fb"+
		"\u0005\u0004\u0000\u0000\u01fb\u0214\u0001\u0000\u0000\u0000\u01fc\u0203"+
		"\u0005<\u0000\u0000\u01fd\u0204\u0003$\u0012\u0000\u01fe\u0204\u0003\\"+
		".\u0000\u01ff\u0200\u0005\u0003\u0000\u0000\u0200\u0201\u0003>\u001f\u0000"+
		"\u0201\u0202\u0005\u0004\u0000\u0000\u0202\u0204\u0001\u0000\u0000\u0000"+
		"\u0203\u01fd\u0001\u0000\u0000\u0000\u0203\u01fe\u0001\u0000\u0000\u0000"+
		"\u0203\u01ff\u0001\u0000\u0000\u0000\u0204\u0214\u0001\u0000\u0000\u0000"+
		"\u0205\u0206\u00050\u0000\u0000\u0206\u0214\u0003\u00c0`\u0000\u0207\u0214"+
		"\u0003(\u0014\u0000\u0208\u0209\u0005W\u0000\u0000\u0209\u020b\u0005!"+
		"\u0000\u0000\u020a\u0208\u0001\u0000\u0000\u0000\u020a\u020b\u0001\u0000"+
		"\u0000\u0000\u020b\u020c\u0001\u0000\u0000\u0000\u020c\u020d\u0005$\u0000"+
		"\u0000\u020d\u020e\u0005\u0003\u0000\u0000\u020e\u020f\u0003>\u001f\u0000"+
		"\u020f\u0211\u0005\u0004\u0000\u0000\u0210\u0212\u0007\u0003\u0000\u0000"+
		"\u0211\u0210\u0001\u0000\u0000\u0000\u0211\u0212\u0001\u0000\u0000\u0000"+
		"\u0212\u0214\u0001\u0000\u0000\u0000\u0213\u01e2\u0001\u0000\u0000\u0000"+
		"\u0213\u01f2\u0001\u0000\u0000\u0000\u0213\u01f7\u0001\u0000\u0000\u0000"+
		"\u0213\u01fc\u0001\u0000\u0000\u0000\u0213\u0205\u0001\u0000\u0000\u0000"+
		"\u0213\u0207\u0001\u0000\u0000\u0000\u0213\u020a\u0001\u0000\u0000\u0000"+
		"\u0214#\u0001\u0000\u0000\u0000\u0215\u0217\u0007\u0004\u0000\u0000\u0216"+
		"\u0215\u0001\u0000\u0000\u0000\u0216\u0217\u0001\u0000\u0000\u0000\u0217"+
		"\u0218\u0001\u0000\u0000\u0000\u0218\u0219\u0005\u00b5\u0000\u0000\u0219"+
		"%\u0001\u0000\u0000\u0000\u021a\u021b\u00054\u0000\u0000\u021b\u021d\u0003"+
		"\u00b0X\u0000\u021c\u021a\u0001\u0000\u0000\u0000\u021c\u021d\u0001\u0000"+
		"\u0000\u0000\u021d\u0243\u0001\u0000\u0000\u0000\u021e\u021f\u0005\u0085"+
		"\u0000\u0000\u021f\u0222\u0005k\u0000\u0000\u0220\u0222\u0005\u00a7\u0000"+
		"\u0000\u0221\u021e\u0001\u0000\u0000\u0000\u0221\u0220\u0001\u0000\u0000"+
		"\u0000\u0222\u0223\u0001\u0000\u0000\u0000\u0223\u0224\u0005\u0003\u0000"+
		"\u0000\u0224\u0229\u0003\u0018\f\u0000\u0225\u0226\u0005\u0005\u0000\u0000"+
		"\u0226\u0228\u0003\u0018\f\u0000\u0227\u0225\u0001\u0000\u0000\u0000\u0228"+
		"\u022b\u0001\u0000\u0000\u0000\u0229\u0227\u0001\u0000\u0000\u0000\u0229"+
		"\u022a\u0001\u0000\u0000\u0000\u022a\u022c\u0001\u0000\u0000\u0000\u022b"+
		"\u0229\u0001\u0000\u0000\u0000\u022c\u022e\u0005\u0004\u0000\u0000\u022d"+
		"\u022f\u0003*\u0015\u0000\u022e\u022d\u0001\u0000\u0000\u0000\u022e\u022f"+
		"\u0001\u0000\u0000\u0000\u022f\u0244\u0001\u0000\u0000\u0000\u0230\u0231"+
		"\u0005/\u0000\u0000\u0231\u0232\u0005\u0003\u0000\u0000\u0232\u0233\u0003"+
		">\u001f\u0000\u0233\u0234\u0005\u0004\u0000\u0000\u0234\u0244\u0001\u0000"+
		"\u0000\u0000\u0235\u0236\u0005T\u0000\u0000\u0236\u0237\u0005k\u0000\u0000"+
		"\u0237\u0238\u0005\u0003\u0000\u0000\u0238\u023d\u0003\u00ba]\u0000\u0239"+
		"\u023a\u0005\u0005\u0000\u0000\u023a\u023c\u0003\u00ba]\u0000\u023b\u0239"+
		"\u0001\u0000\u0000\u0000\u023c\u023f\u0001\u0000\u0000\u0000\u023d\u023b"+
		"\u0001\u0000\u0000\u0000\u023d\u023e\u0001\u0000\u0000\u0000\u023e\u0240"+
		"\u0001\u0000\u0000\u0000\u023f\u023d\u0001\u0000\u0000\u0000\u0240\u0241"+
		"\u0005\u0004\u0000\u0000\u0241\u0242\u0003(\u0014\u0000\u0242\u0244\u0001"+
		"\u0000\u0000\u0000\u0243\u0221\u0001\u0000\u0000\u0000\u0243\u0230\u0001"+
		"\u0000\u0000\u0000\u0243\u0235\u0001\u0000\u0000\u0000\u0244\'\u0001\u0000"+
		"\u0000\u0000\u0245\u0246\u0005\u008a\u0000\u0000\u0246\u0252\u0003\u00c2"+
		"a\u0000\u0247\u0248\u0005\u0003\u0000\u0000\u0248\u024d\u0003\u00ba]\u0000"+
		"\u0249\u024a\u0005\u0005\u0000\u0000\u024a\u024c\u0003\u00ba]\u0000\u024b"+
		"\u0249\u0001\u0000\u0000\u0000\u024c\u024f\u0001\u0000\u0000\u0000\u024d"+
		"\u024b\u0001\u0000\u0000\u0000\u024d\u024e\u0001\u0000\u0000\u0000\u024e"+
		"\u0250\u0001\u0000\u0000\u0000\u024f\u024d\u0001\u0000\u0000\u0000\u0250"+
		"\u0251\u0005\u0004\u0000\u0000\u0251\u0253\u0001\u0000\u0000\u0000\u0252"+
		"\u0247\u0001\u0000\u0000\u0000\u0252\u0253\u0001\u0000\u0000\u0000\u0253"+
		"\u0262\u0001\u0000\u0000\u0000\u0254\u0255\u0005{\u0000\u0000\u0255\u025c"+
		"\u0007\u0005\u0000\u0000\u0256\u0257\u0005\u0099\u0000\u0000\u0257\u025d"+
		"\u0007\u0006\u0000\u0000\u0258\u025d\u0005,\u0000\u0000\u0259\u025d\u0005"+
		"\u0090\u0000\u0000\u025a\u025b\u0005s\u0000\u0000\u025b\u025d\u0005\u001c"+
		"\u0000\u0000\u025c\u0256\u0001\u0000\u0000\u0000\u025c\u0258\u0001\u0000"+
		"\u0000\u0000\u025c\u0259\u0001\u0000\u0000\u0000\u025c\u025a\u0001\u0000"+
		"\u0000\u0000\u025d\u0261\u0001\u0000\u0000\u0000\u025e\u025f\u0005p\u0000"+
		"\u0000\u025f\u0261\u0003\u00b0X\u0000\u0260\u0254\u0001\u0000\u0000\u0000"+
		"\u0260\u025e\u0001\u0000\u0000\u0000\u0261\u0264\u0001\u0000\u0000\u0000"+
		"\u0262\u0260\u0001\u0000\u0000\u0000\u0262\u0263\u0001\u0000\u0000\u0000"+
		"\u0263\u026d\u0001\u0000\u0000\u0000\u0264\u0262\u0001\u0000\u0000\u0000"+
		"\u0265\u0267\u0005t\u0000\u0000\u0266\u0265\u0001\u0000\u0000\u0000\u0266"+
		"\u0267\u0001\u0000\u0000\u0000\u0267\u0268\u0001\u0000\u0000\u0000\u0268"+
		"\u026b\u0005=\u0000\u0000\u0269\u026a\u0005b\u0000\u0000\u026a\u026c\u0007"+
		"\u0007\u0000\u0000\u026b\u0269\u0001\u0000\u0000\u0000\u026b\u026c\u0001"+
		"\u0000\u0000\u0000\u026c\u026e\u0001\u0000\u0000\u0000\u026d\u0266\u0001"+
		"\u0000\u0000\u0000\u026d\u026e\u0001\u0000\u0000\u0000\u026e)\u0001\u0000"+
		"\u0000\u0000\u026f\u0270\u0005{\u0000\u0000\u0270\u0271\u00053\u0000\u0000"+
		"\u0271\u0272\u0007\b\u0000\u0000\u0272+\u0001\u0000\u0000\u0000\u0273"+
		"\u0275\u00055\u0000\u0000\u0274\u0276\u0007\u0002\u0000\u0000\u0275\u0274"+
		"\u0001\u0000\u0000\u0000\u0275\u0276\u0001\u0000\u0000\u0000\u0276\u0277"+
		"\u0001\u0000\u0000\u0000\u0277\u027b\u0005\u00a3\u0000\u0000\u0278\u0279"+
		"\u0005\\\u0000\u0000\u0279\u027a\u0005t\u0000\u0000\u027a\u027c\u0005"+
		"L\u0000\u0000\u027b\u0278\u0001\u0000\u0000\u0000\u027b\u027c\u0001\u0000"+
		"\u0000\u0000\u027c\u0280\u0001\u0000\u0000\u0000\u027d\u027e\u0003\u00b4"+
		"Z\u0000\u027e\u027f\u0005\u0002\u0000\u0000\u027f\u0281\u0001\u0000\u0000"+
		"\u0000\u0280\u027d\u0001\u0000\u0000\u0000\u0280\u0281\u0001\u0000\u0000"+
		"\u0000\u0281\u0282\u0001\u0000\u0000\u0000\u0282\u0287\u0003\u00c6c\u0000"+
		"\u0283\u0288\u0005(\u0000\u0000\u0284\u0288\u0005\u001e\u0000\u0000\u0285"+
		"\u0286\u0005e\u0000\u0000\u0286\u0288\u0005y\u0000\u0000\u0287\u0283\u0001"+
		"\u0000\u0000\u0000\u0287\u0284\u0001\u0000\u0000\u0000\u0287\u0285\u0001"+
		"\u0000\u0000\u0000\u0287\u0288\u0001\u0000\u0000\u0000\u0288\u0297\u0001"+
		"\u0000\u0000\u0000\u0289\u0298\u0005?\u0000\u0000\u028a\u0298\u0005d\u0000"+
		"\u0000\u028b\u0295\u0005\u00a8\u0000\u0000\u028c\u028d\u0005y\u0000\u0000"+
		"\u028d\u0292\u0003\u00ba]\u0000\u028e\u028f\u0005\u0005\u0000\u0000\u028f"+
		"\u0291\u0003\u00ba]\u0000\u0290\u028e\u0001\u0000\u0000\u0000\u0291\u0294"+
		"\u0001\u0000\u0000\u0000\u0292\u0290\u0001\u0000\u0000\u0000\u0292\u0293"+
		"\u0001\u0000\u0000\u0000\u0293\u0296\u0001\u0000\u0000\u0000\u0294\u0292"+
		"\u0001\u0000\u0000\u0000\u0295\u028c\u0001\u0000\u0000\u0000\u0295\u0296"+
		"\u0001\u0000\u0000\u0000\u0296\u0298\u0001\u0000\u0000\u0000\u0297\u0289"+
		"\u0001\u0000\u0000\u0000\u0297\u028a\u0001\u0000\u0000\u0000\u0297\u028b"+
		"\u0001\u0000\u0000\u0000\u0298\u0299\u0001\u0000\u0000\u0000\u0299\u029a"+
		"\u0005{\u0000\u0000\u029a\u029e\u0003\u00b6[\u0000\u029b\u029c\u0005S"+
		"\u0000\u0000\u029c\u029d\u0005E\u0000\u0000\u029d\u029f\u0005\u0094\u0000"+
		"\u0000\u029e\u029b\u0001\u0000\u0000\u0000\u029e\u029f\u0001\u0000\u0000"+
		"\u0000\u029f\u02a2\u0001\u0000\u0000\u0000\u02a0\u02a1\u0005\u00ae\u0000"+
		"\u0000\u02a1\u02a3\u0003>\u001f\u0000\u02a2\u02a0\u0001\u0000\u0000\u0000"+
		"\u02a2\u02a3\u0001\u0000\u0000\u0000\u02a3\u02a4\u0001\u0000\u0000\u0000"+
		"\u02a4\u02ad\u0005)\u0000\u0000\u02a5\u02aa\u0003\u0080@\u0000\u02a6\u02aa"+
		"\u0003d2\u0000\u02a7\u02aa\u00038\u001c\u0000\u02a8\u02aa\u0003p8\u0000"+
		"\u02a9\u02a5\u0001\u0000\u0000\u0000\u02a9\u02a6\u0001\u0000\u0000\u0000"+
		"\u02a9\u02a7\u0001\u0000\u0000\u0000\u02a9\u02a8\u0001\u0000\u0000\u0000"+
		"\u02aa\u02ab\u0001\u0000\u0000\u0000\u02ab\u02ac\u0005\u0001\u0000\u0000"+
		"\u02ac\u02ae\u0001\u0000\u0000\u0000\u02ad\u02a9\u0001\u0000\u0000\u0000"+
		"\u02ae\u02af\u0001\u0000\u0000\u0000\u02af\u02ad\u0001\u0000\u0000\u0000"+
		"\u02af\u02b0\u0001\u0000\u0000\u0000\u02b0\u02b1\u0001\u0000\u0000\u0000"+
		"\u02b1\u02b2\u0005G\u0000\u0000\u02b2-\u0001\u0000\u0000\u0000\u02b3\u02b5"+
		"\u00055\u0000\u0000\u02b4\u02b6\u0007\u0002\u0000\u0000\u02b5\u02b4\u0001"+
		"\u0000\u0000\u0000\u02b5\u02b6\u0001\u0000\u0000\u0000\u02b6\u02b7\u0001"+
		"\u0000\u0000\u0000\u02b7\u02bb\u0005\u00ac\u0000\u0000\u02b8\u02b9\u0005"+
		"\\\u0000\u0000\u02b9\u02ba\u0005t\u0000\u0000\u02ba\u02bc\u0005L\u0000"+
		"\u0000\u02bb\u02b8\u0001\u0000\u0000\u0000\u02bb\u02bc\u0001\u0000\u0000"+
		"\u0000\u02bc\u02c0\u0001\u0000\u0000\u0000\u02bd\u02be\u0003\u00b4Z\u0000"+
		"\u02be\u02bf\u0005\u0002\u0000\u0000\u02bf\u02c1\u0001\u0000\u0000\u0000"+
		"\u02c0\u02bd\u0001\u0000\u0000\u0000\u02c0\u02c1\u0001\u0000\u0000\u0000"+
		"\u02c1\u02c2\u0001\u0000\u0000\u0000\u02c2\u02ce\u0003\u00c8d\u0000\u02c3"+
		"\u02c4\u0005\u0003\u0000\u0000\u02c4\u02c9\u0003\u00ba]\u0000\u02c5\u02c6"+
		"\u0005\u0005\u0000\u0000\u02c6\u02c8\u0003\u00ba]\u0000\u02c7\u02c5\u0001"+
		"\u0000\u0000\u0000\u02c8\u02cb\u0001\u0000\u0000\u0000\u02c9\u02c7\u0001"+
		"\u0000\u0000\u0000\u02c9\u02ca\u0001\u0000\u0000\u0000\u02ca\u02cc\u0001"+
		"\u0000\u0000\u0000\u02cb\u02c9\u0001\u0000\u0000\u0000\u02cc\u02cd\u0005"+
		"\u0004\u0000\u0000\u02cd\u02cf\u0001\u0000\u0000\u0000\u02ce\u02c3\u0001"+
		"\u0000\u0000\u0000\u02ce\u02cf\u0001\u0000\u0000\u0000\u02cf\u02d0\u0001"+
		"\u0000\u0000\u0000\u02d0\u02d1\u0005$\u0000\u0000\u02d1\u02d2\u0003p8"+
		"\u0000\u02d2/\u0001\u0000\u0000\u0000\u02d3\u02d4\u00055\u0000\u0000\u02d4"+
		"\u02d5\u0005\u00ad\u0000\u0000\u02d5\u02d9\u0005\u009c\u0000\u0000\u02d6"+
		"\u02d7\u0005\\\u0000\u0000\u02d7\u02d8\u0005t\u0000\u0000\u02d8\u02da"+
		"\u0005L\u0000\u0000\u02d9\u02d6\u0001\u0000\u0000\u0000\u02d9\u02da\u0001"+
		"\u0000\u0000\u0000\u02da\u02de\u0001\u0000\u0000\u0000\u02db\u02dc\u0003"+
		"\u00b4Z\u0000\u02dc\u02dd\u0005\u0002\u0000\u0000\u02dd\u02df\u0001\u0000"+
		"\u0000\u0000\u02de\u02db\u0001\u0000\u0000\u0000\u02de\u02df\u0001\u0000"+
		"\u0000\u0000\u02df\u02e0\u0001\u0000\u0000\u0000\u02e0\u02e1\u0003\u00b6"+
		"[\u0000\u02e1\u02e2\u0005\u00a9\u0000\u0000\u02e2\u02ee\u0003\u00cae\u0000"+
		"\u02e3\u02e4\u0005\u0003\u0000\u0000\u02e4\u02e9\u0003\u00a4R\u0000\u02e5"+
		"\u02e6\u0005\u0005\u0000\u0000\u02e6\u02e8\u0003\u00a4R\u0000\u02e7\u02e5"+
		"\u0001\u0000\u0000\u0000\u02e8\u02eb\u0001\u0000\u0000\u0000\u02e9\u02e7"+
		"\u0001\u0000\u0000\u0000\u02e9\u02ea\u0001\u0000\u0000\u0000\u02ea\u02ec"+
		"\u0001\u0000\u0000\u0000\u02eb\u02e9\u0001\u0000\u0000\u0000\u02ec\u02ed"+
		"\u0005\u0004\u0000\u0000\u02ed\u02ef\u0001\u0000\u0000\u0000\u02ee\u02e3"+
		"\u0001\u0000\u0000\u0000\u02ee\u02ef\u0001\u0000\u0000\u0000\u02ef1\u0001"+
		"\u0000\u0000\u0000\u02f0\u02f2\u0005\u00b1\u0000\u0000\u02f1\u02f3\u0005"+
		"\u0089\u0000\u0000\u02f2\u02f1\u0001\u0000\u0000\u0000\u02f2\u02f3\u0001"+
		"\u0000\u0000\u0000\u02f3\u02f4\u0001\u0000\u0000\u0000\u02f4\u02f9\u0003"+
		"4\u001a\u0000\u02f5\u02f6\u0005\u0005\u0000\u0000\u02f6\u02f8\u00034\u001a"+
		"\u0000\u02f7\u02f5\u0001\u0000\u0000\u0000\u02f8\u02fb\u0001\u0000\u0000"+
		"\u0000\u02f9\u02f7\u0001\u0000\u0000\u0000\u02f9\u02fa\u0001\u0000\u0000"+
		"\u0000\u02fa3\u0001\u0000\u0000\u0000\u02fb\u02f9\u0001\u0000\u0000\u0000"+
		"\u02fc\u02fd\u00036\u001b\u0000\u02fd\u0302\u0005$\u0000\u0000\u02fe\u0300"+
		"\u0005t\u0000\u0000\u02ff\u02fe\u0001\u0000\u0000\u0000\u02ff\u0300\u0001"+
		"\u0000\u0000\u0000\u0300\u0301\u0001\u0000\u0000\u0000\u0301\u0303\u0005"+
		"q\u0000\u0000\u0302\u02ff\u0001\u0000\u0000\u0000\u0302\u0303\u0001\u0000"+
		"\u0000\u0000\u0303\u0304\u0001\u0000\u0000\u0000\u0304\u0305\u0005\u0003"+
		"\u0000\u0000\u0305\u0306\u0003p8\u0000\u0306\u0307\u0005\u0004\u0000\u0000"+
		"\u03075\u0001\u0000\u0000\u0000\u0308\u0314\u0003\u00b6[\u0000\u0309\u030a"+
		"\u0005\u0003\u0000\u0000\u030a\u030f\u0003\u00ba]\u0000\u030b\u030c\u0005"+
		"\u0005\u0000\u0000\u030c\u030e\u0003\u00ba]\u0000\u030d\u030b\u0001\u0000"+
		"\u0000\u0000\u030e\u0311\u0001\u0000\u0000\u0000\u030f\u030d\u0001\u0000"+
		"\u0000\u0000\u030f\u0310\u0001\u0000\u0000\u0000\u0310\u0312\u0001\u0000"+
		"\u0000\u0000\u0311\u030f\u0001\u0000\u0000\u0000\u0312\u0313\u0005\u0004"+
		"\u0000\u0000\u0313\u0315\u0001\u0000\u0000\u0000\u0314\u0309\u0001\u0000"+
		"\u0000\u0000\u0314\u0315\u0001\u0000\u0000\u0000\u03157\u0001\u0000\u0000"+
		"\u0000\u0316\u0318\u00032\u0019\u0000\u0317\u0316\u0001\u0000\u0000\u0000"+
		"\u0317\u0318\u0001\u0000\u0000\u0000\u0318\u0319\u0001\u0000\u0000\u0000"+
		"\u0319\u031a\u0005?\u0000\u0000\u031a\u031b\u0005U\u0000\u0000\u031b\u031e"+
		"\u0003\u0084B\u0000\u031c\u031d\u0005\u00af\u0000\u0000\u031d\u031f\u0003"+
		">\u001f\u0000\u031e\u031c\u0001\u0000\u0000\u0000\u031e\u031f\u0001\u0000"+
		"\u0000\u0000\u031f\u0321\u0001\u0000\u0000\u0000\u0320\u0322\u0003f3\u0000"+
		"\u0321\u0320\u0001\u0000\u0000\u0000\u0321\u0322\u0001\u0000\u0000\u0000"+
		"\u0322\u0324\u0001\u0000\u0000\u0000\u0323\u0325\u0003\u0092I\u0000\u0324"+
		"\u0323\u0001\u0000\u0000\u0000\u0324\u0325\u0001\u0000\u0000\u0000\u0325"+
		"\u0327\u0001\u0000\u0000\u0000\u0326\u0328\u0003\u0094J\u0000\u0327\u0326"+
		"\u0001\u0000\u0000\u0000\u0327\u0328\u0001\u0000\u0000\u0000\u03289\u0001"+
		"\u0000\u0000\u0000\u0329\u032b\u0005A\u0000\u0000\u032a\u032c\u0005;\u0000"+
		"\u0000\u032b\u032a\u0001\u0000\u0000\u0000\u032b\u032c\u0001\u0000\u0000"+
		"\u0000\u032c\u032d\u0001\u0000\u0000\u0000\u032d\u032e\u0003\u00b4Z\u0000"+
		"\u032e;\u0001\u0000\u0000\u0000\u032f\u0330\u0005D\u0000\u0000\u0330\u0333"+
		"\u0007\t\u0000\u0000\u0331\u0332\u0005\\\u0000\u0000\u0332\u0334\u0005"+
		"L\u0000\u0000\u0333\u0331\u0001\u0000\u0000\u0000\u0333\u0334\u0001\u0000"+
		"\u0000\u0000\u0334\u0338\u0001\u0000\u0000\u0000\u0335\u0336\u0003\u00b4"+
		"Z\u0000\u0336\u0337\u0005\u0002\u0000\u0000\u0337\u0339\u0001\u0000\u0000"+
		"\u0000\u0338\u0335\u0001\u0000\u0000\u0000\u0338\u0339\u0001\u0000\u0000"+
		"\u0000\u0339\u033a\u0001\u0000\u0000\u0000\u033a\u033b\u0003\u00e2q\u0000"+
		"\u033b=\u0001\u0000\u0000\u0000\u033c\u033d\u0003@ \u0000\u033d?\u0001"+
		"\u0000\u0000\u0000\u033e\u0343\u0003B!\u0000\u033f\u0340\u0005|\u0000"+
		"\u0000\u0340\u0342\u0003B!\u0000\u0341\u033f\u0001\u0000\u0000\u0000\u0342"+
		"\u0345\u0001\u0000\u0000\u0000\u0343\u0341\u0001\u0000\u0000\u0000\u0343"+
		"\u0344\u0001\u0000\u0000\u0000\u0344A\u0001\u0000\u0000\u0000\u0345\u0343"+
		"\u0001\u0000\u0000\u0000\u0346\u034b\u0003D\"\u0000\u0347\u0348\u0005"+
		"#\u0000\u0000\u0348\u034a\u0003D\"\u0000\u0349\u0347\u0001\u0000\u0000"+
		"\u0000\u034a\u034d\u0001\u0000\u0000\u0000\u034b\u0349\u0001\u0000\u0000"+
		"\u0000\u034b\u034c\u0001\u0000\u0000\u0000\u034cC\u0001\u0000\u0000\u0000"+
		"\u034d\u034b\u0001\u0000\u0000\u0000\u034e\u0350\u0005t\u0000\u0000\u034f"+
		"\u034e\u0001\u0000\u0000\u0000\u0350\u0353\u0001\u0000\u0000\u0000\u0351"+
		"\u034f\u0001\u0000\u0000\u0000\u0351\u0352\u0001\u0000\u0000\u0000\u0352"+
		"\u0354\u0001\u0000\u0000\u0000\u0353\u0351\u0001\u0000\u0000\u0000\u0354"+
		"\u0355\u0003F#\u0000\u0355E\u0001\u0000\u0000\u0000\u0356\u03a9\u0003"+
		"H$\u0000\u0357\u0358\u0007\n\u0000\u0000\u0358\u03a8\u0003H$\u0000\u0359"+
		"\u035b\u0005h\u0000\u0000\u035a\u035c\u0005t\u0000\u0000\u035b\u035a\u0001"+
		"\u0000\u0000\u0000\u035b\u035c\u0001\u0000\u0000\u0000\u035c\u035f\u0001"+
		"\u0000\u0000\u0000\u035d\u035e\u0005B\u0000\u0000\u035e\u0360\u0005U\u0000"+
		"\u0000\u035f\u035d\u0001\u0000\u0000\u0000\u035f\u0360\u0001\u0000\u0000"+
		"\u0000\u0360\u0361\u0001\u0000\u0000\u0000\u0361\u03a8\u0003H$\u0000\u0362"+
		"\u0364\u0005t\u0000\u0000\u0363\u0362\u0001\u0000\u0000\u0000\u0363\u0364"+
		"\u0001\u0000\u0000\u0000\u0364\u0365\u0001\u0000\u0000\u0000\u0365\u0366"+
		"\u0005*\u0000\u0000\u0366\u0367\u0003H$\u0000\u0367\u0368\u0005#\u0000"+
		"\u0000\u0368\u0369\u0003H$\u0000\u0369\u03a8\u0001\u0000\u0000\u0000\u036a"+
		"\u036c\u0005t\u0000\u0000\u036b\u036a\u0001\u0000\u0000\u0000\u036b\u036c"+
		"\u0001\u0000\u0000\u0000\u036c\u036d\u0001\u0000\u0000\u0000\u036d\u0394"+
		"\u0005_\u0000\u0000\u036e\u0378\u0005\u0003\u0000\u0000\u036f\u0379\u0003"+
		"p8\u0000\u0370\u0375\u0003H$\u0000\u0371\u0372\u0005\u0005\u0000\u0000"+
		"\u0372\u0374\u0003H$\u0000\u0373\u0371\u0001\u0000\u0000\u0000\u0374\u0377"+
		"\u0001\u0000\u0000\u0000\u0375\u0373\u0001\u0000\u0000\u0000\u0375\u0376"+
		"\u0001\u0000\u0000\u0000\u0376\u0379\u0001\u0000\u0000\u0000\u0377\u0375"+
		"\u0001\u0000\u0000\u0000\u0378\u036f\u0001\u0000\u0000\u0000\u0378\u0370"+
		"\u0001\u0000\u0000\u0000\u0378\u0379\u0001\u0000\u0000\u0000\u0379\u037a"+
		"\u0001\u0000\u0000\u0000\u037a\u0395\u0005\u0004\u0000\u0000\u037b\u037c"+
		"\u0003\u00b4Z\u0000\u037c\u037d\u0005\u0002\u0000\u0000\u037d\u037f\u0001"+
		"\u0000\u0000\u0000\u037e\u037b\u0001\u0000\u0000\u0000\u037e\u037f\u0001"+
		"\u0000\u0000\u0000\u037f\u0380\u0001\u0000\u0000\u0000\u0380\u0395\u0003"+
		"\u00b6[\u0000\u0381\u0382\u0003\u00b4Z\u0000\u0382\u0383\u0005\u0002\u0000"+
		"\u0000\u0383\u0385\u0001\u0000\u0000\u0000\u0384\u0381\u0001\u0000\u0000"+
		"\u0000\u0384\u0385\u0001\u0000\u0000\u0000\u0385\u0386\u0001\u0000\u0000"+
		"\u0000\u0386\u0387\u0003\u00dam\u0000\u0387\u0390\u0005\u0003\u0000\u0000"+
		"\u0388\u038d\u0003H$\u0000\u0389\u038a\u0005\u0005\u0000\u0000\u038a\u038c"+
		"\u0003H$\u0000\u038b\u0389\u0001\u0000\u0000\u0000\u038c\u038f\u0001\u0000"+
		"\u0000\u0000\u038d\u038b\u0001\u0000\u0000\u0000\u038d\u038e\u0001\u0000"+
		"\u0000\u0000\u038e\u0391\u0001\u0000\u0000\u0000\u038f\u038d\u0001\u0000"+
		"\u0000\u0000\u0390\u0388\u0001\u0000\u0000\u0000\u0390\u0391\u0001\u0000"+
		"\u0000\u0000\u0391\u0392\u0001\u0000\u0000\u0000\u0392\u0393\u0005\u0004"+
		"\u0000\u0000\u0393\u0395\u0001\u0000\u0000\u0000\u0394\u036e\u0001\u0000"+
		"\u0000\u0000\u0394\u037e\u0001\u0000\u0000\u0000\u0394\u0384\u0001\u0000"+
		"\u0000\u0000\u0395\u03a8\u0001\u0000\u0000\u0000\u0396\u0398\u0005t\u0000"+
		"\u0000\u0397\u0396\u0001\u0000\u0000\u0000\u0397\u0398\u0001\u0000\u0000"+
		"\u0000\u0398\u03a1\u0001\u0000\u0000\u0000\u0399\u039a\u0005n\u0000\u0000"+
		"\u039a\u039d\u0003H$\u0000\u039b\u039c\u0005H\u0000\u0000\u039c\u039e"+
		"\u0003H$\u0000\u039d\u039b\u0001\u0000\u0000\u0000\u039d\u039e\u0001\u0000"+
		"\u0000\u0000\u039e\u03a2\u0001\u0000\u0000\u0000\u039f\u03a0\u0007\u000b"+
		"\u0000\u0000\u03a0\u03a2\u0003H$\u0000\u03a1\u0399\u0001\u0000\u0000\u0000"+
		"\u03a1\u039f\u0001\u0000\u0000\u0000\u03a2\u03a8\u0001\u0000\u0000\u0000"+
		"\u03a3\u03a8\u0005i\u0000\u0000\u03a4\u03a8\u0005v\u0000\u0000\u03a5\u03a6"+
		"\u0005t\u0000\u0000\u03a6\u03a8\u0005w\u0000\u0000\u03a7\u0357\u0001\u0000"+
		"\u0000\u0000\u03a7\u0359\u0001\u0000\u0000\u0000\u03a7\u0363\u0001\u0000"+
		"\u0000\u0000\u03a7\u036b\u0001\u0000\u0000\u0000\u03a7\u0397\u0001\u0000"+
		"\u0000\u0000\u03a7\u03a3\u0001\u0000\u0000\u0000\u03a7\u03a4\u0001\u0000"+
		"\u0000\u0000\u03a7\u03a5\u0001\u0000\u0000\u0000\u03a8\u03ab\u0001\u0000"+
		"\u0000\u0000\u03a9\u03a7\u0001\u0000\u0000\u0000\u03a9\u03aa\u0001\u0000"+
		"\u0000\u0000\u03aaG\u0001\u0000\u0000\u0000\u03ab\u03a9\u0001\u0000\u0000"+
		"\u0000\u03ac\u03b1\u0003J%\u0000\u03ad\u03ae\u0007\f\u0000\u0000\u03ae"+
		"\u03b0\u0003J%\u0000\u03af\u03ad\u0001\u0000\u0000\u0000\u03b0\u03b3\u0001"+
		"\u0000\u0000\u0000\u03b1\u03af\u0001\u0000\u0000\u0000\u03b1\u03b2\u0001"+
		"\u0000\u0000\u0000\u03b2I\u0001\u0000\u0000\u0000\u03b3\u03b1\u0001\u0000"+
		"\u0000\u0000\u03b4\u03b9\u0003L&\u0000\u03b5\u03b6\u0007\r\u0000\u0000"+
		"\u03b6\u03b8\u0003L&\u0000\u03b7\u03b5\u0001\u0000\u0000\u0000\u03b8\u03bb"+
		"\u0001\u0000\u0000\u0000\u03b9\u03b7\u0001\u0000\u0000\u0000\u03b9\u03ba"+
		"\u0001\u0000\u0000\u0000\u03baK\u0001\u0000\u0000\u0000\u03bb\u03b9\u0001"+
		"\u0000\u0000\u0000\u03bc\u03c1\u0003N\'\u0000\u03bd\u03be\u0007\u0004"+
		"\u0000\u0000\u03be\u03c0\u0003N\'\u0000\u03bf\u03bd\u0001\u0000\u0000"+
		"\u0000\u03c0\u03c3\u0001\u0000\u0000\u0000\u03c1\u03bf\u0001\u0000\u0000"+
		"\u0000\u03c1\u03c2\u0001\u0000\u0000\u0000\u03c2M\u0001\u0000\u0000\u0000"+
		"\u03c3\u03c1\u0001\u0000\u0000\u0000\u03c4\u03c9\u0003P(\u0000\u03c5\u03c6"+
		"\u0007\u000e\u0000\u0000\u03c6\u03c8\u0003P(\u0000\u03c7\u03c5\u0001\u0000"+
		"\u0000\u0000\u03c8\u03cb\u0001\u0000\u0000\u0000\u03c9\u03c7\u0001\u0000"+
		"\u0000\u0000\u03c9\u03ca\u0001\u0000\u0000\u0000\u03caO\u0001\u0000\u0000"+
		"\u0000\u03cb\u03c9\u0001\u0000\u0000\u0000\u03cc\u03d1\u0003R)\u0000\u03cd"+
		"\u03ce\u0007\u000f\u0000\u0000\u03ce\u03d0\u0003R)\u0000\u03cf\u03cd\u0001"+
		"\u0000\u0000\u0000\u03d0\u03d3\u0001\u0000\u0000\u0000\u03d1\u03cf\u0001"+
		"\u0000\u0000\u0000\u03d1\u03d2\u0001\u0000\u0000\u0000\u03d2Q\u0001\u0000"+
		"\u0000\u0000\u03d3\u03d1\u0001\u0000\u0000\u0000\u03d4\u03d9\u0003T*\u0000"+
		"\u03d5\u03d6\u00050\u0000\u0000\u03d6\u03d8\u0003\u00c0`\u0000\u03d7\u03d5"+
		"\u0001\u0000\u0000\u0000\u03d8\u03db\u0001\u0000\u0000\u0000\u03d9\u03d7"+
		"\u0001\u0000\u0000\u0000\u03d9\u03da\u0001\u0000\u0000\u0000\u03daS\u0001"+
		"\u0000\u0000\u0000\u03db\u03d9\u0001\u0000\u0000\u0000\u03dc\u03de\u0007"+
		"\u0010\u0000\u0000\u03dd\u03dc\u0001\u0000\u0000\u0000\u03de\u03e1\u0001"+
		"\u0000\u0000\u0000\u03df\u03dd\u0001\u0000\u0000\u0000\u03df\u03e0\u0001"+
		"\u0000\u0000\u0000\u03e0\u03e2\u0001\u0000\u0000\u0000\u03e1\u03df\u0001"+
		"\u0000\u0000\u0000\u03e2\u03e3\u0003V+\u0000\u03e3U\u0001\u0000\u0000"+
		"\u0000\u03e4\u03fd\u0003\\.\u0000\u03e5\u03fd\u0005\u00b6\u0000\u0000"+
		"\u03e6\u03e7\u0003\u00b4Z\u0000\u03e7\u03e8\u0005\u0002\u0000\u0000\u03e8"+
		"\u03ea\u0001\u0000\u0000\u0000\u03e9\u03e6\u0001\u0000\u0000\u0000\u03e9"+
		"\u03ea\u0001\u0000\u0000\u0000\u03ea\u03eb\u0001\u0000\u0000\u0000\u03eb"+
		"\u03ec\u0003\u00b6[\u0000\u03ec\u03ed\u0005\u0002\u0000\u0000\u03ed\u03ee"+
		"\u0003\u00ba]\u0000\u03ee\u03fd\u0001\u0000\u0000\u0000\u03ef\u03fd\u0003"+
		"\u00bc^\u0000\u03f0\u03f2\u0005t\u0000\u0000\u03f1\u03f0\u0001\u0000\u0000"+
		"\u0000\u03f1\u03f2\u0001\u0000\u0000\u0000\u03f2\u03f3\u0001\u0000\u0000"+
		"\u0000\u03f3\u03f5\u0005L\u0000\u0000\u03f4\u03f1\u0001\u0000\u0000\u0000"+
		"\u03f4\u03f5\u0001\u0000\u0000\u0000\u03f5\u03f6\u0001\u0000\u0000\u0000"+
		"\u03f6\u03f7\u0005\u0003\u0000\u0000\u03f7\u03f8\u0003p8\u0000\u03f8\u03f9"+
		"\u0005\u0004\u0000\u0000\u03f9\u03fd\u0001\u0000\u0000\u0000\u03fa\u03fd"+
		"\u0003Z-\u0000\u03fb\u03fd\u0003X,\u0000\u03fc\u03e4\u0001\u0000\u0000"+
		"\u0000\u03fc\u03e5\u0001\u0000\u0000\u0000\u03fc\u03e9\u0001\u0000\u0000"+
		"\u0000\u03fc\u03ef\u0001\u0000\u0000\u0000\u03fc\u03f4\u0001\u0000\u0000"+
		"\u0000\u03fc\u03fa\u0001\u0000\u0000\u0000\u03fc\u03fb\u0001\u0000\u0000"+
		"\u0000\u03fdW\u0001\u0000\u0000\u0000\u03fe\u03ff\u0003\u00b2Y\u0000\u03ff"+
		"\u040f\u0005\u0003\u0000\u0000\u0400\u0402\u0005B\u0000\u0000\u0401\u0400"+
		"\u0001\u0000\u0000\u0000\u0401\u0402\u0001\u0000\u0000\u0000\u0402\u0403"+
		"\u0001\u0000\u0000\u0000\u0403\u0408\u0003>\u001f\u0000\u0404\u0405\u0005"+
		"\u0005\u0000\u0000\u0405\u0407\u0003>\u001f\u0000\u0406\u0404\u0001\u0000"+
		"\u0000\u0000\u0407\u040a\u0001\u0000\u0000\u0000\u0408\u0406\u0001\u0000"+
		"\u0000\u0000\u0408\u0409\u0001\u0000\u0000\u0000\u0409\u040c\u0001\u0000"+
		"\u0000\u0000\u040a\u0408\u0001\u0000\u0000\u0000\u040b\u040d\u0003\u0092"+
		"I\u0000\u040c\u040b\u0001\u0000\u0000\u0000\u040c\u040d\u0001\u0000\u0000"+
		"\u0000\u040d\u0410\u0001\u0000\u0000\u0000\u040e\u0410\u0005\u0007\u0000"+
		"\u0000\u040f\u0401\u0001\u0000\u0000\u0000\u040f\u040e\u0001\u0000\u0000"+
		"\u0000\u040f\u0410\u0001\u0000\u0000\u0000\u0410\u0411\u0001\u0000\u0000"+
		"\u0000\u0411\u0413\u0005\u0004\u0000\u0000\u0412\u0414\u0003^/\u0000\u0413"+
		"\u0412\u0001\u0000\u0000\u0000\u0413\u0414\u0001\u0000\u0000\u0000\u0414"+
		"\u0416\u0001\u0000\u0000\u0000\u0415\u0417\u0003\u0088D\u0000\u0416\u0415"+
		"\u0001\u0000\u0000\u0000\u0416\u0417\u0001\u0000\u0000\u0000\u0417\u0419"+
		"\u0001\u0000\u0000\u0000\u0418\u041a\u0003\u008cF\u0000\u0419\u0418\u0001"+
		"\u0000\u0000\u0000\u0419\u041a\u0001\u0000\u0000\u0000\u041a\u0441\u0001"+
		"\u0000\u0000\u0000\u041b\u041c\u0005\u0003\u0000\u0000\u041c\u0421\u0003"+
		">\u001f\u0000\u041d\u041e\u0005\u0005\u0000\u0000\u041e\u0420\u0003>\u001f"+
		"\u0000\u041f\u041d\u0001\u0000\u0000\u0000\u0420\u0423\u0001\u0000\u0000"+
		"\u0000\u0421\u041f\u0001\u0000\u0000\u0000\u0421\u0422\u0001\u0000\u0000"+
		"\u0000\u0422\u0424\u0001\u0000\u0000\u0000\u0423\u0421\u0001\u0000\u0000"+
		"\u0000\u0424\u0425\u0005\u0004\u0000\u0000\u0425\u0441\u0001\u0000\u0000"+
		"\u0000\u0426\u0427\u0005.\u0000\u0000\u0427\u0428\u0005\u0003\u0000\u0000"+
		"\u0428\u0429\u0003>\u001f\u0000\u0429\u042a\u0005$\u0000\u0000\u042a\u042b"+
		"\u0003 \u0010\u0000\u042b\u042c\u0005\u0004\u0000\u0000\u042c\u0441\u0001"+
		"\u0000\u0000\u0000\u042d\u042f\u0005-\u0000\u0000\u042e\u0430\u0003>\u001f"+
		"\u0000\u042f\u042e\u0001\u0000\u0000\u0000\u042f\u0430\u0001\u0000\u0000"+
		"\u0000\u0430\u0436\u0001\u0000\u0000\u0000\u0431\u0432\u0005\u00ae\u0000"+
		"\u0000\u0432\u0433\u0003>\u001f\u0000\u0433\u0434\u0005\u009f\u0000\u0000"+
		"\u0434\u0435\u0003>\u001f\u0000\u0435\u0437\u0001\u0000\u0000\u0000\u0436"+
		"\u0431\u0001\u0000\u0000\u0000\u0437\u0438\u0001\u0000\u0000\u0000\u0438"+
		"\u0436\u0001\u0000\u0000\u0000\u0438\u0439\u0001\u0000\u0000\u0000\u0439"+
		"\u043c\u0001\u0000\u0000\u0000\u043a\u043b\u0005F\u0000\u0000\u043b\u043d"+
		"\u0003>\u001f\u0000\u043c\u043a\u0001\u0000\u0000\u0000\u043c\u043d\u0001"+
		"\u0000\u0000\u0000\u043d\u043e\u0001\u0000\u0000\u0000\u043e\u043f\u0005"+
		"G\u0000\u0000\u043f\u0441\u0001\u0000\u0000\u0000\u0440\u03fe\u0001\u0000"+
		"\u0000\u0000\u0440\u041b\u0001\u0000\u0000\u0000\u0440\u0426\u0001\u0000"+
		"\u0000\u0000\u0440\u042d\u0001\u0000\u0000\u0000\u0441Y\u0001\u0000\u0000"+
		"\u0000\u0442\u0443\u0005\u0087\u0000\u0000\u0443\u0448\u0005\u0003\u0000"+
		"\u0000\u0444\u0449\u0005]\u0000\u0000\u0445\u0446\u0007\u0011\u0000\u0000"+
		"\u0446\u0447\u0005\u0005\u0000\u0000\u0447\u0449\u0003\u00a0P\u0000\u0448"+
		"\u0444\u0001\u0000\u0000\u0000\u0448\u0445\u0001\u0000\u0000\u0000\u0449"+
		"\u044a\u0001\u0000\u0000\u0000\u044a\u044b\u0005\u0004\u0000\u0000\u044b"+
		"[\u0001\u0000\u0000\u0000\u044c\u044d\u0007\u0012\u0000\u0000\u044d]\u0001"+
		"\u0000\u0000\u0000\u044e\u044f\u0005\u00b2\u0000\u0000\u044f\u0450\u0005"+
		"Y\u0000\u0000\u0450\u0451\u0005\u0003\u0000\u0000\u0451\u0452\u0005}\u0000"+
		"\u0000\u0452\u0453\u0005+\u0000\u0000\u0453\u0454\u0003>\u001f\u0000\u0454"+
		"\u0455\u0005\u0004\u0000\u0000\u0455_\u0001\u0000\u0000\u0000\u0456\u0457"+
		"\u0005\u0003\u0000\u0000\u0457\u045c\u0003>\u001f\u0000\u0458\u0459\u0005"+
		"\u0005\u0000\u0000\u0459\u045b\u0003>\u001f\u0000\u045a\u0458\u0001\u0000"+
		"\u0000\u0000\u045b\u045e\u0001\u0000\u0000\u0000\u045c\u045a\u0001\u0000"+
		"\u0000\u0000\u045c\u045d\u0001\u0000\u0000\u0000\u045d\u045f\u0001\u0000"+
		"\u0000\u0000\u045e\u045c\u0001\u0000\u0000\u0000\u045f\u0460\u0005\u0004"+
		"\u0000\u0000\u0460a\u0001\u0000\u0000\u0000\u0461\u0462\u0005\u00ab\u0000"+
		"\u0000\u0462\u0467\u0003`0\u0000\u0463\u0464\u0005\u0005\u0000\u0000\u0464"+
		"\u0466\u0003`0\u0000\u0465\u0463\u0001\u0000\u0000\u0000\u0466\u0469\u0001"+
		"\u0000\u0000\u0000\u0467\u0465\u0001\u0000\u0000\u0000\u0467\u0468\u0001"+
		"\u0000\u0000\u0000\u0468c\u0001\u0000\u0000\u0000\u0469\u0467\u0001\u0000"+
		"\u0000\u0000\u046a\u046c\u00032\u0019\u0000\u046b\u046a\u0001\u0000\u0000"+
		"\u0000\u046b\u046c\u0001\u0000\u0000\u0000\u046c\u0472\u0001\u0000\u0000"+
		"\u0000\u046d\u0473\u0005d\u0000\u0000\u046e\u0473\u0005\u008f\u0000\u0000"+
		"\u046f\u0470\u0005d\u0000\u0000\u0470\u0471\u0005|\u0000\u0000\u0471\u0473"+
		"\u0007\b\u0000\u0000\u0472\u046d\u0001\u0000\u0000\u0000\u0472\u046e\u0001"+
		"\u0000\u0000\u0000\u0472\u046f\u0001\u0000\u0000\u0000\u0473\u0474\u0001"+
		"\u0000\u0000\u0000\u0474\u0478\u0005g\u0000\u0000\u0475\u0476\u0003\u00b4"+
		"Z\u0000\u0476\u0477\u0005\u0002\u0000\u0000\u0477\u0479\u0001\u0000\u0000"+
		"\u0000\u0478\u0475\u0001\u0000\u0000\u0000\u0478\u0479\u0001\u0000\u0000"+
		"\u0000\u0479\u047a\u0001\u0000\u0000\u0000\u047a\u047d\u0003\u00b6[\u0000"+
		"\u047b\u047c\u0005$\u0000\u0000\u047c\u047e\u0003\u00d0h\u0000\u047d\u047b"+
		"\u0001\u0000\u0000\u0000\u047d\u047e\u0001\u0000\u0000\u0000\u047e\u048a"+
		"\u0001\u0000\u0000\u0000\u047f\u0480\u0005\u0003\u0000\u0000\u0480\u0485"+
		"\u0003\u00ba]\u0000\u0481\u0482\u0005\u0005\u0000\u0000\u0482\u0484\u0003"+
		"\u00ba]\u0000\u0483\u0481\u0001\u0000\u0000\u0000\u0484\u0487\u0001\u0000"+
		"\u0000\u0000\u0485\u0483\u0001\u0000\u0000\u0000\u0485\u0486\u0001\u0000"+
		"\u0000\u0000\u0486\u0488\u0001\u0000\u0000\u0000\u0487\u0485\u0001\u0000"+
		"\u0000\u0000\u0488\u0489\u0005\u0004\u0000\u0000\u0489\u048b\u0001\u0000"+
		"\u0000\u0000\u048a\u047f\u0001\u0000\u0000\u0000\u048a\u048b\u0001\u0000"+
		"\u0000\u0000\u048b\u0495\u0001\u0000\u0000\u0000\u048c\u0490\u0003p8\u0000"+
		"\u048d\u048f\u0003h4\u0000\u048e\u048d\u0001\u0000\u0000\u0000\u048f\u0492"+
		"\u0001\u0000\u0000\u0000\u0490\u048e\u0001\u0000\u0000\u0000\u0490\u0491"+
		"\u0001\u0000\u0000\u0000\u0491\u0496\u0001\u0000\u0000\u0000\u0492\u0490"+
		"\u0001\u0000\u0000\u0000\u0493\u0494\u0005<\u0000\u0000\u0494\u0496\u0005"+
		"\u00ab\u0000\u0000\u0495\u048c\u0001\u0000\u0000\u0000\u0495\u0493\u0001"+
		"\u0000\u0000\u0000\u0496\u0498\u0001\u0000\u0000\u0000\u0497\u0499\u0003"+
		"f3\u0000\u0498\u0497\u0001\u0000\u0000\u0000\u0498\u0499\u0001\u0000\u0000"+
		"\u0000\u0499e\u0001\u0000\u0000\u0000\u049a\u04a3\u0005\u0091\u0000\u0000"+
		"\u049b\u04a4\u0005\u0007\u0000\u0000\u049c\u04a1\u0003>\u001f\u0000\u049d"+
		"\u049f\u0005$\u0000\u0000\u049e\u049d\u0001\u0000\u0000\u0000\u049e\u049f"+
		"\u0001\u0000\u0000\u0000\u049f\u04a0\u0001\u0000\u0000\u0000\u04a0\u04a2"+
		"\u0003\u00be_\u0000\u04a1\u049e\u0001\u0000\u0000\u0000\u04a1\u04a2\u0001"+
		"\u0000\u0000\u0000\u04a2\u04a4\u0001\u0000\u0000\u0000\u04a3\u049b\u0001"+
		"\u0000\u0000\u0000\u04a3\u049c\u0001\u0000\u0000\u0000\u04a4\u04b2\u0001"+
		"\u0000\u0000\u0000\u04a5\u04ae\u0005\u0005\u0000\u0000\u04a6\u04af\u0005"+
		"\u0007\u0000\u0000\u04a7\u04ac\u0003>\u001f\u0000\u04a8\u04aa\u0005$\u0000"+
		"\u0000\u04a9\u04a8\u0001\u0000\u0000\u0000\u04a9\u04aa\u0001\u0000\u0000"+
		"\u0000\u04aa\u04ab\u0001\u0000\u0000\u0000\u04ab\u04ad\u0003\u00be_\u0000"+
		"\u04ac\u04a9\u0001\u0000\u0000\u0000\u04ac\u04ad\u0001\u0000\u0000\u0000"+
		"\u04ad\u04af\u0001\u0000\u0000\u0000\u04ae\u04a6\u0001\u0000\u0000\u0000"+
		"\u04ae\u04a7\u0001\u0000\u0000\u0000\u04af\u04b1\u0001\u0000\u0000\u0000"+
		"\u04b0\u04a5\u0001\u0000\u0000\u0000\u04b1\u04b4\u0001\u0000\u0000\u0000"+
		"\u04b2\u04b0\u0001\u0000\u0000\u0000\u04b2\u04b3\u0001\u0000\u0000\u0000"+
		"\u04b3g\u0001\u0000\u0000\u0000\u04b4\u04b2\u0001\u0000\u0000\u0000\u04b5"+
		"\u04b6\u0005{\u0000\u0000\u04b6\u04c5\u00053\u0000\u0000\u04b7\u04b8\u0005"+
		"\u0003\u0000\u0000\u04b8\u04bd\u0003\u0018\f\u0000\u04b9\u04ba\u0005\u0005"+
		"\u0000\u0000\u04ba\u04bc\u0003\u0018\f\u0000\u04bb\u04b9\u0001\u0000\u0000"+
		"\u0000\u04bc\u04bf\u0001\u0000\u0000\u0000\u04bd\u04bb\u0001\u0000\u0000"+
		"\u0000\u04bd\u04be\u0001\u0000\u0000\u0000\u04be\u04c0\u0001\u0000\u0000"+
		"\u0000\u04bf\u04bd\u0001\u0000\u0000\u0000\u04c0\u04c3\u0005\u0004\u0000"+
		"\u0000\u04c1\u04c2\u0005\u00af\u0000\u0000\u04c2\u04c4\u0003>\u001f\u0000"+
		"\u04c3\u04c1\u0001\u0000\u0000\u0000\u04c3\u04c4\u0001\u0000\u0000\u0000"+
		"\u04c4\u04c6\u0001\u0000\u0000\u0000\u04c5\u04b7\u0001\u0000\u0000\u0000"+
		"\u04c5\u04c6\u0001\u0000\u0000\u0000\u04c6\u04c7\u0001\u0000\u0000\u0000"+
		"\u04c7\u04e2\u0005C\u0000\u0000\u04c8\u04e3\u0005u\u0000\u0000\u04c9\u04ca"+
		"\u0005\u00a8\u0000\u0000\u04ca\u04cd\u0005\u0099\u0000\u0000\u04cb\u04ce"+
		"\u0003\u00ba]\u0000\u04cc\u04ce\u0003\u0082A\u0000\u04cd\u04cb\u0001\u0000"+
		"\u0000\u0000\u04cd\u04cc\u0001\u0000\u0000\u0000\u04ce\u04cf\u0001\u0000"+
		"\u0000\u0000\u04cf\u04d0\u0005\u0006\u0000\u0000\u04d0\u04db\u0003>\u001f"+
		"\u0000\u04d1\u04d4\u0005\u0005\u0000\u0000\u04d2\u04d5\u0003\u00ba]\u0000"+
		"\u04d3\u04d5\u0003\u0082A\u0000\u04d4\u04d2\u0001\u0000\u0000\u0000\u04d4"+
		"\u04d3\u0001\u0000\u0000\u0000\u04d5\u04d6\u0001\u0000\u0000\u0000\u04d6"+
		"\u04d7\u0005\u0006\u0000\u0000\u04d7\u04d8\u0003>\u001f\u0000\u04d8\u04da"+
		"\u0001\u0000\u0000\u0000\u04d9\u04d1\u0001\u0000\u0000\u0000\u04da\u04dd"+
		"\u0001\u0000\u0000\u0000\u04db\u04d9\u0001\u0000\u0000\u0000\u04db\u04dc"+
		"\u0001\u0000\u0000\u0000\u04dc\u04e0\u0001\u0000\u0000\u0000\u04dd\u04db"+
		"\u0001\u0000\u0000\u0000\u04de\u04df\u0005\u00af\u0000\u0000\u04df\u04e1"+
		"\u0003>\u001f\u0000\u04e0\u04de\u0001\u0000\u0000\u0000\u04e0\u04e1\u0001"+
		"\u0000\u0000\u0000\u04e1\u04e3\u0001\u0000\u0000\u0000\u04e2\u04c8\u0001"+
		"\u0000\u0000\u0000\u04e2\u04c9\u0001\u0000\u0000\u0000\u04e3i\u0001\u0000"+
		"\u0000\u0000\u04e4\u04e8\u0005\u0083\u0000\u0000\u04e5\u04e6\u0003\u00b4"+
		"Z\u0000\u04e6\u04e7\u0005\u0002\u0000\u0000\u04e7\u04e9\u0001\u0000\u0000"+
		"\u0000\u04e8\u04e5\u0001\u0000\u0000\u0000\u04e8\u04e9\u0001\u0000\u0000"+
		"\u0000\u04e9\u04ea\u0001\u0000\u0000\u0000\u04ea\u04f1\u0003\u00ccf\u0000"+
		"\u04eb\u04ec\u0005\u0006\u0000\u0000\u04ec\u04f2\u0003l6\u0000\u04ed\u04ee"+
		"\u0005\u0003\u0000\u0000\u04ee\u04ef\u0003l6\u0000\u04ef\u04f0\u0005\u0004"+
		"\u0000\u0000\u04f0\u04f2\u0001\u0000\u0000\u0000\u04f1\u04eb\u0001\u0000"+
		"\u0000\u0000\u04f1\u04ed\u0001\u0000\u0000\u0000\u04f1\u04f2\u0001\u0000"+
		"\u0000\u0000\u04f2k\u0001\u0000\u0000\u0000\u04f3\u04f7\u0003$\u0012\u0000"+
		"\u04f4\u04f7\u0003\u00b0X\u0000\u04f5\u04f7\u0005\u00b7\u0000\u0000\u04f6"+
		"\u04f3\u0001\u0000\u0000\u0000\u04f6\u04f4\u0001\u0000\u0000\u0000\u04f6"+
		"\u04f5\u0001\u0000\u0000\u0000\u04f7m\u0001\u0000\u0000\u0000\u04f8\u0503"+
		"\u0005\u008c\u0000\u0000\u04f9\u0504\u0003\u00c0`\u0000\u04fa\u04fb\u0003"+
		"\u00b4Z\u0000\u04fb\u04fc\u0005\u0002\u0000\u0000\u04fc\u04fe\u0001\u0000"+
		"\u0000\u0000\u04fd\u04fa\u0001\u0000\u0000\u0000\u04fd\u04fe\u0001\u0000"+
		"\u0000\u0000\u04fe\u0501\u0001\u0000\u0000\u0000\u04ff\u0502\u0003\u00b6"+
		"[\u0000\u0500\u0502\u0003\u00c4b\u0000\u0501\u04ff\u0001\u0000\u0000\u0000"+
		"\u0501\u0500\u0001\u0000\u0000\u0000\u0502\u0504\u0001\u0000\u0000\u0000"+
		"\u0503\u04f9\u0001\u0000\u0000\u0000\u0503\u04fd\u0001\u0000\u0000\u0000"+
		"\u0503\u0504\u0001\u0000\u0000\u0000\u0504o\u0001\u0000\u0000\u0000\u0505"+
		"\u0507\u00032\u0019\u0000\u0506\u0505\u0001\u0000\u0000\u0000\u0506\u0507"+
		"\u0001\u0000\u0000\u0000\u0507\u0508\u0001\u0000\u0000\u0000\u0508\u050e"+
		"\u0003t:\u0000\u0509\u050a\u0003~?\u0000\u050a\u050b\u0003t:\u0000\u050b"+
		"\u050d\u0001\u0000\u0000\u0000\u050c\u0509\u0001\u0000\u0000\u0000\u050d"+
		"\u0510\u0001\u0000\u0000\u0000\u050e\u050c\u0001\u0000\u0000\u0000\u050e"+
		"\u050f\u0001\u0000\u0000\u0000\u050f\u0512\u0001\u0000\u0000\u0000\u0510"+
		"\u050e\u0001\u0000\u0000\u0000\u0511\u0513\u0003\u0092I\u0000\u0512\u0511"+
		"\u0001\u0000\u0000\u0000\u0512\u0513\u0001\u0000\u0000\u0000\u0513\u0515"+
		"\u0001\u0000\u0000\u0000\u0514\u0516\u0003\u0094J\u0000\u0515\u0514\u0001"+
		"\u0000\u0000\u0000\u0515\u0516\u0001\u0000\u0000\u0000\u0516q\u0001\u0000"+
		"\u0000\u0000\u0517\u051f\u0003v;\u0000\u0518\u0519\u0003z=\u0000\u0519"+
		"\u051b\u0003v;\u0000\u051a\u051c\u0003|>\u0000\u051b\u051a\u0001\u0000"+
		"\u0000\u0000\u051b\u051c\u0001\u0000\u0000\u0000\u051c\u051e\u0001\u0000"+
		"\u0000\u0000\u051d\u0518\u0001\u0000\u0000\u0000\u051e\u0521\u0001\u0000"+
		"\u0000\u0000\u051f\u051d\u0001\u0000\u0000\u0000\u051f\u0520\u0001\u0000"+
		"\u0000\u0000\u0520s\u0001\u0000\u0000\u0000\u0521\u051f\u0001\u0000\u0000"+
		"\u0000\u0522\u0524\u0005\u0098\u0000\u0000\u0523\u0525\u0007\u0013\u0000"+
		"\u0000\u0524\u0523\u0001\u0000\u0000\u0000\u0524\u0525\u0001\u0000\u0000"+
		"\u0000\u0525\u0526\u0001\u0000\u0000\u0000\u0526\u052b\u0003x<\u0000\u0527"+
		"\u0528\u0005\u0005\u0000\u0000\u0528\u052a\u0003x<\u0000\u0529\u0527\u0001"+
		"\u0000\u0000\u0000\u052a\u052d\u0001\u0000\u0000\u0000\u052b\u0529\u0001"+
		"\u0000\u0000\u0000\u052b\u052c\u0001\u0000\u0000\u0000\u052c\u0530\u0001"+
		"\u0000\u0000\u0000\u052d\u052b\u0001\u0000\u0000\u0000\u052e\u052f\u0005"+
		"U\u0000\u0000\u052f\u0531\u0003r9\u0000\u0530\u052e\u0001\u0000\u0000"+
		"\u0000\u0530\u0531\u0001\u0000\u0000\u0000\u0531\u0534\u0001\u0000\u0000"+
		"\u0000\u0532\u0533\u0005\u00af\u0000\u0000\u0533\u0535\u0003>\u001f\u0000"+
		"\u0534\u0532\u0001\u0000\u0000\u0000\u0534\u0535\u0001\u0000\u0000\u0000"+
		"\u0535\u0544\u0001\u0000\u0000\u0000\u0536\u0537\u0005Y\u0000\u0000\u0537"+
		"\u0538\u0005+\u0000\u0000\u0538\u053d\u0003>\u001f\u0000\u0539\u053a\u0005"+
		"\u0005\u0000\u0000\u053a\u053c\u0003>\u001f\u0000\u053b\u0539\u0001\u0000"+
		"\u0000\u0000\u053c\u053f\u0001\u0000\u0000\u0000\u053d\u053b\u0001\u0000"+
		"\u0000\u0000\u053d\u053e\u0001\u0000\u0000\u0000\u053e\u0542\u0001\u0000"+
		"\u0000\u0000\u053f\u053d\u0001\u0000\u0000\u0000\u0540\u0541\u0005[\u0000"+
		"\u0000\u0541\u0543\u0003>\u001f\u0000\u0542\u0540\u0001\u0000\u0000\u0000"+
		"\u0542\u0543\u0001\u0000\u0000\u0000\u0543\u0545\u0001\u0000\u0000\u0000"+
		"\u0544\u0536\u0001\u0000\u0000\u0000\u0544\u0545\u0001\u0000\u0000\u0000"+
		"\u0545\u0554\u0001\u0000\u0000\u0000\u0546\u0547\u0005\u00b0\u0000\u0000"+
		"\u0547\u0548\u0003\u00d4j\u0000\u0548\u0549\u0005$\u0000\u0000\u0549\u0551"+
		"\u0003\u008aE\u0000\u054a\u054b\u0005\u0005\u0000\u0000\u054b\u054c\u0003"+
		"\u00d4j\u0000\u054c\u054d\u0005$\u0000\u0000\u054d\u054e\u0003\u008aE"+
		"\u0000\u054e\u0550\u0001\u0000\u0000\u0000\u054f\u054a\u0001\u0000\u0000"+
		"\u0000\u0550\u0553\u0001\u0000\u0000\u0000\u0551\u054f\u0001\u0000\u0000"+
		"\u0000\u0551\u0552\u0001\u0000\u0000\u0000\u0552\u0555\u0001\u0000\u0000"+
		"\u0000\u0553\u0551\u0001\u0000\u0000\u0000\u0554\u0546\u0001\u0000\u0000"+
		"\u0000\u0554\u0555\u0001\u0000\u0000\u0000\u0555\u0558\u0001\u0000\u0000"+
		"\u0000\u0556\u0558\u0003b1\u0000\u0557\u0522\u0001\u0000\u0000\u0000\u0557"+
		"\u0556\u0001\u0000\u0000\u0000\u0558u\u0001\u0000\u0000\u0000\u0559\u055a"+
		"\u0003\u00b4Z\u0000\u055a\u055b\u0005\u0002\u0000\u0000\u055b\u055d\u0001"+
		"\u0000\u0000\u0000\u055c\u0559\u0001\u0000\u0000\u0000\u055c\u055d\u0001"+
		"\u0000\u0000\u0000\u055d\u055e\u0001\u0000\u0000\u0000\u055e\u0562\u0003"+
		"\u00b6[\u0000\u055f\u0560\u0005$\u0000\u0000\u0560\u0563\u0003\u00d0h"+
		"\u0000\u0561\u0563\u0003\u00d2i\u0000\u0562\u055f\u0001\u0000\u0000\u0000"+
		"\u0562\u0561\u0001\u0000\u0000\u0000\u0562\u0563\u0001\u0000\u0000\u0000"+
		"\u0563\u0569\u0001\u0000\u0000\u0000\u0564\u0565\u0005a\u0000\u0000\u0565"+
		"\u0566\u0005+\u0000\u0000\u0566\u056a\u0003\u00c4b\u0000\u0567\u0568\u0005"+
		"t\u0000\u0000\u0568\u056a\u0005a\u0000\u0000\u0569\u0564\u0001\u0000\u0000"+
		"\u0000\u0569\u0567\u0001\u0000\u0000\u0000\u0569\u056a\u0001\u0000\u0000"+
		"\u0000\u056a\u058f\u0001\u0000\u0000\u0000\u056b\u056c\u0003\u00b4Z\u0000"+
		"\u056c\u056d\u0005\u0002\u0000\u0000\u056d\u056f\u0001\u0000\u0000\u0000"+
		"\u056e\u056b\u0001\u0000\u0000\u0000\u056e\u056f\u0001\u0000\u0000\u0000"+
		"\u056f\u0570\u0001\u0000\u0000\u0000\u0570\u0571\u0003\u00dam\u0000\u0571"+
		"\u0572\u0005\u0003\u0000\u0000\u0572\u0577\u0003>\u001f\u0000\u0573\u0574"+
		"\u0005\u0005\u0000\u0000\u0574\u0576\u0003>\u001f\u0000\u0575\u0573\u0001"+
		"\u0000\u0000\u0000\u0576\u0579\u0001\u0000\u0000\u0000\u0577\u0575\u0001"+
		"\u0000\u0000\u0000\u0577\u0578\u0001\u0000\u0000\u0000\u0578\u057a\u0001"+
		"\u0000\u0000\u0000\u0579\u0577\u0001\u0000\u0000\u0000\u057a\u057f\u0005"+
		"\u0004\u0000\u0000\u057b\u057d\u0005$\u0000\u0000\u057c\u057b\u0001\u0000"+
		"\u0000\u0000\u057c\u057d\u0001\u0000\u0000\u0000\u057d\u057e\u0001\u0000"+
		"\u0000\u0000\u057e\u0580\u0003\u00d0h\u0000\u057f\u057c\u0001\u0000\u0000"+
		"\u0000\u057f\u0580\u0001\u0000\u0000\u0000\u0580\u058f\u0001\u0000\u0000"+
		"\u0000\u0581\u0582\u0005\u0003\u0000\u0000\u0582\u0583\u0003r9\u0000\u0583"+
		"\u0584\u0005\u0004\u0000\u0000\u0584\u058f\u0001\u0000\u0000\u0000\u0585"+
		"\u0586\u0005\u0003\u0000\u0000\u0586\u0587\u0003p8\u0000\u0587\u058c\u0005"+
		"\u0004\u0000\u0000\u0588\u058a\u0005$\u0000\u0000\u0589\u0588\u0001\u0000"+
		"\u0000\u0000\u0589\u058a\u0001\u0000\u0000\u0000\u058a\u058b\u0001\u0000"+
		"\u0000\u0000\u058b\u058d\u0003\u00d0h\u0000\u058c\u0589\u0001\u0000\u0000"+
		"\u0000\u058c\u058d\u0001\u0000\u0000\u0000\u058d\u058f\u0001\u0000\u0000"+
		"\u0000\u058e\u055c\u0001\u0000\u0000\u0000\u058e\u056e\u0001\u0000\u0000"+
		"\u0000\u058e\u0581\u0001\u0000\u0000\u0000\u058e\u0585\u0001\u0000\u0000"+
		"\u0000\u058fw\u0001\u0000\u0000\u0000\u0590\u059d\u0005\u0007\u0000\u0000"+
		"\u0591\u0592\u0003\u00b6[\u0000\u0592\u0593\u0005\u0002\u0000\u0000\u0593"+
		"\u0594\u0005\u0007\u0000\u0000\u0594\u059d\u0001\u0000\u0000\u0000\u0595"+
		"\u059a\u0003>\u001f\u0000\u0596\u0598\u0005$\u0000\u0000\u0597\u0596\u0001"+
		"\u0000\u0000\u0000\u0597\u0598\u0001\u0000\u0000\u0000\u0598\u0599\u0001"+
		"\u0000\u0000\u0000\u0599\u059b\u0003\u00be_\u0000\u059a\u0597\u0001\u0000"+
		"\u0000\u0000\u059a\u059b\u0001\u0000\u0000\u0000\u059b\u059d\u0001\u0000"+
		"\u0000\u0000\u059c\u0590\u0001\u0000\u0000\u0000\u059c\u0591\u0001\u0000"+
		"\u0000\u0000\u059c\u0595\u0001\u0000\u0000\u0000\u059dy\u0001\u0000\u0000"+
		"\u0000\u059e\u05ac\u0005\u0005\u0000\u0000\u059f\u05a1\u0005r\u0000\u0000"+
		"\u05a0\u059f\u0001\u0000\u0000\u0000\u05a0\u05a1\u0001\u0000\u0000\u0000"+
		"\u05a1\u05a8\u0001\u0000\u0000\u0000\u05a2\u05a4\u0007\u0014\u0000\u0000"+
		"\u05a3\u05a5\u0005\u007f\u0000\u0000\u05a4\u05a3\u0001\u0000\u0000\u0000"+
		"\u05a4\u05a5\u0001\u0000\u0000\u0000\u05a5\u05a9\u0001\u0000\u0000\u0000"+
		"\u05a6\u05a9\u0005c\u0000\u0000\u05a7\u05a9\u00056\u0000\u0000\u05a8\u05a2"+
		"\u0001\u0000\u0000\u0000\u05a8\u05a6\u0001\u0000\u0000\u0000\u05a8\u05a7"+
		"\u0001\u0000\u0000\u0000\u05a8\u05a9\u0001\u0000\u0000\u0000\u05a9\u05aa"+
		"\u0001\u0000\u0000\u0000\u05aa\u05ac\u0005j\u0000\u0000\u05ab\u059e\u0001"+
		"\u0000\u0000\u0000\u05ab\u05a0\u0001\u0000\u0000\u0000\u05ac{\u0001\u0000"+
		"\u0000\u0000\u05ad\u05ae\u0005{\u0000\u0000\u05ae\u05bc\u0003>\u001f\u0000"+
		"\u05af\u05b0\u0005\u00a9\u0000\u0000\u05b0\u05b1\u0005\u0003\u0000\u0000"+
		"\u05b1\u05b6\u0003\u00ba]\u0000\u05b2\u05b3\u0005\u0005\u0000\u0000\u05b3"+
		"\u05b5\u0003\u00ba]\u0000\u05b4\u05b2\u0001\u0000\u0000\u0000\u05b5\u05b8"+
		"\u0001\u0000\u0000\u0000\u05b6\u05b4\u0001\u0000\u0000\u0000\u05b6\u05b7"+
		"\u0001\u0000\u0000\u0000\u05b7\u05b9\u0001\u0000\u0000\u0000\u05b8\u05b6"+
		"\u0001\u0000\u0000\u0000\u05b9\u05ba\u0005\u0004\u0000\u0000\u05ba\u05bc"+
		"\u0001\u0000\u0000\u0000\u05bb\u05ad\u0001\u0000\u0000\u0000\u05bb\u05af"+
		"\u0001\u0000\u0000\u0000\u05bc}\u0001\u0000\u0000\u0000\u05bd\u05bf\u0005"+
		"\u00a6\u0000\u0000\u05be\u05c0\u0005\u001f\u0000\u0000\u05bf\u05be\u0001"+
		"\u0000\u0000\u0000\u05bf\u05c0\u0001\u0000\u0000\u0000\u05c0\u05c4\u0001"+
		"\u0000\u0000\u0000\u05c1\u05c4\u0005f\u0000\u0000\u05c2\u05c4\u0005I\u0000"+
		"\u0000\u05c3\u05bd\u0001\u0000\u0000\u0000\u05c3\u05c1\u0001\u0000\u0000"+
		"\u0000\u05c3\u05c2\u0001\u0000\u0000\u0000\u05c4\u007f\u0001\u0000\u0000"+
		"\u0000\u05c5\u05c7\u00032\u0019\u0000\u05c6\u05c5\u0001\u0000\u0000\u0000"+
		"\u05c6\u05c7\u0001\u0000\u0000\u0000\u05c7\u05c8\u0001\u0000\u0000\u0000"+
		"\u05c8\u05cb\u0005\u00a8\u0000\u0000\u05c9\u05ca\u0005|\u0000\u0000\u05ca"+
		"\u05cc\u0007\b\u0000\u0000\u05cb\u05c9\u0001\u0000\u0000\u0000\u05cb\u05cc"+
		"\u0001\u0000\u0000\u0000\u05cc\u05cd\u0001\u0000\u0000\u0000\u05cd\u05ce"+
		"\u0003\u0084B\u0000\u05ce\u05d1\u0005\u0099\u0000\u0000\u05cf\u05d2\u0003"+
		"\u00ba]\u0000\u05d0\u05d2\u0003\u0082A\u0000\u05d1\u05cf\u0001\u0000\u0000"+
		"\u0000\u05d1\u05d0\u0001\u0000\u0000\u0000\u05d2\u05d3\u0001\u0000\u0000"+
		"\u0000\u05d3\u05d4\u0005\u0006\u0000\u0000\u05d4\u05df\u0003>\u001f\u0000"+
		"\u05d5\u05d8\u0005\u0005\u0000\u0000\u05d6\u05d9\u0003\u00ba]\u0000\u05d7"+
		"\u05d9\u0003\u0082A\u0000\u05d8\u05d6\u0001\u0000\u0000\u0000\u05d8\u05d7"+
		"\u0001\u0000\u0000\u0000\u05d9\u05da\u0001\u0000\u0000\u0000\u05da\u05db"+
		"\u0005\u0006\u0000\u0000\u05db\u05dc\u0003>\u001f\u0000\u05dc\u05de\u0001"+
		"\u0000\u0000\u0000\u05dd\u05d5\u0001\u0000\u0000\u0000\u05de\u05e1\u0001"+
		"\u0000\u0000\u0000\u05df\u05dd\u0001\u0000\u0000\u0000\u05df\u05e0\u0001"+
		"\u0000\u0000\u0000\u05e0\u05e4\u0001\u0000\u0000\u0000\u05e1\u05df\u0001"+
		"\u0000\u0000\u0000\u05e2\u05e3\u0005U\u0000\u0000\u05e3\u05e5\u0003r9"+
		"\u0000\u05e4\u05e2\u0001\u0000\u0000\u0000\u05e4\u05e5\u0001\u0000\u0000"+
		"\u0000\u05e5\u05e8\u0001\u0000\u0000\u0000\u05e6\u05e7\u0005\u00af\u0000"+
		"\u0000\u05e7\u05e9\u0003>\u001f\u0000\u05e8\u05e6\u0001\u0000\u0000\u0000"+
		"\u05e8\u05e9\u0001\u0000\u0000\u0000\u05e9\u05eb\u0001\u0000\u0000\u0000"+
		"\u05ea\u05ec\u0003f3\u0000\u05eb\u05ea\u0001\u0000\u0000\u0000\u05eb\u05ec"+
		"\u0001\u0000\u0000\u0000\u05ec\u05ee\u0001\u0000\u0000\u0000\u05ed\u05ef"+
		"\u0003\u0092I\u0000\u05ee\u05ed\u0001\u0000\u0000\u0000\u05ee\u05ef\u0001"+
		"\u0000\u0000\u0000\u05ef\u05f1\u0001\u0000\u0000\u0000\u05f0\u05f2\u0003"+
		"\u0094J\u0000\u05f1\u05f0\u0001\u0000\u0000\u0000\u05f1\u05f2\u0001\u0000"+
		"\u0000\u0000\u05f2\u0081\u0001\u0000\u0000\u0000\u05f3\u05f4\u0005\u0003"+
		"\u0000\u0000\u05f4\u05f9\u0003\u00ba]\u0000\u05f5\u05f6\u0005\u0005\u0000"+
		"\u0000\u05f6\u05f8\u0003\u00ba]\u0000\u05f7\u05f5\u0001\u0000\u0000\u0000"+
		"\u05f8\u05fb\u0001\u0000\u0000\u0000\u05f9\u05f7\u0001\u0000\u0000\u0000"+
		"\u05f9\u05fa\u0001\u0000\u0000\u0000\u05fa\u05fc\u0001\u0000\u0000\u0000"+
		"\u05fb\u05f9\u0001\u0000\u0000\u0000\u05fc\u05fd\u0005\u0004\u0000\u0000"+
		"\u05fd\u0083\u0001\u0000\u0000\u0000\u05fe\u05ff\u0003\u00b4Z\u0000\u05ff"+
		"\u0600\u0005\u0002\u0000\u0000\u0600\u0602\u0001\u0000\u0000\u0000\u0601"+
		"\u05fe\u0001\u0000\u0000\u0000\u0601\u0602\u0001\u0000\u0000\u0000\u0602"+
		"\u0603\u0001\u0000\u0000\u0000\u0603\u0606\u0003\u00b6[\u0000\u0604\u0605"+
		"\u0005$\u0000\u0000\u0605\u0607\u0003\u00d6k\u0000\u0606\u0604\u0001\u0000"+
		"\u0000\u0000\u0606\u0607\u0001\u0000\u0000\u0000\u0607\u060d\u0001\u0000"+
		"\u0000\u0000\u0608\u0609\u0005a\u0000\u0000\u0609\u060a\u0005+\u0000\u0000"+
		"\u060a\u060e\u0003\u00c4b\u0000\u060b\u060c\u0005t\u0000\u0000\u060c\u060e"+
		"\u0005a\u0000\u0000\u060d\u0608\u0001\u0000\u0000\u0000\u060d\u060b\u0001"+
		"\u0000\u0000\u0000\u060d\u060e\u0001\u0000\u0000\u0000\u060e\u0085\u0001"+
		"\u0000\u0000\u0000\u060f\u0611\u0005\u00aa\u0000\u0000\u0610\u0612\u0003"+
		"\u00b4Z\u0000\u0611\u0610\u0001\u0000\u0000\u0000\u0611\u0612\u0001\u0000"+
		"\u0000\u0000\u0612\u0615\u0001\u0000\u0000\u0000\u0613\u0614\u0005g\u0000"+
		"\u0000\u0614\u0616\u0003\u00a2Q\u0000\u0615\u0613\u0001\u0000\u0000\u0000"+
		"\u0615\u0616\u0001\u0000\u0000\u0000\u0616\u0087\u0001\u0000\u0000\u0000"+
		"\u0617\u0618\u0005P\u0000\u0000\u0618\u0619\u0005\u0003\u0000\u0000\u0619"+
		"\u061a\u0005\u00af\u0000\u0000\u061a\u061b\u0003>\u001f\u0000\u061b\u061c"+
		"\u0005\u0004\u0000\u0000\u061c\u0089\u0001\u0000\u0000\u0000\u061d\u061f"+
		"\u0005\u0003\u0000\u0000\u061e\u0620\u0003\u00d8l\u0000\u061f\u061e\u0001"+
		"\u0000\u0000\u0000\u061f\u0620\u0001\u0000\u0000\u0000\u0620\u062b\u0001"+
		"\u0000\u0000\u0000\u0621\u0622\u0005\u0081\u0000\u0000\u0622\u0623\u0005"+
		"+\u0000\u0000\u0623\u0628\u0003>\u001f\u0000\u0624\u0625\u0005\u0005\u0000"+
		"\u0000\u0625\u0627\u0003>\u001f\u0000\u0626\u0624\u0001\u0000\u0000\u0000"+
		"\u0627\u062a\u0001\u0000\u0000\u0000\u0628\u0626\u0001\u0000\u0000\u0000"+
		"\u0628\u0629\u0001\u0000\u0000\u0000\u0629\u062c\u0001\u0000\u0000\u0000"+
		"\u062a\u0628\u0001\u0000\u0000\u0000\u062b\u0621\u0001\u0000\u0000\u0000"+
		"\u062b\u062c\u0001\u0000\u0000\u0000\u062c\u062e\u0001\u0000\u0000\u0000"+
		"\u062d\u062f\u0003\u0092I\u0000\u062e\u062d\u0001\u0000\u0000\u0000\u062e"+
		"\u062f\u0001\u0000\u0000\u0000\u062f\u0631\u0001\u0000\u0000\u0000\u0630"+
		"\u0632\u0003\u008eG\u0000\u0631\u0630\u0001\u0000\u0000\u0000\u0631\u0632"+
		"\u0001\u0000\u0000\u0000\u0632\u0633\u0001\u0000\u0000\u0000\u0633\u0634"+
		"\u0005\u0004\u0000\u0000\u0634\u008b\u0001\u0000\u0000\u0000\u0635\u064e"+
		"\u0005\u0080\u0000\u0000\u0636\u064f\u0003\u00d4j\u0000\u0637\u0639\u0005"+
		"\u0003\u0000\u0000\u0638\u063a\u0003\u00d8l\u0000\u0639\u0638\u0001\u0000"+
		"\u0000\u0000\u0639\u063a\u0001\u0000\u0000\u0000\u063a\u0645\u0001\u0000"+
		"\u0000\u0000\u063b\u063c\u0005\u0081\u0000\u0000\u063c\u063d\u0005+\u0000"+
		"\u0000\u063d\u0642\u0003>\u001f\u0000\u063e\u063f\u0005\u0005\u0000\u0000"+
		"\u063f\u0641\u0003>\u001f\u0000\u0640\u063e\u0001\u0000\u0000\u0000\u0641"+
		"\u0644\u0001\u0000\u0000\u0000\u0642\u0640\u0001\u0000\u0000\u0000\u0642"+
		"\u0643\u0001\u0000\u0000\u0000\u0643\u0646\u0001\u0000\u0000\u0000\u0644"+
		"\u0642\u0001\u0000\u0000\u0000\u0645\u063b\u0001\u0000\u0000\u0000\u0645"+
		"\u0646\u0001\u0000\u0000\u0000\u0646\u0648\u0001\u0000\u0000\u0000\u0647"+
		"\u0649\u0003\u0092I\u0000\u0648\u0647\u0001\u0000\u0000\u0000\u0648\u0649"+
		"\u0001\u0000\u0000\u0000\u0649\u064b\u0001\u0000\u0000\u0000\u064a\u064c"+
		"\u0003\u008eG\u0000\u064b\u064a\u0001\u0000\u0000\u0000\u064b\u064c\u0001"+
		"\u0000\u0000\u0000\u064c\u064d\u0001\u0000\u0000\u0000\u064d\u064f\u0005"+
		"\u0004\u0000\u0000\u064e\u0636\u0001\u0000\u0000\u0000\u064e\u0637\u0001"+
		"\u0000\u0000\u0000\u064f\u008d\u0001\u0000\u0000\u0000\u0650\u065a\u0003"+
		"\u0090H\u0000\u0651\u0658\u0005J\u0000\u0000\u0652\u0653\u0005s\u0000"+
		"\u0000\u0653\u0659\u0005~\u0000\u0000\u0654\u0655\u00057\u0000\u0000\u0655"+
		"\u0659\u0005\u0094\u0000\u0000\u0656\u0659\u0005Y\u0000\u0000\u0657\u0659"+
		"\u0005\u00a0\u0000\u0000\u0658\u0652\u0001\u0000\u0000\u0000\u0658\u0654"+
		"\u0001\u0000\u0000\u0000\u0658\u0656\u0001\u0000\u0000\u0000\u0658\u0657"+
		"\u0001\u0000\u0000\u0000\u0659\u065b\u0001\u0000\u0000\u0000\u065a\u0651"+
		"\u0001\u0000\u0000\u0000\u065a\u065b\u0001\u0000\u0000\u0000\u065b\u008f"+
		"\u0001\u0000\u0000\u0000\u065c\u0663\u0007\u0015\u0000\u0000\u065d\u0664"+
		"\u0003\u009eO\u0000\u065e\u065f\u0005*\u0000\u0000\u065f\u0660\u0003\u009a"+
		"M\u0000\u0660\u0661\u0005#\u0000\u0000\u0661\u0662\u0003\u009cN\u0000"+
		"\u0662\u0664\u0001\u0000\u0000\u0000\u0663\u065d\u0001\u0000\u0000\u0000"+
		"\u0663\u065e\u0001\u0000\u0000\u0000\u0664\u0091\u0001\u0000\u0000\u0000"+
		"\u0665\u0666\u0005}\u0000\u0000\u0666\u0667\u0005+\u0000\u0000\u0667\u066c"+
		"\u0003\u0096K\u0000\u0668\u0669\u0005\u0005\u0000\u0000\u0669\u066b\u0003"+
		"\u0096K\u0000\u066a\u0668\u0001\u0000\u0000\u0000\u066b\u066e\u0001\u0000"+
		"\u0000\u0000\u066c\u066a\u0001\u0000\u0000\u0000\u066c\u066d\u0001\u0000"+
		"\u0000\u0000\u066d\u0093\u0001\u0000\u0000\u0000\u066e\u066c\u0001\u0000"+
		"\u0000\u0000\u066f\u0670\u0005o\u0000\u0000\u0670\u0673\u0003>\u001f\u0000"+
		"\u0671\u0672\u0007\u0016\u0000\u0000\u0672\u0674\u0003>\u001f\u0000\u0673"+
		"\u0671\u0001\u0000\u0000\u0000\u0673\u0674\u0001\u0000\u0000\u0000\u0674"+
		"\u0095\u0001\u0000\u0000\u0000\u0675\u0678\u0003>\u001f\u0000\u0676\u0677"+
		"\u00050\u0000\u0000\u0677\u0679\u0003\u00c0`\u0000\u0678\u0676\u0001\u0000"+
		"\u0000\u0000\u0678\u0679\u0001\u0000\u0000\u0000\u0679\u067b\u0001\u0000"+
		"\u0000\u0000\u067a\u067c\u0003\u0098L\u0000\u067b\u067a\u0001\u0000\u0000"+
		"\u0000\u067b\u067c\u0001\u0000\u0000\u0000\u067c\u067f\u0001\u0000\u0000"+
		"\u0000\u067d\u067e\u0005x\u0000\u0000\u067e\u0680\u0007\u0017\u0000\u0000"+
		"\u067f\u067d\u0001\u0000\u0000\u0000\u067f\u0680\u0001\u0000\u0000\u0000"+
		"\u0680\u0097\u0001\u0000\u0000\u0000\u0681\u0682\u0007\u0018\u0000\u0000"+
		"\u0682\u0099\u0001\u0000\u0000\u0000\u0683\u0684\u0003>\u001f\u0000\u0684"+
		"\u0685\u0005\u0084\u0000\u0000\u0685\u068c\u0001\u0000\u0000\u0000\u0686"+
		"\u0687\u0003>\u001f\u0000\u0687\u0688\u0005R\u0000\u0000\u0688\u068c\u0001"+
		"\u0000\u0000\u0000\u0689\u068a\u00057\u0000\u0000\u068a\u068c\u0005\u0094"+
		"\u0000\u0000\u068b\u0683\u0001\u0000\u0000\u0000\u068b\u0686\u0001\u0000"+
		"\u0000\u0000\u068b\u0689\u0001\u0000\u0000\u0000\u068c\u009b\u0001\u0000"+
		"\u0000\u0000\u068d\u068e\u0003>\u001f\u0000\u068e\u068f\u0005\u0084\u0000"+
		"\u0000\u068f\u0696\u0001\u0000\u0000\u0000\u0690\u0691\u0003>\u001f\u0000"+
		"\u0691\u0692\u0005R\u0000\u0000\u0692\u0696\u0001\u0000\u0000\u0000\u0693"+
		"\u0694\u00057\u0000\u0000\u0694\u0696\u0005\u0094\u0000\u0000\u0695\u068d"+
		"\u0001\u0000\u0000\u0000\u0695\u0690\u0001\u0000\u0000\u0000\u0695\u0693"+
		"\u0001\u0000\u0000\u0000\u0696\u009d\u0001\u0000\u0000\u0000\u0697\u0698"+
		"\u0003>\u001f\u0000\u0698\u0699\u0005\u0084\u0000\u0000\u0699\u069d\u0001"+
		"\u0000\u0000\u0000\u069a\u069b\u00057\u0000\u0000\u069b\u069d\u0005\u0094"+
		"\u0000\u0000\u069c\u0697\u0001\u0000\u0000\u0000\u069c\u069a\u0001\u0000"+
		"\u0000\u0000\u069d\u009f\u0001\u0000\u0000\u0000\u069e\u069f\u0003>\u001f"+
		"\u0000\u069f\u00a1\u0001\u0000\u0000\u0000\u06a0\u06a1\u0003>\u001f\u0000"+
		"\u06a1\u00a3\u0001\u0000\u0000\u0000\u06a2\u06a4\u0003\u00a6S\u0000\u06a3"+
		"\u06a2\u0001\u0000\u0000\u0000\u06a4\u06a7\u0001\u0000\u0000\u0000\u06a5"+
		"\u06a3\u0001\u0000\u0000\u0000\u06a5\u06a6\u0001\u0000\u0000\u0000\u06a6"+
		"\u00a5\u0001\u0000\u0000\u0000\u06a7\u06a5\u0001\u0000\u0000\u0000\u06a8"+
		"\u06b2\b\u0019\u0000\u0000\u06a9\u06ad\u0005\u0003\u0000\u0000\u06aa\u06ac"+
		"\u0003\u00a8T\u0000\u06ab\u06aa\u0001\u0000\u0000\u0000\u06ac\u06af\u0001"+
		"\u0000\u0000\u0000\u06ad\u06ab\u0001\u0000\u0000\u0000\u06ad\u06ae\u0001"+
		"\u0000\u0000\u0000\u06ae\u06b0\u0001\u0000\u0000\u0000\u06af\u06ad\u0001"+
		"\u0000\u0000\u0000\u06b0\u06b2\u0005\u0004\u0000\u0000\u06b1\u06a8\u0001"+
		"\u0000\u0000\u0000\u06b1\u06a9\u0001\u0000\u0000\u0000\u06b2\u00a7\u0001"+
		"\u0000\u0000\u0000\u06b3\u06bd\b\u001a\u0000\u0000\u06b4\u06b8\u0005\u0003"+
		"\u0000\u0000\u06b5\u06b7\u0003\u00a8T\u0000\u06b6\u06b5\u0001\u0000\u0000"+
		"\u0000\u06b7\u06ba\u0001\u0000\u0000\u0000\u06b8\u06b6\u0001\u0000\u0000"+
		"\u0000\u06b8\u06b9\u0001\u0000\u0000\u0000\u06b9\u06bb\u0001\u0000\u0000"+
		"\u0000\u06ba\u06b8\u0001\u0000\u0000\u0000\u06bb\u06bd\u0005\u0004\u0000"+
		"\u0000\u06bc\u06b3\u0001\u0000\u0000\u0000\u06bc\u06b4\u0001\u0000\u0000"+
		"\u0000\u06bd\u00a9\u0001\u0000\u0000\u0000\u06be\u06bf\u0007\u001b\u0000"+
		"\u0000\u06bf\u00ab\u0001\u0000\u0000\u0000\u06c0\u06c1\u0007\u001c\u0000"+
		"\u0000\u06c1\u00ad\u0001\u0000\u0000\u0000\u06c2\u06c6\u0003\u00aaU\u0000"+
		"\u06c3\u06c6\u0003\u00acV\u0000\u06c4\u06c6\u0005\u0087\u0000\u0000\u06c5"+
		"\u06c2\u0001\u0000\u0000\u0000\u06c5\u06c3\u0001\u0000\u0000\u0000\u06c5"+
		"\u06c4\u0001\u0000\u0000\u0000\u06c6\u00af\u0001\u0000\u0000\u0000\u06c7"+
		"\u06c8\u0003\u00e2q\u0000\u06c8\u00b1\u0001\u0000\u0000\u0000\u06c9\u06ca"+
		"\u0003\u00dcn\u0000\u06ca\u00b3\u0001\u0000\u0000\u0000\u06cb\u06cc\u0003"+
		"\u00e2q\u0000\u06cc\u00b5\u0001\u0000\u0000\u0000\u06cd\u06ce\u0003\u00e2"+
		"q\u0000\u06ce\u00b7\u0001\u0000\u0000\u0000\u06cf\u06d0\u0003\u00e2q\u0000"+
		"\u06d0\u00b9\u0001\u0000\u0000\u0000\u06d1\u06d2\u0003\u00e2q\u0000\u06d2"+
		"\u00bb\u0001\u0000\u0000\u0000\u06d3\u06d4\u0003\u00e0p\u0000\u06d4\u00bd"+
		"\u0001\u0000\u0000\u0000\u06d5\u06d6\u0003\u00e2q\u0000\u06d6\u00bf\u0001"+
		"\u0000\u0000\u0000\u06d7\u06d8\u0003\u00e2q\u0000\u06d8\u00c1\u0001\u0000"+
		"\u0000\u0000\u06d9\u06da\u0003\u00e2q\u0000\u06da\u00c3\u0001\u0000\u0000"+
		"\u0000\u06db\u06dc\u0003\u00e2q\u0000\u06dc\u00c5\u0001\u0000\u0000\u0000"+
		"\u06dd\u06de\u0003\u00e2q\u0000\u06de\u00c7\u0001\u0000\u0000\u0000\u06df"+
		"\u06e0\u0003\u00e2q\u0000\u06e0\u00c9\u0001\u0000\u0000\u0000\u06e1\u06e2"+
		"\u0003\u00e2q\u0000\u06e2\u00cb\u0001\u0000\u0000\u0000\u06e3\u06e4\u0003"+
		"\u00e2q\u0000\u06e4\u00cd\u0001\u0000\u0000\u0000\u06e5\u06e6\u0003\u00e2"+
		"q\u0000\u06e6\u00cf\u0001\u0000\u0000\u0000\u06e7\u06e8\u0003\u00e2q\u0000"+
		"\u06e8\u00d1\u0001\u0000\u0000\u0000\u06e9\u06ea\u0003\u00deo\u0000\u06ea"+
		"\u00d3\u0001\u0000\u0000\u0000\u06eb\u06ec\u0003\u00e2q\u0000\u06ec\u00d5"+
		"\u0001\u0000\u0000\u0000\u06ed\u06ee\u0003\u00e2q\u0000\u06ee\u00d7\u0001"+
		"\u0000\u0000\u0000\u06ef\u06f0\u0003\u00e2q\u0000\u06f0\u00d9\u0001\u0000"+
		"\u0000\u0000\u06f1\u06f2\u0003\u00e2q\u0000\u06f2\u00db\u0001\u0000\u0000"+
		"\u0000\u06f3\u06f8\u0005\u00b4\u0000\u0000\u06f4\u06f8\u0003\u00aaU\u0000"+
		"\u06f5\u06f8\u0003\u00acV\u0000\u06f6\u06f8\u0005\u00b7\u0000\u0000\u06f7"+
		"\u06f3\u0001\u0000\u0000\u0000\u06f7\u06f4\u0001\u0000\u0000\u0000\u06f7"+
		"\u06f5\u0001\u0000\u0000\u0000\u06f7\u06f6\u0001\u0000\u0000\u0000\u06f8"+
		"\u00dd\u0001\u0000\u0000\u0000\u06f9\u06fe\u0005\u00b4\u0000\u0000\u06fa"+
		"\u06fe\u0003\u00aaU\u0000\u06fb\u06fe\u0005\u0087\u0000\u0000\u06fc\u06fe"+
		"\u0005\u00b7\u0000\u0000\u06fd\u06f9\u0001\u0000\u0000\u0000\u06fd\u06fa"+
		"\u0001\u0000\u0000\u0000\u06fd\u06fb\u0001\u0000\u0000\u0000\u06fd\u06fc"+
		"\u0001\u0000\u0000\u0000\u06fe\u00df\u0001\u0000\u0000\u0000\u06ff\u0702"+
		"\u0005\u00b4\u0000\u0000\u0700\u0702\u0003\u00aeW\u0000\u0701\u06ff\u0001"+
		"\u0000\u0000\u0000\u0701\u0700\u0001\u0000\u0000\u0000\u0702\u00e1\u0001"+
		"\u0000\u0000\u0000\u0703\u0707\u0005\u00b4\u0000\u0000\u0704\u0707\u0003"+
		"\u00aeW\u0000\u0705\u0707\u0005\u00b7\u0000\u0000\u0706\u0703\u0001\u0000"+
		"\u0000\u0000\u0706\u0704\u0001\u0000\u0000\u0000\u0706\u0705\u0001\u0000"+
		"\u0000\u0000\u0707\u00e3\u0001\u0000\u0000\u0000\u0107\u00e8\u00ec\u00f0"+
		"\u00f6\u00f8\u0110\u0117\u011e\u0124\u0128\u012d\u0130\u0137\u013a\u013e"+
		"\u0146\u0149\u014d\u0151\u0155\u0158\u015f\u0165\u016b\u0170\u017b\u0181"+
		"\u0186\u0189\u018d\u0193\u0198\u01a1\u01a8\u01ad\u01b1\u01b6\u01bc\u01c0"+
		"\u01c5\u01ca\u01d0\u01dc\u01e0\u01e5\u01e8\u01eb\u01ee\u01f2\u01f5\u0203"+
		"\u020a\u0211\u0213\u0216\u021c\u0221\u0229\u022e\u023d\u0243\u024d\u0252"+
		"\u025c\u0260\u0262\u0266\u026b\u026d\u0275\u027b\u0280\u0287\u0292\u0295"+
		"\u0297\u029e\u02a2\u02a9\u02af\u02b5\u02bb\u02c0\u02c9\u02ce\u02d9\u02de"+
		"\u02e9\u02ee\u02f2\u02f9\u02ff\u0302\u030f\u0314\u0317\u031e\u0321\u0324"+
		"\u0327\u032b\u0333\u0338\u0343\u034b\u0351\u035b\u035f\u0363\u036b\u0375"+
		"\u0378\u037e\u0384\u038d\u0390\u0394\u0397\u039d\u03a1\u03a7\u03a9\u03b1"+
		"\u03b9\u03c1\u03c9\u03d1\u03d9\u03df\u03e9\u03f1\u03f4\u03fc\u0401\u0408"+
		"\u040c\u040f\u0413\u0416\u0419\u0421\u042f\u0438\u043c\u0440\u0448\u045c"+
		"\u0467\u046b\u0472\u0478\u047d\u0485\u048a\u0490\u0495\u0498\u049e\u04a1"+
		"\u04a3\u04a9\u04ac\u04ae\u04b2\u04bd\u04c3\u04c5\u04cd\u04d4\u04db\u04e0"+
		"\u04e2\u04e8\u04f1\u04f6\u04fd\u0501\u0503\u0506\u050e\u0512\u0515\u051b"+
		"\u051f\u0524\u052b\u0530\u0534\u053d\u0542\u0544\u0551\u0554\u0557\u055c"+
		"\u0562\u0569\u056e\u0577\u057c\u057f\u0589\u058c\u058e\u0597\u059a\u059c"+
		"\u05a0\u05a4\u05a8\u05ab\u05b6\u05bb\u05bf\u05c3\u05c6\u05cb\u05d1\u05d8"+
		"\u05df\u05e4\u05e8\u05eb\u05ee\u05f1\u05f9\u0601\u0606\u060d\u0611\u0615"+
		"\u061f\u0628\u062b\u062e\u0631\u0639\u0642\u0645\u0648\u064b\u064e\u0658"+
		"\u065a\u0663\u066c\u0673\u0678\u067b\u067f\u068b\u0695\u069c\u06a5\u06ad"+
		"\u06b1\u06b8\u06bc\u06c5\u06f7\u06fd\u0701\u0706";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}