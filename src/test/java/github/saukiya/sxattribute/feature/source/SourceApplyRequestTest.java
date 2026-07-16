package github.saukiya.sxattribute.feature.source;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/** 来源请求不变量测试。 */
public class SourceApplyRequestTest {

    /** 最大层数必须至少为一，传入集合必须复制为不可变快照。 */
    @Test
    public void normalizesStackLimitAndCopiesValues() {
        java.util.List<String> attributes = new java.util.ArrayList<>(Arrays.asList("攻击力: 10"));
        SourceApplyRequest request = new SourceApplyRequest("test", attributes, 20L, 0,
                SourceApplyRequest.StackMode.STACK, false, Collections.singletonList("buff"));
        attributes.clear();
        Assert.assertEquals(1, request.getMaxStacks());
        Assert.assertEquals(1, request.getAttributes().size());
    }
}
