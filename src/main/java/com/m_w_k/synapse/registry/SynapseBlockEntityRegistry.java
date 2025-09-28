package com.m_w_k.synapse.registry;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.common.block.DASBlock;
import com.m_w_k.synapse.common.block.DistributorBlock;
import com.m_w_k.synapse.common.block.EndpointBlock;
import com.m_w_k.synapse.common.block.RelayBlock;
import com.m_w_k.synapse.common.block.entity.DASBlockEntity;
import com.m_w_k.synapse.common.block.entity.DistributorBlockEntity;
import com.m_w_k.synapse.common.block.entity.EndpointBlockEntity;
import com.m_w_k.synapse.common.block.entity.RelayBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public abstract class SynapseBlockEntityRegistry {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SynapseMod.MODID);

    public static final RegistryObject<BlockEntityType<DistributorBlockEntity>> DISTRIBUTOR_BLOCK = BLOCK_ENTITY_TYPES.register("distributor_block_entity",
            () -> BlockEntityType.Builder.of((pos, state) -> {
                    if (state.getBlock() instanceof DistributorBlock d) {
                        return d.newBlockEntity(pos, state);
                    }
                    return null;
                },
                    SynapseBlockRegistry.DISTRIBUTOR_BLOCK_1.get(), SynapseBlockRegistry.DISTRIBUTOR_BLOCK_2.get(),
                    SynapseBlockRegistry.DISTRIBUTOR_BLOCK_3.get()).build(null));
    public static final RegistryObject<BlockEntityType<DASBlockEntity>> DAS_BLOCK = BLOCK_ENTITY_TYPES.register("das_server_block_entity",
            () -> BlockEntityType.Builder.of((pos, state) -> {
                        if (state.getBlock() instanceof DASBlock d) {
                            return d.newBlockEntity(pos, state);
                        }
                        return null;
                    },
                    SynapseBlockRegistry.DISTRIBUTOR_BLOCK_1.get(), SynapseBlockRegistry.DISTRIBUTOR_BLOCK_2.get(),
                    SynapseBlockRegistry.DISTRIBUTOR_BLOCK_3.get()).build(null));
    public static final RegistryObject<BlockEntityType<EndpointBlockEntity>> ENDPOINT_BLOCK = BLOCK_ENTITY_TYPES.register("endpoint_block_entity",
            () -> BlockEntityType.Builder.of((pos, state) -> {
                        if (state.getBlock() instanceof EndpointBlock d) {
                            return d.newBlockEntity(pos, state);
                        }
                        return null;
                    },
                    SynapseBlockRegistry.ENDPOINT_BASIC.get()).build(null));
    public static final RegistryObject<BlockEntityType<RelayBlockEntity>> RELAY_BLOCK = BLOCK_ENTITY_TYPES.register("relay_block_entity",
            () -> BlockEntityType.Builder.of((pos, state) -> {
                        if (state.getBlock() instanceof RelayBlock d) {
                            return d.newBlockEntity(pos, state);
                        }
                        return null;
                    },
                    SynapseBlockRegistry.RELAY.get()).build(null));

    public static void init(IEventBus bus) {
        BLOCK_ENTITY_TYPES.register(bus);
    }
}
