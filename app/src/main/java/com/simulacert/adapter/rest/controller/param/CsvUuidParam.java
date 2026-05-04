package com.simulacert.adapter.rest.controller.param;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CsvUuidParam {

    String value();

    boolean required() default true;

    boolean allowEmpty() default false;
}

