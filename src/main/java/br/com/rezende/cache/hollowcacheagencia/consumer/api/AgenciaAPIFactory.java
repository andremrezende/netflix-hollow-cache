package br.com.rezende.cache.hollowcacheagencia.consumer.api;

import com.netflix.hollow.api.client.HollowAPIFactory;
import com.netflix.hollow.api.custom.HollowAPI;
import com.netflix.hollow.api.objects.provider.HollowFactory;
import com.netflix.hollow.core.read.dataaccess.HollowDataAccess;
import java.util.Collections;
import java.util.Set;

@SuppressWarnings("all")
public class AgenciaAPIFactory implements HollowAPIFactory {

    private final Set<String> cachedTypes;

    public AgenciaAPIFactory() {
        this(Collections.<String>emptySet());
    }

    public AgenciaAPIFactory(Set<String> cachedTypes) {
        this.cachedTypes = cachedTypes;
    }

    @Override
    public HollowAPI createAPI(HollowDataAccess dataAccess) {
        return new AgenciaAPI(dataAccess, cachedTypes);
    }

    @Override
    public HollowAPI createAPI(HollowDataAccess dataAccess, HollowAPI previousCycleAPI) {
        if (!(previousCycleAPI instanceof AgenciaAPI)) {
            throw new ClassCastException(previousCycleAPI.getClass() + " not instance of AgenciaAPI");        }
        return new AgenciaAPI(dataAccess, cachedTypes, Collections.<String, HollowFactory<?>>emptyMap(), (AgenciaAPI) previousCycleAPI);
    }

}