package de.cubeattack.api.util.version;

@FunctionalInterface
public interface CallbackExecutable
{
    void run(VersionUtils.Result result);
}
