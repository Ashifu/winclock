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

    public void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-s":
                case "--seconds":
                    this.showSeconds = true;
                    break;
                case "-ns":
                case "--no-seconds":
                    this.showSeconds = false;
                    break;
                case "-b":
                case "--blink":
                    this.blinkColon = true;
                    break;
                case "-nb":
                case "--no-blink":
                    this.blinkColon = false;
                    break;
                case "-12":
                case "--12h":
                    this.use12hFormat = true;
                    break;
                case "-24":
                case "--24h":
                    this.use12hFormat = false;
                    break;
                case "-box":
                case "--box":
                    this.showBorder = true;
                    break;
                case "-c":
                case "--color":
                    if (i + 1 < args.length) {
                        this.ansiColor = hexToAnsi(args[++i].replace("#", ""));
                    }
                    break;
            }
        }
    }

    public Config() {
        loadFromFile();
    }

    public void loadFromFile() {
        Properties prop = new Properties();
        String userHome = System.getProperty("user.home");
        File configFile = new File(userHome, ".winclock.properties");

        if (!configFile.exists()) {
            return;
        }

        try (FileInputStream fis = new FileInputStream(configFile)) {
            prop.load(fis);

            String hexColor = prop.getProperty("color.hex", "#E5C3C3").replace("#", "");
            this.ansiColor = hexToAnsi(hexColor);
            this.dateFormatStr = prop.getProperty("date.format", "EEE MMM d");
            this.showQuote = Boolean.parseBoolean(prop.getProperty("quote.show", "false"));
            this.quoteText = prop.getProperty("quote.text", "").replace("\"", "");
            this.showSeconds = Boolean.parseBoolean(prop.getProperty("show.seconds", "true"));
            this.blinkColon = Boolean.parseBoolean(prop.getProperty("blink.colon", "true"));
            this.use12hFormat = Boolean.parseBoolean(prop.getProperty("use.12h", "false"));
            this.showBorder = Boolean.parseBoolean(prop.getProperty("show.border", "false"));

        } catch (Exception ex) {
            // Fallback to defaults
        }
    }

    private String hexToAnsi(String hexColor) {
        try {
            int r = Integer.parseInt(hexColor.substring(0, 2), 16);
            int g = Integer.parseInt(hexColor.substring(2, 4), 16);
            int b = Integer.parseInt(hexColor.substring(4, 6), 16);
            return "\033[38;2;" + r + ";" + g + ";" + b + "m";
        } catch (Exception e) {
            return "\033[38;2;229;195;195m";
        }
    }

    // Getters and Setters for interactive changes
    public String getAnsiColor() { return ansiColor; }
    public String getDateFormatStr() { return dateFormatStr; }
    public boolean isShowQuote() { return showQuote; }
    public String getQuoteText() { return quoteText; }
    public boolean isShowSeconds() { return showSeconds; }
    public void setShowSeconds(boolean showSeconds) { this.showSeconds = showSeconds; }
    public boolean isBlinkColon() { return blinkColon; }
    public void setBlinkColon(boolean blinkColon) { this.blinkColon = blinkColon; }
    public boolean isUse12hFormat() { return use12hFormat; }
    public void setUse12hFormat(boolean use12hFormat) { this.use12hFormat = use12hFormat; }
    public boolean isShowBorder() { return showBorder; }
    public void setShowBorder(boolean showBorder) { this.showBorder = showBorder; }
}
