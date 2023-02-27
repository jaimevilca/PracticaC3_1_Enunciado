package es.codeurjc.mca.events.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("storageService")
@Profile("production")
public class S3ImageService implements ImageService {

    @Autowired
    private AwsS3Config s3;

    @Value("${spring.web.resources.static-locations}")
    private String STATIC_FOLDER;

    private String staticFolder() {
        return System.getProperty("user.dir") + "/" + STATIC_FOLDER.split(":")[1];
    }

    @Override
    public String createImage(MultipartFile multiPartFile) {
        String fileName = multiPartFile.getOriginalFilename();
        String path = "events/" + fileName;
        File file = new File(staticFolder() + path);
        try {
            multiPartFile.transferTo(file);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't save image in S3", ex);
        }

        PutObjectResponse response =
                s3.getAmazonS3Client().putObject(
                        PutObjectRequest.builder()
                                .bucket(s3.getbucketName())
                                .key(multiPartFile.getOriginalFilename())
                                .acl(ObjectCannedACL.PUBLIC_READ)
                                .build(),
                        RequestBody.fromFile(file)
                );

        GetUrlRequest request = GetUrlRequest.builder().bucket(s3.getbucketName()).key(multiPartFile.getOriginalFilename()).build();

        return s3.getAmazonS3Client().utilities().getUrl(request).toExternalForm();
    }

    @Override
    public void deleteImage(String image) {
        String key = image.substring(image.lastIndexOf("/") + 1, image.length());

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(s3.getbucketName())
                .key(key)
                .build();
        s3.getAmazonS3Client().deleteObject(request);
    }

}
