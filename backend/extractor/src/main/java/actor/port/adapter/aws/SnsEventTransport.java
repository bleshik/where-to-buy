package actor.port.adapter.aws;

import actor.domain.model.*;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.AddPermissionRequest;
import com.amazonaws.services.lambda.model.AddPermissionResult;
import com.amazonaws.services.lambda.model.ResourceConflictException;
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
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static actor.port.adapter.aws.AwsUtil.REGION;
import static actor.port.adapter.aws.AwsUtil.ACCOUNT_ID;

public abstract class SnsEventTransport implements EventTransport, RequestHandler<SNSEvent, Object>  {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AmazonSNSClient snsClient =
        new AmazonSNSClient(new ClasspathPropertiesFileCredentialsProvider()) {{ setRegion(REGION); }};
    private final AWSLambdaClient lambdaClient =
        new AWSLambdaClient(new ClasspathPropertiesFileCredentialsProvider()) {{ setRegion(REGION); }};
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
        String lambdaArn = "arn:aws:lambda:" + REGION.getName() + ":" + ACCOUNT_ID + ":function:" + topicName;
        logger.info("Checking subscribtion for lambda " + lambdaArn);
        snsClient.subscribe(topicArn, "lambda", lambdaArn);
        try {
            AddPermissionRequest addPermissionRequest = new AddPermissionRequest()
                .withFunctionName(topicName)
                .withAction("lambda:InvokeFunction")
                .withStatementId("allow_sns_to_call_lambda")
                .withPrincipal("sns.amazonaws.com")
                .withSourceArn(topicArn);
            AddPermissionResult addPermissionResult = lambdaClient.addPermission(addPermissionRequest);
            logger.info("Added permission " + addPermissionResult.toString());
        } catch (ResourceConflictException e) {
            logger.info("Seems like the permission was already added: " + e.getMessage());
        }
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
            logger.debug("Got sns event " + event);
            for (SNSEvent.SNSRecord record : event.getRecords()) {
                Event parsedEvent = Event.fromByteArray(
                    DatatypeConverter.parseHexBinary(record.getSNS().getMessage())
                );
                topicArn = record.getSNS().getTopicArn();
                logger.info("Topic arn " + topicArn);
                if (consumer != null) {
                    logger.debug("Handling the event " + parsedEvent);
                    consumer.accept(parsedEvent);
                    logger.debug("Handled the event " + parsedEvent);
                } else {
                    logger.warn("Noop");
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

