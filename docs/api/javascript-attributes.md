# JavaScript 属性

JavaScript 属性适合实现周期粒子、动态外观、特殊条件和无法用属性 DSL 表达的玩法。它是**可选扩展边界**：单个脚本加载、回调或定时任务异常时，只会熔断该脚本属性，不会中断 SX-Attribute、本体战斗事件或其它 JS 文件。

## 运行要求

- 脚本目录：`plugins/SX-Attribute/Attribute/JavaScript/`。
- 每个 `.js` 文件独立创建一个脚本引擎和属性实例；文件名即属性 ID，例如 `Aura.js` 对应属性 `Aura`。
- 配置目录：`plugins/SX-Attribute/Attribute/SX-Attribute/<文件名>_JS.yml`，例如 `Aura_JS.yml`。
- Java 8 使用内置 Nashorn；Java 15 及以上由 SX-Attribute 从 SX-Item 的类加载器发现 Nashorn。
- 不要自行创建 `ScriptEngineManager` 并依赖其返回的引擎；使用注入的 `SXAEngine`。它已处理插件类加载隔离。

内置 `JSAttribute.js` 在启动时会自动迁移旧的 `engine: jsManager.getEngineByName("JavaScript")` 声明，不会改写其它自定义脚本。

## 已注入对象

| 名称 | 类型/用途 |
|---|---|
| `SXAEngine` | 当前隔离的脚本引擎；用于 `compile()`、`createBindings()`。 |
| `SXAttribute` | 插件主类静态入口。 |
| `SXAttributeType` | `ATTACK`、`DEFENCE`、`UPDATE`、`OTHER` 等属性类型。 |
| `FoliaScheduler` | 实体/全局线程安全调度器。 |
| `Bukkit` | Bukkit 静态入口。 |
| `Arrays` | Java `Arrays` 工具类。 |
| `API` | `SXAPI` 实例。 |
| `config` | 当前脚本的 `_JS.yml` 配置，在 `onEnable` 与 `onReLoad` 前注入。 |
| `<文件名去后缀>` | 当前 `JSAttribute` Java 对象，可调用 `put`、`setLength`、`createRunnable`、`getPlugin`。 |

脚本内不要保存跨玩家共享的 `player` 变量。定时逻辑必须通过 `FoliaScheduler.runEntity(player, ...)` 回到目标实体所属线程。

## 脚本契约

脚本至少应声明 `valuesLength` 和 `types`：

```javascript
var valuesLength = 1;
var types = Arrays.asList(SXAttributeType.UPDATE);
var Player = Packages.org.bukkit.entity.Player.class;
var Particle = Packages.org.bukkit.Particle;
```

可选或常用回调如下：

| 回调 | 参数 | 作用 |
|---|---|---|
| `defaultConfig(config)` | `YamlConfiguration` | 首次创建 `_JS.yml` 时写入默认配置，返回配置对象。 |
| `onEnable()` | 无 | 属性配置加载完成后调用。 |
| `onReLoad()` | 无 | `/sxa reload` 时调用。 |
| `onDisable()` | 无 | 插件关闭时调用；应停止自行维护的任务。 |
| `eventMethod(values, eventData)` | 数组、事件数据 | 处理 `ATTACK`/`DEFENCE`/`UPDATE` 等属性事件。 |
| `loadAttribute(values, lore)` | 数组、Lore 行 | 从物品 Lore 累加属性数值。 |
| `getPlaceholder(values, player, key)` | - | 返回一个 Placeholder 值。 |
| `getPlaceholders()` | - | 返回 Placeholder 名称数组。 |
| `calculationCombatPower(values)` | 数组 | 返回战斗力数值。 |
| `correct(values)` | 数组 | 限制或修正计算后的值。 |

缺少可选回调不会报错；开启 `<回调名>_debug = true` 后，会记录缺失回调的调试提示。

## 最小示例

`Attribute/JavaScript/Aura.js`：

```javascript
var valuesLength = 1;
var types = Arrays.asList(SXAttributeType.UPDATE);

function defaultConfig(config) {
    config.set("Aura.DiscernName", "光环");
    config.set("Aura.CombatPower", 10);
    return config;
}

function loadAttribute(values, lore) {
    if (lore.contains(config.getString("Aura.DiscernName"))) values[0] += 1;
}

function eventMethod(values, eventData) {
    if (values[0] <= 0 || eventData.getClass().getSimpleName() !== "UpdateData") return;
    var player = eventData.getEntity();
    if (Player.isInstance(player)) {
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 3);
    }
}

function getPlaceholders() { return ["Aura"]; }
function getPlaceholder(values, player, key) { return key === "Aura" ? values[0] : null; }
function calculationCombatPower(values) { return values[0] * config.getInt("Aura.CombatPower"); }
```

