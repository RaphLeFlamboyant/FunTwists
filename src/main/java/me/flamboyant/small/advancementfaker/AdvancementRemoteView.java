package me.flamboyant.small.advancementfaker;

import me.flamboyant.utils.Common;
import me.flamboyant.configurable.gui.wrapper.items.AParameterItem;
import me.flamboyant.configurable.gui.wrapper.items.BooleanParameterItem;
import me.flamboyant.configurable.gui.wrapper.items.SinglePlayerParameterItem;
import me.flamboyant.configurable.gui.wrapper.items.StringSelectionParameterItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class AdvancementRemoteView implements Listener {
    private static final int inventorySize = 9 * 3;
    private Inventory view;
    private SinglePlayerParameterItem selectedPlayer;
    private StringSelectionParameterItem selectedAdvancement;
    private BooleanParameterItem isValidated;
    private HashMap<ItemStack, AParameterItem> itemsParameter = new HashMap<>();

    public AdvancementRemoteView() {
        view = Bukkit.createInventory(null, inventorySize, getViewId());
    }

    public void open(Player p, SinglePlayerParameterItem selectedPlayer, StringSelectionParameterItem selectedAdvancement, BooleanParameterItem isValidated) {
        itemsParameter.clear();
        this.selectedAdvancement = selectedAdvancement;
        this.selectedPlayer = selectedPlayer;
        this.isValidated = isValidated;
        itemsParameter.put(selectedAdvancement.getItemStack(), selectedAdvancement);
        itemsParameter.put(selectedPlayer.getItemStack(), selectedPlayer);
        itemsParameter.put(isValidated.getItemStack(), isValidated);

        resetView();
        p.openInventory(getView());
        Common.server.getPluginManager().registerEvents(this, Common.plugin);
    }

    public void close() {
        InventoryClickEvent.getHandlerList().unregister(this);
    }

    public void resetView() {
        int index = 12;

        view.clear();
        view.setItem(index, selectedPlayer.getItemStack());
        view.setItem(index + 1, selectedAdvancement.getItemStack());
        view.setItem(index + 3, isValidated.getItemStack());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory != view) return;
        event.setCancelled(true);
        if (event.getSlotType() == InventoryType.SlotType.QUICKBAR) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        ItemStack updatedItem;
        AParameterItem params = itemsParameter.get(clicked);
        if (params == isValidated) {
            isValidated.OnLeftClick(inventory);

            event.getWhoClicked().closeInventory();
        }

        if (event.isRightClick()) updatedItem = params.OnRightClick(inventory);
        else if (event.isLeftClick()) updatedItem = params.OnLeftClick(inventory);
        else return;

        itemsParameter.remove(clicked);
        itemsParameter.put(updatedItem, params);
        event.getClickedInventory().setItem(event.getSlot(), updatedItem);
    }

    private String getViewId() {
        return "Advancement Selection";
    }

    public Inventory getView() {
        return view;
    }
}
