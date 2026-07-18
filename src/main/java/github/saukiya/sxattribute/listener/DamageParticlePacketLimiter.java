package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * 伤害粒子出站限流的可选数据包库路由器。
 *
 * <p>PacketEvents 优先，ProtocolLib 作为回退；任一适配器发生链接错误时都不会影响伤害结算
 * 或插件生命周期。适配器必须保持互斥，避免同一粒子包被重复处理。</p>
 */
public final class DamageParticlePacketLimiter {

    private static Backend activeBackend = Backend.NONE;

    private DamageParticlePacketLimiter() {
    }

    /**
     * 按 PacketEvents、ProtocolLib 的顺序注册第一个可用适配器。
     *
     * @param plugin SX-Attribute 插件实例
     */
    public static void register(Plugin plugin) {
        if (Config.getDamageParticleLimit() < 0 || activeBackend != Backend.NONE) return;

        if (Bukkit.getPluginManager().isPluginEnabled("packetevents")) {
            try {
                PacketEventsDamageParticleLimiter.register();
                activeBackend = Backend.PACKET_EVENTS;
                plugin.getLogger().info("Damage particle packet limiter is using PacketEvents.");
                return;
            } catch (RuntimeException | LinkageError exception) {
                plugin.getLogger().warning("PacketEvents damage particle limiter unavailable; trying ProtocolLib: "
                        + exception.getMessage());
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            try {
                ProtocolLibDamageParticleLimiter.register(plugin);
                activeBackend = Backend.PROTOCOL_LIB;
                plugin.getLogger().info("Damage particle packet limiter is using ProtocolLib.");
                return;
            } catch (RuntimeException | LinkageError exception) {
                plugin.getLogger().warning("ProtocolLib damage particle limiter unavailable: " + exception.getMessage());
            }
        }

        plugin.getLogger().warning("DamageParticleLimit requires PacketEvents or ProtocolLib; damage particles will not be limited.");
    }

    /**
     * 注销当前适配器，防止插件重载后遗留 PacketEvents 监听器或重复处理数据包。
     *
     * @param plugin SX-Attribute 插件实例
     */
    public static void unregister(Plugin plugin) {
        try {
            if (activeBackend == Backend.PACKET_EVENTS) {
                PacketEventsDamageParticleLimiter.unregister();
            } else if (activeBackend == Backend.PROTOCOL_LIB
                    && Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
                ProtocolLibDamageParticleLimiter.unregister(plugin);
            }
        } catch (RuntimeException | LinkageError exception) {
            plugin.getLogger().warning("Unable to unregister damage particle packet limiter: " + exception.getMessage());
        } finally {
            activeBackend = Backend.NONE;
        }
    }

    /** 可选数据包实现的互斥运行状态。 */
    private enum Backend {
        NONE,
        PACKET_EVENTS,
        PROTOCOL_LIB
    }
}
