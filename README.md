OpenAPI Governor
================

Enforce rules against OpenAPI (fka Swagger) specifications.

Build:

    ./gradlew installDist

Testing:

    ./build/install/openapi-governor/bin/openapi-governor -s ./examples/petstore_v1.yaml -p ./examples/petstore_v2.yaml -r ./examples/ruleset.yaml
