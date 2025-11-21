package antiqueatlaskeybinds.proxy;

import antiqueatlaskeybinds.client.KeyHandler;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {

    }

    @Override
    public void init() {
        KeyHandler.initKeybind();
    }
}