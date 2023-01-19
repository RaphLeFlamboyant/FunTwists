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

public class SacrificedPowers implements Listener, ILaunchablePlugin {
    private SinglePlayerParameter sacrificedPlayerParameter = new SinglePlayerParameter("Sacrifié powers", "A qui les pouvoirs de Sacrifié ?", true);
    private BukkitTask deathTask;
    private BukkitTask messageTask;

    private static SacrificedPowers instance;
    public static SacrificedPowers getInstance()
    {
        if (instance == null)
        {
            instance = new SacrificedPowers();
        }

        return instance;
    }

    protected SacrificedPowers()
    {
    }

    @Override
    public boolean start() {
        if (sacrificedPlayerParameter.getConcernedPlayer() == null) {
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
        cancelTasks();
        running = false;

        return true;
    }

    @Override
    public void resetParameters() {
        sacrificedPlayerParameter = new SinglePlayerParameter("Sacrificed powers", "A qui les pouvoirs du Sacrifié ?", true);
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
        List<AParameter> parameters = Arrays.asList(sacrificedPlayerParameter);
        return parameters;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity() != sacrificedPlayerParameter.getConcernedPlayer()) return;

        event.setDeathMessage(sacrificedPlayerParameter.getConcernedPlayer() + " se sent mieux");
        sacrificedPlayerParameter.getConcernedPlayer().sendMessage(ChatColor.DARK_RED + "Vous sentez vos batteries rechargées");
        cancelTasks();
        scheduleTasks();
    }

    private void scheduleTasks() {
        messageTask = Bukkit.getScheduler().runTaskLater(Common.plugin, () -> feelingWeirdMessage(), 60 * 9 * 20L);
        deathTask = Bukkit.getScheduler().runTaskLater(Common.plugin, () -> sacrificedDeath(), 60 * 10 * 20L);
    }

    private void cancelTasks() {
        Bukkit.getScheduler().cancelTask(messageTask.getTaskId());
        Bukkit.getScheduler().cancelTask(deathTask.getTaskId());
    }

    private void feelingWeirdMessage() {
        sacrificedPlayerParameter.getConcernedPlayer().sendMessage(ChatColor.DARK_RED + "Vous n'êtes pas mort depuis longtemps, cette sensation est bizarre ...");
    }

    private void sacrificedDeath() {
        PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 2 * 60 * 20, 1);
        PotionEffect nightVision = new PotionEffect(PotionEffectType.NIGHT_VISION, 2 * 60 * 20, 1);
        for (Player player : Common.server.getOnlinePlayers()) {
            if (player.getLocation().getWorld() == sacrificedPlayerParameter.getConcernedPlayer().getLocation().getWorld()
                    && player.getLocation().distance(sacrificedPlayerParameter.getConcernedPlayer().getLocation()) < 16) {
                player.addPotionEffect(speed);
                player.addPotionEffect(nightVision);
            }

            PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, 2 * 60 * 20, 3);
            PotionEffect dig = new PotionEffect(PotionEffectType.SLOW_DIGGING, 2 * 60 * 20, 3);
            PotionEffect weak = new PotionEffect(PotionEffectType.WEAKNESS, 2 * 60 * 20, 3);
            PotionEffect hunger = new PotionEffect(PotionEffectType.HUNGER, 2 * 60 * 20, 1);

            sacrificedPlayerParameter.getConcernedPlayer().addPotionEffect(slowness);
            sacrificedPlayerParameter.getConcernedPlayer().addPotionEffect(dig);
            sacrificedPlayerParameter.getConcernedPlayer().addPotionEffect(weak);
            sacrificedPlayerParameter.getConcernedPlayer().addPotionEffect(hunger);

            scheduleTasks();
        }

        sacrificedPlayerParameter.getConcernedPlayer().sendMessage(ChatColor.DARK_RED + "Vous ressentez un gros coup de mou");
    }
}