package br.com.rezende.cache.hollowcacheagencia.consumer.api;

import com.netflix.hollow.api.custom.HollowObjectTypeAPI;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;


@SuppressWarnings("all")
public class AgenciaTypeAPI extends HollowObjectTypeAPI {

    private final AgenciaDelegateLookupImpl delegateLookupImpl;

    public AgenciaTypeAPI(AgenciaAPI api, HollowObjectTypeDataAccess typeDataAccess) {
        super(api, typeDataAccess, new String[] {
            "id",
            "nome",
            "codigo"
        });
        this.delegateLookupImpl = new AgenciaDelegateLookupImpl(this);
    }

    public int getId(int ordinal) {
        if(fieldIndex[0] == -1)
            return missingDataHandler().handleInt("Agencia", ordinal, "id");
        return getTypeDataAccess().readInt(ordinal, fieldIndex[0]);
    }

    public Integer getIdBoxed(int ordinal) {
        int i;
        if(fieldIndex[0] == -1) {
            i = missingDataHandler().handleInt("Agencia", ordinal, "id");
        } else {
            boxedFieldAccessSampler.recordFieldAccess(fieldIndex[0]);
            i = getTypeDataAccess().readInt(ordinal, fieldIndex[0]);
        }
        if(i == Integer.MIN_VALUE)
            return null;
        return Integer.valueOf(i);
    }



    public int getNomeOrdinal(int ordinal) {
        if(fieldIndex[1] == -1)
            return missingDataHandler().handleReferencedOrdinal("Agencia", ordinal, "nome");
        return getTypeDataAccess().readOrdinal(ordinal, fieldIndex[1]);
    }

    public StringTypeAPI getNomeTypeAPI() {
        return getAPI().getStringTypeAPI();
    }

    public int getCodigoOrdinal(int ordinal) {
        if(fieldIndex[2] == -1)
            return missingDataHandler().handleReferencedOrdinal("Agencia", ordinal, "codigo");
        return getTypeDataAccess().readOrdinal(ordinal, fieldIndex[2]);
    }

    public StringTypeAPI getCodigoTypeAPI() {
        return getAPI().getStringTypeAPI();
    }

    public AgenciaDelegateLookupImpl getDelegateLookupImpl() {
        return delegateLookupImpl;
    }

    @Override
    public AgenciaAPI getAPI() {
        return (AgenciaAPI) api;
    }

}