package me.dish.souppotions;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.dish.souppotions.files.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.naming.Name;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class SoupPotions extends JavaPlugin implements Listener {

    private Boolean autoConsume;
    private String[] worldIgnore;
    private Object[] soups;

    private String[] interactableItems = {"BARREL", "CHEST", "TABLE", "FURNACE", "JUKEBOX", "ENCHANTING_TABLE", "SHULKER_BOX", "DISPENSER", "DROPPER", "LECTERN", "NOTE_BLOCK", "DOOR", "LOOM", "GRINDSTONE", "BELL", "FENCE_GATE", "BED", "TRAPDOOR", "SMOKER", "BREWING_STAND", "RESPAWN_ANCHOR", "STONECUTTER", "LODESTONE"};

    @Override
    public void onEnable() {

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        Config.setup();

        if (Config.get().getInt("configNum") != 69) {
            getLogger().info("Generating Config...");

            Bukkit.getServer().getPluginManager().getPlugin("SoupPotions").getDataFolder().listFiles()[0].delete();
            Config.setup();

            Config.get().addDefault("configNum", 69);
            Config.get().addDefault("auto-consume", true);

            Config.get().addDefault("world-ignore", Arrays.asList("test", "test2"));

            ConfigurationSection s = Config.get().createSection("soups");
            ConfigurationSection s2 = s.createSection("Souper Soup");
            s2.addDefault("soupEffects","{{ABSORPTION, 0}, {INCREASE_DAMAGE, 0}, {HEAL, 1}}");
            s2.addDefault("duration", 300);
            s2.addDefault("desc", "Honey, where's my souper soup?");
            s2.addDefault("recipe", "APPLE, SPIDER_EYE");

            Config.get().options().copyDefaults(true);
            Config.save();
            getLogger().info("Done!");
        }

        configSetup(false);

        getLogger().info("Done!");

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Enabled!");

    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled!");
    }

    private void configSetup(boolean resetRecipe) {

        if (resetRecipe) {
            for (Object soup : soups) {
                Bukkit.removeRecipe(new NamespacedKey(this, soup.toString().replace(" ", "_")));
            }
        }

        getLogger().info("Adding items...");
        autoConsume = Config.get().getBoolean("auto-consume");
        Object[] temp = Config.get().getList("world-ignore").toArray();
        worldIgnore = Arrays.copyOf(temp, temp.length, String[].class);

        soups = Config.get().getConfigurationSection("soups").getKeys(false).toArray();

        for (Object soup : soups) {

            String s = soup.toString();

            // Getting metadata
            String[] soupEffectsNoLevel = Config.get().getConfigurationSection("soups").getString(s + ".soupEffects").replaceAll("\\d", "").replace(" ", "").replace("{", "").replace("}", "").split(",");
            String[] soupEffectsLevel = Config.get().getConfigurationSection("soups").getString(s + ".soupEffects").replace(" ", "").replace("{", "").replace("}", "").split(",");

            // Create item
            ItemStack item = new ItemStack(Material.MUSHROOM_STEW, 1);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.addEnchant(Enchantment.CHANNELING, 1, false);
            itemMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + s);

            String effects = Arrays.toString(soupEffectsLevel);
            itemMeta.setLore(List.of(Config.get().getConfigurationSection("soups").getString(s + ".desc"), effects, Config.get().getConfigurationSection("soups").getString(s + ".duration")));
            item.setItemMeta(itemMeta);

            // Create recipe
            String[] recipeArr = Config.get().getConfigurationSection("soups").getString(s + ".recipe").replace(" ", "").split(",");

            ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, s.replace(" ", "_")), item);

            recipe.shape("A ", "BC");
            recipe.setIngredient('A', Material.valueOf(recipeArr[0]));
            recipe.setIngredient('B', Material.MUSHROOM_STEW);
            recipe.setIngredient('C', Material.valueOf(recipeArr[1]));

            Bukkit.addRecipe(recipe);

        }
    }

    @EventHandler
    public void onFoodEat(PlayerItemConsumeEvent e) {

        Player p = e.getPlayer();
        ItemMeta item = p.getInventory().getItemInMainHand().getItemMeta();

        // Checking if item is a soup potion
        if (item.getDisplayName().toLowerCase().contains("§b§l")) {

            String[] soupString = Arrays.copyOf(soups, soups.length, String[].class);

            String[] effects = item.getLore().get(item.getLore().size() - 2).replace(" ", "").replace("[", "").replace("]", "").split(",");
            int duration = Integer.parseInt(item.getLore().get(item.getLore().size() - 1));

            for (int i = 0; i <= effects.length - 2; i+=2) {
                p.addPotionEffect(PotionEffectType.getByName(effects[i]).createEffect(duration, Integer.parseInt(effects[i + 1])));
            }

        }
    }

    @EventHandler
    public void rightClick(PlayerInteractEvent e) {

        Player p = e.getPlayer();
        ItemMeta item = p.getInventory().getItemInMainHand().getItemMeta();

        if (!autoConsume)
            return;

        // Check if player right clicked
        if (!(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
            return;

        if (item == null)
            return;

        // Check if item is a soup
        if (item.getDisplayName().toLowerCase().contains("§b§l")) {

            // Check if right clicking interactable block
            if (!(e.getClickedBlock() == null)) {
                if (Arrays.stream(interactableItems).anyMatch(e.getClickedBlock().getBlockData().getMaterial().name()::contains))
                    return;
            }

            String[] soupString = Arrays.copyOf(soups, soups.length, String[].class);

            String[] effects = item.getLore().get(item.getLore().size() - 2).replace(" ", "").replace("[", "").replace("]", "").split(",");
            int duration = Integer.parseInt(item.getLore().get(item.getLore().size() - 1));

            for (int i = 0; i <= effects.length - 2; i+=2) {
                p.addPotionEffect(PotionEffectType.getByName(effects[i]).createEffect(duration, Integer.parseInt(effects[i + 1])));
            }

            p.setFoodLevel(p.getFoodLevel() + 3);
            if (p.getFoodLevel() > 20)
                p.setFoodLevel(20);

            p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

        }

        // Check if item is vanilla stew
        if (p.getInventory().getItemInMainHand().toString().toLowerCase().contains("mushroom_stew")) {

            if (Arrays.stream(worldIgnore).anyMatch(p.getWorld().getName()::equalsIgnoreCase))
                return;

            if (!(e.getClickedBlock() == null)) {
                if (Arrays.stream(interactableItems).anyMatch(e.getClickedBlock().getBlockData().getMaterial().name()::contains))
                    return;
            }

            p.setFoodLevel(p.getFoodLevel() + 3);

            if (p.getFoodLevel() > 20)
                p.setFoodLevel(20);

            p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }

    }

    @EventHandler
    public void craftCheck(CraftItemEvent e) {
        if (e.getRecipe().getResult().getItemMeta().getDisplayName().toLowerCase().contains("§b§l")) {
            if (Arrays.stream(worldIgnore).anyMatch(e.getInventory().getLocation().getWorld().getName()::equalsIgnoreCase)) {
                e.setCancelled(true);
                e.getInventory().getViewers().get(0).sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Hey!" + ChatColor.GOLD + " You cannot craft that in this world!");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("sp")) {

            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    p.sendMessage(ChatColor.RED + "Invalid command, please type /help for commands.");
                }
            }
            if (args[0].equalsIgnoreCase("reload")) {
                Config.reload();
                configSetup(true);
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    p.sendMessage(ChatColor.GOLD + "Soup Potions has been reloaded!");
                }
            }
        }

        return true;
    }
}
