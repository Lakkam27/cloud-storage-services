package com.Lakkam.cloud.storage.services.Service;

import com.Lakkam.cloud.storage.services.DTO.UserResponseDto;
import com.Lakkam.cloud.storage.services.Enitiy.Folder;
import com.Lakkam.cloud.storage.services.Enitiy.User;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface Service {
    void createUser(User user) throws GeneralSecurityException, IOException;
    UserResponseDto getInfo(String email);
    List<Folder> getUserFolders(String email);
    void createFolder(String email,String location,User user) throws GeneralSecurityException, IOException;
    void uploadFile(String email, String location, MultipartFile file);
    String handleFileUpload( MultipartFile file);
    ResponseEntity<Resource> downloadFile(String fileName);

    void uploadImageToDrive(File tempFile);
}
