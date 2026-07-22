package github.saukiya.sxattribute.util.hologram;

import org.bukkit.Location;

import java.util.Collections;
import java.util.List;

/**
 * 全息显示提供者抽象.
 * <p>
 * 屏蔽 HolographicDisplays / DecentHolograms 等不同全息插件的 API 差异,
 * 上层逻辑仅依赖此接口, 由 {@link github.saukiya.sxattribute.SXAttribute} 在启动时
 * 根据服务器已安装的插件注入具体实现.
 *
 * @author Saukiya
 */
public interface HologramProvider {

    /**
     * 创建一个仅含单行文本的全息, 用于伤害/回血浮空字.
     *
     * @param location 全息位置
     * @param text     文本内容 (已完成颜色符号转换)
     * @return 全息句柄，用于后续移动和删除
     */
    default Handle create(Location location, String text) {
        return create(location, Collections.singletonList(text));
    }

    /**
     * 创建多行全息。伤害事件可能同时产生多条属性消息，适配器必须保持原有行顺序。
     *
     * @param location 全息位置
     * @param lines    已完成颜色转换的文本行
     * @return 全息句柄，用于移动和删除
     */
    Handle create(Location location, List<String> lines);

    /**
     * 全息句柄，屏蔽底层全息对象的类型差异，仅暴露浮空字所需的移动和删除能力。
     */
    interface Handle {

        /**
         * 删除该全息.
         */
        void delete();

        /**
         * 将浮空字向上移动指定距离，避免上层依赖任一全息插件的对象类型。
         *
         * @param distance Y 轴增量
         */
        void moveUp(double distance);
    }
}
