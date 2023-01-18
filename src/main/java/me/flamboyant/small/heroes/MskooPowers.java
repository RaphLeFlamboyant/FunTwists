package  me.flamboyant.small.heroes;

import me.flamboyant.configurable.parameters.AParameter;
import me.flamboyant.configurable.parameters.SinglePlayerParameter;
import me.flamboyant.utils.Common;
import me.flamboyant.utils.ILaunchablePlugin;
import me.flamboyant.utils.ItemHelper;
import org.bukkit.Bukkit;
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

public class MskooPowers implements Listener, ILaunchablePlugin {
    private Player mskooPlayer;
    private SinglePlayerParameter msPlayerParameter = new SinglePlayerParameter("Mskoo powers", "A qui les pouvoirs de Mskoo ?", true);

    private static MskooPowers instance;
    public static MskooPowers getInstance()
    {
        if (instance == null)
        {
            instance = new MskooPowers();
        }

        return instance;
    }

    protected MskooPowers()
    {
    }

    @Override
    public boolean start() {
        mskooPlayer = msPlayerParameter.getConcernedPlayer();
        if (mskooPlayer == null) {
            Bukkit.getLogger().info("Le joueur recevant le pouvoir de Mskoo est absent du manhunt");
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
        msPlayerParameter = new SinglePlayerParameter("Mskoo powers", "A qui les pouvoirs de Mskoo ?", true);
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
        if (target != mskooPlayer) return;
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
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (!ItemHelper.isSameItemKind(item, getMilkBucketItem())) return;

        Player player = event.getPlayer();
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(maxHealth);

        if (player != mskooPlayer) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Cette voluptueuse sensation vous enhivre. " +
                    "Vous voyez des mirages, l'image Mskoo en tenue de nymphe vous obsède. " +
                    "Vous ne comprenez pas ce qui vous arrive ... Vous vous sentez irresistiblement attiré par lui.");
            player.teleport(mskooPlayer.getLocation());
        }
        else player.sendMessage("C'est quoi ce que j'ai bu déjà ?");
    }

    private ItemStack getMilkBucketItem() {
        return ItemHelper.generateItem(Material.MILK_BUCKET, 1, "Essence spirituelle de Mskoo", Arrays.asList("Jus aux 1000 pouvoirs"), true, Enchantment.MULTISHOT, true, false);
    }
}
