package winclock;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;
import org.jline.utils.NonBlockingReader;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Winclock {

    public static void main(String[] args) {
        Config config = new Config();
        config.parseArgs(args);
        Renderer renderer = new Renderer();

        try (Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build()) {

            terminal.enterRawMode();
            NonBlockingReader reader = terminal.reader();

            renderer.hideCursor(terminal.writer());
            terminal.puts(Capability.enter_ca_mode);
            renderer.clearScreen(terminal.writer());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                renderer.resetFormatting(terminal.writer());
                terminal.puts(Capability.exit_ca_mode);
                renderer.showCursor(terminal.writer());
                renderer.resetCursor(terminal.writer());
                System.out.println("Clock closed cleanly.");
            }));

            int lastWidth = -1;
            int lastHeight = -1;

            while (true) {
                int terminalWidth = terminal.getWidth();
                int terminalHeight = terminal.getHeight();

                if (terminalWidth != lastWidth || terminalHeight != lastHeight) {
                    renderer.clearScreen(terminal.writer());
                    lastWidth = terminalWidth;
                    lastHeight = terminalHeight;
                }

                // Check for input
                int ch = reader.read(10); 
                if (ch != -2) {
                    if (ch == 'q' || ch == 'Q' || ch == 3) {
                        break;
                    } else if (ch == 's' || ch == 'S') {
                        config.setShowSeconds(!config.isShowSeconds());
                        renderer.clearScreen(terminal.writer());
                    } else if (ch == 'b' || ch == 'B') {
                        config.setBlinkColon(!config.isBlinkColon());
                        renderer.clearScreen(terminal.writer());
                    } else if (ch == 't' || ch == 'T') {
                        config.setUse12hFormat(!config.isUse12hFormat());
                        renderer.clearScreen(terminal.writer());
                    }
                }

                LocalTime now = LocalTime.now();
                String dateStr;
                try {
                    dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern(config.getDateFormatStr(), Locale.ENGLISH));
                } catch (Exception e) {
                    dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE MMM d", Locale.ENGLISH));
                }

                renderer.render(terminal.writer(), config, now, dateStr, terminalWidth, terminalHeight);

                Thread.sleep(100);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
