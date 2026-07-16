package github.saukiya.sxattribute.feature.equipment.socket;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.feature.equipment.core.AbstractEquipmentFeature;
import github.saukiya.sxattribute.feature.equipment.core.FeatureItemState;
import github.saukiya.sxattribute.feature.equipment.core.FeatureResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** 宝石孔位、插入和属性渲染模块。 */
public final class SocketFeature extends AbstractEquipmentFeature {

    public SocketFeature() {
        super("Socket");
    }

    @Override
    public List<String> render(Player player, ItemStack item, YamlConfiguration state) {
        int slots = state.getInt("Slots", config().getInt("DefaultSlots", 0));
        List<String> gems = state.getStringList("Gems");
        List<String> result = new ArrayList<>();
        result.add(color(config().getString("Lore.Slots", "&7宝石孔: {used}/{slots}")
                .replace("{used}", String.valueOf(gems.size())).replace("{slots}", String.valueOf(slots))));
        for (String gemId : gems) {
            ConfigurationSection gem = config().getConfigurationSection("Gems." + gemId);
            if (gem == null) continue;
            result.add(color(config().getString("Lore.Gem", "&7宝石: {gem}").replace("{gem}", gem.getString("Name", gemId))));
            for (String attribute : gem.getStringList("Attributes")) result.add(color(attribute));
        }
        return result;
    }

    @Override
    public FeatureResult apply(Player player, ItemStack item, YamlConfiguration state) {
        int slots = state.getInt("Slots", config().getInt("DefaultSlots", 0));
        List<String> gems = new ArrayList<>(state.getStringList("Gems"));
        if (player.isSneaking() && config().getBoolean("Extraction.Enable", true)) {
            if (gems.isEmpty()) return FeatureResult.unsupported(message("NoGem", "&c没有可拆卸的宝石"));
            String removed = gems.remove(gems.size() - 1);
            state.set("Gems", gems);
            double destroyChance = config().getDouble("Extraction.DestroyChance", 0D);
            if (Math.random() * 100D < destroyChance) {
                return FeatureResult.success(message("Destroyed", "&c宝石拆卸时损毁"));
            }
            ItemStack returned = createGem(removed);
            java.util.Map<Integer, ItemStack> overflow = player.getInventory().addItem(returned);
            overflow.values().forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
            return FeatureResult.success(message("Extracted", "&a已拆卸 {gem}").replace("{gem}", removed));
        }
        if (gems.size() >= slots) return FeatureResult.unsupported(message("NoSlot", "&c没有空余宝石孔"));
        ItemStack gemItem = SXAttribute.isHigherVersion() ? player.getInventory().getItemInOffHand() : null;
        if (gemItem == null) return FeatureResult.unsupported(message("NeedGem", "&c请在副手放置宝石"));
        String gemId = FeatureItemState.read(gemItem, "SocketGem").getString("Id");
        if (gemId == null || !config().isConfigurationSection("Gems." + gemId)) {
            return FeatureResult.unsupported(message("InvalidGem", "&c副手物品不是已注册宝石"));
        }
        gems.add(gemId);
        state.set("Gems", gems);
        if (gemItem.getAmount() <= 1) player.getInventory().setItemInOffHand(null);
        else gemItem.setAmount(gemItem.getAmount() - 1);
        return FeatureResult.success(message("Success", "&a已镶嵌 {gem}").replace("{gem}", gemId));
    }

    private ItemStack createGem(String gemId) {
        org.bukkit.Material material = org.bukkit.Material.matchMaterial(config().getString("Gems." + gemId + ".Material", "EMERALD"));
        ItemStack item = new ItemStack(material == null ? org.bukkit.Material.EMERALD : material);
        YamlConfiguration gemState = new YamlConfiguration();
        gemState.set("Id", gemId);
        FeatureItemState.write(item, "SocketGem", gemState);
        return item;
    }
}
