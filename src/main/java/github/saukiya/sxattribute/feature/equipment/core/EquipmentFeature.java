package github.saukiya.sxattribute.feature.equipment.core;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * 独立装备功能协议。每个实现拥有独立目录、配置、开关、Lore 与状态转换。
 */
public interface EquipmentFeature {

    String id();

    void reload();

    boolean enabled();

    YamlConfiguration config();

    YamlConfiguration gui();

    YamlConfiguration messages();

    List<String> render(Player player, ItemStack item, YamlConfiguration state);

    FeatureResult apply(Player player, ItemStack item, YamlConfiguration state);
}
