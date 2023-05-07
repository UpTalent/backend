package com.uptalent.filestore;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.uptalent.credentials.model.enums.Role;
import com.uptalent.filestore.exception.FailedToUploadFileException;
import com.uptalent.sponsor.exception.SponsorNotFoundException;
import com.uptalent.sponsor.model.entity.Sponsor;
import com.uptalent.sponsor.repository.SponsorRepository;
import com.uptalent.talent.exception.TalentNotFoundException;
import com.uptalent.talent.model.entity.Talent;
import com.uptalent.talent.repository.TalentRepository;
import com.uptalent.util.service.AccessVerifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static com.uptalent.credentials.model.enums.Role.SPONSOR;
import static com.uptalent.credentials.model.enums.Role.TALENT;
import static com.uptalent.filestore.FileStoreOperation.AVATAR;
import static com.uptalent.util.ImageUtils.*;
import static com.uptalent.util.ImageUtils.compressImage;

@Service
@RequiredArgsConstructor
@Transactional
public class FileStoreService {
    @Value("${aws.bucket.name}")
    private String bucketName;
    private final AmazonS3 s3;
    private final AccessVerifyService accessVerifyService;
    private final TalentRepository talentRepository;
    private final SponsorRepository sponsorRepository;

    public void uploadImage(Long id, MultipartFile image, FileStoreOperation operation) {
        validateFile(image);

        Role role = accessVerifyService.getRole();
        accessVerifyService.tryGetAccess(
                id,
                role,
                "You don't have permissions to edit this user"
        );


        String imageType = role.equals(TALENT) ? getImageType(operation) : getImageType(AVATAR);
        String key = getKey(id, role);
        String filename = getFilename(imageType, image);
        String url = generateUrl(id, filename, role);

        try (InputStream inputStream = compressImage(image)) {
            saveImage(key, filename, inputStream);
            updateEntity(id, role, url, operation);
        } catch (IOException e) {
            throw new FailedToUploadFileException(e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public void deleteImageByUserId(Long id) {
        Role role = accessVerifyService.getRole();
        deleteImageByUserIdAndRole(id, role);
    }

    @Transactional(readOnly = true)
    public void deleteImageByUserIdAndRole(Long id, Role role) {
        if (role.equals(TALENT)) {
            deleteImageIfPresent(talentRepository.findAvatarByTalentId(id));
            deleteImageIfPresent(talentRepository.findBannerByTalentId(id));
        } else if (role.equals(SPONSOR)) {
            deleteImageIfPresent(sponsorRepository.findAvatarBySponsorId(id));
        }
    }

    private void deleteImageIfPresent(Optional<String> imageUrl) {
        if(imageUrl.isPresent()){
            int prefixLength = "https://".length();
            String image = imageUrl.get();
            int bucketNameEndIndex = image.indexOf(".s3.amazonaws.com/");
            String bucketName = image.substring(prefixLength, bucketNameEndIndex);
            String key = image.substring(bucketNameEndIndex + ".s3.amazonaws.com/".length());
            s3.deleteObject(bucketName, key);
        }
    }

    private Sponsor getSponsorById(Long id) {
        return sponsorRepository.findById(id)
                .orElseThrow(() -> new SponsorNotFoundException("Sponsor was not found"));
    }

    private Talent getTalentById(Long id) {
        return talentRepository.findById(id)
                .orElseThrow(() -> new TalentNotFoundException("Talent was not found"));
    }


    private void validateFile(MultipartFile image) {
        isFileEmpty(image);
        isImage(image);
    }

    private String getImageType(FileStoreOperation operation) {
        return operation.name().toLowerCase();
    }

    private String getKey(Long id, Role role) {
        return String.format("%s/%s/%s",
                bucketName, role.name().toLowerCase(), id);
    }

    private String getFilename(String imageType, MultipartFile image) {
        return String.format("%s.%s", imageType, getFileExtension(image));
    }

    private String generateUrl(Long id, String filename, Role role) {
        return String.format("https://%s.s3.amazonaws.com/%s/%s/%s",
                bucketName, role.name().toLowerCase(), id, filename);
    }

    private void saveImage(String key, String filename, InputStream inputStream) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(inputStream.available());
        s3.putObject(key, filename, inputStream, metadata);
    }

    private void updateEntity(Long id, Role role, String imageUrl, FileStoreOperation operation) {
        if (role.equals(TALENT)) {
            Talent talent = getTalentById(id);
            if (operation.equals(AVATAR)) {
                talent.setAvatar(imageUrl);
            } else {
                talent.setBanner(imageUrl);
            }
            talentRepository.save(talent);
        } else if (role.equals(SPONSOR)) {
            Sponsor sponsor = getSponsorById(id);
            sponsor.setAvatar(imageUrl);
            sponsorRepository.save(sponsor);
        }
    }
}
