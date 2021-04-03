package org.quacoin.tayfun

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ButtonType
import javafx.stage.Stage
import org.quacoin.*
import tornadofx.*
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.util.*
fun isAddress(string: String): Boolean {
    return string.all { it in '0'..'9' || it in 'a'..'f' }
}

class Chain {
    companion object {
        val blockchain = importBlockchain()

        fun importBlockchain(): Blockchain {
            return try {
                QuaSave("quasave.yml").read()
            } catch (e: Exception) {
                println(e)
                Blockchain() of Address("${Date()}".sha256())
            }
        }
        fun shutdownHook() {
            QuaSave("quasave.yml", QuaSave.QuaSaveMode.Write, blockchain).write()
        }
    }
}
fun main(args: Array<String>) {
    println(Chain.blockchain)
    launch<Client>(args)
}
class Client: App(AppView::class) {
    override fun start(stage: Stage) {
        super.start(stage)
        beforeShutdown {
            Chain.shutdownHook()
        }
    }
    override fun stop() {
        Chain.shutdownHook()
        println("Stopping!")
    }
}
class AppView: View("Tayfun - QuaCoin graphical client implementation") {
    val fundsLabelProperty = SimpleStringProperty("Funds: ${Chain.blockchain.balance()} QUA")
    val blockLabelProperty = SimpleStringProperty("${Chain.blockchain.chain.count()} block, difficulty ${Chain.blockchain.chain.last().difficulty}, reward ${(Chain.blockchain.perDifficultyReward * Chain.blockchain.chain.last().difficulty).format(6)} QUA")
    override val root = vbox{
        style {
            setPrefSize(500.0, 300.0)
        }
        menubar {
            menu("Tayfun") {
                item("About").action {
                    information("About Tayfun", "QuaCoin graphical client implementation, powered by TornadoFX & JavaFX!", ButtonType.CLOSE, title = "About")
                }
            }
        }
        vbox {
            style {
                padding = CssBox(5.px, 5.px, 5.px, 5.px)
                fontSize = 14.px
            }
            label(fundsLabelProperty)
            label(blockLabelProperty)
            hbox {
                button("Send").action {
                    openInternalWindow<SendView>()
                }
                button("Receive").action {
                    openInternalWindow<ReceiveView>()
                }
                button("Mine ⛏").action {
                    openInternalWindow<MineView>()
                }
            }
        }
    }

    override fun onBeforeShow() {
        super.onBeforeShow()
        Chain.blockchain.registerCallback( "newTransaction"){
            fundsLabelProperty.set("Funds: ${Chain.blockchain.balance()}")
        }
        Chain.blockchain.registerCallback( "newBlock"){
            fundsLabelProperty.set("Funds: ${Chain.blockchain.balance()}")
            blockLabelProperty.set("${Chain.blockchain.chain.count()} block, difficulty ${Chain.blockchain.chain.last().difficulty}, reward ${(Chain.blockchain.perDifficultyReward * Chain.blockchain.chain.last().difficulty).format(6)} QUA")
        }
    }
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)

class SendView: View("Send") {
    val receiverAddressProperty = SimpleStringProperty()
    val amountProperty = SimpleDoubleProperty()
    override val root = vbox {
        style {
            padding = CssBox(5.px, 5.px, 5.px, 5.px)
        }
        hbox {
            label("Receiver: ")
            textfield(receiverAddressProperty) {
                setPrefSize(290.0, 10.0)
                filterInput { isAddress(it.controlNewText)}
            }
        }
        hbox {
            label("Amount: ")
            textfield(amountProperty) {
                filterInput { it.controlNewText.isDouble() }
            }
        }
        buttonbar {
            button("Send").action {
                warning("Is address correct?","Your funds may be lost if you specified address incorrectly!", ButtonType.CANCEL, ButtonType.FINISH) {
                    if(it == ButtonType.FINISH) {
                        sendFunds(receiverAddressProperty.value!!, amountProperty.value!!)
                        close()
                        this@SendView.close()
                    } else {
                        close()
                    }
                }
            }
        }
    }
    private fun sendFunds(to: String, amount: Double){
        println("$to:$amount")
    }
}
class ReceiveView: View("Receive") {
    override val root = vbox {
        label("Your address is '${Chain.blockchain.holder.address}'")
        buttonbar {
            button("Copy to clipboard").action {
                println("Copy to clipboard")
                Toolkit.getDefaultToolkit()
                    .systemClipboard
                    .setContents(
                        StringSelection(Chain.blockchain.holder.address),
                        null
                    )
                this@ReceiveView.close()
                information("Copied!")
            }
        }
    }
}
class MineView: View("Mine ⛏") {
    val count = SimpleIntegerProperty(1)
    override val root = vbox {
        hbox {
            label("Count: ")
            textfield (count) {
                filterInput { it.controlNewText.isInt() && it.controlNewText.toInt() > 0 }
            }
        }
        buttonbar {
            button("Mine").action {
                mineQua(count.value!!)
                information("Successfully mined ${count.value} block(s)!")
            }
        }
    }
    private fun mineQua(blocks: Int) {
        Chain.blockchain.mine(blocks)
    }
}