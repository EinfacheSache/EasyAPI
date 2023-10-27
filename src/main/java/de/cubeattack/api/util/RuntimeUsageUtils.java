package de.cubeattack.api.util;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;


@SuppressWarnings("unused")
public class RuntimeUsageUtils {

    private final static OperatingSystemMXBean osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();


    public static void test() {
        System.out.println(osMxBean.getCommittedVirtualMemorySize());
        System.out.println(osMxBean.getFreePhysicalMemorySize());
        System.out.println(osMxBean.getTotalPhysicalMemorySize());
    }

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
        return osMxBean.getFreeMemorySize() / (1024 * 1024);
    }

    public static long getSystemUsedRam() {
        return getSystemMaxRam() - getSystemFreeRam();
    }
}
