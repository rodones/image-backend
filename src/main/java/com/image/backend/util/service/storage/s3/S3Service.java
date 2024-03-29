package com.image.backend.util.service.storage.s3;

import com.image.backend.config.AwsConfig;
import com.image.backend.util.service.storage.IStorageService;
import com.image.backend.exception.MimeTypeDetectException;
import lombok.SneakyThrows;
import org.apache.commons.compress.utils.FileNameUtils;
import org.codehaus.plexus.util.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.annotation.PreDestroy;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class S3Service implements IStorageService {
    private final AwsConfig awsConfig;
    private final S3Client s3Client;
    private final String baseUrl;
    private List<String> prefixes = new ArrayList<>();

    public S3Service(AwsConfig awsConfig) {
        this.awsConfig = awsConfig;
        baseUrl = "https://" + awsConfig.getBucketName() + "." + awsConfig.getRegion() + "." + awsConfig.getBaseUrl() + "/";
        s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsConfig.getCredentials()))
                .endpointOverride(awsConfig.getEndpoint())
                .region(Region.of(awsConfig.getRegion()))
                .build();
        setPrefixes();
    }

    private void setPrefixes() {
        prefixes = s3Client.listObjects(ListObjectsRequest.builder()
                        .delimiter("/").bucket(awsConfig.getBucketName()).build()).
                commonPrefixes().stream().map(CommonPrefix::prefix).
                collect(java.util.stream.Collectors.toList());
    }

    private URL getUrl(String key) {
        try {
            return new URL(baseUrl + key);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public URL getS3Url(String name) {
        if (name.split("/").length > 1) {
            return getUrl(name);
        }

        String val = "";

        for (String prefix : prefixes) {
            try {
                s3Client.headObject(HeadObjectRequest.builder()
                        .bucket(awsConfig.getBucketName()).key(prefix + name).build());
                val = prefix + name;
                break;
            } catch (NoSuchKeyException ignored) {

            }

        }
        return getUrl(val);

    }

    @Override
    public URL put(File file) {
        return put(file, generatePath(file));
    }

    @Override
    public URL put(File file, Path path) {
        var contentType = getContentType(file);

        return put(RequestBody.fromFile(file), generatePath(file), contentType);
    }

    @Override
    @SneakyThrows
    public URL put(InputStream inputStream, Path path) {
        var bufferedInputStream = new BufferedInputStream(inputStream);
        var contentType = getContentType(bufferedInputStream);

        return put(RequestBody.fromInputStream(bufferedInputStream, bufferedInputStream.available()), path, contentType);
    }

    @Override
    @SneakyThrows
    public URL put(InputStream inputStream) {
        var bufferedInputStream = new BufferedInputStream(inputStream);
        var contentType = getContentType(bufferedInputStream);
        var extension = contentType.split("/")[1];

        return put(
                RequestBody.fromInputStream(bufferedInputStream, bufferedInputStream.available()),
                generatePathWithExtension(extension),
                contentType
        );
    }

    @Override
    @SneakyThrows
    public URL put(MultipartFile file) {
        var contentType = file.getContentType();
        String extension;
        if (contentType != null && contentType.split("/")[1].length() <= 5) {
            extension = contentType.split("/")[1];
        } else {
            extension = FileNameUtils.getExtension(file.getOriginalFilename());
        }
        var bufferedInputStream = new BufferedInputStream(file.getInputStream());

        return put(
                RequestBody.fromInputStream(bufferedInputStream, bufferedInputStream.available()),
                generatePathWithExtension(extension),
                contentType
        );
    }

    private URL put(RequestBody requestBody, Path path, String contentType) {
        if (!path.equals(path.normalize())) {
            throw new IllegalArgumentException("The path must be normalized.");
        }

        var remotePath = getRemotePath(path);
        var req = PutObjectRequest.builder()
                .bucket(awsConfig.getBucketName())
                .acl(ObjectCannedACL.PUBLIC_READ)
                .key(remotePath.toString())
                .contentType(contentType)
                .build();
        s3Client.putObject(req, requestBody);

        return getRemoteUrl(path);
    }

    @Override
    public URL getRemoteUrl(Path path) {
        if (!path.equals(path.normalize())) {
            throw new IllegalArgumentException("");
        }

        var remotePath = getRemotePath(path);

        GetUrlRequest req = GetUrlRequest.builder()
                .bucket(awsConfig.getBucketName())
                .key(remotePath.toString())
                .build();

        return s3Client.utilities().getUrl(req);
    }

    @Override
    public void delete(Path path) {
        var remotePath = getRemotePath(path);

        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(awsConfig.getBucketName())
                .key(remotePath.toString())
                .build();

        s3Client.deleteObject(req);
    }

    @Override
    public void delete(URL url) {
        var urlPath = url.getPath();

        if (urlPath.startsWith("/")) {
            urlPath = urlPath.substring(1);
        }

        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(awsConfig.getBucketName())
                .key(urlPath)
                .build();

        s3Client.deleteObject(req);
    }

    private String getContentType(BufferedInputStream bufferedInputStream) {
        try {
            var mimeType = URLConnection.guessContentTypeFromStream(bufferedInputStream);
            bufferedInputStream.reset();
            return mimeType;
        } catch (IOException e) {
            throw new MimeTypeDetectException();
        }
    }

    private String getContentType(File file) {
        try {
            return Files.probeContentType(file.toPath());
        } catch (IOException e) {
            throw new MimeTypeDetectException();
        }
    }

    @Override
    public InputStream get(Path path) {
        var remotePath = getRemotePath(path);

        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(awsConfig.getBucketName())
                .key(remotePath.toString())
                .build();

        return s3Client.getObject(req);
    }

    private Path getRemotePath(Path path) {
        return Path.of(awsConfig.getBaseUrl()).resolve(path.toString());
    }

    private Path generatePath(File file) {
        var basename = UUID.randomUUID().toString();
        var extension = FileUtils.getExtension(file.toString());

        return Path.of(basename + "." + extension);
    }

    private Path generatePathWithExtension(String extension) {
        var basename = UUID.randomUUID().toString();

        return Path.of(basename + "." + extension);
    }

    @PreDestroy
    public void destroy() {
        s3Client.close();
    }

}
