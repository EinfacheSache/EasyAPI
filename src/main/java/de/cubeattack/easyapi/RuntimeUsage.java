package de.cubeattack.easyapi;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

@SuppressWarnings("unused")
public class RuntimeUsage {

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
        return (long) (Runtime.getRuntime().maxMemory() / Math.pow(10,6));
    }

    public static long getFreeRam(){
        return (long) (Runtime.getRuntime().maxMemory() / Math.pow(10,6)) - getUsedRam();
    }
    public static long getUsedRam(){
        return (long) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / Math.pow(10,6));
    }
}
