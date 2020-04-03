package com.minecolonies.coremod.colony.managers;

import com.google.common.collect.Lists;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.managers.interfaces.IPathBuildingManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.Log;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

public class PathBuildingManager implements IPathBuildingManager {

    private final IColony colony;
    private final Random rand = new Random();

    private final Map<Long, List<BlockPos>> traversedPosOnTick = new HashMap<>();
    private final Map<BlockPos, List<Long>> traversedTickForPos = new HashMap<>();
    private final Map<BlockPos, Long> traversalCountOnPos = new HashMap<>();

    public PathBuildingManager(final IColony colony) {
        this.colony = colony;
    }

    @Override
    public void onCitizenTick(AbstractEntityCitizen citizen) {
        if (!IMinecoloniesAPI.getInstance().getConfig().getCommon().autoCreatePathsEnabled.get())
            return;

        final BlockPos currentPos = citizen.getPosition();
        final BlockPos placementTarget = currentPos.down();

        final BlockState topState = colony.getWorld().getBiome(placementTarget).getSurfaceBuilderConfig().getTop();
        if (citizen.getEntityWorld().getBlockState(placementTarget).getBlock() == Blocks.GRASS_PATH) {
            int pathCountInEnvironment = 0;
            if (colony.getWorld().getBlockState(placementTarget.north()).getBlock() == Blocks.GRASS_PATH)
                pathCountInEnvironment++;

            if (colony.getWorld().getBlockState(placementTarget.south()).getBlock() == Blocks.GRASS_PATH)
                pathCountInEnvironment++;

            if (colony.getWorld().getBlockState(placementTarget.west()).getBlock() == Blocks.GRASS_PATH)
                pathCountInEnvironment++;

            if (colony.getWorld().getBlockState(placementTarget.east()).getBlock() == Blocks.GRASS_PATH)
                pathCountInEnvironment++;

            if (pathCountInEnvironment >= 3) {
                Log.getLogger().debug("Not considering pathing because path already made: " + currentPos);
                return;
            }
        } else if (citizen.getEntityWorld().getBlockState(placementTarget) != topState) {
            Log.getLogger().debug("Not considering pathing, because top state is not a path:" + currentPos + " " + citizen.getEntityWorld().getBlockState(placementTarget));
            return;
        }

        final Long currentTime = citizen.getEntityWorld().getGameTime();

        final boolean isInsideBuilding = Objects.requireNonNull(citizen.getCitizenData()).getColony().getBuildingManager().getBuildings().values().stream().anyMatch((building) -> {
            final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners = building.getCorners();
            final int minX = corners.getA().getA() + 3;
            final int maxX = (int) (corners.getA().getB() + (Math.signum(corners.getA().getB()) * 3));
            final int minZ = corners.getB().getA() + 3;
            final int maxZ = (int) (corners.getB().getB() + (Math.signum(corners.getB().getB()) * 3));

            return minX <= currentPos.getX() && currentPos.getX() <= maxX && minZ <= currentPos.getZ() && currentPos.getZ() <= maxZ;
        });

        if (isInsideBuilding) {
            Log.getLogger().debug("Not considering pathing, because it is possibly to close to a building: " + currentPos);
            return;
        }

        this.traversedPosOnTick.computeIfAbsent(currentTime, time -> Lists.newArrayList()).add(currentPos);
        this.traversedTickForPos.computeIfAbsent(currentPos, pos -> Lists.newArrayList()).add(currentTime);
        this.traversalCountOnPos.compute(currentPos, (BlockPos pos, Long currentCount) -> currentCount == null ? 1 : currentCount + 1);
    }

    @Override
    public void onColonyTick(IColony colony) {
        if (!IMinecoloniesAPI.getInstance().getConfig().getCommon().autoCreatePathsEnabled.get())
            return;

        final Long currentTime = colony.getWorld().getGameTime();
        final List<Long> ticksToRemove = traversedPosOnTick.keySet().stream().filter(tick -> tick < (currentTime - IMinecoloniesAPI.getInstance().getConfig().getCommon().maxTrackingAge.get())).collect(Collectors.toList());

        for (final Long tick : ticksToRemove) {
            final List<BlockPos> posToDecrease = traversedPosOnTick.remove(tick);
            for (final BlockPos pos : posToDecrease) {
                traversalCountOnPos.compute(pos, (p, l) -> l == null || l == 1 ? null : l - 1);
                traversedTickForPos.compute(pos, (p, l) -> {
                    if (l == null || (l.size() == 1 && l.get(0).equals(tick)))
                        return null;

                    l.remove(tick);

                    return l;
                });
            }
        }

        final List<BlockPos> pathBuildingCandidates = traversalCountOnPos.entrySet().stream().sorted(Map.Entry.comparingByValue((l1, l2) -> Long.compare(l2, l1))).limit(25).map(Map.Entry::getKey).collect(Collectors.toList());
        Log.getLogger().info("Running placement logic.");
        pathBuildingCandidates.forEach(pathCandidate -> {
            final BlockState topState = colony.getWorld().getBiome(pathCandidate).getSurfaceBuilderConfig().getTop();

            final BlockPos placementTarget = pathCandidate.down();

            if (colony.getWorld().getBlockState(placementTarget).equals(topState))
                colony.getWorld().setBlockState(placementTarget, Blocks.GRASS_PATH.getDefaultState());


            if (colony.getWorld().getBlockState(placementTarget.north()).equals(topState))
                colony.getWorld().setBlockState(placementTarget.north(), Blocks.GRASS_PATH.getDefaultState());


            if (colony.getWorld().getBlockState(placementTarget.south()).equals(topState))
                colony.getWorld().setBlockState(placementTarget.south(), Blocks.GRASS_PATH.getDefaultState());


            if (colony.getWorld().getBlockState(placementTarget.west()).equals(topState))
                colony.getWorld().setBlockState(placementTarget.west(), Blocks.GRASS_PATH.getDefaultState());


            if (colony.getWorld().getBlockState(placementTarget.east()).equals(topState))
                colony.getWorld().setBlockState(placementTarget.east(), Blocks.GRASS_PATH.getDefaultState());

            traversalCountOnPos.remove(pathCandidate);
            final List<Long> ticksToUpdate = traversedTickForPos.remove(pathCandidate);
            ticksToUpdate.forEach(tick -> {
                traversedPosOnTick.compute(tick, (t, l) -> {
                    if (l == null || (l.size() == 1 && l.get(0).equals(pathCandidate)))
                        return null;

                    l.remove(pathCandidate);

                    return l;
                });
            });
        });
    }
}
