package antiqueatlaskeybinds;

import antiqueatlaskeybinds.command.PutMarkerImportCommand;
import antiqueatlaskeybinds.handlers.ModRegistry;
import antiqueatlaskeybinds.proxy.CommonProxy;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = AntiqueAtlasKeyBinds.MODID, version = AntiqueAtlasKeyBinds.VERSION, name = AntiqueAtlasKeyBinds.NAME, dependencies = "required-after:fermiumbooter")
public class AntiqueAtlasKeyBinds {
    public static final String MODID = "antiqueatlaskeybinds";
    public static final String VERSION = "0.0.0";
    public static final String NAME = "AntiqueAtlasKeyBinds";
    public static final Logger LOGGER = LogManager.getLogger();
    public static boolean completedLoading = false;

    public static final String MARKER_EXPORT_DIRECTORY = "/atlasmarkerexports";
    public static final String MARKER_EXPORT_FILE_EXTENSION = ".markerexport";

    @SidedProxy(clientSide = "antiqueatlaskeybinds.proxy.ClientProxy", serverSide = "antiqueatlaskeybinds.proxy.CommonProxy")
    public static CommonProxy PROXY;
	
	@Instance(MODID)
	public static AntiqueAtlasKeyBinds instance;
	
	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModRegistry.init();
        AntiqueAtlasKeyBinds.PROXY.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event){
        AntiqueAtlasKeyBinds.PROXY.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new PutMarkerImportCommand());
        completedLoading = true;
    }
}