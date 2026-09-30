
# CreateEvaluationRequest


## Properties

Name | Type
------------ | -------------
`courseId` | string
`title` | string
`questions` | [Array&lt;QuestionInput&gt;](QuestionInput.md)

## Example

```typescript
import type { CreateEvaluationRequest } from ''

// TODO: Update the object below with actual values
const example = {
  "courseId": course-123,
  "title": Prueba Unidad 1,
  "questions": null,
} satisfies CreateEvaluationRequest

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as CreateEvaluationRequest
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


