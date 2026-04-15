package com.coffee.server

import com.coffee.server.database.DatabaseFactory
import com.coffee.server.plugins.configureAuthentication
import com.coffee.server.plugins.configureRouting
import com.coffee.server.plugins.configureSerialization
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    DatabaseFactory.init()

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureSerialization()
        configureAuthentication()
        configureRouting()
    }.start(wait = true)
}
