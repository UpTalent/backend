package com.uptalent.util;

import com.uptalent.filestore.exception.EmptyFileException;
import com.uptalent.filestore.exception.IncorrectFileFormatException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

import static org.apache.http.entity.ContentType.IMAGE_JPEG;
import static org.apache.http.entity.ContentType.IMAGE_PNG;

public class ImageUtils {

    public static InputStream compressImage(MultipartFile image) throws IOException {
        BufferedImage originalImage = ImageIO.read(image.getInputStream());
        ByteArrayOutputStream compressedImage = new ByteArrayOutputStream();
        ImageOutputStream outputStream = ImageIO.createImageOutputStream(compressedImage);

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(getFileExtension(image));
        ImageWriter writer = writers.next();

        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writeParam.setCompressionQuality(0.5f);

        writer.setOutput(outputStream);
        writer.write(null, new IIOImage(originalImage, null, null), writeParam);

        return new ByteArrayInputStream(compressedImage.toByteArray());
    }


    public static String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        int dotIndex = StringUtils.lastIndexOf(originalFilename, '.');
        return StringUtils.substring(originalFilename, dotIndex + 1);
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
