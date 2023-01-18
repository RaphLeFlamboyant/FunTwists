package me.flamboyant.small.advancementfaker;

import me.flamboyant.configurable.parameters.AParameter;
import me.flamboyant.configurable.parameters.BooleanParameter;
import me.flamboyant.configurable.parameters.SinglePlayerParameter;
import me.flamboyant.configurable.parameters.StringSelectionParameter;
import me.flamboyant.utils.Common;
import me.flamboyant.utils.ILaunchablePlugin;
import me.flamboyant.utils.ItemHelper;
import me.flamboyant.configurable.gui.wrapper.items.BooleanParameterItem;
import me.flamboyant.configurable.gui.wrapper.items.SinglePlayerParameterItem;
import me.flamboyant.configurable.gui.wrapper.items.StringSelectionParameterItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class AdvancementFaker implements Listener, ILaunchablePlugin {
    private boolean running;
    private AdvancementRemoteView advancementFakerView = new AdvancementRemoteView();
    private SinglePlayerParameter ownerPlayer;
    private SinglePlayerParameter selectedPlayer;
    private StringSelectionParameter selectedAdvancement;
    private BooleanParameter isValidated;
    private HashMap<String, Advancement> advancements = new HashMap<>();

    private static AdvancementFaker instance;
    public static AdvancementFaker getInstance()
    {
        if (instance == null)
        {
            instance = new AdvancementFaker();
        }

        return instance;
    }

    protected AdvancementFaker()
    {
        Iterator<Advancement> iterator = Bukkit.advancementIterator();
        do {
            Advancement current = iterator.next();
            if (current.getDisplay() == null) continue;
            advancements.put(current.getDisplay().getTitle(), current);
        } while (iterator.hasNext());
    }

    public boolean start() {
        if (running) {
            return false;
        }

        Common.server.getPluginManager().registerEvents(this, Common.plugin);
        Bukkit.getScheduler().runTaskLater(Common.plugin, () -> {
            if (ownerPlayer.getConcernedPlayer() != null) {
                ownerPlayer.getConcernedPlayer().getInventory().addItem(getAdvancementFakerItem());
            }
        } , 5L);


        Bukkit.getLogger().info("Launching AdvancementRemote");
        running = true;

        return true;
    }

    public boolean stop() {
        if (!running) {
            return false;
        }

        PlayerInteractEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        PlayerRespawnEvent.getHandlerList().unregister(this);

        Bukkit.getLogger().info("Stopping AdvancementRemote");
        running = false;

        return true;
    }

    @Override
    public void resetParameters() {
        ownerPlayer = new SinglePlayerParameter("Adv Remote player", "Celui qui recevra la commande");
        selectedPlayer = new SinglePlayerParameter("Target player", "");

        String[] sortedTitles = Arrays.stream(advancements.keySet().toArray(new String[0])).sorted(String::compareTo).collect(Collectors.toList()).toArray(new String[0]);

        selectedAdvancement = new StringSelectionParameter(Material.WRITABLE_BOOK, "Advancement", "", sortedTitles);
        isValidated = new BooleanParameter(Material.LEVER, "GO", "");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean canModifyParametersOnTheFly() { return false; }

    @Override
    public List<AParameter> getParameters() {
        return Arrays.asList(ownerPlayer);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || !ItemHelper.isSameItemKind(event.getItem(), getAdvancementFakerItem())) return;

        isValidated.setValue(0);
        advancementFakerView.open(event.getPlayer(), new SinglePlayerParameterItem(selectedPlayer), new StringSelectionParameterItem(selectedAdvancement), new BooleanParameterItem(isValidated));

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() != advancementFakerView.getView()) return;
        advancementFakerView.close();
        if (isValidated.getValue() == 0) return;

        AdvancementProgress progress = selectedPlayer.getConcernedPlayer().getAdvancementProgress(advancements.get(selectedAdvancement.getSelectedValue()));
        for(String criteria : progress.getRemainingCriteria())
            progress.awardCriteria(criteria);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (ownerPlayer.getConcernedPlayer() == null || ownerPlayer.getConcernedPlayer() != event.getPlayer()) return;

        Bukkit.getScheduler().runTaskLater(Common.plugin, () -> event.getPlayer().getInventory().addItem(getAdvancementFakerItem()), 1L);
    }

    private ItemStack getAdvancementFakerItem() {
        return ItemHelper.generateItem(Material.COMMAND_BLOCK, 1, "Advancement faker", Arrays.asList(), true, Enchantment.ARROW_FIRE, true, true);
    }
}