物品 Lore 写为 `光环` 即可让该属性生效。需要周期执行时，按内置 `JSAttribute.js` 的模式编译脚本，并通过 `FoliaScheduler.runEntity` 调度，而非在异步线程直接访问玩家或世界。

## 内置周期模板

内置 `JSAttribute.js` 使用 `List.<ID>` 创建多个子效果，每个节点可配置：

| 路径 | 作用 |
|---|---|
| `DiscernName` | 识别物品 Lore 的文本。 |
| `Initialization` | 启用前执行一次的 JS 行；可初始化 `data`。 |
| `Enabled` | 角色首次拥有属性时按顺序执行的脚本行。 |
| `Disable` | 属性消失时执行的脚本行。 |
| `Continued` | 周期执行的脚本行。 |
| `CombatPower` | 该子效果生效时提供的战斗力。 |

列表中的 `delay 20` 表示后续脚本延迟 20 tick；其它行会被编译为 JavaScript。周期任务默认至少每 20 tick 再次调度一次，避免空列表造成忙循环。

## 事件与线程示例

```javascript
// 先检查事件能力，兼容不同 EventData 实现并避免脚本熔断。
function eventMethod(values, eventData) {
    if (values[0] <= 0 || !eventData || typeof eventData.getEntity !== "function") return;
    var player = eventData.getEntity();
    if (!Player.isInstance(player)) return;
    // 实体和世界操作必须回到所属线程。
    FoliaScheduler.runEntity(player, JSAttribute.getPlugin(), function() {
        player.getWorld().playSound(player.getLocation(), "BLOCK_FIRE_AMBIENT", 0.8, 1.2);
    }, 0);
}
```

可热重载数值应写入 `defaultConfig` 后通过 `config.getDouble` 读取；跨 tick 状态放入 `data`，不要把 `player` 或 `eventData` 保存到全局变量。

## 配置文件示例

脚本首次加载时会创建同名 `_JS.yml`。`defaultConfig` 只负责提供缺省值，服主修改后的值会在重载时继续保留：

```javascript
function defaultConfig(config) {
    // 这些键会落盘到 Attribute/SX-Attribute/Aura_JS.yml，便于不改脚本调整效果。
    config.set("Aura.DiscernName", "光环");
    config.set("Aura.Particle", "END_ROD");
    config.set("Aura.Count", 3);
    config.set("Aura.CombatPower", 10);
    return config;
}

function eventMethod(values, eventData) {
    if (values[0] <= 0 || !eventData || typeof eventData.getEntity !== "function") return;
    var player = eventData.getEntity();
    if (!Player.isInstance(player)) return;
    var count = config.getInt("Aura.Count", 3);
    // 读取配置而不是写死数量，使 /sxa reload 后的新值立即作用于后续事件。
    player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), count);
}
```

## 生命周期与数据隔离

- `loadAttribute` 可能对同一物品 Lore 多次调用，只累加当前 `values`，不要在此创建任务或修改玩家。
- `eventMethod` 的 `values` 是当前实体属性快照；事件对象类型不固定，必须先检查方法或使用 Java 类的 `isInstance`。
- `onEnable` 和 `onReLoad` 适合读取配置、编译脚本和初始化共享常量；重载时应替换旧引用，避免任务重复注册。
- `onDisable` 必须取消脚本自行创建的任务并清空集合；不要继续访问已经卸载的插件对象。
- 每个玩家的计时器、角度和叠层数据应放在该属性实例的 `data` 或玩家绑定中，禁止使用全局 `player`、`eventData` 保存实体引用。

## 常见类型转换

Nashorn 返回的 Bukkit 数值可能是 Java 包装类型。比较前可显式转换为 JavaScript 数字；枚举参数则传入 Bukkit 枚举值：

```javascript
var amount = Number(values[0]);
var sound = Packages.org.bukkit.Sound.BLOCK_FIRE_AMBIENT;
player.getWorld().playSound(player.getLocation(), sound, 0.8, 1.2);
```

## 故障排查

| 日志/现象 | 处理方式 |
|---|---|
| `JavaScript attribute subsystem disabled` | 确认 SX-Item 已启用，且 Java/Nashorn 依赖没有被服务端裁剪。 |
| `createBindings` 为 `null` | 更新到 Beta 2 或更高版本；内置脚本会自动迁移为 `SXAEngine`。 |
| `JS attribute <ID> failed ... and was disabled` | 仅该脚本被熔断；检查对应 JS 与 `_JS.yml` 的语法、变量和实体线程。 |
| 只有某个玩家的效果异常 | 检查 `eventMethod` 是否正确识别 `UpdateData`，以及是否在玩家离线时移除周期状态。 |

脚本不应绕过属性 DSL 来修改来源、数据库或跨服状态；这类需求应优先使用 [属性来源与多服存储](../features/source.md) 与统一属性动作。
