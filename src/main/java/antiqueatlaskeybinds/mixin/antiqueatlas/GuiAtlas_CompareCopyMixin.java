package antiqueatlaskeybinds.mixin.antiqueatlas;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import hunternif.mc.atlas.AntiqueAtlasMod;
import hunternif.mc.atlas.RegistrarAntiqueAtlas;
import hunternif.mc.atlas.SettingsConfig;
import hunternif.mc.atlas.api.AtlasAPI;
import hunternif.mc.atlas.client.Textures;
import hunternif.mc.atlas.client.gui.GuiAtlas;
import hunternif.mc.atlas.client.gui.GuiBookmarkButton;
import hunternif.mc.atlas.client.gui.core.GuiComponent;
import hunternif.mc.atlas.client.gui.core.GuiComponentButton;
import hunternif.mc.atlas.client.gui.core.GuiCursor;
import hunternif.mc.atlas.client.gui.core.GuiStates;
import hunternif.mc.atlas.marker.DimensionMarkersData;
import hunternif.mc.atlas.marker.Marker;
import hunternif.mc.atlas.marker.MarkersData;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(GuiAtlas.class)
public abstract class GuiAtlas_CompareCopyMixin extends GuiComponent {

    @Shadow(remap = false) private ItemStack stack;
    @Shadow(remap = false) private GuiComponentButton selectedButton;

    @Shadow(remap = false) @Final private GuiStates state;
    @Shadow(remap = false) @Final private GuiStates.IState NORMAL;

    @Shadow(remap = false) private Marker hoveredMarker;
    @Shadow(remap = false) private EntityPlayer player;

    @Shadow(remap = false) protected abstract int getAtlasID();

    @Unique
    private ItemStack antiqueAtlasKeyBinds$otherStack;
    @Unique
    private final Set<Marker> antiqueAtlasKeyBinds$uniqueMarkers = new HashSet<>();
    @Unique
    private final GuiCursor antiqueAtlasKeyBinds$compareMarkerCursor = new GuiCursor();
    @Unique
    private final GuiBookmarkButton antiqueAtlasKeyBinds$btnCompareMarker = GuiBookmarkButton_Invoker.invokeInit(1, Textures.MARKER_SCROLL, I18n.format("gui.antiqueatlas.comparemarker"));
    @Unique
    private final GuiStates.IState COMPARE_ATLAS_MARKERS = new GuiStates.IState() {
        @Override
        public void onEnterState() {
            addChild(antiqueAtlasKeyBinds$compareMarkerCursor);
            antiqueAtlasKeyBinds$btnCompareMarker.setSelected(true);
            antiqueAtlasKeyBinds$setUniqueMarkers();
        }
        @Override
        public void onExitState() {
            removeChild(antiqueAtlasKeyBinds$compareMarkerCursor);
            antiqueAtlasKeyBinds$btnCompareMarker.setSelected(false);
            antiqueAtlasKeyBinds$uniqueMarkers.clear();
        }
    };

    @Inject(
            method = "<init>",
            at = @At("TAIL"),
            remap = false
    )
    public void aakb_antiqueAtlasGuiAtlas_initCompareAtlasButton(CallbackInfo ci) {
        this.addChild(antiqueAtlasKeyBinds$btnCompareMarker).offsetGuiCoords(300, 94);
        this.antiqueAtlasKeyBinds$btnCompareMarker.addListener(button -> {
            if (this.stack != null && SettingsConfig.gameplay.itemNeeded) {
                if (this.state.is(COMPARE_ATLAS_MARKERS)) {
                    this.selectedButton = null;
                    this.state.switchTo(NORMAL);
                } else {
                    this.selectedButton = button;
                    this.state.switchTo(COMPARE_ATLAS_MARKERS);
                }
            }
        });
        this.antiqueAtlasKeyBinds$compareMarkerCursor.setTexture(Textures.BTN_POSITION, 12, 14, 2, 11);
    }

    @Inject(
            method = "prepareToOpen()Lhunternif/mc/atlas/client/gui/GuiAtlas;",
            at = @At("RETURN"),
            remap = false
    )
    private void aakb_antiqueAtlasGuiAtlas_prepareToOpenCompareAtlasButton(CallbackInfoReturnable<GuiAtlas> cir){
        this.antiqueAtlasKeyBinds$uniqueMarkers.clear();
        this.antiqueAtlasKeyBinds$otherStack = this.player.getHeldItemOffhand().getItem() == RegistrarAntiqueAtlas.ATLAS
                ? this.player.getHeldItemOffhand()
                : null;
        this.antiqueAtlasKeyBinds$btnCompareMarker.setEnabled(antiqueAtlasKeyBinds$otherStack != null && this.stack != antiqueAtlasKeyBinds$otherStack);
    }

