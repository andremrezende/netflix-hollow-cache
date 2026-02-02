package br.com.rezende.cache.hollowcacheagencia.consumer.api;

import com.netflix.hollow.api.objects.delegate.HollowObjectDelegate;


@SuppressWarnings("all")
public interface AgenciaDelegate extends HollowObjectDelegate {

    public int getId(int ordinal);

    public Integer getIdBoxed(int ordinal);

    public int getNomeOrdinal(int ordinal);

    public int getCodigoOrdinal(int ordinal);

    public AgenciaTypeAPI getTypeAPI();

}