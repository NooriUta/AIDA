-- SKADI test seed: 3 functions + 2 views
-- Used by PostgreSQLSkadiFetcherIntTest (Testcontainers)

CREATE OR REPLACE FUNCTION fn_add(a int, b int) RETURNS int
    LANGUAGE plpgsql AS $$ BEGIN RETURN a + b; END $$;

CREATE OR REPLACE FUNCTION fn_subtract(a int, b int) RETURNS int
    LANGUAGE plpgsql AS $$ BEGIN RETURN a - b; END $$;

CREATE OR REPLACE FUNCTION fn_multiply(a int, b int) RETURNS int
    LANGUAGE plpgsql AS $$ BEGIN RETURN a * b; END $$;

CREATE VIEW v_sum     AS SELECT fn_add(1, 2)      AS result;
CREATE VIEW v_product AS SELECT fn_multiply(3, 4) AS result;
