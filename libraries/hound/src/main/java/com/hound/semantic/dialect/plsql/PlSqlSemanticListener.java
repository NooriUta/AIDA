// src/main/java/com/hound/semantic/dialect/plsql/PlSqlSemanticListener.java
package com.hound.semantic.dialect.plsql;

import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParserBaseListener;
import com.hound.semantic.engine.CanonicalTokenType;
import com.hound.semantic.listener.BaseSemanticListener;
import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.model.CompensationStats;
import com.hound.semantic.model.RoutineInfo;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PlSqlSemanticListener — Java-аналог Python PlSqlAnalyzerListener.
 *
 * ПРАВИЛА:
 * 1. Никаких ctx.tableview_name() — этого метода нет в Table_ref_auxContext и Create_viewContext
 *    данной версии грамматики. Используем рефлексию или getText().
 * 2. Никаких прямых base.current.put / base.protected — только публичные методы base.
 * 3. Никаких BaseSemanticListener.cleanIdentifier() без static — метод public static.
 */
public class PlSqlSemanticListener extends PlSqlParserBaseListener {

    private static final Logger logger = LoggerFactory.getLogger(PlSqlSemanticListener.class);

    private final BaseSemanticListener base;

    public PlSqlSemanticListener(UniversalSemanticEngine engine) {
        this.base = new BaseSemanticListener(engine) {};
    }

    /** Sets the fallback schema name used when no explicit SCHEMA. prefix is found. */
    public void setDefaultSchema(String dbName) {
        base.defaultSchema = dbName;
    }

    // =========================================================================
    // Процедуры и функции
    // =========================================================================

    // ═══════ Create procedure (toplevel) ═══════
    @Override
    public void enterCreate_procedure_body(PlSqlParser.Create_procedure_bodyContext ctx) {
        String name = ctx.procedure_name() != null
                ? BaseSemanticListener.cleanIdentifier(ctx.procedure_name().getText())
                : "UNKNOWN";
        base.onRoutineEnter(name, "PROCEDURE", base.currentSchema(), null, getStartLine(ctx));
        extractParameters(ctx.parameter());
    }

    @Override
    public void exitCreate_procedure_body(PlSqlParser.Create_procedure_bodyContext ctx) {
        base.onRoutineExit();
    }

    // ═══════ Procedure body (в package) ═══════
    @Override
    public void enterProcedure_body(PlSqlParser.Procedure_bodyContext ctx) {
        String name = ctx.identifier() != null
                ? BaseSemanticListener.cleanIdentifier(ctx.identifier().getText())
                : "UNKNOWN";
        base.onRoutineEnter(name, "PROCEDURE", base.currentSchema(), base.currentPackage(), getStartLine(ctx));
        extractParameters(ctx.parameter());
    }

    @Override
    public void exitProcedure_body(PlSqlParser.Procedure_bodyContext ctx) {
        base.onRoutineExit();
    }

    // ═══════ Create function (toplevel) ═══════
    @Override
    public void enterCreate_function_body(PlSqlParser.Create_function_bodyContext ctx) {
        String name = ctx.function_name() != null
                ? BaseSemanticListener.cleanIdentifier(ctx.function_name().getText())
                : "UNKNOWN";
        base.onRoutineEnter(name, "FUNCTION", base.currentSchema(), null, getStartLine(ctx));
        extractParameters(ctx.parameter());
        if (ctx.type_spec() != null) {
            // Typed extraction: navigate to type_name/datatype leaf; handles %TYPE anchors
            base.onRoutineReturnType(extractTypeSpecText(ctx.type_spec()));
        }
        // HND-09: capture PIPELINED flag from the function declaration keyword
        if (!ctx.PIPELINED().isEmpty()) {
            String rGeoid = base.currentRoutine();
            if (rGeoid != null) {
                var ri = base.engine.getBuilder().getRoutines().get(rGeoid);
                if (ri != null) ri.setPipelined(true);
            }
        }
    }

    @Override
    public void exitCreate_function_body(PlSqlParser.Create_function_bodyContext ctx) {
        base.onRoutineExit();
    }

    // ═══════ Function body (в package / WITH FUNCTION) ═══════
    @Override
    public void enterFunction_body(PlSqlParser.Function_bodyContext ctx) {
        String name = ctx.identifier() != null
                ? BaseSemanticListener.cleanIdentifier(ctx.identifier().getText())
                : "UNKNOWN";
        // KI-WITHFUNC-1: detect WITH FUNCTION (inline SQL function in WITH clause).
        // Parent is With_clauseContext when this is an inline function, not a package body function.
        boolean isInlineFunc = ctx.parent instanceof PlSqlParser.With_clauseContext;
        String routineKind = isInlineFunc ? "INLINE_FUNCTION" : "FUNCTION";
        base.onRoutineEnter(name, routineKind, base.currentSchema(), base.currentPackage(), getStartLine(ctx));
        extractParameters(ctx.parameter());
        if (ctx.type_spec() != null) {
            base.onRoutineReturnType(extractTypeSpecText(ctx.type_spec()));
        }
        // HND-09: capture PIPELINED flag from the function declaration keyword
        if (!ctx.PIPELINED().isEmpty()) {
            String rGeoid = base.currentRoutine();
            if (rGeoid != null) {
                var ri = base.engine.getBuilder().getRoutines().get(rGeoid);
                if (ri != null) ri.setPipelined(true);
            }
        }
    }

    @Override
    public void exitFunction_body(PlSqlParser.Function_bodyContext ctx) {
        base.onRoutineExit();
    }

    // ═══════ Variable declarations ═══════
    @Override
    public void enterVariable_declaration(PlSqlParser.Variable_declarationContext ctx) {
        if (ctx == null || ctx.identifier() == null) return;
        String varName = BaseSemanticListener.cleanIdentifier(ctx.identifier().getText());
        PlSqlParser.Type_specContext ts = ctx.type_spec();
        if (ts == null) return;

        // KI-ROWTYPE-1: typed token check — ts.PERCENT_ROWTYPE() != null
        if (ts.PERCENT_ROWTYPE() != null) {
            String tableRef = ts.type_name() != null
                    ? BaseSemanticListener.cleanIdentifier(ts.type_name().getText()) : "UNKNOWN";
            base.onRowtypeVariable(varName, tableRef);

        } else if (ts.PERCENT_TYPE() != null) {
            // Anchored scalar: varname t_tab.col%TYPE — tracked as plain var
            String anchor = ts.type_name() != null
                    ? ts.type_name().getText() + "%TYPE" : ts.getText();
            base.onRoutineVariable(varName, anchor);

        } else {
            // Regular named type (user-defined) or built-in scalar
            String typeName = ts.type_name() != null
                    ? BaseSemanticListener.cleanIdentifier(ts.type_name().getText())
                    : (ts.datatype() != null ? ts.datatype().getText() : ts.getText());
            // HND-04: detect PL/SQL collection type → materialise DaliRecord from PlTypeInfo
            String resolved = BaseSemanticListener.cleanIdentifier(typeName);
            if (resolved != null && base.engine.getBuilder()
                    .resolvePlTypeByName(resolved, base.currentRoutine()) != null) {
                base.onPlTypeVariable(varName, resolved, ctx.getStart().getLine());
            } else {
                base.onRoutineVariable(varName, typeName != null ? typeName : "UNKNOWN");
            }
        }
    }

    // =========================================================================
    // Пакеты
    // =========================================================================

    /** H1.3: tracks whether we are currently inside a PACKAGE SPEC (vs BODY).
     *  procedure_spec / function_spec are only processed in SPEC context to avoid
     *  double-registration when forward-declarations appear in PACKAGE BODY. */
    private boolean inPackageSpec = false;

    /** H1.3 — CREATE PACKAGE ... IS (specification, no body). */
    @Override
    public void enterCreate_package(PlSqlParser.Create_packageContext ctx) {
        if (ctx.package_name() == null || ctx.package_name().isEmpty()) return;
        String schemaName = null;
        if (ctx.schema_object_name() != null && ctx.PERIOD() != null) {
            schemaName = BaseSemanticListener.cleanIdentifier(ctx.schema_object_name().getText());
            if (schemaName != null && !schemaName.isBlank()) {
                base.onSchemaEnter(schemaName);
            }
        }
        var pkgCtxList = ctx.package_name();
        PlSqlParser.Package_nameContext pkgCtx = pkgCtxList.get(pkgCtxList.size() - 1);
        String packageName = BaseSemanticListener.cleanIdentifier(pkgCtx.getText());
        if (packageName.contains(".")) {
            String[] parts = packageName.split("\\.");
            if (schemaName == null) schemaName = String.join(".", Arrays.copyOf(parts, parts.length - 1));
            packageName = parts[parts.length - 1];
        }
        String effectiveSchema = (schemaName != null && !schemaName.isBlank())
                ? schemaName : base.currentSchema();
        String pkgGeoid = base.initPackage(packageName, effectiveSchema);
        base.setPackage(pkgGeoid);
        inPackageSpec = true;
    }

    @Override
    public void exitCreate_package(PlSqlParser.Create_packageContext ctx) {
        inPackageSpec = false;
        base.setPackage(null);
    }

    /** H1.3 — PROCEDURE identifier (...); in package spec: register as PROCEDURE_SPEC routine. */
    @Override
    public void enterProcedure_spec(PlSqlParser.Procedure_specContext ctx) {
        if (!inPackageSpec || ctx == null || ctx.identifier() == null) return;
        String name = BaseSemanticListener.cleanIdentifier(ctx.identifier().getText());
        base.onRoutineEnter(name, "PROCEDURE_SPEC", base.currentSchema(), base.currentPackage(), getStartLine(ctx));
        extractParameters(ctx.parameter());
    }

    @Override
    public void exitProcedure_spec(PlSqlParser.Procedure_specContext ctx) {
        if (!inPackageSpec) return;
        base.onRoutineExit();
    }

    /** H1.3 — FUNCTION identifier (...) RETURN type; in package spec. */
    @Override
    public void enterFunction_spec(PlSqlParser.Function_specContext ctx) {
        if (!inPackageSpec || ctx == null || ctx.identifier() == null) return;
        String name = BaseSemanticListener.cleanIdentifier(ctx.identifier().getText());
        base.onRoutineEnter(name, "FUNCTION_SPEC", base.currentSchema(), base.currentPackage(), getStartLine(ctx));
        extractParameters(ctx.parameter());
        if (ctx.type_spec() != null) {
            base.onRoutineReturnType(extractTypeSpecText(ctx.type_spec()));
        }
        if (!ctx.PIPELINED().isEmpty()) {
            String rGeoid = base.currentRoutine();
            if (rGeoid != null) {
                var ri = base.engine.getBuilder().getRoutines().get(rGeoid);
                if (ri != null) ri.setPipelined(true);
            }
        }
    }

    @Override
    public void exitFunction_spec(PlSqlParser.Function_specContext ctx) {
        if (!inPackageSpec) return;
        base.onRoutineExit();
    }

    @Override
    public void enterCreate_package_body(PlSqlParser.Create_package_bodyContext ctx) {
        if (ctx.package_name() == null || ctx.package_name().isEmpty()) return;

        // STAB-12 Step 1: читаем схему напрямую из ctx, не через currentSchema().
        // Причина: schema — дочерний узел (schema_object_name), enterSchema_name ещё не сработал.
        // Грамматика: CREATE PACKAGE BODY (schema_object_name PERIOD)? package_name IS ...
        String schemaName = null;
        if (ctx.schema_object_name() != null && ctx.PERIOD() != null) {
            schemaName = BaseSemanticListener.cleanIdentifier(ctx.schema_object_name().getText());
            // Немедленно регистрируем схему, чтобы initPackage её нашёл
            if (schemaName != null && !schemaName.isBlank()) {
                base.onSchemaEnter(schemaName);
            }
        }

        // Берём последний package_name (грамматика может давать список)
        var pkgCtxList = ctx.package_name();
        PlSqlParser.Package_nameContext pkgCtx = pkgCtxList.get(pkgCtxList.size() - 1);
        String packageName = BaseSemanticListener.cleanIdentifier(pkgCtx.getText());

        // Guard: если packageName содержит "." — распарсить (HR.TEST_PKG без schema_name узла)
        if (packageName.contains(".")) {
            String[] parts = packageName.split("\\.");
            if (schemaName == null) schemaName = String.join(".", Arrays.copyOf(parts, parts.length - 1));
            packageName = parts[parts.length - 1];
        }

        String effectiveSchema = (schemaName != null && !schemaName.isBlank())
                ? schemaName
                : base.currentSchema();  // fallback: PACKAGE BODY без schema-prefix

        String pkgGeoid = base.initPackage(packageName, effectiveSchema);
        base.setPackage(pkgGeoid);
    }

    @Override
    public void exitCreate_package_body(PlSqlParser.Create_package_bodyContext ctx) {
        base.setPackage(null);
    }

    // =========================================================================
    // PL/SQL TYPE IS RECORD / TABLE OF  (HND-03)
    // =========================================================================

