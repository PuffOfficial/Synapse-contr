package com.m_w_k.synapse.api.connect;

import com.m_w_k.synapse.SynapseUtil;
import it.unimi.dsi.fastutil.objects.*;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.Consumer;

public class AxonTree<T> extends SavedData {

    private final @NotNull AxonType type;
    private final @NotNull Map<UUID, ConnectorDevice> members;

    /**
     * Load the particular axon tree for the type.
     * @param level a level instance. Currently axon trees are not per-level, but that may change.
     * @param type the type of tree to load
     * @param cap the type of capability of the tree. Use {@link AxonType#getCapability()}
     * @return the loaded axon tree.
     * @param <T> the capability type
     */
    public static <T> @NotNull Optional<AxonTree<T>> load(@Nullable LevelAccessor level, @NotNull AxonType type, Capability<T> cap) {
        if (level == null || level.getServer() == null) return Optional.empty();
        return Optional.of(level.getServer().overworld().getDataStorage().computeIfAbsent(
                t -> new AxonTree<>(type, cap, t),
                () -> new AxonTree<>(type, cap),
                "synapse:" + type.getSerializedName()));
    }

    protected AxonTree(@NotNull AxonType type, Capability<T> cap) {
        if (cap != type.getCapability()) throw new IllegalArgumentException("Capability must match type capability!");
        this.type = type;
        this.members = new Object2ObjectOpenHashMap<>();
    }

    /**
     * Ensures the particular UUID is associated with a connection data, and that connection data
     * is associated with the given capability. Address and level will not be overridden if the UUID
     * has already been registered.
     * @param uuid the UUID to register
     * @param identifier the identifier. Determines the part of the connector's address at its level.
     * @param level the connection level to register, if not already registered.
     * @param cap the capability to register.
     * @return the connection data now registered with the provided UUID
     */
    public @NotNull ConnectorDevice register(@NotNull UUID uuid, short identifier, @NotNull ConnectorLevel level, @Nullable T cap) {
        ConnectorDevice data = members.computeIfAbsent(uuid, k -> {
            setDirty();
            return new ConnectorDevice(identifier, level);
        });
        if (cap != null) data.setCap(cap);
        return data;
    }

    /**
     * Removes the provided UUID and its connection data from this tree
     * @param uuid the uuid to remove
     */
    public void retire(@NotNull UUID uuid) {
        ConnectorDevice data = members.remove(uuid);
        if (data == null) return;
        if (data.getUpstream() != null) data.getUpstream().removeDownstream(uuid);
        data.downstream().forEachRemaining(d -> d.removeUpstream(data));
    }

    /**
     * Gets the connection data associated with a particular UUID, if it exists.
     * @param uuid the UUID
     * @return the associated data
     */
    public @Nullable ConnectorDevice get(@NotNull UUID uuid) {
        return members.get(uuid);
    }

    /**
     * Discover if a connection between two connectors exist, and its type.
     * @param aIdentifier the identifier of the first connector.
     * @param aData the data of the first connector. Optional.
     * @param bIdentifier the identifier of the second connector.
     * @param bData the data of the second connector. Optional.
     * @return the type of connection from a to b. Null if not connected,
     * {@link ConnectionType#UNKNOWN} if either is not registered to this tree.
     */
    public @Nullable ConnectionType isConnected(@NotNull UUID aIdentifier, @Nullable ConnectorDevice aData,
                                                @NotNull UUID bIdentifier, @Nullable ConnectorDevice bData) {
        if (aData == null) {
            aData = get(aIdentifier);
            if (aData == null) return ConnectionType.UNKNOWN;
        }
        if (bData == null) {
            bData = get(bIdentifier);
            if (bData == null) return ConnectionType.UNKNOWN;
        }
        if (aData.getUpstream() == bData) {
            return ConnectionType.UPSTREAM;
        } else if (bData.getUpstream() == aData) {
            return ConnectionType.DOWNSTREAM;
        }
        return null;
    }

