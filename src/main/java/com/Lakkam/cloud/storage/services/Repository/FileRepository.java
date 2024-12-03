package com.Lakkam.cloud.storage.services.Repository;

import com.Lakkam.cloud.storage.services.Enitiy.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
}