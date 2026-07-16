package github.saukiya.sxattribute.data.attribute.sub.other;

import org.junit.Assert;
import org.junit.Test;

/** 验证脚本隔离边界不会误吞 JVM 无法恢复的致命错误。 */
public class JSAttributeIsolationTest {

    @Test
    public void distinguishesScriptFailuresFromFatalJvmErrors() {
        Assert.assertFalse(JSAttribute.isFatalFailure(new IllegalStateException("broken script")));
        Assert.assertFalse(JSAttribute.isFatalFailure(new LinkageError("optional adapter mismatch")));
        Assert.assertTrue(JSAttribute.isFatalFailure(new OutOfMemoryError("fatal")));
    }
}
