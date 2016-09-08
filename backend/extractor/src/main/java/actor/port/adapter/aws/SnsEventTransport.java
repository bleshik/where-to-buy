package actor.port.adapter.aws;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import actor.domain.model.*;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicRequest;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicResult;
import com.amazonaws.services.sns.model.ListTopicsRequest;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.Topic;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.xml.bind.DatatypeConverter;

public abstract class SnsEventTransport implements EventTransport, RequestHandler<SNSEvent, Object>  {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Region region = Region.getRegion(
        Optional.ofNullable(System.getenv("AWS_DEFAULT_REGION")).map(Regions::fromName).orElse(Regions.EU_CENTRAL_1)
    );
    private final AmazonIdentityManagementClient iamClient =
        new AmazonIdentityManagementClient(new ClasspathPropertiesFileCredentialsProvider()) {{ setRegion(region); }};
    private final String accountId = iamClient.getUser().getUser().getArn().split(":")[4];
    private final AmazonSNSClient snsClient =
        new AmazonSNSClient(new ClasspathPropertiesFileCredentialsProvider()) {{ setRegion(region); }};
    private String topicArn;
    protected Dispatcher dispatcher;
    private Consumer<EventTransport.Event> consumer;

    @Override
    public void send(Class<? extends Actor> senderClass, Class<? extends Actor> actorClass, Object payload) {
        snsClient.publish(new PublishRequest(
            topicArn,
            DatatypeConverter.printHexBinary(new Event(senderClass, actorClass, payload).toByteArray())
        ));
    }

    protected abstract Dispatcher initializeDispatcher(Context context);

    protected abstract void onEmptyMessage(Context context);

    protected void initializeTopic(String topicName) {
        logger.info("Using topic " + topicName);
        logger.info("Creating/getting the topicArn");
        topicArn = snsClient.createTopic(topicName).getTopicArn();
        String lambdaArn = "arn:aws:lambda:" + region.getName() + ":" + accountId + ":function:" + topicName;
        logger.info("Checking subscribtion for lambda " + lambdaArn);
        snsClient.subscribe(topicArn, "lambda", lambdaArn);
        logger.info("Initialized successfully");
    }

    @Override
    public Object handleRequest(SNSEvent event, Context context) {
        if (dispatcher == null) {
            dispatcher = initializeDispatcher(context);
            listen((e) -> dispatcher.handle(e.senderClass, e.actorClass, e.payload));
        }
        if (event == null || event.getRecords() == null) {
            onEmptyMessage(context);
        } else {
            logger.info("Got sns event " + event);
            for (SNSEvent.SNSRecord record : event.getRecords()) {
                Event parsedEvent = Event.fromByteArray(
                    DatatypeConverter.parseHexBinary(record.getSNS().getMessage())
                );
                topicArn = record.getSNS().getTopicArn();
                logger.info("Topic arn " + topicArn);
                if (consumer != null) {
                    logger.info("Handling the event " + parsedEvent);
                    consumer.accept(parsedEvent);
                    logger.info("Handled the event " + parsedEvent);
                } else {
                    logger.info("Noop");
                }
            }
        }
        return null;
    }

    @Override
    public void listen(Consumer<EventTransport.Event> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void close() {}

}

