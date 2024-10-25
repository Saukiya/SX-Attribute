package github.saukiya.sxattribute.data.itemdata.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.RandomStringManager;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.data.itemdata.IGenerator;
import github.saukiya.sxattribute.data.itemdata.IUpdate;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxitem.SXItem;
import github.saukiya.sxitem.data.item.ItemManager;
import github.saukiya.util.nms.ItemUtil;
import github.saukiya.util.nms.NbtUtil;
import lombok.val;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Saukiya
 */
public class GeneratorSX implements IGenerator, IUpdate {

    String pathName;

    String key;

    ConfigurationSection config;

    String displayName;

    List<String> ids;

    List<String> loreList;

    List<String> enchantList;

    List<String> itemFlagList;

    boolean unbreakable;

    Color color;

    String skullName;

    int hashCode;

    boolean update;

    public GeneratorSX() {
    }

    private GeneratorSX(String pathName, String key, ConfigurationSection config) {
        this.pathName = pathName;
        this.key = key;
        this.config = config;
        this.displayName = config.getString("Name");
        this.ids = config.isList("ID") ? config.getStringList("ID") : Collections.singletonList(config.getString(".ID"));
        this.loreList = config.getStringList("Lore");
        this.enchantList = config.getStringList("EnchantList");
        this.itemFlagList = config.getStringList("ItemFlagList");
        this.unbreakable = config.getBoolean("Unbreakable");
        this.color = config.isString("Color") ? Color.fromRGB(Integer.parseInt(config.getString("Color"), 16)) : null;
        this.skullName = config.getString("SkullName");
        this.hashCode = config.getValues(true).hashCode();
        this.update = config.getBoolean("Update");
    }

    @Override
    public JavaPlugin getPlugin() {
        return SXAttribute.getInst();
    }

    @Override
    public String getType() {
        return "SX";
    }

    @Override
    public IGenerator newGenerator(String pathName, String key, ConfigurationSection config) {
        return new GeneratorSX(pathName, key, config);
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
        return SXAttribute.getRandomStringManager().getInstSpace().replace(displayName);
    }

    @Override
    public ConfigurationSection getConfig() {
        return config;
    }

    @Override
    public ItemStack getItem(Player player) {
        val space = new RandomStringManager.Space(); // LockMap 在里面呢
        String displayName = SXAttribute.getRandomStringManager().processRandomString(this.displayName, space);
        String id = SXAttribute.getRandomStringManager().processRandomString(ids.get(SXAttribute.getRandom().nextInt(ids.size())), space);
        List<String> loreList = SXAttribute.getRandomStringManager().processRandomString(this.loreList, space);

        List<String> enchantList = new ArrayList<>();
        for (String enchant : SXAttribute.getRandomStringManager().processRandomString(this.enchantList, space)) {
            enchant = enchant.replace("/n", "\n");
            enchantList.addAll(Arrays.asList(enchant.split("\n")));
        }

        ItemStack item = getItemStack(displayName, id, loreList, enchantList, itemFlagList, unbreakable, color, skullName);
        if (!space.getLockMap().isEmpty()) {
            val wrapper = NbtUtil.getInst().getItemTagWrapper(item);
            List<String> list = new ArrayList<>();
            for (Map.Entry<String, String> entry : space.getLockMap().entrySet()) {
                list.add(entry.getKey() + "§e§k|§e§r" + entry.getValue());// old
                wrapper.set(SXItem.getInst().getName() + ".Lock." + entry.getKey(), entry.getValue()); // new
            }
            wrapper.set(SXAttribute.getInst().getName() + "。Lock", list);
            wrapper.save();
        }
        if (item.getItemMeta().hasLore()) {
            if (Config.isClearDefaultAttribute() && SXAttribute.getNbtUtil().isEquipment(item) && config.getBoolean("ClearAttribute", true)) {
                SXAttribute.getNbtUtil().clearAttribute(item);
            }
        }
        return item;
    }

    @Override
    public ConfigurationSection saveItem(ItemStack saveItem, ConfigurationSection config) {
        ItemMeta itemMeta = saveItem.getItemMeta();
        config.set("Name", itemMeta.hasDisplayName() ? itemMeta.getDisplayName().replace("§", "&") : null);
        config.set("ID", saveItem.getType().name() + (saveItem.getDurability() != 0 ? ":" + saveItem.getDurability() : ""));
        if (itemMeta.hasLore()) {
            config.set("Lore", itemMeta.getLore().stream().map(s -> s.replace("§", "&")).collect(Collectors.toList()));
        }
        if (itemMeta.hasEnchants()) {
            config.set("EnchantList", itemMeta.getEnchants().entrySet().stream().map(entry -> entry.getKey().getName() + ":" + entry.getValue()).collect(Collectors.toList()));
        }
        if (itemMeta.getItemFlags().size() > 0) {
            config.set("ItemFlagList", itemMeta.getItemFlags().stream().map(Enum::name).collect(Collectors.toList()));
        }
        config.set("Unbreakable", SubCondition.isUnbreakable(itemMeta));
        if (itemMeta instanceof LeatherArmorMeta) {
            config.set("Color", Integer.toHexString(((LeatherArmorMeta) itemMeta).getColor().asRGB()));
        }
        if (itemMeta instanceof SkullMeta) {
            config.set("SkullName", ((SkullMeta) itemMeta).getOwner());
        }
        return config;
    }


    public ItemStack getItemStack(String itemName, String id, List<String> loreList, List<String> enchantList, List<String> itemFlagList, boolean unbreakable, Color color, String skullName) {
        String[] idSplit = id.split(":");
        ItemStack item = new ItemStack(ItemManager.getMaterial(idSplit[0]));

        if (item.getType().getMaxDurability() != 0 && idSplit.length > 1) {
            item.setDurability(Short.parseShort(idSplit[1]));
        }

        ItemMeta meta = item.getItemMeta();

        if (itemName != null) {
            meta.setDisplayName(itemName.replace('&', '§'));
        }

        loreList.replaceAll(lore -> lore.replace('&', '§'));
        meta.setLore(loreList);

        for (String enchant : enchantList) {
            String[] enchantSplit = enchant.split(":");
            Enchantment enchantment = Enchantment.getByName(enchantSplit[0]);
            int level = Integer.valueOf(enchantSplit[1]);
            if (enchantment != null && level != 0) {
                meta.addEnchant(enchantment, level, true);
            }
        }

        for (ItemFlag itemFlag : ItemFlag.values()) {
            if (itemFlagList.contains(itemFlag.name())) {
                meta.addItemFlags(itemFlag);
            }
        }

        ItemUtil.getInst().setUnbreakable(meta, unbreakable);

        if (color != null && meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(color);
        }

        if (skullName != null && meta instanceof SkullMeta) {
            ItemUtil.getInst().setSkull(meta, skullName);
        }

        item.setItemMeta(meta);
        return item;
    }

    @Override
    public int updateCode() {
        return hashCode;
    }

    @Override
    public boolean isUpdate() {
        return update;
    }
}
