package github.saukiya.sxattribute.feature.attribute;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import github.saukiya.sxattribute.event.SXLoadAttributeEvent;
import github.saukiya.sxattribute.event.SXAttributeActionEvent;
import github.saukiya.sxattribute.feature.source.SourceApplyRequest;
import github.saukiya.sxattribute.util.AttributeUtil;
import github.saukiya.sxattribute.util.FoliaScheduler;
import github.saukiya.sxattribute.util.FormulaUtil;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置化属性运行时。
 * <p>
 * 注册表通过完整构建后再 volatile 替换实现热重载；动作只允许本类白名单中的安全原语，
 * 配置无法反射任意 Java 类或执行脚本。
 */
public class AttributeEngine implements Listener {

    private volatile AttributeRegistry registry = AttributeRegistry.load();
    private Object tickTask;
    private final Map<UUID, SXAttributeData> equipmentSnapshots = new ConcurrentHashMap<>();

    public AttributeEngine() {
        Bukkit.getPluginManager().registerEvents(this, SXAttribute.getInst());
        startTicker();
    }

    public boolean reload() {
        try {
            AttributeRegistry replacement = AttributeRegistry.load();
            registry = replacement;
            FoliaScheduler.cancel(tickTask);
            startTicker();
            return true;
        } catch (RuntimeException exception) {
            SXAttribute.getInst().getLogger().severe("Attribute registry reload rejected: " + exception.getMessage());
            return false;
        }
    }

    public AttributeRegistry registry() {
        return registry;
    }

    public void parseLore(SXAttributeData data, String lore) {
        registry.parseLore(data, lore);
    }

    public void correct(SXAttributeData data) {
        registry.correct(data);
    }

    public double combatPower(SXAttributeData data) {
        return registry.combatPower(data);
    }

    public void merge(SXAttributeData target, SXAttributeData addition) {
        registry.merge(target, addition);
    }

    public void fire(AttributeDefinition.AttributeTrigger event, AttributeExecutionContext context) {
        for (AttributeRegistry.AttributeInvocation invocation : registry.invocations(event)) {
            boolean attackerSide = event != AttributeDefinition.AttributeTrigger.DAMAGE_DEFEND;
            Map<String, Double> variables = context.variablesFor(invocation.definition(), attackerSide);
            String when = invocation.trigger().getWhen();
            if (when != null && FormulaUtil.eval(context.formulaPlayer(), when, variables, 0D) <= 0D) continue;
            for (Map<String, Object> action : invocation.trigger().getActions()) {
                SXAttributeActionEvent pre = new SXAttributeActionEvent(invocation.definition().getId(), SXAttributeActionEvent.Phase.PRE, action, context);
                Bukkit.getPluginManager().callEvent(pre);
                if (pre.isCancelled()) continue;
                execute(invocation.definition(), action, variables, context, attackerSide);
                Bukkit.getPluginManager().callEvent(new SXAttributeActionEvent(invocation.definition().getId(), SXAttributeActionEvent.Phase.POST, action, context));
            }
        }
    }

    /** 外部插件触发 API 类型属性，并复用同一条件与动作流水线。 */
    public void fireApi(LivingEntity actor, LivingEntity target) {
        SXAttributeData actorData = SXAttribute.getAttributeManager().getEntityData(actor);
        SXAttributeData targetData = target == null ? actorData : SXAttribute.getAttributeManager().getEntityData(target);
        fire(AttributeDefinition.AttributeTrigger.API,
                new AttributeExecutionContext(actor, target == null ? actor : target, actorData, targetData, null));
    }

