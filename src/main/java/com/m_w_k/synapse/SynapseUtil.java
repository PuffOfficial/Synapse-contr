package com.m_w_k.synapse;

import com.m_w_k.synapse.api.connect.ConnectionType;
import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.api.connect.DeviceDataKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

public final class SynapseUtil {

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

    public static @NotNull ConnectorLevel actualLevel(@NotNull ConnectorLevel.Provider provider) {
        var data = provider.getData();
        if (data == null) return provider.getLevel();
        return DeviceDataKey.RELAYING.getFromMap(data, provider.getLevel());
    }

    public static @NotNull ConnectionType actualTypeOf(@NotNull ConnectorLevel.Provider a, @NotNull ConnectorLevel.Provider b) {
        ConnectionType type = actualLevel(a).typeOf(actualLevel(b));
        if (type == ConnectionType.EQUAL && a.getLevel() != b.getLevel()) {
            return a.getLevel().typeOf(b.getLevel());
        }
        return type;
    }
}
