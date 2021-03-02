package org.quacoin

import com.github.mustachejava.DefaultMustacheFactory
import io.ktor.application.*
import io.ktor.mustache.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import kotlinx.coroutines.async
import java.io.File
import java.util.*

val bc = Blockchain() of Address(Date().toString().sha256())

fun main(args: Array<String>) = EngineMain.main(args)
class ResourceLoader {
    companion object {
        fun resourceAsFile(path: String): File {
            return File(javaClass.classLoader.getResource(path).file)
        }
    }
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
            call.respondFile(ResourceLoader.resourceAsFile("templates/style.css"))
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
            async {
                bc.mine(call.request.queryParameters["blocks"]!!.toInt())
            }
            call.respond(MustacheContent("mine_proceed.hbs", mapOf("blocks" to call.request.queryParameters["blocks"])))
        }
        get("/transfer") {
            call.respond(MustacheContent("transfer.hbs", mapOf<Unit, Unit>()))
        }
        get("/transfer/proceed") {
            val t = bc.newTransaction(Transaction(bc.holder, Address(call.request.queryParameters["address"]!!), call.request.queryParameters["amount"]!!.toDouble()))
            call.respond(MustacheContent("transfer_proceed.hbs", mapOf("transfer" to t)))
        }
        get("/stat/chain") {
            call.respond(MustacheContent("chain_stat.hbs", mapOf("chain" to bc.chain)))
        }
    }
}