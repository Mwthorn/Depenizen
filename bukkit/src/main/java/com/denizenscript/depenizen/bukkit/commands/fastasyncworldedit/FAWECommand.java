package com.denizenscript.depenizen.bukkit.commands.fastasyncworldedit;

import com.boydti.fawe.object.FawePlayer;
import com.boydti.fawe.object.schematic.Schematic;
import com.boydti.fawe.util.EditSessionBuilder;
import com.denizenscript.depenizen.bukkit.support.Support;
import com.denizenscript.depenizen.bukkit.support.plugins.WorldEditSupport;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dCuboid;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.utilities.debugging.dB;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

public class FAWECommand extends AbstractCommand {
    // <--[command]
    // @Name fawe
    // @Syntax fawe [clipboard/schematic/walls/set] (save/paste/clipboard) (cuboid:<cuboid>) (position:<location>) (noAir:true/false) (id:<number>) (data:<number>) (target:<target>)
    // @Group Depenizen
    // @Plugin DepenizenBukkit, WorldEdit, FastAsyncWorldEdit
    // @Required 2
    // @Stable TODO
    // @Short TODO
    // @Author Mwthorn

    // @Description
    // TODO
    // @Tags
    // TODO

    // @Usage
    // - fawe CLIPBOARD load
    //
    // @Usage
    // - fawe set cuboid:
    //
    // @Usage
    // - fawe CLIPBOARD
    //
    // -->

