package mc.betterchatnarrator.mod

import kotlinx.serialization.Serializable
import net.fabricmc.loader.api.FabricLoader
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Representer
import java.nio.file.Files


class Configuration {
    @Serializable
    data class RootConfig(
        var enabled: Boolean = true,
        var disabledNarratorChannels: MutableList<String> = mutableListOf(),
        var azureSpeechNarrator: AzureSpeechNarratorConfig = AzureSpeechNarratorConfig(),
        var narratorType: NarratorType = NarratorType.BASIC_GAME_NARRATOR
    )

    @Serializable
    data class AzureSpeechNarratorConfig(
        var apiKey: String? = null,
        var region: String? = null,
        var voice: String = "en-US-GuyNeural",
        var allowPlayerVoices: Boolean = true,
        var speechRate: Double = 1.0,
        var speechVolume: Double = 1.0
    )

    enum class NarratorType {
        BASIC_GAME_NARRATOR,
        AZURE_SPEECH_NARRATOR
    }

    companion object {
        var instance = RootConfig()
            private set

        private val yaml: Yaml
        private val configFilePath = FabricLoader.getInstance().configDir.resolve("betterchatnarrator.yml")

        init {
            val yamlDumperOptions = DumperOptions()
            yamlDumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            val yamlLoaderOptions = LoaderOptions()

            val constructor = Constructor(RootConfig::class.java, yamlLoaderOptions)
            val rep = Representer(yamlDumperOptions)
            rep.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            rep.addClassTag(RootConfig::class.java, Tag.MAP)

            yaml = Yaml(constructor, rep, yamlDumperOptions, yamlLoaderOptions)
        }

        fun read() {
            try {
                if (!Files.exists(configFilePath)) {
                    write()
                }

                Files.newBufferedReader(configFilePath).use {
                    instance = yaml.load(it)
                }
            } catch (_: Exception) { }
        }

        fun readFromString(configText: String) {
            try {
                instance = yaml.load(configText)
            } catch (_: Exception) { }
        }

        fun write() {
            try {
                val configYaml = yaml.dump(instance)
                Files.writeString(configFilePath, configYaml)
            } catch (_: Exception) { }
        }

        fun writeToString(): String {
            try {
                return yaml.dump(instance)
            } catch (_: Exception) { }

            return ""
        }
    }
}