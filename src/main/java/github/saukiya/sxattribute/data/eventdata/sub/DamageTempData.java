package github.saukiya.sxattribute.data.eventdata.sub;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import lombok.*;
import org.bukkit.entity.LivingEntity;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DamageTempData {

    private String source;

    private LivingEntity damager;

    private LivingEntity defender;

    private double damage;

    private SXAttributeData attributes;

}
