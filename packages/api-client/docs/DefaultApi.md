# DefaultApi

All URIs are relative to *https://api.aulaviva.cl*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**evaluationCompletedPost**](DefaultApi.md#evaluationcompletedpost) | **POST** /evaluationCompleted | Notificación de evaluación completada |



## evaluationCompletedPost

> evaluationCompletedPost(evaluationCompletedEvent)

Notificación de evaluación completada

Notifica al consumidor cuando un estudiante completa una evaluación y se obtiene su nota. 

### Example

```ts
import {
  Configuration,
  DefaultApi,
} from '';
import type { EvaluationCompletedPostRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: bearerAuth
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new DefaultApi(config);

  const body = {
    // EvaluationCompletedEvent
    evaluationCompletedEvent: ...,
  } satisfies EvaluationCompletedPostRequest;

  try {
    const data = await api.evaluationCompletedPost(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **evaluationCompletedEvent** | [EvaluationCompletedEvent](EvaluationCompletedEvent.md) |  | |

### Return type

`void` (Empty response body)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Evento recibido correctamentes |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

