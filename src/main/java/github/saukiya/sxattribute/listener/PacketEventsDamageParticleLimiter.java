package github.saukiya.sxattribute.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import github.saukiya.sxattribute.util.Config;

/** PacketEvents 出站粒子包适配器，仅在对应插件已经启用时加载。 */
final class PacketEventsDamageParticleLimiter {

    private static PacketListenerCommon listener;

    private PacketEventsDamageParticleLimiter() {
    }

    /** 注册高优先级监听器，使数量上限在其他普通数据包修改之后生效。 */
    static void register() {
        listener = PacketEvents.getAPI().getEventManager().registerListener(new PacketListener() {
            @Override
            public void onPacketSend(PacketSendEvent event) {
                if (event.getPacketType() != PacketType.Play.Server.PARTICLE) return;

                WrapperPlayServerParticle packet = new WrapperPlayServerParticle(event);
                if (packet.getParticle().getType() != ParticleTypes.DAMAGE_INDICATOR) return;

                int limit = Config.getDamageParticleLimit();
                if (limit < 0 || packet.getParticleCount() <= limit) return;
                if (limit == 0) {
                    // 协议中的 count=0 会生成单个定向粒子，完全隐藏必须取消整个包。
                    event.setCancelled(true);
                } else {
                    packet.setParticleCount(limit);
                }
            }
        }, PacketListenerPriority.HIGHEST);
    }

    /** 注销由当前插件持有的监听器，PacketEvents 不会按 Bukkit 插件归属自动清理它。 */
    static void unregister() {
        if (listener == null) return;
        PacketEvents.getAPI().getEventManager().unregisterListener(listener);
        listener = null;
    }
}
