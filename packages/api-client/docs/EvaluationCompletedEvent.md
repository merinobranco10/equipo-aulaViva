
# EvaluationCompletedEvent


## Properties

Name | Type
------------ | -------------
`evaluationId` | string
`studentId` | string
`grade` | number

## Example

```typescript
import type { EvaluationCompletedEvent } from ''

// TODO: Update the object below with actual values
const example = {
  "evaluationId": eval-001,
  "studentId": student-123,
  "grade": 5,
} satisfies EvaluationCompletedEvent

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as EvaluationCompletedEvent
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


