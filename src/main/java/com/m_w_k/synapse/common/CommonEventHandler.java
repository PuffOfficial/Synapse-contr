package com.m_w_k.synapse.common;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.common.block.entity.EndpointBlockEntity;
import com.m_w_k.synapse.common.connect.AbstractExposer;
import com.m_w_k.synapse.common.connect.EnergyExposer;
import com.m_w_k.synapse.common.connect.FluidExposer;
import com.m_w_k.synapse.common.connect.ItemExposer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = SynapseMod.MODID)
public final class CommonEventHandler {

    @SubscribeEvent
    public static void attachEndpointCaps(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof EndpointBlockEntity endpoint) {
            event.addCapability(SynapseMod.resLoc("endpoint_energy"), new ICapabilityProvider() {
                private final LazyOptional<EnergyExposer> optional = LazyOptional.of(() -> new EnergyExposer(endpoint));

                @Override
                public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                    if (cap == ForgeCapabilities.ENERGY &&
                            endpoint.activeOnSide(ForgeCapabilities.ENERGY, side)) {
                        return AbstractExposer.sided(side, optional);
                    }
                    return LazyOptional.empty();
                }
            });
            event.addCapability(SynapseMod.resLoc("endpoint_fluid"), new ICapabilityProvider() {
                private final LazyOptional<FluidExposer> optional = LazyOptional.of(() -> new FluidExposer(endpoint));

                @Override
                public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                    if (cap == ForgeCapabilities.FLUID_HANDLER &&
                            endpoint.activeOnSide(ForgeCapabilities.FLUID_HANDLER, side)) {
                        return AbstractExposer.sided(side, optional);
                    }
                    return LazyOptional.empty();
                }
            });
            event.addCapability(SynapseMod.resLoc("endpoint_item"), new ICapabilityProvider() {
                private final LazyOptional<ItemExposer> optional = LazyOptional.of(() -> new ItemExposer(endpoint));

                @Override
                public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                    if (cap == ForgeCapabilities.ITEM_HANDLER &&
                            endpoint.activeOnSide(ForgeCapabilities.ITEM_HANDLER, side)) {
                        return AbstractExposer.sided(side, optional);
                    }
                    return LazyOptional.empty();
                }
            });
        }
    }
}
