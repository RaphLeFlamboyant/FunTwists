package me.flamboyant.small.stuffmirror;

import me.flamboyant.utils.Common;
import me.flamboyant.utils.ItemHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.stream.Collectors;

public class SelectTargetView implements Listener {
    private static final int inventorySize = 9 * 3;
    private static SelectTargetView instance;
    private Inventory view;
    private Player owner;
    private Player selectedTarget;

    protected SelectTargetView() {
    }

    public static SelectTargetView getInstance() {
        if (instance == null) {
            instance = new SelectTargetView();
        }

        return instance;
    }

    public static String getViewID() {
        return "Sélection du joueur";
    }

    public Player getSelectedTarget() { return selectedTarget; }

    public Inventory getViewInstance(Player owner) {
        if (view == null || this.owner != owner) {
            this.owner = owner;
            Inventory myInventory = Bukkit.createInventory(null, inventorySize, getViewID());

            int index = 0;
            List<Player> playerList = Common.server.getOnlinePlayers().stream().filter(p -> p != owner).collect(Collectors.toList());
            myInventory.setItem((inventorySize / 2) - (playerList.size() / 2) + index - 1,
                    ItemHelper.generateItem(Material.BARRIER, 1, "Aucun", Arrays.asList(), false, null, false, false));

            for (Player player : playerList) {
                ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skull = (SkullMeta) playerHead.getItemMeta();
                skull.setOwningPlayer(player);
                skull.setDisplayName(player.getDisplayName());
                playerHead.setItemMeta(skull);

                myInventory.setItem((inventorySize / 2) - (playerList.size() / 2) + index, playerHead);
                index++;
            }

            view = myInventory;
        }

        return view;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory != view) return;
        event.setCancelled(true);
        if (event.getSlotType() == InventoryType.SlotType.QUICKBAR) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir() || (clicked.getType() != Material.PLAYER_HEAD && clicked.getType() != Material.BARRIER)) return;

        if (clicked.getType() == Material.PLAYER_HEAD)
            selectedTarget = Common.server.getPlayer(clicked.getItemMeta().getDisplayName());
        else
            selectedTarget = null;
    }

    public void close() {
        InventoryClickEvent.getHandlerList().unregister(this);
        owner = null;
        selectedTarget = null;
        view.clear();
        view = null;
    }
}
