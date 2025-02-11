package com.wallisonlemos.petshop.model.domain.cliente;

import com.wallisonlemos.petshop.model.domain.cliente.enums.ContatoTipo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "contatos")
@SQLDelete(sql = "UPDATE contatos SET deleted = true, deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class Contato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String tag;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContatoTipo tipo;

    @NotBlank
    @Column(nullable = false)
    private String valor;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted")
    private boolean deleted = false;

    public Contato(String tag, ContatoTipo tipo, String valor, Cliente cliente) {
        this.tag = tag;
        this.tipo = tipo;
        this.valor = valor;
        this.cliente = cliente;
    }
}
