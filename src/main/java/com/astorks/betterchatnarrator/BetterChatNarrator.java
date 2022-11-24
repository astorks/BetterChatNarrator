package com.astorks.betterchatnarrator;

import com.mojang.text2speech.Narrator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.yaml.snakeyaml.Yaml;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Environment(EnvType.CLIENT)
public class BetterChatNarrator implements ClientModInitializer {
    public static final Identifier SYNC_MSG_CHANNEL = new Identifier("bcn", "sync");
    public static final Identifier SAY_MSG_CHANNEL = new Identifier("bcn", "say");
    public static final com.astorks.betterchatnarrator.BetterChatNarratorConfig CONFIG = com.astorks.betterchatnarrator.BetterChatNarratorConfig.createAndLoad();
    public static final Logger logger = LogManager.getLogger("BetterChatNarrator");

    public static List<NarratorChannel> serverNarratorChannels = new ArrayList<>();

    private static final Narrator narrator = Narrator.getNarrator();
    private static String lastNarratorMessage;

    public static NarratorChannel messageChannelMatch(String message) {
        for (final var narratorChannel : serverNarratorChannels) {
            final var matchPattern = narratorChannel.getMatchPattern();

            if(matchPattern != null) {
                 if(matchPattern.matcher(message).matches()) {
                    return narratorChannel;
                }
            }
        }

        return null;
    }

    public static void handleNewMessage(String message) {
        final var narratorChannel = messageChannelMatch(message);

        if(narratorChannel != null) {
            final var say = narratorChannel.getMatchPattern().matcher(message).replaceFirst(narratorChannel.say);
            narratorSay(narratorChannel.name, say);
        }
    }

    public static void narratorSay(String channel, String message) {
        if(!CONFIG.narratorEnabled()) return;
        if(CONFIG.disabledChannels().contains(channel)) return;

        lastNarratorMessage = message;
        narrator.say(message, false);
    }

    public static void narratorReplay() {
        if(lastNarratorMessage != null) {
            narrator.say("re-play: " + lastNarratorMessage, true);
        }
    }

    public static void narratorClear() {
        narrator.clear();
    }

    private KeyBinding narratorClearKeybind;
    private KeyBinding narratorReplayKeybind;
    private KeyBinding narratorMenuKeybind;

    @Override
    public void onInitializeClient() {
        logger.info("Narrator: " + narrator.getClass().getName());
        narratorMenuKeybind = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.betterchatnarrator.menu",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_O,
                        "category.betterchatnarrator"
                )
        );
        narratorClearKeybind = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.betterchatnarrator.clear",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.betterchatnarrator"
            )
        );
        narratorReplayKeybind = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.betterchatnarrator.replay",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.betterchatnarrator"
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (narratorMenuKeybind.wasPressed()) {
                if(client.currentScreen != null) {
                    client.setScreen(null);
                } else {
                    client.setScreen(new ConfigScreen());
                }
            }

            while (narratorClearKeybind.wasPressed()) {
                logger.info("KEYPRESS NARRATOR_CLEAR");
                narratorClear();
            }

            while (narratorReplayKeybind.wasPressed()) {
                logger.info("KEYPRESS NARRATOR_REPLAY");
                narratorReplay();
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(SYNC_MSG_CHANNEL, this::onPluginSyncMessage);
        ClientPlayNetworking.registerGlobalReceiver(SAY_MSG_CHANNEL, this::onPluginSayMessage);
    }

    private void onPluginSyncMessage(final MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        try {
            final int readableBytes = buf.readableBytes();
            if (readableBytes > 0) {
                final String stringPayload = buf.toString(0, buf.readableBytes(), StandardCharsets.UTF_8);

                Yaml yaml = new Yaml();
                Map<String, Map<String, Object>> configMap = yaml.load(stringPayload);

                serverNarratorChannels.clear();

                var narratorChannels = new ArrayList<NarratorChannel>();

                for (var channelName : configMap.keySet()) {
                    var narratorChannelData = configMap.get(channelName);
                    var narratorChannel = new NarratorChannel();

                    narratorChannel.name = channelName;

                    if(narratorChannelData.containsKey("match")) {
                        narratorChannel.match = (String)narratorChannelData.get("match");
                    }

                    if(narratorChannelData.containsKey("say")) {
                        narratorChannel.say = (String)narratorChannelData.get("say");
                    }

                    if(narratorChannelData.containsKey("priority")) {
                        narratorChannel.priority = (int)narratorChannelData.get("priority");
                    }

                    narratorChannels.add(narratorChannel);
                }

                narratorChannels.sort(Comparator.comparingInt(s -> s.priority));
                Collections.reverse(narratorChannels);
                serverNarratorChannels = narratorChannels;

                logger.info("[PLUGIN SYNC] Synced " + serverNarratorChannels.size() + " narrator channels.");
            } else {
                logger.warn("Warning, invalid (zero length) payload received from server");
            }
        } catch (final Exception ex) {
            logger.error("Error decoding payload from server", ex);
        }
    }

    private void onPluginSayMessage(final MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        try {
            final int readableBytes = buf.readableBytes();
            if (readableBytes > 0) {
                final var stringPayload = buf.toString(0, buf.readableBytes(), StandardCharsets.UTF_8).split("\\|", 2);
                final var narratorChannel = stringPayload[0];
                final var narratorSay = stringPayload[1];

                logger.info("[PLUGIN SAY] channel: " + narratorChannel + " say:" + narratorSay);
                narratorSay(narratorChannel, narratorSay);
            } else {
                logger.warn("Warning, invalid (zero length) payload received from server");
            }
        } catch (final Exception ex) {
            logger.error("Error decoding payload from server", ex);
        }
    }
}
