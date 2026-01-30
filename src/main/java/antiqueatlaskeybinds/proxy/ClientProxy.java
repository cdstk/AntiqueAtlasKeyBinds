package antiqueatlaskeybinds.proxy;

import antiqueatlaskeybinds.client.KeyHandler;
import antiqueatlaskeybinds.compat.DefiledLandsKeyHandler;
import antiqueatlaskeybinds.compat.ModLoadedUtil;
import antiqueatlaskeybinds.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ClientProxy extends CommonProxy {

    private ItemStack rightSide = null;

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

    @Override
    public void setAtlasCompareTo(ItemStack rightSide){
        this.rightSide = rightSide;
    }

    @Override
    public ItemStack getComparingAtlas(){
        EntityPlayer player = Minecraft.getMinecraft().player;
        if(player == null || this.rightSide == null || this.rightSide.isEmpty()) return null;
        return this.rightSide;
    }
}