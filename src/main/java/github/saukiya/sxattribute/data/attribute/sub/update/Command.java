package github.saukiya.sxattribute.data.attribute.sub.update;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.UpdateData;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Saukiya
 */
public class Command extends SubAttribute implements Listener {

    private CommandRunnable[] commandRunnables;

    private CommandSender sxSender = new SXSender();

    public Command() {
        super(SXAttribute.getInst(), 0, AttributeType.UPDATE);
    }

    @EventHandler
    void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (CommandRunnable runnable : commandRunnables) {
            if (runnable.players.contains(player.getName())) {
                runnable.disable(player);
            }
        }
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("List.Tick1.DiscernName", "驾驭飞行");
        config.set("List.Tick1.Enabled", Arrays.asList("fly %player% on"));
        config.set("List.Tick1.Disable", Arrays.asList("fly %player% off"));
        config.set("List.Tick1.Continued", Arrays.asList("particle smoke %player_x% %player_y% %player_z% 0.5 0.5 0.5 0 20 normal %player%"));
        config.set("List.Tick2.DiscernName", "速度 II");
        config.set("List.Tick2.Enabled", Arrays.asList("effect %player% minecraft:speed 5 1"));
        config.set("List.Tick2.Disable", Arrays.asList("effect %player% minecraft:speed 0 0"));
        config.set("List.Tick2.Continued", Arrays.asList("delay 40", "effect %player% minecraft:speed 5 1"));
        config.set("List.Tick2.CombatPower", 20);
        return config;
    }

    @Override
    public void onEnable() {
        List<CommandRunnable> list = new ArrayList<>();
        for (String key : getConfig().getConfigurationSection("List").getKeys(false)) {
            list.add(new CommandRunnable(key));
        }
        commandRunnables = list.toArray(new CommandRunnable[0]);
        setLength(commandRunnables.length);
    }

    @Override
    public void onReLoad() {
        for (CommandRunnable commandRunnable : commandRunnables) {
            commandRunnable.load();
        }
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof UpdateData && ((UpdateData) eventData).getEntity() instanceof Player) {
            Player player = (Player) ((UpdateData) eventData).getEntity();
            for (int i = 0; i < commandRunnables.length; i++) {
                CommandRunnable runnable = commandRunnables[i];
                if (runnable.players.contains(player.getName())) {
                    if (values[i] == 0) {
                        runnable.disable(player);
                    }
                } else {
                    if (values[i] > 0) {
                        runnable.enabled(player);
                    }
                }
            }
        }
    }

    @Override
    public Object getPlaceholder(double[] values, Player player, String string) {
        for (int i = 0; i < commandRunnables.length; i++) {
            if (string.equals(commandRunnables[i].name)) {
                return values[i];
            }
        }
        return null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.stream(commandRunnables).map(runnable -> runnable.name).collect(Collectors.toList());
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        for (int i = 0; i < commandRunnables.length; i++) {
            if (lore.contains(commandRunnables[i].discernName)) {
                values[i] += 1;
            }
        }
    }

    @Override
    public double calculationCombatPower(double[] values) {
        int value = 0;
        for (int i = 0; i < commandRunnables.length; i++) {
            if (values[i] > 0) {
                value += commandRunnables[i].combatPower;
            }
        }
        return value;
    }

    public class CommandRunnable {

        String name;

        String discernName;

        List<String> enabledCommands;

        List<String> disableCommands;

        List<String> continuedCommands;

        // TODO 预设
//        List<String> disableWorldList;

        int combatPower;

        List<String> players = new ArrayList<>();

        public CommandRunnable(String name) {
            this.name = name;
            load();
            run();
        }

        public void load() {
            this.discernName = getString("List." + name + ".DiscernName");
            this.enabledCommands = getConfig().getStringList("List." + name + ".Enabled");
            this.disableCommands = getConfig().getStringList("List." + name + ".Disable");
            this.continuedCommands = getConfig().getStringList("List." + name + ".Continued");
//            this.disableWorldList = getConfig().getStringList("List." + name + ".DisableWorldList");
            this.combatPower = getConfig().getInt("List." + name + ".CombatPower");
        }

        public void enabled(Player player) {
            players.add(player.getName());
            runCommand(player, enabledCommands);
        }

        public void disable(Player player) {
            players.remove(player.getName());
            runCommand(player, disableCommands);
        }

        public void runCommand(Player player, List<String> commands) {
            int delay = 0;
            for (String command : commands) {
                if (command.startsWith("delay ")) {
                    delay = Integer.parseInt(command.substring(6));
                } else {
                    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> Bukkit.dispatchCommand(sxSender, (SXAttribute.isPlaceholder() ? PlaceholderAPI.setPlaceholders(player, command) : command).replace("%player%", player.getName())), delay);
                }
            }
        }

        public void run() {
            int delay = 0;
            for (String command : continuedCommands) {
                if (command.startsWith("delay ")) {
                    delay = Integer.parseInt(command.substring(6));
                } else {
                    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                        for (String playerName : players) {
                            Player player = Bukkit.getPlayerExact(playerName);
                            if (player != null) {
                                Bukkit.dispatchCommand(sxSender, (SXAttribute.isPlaceholder() ? PlaceholderAPI.setPlaceholders(player, command) : command).replace("%player%", player.getName()));
                            }
                        }
                    }, delay);
                }
            }
            Bukkit.getScheduler().runTaskLater(getPlugin(), this::run, delay == 0 ? 20 : delay);
        }
    }

}
