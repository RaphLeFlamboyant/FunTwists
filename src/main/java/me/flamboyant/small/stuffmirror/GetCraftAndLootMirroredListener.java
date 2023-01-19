package me.flamboyant.small.stuffmirror;

import me.flamboyant.configurable.parameters.AParameter;
import me.flamboyant.configurable.parameters.SinglePlayerParameter;
import me.flamboyant.utils.Common;
import me.flamboyant.utils.ILaunchablePlugin;
import me.flamboyant.utils.ItemHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GetCraftAndLootMirroredListener implements Listener, ILaunchablePlugin {
    private Player selectedHunter;
    private boolean running;

    private SinglePlayerParameter speedrunnerPlayerParameter;

    private List<AParameter> parameters;

    private static GetCraftAndLootMirroredListener instance;
    public static GetCraftAndLootMirroredListener getInstance()
    {
        if (instance == null)
        {
            instance = new GetCraftAndLootMirroredListener();
        }

        return instance;
    }

    protected GetCraftAndLootMirroredListener()
    {
    }

    @Override
    public boolean start() {
        if (running) {
            return false;
        }

        if (Common.server.getOnlinePlayers().size() < 2) {
            Bukkit.getLogger().info("Not enough players for plugin GetCraftAndLootMirroredListener");
            return false;
        }

        selectedHunter = null;
        giveSpeedrunnerSpecialChest();
        Bukkit.getScheduler().runTaskLater(Common.plugin, () -> Common.server.getPluginManager().registerEvents(this, Common.plugin), 1L);
        running = true;
        return true;
    }

    @Override
    public boolean stop() {
        if (!running) {
            return false;
        }

        PlayerInteractEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        CraftItemEvent.getHandlerList().unregister(this);
        EntityPickupItemEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
        running = false;
        return true;
    }

    @Override
    public void resetParameters() {
        speedrunnerPlayerParameter = new SinglePlayerParameter("Speedrunner", "Joueur qui va recevoir les items copiés");
        parameters = Arrays.asList(speedrunnerPlayerParameter);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean canModifyParametersOnTheFly() { return false; }

    @Override
    public List<AParameter> getParameters() {
        return parameters;
    }

    private void giveSpeedrunnerSpecialChest() {
        speedrunnerPlayerParameter.getConcernedPlayer().getInventory().addItem(getTargetSelectionItem());
    }

    private ItemStack getTargetSelectionItem() {
        if (selectedHunter != null) {
            ItemStack item = ItemHelper.generateItem(Material.PLAYER_HEAD, 1, "Choisir le joueur", Arrays.asList("Joueur sélectionné : " + ChatColor.GREEN + ChatColor.BOLD + selectedHunter.getDisplayName()), true, Enchantment.ARROW_FIRE, true, true);

            SkullMeta skull = (SkullMeta) item.getItemMeta();
            skull.setOwningPlayer(selectedHunter);
            item.setItemMeta(skull);

            return item;
        }

        return ItemHelper.generateItem(Material.BARRIER, 1, "Choisir le joueur", Arrays.asList("Aucun joueur sélectionné"), true, Enchantment.ARROW_FIRE, true, true);
    }

    @EventHandler
    public void onInteractWithChest(PlayerInteractEvent event) {
        Player speedrunner = speedrunnerPlayerParameter.getConcernedPlayer();
        if (event.getPlayer() != speedrunner) return;
        if (!ItemHelper.isExactlySameItemKind(event.getItem(), getTargetSelectionItem())) return;
        event.setCancelled(true);

        Common.server.getPluginManager().registerEvents(SelectTargetView.getInstance(), Common.plugin);
        speedrunner.openInventory(SelectTargetView.getInstance().getViewInstance(speedrunner));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player speedrunner = speedrunnerPlayerParameter.getConcernedPlayer();
        if (event.getPlayer() != speedrunner) return;
        if (event.getInventory() != SelectTargetView.getInstance().getViewInstance(speedrunner)) return;

        selectedHunter = SelectTargetView.getInstance().getSelectedTarget();
        SelectTargetView.getInstance().close();
        int index = speedrunner.getInventory().getHeldItemSlot();
        ItemStack item = speedrunner.getInventory().getItem(index);
        if (selectedHunter == null) {
            item.setType(Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            meta.setLore(Arrays.asList("Aucun joueur sélectionné"));
            item.setItemMeta(meta);
        }
        else {
            item.setType(Material.PLAYER_HEAD);
            SkullMeta skull = (SkullMeta) item.getItemMeta();
            skull.setOwningPlayer(selectedHunter);
            skull.setLore(Arrays.asList("Joueur sélectionné : " + ChatColor.GREEN + ChatColor.BOLD + selectedHunter.getDisplayName()));
            item.setItemMeta(skull);
        }
    }

    @EventHandler
    public void onPlayerCraft(CraftItemEvent event) {
        Player speedrunner = speedrunnerPlayerParameter.getConcernedPlayer();
        if (selectedHunter == null) return;
        if (!event.getWhoClicked().getUniqueId().equals(speedrunner.getUniqueId())
                && !event.getWhoClicked().getUniqueId().equals(selectedHunter.getUniqueId())) return;
        Player target = event.getWhoClicked().getUniqueId().equals(speedrunner.getUniqueId()) ? selectedHunter : speedrunner;
        ItemStack cursorStack = event.getCursor();
        if (!event.isShiftClick() && cursorStack.getType() != Material.AIR && event.getInventory().getResult().getType() != cursorStack.getType())
            return;
        if (!event.isShiftClick() && cursorStack.getType() != Material.AIR && event.getInventory().getResult().getAmount() + cursorStack.getAmount() > cursorStack.getMaxStackSize())
            return;

        int realQuantity = 9999;
        if (event.isShiftClick()) {
            int factor = event.getInventory().getResult().getAmount();
            for (ItemStack item : event.getInventory().getMatrix()) {
                if (item != null) realQuantity = Math.min(realQuantity, item.getAmount() * factor);
            }
            System.out.println("Shift click gave " + realQuantity + " quantity");
        } else {
            realQuantity = event.getInventory().getResult().getAmount();
            System.out.println("Normal click gave " + realQuantity + " quantity");
        }

        // TODO ; gérer le cas inventaire full

        ItemStack itemCopy = new ItemStack(event.getInventory().getResult().getType(), realQuantity);
        addItem(itemCopy, target);
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        Player speedrunner = speedrunnerPlayerParameter.getConcernedPlayer();
        if (selectedHunter == null) return;
        if (!event.getEntity().getUniqueId().equals(speedrunner.getUniqueId())
                && !event.getEntity().getUniqueId().equals(selectedHunter.getUniqueId())) return;
        Player target = event.getEntity().getUniqueId().equals(speedrunner.getUniqueId()) ? selectedHunter : speedrunner;

        ItemStack itemStack = event.getItem().getItemStack();
        ItemStack itemCopy = new ItemStack(itemStack.getType(), itemStack.getAmount());
        addItem(itemCopy, target);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player speedrunner = speedrunnerPlayerParameter.getConcernedPlayer();
        if (selectedHunter == null) return;
        if (event.getInventory() == SelectTargetView.getInstance().getViewInstance(speedrunner)) return;
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() != InventoryType.CHEST) return;
        if (!event.getWhoClicked().getUniqueId().equals(speedrunner.getUniqueId())
                && !event.getWhoClicked().getUniqueId().equals(selectedHunter.getUniqueId())) return;
        if (event.getCurrentItem().getType() == Material.AIR) return;
        if (!event.isShiftClick() && event.getCurrentItem().getType() == event.getCursor().getType()) return;
        Player target = event.getWhoClicked().getUniqueId().equals(speedrunner.getUniqueId()) ? selectedHunter : speedrunner;

        Material material = event.getCurrentItem().getType();
        int quantity = 0;
        if (event.isShiftClick() || (event.isLeftClick() && event.getCursor().getType() == Material.AIR))
            quantity = event.getCurrentItem().getAmount();
        else if (event.isRightClick() && event.getCursor().getType() == Material.AIR)
            quantity = event.getCurrentItem().getAmount() / 2 + (event.getCurrentItem().getAmount() % 1);

        if (quantity == 0) return;

        ItemStack itemCopy = new ItemStack(material, quantity);
        addItem(itemCopy, target);
    }

    private void addItem(ItemStack itemCopy, Player target) {
        HashMap<Integer, ItemStack> abandonnedItems = target.getInventory().addItem(itemCopy);

        for(ItemStack item : abandonnedItems.values())
            target.getWorld().dropItem(target.getLocation(), item);

    }
}