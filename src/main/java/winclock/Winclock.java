package winclock;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Winclock {
    private final Config config = new Config();
    private final Renderer renderer = new Renderer();
    private Terminal terminal;

    public static void main(String[] args) {
        new Winclock().run(args);
    }

    public void run(String[] args) {
        config.parseArgs(args);

        try {
            initTerminal();
            setupShutdownHook();
            mainLoop();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            if (terminal != null) {
                restoreTerminal();
            }
        }
    }

    private void initTerminal() throws IOException {
        terminal = TerminalBuilder.builder()
                .system(true)
                .build();
        
        terminal.enterRawMode();
        terminal.puts(Capability.enter_ca_mode);
        renderer.hideCursor(terminal.writer());
        renderer.clearScreen(terminal.writer());
    }

    private void mainLoop() throws Exception {
        NonBlockingReader reader = terminal.reader();
        int lastWidth = -1;
        int lastHeight = -1;

        while (true) {
            int width = terminal.getWidth();
            int height = terminal.getHeight();

            // Refresh if terminal size changed
            if (width != lastWidth || height != lastHeight) {
                renderer.clearScreen(terminal.writer());
                lastWidth = width;
                lastHeight = height;
            }

            if (handleInput(reader)) break;

            renderFrame(width, height);
            
            // 10 FPS is plenty for a digital clock
            Thread.sleep(100);
        }
    }

    private boolean handleInput(NonBlockingReader reader) throws IOException {
        int ch = reader.read(10);
        if (ch == -2) return false; // No input available

        switch (Character.toLowerCase(ch)) {
            case 'q', 3: // 'q' or Ctrl+C
                return true;
            case 's':
                config.setShowSeconds(!config.isShowSeconds());
                break;
            case 'b':
                config.setBlinkColon(!config.isBlinkColon());
                break;
            case 't':
                config.setUse12hFormat(!config.isUse12hFormat());
                break;
            default:
                return false;
        }
        
        // Clear screen on toggle to avoid ghosting
        renderer.clearScreen(terminal.writer());
        return false;
    }

    private void renderFrame(int width, int height) {
        LocalTime now = LocalTime.now();
        String dateStr;
        
        try {
            dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern(config.getDateFormatStr(), Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            // Fallback for invalid custom patterns
            dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE MMM d", Locale.ENGLISH));
        }

        renderer.render(terminal.writer(), config, now, dateStr, width, height);
    }

    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::restoreTerminal));
    }

    private void restoreTerminal() {
        if (terminal == null) return;
        
        renderer.resetFormatting(terminal.writer());
        terminal.puts(Capability.exit_ca_mode);
        renderer.showCursor(terminal.writer());
        renderer.resetCursor(terminal.writer());
        
        try {
            terminal.close();
        } catch (IOException ignored) {}
        
        System.out.println("Winclock closed cleanly.");
    }
}
