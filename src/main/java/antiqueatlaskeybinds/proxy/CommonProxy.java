package antiqueatlaskeybinds.proxy;

import antiqueatlaskeybinds.AntiqueAtlasKeyBinds;
import antiqueatlaskeybinds.network.PacketHandler;
import net.minecraft.item.ItemStack;

public class CommonProxy {

    public void preInit() {
        PacketHandler.registerMessages(AntiqueAtlasKeyBinds.MODID);
    }

    public void init() {
    }

    public void setAtlasCompareTo(ItemStack rightSide){

    }

    public ItemStack getComparingAtlas(){
        return null;
    }
}