package antiqueatlaskeybinds.mixin.antiqueatlas;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import hunternif.mc.atlas.client.gui.GuiAtlas;
import hunternif.mc.atlas.client.gui.core.GuiComponent;
import hunternif.mc.atlas.marker.Marker;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(GuiAtlas.class)
public abstract class GuiAtlas_TextSearchMixin extends GuiComponent {

    @Shadow(remap = false) @Final private static int MAP_HEIGHT;
    @Shadow(remap = false) @Final private static int MAP_WIDTH;

    @Unique
    private static final int SEARCH_FIELD_ID = 0;
    @Unique
    private GuiTextField antiqueAtlasKeyBinds$searchBar;

    @Inject(
            method = "initGui",
            at = @At("TAIL")
    )
    private void www(CallbackInfo ci) {
        this.antiqueAtlasKeyBinds$searchBar = new GuiTextField(
                SEARCH_FIELD_ID,
                this.fontRenderer,
                (this.width - MAP_WIDTH) / 2,
                ((this.height - MAP_HEIGHT) / 2) + MAP_HEIGHT,
                MAP_WIDTH,
                this.fontRenderer.FONT_HEIGHT + 5
        );
        this.antiqueAtlasKeyBinds$searchBar.setVisible(true);
        this.antiqueAtlasKeyBinds$searchBar.setTextColor(0xFFFFFF);
    }

    @Inject(
            method = "drawScreen",
            at = @At(value = "INVOKE", target = "Lhunternif/mc/atlas/client/gui/core/GuiComponent;drawScreen(IIF)V")
    )
    private void aaa(CallbackInfo ci) {
        this.antiqueAtlasKeyBinds$searchBar.drawTextBox();
    }

    // Do regex or what JEI does
    @WrapWithCondition(
            method = "drawScreen",
            at = @At(value = "INVOKE", target = "Lhunternif/mc/atlas/client/gui/GuiAtlas;renderMarker(Lhunternif/mc/atlas/marker/Marker;D)V", remap = false)
    )
    private boolean aaam_dontShowDisabledMarkers(GuiAtlas instance, Marker marker, double scale) {
        return marker != null && (antiqueAtlasKeyBinds$searchBar.getText().isEmpty() || marker.getLabel().contains(antiqueAtlasKeyBinds$searchBar.getText()));
    }

    @Inject(
            method = "mouseClicked",
            at = @At("TAIL")
    )
    private void wwww(int mouseX, int mouseY, int mouseState, CallbackInfo ci){
        this.antiqueAtlasKeyBinds$searchBar.mouseClicked(mouseX, mouseY, mouseState);
    }

    @ModifyExpressionValue(
            method = "handleKeyboardInput",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z", remap = false)
    )
    private boolean xxx(boolean original){
        return original && !this.antiqueAtlasKeyBinds$searchBar.isFocused();
    }

    @Unique
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.antiqueAtlasKeyBinds$searchBar.textboxKeyTyped(typedChar, keyCode);
    }
}