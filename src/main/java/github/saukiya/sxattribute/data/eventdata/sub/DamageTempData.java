package github.saukiya.sxattribute.data.eventdata.sub;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class DamageTempData {

    private String source;

    private UUID damager;

    private UUID defender;

    private SXAttributeData attributes;

}
