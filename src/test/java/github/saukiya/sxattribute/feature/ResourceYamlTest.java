package github.saukiya.sxattribute.feature;

import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 校验发布包来源目录中的全部 YAML，防止任一独立模块因示例配置语法错误而无法启动。
 */
public class ResourceYamlTest {

    /**
     * 使用与 Bukkit 相同的 SnakeYAML 解析语法；安全构造器不会尝试实例化仅在 CraftBukkit 中存在的 ItemMeta 实现。
     */
    @Test
    public void parsesEveryBundledYaml() throws Exception {
        Path resources = Paths.get("src", "main", "resources");
        List<Path> files;
        try (Stream<Path> paths = Files.walk(resources)) {
            files = paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".yml"))
                    .collect(Collectors.toList());
        }
        Assert.assertFalse("No YAML resources were discovered", files.isEmpty());
        Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
        for (Path file : files) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                yaml.load(reader);
            }
        }
    }
}
