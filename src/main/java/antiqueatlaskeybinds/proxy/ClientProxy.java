package antiqueatlaskeybinds.proxy;

import antiqueatlaskeybinds.client.KeyHandler;
import antiqueatlaskeybinds.compat.DefiledLandsKeyHandler;
import antiqueatlaskeybinds.compat.ModLoadedUtil;
import antiqueatlaskeybinds.network.PacketHandler;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();
        PacketHandler.registerClientMessages();
    }

    @Override
    public void init() {
        KeyHandler.initKeybind();
        if(ModLoadedUtil.getDefiledLandsLoaded()) DefiledLandsKeyHandler.initKeybind();
    }
}