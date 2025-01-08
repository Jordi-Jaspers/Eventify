## Getting Started 🚀

### Prerequisites

Before you begin, ensure you have the following installed:

- **Docker**
- **Docker Compose**
- **SDKMAN** or **ASDF**

### Project Setup

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   ```

2. **Start the services**  
   Run the Docker Compose file to set up required services. This will start:
    * Inbucket (Email service)
    * TimescaleDB HA (High Availability version for Timescale extensions)

    ```bash
    docker-compose up -d
    ```

3. **Setup the database**  
   Use the provided Liquibase script to set up the database with test data:
   ```bash
   bash ../scripts/database-reset.sh -h localhost -p 5432 -P root
    ```

4. **Start the frontend**  
   Navigate to the client directory and install dependencies:
    ```bash
    cd client
    bun install
    bun run dev
    ```

5. **Start the backend**  
   Navigate to the server directory and run the application:
    ```bash
    cd server
    ./gradlew bootRun
    ```
6. **Access the app**  
   Open your browser and go to the following URL:
    - http://localhost:5173/login

   Use the default credentials to log in:
    - **Username:** johndoe@gmail.com
    - **Password:** Testing1!


7. **API Documentation**  
   You can view the API documentation at:
    - http://localhost:8080/api/public/docs/openapi.html


8. **Adjust Environment Variables**  
   You can adjust the environment variables in the `./gradlew bootRun` command to customize the application
   configuration.

| Environment Variable          | Default Value                                   | Purpose                                                 |
|-------------------------------|-------------------------------------------------|---------------------------------------------------------|
| **APPLICATION CONFIGURATION** |                                                 |                                                         |
| `SPRING_APPLICATION_URL`      | `http://localhost:5173`                         | Base URL of the application.                            |
| **EMAIL CONFIGURATION**       |                                                 |                                                         |
| `EMAIL_HOST`                  | `localhost`                                     | Email server host.                                      |
| `EMAIL_PORT`                  | `2500`                                          | Email server port.                                      |
| `EMAIL_ADDRESS`               | `no-reply@localhost`                            | Sender email address.                                   |
| `EMAIL_USERNAME`              | `no-login@localhost.com`                        | Username for email authentication.                      |
| `EMAIL_PASSWORD`              | `password`                                      | Password for email authentication.                      |
| **SECURITY CONFIGURATION**    |                                                 |                                                         |
| `JWT_LIFETIME_SECONDS`        | `900`                                           | Lifetime of JWT tokens in seconds.                      |
| `CORS_ALLOWED_ORIGINS`        | `*`                                             | Comma-separated list of allowed origins for CORS.       |
| **SPRING CONFIGURATION**      |                                                 |                                                         |
| `SPRING_PROFILES_INCLUDE`     | `development`                                   | Active Spring profiles.                                 |
| `DATASOURCE_TOP_URL`          | `jdbc:postgresql://localhost:5432/tst_eventify` | JDBC URL for the database.                              |
| `DATASOURCE_TOP_USERNAME`     | `tst_eventify`                                  | Database username.                                      |
| `DATASOURCE_TOP_PASSWORD`     | `tst_eventify`                                  | Database password.                                      |
| **LOGGING CONFIGURATION**     |                                                 |                                                         |
| `SQL_LOG_LEVEL`               | `INFO`                                          | `DEBUG` for database logging and `INFO` to turn it off. |
