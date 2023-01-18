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

public class BryanPowers implements Listener, ILaunchablePlugin {
    private SinglePlayerParameter bryanPlayerParameter = new SinglePlayerParameter("MARTBRYAN powers", "A qui les pouvoirs de MARTBRYAN ?", true);
    private BukkitTask checkTask;
    private int awayCount;
    private int friendshipCount;

    private static BryanPowers instance;
    public static BryanPowers getInstance()
    {
        if (instance == null)
        {
            instance = new BryanPowers();
        }

        return instance;
    }

    protected BryanPowers()
    {
    }
    @Override
    public boolean start() {
        if (bryanPlayerParameter.getConcernedPlayer() == null) {
            Bukkit.getLogger().info("Le joueur recevant le pouvoir de MARTBRYAN est absent du manhunt");
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
        bryanPlayerParameter = new SinglePlayerParameter("MARTBRYAN powers", "A qui les pouvoirs de MARTBRYAN ?", true);
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
        List<AParameter> parameters = Arrays.asList(bryanPlayerParameter);
        return parameters;
    }

    private void checkDistanceToPeople() {
        Player bryan = bryanPlayerParameter.getConcernedPlayer();
        for (Player player : Common.server.getOnlinePlayers()) {
            if (player == bryan) continue;
            if (player.getWorld() == bryan.getWorld() && player.getLocation().distance(bryan.getLocation()) < 500) {
                awayCount = -1;
                friendshipCount++;

                if (friendshipCount == 5)
                    bryan.sendMessage(ChatColorUtils.feedback("Vous êtes heureux d'être proche des gens !"));

                return;
            }
        }

        awayCount++;
        friendshipCount = -1;

        if (awayCount == 2) {
            bryan.sendMessage(ChatColorUtils.feedback("Vous vous sentez seul si loin de tous"));
        } else if (awayCount == 5) {
            bryan.sendMessage(ChatColorUtils.feedback("La solitude pèse sur votre armure (... ouvre ton inventaire !)"));
            PlayerInventory inv = bryan.getInventory();
            inv.setBoots(ItemHelper.generateItem(Material.DIAMOND_BOOTS, 1, "Bottes solitaire", Arrays.asList(), true, Enchantment.BINDING_CURSE, 1, false, true));
            inv.setChestplate(ItemHelper.generateItem(Material.DIAMOND_CHESTPLATE, 1, "Armure solitaire", Arrays.asList(), true, Enchantment.BINDING_CURSE, 1, false, true));
            inv.setHelmet(ItemHelper.generateItem(Material.DIAMOND_HELMET, 1, "Casque solitaire", Arrays.asList(), true, Enchantment.BINDING_CURSE, 1, false, true));
            inv.setLeggings(ItemHelper.generateItem(Material.DIAMOND_LEGGINGS, 1, "Pantalon solitaire", Arrays.asList(), true, Enchantment.BINDING_CURSE, 1, false, true));
        }

    }

}
