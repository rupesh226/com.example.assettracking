# Asset Tracking API

## Overview

This project is a solution to the Asset Tracking API exercise. It is built using Java 17, Spring Boot, and DynamoDB, with a caching layer implemented using Redis. The API allows querying and retrieving GPS coordinate data for simulated vehicles over a given time range.

This program includes `src/test/java/com/example/assettracking/DataToDynamoDB.java` class for data integration. We are using `data.json` file to insert the data, and we are also modifying the `createdAt` field by converting the ISO string to a Unix timestamp in milliseconds. The long fields provide better performance for sorting and searching operations.

We are also using caching to improve performance.

## Features

- Retrieve all events for a given asset and time range
- Query a single event by its ID
- Return the latest event for all assets
- Pagination support for large datasets
- Caching with Redis to improve performance
- Effective indexing in DynamoDB for optimized queries

## Technologies Used

- Java 17
- Spring Boot
- DynamoDB (local)
- Redis
- Lombok
- Maven

## Setup Instructions

### Setup Video (With Docker Compose)

[![Watch the video](https://github.com/rupesh226/assettracking-app/blob/main/img/gHrep.png)](https://www.youtube.com/watch?v=s_TeDU3cSKw)

https://github.com/rupesh226/assettracking-app

### Prerequisites

- Java 17
- Maven
- Docker (for running DynamoDB and Redis locally)

### Running DynamoDB Locally

1. Run DynamoDB local with default dataset:
   ```sh
   cd dynamodb_local_latest
   sh start.sh
   ```

### Running Redis Locally

1. Pull the Redis image:
   ```sh
   docker pull redis
   ```
2. Run Redis:
   ```sh
   docker run -d -p 6379:6379 redis
   ```

### Project Setup

1. Clone the repository:

   ```sh
   git clone https://github.com/rupesh226/com.example.assettracking/
   cd com.example.assettracking
   ```

2. Install dependencies and build the project:

   ```sh
   mvn clean install
   ```

   ![Swagger-UI](https://github.com/rupesh226/com.example.assettracking/blob/main/img/MVN-build.png)

3. Run the application:

   ```sh
   mvn spring-boot:run
   ```

4. Access Apps from - http://localhost:8080/swagger-ui/index.html

![Swagger-UI](https://github.com/rupesh226/com.example.assettracking/blob/main/img/Home-Page.png)

## API Endpoints

### Get Events by Asset and Time Range

- **URL**: `/api/events`
- **Method**: `GET`
- **Parameters**:

  - `asset` (String) - Asset identifier
  - `start` (long) - Start timestamp (Unix milliseconds)
  - `end` (long) - End timestamp (Unix milliseconds)
  - `page` (int) - Page number (required)
  - `size` (int) - Page size (required)

- **Response**: JSON array of events

### Get Event by ID

- **URL**: `/api/events/{id}`
- **Method**: `GET`
- **Parameters**:
  - `id` (String) - Event ID
- **Response**: JSON object of the event

### Get Latest Events

- **URL**: `/api/events/latest`
- **Method**: `GET`
- **Parameters**:
  - `page` (int) - Page number (required)
  - `size` (int) - Page size (required)
- **Response**: JSON array of latest events

## Technical Details

### Caching

The API uses Redis for caching frequently accessed data. The following methods are cached:

- `getEventsByAssetAndTimeRange`
- `getEventById`
- `getLatestEvents`

Caching helps to reduce the load on DynamoDB and improves the response times for frequently accessed data.

### Technical Depth

### Technical Depth

#### Pagination Support

Pagination is implemented at the database level to efficiently handle large datasets. By using effective partition keys and indexing, the system can improve the performance of paginated queries.

#### Indexing in DynamoDB

Effective indexing is crucial for optimizing query performance, especially for large datasets. A Global Secondary Index (GSI) is created in DynamoDB on the `asset` and `createdAt` attributes to enable efficient querying of events based on these fields. While the current implementation provides basic indexing, further optimization and review of the indexing strategy can improve performance.

#### Improved Architecture Design

Currently, the API retrieves the latest asset event directly from the database. While this approach works, it can lead to high I/O operations, especially under heavy load, affecting performance and scalability.

To address this, an improved architecture design can be implemented using a Pub/Sub model or a queuing mechanism to keep the latest data in the cache. Here’s how it works:

1. **Event Listener**: A service that updates events can include an event listener. This listener subscribes to updates and changes related to asset events.
2. **Cache Updater**: The listener updates the cache with the latest events. This can be done using a time-based sliding window, where the last 5 minutes to 1 day of data is kept in the cache.
3. **Cache Retrieval**: The API retrieves the latest events directly from the cache, reducing the number of database I/O operations significantly.

By caching the latest events, the system can serve these frequently accessed data points much faster. This not only improves response times but also offloads the read burden from DynamoDB, allowing it to handle other critical operations more efficiently.

Implementing this architecture requires careful consideration of cache eviction policies, data consistency, and the design of the event listener to ensure it scales with the application's load. However, the benefits in terms of performance improvements and reduced database load are substantial.

## Testing

### Unit Tests

Unit tests are written using JUnit and Mockito. To run the tests:

```sh
mvn test
```

![Swagger-UI](https://github.com/rupesh226/com.example.assettracking/blob/main/img/Test-Results.png)

## Future Enhancements

- Add support for querying events by trip ID
- Implement additional caching strategies
- Enhance error handling and validation

## License

This project is licensed under the MIT License. See the LICENSE file for details.
