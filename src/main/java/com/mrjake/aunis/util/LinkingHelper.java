package com.mrjake.aunis.util;

import com.mrjake.aunis.config.AunisConfig;
import com.mrjake.aunis.tileentity.DHDTile;
import com.mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import com.mrjake.aunis.util.minecraft.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class LinkingHelper {

    /**
     * Finds closest block of the given type within given radius.
     *
     * @param world World instance.
     * @param startPos Starting position.
     * @param radius Radius. Subtracted and added to the startPos.
     * @param targetBlock. Searched block instance. Must provide {@link TileEntity} and {@link TileEntity} should implement {@link ILinkable}.
     * @return Found block's {@link BlockPos}. Null if not found.
     */

    @Nullable
    public static BlockPos findClosestUnlinked(World world, BlockPos startPos, BlockPos radius, Block targetBlock) {
        double closestDistance = Double.MAX_VALUE;
        BlockPos closest = null;

        for (BlockPos target : BlockPos.getAllInBoxMutable(startPos.subtract(radius), startPos.add(radius))) {

            if (BaseUtils.getWorldBlockState(world, target).getBlock() == targetBlock) {

                ILinkable linkedTile = (ILinkable) world.getTileEntity(target.getX(), target.getY(), target.getZ());

                if (linkedTile.canLinkTo()) {
                    double distanceSq = startPos.distanceSq(target);

                    if (distanceSq < closestDistance) {
                        closestDistance = distanceSq;
                        closest = target.toImmutable();
                    }
                }
            }
        }

        return closest;
    }

    /**
     * Returns proper DHD range.
     *
     * @return DHD range.
     */
    public static BlockPos getDhdRange() {
        int xz = AunisConfig.dhdConfig.rangeFlat;
        int y = AunisConfig.dhdConfig.rangeVertical;

        return new BlockPos(xz, y ,xz);
    }

    public static void updateLinkedGate(World world, BlockPos gatePos, BlockPos dhdPos) {
        StargateMilkyWayBaseTile gateTile = (StargateMilkyWayBaseTile) world.getTileEntity(gatePos.getX(), gatePos.getY(), gatePos.getZ());
        DHDTile dhdTile = (DHDTile) world.getTileEntity(dhdPos.getX(), dhdPos.getY(), dhdPos.getZ());

        if (dhdTile != null) {
            dhdTile.setLinkedGate(gatePos);
            gateTile.setLinkedDHD(dhdPos);
        }
    }
}
