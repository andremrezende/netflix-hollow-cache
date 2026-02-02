package br.com.rezende.cache.hollowcacheagencia.consumer.api;

import com.netflix.hollow.api.objects.delegate.HollowObjectAbstractDelegate;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;
import com.netflix.hollow.core.schema.HollowObjectSchema;
import com.netflix.hollow.api.custom.HollowTypeAPI;
import com.netflix.hollow.api.objects.delegate.HollowCachedDelegate;

@SuppressWarnings("all")
public class AgenciaDelegateCachedImpl extends HollowObjectAbstractDelegate implements HollowCachedDelegate, AgenciaDelegate {

    private final Integer id;
    private final int nomeOrdinal;
    private final int codigoOrdinal;
    private AgenciaTypeAPI typeAPI;

    public AgenciaDelegateCachedImpl(AgenciaTypeAPI typeAPI, int ordinal) {
        this.id = typeAPI.getIdBoxed(ordinal);
        this.nomeOrdinal = typeAPI.getNomeOrdinal(ordinal);
        this.codigoOrdinal = typeAPI.getCodigoOrdinal(ordinal);
        this.typeAPI = typeAPI;
    }

    public int getId(int ordinal) {
        if(id == null)
            return Integer.MIN_VALUE;
        return id.intValue();
    }

    public Integer getIdBoxed(int ordinal) {
        return id;
    }

    public int getNomeOrdinal(int ordinal) {
        return nomeOrdinal;
    }

    public int getCodigoOrdinal(int ordinal) {
        return codigoOrdinal;
    }

    @Override
    public HollowObjectSchema getSchema() {
        return typeAPI.getTypeDataAccess().getSchema();
    }

    @Override
    public HollowObjectTypeDataAccess getTypeDataAccess() {
        return typeAPI.getTypeDataAccess();
    }

    public AgenciaTypeAPI getTypeAPI() {
        return typeAPI;
    }

    public void updateTypeAPI(HollowTypeAPI typeAPI) {
        this.typeAPI = (AgenciaTypeAPI) typeAPI;
    }

}