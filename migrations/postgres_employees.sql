-- PostgreSQL Migration of Oracle EMPLOYEES Table and Related Objects

-- 1. Table Creation: Data Types & Defaults
CREATE TABLE EMPLOYEES (
    EMP_ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- Auto-increment replaces sequence/trigger
    FIRST_NAME VARCHAR(50),
    LAST_NAME VARCHAR(50),
    EMAIL VARCHAR(100) UNIQUE,
    HIRE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- SYSDATE mapped to CURRENT_TIMESTAMP
    SALARY NUMERIC(10,2),
    DEPARTMENT_ID INTEGER NOT NULL
);

-- 2. Index Creation
CREATE INDEX IDX_EMP_LAST_NAME ON EMPLOYEES(LAST_NAME);

-- 3. Salary Raise Function (PostgreSQL uses functions for DML)
CREATE OR REPLACE FUNCTION GIVE_RAISE(p_emp_id BIGINT, p_percent NUMERIC)
RETURNS VOID AS $$
BEGIN
    UPDATE EMPLOYEES
    SET SALARY = SALARY + (SALARY * p_percent / 100)
    WHERE EMP_ID = p_emp_id;
    -- Transaction control is managed outside the function in PostgreSQL
END;
$$ LANGUAGE plpgsql;

-- 4. Grant Privileges
GRANT SELECT, INSERT, UPDATE, DELETE ON EMPLOYEES TO HR_USER;

-- 5. Performance Optimization
-- Use EXPLAIN ANALYZE in PostgreSQL to compare and optimize query performance.
-- Example: EXPLAIN ANALYZE SELECT * FROM EMPLOYEES WHERE LAST_NAME = 'Smith';
