package es.codeurjc.mca.events.image;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsS3Config {

    @Value("${amazon.aws.bucket-name}")
    private String bucketName;

    @Bean
    public S3Client getAmazonS3Client() {
        return  S3Client.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    public String getbucketName() {
        return bucketName;
    }
}
