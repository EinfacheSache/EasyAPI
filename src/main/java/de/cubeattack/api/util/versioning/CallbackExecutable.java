package de.cubeattack.api.util.versioning;

@FunctionalInterface
public interface CallbackExecutable
{
    void run(VersionUtils.Result result);
}
