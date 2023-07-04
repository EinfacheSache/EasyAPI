package de.cubeattack.api.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

@SuppressWarnings("unused")
public class RuntimeUsageUtils {

    public static double getCpuUsage(){
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        if (threadBean.isThreadCpuTimeSupported() && threadBean.isThreadCpuTimeEnabled()) {
            long[] threadIds = threadBean.getAllThreadIds();
            double totalCpuLoad = 0;

            for (long threadId : threadIds) {
                totalCpuLoad += threadBean.getThreadCpuTime(threadId);
            }

            return totalCpuLoad / (threadIds.length * 1000000);
        } else {
            return -1;
        }
    }

    public static long getMaxRam(){
        return Runtime.getRuntime().maxMemory() / (1024 * 1024);
    }

    public static long getFreeRam(){
        return (Runtime.getRuntime().maxMemory() / (1024 * 1024)) - getUsedRam();
    }
    public static long getUsedRam(){
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
    }
}
