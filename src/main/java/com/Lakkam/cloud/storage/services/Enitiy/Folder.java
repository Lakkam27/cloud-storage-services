package com.Lakkam.cloud.storage.services.Enitiy;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false) // Link the folder to the owning user
    private User user;

    @ManyToOne
    @JsonIgnore
    @JsonBackReference
    @JoinColumn(name = "parent_folder_id") // Self-referential relationship for nested folders
    private Folder parentFolder;

    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Folder> subFolders; // Subfolders within this folder

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> files; // Files in this folder

    public void setDriveId(String id) {
    }
}
