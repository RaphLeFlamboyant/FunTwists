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

import java.util.Arrays;
import java.util.List;

public class SurvivalistPowers implements Listener, ILaunchablePlugin {
    private static final List<EntityType> immuneMobs = Arrays.asList(EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.ELDER_GUARDIAN, EntityType.WARDEN);
    private Player survivalistPlayer;
    private SinglePlayerParameter survivalistPlayerParameter = new SinglePlayerParameter("Le Survivaliste powers", "A qui les pouvoirs de Le Survivaliste ?", true);

    private static SurvivalistPowers instance;
    public static SurvivalistPowers getInstance()
    {
        if (instance == null)
        {
            instance = new SurvivalistPowers();
        }

        return instance;
    }

    protected SurvivalistPowers()
    {
    }

    @Override
    public boolean start() {
        survivalistPlayer = survivalistPlayerParameter.getConcernedPlayer();
        if (survivalistPlayer == null) {
            return false;
        }

        Common.server.getPluginManager().registerEvents(this, Common.plugin);
        running = true;
        return true;
    }

    @Override
    public boolean stop() {
        EntityDamageEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
        running = false;

        return true;
    }

    @Override
    public void resetParameters() {
        survivalistPlayerParameter = new SinglePlayerParameter("Le Survivaliste powers", "A qui les pouvoirs de Le Survivaliste ?", true);
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
        List<AParameter> parameters = Arrays.asList(survivalistPlayerParameter);
        return parameters;
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (event.getEntity() != survivalistPlayer) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)
            return;

        if (event.getDamage() > 0.5) event.setDamage(0.5);
    }

    @EventHandler
    public void onPlayerDamagedByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() != survivalistPlayer) return;
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
}