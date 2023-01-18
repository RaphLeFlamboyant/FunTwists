package me.flamboyant.small;

import me.flamboyant.configurable.parameters.AParameter;
import me.flamboyant.utils.Common;
import me.flamboyant.utils.ILaunchablePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class MangroveChaos implements Listener, ILaunchablePlugin {
    private boolean running;


    private static MangroveChaos instance;
    public static MangroveChaos getInstance()
    {
        if (instance == null)
        {
            instance = new MangroveChaos();
        }

        return instance;
    }

    protected MangroveChaos()
    {

    }

    public boolean start() {
        if (running) {
            return false;
        }

        Common.server.getPluginManager().registerEvents(this, Common.plugin);

        Bukkit.getLogger().info("Launching Wtf10Minutes");
        running = true;

        return true;
    }

    public boolean stop() {
        if (!running) {
            return false;
        }

        PlayerInteractEvent.getHandlerList().unregister(this);

        Bukkit.getLogger().info("Stopping Wtf10Minutes");
        running = false;

        return true;
    }

    @Override
    public void resetParameters() {

    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean canModifyParametersOnTheFly() { return false; }

    @Override
    public List<AParameter> getParameters() {
        return new ArrayList<>();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        if (event.getClickedBlock().getType() == Material.MANGROVE_DOOR)
        {
            Location location = event.getPlayer().getLocation();
            location.getWorld().createExplosion(location, 20f, true);
        }

        if (event.getClickedBlock().getType() == Material.MANGROVE_BUTTON && event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            Bukkit.getScheduler().runTaskLater(Common.plugin, () ->
            {
                Location location = event.getClickedBlock().getLocation();
                location.getWorld().createExplosion(location, 20f, true);
            }, 1 * 60 * 20L);
        }

        if (event.getAction() == Action.PHYSICAL && event.getMaterial() == Material.MANGROVE_DOOR)
        {
            event.getPlayer().setVelocity(new Vector(0, 100, 0));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != Material.MANGROVE_PLANKS) return;

        event.getBlock().setType(Material.DIAMOND_BLOCK);
    }
}
