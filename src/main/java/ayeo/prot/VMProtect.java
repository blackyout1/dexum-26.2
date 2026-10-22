package ayeo.prot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface VMProtect {
    Type protect() default Type.MUTATION;

    enum Type {
        MUTATION,
        VIRTUALIZATION,
        ULTRA
    }
}
