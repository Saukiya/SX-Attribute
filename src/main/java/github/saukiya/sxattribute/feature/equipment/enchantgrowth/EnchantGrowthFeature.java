package github.saukiya.sxattribute.feature.equipment.enchantgrowth;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.event.SXDamageEvent;
import github.saukiya.sxattribute.event.SXEnchantGrowthEvent;
import github.saukiya.sxattribute.feature.equipment.core.AbstractEquipmentFeature;
import github.saukiya.sxattribute.feature.equipment.core.FeatureItemState;
import github.saukiya.sxattribute.feature.equipment.core.FeatureResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 原版与真实自定义附魔共用的成长模块。
 */
public final class EnchantGrowthFeature extends AbstractEquipmentFeature implements Listener {

    private final CustomEnchantRegistrar registrar = new CustomEnchantRegistrar();

    public EnchantGrowthFeature() {
        super("EnchantGrowth");
    }

    @Override
    protected void onReload() {
        ConfigurationSection enchants = config().getConfigurationSection("Enchants");
        if (enchants == null) return;
        for (String id : enchants.getKeys(false)) {
            ConfigurationSection definition = enchants.getConfigurationSection(id);
            if (definition != null && definition.getBoolean("Custom", false) && registrar.get(id) == null) {
                registrar.register(id, definition);
            }
        }
    }

    @Override
    public List<String> render(Player player, ItemStack item, YamlConfiguration state) {
        ConfigurationSection states = state.getConfigurationSection("Enchants");
        if (states == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (String id : states.getKeys(false)) {
            int level = states.getInt(id + ".Level", 1);
            double experience = states.getDouble(id + ".Experience", 0D);
            ConfigurationSection definition = config().getConfigurationSection("Enchants." + id);
            if (definition == null) continue;
            result.add(color(config().getString("Lore.Level", "&7{name} {level} &8({exp})")
                    .replace("{name}", definition.getString("Name", id)).replace("{level}", String.valueOf(level))
                    .replace("{exp}", SXAttribute.getDf().format(experience))));
            for (String line : definition.getStringList("Attributes")) {
                result.add(color(line.replace("{level}", String.valueOf(level))));
            }
            Enchantment enchantment = resolveEnchant(id, definition);
            if (enchantment != null) item.addUnsafeEnchantment(enchantment, level);
        }
        return result;
    }

    @Override
    public FeatureResult apply(Player player, ItemStack item, YamlConfiguration state) {
        String id = config().getString("DefaultEnchant");
        ConfigurationSection definition = id == null ? null : config().getConfigurationSection("Enchants." + id);
        if (definition == null) return FeatureResult.unsupported(message("NoDefaultEnchant", "&c未配置默认成长附魔"));
        if (definition.getBoolean("Custom", false) && registrar.get(id) == null) {
            return FeatureResult.unsupported(message("RegistrationFailed", "&c该自定义附魔未真实注册，已拒绝伪装应用"));
        }
        if (!state.isConfigurationSection("Enchants." + id)) state.createSection("Enchants." + id);
        state.set("Enchants." + id + ".Level", Math.max(1, state.getInt("Enchants." + id + ".Level", 1)));
        gain(player, item, state, config().getDouble("ManualExperience", 1D));
        return FeatureResult.success(message("Success", "&a附魔获得成长经验"));
    }

    private void gain(Player player, ItemStack item, YamlConfiguration state, double amount) {
        ConfigurationSection states = state.getConfigurationSection("Enchants");
        if (states == null) return;
        for (String id : states.getKeys(false)) {
            ConfigurationSection definition = config().getConfigurationSection("Enchants." + id);
            if (definition == null) continue;
            int level = states.getInt(id + ".Level", 1);
            int oldLevel = level;
            int max = definition.getInt("MaxLevel", 10);
            double experience = states.getDouble(id + ".Experience", 0D) + amount;
            while (level < max) {
                double required = formula(player, "Enchants." + id + ".RequiredExperience", 100D * level, "level", level);
                if (experience < required) break;
                experience -= required;
                level++;
            }
            states.set(id + ".Level", level);
            states.set(id + ".Experience", experience);
            org.bukkit.Bukkit.getPluginManager().callEvent(new SXEnchantGrowthEvent(player, item, id, oldLevel, level, amount));
        }
        FeatureItemState.write(item, id(), state);
    }

    private Enchantment resolveEnchant(String id, ConfigurationSection definition) {
        return definition.getBoolean("Custom", false) ? registrar.get(id)
                : Enchantment.getByName(definition.getString("VanillaName", id));
    }

    private void gainHeld(Player player, String path) {
        if (!enabled()) return;
        ItemStack item = SXAttribute.isHigherVersion() ? player.getInventory().getItemInMainHand() : player.getItemInHand();
        if (item == null) return;
        YamlConfiguration state = FeatureItemState.read(item, id());
        gain(player, item, state, config().getDouble(path, 0D));
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(SXDamageEvent event) {
        if (event.getData().getAttacker() instanceof Player) gainHeld((Player) event.getData().getAttacker(), "Gain.Damage");
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) gainHeld(event.getEntity().getKiller(), "Gain.Kill");
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        gainHeld(event.getPlayer(), "Gain.BlockBreak");
    }

    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) gainHeld(event.getPlayer(), "Gain.Fishing");
    }
}
