package mc.betterchatnarrator.velocity

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.velocity.contexts.OnlinePlayer
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component

@CommandAlias("narrator")
class VelocityNarratorCommand(private val main: VelocityMain) : BaseCommand() {

    @Subcommand("say")
    @CommandCompletion("@players @channels @voices *")
    @Syntax("<target> <channel> <voice> <message>")
    @CommandPermission("bcn.cmd.say")
    fun say(source: CommandSource, target: OnlinePlayer, channel: String, voice: String, message: String) {
        main.narratorSay(target.player, channel, message, voice)
        source.sendMessage(Component.text().content("[BetterChatNarrator] Sent narrator message to ${target.player.username}."))
    }

    @Subcommand("sayall")
    @CommandCompletion("@channels @voices *")
    @Syntax("<channel> <voice> <message>")
    @CommandPermission("bcn.cmd.sayall")
    fun sayall(source: CommandSource, channel: String, voice: String, message: String) {
        for (player in main.proxyServer.allPlayers) {
            main.narratorSay(player, channel, message, voice)
        }
        source.sendMessage(Component.text().content("[BetterChatNarrator] Sent narrator message to all players."))
    }

    @Subcommand("sync")
    @CommandCompletion("@players")
    @Syntax("<target>")
    @CommandPermission("bcn.cmd.sync")
    fun sync(source: CommandSource, target: OnlinePlayer) {
        main.syncServerChannels(target.player)
        source.sendMessage(Component.text().content("[BetterChatNarrator] Syncing server narrator channels with ${target.player.username}"))
    }

    @Subcommand("menu")
    @CommandCompletion("@players")
    @Syntax("<target>")
    @CommandPermission("bcn.cmd.menu")
    fun menu(source: CommandSource, target: OnlinePlayer) {
        main.openNarratorMenu(target.player)
        source.sendMessage(Component.text().content("[BetterChatNarrator] Opening narrator menu for ${target.player.username}"))
    }

    @Subcommand("reload")
    @CommandPermission("bcn.cmd.reload")
    fun reload(source: CommandSource) {
        main.reloadServerConfig()
        source.sendMessage(Component.text().content("[BetterChatNarrator] Server config has been reloaded."))
    }
}