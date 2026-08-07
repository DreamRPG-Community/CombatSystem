# CombatSystem

CombatSystem 是一个面向 Paper 1.12.2 的轻量 RPG Lore 战斗属性插件。

## 依赖

- Java 21
- Paper 1.12.2
- `Lib.jar`

## 支持的 Lore

```text
§4伤害: §f+400-450
§b防御: §f+23%
§a生命值: §f+1700
§2生命回复: §f+5%
§c暴击几率: §f+30%
§4暴击伤害: §f+49%
```

属性从主手和盔甲栏读取，副手不参与。`config.yml` 的 `labels` 只配置可见属性名，不包含颜色；Lore 中的属性名会去掉颜色和格式码后匹配，属性名后必须跟可识别的固定值或范围值。数值颜色可以不同，半角/全角冒号和旧 MythicMobs Lore 格式均可使用。

```yaml
labels:
  damage: "伤害"
  defense: "防御"
  health: "生命值"
  health-regen: "生命回复"
  crit-chance: "暴击几率"
  crit-damage: "暴击伤害"
```

属性分为两类: `DAMAGE`、`DEFENSE`、`HEALTH` 是常驻属性; `HEALTH_REGEN`、`CRIT_CHANCE`、`CRIT_DAMAGE` 是触发属性。触发提示由 `combat-feedback` 控制，文案配置在 `messages.critical-hit` 和 `messages.health-regen`，支持 `{damage}` 与 `{amount}` 占位符。

`CRIT_DAMAGE` 是额外暴击伤害百分比, Lore `+49%` 会使最终暴击倍率变为 `1.49x`; 未配置时额外值为 `0%`, 最终倍率为 `1.0x`。

暴击成功时会在受击者身体中心附近播放原版 `CRIT` 粒子和 `ENTITY_PLAYER_ATTACK_CRIT` 音效。该效果不受 `combat-feedback` 聊天提示开关影响。

玩家攻击非玩家生物时，攻击者会看到类似 ThePit 的临时 Actionbar 血条。血条显示 MythicMob 配置名、自定义名称或实体名称，并显示心形生命段；普通目标每 2 点生命值显示一个心，高生命值目标压缩为最多 40 段，保留 2 秒并每 10 tick 刷新，新的命中会覆盖同一玩家之前的目标。玩家互殴不会显示这条血条。

CombatSystem 对 MythicMobs 使用可选软兼容：命中 MythicMobs 生物仍按 Bukkit `LivingEntity` 处理，并优先读取 MythicMob 的配置显示名；未安装或未启用 MythicMobs 时不影响插件启动，也不会把 MythicMobs API 打进 JAR。

```yaml
combat-feedback: true
messages:
  critical-hit: "&6暴击! &f造成 &c{damage} &f点伤害"
  health-regen: "&a生命回复 &f+{amount}"
```

## 指令

```text
/combatsystem stats
/combatsystem reload
/combatsystem debug
```

`/cs` 是 `/combatsystem` 的缩写；帮助、错误和用法提示始终显示完整的 `/combatsystem`。

`/combatsystem stats` 的面板固定使用以下顺序和颜色，仅显示 CombatSystem 当前已实现的六项属性:

```text
&8&lCombatSystem &f| &7战斗属性
  &f
  &4伤害: &f100
  &b防御: &f100
  &a生命值: &f100
  &2生命回复: &f100%
  &c暴击几率: &f100%
  &4暴击伤害: &f100%
  &f
```

源码按职责分包: `lore` 保存 Lore 属性模型和解析器, `stats` 负责装备汇总, `combat` 负责战斗公式, `listener` 负责 Bukkit 事件, `command` 负责指令, `api` 提供只读接口。后续战斗模块可以在独立包中继续扩展。
