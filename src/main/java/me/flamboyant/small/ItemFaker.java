package me.flamboyant.small;

import me.flamboyant.configurable.parameters.AParameter;
import me.flamboyant.utils.Common;
import me.flamboyant.utils.ILaunchablePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.*;
import java.util.function.Function;

public class ItemFaker implements Listener, ILaunchablePlugin {
    private boolean running;

    private HashMap<String, Function<ItemStack, ItemStack>> complexReplacement = new HashMap<String, Function<ItemStack, ItemStack>>() {{
        put("Diamond Leggings", i -> armorTransformation(i));
        put("Golden Leggings", i -> armorTransformation(i));
        put("Iron Leggings", i -> armorTransformation(i));
        put("Diamond Helmet", i -> armorTransformation(i));
        put("Golden Helmet", i -> armorTransformation(i));
        put("Iron Helmet", i -> armorTransformation(i));
        put("Diamond Chestplate", i -> armorTransformation(i));
        put("Golden Chestplate", i -> armorTransformation(i));
        put("Iron Chestplate", i -> armorTransformation(i));
        put("Diamond Boots", i -> armorTransformation(i));
        put("Golden Boots", i -> armorTransformation(i));
        put("Iron Boots", i -> armorTransformation(i));
        put("Turtle Shell", i -> armorTransformation(i));
    }};

    private HashMap<String, Map.Entry<Material, Material>> complexReplacementRecipe = new HashMap<String, Map.Entry<Material, Material>>() {{
        put("Diamond Leggings", new SimpleEntry<>(Material.LEATHER_LEGGINGS, Material.DIAMOND_LEGGINGS));
        put("Golden Leggings", new SimpleEntry<>(Material.LEATHER_LEGGINGS, Material.GOLDEN_LEGGINGS));
        put("Iron Leggings", new SimpleEntry<>(Material.LEATHER_LEGGINGS, Material.IRON_LEGGINGS));
        put("Diamond Helmet", new SimpleEntry<>(Material.LEATHER_HELMET, Material.DIAMOND_HELMET));
        put("Golden Helmet", new SimpleEntry<>(Material.LEATHER_HELMET, Material.GOLDEN_HELMET));
        put("Iron Helmet", new SimpleEntry<>(Material.LEATHER_HELMET, Material.IRON_HELMET));
        put("Diamond Chestplate", new SimpleEntry<>(Material.LEATHER_CHESTPLATE, Material.DIAMOND_CHESTPLATE));
        put("Golden Chestplate", new SimpleEntry<>(Material.LEATHER_CHESTPLATE, Material.GOLDEN_CHESTPLATE));
        put("Iron Chestplate", new SimpleEntry<>(Material.LEATHER_CHESTPLATE, Material.IRON_CHESTPLATE));
        put("Diamond Boots", new SimpleEntry<>(Material.LEATHER_BOOTS, Material.DIAMOND_BOOTS));
        put("Golden Boots", new SimpleEntry<>(Material.LEATHER_BOOTS, Material.GOLDEN_BOOTS));
        put("Iron Boots", new SimpleEntry<>(Material.LEATHER_BOOTS, Material.IRON_BOOTS));
        put("Turtle Shell", new SimpleEntry<>(Material.LEATHER_BOOTS, Material.TURTLE_HELMET));
    }};

