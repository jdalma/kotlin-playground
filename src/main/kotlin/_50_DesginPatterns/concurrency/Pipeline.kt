package _50_DesginPatterns.concurrency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

fun main() {
    runBlocking {
        val pagesProducer: ReceiveChannel<String> = producePages()
        val domProducer: ReceiveChannel<Document> = produceDom(pagesProducer)
        val titleProducer: ReceiveChannel<String> = produceTitles(domProducer)
        titleProducer.consumeEach {
            println(it)
        }
    }
}

fun CoroutineScope.producePages() = produce {
    fun getPages(): List<String> {
        return listOf(
            "<html><body><h1>Cool stuff</h1></body></html>",
            "<html><body><h1>Even more stuff</h1></body></html>"
        )
    }

    val pages = getPages()
    var count = 0
    while (this.isActive) {
        for (p in pages) {
            send(p)
        }
        if(count++ == 2) {
            cancel()
        }
        delay(TimeUnit.SECONDS.toMillis(5))
    }
}

fun CoroutineScope.produceDom(pages: ReceiveChannel<String>) = produce {
    fun parseDom(page: String): Document {
        return Document(page)
    }

    for (p in pages) {
        send(parseDom(p))
    }
}

fun CoroutineScope.produceTitles(parsedPages: ReceiveChannel<Document>) = produce {
    fun getTitles(dom: Document): List<String> {
        return dom.getElementsByTagName("h1").map {
            it.toString()
        }
    }

    for (page in parsedPages) {
        for (t in getTitles(page)) {
            send(t)
        }
    }
}

data class Document(val html: String) {
    fun getElementsByTagName(tag: String): List<Document> {
        return listOf(Document("Some title"))
    }
}
