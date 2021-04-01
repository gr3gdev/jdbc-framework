package com.github.gr3gdev.jdbc.dao

import com.github.gr3gdev.jdbc.processor.JDBCProcessor

internal interface StringGenerator {

    fun tabs(nb: Int = 3) = JDBCProcessor.TAB.repeat(nb)

}