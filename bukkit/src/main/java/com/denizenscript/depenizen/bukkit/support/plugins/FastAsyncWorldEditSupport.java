package com.denizenscript.depenizen.bukkit.support.plugins;

import com.denizenscript.depenizen.bukkit.commands.fastasyncworldedit.FAWECommand;
import com.denizenscript.depenizen.bukkit.support.Support;

public class FastAsyncWorldEditSupport extends Support {
    public FastAsyncWorldEditSupport() {
        new FAWECommand().activate().as("fawe").withOptions("See Documentation.", 1);
    }
}
