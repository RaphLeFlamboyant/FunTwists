package me.flamboyant.small.heroes;

import me.flamboyant.configurable.parameters.AParameter;
import me.flamboyant.utils.ILaunchablePlugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HeroesAdapter implements ILaunchablePlugin {
    private List<ILaunchablePlugin> powerList = Arrays.asList(
            AzadaxPowers.getInstance(),
            BryanPowers.getInstance(),
            DpkPowers.getInstance(),
            FlamboyantPowers.getInstance(),
            MskooPowers.getInstance(),
            OneigPowers.getInstance()
    );

    private static HeroesAdapter instance;
    public static HeroesAdapter getInstance() {
        if (instance == null) {
            instance = new HeroesAdapter();
        }

        return instance;
    }

    private HeroesAdapter()
    {

    }

    @Override
    public void resetParameters() {
        for (ILaunchablePlugin plugin : powerList)
            plugin.resetParameters();
    }

    @Override
    public boolean start() {
        for (ILaunchablePlugin plugin : powerList)
        {
            if (!plugin.isRunning())
                plugin.start();
        }

        return true;
    }

    @Override
    public boolean stop() {
        for (ILaunchablePlugin plugin : powerList)
        {
            if (plugin.isRunning())
                plugin.stop();
        }

        instance = null;

        return true;
    }

    @Override
    public boolean isRunning() {
        return powerList.stream().allMatch(p -> p.isRunning());
    }

    @Override
    public boolean canModifyParametersOnTheFly() { return false; }

    @Override
    public List<AParameter> getParameters() {
        return powerList.stream().flatMap(p -> p.getParameters().stream()).collect(Collectors.toList());
    }
}
