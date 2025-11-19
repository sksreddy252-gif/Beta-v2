-- Create a table with PostgreSQL data types
CREATE TABLE employees (
    emp_id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    hire_date DATE DEFAULT CURRENT_DATE,
    salary NUMERIC(10,2),
    department_id INTEGER NOT NULL
);

-- Index on last_name column
CREATE INDEX idx_emp_last_name ON employees(last_name);

-- Function to give salary raise
CREATE OR REPLACE FUNCTION give_raise(
    p_emp_id INTEGER,
    p_percent NUMERIC
) RETURNS void AS $$
BEGIN
    UPDATE employees
    SET salary = salary + (salary * p_percent / 100)
    WHERE emp_id = p_emp_id;
END;
$$ LANGUAGE plpgsql;

-- Grant privileges
GRANT SELECT, INSERT, UPDATE, DELETE ON employees TO hr_user;