package com.m_w_k.synapse.data;

import com.m_w_k.synapse.SynapseMod;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class SynapseAdvancementGen implements DataProvider.Factory<ForgeAdvancementProvider> {

    private final CompletableFuture<HolderLookup.Provider> registries;
    private final ExistingFileHelper existingFileHelper;

    public SynapseAdvancementGen(CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        this.registries = registries;
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    public @NotNull ForgeAdvancementProvider create(@NotNull PackOutput output) {
        return new ForgeAdvancementProvider(
                output,
                registries,
                existingFileHelper,
                List.of()
        );
    }

    private final class AdvancementHelper {

        private final String name;

        private final Advancement.Builder builder = Advancement.Builder.recipeAdvancement();

        private ItemStack icon;
        private FrameType frame = FrameType.TASK;
        private boolean hidden = false;

        private boolean toast = false;
        private boolean chat = false;

        public AdvancementHelper(String name) {
            this.name = name;
        }

        public AdvancementHelper parent(Advancement parent) {
            builder.parent(parent);
            return this;
        }

        public AdvancementHelper icon(ItemLike icon) {
            return icon(new ItemStack(icon));
        }

        public AdvancementHelper icon(ItemStack icon) {
            this.icon = icon;
            return this;
        }

        public void frame(FrameType frame) {
            this.frame = frame;
        }

        public void hidden(boolean hidden) {
            this.hidden = hidden;
        }

        public void toast(boolean toast) {
            this.toast = toast;
        }

        public void chat(boolean chat) {
            this.chat = chat;
        }

        public AdvancementHelper getItem(ItemLike item)
        {
            return this.icon(item).hasItems(item);
        }

        public AdvancementHelper hasItems(ItemLike... items) {
            builder.addCriterion("has_item", InventoryChangeTrigger.TriggerInstance
                    .hasItems(ItemPredicate.Builder.item().of(items).build()));
            return this;
        }

        public AdvancementHelper toBuilder(Consumer<Advancement.Builder> action) {
            action.accept(builder);
            return this;
        }

        public Advancement save(@NotNull Consumer<Advancement> writer) {
            return builder.display(
                    icon,
                    Component.translatable("advancement.synapse."+name),
                    Component.translatable("advancement.synapse."+name+".desc"),
                    null,
                    frame,
                    toast,
                    chat,
                    hidden
            ).save(writer, SynapseMod.resLoc(name), existingFileHelper);
        }
    }
}