    /**
     * Form a connection between two connectors.
     * @param aIdentifier the identifier of the first connector.
     * @param aData the data of the first connector. Optional.
     * @param bIdentifier the identifier of the second connector.
     * @param bData the data of the second connector. Optional.
     * @return the type of connection formed. Will return null if the downstream connector already has an upstream,
     * the two connectors are of the same level, or either are not registered to this tree.
     */
    public @Nullable ConnectionType connect(@NotNull UUID aIdentifier, @Nullable ConnectorDevice aData,
                                            @NotNull UUID bIdentifier, @Nullable ConnectorDevice bData) {
        if (aData == null) {
            aData = get(aIdentifier);
            if (aData == null) return null;
        }
        if (bData == null) {
            bData = get(bIdentifier);
            if (bData == null) return null;
        }
        ConnectionType type = SynapseUtil.actualTypeOf(aData, bData);
        // the check for an existing upstream covers the case that the two are already connected.
        if (type.upstream() && !aData.hasUpstream()) {
            aData.setUpstream(bIdentifier, bData);
            bData.addDownstream(aIdentifier, aData);
        } else if (type.downstream() && !bData.hasUpstream()) {
            bData.setUpstream(aIdentifier, aData);
            aData.addDownstream(bIdentifier, bData);
        } else {
            return null;
        }
        setDirty();
        return type;
    }

    /**
     * Remove a connection between two connectors.
     * @param aIdentifier the identifier of the first connector.
     * @param aData the data of the first connector. Optional.
     * @param bIdentifier the identifier of the second connector.
     * @param bData the data of the second connector. Optional.
     * @return whether a connection was removed. Fails if the connectors are not connected
     * or either is not registered to this tree.
     */
    public boolean removeConnection(@NotNull UUID aIdentifier, @Nullable ConnectorDevice aData,
                                    @NotNull UUID bIdentifier, @Nullable ConnectorDevice bData) {
        if (aData == null) {
            aData = get(aIdentifier);
            if (aData == null) return false;
        }
        if (bData == null) {
            bData = get(bIdentifier);
            if (bData == null) return false;
        }
        ConnectionType type = isConnected(aIdentifier, aData, bIdentifier, bData);
        if (type == null) return false;
        if (type.upstream()) {
            aData.removeUpstream(bData);
            bData.removeDownstream(aIdentifier);
        } else if (type.downstream()) {
            bData.removeUpstream(aData);
            aData.removeDownstream(bIdentifier);
        } else {
            return false;
        }
        setDirty();
        return true;
    }

    /**
     * Finds connections matching the given address, starting from the given UUID.
     * @param from the UUID to search from
     * @param seek the address(es) to match
     * @return null if {@code from} is not registered with this tree, otherwise the result of {@link #find(ConnectorDevice, Collection)}
     */
    public @Nullable List<Connection<T>> find(@NotNull UUID from, @NotNull Collection<AxonAddress> seek) {
        ConnectorDevice data = members.get(from);
        if (data == null) return null;
        return find(data, seek);
    }

    /**
     * Finds connections matching the given address, starting from the given connection data.
     * @param from the connection data to search from
     * @param seek the address(es) to match
     * @return connections that match the given address
     */
    public @NotNull List<Connection<T>> find(@NotNull ConnectorDevice from, @NotNull Collection<AxonAddress> seek) {
        List<Connection<T>> out = new ObjectArrayList<>();
        findRecurseOut(from, seek, out, new ObjectArrayList<>(ConnectorLevel.values().length), null);
        return out;
    }

    protected void findRecurseOut(@NotNull ConnectorDevice from, @NotNull Collection<AxonAddress> seek,
                                  @NotNull List<Connection<T>> out, @NotNull List<AxonConnection> recurseDepth,
                                  @Nullable ConnectorDevice previous) {
        // filter addresses to only those that match 'from'
        List<AxonAddress> matchingSeek = seek.stream().filter(Objects::nonNull)
                .filter(address -> address.matchesAtAndAbove(from.address, SynapseUtil.actualLevel(from))).toList();
        if (!matchingSeek.isEmpty()) {
            // short circuit inward if 'from' matches the seek
            findRecurseIn(from, matchingSeek, out, new ObjectArrayList<>(recurseDepth), previous);
        }
        ConnectorDevice upstream = from.getUpstream();
        if (upstream != null) {
            recurseDepth.add(from.upstreamConnection);
            // skip 'from' when we recurse inward from further out
            findRecurseOut(upstream, seek, out, recurseDepth, from);
        }
    }

