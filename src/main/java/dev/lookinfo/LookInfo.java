package dev.lookinfo;

import dev.lookinfo.client.InfoHud;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * 客户端入口：读配置、挂 HUD、注册 I 键。
 *
 * <p>纯客户端 mod（{@code environment: client}），服务器不用装。</p>
 */
public class LookInfo implements ClientModInitializer {

    private static KeyMapping toggleKey;

    @Override
    public void onInitializeClient() {
        LookInfoConfig.loadIfNeeded();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.parse("lookinfo"));
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.lookinfo.toggle", InputConstants.Type.KEYBOARD, InputConstants.KEY_I, category));

        HudElementRegistry.addLast(Identifier.parse("lookinfo:hud"), new InfoHud());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey != null && toggleKey.consumeClick()) {
                LookInfoConfig.enable = !LookInfoConfig.enable;
                LookInfoConfig.save();
            }
        });

        Log.info("已加载：准星指到方块或生物时在屏幕顶部显示信息（I 键开关）");
    }
}
