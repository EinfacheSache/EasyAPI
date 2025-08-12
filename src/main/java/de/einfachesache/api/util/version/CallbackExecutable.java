package de.einfachesache.api.util.version;

@FunctionalInterface
public interface CallbackExecutable
{
    void run(VersionUtils.Result result);
}
