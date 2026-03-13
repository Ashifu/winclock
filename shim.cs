using System;
using System.Diagnostics;
using System.IO;
using System.Linq;

class winclock {
    static void Main(string[] args) {
        string exePath = AppDomain.CurrentDomain.BaseDirectory;
        string jarPath = Path.Combine(exePath, "win-clock-1.0.jar");

        if (!File.Exists(jarPath)) {
            Console.WriteLine("Error: win-clock-1.0.jar not found in " + exePath);
            return;
        }

        // Build arguments to pass to Java
        string jarArgs = string.Join(" ", args.Select(a => (a.Contains(" ") ? "\"" + a + "\"" : a)));
        
        ProcessStartInfo startInfo = new ProcessStartInfo {
            FileName = "java",
            Arguments = "-Xmx16M -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -jar \"" + jarPath + "\" " + jarArgs,
            UseShellExecute = false,
            CreateNoWindow = false,
            RedirectStandardOutput = false,
            RedirectStandardError = false
        };

        try {
            using (Process process = Process.Start(startInfo)) {
                process.WaitForExit();
            }
        } catch (Exception ex) {
            Console.WriteLine("Error starting win-clock: " + ex.Message);
        }
    }
}
