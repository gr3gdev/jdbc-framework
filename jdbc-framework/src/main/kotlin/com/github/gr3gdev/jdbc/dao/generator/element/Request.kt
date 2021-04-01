package com.github.gr3gdev.jdbc.dao.generator.element

import com.github.gr3gdev.jdbc.metadata.element.TableElement

internal class Request(val table: TableElement, val sql: String, val requestElements: List<RequestElement>)
