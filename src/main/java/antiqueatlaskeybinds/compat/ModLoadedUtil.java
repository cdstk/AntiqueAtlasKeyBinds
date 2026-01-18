package antiqueatlaskeybinds.compat;

import net.minecraftforge.fml.common.Loader;

public abstract class ModLoadedUtil {

    public static final String DEFILEDLANDS_MODID = "defiledlands";

    private static Boolean defiledLandsLoaded = null;

    public static boolean getDefiledLandsLoaded() {
        if(defiledLandsLoaded == null) defiledLandsLoaded = Loader.isModLoaded(DEFILEDLANDS_MODID);
        return defiledLandsLoaded;
    }
}
