package com.github.gr3gdev.jdbc.generator.element

import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

class MethodElement(val name: String, val returnType: TypeMirror, val parameters: List<VariableElement>)
