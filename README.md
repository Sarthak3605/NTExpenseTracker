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


## Project Structure

### Backend (Spring Boot - Java 17)
-- This contains all the models , controllers, service, dto (Data Transfer Object), repository files...
including the security files which manage the token and security measures, config which manage the rights to access

### Frontend (HTML, CSS, JavaScript)
-- This contains all the HTML files of dashboards , CSS files to style the pages and JS files such as auth.js which controls the login and register page, manager.js and other files.

## Setup Instructions
-- First Step
### Clone the Repository

git clone <URL of Repo>
cd expense-tracker -- go to the project file

--Second Step
### Backend Setup
Configure PostgreSQL in application.properties add username PostgreSQL link and password or some important syntax

-- Run the backend using:

## mvn clean spring-boot:run --This is main command to run the backend at http://localhost:8080/

Frontend Setup
Open index.html in a browser or just go live (for VS Code users) this will run at http://localhost:5500/
Ensure the backend is running for API calls

## API Endpoints
-- Authentication
POST /auth/register → User Registration

POST /auth/login → User Login

POST /auth/logout → User Logout

-- Expense Management
POST /employee/add-expense → Employee adds expense

POST /manager/add → Manager adds expense

POST /finance/approve-expense → Finance Team approves expense

DELETE /manager/delete-expense/{id} → Manager deletes pending expenses

-- Budget & Department
POST /finance/add-department → Add a new department

POST /finance/set-budget → Set budget for a department


-- Environment Variables
Ensure you have the following environment variables set in application.properties:

spring.datasource.url=jdbc:postgresql://localhost:5432/expensetracker (here the databse name comes)
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
jwt.secret=your_jwt_secret_key (secret key varies)
