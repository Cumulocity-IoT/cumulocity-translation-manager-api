## cumulocity-microservice-translation-manager-api

## Purpose

This microservice extends the [cumulocity localization (User defined translations)](https://cumulocity.com/docs/standard-tenant/changing-settings/#localization) with an suitable REST API to show and add translations during runtime. The big difference to the built-in localization (user defined translations) is that the translations can be added and changed without uploading the complete language file. Only send what you want to add or change. This means on the other hand that `key` should have a unique name (use prefix) to avoid conflicts, otherwise other translations could be overwritten. Removing translations is not supported!

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

# Useful links 

📘 Explore the Knowledge Base   
Dive into a wealth of Cumulocity IoT tutorials and articles in our [Tech Community](https://community.cumulocity.com/).  

💡 Get Expert Answers    
Stuck or just curious? Ask the Cumulocity IoT experts directly on our [Forum](https://community.cumulocity.com/c/forum/5).   

🚀 Try Cumulocity IoT    
See Cumulocity IoT in action with a [Free Trial](https://www.cumulocity.com/start-your-journey/free-trial).   

✍️ Share Your Feedback    
Your input drives our innovation. If you find a bug, please create an issue in the repository. If you'd like to share your ideas or feedback, please post them [here](https://community.cumulocity.com/c/feedback-ideas/14). 

   
# Authors 

[Alexander Pester](mailto:alexander.pester@cumulocity.com)

# Disclaimer

These tools are provided as-is and without warranty or support. They do not constitute part of the Cumulocity product suite. Users are free to use, fork and modify them, subject to the license agreement. While Cumulocity welcomes contributions, we cannot guarantee to include every contribution in the master project.
