package github.saukiya.sxattribute.data.itemdata;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Saukiya
 */
public interface IGenerator {

    /**
     * 返回plugin
     *
     * @return ·JavaPlugin
     */
    JavaPlugin getPlugin();

    /**
     * 返回生成器类型
     *
     * @return Type
     */
    String getType();

    /**
     * 实例化物品生成器
     *
     * @param pathName 路径
     * @param key      编号
     * @param config   配置
     * @return ItemGenerator
     */
    IGenerator newGenerator(String pathName, String key, ConfigurationSection config);

    /**
     * 返回路径
     *
     * @return Path
     */
    String getPathName();

    /**
     * 返回物品编号
     *
     * @return Key
     */
    String getKey();

    /**
     * 返回物品展示名
     *
     * @return Name
     */
    String getName();

    /**
     * 返回配置信息
     *
     * @return Config
     */
    ConfigurationSection getConfig();

    /**
     * 获取物品
     *
     * @param player Player
     * @return Item
     */
    ItemStack getItem(Player player);

    /**
     * 保存物品到config中
     * null则不带save功能
     *
     * @param saveItem ItemStack
     * @param config   ConfigurationSection
     * @return
     */
    ConfigurationSection saveItem(ItemStack saveItem, ConfigurationSection config);

}
