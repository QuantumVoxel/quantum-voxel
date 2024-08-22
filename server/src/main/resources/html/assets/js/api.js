function login() {
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;

    // Send to /api/login
    // If success, redirect to dashboard
    // If fail, display error

    fetch("/api/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username: username,
            password: password
        })
    }).then((response) => {
        if (response.ok) {
            window.location.href = "/dashboard";
        } else {
            alert("Invalid username or password");
        }
    });
}

function fetch_console() {
    fetch("/api/console-logs")
        .then((response) => response.text())
        .then((data) => {
            document.getElementById("console").innerText = data;

            let console = document.getElementById("console");
            if (!console) console.scrollTop = console.scrollHeight;
        });
}

let last_fetch = 0;

function should_fetch_console() {
    // Check based on time, every 1 second

    var now = Date.now();

    if (now - last_fetch > 1000) {
        last_fetch = now;
        return true;
    } else {
        return false;
    }
}

setInterval(() => {
    if (should_fetch_console()) fetch_console();
}, 100);
