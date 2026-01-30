package antiqueatlaskeybinds.mixin.antiqueatlas;

import antiqueatlaskeybinds.AntiqueAtlasKeyBinds;
import antiqueatlaskeybinds.client.KeyHandler;
import antiqueatlaskeybinds.network.PacketExportPutMarker;
import antiqueatlaskeybinds.network.PacketHandler;
import antiqueatlaskeybinds.util.IOHelper;
import com.llamalad7.mixinextras.sugar.Local;
import hunternif.mc.atlas.SettingsConfig;
import hunternif.mc.atlas.client.Textures;
import hunternif.mc.atlas.client.gui.GuiAtlas;
import hunternif.mc.atlas.client.gui.GuiBookmarkButton;
import hunternif.mc.atlas.client.gui.GuiPositionButton;
import hunternif.mc.atlas.client.gui.core.GuiComponent;
import hunternif.mc.atlas.client.gui.core.GuiComponentButton;
import hunternif.mc.atlas.client.gui.core.GuiCursor;
import hunternif.mc.atlas.client.gui.core.GuiStates;
import hunternif.mc.atlas.marker.Marker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import org.apache.commons.io.FileUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Mixin(GuiAtlas.class)
public abstract class GuiAtlas_KeybindMixin extends GuiComponent {

    @Shadow(remap = false) private ItemStack stack;
    @Shadow(remap = false) @Final private GuiBookmarkButton btnMarker;
    @Shadow(remap = false) @Final private GuiBookmarkButton btnDelMarker;
    @Shadow(remap = false) @Final private GuiBookmarkButton btnShowMarkers;
    @Shadow(remap = false) @Final private GuiPositionButton btnPosition;
    @Shadow(remap = false) private GuiComponentButton selectedButton;

    @Shadow(remap = false) @Final private GuiStates state;
    @Shadow(remap = false) @Final private GuiStates.IState NORMAL;

    @Shadow (remap = false) @Mutable @Final private GuiStates.IState DELETING_MARKER;

    @Shadow(remap = false) @Final private GuiCursor eraser;
    @Shadow(remap = false) private Marker hoveredMarker;
    @Shadow(remap = false) private EntityPlayer player;

    @Shadow(remap = false) protected abstract int getAtlasID();

    @Unique
    private final GuiCursor antiqueAtlasKeyBinds$exportMarkerCursor = new GuiCursor();

