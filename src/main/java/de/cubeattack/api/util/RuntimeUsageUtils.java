package de.cubeattack.api.util;

import com.sun.management.OperatingSystemMXBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@SuppressWarnings("unused")
public class RuntimeUsageUtils {

    private final static OperatingSystemMXBean osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();


    public static int getCpuCores() {
        return osMxBean.getAvailableProcessors();
    }

    public static double getCpuUsage() {
        return osMxBean.getProcessCpuLoad() * 100;
    }


    public static long getRuntimeMaxRam() {
        return Runtime.getRuntime().maxMemory() / (1024 * 1024);
    }

    public static long getRuntimeFreeRam() {
        return (Runtime.getRuntime().maxMemory() / (1024 * 1024)) - getRuntimeUsedRam();
    }

    public static long getRuntimeUsedRam() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
    }


    public static long getSystemMaxRam() {
        return osMxBean.getTotalMemorySize() / (1024 * 1024);
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
                        if (count == 1) {
                            return Long.parseLong(matcher.group());
                        }
                        count++;
                    }
                }
            }
        } catch (IOException e) {
            return getSystemMaxRam() - (osMxBean.getFreeMemorySize() / (1024 * 1024));
        }
        return Integer.MIN_VALUE;
    }
}
