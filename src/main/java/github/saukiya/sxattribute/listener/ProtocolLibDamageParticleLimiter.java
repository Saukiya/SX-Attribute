package github.saukiya.sxattribute.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import github.saukiya.sxattribute.util.Config;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * 在 ProtocolLib 的出站边界压缩原版伤害指示粒子包，作为 PacketEvents 不可用时的回退实现。
 *
 * <p>该组件不修改 Bukkit 伤害事件、实体生命或击杀判定，只改写客户端最终收到的
 * {@code WORLD_PARTICLES} 数量字段。ProtocolLib 是可选依赖，本类只会在依赖存在时加载。</p>
 */
final class ProtocolLibDamageParticleLimiter {

    private static final String DAMAGE_INDICATOR = "DAMAGE_INDICATOR";

    private ProtocolLibDamageParticleLimiter() {
    }

    /**
     * 注册伤害粒子出站监听器。监听器每次发包时读取最新配置，因此 {@code /sx reload}
     * 可以即时调整上限，无需重复注册数据包监听器。
     *
     * @param plugin 作为 ProtocolLib 监听器所有者的 SX-Attribute 实例
     */
    public static void register(Plugin plugin) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.WORLD_PARTICLES) {
            @Override
            public void onPacketSending(PacketEvent event) {
                int limit = Config.getDamageParticleLimit();
                if (limit < 0 || !isDamageIndicator(event.getPacket())) return;

                StructureModifier<Integer> integers = event.getPacket().getIntegers();
                Integer count = integers.readSafely(0);
                if (count == null || count <= limit) return;

                if (limit == 0) {
                    // 粒子包的 count=0 代表单个定向粒子而非零粒子，完全隐藏必须取消整个包。
                    event.setCancelled(true);
                } else {
                    integers.writeSafely(0, limit);
                }
            }
        });
    }

    /** 显式移除插件拥有的 ProtocolLib 监听器，兼容非标准插件重载流程。 */
    static void unregister(Plugin plugin) {
        ProtocolLibrary.getProtocolManager().removePacketListeners(plugin);
    }

    /**
     * 同时兼容 1.13+ 的 WrappedParticle 与旧协议的 EnumWrappers.Particle。
     * 新旧 ProtocolLib 暴露的读取器可能在不适用的协议上抛出转换异常，因此按顺序安全降级。
     */
    private static boolean isDamageIndicator(PacketContainer packet) {
        try {
            Object wrappedParticle = packet.getNewParticles().readSafely(0);
            if (wrappedParticle != null) {
                Method getter = wrappedParticle.getClass().getMethod("getParticle");
                Object particle = getter.invoke(wrappedParticle);
                if (particle != null) return DAMAGE_INDICATOR.equals(String.valueOf(particle));
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            // 当前协议不使用 WrappedParticle 时继续读取旧枚举字段。
        }

        try {
            Object particle = packet.getParticles().readSafely(0);
            return particle != null && DAMAGE_INDICATOR.equals(String.valueOf(particle));
        } catch (RuntimeException | LinkageError ignored) {
            // 未识别的数据包保持原样，避免协议差异影响其他粒子。
            return false;
        }
    }
}
