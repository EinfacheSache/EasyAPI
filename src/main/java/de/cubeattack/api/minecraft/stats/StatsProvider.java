package de.cubeattack.api.minecraft.stats;

import org.jetbrains.annotations.NotNull;

public interface StatsProvider {

    @NotNull
    Stats getStats();
}
