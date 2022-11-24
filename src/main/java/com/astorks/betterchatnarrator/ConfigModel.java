package com.astorks.betterchatnarrator;

import io.wispforest.owo.config.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@Config(name = "assets/betterchatnarrator", wrapperName = "BetterChatNarratorConfig")
public class ConfigModel {
    public boolean narratorEnabled = true;
    public List<String> disabledChannels = new ArrayList<>();
}
