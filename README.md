# Readme

[中文版](./README_ch.md)

## What is it ?

This is a bank fraud transaction detection system.

- High concurrency, high performance
- Provides auto-scalability in a cloud environment, enhancing high availability
- Performs fraud detection based on predefined rules
  If you plan to use it in a production environment, it is advisable to refer to these [recommendations for production use](#Recommendations for production).


## Architecture design

![Architecture](doc/images/architecture_diagram.png "Architecture")

Key Points

- **Message Queue**: Common message queues such as Kafka or RocketMQ can be used, or you can opt for cloud provider MQ products, like AWS SQS.
- **Data Streams Framework**: Flink is a suitable choice.
- **Drools**: This component is used for predefined fraud rules, supporting hot deployment. Business personnel can establish fraud rules using a unique rule language without needing to understand technical code.
- **Production Environment**: In a production setting, there will be a frontend composed of producer-consumer agents and MQ. Ultimately, the combination of Springboot + Mybatis will persist data to the database.

If you are interested in learning more about the stories from the research phase, [please refer to this section](#Research Stories).


## How to use it

### Preparation

- JDK 17
- Maven, better to setup mirrors
- Docker + docker-compose， better to setup mirrors
- K8S

### Download source code

```declarative
git clone https://github.com/jpixy/frauddetector.git
cd frauddetector/
```

### Run Unit Tests

```declarative
make test
```

![Unit tests results](doc/images/make_test.png "UT")

![UT in IDE](doc/images/ide_junit_test.png "UT")

#### Fraud Rules

```declarative

Abnormal Transaction Location:
Check if the transaction location is inconsistent with the account registration location.
If it is inconsistent, mark it as a fraudulent transaction and log it.

Nighttime Transactions:
Check if the transaction time is during the night (22:00 to 06:00).
If it is a nighttime transaction, mark it as a fraudulent transaction and log it.

High-Value Transactions:
Check if the transaction amount exceeds 10,000 yuan.
If it exceeds, mark it as a fraudulent transaction and log it.


```

From the test screenshots, the following unit tests are provided and have passed:

- Transactions below the threshold
- Normal transactions
- Transactions with multiple rule overlaps
- Transactions equal to the threshold
- Transactions with abnormal transaction locations
- Transactions with unchanged transaction addresses
- Transactions above the threshold
- Nighttime transactions


### Building

```declarative
make build
```

### Package it to docker image

```declarative
make docker-build
```

### Run whole system

```declarative
make run

# stop it

make stop
```

### Deploy it in K8S env

```declarative
make k8s-deploy
make k8s-list
make k8s-delete
```

#### How to auto-scalling in K8S

```declarative
# Setup HPA in helm

autoscaling:
enabled: true
minReplicas: 3
maxReplicas: 10
targetCPUUtilizationPercentage: 80


# you can run this tool for testing this function:

hey -z 1m -c 10 -q 10 http://<kube-IP>:8080
```

Please refer to Makefile to know more details

### Commands for testing and demo

#### Create high value transaction records

```shell
curl -X POST http://localhost:8080/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumberFrom": "1234567890",
    "accountNumberTo": "0987654321",
    "amount": 15000.00,
    "transactionType": "DEBIT",
    "isFraud": false,
    "status": "PENDING"
  }'

# Logs

2025-01-15T15:31:43.970Z  INFO 1 --- [frauddetector] [nio-8080-exec-1] c.j.frauddetector.config.DroolsConfig    : [FRAUD DETECTED] High amount transaction detected for account: 1234567890


```

#### Transaction location anomaly

```declarative
curl -X POST http://localhost:8080/v1/transactions \                                          
-H "Content-Type: application/json" \
-d '{
"accountNumberFrom": "1234567890", "accountNumberTo": "0987654321", "accountLocation": "Beijing", "transactionLocation": "Shanghai", "amount": 1000.00,
"transactionType": "DEBIT", "isFraud": false, "status": "PENDING"
}'

# Logs

2025-01-15T15:33:25.090Z  INFO 1 --- [frauddetector] [nio-8080-exec-3] c.j.frauddetector.config.DroolsConfig    : [FRAUD DETECTED] Suspicious transaction location detected for account: 1234567890

```

Other CRUD (Create, Read, Update, Delete) Restful APIs are also available. For specifics, you can refer to the code for testing.


## Recommendations for production

- Add a complete message queue and stream processing framework as a frontend, then call the API for detecting transactions in this example to make fraud judgments.
- To use a message queue, you need to prepare producer and consumer agents.
- The consumer of the message queue directly sends messages to the stream processing end, such as Flink.
- The ID in the database table structure is simplified to an auto-incremented database ID; in a production environment, consider using a distributed ID generation system.
- The database may need to consider sharding tables and databases.
- Even consider adding a caching layer between the business layer and the database.
- The transaction table is simplified to only consider the account; in reality, there is a relationship between user and account, with a common scenario where a user has multiple accounts.


## Research Stories

Before development, I conducted preliminary research on cloud products from various cloud service providers.

The objectives were as follows:
- AWS EKS, GCP GKE, Alibaba ACK
- AWS SQS, GCP Pub/Sub, Alibaba Message Service
- AWS CloudWatch, GCP Stackdriver, Alibaba Cloud Log Service

The initial focus was on evaluating AWS EKS, GCP GKE, and Alibaba ACK, as these are the foundational deployment platforms. Only after selecting one of these could we consider additional complementary products.
- Alibaba ACK: After practical testing, the trial version has been discontinued, and currently, only the Pro version is available for use.
- GCP GKE: The overall suite of products offered by the Google Cloud platform is comparatively less comprehensive when compared to other providers.
- AWS EKS: The final choice.

Below are the operational commands used during the demo run,

### Create AWS EKS via CLI

First, install eksctl， aws cli， helm etc, and we use us-east-1 region
```declarative
eksctl create cluster -f cluster-config.yaml
export CLUSTER_REGION=us-east-1
export CLUSTER_VPC=$(aws eks describe-cluster --name web-quickstart --region $CLUSTER_REGION --query "cluster.resourcesVpcConfig.vpcId" --output text)
helm repo add eks https://aws.github.io/eks-charts
helm repo update eks


helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
--namespace kube-system \
--set clusterName=web-quickstart \
--set serviceAccount.create=false \
--set region=${CLUSTER_REGION} \
--set vpcId=${CLUSTER_VPC} \
--set serviceAccount.name=aws-load-balancer-controller

## Take a note, you need to use VPN , or just like me, use oversee VM for it

kubectl create namespace johnny-dev --save-config
kubectl apply -n johnny-dev -f app.yaml
kubectl get ingress -n johnny-dev

```

### AWS SQS

SQS creation is simple, we just show the Java integration code

```pom.xml
        <dependencies>
            <dependency>
                <groupId>software.amazon.awssdk</groupId>
                <artifactId>bom</artifactId>
                <version>${aws.java.sdk.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>

```

Java code

```java
package com.johnnydemo.aws;

import com.johnnydemo.utils.RandomStringGenerator;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

public class SQSWrapper {
    public static void main(String[] args) {
        String queueName = "J_SQS_01";
        String message = RandomStringGenerator.generateRandomString(20);

        // 显式指定临时凭证
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create("<your ID>", "<your key>");

        SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
        sendMessage(sqsClient, queueName, message);
        receiveMessage(sqsClient, queueName);
        sqsClient.close();
    }

    public static String getQueueURL(SqsClient sqsClient, String queueName) {
        CreateQueueRequest request = CreateQueueRequest.builder()
                .queueName(queueName)
                .build();
        sqsClient.createQueue(request);

        GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build();

        return sqsClient.getQueueUrl(getQueueRequest).queueUrl();
    }


    public static void sendMessage(SqsClient sqsClient, String queueName, String message) {
        try {

            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(getQueueURL(sqsClient, queueName))
                    .messageBody(message)
                    .delaySeconds(5)
                    .build();

            sqsClient.sendMessage(sendMsgRequest);

        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public static void receiveMessage(SqsClient sqsClient, String queueName) {
        try {
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(getQueueURL(sqsClient, queueName))
                    .maxNumberOfMessages(5)
                    .build();

            List<Message> msgList = sqsClient.receiveMessage(receiveMessageRequest).messages();

            System.out.println("=== Get " + msgList.size() + " messages from SQS ===");
            for(Message msg : msgList){
                System.out.println("=== The message is : "+ msg + " ===");
            }
            deleteMessage(sqsClient, queueName, msgList);
            System.out.println("=== Delete " + msgList.size() + " messages from SQS ===");

        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public static void deleteMessage(SqsClient sqsClient, String queueName, List<Message> msgList) {
        try {
            for (Message message : msgList) {
                DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                        .queueUrl(getQueueURL(sqsClient, queueName))
                        .receiptHandle(message.receiptHandle())
                        .build();
                sqsClient.deleteMessage(deleteMessageRequest);
            }
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
}


```
### The Fee if we use AWS

Under the aforementioned configuration, using AWS EKS and SQS would incur approximate costs of $5.5 to $7+ per day, with the exact expenses being influenced by various factors such as the amount of traffic utilized.
