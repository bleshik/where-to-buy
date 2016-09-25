package actor.port.adapter.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AwsUtil {
    private AwsUtil() {}

    public static final Region REGION = Region.getRegion(
        Optional.ofNullable(System.getenv("AWS_REGION")).map(Regions::fromName).orElse(
            Optional.ofNullable(System.getenv("AWS_DEFAULT_REGION")).map(Regions::fromName).orElse(Regions.EU_CENTRAL_1)
        )
    );

    public static final String ACCOUNT_ID =
        new AmazonIdentityManagementClient(new ClasspathPropertiesFileCredentialsProvider()) {{ setRegion(REGION); }}
            .getUser().getUser().getArn().split(":")[4];

}
