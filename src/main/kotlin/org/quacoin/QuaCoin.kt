package org.quacoin

import java.security.MessageDigest
import java.util.*

fun main() {
    val bc = Blockchain() of Address(Date().toString().sha256())
//    println(bc.chain[0])
    println(bc)
//    bc.newTransaction(org.quacoin.PendingTransaction() from org.quacoin.Address("0") to bc.holder amount 100.0)
    println("Mining 5 blocks")
    bc.mine(5)
    println(bc)
    println(bc.balance())
}

class Blockchain(genesis: Block = generateGenesis()) {
    val chain = mutableListOf<Block>()
    val transactions = mutableListOf<Transaction>()
    var holder: Address = Address("")

    init {
        chain.add(genesis)
        mine(5)
    }

    infix fun of(address: Address): Blockchain {
        holder = address
        return this
    }

    fun balance(): Number {
        return balance(holder);
    }

    fun balance(address: Address): Number {
        var balance = 0.0
        chain.forEach {
            it.transactions.forEach {
                if (it.from == address) balance -= it.amount
                else if (it.to == address) balance += it.amount
            }
        }
        return balance
    }

    override fun toString(): String {
        return "Blockchain(chain=$chain, transactions=$transactions, holder=$holder)"
    }

    fun newTransaction(t: Transaction): Transaction {
        transactions.add(t)
        return t
    }

    fun newTransaction(pendingT: PendingTransaction): Transaction {
        return newTransaction(pendingT.final())
    }

    fun newTransaction(from: Address, to: Address, amount: Double): Transaction {
        return newTransaction(Transaction(from, to, amount))
    }

    fun mine(): Boolean {
        try {
            val block = Block(
                id = chain.count(),
                transactions = transactions,
                proof = 0,
                previousHash = chain[chain.count()-1].hash,
                difficulty = chain.count() / 1000 + 1
            )
            chain.add(block)
            transactions.clear()
            newTransaction(PendingTransaction() from Address("0") to holder amount block.difficulty * 0.01)
            println("debug: balance: ${balance()}")
            println("debug: difficulty: ${block.difficulty}")

            return true
        } catch (e: Exception){
            return false
        }
    }
    fun mine(num: Int) {
        for (i in 0 until num) {
            mine()
        }
    }
    fun import(bc: Blockchain) {
        this.transactions.clear()
        this.transactions.addAll(bc.transactions)
        this.chain.clear()
        this.chain.addAll(bc.chain)
    }
    fun export(): Blockchain {
        return this.clone()
    }
    fun clone(): Blockchain {
        val self = this
        val bc = Blockchain() of Address("")
        with(bc) {
            this.transactions.clear()
            this.transactions.addAll(self.transactions)
            this.chain.clear()
            this.chain.addAll(self.chain)
        }
        return bc
    }
}

fun generateGenesis(): Block {
    val genesis = Block(
        id = 0,
        transactions = mutableListOf(),
        proof = 0,
        previousHash = "",
        difficulty = 1
    )
    return genesis
}

data class Block(
    var id: Number,
    var timestamp: Date = Date(),
    var proof: Number,
    val transactions: MutableList<Transaction>,
    var hash: String = "",
    var previousHash: String,
    val difficulty: Int
) {
    init {
        assert(difficulty > 0)
        this.proof = this.generateProof()
        this.hash = this.hash()
    }
    fun hash(): String = "$id:$timestamp:$transactions:$previousHash:$proof:$difficulty".sha256()
    fun generateProof(): Number {
        var guess: Int = 0
//        println("$id:$timestamp:$transactions:$previousHash:$guess:$difficulty".org.quacoin.sha256().substring(63 - difficulty..63))
        while (!validProof(guess)) {
            guess += 1
//            println(
//                "$id:$timestamp:$transactions:$previousHash:$guess:$difficulty".org.quacoin.sha256().substring(63 - difficulty..63)
//            )
        }
        return guess
    }

    fun validProof(proof: Number): Boolean =
        "$id:$timestamp:$transactions:$previousHash:$proof:$difficulty".sha256()
            .substring(63 - difficulty..63) == "0" times difficulty + 1
}

private infix fun String.times(i: Int): String {
    return this.repeat(i)
}

data class Transaction(val from: Address, val to: Address, val amount: Double)

class PendingTransaction() {
    var from: Address = Address("")
    var to: Address = Address("")
    var amount: Double = 0.0
    infix fun from(a: Address): PendingTransaction {
        this.from = a
        return this
    }

    infix fun to(a: Address): PendingTransaction {
        this.to = a
        return this
    }

    infix fun amount(amount: Double): PendingTransaction {
        this.amount = amount
        return this
    }

    fun final(): Transaction {
        return Transaction(from, to, amount)
    }

    override fun toString(): String {
        return "PendingTransaction(from=$from, to=$to, amount=$amount)"
    }

}

data class Address(val address: String) {
    fun none(): Address = Address("")
}

fun String.sha256() = this.hash("SHA-256")
fun String.hash(algorithm: String): String {
    return MessageDigest
        .getInstance(algorithm)
        .digest(this.toByteArray())
        .fold("", { str, it -> str + "%02x".format(it) })
}