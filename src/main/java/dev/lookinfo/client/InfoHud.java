package dev.lookinfo.client;

import dev.lookinfo.LookInfoConfig;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
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
 * <p>已知限制：容器的<b>内容</b>客户端拿不到（原版只在打开界面时同步），所以这里只显示格数。</p>
 */
public class InfoHud implements HudElement {

    private static final int PAD = 4;
    private static final int LINE_H = 10;

    private static final int COL_BG = 0xA0101218;
    private static final int COL_BORDER = 0x50FFFFFF;
    private static final int COL_TITLE = 0xFFFFFFFF;
    private static final int COL_KEY = 0xFF9AA4B2;
    private static final int COL_VALUE = 0xFFE6E6E6;
    private static final int COL_WARN = 0xFFFF7070;

    /** 一行：整行一个颜色，简单够用。 */
    private record Line(Component text, int color) {
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
        List<Line> out = new ArrayList<>(6);
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
        out.add(new Line(state.getBlock().getName(), COL_TITLE));

        if (LookInfoConfig.showSource) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (id != null) {
                String ns = id.getNamespace();
                // 原版方块不占一行，省得每条都写 minecraft
                if (!"minecraft".equals(ns)) {
                    out.add(new Line(Component.literal(modName(ns)), COL_KEY));
                }
            }
        }

        if (LookInfoConfig.showHardness) {
            float hardness = state.getDestroySpeed(mc.level, pos);
            if (hardness < 0.0F) {
                out.add(new Line(Component.translatable("lookinfo.unbreakable"), COL_WARN));
            } else {
                out.add(new Line(Component.translatable("lookinfo.hardness",
                        trim(hardness)), COL_VALUE));
            }
        }

        if (LookInfoConfig.showTool && state.requiresCorrectToolForDrops()) {
            out.add(new Line(Component.translatable("lookinfo.need_tool"), COL_VALUE));
        }

        if (LookInfoConfig.showContainer) {
            var be = mc.level.getBlockEntity(pos);
            if (be instanceof Container container) {
                int size = container.getContainerSize();
                if (size > 0) {
                    out.add(new Line(Component.translatable("lookinfo.container", size), COL_VALUE));
                }
            }
        }
    }

    private void describeEntity(Entity entity, List<Line> out) {
        if (entity == null) {
            return;
        }
        out.add(new Line(entity.getName(), COL_TITLE));

        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (id != null && !"minecraft".equals(id.getNamespace())) {
            out.add(new Line(Component.literal(modName(id.getNamespace())), COL_KEY));
        }

        if (LookInfoConfig.showHealth && entity instanceof LivingEntity living) {
            float hp = living.getHealth();
            float max = living.getMaxHealth();
            int color = max > 0.0F && hp / max <= 0.3F ? COL_WARN : COL_VALUE;
            out.add(new Line(Component.translatable("lookinfo.health",
                    trim(hp), trim(max)), color));
            int armor = living.getArmorValue();
            if (armor > 0) {
                out.add(new Line(Component.translatable("lookinfo.armor", armor), COL_VALUE));
            }
        }
    }

    /** modid → 显示名，查不到就用 modid 本身。 */
    private static String modName(String namespace) {
        return FabricLoader.getInstance().getModContainer(namespace)
                .map(container -> container.getMetadata().getName())
                .orElse(namespace);
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
        for (Line line : lines) {
            width = Math.max(width, mc.font.width(line.text()));
        }
        int boxW = width + PAD * 2;
        int boxH = lines.size() * LINE_H + PAD * 2;
        int x = (g.guiWidth() - boxW) / 2;
        int y = LookInfoConfig.offsetY;

        g.fill(x - 1, y - 1, x + boxW + 1, y + boxH + 1, COL_BORDER);
        g.fill(x, y, x + boxW, y + boxH, COL_BG);

        int textY = y + PAD;
        for (Line line : lines) {
            g.text(mc.font, line.text(), x + PAD, textY, line.color());
            textY += LINE_H;
        }
    }
}
