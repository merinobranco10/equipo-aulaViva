# ProgressApi

All URIs are relative to *https://api.aulaviva.cl*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getStudentProgress**](ProgressApi.md#getstudentprogress) | **GET** /v1/students/{studentId}/progress | Consultar progreso de un estudiante |



## getStudentProgress

> StudentProgressResponse getStudentProgress(studentId)

Consultar progreso de un estudiante

Permite a un profesor o apoderado consultar el progreso académico de un estudiante. 

### Example

```ts
import {
  Configuration,
  ProgressApi,
} from '';
import type { GetStudentProgressRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: bearerAuth
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new ProgressApi(config);

  const body = {
    // string | Identificador del estudiante
    studentId: student-123,
  } satisfies GetStudentProgressRequest;

  try {
    const data = await api.getStudentProgress(body);
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
| **studentId** | `string` | Identificador del estudiante | [Defaults to `undefined`] |

### Return type

[**StudentProgressResponse**](StudentProgressResponse.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`, `application/problem+json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Progreso académico obtenido correctamente |  -  |
| **401** | Usuario no autenticado. |  -  |
| **403** | El usuario no tiene permisos para realizar la operación. |  -  |
| **404** | Recurso no encontrado. |  -  |
| **500** | Error interno del servidor. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

