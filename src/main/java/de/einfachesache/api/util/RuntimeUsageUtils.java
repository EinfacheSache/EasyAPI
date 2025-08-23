package de.einfachesache.api.util;

import com.sun.management.OperatingSystemMXBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@SuppressWarnings("unused")
public class RuntimeUsageUtils {

    private static final long KB = 1024L;
    private static final long MB = 1024L * KB;
    private static final OperatingSystemMXBean osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public static int getCpuCores() {
        return osMxBean.getAvailableProcessors();
    }

    public static double getCpuUsage() {
        return osMxBean.getCpuLoad() * 100;
    }

    public static double getProcessCpuUsage() {
        return osMxBean.getProcessCpuLoad() * 100;
    }

    // --- Prozess / JVM ---

    public static long getProcessMaxRam() {
        return Runtime.getRuntime().maxMemory() / MB;
    }

    public static long getProcessFreeRam() {
        return getProcessMaxRam() - getProcessUsedRam();
    }

    public static long getProcessUsedRam() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MB;
    }

    // --- System (Container-aware in neuen JDKs) ---

    public static long getSystemMaxRam() {
        return osMxBean.getTotalMemorySize() / MB;
    }

    public static long getSystemFreeRam() {
        return getSystemMaxRam() - getSystemUsedRam();
    }

    public static long getSystemUsedRam() {
        try {
            Process process = Runtime.getRuntime().exec("free -m");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Mem:")) {
                    Pattern pattern = Pattern.compile("\\d+");
                    Matcher matcher = pattern.matcher(line);
                    int count = 0;
                    while (matcher.find()) {
                        if (count == 1) { // 2. Zahl aus "Mem:"-Zeile = used MB
                            return Long.parseLong(matcher.group());
                        }
                        count++;
                    }
                }
            }
        } catch (IOException e) {
            return getSystemMaxRam() - (osMxBean.getFreeMemorySize() / MB);
        }
        return -1;
    }

    // --- Host-Werte über /proc/meminfo (ignoriert cgroups) ---

    public static long getHostMaxRamMB() {
        return readMeminfoValue("MemTotal:") / MB;
    }

    public static long getHostUsedRamMB() {
        long total = readMeminfoValue("MemTotal:");
        long available = readMeminfoValue("MemAvailable:");
        if (total <= 0 || available <= 0) return -1;
        return (total - available) / MB;
    }

    public static long getHostFreeRamMB() {
        long total = getHostMaxRamMB();
        long used = getHostUsedRamMB();
        if (total < 0 || used < 0) return -1;
        return total - used;
    }

    private static long readMeminfoValue(String key) {
        Path memoryInfo = Path.of("/proc/meminfo");
        if (!Files.isReadable(memoryInfo)) return -1;
        try (BufferedReader br = Files.newBufferedReader(memoryInfo)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(key)) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 3 && "kB".equalsIgnoreCase(parts[2])) {
                        return Long.parseLong(parts[1]) * KB;
                    }
                }
            }
        } catch (IOException | NumberFormatException ignored) {}
        return -1;
    }
}