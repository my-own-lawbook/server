# MOL-Server
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=my-own-lawbook_server&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=my-own-lawbook_server) [![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=my-own-lawbook_server&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=my-own-lawbook_server)

This is the repository for the server-component of the MOL-Organization.

## Features
This server acts as the backend-component for the MOL-Clients. As such, it takes care of the following aspects:


- Data persistance via a SQL-Database
- Authorization and access-management
- Business logic and error handling

The server exposes a REST-API over which clients can access the resources platform-independant.

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

Release branches are created for all kinds of releases (Major, Minar and Patch).

Release branches are only created once and never to be touched again. They are supposed to freeze the code at the point of the tag/version.
### Creating a release
To create a release, following steps are to be made:

- Merge `develop` into `master`
- Create new `release/<version>` branch off `master` with `<version>` being the version number

Creating a branch with the `release/` prefix will trigger GitHub actions to perform the following actions:

- Create a GitHub tag for the version
- Create a GitHub release for the tag with some artifacts and source code archives
- Publish a new image to the `ghcr.io` registry, tagged with the created tag 
