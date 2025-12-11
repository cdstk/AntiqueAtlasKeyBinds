package antiqueatlaskeybinds.mixin.antiqueatlas;

import antiqueatlaskeybinds.client.KeyHandler;
import antiqueatlaskeybinds.client.gui.LolACopyOfGuiBookmarkButton;
import com.llamalad7.mixinextras.sugar.Local;
import hunternif.mc.atlas.SettingsConfig;
import hunternif.mc.atlas.client.Textures;
import hunternif.mc.atlas.client.gui.GuiAtlas;
import hunternif.mc.atlas.client.gui.GuiBookmarkButton;
import hunternif.mc.atlas.client.gui.core.GuiComponent;
import hunternif.mc.atlas.client.gui.core.GuiComponentButton;
import hunternif.mc.atlas.client.gui.core.GuiCursor;
import hunternif.mc.atlas.client.gui.core.GuiStates;
import hunternif.mc.atlas.marker.Marker;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiAtlas.class)
public class GuiAtlas_KeybindMixin extends GuiComponent {

    @Shadow(remap = false) private ItemStack stack;
    @Shadow(remap = false) @Final private GuiBookmarkButton btnMarker;
    @Shadow(remap = false) @Final private GuiBookmarkButton btnDelMarker;
    @Shadow(remap = false) @Final private GuiBookmarkButton btnShowMarkers;
    @Shadow(remap = false) @Final private GuiCursor eraser;

    @Shadow (remap = false) @Mutable
    @Final private GuiStates.IState DELETING_MARKER;

    @Shadow(remap = false) @Final private GuiStates state;
    @Shadow(remap = false) @Final private GuiStates.IState NORMAL;
    @Shadow(remap = false) private GuiComponentButton selectedButton;
    @Shadow(remap = false) private Marker hoveredMarker;
    @Shadow(remap = false) private EntityPlayer player;
    @Unique
    private final GuiCursor antiqueAtlasKeyBinds$copyData = new GuiCursor();

    @Unique
//    private final LolACopyOfGuiBookmarkButton antiqueAtlasKeyBinds$btnCopyMarkerData = new LolACopyOfGuiBookmarkButton(1, Textures.ICON_EXPORT, I18n.format("gui.antiqueatlas.exportImage"));
    private final LolACopyOfGuiBookmarkButton antiqueAtlasKeyBinds$btnCopyMarkerData = new LolACopyOfGuiBookmarkButton(1, Textures.ICON_EXPORT, "Put Marker Clipboard");
    @Unique
    private final GuiStates.IState COPY_MARKER_DATA = new GuiStates.IState() {
        @Override
        public void onEnterState() {
            addChild(antiqueAtlasKeyBinds$copyData);
            antiqueAtlasKeyBinds$btnCopyMarkerData.setSelected(true);
        }
        @Override
        public void onExitState() {
            removeChild(antiqueAtlasKeyBinds$copyData);
            antiqueAtlasKeyBinds$btnCopyMarkerData.setSelected(false);
        }
    };

    @Inject(
            method = "<init>",
            at = @At("TAIL"),
            remap = false
    )
    public void aakb_antiqueAtlasGuiAtlas_initCancelDeleteReset(CallbackInfo ci){
        // I don't know why it originally needed to grab the mouse cursor, but the behavior is awkward
        DELETING_MARKER = new GuiStates.IState() {
            @Override
            public void onEnterState() {
                addChild(eraser);
                btnDelMarker.setSelected(true);
            }
            @Override
            public void onExitState() {
                removeChild(eraser);
                btnDelMarker.setSelected(false);
            }
        };

        this.addChild(antiqueAtlasKeyBinds$btnCopyMarkerData).offsetGuiCoords(300, -5);
        this.antiqueAtlasKeyBinds$btnCopyMarkerData.addListener(button -> {
            if (this.stack != null || !SettingsConfig.gameplay.itemNeeded) {
                if (this.state.is(COPY_MARKER_DATA)) {
                    this.selectedButton = null;
                    this.state.switchTo(NORMAL);
                } else {
                    this.selectedButton = button;
                    this.state.switchTo(COPY_MARKER_DATA);
                }
            }
        });

        this.antiqueAtlasKeyBinds$copyData.setTexture(Textures.BOOK, 12, 14, 2, 11);
    }

    @Inject(
            method = "mouseClicked",
            at = @At(value = "INVOKE", target = "Lhunternif/mc/atlas/client/gui/core/GuiStates;is(Lhunternif/mc/atlas/client/gui/core/GuiStates$IState;)Z", ordinal = 4, remap = false)
    )
    private void aakb_antiqueAtlasGuiAtlas_mouseClickedCopyData(int mouseX, int mouseY, int mouseState, CallbackInfo ci, @Local boolean isMouseOverMap, @Local(name = "atlasID") int atlasID){
        if (this.state.is(COPY_MARKER_DATA) && isMouseOverMap && mouseState == 0 && this.player.getEntityWorld().isRemote){ // If clicked on a marker, delete it:
            if(this.hoveredMarker != null && !hoveredMarker.isGlobal()){
                String putMarker = "/aaam putmarker " + hoveredMarker.getX() + " " + hoveredMarker.getZ() + " " + hoveredMarker.getType() + " " + hoveredMarker.getLabel();
                GuiScreen.setClipboardString(putMarker);
                this.player.sendMessage(new TextComponentTranslation(hoveredMarker.getLabel() + " copied to Clipboard"));
            }
            else {
                this.player.sendMessage(new TextComponentTranslation("No marker selected"));
            }
        }
    }

    @Inject(
            method = "handleKeyboardInput",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;getKeyCode()I", ordinal = 0)
    )
    public void aakb_antiqueAtlasGuiAtlas_handleKeyboardInputClicksButton(CallbackInfo ci, @Local int key) {
        if(KeyHandler.addButtonKey.isActiveAndMatches(key)){
            ((GuiComponentButtonInvoker)this.btnMarker).invokeOnClick();
        }
        else if(KeyHandler.deleteButtonKey.isActiveAndMatches(key)){
            ((GuiComponentButtonInvoker)this.btnDelMarker).invokeOnClick();
        }
        else if(KeyHandler.toggleButtonKey.isActiveAndMatches(key)){
            ((GuiComponentButtonInvoker)this.btnShowMarkers).invokeOnClick();
        }
        else if(KeyHandler.copyDataKey.isActiveAndMatches(key)){
            ((GuiComponentButtonInvoker)this.antiqueAtlasKeyBinds$btnCopyMarkerData).invokeOnClick();
        }
    }
}
