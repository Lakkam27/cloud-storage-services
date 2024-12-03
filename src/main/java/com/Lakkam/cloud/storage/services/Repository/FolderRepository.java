package com.Lakkam.cloud.storage.services.Repository;

import com.Lakkam.cloud.storage.services.Enitiy.File;
import com.Lakkam.cloud.storage.services.Enitiy.Folder;
import com.Lakkam.cloud.storage.services.Enitiy.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder,Long> {
    static void save(File fileEntity) {
    }

    Folder findByNameAndParentFolderAndUser(String folderName, Folder parentFolder, User user);
}
