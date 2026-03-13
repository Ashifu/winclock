using System;
using System.Diagnostics;
using System.IO;
using System.Linq;

/**
 * Native Windows shim for Winclock.
 */
class winclock {
    static void Main(string[] args) {
        string baseDir = AppDomain.CurrentDomain.BaseDirectory;
        string jarPath = Path.Combine(baseDir, "win-clock-1.0.jar");

        if (!File.Exists(jarPath)) {
            Console.WriteLine($"Error: win-clock-1.0.jar not found in {baseDir}");
            return;
        }

        // Sanitize and wrap arguments for the Java process
        string cleanArgs = string.Join(" ", args.Select(arg => arg.Contains(" ") ? $"\"{arg}\"" : arg));
        
        // JVM Optimization Flags:
        // -Xmx16M: Limit max heap
        // -XX:+UseSerialGC: Use the leanest garbage collector
        // -XX:TieredStopAtLevel=1: Reduce JIT overhead for this simple CLI app
        string jvmFlags = "-Xmx16M -XX:+UseSerialGC -XX:TieredStopAtLevel=1";

        ProcessStartInfo startInfo = new ProcessStartInfo {
            FileName = "java",
            Arguments = $"{jvmFlags} -jar \"{jarPath}\" {cleanArgs}",
            UseShellExecute = false,
            CreateNoWindow = false,
            RedirectStandardOutput = false,
            RedirectStandardError = false
        };

        try {
            using (Process process = Process.Start(startInfo)) {
                process?.WaitForExit();
            }
        } catch (Exception ex) {
            Console.WriteLine($"Critical failure starting Winclock: {ex.Message}");
        }
    }
}
