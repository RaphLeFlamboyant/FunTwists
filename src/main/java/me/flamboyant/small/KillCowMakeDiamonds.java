package me.flamboyant.small;

import me.flamboyant.configurable.parameters.AParameter;
import me.flamboyant.utils.Common;
import me.flamboyant.utils.ILaunchablePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KillCowMakeDiamonds implements Listener, ILaunchablePlugin {
    private boolean running;
    private static KillCowMakeDiamonds instance;
    public static KillCowMakeDiamonds getInstance()
    {
        if (instance == null)
        {
            instance = new KillCowMakeDiamonds();
        }

        return instance;
    }

    protected KillCowMakeDiamonds()
    {
    }

    public boolean start() {
        if (running) {
            return false;
        }

        Common.server.getPluginManager().registerEvents(this, Common.plugin);

        Bukkit.getLogger().info("Launching KillCowMakeDiamonds");
        running = true;

        return true;
    }

    public boolean stop() {
        if (!running) {
            return false;
        }

        EntityDeathEvent.getHandlerList().unregister(this);

        Bukkit.getLogger().info("Stopping KillCowMakeDiamonds");
        running = false;

        return true;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean canModifyParametersOnTheFly() { return true; }

    @Override
    public void resetParameters() {

    }

    @Override
    public List<AParameter> getParameters() {
        return new ArrayList<>();
    }

    @EventHandler
    public void onProjectileHitEvent(EntityDeathEvent event) {
        if (event.getEntity().getType() != EntityType.COW) return;

        ItemStack diamond = new ItemStack(Material.DIAMOND);
        event.getDrops().add(diamond);
    }
}
