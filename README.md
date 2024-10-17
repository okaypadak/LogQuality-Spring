# This Java application assigns specific log IDs to log messages, marks the start and end of log sequences, and sends logs to Elasticsearch over TCP with several settings.

## Features

- **Log IDs:** Assigns unique log IDs to each log sequence.
- **Start and End Markers:** Clearly indicates the start and end of log sequences.
- **Elasticsearch Integration:** Sends logs to Elasticsearch over TCP.
- **Metrics Activation:** By adding the actuator library, the metrics, health, and info endpoints are exposed on the service.

## Related Projects

- [LogQuality-Server](https://github.com/okaypadak/LogQuality-Server): The server component that handles the log data and communication with Elasticsearch.
- [LogQuality-SpringTest](https://github.com/okaypadak/LogQuality-SpringTest): A project designed to test the logging functionality with Spring Boot applications.
- [LogQuality-Exception-Generator](https://github.com/okaypadak/LogQuality-Exception-Generator): A tool for generating exceptions to test error handling and logging.

## Getting Started

### Requirements

- Java 17 or higher
- Maven

### Installation

1. Clone the repository:

    ```sh
    git clone https://github.com/okaypadak/LogQuality-Spring
    cd LogQuality-Spring
    ```

2. Add it to your Maven folder:

    ```sh
    mvn clean install
    ```

### Add to Your Other Projects, Configuration

3. Add the following dependencies and plugins to the `pom.xml` file of your other projects:

    ```xml
    <dependency>
        <groupId>dev.padak</groupId>
        <artifactId>log-quality</artifactId>
        <version>1.0</version>
    </dependency>
    ```

4. Configure the logging settings in the `src/main/resources/application.yml` file:

    ```yml
    logquality:
      project_name: testapp
      file: false
      filebeat: false
      logstash:
        connection: true
        host: 192.168.1.15
        port: 31501

    management:
      endpoints:
        web:
          exposure:
            include: metrics
    ```

## Contributing

Contributions are welcome! Please send a pull request or open an issue to discuss the changes you would like to make.

## License

This project is licensed under the MIT License
