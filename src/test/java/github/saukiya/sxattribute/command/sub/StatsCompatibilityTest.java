package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.AttributeConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** 使用发行配置复现旧服面板问题，防止版本门控与默认显示行再次脱节。 */
public class StatsCompatibilityTest {

    private Object previousConfig;
    private Object previousEnabled;
    private int[] previousVersion;

    /** 保存并替换静态服务端上下文，测试只依赖 YAML 与 API，不启动插件。 */
    @Before
    public void setUp() throws Exception {
        previousConfig = field("config").get(null);
        previousEnabled = field("enabled").get(null);
        previousVersion = SXAttribute.getVersionSplit().clone();
        try (InputStreamReader reader = new InputStreamReader(
                getClass().getResourceAsStream("/Feature/Attribute/definitions/builtin.yml"),
                StandardCharsets.UTF_8)) {
            field("config").set(null, YamlConfiguration.loadConfiguration(reader));
        }
        field("enabled").set(null, true);
        version(1, 12, 2);
    }

    /** 静态配置必须恢复，避免改变其它属性测试的注册环境。 */
    @After
    public void tearDown() throws Exception {
        field("config").set(null, previousConfig);
        field("enabled").set(null, previousEnabled);
        System.arraycopy(previousVersion, 0, SXAttribute.getVersionSplit(), 0, previousVersion.length);
    }

    /** 截图中的五种采集属性必须同时从低版本面板排除，而非显示无效数值。 */
    @Test
    public void hidesAllGatherAttributesFromIssue55On1122() {
        for (String name : new String[]{"BlockRange", "MiningEfficiency", "BlockBreakSpeed", "SubmergedMining", "OxygenBonus"}) {
            assertFalse(name, AttributeConfig.isSupported(name));
            assertTrue(name, StatsCommand.containsUnavailableAttribute("&e采集: %sx_" + name + "%%"));
        }
    }

    /** 旧服本来支持的属性与未知扩展占位符不能被兼容性过滤误删。 */
    @Test
    public void preservesLegacyAndUnknownPlaceholders() {
        assertTrue(AttributeConfig.isSupported("Damage"));
        assertTrue(AttributeConfig.isSupported("Luck"));
        assertFalse(StatsCommand.containsUnavailableAttribute("攻击: %sx_Damage% 幸运: %sx_Luck%"));
        assertFalse(StatsCommand.containsUnavailableAttribute("%player_name% %sx_ExternalAttribute%"));
    }

    /** 百分比后缀和其它 PAPI 前缀不能阻止扫描一行中后续的 SX 属性。 */
    @Test
    public void findsUnavailableAttributeAfterOtherPlaceholdersAndPercentSuffix() {
        assertTrue(StatsCommand.containsUnavailableAttribute("%player_name% %sx_CritRate%% / %sx_OxygenBonus%"));
        assertFalse(StatsCommand.containsUnavailableAttribute("%sx_CritRate%% / %sx_Damage%"));
    }

    /** 配置禁用与版本不支持都不应留下会产生 N/A 的面板行。 */
    @Test
    public void hidesExplicitlyDisabledAttributes() {
        AttributeConfig.getSection("Damage").set("Enable", false);
        assertTrue(StatsCommand.containsUnavailableAttribute("%sx_Damage%"));
    }

    /** 配置版本只是下限，不能代替服务端实际提供原版属性。 */
    @Test
    public void rejectsMissingNativeApiEvenWhenVersionRequirementIsLowered() {
        AttributeConfig.getSection("BlockRange").set("Version", "1.9");
        AttributeConfig.getSection("BlockRange").set("RegistryKey", "sx_missing_attribute");
        AttributeConfig.getSection("BlockRange").set("LegacyName", "SX_MISSING_ATTRIBUTE");
        assertFalse(AttributeConfig.isSupported("BlockRange"));
    }

    /** 不能将 1.20.5 的原版能力提前暴露给同一次版本的 1.20.4。 */
    @Test
    public void respectsPatchVersionBeforeResolvingNativeApi() {
        version(1, 20, 4);
        // 借用测试 API 中已有的幸运属性，隔离并验证 1.20.5 的补丁门槛。
        AttributeConfig.getSection("BlockRange").set("RegistryKey", "luck");
        AttributeConfig.getSection("BlockRange").set("LegacyName", "GENERIC_LUCK");
        assertFalse(AttributeConfig.isSupported("BlockRange"));
        version(1, 20, 5);
        assertTrue(AttributeConfig.isSupported("BlockRange"));
        assertFalse(StatsCommand.containsUnavailableAttribute("%sx_BlockRange%"));
    }

    /** 早于 Attribute API 的版本仍需保留插件自己的普通属性。 */
    @Test
    public void skipsNativeApiBeforeMinecraft19() {
        version(1, 8, 8);
        AttributeConfig.getSection("Luck").set("Version", "1.0");
        assertFalse(AttributeConfig.isSupported("Luck"));
        assertTrue(AttributeConfig.isSupported("Damage"));
    }

    /** 不替换 final/缓存对象，只修改插件原有版本数组以模拟服务端环境。 */
    private void version(int major, int minor, int patch) {
        int[] values = SXAttribute.getVersionSplit();
        values[0] = major;
        values[1] = minor;
        values[2] = patch;
    }

    private Field field(String name) throws Exception {
        Field field = AttributeConfig.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
