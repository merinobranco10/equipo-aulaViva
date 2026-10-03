
# TutorQuestionRequest


## Properties

Name | Type
------------ | -------------
`courseId` | string
`question` | string

## Example

```typescript
import type { TutorQuestionRequest } from ''

// TODO: Update the object below with actual values
const example = {
  "courseId": course-123,
  "question": ¿Qué es la fotosíntesis?,
} satisfies TutorQuestionRequest

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as TutorQuestionRequest
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


