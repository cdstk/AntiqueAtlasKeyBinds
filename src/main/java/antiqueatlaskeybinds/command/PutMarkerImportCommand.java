package antiqueatlaskeybinds.command;

import antiqueatlaskeybinds.AntiqueAtlasKeyBinds;
import hunternif.mc.atlas.api.AtlasAPI;
import net.minecraft.client.Minecraft;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class PutMarkerImportCommand implements ICommand {

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

        File importMarkerFile = new File(Minecraft.getMinecraft().gameDir.toString() + args[1]);
        if(!importMarkerFile.exists() || !importMarkerFile.canRead())
            throw new CommandException("commands.aakb.import.missingfile", args[1]);
        if(!importMarkerFile.getAbsolutePath().endsWith(AntiqueAtlasKeyBinds.MARKER_EXPORT_FILE_EXTENSION))
            throw new CommandException("commands.aakb.import.invalidfile");

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
            Path destination = source.resolveSibling(importMarkerFile.getName() + "." + System.currentTimeMillis() + ".txt");
            Files.move(source, destination);

            ITextComponent clickableLink = new TextComponentString(destination.getFileName().toString());
            clickableLink.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, destination.toString()));
            clickableLink.getStyle().setUnderlined(true);
            sender.sendMessage(new TextComponentTranslation("commands.aakb.import.success", clickableLink));

        } catch (IOException e) {
            AntiqueAtlasKeyBinds.LOGGER.error(e);
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
            completions.add("importmarkers");
            //return CommandBase.getListOfStringsMatchingLastWord(args, completions); //only needed once there's multiple commands
        }
        else if(args.length == 2) {
            File importDir = new File(Minecraft.getMinecraft().gameDir + AntiqueAtlasKeyBinds.MARKER_EXPORT_DIRECTORY);
            if(importDir.isDirectory()) {
                try (Stream<Path> paths = Files.walk(importDir.toPath())) {
                    paths.filter(Files::isRegularFile)
                            .filter(path -> path.getFileName().toString().endsWith(AntiqueAtlasKeyBinds.MARKER_EXPORT_FILE_EXTENSION))
                            .forEach(path -> completions.add(AntiqueAtlasKeyBinds.MARKER_EXPORT_DIRECTORY + "/" + path.getFileName()));
                } catch (IOException e) {
                    AntiqueAtlasKeyBinds.LOGGER.error(e);
                }
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
