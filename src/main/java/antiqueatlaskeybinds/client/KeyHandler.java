package antiqueatlaskeybinds.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyHandler {

    public static KeyBinding addButtonKey;
    public static KeyBinding deleteButtonKey;
    public static KeyBinding toggleButtonKey;
    public static KeyBinding toggleFollowPlayer;
    public static KeyBinding exportMarkerKey;

    public static void initKeybind() {
        addButtonKey = new KeyBinding(
                "gui.antiqueatlas.addMarker",
                KeyConflictContext.GUI,
                Keyboard.KEY_A,
                "key.antiqueatlas.category"
        );
        deleteButtonKey = new KeyBinding(
                "gui.antiqueatlas.delMarker",
                KeyConflictContext.GUI,
                Keyboard.KEY_D,
                "key.antiqueatlas.category"
        );
        toggleButtonKey = new KeyBinding(
                "gui.antiqueatlas.hideMarkers",
                KeyConflictContext.GUI,
                Keyboard.KEY_W,
                "key.antiqueatlas.category"
        );
        toggleFollowPlayer = new KeyBinding(
                "gui.antiqueatlas.followPlayer",
                KeyConflictContext.GUI,
                KeyModifier.CONTROL,
                Keyboard.KEY_F,
                "key.antiqueatlas.category"
        );
        exportMarkerKey = new KeyBinding(
                "gui.antiqueatlas.exportmarkerdata",
                KeyConflictContext.GUI,
                KeyModifier.CONTROL,
                Keyboard.KEY_C,
                "key.antiqueatlas.category"
        );
        ClientRegistry.registerKeyBinding(addButtonKey);
        ClientRegistry.registerKeyBinding(deleteButtonKey);
        ClientRegistry.registerKeyBinding(toggleButtonKey);
        ClientRegistry.registerKeyBinding(toggleFollowPlayer);
        ClientRegistry.registerKeyBinding(exportMarkerKey);
    }
}