package github.saukiya.sxattribute.data.eventdata.sub;

import github.saukiya.sxattribute.data.eventdata.EventData;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;

/**
 * 更新事件
 *
 * @author Saukiya
 */
@Getter
public class UpdateData implements EventData {

    /**
     * -- GETTER --
     *  获取该事件的实体
     *
     * @return LivingEntity
     */
    private final LivingEntity entity;

    public UpdateData(LivingEntity entity) {
        this.entity = entity;
    }

}
