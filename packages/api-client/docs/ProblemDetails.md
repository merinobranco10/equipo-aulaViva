
# ProblemDetails

Formato estándar de error basado en RFC 7807.

## Properties

Name | Type
------------ | -------------
`type` | string
`title` | string
`status` | number
`detail` | string
`instance` | string
`traceId` | string
`errors` | [Array&lt;ProblemDetailsErrorsInner&gt;](ProblemDetailsErrorsInner.md)

## Example

```typescript
import type { ProblemDetails } from ''

// TODO: Update the object below with actual values
const example = {
  "type": https://api.aulaviva.cl/problems/validation-error,
  "title": Datos de entrada inválidos,
  "status": 422,
  "detail": El campo title es obligatorio.,
  "instance": https://api.aulaviva.cl/v1/evaluations,
  "traceId": 4bf92f3577b34da6a3ce929d0e0e4736,
  "errors": null,
} satisfies ProblemDetails

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as ProblemDetails
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


