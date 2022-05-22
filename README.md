# jackson-validation

Integrates Jakarta Bean Validation with Jackson object deserialization.

## Limitations

**Note: `static` methods annotated with `@JsonCreator` will not be validated**.
This is [due to Bean Validation itself not supporting them.](https://jakarta.ee/specifications/bean-validation/3.0/jakarta-bean-validation-spec-3.0.html#integration-general-executable)

As an alternative, consider switching to a `@JsonCreator` constructor.

## Usage

### Register `ValidationModule` with the `ObjectMapper`

```java
var mapper = new ObjectMapper();
var validator = Validation.buildDefaultValidatorFactory().getValidator();
mapper.registerModule(new ValidationModule(validator));
```

### Annotate class with constraint annotations

You can apply constraints to constructor arguments:

```java
public class MyBeanForValidation {

    private final String name;
    private final Integer age;

    @JsonCreator
    public MyBeanForValidation(@NotBlank String name, @NotNull @PositiveOrZero Integer age) {
        this.name = Objects.requireNonNull(name);
        this.age = Objects.requireNonNull(age);
    }
}
```

### Call `ObjectMapper.readValue`

Now, an attempt to deserialize an incorrect value will fail with `FailedValidationException`:

```java

ObjectMapper mapper = // get mapper from somewhere...
try {
    // omit the `age` property on purpose
    mapper.readValue("""
        {
            "name": "the-name"
        }
        """)
} catch (FailedValidationException e) {
    JsonObjectViolations violations = e.getJsonViolations();
    violations.getNode("name").getViolations(); // contains "must not be blank" violation
    violations.getNode("age").getViolations(); // contains "must not be null" violation
}
```

## Rationale ("Why should I use this?")

Strongly typed languages like Java generally put a lot of emphasis on creating types
that abstract away their internals, and checking that you don't mix apples and oranges;
for example, although URIs can be easily represented as strings, the standard `java.net.URI`
type guarantees that it contains a valid URI and provides a special API for accessing its components:

```java
private void foo(URI uri) {
    // ...
}

URI uri = URI.create("https://example.com");
foo(uri); // compiles!

String uriString = "https://example.com";
foo(uriString); // compiler error!
```

However, this strong enforcement of types often clashes with the need to parse JSON,
which uses a limited set of types to represent data;
The `uri` property in the example below is technically a JSON string but contains a URI:
```json
{
    "uri": "https://example.com"
}
```

Traditionally, this has been overcome by the use of frameworks such as Jackson, that
provide facilities to "bind" JSON data into a class structure:

```java
public class MyObject {

    @JsonProperty
    private URI uri;
}
```

While this works well for the happy path where JSON values match the expected structure,
it doesn't work so well when they don't;
consider the folowing JSON object:
```json
{
    "uri": "431"
}
```

The value of the `uri` property is clearly not a URI, so parsing will fail, generally 
by throwing an exception.

To be continued...
