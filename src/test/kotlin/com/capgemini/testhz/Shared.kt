package com.capgemini.testhz

import com.fasterxml.jackson.databind.json.JsonMapper
import java.text.DateFormat

object Shared {
    val mapper: JsonMapper by lazy {
        JsonMapper.builder().findAndAddModules()
            .defaultDateFormat(DateFormat.getDateInstance())
            .build()
    }
}