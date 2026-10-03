
# TutorQuestionResponse


## Properties

Name | Type
------------ | -------------
`answer` | string
`sources` | Array&lt;string&gt;

## Example

```typescript
import type { TutorQuestionResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "answer": La fotosíntesis es el proceso mediante el cual...,
  "sources": [Biología - Unidad 1, Material del curso],
} satisfies TutorQuestionResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as TutorQuestionResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


