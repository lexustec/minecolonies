package com.minecolonies.coremod.colony.managers;

import com.google.common.collect.Lists;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.block.BlockState;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

public class PathBuildingManager {

    private final IColony colony;

    private final Map<Long, List<BlockPos>> traversedPosOnTick = new HashMap<>();
    private final Map<BlockPos, Long> traversalCountOnPos = new HashMap<>();

    public PathBuildingManager(final IColony colony) {
        this.colony = colony;
    }

    public void onCitizenTick(AbstractEntityCitizen citizen) {
        if (!IMinecoloniesAPI.getInstance().getConfig().getCommon().autoCreatePathsEnabled.get())
            return;

        final BlockPos currentPos = citizen.getPosition();

        final Long currentTime = citizen.getEntityWorld().getGameTime();

        final boolean isInsideBuilding = Objects.requireNonNull(citizen.getCitizenData()).getColony().getBuildingManager().getBuildings().values().stream().noneMatch((building) -> {
            final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners = building.getCorners();
            final int minX = corners.getA().getA();
            final int maxX = corners.getA().getB();
            final int minZ = corners.getB().getA();
            final int maxZ = corners.getB().getB();

            return minX <= currentPos.getX() && currentPos.getX() <= maxX && minZ <= currentPos.getZ() && currentPos.getZ() <= maxZ;
        });

        if (isInsideBuilding)
            return;

        this.traversedPosOnTick.computeIfAbsent(currentTime, time -> Lists.newArrayList()).add(currentPos);
        this.traversalCountOnPos.compute(currentPos, (BlockPos pos, Long currentCount) -> currentCount == null ? 1 : currentCount + 1);
    }

    public void onServerTick(IColony colony) {
        if (!IMinecoloniesAPI.getInstance().getConfig().getCommon().autoCreatePathsEnabled.get())
            return;

        final Long currentTime = colony.getWorld().getGameTime();
        final List<Long> ticksToRemove = traversedPosOnTick.keySet().stream().filter(tick -> tick < (currentTime - IMinecoloniesAPI.getInstance().getConfig().getCommon().maxTrackingAge.get())).collect(Collectors.toList());

        for (final Long tick : ticksToRemove) {
            final List<BlockPos> posToDecrease = traversedPosOnTick.remove(tick);
            for (final BlockPos pos : posToDecrease) {
                traversalCountOnPos.compute(pos, (p, l) -> l == null || l == 1 ? null : l - 1);
            }
        }
    }
}