    @Override
    public void exitType_declaration(PlSqlParser.Type_declarationContext ctx) {
        if (ctx == null || ctx.identifier() == null) return;
        String typeName = BaseSemanticListener.cleanIdentifier(ctx.identifier().getText());
        if (typeName == null || typeName.isBlank()) return;

        // Scope: routine takes priority over package
        String scopeGeoid = base.currentRoutine() != null
                ? base.currentRoutine()
                : base.currentPackage();
        if (scopeGeoid == null) return;

        var builder = base.engine.getBuilder();

        if (ctx.record_type_def() != null) {
            // TYPE t_rec IS RECORD (field1 TYPE1, field2 TYPE2, ...)
            var pt = new com.hound.semantic.model.PlTypeInfo(
                    typeName, com.hound.semantic.model.PlTypeInfo.Kind.RECORD, scopeGeoid);
            pt.setDeclaredAtLine(ctx.start != null ? ctx.start.getLine() : 0);
            var fieldSpecs = ctx.record_type_def().field_spec();
            logger.debug("HND-03 RECORD={} scope={} fieldSpecs={}", typeName, scopeGeoid,
                    fieldSpecs == null ? "null" : fieldSpecs.size());
            if (fieldSpecs != null) {
                for (int i = 0; i < fieldSpecs.size(); i++) {
                    var fs = fieldSpecs.get(i);
                    logger.debug("HND-03  fs[{}] colName={} raw={}", i,
                            fs.column_name() == null ? "NULL" : fs.column_name().getText(),
                            fs.getText());
                    if (fs.column_name() == null) continue;
                    String fieldName = BaseSemanticListener.cleanIdentifier(fs.column_name().getText());
                    // Use typed access: navigate to type_name/datatype leaf rather than whole type_spec
                    String fieldType = extractTypeSpecText(fs.type_spec());
                    pt.addField(fieldName, fieldType, i + 1);
                }
            }
            builder.registerPlType(pt);

        } else if (ctx.table_type_def() != null) {
            // TYPE t_tab IS TABLE OF t_rec INDEX BY PLS_INTEGER
            var pt = new com.hound.semantic.model.PlTypeInfo(
                    typeName, com.hound.semantic.model.PlTypeInfo.Kind.COLLECTION, scopeGeoid);
            pt.setDeclaredAtLine(ctx.start != null ? ctx.start.getLine() : 0);
            // Typed: navigate to the leaf type_name or datatype, not whole type_spec.getText()
            String elemType = extractTypeSpecText(ctx.table_type_def().type_spec());
            if (elemType != null) pt.setElementTypeName(elemType);
            builder.registerPlType(pt);

        } else if (ctx.varray_type_def() != null) {
            // HND-12: TYPE t_arr IS VARRAY(N) OF element_type
            var pt = new com.hound.semantic.model.PlTypeInfo(
                    typeName, com.hound.semantic.model.PlTypeInfo.Kind.VARRAY, scopeGeoid);
            pt.setDeclaredAtLine(ctx.start != null ? ctx.start.getLine() : 0);
            String elemType = extractTypeSpecText(ctx.varray_type_def().type_spec());
            if (elemType != null) pt.setElementTypeName(elemType);
            builder.registerPlType(pt);

        } else if (ctx.ref_cursor_type_def() != null) {
            // HND-13: TYPE t_cur IS REF CURSOR [RETURN type_spec]
            var pt = new com.hound.semantic.model.PlTypeInfo(
                    typeName, com.hound.semantic.model.PlTypeInfo.Kind.REF_CURSOR, scopeGeoid);
            pt.setDeclaredAtLine(ctx.start != null ? ctx.start.getLine() : 0);
            var rc = ctx.ref_cursor_type_def();
            if (rc.type_spec() != null) {
                // strong cursor: RETURN tbl%ROWTYPE or record type — typed extraction
                pt.setElementTypeName(extractTypeSpecText(rc.type_spec()));
            }
            builder.registerPlType(pt);
        }
    }

    // HND-08: CREATE TYPE schema.T_OBJ AS OBJECT (...) / TABLE OF / VARRAY
    @Override
    public void enterCreate_type(PlSqlParser.Create_typeContext ctx) {
        if (ctx == null || ctx.type_definition() == null) return;
        PlSqlParser.Type_definitionContext typeDef = ctx.type_definition();
        if (typeDef.type_name() == null) return;

        // Extract possibly schema-qualified type name: CRM.T_PRICE_BREAK → schema=CRM, name=T_PRICE_BREAK
        var idExprs = typeDef.type_name().id_expression();
        if (idExprs == null || idExprs.isEmpty()) return;
        String schema;
        String typeName;
        if (idExprs.size() >= 2) {
            schema   = BaseSemanticListener.cleanIdentifier(idExprs.get(idExprs.size() - 2).getText());
            typeName = BaseSemanticListener.cleanIdentifier(idExprs.get(idExprs.size() - 1).getText());
        } else {
            schema   = base.currentSchema() != null ? base.currentSchema() : "_GLOBAL_";
            typeName = BaseSemanticListener.cleanIdentifier(idExprs.get(0).getText());
        }
        if (typeName == null || typeName.isBlank()) return;
        // scopeGeoid for schema-level types uses the schema name as a bare key
        String scopeGeoid = (schema != null && !schema.isBlank()) ? schema : "_GLOBAL_";

        PlSqlParser.Object_type_defContext objDef =
                typeDef.object_type_def() != null ? typeDef.object_type_def() : null;
        if (objDef == null || objDef.object_as_part() == null) return;

        var asPart = objDef.object_as_part();
        var builder = base.engine.getBuilder();

        if (asPart.OBJECT() != null) {
            // CREATE TYPE x AS OBJECT (field1 type1, ...)
            var pt = new com.hound.semantic.model.PlTypeInfo(
                    typeName, com.hound.semantic.model.PlTypeInfo.Kind.OBJECT, scopeGeoid);
            int pos = 1;
            for (var member : objDef.object_member_spec()) {
                // Alt 1: identifier() + type_spec() → attribute definition
                if (member.identifier() != null && member.type_spec() != null) {
                    String fieldName = BaseSemanticListener.cleanIdentifier(member.identifier().getText());
                    String fieldType = member.type_spec().getText();
                    pt.addField(fieldName, fieldType, pos++);
                }
                // Alt 2: element_spec() → method / subprogram — skip for lineage
            }
            logger.debug("HND-08 OBJECT={} scope={} fields={}", typeName, scopeGeoid, pt.getFields().size());
            builder.registerPlType(pt);

        } else if (asPart.nested_table_type_def() != null) {
            // CREATE TYPE x AS TABLE OF elem_type — schema-level COLLECTION
            // Typed: navigate to type_name leaf instead of whole type_spec.getText()
            String elemTypeName = extractTypeSpecText(asPart.nested_table_type_def().type_spec());
            var pt = new com.hound.semantic.model.PlTypeInfo(
                    typeName, com.hound.semantic.model.PlTypeInfo.Kind.COLLECTION, scopeGeoid);
            if (elemTypeName != null) pt.setElementTypeName(elemTypeName);
            logger.debug("HND-08 COLLECTION={} scope={} elemType={}", typeName, scopeGeoid, elemTypeName);
            builder.registerPlType(pt);

        } else if (asPart.varray_type_def() != null) {
            // HND-12: CREATE TYPE x AS VARRAY(N) OF elem_type — schema-level VARRAY
            String elemTypeName = extractTypeSpecText(asPart.varray_type_def().type_spec());
            var pt = new com.hound.semantic.model.PlTypeInfo(
                    typeName, com.hound.semantic.model.PlTypeInfo.Kind.VARRAY, scopeGeoid);
            if (elemTypeName != null) pt.setElementTypeName(elemTypeName);
            logger.debug("HND-12 VARRAY={} scope={} elemType={}", typeName, scopeGeoid, elemTypeName);
            builder.registerPlType(pt);
        }
    }

    // =========================================================================
    // Схема
    // =========================================================================

    @Override
    public void enterSchema_name(PlSqlParser.Schema_nameContext ctx) {
        if (ctx == null || ctx.identifier() == null) return;
        base.onSchemaEnter(BaseSemanticListener.cleanIdentifier(ctx.identifier().getText()));
    }

    @Override
    public void exitSchema_name(PlSqlParser.Schema_nameContext ctx) {
        base.onSchemaExit();
    }

    // STAB-13 Part A: schema_object_name is used in DDL (PACKAGE BODY, DROP PACKAGE, etc.)
    // where schema_name is not available in the grammar rule.
    @Override
    public void enterSchema_object_name(PlSqlParser.Schema_object_nameContext ctx) {
        if (ctx == null || ctx.id_expression() == null) return;
        String schemaName = BaseSemanticListener.cleanIdentifier(ctx.id_expression().getText());
        if (schemaName != null && !schemaName.isBlank()) base.onSchemaEnter(schemaName);
    }

    @Override
    public void exitSchema_object_name(PlSqlParser.Schema_object_nameContext ctx) {
        // No reset — DDL constructs manage their own schema lifecycle
    }

    // =========================================================================
    // DML statements
    // =========================================================================

    @Override
    public void enterSelect_statement(PlSqlParser.Select_statementContext ctx) {
        // Python behavior: when a select_statement is the direct body of a parent scope
        // (e.g. DINAMIC_CURSOR: FOR rec IN (SELECT ...) LOOP), isFirstSubq is set to
        // this select's start position by the parent's enter handler (enterLoop_statement).
        // In that case, do NOT push a new SELECT scope — atoms register to the parent.
        Integer firstLine = base.getIsFirstSubq();
        Integer firstCol  = base.getIsFirstSubqp();
        int ctxLine = ctx.start.getLine();
        int ctxCol  = ctx.start.getCharPositionInLine();
        if (firstLine != null && firstLine == ctxLine && firstCol != null && firstCol == ctxCol) {
            suppressedSelects.add(ctx);
            // Advance isFirstSubq to the inner subquery so enterSubquery also skips.
            if (ctx.select_only_statement() != null && ctx.select_only_statement().subquery() != null) {
                var sq = ctx.select_only_statement().subquery();
                base.setIsFirstSubq(sq.start.getLine());
                base.setIsFirstSubqp(sq.start.getCharPositionInLine());
            }
            return;
        }

        // STAB-13 Part B: if this SELECT is in a FROM position (inline subquery FROM (SELECT...) alias),
        // pass the alias so it gets registered on the parent scope for alias resolution.
        // Also handles MERGE USING (SELECT ...) alias — grammar: selected_tableview → '(' select_statement ')'
        // In that case getCurrentParentContext() returns "MERGE" (not "FROM"), so we include it here.
        List<String> aliasStack = base.subqueryAliasStack();
        String parentCtx = base.getCurrentParentContext();
        String inlineAlias = (!aliasStack.isEmpty()
                && ("FROM".equals(parentCtx) || "MERGE".equals(parentCtx)))
                ? aliasStack.get(aliasStack.size() - 1) : null;
        if (inlineAlias != null && !inlineAlias.isBlank()) {
            base.onStatementEnter("SELECT", extract(ctx), getStartLine(ctx), getEndLine(ctx), inlineAlias);
        } else {
            base.onStatementEnter("SELECT", extract(ctx), getStartLine(ctx), getEndLine(ctx));
        }
        // Mark the inner subquery position so enterSubquery skips double-scope push.
        // Grammar: select_statement → select_only_statement → with_clause? subquery
        if (ctx.select_only_statement() != null && ctx.select_only_statement().subquery() != null) {
            var sq = ctx.select_only_statement().subquery();
            base.setIsFirstSubq(sq.start.getLine());
            base.setIsFirstSubqp(sq.start.getCharPositionInLine());
        }
    }

    @Override
    public void exitSelect_statement(PlSqlParser.Select_statementContext ctx) {
        if (suppressedSelects.remove(ctx)) return; // scope was suppressed — no pop needed
        base.onStatementExit();
    }

    @Override
    public void enterInsert_statement(PlSqlParser.Insert_statementContext ctx) {
        base.onDmlTargetEnter();
        // KI-INSALL-1: INSERT ALL / INSERT FIRST detected via multi_table_insert child
        String stmtType = ctx.multi_table_insert() != null ? "INSERT_MULTI" : "INSERT";
        base.onStatementEnter(stmtType, extract(ctx), getStartLine(ctx), getEndLine(ctx));
    }

    @Override
    public void exitInsert_statement(PlSqlParser.Insert_statementContext ctx) {
        base.onStatementExit();
        base.onDmlTargetExit();
    }

    /**
     * G5: extract explicit column list from INSERT INTO t (col1, col2, col3).
     * At this point general_table_ref has already been parsed, so the target table
     * geoid is available in StatementInfo.targetTables.
     */
    @Override
    public void exitInsert_into_clause(PlSqlParser.Insert_into_clauseContext ctx) {
        if (ctx == null || ctx.paren_column_list() == null) return;
        PlSqlParser.Column_listContext colList = ctx.paren_column_list().column_list();
        if (colList == null || colList.column_name() == null) return;
        List<String> cols = new ArrayList<>();
        for (PlSqlParser.Column_nameContext cn : colList.column_name()) {
            String name = BaseSemanticListener.cleanColumnName(cn.getText());
            if (name != null && !name.isBlank()) cols.add(name);
        }
        if (!cols.isEmpty()) base.onInsertColumnList(cols);
    }

    @Override
    public void enterUpdate_statement(PlSqlParser.Update_statementContext ctx) {
        base.onDmlTargetEnter();
        base.onStatementEnter("UPDATE", extract(ctx), getStartLine(ctx), getEndLine(ctx));
    }

    @Override
    public void exitUpdate_statement(PlSqlParser.Update_statementContext ctx) {
        base.onStatementExit();
        base.onDmlTargetExit();
    }

    /**
     * UPDATE SET target column registration.
     *
     * Handles two forms:
     *   1. col = expr           → single column_name on left side
     *   2. (col1, col2) = subq  → paren_column_list on left side
     *
     * Registers the left-hand column(s) as UPDATE affected columns (with poliage_update),
     * then sets in_update_set_expr=true so atoms in the right-hand expression get
     * parent_context="SET_EXPR" and do NOT receive a duplicate poliage_update.
     * The flag is cleared at exit.
     */
    @Override
    public void enterColumn_based_update_set_clause(
            PlSqlParser.Column_based_update_set_clauseContext ctx) {
        if (ctx == null) return;
        if (ctx.column_name() != null) {
            String colName = BaseSemanticListener.cleanColumnName(ctx.column_name().getText());
            if (colName != null && !colName.isBlank()) {
                base.onUpdateSetColumn(colName);
            }
        } else if (ctx.paren_column_list() != null
                && ctx.paren_column_list().column_list() != null) {
            List<String> cols = new ArrayList<>();
            for (var cn : ctx.paren_column_list().column_list().column_name()) {
                String c = BaseSemanticListener.cleanColumnName(cn.getText());
                if (c != null && !c.isBlank()) cols.add(c);
            }
            if (!cols.isEmpty()) base.onUpdateSetColumnList(cols);
        }
    }

