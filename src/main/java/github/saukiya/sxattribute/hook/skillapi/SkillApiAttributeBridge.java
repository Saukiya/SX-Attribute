package github.saukiya.sxattribute.hook.skillapi;

import com.sucy.skill.api.event.AttributeGetEvent;
import com.sucy.skill.api.event.AttributeEntityAddEvent;
import com.sucy.skill.api.event.TempAttributeAddEvent;
import com.sucy.skill.api.event.SetBonusAttributeEvent;
import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.util.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.Collections;
import java.util.UUID;

/** 将同名 SX 属性写回 ProSkillAPI 的统一读取事件，避免两套战斗数值并行。 */
public final class SkillApiAttributeBridge implements Listener {
    private static final ThreadLocal<Boolean> GUARD = new ThreadLocal<>();
    /** 同步任务读取 PSA 原始值时屏蔽本桥回写，避免 SX 值被再次导入形成指数累加。 */
    private static final ThreadLocal<Boolean> IMPORT_GUARD = new ThreadLocal<>();

    @EventHandler
    public void onAttributeGet(AttributeGetEvent event) {
        if (!Config.isSkillApiEnabled() || !SkillApiHook.isActive() || Boolean.TRUE.equals(GUARD.get()) || Boolean.TRUE.equals(IMPORT_GUARD.get())) return;
        SubAttribute attribute = SubAttribute.getSubAttribute(event.getAttribute());
        if (attribute == null) return;
        GUARD.set(Boolean.TRUE);
        try {
            SXAttributeData data = SXAttribute.getApi().getEntityData(event.getCaster());
            double[] values = data.getValues(attribute);
            if (values.length > 0) event.setValue(event.getValue() + (int) Math.round(values[0]));
        } finally { GUARD.remove(); }
    }

    /** 将临时属性转换为可撤销的 SX 命名来源，并在到期时自动删除。 */
    @EventHandler
    public void onTemporaryAttribute(TempAttributeAddEvent event) {
        if (!enabled() || event.isCancelled()) return;
        String source = source("temp", event.getCaster().getUniqueId(), UUID.randomUUID());
        apply(event.getCaster(), source, event.getAttribute(), event.getValue());
        Bukkit.getScheduler().runTaskLater(SXAttribute.getInst(), () -> SXAttribute.getApi().takeSourceAttribute(event.getCaster(), source, true), Math.max(1L, event.getTick()));
    }

    /** 将非玩家属性增加转换为实体绑定来源，后续由实体生命周期清理。 */
    @EventHandler
    public void onEntityAttribute(AttributeEntityAddEvent event) {
        if (!enabled() || event.isCancelled()) return;
        apply(event.getCaster(), source("entity", event.getCaster().getUniqueId(), UUID.randomUUID()), event.getAttribute(), event.getValue());
    }

    /** 套装刷新使用固定来源，先删除旧快照再写入新快照，确保生命周期可逆。 */
    @EventHandler
    public void onSetBonus(SetBonusAttributeEvent event) {
        if (!enabled()) return;
        org.bukkit.entity.Player player = event.getPlayer();
        String source = "skillapi:setbonus:" + player.getUniqueId();
        SXAttribute.getApi().takeSourceAttribute(player, source, false);
        // ProSkillAPI 仍负责持有套装数值；这里只保留生命周期标记，避免本地加成与 SX 来源重复求和。
        SXAttribute.getApi().createStaticAttributeSource(player, source, new SXAttributeData(), false);
        SXAttribute.getAttributeManager().attributeUpdateEvent(player);
    }

    /** 实体失效时撤销本桥接产生的全部来源，避免 UUID 数据长期占用内存。 */
    @EventHandler
    public void onEntityGone(EntityDeathEvent event) { clearSources(event.getEntity()); }

    /** 玩家退出也要清理临时/实体来源；重新登录时同步任务会重建持久贡献。 */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) { clearSources(event.getPlayer()); }

    private static void clearSources(org.bukkit.entity.LivingEntity entity) {
        for (String name : SXAttribute.getApi().getSourceNames(entity.getUniqueId())) {
            if (name.startsWith("skillapi:")) SXAttribute.getApi().takeSourceAttribute(entity, name, false);
        }
        SXAttribute.getAttributeManager().attributeUpdateEvent(entity);
    }

    private boolean enabled() { return Config.isSkillApiEnabled() && SkillApiHook.isActive(); }
    private static String source(String kind, UUID entity, UUID instance) { return "skillapi:" + kind + ":" + entity + ":" + instance; }
    private static void apply(org.bukkit.entity.LivingEntity entity, String source, String key, double amount) {
        SubAttribute attribute = SubAttribute.getSubAttribute(key);
        if (attribute == null) return;
        SXAttributeData data = new SXAttributeData();
        double[] values = data.getValues(attribute);
        if (values.length > 0) values[0] = amount;
        SXAttribute.getApi().createStaticAttributeSource(entity, source, data, true);
    }

    /** 同步数值来源的公共入口，供低频玩家成长刷新任务使用。 */
    static void syncNumeric(org.bukkit.entity.LivingEntity entity, String source, String key, double amount) {
        apply(entity, source, key, amount);
    }

    static void beginImport() { IMPORT_GUARD.set(Boolean.TRUE); }
    static void endImport() { IMPORT_GUARD.remove(); }
}
