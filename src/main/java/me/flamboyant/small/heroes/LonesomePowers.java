package  me.flamboyant.small.heroes;

import me.flamboyant.configurable.parameters.AParameter;
import me.flamboyant.configurable.parameters.SinglePlayerParameter;
import me.flamboyant.utils.ChatColorUtils;
import me.flamboyant.utils.Common;
import me.flamboyant.utils.ILaunchablePlugin;
import me.flamboyant.utils.ItemHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.List;

public class LonesomePowers implements Listener, ILaunchablePlugin {
    private SinglePlayerParameter lonesomePlayerParameter = new SinglePlayerParameter("Lonesome powers", "A qui les pouvoirs de Lonesome ?", true);
    private BukkitTask checkTask;
    private int awayCount;
    private int friendshipCount;

    private static LonesomePowers instance;
    public static LonesomePowers getInstance()
    {
        if (instance == null)
        {
            instance = new LonesomePowers();
        }

        return instance;
    }

    protected LonesomePowers()
    {
    }
    @Override
    public boolean start() {
        if (lonesomePlayerParameter.getConcernedPlayer() == null) {
            Bukkit.getLogger().info("Le joueur recevant le pouvoir de Lonesome est absent du manhunt");
            return false;
        }

        running = true;

        awayCount = -1;
        friendshipCount = -1;
        checkTask = Bukkit.getScheduler().runTaskTimer(Common.plugin, () -> checkDistanceToPeople(), 60 * 1 * 20L, 60 * 1 * 20L);

        return true;
    }

    @Override
    public boolean stop() {
        Bukkit.getScheduler().cancelTask(checkTask.getTaskId());
        PlayerDeathEvent.getHandlerList().unregister(this);
        running = false;

        return true;
    }

    @Override
    public void resetParameters() {
        lonesomePlayerParameter = new SinglePlayerParameter("Lonesome powers", "A qui les pouvoirs de Lonesome ?", true);
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
        List<AParameter> parameters = Arrays.asList(lonesomePlayerParameter);
        return parameters;
    }

    private void checkDistanceToPeople() {
        Player lonesome = lonesomePlayerParameter.getConcernedPlayer();
        for (Player player : Common.server.getOnlinePlayers()) {
            if (player == lonesome) continue;
            if (player.getWorld() == lonesome.getWorld() && player.getLocation().distance(lonesome.getLocation()) < 500) {
                awayCount = -1;
                friendshipCount++;

                if (friendshipCount == 5)
                    lonesome.sendMessage(ChatColorUtils.feedback("Vous êtes heureux d'être proche des gens !"));

                return;
            }
        }

        awayCount++;
        friendshipCount = -1;

        if (awayCount == 2) {
            lonesome.sendMessage(ChatColorUtils.feedback("Vous vous sentez seul si loin de tous"));
        } else if (awayCount == 5) {
            lonesome.sendMessage(ChatColorUtils.feedback("La solitude pèse sur votre armure (... ouvre ton inventaire !)"));
            PlayerInventory inv = lonesome.getInventory();
            inv.setBoots(ItemHelper.generateItem(Material.DIAMOND_BOOTS, 1, "Bottes solitaire", Arrays.asList(), true, Enchantment.BINDING_CURSE, 1, false, true));
            inv.setChestplate(ItemHelper.generateItem(Material.DIAMOND_CHESTPLATE, 1, "Armure solitaire", Arrays.asList(), true, Enchantment.BINDING_CURSE, 1, false, true));
            inv.setHelmet(ItemHelper.generateItem(Material.DIAMOND_HELMET, 1, "Casque solitaire", Arrays.asList(), true, Enchantment.BINDING_CURSE, 1, false, true));
            inv.setLeggings(ItemHelper.generateItem(Material.DIAMOND_LEGGINGS, 1, "Pantalon solitaire", Arrays.asList(), true, Enchantment.BINDING_CURSE, 1, false, true));
        }

    }

}
