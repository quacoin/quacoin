import java.security.MessageDigest
import java.util.*

fun main() {
    val bc = Blockchain() of Address("")
    println(bc.chain[0])
}

class Blockchain(genesis: Block = generateGenesis()) {
    val chain = mutableListOf<Block>()
    val transactions = mutableListOf<Transaction>()
    var holder: Address = Address("")

    init {
        chain.add(genesis)
    }
    infix fun of(address: Address): Blockchain {
        holder = address
        return this
    }
}

fun generateGenesis(): Block {
    val genesis = Block(
        id = 0,
        transactions = mutableListOf(),
        proof = 0,
        previousHash = "",
        hash = ""
    )
    with(genesis) {
        this.proof  = this.generateProof()
        this.hash   = this.hash()
    }
    return genesis
}

data class Block(
    var id: Number,
    var timestamp: Date = Date(),
    var proof: Number,
    val transactions: MutableList<Transaction>,
    var hash: String,
    var previousHash: String
) {
    fun hash(): String = "$id:$timestamp:$transactions:$previousHash:$proof".sha256()
    fun generateProof(): Number {
        var guess: Int = 0
        println("$id:$timestamp:$transactions:$previousHash:$guess".sha256().substring(62..63))
        while (!validProof(guess)) {
            guess += 1
            println("$id:$timestamp:$transactions:$previousHash:$guess".sha256().substring(62..63))
        }
        return guess
    }

    fun validProof(proof: Number): Boolean = "$id:$timestamp:$transactions:$previousHash:$proof".sha256().substring(62..63) == "00"
}

data class Transaction(val from: Address, val to: Address, val amount: Number)

data class Address(val address: String){
    fun none(): Address = Address("")
}

fun String.sha256() = this.hash("SHA-256")
fun String.hash(algorithm: String): String {
    return MessageDigest
        .getInstance(algorithm)
        .digest(this.toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })
}