    private enum Action {SCHEMATIC, WALLS, SET, CLIPBOARD}
    private enum SchematicAction {SAVE, PASTE, LOAD}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("position")
                    && arg.matchesPrefix("position")) {
                scriptEntry.addObject("position", arg.asType(dLocation.class));
            }

            else if (!scriptEntry.hasObject("position2")
                    && arg.matchesPrefix("position2")) {
                scriptEntry.addObject("position2", arg.asType(dLocation.class));
            }

            else if (!scriptEntry.hasObject("filePath")
                    && arg.matchesPrefix("filePath")) {
                scriptEntry.addObject("filePath", arg.asElement());
            }

            else if (!scriptEntry.hasObject("cuboid")
                    && arg.matchesPrefix("cuboid")) {
                scriptEntry.addObject("cuboid", arg.asType(dCuboid.class));
            }

            else if (!scriptEntry.hasObject("id")
                    && arg.matchesPrefix("id")) {
                scriptEntry.addObject("id", arg.asElement());
            }

            else if (!scriptEntry.hasObject("data")
                    && arg.matchesPrefix("data")) {
                scriptEntry.addObject("data", arg.asElement());
            }

            else if (!scriptEntry.hasObject("noair")
                    && arg.matchesPrefix("noair")) {
                scriptEntry.addObject("noair", arg.asElement());
            }

            else if (!scriptEntry.hasObject("target")
                    && arg.matchesPrefix("target")) {
                scriptEntry.addObject("target", arg.asType(dEntity.class));
            }

            else if (!scriptEntry.hasObject("schematicAction")
                    && arg.matchesEnum(SchematicAction.values())) {
                scriptEntry.addObject("schematicAction", arg.asElement());
            }

            else if (!scriptEntry.hasObject("action")
                    && arg.matchesEnum(Action.values())) {
                scriptEntry.addObject("action", arg.asElement());
            }

            else {
                arg.reportUnhandled();
            }

        }

        if (!scriptEntry.hasObject("action")) {
            throw new InvalidArgumentsException("Action not specified!");
        }

        if (!scriptEntry.hasObject("noair")) {
            scriptEntry.addObject("noair", new Element(false));
        }

        if (!scriptEntry.hasObject("id")) {
            scriptEntry.addObject("id", new Element(1));
        }

        if (!scriptEntry.hasObject("data")) {
            scriptEntry.addObject("data", new Element(0));
        }

        if (!scriptEntry.hasObject("target")) {
            if (((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()) {
                scriptEntry.addObject("target", ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getDenizenEntity());
            }
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element action = scriptEntry.getdObject("action");
        Element schematicAction = scriptEntry.getdObject("schematicAction");
        dLocation position = scriptEntry.getdObject("position");
        Element filePath = scriptEntry.getdObject("filepath");
        Element noAir = scriptEntry.getdObject("noair");
        dCuboid cuboid = scriptEntry.getdObject("cuboid");
        Element id = scriptEntry.getdObject("id");
        Element data = scriptEntry.getdObject("data");
        dEntity target = scriptEntry.getdObject("target");
        dLocation position2 = scriptEntry.getdObject("position2");

        // Report to dB
        dB.report(scriptEntry, getName(), action.debug()
                + (schematicAction != null ? schematicAction.debug() : "")
                + (position != null ? position.debug() : "")
                + (noAir != null ? noAir.debug() : "")
                + (cuboid != null ? cuboid.debug() : "")
                + (id != null ? id.debug() : "")
                + (data != null ? data.debug() : "")
                + (filePath != null ? filePath.debug() : ""));

        if (action.asString().equalsIgnoreCase("schematic")) {

            if (schematicAction == null) {
                dB.echoError(scriptEntry.getResidingQueue(), "Type not specified! (SAVE/PASTE/LOAD)");
                return;
            }

            if (filePath == null) {
                dB.echoError(scriptEntry.getResidingQueue(), "File path not specified!");
                return;
            }

            if (schematicAction.asString().equalsIgnoreCase("paste")) {
                if (position == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Location not specified!");
                    return;
                }

                if (noAir == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Air Boolean not specified!");
                    return;
                }

                String directory = URLDecoder.decode(System.getProperty("user.dir"));
                File file = new File(directory + "/plugins/Denizen/schematics/" + filePath + ".schematic");
                try {
                    World w = new BukkitWorld(position.getWorld());
                    Vector pos = new Vector(position.getX(),position.getY(),position.getZ());
                    ClipboardFormat.SCHEMATIC.load(file).paste(w, pos, true, !noAir.asBoolean(), null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            else if (schematicAction.asString().equalsIgnoreCase("save")) {
                if (cuboid == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Cuboid not specified!");
                    return;
                }

                String directory = URLDecoder.decode(System.getProperty("user.dir"));
                File file = new File(directory + "/plugins/Denizen/schematics/" + filePath + ".schematic");
                dLocation top1 = new dLocation(cuboid.getHigh(0));
                dLocation bot1 = new dLocation(cuboid.getLow(0));
                Vector bot = new Vector(bot1.getX(), bot1.getY(), bot1.getZ());
                Vector top = new Vector(top1.getX(), top1.getY(), top1.getZ());
                World w = new BukkitWorld(cuboid.getWorld());
                CuboidRegion region = new CuboidRegion(w, bot, top);
                Schematic schem = new Schematic(region);
                try {
                    schem.save(file, ClipboardFormat.SCHEMATIC);
                } catch (IOException e) {
                    dB.echoError(scriptEntry.getResidingQueue(), "File not found error!");
                    e.printStackTrace();
                }
            }
        }

        else if (action.asString().equalsIgnoreCase("clipboard")) {

            if (schematicAction == null) {
                dB.echoError(scriptEntry.getResidingQueue(), "Type not specified! (SAVE)");
                return;
            }

            if (schematicAction.asString().equalsIgnoreCase("save")) {
                if (target == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Player was not found!");
                    return;
                }
                if (!target.isPlayer()) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Player was not found!");
                    return;
                }

                if (cuboid == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Cuboid not specified!");
                    return;
                }

                if (position == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Position not specified!");
                    return;
                }

                dLocation top1 = new dLocation(cuboid.getHigh(0));
                dLocation bot1 = new dLocation(cuboid.getLow(0));
                Vector bot = new Vector(bot1.getX(), bot1.getY(), bot1.getZ());
                Vector top = new Vector(top1.getX(), top1.getY(), top1.getZ());
                Vector origin = new Vector(position.getX(), position.getY(), position.getZ());
                World w = new BukkitWorld(cuboid.getWorld());
                CuboidRegion region = new CuboidRegion(w, bot, top);
                Clipboard cb = new Schematic(region).getClipboard();

                if (cb == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Clipboard became null!");
                    return;
                }

                cb.setOrigin(origin);
                WorldEditPlugin wep = Support.getPlugin(WorldEditSupport.class);
                com.sk89q.worldedit.entity.Player p = wep.wrapPlayer(target.getPlayer());
                FawePlayer fp = FawePlayer.wrap(p);
                fp.getSession().setClipboard(new ClipboardHolder(cb, p.getWorld().getWorldData()));

            }
            else if (schematicAction.asString().equalsIgnoreCase("load")) {
                if (target == null) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Player was not found!");
                    return;
                }
                if (!target.isPlayer()) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Player was not found!");
                    return;
                }
                String directory = URLDecoder.decode(System.getProperty("user.dir"));
                File file = new File(directory + "/plugins/Denizen/schematics/" + filePath + ".schematic");

                if (file.exists() && file.length() > 5) {
                    try {
                        WorldEditPlugin wep = Support.getPlugin(WorldEditSupport.class);
                        com.sk89q.worldedit.entity.Player p = wep.wrapPlayer(target.getPlayer());
                        FawePlayer fp = FawePlayer.wrap(p);
                        Clipboard cb = ClipboardFormat.SCHEMATIC.load(file).getClipboard();
                        if (cb == null) {
                            dB.echoError(scriptEntry.getResidingQueue(), "Schematic became null!");
                            return;
                        }
                        fp.getSession().setClipboard(new ClipboardHolder(cb, p.getWorld().getWorldData()));

                    } catch (IOException e) {
                        dB.echoError(scriptEntry.getResidingQueue(), "Schematic file not found!");
                    }
                }
                else {
                    dB.echoError(scriptEntry.getResidingQueue(), "Schematic file not found!");
                }
            }

        }
        else if (action.asString().equalsIgnoreCase("walls")) {

            if (id == null) {
                dB.echoError(scriptEntry.getResidingQueue(), "ID not specified!");
                return;
            }

            if (data == null) {
                dB.echoError(scriptEntry.getResidingQueue(), "Data not specified!");
                return;
            }

            if (position == null) {
                dB.echoError(scriptEntry.getResidingQueue(), "Position not specified!");
                return;
            }

            if (position2 == null) {
                dB.echoError(scriptEntry.getResidingQueue(), "Position2 not specified!");
                return;
            }

            World w = new BukkitWorld(position.getWorld());
            Vector bot = new Vector(position.getX(), position.getY(), position.getZ());
            Vector top = new Vector(position2.getX(), position2.getY(), position2.getZ());
            EditSession editSession = new EditSessionBuilder(w).fastmode(true).build();
            try {
                editSession.makeWalls(new CuboidRegion(w, bot, top), new BaseBlock(id.asInt(),data.asInt()));
                editSession.flushQueue();
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        }
        else if (action.asString().equalsIgnoreCase("set")) {

            if (id == null) {
                dB.echoError(scriptEntry.getResidingQueue(), "ID not specified!");
                return;
            }

            if (data == null) {
                dB.echoError(scriptEntry.getResidingQueue(), "Data not specified!");
                return;
            }

            if (position == null) {
                dB.echoError(scriptEntry.getResidingQueue(), "Position not specified!");
                return;
            }

            if (position2 == null) {
                dB.echoError(scriptEntry.getResidingQueue(), "Position2 not specified!");
                return;
            }

            World w = new BukkitWorld(position.getWorld());
            Vector bot = new Vector(position.getX(), position.getY(), position.getZ());
            Vector top = new Vector(position2.getX(), position2.getY(), position2.getZ());
            EditSession editSession = new EditSessionBuilder(w).fastmode(true).build();
            editSession.setBlocks(new CuboidRegion(w, bot, top), new BaseBlock(id.asInt(),data.asInt()));
            editSession.flushQueue();
        }
        /*
        else if (action.asString().equalsIgnoreCase("set")) {
            if (cuboid == null) {
                dB.echoError(scriptEntry.getResidingQueue(), "Cuboid not specified!");
                return;
            }

            if (id == null) {
                dB.echoError(scriptEntry.getResidingQueue(), "ID not specified!");
                return;
            }

            if (data == null) {
                dB.echoError(scriptEntry.getResidingQueue(), "Data not specified!");
                return;
            }

            World w = new BukkitWorld(cuboid.getWorld());
            dLocation top1 = new dLocation(cuboid.getHigh(0));
            dLocation bot1 = new dLocation(cuboid.getLow(0));
            Vector bot = new Vector(bot1.getX(), bot1.getY(), bot1.getZ());
            Vector top = new Vector(top1.getX(), top1.getY(), top1.getZ());
            EditSession editSession = new EditSessionBuilder(w).fastmode(true).build();
            editSession.setBlocks(new CuboidRegion(w, bot, top), new BaseBlock(id.asInt(),data.asInt()));
            editSession.flushQueue();
        }
        */
    }
}
