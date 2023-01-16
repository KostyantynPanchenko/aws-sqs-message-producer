package org.example;

import java.util.UUID;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * Hello world!
 */
public class App {

  public static void main(final String[] args) {
    if (args.length != 2) {
      System.out.println("Invalid program arguments!");
      System.out.println("Usage: <queue_name> <message>");
      System.exit(1);
    }

    final var queue = args[0];
    final var message = args[1];

    final var sqsClient = getSqsClient();
    sendMessage(sqsClient, queue, message);
    sqsClient.close();
  }

  private static SqsClient getSqsClient() {
    return SqsClient.builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(ProfileCredentialsProvider.create("default"))
//        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .build();
  }

  private static void sendMessage(final SqsClient client, final String queueName, final String message) {
    try {
      final var getQueueUrlRequest = GetQueueUrlRequest.builder().queueName(queueName).build();

      final var queueUrlString = client.getQueueUrl(getQueueUrlRequest).queueUrl();

      String uuid = UUID.randomUUID().toString();
      String composedMessage = "UUID: " + uuid + ". Body: " + message;
      final var messageRequest = SendMessageRequest.builder()
          .queueUrl(queueUrlString)
          .messageBody(composedMessage)
          .messageDeduplicationId(uuid)
          .messageGroupId("aws-sqs-message-provider-group")
          .build();

      client.sendMessage(messageRequest);
      System.out.println("Message '" + composedMessage + "' sent!");
    } catch (final SdkClientException exception) {
      System.err.println("Failed to send the message '" + message + "'");
      System.err.println(exception.getMessage());
    }
  }
}
