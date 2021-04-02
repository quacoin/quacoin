package org.quacoin

import com.charleskorn.kaml.Yaml
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

fun gzip(content: String): ByteArray {
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).bufferedWriter(UTF_8).use { it.write(content) }
    return bos.toByteArray()
}

fun ungzip(content: ByteArray): String =
    GZIPInputStream(content.inputStream()).bufferedReader(UTF_8).use { it.readText() }
class QuaSave(val path: String, val mode: QuaSaveMode = QuaSaveMode.Read, val bc: Blockchain) {
    enum class QuaSaveMode {
        Read,
        Write
    }

    //    val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(path)))
    val data = if(mode == QuaSaveMode.Read) Files.readAllLines(Paths.get(path)).joinToString("\n") else Yaml.default.encodeToString(Blockchain.serializer(), bc)
    constructor(path: String) : this(path, bc= Blockchain())
    init {
//        println("debug: $this")
    }
    fun export() : String {
        if(mode == QuaSaveMode.Read) throw IllegalStateException("Exporting in read mode")
        return String(gzip(data));
    }
    fun import() : Blockchain {
        if(mode == QuaSaveMode.Write) throw IllegalStateException("Importing in write mode")
        return Yaml.default.decodeFromString(Blockchain.serializer(), ungzip(data.toByteArray()))
    }
    fun write() {
        val bw = BufferedWriter(OutputStreamWriter(FileOutputStream(path)))
        bw.write(export())
        bw.close()
    }
    fun read() : Blockchain {
        return import()
    }

    override fun toString(): String {
        return "QuaSave(path='$path', mode=$mode, data='$data')"
    }

}