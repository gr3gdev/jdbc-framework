package com.github.gr3gdev.jdbc.dao.generator.element

import com.github.gr3gdev.jdbc.metadata.element.ColumnElement
import com.github.gr3gdev.jdbc.metadata.element.TableElement

internal class RequestElement(var table: TableElement, val columnElement: ColumnElement, val parentTable: TableElement?, val part: Part) {
    enum class Part {
        SELECT, UPDATE, WHERE, INSERT
    }
}