-- Table: Department
CREATE TABLE department (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL
);

-- Table: Budget
CREATE TABLE budget (
    id SERIAL PRIMARY KEY,
    department_id INT UNIQUE NOT NULL,
    total_budget DECIMAL(15,2) NOT NULL,
    remaining_budget DECIMAL(15,2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE CASCADE
);

-- Table: Users
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('EMPLOYEE', 'MANAGER', 'FINANCE_TEAM')),
    department_id INT NOT NULL,
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE CASCADE
);

-- Table: Expense
CREATE TABLE expense (
    id SERIAL PRIMARY KEY,
    expense_name VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    expense_type VARCHAR(50) NOT NULL CHECK (expense_type IN ('TRAVEL', 'FOOD', 'SUPPLIES', 'OTHER')),
    expense_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    user_id INT NOT NULL,
    department_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE CASCADE
);

-- Table: Approval
CREATE TABLE approval (
    id SERIAL PRIMARY KEY,
    expense_id INT NOT NULL,
    approved_by INT NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('APPROVED', 'REJECTED')),
    comment TEXT,
    approval_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (expense_id) REFERENCES expense(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE CASCADE
);
INSERT INTO department (name) VALUES 
('HR'), ('Finance'), ('IT'), ('Marketing'), ('Sales')
ON CONFLICT (name) DO NOTHING;