package com.m_w_k.synapse.data;

import com.m_w_k.synapse.registry.SynapseBlockRegistry;
import com.m_w_k.synapse.registry.SynapseItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class SynapseRecipeProvider extends RecipeProvider {

    public SynapseRecipeProvider(PackOutput p_248933_) {
        super(p_248933_);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> writer) {
        materialRecipes(writer);
        functionalRecipes(writer);
    }

    private void materialRecipes(@NotNull Consumer<FinishedRecipe> writer) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SynapseItemRegistry.BIOSTEEL.get())
                .requires(Tags.Items.INGOTS_IRON)
                .requires(Items.ROTTEN_FLESH, 2)
                .requires(Items.COAL)
                .unlockedBy("has_rotten_flesh", has(Items.ROTTEN_FLESH))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SynapseItemRegistry.DUNED_GOLD.get())
                .requires(Tags.Items.INGOTS_GOLD)
                .requires(Tags.Items.BONES)
                .requires(Tags.Items.BONES)
                .requires(Tags.Items.SAND)
                .unlockedBy("has_bones", has(Tags.Items.BONES))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SynapseItemRegistry.NEURAL_THREAD.get())
                .requires(Tags.Items.GEMS_LAPIS)
                .requires(Tags.Items.STRING)
                .requires(Tags.Items.STRING)
                .requires(Items.GLOW_INK_SAC)
                .unlockedBy("has_string", has(Tags.Items.STRING))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SynapseItemRegistry.TRANSFER_POWDER.get())
                .requires(Tags.Items.DUSTS_REDSTONE)
                .requires(Items.GUNPOWDER, 2)
                .requires(Tags.Items.DUSTS_GLOWSTONE)
                .unlockedBy("has_gunpowder", has(Tags.Items.GUNPOWDER))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SynapseItemRegistry.WETWARE_CHIP.get())
                .requires(Tags.Items.INGOTS_COPPER)
                .requires(Items.FERMENTED_SPIDER_EYE, 2)
                .requires(Tags.Items.GEMS_AMETHYST)
                .unlockedBy("has_spider_eyes", has(Items.SPIDER_EYE))
                .save(writer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SynapseItemRegistry.PIEZOELECTRIC_CRYSTAL.get())
                .requires(Tags.Items.GEMS_QUARTZ)
                .requires(Tags.Items.GEMS_PRISMARINE)
                .requires(Tags.Items.GEMS_PRISMARINE)
                .requires(Items.PACKED_ICE)
                .unlockedBy("has_prismarine", has(Tags.Items.GEMS_PRISMARINE))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SynapseItemRegistry.MAGMA_CRYSTAL.get())
                .requires(Tags.Items.GEMS_EMERALD)
                .requires(Items.MAGMA_CREAM, 2)
                .requires(Tags.Items.INGOTS_NETHER_BRICK)
                .unlockedBy("has_magma_cream", has(Items.MAGMA_CREAM))
                .save(writer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, SynapseItemRegistry.ENDER_CRYSTAL.get())
                .requires(Tags.Items.GEMS_DIAMOND)
                .requires(Tags.Items.ENDER_PEARLS)
                .requires(Tags.Items.ENDER_PEARLS)
                .requires(Items.CHORUS_FRUIT)
                .unlockedBy("has_pearls", has(Tags.Items.ENDER_PEARLS))
                .save(writer);
    }

    private void functionalRecipes(@NotNull Consumer<FinishedRecipe> writer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SynapseItemRegistry.ENERGY_AXON.get(), 3)
                .pattern("sis")
                .pattern("iti")
                .pattern("sis")
                .define('i', Items.GOLD_INGOT)
                .define('s', SynapseItemRegistry.NEURAL_THREAD.get())
                .define('t', SynapseItemRegistry.TRANSFER_POWDER.get())
                .unlockedBy("has_thread", has(SynapseItemRegistry.NEURAL_THREAD.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SynapseItemRegistry.ITEM_AXON.get(), 3)
                .pattern("sis")
                .pattern("iti")
                .pattern("sis")
                .define('i', Items.IRON_INGOT)
                .define('s', SynapseItemRegistry.NEURAL_THREAD.get())
                .define('t', SynapseItemRegistry.TRANSFER_POWDER.get())
                .unlockedBy("has_thread", has(SynapseItemRegistry.NEURAL_THREAD.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SynapseItemRegistry.FLUID_AXON.get(), 3)
                .pattern("sis")
                .pattern("iti")
                .pattern("sis")
                .define('i', Items.COPPER_INGOT)
                .define('s', SynapseItemRegistry.NEURAL_THREAD.get())
                .define('t', SynapseItemRegistry.TRANSFER_POWDER.get())
                .unlockedBy("has_thread", has(SynapseItemRegistry.NEURAL_THREAD.get()))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SynapseBlockRegistry.ENDPOINT_BASIC.get(), 2)
                .pattern("btb")
                .pattern(" c ")
                .pattern("btb")
                .define('b', SynapseItemRegistry.BIOSTEEL.get())
                .define('c', SynapseItemRegistry.WETWARE_CHIP.get())
                .define('t', SynapseItemRegistry.TRANSFER_POWDER.get())
                .unlockedBy("has_biosteel", has(SynapseItemRegistry.BIOSTEEL.get()))
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SynapseBlockRegistry.DISTRIBUTOR_BLOCK_1.get())
                .pattern("gbg")
                .pattern("tct")
                .pattern("gbg")
                .define('b', SynapseItemRegistry.BIOSTEEL.get())
                .define('g', SynapseItemRegistry.DUNED_GOLD.get())
                .define('c', SynapseItemRegistry.WETWARE_CHIP.get())
                .define('t', SynapseItemRegistry.PIEZOELECTRIC_CRYSTAL.get())
                .unlockedBy("has_duned_gold", has(SynapseItemRegistry.DUNED_GOLD.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SynapseBlockRegistry.DISTRIBUTOR_BLOCK_2.get())
                .pattern("gbg")
                .pattern("tct")
                .pattern("gbg")
                .define('b', SynapseItemRegistry.BIOSTEEL.get())
                .define('g', SynapseItemRegistry.DUNED_GOLD.get())
                .define('c', SynapseItemRegistry.WETWARE_CHIP.get())
                .define('t', SynapseItemRegistry.MAGMA_CRYSTAL.get())
                .unlockedBy("has_duned_gold", has(SynapseItemRegistry.DUNED_GOLD.get()))
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SynapseBlockRegistry.DISTRIBUTOR_BLOCK_3.get())
                .pattern("gbg")
                .pattern("tct")
                .pattern("gbg")
                .define('b', SynapseItemRegistry.BIOSTEEL.get())
                .define('g', SynapseItemRegistry.DUNED_GOLD.get())
                .define('c', SynapseItemRegistry.WETWARE_CHIP.get())
                .define('t', SynapseItemRegistry.ENDER_CRYSTAL.get())
                .unlockedBy("has_duned_gold", has(SynapseItemRegistry.DUNED_GOLD.get()))
                .save(writer);
    }
}
