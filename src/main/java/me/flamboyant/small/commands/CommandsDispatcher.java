package me.flamboyant.small.commands;

import me.flamboyant.configurable.gui.ConfigurablePluginListener;
import me.flamboyant.small.ItemFaker;
import me.flamboyant.small.MilkingFriends;
import me.flamboyant.small.MobSummoningBall;
import me.flamboyant.small.MangroveChaos;
import me.flamboyant.small.advancementfaker.AdvancementFaker;
import me.flamboyant.small.heroes.HeroesAdapter;
import me.flamboyant.small.stuffmirror.GetCraftAndLootMirroredListener;
import me.flamboyant.utils.ILaunchablePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandsDispatcher implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
        if(sender instanceof Player)
        {
            Player commandSender = (Player) sender;
            ILaunchablePlugin pluginToLaunch = null;
            switch (cmd.getName())
            {
                case "f_item_faker":
                    pluginToLaunch = ItemFaker.getInstance();
                    break;
                case "f_milk_human":
                    pluginToLaunch = MilkingFriends.getInstance();
                    break;
                case "f_summon_ball":
                    pluginToLaunch = MobSummoningBall.getInstance();
                    break;
                case "f_mangrove_chaos":
                    pluginToLaunch = MangroveChaos.getInstance();
                    break;
                case "f_heroes":
                    pluginToLaunch = HeroesAdapter.getInstance();
                    break;
                case "f_advancement_faker":
                    pluginToLaunch = AdvancementFaker.getInstance();
                    break;
                case "f_stuff_mirror":
                    pluginToLaunch = GetCraftAndLootMirroredListener.getInstance();
                    break;
                default :
                    break;
            }
            if (pluginToLaunch != null) handleTwist(commandSender, pluginToLaunch);
            return true;
        }
        return false;
    }

    private void handleTwist(Player sender, ILaunchablePlugin twist) {
        if (twist.isRunning()) {
            sender.sendMessage(ChatColor.RED + "Plugin stopped");
            twist.stop();
            return;
        }

        twist.resetParameters();

        if (!ConfigurablePluginListener.getInstance().isLaunched())
            ConfigurablePluginListener.getInstance().launch(twist, sender);

        sender.sendMessage("Plugin started");
    }
}
