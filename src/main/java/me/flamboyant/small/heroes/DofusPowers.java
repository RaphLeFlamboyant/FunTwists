package  me.flamboyant.small.heroes;

import me.flamboyant.configurable.parameters.AParameter;
import me.flamboyant.configurable.parameters.SinglePlayerParameter;
import me.flamboyant.utils.ChatColorUtils;
import me.flamboyant.utils.Common;
import me.flamboyant.utils.ILaunchablePlugin;
import me.flamboyant.utils.ItemHelper;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DofusPowers implements Listener, ILaunchablePlugin {
    private static final int bramblesCooldownTicks = 10 * 60 * 20;
    private static final int crazyCooldownTicks = 5 * 60 * 20;
    private static final int sacrificedCooldownTicks = 15 * 60 * 20;
    private static final int treeingCooldownTicks = 1 * 60 * 20;
    private static final int absorbCooldownTicks = 15 * 60 * 20;
    private SinglePlayerParameter dofusPlayerParameter = new SinglePlayerParameter("Dofus powers", "A qui les pouvoirs de Dofus ?", true);
    private List<ItemStack> dofusItems = Arrays.asList(getBramblesItem(), getTreeingItem(), getCrazyPuppetItem(), getPuppetAbsorbItem(), getSacrificedPuppetItem());
    private BukkitTask task;
    private HashMap<UUID, Boolean> playerBlocked = new HashMap<>();

    private Entity crazyEntity;
    private Entity sacrificedEntity;
    private int sacrificedTicksRemaining;

    private static DofusPowers instance;
    public static DofusPowers getInstance()
    {
        if (instance == null)
        {
            instance = new DofusPowers();
        }

        return instance;
    }

    protected DofusPowers()
    {
    }

    @Override
    public boolean start() {
        Player dofusPlayer = dofusPlayerParameter.getConcernedPlayer();
        if (dofusPlayer == null) {
            return false;
        }

        Common.server.getPluginManager().registerEvents(this, Common.plugin);
        task = Bukkit.getScheduler().runTaskTimer(Common.plugin, () -> checkPuppets(), 5 * 20, 5 * 20);

        Inventory inv = dofusPlayer.getInventory();
        inv.setItem(4, getBramblesItem());
        inv.setItem(5, getCrazyPuppetItem());
        inv.setItem(6, getSacrificedPuppetItem());
        inv.setItem(7, getTreeingItem());
        inv.setItem(8, getPuppetAbsorbItem());


        dofusPlayer.setCooldown(Material.OAK_SAPLING, 0);
        dofusPlayer.setCooldown(Material.ARMOR_STAND, 0);
        dofusPlayer.setCooldown(Material.GHAST_TEAR, 0);
        dofusPlayer.setCooldown(Material.TNT, 0);
        dofusPlayer.setCooldown(Material.WEEPING_VINES, 0);

        running = true;

        dofusPlayer.sendMessage(ChatColorUtils.feedback("Tu as reçu les pouvoirs de Dofus"));

        return true;
    }

    @Override
    public boolean stop() {
        PlayerRespawnEvent.getHandlerList().unregister(this);
        EntityDeathEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerDropItemEvent.getHandlerList().unregister(this);
        PlayerSwapHandItemsEvent.getHandlerList().unregister(this);
        PlayerInteractAtEntityEvent.getHandlerList().unregister(this);

        Bukkit.getScheduler().cancelTask(task.getTaskId());

        running = false;

        return true;
    }

    @Override
    public void resetParameters() {
        dofusPlayerParameter = new SinglePlayerParameter("Dofus powers", "A qui les pouvoirs de Dofus ?", true);
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
        List<AParameter> parameters = Arrays.asList(dofusPlayerParameter);
        return parameters;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player dofusPlayer = dofusPlayerParameter.getConcernedPlayer();
        if (event.getPlayer() == dofusPlayer) {
            Inventory inv = dofusPlayer.getInventory();
            inv.setItem(4, getBramblesItem());
            inv.setItem(5, getCrazyPuppetItem());
            inv.setItem(6, getSacrificedPuppetItem());
            inv.setItem(7, getTreeingItem());
            inv.setItem(8, getPuppetAbsorbItem());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() != dofusPlayerParameter.getConcernedPlayer()) return;
        if (event.getSlot() >= 4 && event.getSlot() <= 8) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) return;
        Player player = event.getPlayer();
        if (player != dofusPlayerParameter.getConcernedPlayer()) {
            UUID playerId = player.getUniqueId();
            if (!playerBlocked.containsKey(playerId) || !playerBlocked.get(playerId)) return;
            for (int i = 4; i < 9; i++) {
                if (ItemHelper.isExactlySameItemKind(event.getItem(), player.getInventory().getItem(i))) {
                    player.sendMessage("Vous êtes trop engourdi pour utiliser cet objet");
                    event.setCancelled(true);
                }

                return;
            }
        }

        if (ItemHelper.isSameItemKind(event.getItem(), getBramblesItem())) {
            invokeBrambles();
            event.setCancelled(true);
        }
        if (ItemHelper.isSameItemKind(event.getItem(), getCrazyPuppetItem())) {
            invokeCrazy();
            event.setCancelled(true);
        }
        if (ItemHelper.isSameItemKind(event.getItem(), getSacrificedPuppetItem())) {
            invokeSacrificed();
            event.setCancelled(true);
        }
        if (ItemHelper.isSameItemKind(event.getItem(), getTreeingItem())) {
            event.setCancelled(true);
        }
        if (ItemHelper.isSameItemKind(event.getItem(), getSacrificedPuppetItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        if (player != dofusPlayerParameter.getConcernedPlayer()) return;

        if (event.getRightClicked() instanceof Mob
                && ItemHelper.isSameItemKind(player.getInventory().getItem(event.getHand()), getTreeingItem())) {
            treeEntity(event.getRightClicked());
            event.setCancelled(true);
        }
        if (ItemHelper.isSameItemKind(player.getInventory().getItem(event.getHand()), getPuppetAbsorbItem())) {
            applyAbsorbing(event.getRightClicked());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getPlayer() != dofusPlayerParameter.getConcernedPlayer()) return;
        boolean isItem = false;
        for (int i = 0; i < dofusItems.size() && !isItem; i++) {
            isItem |= ItemHelper.isSameItemKind(event.getItemDrop().getItemStack(), dofusItems.get(i));
        }
        if (!isItem) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (event.getPlayer() != dofusPlayerParameter.getConcernedPlayer()) return;
        boolean isItem = false;
        for (int i = 0; i < dofusItems.size() && !isItem; i++) {
            isItem |= ItemHelper.isSameItemKind(event.getOffHandItem(), dofusItems.get(i));
        }
        if (!isItem) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onArmorStandDeath(EntityDeathEvent event) {
        if (event.getEntity().getType() != EntityType.ARMOR_STAND) return;

        if (event.getEntity() == crazyEntity) {
            crazyEntity = null;
            for (Player player : Common.server.getOnlinePlayers()) {
                playerBlocked.put(player.getUniqueId(), false);
            }
        }

        if (event.getEntity() == sacrificedEntity) {
            sacrificedEntity = null;
            sacrificedTicksRemaining = 0;
        }
    }

    private void checkPuppets() {
        if (sacrificedEntity != null && sacrificedTicksRemaining > 0) {
            sacrificedTicksRemaining--;

            if (sacrificedTicksRemaining == 0) {
                Bukkit.broadcastMessage("La Sacrifiée lance Sacrifice");
                sacrificedEntity.getWorld().createExplosion(sacrificedEntity.getLocation(), 5f);
            }
        }

        if (crazyEntity != null) {
            for (Player player : Common.server.getOnlinePlayers()) {
                if (player == dofusPlayerParameter.getConcernedPlayer()) continue;
                Boolean oldState = false;
                if (playerBlocked.containsKey(player.getUniqueId()))
                    oldState = playerBlocked.get(player.getUniqueId());

                Boolean newState = player.getLocation().distance(crazyEntity.getLocation()) < 32;
                playerBlocked.put(player.getUniqueId(), newState);
                if (!oldState && newState) {
                    Bukkit.broadcastMessage("La Folle lance Agacement");
                    Bukkit.broadcastMessage(player.getDisplayName() + " perd 5 PA");
                }
            }
        }
    }

    private void applyAbsorbing(Entity ety) {
        Player dofusPlayer = dofusPlayerParameter.getConcernedPlayer();
        if (dofusPlayer.getCooldown(Material.GHAST_TEAR) > 0) return;

        int cd = absorbCooldownTicks;
        if (ety == crazyEntity || ety == sacrificedEntity) {
            ((ArmorStand) ety).setHealth(0);
            dofusPlayer.setHealth(dofusPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        }
        else if (ety instanceof Mob) {
            ((Mob) ety).damage(6, dofusPlayer);
            dofusPlayer.setHealth(Math.min(dofusPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), dofusPlayer.getHealth() + 3));
            cd = cd / 15;
        }
        else return;

        Bukkit.broadcastMessage(dofusPlayer.getDisplayName() + " lance Sacrifice Poupesque");
        dofusPlayer.setCooldown(Material.GHAST_TEAR, cd);
    }

    private void treeEntity(Entity ety) {
        Player dofusPlayer = dofusPlayerParameter.getConcernedPlayer();
        if (dofusPlayer.getCooldown(Material.OAK_SAPLING) > 0) return;

        Location loc = ety.getLocation();
        ety.remove();
        loc.getWorld().generateTree(loc, Common.rng, TreeType.BIG_TREE);

        Bukkit.broadcastMessage(dofusPlayer.getDisplayName() + " lance Puissance Sylvestre");
        dofusPlayer.setCooldown(Material.OAK_SAPLING, treeingCooldownTicks);
    }

    private void invokeSacrificed() {
        Player dofusPlayer = dofusPlayerParameter.getConcernedPlayer();
        if (dofusPlayer.getCooldown(Material.TNT) > 0) return;

        Block block = dofusPlayer.getTargetBlock(null, 8);
        Location upperLocation = new Location(dofusPlayer.getWorld(), block.getX(), block.getY() + 1, block.getZ());
        if (block == null
                || block.getWorld().getMaxHeight() - 2 < block.getY()
                || block.getWorld().getBlockAt(block.getX(), block.getY() + 1, block.getZ()).getType() != Material.AIR
                || block.getWorld().getBlockAt(block.getX(), block.getY() + 2, block.getZ()).getType() != Material.AIR) {

            dofusPlayer.sendMessage("Le sort doit être lancé vers un bloc qui a deux espace libre au dessus de lui");
            return;
        }

        sacrificedEntity = dofusPlayer.getWorld().spawnEntity(upperLocation, EntityType.ARMOR_STAND);
        sacrificedTicksRemaining = 2;
        sacrificedEntity.setCustomName("La Sacrifiée");

        Bukkit.broadcastMessage(dofusPlayer.getDisplayName() + " lance Invocation La Sacrifiée");
        dofusPlayer.setCooldown(Material.TNT, sacrificedCooldownTicks);
    }

    private void invokeCrazy() {
        Player dofusPlayer = dofusPlayerParameter.getConcernedPlayer();
        if (dofusPlayer.getCooldown(Material.ARMOR_STAND) > 0) return;

        Block block = dofusPlayer.getTargetBlock(null, 16);
        Location upperLocation = new Location(dofusPlayer.getWorld(), block.getX(), block.getY() + 1, block.getZ());
        if (block == null
                || block.getWorld().getMaxHeight() - 2 < block.getY()
                || block.getWorld().getBlockAt(block.getX(), block.getY() + 1, block.getZ()).getType() != Material.AIR
                || block.getWorld().getBlockAt(block.getX(), block.getY() + 2, block.getZ()).getType() != Material.AIR) {

            dofusPlayer.sendMessage("Le sort doit être lancé vers un bloc qui a deux espace libre au dessus de lui");
            return;
        }

        crazyEntity = dofusPlayer.getWorld().spawnEntity(upperLocation, EntityType.ARMOR_STAND);
        crazyEntity.setCustomName("La Folle");

        Bukkit.broadcastMessage(dofusPlayer.getDisplayName() + " lance Invocation La Folle");
        dofusPlayer.setCooldown(Material.ARMOR_STAND, crazyCooldownTicks);
    }

    private void invokeBrambles() {
        Player dofusPlayer = dofusPlayerParameter.getConcernedPlayer();
        if (dofusPlayer.getCooldown(Material.WEEPING_VINES) > 0) return;

        Location loc = dofusPlayer.getLocation();

        Block block = dofusPlayer.getTargetBlock(null, 12);
        if (block == null
                || block.getType() == Material.AIR
                || block.getWorld().getBlockAt(block.getX(), block.getY() + 1, block.getZ()).getType() != Material.AIR) {
            Vector vector = loc.getDirection().normalize().multiply(12);
            block = block.getWorld().getBlockAt(block.getX() + vector.getBlockX(), block.getY() + vector.getBlockY(), block.getZ() + vector.getBlockZ());

            if (!checkBramblesInvokingValidity(block.getLocation(), 3)) {
                dofusPlayer.sendMessage("Le sort doit être lancé vers un bloc plein qui a un espace libre au dessus de lui");
                return;
            }
        }

        loc = block.getLocation();
        invokeBramblesCircle(loc, 3);

        for (Entity ety : loc.getWorld().getNearbyEntities(loc, 4, 4, 4)) {
            if (!(ety instanceof LivingEntity)) continue;
            if (ety.getLocation().distance(loc) > 3) continue;

            ((LivingEntity) ety).damage(4, dofusPlayer);
        }

        Bukkit.broadcastMessage(dofusPlayer.getDisplayName() + " lance Ronces Multiples");
        dofusPlayer.setCooldown(Material.WEEPING_VINES, bramblesCooldownTicks);
    }

    private Boolean checkBramblesInvokingValidity(Location loc, int range) {
        if (loc.getBlockY() < 5) return true;
        if (range == -1) return false;

        Boolean res = checkBramblesInvokingValidity(loc, range - 1);

        World w = loc.getWorld();
        int xOffset = range;
        int zOffset = 0;

        while (xOffset >= 0 && !res) {
            for (int y = -2; y < 3; y++) {
                int yLoc = loc.getBlockY() + y;
                if (yLoc <= 0 || y >= w.getMaxHeight()) continue;
                res |= w.getBlockAt(loc.getBlockX() + xOffset, yLoc, loc.getBlockZ() + zOffset).getType() != Material.AIR;
                res |= w.getBlockAt(loc.getBlockX() - xOffset, yLoc, loc.getBlockZ() - zOffset).getType() != Material.AIR;
                res |= w.getBlockAt(loc.getBlockX() - zOffset, yLoc, loc.getBlockZ() + xOffset).getType() != Material.AIR;
                res |= w.getBlockAt(loc.getBlockX() + zOffset, yLoc, loc.getBlockZ() - xOffset).getType() != Material.AIR;
            }

            xOffset--;
            zOffset++;
        }

        return res;
    }

    private void invokeBramblesCircle(Location loc, int range) {
        if (range == -1) return;
        invokeBramblesCircle(loc, range - 1);

        World w = loc.getWorld();
        int xOffset = range;
        int zOffset = 0;

        while (xOffset >= 0) {
            for (int y = -2; y < 3; y++) {
                int yLoc = loc.getBlockY() + y;
                if (yLoc <= 0 || y >= w.getMaxHeight()) continue;
                placeVines(w, loc.getBlockX() + xOffset, yLoc, loc.getBlockZ() + zOffset);
                placeVines(w, loc.getBlockX() - xOffset, yLoc, loc.getBlockZ() - zOffset);
                placeVines(w, loc.getBlockX() - zOffset, yLoc, loc.getBlockZ() + xOffset);
                placeVines(w, loc.getBlockX() + zOffset, yLoc, loc.getBlockZ() - xOffset);
            }

            xOffset--;
            zOffset++;
        }
    }

    private void placeVines(World w, int x, int y, int z) {
        Block block = w.getBlockAt(x, y, z);
        if (block.getType() == Material.AIR) block.setType(Material.TWISTING_VINES);
    }

    private ItemStack getCrazyPuppetItem() {
        ItemStack res = ItemHelper.generateItem(Material.ARMOR_STAND, 1, "Folle", Arrays.asList("Invoque une poupée qui", "entrave les joueurs proches", "Portée 16"), true, Enchantment.ARROW_FIRE, true, true);
        return res;
    }

    private ItemStack getSacrificedPuppetItem() {
        ItemStack res = ItemHelper.generateItem(Material.TNT, 1, "Sacrifiée", Arrays.asList("Invoque une poupée qui", "explose après quelques temps", "Portée 8"), true, Enchantment.ARROW_FIRE, true, true);
        return res;
    }

    private ItemStack getTreeingItem() {
        ItemStack res = ItemHelper.generateItem(Material.OAK_SAPLING, 1, "Puissance Sylvestre", Arrays.asList("Transforme un mob en arbre"), true, Enchantment.ARROW_FIRE, true, true);
        return res;
    }

    private ItemStack getPuppetAbsorbItem() {
        ItemStack res = ItemHelper.generateItem(Material.GHAST_TEAR, 1, "Sacrifice poupesque", Arrays.asList("Sur poupée : la tue", "et te soigne totalement", "Sur mob : inflige 6", "dégâts et te regen la moitié"), true, Enchantment.ARROW_FIRE, true, true);
        return res;
    }

    private ItemStack getBramblesItem() {
        ItemStack res = ItemHelper.generateItem(Material.WEEPING_VINES, 1, "Ronces multiples", Arrays.asList("Invoque des ronces", "infligeant des dégâts", "a 12 blocs de distance", "et ralentit les cibles", "Portée 12"), true, Enchantment.ARROW_FIRE, true, true);
        return res;
    }
}