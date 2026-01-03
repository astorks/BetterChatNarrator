package mc.betterchatnarrator.mod

import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer
import com.mojang.text2speech.Narrator
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class AzureSpeechNarrator(apiKey: String, region: String, var primaryVoice: String = "en-US-GuyNeural") : Narrator {
    companion object {
        private var shutdown = false
        private val logger = LogManager.getLogger("AzureNarrator")
    }

    private val speechConfig: SpeechConfig
    private val speechSynthesizer: SpeechSynthesizer
    private val narratorThread: NarratorThread

    private val defaultRate: Double
        get() {
            return Configuration.instance.azureSpeechNarrator.speechRate
        }

    private val defaultVolume: Double
        get() {
            return Configuration.instance.azureSpeechNarrator.speechVolume
        }

    init {
        shutdown = false

        speechConfig = SpeechConfig.fromSubscription(apiKey, region)
        speechSynthesizer = SpeechSynthesizer(speechConfig)

        narratorThread = NarratorThread(speechSynthesizer)

        val thread = Thread(narratorThread)
        thread.name = "AzureNarrator"
        thread.start()
    }

    private fun buildSsmlSpeak(vararg voices: String): String {
        return "<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xml:lang=\"en-US\">\n" +
                voices.joinToString("\n") +
                "</speak>"
    }

    private fun buildSsmlVoice(msg: String, voice: String = primaryVoice, rate: Double = defaultRate, volume: Double = defaultVolume): String {
        return "<voice name=\"${voice}\">\n" +
                "<prosody rate=\"${rate}\" volume=\"${volume.times(100)}\">\n" +
                removeXmlTags(msg) +
                "\n</prosody>\n" +
                "</voice>\n"
    }

    private fun removeXmlTags(xmlString: String): String {
        return xmlString.replace(Regex("<.*?>"), "")
    }

    override fun say(msg: String?, interrupt: Boolean, unknown: Float) {
        if(msg == null) return
        if(interrupt) { clear() }

        try {
            val ssml = buildSsmlSpeak(buildSsmlVoice(msg))
            logger.info(ssml)
            narratorThread.add(Message(ssml))
        } catch (e: Throwable) {
            logger.error(String.format("Narrator crashed : %s", e))
        }
    }

    fun sayVoice(msg: String?, voice: String = primaryVoice, rate: Double = defaultRate, volume: Double = defaultVolume, interrupt: Boolean = false) {
        if(msg == null) return
        if(interrupt) { clear() }

        try {
            val ssml = buildSsmlSpeak(buildSsmlVoice(msg, voice, rate, volume))
            logger.info(ssml)
            narratorThread.add(Message(ssml))
        } catch (e: Throwable) {
            logger.error(String.format("Narrator crashed : %s", e))
        }
    }

    override fun clear() {
        try {
            narratorThread.clear()
        } catch (e: Throwable) {
            logger.error(String.format("Narrator crashed : %s", e))
        }
    }

    override fun active(): Boolean {
        return !shutdown
    }

    override fun destroy() {
        shutdown = true
        narratorThread.clear()
        try {
            narratorThread.join()
        } catch (_: InterruptedException) { }
        speechSynthesizer.close()
    }

    private class NarratorThread(private val speechSynthesizer: SpeechSynthesizer) : Thread() {
        private val messages: Queue<Message> = ConcurrentLinkedQueue<Message>();

        override fun run() {
            while (!shutdown) {
                val msg = messages.poll()

                if(msg != null) {
                    speechSynthesizer.SpeakSsml(msg.text)
                }

                try {
                    sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }

        fun add(msg: Message) {
            messages.add(msg)
        }

        fun clear() {
            messages.clear()
            speechSynthesizer.StopSpeakingAsync().get()
        }
    }

    private data class Message(val text: String)
}