    private HashMap<String, Map.Entry<Material, Material>> simpleRenamingRecipe = new HashMap<String, Map.Entry<Material, Material>>() {{
        put("Ender Pearl", new SimpleEntry<>(Material.SNOWBALL, Material.ENDER_PEARL));
        put("Block of Iron", new SimpleEntry<>(Material.SMOOTH_STONE, Material.IRON_BLOCK));
        put("Block of Gold", new SimpleEntry<>(Material.COPPER_BLOCK, Material.GOLD_BLOCK));
        put("Block of Diamond", new SimpleEntry<>(Material.LIGHT_BLUE_CONCRETE, Material.DIAMOND_BLOCK));
        put("Obsidian", new SimpleEntry<>(Material.COAL_BLOCK, Material.OBSIDIAN));
        put("Bookshelf", new SimpleEntry<>(Material.OAK_PLANKS, Material.BOOKSHELF));
        put("Ancient Debris", new SimpleEntry<>(Material.POLISHED_BASALT, Material.ANCIENT_DEBRIS));
        put("Copper Ingot", new SimpleEntry<>(Material.BRICK, Material.COPPER_INGOT));
        put("Observer", new SimpleEntry<>(Material.FURNACE, Material.OBSERVER));
        put("Infested Cobblestone", new SimpleEntry<>(Material.COBBLESTONE, Material.INFESTED_COBBLESTONE));
        put("Ender Chest", new SimpleEntry<>(Material.CHEST, Material.ENDER_CHEST));
        put("Block of Emerald", new SimpleEntry<>(Material.CHISELED_STONE_BRICKS, Material.EMERALD_BLOCK));
        put("Block of Redstone", new SimpleEntry<>(Material.RED_CONCRETE, Material.REDSTONE_BLOCK));
        put("Block of Lapis Lazuli", new SimpleEntry<>(Material.BLUE_CONCRETE, Material.LAPIS_BLOCK));
        put("Slime Block", new SimpleEntry<>(Material.LIME_CONCRETE, Material.SLIME_BLOCK));
        put("Honey Block", new SimpleEntry<>(Material.YELLOW_CONCRETE, Material.HONEY_BLOCK));
        put("Sticky Piston", new SimpleEntry<>(Material.PISTON, Material.STICKY_PISTON));
        put("Target", new SimpleEntry<>(Material.CHISELED_STONE_BRICKS, Material.TARGET));
        put("Powered Rail", new SimpleEntry<>(Material.RAIL, Material.POWERED_RAIL));
        put("Activator Rail", new SimpleEntry<>(Material.RAIL, Material.ACTIVATOR_RAIL));
        put("Detector Rail", new SimpleEntry<>(Material.RAIL, Material.DETECTOR_RAIL));
        put("Minecart", new SimpleEntry<>(Material.OAK_BOAT, Material.MINECART));
        put("Saddle", new SimpleEntry<>(Material.LEATHER, Material.SADDLE));
        put("Golden Apple", new SimpleEntry<>(Material.APPLE, Material.GOLDEN_APPLE));
        put("Cooked Porkchop", new SimpleEntry<>(Material.PORKCHOP, Material.COOKED_PORKCHOP));
        put("Cooked Cod", new SimpleEntry<>(Material.COD, Material.COOKED_COD));
        put("Cooked Salmon", new SimpleEntry<>(Material.SALMON, Material.COOKED_SALMON));
        put("Steak", new SimpleEntry<>(Material.BEEF, Material.COOKED_BEEF));
        put("Cooked Chicken", new SimpleEntry<>(Material.CHICKEN, Material.COOKED_CHICKEN));
        put("Cooked Rabbit", new SimpleEntry<>(Material.RABBIT, Material.COOKED_RABBIT));
        put("Cooked Mutton", new SimpleEntry<>(Material.MUTTON, Material.COOKED_MUTTON));
        put("Arrow", new SimpleEntry<>(Material.STICK, Material.ARROW));
        put("Slimeball", new SimpleEntry<>(Material.SNOWBALL, Material.SLIME_BALL));
        put("Glowstone Dust", new SimpleEntry<>(Material.SUGAR, Material.GLOWSTONE_DUST));
        put("Gunpowder", new SimpleEntry<>(Material.SUGAR, Material.GUNPOWDER));
        put("Sugar Cane", new SimpleEntry<>(Material.ROTTEN_FLESH, Material.SUGAR_CANE));
        put("Golden Carrot", new SimpleEntry<>(Material.CARROT, Material.GOLDEN_CARROT));
        put("Fire Charge", new SimpleEntry<>(Material.SNOWBALL, Material.FIRE_CHARGE));
        put("End Crystal", new SimpleEntry<>(Material.GLASS_PANE, Material.END_CRYSTAL));
        put("Dispenser", new SimpleEntry<>(Material.SMOKER, Material.DISPENSER));
        put("Hay Bale", new SimpleEntry<>(Material.BARREL, Material.HAY_BLOCK));
        put("Respawn Anchor", new SimpleEntry<>(Material.COMPOSTER, Material.RESPAWN_ANCHOR));
        put("Bottle o' Enchanting", new SimpleEntry<>(Material.GLASS_BOTTLE, Material.EXPERIENCE_BOTTLE));
    }};