    protected void findRecurseIn(@NotNull ConnectorDevice from, @NotNull Collection<AxonAddress> seek,
                                 @NotNull List<Connection<T>> out, @NotNull List<AxonConnection> recurseDepth,
                                 @Nullable ConnectorDevice visitedDownstream) {
        if (seek.stream().filter(Objects::nonNull).anyMatch(address -> address.matches(from.address))) {
            out.add(new Connection<>(from.getCap(), recurseDepth, from.address));
        }
        for (Iterator<ConnectorDevice> it = from.downstream(); it.hasNext(); ) {
            ConnectorDevice downstream = it.next();
            if (downstream == visitedDownstream) continue;
            // discard non-matching seeks so that they don't start matching when they shouldn't.
            // we only need to check at the downstream level, because everything above that was
            // matched in findRecurseOut
            List<AxonAddress> matchingSeek = seek.stream().filter(Objects::nonNull)
                    .filter(address -> address.matchesAt(downstream.address, SynapseUtil.actualLevel(downstream))).toList();
            if (!matchingSeek.isEmpty()) {
                List<AxonConnection> depth = new ObjectArrayList<>(recurseDepth);
                depth.add(downstream.upstreamConnection);
                findRecurseIn(downstream, matchingSeek, out, depth, visitedDownstream);
            }
        }
    }

