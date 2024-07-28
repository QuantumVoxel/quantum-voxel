package dev.ultreon.quantum.client.util;

import com.badlogic.gdx.math.*;
import com.mojang.serialization.Codec;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ExtraCodecs {
    public static final Codec<UUID> UUID = Codec.STRING.xmap(java.util.UUID::fromString, java.util.UUID::toString);
    public static final Codec<LocalDateTime> LOCAL_DATE_TIME = Codec.STRING.xmap(
            s -> LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            ldt -> ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    );

    public static final Codec<ZonedDateTime> ZONED_DATE_TIME = Codec.STRING.xmap(
            s -> ZonedDateTime.parse(s, DateTimeFormatter.ISO_ZONED_DATE_TIME),
            zdt -> zdt.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
    );

    public static final Codec<BigInteger> BIG_INTEGER = Codec.STRING.xmap(
            BigInteger::new,
            BigInteger::toString
    );

    public static final Codec<BigDecimal> BIG_DECIMAL = Codec.STRING.xmap(
            BigDecimal::new,
            BigDecimal::toString
    );

    public static final Codec<Vector2> VECTOR_2 = Codec.list(Codec.FLOAT, 2, 2).xmap(
            list -> new Vector2(list.getFirst(), list.get(1)),
            vec -> List.of(vec.x, vec.y)
    );

    public static final Codec<Vector3> VECTOR_3 = Codec.list(Codec.FLOAT, 3, 3).xmap(
            list -> new Vector3(list.getFirst(), list.get(1), list.get(2)),
            vec -> List.of(vec.x, vec.y, vec.z)
    );

    public static final Codec<Quaternion> QUATERNION = Codec.list(Codec.FLOAT, 4, 4).xmap(
            list -> new Quaternion(list.getFirst(), list.get(1), list.get(2), list.get(3)),
            quat -> List.of(quat.x, quat.y, quat.z, quat.w)
    );

    public static final Codec<GridPoint2> GRID_POINT_2 = Codec.list(Codec.INT, 2, 2).xmap(
            list -> new GridPoint2(list.getFirst(), list.get(1)),
            vec -> List.of(vec.x, vec.y)
    );

    public static final Codec<GridPoint3> GRID_POINT_3 = Codec.list(Codec.INT, 3, 3).xmap(
            list -> new GridPoint3(list.getFirst(), list.get(1), list.get(2)),
            vec -> List.of(vec.x, vec.y, vec.z)
    );

    public static <T extends Enum<T>> Codec<T> enumCodec(Class<T> enumClass) {
        return Codec.STRING.xmap(
                s -> Enum.valueOf(enumClass, s),
                Enum::name
        );
    }
}