    private static ItemFaker instance;
    public static ItemFaker getInstance()
    {
        if (instance == null)
        {
            instance = new ItemFaker();
        }

        return instance;
    }

    protected ItemFaker()
    {

    }

    public boolean start() {
        if (running) {
            return false;
        }

        Common.server.getPluginManager().registerEvents(this, Common.plugin);

        Bukkit.getLogger().info("Launching ItemFaker");
        running = true;

        return true;
    }

    public boolean stop() {
        if (!running) {
            return false;
        }

        InventoryClickEvent.getHandlerList().unregister(this);

        Bukkit.getLogger().info("Stopping ItemFaker");
        running = false;

        return true;
    }

    @Override
    public void resetParameters() {

    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean canModifyParametersOnTheFly() { return false; }

    @Override
    public List<AParameter> getParameters() {
        return new ArrayList<>();
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;
        if (event.getInventory().getType() != InventoryType.ANVIL) return;
        if (event.getCursor() != null && event.getCursor().getType() != Material.AIR && !event.isShiftClick()) return;
        if (event.getCurrentItem() == null) return;
        String name = event.getCurrentItem().getItemMeta().getDisplayName();
        if (!simpleRenamingRecipe.containsKey(name) && !complexReplacementRecipe.containsKey(name)) return;

        ItemStack itemResult = event.getCurrentItem();
        if (simpleRenamingRecipe.containsKey(name))
            itemResult = simpleTransformation(itemResult);
        else
            itemResult = complexReplacement.get(name).apply(itemResult);
        event.setCurrentItem(itemResult);
    }

    private ItemStack simpleTransformation(ItemStack items) {
        String name = items.getItemMeta().getDisplayName();
        if (!simpleRenamingRecipe.containsKey(name) || simpleRenamingRecipe.get(name).getKey() != items.getType()) return items;

        items.setType(simpleRenamingRecipe.get(name).getValue());
        broadcastFakeItemMessage();

        return items;
    }

    private ItemStack armorTransformation(ItemStack items) {
        if(!items.getType().toString().contains("LEATHER_")) return items;

        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) items.getItemMeta();
        String name = leatherArmorMeta.getDisplayName();
        String colorString = leatherArmorMeta.getColor().toString();
        if ((colorString.equals("Color:[rgb0x3AB3DA]") && !name.contains("Diamond"))
                || (colorString.equals("Color:[rgb0xF9FFFE]") && !name.contains("Iron"))
                || (colorString.equals("Color:[rgb0xFED83D]") && !name.contains("Golden"))
                || (colorString.equals("Color:[rgb0x5E7C16]") && (!name.contains("Turtle") || items.getType() != Material.LEATHER_HELMET))
                || !Arrays.asList("Color:[rgb0x3AB3DA]", "Color:[rgb0xF9FFFE]", "Color:[rgb0xFED83D]", "Color:[rgb0x5E7C16]").contains(colorString))
            return items;

        items.setType(complexReplacementRecipe.get(name).getValue());
        broadcastFakeItemMessage();

        return items;
    }

    private void broadcastFakeItemMessage() {
        Bukkit.broadcastMessage("" + ChatColor.RED + ChatColor.ITALIC + "The server protection against counterfeit items is broken. " +
                "Please contact Mojang support, this is a critical issue.");
    }
}
