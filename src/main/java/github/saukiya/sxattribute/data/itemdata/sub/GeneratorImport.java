package github.saukiya.sxattribute.data.itemdata.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.itemdata.IGenerator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Saukiya
 */
public class GeneratorImport implements IGenerator {

    String pathName;

    String key;

    ConfigurationSection config;

    ItemStack item;

    public GeneratorImport() {
    }

    private GeneratorImport(String pathName, String key, ConfigurationSection config) {
        this.pathName = pathName;
        this.key = key;
        this.config = config;
        this.item = config.getItemStack("Item");
    }

    @Override
    public JavaPlugin getPlugin() {
        return SXAttribute.getInst();
    }

    @Override
    public String getType() {
        return "Import";
    }

    @Override
    public IGenerator newGenerator(String pathName, String key, ConfigurationSection config) {
        return new GeneratorImport(pathName, key, config);
    }

    @Override
    public String getPathName() {
        return pathName;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name();
    }

    @Override
    public ConfigurationSection getConfig() {
        return config;
    }

    @Override
    public ItemStack getItem(Player player) {
        return item.clone();
    }

    @Override
    public ConfigurationSection saveItem(ItemStack saveItem, ConfigurationSection config) {
        config.set("Item", saveItem);
        return config;
    }
}
