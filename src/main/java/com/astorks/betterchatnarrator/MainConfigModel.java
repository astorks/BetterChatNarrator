package com.astorks.betterchatnarrator;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.SectionHeader;

@Modmenu(modId = "betterchatnarrator")
@Config(name = "main-config", wrapperName = "MainConfig")
public class MainConfigModel {
    @SectionHeader("enable")
    public boolean chatMessageEnabled = true;
    public boolean discordMessageEnabled = true;
    public boolean privateMessageEnabled = true;
    public boolean systemMessageEnabled = true;

    @SectionHeader("say")
    public String chatMessageSay = "$1 says, $2";
    public String discordMessageSay = "discord $1 says, $2";
    public String privateMessageSay = "$1 sent $2 a message, $3";
    public String systemMessageSay = "$1";

    @SectionHeader("advanced")
    public String chatMessageMatch = "\\[C\\](.*?)\\: (.*?)";
    public String discordMessageMatch = "\\[D\\](.*?)\\: (.*?)";
    public String privateMessageMatch = "\\[M\\]\\[(.*?) \\-\\> (.*?)\\] (.*?)";
    public String systemMessageMatch = "\\[S\\](.*?)";
}
