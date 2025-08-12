package de.cubeattack.api.minecraft.metric;

import org.jetbrains.annotations.NotNull;

public interface StatsProvider {

    @NotNull
    StatsContainer getStats();
}
