package github.saukiya.sxattribute.util;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.tools.nms.MessageUtil;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public enum Message {

    PLAYER__NO_REGISTER_SLOTS,
    PLAYER__NO_LEVEL_USE,
    PLAYER__NO_ROLE,
    PLAYER__NO_USE_SLOT,
    PLAYER__OVERDUE_ITEM,
    PLAYER__EXP_ADDITION,
    PLAYER__NO_VAULT,
    PLAYER__NO_DURABILITY,
    PLAYER__SELL,
    PLAYER__BATTLE__FIRST_PERSON,
    PLAYER__HOLOGRAPHIC__HURT,
    PLAYER__HOLOGRAPHIC__HEALTH,
    INVENTORY__STATS__NAME,
    INVENTORY__STATS__HIDE_ON,
    INVENTORY__STATS__HIDE_OFF,
    INVENTORY__STATS__SKULL_NAME,
    INVENTORY__STATS__SKULL_LORE,
    INVENTORY__STATS__ATTACK,
    INVENTORY__STATS__ATTACK_LORE,
    INVENTORY__STATS__DEFENSE,
    INVENTORY__STATS__DEFENSE_LORE,
    INVENTORY__STATS__BASE,
    INVENTORY__STATS__BASE_LORE,
    INVENTORY__SELL__NAME,
    INVENTORY__SELL__SELL,
    INVENTORY__SELL__ENTER,
    INVENTORY__SELL__OUT,
    INVENTORY__SELL__NO_SELL,
    INVENTORY__SELL__LORE__DEFAULT,
    INVENTORY__SELL__LORE__FORMAT,
    INVENTORY__SELL__LORE__NO_SELL,
    INVENTORY__SELL__LORE__ALL_SELL,
    INVENTORY__REPAIR__NAME,
    INVENTORY__REPAIR__GUIDE,
    INVENTORY__REPAIR__ENTER,
    INVENTORY__REPAIR__MONEY,
    INVENTORY__REPAIR__NO_MONEY,
    INVENTORY__REPAIR__UNSUITED,
    INVENTORY__REPAIR__REPAIR,
    INVENTORY__REPAIR__LORE__ENTER,
    INVENTORY__REPAIR__LORE__MONEY,
    INVENTORY__DISPLAY_SLOTS_NAME,
    ADMIN__CLEAR_ENTITY_DATA,
    ADMIN__NO_ITEM,
    ADMIN__GIVE_ITEM,
    ADMIN__HAS_ITEM,
    ADMIN__SAVE_ITEM,
    ADMIN__SAVE_NO_TYPE,
    ADMIN__SAVE_ITEM_ERROR,
    ADMIN__NO_PERMISSION_CMD,
    ADMIN__NO_CMD,
    ADMIN__NO_FORMAT,
    ADMIN__NO_CONSOLE,
    ADMIN__PLUGIN_RELOAD,
    ADMIN__NO_ONLINE,
    COMMAND__STATS,
    COMMAND__SELL,
    COMMAND__REPAIR,
    COMMAND__GIVE,
    COMMAND__SAVE,
    COMMAND__NBT,
    COMMAND__ATTRIBUTELIST,
    COMMAND__CONDITIONLIST,
    COMMAND__RELOAD,
    REPLACE_LIST;

    @Getter
    private static YamlConfiguration messages;

    private static Tool tool = new Tool();

    /**
     * 加载Message类
     */
    public static void loadMessage() {
        File file = new File(SXAttribute.getInst().getDataFolder(), "Message.yml");

        if (!file.exists()) {
            SXAttribute.getInst().getLogger().info("Create Message.yml");
            SXAttribute.getInst().saveResource("Message.yml", true);
        }
        messages = YamlConfiguration.loadConfiguration(file);
        tool.setConfig(messages);
        SubAttribute.setFirstPerson(Message.getMsg(Message.PLAYER__BATTLE__FIRST_PERSON));
    }

    /**
     * 获取String
     *
     * @param loc  Message
     * @param args Object...
     * @return String
     */
    public static String getMsg(Message loc, Object... args) {
        return tool.getString(loc.toString(), args);
    }

    /**
     * 获取List
     *
     * @param loc  Message
     * @param args Object...
     * @return List
     */
    public static List<String> getStringList(Message loc, Object... args) {
        return tool.getStringList(loc.toString(), args);
    }

    /**
     * 发送消息给玩家
     *
     * @param entity LivingEntity
     * @param loc    Message
     * @param args   Object...
     */
    public static void send(LivingEntity entity, Message loc, Object... args) {
        tool.send(entity, loc.toString(), args);
    }

    @Override
    public String toString() {
        return name().replace("__", ".");
    }

    @Setter
    public static class Tool {

        YamlConfiguration config;

        public YamlConfiguration config() {
            return config;
        }

        public String getString(String loc, Object... args) {
            return MessageFormat.format(config.getString(loc, "Null Message: " + loc), args).replace('&', '§');
        }

        public List<String> getStringList(String loc, Object... args) {
            List<String> list = config.getStringList(loc);
            if (list.size() == 0) return Collections.singletonList("Null Message: " + loc);
            IntStream.range(0, list.size()).forEach(i -> list.set(i, MessageFormat.format(list.get(i), args).replace('&', '§')));
            return list;
        }

        public void send(LivingEntity entity, String loc, Object... args) {
            send(entity, getString(loc, args));
        }

        public static void send(LivingEntity entity, String msg) {
            MessageUtil.send(entity, msg);
        }

        public static TextComponent getTextComponent(String msg, String command, String showText) {
            TextComponent tc = new TextComponent(msg);
            if (showText != null)
                tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7" + showText).create()));
            if (command != null) tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
            return tc;
        }

        public static void sendTextComponent(CommandSender sender, TextComponent tc) {
            if (sender instanceof Player) {
                ((Player) sender).spigot().sendMessage(tc);
            } else {
                sender.sendMessage(tc.getText());
            }
        }
    }
}