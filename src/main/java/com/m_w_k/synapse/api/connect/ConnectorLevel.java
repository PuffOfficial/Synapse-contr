package com.m_w_k.synapse.api.connect;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public enum ConnectorLevel implements StringRepresentable, IExtensibleEnum {
    ENDPOINT(0), RELAY(Double.NEGATIVE_INFINITY),
    DISTRIBUTOR_1(1), DISTRIBUTOR_2(2), DISTRIBUTOR_3(3);

    public static final Codec<ConnectorLevel> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(ConnectorLevel::values, ConnectorLevel::valueOf);

    public static final ObjectRBTreeSet<ConnectorLevel> ADDRESS_SPACE = new ObjectRBTreeSet<>(Comparator.comparingDouble(ConnectorLevel::getPrio).reversed());

    static {
        ADDRESS_SPACE.add(DISTRIBUTOR_1);
        ADDRESS_SPACE.add(DISTRIBUTOR_2);
        ADDRESS_SPACE.add(DISTRIBUTOR_3);
    }

    private final double prio;

    ConnectorLevel(double prio) {
        this.prio = prio;
    }

    public static ConnectorLevel create(String name, double prio) {
        throw new IllegalStateException("Enum not extended");
    }

    @Override
    public @NotNull String getSerializedName() {
        return name();
    }

    public double getPrio() {
        return prio;
    }

    /**
     * Gets the relative connection type to another tier
     * @param other the other tier
     * @return the connection type of any connection that runs from this to {@code other}.
     */
    public @NotNull ConnectionType typeOf(@NotNull ConnectorLevel other) {
        if (Double.isFinite(this.getPrio()) && Double.isFinite(other.getPrio())) {
            if (this.getPrio() == other.getPrio()) {
                return ConnectionType.EQUAL;
            } else if (this.getPrio() < other.getPrio()) {
                return ConnectionType.UPSTREAM;
            }
            return ConnectionType.DOWNSTREAM;
        }
        if (this.getPrio() < other.getPrio()) {
            return ConnectionType.UNKNOWN_UP;
        } else if (this.getPrio() > other.getPrio()) {
            return ConnectionType.UNKNOWN_DOWN;
        }
        return ConnectionType.UNKNOWN;
    }

}