    protected AxonTree(@NotNull AxonType type, Capability<T> cap, @NotNull CompoundTag tag) {
        this(type, cap);
        int[] uuids = tag.getIntArray("UUIDs");
        ListTag data = tag.getList("Data", Tag.TAG_COMPOUND);
        for (int i = 0; i < uuids.length / 4; i++) {
            UUID id = UUIDUtil.uuidFromIntArray(Arrays.copyOfRange(uuids, 4 * i, 4 * i + 4));
            members.put(id, ConnectorDevice.from(this, data.getCompound(i)));
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        int[] uuids = new int[4 * members.size()];
        ListTag data = new ListTag();
        int i = 0;
        for (var entry : members.entrySet()) {
            System.arraycopy(UUIDUtil.uuidToIntArray(entry.getKey()), 0, uuids, 4 * i++, 4);
            data.add(entry.getValue().serializeNBT());
        }
        tag.putIntArray("UUIDs", uuids);
        tag.put("Data", data);
        return tag;
    }

    public @NotNull AxonType getType() {
        return type;
    }

    public record Connection<T>(@Nullable T capability, List<AxonConnection> connection, @NotNull AxonAddress destination) {}

    public final class ConnectorDevice implements INBTSerializable<CompoundTag>, ConnectorLevel.Provider {
        public static final UUID NO_UPSTREAM = new UUID(0, 0);

        private final @NotNull AxonAddress address;

        private final @NotNull ConnectorLevel level;
        private @Nullable T cap;

        private @NotNull UUID upstream;
        private @NotNull AxonConnection upstreamConnection = new AxonConnection(getType());
        private @Nullable ConnectorDevice upstreamCache;
        private final @NotNull Map<UUID, ConnectorDevice> downstream;

        private final @NotNull Map<DeviceDataKey<?>, Object> data;

        public ConnectorDevice(short id, @NotNull ConnectorLevel level) {
            this.address = new AxonAddress();
            this.address.put(level, id);
            this.level = level;
            this.upstream = NO_UPSTREAM;
            downstream = new Reference2ObjectOpenHashMap<>();
            data = new Object2ObjectOpenHashMap<>();
        }

        private ConnectorDevice(@NotNull AxonAddress address, @NotNull ConnectorLevel level, Map<DeviceDataKey<?>, Object> data) {
            this.address = address;
            this.level = level;
            this.upstream = NO_UPSTREAM;
            downstream = new Reference2ObjectOpenHashMap<>();
            this.data = data;
        }

        /**
         * Changes the ID of this connector. Updates downstream addresses accordingly.
         * @param id the new id.
         * @return the result of this ID set operation
         */
        public @NotNull IDSetResult setId(@Range(from = 0, to = Short.MAX_VALUE) short id) {
            if (AxonAddress.isSpecialCharacter(id)) return IDSetResult.FAIL_SPECIAL_CODE;
            if (this.getId() == id) return IDSetResult.SUCCESS_UNCHANGED;
            if (hasUpstream()) {
                for (@NotNull Iterator<ConnectorDevice> it = getUpstream().downstream(); it.hasNext(); ) {
                    ConnectorDevice conflictCandidate = it.next();
                    if (id == conflictCandidate.getId()) return IDSetResult.FAIL_CHILD_CONFLICT;
                }
            }
            updateCascade(a -> a.put(level, id));
            setDirty();
            return IDSetResult.SUCCESS;
        }

        /**
         * @return the current ID of this connector.
         */
        public short getId() {
            return getAddress().getShort(level);
        }

        private void updateCascade(Consumer<AxonAddress> action) {
            action.accept(address);
            for (Iterator<ConnectorDevice> it = downstream(); it.hasNext(); ) {
                ConnectorDevice downstream = it.next();
                downstream.updateCascade(action);
            }
        }

        /**
         * Get the address of this connector. DO NOT MODIFY DIRECTLY! Use {@link #setId(short)}!
         * @return the current connector address.
         */
        public @NotNull @UnmodifiableView AxonAddress getAddress() {
            return address;
        }

        public @NotNull ConnectorLevel getLevel() {
            return level;
        }

        @Override
        public @NotNull Map<DeviceDataKey<?>, Object> getData() {
            return data;
        }

        /**
         * Set the capability instance for this connector. Used during route finding.
         * @param cap the capability
         */
        public void setCap(@Nullable T cap) {
            this.cap = cap;
        }

        /**
         * Set the capability instance for this connector. Used during route finding.
         * @return the capability
         */
        public @Nullable T getCap() {
            return cap;
        }

        void setUpstream(@NotNull UUID upstream, @NotNull ConnectorDevice upstreamCache) {
            this.upstream = upstream;
            this.upstreamCache = upstreamCache;
            this.upstreamConnection = new AxonConnection(getType());
            // resolve any potential conflicts with already existing downstreams of our upstream
            ShortSet conflictCandidates = new ShortOpenHashSet(upstreamCache.downstream.size());
            for (@NotNull Iterator<ConnectorDevice> it = upstreamCache.downstream(); it.hasNext(); ) {
                ConnectorDevice conflictCandidate = it.next();
                conflictCandidates.add(conflictCandidate.getId());
            }
            short id = getId();
            while (conflictCandidates.contains(id)) id = (short) (id * 31 + conflictCandidates.size());
            this.address.put(level, id);
            updateCascade(a -> a.copyAtAndAbove(this.upstreamCache.address, SynapseUtil.actualLevel(this)));
        }

        void removeUpstream(@NotNull ConnectorDevice formerUpstream) {
            this.upstream = NO_UPSTREAM;
            this.upstreamCache = null;
            updateCascade(a -> a.clearAtAndAbove(SynapseUtil.actualLevel(formerUpstream)));
        }

        /**
         * @return whether the connector has an upstream connector.
         */
        public boolean hasUpstream() {
            return upstream != NO_UPSTREAM;
        }

        /**
         * @return the upstream connector.
         */
        public @Nullable ConnectorDevice getUpstream() {
            if (upstream != NO_UPSTREAM && upstreamCache == null) {
                upstreamCache = members.get(upstream);
            }
            return upstreamCache;
        }

        void addDownstream(@NotNull UUID downstream, @Nullable ConnectorDevice downstreamCache) {
            this.downstream.put(downstream, downstreamCache);
        }

        void removeDownstream(@NotNull UUID downstream) {
            this.downstream.remove(downstream);
        }

        /**
         * @return an iterator through downstream connectors.
         */
        public @NotNull Iterator<ConnectorDevice> downstream() {
            return new DownstreamIter();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag dat = new CompoundTag();
            AxonAddress.CODEC.encodeStart(NbtOps.INSTANCE, this.address).get()
                    .ifLeft(tag -> dat.put("Address", tag));
            ConnectorLevel.CODEC.encodeStart(NbtOps.INSTANCE, this.level).get()
                    .ifLeft(tag -> dat.put("Level", tag));
            DeviceDataKey.MAP_CODEC.encodeStart(NbtOps.INSTANCE, this.data).get()
                    .ifLeft(tag -> dat.put("Data", tag));

            if (this.upstream != NO_UPSTREAM) {
                dat.putUUID("Upstream", this.upstream);
                AxonConnection.CODEC.encodeStart(NbtOps.INSTANCE, upstreamConnection).get()
                        .ifLeft(t -> dat.put("Connection", t));

            }
            int[] downstream = new int[4 * this.downstream.size()];
            int j = 0;
            for (var down : this.downstream.entrySet()) {
                System.arraycopy(UUIDUtil.uuidToIntArray(down.getKey()), 0, downstream, 4 * j++, 4);
            }
            dat.putIntArray("Downstream", downstream);
            return dat;
        }

        static <T> AxonTree<T>.@NotNull ConnectorDevice from(@NotNull AxonTree<T> tree, @NotNull CompoundTag nbt) {
            AxonTree<T>.ConnectorDevice d = ConnectorLevel.CODEC.parse(NbtOps.INSTANCE, nbt.get("Level")).result()
                    .map(connectionTier -> tree.new ConnectorDevice(AxonAddress.CODEC.parse(NbtOps.INSTANCE, nbt.get("Address"))
                    .result().orElseGet(AxonAddress::new), connectionTier, DeviceDataKey.MAP_CODEC.parse(NbtOps.INSTANCE, nbt.get("Data"))
                            .result().orElseGet(Object2ObjectOpenHashMap::new)))
                    .orElseGet(() -> tree.new ConnectorDevice((short) 0, ConnectorLevel.CORRUPTED));
            d.deserializeNBT(nbt);
            return d;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            if (nbt.hasUUID("Upstream")) {
                this.upstream = nbt.getUUID("Upstream");
                this.upstreamCache = null;
                AxonConnection.CODEC.parse(NbtOps.INSTANCE, nbt.get("Connection")).get()
                        .ifLeft(c -> upstreamConnection = c)
                        .ifRight(r -> upstreamConnection = new AxonConnection(getType()));
            }
            int[] downstream = nbt.getIntArray("Downstream");
            for (int j = 0; j < downstream.length / 4; j++) {
                this.downstream.put(UUIDUtil.uuidFromIntArray(Arrays.copyOfRange(downstream, 4 * j, 4 * j + 4)), null);
            }
        }

        private final class DownstreamIter implements Iterator<ConnectorDevice> {

            final Iterator<Map.Entry<UUID, ConnectorDevice>> backing = downstream.entrySet().iterator();
            Map.Entry<UUID, ConnectorDevice> next;

            @Override
            public boolean hasNext() {
                while (next == null && backing.hasNext()) {
                    next = backing.next();
                    if (next.getValue() == null) {
                        ConnectorDevice cache = members.get(next.getKey());
                        if (cache == null) {
                            backing.remove();
                            next = null;
                        } else {
                            next.setValue(cache);
                        }
                    }
                }
                return next != null;
            }

            @Override
            public ConnectorDevice next() {
                if (!hasNext()) throw new NoSuchElementException();
                ConnectorDevice n = next.getValue();
                next = null;
                return n;
            }
        }
    }
}
