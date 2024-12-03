package com.Lakkam.cloud.storage.services.Controller;

import com.Lakkam.cloud.storage.services.DTO.UserResponseDto;
import com.Lakkam.cloud.storage.services.Enitiy.User;
import com.Lakkam.cloud.storage.services.Service.Service;
import com.Lakkam.cloud.storage.services.Service.ServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/")

public class UserController {
    private User currentUser=null;

    private final ServiceImpl serviceImpl;
    private final Service service;

    public UserController(ServiceImpl serviceImpl, Service service) {
        this.serviceImpl = serviceImpl;
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<UserResponseDto> createUser(@RequestBody User user) throws GeneralSecurityException, IOException {
        serviceImpl.createUser(user);
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setUsername(user.getUsername());
        userResponseDto.setId(user.getId());
        currentUser=user;
        userResponseDto.setMessage("User successfully created");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userResponseDto);
    }

    @GetMapping("/usersInfo")
    public ResponseEntity<UserResponseDto> userInfo() {
        return ResponseEntity.status(HttpStatus.OK).body(serviceImpl.getInfo(currentUser.getEmail()));
    }

    @PostMapping("/users/{FolderName}")
    public ResponseEntity<String> createFolder(@PathVariable String FolderName) throws GeneralSecurityException, IOException {
        serviceImpl.createFolder(currentUser.getEmail(), FolderName,currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body("Folder created successfully.");
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile fileToUpload) throws IOException {
        if (fileToUpload.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty.");
        }
        File tempFile = File.createTempFile("temp", null);
        fileToUpload.transferTo(tempFile);
        serviceImpl.uploadImageToDrive(tempFile);
        return ResponseEntity.ok("File is successfully uploaded.");
    }
    @GetMapping(value = "/getAllfiles")
    public ResponseEntity<List<Map<String, String>>> getAllFiles() throws Exception {
       return ResponseEntity.status(HttpStatus.OK).body(serviceImpl.listFoldersInDrive());
    }





}