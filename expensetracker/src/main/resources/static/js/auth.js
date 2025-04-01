const BASE_URL = "http://localhost:8080/auth";  // Update with actual backend URL

document.addEventListener("DOMContentLoaded", () => { //it will call when page is loaded

    const loginForm = document.getElementById("loginForm");
    const registerForm = document.getElementById("registerForm");

	//when you try to login it will run this
    if (loginForm) {
        loginForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const email = document.getElementById("email").value.trim();
            const password = document.getElementById("password").value.trim();
			//console.log("Clicked on login, sending request...");
			document.getElementById('errorMessage').innerText = "";

     try{ //try - catch block to handle exception
            const response = await fetch(`${BASE_URL}/login`, {
                method: "POST", //post request
                headers: { "Content-Type": "application/json" }, //this will ensure the content is of JSON type
                body: JSON.stringify({ email, password }), //convert into string like key value pair
            });

			//console.log("Response Received: ",response.status); //debug

            if (response.ok) {
				const token = await response.text();

				if(!token){
					alert("Invalid login no token");
					return;
				}

                localStorage.setItem("token", token);
       try{
				const payload = JSON.parse(atob(token.split(".")[1]));
				//console.log("decoded jwt :",payload);

                const role = Array.isArray(payload.authorities) ? payload.authorities.map(a => a.authority).join(",") : payload.role;
                //console.log("Extracted Role:", role);

				if(!role){
					alert("Invalid role!");
					return;
				}


                localStorage.setItem("role", role);
                redirectToDashboard(role);

            } catch (error){
				showErrorMessage("Invalid token structure");
            }}else{

				setTimeout(function() {
					document.getElementById('errorMessage').innerText = "Invalid Credentials!";
					setTimeout(function() {
						document.getElementById('errorMessage').innerText = "";
					}, 2000);
				}, 1000);

			}
        }
	catch(error){
		console.error("Error during login",error);
	}
});
    }

	//if try to register this will execute
	if (registerForm) {

        registerForm.addEventListener("submit", async (e) => {
            e.preventDefault();

            const name = document.getElementById("name").value.trim();
            const email = document.getElementById("email").value.trim();
            const password = document.getElementById("password").value.trim();
            const role = document.getElementById("role").value.trim();
            const department = document.getElementById("department").value.trim();

			if(!email.endsWith("@gmail.com")){
				alert("Please use a valid company email");
				return;
			}

            const requestData = { //method 2: we can also get elements directly here inside this
                name,
                email,
                password,
                role: role.toUpperCase(),
                department
            };

            //console.log("Sending Registration Data:", JSON.stringify(requestData)); // Debug

     try{
            const response = await fetch(`${BASE_URL}/register`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(requestData),
            });

            if (response.ok) {
                alert("Registration successful! Please login.");
                window.location.href = "login.html";
            } else {
                const errorMessage = await response.text();
                alert("Registration failed: " + errorMessage);
                console.error("Registration Error:", errorMessage);
            }

		}catch(error){
			console.error("Registration Error: ",error);
			alert("An error occured during registration.");
		}
        });
    }
});

//this will redirect to the dashboards for different role using if else lader
function redirectToDashboard(role) {
	console.log("User role: ",role);
    if (role === "EMPLOYEE") {window.location.href = "dashboard-employee.html";}
    else if (role === "MANAGER") {window.location.href = "dashboard-manager.html";}
    else if (role === "FINANCE_TEAM") {window.location.href = "dashboard-finance.html";}
	else{alert("Unknown Role: "+role);}
}
