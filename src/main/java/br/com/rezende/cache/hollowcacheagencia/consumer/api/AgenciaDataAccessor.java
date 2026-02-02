package br.com.rezende.cache.hollowcacheagencia.consumer.api;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.consumer.data.AbstractHollowDataAccessor;
import com.netflix.hollow.core.index.key.PrimaryKey;
import com.netflix.hollow.core.read.engine.HollowReadStateEngine;


@SuppressWarnings("all")
public class AgenciaDataAccessor extends AbstractHollowDataAccessor<Agencia> {

    public static final String TYPE = "Agencia";
    private AgenciaAPI api;

    public AgenciaDataAccessor(HollowConsumer consumer) {
        super(consumer, TYPE);
        this.api = (AgenciaAPI)consumer.getAPI();
    }

    public AgenciaDataAccessor(HollowReadStateEngine rStateEngine, AgenciaAPI api) {
        super(rStateEngine, TYPE);
        this.api = api;
    }

    public AgenciaDataAccessor(HollowReadStateEngine rStateEngine, AgenciaAPI api, String ... fieldPaths) {
        super(rStateEngine, TYPE, fieldPaths);
        this.api = api;
    }

    public AgenciaDataAccessor(HollowReadStateEngine rStateEngine, AgenciaAPI api, PrimaryKey primaryKey) {
        super(rStateEngine, TYPE, primaryKey);
        this.api = api;
    }

    @Override public Agencia getRecord(int ordinal){
        return api.getAgencia(ordinal);
    }

}