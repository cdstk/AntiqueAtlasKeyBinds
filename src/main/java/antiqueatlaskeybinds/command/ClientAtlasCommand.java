package antiqueatlaskeybinds.command;

import antiqueatlaskeybinds.AntiqueAtlasKeyBinds;
import antiqueatlaskeybinds.handlers.ForgeConfigHandler;
import antiqueatlaskeybinds.util.IOHelper;
import hunternif.mc.atlas.AntiqueAtlasMod;
import hunternif.mc.atlas.api.AtlasAPI;
import hunternif.mc.atlas.registry.MarkerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class ClientAtlasCommand implements ICommand {

    @Override
    @Nonnull
    public String getName() {
        return "aakb";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "[/aakb <importmarkers> <file>]";
    }

    @Override
    @Nonnull
    public List<String> getAliases() {
        return Collections.emptyList();
    }


    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if(!(sender instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) sender;
        if (args.length < 2) throw new CommandException("commands.aakb.import.invalidusage", this.getUsage(sender));

        if("importmarkers".equals(args[0])){
            StringBuilder importDir = new StringBuilder(args[1]);
            // cleanup any spaced args
            for(int i = 2; i < args.length; i++) importDir.append(" ").append(args[i]);
            if(importDir.indexOf("'") == 0)  importDir.deleteCharAt(0);
            if(importDir.lastIndexOf("'") == importDir.length() - 1) importDir.deleteCharAt(importDir.length() - 1);

            File importMarkerFile = new File(Minecraft.getMinecraft().gameDir.toString() + importDir);
            if(!importMarkerFile.exists() || !importMarkerFile.canRead())
                throw new CommandException("commands.aakb.import.missingfile", importDir);
            if(!importMarkerFile.getAbsolutePath().contains(IOHelper.MARKER_EXPORT_FILE_EXTENSION))
                throw new CommandException("commands.aakb.import.invalidfile");

            String[] worldCheck = importMarkerFile.getName().split("\\.");
            if(worldCheck.length < 4) throw new CommandException("commands.aakb.import.invalidfile");

            boolean invalidWorld = true;
            boolean invalidDimension = !worldCheck[2].equals(String.valueOf(player.dimension));

            if(Minecraft.getMinecraft().isSingleplayer()){
                invalidWorld = !worldCheck[1].equals(Minecraft.getMinecraft().getIntegratedServer().getFolderName());
            }
            else if(Minecraft.getMinecraft().getConnection() != null){
                AntiqueAtlasKeyBinds.LOGGER.log(Level.INFO, "Socket Address: {}", Minecraft.getMinecraft().getConnection().getNetworkManager().getRemoteAddress());
                invalidWorld = !worldCheck[1].equals(IOHelper.simplifyFileName(Minecraft.getMinecraft().getConnection().getNetworkManager().getRemoteAddress().toString()));
            }

            if(invalidWorld || invalidDimension) throw new CommandException("commands.aakb.import.invalidworldinfo", invalidWorld, invalidDimension);

            try {
                for(String line : FileUtils.readLines(importMarkerFile, StandardCharsets.UTF_8)){
                    try {
                        String[] putmarker = line.split(" ");
                        if (putmarker.length < 6) throw new IllegalArgumentException();

                        int x = Integer.parseInt(putmarker[2]);
                        int z = Integer.parseInt(putmarker[3]);

                        String type = putmarker[4].contains(":") ? putmarker[4] : "antiqueatlas:" + putmarker[3];

                        StringBuilder label = new StringBuilder(putmarker[5]);
                        for (int i = 6; i < putmarker.length; i++) label.append(" ").append(putmarker[i]);

                        AtlasAPI.getPlayerAtlases(player).forEach(atlasId ->
                                AtlasAPI.getMarkerAPI().putMarker(player.world, true, atlasId, type, label.toString(), x, z)
                        );
                    } catch (IllegalArgumentException e) {
                        AntiqueAtlasKeyBinds.LOGGER.log(Level.INFO, "Skipping invalid put marker entry: {}", line);
                    }
                }

                Path source = Paths.get(importMarkerFile.getAbsolutePath());
                SimpleDateFormat sdf = new SimpleDateFormat(ForgeConfigHandler.client.importSDF);
                Path destination = source.resolveSibling(importMarkerFile.getName().replace(IOHelper.MARKER_EXPORT_FILE_EXTENSION,"." + sdf.format(new Date(System.currentTimeMillis()))));
                Files.move(source, destination);

                ITextComponent clickableLink = new TextComponentString(destination.getFileName().toString());
                clickableLink.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, destination.toString()));
                clickableLink.getStyle().setUnderlined(true);
                sender.sendMessage(new TextComponentTranslation("commands.aakb.import.success", clickableLink));

            } catch (IOException e) {
                AntiqueAtlasKeyBinds.LOGGER.error(e);
            }   
        } else if ("removemarkers".equals(args[0])) {
            if (args.length < 4) throw new CommandException("commands.aakb.remove.invalidusage", this.getUsage(sender));

            if(!MarkerRegistry.hasKey(args[1])) throw new CommandException("commands.aakb.remove.invalidmarker", args[1]);
            String markerMatch = args[1];
            final int range;
            try {
                range = Integer.parseInt(args[2]);
            }
            catch (NumberFormatException e) {
                throw new CommandException("commands.aakb.remove.invalidusage", this.getUsage(sender));
            }
            // cleanup any spaced args
            StringBuilder labelMatch = new StringBuilder(args[3]);
            for(int i = 4; i < args.length; i++) labelMatch.append(" ").append(args[i]);
            if(labelMatch.indexOf("'") == 0)  labelMatch.deleteCharAt(0);
            if(labelMatch.lastIndexOf("'") == labelMatch.length() - 1) labelMatch.deleteCharAt(labelMatch.length() - 1);

            List<Integer> atlases = AtlasAPI.getPlayerAtlases(player);
            if(atlases.isEmpty()) throw new CommandException("commands.aakb.remove.missingatlas");

            for(int atlasID : atlases) {
                AntiqueAtlasMod.markersData
                        .getMarkersData(atlasID, player.world)
                        .getMarkersDataInDimension(player.world.provider.getDimension())
                        .getAllMarkers()
                        .stream()
                        .filter(marker -> Math.abs(marker.getX() - player.posX) < range && Math.abs(marker.getZ() - player.posZ) < range)
                        .filter(marker -> marker.getType().equals(markerMatch))
                        .filter(marker -> labelMatch.toString().equals("*") || marker.getLabel().contentEquals(labelMatch))
                        .forEach(marker -> AtlasAPI.getMarkerAPI().deleteMarker(player.world, atlasID, marker.getId()));
            }
            sender.sendMessage(new TextComponentTranslation("commands.aakb.remove.success"));
        }
    }

    @Override
    public boolean checkPermission(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender) {
        return true;
    }

    @Override
    @Nonnull
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            return CommandBase.getListOfStringsMatchingLastWord(args, "importmarkers", "removemarkers");
//            completions.add("importmarkers");
            //return CommandBase.getListOfStringsMatchingLastWord(args, completions); //only needed once there's multiple commands
        }
        else if(args.length == 2) {
            if("importmarkers".equals(args[0])){
                File importDir = new File(Minecraft.getMinecraft().gameDir + IOHelper.MARKER_EXPORT_DIRECTORY);
                if(importDir.isDirectory()) {
                    try (Stream<Path> paths = Files.walk(importDir.toPath())) {
                        paths.filter(Files::isRegularFile)
                                .filter(path -> path.getFileName().toString().contains(IOHelper.MARKER_EXPORT_FILE_EXTENSION))
                                .forEach(path -> completions.add("'" + IOHelper.MARKER_EXPORT_DIRECTORY + "/" + path.getFileName() + "'"));
                        return CommandBase.getListOfStringsMatchingLastWord(args, completions);
                    } catch (IOException e) {
                        AntiqueAtlasKeyBinds.LOGGER.error(e);
                    }
                }
            }
            else if("removemarkers".equals(args[0])){
                return CommandBase.getListOfStringsMatchingLastWord(args, MarkerRegistry.getKeys());
            }
        }
        else if(args.length == 3){
            if("removemarkers".equals(args[0])){
                return CommandBase.getListOfStringsMatchingLastWord(args, "16", "32", "64");
            }
        }
        else if(args.length == 4){
            if("removemarkers".equals(args[0])){
                return CommandBase.getListOfStringsMatchingLastWord(args, "'*'");
            }
        }
        return completions;
    }

    @Override
    public boolean isUsernameIndex(@Nonnull String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand other) {
        return this.getName().compareTo(other.getName());
    }
}
