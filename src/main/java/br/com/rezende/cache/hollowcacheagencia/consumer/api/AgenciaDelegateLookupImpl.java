package br.com.rezende.cache.hollowcacheagencia.consumer.api;

import com.netflix.hollow.api.objects.delegate.HollowObjectAbstractDelegate;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;
import com.netflix.hollow.core.schema.HollowObjectSchema;

@SuppressWarnings("all")
public class AgenciaDelegateLookupImpl extends HollowObjectAbstractDelegate implements AgenciaDelegate {

    private final AgenciaTypeAPI typeAPI;

    public AgenciaDelegateLookupImpl(AgenciaTypeAPI typeAPI) {
        this.typeAPI = typeAPI;
    }

    public int getId(int ordinal) {
        return typeAPI.getId(ordinal);
    }

    public Integer getIdBoxed(int ordinal) {
        return typeAPI.getIdBoxed(ordinal);
    }

    public int getNomeOrdinal(int ordinal) {
        return typeAPI.getNomeOrdinal(ordinal);
    }

    public int getCodigoOrdinal(int ordinal) {
        return typeAPI.getCodigoOrdinal(ordinal);
    }

    public AgenciaTypeAPI getTypeAPI() {
        return typeAPI;
    }

    @Override
    public HollowObjectSchema getSchema() {
        return typeAPI.getTypeDataAccess().getSchema();
    }

    @Override
    public HollowObjectTypeDataAccess getTypeDataAccess() {
        return typeAPI.getTypeDataAccess();
    }

}