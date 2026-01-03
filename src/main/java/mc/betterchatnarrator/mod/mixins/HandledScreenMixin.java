package mc.betterchatnarrator.mod.mixins;

import mc.betterchatnarrator.mod.BetterChatNarrator;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(DrawContext.class)
public class HandledScreenMixin {
    @Inject(method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V", at = @At(value = "HEAD"))
    public void drawTooltip(TextRenderer textRenderer, List<Text> text, Optional<TooltipData> data, int x, int y, CallbackInfo ci) {
        BetterChatNarrator.Companion.setTooltip(text);
    }
}
