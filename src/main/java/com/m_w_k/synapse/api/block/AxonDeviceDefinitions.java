package com.m_w_k.synapse.api.block;

import com.m_w_k.synapse.api.block.ruleset.FluidTransferRuleset;
import com.m_w_k.synapse.api.block.ruleset.ItemTransferRuleset;
import com.m_w_k.synapse.api.block.ruleset.TransferRuleset;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.common.connect.EnergyExposer;
import com.m_w_k.synapse.common.connect.FluidExposer;
import com.m_w_k.synapse.common.connect.IEndpointCapability;
import com.m_w_k.synapse.common.connect.ItemExposer;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public abstract class AxonDeviceDefinitions {

    public static final Reference2IntOpenHashMap<AxonType> STANDARD = new Reference2IntOpenHashMap<>(AxonType.values().length);
    public static final List<AxonType> STANDARD_INV = new ObjectArrayList<>(AxonType.values().length);
    public static final Object2IntOpenHashMap<Pair<AxonType, Direction>> ENDPOINTS =
            new Object2IntOpenHashMap<>(Direction.values().length * AxonType.values().length);
    public static final List<Pair<AxonType, Direction>> ENDPOINTS_INV =
            new ObjectArrayList<>(Direction.values().length * AxonType.values().length);

    public static final Reference2ObjectOpenHashMap<AxonType, TransferRuleset> ENDPOINT_RULES =
            new Reference2ObjectOpenHashMap<>(AxonType.values().length);
    public static final Reference2ObjectOpenHashMap<AxonType, Function<IFacedAxonBlockEntity, IEndpointCapability>> ENDPOINT_CAPABILITIES =
            new Reference2ObjectOpenHashMap<>(AxonType.values().length);

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
        ENDPOINT_RULES.put(AxonType.ITEM, new ItemTransferRuleset(Dist.DEDICATED_SERVER));
        ENDPOINT_RULES.put(AxonType.FLUID, new FluidTransferRuleset(Dist.DEDICATED_SERVER));
        ENDPOINT_CAPABILITIES.put(AxonType.ITEM, ItemExposer::new);
        ENDPOINT_CAPABILITIES.put(AxonType.FLUID, FluidExposer::new);
        ENDPOINT_CAPABILITIES.put(AxonType.ENERGY, EnergyExposer::new);
    }

    public static int standard(AxonType type) {
        return STANDARD.getInt(type);
    }

    public static int endpoint(AxonType type, Direction direction) {
        return ENDPOINTS.getInt(Pair.of(type, direction));
    }

    public static @Nullable TransferRuleset newEndpointRuleset(AxonType type, Dist dist) {
        TransferRuleset pattern = ENDPOINT_RULES.get(type);
        return pattern == null ? null : pattern.createNew(dist);
    }
}
