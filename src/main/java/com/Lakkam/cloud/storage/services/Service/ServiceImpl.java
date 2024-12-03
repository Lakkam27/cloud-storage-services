package com.Lakkam.cloud.storage.services.Service;

import com.Lakkam.cloud.storage.services.DTO.UserResponseDto;
import com.Lakkam.cloud.storage.services.Enitiy.Folder;
import com.Lakkam.cloud.storage.services.Enitiy.User;
import com.Lakkam.cloud.storage.services.Repository.FileRepository;
import com.Lakkam.cloud.storage.services.Repository.FolderRepository;
import com.Lakkam.cloud.storage.services.Repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.*;

@Slf4j
@org.springframework.stereotype.Service
public class ServiceImpl implements Service {

    @Value("${upload-dir}")
    private String uploadDir;
    @Value("${folderId}")
    private  String folderId;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String SERVICE_ACOUNT_KEY_PATH = getPathToGoodleCredentials();

    private static String getPathToGoodleCredentials() {
        String currentDirectory = System.getProperty("user.dir");
        Path filePath = Paths.get(currentDirectory, "cred.json");
        return filePath.toString();
    }
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private FileRepository fileRepository;



    @Override
    public void createUser(User user) throws GeneralSecurityException, IOException {
        if(userRepository.existsByEmail(user.getEmail())){
            return;
        }
        userRepository.save(user);
        Folder rootFolder = new Folder();
        rootFolder.setName(user.getEmail());
        rootFolder.setUser(user);
        Drive drive = createDriveService();
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(rootFolder.getName());
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        fileMetadata.setParents(Collections.singletonList(folderId));
        com.google.api.services.drive.model.File folder =
                drive.files().create(fileMetadata)
                        .setFields("id")
                        .execute();
        rootFolder.setDriveId(folder.getId());
        folderId = folder.getId();
        folderRepository.save(rootFolder);
    }

    @Override
    public UserResponseDto getInfo(String email) {
        UserResponseDto userResponseDto = new UserResponseDto();
        User user = userRepository.findByEmail(email);
        if (user != null) {
            userResponseDto.setEmail(user.getEmail());
            userResponseDto.setUsername(user.getUsername());
            userResponseDto.setMessage("Here we go, your details");
        } else {
            userResponseDto.setMessage("No user found with the given email.");
        }
        return userResponseDto;
    }

    @Override
    public List<Folder> getUserFolders(String email) {
        return List.of();
    }



    @Override
    public void createFolder(String email, String folderName,User user) throws GeneralSecurityException, IOException {
        Folder rootFolder = new Folder();
        rootFolder.setName(folderName);
        rootFolder.setUser(user);
        Drive drive = createDriveService();
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(rootFolder.getName());
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        fileMetadata.setParents(Collections.singletonList(folderId));
        com.google.api.services.drive.model.File folder =
                drive.files().create(fileMetadata)
                        .setFields("id")
                        .execute();
        rootFolder.setDriveId(folder.getId());
        folderRepository.save(rootFolder);
    }


    @Override
    public void uploadFile(String email, String location, MultipartFile file) {
        try {
            // Validate file existence
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            // Create a directory for the file
            java.io.File directory = new java.io.File("uploads/" + location);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Save the file to disk
            java.io.File destination = new java.io.File(directory, file.getOriginalFilename());
            file.transferTo(destination);

            // Optionally save metadata (e.g., filename, email, location) to the database
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public String handleFileUpload(MultipartFile file) {
        try {
            Path directory = Paths.get(uploadDir);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            Path filePath = directory.resolve(file.getOriginalFilename());
            log.info("file path : {}", filePath);
            Files.write(filePath, file.getBytes());
            return "File uploaded successfully: " + file.getOriginalFilename()+filePath;
        } catch (IOException e) {
            return "Failed to upload file: " + e.getMessage();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public ResponseEntity<Resource> downloadFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            log.info("Download file path: {}", filePath);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.error("File not found or unreadable: {}", fileName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            log.error("Error while downloading file: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public void uploadImageToDrive(File tempFile) {
        try{
            com.Lakkam.cloud.storage.services.Enitiy.File myfileinfo=new  com.Lakkam.cloud.storage.services.Enitiy.File();
            Drive drive = createDriveService();
            com.google.api.services.drive.model.File fileMetaData = new com.google.api.services.drive.model.File();
            fileMetaData.setName(tempFile.getName());
            fileMetaData.setParents(Collections.singletonList(folderId));
            FileContent mediaContent = new FileContent("image/jpeg", tempFile);
            com.google.api.services.drive.model.File uploadedFile = drive.files().create(fileMetaData, mediaContent)
                    .setFields("id").execute();
            String fileUrl = "https://drive.google.com/uc?export=view&id="+uploadedFile.getId();
            System.out.println("File URL: " + fileUrl);
            tempFile.delete();
            myfileinfo.setName(tempFile.getName());
            myfileinfo.setPath(fileUrl);

        }catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        }


    }

    private Drive createDriveService()  throws GeneralSecurityException, IOException  {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(SERVICE_ACOUNT_KEY_PATH))
                .createScoped(Collections.singleton(DriveScopes.DRIVE));
        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .build();

    }

    public List<Map<String, String>> listFoldersInDrive() throws Exception {
        Drive drive = createDriveService();
        List<Map<String, String>> folderDetails = new ArrayList<>();

        String pageToken = null;
        do {
            FileList result = drive.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name, parents)")
                    .setPageToken(pageToken)
                    .execute();

            for (com.google.api.services.drive.model.File folder : result.getFiles()) {
                Map<String, String> folderInfo = new HashMap<>();
                folderInfo.put("name", folder.getName());
                folderInfo.put("id", folder.getId());

                // Get parent folder details if available
                if (folder.getParents() != null && !folder.getParents().isEmpty()) {
                    String parentId = folder.getParents().get(0);
                    com.google.api.services.drive.model.File parentFolder =
                            drive.files().get(parentId).setFields("name").execute();
                    folderInfo.put("parentName", parentFolder.getName());
                    folderInfo.put("parentId", parentId);
                } else {
                    folderInfo.put("parentName", "Root");
                }

                folderDetails.add(folderInfo);
            }

            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return folderDetails;
    }


}
