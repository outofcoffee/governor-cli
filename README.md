OpenAPI Governor
================

Enforce rules against OpenAPI (fka Swagger) specifications.

## Example

Say you have added a new, required, parameter in the latest version of your OpenAPI specification.

When you run Governor, passing the path of the previous and latest versions of your OpenAPI specifications, the `required-parameters-added` rule will fail with the following message:

    $ governor -s ./examples/petstore_v2.yaml -p ./examples/petstore_v1.yaml -r ./examples/ruleset.yaml
    
    WARN  io.gatehill.governor.RuleEnforcer - Failing rules:
    Required parameter 'category' in GET /pets: new in latest version

This check works for the following scenarios:

- an existing parameter is made required (i.e. newly mandatory)
- a new parameter is added, and marked as required
- a new path or operation is added containing required parameters

## Usage

```
Usage: governor options_list
Options: 
    --currentSpecFile, -s -> OpenAPI specification file (always required) { String }
    --previousSpecFile, -p -> Previous OpenAPI specification file { String }
    --rulesFile, -r -> Rules file (always required) { String }
    --nonZeroExitCodeOnFailure, -z -> Return a non-zero exit code if rule evaluation fails
    --help, -h -> Usage info
```
### Quick start

The `examples` directory contains sample OpenAPI specifications and rules.

If you have Docker installed, use the convenience script that wraps the Docker image build, then
starts a container passing through arguments.

Prerequisites:
- JDK 8 or newer
- Docker

Usage:

    ./governor.sh [args]

> See above for list of valid arguments.

Example:

    $ ./governor.sh -s ./examples/petstore_v2.yaml -p ./examples/petstore_v1.yaml -r ./examples/ruleset.yaml
    
    WARN  io.gatehill.governor.RuleEnforcer - Failing rules:
    required-parameters-added: Required parameter 'category' in GET /pets: new in latest version

## Building

### Using Docker

This method builds a Docker image, named `outofcoffee/guvernor:latest`. You can then bind-mount a directory containing your specifications, use it as a base image etc.

Build:

    ./gradlew dockerBuildImage

Test:

    docker run --rm -it -v $PWD/examples:/app/examples outofcoffee/guvernor:latest \
            -s ./examples/petstore_v2.yaml -p ./examples/petstore_v1.yaml -r ./examples/ruleset.yaml

### Without Docker

This method builds an application distributable, with entrypoint scripts for *NIX and Windows, under the `build/install/openapi-governor/bin` directory.

Build:

    ./gradlew installDist

Test:

    ./build/install/openapi-governor/bin/openapi-governor -s ./examples/petstore_v2.yaml -p ./examples/petstore_v1.yaml -r ./examples/ruleset.yaml
