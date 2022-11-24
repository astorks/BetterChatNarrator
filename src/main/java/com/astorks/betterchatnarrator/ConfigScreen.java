package com.astorks.betterchatnarrator;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ConfigScreen extends BaseOwoScreen<FlowLayout> {
    private final Screen previousScreen;

    public ConfigScreen() {
        previousScreen = null;
    }

    public ConfigScreen(Screen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    public void close() {
        if(client != null) {
            client.setScreen(previousScreen);
        }
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        FlowLayout mainContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());

        final var disabledChannels = BetterChatNarrator.CONFIG.disabledChannels();
        for (var narratorChannel : BetterChatNarrator.serverNarratorChannels) {
            if(disabledChannels.contains(narratorChannel.name)) {
                mainContainer = mainContainer.child(Components.button(Text.literal("§c§m" + narratorChannel.name.replace('_', ' ')), (ButtonComponent button) -> {
                    disabledChannels.remove(narratorChannel.name);
                    BetterChatNarrator.CONFIG.disabledChannels(disabledChannels);
                    BetterChatNarrator.CONFIG.save();
                    if(client != null) {
                        client.setScreen(new ConfigScreen(previousScreen));
                    }
                }).margins(Insets.bottom(2)));
            } else {
                mainContainer = mainContainer.child(Components.button(Text.literal("§a" + narratorChannel.name.replace('_', ' ')), (ButtonComponent button) -> {
                    disabledChannels.add(narratorChannel.name);
                    BetterChatNarrator.CONFIG.disabledChannels(disabledChannels);
                    BetterChatNarrator.CONFIG.save();
                    if(client != null) {
                        client.setScreen(new ConfigScreen(previousScreen));
                    }
                }).margins(Insets.bottom(2)));
            }
        }


        rootComponent.child(
            Containers
                .verticalFlow(Sizing.fixed(300), Sizing.fill(90))
                .child(
                    Components.button(Text.literal(BetterChatNarrator.CONFIG.narratorEnabled() ? "§aBetter Chat Narrator Enabled" : "§cBetter Chat Narrator Disabled"), (ButtonComponent button) -> {
                        BetterChatNarrator.CONFIG.narratorEnabled(!BetterChatNarrator.CONFIG.narratorEnabled());
                        BetterChatNarrator.CONFIG.save();
                        if(client != null) {
                            client.setScreen(new ConfigScreen(previousScreen));
                        }
                    }).margins(Insets.bottom(10))
                )
                .child(Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), mainContainer))
                .child(
                    Components.button(Text.literal("Close"), (ButtonComponent button) -> {
                        close();
                    }).margins(Insets.top(10))
                )
                .padding(Insets.of(40, 40, 16,16))
                .surface(Surface.DARK_PANEL)
                .allowOverflow(true)
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER)
        );
    }
}
