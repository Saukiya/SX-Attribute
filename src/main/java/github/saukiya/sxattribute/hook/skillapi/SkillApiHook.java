package github.saukiya.sxattribute.hook.skillapi;

import com.sucy.skill.api.skills.Skill;
import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.hook.mythic.SkillDamageContext;
import github.saukiya.sxattribute.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.manager.AttributeManager;

/** ProSkillAPI 双向握手入口，保护可选 API 的链接错误并提供伤害回滚。 */
public final class SkillApiHook {
    private static boolean active;
    private static BukkitTask syncTask;
    private static SkillApiAttributeBridge attributes;
    private SkillApiHook() { }
    /** 双方开关和插件均可用时注册 SX 技能伤害管线。 */
    public static void setup() {
        if (!Config.isSkillApiEnabled() || !Bukkit.getPluginManager().isPluginEnabled("SkillAPI")) return;
        try {
            // 双方均显式开启才接管数值，避免只改一侧配置产生半集成状态。
            if (!com.sucy.skill.SkillAPI.getSettings().isSxAttributeEnabled()) {
                SXAttribute.getInst().getLogger().info("SkillAPI bridge waiting: ProSkillAPI side is disabled.");
                return;
            }
            Skill.setSkillDamageHandler(SkillApiHook::damage); active = true; attributes = new SkillApiAttributeBridge(); Bukkit.getPluginManager().registerEvents(attributes, SXAttribute.getInst()); syncTask = Bukkit.getScheduler().runTaskTimer(SXAttribute.getInst(), SkillApiHook::syncPlayers, 20L, 40L);
            // 治疗也由桥接入口统一写入，保留原版事件前的单一 Bukkit 写入权。
            com.sucy.skill.SkillAPI.setSkillHealHandler(SkillApiHook::heal);
            SXAttribute.getInst().getLogger().info("SkillAPI bridge enabled: SX is the authoritative combat pipeline.");
        } catch (LinkageError e) { SXAttribute.getInst().getLogger().warning("SkillAPI bridge disabled: " + e); }
    }
    /** 关闭时恢复 ProSkillAPI 原生伤害路径。 */
    public static void teardown() { if (active) { try { Skill.setSkillDamageHandler(null); com.sucy.skill.SkillAPI.setSkillHealHandler(null); } catch (LinkageError ignored) { } if (syncTask != null) syncTask.cancel(); syncTask = null; if (attributes != null) org.bukkit.event.HandlerList.unregisterAll(attributes); attributes = null; active = false; } }
    public static boolean isActive() { return active; }
    private static boolean damage(Skill skill, LivingEntity target, double amount, LivingEntity source, String type, boolean trueDamage) {
        if (target == null || target.isDead() || source == null) return false;
        SXAttributeData data = SXAttribute.getAttributeManager().getEntityData(source);
        return new SkillDamageContext(source, target, data, 1D, trueDamage ? "true" : type, java.util.Collections.emptyList()).damage(amount, trueDamage);
    }
    private static boolean heal(org.bukkit.entity.Player source, LivingEntity target, double amount) {
        if (target == null || target.isDead() || !Double.isFinite(amount) || amount <= 0D) return true;
        target.setHealth(Math.min(SXAttribute.getApi().getMaxHealth(target), target.getHealth() + amount));
        return true;
    }

    /** 低频覆盖玩家属性来源，避免把每次属性读取变成跨插件事件风暴。 */
    private static void syncPlayers() {
        AttributeManager manager = com.sucy.skill.SkillAPI.getAttributeManager();
        if (manager == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String key : manager.getAttributes().keySet()) {
                SkillApiAttributeBridge.beginImport();
                try {
                    SkillApiAttributeBridge.syncNumeric(player, "skillapi:attribute:" + key, key, AttributeAPI.getAttribute(player, key));
                } finally {
                    SkillApiAttributeBridge.endImport();
                }
            }
        }
    }
}
