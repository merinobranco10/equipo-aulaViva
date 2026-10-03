# EvaluationsApi

All URIs are relative to *https://api.aulaviva.cl*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createEvaluation**](EvaluationsApi.md#createevaluationoperation) | **POST** /v1/evaluations | Crear una evaluación |
| [**getEvaluation**](EvaluationsApi.md#getevaluation) | **GET** /v1/evaluations/{evaluationId} | Consultar una evaluación |
| [**submitEvaluation**](EvaluationsApi.md#submitevaluation) | **POST** /v1/evaluations/{evaluationId}/submissions | Enviar respuestas de una evaluación |



## createEvaluation

> EvaluationCreatedResponse createEvaluation(idempotencyKey, createEvaluationRequest)

Crear una evaluación

Permite a un profesor crear una evaluación para un curso.

### Example

```ts
import {
  Configuration,
  EvaluationsApi,
} from '';
import type { CreateEvaluationOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: bearerAuth
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new EvaluationsApi(config);

  const body = {
    // string | Clave única utilizada para garantizar la idempotencia de la operación. 
    idempotencyKey: 550e8400-e29b-41d4-a716-446655440000,
    // CreateEvaluationRequest
    createEvaluationRequest: ...,
  } satisfies CreateEvaluationOperationRequest;

  try {
    const data = await api.createEvaluation(body);
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
| **createEvaluationRequest** | [CreateEvaluationRequest](CreateEvaluationRequest.md) |  | |

### Return type

[**EvaluationCreatedResponse**](EvaluationCreatedResponse.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`, `application/problem+json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Evaluación creada correctamente |  -  |
| **400** | Datos de entrada inválidos. |  -  |
| **401** | Usuario no autenticado. |  -  |
| **403** | El usuario no tiene permisos para realizar la operación. |  -  |
| **422** | Los datos son válidos sintácticamente pero no semánticamente. |  -  |
| **429** | Límite de peticiones excedido. |  * Retry-After - Segundos que debe esperar antes de reintentar. <br>  |
| **500** | Error interno del servidor. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getEvaluation

> EvaluationResponse getEvaluation(evaluationId)

Consultar una evaluación

Permite a un estudiante consultar una evaluación. Las respuestas correctas no son incluidas en la respuesta. 

### Example

```ts
import {
  Configuration,
  EvaluationsApi,
} from '';
import type { GetEvaluationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: bearerAuth
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new EvaluationsApi(config);

  const body = {
    // string | Identificador de la evaluación
    evaluationId: eval-001,
  } satisfies GetEvaluationRequest;

  try {
    const data = await api.getEvaluation(body);
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
| **evaluationId** | `string` | Identificador de la evaluación | [Defaults to `undefined`] |

### Return type

[**EvaluationResponse**](EvaluationResponse.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`, `application/problem+json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Evaluación encontrada |  -  |
| **401** | Usuario no autenticado. |  -  |
| **403** | El usuario no tiene permisos para realizar la operación. |  -  |
| **404** | Recurso no encontrado. |  -  |
| **500** | Error interno del servidor. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## submitEvaluation

> SubmissionResponse submitEvaluation(evaluationId, idempotencyKey, submissionRequest)

Enviar respuestas de una evaluación

Permite a un estudiante enviar sus respuestas para realizar la corrección automática. 

### Example

```ts
import {
  Configuration,
  EvaluationsApi,
} from '';
import type { SubmitEvaluationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: bearerAuth
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new EvaluationsApi(config);

  const body = {
    // string | Identificador de la evaluación
    evaluationId: eval-001,
    // string | Clave única utilizada para garantizar la idempotencia de la operación. 
    idempotencyKey: 550e8400-e29b-41d4-a716-446655440000,
    // SubmissionRequest
    submissionRequest: ...,
  } satisfies SubmitEvaluationRequest;

  try {
    const data = await api.submitEvaluation(body);
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
| **evaluationId** | `string` | Identificador de la evaluación | [Defaults to `undefined`] |
| **idempotencyKey** | `string` | Clave única utilizada para garantizar la idempotencia de la operación.  | [Defaults to `undefined`] |
| **submissionRequest** | [SubmissionRequest](SubmissionRequest.md) |  | |

### Return type

[**SubmissionResponse**](SubmissionResponse.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`, `application/problem+json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Evaluación enviada y corregida correctamente |  -  |
| **400** | Datos de entrada inválidos. |  -  |
| **401** | Usuario no autenticado. |  -  |
| **403** | El usuario no tiene permisos para realizar la operación. |  -  |
| **404** | Recurso no encontrado. |  -  |
| **422** | Los datos son válidos sintácticamente pero no semánticamente. |  -  |
| **429** | Límite de peticiones excedido. |  * Retry-After - Segundos que debe esperar antes de reintentar. <br>  |
| **500** | Error interno del servidor. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

