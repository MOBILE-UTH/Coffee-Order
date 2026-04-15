package com.coffee.server.routes

import io.ktor.http.ContentType
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

private val HEALTH_HTML = """
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Coffee Order Server</title>
    <style>
        body {
            font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            display: grid;
            place-items: center;
            min-height: 100vh;
            margin: 0;
            background: #f7f3ef;
            color: #2c1f18;
        }
        .card {
            background: white;
            border-radius: 16px;
            padding: 32px 40px;
            box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
            text-align: center;
        }
        h1 { margin: 0 0 12px; }
        p { margin: 0; color: #6b5b53; }
        code {
            display: inline-block;
            margin-top: 16px;
            padding: 6px 10px;
            border-radius: 8px;
            background: #f2ebe6;
        }
    </style>
</head>
<body>
<main class="card">
    <h1>☕ Coffee Order Server is running</h1>
    <p>The server is healthy and ready to receive requests.</p>
    <code>GET /health</code>
</main>
</body>
</html>
""".trimIndent()

fun Route.healthRoutes() {
    get("/health") {
        call.respondText(HEALTH_HTML, ContentType.Text.Html)
    }

    route("/api") {
        get("/health") {
            call.respondText(HEALTH_HTML, ContentType.Text.Html)
        }
    }
}
