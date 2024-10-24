package github.saukiya.sxattribute.data.attribute.sub.update;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.UpdateData;
import github.saukiya.sxattribute.data.itemdata.IGenerator;
import github.saukiya.sxattribute.event.SXItemSpawnEvent;
import github.saukiya.sxattribute.util.NbtUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AttackSpeed extends SubAttribute implements Listener {

    private NbtUtil nbtUtil = null;

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
    public void onEnable() {
        nbtUtil = SXAttribute.getNbtUtil();
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof UpdateData && ((UpdateData) eventData).getEntity() instanceof Player) {
            Player player = (Player) ((UpdateData) eventData).getEntity();

            if (SXAttribute.getVersionSplit()[1] > 8) {
                player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(config().getDouble("AttackSpeed.Default") * (100 + values[0]) / 100);
            } else {
                player.setWalkSpeed((float) (values[0] / 500.0D));
            }
        }
    }

    @Override
    public Object getPlaceholder(double[] values, Player player, String string) {
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
        values[0] = Math.min(Math.max(values[0], -100), config().getInt("AttackSpeed.UpperLimit", Integer.MAX_VALUE));
    }


    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * config().getInt("AttackSpeed.CombatPower");
    }

    @EventHandler
    void onItemSpawnEvent(SXItemSpawnEvent event) {
        // 整理攻击速度
        if (event.getItem().getItemMeta().hasLore()) {
            IGenerator ig = event.getIg();
            if (config().getStringList("SupportIGList").contains(ig.getType())) {
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
        return Math.min(config().getDouble("ItemDefaultSpeed." + item.getType().name(), -1), 4);
    }


    /**
     * 设置物品攻击速度
     *
     * @param item ItemStack
     * @return ItemStack
     */
    public ItemStack setAttackSpeed(ItemStack item, double speed) {
        if (item != null && !item.getType().name().equals("AIR")) {
            try {
                Object nmsItem = nbtUtil.getXAsNMSCopay().invoke(nbtUtil.getXCraftItemStack(), item);
                Object compound = ((Boolean) nbtUtil.getXHasTag().invoke(nmsItem)) ? nbtUtil.getXGetTag().invoke(nmsItem) : nbtUtil.getXNBTTagCompound().newInstance();
                Object modifiers = nbtUtil.getXNBTTagList().newInstance();
                Object attackSpeed = nbtUtil.getXNBTTagCompound().newInstance();
                nbtUtil.getXSet().invoke(attackSpeed, "AttributeName", nbtUtil.getXNewNBTTagString().newInstance("generic.attackSpeed"));
                nbtUtil.getXSet().invoke(attackSpeed, "Name", nbtUtil.getXNewNBTTagString().newInstance("AttackSpeed"));
                nbtUtil.getXSet().invoke(attackSpeed, "Amount", nbtUtil.getXNewNBTTagDouble().newInstance(speed));
                nbtUtil.getXSet().invoke(attackSpeed, "Operation", nbtUtil.getXNewNBTTagInt().newInstance(0));
                nbtUtil.getXSet().invoke(attackSpeed, "UUIDLeast", nbtUtil.getXNewNBTTagInt().newInstance(20000));
                nbtUtil.getXSet().invoke(attackSpeed, "UUIDMost", nbtUtil.getXNewNBTTagInt().newInstance(1000));
                nbtUtil.getXSet().invoke(attackSpeed, "Slot", nbtUtil.getXNewNBTTagString().newInstance("mainhand"));
                nbtUtil.getXAdd().invoke(modifiers, attackSpeed);
                nbtUtil.getXSet().invoke(compound, "AttributeModifiers", modifiers);
                nbtUtil.getXSetTag().invoke(nmsItem, compound);
                item.setItemMeta(((ItemStack) nbtUtil.getXAsBukkitCopy().invoke(nbtUtil.getXCraftItemStack(), nmsItem)).getItemMeta());
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        return item;
    }
}