    @Override
    public void exitColumn_based_update_set_clause(
            PlSqlParser.Column_based_update_set_clauseContext ctx) {
        base.onUpdateSetExit();
    }

    @Override
    public void enterDelete_statement(PlSqlParser.Delete_statementContext ctx) {
        base.onDmlTargetEnter();
        base.onStatementEnter("DELETE", extract(ctx), getStartLine(ctx), getEndLine(ctx));
    }

    @Override
    public void exitDelete_statement(PlSqlParser.Delete_statementContext ctx) {
        base.onStatementExit();
        base.onDmlTargetExit();
    }

    @Override
    public void enterMerge_statement(PlSqlParser.Merge_statementContext ctx) {
        base.onStatementEnter("MERGE", extract(ctx), getStartLine(ctx), getEndLine(ctx));
        base.onDmlTargetEnter();

        // Case A: MERGE INTO table_name alias (прямая таблица)
        if (ctx.tableview_name() != null) {
            String targetTable = BaseSemanticListener.cleanIdentifier(ctx.tableview_name().getText());
            String targetAlias = extractAlias(ctx.table_alias());
            if (targetTable != null) {
                base.onTableReference(targetTable, targetAlias, getStartLine(ctx), getEndLine(ctx));
            }
            // Прямая таблица — сразу сбрасываем target-флаг
            base.onDmlTargetExit();
        }
        // Case B: MERGE INTO (SELECT ... FROM target_table) msubquery (updatable subquery)
        // НЕ сбрасываем in_dml_target — он пропагируется в подзапрос,
        // target_table внутри зарегистрируется как TARGET при walk.
        // Сброс произойдёт в enterSelected_tableview (граница INTO → USING).
        else {
            String subqAlias = extractAlias(ctx.table_alias());
            if (subqAlias != null) {
                base.setMergeIntoSubqueryAlias(subqAlias);
            }
        }
    }

    /**
     * Граница MERGE INTO → USING.
     * Сбрасываем in_dml_target: всё что после USING — это SOURCE.
     * Также регистрируем USING source таблицу/подзапрос.
     */
    @Override
    public void enterSelected_tableview(PlSqlParser.Selected_tableviewContext ctx) {
        // Сброс target-флага для Case B (updatable subquery в MERGE INTO)
        if (base.isInDmlTarget()) {
            base.onDmlTargetExit();
        }
        // Регистрируем USING source
        String sourceAlias = extractAlias(ctx.table_alias());
        if (ctx.tableview_name() != null) {
            // Case A: USING direct_table alias
            String sourceName = BaseSemanticListener.cleanIdentifier(ctx.tableview_name().getText());
            if (sourceName != null) {
                base.onTableReference(sourceName, sourceAlias, getStartLine(ctx), getEndLine(ctx));
            }
        } else if (ctx.table_collection_expression() != null && sourceAlias != null) {
            // Case C: USING TABLE(collection_expr) alias — register alias in scope directly
            // (tableview_name is null, no subquery is entered, so the alias stack alone is insufficient)
            var tce = ctx.table_collection_expression();
            String collExpr = tce.expression() != null
                    ? BaseSemanticListener.cleanIdentifier(tce.expression().getText())
                    : null;
            String syntheticName = (collExpr != null && !collExpr.isBlank()) ? collExpr : "COLLECTION_SOURCE";
            base.onTableReference(syntheticName, sourceAlias, getStartLine(ctx), getEndLine(ctx));
        }
        if (sourceAlias != null) {
            base.setSubqueryAlias(sourceAlias);
            base.subqueryAliasStack().add(sourceAlias);
        }
    }

    @Override
    public void exitMerge_statement(PlSqlParser.Merge_statementContext ctx) {
        base.clearMergeIntoSubqueryAlias();
        base.onStatementExit();
    }

    // =========================================================================
    // Курсоры
    // =========================================================================

    @Override
    public void enterCursor_declaration(PlSqlParser.Cursor_declarationContext ctx) {
        base.onStatementEnter("CURSOR", extract(ctx), getStartLine(ctx), getEndLine(ctx));
        if (ctx.select_statement() != null) {
            base.setIsFirstSubq(ctx.select_statement().start.getLine());
            base.setIsFirstSubqp(ctx.select_statement().start.getCharPositionInLine());
        }
    }

    @Override
    public void exitCursor_declaration(PlSqlParser.Cursor_declarationContext ctx) {
        // Register cursor name → statement geoid BEFORE the statement scope is popped
        if (ctx.identifier() != null) {
            String cursorName = BaseSemanticListener.cleanIdentifier(ctx.identifier().getText());
            base.onCursorDeclared(cursorName);
        }
        base.onStatementExit();
        base.setIsFirstSubq(null);
        base.setIsFirstSubqp(null);
    }

    // ═══════ OPEN FOR (ref cursor) ═══════

    @Override
    public void enterOpen_for_statement(PlSqlParser.Open_for_statementContext ctx) {
        if (ctx == null) return;
        PlSqlParser.Select_statementContext selectCtx = ctx.select_statement();
        if (selectCtx != null) {
            base.setIsFirstSubq(selectCtx.getStart().getLine());
            base.setIsFirstSubqp(selectCtx.getStart().getCharPositionInLine());
            base.onStatementEnter("REF CURSOR", extract(ctx), getStartLine(ctx), getEndLine(ctx));
        }
    }

    @Override
    public void exitOpen_for_statement(PlSqlParser.Open_for_statementContext ctx) {
        if (ctx != null && ctx.select_statement() != null) {
            base.onStatementExit();
            base.setIsFirstSubq(null);
            base.setIsFirstSubqp(null);
        }
    }

    // ═══════ LOOP с cursor (FOR rec IN SELECT) ═══════

    @Override
    public void enterLoop_statement(PlSqlParser.Loop_statementContext ctx) {
        if (ctx == null || ctx.cursor_loop_param() == null) return;
        PlSqlParser.Cursor_loop_paramContext cursorParam = ctx.cursor_loop_param();
        PlSqlParser.Select_statementContext selectCtx = cursorParam.select_statement();
        if (selectCtx != null) {
            // Inline cursor: FOR rec IN (SELECT ...) LOOP
            base.setIsFirstSubq(selectCtx.getStart().getLine());
            base.setIsFirstSubqp(selectCtx.getStart().getCharPositionInLine());
            base.onStatementEnter("DINAMIC_CURSOR", extract(ctx), getStartLine(ctx), getEndLine(ctx));
        } else if (cursorParam.cursor_name() != null) {
            // Named cursor: FOR rec IN cursor_name LOOP — create scope for loop body
            base.onStatementEnter("CURSOR_LOOP", extract(ctx), getStartLine(ctx), getEndLine(ctx));
        }
    }

    @Override
    public void exitLoop_statement(PlSqlParser.Loop_statementContext ctx) {
        if (ctx == null || ctx.cursor_loop_param() == null) return;
        PlSqlParser.Cursor_loop_paramContext cursorParam = ctx.cursor_loop_param();
        if (cursorParam.select_statement() != null) {
            base.onStatementExit();
            base.setIsFirstSubq(null);
            base.setIsFirstSubqp(null);
        } else if (cursorParam.cursor_name() != null) {
            base.onStatementExit();
        }
    }

    @Override
    public void exitCursor_loop_param(PlSqlParser.Cursor_loop_paramContext ctx) {
        if (ctx == null || ctx.record_name() == null) return;
        String recName = BaseSemanticListener.cleanIdentifier(ctx.record_name().getText());
        if (ctx.cursor_name() != null) {
            // Named cursor: rec aliases the cursor's scope
            // cursor_name uses general_element which may include arguments: c_txns(p1,p2)
            // Strip arguments to get the bare cursor name for registry lookup
            String rawCursorName = ctx.cursor_name().getText();
            int parenIdx = rawCursorName.indexOf('(');
            String cursorNameOnly = parenIdx >= 0 ? rawCursorName.substring(0, parenIdx) : rawCursorName;
            String cursorName = BaseSemanticListener.cleanIdentifier(cursorNameOnly);
            base.onCursorRecordNamed(recName, cursorName);
        } else if (ctx.select_statement() != null) {
            // Inline cursor: rec aliases the DINAMIC_CURSOR scope (current statement)
            base.onCursorRecordInline(recName);
        }
    }

    // ═══════ FORALL loop ═══════

    /**
     * Bug A fix: FORALL index variable (e.g. `i` in `FORALL i IN 1..N INSERT ...`)
     * was not registered as a variable, so every use of `i` inside the DML body
     * fired as an `atom` with 1 token and was incorrectly resolved as a column
     * reference against the target table (creating spurious DWH.STG_FX_RATES.I).
     *
     * Registering the index as a PLS_INTEGER variable puts it in the routine's
     * variable map, so AtomProcessor.classifyAtom() short-circuits before
     * reaching resolveImplicitTable().
     */
    @Override
    public void enterForall_statement(PlSqlParser.Forall_statementContext ctx) {
        if (ctx == null || ctx.index_name() == null) return;
        String indexVar = BaseSemanticListener.cleanIdentifier(ctx.index_name().getText());
        if (!indexVar.isEmpty()) {
            base.onRoutineVariable(indexVar, "PLS_INTEGER");
        }
    }

    /**
     * KI-PIPE-1: Marks the enclosing function as pipelined when a PIPE ROW statement is found.
     * HND-09: If the expression is an OBJECT type constructor call (e.g. SCHEMA.T_REC(...)),
     * materialise a DaliRecord + DaliRecordField from the PlTypeInfo so lineage can flow
     * through the piped fields.  The synthetic variable name "__PIPE_ROW_OUT__" is used as
     * the record geoid anchor; it is unique per routine and idempotent on multiple PIPE ROW calls.
     */
    @Override
    public void enterPipe_row_statement(PlSqlParser.Pipe_row_statementContext ctx) {
        if (ctx == null) return;
        String routineGeoid = base.currentRoutine();
        if (routineGeoid == null) return;
        var ri = base.engine.getBuilder().getRoutines().get(routineGeoid);
        if (ri != null) ri.setPipelined(true);

        // Detect constructor call: PIPE ROW(TYPE_NAME(arg, ...))
        // Use token stream (with spaces) for reliable extraction before first '('.
        if (ctx.expression() == null) return;
        String typeRef = extractConstructorTypeName(ctx.expression());
        if (typeRef == null || typeRef.isBlank()) return;

        // Resolve to a PlTypeInfo that has fields (OBJECT or RECORD)
        com.hound.semantic.model.PlTypeInfo pt =
                base.engine.getBuilder().resolvePlTypeByName(typeRef, routineGeoid);
        if (pt == null || !pt.hasFields()) return;

        // Materialise a DaliRecord so DaliRecordField vertices are emitted and
        // INSTANTIATES_TYPE edge links this output instance to the PlType definition.
        // varName encodes the source type so the geoid is self-describing:
        // e.g. PIPE_ROW_OUT_T_LINE_REC (schema prefix stripped, bare type name only).
        // varName = full PlType geoid + ":PIPE_ROW_OUT" so the DaliRecord geoid is self-describing:
        // e.g. "TESTSCHEMA:TYPE:T_LINE_REC:PIPE_ROW_OUT"
        base.engine.onPlTypeVariable(pt.getGeoid() + ":PIPE_ROW_OUT", typeRef, ctx.getStart().getLine());
        logger.debug("HND-09/PIPE_ROW: materialised DaliRecord for constructor {} in {}",
                typeRef, routineGeoid);
    }

    /**
     * KI-PRAGMA-1: Marks the enclosing routine as autonomous when
     * PRAGMA AUTONOMOUS_TRANSACTION is declared.
     */
    @Override
    public void enterPragma_declaration(PlSqlParser.Pragma_declarationContext ctx) {
        if (ctx == null || ctx.AUTONOMOUS_TRANSACTION() == null) return;
        String routineGeoid = base.currentRoutine();
        if (routineGeoid == null) return;
        var ri = base.engine.getBuilder().getRoutines().get(routineGeoid);
        if (ri != null) ri.setAutonomousTransaction(true);
    }

    /**
     * KI-FLASHBACK-1: Captures AS OF TIMESTAMP / AS OF SCN meta-fields on the current statement.
     */
    @Override
    public void enterFlashback_query_clause(PlSqlParser.Flashback_query_clauseContext ctx) {
        if (ctx == null) return;
        String stmtGeoid = base.engine.getScopeManager().currentStatement();
        if (stmtGeoid == null) return;
        var si = base.engine.getBuilder().getStatements().get(stmtGeoid);
        if (si == null) return;
        String type = ctx.TIMESTAMP() != null ? "TIMESTAMP" : (ctx.SCN() != null ? "SCN" : null);
        String expr = !ctx.expression().isEmpty() ? ctx.expression(0).getText() : null;
        si.setFlashbackType(type);
        si.setFlashbackExpr(expr);
    }

    // =========================================================================
    // CTE / WITH clause
    // =========================================================================

    /** Saved is_first_subq marker — restored after WITH clause so the main
     *  subquery of select_only_statement is still recognized as "first". */
    private Integer savedFirstSubqLine;
    private Integer savedFirstSubqCol;

    /**
     * select_statement contexts whose scope push was suppressed because they are
     * the direct body of a parent scope (DINAMIC_CURSOR).
     * exitSelect_statement checks this set and skips the corresponding pop.
     */
    private final Set<PlSqlParser.Select_statementContext> suppressedSelects = new HashSet<>();

