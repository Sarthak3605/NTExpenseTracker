const MANAGER_API = "http://localhost:8080/expenses";

document.addEventListener("DOMContentLoaded", function () {
    const token = localStorage.getItem("token");

    if (!token) {
        alert("No Authorization");
        return;
    }

    //console.log("Token found:", token);  // Debugging log
    loadPendingExpenses(); //load pending expenses

    document.getElementById("approveExpenseForm").addEventListener("submit", function (event) {
        event.preventDefault();

        const expenseId = document.getElementById("expenseId").value;
        const status = document.getElementById("approvalStatus").value;

        if (!expenseId || !status) {
            alert("Please provide Expense ID and select a status.");
            return;
        }

        updateExpenseStatus(expenseId, status); //update status
    });
});

//load all expenses
function loadPendingExpenses() {
    const token = localStorage.getItem("token");

    fetch(`${MANAGER_API}/pending`, {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        }
    })
    .then(response => {
        //console.log("Fetch Response Status:", response.status); // Debugging log
        if (!response.ok) {
			return response.json().then(err => {
                throw new Error(err.message || "Access Denied: You don't have permission!");
            });
        }
        return response.json();
    })
    .then(data => {
        //console.log("Pending Expense Data:", data);
        const tableBody = document.getElementById("pendingExpensesTable").querySelector("tbody");
        tableBody.innerHTML = '';

        if (!data || data.length === 0) {
            tableBody.innerHTML = "<tr><td colspan='7'>No pending expenses</td></tr>";
            return;
        }
//fill data in table
        data.forEach(expense => {
            let row = document.createElement("tr");
            row.innerHTML = `
                <td>${expense.id || 'N/A'}</td>
                <td>${expense.user?.name || 'N/A'}</td>
                <td>${expense.amount || 0}</td>
                <td>${expense.expenseType || 'N/A'}</td>
                <td>${expense.expenseDate || 'N/A'}</td>
                <td id="status-${expense.id}">${expense.status || 'N/A'}</td>
                <td>
                    <button class="submitbtn" onclick="updateExpenseStatus(${expense.id}, 'APPROVED')">Approve</button>
                    <button class="submitbtn" onclick="updateExpenseStatus(${expense.id}, 'REJECTED')">Reject</button>
                </td>
            `;
            tableBody.appendChild(row);
        });
    })
    .catch(error => {
        //console.error("Error loading expenses:", error);
        alert(error.message);
    });
}
//update the status function
function updateExpenseStatus(expenseId, status) {
    const token = localStorage.getItem("token");

    fetch(`${MANAGER_API}/${expenseId}/update-status?status=${status}`, {
        method: "PUT",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        }
    })
    .then(response => {
       // console.log("Update Status Response:", response.status); // Debugging log
        if (response.status === 403) {
            throw new Error("Access Denied: You don't have permission!");
        }
        if (!response.ok) {
            throw new Error(`Error: ${response.statusText} (Status: ${response.status})`);
        }
        return response.json();
    })
    .then(data => {
        alert(`Expense updated successfully!`);
        loadPendingExpenses(); //load expenses again
    })
    .catch(error => {
        //console.error("Error updating expense:", error);
        alert(error.message);
    });
}


document.getElementById("addManagerExpenseForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const expenseName = document.getElementById("managerExpenseName").value.trim();
    const amountInput = document.getElementById("managerAmount").value.trim();
    const amount = amountInput ? parseFloat(amountInput) : NaN;
    const expenseType = document.getElementById("managerExpenseType").value.trim().toUpperCase();
    const expenseDate = document.getElementById("managerExpenseDate").value;
	const departmentId= localStorage.getItem("departmentId")

	const expenseData = {
        expenseName: expenseName,
        amount: amount,
        expenseType: expenseType,
        expenseDate: expenseDate,
        departmentId: departmentId
    };
    const token = localStorage.getItem("token"); //Get JWT from localStorage
    //console.log("🔹 Sending Token:", token); // Debugging

    if (!token) {
        alert("User not logged in! Please log in again.");
        return;
    }

    try {
        const response = await fetch(`${MANAGER_API}/manager/add`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(expenseData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error("Failed to submit!" + errorText);
        }

        alert("Expense submitted for approval from finance team");
		location.reload(); //load the page
    } catch (error) {
        //console.error("Error submitting expense:", error);
        alert(`Error: ${error.message}`);
    }

});
//fn for log out
function logout() {
    localStorage.removeItem("token");  // Remove JWT token
    window.location.href = "login.html";  // Redirect to login page
}
