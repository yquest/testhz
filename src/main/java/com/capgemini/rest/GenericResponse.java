package com.capgemini.rest;

import java.util.Objects;

public class GenericResponse<T> {
    private final String result;
    private final T value;

    public static <T> GenericResponse<T> createOk(T value){
        return new GenericResponse<>("ok", value);
    }
    public static <T> GenericResponse<T> createNok(String message){
        return new GenericResponse<>(message, null);
    }

    public GenericResponse(String result, T value) {
        this.result = result;
        this.value = value;
    }

    public GenericResponse(String result) {
        this.result = result;
        this.value = null;
    }

    public String getResult() {
        return result;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericResponse<?> that = (GenericResponse<?>) o;
        return Objects.equals(result, that.result) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, value);
    }

    @Override
    public String toString() {
        return "GenericResponse{" +
                "result='" + result + '\'' +
                ", value=" + value +
                '}';
    }
}
