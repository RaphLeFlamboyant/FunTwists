package  me.flamboyant.small.heroes;

import me.flamboyant.configurable.parameters.AParameter;
import me.flamboyant.configurable.parameters.SinglePlayerParameter;
import me.flamboyant.utils.Common;
import me.flamboyant.utils.ILaunchablePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class FlamboyantPowers implements Listener, ILaunchablePlugin {
    private static final int entranceCooldownTicks = 10 * 60 * 20;
    private Date useTime;
    private SinglePlayerParameter flamboyantPlayerParameter = new SinglePlayerParameter("Flamboyant powers", "A qui les pouvoirs du Flamboyant ?", true);
    private HashMap<Material, Material> cutCleanMap = new HashMap<Material, Material>() {{
        put(Material.COPPER_ORE, Material.COPPER_INGOT);
        put(Material.GOLD_ORE, Material.GOLD_INGOT);
        put(Material.IRON_ORE, Material.IRON_INGOT);
        put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
    }};

    private Player flamboyantPlayer;

    private static FlamboyantPowers instance;
    public static FlamboyantPowers getInstance()
    {
        if (instance == null)
        {
            instance = new FlamboyantPowers();
        }

        return instance;
    }

    protected FlamboyantPowers()
    {
    }

    @Override
    public boolean start() {
        Common.server.getPluginManager().registerEvents(this, Common.plugin);

        flamboyantPlayer = flamboyantPlayerParameter.getConcernedPlayer();
        if (flamboyantPlayer == null) {
            return false;
        }

        giveFireRes();
        flamboyantPlayer.setCooldown(Material.STICK, 0);

        running = true;

        return true;
    }

    @Override
    public boolean stop() {
        PlayerItemConsumeEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
        EntityShootBowEvent.getHandlerList().unregister(this);
        BlockBreakEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);

        if (flamboyantPlayer != null) {
            flamboyantPlayer.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        }

        running = false;

        return true;
    }

    @Override
    public void resetParameters() {
        flamboyantPlayerParameter = new SinglePlayerParameter("Flamboyant powers", "A qui les pouvoirs du Flamboyant ?", true);
    }

    private boolean running;
    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean canModifyParametersOnTheFly() { return false; }

    @Override
    public List<AParameter> getParameters() {
        List<AParameter> parameters = Arrays.asList(flamboyantPlayerParameter);
        return parameters;
    }

    private void giveFireRes() {
        PotionEffect effect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 6 * 60 * 60 * 20, 1, false, false);
        flamboyantPlayer.addPotionEffect(effect);
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (event.getEntity() != flamboyantPlayer) return;
        if (useTime == null) return;
        Date date = new Date();
        System.out.println(date.compareTo(useTime));
        if (date.compareTo(useTime) < 1000) event.setCancelled(true);
        useTime = null;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer() != flamboyantPlayer) return;
        if (!event.hasItem() || event.getItem().getType() != Material.STICK) return;
        if (flamboyantPlayer.getCooldown(Material.STICK) > 0) return;

        useTime = new Date();
        Location l = flamboyantPlayer.getLocation();
        flamboyantPlayer.getWorld().createExplosion(l, 4f);
        flamboyantPlayer.chat(ChatColor.AQUA + "JE SUIS FLAMBOYANT !!!! ... en position " + l.getBlockX() + "; " + l.getBlockY() + "; " + l.getBlockZ());

        flamboyantPlayer.setCooldown(Material.STICK, entranceCooldownTicks);
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() != Material.MILK_BUCKET) return;
        if (event.getPlayer() != flamboyantPlayer || flamboyantPlayer == null) return;

        Bukkit.getScheduler().runTaskLater(Common.plugin, () -> giveFireRes(), 1L);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() != flamboyantPlayer) return;

        event.getEntity().setFireTicks(20 * 5);
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() != flamboyantPlayer) return;

        Entity projectile = event.getProjectile();
        projectile.setFireTicks(20 * 5);
        event.setProjectile(projectile);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer() != flamboyantPlayer) return;
        if (!cutCleanMap.containsKey(event.getBlock().getType())) return;

        Material itemToSpawn = cutCleanMap.get(event.getBlock().getType());
        event.setDropItems(false);
        flamboyantPlayer.getWorld().spawn(event.getBlock().getLocation(), ExperienceOrb.class).setExperience(2);
        flamboyantPlayer.getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(itemToSpawn, 1));
    }
}
