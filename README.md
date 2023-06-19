## cumulocity-microservice-translation-manager-api

## Purpose

This microservice extends the [cumulocity translation manager](https://github.com/SoftwareAG/cumulocity-translation-manager) with an suitable REST API to show and add translations during runtime.

### API:

[Open API Specification](./docs/README.md)

GET {{baseUrl}}/services/translation-manager-api/api/translations

RESPONSE BODY:

```json
[
    {
        "key": "signal/hello/world",
        "translations": {
            "de": "Hallo Welt",
            "en": "Hello World"
        }
    },
    {
        "key": "signal/welcome/description",
        "translations": {
            "de": "Willkommen",
            "en": "Welcome"
        }
    }
]
```

POST {{baseUrl}}/services/translation-manager-api/api/translations

REQUEST BODY:

```json
[
    {
        "key": "signal/welcome/description",
        "translations": {
            "de": "Willkommen",
            "en": "Welcome",
            "fr": "Bienvenue"
        }
    }
]
```

## Prerequisites

- Java installed >= 11
- Maven installed >= 3.6
- Cumulocity IoT Tenant >= 1010.0.0
- Cumulocity IoT User Credentials (Base64 encoded)


## Run

Cloning this repository into you local GIT repository

```console
git clone ...
```

Install archetype localy in your local maven repository

```console
mvn install
```


## Authors 

[Alexander Pester](mailto:alexander.pester@softwareag.com)

## Disclaimer

These tools are provided as-is and without warranty or support. They do not constitute part of the Software AG product suite. Users are free to use, fork and modify them, subject to the license agreement. While Software AG welcomes contributions, we cannot guarantee to include every contribution in the master project.

## Contact

For more information you can Ask a Question in the [TECHcommunity Forums](http://tech.forums.softwareag.com/techjforum/forums/list.page?product=cumulocity).

You can find additional information in the [Software AG TECHcommunity](https://tech.forums.softwareag.com/tag/Cumulocity-IoT).

_________________
Contact us at [TECHcommunity](mailto:technologycommunity@softwareag.com?subject=Github/SoftwareAG) if you have any questions.
