const EMPLOYEE_API = "http://localhost:8080/expenses";

document.addEventListener("DOMContentLoaded", () => {
	loadExpenses(); //it will load expenses

    document.getElementById("addExpenseForm").addEventListener("submit", async (e) => {
        e.preventDefault(); //prevents the event to occur
        const token = localStorage.getItem("token");

		if(!token){
			alert("No authorization!");
			return;
		}

		//console.log("Submitting expense with token: ",token);


		const expenseName = document.getElementById("expenseName").value.trim();
        const amountInput = document.getElementById("amount").value.trim();
        const amount = amountInput ? parseFloat(amountInput) : NaN;
        const expenseType = document.getElementById("expenseType").value.trim().toUpperCase();
        const expenseDate = document.getElementById("expenseDate").value;


		if(!expenseName || isNaN(amount) || amount<=0 || !expenseType){
			alert("All fields are required and amount must be a valid number!");
			return;
		}

		const expenseData = {
            expenseName,
            amount,
            expenseType,
            expenseDate,
            status: "PENDING"
        };

				        // Debugging: Check input values in console
						console.log("Expense Name:", expenseName);
						console.log("Amount:", amountInput);
						console.log("Parsed Amount:", amount);
						console.log("Expense Type:", expenseType);
						console.log("Expense Date:", expenseDate);

try{
        const response = await fetch(`${EMPLOYEE_API}/add`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(expenseData)
        });

		//console.log("Response status: ",response.status);

        if (!response.ok) {
			const errorText = await response.text();
			throw new Error(errorText || "Unknown error");
        }
		alert("Expense submit!");
		loadExpenses(); //load all expenses

	}catch(error){
		console.error("Error submitting expense: ",error)
		alert(`Error: ${error.message}`);
	}
    });
});
 //fn to load expenses
async function loadExpenses() {
    const token = localStorage.getItem("token");
	if(!token){
		alert("No Authorization!");
		return;

	}

	try{
    const response = await fetch(`${EMPLOYEE_API}/my-expenses`, {
		method: "GET",
        headers: { "Authorization": `Bearer ${token}`,
	"Accept": "application/json"
}
    });

    if (response.ok) {
        const expenses = await response.json();
        const expenseTable = document.querySelector("#expenseTable tbody");
        expenseTable.innerHTML = "";

		// Extract role from token (decode JWT)
		const payload = JSON.parse(atob(token.split(".")[1]));
		const userRole = payload.role;  // Get role from token
		console.log("User Role:", userRole);

		//handle table for ui
        expenses.forEach(expense => {
            const row = expenseTable.insertRow();
            row.innerHTML = `
                <td>${expense.expenseName}</td>
                <td>${expense.amount}</td>
                <td>${expense.expenseType}</td>
                <td>${expense.expenseDate}</td>
                <td>${expense.status}</td>
      ${userRole === "EMPLOYEE" && expense.status === "PENDING" ?
                    `<td><button class="submitbtn" onclick="deleteExpense(${expense.id})">Delete</button></td>`
                        : `<td></td>`
                    }
                `;
            });
    }else if(response.status === 403){
		alert("Access denied! you can't view expenses!");
	} else{
		const error = await response.json().catch(()=>null);
		alert(`Failed to load! ${error.message}`);
	}

}catch(error){
	alert(`Error! ${error.message}`);
}
}

//function to delete the expense
async function deleteExpense(expenseId) {
    const token = localStorage.getItem("token");

    if (!token) {
        alert("Authorization token missing!");
        return;
    }

    if (!confirm("Are you sure you want to delete this expense?")) {
        return;
    }

    try {
        const response = await fetch(`${EMPLOYEE_API}/delete/${expenseId}`, {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });
		const errorText = await response.text();
        console.log("Server Response:", response.status, errorText); // 🔍 Debugging


        if (!response.ok) {
            alert(`Error: ${errorText}`);
            return;
        }

        alert("Expense deleted successfully!");
        loadExpenses(); // Refresh the list
    } catch (error) {
        console.error("Failed to delete expense:", error);
        alert("Failed to delete expense.");
    }
}
//fn for log out
function logout() {
    localStorage.removeItem("token");  // Remove JWT token
    window.location.href = "login.html";  // Redirect to login page
}