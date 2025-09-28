package com.m_w_k.synapse.data;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.common.block.EndpointBlock;
import com.m_w_k.synapse.registry.SynapseBlockRegistry;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class SynapseLootTableGen implements DataProvider.Factory<LootTableProvider> {
    public static final SynapseLootTableGen INSTANCE = new SynapseLootTableGen();

    private SynapseLootTableGen() {}

    @Override
    public @NotNull LootTableProvider create(@NotNull PackOutput output) {
        return new LootTableProvider(output, Collections.emptySet(), List.of(
                new LootTableProvider.SubProviderEntry(BlockSubProvider::new, LootContextParamSets.BLOCK)
        ));
    }

    protected static final class BlockSubProvider extends BlockLootSubProvider {

        public BlockSubProvider() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected @NotNull Iterable<Block> getKnownBlocks() {
            return SynapseBlockRegistry.BLOCKS.getEntries()
                    .stream()
                    .flatMap(RegistryObject::stream)
                    ::iterator;
        }

        @Override
        protected void generate() {
            this.add(SynapseBlockRegistry.ENDPOINT_BASIC.get(), (block) -> LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(this.applyExplosionDecay(SynapseBlockRegistry.ENDPOINT_BASIC.get(),
                                    LootItem.lootTableItem(block).apply(List.of(2, 3, 4, 5, 6),
                                            (i) -> SetItemCountFunction.setCount(ConstantValue.exactly((float) i))
                                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                                                    .hasProperty(EndpointBlock.ENDPOINTS, i))))))));
            this.dropSelf(SynapseBlockRegistry.DISTRIBUTOR_BLOCK_1.get());
            this.dropSelf(SynapseBlockRegistry.DISTRIBUTOR_BLOCK_2.get());
            this.dropSelf(SynapseBlockRegistry.DISTRIBUTOR_BLOCK_3.get());
        }
    }
}
