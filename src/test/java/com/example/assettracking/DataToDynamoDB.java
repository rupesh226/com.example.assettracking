package com.example.assettracking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.LocalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

public class DataToDynamoDB {

	Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	
	private static final String TABLE_NAME = "AssetEvents";
	private static final String FILE_PATH = "/Users/rupesh/Documents/workspace-spring-tool-suite/com.example.assettracking.data.ingestion/src/main/resources/data.json";
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

	private static final String partitionKeyName = "id";
	private static final String sortKeyName = "createdAt";

	// Secondary index attributes
	String gsiAssetPartitionKey = "asset";
    String gsiTripSortKey = "trip";
    //String gsiTripSortKey = "trip";

	DynamoDbClient ddb;

	public DataToDynamoDB() {
		root.setLevel(Level.ERROR);
		
		ddb = DynamoDbClient.builder().endpointOverride(URI.create("http://localhost:8000")).region(Region.US_EAST_1)
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
						"wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
						"IQoJb3JpZ2luX2IQoJb3JpZ2luX2IQoJb3JpZ2luX2IQoJb3JpZ2luX2IQoJb3JpZVERYLONGSTRINGEXAMPLE")))
				.build();
	}

	public static void main(String[] args) {

		DataToDynamoDB dataToDynamoDB = new DataToDynamoDB();

		try {
			dataToDynamoDB.deleteTable();
		} catch (DynamoDbException e) {
			System.err.println("Unable to delete table: " + e.getMessage());
		}

		try {
			dataToDynamoDB.createTable();
		} catch (DynamoDbException e) {
			System.err.println("Unable to create table: " + e.getMessage());
		}

		try {
			dataToDynamoDB.insertdata();
		} catch (DynamoDbException e) {
			System.err.println("Unable to insert data into table: " + e.getMessage());
		}
	}

	void insertdata() {

		try (BufferedReader reader = new BufferedReader(new FileReader(new File(FILE_PATH)))) {
			String line;
			while ((line = reader.readLine()) != null) {
				@SuppressWarnings("unchecked")
				Map<String, Object> item = objectMapper.readValue(line, Map.class);
				Map<String, AttributeValue> itemValues = new HashMap<>();

				itemValues.put("id", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
				itemValues.put("asset", AttributeValue.builder().n(String.valueOf(item.get("asset"))).build());
				itemValues.put("trip", AttributeValue.builder().n(String.valueOf(item.get("trip"))).build());
				itemValues.put("x", AttributeValue.builder().n(String.valueOf(item.get("x"))).build());
				itemValues.put("y", AttributeValue.builder().n(String.valueOf(item.get("y"))).build());
				itemValues.put("speed", AttributeValue.builder().n(String.valueOf(item.get("speed"))).build());
				itemValues.put("createdAtISO",
						AttributeValue.builder().s(String.valueOf(item.get("createdAt"))).build());
				itemValues.put("createdAt", AttributeValue.builder()
						.n(toUnixTimeMillis(String.valueOf(item.get("createdAt"))) + "").build());

				PutItemRequest request = PutItemRequest.builder().tableName(TABLE_NAME).item(itemValues).build();
				ddb.putItem(request);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			ddb.close();
		}
	}

	void deleteTable() {

		DeleteTableRequest request = DeleteTableRequest.builder().tableName(TABLE_NAME).build();

		ddb.deleteTable(request);
		System.out.println("Table " + TABLE_NAME + " deleted successfully.");

	}

	void createTable() {

		CreateTableRequest request = CreateTableRequest.builder()
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName(partitionKeyName)
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName(sortKeyName)
                                .attributeType(ScalarAttributeType.N)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName(gsiAssetPartitionKey)
                                .attributeType(ScalarAttributeType.N)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName(gsiTripSortKey)
                                .attributeType(ScalarAttributeType.N)
                                .build())
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName(partitionKeyName)
                                .keyType(KeyType.HASH)
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName(sortKeyName)
                                .keyType(KeyType.RANGE)
                                .build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(10L)
                        .writeCapacityUnits(10L)
                        .build())
                .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                                .indexName("AssetTripIndex")
                                .keySchema(
                                        KeySchemaElement.builder()
                                                .attributeName(gsiAssetPartitionKey)
                                                .keyType(KeyType.HASH)
                                                .build(),
                                        KeySchemaElement.builder()
                                                .attributeName(gsiTripSortKey)
                                                .keyType(KeyType.RANGE)
                                                .build())
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.ALL)
                                        .build())
                                .provisionedThroughput(ProvisionedThroughput.builder()
                                        .readCapacityUnits(10L)
                                        .writeCapacityUnits(10L)
                                        .build())
                                .build(),
                        GlobalSecondaryIndex.builder()
                                .indexName("AssetIndex")
                                .keySchema(
                                        KeySchemaElement.builder()
                                                .attributeName(partitionKeyName)
                                                .keyType(KeyType.HASH)
                                                .build())
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.ALL)
                                        .build())
                                .provisionedThroughput(ProvisionedThroughput.builder()
                                        .readCapacityUnits(10L)
                                        .writeCapacityUnits(10L)
                                        .build())
                                .build())
                .localSecondaryIndexes(
                        LocalSecondaryIndex.builder()
                                .indexName("CreatedAtTripIndex")
                                .keySchema(
                                        KeySchemaElement.builder()
                                                .attributeName(partitionKeyName)
                                                .keyType(KeyType.HASH)
                                                .build(),
                                        KeySchemaElement.builder()
                                                .attributeName(gsiTripSortKey)
                                                .keyType(KeyType.RANGE)
                                                .build())
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.ALL)
                                        .build())
                                .build())
                .tableName(TABLE_NAME)
                .build();
		ddb.createTable(request);
		System.out.println("Table created successfully.");

	}

	private static long toUnixTimeMillis(String dateTimeString) {
		ZonedDateTime zdt = ZonedDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
		return zdt.toInstant().toEpochMilli();
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		ddb.close();
	}
}
