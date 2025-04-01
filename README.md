# Expense Tracker

A role-based expense management system for organizations, allowing Employees to submit expenses, Managers to oversee expenses, and the Finance Team to allocate budgets and approve expenses.



## Tech Stack
- **Frontend:** HTML, CSS, JavaScript
- **Backend:** Spring Boot (Java 17), Spring Security (JWT), Lombok
- **Database:** PostgreSQL
- **Version Control:** Git



## Features

### Authentication & User Management
- Secure **JWT-based authentication**
- **Role-based access control (RBAC)** for **Employee, Manager, Finance Team**
- **Company email-based registration**

### Expense Management
- **Employees** can submit expenses for approval
- **Managers** can review Employee expenses and delete before approval also they can submit the expense for finance team
- **Finance Team** manage to paid expenses , they can also add the new departments
- **Managers can add expenses directly**, which go to Finance Team with his own approval

### Budget & Department Management
- **Finance Team** can allocate budgets per department
- **Finance Team** can **add new departments**
- Remaining budget is displayed for transparency to the finance dashboard

### Additional Features
- **Logout functionality**
- **Data validation & error handling**

## Setup Instructions
-- First Step
### Clone the Repository

git clone <URL of Repo>
cd expense-tracker -- go to the project file

-- Environment Variables
Ensure you have the following environment variables set in application.properties:

spring.datasource.url=jdbc:postgresql://localhost:5432/expensetracker (here the databse name comes)
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
jwt.secret=your_jwt_secret_key (secret key varies)