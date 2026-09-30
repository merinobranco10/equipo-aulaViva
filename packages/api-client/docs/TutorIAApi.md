# TutorIAApi

All URIs are relative to *https://api.aulaviva.cl*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**askTutor**](TutorIAApi.md#asktutor) | **POST** /v1/tutor/questions | Realizar una consulta al Tutor IA |



## askTutor

> TutorQuestionResponse askTutor(idempotencyKey, tutorQuestionRequest)

Realizar una consulta al Tutor IA

Permite a un estudiante realizar una consulta académica. El Tutor IA genera una respuesta utilizando información contextual recuperada mediante RAG. 

### Example

```ts
import {
  Configuration,
  TutorIAApi,
} from '';
import type { AskTutorRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: bearerAuth
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new TutorIAApi(config);

  const body = {
    // string | Clave única utilizada para garantizar la idempotencia de la operación. 
    idempotencyKey: 550e8400-e29b-41d4-a716-446655440000,
    // TutorQuestionRequest
    tutorQuestionRequest: ...,
  } satisfies AskTutorRequest;

  try {
    const data = await api.askTutor(body);
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
| **idempotencyKey** | `string` | Clave única utilizada para garantizar la idempotencia de la operación.  | [Defaults to `undefined`] |
| **tutorQuestionRequest** | [TutorQuestionRequest](TutorQuestionRequest.md) |  | |

### Return type

[**TutorQuestionResponse**](TutorQuestionResponse.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`, `application/problem+json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Respuesta generada por el Tutor IA |  -  |
| **400** | Datos de entrada inválidos. |  -  |
| **401** | Usuario no autenticado. |  -  |
| **403** | El usuario no tiene permisos para realizar la operación. |  -  |
| **422** | Los datos son válidos sintácticamente pero no semánticamente. |  -  |
| **429** | Límite de peticiones excedido. |  * Retry-After - Segundos que debe esperar antes de reintentar. <br>  |
| **500** | Error interno del servidor. |  -  |
| **503** | Servicio temporalmente no disponible. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

