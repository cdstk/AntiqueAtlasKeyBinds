package antiqueatlaskeybinds.proxy;

import antiqueatlaskeybinds.AntiqueAtlasKeyBinds;
import antiqueatlaskeybinds.network.PacketHandler;

public class CommonProxy {

    public void preInit() {
        PacketHandler.registerMessages(AntiqueAtlasKeyBinds.MODID);
    }

    public void init() {
    }
}