package com.uptalent.filestore;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.uptalent.filestore.exception.FailedToUploadFileException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FileStoreService {
    private final AmazonS3 s3;

    public void save(String path,
                     String fileName,
                     Optional<Map<String, String>> optionalMetadata,
                     InputStream inputStream) {
        ObjectMetadata metadata = new ObjectMetadata();
        optionalMetadata.ifPresent(map -> {
            if(!map.isEmpty()){
                map.forEach(metadata::addUserMetadata);
            }
        });

        try{
            metadata.setContentLength(inputStream.available());
            s3.putObject(path, fileName, inputStream, metadata);
        } catch (AmazonServiceException | IOException e){
            throw new FailedToUploadFileException("Failed to upload file: " + e.getMessage());
        }
    }
}
