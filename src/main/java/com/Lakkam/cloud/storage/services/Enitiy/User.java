package com.Lakkam.cloud.storage.services.Enitiy;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;
@Data
@Entity
@Table(name = "app_user")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username; // User's name or unique identifier
    private String email;    // User's email address

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) // Use 'user' instead of 'owner'
    private List<Folder> folders; // Root folders owned by the user

}
