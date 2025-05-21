package com.Server.service.config;

import com.Server.exception.OurException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.UUID;

@Configuration
public class AwsS3Config {
    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.access.key}")
    private String awsS3AccessKey;

    @Value("${aws.s3.secret.key}")
    private String awsS3SecreteKey;

    private String bucketUrl;

    private AmazonS3 s3Client;

    public AwsS3Config() {
    }

    @PostConstruct
    public void initializeS3Client() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsS3AccessKey, awsS3SecreteKey);

        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.AP_SOUTHEAST_1)
                .build();

        bucketUrl = "https://" + bucketName + ".s3.amazonaws.com/";
    }

    @Bean
    public AmazonS3 amazonS3() {
        if (s3Client == null) {
            initializeS3Client();
        }
        return s3Client;
    }

    public String saveFileToS3(MultipartFile file) {
        try {
            String s3FileName = UUID.randomUUID().toString() + "-"
                    + file.getOriginalFilename().replaceAll("\\s+", "_");

            InputStream inputStream = file.getInputStream();
            ObjectMetadata metadata = new ObjectMetadata();

            String contentType = null;
            if (s3FileName != null) {
                if (s3FileName.endsWith(".png") || s3FileName.endsWith(".jpg") || s3FileName.endsWith(".jpeg")) {
                    contentType = "image/" + s3FileName.substring(s3FileName.lastIndexOf(".") + 1);
                } else if (s3FileName.endsWith(".mp4") || s3FileName.endsWith(".avi") || s3FileName.endsWith(".mov")) {
                    contentType = "video/" + s3FileName.substring(s3FileName.lastIndexOf(".") + 1);
                } else if (s3FileName.endsWith(".mp3") || s3FileName.endsWith(".wav") || s3FileName.endsWith(".aac")) {
                    contentType = "audio/" + s3FileName.substring(s3FileName.lastIndexOf(".") + 1);
                }
            }
            if (contentType == null) {
                throw new OurException("Only accept files with format JPG, JPEG, PNG, MP4, AVI, MOV, MP3, WAV, AAC");
            }
            metadata.setContentType(contentType);

            metadata.setContentLength(file.getSize());

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, s3FileName, inputStream, metadata);
            s3Client.putObject(putObjectRequest);

            return bucketUrl + s3FileName;
        } catch (Exception e) {
            e.printStackTrace();
            throw new OurException(e.getMessage());
        }
    }

    public void deleteFileFromS3(String fileUrl) {
        try {
            String fileName = fileUrl.replace(bucketUrl, "");

            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, fileName);
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
            throw new OurException("Error occurred while deleting the image: " + e.getMessage());
        }
    }
}