    private void execute(AttributeDefinition definition, Map<String, Object> action, Map<String, Double> variables,
                         AttributeExecutionContext context, boolean attackerSide) {
        String type = text(action, "Type", "").toUpperCase();
        double chance = formula(action, "Chance", variables, context, 100D);
        if (chance < 100D && Math.random() * 100D >= chance) return;
        LivingEntity target = selectTarget(action, context, attackerSide);
        DamageData damage = context.getDamageData();
        switch (type) {
            case "DAMAGE":
                if (damage == null) return;
                double amount = formula(action, "Formula", variables, context, 0D);
                String mode = text(action, "Mode", "ADD").toUpperCase();
                if ("SET".equals(mode)) damage.setDamage(amount);
                else if ("MULTIPLY".equals(mode)) damage.setDamage(damage.getDamage() * amount);
                else damage.addDamage(amount);
                variables.put("event_damage", damage.getDamage());
                break;
            case "HEAL":
                if (target != null) setHealth(target, target.getHealth() + formula(action, "Formula", variables, context, 0D));
                break;
            case "CANCEL":
                if (damage != null) damage.setCancelled(true);
                break;
            case "VANILLA_ATTRIBUTE":
                applyVanillaAttribute(target, action, variables, context);
                break;
            case "POTION":
                applyPotion(target, action, variables, context);
                break;
            case "SOURCE_APPLY":
                if (target != null && SXAttribute.getSourceService() != null) {
                    SXAttribute.getSourceService().apply(target, SourceApplyRequest.fromAction(definition.getId(), action, variables, context.formulaPlayer()));
                }
                break;
            case "SOURCE_REMOVE":
                if (target != null && SXAttribute.getSourceService() != null) {
                    SXAttribute.getSourceService().remove(target, text(action, "Source", definition.getId()));
                }
                break;
            case "FIRE":
                if (target != null) target.setFireTicks((int) formula(action, "Formula", variables, context, 40D));
                break;
            case "LIGHTNING":
                if (target != null) target.getWorld().strikeLightning(target.getLocation());
                break;
            case "COMMAND":
                runCommand(target, text(action, "Command", ""));
                break;
            case "MESSAGE":
                if (target != null) Message.Tool.send(target, color(text(action, "Message", "")));
                break;
            case "HOLOGRAM":
                if (damage != null) damage.sendHolo(color(text(action, "Message", "")));
                break;
            case "PARTICLE":
                spawnParticle(target, text(action, "Particle", "CRIT"), (int) formula(action, "Count", variables, context, 1D));
                break;
            case "SOUND":
                playSound(target, text(action, "Sound", "ENTITY_EXPERIENCE_ORB_PICKUP"),
                        (float) formula(action, "Volume", variables, context, 1D),
                        (float) formula(action, "Pitch", variables, context, 1D));
                break;
            case "EXP":
                if (target instanceof Player) ((Player) target).giveExp((int) formula(action, "Formula", variables, context, 0D));
                break;
            case "DURABILITY":
                damageItem(target, (int) formula(action, "Formula", variables, context, 1D));
                break;
            default:
                SXAttribute.getInst().getLogger().warning("Unknown attribute action ignored: " + type);
        }
    }

    private double formula(Map<String, Object> action, String key, Map<String, Double> variables,
                           AttributeExecutionContext context, double fallback) {
        Object value = action.get(key);
        if (value instanceof Number) return ((Number) value).doubleValue();
        return value == null ? fallback : FormulaUtil.eval(context.formulaPlayer(), String.valueOf(value), variables, fallback);
    }

    private LivingEntity selectTarget(Map<String, Object> action, AttributeExecutionContext context, boolean attackerSide) {
        String configured = text(action, "Target", attackerSide ? "DEFENDER" : "ATTACKER").toUpperCase();
        return "ATTACKER".equals(configured) ? context.getAttacker() : context.getDefender();
    }

    private void applyVanillaAttribute(LivingEntity entity, Map<String, Object> action,
                                       Map<String, Double> variables, AttributeExecutionContext context) {
        if (entity == null) return;
        AttributeInstance instance = AttributeUtil.getInstance(entity, text(action, "RegistryKey", ""), text(action, "LegacyName", ""));
        if (instance != null) instance.setBaseValue(formula(action, "Formula", variables, context, instance.getBaseValue()));
    }

    private void applyPotion(LivingEntity entity, Map<String, Object> action,
                             Map<String, Double> variables, AttributeExecutionContext context) {
        if (entity == null) return;
        PotionEffectType type = PotionEffectType.getByName(text(action, "Potion", ""));
        if (type == null) return;
        int duration = (int) formula(action, "Duration", variables, context, 20D);
        int amplifier = (int) formula(action, "Amplifier", variables, context, 0D);
        entity.addPotionEffect(new PotionEffect(type, Math.max(1, duration), Math.max(0, amplifier)), true);
    }

    private void setHealth(LivingEntity entity, double health) {
        entity.setHealth(Math.max(0D, Math.min(entity.getMaxHealth(), health)));
    }

