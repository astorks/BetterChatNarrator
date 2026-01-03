package mc.betterchatnarrator.mod

import com.mojang.text2speech.Narrator
import mc.betterchatnarrator.mod.ui.MenuScreen
import mc.betterchatnarrator.shared.ClientInfo
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.client.world.ClientWorld
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.lwjgl.glfw.GLFW
import org.yaml.snakeyaml.Yaml
import java.nio.charset.StandardCharsets


class BetterChatNarrator : ClientModInitializer {

    private val netMessageIdentifierInit = Identifier.of("betterchatnarrator", "init")
    private val netmsgIdentifierChannels = Identifier.of("betterchatnarrator", "channels")
    private val netmsgIdentifierConfig = Identifier.of("betterchatnarrator", "config")
    private val netmsgIdentifierMenu = Identifier.of("betterchatnarrator", "menu")
    private val netmsgIdentifierSay = Identifier.of("betterchatnarrator", "say")

    private val keybindCategory = KeyBinding.Category(Identifier.of("betterchatnarrator", "category.betterchatnarrator"))
    private val keybindMenu = KeyBinding("key.betterchatnarrator.menu", InputUtil.Type.SCANCODE, GLFW.GLFW_KEY_O, keybindCategory)
    private val keybindSpeakTooltip = KeyBinding("key.betterchatnarrator.speak_tooltip", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, keybindCategory)
    private val keybindClear = KeyBinding("key.betterchatnarrator.clear", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, keybindCategory)
    private val keybindReplay = KeyBinding("key.betterchatnarrator.replay", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, keybindCategory)

    override fun onInitializeClient() {
        Configuration.read()
        reloadNarrator()

        KeyBindingHelper.registerKeyBinding(keybindMenu)
        KeyBindingHelper.registerKeyBinding(keybindClear)
        KeyBindingHelper.registerKeyBinding(keybindReplay)

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick)
        ClientTickEvents.END_WORLD_TICK.register(this::onEndWorldTick)
        ClientPlayConnectionEvents.JOIN.register { _, _, client -> onClientPlayNetworkingJoin(client) }
        ClientPlayConnectionEvents.DISCONNECT.register { _, client -> onClientPlayNetworkingDisconnect(client) }

