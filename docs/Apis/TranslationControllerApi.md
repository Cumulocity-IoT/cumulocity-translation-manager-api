# TranslationControllerApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**addTranslations**](TranslationControllerApi.md#addTranslations) | **POST** /api/translations | CREATE or UPDATE translations |
| [**getAllTranslation**](TranslationControllerApi.md#getAllTranslation) | **GET** /api/translations | GET all translations |


<a name="addTranslations"></a>
# **addTranslations**
> List addTranslations(Translation)

CREATE or UPDATE translations

    Creates or updates translations.

### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **Translation** | [**List**](../Models/Translation.md)|  | |

### Return type

[**List**](../Models/Translation.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

<a name="getAllTranslation"></a>
# **getAllTranslation**
> List getAllTranslation()

GET all translations

    Returns all translations

### Parameters
This endpoint does not need any parameter.

### Return type

[**List**](../Models/Translation.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

