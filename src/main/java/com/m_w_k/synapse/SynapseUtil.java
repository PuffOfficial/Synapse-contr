package com.m_w_k.synapse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public abstract class SynapseUtil {

    public static Direction facingTo(BlockPos from, BlockPos to) {
        BlockPos diff = to.subtract(from);
        if (Math.abs(diff.getX()) + Math.abs(diff.getY()) + Math.abs(diff.getZ()) != 1) {
            return null;
        }
        if (Math.abs(diff.getX()) == 1) {
            return Direction.fromAxisAndDirection(Direction.Axis.X, diff.getX() > 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);
        } else if (Math.abs(diff.getY()) == 1) {
            return Direction.fromAxisAndDirection(Direction.Axis.Y, diff.getY() > 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);
        } else {
            return Direction.fromAxisAndDirection(Direction.Axis.Z, diff.getZ() > 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);
        }
    }
}