    @Override
    public void enterWith_clause(PlSqlParser.With_clauseContext ctx) {
        savedFirstSubqLine = base.getIsFirstSubq();
        savedFirstSubqCol  = base.getIsFirstSubqp();
        // CTE scope opened per-factoring-clause in enterSubquery_factoring_clause
    }

    @Override
    public void exitWith_clause(PlSqlParser.With_clauseContext ctx) {
        // CTE scope closed per-factoring-clause in exitSubquery_factoring_clause
        // Restore so the main subquery after WITH is recognised as "first" (no extra scope push)
        base.setIsFirstSubq(savedFirstSubqLine);
        base.setIsFirstSubqp(savedFirstSubqCol);
    }

    /**
     * H1.1 — CTE alias registration.
     * Each individual WITH x AS (...) registers alias "x" on the outer scope
     * so that FROM x in the main query resolves to this CTE.
     */
    @Override
    public void enterSubquery_factoring_clause(PlSqlParser.Subquery_factoring_clauseContext ctx) {
        if (ctx == null || ctx.query_name() == null) return;
        String cteName = BaseSemanticListener.cleanIdentifier(ctx.query_name().getText());
        if (cteName == null || cteName.isBlank()) return;
        base.setSubqueryAlias(cteName.toUpperCase());
        base.onCTEEnter(extract(ctx), getStartLine(ctx), getEndLine(ctx));
        // Python behavior: the first (only) subquery inside a CTE IS the CTE body —
        // it must NOT push a separate SUBQUERY scope. Set isFirstSubq to the inner
        // subquery's position so that enterSubquery recognises and skips the push.
        // Grammar: subquery_factoring_clause → ... AS '(' subquery ... ')'
        if (ctx.subquery() != null && ctx.subquery().start != null) {
            base.setIsFirstSubq(ctx.subquery().start.getLine());
            base.setIsFirstSubqp(ctx.subquery().start.getCharPositionInLine());
        } else {
            base.setIsFirstSubq(null);
            base.setIsFirstSubqp(null);
        }
    }

    @Override
    public void exitSubquery_factoring_clause(PlSqlParser.Subquery_factoring_clauseContext ctx) {
        base.onCTEExit();
    }

    // =========================================================================
    // Подзапросы
    // =========================================================================

    @Override
    public void enterSubquery(PlSqlParser.SubqueryContext ctx) {
        if (ctx == null) return;

        Integer firstLine = base.getIsFirstSubq();
        Integer firstCol  = base.getIsFirstSubqp();
        int ctxLine = ctx.start.getLine();
        int ctxCol  = ctx.start.getCharPositionInLine();

        if (firstLine != null && firstLine == ctxLine
                && firstCol != null && firstCol == ctxCol) {
            base.setIsFirstSubq(ctxLine);
            base.setIsFirstSubqp(ctxCol);
            return;
        }

        base.setIsFirstSubq(ctxLine);
        base.setIsFirstSubqp(ctxCol);
        base.setIsUnion(ctx.subquery_operation_part() != null
                && !ctx.subquery_operation_part().isEmpty());

        if (!base.isInDmlTarget()) {
            base.onSubqueryEnter(extract(ctx), getStartLine(ctx), getEndLine(ctx));
        } else {
            base.onStatementEnter("USUBQUERY", extract(ctx), getStartLine(ctx), getEndLine(ctx));
        }
    }

    @Override
    public void exitSubquery(PlSqlParser.SubqueryContext ctx) {
        String stmtType = base.currentStatementType();
        if ("SUBQUERY".equals(stmtType) || "USUBQUERY".equals(stmtType)) {
            base.onSubqueryExit();
        }
    }

    // =========================================================================
    // UNION / INTERSECT / MINUS branches
    //
    // Grammar: subquery = subquery_basic_elements subquery_operation_part*
    //          subquery_operation_part = (UNION ALL? | INTERSECT | MINUS) subquery_basic_elements
    //
    // Python analogue: enterSubquery_operation_part → _init_statement('UIM_SUBQ')
    //                  exitSubquery_operation_part  → _exit_statement()
    //
    // Each branch beyond the first gets its own USUBQUERY scope so that atoms,
    // output columns and source tables are isolated per branch rather than
    // collapsed into the parent SUBQUERY scope.
    // =========================================================================

    @Override
    public void enterSubquery_operation_part(PlSqlParser.Subquery_operation_partContext ctx) {
        if (ctx == null) return;
        base.onStatementEnter("USUBQUERY", extract(ctx), getStartLine(ctx), getEndLine(ctx));
    }

    @Override
    public void exitSubquery_operation_part(PlSqlParser.Subquery_operation_partContext ctx) {
        if ("USUBQUERY".equals(base.currentStatementType())) {
            base.onStatementExit();
        }
    }

    // =========================================================================
    // FROM clause
    // =========================================================================

    @Override
    public void enterFrom_clause(PlSqlParser.From_clauseContext ctx) {
        base.onFromEnter(getStartLine(ctx));
    }

    @Override
    public void exitFrom_clause(PlSqlParser.From_clauseContext ctx) {
        base.onFromExit();
    }

    // =========================================================================
    // Таблицы — enterTable_ref_aux
    //
    // ctx.tableview_name() НЕ существует в Table_ref_auxContext этой грамматики.
    // Имя таблицы извлекается через рефлексию из table_ref_aux_internal,
    // либо как getText() всего контекста минус алиас.
    // =========================================================================

    @Override
    public void enterTable_ref_aux(PlSqlParser.Table_ref_auxContext ctx) {
        if (ctx == null) return;

        String tableAlias = extractAlias(ctx.table_alias());

        // TABLE(collection_expr) — extract function name as synthetic table id so alias resolves
        String collectionName = extractTableCollectionName(ctx, tableAlias);
        if (collectionName != null) {
            base.onTableReference(collectionName, tableAlias, getStartLine(ctx), getEndLine(ctx));
            base.setCurrentTable(collectionName);
            base.setCurrentTableAlias(tableAlias);
            base.setSubqueryAlias(tableAlias);
            base.subqueryAliasStack().add(tableAlias != null ? tableAlias : "");
            // HND-14: inject virtual columns from PIPELINED return type into synthetic table
            injectColumnsFromPipelinedFunc(collectionName, ctx);
            return;
        }

        String tableName = extractTableName(ctx, tableAlias);

        if (tableName != null && !tableName.isBlank()) {
            base.onTableReference(tableName, tableAlias, getStartLine(ctx), getEndLine(ctx));
            base.setCurrentTable(tableName);
            base.setCurrentTableAlias(tableAlias);
        }

        base.setSubqueryAlias(tableAlias);
        base.subqueryAliasStack().add(tableAlias != null ? tableAlias : "");
    }

    @Override
    public void exitTable_ref_aux(PlSqlParser.Table_ref_auxContext ctx) {
        List<String> stack = base.subqueryAliasStack();
        if (!stack.isEmpty()) stack.remove(stack.size() - 1);
    }

    /**
     * KI-LATERAL-1: detect LATERAL (subquery) in FROM clause.
     * table_ref_aux_internal_one wraps dml_table_expression_clause which has LATERAL() terminal.
     * Marks the enclosing statement so NameResolver can consult outer scope for unresolved refs.
     */
    @Override
    public void enterTable_ref_aux_internal_one(PlSqlParser.Table_ref_aux_internal_oneContext ctx) {
        if (ctx == null) return;
        PlSqlParser.Dml_table_expression_clauseContext dml = ctx.dml_table_expression_clause();
        if (dml != null && dml.LATERAL() != null) {
            String stmtGeoid = base.engine.getScopeManager().currentStatement();
            if (stmtGeoid != null)
                base.engine.getScopeManager().markHasLateral(stmtGeoid);
        }
    }

    // =========================================================================
    // General table ref (DML target)
    // =========================================================================

    @Override
    public void enterGeneral_table_ref(PlSqlParser.General_table_refContext ctx) {
        if (ctx == null) return;
        base.onDmlTargetEnter();

        String tableAlias = extractAlias(ctx.table_alias());
        String tableName  = null;

        if (ctx.dml_table_expression_clause() != null) {
            PlSqlParser.Dml_table_expression_clauseContext dml = ctx.dml_table_expression_clause();
            if (dml.tableview_name() != null) {
                tableName = BaseSemanticListener.cleanIdentifier(dml.tableview_name().getText());
            }
        }

        if (tableName != null && !tableName.isBlank()) {
            base.onTableReference(tableName, tableAlias, getStartLine(ctx), getEndLine(ctx));
            base.setCurrentTable(tableName);
        }
    }

    @Override
    public void exitGeneral_table_ref(PlSqlParser.General_table_refContext ctx) {
        base.setGeneralTable(null);
        base.onDmlTargetExit();
    }

    // =========================================================================
    // JOIN
    // =========================================================================

    @Override
    public void enterJoin_clause(PlSqlParser.Join_clauseContext ctx) {
        base.onJoinEnter(extract(ctx), getStartLine(ctx), getEndLine(ctx));
    }

    @Override
    public void exitJoin_clause(PlSqlParser.Join_clauseContext ctx) {
        if (ctx == null) {
            base.onJoinExit();
            return;
        }

        // Порт Python: exitJoin_clause — полная обработка

        // 1. Join type
        String joinType = extractJoinType(ctx);

        // 2. Conditions
        List<String> conditions = new ArrayList<>();
        try {
            var onParts = ctx.join_on_part();
            if (onParts != null) {
                for (var onPart : onParts) {
                    if (onPart.condition() != null) {
                        conditions.add(extract(onPart.condition()));
                    }
                }
            }
        } catch (Exception e) {
            // Grammar may not have join_on_part, fallback
        }

        // 3. Source table determination через regex условий
        String targetName = base.currentTable();
        String targetAlias = base.currentTableAlias();
        String filterAlias = targetAlias != null ? targetAlias : targetName;
        String sourceAlias = determineJoinSourceFromConditions(conditions, filterAlias);

        // 4. Complete join registration
        base.onJoinComplete(joinType, conditions, sourceAlias, getStartLine(ctx));

        // KI-APPLY-1: CROSS APPLY / OUTER APPLY — lateral-like correlated join.
        // Mark enclosing statement so NameResolver can consult outer scope for refs.
        if (joinType.contains("APPLY")) {
            String stmtGeoid = base.engine.getScopeManager().currentStatement();
            if (stmtGeoid != null) base.engine.getScopeManager().markHasLateral(stmtGeoid);
        }

        base.onJoinExit();
    }

    /**
     * Определяет тип JOIN из контекста через прямые token checks (grammar-rule based).
     * Fallback на text только для неизвестных случаев.
     */
    private String extractJoinType(PlSqlParser.Join_clauseContext ctx) {
        // APPLY variants: (CROSS | OUTER) APPLY — no JOIN token present
        if (ctx.APPLY() != null) {
            return ctx.CROSS() != null ? "CROSS APPLY" : "OUTER APPLY";
        }
        if (ctx.NATURAL() != null) return "NATURAL JOIN";
        if (ctx.CROSS()   != null) return "CROSS JOIN";
        // FULL / LEFT / RIGHT [OUTER] JOIN
        var ojt = ctx.outer_join_type();
        if (ojt != null) {
            StringBuilder type = new StringBuilder();
            if (ojt.FULL()  != null) type.append("FULL ");
            if (ojt.LEFT()  != null) type.append("LEFT ");
            if (ojt.RIGHT() != null) type.append("RIGHT ");
            if (ojt.OUTER() != null) type.append("OUTER ");
            type.append("JOIN");
            return type.toString().trim();
        }
        if (ctx.INNER() != null) return "INNER JOIN";
        return "JOIN";
    }

    /**
     * Порт Python: _determine_join_source_table()
     * Определяет source table из ON conditions через regex по spaced text от extract().
     * Conditions уже извлечены через extract() (с пробелами), не ctx.getText().
     */
    private static final Pattern JOIN_ALIAS_PATTERN = Pattern.compile("\\b([a-zA-Z_]\\w*)\\.\\w+\\b");
    private static final java.util.Set<String> SQL_BUILTIN_FUNCTIONS = java.util.Set.of(
        "NVL", "NVL2", "COALESCE", "DECODE", "CASE", "NULLIF", "GREATEST", "LEAST",
        "TO_CHAR", "TO_DATE", "TO_NUMBER", "TO_TIMESTAMP", "TRUNC", "ROUND", "SUBSTR",
        "INSTR", "LENGTH", "UPPER", "LOWER", "TRIM", "LTRIM", "RTRIM", "REPLACE",
        "LPAD", "RPAD", "CONCAT", "CHR", "ASCII", "SYSDATE", "SYSTIMESTAMP",
        "COUNT", "SUM", "AVG", "MIN", "MAX", "LISTAGG", "STRAGG",
        "ROWNUM", "ROWID", "SYS_GUID", "DBMS_UTILITY"
    );

    private String determineJoinSourceFromConditions(List<String> conditions, String targetRef) {
        Set<String> usedAliases = new LinkedHashSet<>();
        for (String condition : conditions) {
            Matcher m = JOIN_ALIAS_PATTERN.matcher(condition);
            while (m.find()) {
                String prefix = m.group(1).toUpperCase();
                // Skip known SQL built-in function names (e.g. NVL.something is not an alias)
                if (SQL_BUILTIN_FUNCTIONS.contains(prefix)) continue;
                // Skip if the full match contains '@' (DBLINK: schema.table@link)
                // The matched group is just the prefix, check the char after the match
                int end = m.end();
                if (end < condition.length() && condition.charAt(end) == '@') continue;
                usedAliases.add(prefix);
            }
        }
        if (targetRef != null) usedAliases.remove(targetRef.toUpperCase());
        return usedAliases.isEmpty() ? null : usedAliases.iterator().next();
    }

    // =========================================================================
    // Selected list
    // =========================================================================

