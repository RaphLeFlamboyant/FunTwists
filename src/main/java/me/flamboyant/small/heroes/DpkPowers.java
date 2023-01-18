package  me.flamboyant.small.heroes;

import me.flamboyant.configurable.parameters.AParameter;
import me.flamboyant.configurable.parameters.SinglePlayerParameter;
import me.flamboyant.utils.Common;
import me.flamboyant.utils.ILaunchablePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DpkPowers implements Listener, ILaunchablePlugin {
    private UUID speedrunnerId;
    private SinglePlayerParameter dpkPlayerParameter = new SinglePlayerParameter("DpKBulb powers", "A qui les pouvoirs de DpKBulb ?", true);
    private boolean justDeadByPlugin = false;
    private BukkitTask deathTask;
    private BukkitTask messageTask;

    private static DpkPowers instance;
    public static DpkPowers getInstance()
    {
        if (instance == null)
        {
            instance = new DpkPowers();
        }

        return instance;
    }

    protected DpkPowers()
    {
    }

    public void setSpeedrunner(Player speedrunner) {
        this.speedrunnerId = speedrunner.getUniqueId();
    }

    @Override
    public boolean start() {
        if (dpkPlayerParameter.getConcernedPlayer() == null) {
            Bukkit.getLogger().info("Le joueur recevant le pouvoir de Dpkbulb est absent du manhunt");
            return false;
        }

        Common.server.getPluginManager().registerEvents(this, Common.plugin);
        scheduleTasks();
        running = true;

        return true;
    }

    @Override
    public boolean stop() {
        PlayerDeathEvent.getHandlerList().unregister(this);
        PlayerRespawnEvent.getHandlerList().unregister(this);
        cancelTasks();
        running = false;

        return true;
    }

    @Override
    public void resetParameters() {
        dpkPlayerParameter = new SinglePlayerParameter("DpKBulb powers", "A qui les pouvoirs de DpKBulb ?", true);
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
        List<AParameter> parameters = Arrays.asList(dpkPlayerParameter);
        return parameters;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity() != dpkPlayerParameter.getConcernedPlayer()) return;

        if (justDeadByPlugin) {
            event.setDeathMessage("Dpkbulb has died by tradition");
        }
        else {
            cancelTasks();
            scheduleTasks();
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.getPlayer() != dpkPlayerParameter.getConcernedPlayer()) return;

        if (justDeadByPlugin) {
            PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 1 * 60 * 20, 1);
            PotionEffect jumpBoost = new PotionEffect(PotionEffectType.JUMP, 4 * 60 * 20, 3);
            PotionEffect slowFalling = new PotionEffect(PotionEffectType.SLOW_FALLING, 4 * 60 * 20, 1);

            event.getPlayer().addPotionEffect(speed);
            event.getPlayer().addPotionEffect(jumpBoost);
            event.getPlayer().addPotionEffect(slowFalling);

            justDeadByPlugin = false;
        }
    }

    private void scheduleTasks() {
        messageTask = Bukkit.getScheduler().runTaskLater(Common.plugin, () -> feelingWeirdMessage(), 60 * 9 * 20L);
        deathTask = Bukkit.getScheduler().runTaskLater(Common.plugin, () -> dpkDeath(), 60 * 10 * 20L);
    }

    private void cancelTasks() {
        Bukkit.getScheduler().cancelTask(messageTask.getTaskId());
        Bukkit.getScheduler().cancelTask(deathTask.getTaskId());
    }

    private void feelingWeirdMessage() {
        dpkPlayerParameter.getConcernedPlayer().sendMessage( ChatColor.DARK_RED + "Vous n'êtes pas mort depuis longtemps, cette sensation est bizarre ...");
    }

    private void dpkDeath() {
        justDeadByPlugin = true;

        PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 2 * 60 * 20, 1);
        PotionEffect nightVision = new PotionEffect(PotionEffectType.NIGHT_VISION, 2 * 60 * 20, 1);
        for (Player player : Common.server.getOnlinePlayers()) {
            if (player.getUniqueId() != speedrunnerId
                    && player.getLocation().getWorld() == dpkPlayerParameter.getConcernedPlayer().getLocation().getWorld()
                    && player.getLocation().distance(dpkPlayerParameter.getConcernedPlayer().getLocation()) < 16) {
                player.addPotionEffect(speed);
                player.addPotionEffect(nightVision);
            }
        }

        dpkPlayerParameter.getConcernedPlayer().setHealth(0);
        scheduleTasks();
    }
}