    @Unique
    private final GuiBookmarkButton antiqueAtlasKeyBinds$btnExportMarkerData = GuiBookmarkButton_Invoker.invokeInit(1, Textures.MARKER_SCROLL, I18n.format("gui.antiqueatlas.exportmarkerdata"));
    @Unique
    private final GuiStates.IState EXPORT_MARKER_DATA = new GuiStates.IState() {
        @Override
        public void onEnterState() {
            addChild(antiqueAtlasKeyBinds$exportMarkerCursor);
            antiqueAtlasKeyBinds$btnExportMarkerData.setSelected(true);
        }
        @Override
        public void onExitState() {
            removeChild(antiqueAtlasKeyBinds$exportMarkerCursor);
            antiqueAtlasKeyBinds$btnExportMarkerData.setSelected(false);
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

        this.addChild(antiqueAtlasKeyBinds$btnExportMarkerData).offsetGuiCoords(300, -5);
        this.antiqueAtlasKeyBinds$btnExportMarkerData.addListener(button -> {
            if (this.stack != null || !SettingsConfig.gameplay.itemNeeded) {
                if (this.state.is(EXPORT_MARKER_DATA)) {
                    this.selectedButton = null;
                    this.state.switchTo(NORMAL);
                } else {
                    this.selectedButton = button;
                    this.state.switchTo(EXPORT_MARKER_DATA);
                }
            }
        });

        this.antiqueAtlasKeyBinds$exportMarkerCursor.setTexture(Textures.BOOK, 12, 14, 2, 11);
    }

    @Inject(
            method = "mouseClicked",
            at = @At(value = "INVOKE", target = "Lhunternif/mc/atlas/client/gui/core/GuiStates;is(Lhunternif/mc/atlas/client/gui/core/GuiStates$IState;)Z", ordinal = 4, remap = false)
    )
    private void aakb_antiqueAtlasGuiAtlas_mouseClickedCopyData(int mouseX, int mouseY, int mouseState, CallbackInfo ci, @Local boolean isMouseOverMap, @Local(name = "atlasID") int atlasID){
        if (this.state.is(EXPORT_MARKER_DATA) && isMouseOverMap && mouseState == 0 && this.player.getEntityWorld().isRemote){ // If clicked on a marker, export it:
            antiqueAtlasKeyBinds$doExportMarker(this.hoveredMarker, this.player);
        }
    }

    @Inject(
            method = "handleKeyboardInput",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;getKeyCode()I", ordinal = 0)
    )
    public void aakb_antiqueAtlasGuiAtlas_handleKeyboardInputClicksButton(CallbackInfo ci, @Local int key) {
        if(KeyHandler.addButtonKey.isActiveAndMatches(key)){
            ((GuiComponentButton_Invoker)this.btnMarker).invokeOnClick();
        }
        else if(KeyHandler.deleteButtonKey.isActiveAndMatches(key)){
            ((GuiComponentButton_Invoker)this.btnDelMarker).invokeOnClick();
        }
        else if(KeyHandler.toggleButtonKey.isActiveAndMatches(key)){
            ((GuiComponentButton_Invoker)this.btnShowMarkers).invokeOnClick();
        }
        else if(KeyHandler.toggleFollowPlayer.isActiveAndMatches(key)){
            ((GuiComponentButton_Invoker)this.btnPosition).invokeOnClick();
        }
        else if(KeyHandler.exportMarkerKey.isActiveAndMatches(key)){
            ((GuiComponentButton_Invoker)this.antiqueAtlasKeyBinds$btnExportMarkerData).invokeOnClick();
        }
    }

    @Unique
    private void antiqueAtlasKeyBinds$doExportMarker(Marker selectedMarker, EntityPlayer atlasPlayer){
        if(selectedMarker != null && !selectedMarker.isGlobal()){
            String labelForMessage = selectedMarker.getLabel().isEmpty()
                    ? I18n.format("gui.aakb.exportmarkerdata.clipboardlabel")
                    : "[" + I18n.format(selectedMarker.getLabel()) + "]";
            StringBuilder command = new StringBuilder("/aaam putmarker");
            command.append(" ").append(selectedMarker.getX())
                    .append(" ").append(selectedMarker.getZ())
                    .append(" ").append(selectedMarker.getType());
            if(selectedMarker.getLabel().isEmpty()){
                command.append(" ").append("_");
            }
            else {
                command.append(" ").append(selectedMarker.getLabel());
            }

            if(GuiScreen.isShiftKeyDown()){
                File exportFolder = new File(Minecraft.getMinecraft().gameDir + IOHelper.MARKER_EXPORT_DIRECTORY);
                if (!exportFolder.isDirectory()) {
                    exportFolder.mkdir();
                }

                String serverIdentifier = null;

                if(this.mc.isSingleplayer()){
                    serverIdentifier = this.mc.getIntegratedServer().getFolderName();
                }
                else if(this.mc.getConnection() != null){
                    serverIdentifier = IOHelper.simplifyFileName(this.mc.getConnection().getNetworkManager().getRemoteAddress().toString());
                }

                File exportMarkerFile = new File(exportFolder,
                        atlasPlayer.getName()
                                + "." + serverIdentifier
                                + "." + atlasPlayer.dimension
                                + IOHelper.MARKER_EXPORT_FILE_EXTENSION
                                + ".txt"
                );
                if (!exportMarkerFile.isFile()){
                    try {
                        exportMarkerFile.createNewFile();
                    } catch (IOException e) {
                        AntiqueAtlasKeyBinds.LOGGER.error(e);
                    }
                }
                if (exportMarkerFile.isFile() && exportMarkerFile.canWrite()) {
                    try {
                        FileUtils.writeStringToFile(exportMarkerFile, command + System.lineSeparator(), StandardCharsets.UTF_8, true);
                        ITextComponent clickableLink = new TextComponentString(IOHelper.MARKER_EXPORT_DIRECTORY + "/" + exportMarkerFile.getName());
                        clickableLink.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, exportMarkerFile.getAbsolutePath()));
                        clickableLink.getStyle().setUnderlined(true);
                        atlasPlayer.sendMessage(new TextComponentTranslation("gui.aakb.exportmarkerdata.exportfile", labelForMessage, clickableLink));
                    } catch (IOException e) {
                        AntiqueAtlasKeyBinds.LOGGER.error(e);
                    }
                }
            }
            else if(GuiScreen.isCtrlKeyDown()){
                PacketHandler.instance.sendToServer(new PacketExportPutMarker(
                        atlasPlayer.getName(),
                        this.getAtlasID(),
                        selectedMarker.getX(),
                        selectedMarker.getZ(),
                        selectedMarker.getType(),
                        selectedMarker.getLabel().isEmpty() ? "_" : selectedMarker.getLabel()
                ));
            }
            else {
                GuiScreen.setClipboardString(command.toString());
                atlasPlayer.sendMessage(new TextComponentTranslation("gui.aakb.exportmarkerdata.clipboard", labelForMessage));
            }
        }
        else {
            atlasPlayer.sendMessage(new TextComponentTranslation("gui.aakb.exportmarkerdata.nomarker"));
        }
    }

}
