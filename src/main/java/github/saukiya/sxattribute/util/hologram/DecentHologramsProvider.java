package github.saukiya.sxattribute.util.hologram;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DecentHolograms 适配器.
 * <p>
 * 仅在服务器已启用 DecentHolograms 时才会被实例化, 将
 * {@code eu.decentsoftware.*} 的类加载隔离在本类内部.
 * <p>
 * 与 HolographicDisplays 不同, DecentHolograms 要求每个全息拥有唯一名称,
 * 且重名会抛出异常, 因此此处通过自增序号生成唯一名称.
 *
 * @author Saukiya
 */
public class DecentHologramsProvider implements HologramProvider {

    /**
     * 全息名称自增序号, 保证唯一, 避免重名异常.
     */
    private static final AtomicLong COUNTER = new AtomicLong();

    @Override
    public Handle create(Location location, String text) {
        String name = "SXAttribute_" + COUNTER.getAndIncrement();
        // 非持久化全息, 创建时直接带入首行文本 (避免使用配置默认文本)
        Hologram hologram = DHAPI.createHologram(name, location, Collections.singletonList(text));
        return hologram::delete;
    }
}
