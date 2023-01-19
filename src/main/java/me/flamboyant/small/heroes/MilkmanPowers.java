package  me.flamboyant.small.heroes;

import me.flamboyant.configurable.parameters.AParameter;
import me.flamboyant.configurable.parameters.SinglePlayerParameter;
import me.flamboyant.utils.Common;
import me.flamboyant.utils.ILaunchablePlugin;
import me.flamboyant.utils.ItemHelper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.List;

public class MilkmanPowers implements Listener, ILaunchablePlugin {
    private Player milkmanPlayer;
    private SinglePlayerParameter msPlayerParameter = new SinglePlayerParameter("Milkman powers", "A qui les pouvoirs de Milkman ?", true);

    private static MilkmanPowers instance;
    public static MilkmanPowers getInstance()
    {
        if (instance == null)
        {
            instance = new MilkmanPowers();
        }

        return instance;
    }

    protected MilkmanPowers()
    {
    }

    @Override
    public boolean start() {
        milkmanPlayer = msPlayerParameter.getConcernedPlayer();
        if (milkmanPlayer == null) {
            return false;
        }

        Common.server.getPluginManager().registerEvents(this, Common.plugin);
        running = true;

        return true;
    }

    @Override
    public boolean stop() {
        PlayerItemConsumeEvent.getHandlerList().unregister(this);
        PlayerInteractEntityEvent.getHandlerList().unregister(this);
        running = false;

        return true;
    }

    @Override
    public void resetParameters() {
        msPlayerParameter = new SinglePlayerParameter("Milkman powers", "A qui les pouvoirs de Milkman ?", true);
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
        List<AParameter> parameters = Arrays.asList(msPlayerParameter);
        return parameters;
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) return;
        Player target = (Player) event.getRightClicked();
        if (target != milkmanPlayer) return;
        Player player = event.getPlayer();
        if (player.getInventory().getItem(event.getHand()).getType() != Material.BUCKET) return;

        PlayerInventory inv = player.getInventory();
        ItemStack buckets = inv.getItemInMainHand();
        ItemStack item = getMilkBucketItem();

        if (buckets.getAmount() > 1) {
            buckets.setAmount(buckets.getAmount() - 1);
            inv.setItemInMainHand(buckets);
            inv.addItem(item);
        }
        else
            inv.setItemInMainHand(item);

        milkmanPlayer.sendMessage(player.getDisplayName() + " vous a pris du lait");
        player.sendMessage("Milkman vous a donné du lait");
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (!ItemHelper.isSameItemKind(item, getMilkBucketItem())) return;

        Player player = event.getPlayer();
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(maxHealth);

        player.sendMessage("Le lait vous a fait du bien");
    }

    private ItemStack getMilkBucketItem() {
        return ItemHelper.generateItem(Material.MILK_BUCKET, 1, "Lait offert par Milkman", Arrays.asList("Lait aux 1000 pouvoirs"), true, Enchantment.MULTISHOT, true, false);
    }
}
