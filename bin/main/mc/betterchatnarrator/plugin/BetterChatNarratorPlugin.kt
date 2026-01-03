package mc.betterchatnarrator.plugin

import co.aikar.commands.PaperCommandManager
import mc.betterchatnarrator.shared.ClientInfo
import mc.betterchatnarrator.shared.VoiceInfo
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRegisterChannelEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.UUID


class BetterChatNarratorPlugin : JavaPlugin(), Listener {
    private lateinit var commandManager: PaperCommandManager
    private var savedChannelConfig: String = ""

    private val netMessageIdentifierInit = "betterchatnarrator:init"
    private val netmsgIdentifierChannels = "betterchatnarrator:channels"
    private val netmsgIdentifierConfig = "betterchatnarrator:config"
    private val netmsgIdentifierSay = "betterchatnarrator:say"
    private val netmsgIdentifierMenu = "betterchatnarrator:menu"

    private val playerVoices = mutableMapOf<String, String>()

    override fun onEnable() {
        server.messenger.registerIncomingPluginChannel(this, netMessageIdentifierInit) { _, player, message ->
            onReceivedNetMessageInit(player, message.decodeToString())
        }

        server.messenger.registerOutgoingPluginChannel(this, netmsgIdentifierChannels)
        server.messenger.registerOutgoingPluginChannel(this, netmsgIdentifierConfig)
        server.messenger.registerOutgoingPluginChannel(this, netmsgIdentifierMenu)
        server.messenger.registerOutgoingPluginChannel(this, netmsgIdentifierSay)

        server.pluginManager.registerEvents(this, this)

        commandManager = PaperCommandManager(this)
        commandManager.registerCommand(NarratorPluginCommand(this))

        reloadServerConfig()
    }

    override fun onDisable() {
        writePlayerVoices()
        saveConfig()

        server.messenger.unregisterOutgoingPluginChannel(this)
        server.messenger.unregisterIncomingPluginChannel(this)
    }

    private fun readPlayerVoices() {
        val playerVoicesFile = File("$dataFolder/${config.getString("playerVoicesFile", "player-voices.yml")}")
        playerVoices.clear()
        if(playerVoicesFile.exists()) {
            val playerVoicesConfig = YamlConfiguration.loadConfiguration(playerVoicesFile)
            val playerIds = playerVoicesConfig.getKeys(false)
            for(playerId in playerIds) {
                playerVoices[playerId] = playerVoicesConfig.getString(playerId, "default") ?: "default"
            }
        }
    }

    private fun writePlayerVoices() {
        val playerVoicesFile = File("$dataFolder/${config.getString("playerVoicesFile", "player-voices.yml")}")
        val playerVoicesConfig = YamlConfiguration()
        for (playerVoice in playerVoices) {
            if(playerVoice.value != "default") {
                playerVoicesConfig.set(playerVoice.key, playerVoice.value)
            }
        }
        playerVoicesConfig.save(playerVoicesFile)
    }

    fun reloadServerConfig() {
        saveDefaultConfig()
        saveConfig()
        reloadConfig()

        readPlayerVoices()

        val channelsConfigFile = File("$dataFolder/${config.getString("narratorChannelsFile", "channels.yml")}")
        if(!channelsConfigFile.exists()) {
            val defaultChannelsConfig = getResource("channels.yml")
            val channelsConfigOutput = channelsConfigFile.outputStream()
            defaultChannelsConfig?.copyTo(channelsConfigOutput)
            defaultChannelsConfig?.close()
            channelsConfigOutput.close()
        }

        savedChannelConfig = channelsConfigFile.readText()
        val channelsConfig = YamlConfiguration.loadConfiguration(channelsConfigFile)

        commandManager.commandCompletions.registerAsyncCompletion("channels") {
            channelsConfig.getKeys(false).toList()
        }

        commandManager.commandCompletions.registerAsyncCompletion("voices") {
            listOf("default") + VoiceInfo.allVoices.map { it.id }
        }

        for (player in server.onlinePlayers) {
            syncServerChannels(player)
        }
    }

