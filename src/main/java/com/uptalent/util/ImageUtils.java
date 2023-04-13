package com.uptalent.util;

import com.uptalent.filestore.exception.EmptyFileException;
import com.uptalent.filestore.exception.IncorrectFileFormatException;
import org.imgscalr.Scalr;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.apache.http.entity.ContentType.IMAGE_JPEG;
import static org.apache.http.entity.ContentType.IMAGE_PNG;

public class ImageUtils {
    public static InputStream resizeImage(MultipartFile image) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(image.getInputStream());

        int targetWidth = bufferedImage.getWidth() / 2;
        int targetHeight = bufferedImage.getHeight() / 2;

        BufferedImage resizedImage = Scalr.resize(bufferedImage,
                Scalr.Method.AUTOMATIC,
                Scalr.Mode.AUTOMATIC,
                targetWidth,
                targetHeight);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, getFileExtension(image), os);

        return new ByteArrayInputStream(os.toByteArray());
    }

    public static String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        int dotIndex = originalFilename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : originalFilename.substring(dotIndex + 1);
    }

    public static void isImage(MultipartFile file) {
        if(!Arrays.asList(IMAGE_JPEG.getMimeType(), IMAGE_PNG.getMimeType()).contains(file.getContentType())){
            throw new IncorrectFileFormatException("File must be an image");
        }
    }

    public static void isFileEmpty(MultipartFile file) {
        if(file.isEmpty()){
            throw new EmptyFileException("File must not be empty");
        }
    }
}
