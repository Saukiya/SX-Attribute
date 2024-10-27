package github.saukiya.sxattribute.util;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import lombok.AllArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Placeholders {

    static Map<UUID, SXAttributeData> dataMap = new HashMap<>();

    static BukkitTask task;

    public Placeholders() {
        if (task != null) {
            Bukkit.getScheduler().cancelTask(task.getTaskId());
        }
        task = Bukkit.getScheduler().runTaskTimer(SXAttribute.getInst(), () -> dataMap.clear(), 20, 20);
        Placeholder.register(SXAttribute.getInst(), "sx");
    }

    public static String onPlaceholderRequest(Player player, String string, SXAttributeData attributeData) {
        if (string.equals("Money") && SXAttribute.isVault())
            return SXAttribute.getDf().format(MoneyUtil.get(player));
        if (string.equals("CombatPower")) return SXAttribute.getDf().format(attributeData.getCombatPower());
        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            Object obj = attribute.getPlaceholder(attributeData.getValues(attribute), player, string);
            if (obj != null) {
                return obj instanceof Double ? SXAttribute.getDf().format(obj) : obj.toString();
            }
        }
        return "§cN/A - " + string;
    }

    @AllArgsConstructor
    static class Placeholder extends PlaceholderExpansion {

        final JavaPlugin plugin;
        final String identifier;

        public static void register(JavaPlugin plugin, String identifier) {
            new Placeholder(plugin, identifier);
        }

        @Nonnull
        @Override
        public String getIdentifier() {
            return identifier;
        }

        @Override
        public String getPlugin() {
            return plugin.getName();
        }

        @Nonnull
        @Override
        public String getAuthor() {
            return plugin.getDescription().getAuthors().toString();
        }

        @Nonnull
        @Override
        public String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public String onPlaceholderRequest(Player player, String string) {
            return Placeholders.onPlaceholderRequest(player, string, dataMap.computeIfAbsent(player.getUniqueId(), k -> SXAttribute.getAttributeManager().getEntityData(player)));
        }
    }
}
