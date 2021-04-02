package org.quacoin

import com.github.mustachejava.DefaultMustacheFactory
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.mustache.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import java.util.*

val bc = importBlockchain()

fun importBlockchain(): Blockchain {
    return try {
        QuaSave("quasave.yml").read()
    } catch (e: Exception) {
        Blockchain() of Address("${Date()}".sha256())
    }
}

fun main(args: Array<String>) { shutdownHook(); EngineMain.main(args) }

fun shutdownHook() {
    Runtime.getRuntime().addShutdownHook(Thread {
        run {
            QuaSave("quasave.yml", QuaSave.QuaSaveMode.Write, bc).write()
        }
    })
}

fun Application.module(testing: Boolean = true) {
    install(Mustache) {
        mustacheFactory = DefaultMustacheFactory("templates")
    }
    routing {
        get("/") {
            call.respond(MustacheContent("index.hbs", mapOf("holder" to bc.holder, "balance" to bc.balance())))
        }
        get("/styles") {
            call.respond(MustacheContent("style.css", mapOf<Unit, Unit>(), contentType = ContentType.Text.CSS))
        }
        get("/mine") {
            call.respond(MustacheContent("mine.hbs", mapOf<Unit, Unit>()))
        }
        get("/mine/proceed") {
            if (call.request.queryParameters["blocks"] == null) {
                call.respond(
                    MustacheContent(
                        "error.hbs",
                        mapOf("error" to "Blocks not specified")
                    )
                )
                return@get
            }
            bc.mine(call.request.queryParameters["blocks"]!!.toInt())
            call.respond(MustacheContent("mine_proceed.hbs", mapOf("blocks" to call.request.queryParameters["blocks"])))
        }
        get("/transfer") {
            call.respond(MustacheContent("transfer.hbs", mapOf<Unit, Unit>()))
        }
        get("/transfer/proceed") {
            val t = bc.newTransaction(
                bc.holder,
                Address(call.request.queryParameters["address"]!!),
                call.request.queryParameters["amount"]!!.toDouble()
            )
            call.respond(MustacheContent("transfer_proceed.hbs", mapOf("transfer" to t)))
        }
        get("/stat/chain") {
            call.respond(MustacheContent("chain_stat.hbs", mapOf("chain" to bc.chain)))
        }
    }
}