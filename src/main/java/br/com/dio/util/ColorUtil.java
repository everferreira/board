package br.com.dio.util;

import java.util.List;

/**
 * Utilitário para colorir a saída do terminal com códigos ANSI.
 * Funciona em terminais modernos (Linux, macOS, Windows Terminal, VS Code).
 */
public final class ColorUtil {

    public static final String RESET = "\u001B[0m";

    // Cores do texto
    public static final String RED     = "\u001B[31m";
    public static final String GREEN   = "\u001B[32m";
    public static final String YELLOW  = "\u001B[33m";
    public static final String BLUE    = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN    = "\u001B[36m";
    public static final String WHITE   = "\u001B[37m";
    public static final String GRAY    = "\u001B[90m";

    // Cores de fundo
    public static final String BG_RED   = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_BLUE  = "\u001B[44m";

    // Estilos
    public static final String BOLD       = "\u001B[1m";
    public static final String DIM        = "\u001B[2m";
    public static final String UNDERLINE  = "\u001B[4m";

    private ColorUtil() {}

    public static String colorize(final String text, final String color) {
        return color + text + RESET;
    }

    public static String bold(final String text) {
        return BOLD + text + RESET;
    }

    public static String header(final String text) {
        return BOLD + BLUE + "╔══════════════════════════════════════╗\n" +
               "║  " + text + RESET + BOLD + BLUE + "               ║\n" +
               "╚══════════════════════════════════════╝" + RESET;
    }

    public static void printBox(final String title, final List<String> lines) {
        int maxLen = title.length();
        for (var line : lines) {
            maxLen = Math.max(maxLen, stripAnsi(line).length());
        }
        var border = "┌" + "─".repeat(maxLen + 2) + "┐";
        System.out.println(CYAN + border + RESET);
        System.out.println(CYAN + "│ " + BOLD + WHITE + center(title, maxLen) + RESET + CYAN + " │" + RESET);
        if (!lines.isEmpty()) {
            System.out.println(CYAN + "├" + "─".repeat(maxLen + 2) + "┤" + RESET);
            for (var line : lines) {
                var plain = stripAnsi(line);
                System.out.println(CYAN + "│ " + line + " ".repeat(maxLen - plain.length()) + CYAN + " │" + RESET);
            }
        }
        System.out.println(CYAN + "└" + "─".repeat(maxLen + 2) + "┘" + RESET);
    }

    public static void printSuccess(final String msg) {
        System.out.println(GREEN + "✔ " + RESET + msg);
    }

    public static void printError(final String msg) {
        System.out.println(RED + "✘ " + RESET + msg);
    }

    public static void printInfo(final String msg) {
        System.out.println(BLUE + "ℹ " + RESET + msg);
    }

    public static void printWarning(final String msg) {
        System.out.println(YELLOW + "⚠ " + RESET + msg);
    }

    private static String stripAnsi(final String s) {
        return s.replaceAll("\u001B\\[[0-9;]*m", "");
    }

    private static String center(final String s, final int width) {
        var plain = stripAnsi(s);
        var pad = width - plain.length();
        if (pad <= 0) return s;
        var left = pad / 2;
        var right = pad - left;
        return " ".repeat(left) + s + " ".repeat(right);
    }
}