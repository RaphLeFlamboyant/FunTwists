package me.flamboyant.small;

import me.flamboyant.configurable.parameters.AParameter;
import me.flamboyant.configurable.parameters.IntParameter;
import me.flamboyant.utils.Common;
import me.flamboyant.utils.ILaunchablePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MobSummoningBall implements Listener, ILaunchablePlugin {
    private boolean running;
    private static final int initialMobNumberByBall = 1;
    private IntParameter mobNumberByBallParameter = new IntParameter(Material.CREEPER_HEAD, "Mob to summon with ball", "Number of balls to summon", initialMobNumberByBall, 1, 10);

    private HashMap<String, EntityType> summoningDico = new HashMap<String, EntityType>() {{
        put("Creeper", EntityType.CREEPER);
        put("Blaze", EntityType.BLAZE);
        put("Cave Spider", EntityType.CAVE_SPIDER);
        put("Drowned", EntityType.DROWNED);
        put("Evoker", EntityType.EVOKER);
        put("Enderman", EntityType.ENDERMAN);
        put("Endermite", EntityType.ENDERMITE);
        put("Ghast", EntityType.GHAST);
        put("Giant", EntityType.GIANT);
        put("Guardian", EntityType.GUARDIAN);
        put("Husk", EntityType.HUSK);
        put("Hoglin", EntityType.HOGLIN);
        put("Illusioner", EntityType.ILLUSIONER);
        put("Iron Golem", EntityType.IRON_GOLEM);
        put("Magma Cube", EntityType.MAGMA_CUBE);
        put("Phantom", EntityType.PHANTOM);
        put("Piglin", EntityType.PIGLIN);
        put("Piglin Brute", EntityType.PIGLIN_BRUTE);
        put("Pillager", EntityType.PILLAGER);
        put("Pufferfish", EntityType.PUFFERFISH);
        put("Ravager", EntityType.RAVAGER);
        put("Shulker", EntityType.SHULKER);
        put("Silverfish", EntityType.SILVERFISH);
        put("Skeleton", EntityType.SKELETON);
        put("Slime", EntityType.SLIME);
        put("Spider", EntityType.SPIDER);
        put("Stray", EntityType.STRAY);
        put("Vex", EntityType.VEX);
        put("Vindicator", EntityType.VINDICATOR);
        put("Witch", EntityType.WITCH);
        put("Wither Skeleton", EntityType.WITHER_SKELETON);
        put("Zombie", EntityType.ZOMBIE);
    }};

    private HashMap<UUID, EntityType> entityToSpawnByPlayer = new HashMap<>();

    private static MobSummoningBall instance;
    public static MobSummoningBall getInstance()
    {
        if (instance == null)
        {
            instance = new MobSummoningBall();
        }

        return instance;
    }

    protected MobSummoningBall()
    {

    }

    public boolean start() {
        if (running) {
            return false;
        }

        Common.server.getPluginManager().registerEvents(this, Common.plugin);

        Bukkit.getLogger().info("Launching MobSummoningBall");
        running = true;

        return true;
    }

    public boolean stop() {
        if (!running) {
            return false;
        }

        InventoryClickEvent.getHandlerList().unregister(this);

        Bukkit.getLogger().info("Stopping MobSummoningBall");
        running = false;

        return true;
    }

    @Override
    public void resetParameters() {
        mobNumberByBallParameter = new IntParameter(Material.CREEPER_HEAD, "Mob to summon with ball", "Number of balls to summon", initialMobNumberByBall, 1, 10);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean canModifyParametersOnTheFly() { return true; }

    @Override
    public List<AParameter> getParameters() {
        return Arrays.asList(mobNumberByBallParameter);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntityType() != EntityType.SNOWBALL) return;
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player shooter = (Player) event.getEntity().getShooter();

        ItemStack snowball = shooter.getInventory().getItemInMainHand();
        if (snowball == null || snowball.getType() != Material.SNOWBALL) {
            snowball = shooter.getInventory().getItemInOffHand();
        }

        if (snowball == null) {
            Bukkit.getLogger().warning("Snowball thrown but none of hands have a snowball in it");
            return;
        }

        if (summoningDico.containsKey(snowball.getItemMeta().getDisplayName())) {
            entityToSpawnByPlayer.put(shooter.getUniqueId(), summoningDico.get(snowball.getItemMeta().getDisplayName()));
        }

        shooter.setCooldown(Material.SNOWBALL, 60 * 60 * 5);
    }

    @EventHandler
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        if (event.getEntity().getType() != EntityType.SNOWBALL) return;
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        Player shooter = (Player) event.getEntity().getShooter();
        shooter.setCooldown(Material.SNOWBALL, 0);

        if (entityToSpawnByPlayer.containsKey(shooter.getUniqueId())) {
            for (int i = 0; i < mobNumberByBallParameter.getValue(); i++) {
                event.getEntity().getLocation().getWorld().spawnEntity(event.getEntity().getLocation(), entityToSpawnByPlayer.get(shooter.getUniqueId()));
            }

            entityToSpawnByPlayer.remove(shooter.getUniqueId());
        }
    }
}
