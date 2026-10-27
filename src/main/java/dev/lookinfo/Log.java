package dev.lookinfo;

/** 统一日志前缀，方便在 logs 里搜 {@code [LookInfo]}。 */
public final class Log {

    private static final System.Logger LOGGER = System.getLogger("LookInfo");

    private Log() {
    }

    public static void info(String message) {
        LOGGER.log(System.Logger.Level.INFO, "[LookInfo] " + message);
    }

    public static void warn(String message) {
        LOGGER.log(System.Logger.Level.WARNING, "[LookInfo] " + message);
    }

    public static void error(String message, Throwable throwable) {
        LOGGER.log(System.Logger.Level.ERROR, "[LookInfo] " + message, throwable);
    }
}
