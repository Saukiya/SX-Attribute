package github.saukiya.sxattribute.event;

import org.junit.Assert;
import org.junit.Test;

/** 验证所有旧事件都能使用的线程事实源，不再依赖调用方传入的异步意图。 */
public class SXPreLoadItemEventTest {

    @Test
    public void derivesEventModeOnlyFromActualThread() {
        Assert.assertFalse(EventThreadContext.isAsynchronous(true));
        Assert.assertTrue(EventThreadContext.isAsynchronous(false));
    }
}
