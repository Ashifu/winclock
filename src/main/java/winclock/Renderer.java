package winclock;

import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Renderer {
    private static final String[][] DIGITS = {
            { "██████", "██  ██", "██  ██", "██  ██", "██████" }, // 0
            { "    ██", "    ██", "    ██", "    ██", "    ██" }, // 1
            { "██████", "    ██", "██████", "██    ", "██████" }, // 2
            { "██████", "    ██", "██████", "    ██", "██████" }, // 3
            { "██  ██", "██  ██", "██████", "    ██", "    ██" }, // 4
            { "██████", "██    ", "██████", "    ██", "██████" }, // 5
            { "██████", "██    ", "██████", "██  ██", "██████" }, // 6
            { "██████", "    ██", "    ██", "    ██", "    ██" }, // 7
            { "██████", "██  ██", "██████", "██  ██", "██████" }, // 8
            { "██████", "██  ██", "██████", "    ██", "██████" } // 9
    };

    private static final String[] COLON = { "  ", "██", "  ", "██", "  " };
    private static final String[] EMPTY_COLON = { "  ", "  ", "  ", "  ", "  " };

    public void clearScreen(PrintWriter writer) {
        writer.print("\033[2J\033[H");
        writer.flush();
    }

    public void resetCursor(PrintWriter writer) {
        writer.print("\033[H");
        writer.flush();
    }

    public void hideCursor(PrintWriter writer) {
        writer.print("\033[?25l");
        writer.flush();
    }

    public void showCursor(PrintWriter writer) {
        writer.print("\033[?25h");
        writer.flush();
    }

    public void resetFormatting(PrintWriter writer) {
        writer.print("\033[0m");
        writer.flush();
    }

    public void render(PrintWriter writer, Config config, LocalTime now, String dateStr, int termWidth,
            int termHeight) {
        String timeStr = formatTime(now, config);
        int clockWidth = calculateClockWidth(timeStr);
        String quoteText = config.getQuoteText();

        // Horizontal Layout
        int contentWidth = Math.max(clockWidth, dateStr.length());
        if (config.isShowQuote()) {
            contentWidth = Math.max(contentWidth, quoteText.length());
        }

        int boxWidth = contentWidth + (config.isShowBorder() ? 4 : 0);
        int startCol = Math.max(0, (termWidth - boxWidth) / 2);

        // Vertical Layout
        int clockHeight = 5 + 2; // Time row + gap + date row
        if (config.isShowQuote())
            clockHeight += 2;
        if (config.isShowBorder())
            clockHeight += 2;

        int startRow = Math.max(0, (termHeight - clockHeight) / 2);

        StringBuilder frame = new StringBuilder();
        frame.append("\033[H"); // Cursor home
        frame.append(config.getAnsiColor());

        // Top Padding
        for (int i = 0; i < startRow; i++) {
            frame.append(" ".repeat(termWidth)).append("\n");
        }

        if (config.isShowBorder()) {
            appendTitledLine(frame, "┌", "┐", "─", boxWidth, startCol, termWidth);
        }

        // ASCII Clock Digits
        boolean showColon = !config.isBlinkColon() || now.getSecond() % 2 == 0;
        int clockPadding = (contentWidth - clockWidth) / 2;

        for (int row = 0; row < 5; row++) {
            StringBuilder line = new StringBuilder(" ".repeat(startCol));
            if (config.isShowBorder())
                line.append("│ ");
            line.append(" ".repeat(clockPadding));

            for (char c : timeStr.toCharArray()) {
                if (c == ':') {
                    line.append(showColon ? COLON[row] : EMPTY_COLON[row]).append("  ");
                } else if (Character.isDigit(c)) {
                    line.append(DIGITS[c - '0'][row]).append("  ");
                }
            }

            line.append(" ".repeat(contentWidth - clockWidth - clockPadding));
            if (config.isShowBorder())
                line.append(" │");

            finalizeLine(frame, line.toString(), termWidth);
        }

        // Gap
        appendEmptyLine(frame, config.isShowBorder(), contentWidth, startCol, termWidth);

        // Date Row
        appendCenteredLine(frame, dateStr, config.isShowBorder(), contentWidth, startCol, termWidth);

        // Quote Row
        if (config.isShowQuote()) {
            appendEmptyLine(frame, config.isShowBorder(), contentWidth, startCol, termWidth);
            String styledQuote = "\033[3m" + quoteText + "\033[23m";
            appendCenteredLine(frame, styledQuote, config.isShowBorder(), contentWidth, startCol, termWidth,
                    quoteText.length());
        }

        if (config.isShowBorder()) {
            appendTitledLine(frame, "└", "┘", "─", boxWidth, startCol, termWidth);
        }

        writer.print(frame);
        writer.flush();
    }

    private String formatTime(LocalTime now, Config config) {
        String pattern = config.isUse12hFormat() ? "hh:mm" : "HH:mm";
        if (config.isShowSeconds())
            pattern += ":ss";
        return now.format(DateTimeFormatter.ofPattern(pattern));
    }

    private void appendCenteredLine(StringBuilder sb, String text, boolean border, int contentWidth, int startCol,
            int termWidth) {
        appendCenteredLine(sb, text, border, contentWidth, startCol, termWidth, text.length());
    }

    private void appendCenteredLine(StringBuilder sb, String text, boolean border, int contentWidth, int startCol,
            int termWidth, int visibleLen) {
        int padding = (contentWidth - visibleLen) / 2;
        StringBuilder line = new StringBuilder(" ".repeat(startCol));

        if (border)
            line.append("│ ");
        line.append(" ".repeat(padding)).append(text).append(" ".repeat(contentWidth - visibleLen - padding));
        if (border)
            line.append(" │");

        int totalVisible = startCol + (border ? 4 : 0) + contentWidth;
        finalizeLine(sb, line.toString(), termWidth, totalVisible);
    }

    private void appendEmptyLine(StringBuilder sb, boolean border, int contentWidth, int startCol, int termWidth) {
        String content = border ? "│ " + " ".repeat(contentWidth) + " │" : "";
        finalizeLine(sb, " ".repeat(startCol) + content, termWidth);
    }

    private void appendTitledLine(StringBuilder sb, String open, String close, String fill, int width, int startCol,
            int termWidth) {
        String line = " ".repeat(startCol) + open + fill.repeat(width - 2) + close;
        finalizeLine(sb, line, termWidth);
    }

    private void finalizeLine(StringBuilder sb, String line, int termWidth) {
        finalizeLine(sb, line, termWidth, line.length());
    }

    private void finalizeLine(StringBuilder sb, String line, int termWidth, int visibleLen) {
        sb.append(line).append(" ".repeat(Math.max(0, termWidth - visibleLen))).append("\n");
    }

    private int calculateClockWidth(String timeStr) {
        int width = 0;
        for (char c : timeStr.toCharArray()) {
            width += (c == ':' ? 2 : 6) + 2; // Char width + space
        }
        return width;
    }
}
