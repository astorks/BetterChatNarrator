package mc.betterchatnarrator.velocity

import co.aikar.commands.VelocityCommandManager
import com.charleskorn.kaml.Yaml
import com.google.inject.Inject
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PluginMessageEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import kotlinx.serialization.decodeFromString
import mc.betterchatnarrator.mod.NarratorChannel
import mc.betterchatnarrator.shared.ClientInfo
import mc.betterchatnarrator.shared.VoiceInfo
import org.slf4j.Logger
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectory
import kotlin.io.path.exists


@Plugin(id = "betterchatnarrator")
class VelocityMain @Inject constructor(
    val logger: Logger,
    val proxyServer: ProxyServer,
    @DataDirectory private val dataDirectory: Path) {
    private val playerInfoMap = mutableMapOf<UUID, ClientInfo>()
    private lateinit var channelsData: String
    private lateinit var channels: Map<String, NarratorChannel>
    private lateinit var commandManager: VelocityCommandManager

    init {
        logger.info("BetterChatNarrator::constructor")
        if(!dataDirectory.exists()) dataDirectory.createDirectory()

        val channelsConfigFile = File("${dataDirectory.toFile()}/channels.yml")
        if(!channelsConfigFile.exists()) {
            val defaultChannelsConfig = getResource("channels.yml")
            val channelsConfigOutput = channelsConfigFile.outputStream()
            defaultChannelsConfig?.copyTo(channelsConfigOutput)
            defaultChannelsConfig?.close()
            channelsConfigOutput.close()
        }
    }

    private fun getResource(filename: String): InputStream? {
        return try {
            val url: URL = ClassLoader.getPlatformClassLoader().getResource(filename) ?: return null
            val connection = url.openConnection()
            connection.useCaches = false
            connection.getInputStream()
        } catch (ex: IOException) {
            null
        }
    }

    fun reloadServerConfig() {
        val channelsConfigFile = File("${dataDirectory.toFile()}/channels.yml")
        channelsData = channelsConfigFile.readText()
        channels = NarratorChannel.read(channelsData)
        logger.info("Loaded ${channels.size} channels.")

        commandManager.commandCompletions.registerAsyncCompletion("channels") {
            channels.keys.toList()
        }

        commandManager.commandCompletions.registerAsyncCompletion("voices") {
            listOf("default") + VoiceInfo.allVoices.map { it.id }
        }

        for (player in proxyServer.allPlayers) {
            syncServerChannels(player)
        }
    }

    fun syncServerChannels(player: Player) {
        logger.info("syncServerChannels: ${player.username}")
        player.sendPluginMessage(
            MinecraftChannelIdentifier.from("betterchatnarrator:channels"),
            channelsData.encodeToByteArray()
        )
    }

    fun openNarratorMenu(player: Player) {
        player.sendPluginMessage(
            MinecraftChannelIdentifier.from("betterchatnarrator:menu"),
            "true".encodeToByteArray()
        )
    }

    fun narratorSay(player: Player, channel: String, message: String, voiceId: String? = null) {
        player.sendPluginMessage(
            MinecraftChannelIdentifier.from("betterchatnarrator:say"),
            "$channel|$voiceId|$message".toByteArray()
        )
    }

    @Subscribe(order = PostOrder.FIRST)
    fun onInit(event: ProxyInitializeEvent) {
        logger.info("BetterChatNarrator::onInit")

        commandManager = VelocityCommandManager(proxyServer, this)
        commandManager.registerCommand(VelocityNarratorCommand(this))

        proxyServer.channelRegistrar.register(MinecraftChannelIdentifier.from("betterchatnarrator:init"))

        reloadServerConfig()
    }

    @Subscribe
    fun onPluginMessage(event: PluginMessageEvent) {
        if(event.identifier.id == "tab:bridge-4") return

        if(event.identifier.id == "betterchatnarrator:init") {
            event.result = PluginMessageEvent.ForwardResult.handled()
            val clientInfo = ClientInfo.deserialize(event.data.decodeToString())
            val player = event.source

            if (player is Player) {
                logger.info("Received client init from ${player.username}, version=${clientInfo.version}")
                playerInfoMap[player.uniqueId] = clientInfo
                syncServerChannels(player)
            }

            return
        }

        event.result = PluginMessageEvent.ForwardResult.forward()
    }
}