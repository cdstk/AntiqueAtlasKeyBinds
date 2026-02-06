package antiqueatlaskeybinds.compat;

import antiqueatlaskeybinds.handlers.ForgeConfigHandler;
import hunternif.mc.atlas.AntiqueAtlasMod;
import hunternif.mc.atlas.api.AtlasAPI;
import lykrast.defiledlands.common.entity.passive.EntityBookWyrm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.stream.Collectors;

public class DefiledLandsKeyHandler {

    public static KeyBinding bookwrymKey;

    public static void initKeybind() {
        bookwrymKey = new KeyBinding(
                "key.aaam.bookwrym",
                KeyConflictContext.UNIVERSAL,
                Keyboard.KEY_W,
                "key.antiqueatlas.category"
        );
        ClientRegistry.registerKeyBinding(bookwrymKey);

        MinecraftForge.EVENT_BUS.register(DefiledLandsKeyHandler.class);
    }

    @SubscribeEvent
    public static void onKeyPressed(InputEvent.KeyInputEvent event){
        if(!bookwrymKey.isKeyDown() && !bookwrymKey.isPressed()) return;

        EntityPlayer player = Minecraft.getMinecraft().player;
        List<Integer> atlases = AtlasAPI.getPlayerAtlases(player);
        if(atlases.isEmpty()) return;

        World world = Minecraft.getMinecraft().world;
        List<EntityBookWyrm> bookWyrms = world.getLoadedEntityList().stream()
                .filter(v -> v instanceof EntityBookWyrm)
                .map(v -> (EntityBookWyrm) v)
                .filter(EntityBookWyrm::isGolden)
                .collect(Collectors.toList());

        for (EntityBookWyrm bookWyrm : bookWyrms) {
            int villX = (int) bookWyrm.posX;
            int villZ = (int) bookWyrm.posZ;
            for(int atlasID : atlases) {
                AntiqueAtlasMod.markersData
                        .getMarkersData(atlasID, player.world)
                        .getMarkersDataInDimension(player.world.provider.getDimension())
                        .getAllMarkers()
                        .stream()
                        .filter(marker -> Math.abs(marker.getX() - villX) < 64 && Math.abs(marker.getZ() - villZ) < 64)
                        .filter(marker -> marker.getType().equals(ForgeConfigHandler.client.bookwrymKeyMarker))
                        .filter(marker -> marker.getLabel().equals(bookWyrm.getDisplayName().getFormattedText()))
                        .forEach(marker -> AtlasAPI.getMarkerAPI().deleteMarker(world, atlasID, marker.getId()));

                AtlasAPI.getMarkerAPI().putMarker(world, true, atlasID, ForgeConfigHandler.client.bookwrymKeyMarker, bookWyrm.getDisplayName().getFormattedText(), villX, villZ);
            }
        }
    }
}
