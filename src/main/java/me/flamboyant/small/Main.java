package me.flamboyant.small;

import me.flamboyant.FlamboyantPlugin;
import me.flamboyant.small.commands.CommandsDispatcher;

public class Main extends FlamboyantPlugin {
    @Override
    public void onEnable() {
        super.onEnable();

        CommandsDispatcher commandDispatcher = new CommandsDispatcher();

        getCommand("f_item_faker").setExecutor(commandDispatcher);
        getCommand("f_milk_human").setExecutor(commandDispatcher);
        getCommand("f_summon_ball").setExecutor(commandDispatcher);
        getCommand("f_mangrove_chaos").setExecutor(commandDispatcher);
        getCommand("f_heroes").setExecutor(commandDispatcher);
        getCommand("f_advancement_faker").setExecutor(commandDispatcher);
        getCommand("f_stuff_mirror").setExecutor(commandDispatcher);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
