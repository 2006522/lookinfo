package dev.lookinfo.client;

import dev.lookinfo.LookInfoConfig;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 读准星指向的东西，在屏幕顶部画一块小面板。
 *
 * <p>纯客户端：全部信息都来自客户端已有的状态（{@code Minecraft#hitResult}、方块状态、
 * 实体血量），不向服务端请求任何东西，所以服务器不用装。</p>
 *
 * <p>方块的标题优先显示<b>由注册名推导的英文名</b>（{@code iron_ore} → {@code Iron Ore}），
 * 下面再跟一行当前语言的名称 —— 找 wiki、跟别人描述方块时英文名更通用。</p>
 *
 * <p>已知限制：容器的<b>内容</b>客户端拿不到（原版只在打开界面时同步），所以这里只显示格数。</p>
 */
public class InfoHud implements HudElement {

    private static final int PAD = 4;
    private static final int ROW_H = 10;
    private static final int ROW_H_ICON = 17;
    private static final int ICON_SIZE = 16;
    private static final int ICON_GAP = 2;

    private static final int COL_BG = 0xA0101218;
    private static final int COL_BORDER = 0x50FFFFFF;
    private static final int COL_TITLE = 0xFFFFFFFF;
    /** 当前语言的方块名，压在英文名下面当副标题。 */
    private static final int COL_SUB = 0xFF9AA4B2;
    private static final int COL_KEY = 0xFF9AA4B2;
    private static final int COL_VALUE = 0xFFE6E6E6;
    private static final int COL_GOOD = 0xFF7CE38B;
    private static final int COL_WARN = 0xFFFF7070;

    /** 工具等级从低到高对应的物品，下标就是 {@link #tierOf} 的返回值。 */
    private static final Item[] PICKAXES = {
            Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.COPPER_PICKAXE,
            Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE
    };
    private static final Item[] SHOVELS = {
            Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.COPPER_SHOVEL,
            Items.IRON_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL
    };
    private static final Item[] AXES = {
            Items.WOODEN_AXE, Items.STONE_AXE, Items.COPPER_AXE,
            Items.IRON_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE
    };
    private static final Item[] HOES = {
            Items.WOODEN_HOE, Items.STONE_HOE, Items.COPPER_HOE,
            Items.IRON_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE
    };

    /** 一行：文字 + 可选的行首物品图标。 */
    private record Line(Component text, int color, ItemStack icon) {
        static Line of(Component text, int color) {
            return new Line(text, color, ItemStack.EMPTY);
        }

        int height() {
            return icon.isEmpty() ? ROW_H : ROW_H_ICON;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g) {
        if (!LookInfoConfig.enable) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null || mc.font == null) {
            return;
        }
        // F1 隐藏界面时跟着藏
        if (mc.gui != null && mc.gui.hud != null && mc.gui.hud.isHidden()) {
            return;
        }
        // 开着界面时准星本来就不显示
        if (mc.gui != null && mc.gui.screen() != null) {
            return;
        }

        List<Line> lines = collect(mc);
        if (lines.isEmpty()) {
            return;
        }
        draw(g, mc, lines);
    }

    // ---------------------------------------------------------------- 收集

    private List<Line> collect(Minecraft mc) {
        List<Line> out = new ArrayList<>(7);
        HitResult hit = mc.hitResult;
        if (hit == null) {
            return out;
        }
        if (hit instanceof BlockHitResult block) {
            describeBlock(mc, block.getBlockPos(), out);
        } else if (hit instanceof EntityHitResult entity) {
            describeEntity(entity.getEntity(), out);
        }
        return out;
    }

    private void describeBlock(Minecraft mc, BlockPos pos, List<Line> out) {
        BlockState state = mc.level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        // 标题：中文名后面直接跟英文名（灰色括号），省一行高度
        out.add(Line.of(nameWithEnglish(state.getBlock().getName(), englishName(id)), COL_TITLE));

        if (LookInfoConfig.showSource && id != null && !"minecraft".equals(id.getNamespace())) {
            out.add(Line.of(Component.literal(modName(id.getNamespace())), COL_KEY));
        }

        if (LookInfoConfig.showHardness) {
            float hardness = state.getDestroySpeed(mc.level, pos);
            if (hardness < 0.0F) {
                out.add(Line.of(Component.translatable("lookinfo.unbreakable"), COL_WARN));
            } else {
                out.add(Line.of(Component.translatable("lookinfo.hardness", trim(hardness)), COL_VALUE));
            }
        }

        if (LookInfoConfig.showTool) {
            Item tool = toolFor(state);
            if (tool != null) {
                // 顺手标一下手上这套家伙能不能挖：绿勾 / 红叉
                boolean canHarvest = mc.player != null && mc.player.hasCorrectToolForDrops(state);
                Component text = Component.empty()
                        .append(Component.literal(canHarvest ? "✓ " : "✗ ")
                                .withStyle(Style.EMPTY.withColor(canHarvest ? COL_GOOD : COL_WARN)))
                        .append(Component.translatable("lookinfo.min_tool", toolName(tool)));
                out.add(new Line(text, COL_VALUE, new ItemStack(tool)));
            }
        }

        cropStage(state, out);

        if (LookInfoConfig.showContainer) {
            var be = mc.level.getBlockEntity(pos);
            if (be instanceof Container container) {
                int size = container.getContainerSize();
                if (size > 0) {
                    out.add(Line.of(Component.translatable("lookinfo.container", size), COL_VALUE));
                }
            }
        }
    }

    private void describeEntity(Entity entity, List<Line> out) {
        if (entity == null) {
            return;
        }
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        // 中文名（或玩家自定义名）后面跟英文名
        out.add(Line.of(nameWithEnglish(entity.getName(), englishName(id)), COL_TITLE));

        if (id != null && !"minecraft".equals(id.getNamespace())) {
            out.add(Line.of(Component.literal(modName(id.getNamespace())), COL_KEY));
        }

        if (LookInfoConfig.showHealth && entity instanceof LivingEntity living) {
            float hp = living.getHealth();
            float max = living.getMaxHealth();
            int color = max > 0.0F && hp / max <= 0.3F ? COL_WARN : COL_VALUE;
            out.add(Line.of(Component.translatable("lookinfo.health", trim(hp), trim(max)), color));
            int armor = living.getArmorValue();
            if (armor > 0) {
                out.add(Line.of(Component.translatable("lookinfo.armor", armor), COL_VALUE));
            }
        }
    }

    // ---------------------------------------------------------------- 作物成熟度

    /**
     * 作物显示 {@code 成熟度: 5 / 7}。
     *
     * <p>判定不靠写死方块类：只要有 {@code age} 属性、且属于 {@code minecraft:crops}
     * 或者那几个特殊的（下界疣、可可、甜浆果），就当作物。
     * 甘蔗、仙人掌、竹子的 {@code age} 其实是"长了几节"，不算成熟度，故意排除。</p>
     */
    private void cropStage(BlockState state, List<Line> out) {
        if (!isCrop(state)) {
            return;
        }
        IntegerProperty age = ageProperty(state);
        if (age == null) {
            return;
        }
        int current = state.getValue(age);
        int max = 0;
        for (int value : age.getPossibleValues()) {
            max = Math.max(max, value);
        }
        if (max <= 0) {
            return;
        }
        boolean ripe = current >= max;
        out.add(new Line(
                Component.translatable("lookinfo.maturity", current, max),
                ripe ? COL_GOOD : COL_VALUE,
                ItemStack.EMPTY));
        out.add(Line.of(Component.translatable(ripe ? "lookinfo.ripe" : "lookinfo.growing"),
                ripe ? COL_GOOD : COL_KEY));
    }

    private static boolean isCrop(BlockState state) {
        if (state.is(BlockTags.CROPS)) {
            return true;
        }
        var block = state.getBlock();
        return block instanceof NetherWartBlock
                || block instanceof CocoaBlock
                || block instanceof SweetBerryBushBlock;
    }

    private static IntegerProperty ageProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty integer && "age".equals(property.getName())) {
                return integer;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- 工具

    /**
     * 挖这个方块需要的最低档工具物品。
     *
     * <p>返回 {@code null} 表示"不需要正确工具也能掉东西"（泥土、沙子这类），
     * 这种情况不占一行 —— 玩家真正关心的是"没带对工具挖不下来"。</p>
     */
    private static Item toolFor(BlockState state) {
        if (!state.requiresCorrectToolForDrops()) {
            return null;
        }
        Item[] family = toolFamily(state);
        if (family == null) {
            return null;
        }
        int tier = tierOf(state);
        if (tier < 0) {
            return null;
        }
        return family[Math.min(tier, family.length - 1)];
    }

    /** 方块用哪一类工具挖：镐 / 锹 / 斧 / 锄。 */
    private static Item[] toolFamily(BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return PICKAXES;
        }
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return SHOVELS;
        }
        if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            return AXES;
        }
        if (state.is(BlockTags.MINEABLE_WITH_HOE)) {
            return HOES;
        }
        return null;
    }

    /**
     * 最低可用等级：0 木 / 1 石 / 2 铜 / 3 铁 / 4 钻 / 5 下界合金。
     *
     * <p>原版的 {@code INCORRECT_FOR_*} 标签是"这个工具挖不动"的集合，
     * 所以第一个"不在此集合里"的等级就是最低可用等级。金镐和木镐同档，
     * 没必要单独提，跳过。</p>
     */
    private static int tierOf(BlockState state) {
        if (!state.is(BlockTags.INCORRECT_FOR_WOODEN_TOOL)) {
            return 0;
        }
        if (!state.is(BlockTags.INCORRECT_FOR_STONE_TOOL)) {
            return 1;
        }
        if (!state.is(BlockTags.INCORRECT_FOR_COPPER_TOOL)) {
            return 2;
        }
        if (!state.is(BlockTags.INCORRECT_FOR_IRON_TOOL)) {
            return 3;
        }
        if (!state.is(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)) {
            return 4;
        }
        if (!state.is(BlockTags.INCORRECT_FOR_NETHERITE_TOOL)) {
            return 5;
        }
        return -1;
    }

    // ---------------------------------------------------------------- 名字

    /**
     * 名字后面跟一个英文名：{@code 圆石 (Cobblestone)}。
     *
     * <p>英文环境和中文名相同时就省略括号部分，不然会出现 {@code Stone (Stone)}。</p>
     */
    private static Component nameWithEnglish(Component localized, String english) {
        MutableComponent out = Component.empty().append(localized.copy());
        if (!english.isEmpty() && !english.equalsIgnoreCase(localized.getString())) {
            out.append(Component.literal(" (" + english + ")")
                    .withStyle(Style.EMPTY.withColor(COL_SUB & 0xFFFFFF)));
        }
        return out;
    }

    /** 注册名 → 可读英文名：{@code iron_ore} → {@code Iron Ore}。 */
    private static String englishName(Identifier id) {
        if (id == null) {
            return "";
        }
        String path = id.getPath();
        StringBuilder sb = new StringBuilder(path.length() + 4);
        boolean upper = true;
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '_' || c == '/') {
                sb.append(' ');
                upper = true;
                continue;
            }
            sb.append(upper ? Character.toUpperCase(c) : c);
            upper = false;
        }
        return sb.toString();
    }

    /** modid → 显示名，查不到就用 modid 本身。 */
    private static String modName(String namespace) {
        return FabricLoader.getInstance().getModContainer(namespace)
                .map(container -> container.getMetadata().getName())
                .orElse(namespace);
    }

    private static Component toolName(Item item) {
        return new ItemStack(item).getHoverName();
    }

    /** 整数就不带小数点，避免「硬度: 2.0」。 */
    private static String trim(float value) {
        if (Math.abs(value - Math.round(value)) < 0.05F) {
            return Integer.toString(Math.round(value));
        }
        return String.format("%.1f", value);
    }

    // ---------------------------------------------------------------- 绘制

    private void draw(GuiGraphicsExtractor g, Minecraft mc, List<Line> lines) {
        int width = 0;
        int height = PAD * 2;
        for (Line line : lines) {
            int w = mc.font.width(line.text());
            if (!line.icon().isEmpty()) {
                w += ICON_SIZE + ICON_GAP;
            }
            width = Math.max(width, w);
            height += line.height();
        }
        int boxW = width + PAD * 2;
        int x = (g.guiWidth() - boxW) / 2;
        int y = LookInfoConfig.offsetY;

        g.fill(x - 1, y - 1, x + boxW + 1, y + height + 1, COL_BORDER);
        g.fill(x, y, x + boxW, y + height, COL_BG);

        int rowY = y + PAD;
        for (Line line : lines) {
            int rowH = line.height();
            int textX = x + PAD;
            if (!line.icon().isEmpty()) {
                int iconY = rowY + (rowH - ICON_SIZE) / 2;
                g.item(line.icon(), textX, iconY);
                textX += ICON_SIZE + ICON_GAP;
            }
            int textY = rowY + (rowH - 8) / 2;
            g.text(mc.font, line.text(), textX, textY, line.color());
            rowY += rowH;
        }
    }
}
