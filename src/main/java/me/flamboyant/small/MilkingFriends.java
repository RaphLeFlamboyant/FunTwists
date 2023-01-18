package me.flamboyant.small;

import me.flamboyant.configurable.parameters.AParameter;
import me.flamboyant.configurable.parameters.SinglePlayerParameter;
import me.flamboyant.utils.Common;
import me.flamboyant.utils.ILaunchablePlugin;
import me.flamboyant.utils.ItemHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MilkingFriends implements Listener, ILaunchablePlugin {
    private boolean running;
    private SinglePlayerParameter lootMilkPlayerParameter = new SinglePlayerParameter("Lait de loot", "De qui le lait loot des items ?", true);
    private SinglePlayerParameter mobSpawnPlayerParameter = new SinglePlayerParameter("Lait de mobs", "De qui le lait fait spawn des mobs ?", true);
    private SinglePlayerParameter potionMilkPlayerParameter = new SinglePlayerParameter("Lait de survie", "De qui le lait donne des effets ?", true);
    private SinglePlayerParameter noFallPlayerParameter = new SinglePlayerParameter("Lait de chute", "De qui le lait annule une chute ?", true);
    private SinglePlayerParameter enchantPlayerParameter = new SinglePlayerParameter("Lait de bibliothèque", "De qui le lait donne le savoir ?", true);

    private static MilkingFriends instance;
    public static MilkingFriends getInstance()
    {
        if (instance == null)
        {
            instance = new MilkingFriends();
        }

        return instance;
    }

    protected MilkingFriends()
    {
    }

    @Override
    public boolean start() {
        if (running) {
            return false;
        }

        Common.server.getPluginManager().registerEvents(this, Common.plugin);

        Bukkit.getLogger().info("Launching MilkingFriends");
        running = true;

        return true;
    }

    @Override
    public boolean stop() {
        if (!running) {
            return false;
        }

        PlayerInteractEntityEvent.getHandlerList().unregister(this);
        PlayerItemConsumeEvent.getHandlerList().unregister(this);

        Bukkit.getLogger().info("Stopping MilkingFriends");
        running = false;

        return true;
    }

    @Override
    public void resetParameters() {
        lootMilkPlayerParameter = new SinglePlayerParameter("Lait de loot", "De qui le lait loot des items ?", true);
        mobSpawnPlayerParameter = new SinglePlayerParameter("Lait de mobs", "De qui le lait fait spawn des mobs ?", true);
        potionMilkPlayerParameter = new SinglePlayerParameter("Lait de survie", "De qui le lait donne des effets ?", true);
        noFallPlayerParameter = new SinglePlayerParameter("Lait de chute", "De qui le lait annule une chute ?", true);
        enchantPlayerParameter = new SinglePlayerParameter("Lait de bibliothèque", "De qui le lait donne le savoir ?", true);

        List<SinglePlayerParameter> powers = new ArrayList<>(Arrays.asList(lootMilkPlayerParameter, mobSpawnPlayerParameter, potionMilkPlayerParameter, noFallPlayerParameter, enchantPlayerParameter));
        for (Player player : Common.server.getOnlinePlayers()) {
            SinglePlayerParameter param = powers.get(Common.rng.nextInt(powers.size()));
            powers.remove(param);
            param.setConcernedPlayer(player);

        for (SinglePlayerParameter power : powers) {
            power.setConcernedPlayer(null);
        }

            if (powers.isEmpty()) break;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public List<AParameter> getParameters() {
        return Arrays.asList(lootMilkPlayerParameter, mobSpawnPlayerParameter, potionMilkPlayerParameter, noFallPlayerParameter, enchantPlayerParameter);
    }

    @Override
    public boolean canModifyParametersOnTheFly() { return false; }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Bukkit.getLogger().info("PlayerInteractEntityEvent");
        if (!(event.getRightClicked() instanceof Player)) return;
        Player target = (Player) event.getRightClicked();
        Player player = event.getPlayer();
        if (player.getInventory().getItem(event.getHand()).getType() != Material.BUCKET) return;

        PlayerInventory inv = player.getInventory();
        ItemStack buckets = inv.getItemInMainHand();
        ItemStack item = getMilkBucketItem(target);

        if (buckets.getAmount() > 1) {
            buckets.setAmount(buckets.getAmount() - 1);
            inv.setItemInMainHand(buckets);

            inv.addItem(item);
        }
        else
            inv.setItemInMainHand(item);
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item.getType() != Material.MILK_BUCKET
                || !item.getItemMeta().getDisplayName().contains("Lait de ")
                || !item.getEnchantments().containsKey(Enchantment.ARROW_FIRE)) return;

        Player drinker = event.getPlayer();
        String playerName = item.getItemMeta().getDisplayName().substring(8);

        if (Common.server.getPlayer(playerName) == null) {
            Bukkit.getLogger().warning("The player " + playerName + " whom milk was drunk is not on the server");
        }

        Bukkit.getScheduler().runTaskLater(Common.plugin,() -> {
            if (playerName.equals(getPlayerName(lootMilkPlayerParameter)))
                dpkMilkEffect(drinker);
            else if (playerName.equals(getPlayerName(mobSpawnPlayerParameter)))
                raphMilkEffect(drinker);
            else if (playerName.equals(getPlayerName(potionMilkPlayerParameter)))
                oneigMilkEffect(drinker);
            else if (playerName.equals(getPlayerName(enchantPlayerParameter)))
                crazadaxMilkEffect(drinker);
            else if (playerName.equals(getPlayerName(noFallPlayerParameter)))
                mskooMilkEffect(drinker);
            else
                defaultMilkEffect(drinker);
        }, 1L);
    }

    private String getPlayerName(SinglePlayerParameter param) {
        return param.getConcernedPlayer() == null ? "" : param.getConcernedPlayer().getDisplayName();
    }

    private void defaultMilkEffect(Player drinker) {
        drinker.setHealth(Math.min(drinker.getHealth() + 8, drinker.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
    }

    private void mskooMilkEffect(Player drinker) {
        drinker.setFallDistance(0);

        Location loc = drinker.getLocation();
        int targetY = drinker.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ());
        if (targetY > loc.getBlockY()) {
            for (targetY = loc.getBlockY(); targetY > loc.getWorld().getMinHeight(); targetY--) {
                if (loc.getWorld().getBlockAt(loc.getBlockX(), targetY, loc.getBlockZ()).getType() != Material.AIR)
                    break;
            }
        }

        if (targetY > loc.getWorld().getMinHeight())
            drinker.teleport(new Location(loc.getWorld(), loc.getBlockX(), targetY + 1, loc.getBlockZ(), loc.getYaw(), loc.getPitch()));
    }

    private void crazadaxMilkEffect(Player drinker) {
        Location location = drinker.getLocation();

        LootContext context =
                new LootContext.Builder(location)
                        .luck(3)
                        .lootingModifier(3)
                        .build();

        LootTable lt = LootTables.STRONGHOLD_LIBRARY.getLootTable();
        Collection<ItemStack> items = lt.populateLoot(Common.rng, context);

        for (ItemStack item : items) {
            drinker.getWorld().dropItem(location, item);
        }
    }

    private void oneigMilkEffect(Player drinker) {
        PotionEffect effect = new PotionEffect(PotionEffectType.SLOW_FALLING, 5 * 60 * 20, 1, false, true);
        drinker.addPotionEffect(effect);
        effect = new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 2, false, true);
        drinker.addPotionEffect(effect);
        effect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 20, 10, false, true);
        drinker.addPotionEffect(effect);
        effect = new PotionEffect(PotionEffectType.WATER_BREATHING, 5 * 60 * 20, 1, false, true);
        drinker.addPotionEffect(effect);
        effect = new PotionEffect(PotionEffectType.NIGHT_VISION, 5 * 60 * 20, 1, false, true);
        drinker.addPotionEffect(effect);
    }

    private void raphMilkEffect(Player drinker) {
        for (int i = 0; i < 4; i++) {
            drinker.getWorld().spawnEntity(drinker.getLocation(), EntityType.BLAZE);
            drinker.getWorld().spawnEntity(drinker.getLocation(), EntityType.ENDERMAN);
        }
    }

    private void dpkMilkEffect(Player drinker) {
        PotionEffect effect = new PotionEffect(PotionEffectType.CONFUSION, 60 * 20, 1, true, true);
        drinker.addPotionEffect(effect);

        PlayerInventory inv = drinker.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                Material itemType = ItemHelper.getRandomLegitMaterial();
                inv.addItem(new ItemStack(itemType, Common.rng.nextInt(itemType.getMaxStackSize())));
            }
        }
    }


    private ItemStack getMilkBucketItem(Player player) {
        return ItemHelper.generateItem(Material.MILK_BUCKET, 1, composeBucketName(player), Arrays.asList("Jus aux 1000 pouvoirs"), true, Enchantment.ARROW_FIRE, true, false);
    }

    private String composeBucketName(Player player) {
        return "Lait de " + player.getDisplayName();
    }
}
