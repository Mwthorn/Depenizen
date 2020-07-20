package com.denizenscript.depenizen.bukkit.events.libsdisguises;

import com.denizenscript.depenizen.bukkit.objects.libsdisguises.LibsDisguiseTag;
import me.libraryaddict.disguise.events.DisguiseEvent;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EntityDisguisesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // libsdisguises disguises disguise
    // libsdisguises disguise disguise
    // libsdisguises disguises <dLibsDisguises>
    // libsdisguises disguise <dLibsDisguises>
    //
    // @Regex ^on libsdisguises [^\s]+ disguise$
    //
    // @Cancellable true
    //
    // @Triggers when a entity disguises.
    //
    // @Context
    // <context.entity> returns the entity who disguised.
    // <context.disguise> returns the disguise in use.
    //
    // @Plugin Depenizen, LibsDisguises
    //
    // @Group Depenizen
    //
    // -->

    public EntityDisguisesScriptEvent() {
        instance = this;
    }

    public static EntityDisguisesScriptEvent instance;

    public DisguiseEvent event;
    public EntityTag entity;
    public LibsDisguiseTag disguise;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("libsdisguises disguise");
    }

    @Override
    public boolean matches(ScriptPath path) {
        String disguiseName = path.eventArgLowerAt(2);

        if (!disguiseName.equals("disguise") && !disguise.equals(LibsDisguiseTag.valueOf(disguiseName))) {
            return false;
        }

        return super.matches(path);
    }

    @Override
    public String getName() {
        return "DisguiseEvent";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(null, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("entity")) {
            return entity;
        }
        else if (name.equals("disguise")) {
            return disguise;
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onDisguise(DisguiseEvent event) {
        disguise = new LibsDisguiseTag(event.getDisguise());
        entity = new EntityTag(event.getEntity());
        this.event = event;
        fire(event);
    }
}
