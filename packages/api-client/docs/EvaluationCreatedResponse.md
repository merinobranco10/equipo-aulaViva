
# EvaluationCreatedResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`title` | string
`status` | string

## Example

```typescript
import type { EvaluationCreatedResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": eval-001,
  "title": Prueba Unidad 1,
  "status": CREATED,
} satisfies EvaluationCreatedResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as EvaluationCreatedResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