    private void runCommand(LivingEntity entity, String command) {
        if (command.isEmpty()) return;
        String rendered = command.replace("%player%", entity instanceof Player ? entity.getName() : "");
        FoliaScheduler.runSync(SXAttribute.getInst(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), rendered), 0L);
    }

    private void spawnParticle(LivingEntity entity, String particleName, int count) {
        if (entity == null) return;
        try {
            entity.getWorld().spawnParticle(Particle.valueOf(particleName.toUpperCase()), entity.getLocation(), Math.max(1, count));
        } catch (Throwable ignored) {
            SXAttribute.getInst().getLogger().warning("Particle not supported by this server: " + particleName);
        }
    }

    private void playSound(LivingEntity entity, String soundName, float volume, float pitch) {
        if (entity == null) return;
        try {
            entity.getWorld().playSound(entity.getLocation(), Sound.valueOf(soundName.toUpperCase()), volume, pitch);
        } catch (Throwable ignored) {
            SXAttribute.getInst().getLogger().warning("Sound not supported by this server: " + soundName);
        }
    }

    private void damageItem(LivingEntity entity, int amount) {
        if (entity == null || entity.getEquipment() == null) return;
        ItemStack item = SXAttribute.isHigherVersion() ? entity.getEquipment().getItemInMainHand() : entity.getEquipment().getItemInHand();
        if (item != null && item.getType().getMaxDurability() > 0) item.setDurability((short) (item.getDurability() + Math.max(0, amount)));
    }

    private String text(Map<String, Object> action, String key, String fallback) {
        Object value = action.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private String color(String value) {
        return value.replace('&', '§');
    }

    private void startTicker() {
        long period = Math.max(1L, AttributeConfigValue.tickPeriod());
        tickTask = FoliaScheduler.runTimer(SXAttribute.getInst(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                FoliaScheduler.runEntity(player, SXAttribute.getInst(), () -> {
                    SXAttributeData data = SXAttribute.getAttributeManager().getEntityData(player);
                    fire(AttributeDefinition.AttributeTrigger.TICK,
                            new AttributeExecutionContext(player, player, data, data, null));
                }, 1L);
            }
        }, period, period);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLoad(SXLoadAttributeEvent event) {
        SXAttributeData current = new SXAttributeData().add(event.getAttributeData());
        SXAttributeData previous = equipmentSnapshots.put(event.getEntity().getUniqueId(), current);
        boolean changed = previous == null || !sameData(previous, current);
        if (previous != null && changed) {
            fire(AttributeDefinition.AttributeTrigger.UNEQUIP,
                    new AttributeExecutionContext(event.getEntity(), event.getEntity(), previous, previous, null));
        }
        AttributeExecutionContext context = new AttributeExecutionContext(event.getEntity(), event.getEntity(), current, current, null);
        fire(AttributeDefinition.AttributeTrigger.LOAD, context);
        if (changed) fire(AttributeDefinition.AttributeTrigger.EQUIP, context);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeal(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity entity = (LivingEntity) event.getEntity();
        SXAttributeData data = SXAttribute.getAttributeManager().getEntityData(entity);
        fire(AttributeDefinition.AttributeTrigger.HEAL, new AttributeExecutionContext(entity, entity, data, data, null));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExperience(PlayerExpChangeEvent event) {
        SXAttributeData data = SXAttribute.getAttributeManager().getEntityData(event.getPlayer());
        AttributeExecutionContext context = new AttributeExecutionContext(event.getPlayer(), event.getPlayer(), data, data, null);
        context.getVariables().put("event_exp", (double) event.getAmount());
        fire(AttributeDefinition.AttributeTrigger.EXP_GAIN, context);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        LivingEntity killer = dead.getKiller();
        SXAttributeData deadData = SXAttribute.getAttributeManager().getEntityData(dead);
        SXAttributeData killerData = killer == null ? new SXAttributeData() : SXAttribute.getAttributeManager().getEntityData(killer);
        AttributeExecutionContext context = new AttributeExecutionContext(killer, dead, killerData, deadData, null);
        fire(AttributeDefinition.AttributeTrigger.DEATH, context);
        if (killer != null) fire(AttributeDefinition.AttributeTrigger.KILL, context);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectile(EntityShootBowEvent event) {
        SXAttributeData data = SXAttribute.getAttributeManager().getEntityData(event.getEntity());
        fire(AttributeDefinition.AttributeTrigger.PROJECTILE_SHOOT,
                new AttributeExecutionContext(event.getEntity(), event.getEntity(), data, data, null));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        equipmentSnapshots.remove(event.getPlayer().getUniqueId());
    }

    public void disable() {
        FoliaScheduler.cancel(tickTask);
        equipmentSnapshots.clear();
    }

    private boolean sameData(SXAttributeData left, SXAttributeData right) {
        return Arrays.deepEquals(left.getValues(), right.getValues())
                && left.getDynamicValues().equals(right.getDynamicValues());
    }

    /**
     * 避免属性引擎依赖整个 Config 常量类，只读取新主清单中的周期设置。
     */
    private static final class AttributeConfigValue {
        private static long tickPeriod() {
            ConfigurationSection config = github.saukiya.sxattribute.util.AttributeConfig.getConfig();
            return config == null ? 20L : config.getLong("Settings.TickPeriod", 20L);
        }
    }
}
