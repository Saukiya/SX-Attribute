package github.saukiya.sxattribute.data.attribute.sub.other;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 验证历史内置 JavaScript 配置能够在启动前被定向修复。 */
public class JSAttributeMigrationTest {

    @Test
    public void repairsBrokenTickStatementsOnly() {
        List<String> scripts = new ArrayList<>(Arrays.asList(
                "delay 1",
                "if (js.Tick1Boo) {var v = js.drawRound(js.Tick1Angle, 2)player.getWorld().spawnParticle(Particle.SPELL_WITCH, x, 1, 0, 0, 0, 0)player.getWorld().spawnParticle(Particle.SPELL_MOB, x, 1, 0, 0, 0, 0)"
        ));

        Assert.assertTrue(JSAttribute.repairLegacyTickScript(scripts));
        Assert.assertTrue(scripts.get(1).contains("2);player"));
        Assert.assertTrue(scripts.get(1).contains(", 0);player.getWorld().spawnParticle(Particle.SPELL_MOB"));
        Assert.assertTrue(scripts.get(1).endsWith(";"));
    }
}
