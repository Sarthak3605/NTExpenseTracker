const FINANCE_API = "http://localhost:8080/finance";

document.addEventListener("DOMContentLoaded", () => {
	loadPendingPayments(); //load all payments
	loadRemainingBudget(); //load the calculated remaining budgets

    document.getElementById("addBudgetForm").addEventListener("submit", async (e) => {
        e.preventDefault(); //prevent to submit
        const token = localStorage.getItem("token");

		if(!token){
			alert("No authorization!");
			return;
		}
        //all the data we get
        const budgetData = {
            departmentName: document.getElementById("departmentName").value,
            totalBudget: parseFloat(document.getElementById("totalBudget").value),
            startDate: document.getElementById("startDate").value,
            endDate: document.getElementById("endDate").value
        };

       try{
        const response = await fetch(`${FINANCE_API}/allocate`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(budgetData)
        });

		//console.log("Response Status:",response.status);

        if (response.ok) {
            alert("Budget allocated!");
			loadPendingPayments(); //load payments
        }
		else if(response.status === 403){
			alert("Access denied!");
		} else {
			const error = await response.text();
            alert(`Failed to allocate budget! ${error.message}`);
        }

	}catch(error){
alert(`Error: ${error.message}`)
	}
    });
});

//load all payments that are pending
function loadPendingPayments() {
	const token = localStorage.getItem("token");
	if (!token) {
        console.error("No authorization token found!");
        return;
    }

    fetch(`${FINANCE_API}/pending-payments`, {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Failed to load pending payments");
        }
        return response.json();
    })
    .then(data => {
        console.log("Loaded Pending Payments:", data);
        displayPendingPayments(data);
    })
    .catch(error => {
        console.error("Error loading pending payments:", error);
		alert(error.message);
    });
}

//function to display the table in ui
function displayPendingPayments(payments) {
    const tableBody = document.querySelector("#pendingPayments tbody");
    tableBody.innerHTML = ""; // Clear existing table rows

    if (!payments ||!Array.isArray(payments) || payments.length === 0) {
        tableBody.innerHTML = "<tr><td colspan='7'>No pending payments available</td></tr>";
        return;
    }

    payments.forEach((payment) => {
		if(!payment) return;

        let row = document.createElement("tr");

		//console.log("Processing Payment: ",payment);

		const userName = payment.user ? payment.user.name || "Unknown" : "Unknown";

		let actionbtn = "";
		if (payment.status === 'PENDING') {
            if (payment.user && payment.user.role === 'EMPLOYEE') {
                actionButtons = `<button class="submitbtn" onclick="markExpenseAsPaid(${payment.id})" class="btn btn-success">Mark as Paid</button>`;
            } else if (payment.user && payment.user.role === 'MANAGER') {
                actionButtons = `
                    <button class="submitbtn" onclick="approveExpense(${payment.id})" class="btn btn-success">Approve</button>
                    <button class="submitbtn" onclick="rejectExpense(${payment.id})" class="btn btn-danger">Reject</button>
                `;
            }
		}

//fill the row with data
        row.innerHTML = `
		    <td>${payment.user ? payment.user.name : "Unknown"}</td>
            <td>${payment.expenseName || "N/A"}</td>
            <td>${payment.amount || 0}</td>
            <td>${payment.expenseType || 'N/A'}</td>
            <td>${payment.expenseDate || 'N/A'}</td>
            <td>${payment.status || 'N/A'}</td>
            <td>
                <button class="submitbtn" onclick="markExpenseAsPaid(${payment.id})" class="btn btn-success">Mark as Paid</button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

// Function to approve an expense (for manager expenses)
async function approveExpense(expenseId) {
    if (!confirm("Are you sure you want to approve this expense?")) {
        return;
    }

    const token = localStorage.getItem("token");
    if (!token) {
        console.error("Token missing!");
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/expenses/approve/${expenseId}`, {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Error approving the expense.");
        }

        alert("Expense approved successfully!");
        loadPendingPayments();  // Refresh the expense list after approval
    } catch (error) {
        console.error("Server response: ", error.message);
        alert(error.message);
    }
}

// Function to reject an expense (for manager expenses)
async function rejectExpense(expenseId) {
    if (!confirm("Are you sure you want to reject this expense?")) {
        return;
    }

    const token = localStorage.getItem("token");
    if (!token) {
        console.error("Token missing!");
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/expenses/reject/${expenseId}`, {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Error rejecting the expense.");
        }

        alert("Expense rejected successfully!");
        loadPendingPayments();  // Refresh the expense list after rejection
    } catch (error) {
        //console.error("Server response: ", error.message);
        alert(error.message);
    }
}
// function to mark as paid by the finance team
async function markExpenseAsPaid(expenseId) {
	if (!confirm("Are you sure you want to mark this expense as paid?")) {
		return;
	}
    const token = localStorage.getItem("token");
   // console.log("Token sent: ", token); // Debugging

    if (!token) {
        console.error("Token missing!");
        return;
    }

        fetch(`http://localhost:8080/expenses/${expenseId}/mark-paid`, {
			method: "PUT",
			headers: {
				"Authorization": `Bearer ${token}`,
				"Content-Type": "application/json"
			}
		})
		.then(response => {
			//console.log("Response status:", response.status);
			if (!response.ok) {
				return response.text().then(err => {
					throw new Error(err || "Access Denied: You don't have permission!");
				});
			}
			return response.json();
		})
		.then(data => {
			alert(`Expense marked as Paid successfully!`);
			loadPendingPayments();  // Refresh the list
		})
		.catch(error => {
			//console.error("Server response: ", error.message);
			alert(error.message);
		});
}
//function to load remaining budget
async function loadRemainingBudget() {
	const token = localStorage.getItem("token");
	//console.log("JWT Token Sent:", token);

    if (!token) {
        console.error("Authorization token missing!");
        return;
    }
	const payload = JSON.parse(atob(token.split(".")[1])); // Decodes the JWT payload
    const department = payload.department; // Extract department

    if (!department) {
        console.error("❌ Department not found in token!");
        return;
    }

    //console.log("✅ Extracted Department:", department);


    try {
        const response = await fetch(`http://localhost:8080/budget/remaining/${department}`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
			const errorText = await response.text();
            //console.error("API Error Response:", errorText);
            throw new Error(`Failed to load remaining budget: ${errorText}`);
        }

		const remainingBudget = await response.json();
		//console.log(remainingBudget);

		const budgetElement = document.getElementById("remainingBudget");
        if (budgetElement) {
            budgetElement.textContent = `${remainingBudget}`;
        } else {
            console.error("Element with ID 'remainingBudget' not found!");
        }
    } catch (error) {
        console.error("Error fetching remaining budget:", error);
    }
}
 //when form submit
document.getElementById("addDepartmentForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const departmentName = document.getElementById("departmentName").value;

    if (!departmentName) {
        alert("Please enter a department name.");
        return;
    }

    const token = localStorage.getItem("token");

    try {
        const response = await fetch("http://localhost:8080/departments/add", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify({ name: departmentName })
        });

        if (response.ok) {
            alert(departmentName+" Department added successfully.");

        } else {
            const errorMessage = await response.text();
            alert("Error: " + errorMessage);
        }
    } catch (error) {
        //console.error("Error adding department:", error);
        alert("Failed to add department.");
    }
});
//fn for log out
function logout() {
    localStorage.removeItem("token");  // Remove JWT token
    window.location.href = "login.html";  // Redirect to login page
}