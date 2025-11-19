-- PostgreSQL Migration Script Converted from Oracle SQL

-- 1. Table Creation
CREATE TABLE employees (
    emp_id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    hire_date DATE DEFAULT CURRENT_DATE,
    salary NUMERIC(10,2),
    department_id INTEGER NOT NULL
);

-- 2. Index Creation
CREATE INDEX idx_emp_last_name ON employees(last_name);

-- 3. Stored Procedure for Salary Raise
CREATE OR REPLACE FUNCTION give_raise(p_emp_id BIGINT, p_percent NUMERIC)
RETURNS VOID AS $$
BEGIN
    UPDATE employees
    SET salary = salary + (salary * p_percent / 100)
    WHERE emp_id = p_emp_id;
END;
$$ LANGUAGE plpgsql;

-- 4. Grant Statements
GRANT SELECT, INSERT, UPDATE, DELETE ON employees TO hr_user;

-- Migration Notes:
-- - Oracle NUMBER mapped to BIGSERIAL/INTEGER/NUMERIC as appropriate.
-- - VARCHAR2 mapped to VARCHAR.
-- - DATE DEFAULT SYSDATE mapped to DATE DEFAULT CURRENT_DATE.
-- - Oracle's UPPER() is the same in PostgreSQL.
-- - Oracle's NOW() mapped to PostgreSQL's CURRENT_TIMESTAMP.
-- - IN clause remains the same for subqueries; for array containment, use ANY or @> operator.
-- - JOIN syntax is standard SQL in both.
-- - Index creation is similar; use EXPLAIN ANALYZE for performance tuning.
-- - Transaction control (COMMIT/ROLLBACK) is managed outside functions in PostgreSQL.
