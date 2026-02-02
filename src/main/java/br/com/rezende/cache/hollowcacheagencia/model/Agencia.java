package br.com.rezende.cache.hollowcacheagencia.model;

import com.netflix.hollow.core.write.objectmapper.HollowPrimaryKey;

import java.util.Objects;

@HollowPrimaryKey(fields="id")
public class Agencia {
    private int id;
    private String nome;
    private String codigo;

    // Construtor vazio necessário para o Hollow
    public Agencia() {
    }

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Agencia agencia = (Agencia) o;
        return id == agencia.id && Objects.equals(nome, agencia.nome) && Objects.equals(codigo, agencia.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, codigo);
    }

    @Override
    public String toString() {
        return "Agencia{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", codigo='" + codigo + '\'' +
                '}';
    }
}