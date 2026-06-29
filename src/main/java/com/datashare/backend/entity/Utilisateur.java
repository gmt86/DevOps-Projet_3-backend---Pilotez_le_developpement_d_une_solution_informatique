package com.datashare.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "utilisateurs")
@Data // génère aussi @EqualsAndHashCode et @ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // pour éviter les boucles infinies sur des relations @OneToMany ou @ManyToMany
@NoArgsConstructor
@AllArgsConstructor
@Builder // annotation Lombok pour créer des objets de façon lisible. Évite les constructeurs avec trop de paramètres
public class Utilisateur implements UserDetails {

    @Id
    @EqualsAndHashCode.Include  // uniquement l'id pour equals/hashCode
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Email   
    @Column(name = "email",nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(name = "password", nullable = false)
    private String password;

  
    @CreationTimestamp
    @Column(name = "dateCreation")
    private LocalDateTime dateCreation;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}