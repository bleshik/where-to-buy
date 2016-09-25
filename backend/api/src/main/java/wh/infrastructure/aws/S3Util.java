package wh.infrastructure.aws;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class S3Util {
    private static final Logger logger = LoggerFactory.getLogger(S3Util.class);
    private S3Util() {}

    private static AmazonS3Client s3Client = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
    private static boolean mocked = "true".equals(System.getenv("MOCK_S3"));
    private static Path mockedS3Path = Paths.get(System.getProperty("java.io.tmpdir"), "fake", "s3");
    static {
        if (mocked) {
            logger.warn("Using mocked s3 at " + mockedS3Path);
        }
    }

    private static Path getMockedResourcePath(String bucket, String key) {
        return mockedS3Path.resolve(bucket).resolve(
            Arrays.asList(key.split("/")).stream().collect(Collectors.joining(File.separator))
        );
    }

    public static URL getResourceUrl(String bucket, String key) {
        try {
            return mocked ? getMockedResourcePath(bucket, key).toUri().toURL() : s3Client.getUrl(bucket, key);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static URL uploadPublicFile(String bucket, String key, byte[] in) {
        return uploadFile(bucket, key, in, true);
    }

    public static URL uploadFile(String bucket, String key, byte[] in, boolean readableForAll) {
        return uploadFile(bucket, key, new ByteArrayInputStream(in), readableForAll);
    }

    public static URL uploadPublicFile(String bucket, String key, InputStream in) {
        return uploadFile(bucket, key, in, true);
    }

    public static URL uploadFile(String bucket, String key, InputStream in, boolean readableForAll) {
        if (mocked) {
            Path path = getMockedResourcePath(bucket, key);
            try {
                path.getParent().toFile().mkdirs();
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to upload file to the mocked S3", e);
            }
        } else {
            PutObjectRequest request = new PutObjectRequest(bucket, key, in, new ObjectMetadata());
            if (readableForAll) {
                request.withCannedAcl(CannedAccessControlList.PublicRead);
            }
            s3Client.putObject(request);
        }
        return getResourceUrl(bucket, key);
    }
    
}
