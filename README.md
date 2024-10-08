# MOL-Server
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=my-own-lawbook_server&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=my-own-lawbook_server) [![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=my-own-lawbook_server&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=my-own-lawbook_server) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=my-own-lawbook_server&metric=coverage)](https://sonarcloud.io/summary/new_code?id=my-own-lawbook_server)

This is the repository for the server-component of the MOL-Organization.

## Features
This server acts as the backend-component for the MOL-Clients. As such, it takes care of the following aspects:

- Data persistence via a SQL-Database
- Authorization and access-management
- Business logic and error handling

The server exposes a REST-API over which clients can access the resources platform-independent.

## Installing

### Environment Variables

Running the server requires a certain configuration via environment variables. Below you can see a list of them.

| Name                                | Description                                        | Default | Format         | Example                                   |
|-------------------------------------|----------------------------------------------------|---------|----------------|-------------------------------------------|
| MOL_SECURITY_JWT_SECRET             | Secret for signing the JWT's                       | -       | String         | 73eb7d83-04f1-41d2-a887-e4cb734fa84c      |
| MOL_DATABASE_USER                   | Username for signing into the database             | -       | String         | admin                                     |
| MOL_DATABASE_PASSWORD               | Password for signing into the database             | -       | String         | admin                                     |
| MOL_DATABASE_URL                    | Url to the database                                | -       | String, URL    | jdbc:postgresql://localhost:5432/postgres |
| MOL_MAIL_HOST                       | SMTP host of the mail provider                     | -       | String, Domain | smtp_gmail_com                            |
| MOL_MAIL_PORT                       | SMTP port of the mail provider                     | -       | Integer, Port  | 587                                       |
| MOL_MAIL_FROM                       | Username to the SMTP server                        | -       | String, Email  | john@doe_com                              |
| MOL_MAIL_PASSWORD                   | Password to the SMTP server                        | -       | String, Email  | abcd1234                                  |
| MOL_MAIL_SSL                        | Whether to use SSL for the mail                    | True    | Boolean        | true                                      |
| MOL_SECURITY_JWT_DURATION           | Validity duration of the access tokens in minutes  | 15      | Int, Minute    | 20                                        |
| MOL_SECURITY_REFRESH_TOKEN_DURATION | Validity duration of the refresh tokens in days    | 15      | Int, Day       | 20                                        |
| MOL_SECURITY_EMAIL_TOKEN_DURATION   | Validity duration of the email-verification tokens | 5       | Int, Minute    | 20                                        |
| MOL_PORT                            | Port at which the server runs                      | 8080    | Int, Port      | 20                                        |

### Deployment Methods

#### Jar

Download the latest jar from [the releases page](https://github.com/my-own-lawbook/server/releases) and run it like
this:

```bash
java -jar server.jar
```

Setting up the environment variables depends on your system.

#### Docker

Run the latest container with docker. For the latest tag, check
out [the packages page](https://github.com/orgs/my-own-lawbook/packages?repo_name=server) and run it like this:

Refer to [this guid](https://docs.docker.com/compose/how-tos/environment-variables/set-environment-variables/) on how to
set up the environment variables with docker.

```bash
docker run my-own-lawbook:latest
```

#### Docker Compose

Example `docker-compose.yaml` file:

```yaml
services:
  mol-server:
    image: my-own-lawbook:latest
    ports:
      - 8080:8080
    environment:
      MOL_DATABASE_PASSWORD: admin
      MOL_DATABASE_URL: jdbc:postgresql://localhost:5432/postgres
      MOL_DATABASE_USER: admin
      MOL_MAIL_FROM: simon@bumiller.me
      MOL_MAIL_HOST: smtp.gmail.com
      MOL_MAIL_PASSWORD: abcd1234
      MOL_MAIL_PORT: 578
      MOL_MAIL_SSL: true
      MOL_SECURITY_JWT_SECRET: secret
```

Info: Changing the `MOL_PORT` environment variable changes the **internal** port of the docker container.

## Contributing
### Branching structure
#### Feature branches
Feature branches are named in the `feature/*` way, where `*` would be the issue/milestone id for the feature.

Once a feature is done, it is merged into the `develop` branch. **Merging into the development branch is only done when the feature is fully finished.** 
#### Development branch
The development branch is the branch that tracks the current state of the development. 

#### Master branch
The master branch is the branch that tracks the state of the latest release.

#### Release branches
Feature branches are named in the `release/*` way, where `*` would be the semver version (**Without** a 'v'-prefix).

Release branches are created for all kinds of releases (Major, Minor and Patch).

Release branches are only created once and never to be touched again. They are supposed to freeze the code at the point of the tag/version.
### Creating a release
To create a release, following steps are to be made:

- Merge `develop` into `master`
- Create new `release/<version>` branch off `master` with `<version>` being the version number

Creating a branch with the `release/` prefix will trigger GitHub actions to perform the following actions:

- Create a GitHub tag for the version
- Create a GitHub release for the tag with some artifacts and source code archives
- Publish a new image to the `ghcr.io` registry, tagged with the created tag 
