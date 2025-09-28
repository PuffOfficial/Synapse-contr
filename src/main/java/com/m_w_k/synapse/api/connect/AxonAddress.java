package com.m_w_k.synapse.api.connect;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ShortRBTreeMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Map;
import java.util.function.UnaryOperator;

public final class AxonAddress extends Object2ShortRBTreeMap<ConnectorLevel> {

    public static final Codec<AxonAddress> CODEC = Codec.unboundedMap(ConnectorLevel.CODEC, Codec.SHORT).xmap(AxonAddress::new, UnaryOperator.identity());

    public static final short WILDCARD = -1;
    public static final short UNIVERSAL_WILDCARD = -2;
    public static final short EMPTY = -3;

    public AxonAddress() {
        super(Comparator.comparingDouble(ConnectorLevel::getPrio));
        defaultReturnValue(EMPTY);
    }

    public AxonAddress(Map<ConnectorLevel, Short> map) {
        this();
        putAll(map);
    }

    public @NotNull AxonAddress copy() {
        return new AxonAddress(this);
    }

    public void copyAbove(@NotNull AxonAddress source, @NotNull ConnectorLevel level) {
        for (var entry : source.object2ShortEntrySet()) {
            if (entry.getKey().getPrio() > level.getPrio()) {
                this.put(entry.getKey(), entry.getShortValue());
            }
        }
    }

    public void clearAbove(@NotNull ConnectorLevel level) {
        for (var entry : object2ShortEntrySet()) {
            if (entry.getKey().getPrio() > level.getPrio()) {
                removeShort(level);
            }
        }
    }

    /**
     * Creates a wildcard address for matching with other addresses. The wildcardness is not preserved through Codec read/write.
     * @param universal whether the wildcard should match empty address sections as well.
     * @return an address that by default matches with any other address, and can have specificity added.
     */
    public static AxonAddress wildcard(boolean universal) {
        AxonAddress ret = new AxonAddress();
        ret.defaultReturnValue(universal ? UNIVERSAL_WILDCARD : WILDCARD);
        return ret;
    }

    public boolean hasPort() {
        return this.containsKey(ConnectorLevel.ENDPOINT);
    }

    public short getPort() {
        return this.getShort(ConnectorLevel.ENDPOINT);
    }

    public short[] getAddress() {
        ShortList list = new ShortArrayList(this.size());
        for (ConnectorLevel tier : ConnectorLevel.values()) {
            if (!Double.isFinite(tier.getPrio())) continue;
            list.add(this.getShort(tier));
        }
        return list.toShortArray();
    }

    public boolean matches(AxonAddress other) {
        if (other == this) return true;
        for (ConnectorLevel tier : ConnectorLevel.values()) {
            if (!matchesAt(other, tier)) return false;
        }
        return true;
    }

    public boolean matchesAtAndAbove(@NotNull AxonAddress other, @NotNull ConnectorLevel tier) {
        if (other == this) return true;
        for (ConnectorLevel t : ConnectorLevel.values()) {
            if (Double.isFinite(tier.getPrio()) && t.getPrio() >= tier.getPrio() && !matchesAt(other, t)) {
                return false;
            }
        }
        return true;
    }

    public boolean matchesAt(@NotNull AxonAddress other, @NotNull ConnectorLevel tier) {
        short us = this.getShort(tier);
        if (us == UNIVERSAL_WILDCARD) return true;
        short them = other.getShort(tier);
        if (them == UNIVERSAL_WILDCARD) return true;
        if (us == them) return true;
        return (us != EMPTY && them == WILDCARD) || (them != EMPTY && us == WILDCARD);
    }

    /**
     * Parse a string address. Expects Hexadecimal representation of the shorts.
     * @param address the string address to parse.
     * @see #toString()
     * @return the parsed address, or a failure reason if it failed in an expected way.
     */
    public static @NotNull Either<AxonAddress, ParseFailure> parse(@NotNull String address) {
        String[] split = address.split(":");
        if (split.length > 2) return Either.right(ParseFailure.TOO_MANY);
        try {
            AxonAddress buildingAddress = new AxonAddress();
            if (split.length == 2) {
                short port = Short.parseShort(split[1], 16);
                buildingAddress.put(ConnectorLevel.ENDPOINT, port);
            }
            split = split[0].split("\\.");
            ConnectorLevel[] tiers = ConnectorLevel.values();
            if (split.length > tiers.length - 2) {
                return Either.right(ParseFailure.TOO_LONG);
            }
            for (int i = 0; i < split.length; i++) {
                buildingAddress.put(tiers[i + 2], switch (split[i]) {
                    case "" -> EMPTY;
                    case "**" -> UNIVERSAL_WILDCARD;
                    case "*" -> WILDCARD;
                    default -> Short.parseShort(split[i], 16);
                });
            }
            return Either.left(buildingAddress);
        } catch (NumberFormatException e) {
            return Either.right(ParseFailure.NUMERICAL);
        }
    }

    /**
     * Converts the address to a string representation in Hexadecimal.
     * @see #parse(String)
     * @return the stringified address
     */
    @Override
    public String toString() {
        StringBuilder composed = new StringBuilder();
        short[] address = getAddress();
        for (int i = 0; i < address.length; i++) {
            short s = address[i];
            if (s >= 0) {
                composed.append(Integer.toHexString(s & 0xffff));
            } else if (s == UNIVERSAL_WILDCARD) {
                composed.append("**");
            } else if (s == WILDCARD) {
                composed.append('*');
            }
            if (i != address.length - 1) {
                composed.append('.');
            }
        }
        composed.append(":").append(getPort());
        return composed.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof AxonAddress a)) return false;
        for (ConnectorLevel tier : ConnectorLevel.values()) {
            if (this.getShort(tier) != a.getShort(tier)) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int cumulative = 0;
        for (ConnectorLevel tier : ConnectorLevel.values()) {
            cumulative *= 127;
            cumulative += this.getShort(tier);
        }
        return cumulative;
    }

    public enum ParseFailure {
        TOO_MANY, TOO_LONG, NUMERICAL
    }
}
