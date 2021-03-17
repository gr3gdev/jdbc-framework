package com.github.gr3gdev.jdbc.processor

import com.github.gr3gdev.jdbc.JDBC
import com.github.gr3gdev.jdbc.metadata.Table
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.StringWriter
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.Elements
import javax.tools.JavaFileObject

@RunWith(MockitoJUnitRunner::class)
class JDBCProcessorTest {

    @Mock
    private lateinit var processingEnvironment: ProcessingEnvironment

    @Mock
    private lateinit var roundEnv: RoundEnvironment

    private val annotations = HashSet<TypeElement>()

    private lateinit var tables: MutableSet<TypeElement>
    private lateinit var jdbc: MutableSet<TypeElement>

    @Before
    fun init() {
        tables = HashSet()
        Mockito.`when`(roundEnv.getElementsAnnotatedWith(Mockito.eq(Table::class.java))).thenReturn(tables)

        jdbc = HashSet()
        Mockito.`when`(roundEnv.getElementsAnnotatedWith(Mockito.eq(JDBC::class.java))).thenReturn(jdbc)
    }

    @Test
    fun `test multiple @JDBC`() {
        jdbc.add(Mockito.mock(TypeElement::class.java))
        jdbc.add(Mockito.mock(TypeElement::class.java))
        try {
            JDBCProcessor().process(annotations, roundEnv)
            Assert.fail()
        } catch (exc: RuntimeException) {
            Assert.assertEquals("Only one @JDBC is authorized !", exc.message)
        }
    }

    @Test
    fun `test process`() {
        val elementUtils = Mockito.mock(Elements::class.java)

        val element = Mockito.mock(TypeElement::class.java)

        val packageElement = Mockito.mock(PackageElement::class.java)
        Mockito.`when`(packageElement.toString()).thenReturn("package")
        Mockito.`when`(elementUtils.getPackageOf(element)).thenReturn(packageElement)
        Mockito.`when`(processingEnvironment.elementUtils).thenReturn(elementUtils)

        val annotation = Mockito.mock(AnnotationMirror::class.java)
        val annotationType = Mockito.mock(DeclaredType::class.java)
        Mockito.`when`(element.annotationMirrors).thenReturn(listOf(annotation))
        Mockito.`when`(annotation.annotationType).thenReturn(annotationType)
        Mockito.`when`(annotationType.toString()).thenReturn(JDBC::class.qualifiedName)
        jdbc.add(element)

        val writer = StringWriter()
        val filer = Mockito.mock(Filer::class.java)
        Mockito.`when`(processingEnvironment.filer).thenReturn(filer)
        val sourceFile = Mockito.mock(JavaFileObject::class.java)
        Mockito.`when`(sourceFile.openWriter()).thenReturn(writer)
        Mockito.`when`(filer.createSourceFile(Mockito.anyString())).thenReturn(sourceFile)

        val processor = JDBCProcessor()
        processor.init(processingEnvironment)
        processor.process(annotations, roundEnv)
        Assert.assertTrue(writer.buffer.isNotEmpty())
    }

}