    fun setPlayerVoice(uuid: UUID, voice: String?) {
        if(voice == null || voice == "default") {
            playerVoices.remove(uuid.toString())
        } else {
            playerVoices[uuid.toString()] = voice
        }
    }

    fun getPlayerVoice(uuid: UUID): String? {
        return if(playerVoices.containsKey(uuid.toString())) { playerVoices[uuid.toString()] } else { null }
    }

    private fun onReceivedNetMessageInit(player: Player, message: String) {
        val clientInfo = ClientInfo.deserialize(message)
        logger.info("Received client init from ${player.name}, version=${clientInfo.version}")
        player.setMetadata("betterchatnarrator_client_info", FixedMetadataValue(this, clientInfo))
    }

    fun syncServerChannels(player: Player) {
        if(!player.hasMetadata("betterchatnarrator_client_info")) {
            logger.warning("Sync server channels for ${player.name}, using outdated mod.")
        }

        logger.info("syncServerChannels: ${player.name}")
        player.sendPluginMessage(this, netmsgIdentifierChannels, savedChannelConfig.toByteArray())
    }

    fun syncClientConfig(player: Player): Boolean {
        if(!player.hasMetadata("betterchatnarrator_client_info")) {
            logger.warning("Unable to sync client config for ${player.name}, using outdated mod.")
        }

        val clientConfigFile = File("$dataFolder/${config.getString("clientConfigFile", "betterchatnarrator.yml")}")
        val clientConfig = if(clientConfigFile.exists()) clientConfigFile.readText() else null

        if(clientConfig != null) {
            player.sendPluginMessage(this, netmsgIdentifierConfig, clientConfig.toByteArray())
            return true
        }

        return false
    }

    fun openNarratorMenu(player: Player) {
        if(!player.hasMetadata("betterchatnarrator_client_info")) {
            logger.warning("Unable to open menu for ${player.name}, using outdated mod.")
        }

        player.sendPluginMessage(this, netmsgIdentifierMenu, "true".toByteArray())
    }

    fun narratorSay(player: Player, channel: String, message: String, voiceId: String? = null) {
        player.sendPluginMessage(this, netmsgIdentifierSay, "$channel|$voiceId|$message".toByteArray())
    }

    fun narratorSayAll(channel: String, message: String, voiceId: String? = null) {
        for (player in server.onlinePlayers) {
            narratorSay(player, channel, message, voiceId)
        }
    }

    @EventHandler
    fun onPlayerRegisterChannelEvent(event: PlayerRegisterChannelEvent) {
        val player = event.player

        if (event.channel == netmsgIdentifierChannels) {
            syncServerChannels(player)

            if(config.contains("sayAfterChannelSync")) {
                val sayInfoList = config.getMapList("sayAfterChannelSync")
                for (sayInfo in sayInfoList) {
                    val permission = sayInfo["permission"] as String?
                    val message = sayInfo["message"] as String?
                    val channel = sayInfo["channel"] as String?

                    if(message == null || channel == null) continue
                    if (permission != null && !player.hasPermission(permission)) continue

                    val placeholderMessage = PlaceholderAPI.setPlaceholders(player, message)
                    logger.info("Sending '$placeholderMessage' via $channel to ${player.name}")
                    narratorSay(player, channel, placeholderMessage)
                }
            }
        }
    }

//    @EventHandler(priority = EventPriority.LOWEST)
//    fun onPlayerChatEvent(event: AsyncPlayerChatEvent) {
//        if(event.isCancelled) return
//
//        val player = event.player
//        val voice = getPlayerVoice(player.uniqueId)
//        val message = ChatColor.stripColor(event.message)
//        narratorSayAll("Game_Chat", "${player.name} says, $message", voice)
//    }
//
//    @EventHandler(priority = EventPriority.LOWEST)
//    fun onExternalChatReceived(event: ExternalChatReceiveEvent) {
//        if(event.isCancelled) return
//
//        val uuid = event.uuid
//        val playerName = event.player?.name ?: ChatColor.stripColor(event.name)
//        val message = ChatColor.stripColor(event.message)
//        val voice = if(uuid != null) getPlayerVoice(uuid) else null
//
//        narratorSayAll("Game_Chat", "$playerName says, $message", voice)
//    }
}