        ClientPlayNetworking.registerGlobalReceiver(netmsgIdentifierChannels) { _, _, buf, _ -> onNetMessageChannels(buf) }
        ClientPlayNetworking.registerGlobalReceiver(netmsgIdentifierConfig) { client, _, buf, _ -> onNetMessageConfig(client, buf) }
        ClientPlayNetworking.registerGlobalReceiver(netmsgIdentifierMenu) { client, _, buf, _ -> onNetMessageMenu(client, buf) }
        ClientPlayNetworking.registerGlobalReceiver(netmsgIdentifierSay) { _, _, buf, _ -> onNetMessageSay(buf) }
    }

    private fun onEndWorldTick(world: ClientWorld) {
        if(keybindSpeakTooltip.wasPressed()) {
            narratorSpeakTooltip()
        }
    }

    private fun onEndClientTick(client: MinecraftClient) {
        if (keybindMenu.wasPressed()) {
            if(client.currentScreen == null) {
                client.setScreen(MenuScreen())
            } else {
                client.setScreen(null)
            }
        }

        if(keybindClear.wasPressed()) {
            narratorClear()
        }

        if(keybindReplay.wasPressed()) {
            narratorReplay()
        }
    }

    private fun onClientPlayNetworkingJoin(client: MinecraftClient) {
        val clientInfo = ClientInfo("dev")
        val clientInfoData = ClientInfo.serialize(clientInfo)

        logger.info("Sending client init message to server, $clientInfoData")

        val packetBuf = PacketByteBufs.create()
        packetBuf.writeBytes(clientInfoData.toByteArray())
        ClientPlayNetworking.send(netMessageIdentifierInit, packetBuf)
    }

    private fun onClientPlayNetworkingDisconnect(client: MinecraftClient) {
        narratorChannels = mapOf()
        reloadNarrator()
    }

    private fun onNetMessageChannels(buf: PacketByteBuf) {
        try {
            val readableBytes = buf.readableBytes()
            if (readableBytes > 0) {
                val stringPayload = buf.toString(0, buf.readableBytes(), StandardCharsets.UTF_8)
                narratorChannels = NarratorChannel.read(stringPayload)
                logger.info("[PLUGIN SYNC] Synced ${narratorChannels.size} narrator channels.")
            } else {
                logger.warn("Warning, invalid (zero length) payload received from server")
            }
        }
        catch (ex: Exception) {
            logger.error("Error decoding payload from server", ex)
        }

    }

    private fun onNetMessageConfig(client: MinecraftClient, buf: PacketByteBuf) {
        try {
            val readableBytes = buf.readableBytes()
            if (readableBytes > 0) {
                val stringPayload = buf.toString(0, buf.readableBytes(), StandardCharsets.UTF_8)
                Configuration.readFromString(stringPayload)
                Configuration.write()
                reloadNarrator()

                client.execute {
                    if(client.currentScreen is MenuScreen) {
                        client.setScreen(MenuScreen())
                    }
                }
            } else {
                logger.warn("Warning, invalid (zero length) payload received from server")
            }
        } catch (ex: Exception) {
            logger.error("Error decoding payload from server", ex)
        }
    }

    private fun onNetMessageSay(buf: PacketByteBuf) {
        try {
            val readableBytes = buf.readableBytes()
            if (readableBytes > 0) {
                val stringPayload = buf.toString(0, buf.readableBytes(), StandardCharsets.UTF_8).split("\\|".toRegex(), limit = 3).toTypedArray()
                val narratorChannel = stringPayload[0]
                var voiceId: String? = stringPayload[1]
                val narratorSay = stringPayload[2]

                if(!Configuration.instance.azureSpeechNarrator.allowPlayerVoices) voiceId = null
                if(voiceId.isNullOrEmpty() || voiceId == "default" || voiceId == "null") voiceId = null

                logger.info("[PLUGIN SAY] channel: $narratorChannel say: $narratorSay, voice: $voiceId")
                narratorSayVoice(narratorChannel, narratorSay, voiceId)
            } else {
                logger.warn("Warning, invalid (zero length) payload received from server")
            }
        } catch (ex: Exception) {
            logger.error("Error decoding payload from server", ex)
        }
    }

    private fun onNetMessageMenu(client: MinecraftClient, buf: PacketByteBuf) {
        try {
            client.execute {
                client.setScreen(MenuScreen())
            }
        } catch (ex: Exception) {
            logger.error("Error decoding payload from server", ex)
        }
    }

    companion object {
        var narrator: Narrator? = null
        private var lastNarratorMessage: String? = null
        private val logger = LogManager.getLogger("BetterChatNarrator")

        private var tooltipMessage: String? = null
        private var tooltipHash: Int = 0

        var narratorChannels = mapOf<String, NarratorChannel>()

        private fun messageChannelMatch(message: String): NarratorChannel? {
            for (narratorChannel in narratorChannels.values) {
                val matchPattern = narratorChannel.matchPattern
                if (matchPattern != null) {
                    if (matchPattern.matcher(message).matches()) {
                        return narratorChannel
                    }
                }
            }
            return null
        }

        fun reloadNarrator() {
            if(narrator is AzureSpeechNarrator) {
                narrator?.destroy()
            }

            narrator = try {
                when (Configuration.instance.narratorType) {
                    Configuration.NarratorType.BASIC_GAME_NARRATOR -> Narrator.getNarrator()
                    Configuration.NarratorType.AZURE_SPEECH_NARRATOR -> AzureSpeechNarrator(
                        Configuration.instance.azureSpeechNarrator.apiKey ?: "",
                        Configuration.instance.azureSpeechNarrator.region ?: "westus",
                        Configuration.instance.azureSpeechNarrator.voice
                    )
                }
            } catch (ex: Exception) {
                logger.fatal(ex)
                Configuration.instance.narratorType = Configuration.NarratorType.BASIC_GAME_NARRATOR
                Narrator.getNarrator()
            }

        }

        fun updatePrimaryVoice(primaryVoice: String) {
            val narrator = narrator
            if(narrator is AzureSpeechNarrator) {
                narrator.primaryVoice = primaryVoice
            }
        }

        fun testNarrator(say: String, voice: String) {
            val narrator = narrator
            if(narrator is AzureSpeechNarrator) {
                narrator.sayVoice(say, voice, interrupt = true)
            } else {
                narrator?.say(say, true, 1.0f)
            }
        }

        fun testNarrator(say: String) {
            narrator?.say(say, true, 1.0f)
        }

        fun setTooltip(text: List<Text>) {
            if(tooltipHash != text.hashCode()) {
                tooltipMessage = text.joinToString("\n") { it.withoutStyle().joinToString("") { it2 -> it2.string } }
                tooltipHash = text.hashCode()
                logger.info(tooltipMessage)
            }
        }

        fun addNewChatMessage(message: Text) {
            val stringMessage = message.withoutStyle().joinToString("") { it.string }
            logger.info(stringMessage)
            val narratorChannel = messageChannelMatch(stringMessage)
            if (narratorChannel?.say != null) {
                val matchPattern = narratorChannel.matchPattern
                if(matchPattern != null) {
                    val say = matchPattern.matcher(stringMessage).replaceFirst(narratorChannel.say)
                    narratorSay(narratorChannel.name, say)
                }
            }
        }

        fun narratorSayVoice(channel: String, message: String, voiceId: String?) {
            if (!Configuration.instance.enabled) return
            if (Configuration.instance.disabledNarratorChannels.contains(channel)) return

            lastNarratorMessage = message

            val narrator = narrator
            if(narrator is AzureSpeechNarrator) {
                narrator.sayVoice(message, voiceId ?: narrator.primaryVoice, interrupt = false)
            } else {
                narrator?.say(message, false, 1.0f)
            }

        }

        fun narratorSay(channel: String, message: String) {
            if (!Configuration.instance.enabled) return
            if (Configuration.instance.disabledNarratorChannels.contains(channel)) return

            lastNarratorMessage = message
            narrator?.say(message, false, 1.0f)
        }

        fun narratorReplay() {
            if (!Configuration.instance.enabled) return

            if (lastNarratorMessage != null) {
                narrator?.say("re-play: $lastNarratorMessage", true, 1.0f)
            }
        }

        fun narratorSpeakTooltip() {
            if (!Configuration.instance.enabled) return

            if (tooltipMessage != null) {
                narrator?.say(tooltipMessage, true, 1.0f)
            }
        }

        fun narratorClear() {
            narrator?.clear()
        }
    }
}