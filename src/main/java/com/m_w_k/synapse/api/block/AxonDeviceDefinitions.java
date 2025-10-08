package com.m_w_k.synapse.api.block;

import com.m_w_k.synapse.api.connect.AxonType;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.Direction;

import java.util.List;

public abstract class AxonDeviceDefinitions {

    public static final Reference2IntOpenHashMap<AxonType> STANDARD = new Reference2IntOpenHashMap<>(AxonType.values().length);
    public static final List<AxonType> STANDARD_INV = new ObjectArrayList<>(AxonType.values().length);
    public static final Object2IntOpenHashMap<Pair<AxonType, Direction>> ENDPOINTS =
            new Object2IntOpenHashMap<>(Direction.values().length * AxonType.values().length);
    public static final List<Pair<AxonType, Direction>> ENDPOINTS_INV =
            new ObjectArrayList<>(Direction.values().length * AxonType.values().length);

    static {
        for (AxonType type : AxonType.values()) {
            STANDARD.put(type, type.ordinal());
            STANDARD_INV.add(type.ordinal(), type);
            for (Direction dir : Direction.values()) {
                Pair<AxonType, Direction> pair = Pair.of(type, dir);
                ENDPOINTS.put(pair, dir.ordinal() + Direction.values().length * type.ordinal());
                ENDPOINTS_INV.add(dir.ordinal() + Direction.values().length * type.ordinal(), pair);
            }
        }
    }

    public static int standard(AxonType type) {
        return STANDARD.getInt(type);
    }

    public static int endpoint(AxonType type, Direction direction) {
        return ENDPOINTS.getInt(Pair.of(type, direction));
    }
}
