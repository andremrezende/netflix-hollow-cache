package br.com.matera.cache.hollowcacheagencia.model;

import com.netflix.hollow.core.write.objectmapper.HollowPrimaryKey;
@HollowPrimaryKey(fields="id")
public class Agencia {
    private int id;
    private String nome;
    private String codigo;

    // Construtores, Getters e Setters
    public Agencia(int id, String nome, String codigo) {
        this.id = id;
        this.nome = nome;
        this.codigo = codigo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}