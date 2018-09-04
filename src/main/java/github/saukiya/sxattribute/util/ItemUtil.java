package github.saukiya.sxattribute.util;

import github.saukiya.sxattribute.SXAttribute;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Saukiya
 */

public class ItemUtil {

    private Class<?> xCraftItemStack;
    private Class<?> xNBTTagCompound;
    private Class<?> xNBTTagList;
    private Constructor<?> xNewNBTTagString;
    private Constructor<?> xNewNBTTagDouble;
    private Constructor<?> xNewNBTTagInt;

    private Method xAsNMSCopay;
    private Method xGetTag;
    private Method xHasTag;
    private Method xSet;
    private Method xAdd;
    private Method xRemove;
    private Method xSetTag;
    private Method xAsBukkitCopy;

    private Method xHasKey;
    private Method xGet;
    private Method xGetString;
    private Method xGetListString;
    private Method xSize;

    private SXAttribute plugin;

    /**
     * 加载NBT反射类
     * 此类所附加的nbt均带有插件名
     *
     * @param plugin SXAttribute
     * @throws NoSuchMethodException  NoSuchMethodException
     * @throws ClassNotFoundException ClassNotFoundException
     */
    public ItemUtil(SXAttribute plugin) throws NoSuchMethodException, ClassNotFoundException {
        this.plugin = plugin;
        String packet = Bukkit.getServer().getClass().getPackage().getName();
        String nmsName = "net.minecraft.server." + packet.substring(packet.lastIndexOf('.') + 1);
        xCraftItemStack = Class.forName(packet + ".inventory.CraftItemStack");
        Class<?> xNMSItemStack = Class.forName(nmsName + ".ItemStack");
        xNBTTagCompound = Class.forName(nmsName + ".NBTTagCompound");
        Class<?> xNBTTagString = Class.forName(nmsName + ".NBTTagString");
        Class<?> xNBTTagDouble = Class.forName(nmsName + ".NBTTagDouble");
        Class<?> xNBTTagInt = Class.forName(nmsName + ".NBTTagInt");
        xNBTTagList = Class.forName(nmsName + ".NBTTagList");
        Class<?> xNBTBase = Class.forName(nmsName + ".NBTBase");
        xNewNBTTagString = xNBTTagString.getConstructor(String.class);
        xNewNBTTagDouble = xNBTTagDouble.getConstructor(double.class);
        xNewNBTTagInt = xNBTTagInt.getConstructor(int.class);

        xAsNMSCopay = xCraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
        xGetTag = xNMSItemStack.getDeclaredMethod("getTag");
        xHasTag = xNMSItemStack.getDeclaredMethod("hasTag");
        xSet = xNBTTagCompound.getDeclaredMethod("set", String.class, xNBTBase);
        xAdd = xNBTTagList.getDeclaredMethod("add", xNBTBase);
        xRemove = xNBTTagCompound.getDeclaredMethod("remove", String.class);
        xSetTag = xNMSItemStack.getDeclaredMethod("setTag", xNBTTagCompound);
        xAsBukkitCopy = xCraftItemStack.getDeclaredMethod("asBukkitCopy", xNMSItemStack);

        xHasKey = xNBTTagCompound.getDeclaredMethod("hasKey", String.class);
        xGet = xNBTTagCompound.getDeclaredMethod("get", String.class);
        xGetString = xNBTTagCompound.getDeclaredMethod("getString", String.class);
        xGetListString = xNBTTagList.getDeclaredMethod("getString", int.class);
        xSize = xNBTTagList.getDeclaredMethod("size");
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Load ItemUtil! ");
    }

