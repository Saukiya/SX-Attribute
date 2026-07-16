package github.saukiya.sxattribute.feature.equipment.core;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.MoneyUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GUI 成长操作的通用消耗事务。
 * <p>
 * 先完整校验再扣除；异常时可按快照退回金币、经验、等级与材料，避免部分扣款。
 */
public final class CostTransaction {

    private final Player player;
    private final double money;
    private final int experience;
    private final int levels;
    private final List<ItemStack> removed = new ArrayList<>();
    private boolean charged;

    private CostTransaction(Player player, ConfigurationSection cost) {
        this.player = player;
        this.money = cost == null ? 0D : cost.getDouble("Money", 0D);
        this.experience = cost == null ? 0 : cost.getInt("Experience", 0);
        this.levels = cost == null ? 0 : cost.getInt("Levels", 0);
    }

    public static CostTransaction create(Player player, ConfigurationSection cost) {
        return new CostTransaction(player, cost);
    }

    public boolean charge(ConfigurationSection cost) {
        if (money > 0D && (!SXAttribute.isVault() || !MoneyUtil.has(player, money))) return false;
        if (experience > player.getTotalExperience() || levels > player.getLevel()) return false;
        if (!hasItems(cost)) return false;
        if (money > 0D) MoneyUtil.take(player, money);
        if (experience > 0) player.setTotalExperience(Math.max(0, player.getTotalExperience() - experience));
        if (levels > 0) player.setLevel(Math.max(0, player.getLevel() - levels));
        takeItems(cost);
        charged = true;
        return true;
    }

    public void rollback() {
        if (!charged) return;
        if (money > 0D) MoneyUtil.give(player, money);
        if (experience > 0) player.setTotalExperience(player.getTotalExperience() + experience);
        if (levels > 0) player.setLevel(player.getLevel() + levels);
        for (ItemStack item : removed) {
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(item);
            overflow.values().forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
        }
        charged = false;
    }

    private boolean hasItems(ConfigurationSection cost) {
        if (cost == null) return true;
        for (Map<?, ?> item : cost.getMapList("Items")) {
            Material material = Material.matchMaterial(String.valueOf(item.get("Material")));
            int amount = item.get("Amount") instanceof Number ? ((Number) item.get("Amount")).intValue() : 1;
            if (material == null || count(material, item) < amount) return false;
        }
        return true;
    }

    private void takeItems(ConfigurationSection cost) {
        if (cost == null) return;
        for (Map<?, ?> requirement : cost.getMapList("Items")) {
            Material material = Material.matchMaterial(String.valueOf(requirement.get("Material")));
            int remaining = requirement.get("Amount") instanceof Number ? ((Number) requirement.get("Amount")).intValue() : 1;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null || item.getType() != material || remaining <= 0 || !matches(item, requirement)) continue;
                int take = Math.min(remaining, item.getAmount());
                ItemStack snapshot = item.clone();
                snapshot.setAmount(take);
                removed.add(snapshot);
                item.setAmount(item.getAmount() - take);
                remaining -= take;
            }
        }
    }

    private int count(Material material, Map<?, ?> requirement) {
        int amount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material && matches(item, requirement)) amount += item.getAmount();
        }
        return amount;
    }

    /** 可选 NbtKey/NbtValue 让同材质的普通物品不能冒充成长材料。 */
    private boolean matches(ItemStack item, Map<?, ?> requirement) {
        Object key = requirement.get("NbtKey");
        if (key == null) return true;
        String value = SXAttribute.getNbtUtil().getNBT(item, String.valueOf(key));
        Object expected = requirement.get("NbtValue");
        return expected == null ? value != null : String.valueOf(expected).equals(value);
    }
}
