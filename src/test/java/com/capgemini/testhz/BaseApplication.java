package com.capgemini.testhz;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BaseApplication {
    @Autowired
    ObjectMapper mapper;

    public ObjectMapper getMapper() {
        return mapper;
    }
}
