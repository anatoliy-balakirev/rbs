# Booking Reports API for RBS

This is a micro-service, which returns list of bookings, created in the current calendar month for logged in client.

## Auth
To authenticate client, JWT token is used. Authorization server is not part of this service.

## Code structure
Code is split between packages based on type of classes. So there are dedicated packages for:
* Configurations (including properties)
* Controllers
* Data (entities and repositories)
* Models (DTOs used in controllers)
* Services (business logic)

Database is initialized using Flyway scripts.

There is an Open API specification in the `resources/spec.yaml`

## Testing
There are a couple of entries inserted into the DB for two clients, to allow some end to end testing. Ids of those clients are:
* 'e4473f24-55e6-4e7b-b11a-8211744fbdfa'
* 'e4473f24-55e6-4e7b-b11a-8211744fbdfd'

In order to test this service end to end - first start it by executing:

`./gradlew bootRun`

Alternatively, build a docker image with:

`./gradlew bootBuildImage`

And then run it with something like:

`docker container run -p 8080:8080 rbs:0.0.1-SNAPSHOT`

Then create JWT token by running:

`./gradlew test --tests com.rbs.service.JwtGeneratorHelper.generateToken`

There will be a log entry like this:

`Generated token is: eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlNDQ3M2YyNC01NWU2LTRlN2ItYjExYS04MjExNzQ0ZmJkZmEiLCJleHAiOjE1OTIxODg5MDIsImlhdCI6MTU5MjE3MDkwMn0.4FKZULJealNvYB9Wm-XMcQtYisF6hvjD6kmCRQu8Q3kPJd2Xza7iMrpmlloQUyfxC3c-539mOd6KRmT_WQ2QtA`

Send `GET` request to `http://localhost:8080/client/bookings` using that token as an `Authorization: Bearer ` header.

## Known issues
* This service is using H2 database. It is not in memory one, but still should be switched to something more robust.
* I'm using `org.javamoney:moneta` to convert from one currency to another, which is of course not a production solution.
* Total amount of bookings is calculated as is, without normalizing number of decimal places per currency.
* There is no endpoint to create bookings, so I had to add flyway migration script to insert data for a couple of users.
* Auth server is not supposed to be part of this micro-service, but it's also not provided as a standalone one. So in order to create a token - test must be used.
* Open API spec is not exposed via an endpoint in the running service.
* There are no integration tests (even though everything is covered with unit and componet tests, so it's not a big deal).
* Currently, creation of the docker image is not configured properly: it is using defaults, which do not have volume and are using weird tag.
* K8s deployment descriptors are not provided. 