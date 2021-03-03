package org.quacoin

import java.nio.file.Files
import java.nio.file.Paths


fun main() {
    QuaSave("test.bin")
}

class QuaSave(val path: String) {
//    val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(path)))
    val bin = Files.readAllBytes(Paths.get(path))
    val magic = QuaSave::class.java.classLoader.getResourceAsStream("magic.bin")!!.readBytes()
    init {
        validate()
    }

    fun validate(): Boolean {
        return bin.slice(0..15).toByteArray().contentEquals(magic)
    }
}