package br.com.rezende.cache.hollowcacheagencia.consumer.api;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.consumer.index.UniqueKeyIndex;
import com.netflix.hollow.api.objects.HollowObject;
import com.netflix.hollow.core.schema.HollowObjectSchema;


@SuppressWarnings("all")
public class Agencia extends HollowObject {

    public Agencia(AgenciaDelegate delegate, int ordinal) {
        super(delegate, ordinal);
    }

    public int getId() {
        return delegate().getId(ordinal);
    }

    public Integer getIdBoxed() {
        return delegate().getIdBoxed(ordinal);
    }

    public HString getNome() {
        int refOrdinal = delegate().getNomeOrdinal(ordinal);
        if(refOrdinal == -1)
            return null;
        return  api().getHString(refOrdinal);
    }

    public HString getCodigo() {
        int refOrdinal = delegate().getCodigoOrdinal(ordinal);
        if(refOrdinal == -1)
            return null;
        return  api().getHString(refOrdinal);
    }

    public AgenciaAPI api() {
        return typeApi().getAPI();
    }

    public AgenciaTypeAPI typeApi() {
        return delegate().getTypeAPI();
    }

    protected AgenciaDelegate delegate() {
        return (AgenciaDelegate)delegate;
    }

    /**
     * Creates a unique key index for {@code Agencia} that has a primary key.
     * The primary key is represented by the type {@code int}.
     * <p>
     * By default the unique key index will not track updates to the {@code consumer} and thus
     * any changes will not be reflected in matched results.  To track updates the index must be
     * {@link HollowConsumer#addRefreshListener(HollowConsumer.RefreshListener) registered}
     * with the {@code consumer}
     *
     * @param consumer the consumer
     * @return the unique key index
     */
    public static UniqueKeyIndex<Agencia, Integer> uniqueIndex(HollowConsumer consumer) {
        return UniqueKeyIndex.from(consumer, Agencia.class)
            .bindToPrimaryKey()
            .usingPath("id", int.class);
    }

}