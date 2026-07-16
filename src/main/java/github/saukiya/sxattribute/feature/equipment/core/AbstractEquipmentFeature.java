package github.saukiya.sxattribute.feature.equipment.core;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.FormulaUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 独立装备目录配置基类。
 */
public abstract class AbstractEquipmentFeature implements EquipmentFeature {

    private final String id;
    private YamlConfiguration config;
    private YamlConfiguration gui;
    private YamlConfiguration messages;

    protected AbstractEquipmentFeature(String id) {
        this.id = id;
    }

    @Override
    public final String id() {
        return id;
    }

    @Override
    public final void reload() {
        File file = new File(SXAttribute.getInst().getDataFolder(), "Feature" + File.separator + id + File.separator + "Config.yml");
        if (!file.exists()) SXAttribute.getInst().saveResource("Feature/" + id + "/Config.yml", false);
        config = YamlConfiguration.loadConfiguration(file);
        gui = loadCompanion("Gui.yml");
        messages = loadCompanion("Messages.yml");
        onReload();
    }

    private YamlConfiguration loadCompanion(String name) {
        File file = new File(SXAttribute.getInst().getDataFolder(), "Feature" + File.separator + id + File.separator + name);
        if (!file.exists()) SXAttribute.getInst().saveResource("Feature/" + id + "/" + name, false);
        return YamlConfiguration.loadConfiguration(file);
    }

    protected void onReload() {
    }

    @Override
    public boolean enabled() {
        return config != null && config.getBoolean("Enable", true);
    }

    @Override
    public YamlConfiguration config() {
        return config;
    }

    @Override
    public YamlConfiguration gui() {
        return gui;
    }

    @Override
    public YamlConfiguration messages() {
        return messages;
    }

    protected double formula(Player player, String path, double fallback, Object... pairs) {
        Map<String, Double> variables = new LinkedHashMap<>();
        for (int index = 0; index + 1 < pairs.length; index += 2) {
            if (pairs[index + 1] instanceof Number) {
                variables.put(String.valueOf(pairs[index]), ((Number) pairs[index + 1]).doubleValue());
            }
        }
        return FormulaUtil.eval(player, config.getString(path), variables, fallback);
    }

    protected String color(String value) {
        return value == null ? "" : value.replace('&', '§');
    }

    protected String message(String key, String fallback) {
        return color(messages == null ? fallback : messages.getString(key, fallback));
    }
}
