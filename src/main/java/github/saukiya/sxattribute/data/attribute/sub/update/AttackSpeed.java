package github.saukiya.sxattribute.data.attribute.sub.update;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.UpdateData;
import github.saukiya.sxitem.data.item.IGenerator;
import github.saukiya.sxitem.event.SXItemSpawnEvent;
import github.saukiya.util.nms.ItemUtil;
import github.saukiya.util.nms.NMS;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AttackSpeed extends SubAttribute implements Listener {

    /**
     * double[0] 攻击速度
     */
    public AttackSpeed() {
        super(SXAttribute.getInst(), 1, AttributeType.UPDATE);
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("AttackSpeed.DiscernName", "攻速增幅");
        config.set("AttackSpeed.Default", 3.5);
        config.set("AttackSpeed.CombatPower", 1);
        config.set("SupportIGList", Arrays.asList("SX"));
        config.set("ItemDefaultSpeed.DIAMOND_HOE", 4);
        config.set("ItemDefaultSpeed.IRON_HOE", 3);
        config.set("ItemDefaultSpeed.STONE_HOE", 2);
        config.set("ItemDefaultSpeed.DIAMOND_SWORD", 1.6);
        config.set("ItemDefaultSpeed.IRON_SWORD", 1.6);
        config.set("ItemDefaultSpeed.GOLD_SWORD", 1.6);
        config.set("ItemDefaultSpeed.STONE_SWORD", 1.6);
        config.set("ItemDefaultSpeed.WOOD_SWORD", 1.6);
        config.set("ItemDefaultSpeed.DIAMOND_PICKAXE", 1.2);
        config.set("ItemDefaultSpeed.IRON_PICKAXE", 1.2);
        config.set("ItemDefaultSpeed.GOLD_PICKAXE", 1.2);
        config.set("ItemDefaultSpeed.STONE_PICKAXE", 1.2);
        config.set("ItemDefaultSpeed.WOOD_PICKAXE", 1.2);
        config.set("ItemDefaultSpeed.DIAMOND_SPADE", 1);
        config.set("ItemDefaultSpeed.IRON_SPADE", 1);
        config.set("ItemDefaultSpeed.GOLD_SPADE", 1);
        config.set("ItemDefaultSpeed.STONE_SPADE", 1);
        config.set("ItemDefaultSpeed.WOOD_SPADE", 1);
        config.set("ItemDefaultSpeed.DIAMOND_AXE", 1);
        config.set("ItemDefaultSpeed.GOLD_AXE", 1);
        config.set("ItemDefaultSpeed.WOOD_HOE", 1);
        config.set("ItemDefaultSpeed.GOLD_HOE", 1);
        config.set("ItemDefaultSpeed.IRON_AXE", 0.9);
        config.set("ItemDefaultSpeed.STONE_AXE", 0.8);
        config.set("ItemDefaultSpeed.WOOD_AXE", 0.8);
        return config;
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof UpdateData && ((UpdateData) eventData).getEntity() instanceof Player) {
            Player player = (Player) ((UpdateData) eventData).getEntity();

            if (NMS.compareTo(1,9,0) >= 0) {
                player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(getConfig().getDouble("AttackSpeed.Default") * (100 + values[0]) / 100);
            } else {
                player.setWalkSpeed((float) (values[0] / 500.0D));
            }
        }
    }

    @Override
    public Object getPlaceholder(double[] values, LivingEntity player, String string) {
        return string.equals(getName()) ? values[0] : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList(getName());
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        if (lore.contains(getString("AttackSpeed.DiscernName"))) {
            values[0] += getNumber(lore);
        }
    }

    @Override
    public void correct(double[] values) {
        values[0] = Math.min(Math.max(values[0], -100), getConfig().getInt("AttackSpeed.UpperLimit", Integer.MAX_VALUE));
    }


    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * getConfig().getInt("AttackSpeed.CombatPower");
    }

    @EventHandler
    void onItemSpawnEvent(SXItemSpawnEvent event) {
        // 整理攻击速度
        if (event.getItem().getItemMeta().hasLore()) {
            IGenerator ig = event.getIg();
            if (getConfig().getStringList("SupportIGList").contains(ig.getType())) {
                double speed = ig.getConfig().getDouble("AttackSpeed", getAttackSpeed(event.getItem()));
                if (speed > -1) {
                    setAttackSpeed(event.getItem(), speed - 4);
                }
            }
        }
    }


    /**
     * 获取物品攻击速度
     *
     * @param item ItemStack
     * @return double
     */
    public double getAttackSpeed(ItemStack item) {
        return Math.min(getConfig().getDouble("ItemDefaultSpeed." + item.getType().name(), -1), 4);
    }


    /**
     * 设置物品攻击速度
     *
     * @param item ItemStack
     * @return ItemStack
     */
    public ItemStack setAttackSpeed(ItemStack item, double speed) {
        if (item != null && !item.getType().name().equals("AIR")) {
            ItemUtil.getInst().addAttribute(item, new ItemUtil.AttributeData()
            .setAttrName("GENERIC_ATTACK_SPEED")
            .setAmount(speed)
            .setName(SXAttribute.getInst().getName())
            .setSlot("HAND"));
        }
        return item;
    }
}
