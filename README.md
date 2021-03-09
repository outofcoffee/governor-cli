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
    --help, -h -> Usage info 
```

## Building

Build:

    ./gradlew installDist

Testing:

    ./build/install/openapi-governor/bin/openapi-governor -s ./examples/petstore_v2.yaml -p ./examples/petstore_v1.yaml -r ./examples/ruleset.yaml
