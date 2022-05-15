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

You can also apply constraints to constructor arguments:

```java
public class MyBeanForValidation {

    @NotBlank
    private final String name;
    @PositiveOrZero
    private final Integer age;

    @JsonCreator
    public MyBeanForValidation(@NotNull String name, @NotNull Integer age) {
        this.name = Objects.requireNonNull(name);
        this.age = Objects.requireNonNull(age);
    }
}
```