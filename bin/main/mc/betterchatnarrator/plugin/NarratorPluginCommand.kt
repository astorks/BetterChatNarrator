package mc.betterchatnarrator.plugin

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("narrator")
class NarratorPluginCommand(private val plugin: BetterChatNarratorPlugin) : BaseCommand() {

    @Subcommand("say")
    @CommandCompletion("@players @channels @voices *")
    @Syntax("<target> <channel> <voice> <message>")
    @CommandPermission("bcn.cmd.say")
    fun say(sender: CommandSender, target: OnlinePlayer, channel: String, voice: String, message: String) {
        val playerMessage = PlaceholderAPI.setPlaceholders(target.player, message)
        plugin.narratorSay(target.player, channel, playerMessage, voice)
        sender.sendMessage("[BetterChatNarrator] Sent narrator message to ${target.player.displayName}")
    }

    @Subcommand("sayall")
    @CommandCompletion("@channels @voices *")
    @Syntax("<channel> <voice> <message>")
    @CommandPermission("bcn.cmd.sayall")
    fun sayall(sender: CommandSender, channel: String, voice: String, message: String) {
        for (player in plugin.server.onlinePlayers) {
            val playerMessage = PlaceholderAPI.setPlaceholders(player, message)
            plugin.narratorSay(player, channel, playerMessage, voice)
        }
        sender.sendMessage("[BetterChatNarrator] Sent narrator message to all players.")
    }

    @Subcommand("sync")
    @CommandCompletion("@players")
    @Syntax("<target>")
    @CommandPermission("bcn.cmd.sync")
    fun sync(sender: CommandSender, target: OnlinePlayer) {
        plugin.syncServerChannels(target.player)
        sender.sendMessage("[BetterChatNarrator] Syncing server narrator channels with ${target.player.displayName}")
    }

    @Subcommand("sendconfig")
    @CommandCompletion("@players")
    @Syntax("<target>")
    @CommandPermission("bcn.cmd.sendconfig")
    fun sendconfig(sender: CommandSender, target: OnlinePlayer) {
        if(plugin.syncClientConfig(target.player)) {
            sender.sendMessage("[BetterChatNarrator] Syncing client config with ${target.player.displayName}")
        } else {
            sender.sendMessage("[BetterChatNarrator] Failed to read client config file.")
        }
    }

    @Subcommand("voice")
    @CommandCompletion("@voices")
    @Syntax("<voice>")
    @CommandPermission("bcn.cmd.voice")
    fun voice(sender: Player, voice: String) {
        plugin.setPlayerVoice(sender.uniqueId, voice)
        sender.sendMessage("[BetterChatNarrator] Updated your chat voice to $voice.")
    }

    @Subcommand("playervoice")
    @CommandCompletion("@players @voices")
    @Syntax("<player> <voice>")
    @CommandPermission("bcn.cmd.playervoice")
    fun playerVoice(sender: Player, target: OnlinePlayer, voice: String) {
        plugin.setPlayerVoice(target.player.uniqueId, voice)
        sender.sendMessage("[BetterChatNarrator] Updated ${target.player.name} chat voice to $voice.")
    }

    @Subcommand("menu")
    @CommandCompletion("@players")
    @Syntax("<target>")
    @CommandPermission("bcn.cmd.menu")
    fun menu(sender: CommandSender, target: OnlinePlayer) {
        plugin.openNarratorMenu(target.player)
        sender.sendMessage("[BetterChatNarrator] Opening narrator menu for ${target.player.displayName}")
    }

    @Subcommand("reload")
    @CommandPermission("bcn.cmd.reload")
    fun reload(sender: CommandSender) {
        plugin.reloadServerConfig()
        sender.sendMessage("[BetterChatNarrator] Server config has been reloaded.")
    }
}