    @WrapWithCondition(
            method = "drawScreen",
            at = @At(value = "INVOKE", target = "Lhunternif/mc/atlas/client/gui/GuiAtlas;renderMarker(Lhunternif/mc/atlas/marker/Marker;D)V", remap = false)
    )
    private boolean aakb_antiqueAtlasGuiAtlas_drawScreenCompareAtlasMarkers(GuiAtlas instance, Marker marker, double scale, @Local(name = "x") int x, @Local(name = "z") int z) {
        if(this.state.is(COMPARE_ATLAS_MARKERS) && !this.antiqueAtlasKeyBinds$uniqueMarkers.isEmpty()){
            return this.antiqueAtlasKeyBinds$uniqueMarkers.contains(marker);
        }
        return marker != null;
    }

    @WrapOperation(
            method = "mouseClicked",
            at = @At(value = "INVOKE", target = "Lhunternif/mc/atlas/client/gui/core/GuiStates;switchTo(Lhunternif/mc/atlas/client/gui/core/GuiStates$IState;)V", remap = false)
    )
    private void aakb_antiqueAtlasGuiAtlas_mouseClickedCopyOverMarker(GuiStates instance, GuiStates.IState state, Operation<Void> original, @Local(name = "mouseState") int mouseState, @Local boolean isMouseOverMap, @Local(name = "atlasID") int atlasID){
        if (this.state.is(COMPARE_ATLAS_MARKERS) && mouseState == 0){
            if(this.hoveredMarker != null && !this.hoveredMarker.isGlobal() && isMouseOverMap && GuiScreen.isShiftKeyDown()) { // If clicked on a marker, export it:
                if (this.antiqueAtlasKeyBinds$uniqueMarkers.contains(this.hoveredMarker)) {
                    if (antiqueAtlasKeyBinds$otherStack != null) {
                        String markerName = this.hoveredMarker.getLabel().isEmpty() ? this.hoveredMarker.getType() : this.hoveredMarker.getLocalizedLabel();
                        this.player.sendMessage(new TextComponentTranslation("gui.aakb.comparemarker.copy", markerName, antiqueAtlasKeyBinds$otherStack.getItemDamage()));
                        AtlasAPI.getMarkerAPI().putMarker(this.player.world, true, antiqueAtlasKeyBinds$otherStack.getItemDamage(), this.hoveredMarker.getType(), this.hoveredMarker.getLabel(), this.hoveredMarker.getX(), this.hoveredMarker.getZ());
                        this.antiqueAtlasKeyBinds$uniqueMarkers.remove(this.hoveredMarker);
                    }
                }
            }
        }
        else {
            original.call(instance, state);
        }
    }

    @Unique
    private void antiqueAtlasKeyBinds$setUniqueMarkers(){
        if(this.antiqueAtlasKeyBinds$otherStack != null && this.stack != this.antiqueAtlasKeyBinds$otherStack){
            DimensionMarkersData leftMarkerData = null;
            final DimensionMarkersData rightMarkerData;
            MarkersData markersData = AntiqueAtlasMod.markersData.getMarkersData(this.antiqueAtlasKeyBinds$otherStack.getItemDamage(), this.player.getEntityWorld());
            if(markersData != null){
                rightMarkerData = markersData.getMarkersDataInDimension(this.player.dimension);
                markersData = AntiqueAtlasMod.markersData.getMarkersData(getAtlasID(), this.player.getEntityWorld());
                if(markersData != null){
                    leftMarkerData = markersData.getMarkersDataInDimension(this.player.dimension);
                }
            }
            else {
                rightMarkerData = null;
            }

            if(leftMarkerData != null && rightMarkerData != null){
                this.antiqueAtlasKeyBinds$uniqueMarkers.addAll(
                    leftMarkerData.getAllMarkers().stream().filter(leftMarker -> {
                        List<Marker> rightMarkers = rightMarkerData.getMarkersAtChunk(leftMarker.getChunkX() / 8, leftMarker.getChunkZ() / 8);
                        if(rightMarkers != null){
                            for (Marker rightMarker : rightMarkers) {
                                boolean typeComparison = leftMarker.getType().equals(rightMarker.getType());
                                if(!typeComparison){
                                    if(antiqueAtlasKeyBinds$splitContext(leftMarker.getType()).length == 2 && antiqueAtlasKeyBinds$splitContext(rightMarker.getType()).length == 2){
                                        typeComparison = antiqueAtlasKeyBinds$splitContext(leftMarker.getType())[1].equals(antiqueAtlasKeyBinds$splitContext(rightMarker.getType())[1]);
                                    }
                                }
                                if ((leftMarker.getX() == rightMarker.getX()
                                        && (leftMarker.getZ() == rightMarker.getZ())
                                        && typeComparison
                                        && (leftMarker.getLabel().equals(rightMarker.getLabel())))
                                )
                                    return false;
                            }
                        }
                        return true;
                    }).collect(Collectors.toSet())
                );
            }
        }
    }

    // TODO OFC Call from AAAM
    @Unique
    private static String[] antiqueAtlasKeyBinds$splitContext(String type) {
        String[] split = type.split(";");
        return split.length == 2 ? split : new String[]{"", type};
    }
}