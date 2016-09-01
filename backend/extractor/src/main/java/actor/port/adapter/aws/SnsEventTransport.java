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
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.xml.bind.DatatypeConverter;

public class SnsEventTransport implements EventTransport, RequestHandler<SNSEvent, Object>  {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Region region = Region.getRegion(
        Optional.ofNullable(System.getenv("AWS_DEFAULT_REGION")).map(Regions::fromName).orElse(Regions.EU_CENTRAL_1)
    );
    private final AmazonSNSClient snsClient; 
    private String topicArn;
    private Consumer<Event> consumer;

    public SnsEventTransport(String topicName, String accountId) {
        this();
        logger.info("Using topic " + topicName);
        logger.info("Creating/getting the topicArn");
        topicArn = snsClient.createTopic(topicName).getTopicArn();
        String lambdaArn = "arn:aws:lambda:" + region.getName() + ":" + accountId + ":function:" + topicName;
        logger.info("Checking subscribtion for lambda " + lambdaArn);
        snsClient.subscribe(topicArn, "lambda", lambdaArn);
        logger.info("Initialized successfully");
    }

    private SnsEventTransport() {
        logger.info("Initializing snsClient");
        snsClient = new AmazonSNSClient(new ClasspathPropertiesFileCredentialsProvider()) {{ setRegion(region); }};
    }

    @Override
    public void send(Class<? extends Actor> senderClass, Class<? extends Actor> actorClass, Object payload) {
        snsClient.publish(new PublishRequest(
            topicArn,
            DatatypeConverter.printHexBinary(new Event(senderClass, actorClass, payload).toByteArray())
        ));
    }

    /*
    // TODO: make more efficient? see http://stackoverflow.com/questions/36721014/aws-sns-how-to-get-topic-arn-by-topic-name
    private Optional<Topic> getTopic(String name) {
        Optional<ListTopicsRequest> request = Optional.of(new ListTopicsRequest());
        Optional<Topic> topic = Optional.empty();
        while (request.isPresent() && !topic.isPresent()) {
            ListTopicsResult listTopicsResult = snsClient.listTopics(request.get());
            String nextToken = listTopicsResult.getNextToken();
            topic = listTopicsResult.getTopics().stream().filter((t) -> t.getTopicArn().endsWith(name)).findAny();
            request = Optional.ofNullable(listTopicsResult.getNextToken())
                .map((token) -> new ListTopicsResult().withNextToken(token));
        }
        return topic;
    }

    private Optional<Subscription> getSubscription() {
        Optional<ListSubscriptionsByTopicRequest> request = Optional.of(new ListSubscriptionsByTopicRequest(topicArn));
        Optional<Subscription> subscription = Optional.empty();
        while (request.isPresent() && !subscription.isPresent()) {
            ListSubscriptionsByTopicResult result = snsClient.listSubscriptionsByTopic(request.get());
            String nextToken = result.getNextToken();
            subscription = result.getSubscriptions()
                .stream()
                .filter((t) -> { System.out.println(t); return t.getEndpoint().contains(this.getClass().getSimpleName());})
                .findAny();
            request = Optional.ofNullable(result.getNextToken())
                .map((token) -> new ListSubscriptionsByTopicRequest(topicArn).withNextToken(token));
        }
        return subscription;
    }
    */

    @Override
    public Object handleRequest(SNSEvent event, Context context) {
        for (SNSEvent.SNSRecord record : event.getRecords()) {
            Event parsedEvent = Event.fromByteArray(
                DatatypeConverter.parseHexBinary(record.getSNS().getMessage())
            );
            topicArn = record.getSNS().getTopicArn();
            if (consumer != null) {
                consumer.accept(parsedEvent);
            }
        }
        return null;
    }

    @Override
    public void listen(Consumer<Event> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void close() {}

}

