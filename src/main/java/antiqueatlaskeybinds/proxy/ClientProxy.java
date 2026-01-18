package antiqueatlaskeybinds.proxy;

import antiqueatlaskeybinds.client.KeyHandler;
import antiqueatlaskeybinds.compat.DefiledLandsKeyHandler;
import antiqueatlaskeybinds.compat.ModLoadedUtil;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {

    }

    @Override
    public void init() {
        KeyHandler.initKeybind();
        if(ModLoadedUtil.getDefiledLandsLoaded()) DefiledLandsKeyHandler.initKeybind();
    }
}