    @Override
    public void enterSelected_list(PlSqlParser.Selected_listContext ctx) {
        base.onSelectedListEnter(getStartLine(ctx));
    }

    @Override
    public void exitSelected_list(PlSqlParser.Selected_listContext ctx) {
        base.onSelectedListExit();

        // STAB-11: голый SELECT * — нет select_list_elements событий, обрабатываем здесь.
        // ASTERISK() — terminal node; select_list_elements() — список дочерних правил.
        if (ctx == null) return;
        try {
            boolean hasAsterisk = ctx.ASTERISK() != null;
            boolean hasElements = ctx.select_list_elements() != null
                    && !ctx.select_list_elements().isEmpty();
            if (hasAsterisk && !hasElements) {
                base.onBareStar(getStartLine(ctx), getStartCol(ctx));
            }
        } catch (Exception ignored) {}
    }

    // =========================================================================
    // Output column binding (select_list_elements)
    // =========================================================================

    @Override
    public void enterSelect_list_elements(PlSqlParser.Select_list_elementsContext ctx) {
        if (ctx == null) return;
        base.onOutputColumnEnter(getStartLine(ctx), getStartCol(ctx));
    }

    @Override
    public void exitSelect_list_elements(PlSqlParser.Select_list_elementsContext ctx) {
        if (ctx == null) return;
        String expr = extract(ctx);
        boolean isTableStar = false;
        try {
            if (ctx.tableview_name() != null && ctx.ASTERISK() != null) {
                isTableStar = true;
                expr = ctx.tableview_name().getText() + ".*";
            }
        } catch (Exception ignored) {}

        base.onOutputColumnExit(getStartLine(ctx), getStartCol(ctx),
                getEndLine(ctx), getEndCol(ctx), expr, isTableStar);
    }

    // =========================================================================
    // Column alias
    // =========================================================================

    @Override
    public void enterColumn_alias(PlSqlParser.Column_aliasContext ctx) {
        if (ctx == null) return;
        if (ctx.identifier() != null) {
            base.onColumnAlias(ctx.identifier().getText());
        } else if (ctx.quoted_string() != null) {
            base.onColumnAlias(ctx.quoted_string().getText());
        }
    }

    // =========================================================================
    // Column name
    // =========================================================================

    // STAB-7: перенесено с enter на exit — при enter внутри сложных выражений
    // enterSchema_name может сработать ПОСЛЕ и загрязнить state.
    // На exit все дочерние узлы уже обработаны, scope чист.
    @Override
    public void enterColumn_name(PlSqlParser.Column_nameContext ctx) { }

    @Override
    public void exitColumn_name(PlSqlParser.Column_nameContext ctx) {
        if (ctx == null || ctx.getText() == null) return;
        String columnRef = BaseSemanticListener.cleanIdentifier(ctx.getText());
        if (columnRef != null && !columnRef.isBlank()) {
            base.onColumnRef(columnRef, getStartLine(ctx), getStartCol(ctx), getEndLine(ctx));
        }
    }

    // =========================================================================
    // WHERE / HAVING / ORDER BY / GROUP BY
    // =========================================================================

    @Override
    public void enterWhere_clause(PlSqlParser.Where_clauseContext ctx)   { base.onWhereEnter(getStartLine(ctx)); }
    @Override
    public void exitWhere_clause(PlSqlParser.Where_clauseContext ctx)    { base.onWhereExit(); }

    @Override
    public void enterHaving_clause(PlSqlParser.Having_clauseContext ctx) { base.onHavingEnter(getStartLine(ctx)); }
    @Override
    public void exitHaving_clause(PlSqlParser.Having_clauseContext ctx)  { base.onHavingExit(); }

    @Override
    public void enterOrder_by_clause(PlSqlParser.Order_by_clauseContext ctx) { base.onOrderByEnter(getStartLine(ctx)); }
    @Override
    public void exitOrder_by_clause(PlSqlParser.Order_by_clauseContext ctx)  { base.onOrderByExit(); }

    @Override
    public void enterGroup_by_clause(PlSqlParser.Group_by_clauseContext ctx) { base.onGroupByEnter(getStartLine(ctx)); }
    @Override
    public void exitGroup_by_clause(PlSqlParser.Group_by_clauseContext ctx)  { base.onGroupByExit(); }

    // =========================================================================
    // H1.1 — CONNECT BY (hierarchical queries)
    // Column refs inside START WITH / CONNECT BY are already tracked by enterColumn_name.
    // This handler marks the statement so it can be classified as hierarchical.
    // =========================================================================

    @Override
    public void enterHierarchical_query_clause(PlSqlParser.Hierarchical_query_clauseContext ctx) {
        // Mark current statement as hierarchical; column refs handled by enterColumn_name
        String stmtGeoid = base.currentStatement();
        if (stmtGeoid != null) {
            var si = base.engine.getBuilder().getStatements().get(stmtGeoid);
            if (si != null) si.setSubtype("HIERARCHICAL");
        }
    }

    // =========================================================================
    // H1.1 — PIVOT / UNPIVOT
    // The pivot result becomes a virtual table; alias is registered via table_ref_aux.
    // Column refs in FOR / IN clauses are tracked by enterColumn_name automatically.
    // =========================================================================

    @Override
    public void enterPivot_clause(PlSqlParser.Pivot_clauseContext ctx) {
        // Pivot FOR / IN column refs handled by enterColumn_name
        if (ctx == null) return;
        if (ctx.table_alias() != null) {
            String alias = extractAlias(ctx.table_alias());
            if (alias != null && !alias.isBlank()) {
                base.setCurrentTableAlias(alias);
                base.setSubqueryAlias(alias);
            }
        }
    }

    @Override
    public void enterUnpivot_clause(PlSqlParser.Unpivot_clauseContext ctx) {
        // Unpivot FOR / IN column refs handled by enterColumn_name
        if (ctx == null) return;
        if (ctx.table_alias() != null) {
            String alias = extractAlias(ctx.table_alias());
            if (alias != null && !alias.isBlank()) {
                base.setCurrentTableAlias(alias);
                base.setSubqueryAlias(alias);
            }
        }
    }

    // =========================================================================
    // H1.1 — SELECT INTO (PL/SQL variable assignment)
    // Only active in query_block context, not in INSERT INTO or RETURNING INTO.
    // =========================================================================

    @Override
    public void enterInto_clause(PlSqlParser.Into_clauseContext ctx) {
        if (ctx == null || ctx.parent == null) return;
        // Only handle SELECT INTO (query_block), not INSERT INTO or RETURNING INTO
        if (!(ctx.parent instanceof PlSqlParser.Query_blockContext)) return;
        // Use ScopeManager geoid (matches StructureAndLineageBuilder.getStatements() keys)
        // base.currentStatement() returns the internal scope key, not the statement geoid
        String stmtGeoid = base.engine.getScopeManager().currentStatement();
        if (stmtGeoid == null) return;
        var si = base.engine.getBuilder().getStatements().get(stmtGeoid);
        if (ctx.BULK() != null) {
            // G6: BULK COLLECT INTO — register the target collection variable
            if (si != null) si.setSubtype("BULK_COLLECT");
            List<PlSqlParser.General_elementContext> targets = ctx.general_element();
            if (targets != null && !targets.isEmpty()) {
                // Typed access: navigate to the first identifier via general_element_part.id_expression
                // to get the bare variable name without any subscript suffix (e.g. l_tab not l_tab(i))
                var ge = targets.get(0);
                var parts = ge.general_element_part();
                String varName;
                if (parts != null && !parts.isEmpty()
                        && parts.get(0).id_expression() != null) {
                    varName = BaseSemanticListener.cleanIdentifier(
                            parts.get(0).id_expression().getText()).toUpperCase();
                } else {
                    varName = BaseSemanticListener.cleanIdentifier(ge.getText()).toUpperCase();
                }
                String routineGeoid = base.currentRoutine();
                // Create RecordInfo and link it to this SELECT statement
                var rec = base.engine.getBuilder().ensureRecord(varName, routineGeoid, ctx.getStart().getLine());
                rec.setSourceStatementGeoid(stmtGeoid);
                // Register as cursor-record alias so AtomProcessor can resolve collection.field
                base.engine.getScopeManager().registerCursorRecord(varName, stmtGeoid, true);
            }
        } else {
            if (si != null) si.setSubtype("SELECT_INTO");
            // Variable names are tracked as column refs via general_element / bind_variable children
        }
    }

    // =========================================================================
    // G8: FETCH cursor BULK COLLECT INTO collection — create RecordInfo
    // =========================================================================

    @Override
    public void exitFetch_statement(PlSqlParser.Fetch_statementContext ctx) {
        if (ctx == null || ctx.BULK() == null) return; // only BULK COLLECT INTO
        if (ctx.variable_or_collection() == null || ctx.variable_or_collection().isEmpty()) return;

        // Collection variable name (first target after BULK COLLECT INTO)
        // Strip collection index if present: l_tab(i) → l_tab
        String rawVar = ctx.variable_or_collection(0).getText();
        int varParen = rawVar.indexOf('(');
        String varName = BaseSemanticListener.cleanIdentifier(
                varParen > 0 ? rawVar.substring(0, varParen) : rawVar).toUpperCase();

        // Cursor name — resolve its SELECT statement geoid
        String cursorSelectGeoid = null;
        if (ctx.cursor_name() != null) {
            String rawCursor = ctx.cursor_name().getText();
            int paren = rawCursor.indexOf('(');
            String cursorName = BaseSemanticListener.cleanIdentifier(
                    paren >= 0 ? rawCursor.substring(0, paren) : rawCursor);
            cursorSelectGeoid = base.engine.getScopeManager().getCursorStmtGeoid(cursorName);
        }

        String routineGeoid = base.currentRoutine();
        var rec = base.engine.getBuilder().ensureRecord(varName, routineGeoid, ctx.getStart().getLine());

        // Link to cursor's SELECT statement (so BULK_COLLECTS_INTO edge can be created)
        if (cursorSelectGeoid != null && rec.getSourceStatementGeoid() == null) {
            rec.setSourceStatementGeoid(cursorSelectGeoid);
        }

        // Register as cursor-record alias for col resolution in INSERT VALUES atoms
        String stmtGeoid = cursorSelectGeoid != null ? cursorSelectGeoid
                : base.engine.getScopeManager().currentStatement();
        if (stmtGeoid != null)
            base.engine.getScopeManager().registerCursorRecord(varName, stmtGeoid, true);

        logger.debug("G8 FETCH BULK COLLECT: {} → cursor stmt {}", varName, cursorSelectGeoid);
    }

    // =========================================================================
    // HAL3-02: assignment_statement → ASSIGNS_TO_VARIABLE edge
    // =========================================================================

    @Override
    public void exitAssignment_statement(PlSqlParser.Assignment_statementContext ctx) {
        if (ctx == null) return;
        PlSqlParser.General_elementContext ge = ctx.general_element();
        if (ge == null) return;

        var parts = ge.general_element_part();
        if (parts == null || parts.isEmpty()) return;
        String varName;
        if (parts.get(0).id_expression() != null) {
            varName = BaseSemanticListener.cleanIdentifier(
                    parts.get(0).id_expression().getText()).toUpperCase();
        } else {
            varName = BaseSemanticListener.cleanIdentifier(ge.getText()).toUpperCase();
            int dot = varName.indexOf('.');
            if (dot > 0) varName = varName.substring(0, dot);
            int paren = varName.indexOf('(');
            if (paren > 0) varName = varName.substring(0, paren);
        }

        String routineGeoid = base.currentRoutine();
        if (routineGeoid == null) return;

        RoutineInfo ri = base.engine.getBuilder().getRoutines().get(routineGeoid);
        if (ri == null) return;

        String targetGeoid = null;
        String targetKind = null;

        int vIdx = 0;
        for (RoutineInfo.VariableInfo v : ri.getTypedVariables()) {
            if (varName.equalsIgnoreCase(v.name())) {
                targetGeoid = routineGeoid + ":VAR:" + vIdx;
                targetKind = CompensationStats.KIND_VARIABLE;
                break;
            }
            vIdx++;
        }
        if (targetGeoid == null) {
            int pIdx = 0;
            for (RoutineInfo.ParameterInfo p : ri.getTypedParameters()) {
                if (varName.equalsIgnoreCase(p.name())) {
                    targetGeoid = routineGeoid + ":PARAM:" + pIdx;
                    targetKind = CompensationStats.KIND_PARAMETER;
                    break;
                }
                pIdx++;
            }
        }
        if (targetGeoid == null) return;

        String sourceGeoid = base.engine.getScopeManager().currentStatement();
        if (sourceGeoid == null) sourceGeoid = routineGeoid;

        base.engine.getBuilder().addCompensationStat(new CompensationStats(
                sourceGeoid,
                CompensationStats.EDGE_ASSIGNS_TO_VARIABLE,
                targetGeoid,
                targetKind,
                null));
    }

    // =========================================================================
    // HND-15: CAST(MULTISET(SELECT...) AS t_list) and CAST(COLLECT(col) AS t_list)
    // =========================================================================

