package com.uptalent.util;

import com.uptalent.filestore.exception.EmptyFileException;
import com.uptalent.filestore.exception.IncorrectFileFormatException;
import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.apache.http.entity.ContentType.*;

public class ImageUtils {
    private static final double IMAGE_RESIZE_RATIO = 1.2;

    public static InputStream compressImage(MultipartFile image) throws IOException {
        BufferedImage originalImage = ImageIO.read(image.getInputStream());
        BufferedImage compressedImage = Scalr.resize(
                originalImage,
                Scalr.Method.BALANCED,
                Scalr.Mode.AUTOMATIC,
                (int) (originalImage.getWidth()/IMAGE_RESIZE_RATIO),
                (int) (originalImage.getHeight()/IMAGE_RESIZE_RATIO));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(compressedImage, image.getContentType().split("/")[1], os);
        return new ByteArrayInputStream(os.toByteArray());
    }


    public static String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        int dotIndex = StringUtils.lastIndexOf(originalFilename, '.');
        return StringUtils.substring(originalFilename, dotIndex + 1);
    }

    public static void validateCorrectFileFormat(MultipartFile file) {
        if(!isPng(file) && !isJpeg(file) && !isGif(file) && !isBmp(file) && !isWebp(file)){
            throw new IncorrectFileFormatException("File format must be JPEG, PNG, BMP, WEBP or GIF");
        }
    }

    public static boolean isPng(MultipartFile file) {
        return file.getContentType().equals(IMAGE_PNG.getMimeType());
    }

    public static boolean isJpeg(MultipartFile file) {
        return file.getContentType().equals(IMAGE_JPEG.getMimeType());
    }

    public static boolean isGif(MultipartFile file) {
        return file.getContentType().equals(IMAGE_GIF.getMimeType());
    }

    public static boolean isBmp(MultipartFile file) {
        return file.getContentType().equals(IMAGE_BMP.getMimeType());
    }

    public static boolean isWebp(MultipartFile file) {
        return file.getContentType().equals(IMAGE_WEBP.getMimeType());
    }

    public static void isFileEmpty(MultipartFile file) {
        if(file.isEmpty()){
            throw new EmptyFileException("File must not be empty");
        }
    }
}
