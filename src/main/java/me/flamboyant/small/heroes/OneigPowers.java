package  me.flamboyant.small.heroes;

import me.flamboyant.configurable.parameters.AParameter;
import me.flamboyant.configurable.parameters.SinglePlayerParameter;
import me.flamboyant.utils.Common;
import me.flamboyant.utils.ILaunchablePlugin;
import me.flamboyant.utils.MobsHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.List;

public class OneigPowers implements Listener, ILaunchablePlugin {
    private static final List<EntityType> immuneMobs = Arrays.asList(EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.ELDER_GUARDIAN, EntityType.WARDEN);
    private Player oneigPlayer;
    private SinglePlayerParameter oneigPlayerParameter = new SinglePlayerParameter("Oneig_Atyff powers", "A qui les pouvoirs de Oneig_Atyff ?", true);
    private Player speedrunner;
    private boolean isCloseToSpeedrunner;
    private BukkitTask proximityCheck;

    private static OneigPowers instance;
    public static OneigPowers getInstance()
    {
        if (instance == null)
        {
            instance = new OneigPowers();
        }

        return instance;
    }

    protected OneigPowers()
    {
    }

    public void setSpeedrunner(Player speedrunner) {
        this.speedrunner = speedrunner;
    }

    @Override
    public boolean start() {
        oneigPlayer = oneigPlayerParameter.getConcernedPlayer();
        if (oneigPlayer == null) {
            Bukkit.getLogger().info("Le joueur recevant le pouvoir de Oneig_Atyff est absent du manhunt");
            return false;
        }

        Common.server.getPluginManager().registerEvents(this, Common.plugin);

        proximityCheck = Bukkit.getScheduler().runTaskTimer(Common.plugin, () -> checkProximityToSpeedrunner(), 5 * 60 * 20L, 2 * 20L);
        running = true;

        return true;
    }

    @Override
    public boolean stop() {
        EntityDamageEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
        Bukkit.getScheduler().cancelTask(proximityCheck.getTaskId());
        running = false;

        return true;
    }

    @Override
    public void resetParameters() {
        oneigPlayerParameter = new SinglePlayerParameter("Oneig_Atyff powers", "A qui les pouvoirs de Oneig_Atyff ?", true);
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
        List<AParameter> parameters = Arrays.asList(oneigPlayerParameter);
        return parameters;
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (event.getEntity() != oneigPlayer) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)
            return;

        if (event.getDamage() > 0.5) event.setDamage(0.5);
    }

    @EventHandler
    public void onPlayerDamagedByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() != oneigPlayer) return;
        if (event.getDamager().getType() == EntityType.PLAYER) return;
        if (event.getDamager().getType() == EntityType.ARROW) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) return;
        }

        if (MobsHelper.aggressiveMobs.contains(event.getDamager().getType())
                && !immuneMobs.contains(event.getDamager().getType())) {
            event.getDamager().getWorld().spawnEntity(event.getDamager().getLocation(), EntityType.BAT);
            event.getDamager().remove();
            event.setCancelled(true);
        }

        if (event.getDamage() > 0.5) event.setDamage(0.5);
    }

    private void checkProximityToSpeedrunner() {
        Location srLocation = speedrunner.getLocation();
        Location onLocation = oneigPlayer.getLocation();
        boolean isClose = srLocation.getWorld() == onLocation.getWorld() && srLocation.distance(onLocation) < 50;
        if (isClose && !isCloseToSpeedrunner) {
            isCloseToSpeedrunner = true;
            oneigPlayer.chat("" + ChatColor.ITALIC + ChatColor.GRAY + "Avançons doucement pour qu'il ne nous repère pas ...");
        }
        else if (isCloseToSpeedrunner && !isClose) {
            isCloseToSpeedrunner = false;
            oneigPlayer.chat("" + ChatColor.ITALIC + ChatColor.GRAY + "Mince, où est-il passé ?");
        }
    }
}