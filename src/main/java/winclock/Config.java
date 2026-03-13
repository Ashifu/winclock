package winclock;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class Config {
    private String ansiColor = "\033[38;2;229;195;195m"; // Default Peach
    private String dateFormatStr = "EEE MMM d";
    private boolean showQuote = false;
    private String quoteText = "";
    private boolean showSeconds = true;
    private boolean blinkColon = true;
    private boolean use12hFormat = false;
    private boolean showBorder = false;

    public Config() {
        loadFromFile();
    }

    public void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-s", "--seconds" -> showSeconds = true;
                case "-ns", "--no-seconds" -> showSeconds = false;
                case "-b", "--blink" -> blinkColon = true;
                case "-nb", "--no-blink" -> blinkColon = false;
                case "-12", "--12h" -> use12hFormat = true;
                case "-24", "--24h" -> use12hFormat = false;
                case "-box", "--box" -> showBorder = true;
                case "-c", "--color" -> {
                    if (i + 1 < args.length) {
                        ansiColor = hexToAnsi(args[++i].replace("#", ""));
                    }
                }
            }
        }
    }

    private void loadFromFile() {
        Properties prop = new Properties();
        File configFile = new File(System.getProperty("user.home"), ".winclock.properties");

        if (!configFile.exists()) return;

        try (FileInputStream fis = new FileInputStream(configFile)) {
            prop.load(fis);

            ansiColor = hexToAnsi(prop.getProperty("color.hex", "#E5C3C3").replace("#", ""));
            dateFormatStr = prop.getProperty("date.format", "EEE MMM d");
            showQuote = Boolean.parseBoolean(prop.getProperty("quote.show", "false"));
            quoteText = prop.getProperty("quote.text", "").replace("\"", "");
            showSeconds = Boolean.parseBoolean(prop.getProperty("show.seconds", "true"));
            blinkColon = Boolean.parseBoolean(prop.getProperty("blink.colon", "true"));
            use12hFormat = Boolean.parseBoolean(prop.getProperty("use.12h", "false"));
            showBorder = Boolean.parseBoolean(prop.getProperty("show.border", "false"));

        } catch (Exception ignored) {
            // Revert to defaults if file is corrupt
        }
    }

    private String hexToAnsi(String hex) {
        try {
            if (hex.length() != 6) throw new IllegalArgumentException();
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return String.format("\033[38;2;%d;%d;%dm", r, g, b);
        } catch (Exception e) {
            return "\033[38;2;229;195;195m"; // Fallback to default peach
        }
    }

    // Getters
    public String getAnsiColor() { return ansiColor; }
    public String getDateFormatStr() { return dateFormatStr; }
    public boolean isShowQuote() { return showQuote; }
    public String getQuoteText() { return quoteText; }
    
    // Interactive Toggles
    public boolean isShowSeconds() { return showSeconds; }
    public void setShowSeconds(boolean showSeconds) { this.showSeconds = showSeconds; }
    
    public boolean isBlinkColon() { return blinkColon; }
    public void setBlinkColon(boolean blinkColon) { this.blinkColon = blinkColon; }
    
    public boolean isUse12hFormat() { return use12hFormat; }
    public void setUse12hFormat(boolean use12hFormat) { this.use12hFormat = use12hFormat; }
    
    public boolean isShowBorder() { return showBorder; }
    public void setShowBorder(boolean showBorder) { this.showBorder = showBorder; }
}
