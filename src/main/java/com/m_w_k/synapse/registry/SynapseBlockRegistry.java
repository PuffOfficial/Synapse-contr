package com.m_w_k.synapse.registry;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.common.block.DASBlock;
import com.m_w_k.synapse.common.block.DistributorBlock;
import com.m_w_k.synapse.common.block.EndpointBlock;
import com.m_w_k.synapse.common.block.RelayBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public abstract class SynapseBlockRegistry {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, SynapseMod.MODID);

    public static final RegistryObject<DistributorBlock> DISTRIBUTOR_BLOCK_1 = BLOCKS.register("distributor_block_1",
            () -> new DistributorBlock(BlockBehaviour.Properties.of(), ConnectorLevel.DISTRIBUTOR_1));
    public static final RegistryObject<DistributorBlock> DISTRIBUTOR_BLOCK_2 = BLOCKS.register("distributor_block_2",
            () -> new DistributorBlock(BlockBehaviour.Properties.of(), ConnectorLevel.DISTRIBUTOR_2));
    public static final RegistryObject<DistributorBlock> DISTRIBUTOR_BLOCK_3 = BLOCKS.register("distributor_block_3",
            () -> new DistributorBlock(BlockBehaviour.Properties.of(), ConnectorLevel.DISTRIBUTOR_3));

    public static final RegistryObject<DASBlock> DISTRIBUTOR_ALIAS_SYSTEM_SERVER = BLOCKS.register("das_server",
            () -> new DASBlock(BlockBehaviour.Properties.of()));

    public static final RegistryObject<EndpointBlock> ENDPOINT_BASIC = BLOCKS.register("endpoint_basic",
            () -> new EndpointBlock(BlockBehaviour.Properties.of()));
    public static final RegistryObject<RelayBlock> RELAY = BLOCKS.register("relay",
            () -> new RelayBlock(BlockBehaviour.Properties.of()));

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        register(DISTRIBUTOR_BLOCK_1, new Item.Properties());
        register(DISTRIBUTOR_BLOCK_2, new Item.Properties());
        register(DISTRIBUTOR_BLOCK_3, new Item.Properties());

        register(DISTRIBUTOR_ALIAS_SYSTEM_SERVER, new Item.Properties());

        register(ENDPOINT_BASIC, new Item.Properties());

        register(RELAY, new Item.Properties());
    }

    private static void register(RegistryObject<? extends Block> block, Item.Properties props) {
        SynapseItemRegistry.ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), props));
    }

}