    @Override
    public void enterOther_function(PlSqlParser.Other_functionContext ctx) {
        if (ctx == null) return;

        // HND-15b: CAST(MULTISET(SELECT...) AS t_list) → emit MULTISET_INTO edge
        if (ctx.CAST() != null && ctx.MULTISET() != null && ctx.type_spec() != null) {
            String stmtGeoid = base.engine.getScopeManager().currentStatement();
            if (stmtGeoid != null) {
                String typeName = BaseSemanticListener.cleanIdentifier(ctx.type_spec().getText());
                com.hound.semantic.model.PlTypeInfo pt =
                        base.engine.getBuilder().resolvePlTypeByName(typeName, null);
                if (pt == null) {
                    base.engine.getBuilder().markPendingMultiset(stmtGeoid);
                    logger.debug("HAL2-01: MULTISET pending — type {} not resolved, stmt {}",
                            typeName, stmtGeoid);
                }
                String targetGeoid = pt != null ? pt.getGeoid() : typeName;
                base.engine.getBuilder().addLineageEdge(stmtGeoid, targetGeoid, "MULTISET_INTO", stmtGeoid);
                logger.debug("HND-15b MULTISET_INTO {} → {}", stmtGeoid, targetGeoid);
            }
            return;
        }

        // HND-15a: COLLECT inside CAST(...AS t_list) → emit RETURNS_INTO edge
        // Typed: COLLECT token present in this ctx; walk parent chain for enclosing CAST other_function
        if (ctx.COLLECT() != null) {
            org.antlr.v4.runtime.RuleContext ancestor = ctx.parent;
            while (ancestor != null) {
                if (ancestor instanceof PlSqlParser.Other_functionContext castCtx
                        && castCtx.CAST() != null && castCtx.type_spec() != null) {
                    String stmtGeoid = base.engine.getScopeManager().currentStatement();
                    if (stmtGeoid != null) {
                        String typeName = BaseSemanticListener.cleanIdentifier(castCtx.type_spec().getText());
                        com.hound.semantic.model.PlTypeInfo pt =
                                base.engine.getBuilder().resolvePlTypeByName(typeName, null);
                        String targetGeoid = pt != null ? pt.getGeoid() : typeName;
                        base.engine.getBuilder().addLineageEdge(stmtGeoid, targetGeoid, "RETURNS_INTO", stmtGeoid);
                        logger.debug("HND-15a RETURNS_INTO {} → {}", stmtGeoid, targetGeoid);
                    }
                    break;
                }
                ancestor = ancestor.parent;
            }
        }
    }

    @Override
    public void enterOver_clause_keyword(PlSqlParser.Over_clause_keywordContext ctx) { base.onAnalyticEnter(); }

    // =========================================================================
    // MERGE insert/update parts
    // =========================================================================

    @Override
    public void enterMerge_insert_clause(PlSqlParser.Merge_insert_clauseContext ctx) {
        base.onMergeInsertEnter();
        // G3: extract explicit column list (col1, col2, ...) if present
        if (ctx.paren_column_list() != null
                && ctx.paren_column_list().column_list() != null) {
            var colList = ctx.paren_column_list().column_list().column_name();
            if (colList != null && !colList.isEmpty()) {
                java.util.List<String> cols = new java.util.ArrayList<>();
                for (var cn : colList) {
                    if (cn != null) cols.add(BaseSemanticListener.cleanColumnName(cn.getText()));
                }
                base.onMergeInsertColumns(cols);
            }
        }
    }
    @Override
    public void exitMerge_insert_clause(PlSqlParser.Merge_insert_clauseContext ctx)  { base.onMergeInsertExit(); }

    @Override
    public void enterMerge_update_clause(PlSqlParser.Merge_update_clauseContext ctx) { base.onMergeUpdateEnter(); }
    @Override
    public void exitMerge_update_clause(PlSqlParser.Merge_update_clauseContext ctx)  { base.onMergeUpdateExit(); }

    /** G3: MERGE UPDATE SET col = expr — track per-element target column. */
    @Override
    public void enterMerge_element(PlSqlParser.Merge_elementContext ctx) {
        if (ctx.column_name() != null) {
            base.onMergeElementEnter(
                    BaseSemanticListener.cleanColumnName(ctx.column_name().getText()));
        }
    }
    @Override
    public void exitMerge_element(PlSqlParser.Merge_elementContext ctx) {
        // Pass the RHS expression position range so BSL can post-bind atoms to the MERGE UPDATE target.
        // Using the expression (not the whole element) excludes the LHS column_name from binding.
        PlSqlParser.ExpressionContext exprCtx = ctx.expression();
        base.onMergeElementExit(
                getStartLine(exprCtx != null ? exprCtx : ctx),
                getStartCol(exprCtx != null ? exprCtx : ctx),
                getEndLine(exprCtx != null ? exprCtx : ctx),
                getEndCol(exprCtx != null ? exprCtx : ctx));
    }

    // =========================================================================
    // CALLS (STAB-9): межпроцедурные зависимости
    // Аналог Python: enterCall_statement + _add_called_routine()
    // =========================================================================

    @Override
    public void enterCall_statement(PlSqlParser.Call_statementContext ctx) {
        if (ctx == null || base.currentRoutine() == null) return;

        String calledName = null;

        // Стратегия 1: routine_name() — ANTLR4 всегда возвращает List
        try {
            var routineNames = ctx.routine_name();
            if (routineNames != null && !routineNames.isEmpty()) {
                for (var rn : routineNames) {
                    if (rn == null) continue;
                    try {
                        Method gt = rn.getClass().getMethod("getText");
                        String rText = (String) gt.invoke(rn);
                        if (rText != null && !rText.isBlank())
                            base.onCallStatement(BaseSemanticListener.cleanIdentifier(rText), getStartLine(ctx));
                    } catch (Exception ignored) {}
                }
                return; // handled via list
            }
        } catch (Exception ignored) {}

        // Стратегия 2: fallback — getText() до первой '('
        if (calledName == null || calledName.isBlank()) {
            try {
                String raw = ctx.getText();
                int paren = raw.indexOf('(');
                calledName = BaseSemanticListener.cleanIdentifier(
                        paren > 0 ? raw.substring(0, paren) : raw);
            } catch (Exception ignored) {}
        }

        if (calledName != null && !calledName.isBlank()) {
            base.onCallStatement(calledName, getStartLine(ctx));
        }
    }

    // =========================================================================
    // HAL3-03: WRITES_TO_PARAMETER — OUT params at call sites
    // =========================================================================

    @Override
    public void exitCall_statement(PlSqlParser.Call_statementContext ctx) {
        if (ctx == null) return;
        String callerRoutineGeoid = base.currentRoutine();
        if (callerRoutineGeoid == null) return;

        String calledName = null;
        var routineNames = ctx.routine_name();
        if (routineNames != null && !routineNames.isEmpty()) {
            calledName = BaseSemanticListener.cleanIdentifier(routineNames.get(0).getText());
        }
        if (calledName == null || calledName.isBlank()) {
            String raw = ctx.getText();
            int paren = raw.indexOf('(');
            calledName = BaseSemanticListener.cleanIdentifier(
                    paren > 0 ? raw.substring(0, paren) : raw);
        }
        if (calledName == null || calledName.isBlank()) return;

        String calledUpper = calledName.toUpperCase();
        RoutineInfo calledRoutine = null;
        String calledGeoid = null;
        for (var entry : base.engine.getBuilder().getRoutines().entrySet()) {
            String rg = entry.getKey().toUpperCase();
            if (rg.endsWith(":" + calledUpper) || rg.equals(calledUpper)) {
                calledRoutine = entry.getValue();
                calledGeoid = entry.getKey();
                break;
            }
        }
        if (calledRoutine == null) return;

        String sourceGeoid = base.engine.getScopeManager().currentStatement();
        if (sourceGeoid == null) sourceGeoid = callerRoutineGeoid;

        int pIdx = 0;
        for (RoutineInfo.ParameterInfo p : calledRoutine.getTypedParameters()) {
            if ("OUT".equalsIgnoreCase(p.mode()) || "INOUT".equalsIgnoreCase(p.mode())
                    || "IN OUT".equalsIgnoreCase(p.mode())) {
                base.engine.getBuilder().addCompensationStat(new CompensationStats(
                        sourceGeoid,
                        CompensationStats.EDGE_WRITES_TO_PARAMETER,
                        calledGeoid + ":PARAM:" + pIdx,
                        CompensationStats.KIND_PARAMETER,
                        null));
            }
            pIdx++;
        }
    }

    // =========================================================================
    // Values clause
    // =========================================================================

    @Override
    public void enterValues_clause(PlSqlParser.Values_clauseContext ctx) { base.onValuesClauseEnter(); }

    @Override
    public void exitValues_clause(PlSqlParser.Values_clauseContext ctx) { base.onValuesClauseExit(); }

    /**
     * STAB-6: exitExpression — VALUES counter + atom binding.
     * Обнаруживаем, когда expression находится непосредственно внутри values_clause
     * (expression → expression_list → values_clause), и вызываем onValuesExpressionExit.
     */
    @Override
    public void exitExpression(PlSqlParser.ExpressionContext ctx) {
        if (ctx == null) return;
        boolean isInValues = false;
        try {
            // Типичная иерархия: values_clause → expression_list → expression
            // Поднимаемся на 2 уровня вверх
            if (ctx.parent != null && ctx.parent.parent != null) {
                int ruleIdx = ((ParserRuleContext) ctx.parent.parent).getRuleIndex();
                if ("values_clause".equals(PlSqlParser.ruleNames[ruleIdx])) {
                    isInValues = true;
                }
            }
        } catch (Exception ignored) {}
        if (isInValues) {
            base.onValuesExpressionExit(
                    getStartLine(ctx), getStartCol(ctx),
                    getEndLine(ctx), getEndCol(ctx));
        }
    }

    // =========================================================================
    // Atom
    // =========================================================================

    @Override
    public void enterAtom(PlSqlParser.AtomContext ctx) {
        if (ctx == null) return;
        String text = ctx.getText() != null ? ctx.getText() : "";

        // Извлечение token details — ПОРТ из Python enterAtom
        List<String> tokens = new ArrayList<>();
        List<Map<String, String>> tokenDetails = new ArrayList<>();
        collectTerminalTokens(ctx, tokens, tokenDetails);

        boolean isComplex = tokens.size() > 1;
        int nestedAtomCount = countNestedAtoms(ctx);

        base.onAtom(text, getStartLine(ctx), getStartCol(ctx),
                getEndLine(ctx), getEndCol(ctx), isComplex,
                tokens, tokenDetails, nestedAtomCount);
    }

    /**
     * Собирает все terminal-токены из поддерева контекста.
     * Аналог Python: итерация по токенам от ctx.start до ctx.stop.
     */
    private void collectTerminalTokens(ParserRuleContext ctx,
                                        List<String> tokens,
                                        List<Map<String, String>> tokenDetails) {
        List<TerminalNode> terminals = new ArrayList<>();
        collectTerminals(ctx, terminals);
        for (TerminalNode tn : terminals) {
            Token token = tn.getSymbol();
            String rawName = PlSqlLexer.VOCABULARY.getSymbolicName(token.getType());
            if (rawName == null) rawName = String.valueOf(token.getType());

            CanonicalTokenType canonical = PlSqlTokenMapper.map(rawName);
            if (canonical == CanonicalTokenType.WHITESPACE) continue;

            tokens.add(token.getText());
            tokenDetails.add(Map.of(
                    "text", token.getText(),
                    "type", canonical.name(),
                    "position", token.getLine() + ":" + token.getCharPositionInLine()
            ));
        }
    }

    private void collectTerminals(ParseTree tree, List<TerminalNode> result) {
        if (tree instanceof TerminalNode) {
            result.add((TerminalNode) tree);
        } else {
            for (int i = 0; i < tree.getChildCount(); i++) {
                collectTerminals(tree.getChild(i), result);
            }
        }
    }

