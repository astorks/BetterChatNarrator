package com.astorks.betterchatnarrator;

import com.mojang.text2speech.Narrator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class BetterChatNarrator implements ClientModInitializer {
    public static final com.astorks.betterchatnarrator.MainConfig CONFIG = com.astorks.betterchatnarrator.MainConfig.createAndLoad();
    public static final Logger logger = LogManager.getLogger("BetterChatNarrator");

    private static final Narrator narrator = Narrator.getNarrator();
    private static String lastNarratorMessage;

    public static void handleNewMessage(String plainTextMessage) {
        if(CONFIG.chatMessageEnabled()) {
            Matcher matcher = Pattern.compile(CONFIG.chatMessageMatch()).matcher(plainTextMessage);
            if (matcher.matches()) {
                narratorSay(matcher.replaceFirst(CONFIG.chatMessageSay()));
            }
        }

        if(CONFIG.discordMessageEnabled()) {
            Matcher matcher = Pattern.compile(CONFIG.discordMessageMatch()).matcher(plainTextMessage);
            if (matcher.matches()) {
                narratorSay(matcher.replaceFirst(CONFIG.discordMessageSay()));
            }
        }

        if(CONFIG.privateMessageEnabled()) {
            Matcher matcher = Pattern.compile(CONFIG.privateMessageMatch()).matcher(plainTextMessage);
            if (matcher.matches()) {
                narratorSay(matcher.replaceFirst(CONFIG.privateMessageSay()));
            }
        }

        if(CONFIG.systemMessageEnabled()) {
            Matcher matcher = Pattern.compile(CONFIG.systemMessageMatch()).matcher(plainTextMessage);
            if (matcher.matches()) {
                narratorSay(matcher.replaceFirst(CONFIG.systemMessageSay()));
            }
        }
    }

    public static void narratorSay(String message) {
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

    @Override
    public void onInitializeClient() {
        logger.info("Narrator: " + narrator.getClass().getName());
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
            while (narratorClearKeybind.wasPressed()) {
                logger.info("KEYPRESS NARRATOR_CLEAR");
                narratorClear();
            }

            while (narratorReplayKeybind.wasPressed()) {
                logger.info("KEYPRESS NARRATOR_REPLAY");
                narratorReplay();
            }
        });
    }
}
