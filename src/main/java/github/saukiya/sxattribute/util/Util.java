package github.saukiya.sxattribute.util;

import github.saukiya.sxattribute.SXAttribute;
import org.bukkit.plugin.java.JavaPlugin;

public class Util {

    private static JavaPlugin plugin = SXAttribute.getInst();

    public static String format(double value) {
        return String.valueOf(Math.round(value * 100) / 100D);
    }

    public static void info(String msg) {
        if (false) {
            plugin.getLogger().info(msg);
        }
    }
}
