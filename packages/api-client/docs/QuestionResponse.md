
# QuestionResponse


## Properties

Name | Type
------------ | -------------
`id` | string
`text` | string
`options` | Array&lt;string&gt;
`points` | number

## Example

```typescript
import type { QuestionResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": question-1,
  "text": ¿Cuál es la capital de Chile?,
  "options": [Santiago, Valparaíso, Concepción],
  "points": 2,
} satisfies QuestionResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as QuestionResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


