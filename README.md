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
docker run ghcr.io/my-own-lawbook:<version>
```

#### Docker Compose

Example `docker-compose.yaml` file:

```yaml
services:
  mol-server:
    image: ghcr.io/my-own-lawbook:<version>
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

### Making changes

If you want to push changes, do the following steps:

- (If needed: fork the repository)
- Choose or a create an issue/milestone in which you describe the problem/new feature
- Create a new branch, and name it:
  - If for milestone: `milestone-<milestone-id>`
  - If for issue: `issue-<issue-id>`
- Do your changes, prefixing the commits with the id of the issue the commit is related to, e.g.:
  `371: Did some changes`
- State your changes in the `./changelogs/next-changelog.md` (or create if it doesn't exist)
- Create a pull request

### Create a release

If you want to create a release, do the following steps:

- Merge the `develop` branch into the `main` or `master` branch
- Rename `./changelogs/next-changelog.md` to `<release-semver>.md` and adjust content if needed
- Update the `version` field in `./build.gradle.kts` to the semver
- Trigger the 'release.yml' workflow and enter the semver as the release version