    /**
     * 清除物品标签
     *
     * @param player Player
     */
    @SuppressWarnings("deprecation")
    public void clearAttribute(Player player) {
        EntityEquipment eq = player.getEquipment();
        for (ItemStack item : eq.getArmorContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                if (item.getType().getMaxDurability() != 0) {
                    clearAttribute(item);
                }
            }
        }
        if (SXAttribute.getVersionSplit()[1] >= 9) {
            ItemStack mainItem = eq.getItemInMainHand();
            if (mainItem != null && mainItem.hasItemMeta() && mainItem.getItemMeta().hasLore()) {
                if (mainItem.getType().getMaxDurability() != 0) {
                    clearAttribute(mainItem);
                }
            }
            ItemStack offItem = eq.getItemInOffHand();
            if (offItem != null && offItem.hasItemMeta() && offItem.getItemMeta().hasLore()) {
                if (offItem.getType().getMaxDurability() != 0) {
                    clearAttribute(offItem);
                }
            }
        } else {
            ItemStack mainItem = eq.getItemInHand();
            if (mainItem != null && mainItem.hasItemMeta() && mainItem.getItemMeta().hasLore()) {
                if (mainItem.getType().getMaxDurability() != 0) {
                    clearAttribute(mainItem);
                }
            }
        }
    }

    /**
     * 获取物品攻击速度
     *
     * @param item     ItemStack
     * @param addSpeed double[]
     * @return double
     */
    private double getAttackSpeed(ItemStack item, double... addSpeed) {
        double attackSpeed = -0.30D;
        String itemType = item.getType().name();
        if (itemType.contains("_PICKAXE")) {
            attackSpeed = -0.70D;
        } else if (itemType.contains("_AXE")) {
            attackSpeed = -0.80D;
        } else if (itemType.contains("_HOE")) {
            attackSpeed = -0.50D;
        } else if (itemType.contains("_SPADE")) {
            attackSpeed = -0.77D;
        } else if (itemType.contains("_SWORD")) {
            attackSpeed = -0.60D;
        }
        if (addSpeed.length > 0) {
            attackSpeed += addSpeed[0] / 500D;
        }
        return attackSpeed <= -1D ? -0.99D : attackSpeed;
    }

    /**
     * 设置物品攻击速度
     *
     * @param item  ItemStack
     * @param speed double[]
     * @return ItemStack
     */
    public ItemStack setAttackSpeed(ItemStack item, double... speed) {
        if (item != null && !item.getType().name().equals("AIR")) {
            try {
                Object nmsItem = xAsNMSCopay.invoke(xCraftItemStack, setNBT(item, "Clear", "yes" + Config.isDamageGauges()));
                Object compound = ((Boolean) xHasTag.invoke(nmsItem)) ? xGetTag.invoke(nmsItem) : xNBTTagCompound.newInstance();
                Object modifiers = xNBTTagList.newInstance();
                Object attackSpeed = xNBTTagCompound.newInstance();
                xSet.invoke(attackSpeed, "AttributeName", xNewNBTTagString.newInstance("generic.attackSpeed"));
                xSet.invoke(attackSpeed, "Name", xNewNBTTagString.newInstance("AttackSpeed"));
                xSet.invoke(attackSpeed, "Amount", xNewNBTTagDouble.newInstance(getAttackSpeed(item, speed)));
                xSet.invoke(attackSpeed, "Operation", xNewNBTTagInt.newInstance(1));
                xSet.invoke(attackSpeed, "UUIDLeast", xNewNBTTagInt.newInstance(20000));
                xSet.invoke(attackSpeed, "UUIDMost", xNewNBTTagInt.newInstance(1000));
                xSet.invoke(attackSpeed, "Slot", xNewNBTTagString.newInstance("mainhand"));
                xAdd.invoke(modifiers, attackSpeed);
                xSet.invoke(compound, "AttributeModifiers", modifiers);
                xSetTag.invoke(nmsItem, compound);
                ItemMeta meta = ((ItemStack) xAsBukkitCopy.invoke(xCraftItemStack, nmsItem)).getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                item.setItemMeta(meta);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }

        }
        return item;
    }

    /**
     * 清除物品默认属性标签
     *
     * @param item ItemStack
     */
    public void clearAttribute(ItemStack item) {
        if (item != null && (!isNBT(item, "Clear") || !Objects.equals(getNBT(item, "Clear"), "yes" + Config.isDamageGauges()))) {
            try {
                Object nmsItem = xAsNMSCopay.invoke(xCraftItemStack, setNBT(item, "Clear", "yes" + Config.isDamageGauges()));
                Object compound = ((Boolean) xHasTag.invoke(nmsItem)) ? xGetTag.invoke(nmsItem) : xNBTTagCompound.newInstance();
                Object modifiers = xNBTTagList.newInstance();
                xSet.invoke(compound, "AttributeModifiers", modifiers);
                xSetTag.invoke(nmsItem, compound);
                ItemMeta meta = ((ItemStack) xAsBukkitCopy.invoke(xCraftItemStack, nmsItem)).getItemMeta();
                if (Config.isDamageGauges()) {
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                }
                item.setItemMeta(meta);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 回复物品默认标签
     *
     * @param player Player
     */
    @SuppressWarnings("deprecation")
    public void removeAttribute(Player player) {
        EntityEquipment eq = player.getEquipment();
        for (ItemStack item : eq.getArmorContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                if (item.getType().getMaxDurability() != 0) {
                    removeAttribute(item);
                }
            }
        }
        if (SXAttribute.getVersionSplit()[1] >= 9) {
            ItemStack mainItem = eq.getItemInMainHand();
            if (mainItem != null && mainItem.hasItemMeta() && mainItem.getItemMeta().hasLore()) {
                if (mainItem.getType().getMaxDurability() != 0) {
                    removeAttribute(mainItem);
                }
            }
            ItemStack offItem = eq.getItemInOffHand();
            if (offItem != null && offItem.hasItemMeta() && offItem.getItemMeta().hasLore()) {
                if (offItem.getType().getMaxDurability() != 0) {
                    removeAttribute(offItem);
                }
            }
        } else {
            ItemStack mainItem = eq.getItemInHand();
            if (mainItem != null && mainItem.hasItemMeta() && mainItem.getItemMeta().hasLore()) {
                if (mainItem.getType().getMaxDurability() != 0) {
                    removeAttribute(mainItem);
                }
            }
        }
    }

    /**
     * 恢复物品默认属性标签
     *
     * @param item ItemStack
     */
    private void removeAttribute(ItemStack item) {
        if (item != null && (!isNBT(item, "Clear") || !Objects.equals(getNBT(item, "Clear"), "no"))) {
            try {
                Object nmsItem = xAsNMSCopay.invoke(xCraftItemStack, setNBT(item, "Clear", "no"));
                Object compound = ((Boolean) xHasTag.invoke(nmsItem)) ? xGetTag.invoke(nmsItem) : xNBTTagCompound.newInstance();
                xRemove.invoke(compound, "AttributeModifiers");
                xSetTag.invoke(nmsItem, compound);
                item.setItemMeta(((ItemStack) xAsBukkitCopy.invoke(xCraftItemStack, nmsItem)).getItemMeta());
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }
            ItemMeta meta = item.getItemMeta();
            meta.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
    }

    /**
     * 获取全部NBT数据
     *
     * @param item ItemStack
     * @return String
     */
    public String getAllNBT(ItemStack item) {
        try {
            Object nmsItem = xAsNMSCopay.invoke(xCraftItemStack, item);
            Object itemTag = ((Boolean) xHasTag.invoke(nmsItem)) ? xGetTag.invoke(nmsItem) : xNBTTagCompound.newInstance();
            return "[" + item.getTypeId() + ":" + item.getDurability() + "-" + item.hashCode() + "]>" + itemTag.toString();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
            return null;
        }
    }

    /**
     * 设置物品NBT数据
     *
     * @param item  ItemStack
     * @param key   String
     * @param value String
     * @return ItemStack
     */
    public ItemStack setNBT(ItemStack item, String key, String value) {
        try {
            Object nmsItem = xAsNMSCopay.invoke(xCraftItemStack, item);
            Object itemTag = ((Boolean) xHasTag.invoke(nmsItem)) ? xGetTag.invoke(nmsItem) : xNBTTagCompound.newInstance();
            Object tagString = xNewNBTTagString.newInstance(value);
            xSet.invoke(itemTag, plugin.getName() + "-" + key, tagString);
            xSetTag.invoke(nmsItem, itemTag);
            item = (ItemStack) xAsBukkitCopy.invoke(xCraftItemStack, nmsItem);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return item;
    }

    /**
     * 设置物品NBT数据 List 会被设置ItemMeta
     *
     * @param item ItemStack
     * @param key  String
     * @param list List
     * @return ItemStack
     */
    public ItemStack setNBTList(ItemStack item, String key, List<String> list) {
        try {
            Object nmsItem = xAsNMSCopay.invoke(xCraftItemStack, item);
            Object itemTag = ((Boolean) xHasTag.invoke(nmsItem)) ? xGetTag.invoke(nmsItem) : xNBTTagCompound.newInstance();
            Object tagList = xNBTTagList.newInstance();
            for (String str : list) {
                xAdd.invoke(tagList, xNewNBTTagString.newInstance(str));
            }
            xSet.invoke(itemTag, plugin.getName() + "-" + key, tagList);
            xSetTag.invoke(nmsItem, itemTag);
            item.setItemMeta(((ItemStack) xAsBukkitCopy.invoke(xCraftItemStack, nmsItem)).getItemMeta());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return item;
    }

    /**
     * 获取物品NBT数据
     *
     * @param item ItemStack
     * @param key  String
     * @return String
     */
    public String getNBT(ItemStack item, String key) {
        try {
            Object nmsItem = xAsNMSCopay.invoke(xCraftItemStack, item);
            Object itemTag = ((Boolean) xHasTag.invoke(nmsItem)) ? xGetTag.invoke(nmsItem) : xNBTTagCompound.newInstance();
            if ((boolean) xHasKey.invoke(itemTag, plugin.getName() + "-" + key))
                return (String) xGetString.invoke(itemTag, plugin.getName() + "-" + key);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 设置物品NBT数据 List
     *
     * @param item ItemStack
     * @param key  String
     * @return List
     */
    public List<String> getNBTList(ItemStack item, String key) {
        List<String> list = new ArrayList<>();
        try {
            Object nmsItem = xAsNMSCopay.invoke(xCraftItemStack, item);
            Object itemTag = ((Boolean) xHasTag.invoke(nmsItem)) ? xGetTag.invoke(nmsItem) : xNBTTagCompound.newInstance();
            Object tagList = (Boolean) xHasKey.invoke(itemTag, plugin.getName() + "-" + key) ? xGet.invoke(itemTag, plugin.getName() + "-" + key) : xNBTTagList.newInstance();
            for (int i = 0; i < (Integer) xSize.invoke(tagList); i++) {
                list.add((String) xGetListString.invoke(tagList, i));
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 判断是否有物品NBT数据
     *
     * @param item ItemStack
     * @param key  String
     * @return Boolean
     */
    public Boolean isNBT(ItemStack item, String key) {
        try {
            Object nmsItem = xAsNMSCopay.invoke(xCraftItemStack, item);
            Object itemTag = ((Boolean) xHasTag.invoke(nmsItem)) ? xGetTag.invoke(nmsItem) : xNBTTagCompound.newInstance();
            if ((boolean) xHasKey.invoke(itemTag, plugin.getName() + "-" + key)) return true;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return false;
    }
}
