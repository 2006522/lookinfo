package dev.lookinfo;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

/**
 * 配置读写，文件在 {@code config/lookinfo.properties}。
 *
 * <p>选项很少，没做图形界面：改文件重启生效，游戏里按 {@code I} 切总开关。</p>
 */
public final class LookInfoConfig {

    /** 总开关。 */
    public static boolean enable = true;

    /** 面板离屏幕顶边的距离（GUI 像素）。 */
    public static int offsetY = 6;

    /** 显示方块/实体所属的模组名。 */
    public static boolean showSource = true;

    /** 显示"需要正确工具"提示。 */
    public static boolean showTool = true;

    /** 显示容器格数。 */
    public static boolean showContainer = true;

    /** 显示生物血量。 */
    public static boolean showHealth = true;

    private static boolean loaded;

    private LookInfoConfig() {
    }

    public static void loadIfNeeded() {
        if (loaded) {
            return;
        }
        reload();
    }

    public static void reload() {
        loaded = true;
        Path file = path();
        Properties p = new Properties();
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                p.load(in);
            } catch (Exception e) {
                Log.warn("读取配置失败，使用默认值: " + e.getMessage());
            }
        }
        enable = bool(p, "enable", enable);
        offsetY = clampInt(p, "offset_y", offsetY, 0, 200);
        showSource = bool(p, "show_source", showSource);
        showTool = bool(p, "show_tool", showTool);
        showContainer = bool(p, "show_container", showContainer);
        showHealth = bool(p, "show_health", showHealth);
        if (!Files.exists(file)) {
            save();
        }
    }

    public static void save() {
        Path file = path();
        Properties out = new Properties();
        out.setProperty("enable", Boolean.toString(enable));
        out.setProperty("offset_y", Integer.toString(offsetY));
        out.setProperty("show_source", Boolean.toString(showSource));
        out.setProperty("show_tool", Boolean.toString(showTool));
        out.setProperty("show_container", Boolean.toString(showContainer));
        out.setProperty("show_health", Boolean.toString(showHealth));
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream os = Files.newOutputStream(file)) {
                out.store(os, "LookInfo - what am I looking at (press I in game to toggle)");
            }
        } catch (Exception e) {
            Log.warn("写入配置失败: " + e.getMessage());
        }
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("lookinfo.properties");
    }

    private static boolean bool(Properties p, String key, boolean fallback) {
        String raw = p.getProperty(key);
        return raw == null ? fallback : Boolean.parseBoolean(raw.trim());
    }

    private static int clampInt(Properties p, String key, int fallback, int min, int max) {
        String raw = p.getProperty(key);
        if (raw == null) {
            return fallback;
        }
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(raw.trim())));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
