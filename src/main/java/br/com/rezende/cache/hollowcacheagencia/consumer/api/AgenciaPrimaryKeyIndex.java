package br.com.rezende.cache.hollowcacheagencia.consumer.api;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.consumer.index.AbstractHollowUniqueKeyIndex;
import com.netflix.hollow.api.consumer.index.HollowUniqueKeyIndex;
import com.netflix.hollow.core.schema.HollowObjectSchema;

/**
 * @deprecated see {@link com.netflix.hollow.api.consumer.index.UniqueKeyIndex} which can be built as follows:
 * <pre>{@code
 *     UniqueKeyIndex<Agencia, K> uki = UniqueKeyIndex.from(consumer, Agencia.class)
 *         .usingBean(k);
 *     Agencia m = uki.findMatch(k);
 * }</pre>
 * where {@code K} is a class declaring key field paths members, annotated with
 * {@link com.netflix.hollow.api.consumer.index.FieldPath}, and {@code k} is an instance of
 * {@code K} that is the key to find the unique {@code Agencia} object.
 */
@Deprecated

@SuppressWarnings("all")
public class AgenciaPrimaryKeyIndex extends AbstractHollowUniqueKeyIndex<AgenciaAPI, Agencia> implements HollowUniqueKeyIndex<Agencia> {

    public AgenciaPrimaryKeyIndex(HollowConsumer consumer) {
        this(consumer, true);
    }

    public AgenciaPrimaryKeyIndex(HollowConsumer consumer, boolean isListenToDataRefresh) {
        this(consumer, isListenToDataRefresh, ((HollowObjectSchema)consumer.getStateEngine().getNonNullSchema("Agencia")).getPrimaryKey().getFieldPaths());
    }

    public AgenciaPrimaryKeyIndex(HollowConsumer consumer, String... fieldPaths) {
        this(consumer, true, fieldPaths);
    }

    public AgenciaPrimaryKeyIndex(HollowConsumer consumer, boolean isListenToDataRefresh, String... fieldPaths) {
        super(consumer, "Agencia", isListenToDataRefresh, fieldPaths);
    }

    @Override
    public Agencia findMatch(Object... keys) {
        int ordinal = idx.getMatchingOrdinal(keys);
        if(ordinal == -1)
            return null;
        return api.getAgencia(ordinal);
    }

}