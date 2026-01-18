package antiqueatlaskeybinds.mixin.aaam;

import antiqueatlasautomarker.handlers.LibrarianMarkerHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import hunternif.mc.atlas.marker.Marker;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Mixin(LibrarianMarkerHandler.class)
public abstract class LibrarianMarkerHandler_Mixin {

    @Shadow(remap = false) public static KeyBinding librarianKey;

    @Unique
    private static final Set<String> antiqueAtlasKeyBinds$genericDisplayNames = new HashSet<>(Arrays.asList("Librarian§r", "Cartographer§r"));

    @Unique
    private static String antiqueAtlasKeyBinds$markerLabelReplacement = "";

    @ModifyExpressionValue(
            method = "onKeyPressed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isPressed()Z"),
            remap = false
    )
    private static boolean aakb_aaamLibrarianMarkerHandler_onKeyPressedDown(boolean isPressed){
        return isPressed || librarianKey.isKeyDown();
    }

    @Inject(
            method = "onKeyPressed",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/passive/EntityVillager;posX:D")
    )
    private static void aakb_aaamLibrarianMarkerHandler_onKeyPressedGetDisplayName(InputEvent.KeyInputEvent event, CallbackInfo ci, @Local EntityVillager villager){
        antiqueAtlasKeyBinds$markerLabelReplacement = villager.getDisplayName().getFormattedText();
    }

    @ModifyExpressionValue(
            method = "lambda$onKeyPressed$5",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z"),
            remap = false
    )
private static boolean aakb_aaamLibrarianMarkerHandler_onKeyPressedDeleteDisplayName(boolean labelEquals, @Local(argsOnly = true) Marker marker){
        return labelEquals || marker.getLabel().equals(antiqueAtlasKeyBinds$markerLabelReplacement) || antiqueAtlasKeyBinds$genericDisplayNames.contains(marker.getLabel());
    }

    @ModifyExpressionValue(
            method = "onKeyPressed",
            at = @At(value = "FIELD", target = "Lantiqueatlasautomarker/config/folders/EnchantmentConfig;librarianKeyLabel:Ljava/lang/String;"),
            remap = false
    )
    private static String aakb_aaamLibrarianMarkerHandler_onKeyPressedWriteDisplayName(String markerLabel){
        return antiqueAtlasKeyBinds$markerLabelReplacement.isEmpty() ? markerLabel : antiqueAtlasKeyBinds$markerLabelReplacement;
    }
}
