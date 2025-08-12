package de.einfachesache.api.minecraft.metric;

@SuppressWarnings({"unused", "FieldMayBeFinal"})
public class StatsContainer {

    private String serverType;
    private String serverVersion;
    private String serverName;
    private String javaVersion;
    private String osName;
    private String osArch;
    private String osVersion;
    private String pluginVersion;
    private String versionStatus;
    private String versionError;
    private String updateSetting;
    private String neoProtectPlan;

    private int playerAmount;
    private int managedServers;
    private int coreCount;

    private boolean onlineMode;
    private boolean proxyProtocol;

    public StatsContainer(){
        this.playerAmount = 0;
    }

    public StatsContainer(String serverType, String serverVersion, String serverName, String javaVersion, String osName, String osArch, String osVersion, String pluginVersion, String versionStatus, String versionError, String updateSetting, String neoProtectPlan, int playerAmount, int managedServers, int coreCount, boolean onlineMode, boolean proxyProtocol) {
        this.serverType = serverType.toLowerCase();
        this.serverVersion = serverVersion;
        this.serverName = serverName;
        this.javaVersion = javaVersion;
        this.osName = osName;
        this.osArch = osArch;
        this.osVersion = osVersion;
        this.pluginVersion = pluginVersion;
        this.versionStatus = versionStatus;
        this.versionError = versionError;
        this.updateSetting = updateSetting;
        this.neoProtectPlan = neoProtectPlan;
        this.playerAmount = playerAmount;
        this.managedServers = managedServers;
        this.coreCount = coreCount;
        this.onlineMode = onlineMode;
        this.proxyProtocol = proxyProtocol;
    }

    public String getServerType() {
        return serverType;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public String getServerName() {
        return serverName;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsArch() {
        return osArch;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public String getVersionStatus() {
        return versionStatus;
    }

    public String getVersionError() {
        return versionError;
    }

    public String getUpdateSetting() {
        return updateSetting;
    }

    public String getNeoProtectPlan() {
        return neoProtectPlan;
    }

    public int getPlayerAmount() {
        return playerAmount;
    }

    public int getManagedServers() {
        return managedServers;
    }

    public int getCoreCount() {
        return coreCount;
    }

    public boolean isOnlineMode() {
        return onlineMode;
    }

    public boolean isProxyProtocol() {
        return proxyProtocol;
    }
}
