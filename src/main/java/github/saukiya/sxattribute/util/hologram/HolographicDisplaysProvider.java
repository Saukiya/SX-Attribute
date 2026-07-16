package github.saukiya.sxattribute.util.hologram;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import github.saukiya.sxattribute.SXAttribute;
import org.bukkit.Location;

/**
 * HolographicDisplays 适配器.
 * <p>
 * 仅在服务器已启用 HolographicDisplays 时才会被实例化, 从而将
 * {@code com.gmail.filoghost.*} 的类加载隔离在本类内部, 避免缺失该插件时报错.
 *
 * @author Saukiya
 */
public class HolographicDisplaysProvider implements HologramProvider {

    @Override
    public Handle create(Location location, String text) {
        Hologram hologram = HologramsAPI.createHologram(SXAttribute.getInst(), location);
        hologram.appendTextLine(text);
        return hologram::delete;
    }
}
