package mc.betterchatnarrator.mod

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.io.File
import java.util.regex.Pattern

@Serializable
data class NarratorChannel(
        val name: String,
        val priority: Int = 0,
        val match: String? = null,
        val say: String? = null
) {
    @Contextual
    private var compiledPattern: Pattern? = null
    val matchPattern: Pattern?
        get() {
            if (match != null && compiledPattern == null) {
                compiledPattern = Pattern.compile(match)
            }

            return compiledPattern
        }

    companion object {
        fun read(yaml: String): Map<String, NarratorChannel> {
            return Yaml.default.decodeFromString<Map<String, NarratorChannel>>(yaml)
        }

        fun read(file: File): Map<String, NarratorChannel> {
            return read(file.readText())
        }
    }
}