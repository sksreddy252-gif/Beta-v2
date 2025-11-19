-- PostgreSQL Migration Script (converted from Oracle)

CREATE TABLE employees (
    emp_id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    hire_date DATE DEFAULT CURRENT_DATE,
    salary NUMERIC(10,2),
    department_id INTEGER NOT NULL
);

CREATE INDEX idx_emp_last_name ON employees(last_name);

CREATE OR REPLACE FUNCTION give_raise(p_emp_id BIGINT, p_percent NUMERIC)
RETURNS VOID AS $$
BEGIN
    UPDATE employees
    SET salary = salary + (salary * p_percent / 100)
    WHERE emp_id = p_emp_id;
END;
$$ LANGUAGE plpgsql;

GRANT SELECT, INSERT, UPDATE, DELETE ON employees TO hr_user;

-- Key Migration Notes:
-- 1. NUMBER(10) mapped to BIGSERIAL for auto-increment PK.
-- 2. VARCHAR2 mapped to VARCHAR.
-- 3. DATE DEFAULT SYSDATE mapped to DATE DEFAULT CURRENT_DATE.
-- 4. NUMBER(10,2) mapped to NUMERIC(10,2).
-- 5. Sequence/trigger replaced by BIGSERIAL.
-- 6. Procedure converted to PL/pgSQL function (transaction control handled externally).
-- 7. Index syntax is similar.
-- 8. Grant syntax is similar.
-- 9. Use EXPLAIN ANALYZE in PostgreSQL for performance tuning.
