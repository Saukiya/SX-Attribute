package github.saukiya.sxattribute.data;

import github.saukiya.sxattribute.util.Config;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;

/**
 * @author Saukiya
 */
public class NameData {

    @Getter
    private String name;

    @Getter
    private LivingEntity entity;

    @Getter
    private boolean visible;

    @Getter
    private Long tick;

    private int updateTick = Config.getConfig().getInt(Config.HEALTH_NAME_VISIBLE_DISPLAY_TIME);

    /**
     * 头顶血条显示数据
     *
     * @param entity  实体
     * @param name    原来的名字
     * @param visible 是否隐藏
     */
    public NameData(LivingEntity entity, String name, boolean visible) {
        this.entity = entity;
        this.name = name;
        this.visible = visible;
    }

    public void updateTick() {
        this.tick = System.currentTimeMillis() + (updateTick * 1000);
    }

}
