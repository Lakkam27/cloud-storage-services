package com.Lakkam.cloud.storage.services.Enitiy;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Name of the file
    private String path; // Path to the file on disk

    @ManyToOne
    @JoinColumn(name = "folder_id", nullable = false) // Associates the file with a specific folder
    private Folder folder;

}
