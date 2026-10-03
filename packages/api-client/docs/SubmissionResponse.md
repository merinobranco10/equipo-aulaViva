
# SubmissionResponse

Resultado de la corrección automática de una evaluación.

## Properties

Name | Type
------------ | -------------
`grade` | number
`score` | number
`maxScore` | number
`status` | string

## Example

```typescript
import type { SubmissionResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "grade": 4.2,
  "score": 12,
  "maxScore": 20,
  "status": COMPLETED,
} satisfies SubmissionResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as SubmissionResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