    /**
     * Подсчитывает вложенные AtomContext в поддереве (не считая сам ctx).
     */
    private int countNestedAtoms(PlSqlParser.AtomContext ctx) {
        int count = 0;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            count += countNestedAtomsRecursive(ctx.getChild(i));
        }
        return count;
    }

    private int countNestedAtomsRecursive(ParseTree tree) {
        int count = 0;
        if (tree instanceof PlSqlParser.AtomContext) {
            count++;
        }
        for (int i = 0; i < tree.getChildCount(); i++) {
            count += countNestedAtomsRecursive(tree.getChild(i));
        }
        return count;
    }

    // =========================================================================
    // CREATE VIEW
    // ctx.tableview_name() НЕ существует в Create_viewContext — используем рефлексию
    // =========================================================================

    @Override
    public void enterCreate_view(PlSqlParser.Create_viewContext ctx) {
        if (ctx == null) return;

        // STAB-8: читаем схему напрямую из ctx (аналог STAB-12 для пакетов).
        // Грамматика: CREATE VIEW (schema_name PERIOD)? v=id_expression AS ...
        String schemaName = null;
        if (ctx.schema_name() != null && ctx.PERIOD() != null) {
            schemaName = BaseSemanticListener.cleanIdentifier(ctx.schema_name().getText());
            if (schemaName != null && !schemaName.isBlank()) {
                base.onSchemaEnter(schemaName);
            }
        }

        // ctx.v — labeled field Id_expressionContext для имени view
        String viewName = null;
        if (ctx.v != null) {
            viewName = BaseSemanticListener.cleanIdentifier(ctx.v.getText());
        }
        // Fallback: берём первый id_expression из списка
        if (viewName == null || viewName.isBlank()) {
            var idList = ctx.id_expression();
            if (idList != null && !idList.isEmpty()) {
                viewName = BaseSemanticListener.cleanIdentifier(idList.get(0).getText());
            }
        }

        String effectiveSchema = (schemaName != null && !schemaName.isBlank())
                ? schemaName : base.currentSchema();

        if (viewName != null && !viewName.isBlank()) {
            base.onViewDeclaration(viewName, effectiveSchema, getStartLine(ctx));
        }
        base.onStatementEnter("CREATE_VIEW", extract(ctx), getStartLine(ctx), getEndLine(ctx));
        base.setIsFirstSubq(null);
        base.setIsFirstSubqp(null);
    }

    @Override
    public void exitCreate_view(PlSqlParser.Create_viewContext ctx) {
        base.onStatementExit();
        base.setIsFirstSubq(null);
        base.setIsFirstSubqp(null);
    }

    // =========================================================================
    // G10: CREATE VIEW (col1, col2, ...) column alias list
    // =========================================================================

    @Override
    public void enterView_alias_constraint(PlSqlParser.View_alias_constraintContext ctx) {
        if (ctx == null) return;
        var aliasList = ctx.table_alias();
        if (aliasList == null || aliasList.isEmpty()) return;
        List<String> cols = new ArrayList<>();
        for (var ta : aliasList) {
            String name = BaseSemanticListener.cleanIdentifier(ta.getText());
            if (name != null && !name.isBlank()) cols.add(name);
        }
        if (!cols.isEmpty()) base.onViewColumnAliases(cols);
    }

    // =========================================================================
    // T14: CREATE TABLE / ALTER TABLE — DDL column registration with ordinal_position
    // =========================================================================

    @Override
    public void enterCreate_table(PlSqlParser.Create_tableContext ctx) {
        if (ctx == null) return;
        // Extract optional schema name
        String schemaName = null;
        if (ctx.schema_name() != null) {
            schemaName = BaseSemanticListener.cleanIdentifier(ctx.schema_name().getText());
        }
        // Extract table name
        String tableName = ctx.table_name() != null
                ? BaseSemanticListener.cleanIdentifier(ctx.table_name().getText())
                : null;
        if (tableName == null || tableName.isBlank()) return;
        base.onCreateTableEnter(schemaName, tableName, extract(ctx), getStartLine(ctx), getEndLine(ctx));
    }

    @Override
    public void exitCreate_table(PlSqlParser.Create_tableContext ctx) {
        base.onCreateTableExit();
    }

    @Override
    public void enterAlter_table(PlSqlParser.Alter_tableContext ctx) {
        if (ctx == null || ctx.tableview_name() == null) return;
        String fullName = BaseSemanticListener.cleanIdentifier(ctx.tableview_name().getText());
        if (fullName == null || fullName.isBlank()) return;
        base.onAlterTableEnter(fullName);
        base.onStatementEnter("ALTER_TABLE", extract(ctx), getStartLine(ctx), getEndLine(ctx));
        base.registerDdlTableAsSource();
    }

    @Override
    public void exitAlter_table(PlSqlParser.Alter_tableContext ctx) {
        base.onAlterTableExit();
        base.onStatementExit();
    }

    // ── KI-RETURN-1: RETURNING INTO clause ───────────────────────────────────

    /**
     * Fired for: UPDATE ... RETURNING col INTO :v / l_rec.f / p_out
     *            DELETE ... RETURNING col INTO ...
     *            INSERT ... RETURNING col INTO ...  (all via static_returning_clause grammar rule)
     *
     * For each INTO target (general_element or bind_variable), classifies the target kind
     * (VARIABLE / PARAMETER / RECORD_FIELD / RECORD) and registers a ReturningTarget on
     * the current statement so RemoteWriter can emit RETURNS_INTO edges.
     */
    @Override
    public void enterStatic_returning_clause(PlSqlParser.Static_returning_clauseContext ctx) {
        if (ctx == null || ctx.into_clause() == null) return;
        String stmtGeoid = base.engine.getScopeManager().currentStatement();
        if (stmtGeoid == null) return;

        // Extract returned column expressions
        List<String> retExprs = new ArrayList<>();
        if (ctx.expressions_() != null)
            for (var e : ctx.expressions_().expression())
                retExprs.add(BaseSemanticListener.cleanColumnName(e.getText()));

        PlSqlParser.Into_clauseContext into = ctx.into_clause();
        boolean isBulk = into.BULK() != null;

        // general_element targets: plain PL/SQL vars, record fields, parameters
        for (var ge : into.general_element()) {
            String varName = BaseSemanticListener.cleanIdentifier(ge.getText()).toUpperCase();
            if (!varName.isBlank())
                base.onReturningTarget(varName, retExprs, stmtGeoid, isBulk);
        }
        // bind_variable targets: :v_name
        for (var bv : into.bind_variable()) {
            String varName = BaseSemanticListener.cleanIdentifier(
                    bv.getText().replace(":", "")).toUpperCase();
            if (!varName.isBlank())
                base.onReturningTarget(varName, retExprs, stmtGeoid, false);
        }
    }

    // ── KI-DDL-1: ALTER TABLE column change tracking ─────────────────────────

    /**
     * ALTER TABLE ... ADD (col1 type1, col2 type2)
     * Extracts column names from the add_column_clause and registers each as ADD.
     */
    @Override
    public void enterAdd_column_clause(PlSqlParser.Add_column_clauseContext ctx) {
        if (ctx == null || !base.isInDdlContext()) return;
        for (PlSqlParser.Column_definitionContext cd : ctx.column_definition()) {
            if (cd.column_name() == null) continue;
            String colName = BaseSemanticListener.cleanColumnName(cd.column_name().getText());
            if (colName != null && !colName.isBlank())
                base.onDdlColumnChange(colName, "ADD");
        }
    }

    /**
     * ALTER TABLE ... MODIFY (col1 new_type, col2 ...)
     * Extracts column names from each modify_col_properties and registers as MODIFY.
     */
    @Override
    public void enterModify_column_clauses(PlSqlParser.Modify_column_clausesContext ctx) {
        if (ctx == null || !base.isInDdlContext()) return;
        for (PlSqlParser.Modify_col_propertiesContext mcp : ctx.modify_col_properties()) {
            if (mcp.column_name() == null) continue;
            String colName = BaseSemanticListener.cleanColumnName(mcp.column_name().getText());
            if (colName != null && !colName.isBlank())
                base.onDdlColumnChange(colName, "MODIFY");
        }
    }

    /**
     * ALTER TABLE ... DROP COLUMN col1 / DROP (col1, col2)
     * Extracts column names from drop_column_clause and registers each as DROP.
     */
    @Override
    public void enterDrop_column_clause(PlSqlParser.Drop_column_clauseContext ctx) {
        if (ctx == null || !base.isInDdlContext()) return;
        for (PlSqlParser.Column_nameContext cn : ctx.column_name()) {
            String colName = BaseSemanticListener.cleanColumnName(cn.getText());
            if (colName != null && !colName.isBlank())
                base.onDdlColumnChange(colName, "DROP");
        }
    }

    /**
     * T14 (Step 1): Creates a statement scope for CREATE INDEX so that column-name atoms
     * in the index column list are attached to a statement and can be resolved via the
     * implicit-table strategy (the indexed table is registered as source).
     */
    @Override
    public void enterCreate_index(PlSqlParser.Create_indexContext ctx) {
        if (ctx == null) return;
        String tableRef = null;
        if (ctx.table_index_clause() != null && ctx.table_index_clause().tableview_name() != null) {
            tableRef = BaseSemanticListener.cleanIdentifier(
                    ctx.table_index_clause().tableview_name().getText());
        } else if (ctx.bitmap_join_index_clause() != null
                && !ctx.bitmap_join_index_clause().tableview_name().isEmpty()) {
            tableRef = BaseSemanticListener.cleanIdentifier(
                    ctx.bitmap_join_index_clause().tableview_name().get(0).getText());
        }
        base.onCreateIndexEnter(tableRef, extract(ctx), getStartLine(ctx), getEndLine(ctx));
    }

    @Override
    public void exitCreate_index(PlSqlParser.Create_indexContext ctx) {
        base.onStatementExit();
    }

    /**
     * T14: Fires for every column_definition in the parse tree.
     * Only acts when inside a DDL context (enterCreate_table or enterAlter_table set ddl_table_geoid).
     * Extracts column name, DDL data type, NOT NULL constraint, and DEFAULT expression.
     *
     * <p>Atom suppression: DEFAULT expressions contain atom nodes (literals, pseudocolumns like USER,
     * SYSTIMESTAMP, etc.). BaseSemanticListener.addAtom() suppresses these when ddl_table_geoid is set,
     * so no DaliAtom lineage records are created for DDL default values.</p>
     */
    @Override
    public void enterColumn_definition(PlSqlParser.Column_definitionContext ctx) {
        // Suppress atoms for the entire column definition sub-tree (DEFAULT expressions,
        // inline constraints, data type tokens). Structural info is extracted explicitly below.
        base.setDdlAtomCtx("SUPPRESS");
        if (ctx == null || ctx.column_name() == null) return;
        String colName = BaseSemanticListener.cleanColumnName(ctx.column_name().getText());
        if (colName == null || colName.isBlank()) return;

        // Extract data type: prefer datatype() (e.g. VARCHAR2, NUMBER), fall back to type_name()
        // (user-defined / package types like SYS.XMLTYPE or MY_SCHEMA.MY_TYPE).
        String dataType = null;
        if (ctx.datatype() != null) {
            dataType = ctx.datatype().getText();
        } else if (ctx.type_name() != null) {
            dataType = ctx.type_name().getText();
        }
        if (dataType != null) dataType = dataType.toUpperCase();

        // Extract DEFAULT expression text (e.g. "'N'", "SYSTIMESTAMP", "USER", "SYS_GUID()").
        // ctx.expression() is the single ExpressionContext child for the DEFAULT value.
        String defaultValue = null;
        if (ctx.DEFAULT() != null && ctx.expression() != null) {
            defaultValue = ctx.expression().getText().toUpperCase();
        }

        // Detect NOT NULL constraint from inline_constraint list.
        // ic.NOT() != null && ic.NULL_() != null → NOT NULL_ in grammar.
        boolean isRequired = false;
        for (PlSqlParser.Inline_constraintContext ic : ctx.inline_constraint()) {
            if (ic.NOT() != null && ic.NULL_() != null) {
                isRequired = true;
                break;
            }
        }

        base.onDdlColumnDefinition(colName, dataType, isRequired, defaultValue);
    }

    @Override
    public void exitColumn_definition(PlSqlParser.Column_definitionContext ctx) {
        base.setDdlAtomCtx(null);  // restore atom creation for out-of-line constraints
    }

    /**
     * Suppresses atoms inside REFERENCES ... (table + column list) — the FK target info is
     * already captured structurally via ConstraintInfo / REFERENCES_TABLE / REFERENCES_COLUMN edges.
     * Only active in DDL context to avoid suppressing SELECT-clause references.
     */
    @Override
    public void enterReferences_clause(PlSqlParser.References_clauseContext ctx) {
        if (base.isInDdlContext()) base.setDdlAtomCtx("SUPPRESS");
    }

    @Override
    public void exitReferences_clause(PlSqlParser.References_clauseContext ctx) {
        if (base.isInDdlContext()) base.setDdlAtomCtx(null);
    }

    // =========================================================================
    // Out-of-line constraints (PK, FK, UNIQUE, CHECK)
    // =========================================================================

    /**
     * Handles out_of_line_constraint inside ALTER TABLE … ADD CONSTRAINT or CREATE TABLE.
     * Currently processes PRIMARY KEY and FOREIGN KEY; UNIQUE and CHECK are recognized but
     * not yet persisted (deferred).
     *
     * <p>Only fires when ddl_table_geoid is set (i.e. inside a DDL context).
     * BaseSemanticListener.onPrimaryKeyConstraint / onForeignKeyConstraint handle geoid
     * computation and builder registration.</p>
     */
    @Override
    public void enterOut_of_line_constraint(PlSqlParser.Out_of_line_constraintContext ctx) {
        if (ctx == null) return;

        // Must be inside a DDL context (ALTER TABLE or CREATE TABLE sets ddl_table_geoid)
        // We check via the listener methods — they will guard internally.

        // ── PRIMARY KEY ──────────────────────────────────────────────────────
        if (ctx.PRIMARY() != null && ctx.KEY() != null) {
            String constraintName = null;
            if (ctx.constraint_name() != null) {
                constraintName = BaseSemanticListener.cleanIdentifier(ctx.constraint_name().getText());
            }
            // Columns are direct children of out_of_line_constraint (not via paren_column_list)
            // Grammar: PRIMARY KEY '(' column_name (',' column_name)* ')'
            List<String> pkColumns = new ArrayList<>();
            for (PlSqlParser.Column_nameContext cn : ctx.column_name()) {
                String name = BaseSemanticListener.cleanColumnName(cn.getText());
                if (name != null && !name.isBlank()) pkColumns.add(name.toUpperCase());
            }
            base.onPrimaryKeyConstraint(constraintName, pkColumns);
            return;
        }

        // ── FOREIGN KEY ──────────────────────────────────────────────────────
        if (ctx.foreign_key_clause() != null) {
            String constraintName = null;
            if (ctx.constraint_name() != null) {
                constraintName = BaseSemanticListener.cleanIdentifier(ctx.constraint_name().getText());
            }

            PlSqlParser.Foreign_key_clauseContext fkCtx = ctx.foreign_key_clause();

            // FK columns (in the host table)
            List<String> fkColumns = extractColumnList(fkCtx.paren_column_list());

            // References clause
            PlSqlParser.References_clauseContext refCtx = fkCtx.references_clause();
            if (refCtx == null) return;

            String refTableRaw = refCtx.tableview_name() != null
                    ? refCtx.tableview_name().getText() : null;
            List<String> refColumns = extractColumnList(refCtx.paren_column_list());

            // ON DELETE action — from references_clause (ON DELETE CASCADE / SET NULL_)
            String onDelete = null;
            if (refCtx.DELETE() != null) {
                if (refCtx.CASCADE() != null)      onDelete = "CASCADE";
                else if (refCtx.NULL_() != null)   onDelete = "SET NULL";
            }
            // Fallback: check on_delete_clause at the end of foreign_key_clause
            if (onDelete == null && fkCtx.on_delete_clause() != null) {
                PlSqlParser.On_delete_clauseContext odCtx = fkCtx.on_delete_clause();
                if (odCtx.CASCADE() != null)      onDelete = "CASCADE";
                else if (odCtx.NULL_() != null)   onDelete = "SET NULL";
            }

            base.onForeignKeyConstraint(constraintName, fkColumns, refTableRaw, refColumns, onDelete);
        }
        // KI-005: UNIQUE constraint
        if (ctx.UNIQUE() != null) {
            String constraintName = ctx.constraint_name() != null
                    ? BaseSemanticListener.cleanIdentifier(ctx.constraint_name().getText()) : null;
            List<String> cols = new ArrayList<>();
            for (PlSqlParser.Column_nameContext cn : ctx.column_name()) {
                String name = BaseSemanticListener.cleanColumnName(cn.getText());
                if (name != null && !name.isBlank()) cols.add(name.toUpperCase());
            }
            base.onUniqueConstraint(constraintName, cols);
            return;
        }
        // KI-005: CHECK constraint
        if (ctx.CHECK() != null && ctx.condition() != null) {
            String constraintName = ctx.constraint_name() != null
                    ? BaseSemanticListener.cleanIdentifier(ctx.constraint_name().getText()) : null;
            base.onCheckConstraint(constraintName, ctx.condition().getText());
        }
    }

    /** Extracts ordered column names from a paren_column_list context. */
    private List<String> extractColumnList(PlSqlParser.Paren_column_listContext pclCtx) {
        List<String> cols = new ArrayList<>();
        if (pclCtx == null || pclCtx.column_list() == null) return cols;
        for (PlSqlParser.Column_nameContext cn : pclCtx.column_list().column_name()) {
            String name = BaseSemanticListener.cleanColumnName(cn.getText());
            if (name != null && !name.isBlank()) cols.add(name.toUpperCase());
        }
        return cols;
    }

    // =========================================================================
    // HELPERS
    // =========================================================================


       private String extract(ParserRuleContext ctx) {
           if (ctx == null || ctx.getStart() == null || ctx.getStop() == null) return "";
           try {
               return ctx.getStart().getInputStream()
                       .getText(new org.antlr.v4.runtime.misc.Interval(
                               ctx.getStart().getStartIndex(),
                               ctx.getStop().getStopIndex()));
           } catch (Exception e) {
               return "";
           }
       }

    private int getStartLine(ParserRuleContext ctx) {
        return (ctx != null && ctx.getStart() != null) ? ctx.getStart().getLine() : 0;
    }

    private int getEndLine(ParserRuleContext ctx) {
        return (ctx != null && ctx.getStop() != null) ? ctx.getStop().getLine() : getStartLine(ctx);
    }

    private int getStartCol(ParserRuleContext ctx) {
        return (ctx != null && ctx.getStart() != null) ? ctx.getStart().getCharPositionInLine() : 0;
    }

    private int getEndCol(ParserRuleContext ctx) {
        return (ctx != null && ctx.getStop() != null) ? ctx.getStop().getCharPositionInLine() : getStartCol(ctx);
    }

    /** Алиас из table_alias контекста */
    private String extractAlias(PlSqlParser.Table_aliasContext aliasCtx) {
        if (aliasCtx == null) return null;
        try {
            String t = aliasCtx.getText();
            return (t != null && !t.isBlank()) ? BaseSemanticListener.cleanIdentifier(t) : null;
        } catch (Exception e) { return null; }
    }

    /**
     * Extracts the table name from Table_ref_auxContext using typed grammar-rule access.
     * No reflection, no text heuristics for the primary path.
     *
     * Primary (Стратегия 1): table_ref_aux_internal → instanceof one → dml.tableview_name()
     * Fallback (Стратегия 2): short token span → token-stream text minus alias suffix
     */
    private String extractTableName(PlSqlParser.Table_ref_auxContext ctx, String tableAlias) {
        // Стратегия 1: typed grammar-rule access
        var internal = ctx.table_ref_aux_internal();
        if (internal != null) {
            if (isComplexTableExpression(internal)) return null;
            if (internal instanceof PlSqlParser.Table_ref_aux_internal_oneContext one) {
                var dml = one.dml_table_expression_clause();
                if (dml != null && dml.tableview_name() != null) {
                    return BaseSemanticListener.cleanIdentifier(dml.tableview_name().getText());
                }
            }
        }

        // Стратегия 2 (fallback): token-stream text minus alias — only for short simple spans
        if (ctx.stop != null && ctx.start != null
                && (ctx.stop.getTokenIndex() - ctx.start.getTokenIndex()) > 50) {
            return null;
        }
        try {
            String full = extract(ctx);
            if (full == null || full.isBlank()) return null;
            // Strip alias suffix by whitespace boundary — not text search
            if (tableAlias != null && !tableAlias.isBlank()) {
                String upper = full.trim().toUpperCase();
                String aliasUpper = tableAlias.toUpperCase();
                int lastSpace = upper.lastIndexOf(' ');
                if (lastSpace >= 0 && upper.substring(lastSpace + 1).equals(aliasUpper)) {
                    full = full.trim().substring(0, lastSpace).trim();
                }
            }
            // Validate remaining: plain identifier pattern only (no parens, spaces, keywords)
            String candidate = full.trim().replaceAll("\\s+", "");
            if (!candidate.isEmpty() && candidate.matches("[A-Za-z_][A-Za-z0-9_.@]*")) {
                return BaseSemanticListener.cleanIdentifier(candidate);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns true when a table_ref_aux_internal context represents a complex expression
     * (subquery, LATERAL, TABLE(), JSON_TABLE, parenthesized join, ONLY clause) that
     * cannot be reduced to a plain table name.
     */
    private static boolean isComplexTableExpression(
            PlSqlParser.Table_ref_aux_internalContext internal) {
        // Parenthesized join: (t1 JOIN t2 ON ...)
        if (internal instanceof PlSqlParser.Table_ref_aux_internal_twoContext) return true;
        // ONLY (table) clause
        if (internal instanceof PlSqlParser.Table_ref_aux_internal_threContext) return true;
        if (internal instanceof PlSqlParser.Table_ref_aux_internal_oneContext one) {
            var dml = one.dml_table_expression_clause();
            if (dml == null) return true;
            // Complex forms checked via typed tokens / typed rule references
            return dml.select_statement()           != null
                || dml.subquery()                   != null
                || dml.LATERAL()                    != null
                || dml.json_table_clause()          != null
                || dml.table_collection_expression() != null;
        }
        return false;
    }

    /**
     * If table_ref_aux contains TABLE(collection_expr), returns a synthetic table name
     * like "FUNC_TABLE__SCHEMA__PKG__FUNC" (function name, dots replaced with __).
     * Returns null for any other table expression form.
     *
     * Example: TABLE(dwh.parse.strlist(x, ',')) t  →  "FUNC_TABLE__DWH__PARSE__STRLIST"
     *
     * Uses typed grammar-rule access only — no reflection.
     */
    private String extractTableCollectionName(PlSqlParser.Table_ref_auxContext ctx,
                                              String alias) {
        var internal = ctx.table_ref_aux_internal();
        if (!(internal instanceof PlSqlParser.Table_ref_aux_internal_oneContext one)) return null;
        var dml = one.dml_table_expression_clause();
        if (dml == null || dml.table_collection_expression() == null) return null;

        var tce = dml.table_collection_expression();
        String funcName = null;
        if (tce.expression() != null) {
            // Token-stream text (with spaces) for reliable leading-function extraction
            String exprText = extract(tce.expression());
            String raw = extractFunctionNameFromText(exprText);
            if (raw != null && !raw.isBlank()) {
                funcName = "FUNC_TABLE__" + raw.replace(".", "__");
            }
        }
        if (funcName == null || funcName.isBlank()) {
            funcName = alias != null && !alias.isBlank()
                    ? "FUNC_TABLE__" + alias.toUpperCase()
                    : "FUNC_TABLE";
        }
        return funcName;
    }

    /**
     * Extracts the leading function/constructor name from an expression text.
     * Handles both collapsed (getText) and spaced (extract) text.
     * Strips SQL keyword wrappers (CAST, MULTISET) to find the actual function.
     * Falls back to substring-before-paren only if result looks like a valid identifier.
     */
    private static String extractFunctionNameFromText(String exprText) {
        if (exprText == null || exprText.isBlank()) return null;
        String trimmed = exprText.trim();
        String upper = trimmed.toUpperCase().replaceAll("\\s+", "");
        // Skip pure SQL keyword wrappers — the inner function is what matters
        if (upper.startsWith("CAST(") || upper.startsWith("MULTISET(")) return null;
        // Find the function name: everything before the first '('
        String collapsed = upper;  // already no spaces
        int parenIdx = collapsed.indexOf('(');
        String candidate = parenIdx > 0 ? collapsed.substring(0, parenIdx) : collapsed;
        // Validate: must be a valid identifier (letters, digits, underscores, dots)
        if (candidate.isEmpty() || !candidate.matches("[A-Z_][A-Z0-9_.]*")) return null;
        // Skip if candidate is a plain SQL keyword (not a type/function name)
        if (SQL_BUILTIN_FUNCTIONS.contains(candidate)) return null;
        return candidate;
    }

    /**
     * HND-14: After TABLE(pkg.func()) creates a synthetic table geoid, resolve the
     * PIPELINED function's return COLLECTION → element RECORD/OBJECT type, then inject
     * its fields as virtual columns so downstream atom references resolve correctly.
     */
    private void injectColumnsFromPipelinedFunc(String syntheticTableGeoid,
                                                PlSqlParser.Table_ref_auxContext ctx) {
        var internal = ctx.table_ref_aux_internal();
        if (!(internal instanceof PlSqlParser.Table_ref_aux_internal_oneContext one)) return;
        var dml = one.dml_table_expression_clause();
        if (dml == null || dml.table_collection_expression() == null) return;
        var tce = dml.table_collection_expression();
        if (tce.expression() == null) return;

        String rawFuncName = extractFunctionNameFromText(extract(tce.expression()));
        if (rawFuncName == null) return;

        // Match routine by bare name (strip optional package prefix)
        String bareName = rawFuncName.contains(".")
                ? rawFuncName.substring(rawFuncName.lastIndexOf('.') + 1)
                : rawFuncName;
        com.hound.semantic.model.RoutineInfo ri =
                base.engine.getBuilder().getRoutines().values().stream()
                        .filter(r -> r.getName().equalsIgnoreCase(bareName))
                        .findFirst().orElse(null);
        if (ri == null || ri.getReturnType() == null) {
            base.engine.getBuilder().markPendingPipelined(syntheticTableGeoid);
            logger.debug("HAL2-01: PIPELINED pending — routine {} not resolved, table {}",
                    bareName, syntheticTableGeoid);
            return;
        }

        com.hound.semantic.model.PlTypeInfo collType =
                base.engine.getBuilder().resolvePlTypeByName(ri.getReturnType(), null);
        if (collType == null || !collType.isCollection()) {
            base.engine.getBuilder().markPendingPipelined(syntheticTableGeoid);
            logger.debug("HAL2-01: PIPELINED pending — collection type {} not resolved, table {}",
                    ri.getReturnType(), syntheticTableGeoid);
            return;
        }

        com.hound.semantic.model.PlTypeInfo elemType =
                base.engine.getBuilder().resolvePlTypeByName(collType.getElementTypeName(), null);
        if (elemType != null && (elemType.isRecord() || elemType.isObject())) {
            base.engine.getBuilder().injectColumnsFromPlType(syntheticTableGeoid, elemType);
            logger.debug("HND-14: injected {} cols from {} into {}",
                    elemType.getFields().size(), elemType.getName(), syntheticTableGeoid);
        } else {
            base.engine.getBuilder().markPendingPipelined(syntheticTableGeoid);
            logger.debug("HAL2-01: PIPELINED pending — element type {} not resolved, table {}",
                    collType.getElementTypeName(), syntheticTableGeoid);
        }
    }

    /**
     * Extracts the type constructor name from a PIPE ROW expression context.
     * Tries grammar-rule traversal first; falls back to text only for simple cases.
     * Returns the type ref (e.g. "CRM.T_PRICE_BREAK") or null if not resolvable.
     */
    private String extractConstructorTypeName(PlSqlParser.ExpressionContext exprCtx) {
        if (exprCtx == null) return null;
        // Use spaced text (extract) — avoids token-concatenation issues
        String text = extract(exprCtx);
        return extractFunctionNameFromText(text);
    }

    /**
     * Extracts the canonical text from a type_spec context using typed token checks.
     * Rule: getText() is only called at the leaf level (after navigating to the
     * specific element via grammar rules), never on the whole type_spec.
     *
     * Priority:  %ROWTYPE → %TYPE → type_name → datatype → getText() fallback
     */
    private static String extractTypeSpecText(PlSqlParser.Type_specContext ts) {
        if (ts == null) return null;
        if (ts.PERCENT_ROWTYPE() != null) {
            String base = ts.type_name() != null ? ts.type_name().getText() : "";
            return base + "%ROWTYPE";
        }
        if (ts.PERCENT_TYPE() != null) {
            String base = ts.type_name() != null ? ts.type_name().getText() : "";
            return base + "%TYPE";
        }
        if (ts.type_name() != null) return ts.type_name().getText();
        if (ts.datatype()  != null) return ts.datatype().getText();
        return ts.getText(); // last resort: REF or otherwise unrecognized form
    }

    /**
     * Порт Python: _init_routine_parameters()
     * Извлекает параметры из списка ParameterContext.
     */
    private void extractParameters(List<PlSqlParser.ParameterContext> params) {
        if (params == null) return;
        for (PlSqlParser.ParameterContext p : params) {
            if (p.parameter_name() == null) continue;
            String name = BaseSemanticListener.cleanIdentifier(p.parameter_name().getText());
            String type = p.type_spec() != null ? extractTypeSpecText(p.type_spec()) : "UNKNOWN";
            String mode = "IN";
            if (p.IN() != null && !p.IN().isEmpty() && p.OUT() != null && !p.OUT().isEmpty()) mode = "IN OUT";
            else if (p.OUT() != null && !p.OUT().isEmpty()) mode = "OUT";
            else if (p.INOUT() != null && !p.INOUT().isEmpty()) mode = "IN OUT";
            base.onRoutineParameter(name, type, mode);
        }
    }
}