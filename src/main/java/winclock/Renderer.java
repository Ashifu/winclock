package winclock;

import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Renderer {
    private static final String[][] DIGITS = {
            { "██████", "██  ██", "██  ██", "██  ██", "██████" }, // 0
            { "    ██", "    ██", "    ██", "    ██", "    ██" }, // 1 (Right-aligned within 6-wide box)
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

    public void render(PrintWriter writer, Config config, LocalTime now, String dateStr, int terminalWidth, int terminalHeight) {
        boolean showColon = !config.isBlinkColon() || now.getSecond() % 2 == 0;
        String timePattern = config.isUse12hFormat() ? "hh:mm" : "HH:mm";
        if (config.isShowSeconds()) {
            timePattern += ":ss";
        }
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(timePattern);
        String timeStr = now.format(timeFormatter);

        int clockWidth = calculateWidth(timeStr);
        String quoteText = config.getQuoteText();
        int contentWidth = Math.max(clockWidth, dateStr.length());
        if (config.isShowQuote()) {
            contentWidth = Math.max(contentWidth, quoteText.length());
        }

        int boxWidth = contentWidth + (config.isShowBorder() ? 4 : 0);
        int clockHeight = 5 + 1 + 1 + (config.isShowQuote() ? 2 : 0); 
        if (config.isShowBorder()) {
            clockHeight += 2;
        }

        int startRow = Math.max(0, (terminalHeight - clockHeight) / 2);
        int startCol = Math.max(0, (terminalWidth - boxWidth) / 2);

        StringBuilder frame = new StringBuilder();
        frame.append("\033[H"); // Home
        frame.append(config.getAnsiColor());

        // We'll build the entire relevant area. To avoid scrolling, we stop at the last row of the clock.
        // For lines above the clock:
        for (int i = 0; i < startRow; i++) {
            frame.append(" ".repeat(terminalWidth)).append("\n");
        }

        // Top Border
        if (config.isShowBorder()) {
            String line = " ".repeat(startCol) + "┌" + "─".repeat(boxWidth - 2) + "┐";
            frame.append(line).append(" ".repeat(Math.max(0, terminalWidth - line.length()))).append("\n");
        }

        // Clock Rows
        int clockPadding = (contentWidth - clockWidth) / 2;
        for (int row = 0; row < 5; row++) {
            StringBuilder lineSb = new StringBuilder(" ".repeat(startCol));
            if (config.isShowBorder()) lineSb.append("│ ");
            lineSb.append(" ".repeat(clockPadding));
            for (char c : timeStr.toCharArray()) {
                if (c == ':') {
                    lineSb.append(showColon ? COLON[row] : EMPTY_COLON[row]).append("  ");
                } else if (Character.isDigit(c)) {
                    lineSb.append(DIGITS[c - '0'][row]).append("  ");
                } else {
                    lineSb.append("      ").append("  "); 
                }
            }
            lineSb.append(" ".repeat(contentWidth - clockWidth - clockPadding));
            if (config.isShowBorder()) lineSb.append(" │");
            
            String line = lineSb.toString();
            frame.append(line).append(" ".repeat(Math.max(0, terminalWidth - line.length()))).append("\n");
        }

        // Gap
        String gapLine = " ".repeat(startCol) + (config.isShowBorder() ? "│ " + " ".repeat(contentWidth) + " │" : "");
        frame.append(gapLine).append(" ".repeat(Math.max(0, terminalWidth - gapLine.length()))).append("\n");

        // Date Row
        int datePadding = (contentWidth - dateStr.length()) / 2;
        String dateText = " ".repeat(datePadding) + dateStr + " ".repeat(contentWidth - dateStr.length() - datePadding);
        String dateLine = " ".repeat(startCol) + (config.isShowBorder() ? "│ " : "") + dateText + (config.isShowBorder() ? " │" : "");
        frame.append(dateLine).append(" ".repeat(Math.max(0, terminalWidth - dateLine.length()))).append("\n");

        // Quote Row
        if (config.isShowQuote()) {
            String quoteGap = " ".repeat(startCol) + (config.isShowBorder() ? "│ " + " ".repeat(contentWidth) + " │" : "");
            frame.append(quoteGap).append(" ".repeat(Math.max(0, terminalWidth - quoteGap.length()))).append("\n");
            
            int quotePadding = (contentWidth - quoteText.length()) / 2;
            String quoteTextPadded = " ".repeat(quotePadding) + quoteText + " ".repeat(contentWidth - quoteText.length() - quotePadding);
            // Note: We handle italics with escape codes inside the line, but they don't count for length
            String quoteLine = " ".repeat(startCol) + (config.isShowBorder() ? "│ " : "") + "\033[3m" + quoteTextPadded + "\033[23m" + (config.isShowBorder() ? " │" : "");
            // Length calculation for padding needs to be careful with escape codes
            int visibleLen = startCol + (config.isShowBorder() ? 2 : 0) + quoteTextPadded.length() + (config.isShowBorder() ? 2 : 0);
            frame.append(quoteLine).append(" ".repeat(Math.max(0, terminalWidth - visibleLen))).append("\n");
        }

        // Bottom Border
        if (config.isShowBorder()) {
            String line = " ".repeat(startCol) + "└" + "─".repeat(boxWidth - 2) + "┘";
            frame.append(line).append(" ".repeat(Math.max(0, terminalWidth - line.length()))).append("\n");
        }
        
        writer.print(frame.toString());
        writer.flush();
    }

    private int calculateWidth(String timeStr) {
        int width = 0;
        for (char c : timeStr.toCharArray()) {
            if (c == ':') {
                width += COLON[0].length() + 2;
            } else if (Character.isDigit(c)) {
                width += 6 + 2; 
            } else {
                width += 6 + 2; 
            }
        }
        return width;
    }
}
