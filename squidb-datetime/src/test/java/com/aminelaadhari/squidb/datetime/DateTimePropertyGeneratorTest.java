package com.aminelaadhari.squidb.datetime;

import com.google.testing.compile.CompilationRule;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.yahoo.squidb.processor.data.TableModelSpecWrapper;
import com.yahoo.squidb.processor.plugins.PluginEnvironment;
import com.yahoo.squidb.processor.plugins.defaults.properties.generators.interfaces.PropertyGenerator;
import com.yahoo.squidb.processor.plugins.defaults.properties.generators.interfaces.TableModelPropertyGenerator;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


public class DateTimePropertyGeneratorTest {

    @Rule
    public CompilationRule rule = new CompilationRule();
    private Elements elements;
    private Types types;

    @Mock
    protected PluginEnvironment pluginEnv;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        elements = rule.getElements();
        types = rule.getTypes();
    }

    private TableModelPropertyGenerator mockPropertyGenerator(Class fieldClass) {
        PropertyGenerator propertyGenerator = mock(TableModelPropertyGenerator.class);
        VariableElement field = mock(VariableElement.class);
        when(propertyGenerator.getField()).thenReturn(field);
        when(field.getSimpleName()).thenReturn(elements.getTypeElement(DateTime.class.getCanonicalName()).getQualifiedName());
        //   when(field.getAnnotation(annotationClass)).thenReturn(annotation);
        return (TableModelPropertyGenerator) propertyGenerator;
    }

    @Test
    public void thatNormalUseCaseIsCovered() {
        DateTimePlugin plugin = new DateTimePlugin();
        plugin.init(getMockedModelSpec(), pluginEnv);
        HashSet<TypeName> interfaces = new HashSet<>();
        TableModelPropertyGenerator propertyGenerator = plugin.getPropertyGenerator(mockField(), TypeName.get(DateTime.class));
        plugin.addInterfacesToImplement(interfaces);
        String columnName = propertyGenerator.getColumnName();
        String propertyName = propertyGenerator.getPropertyName();
        FieldSpec.Builder builder = propertyGenerator.buildTablePropertyDeclaration("TABLE_MODEL_NAME");
        FieldSpec build = builder.build();


        assertEquals(2, interfaces.size());

    }


    @Test
    public void thatNormalUseCaseIsCovered2() {

        HashSet<TypeName> interfaces = new HashSet<>();

        TableModelSpecWrapper mockedModelSpec = getMockedModelSpec();
        TableModelPropertyGenerator propertyGenerator = new DateTimePropertyGenerator(mockedModelSpec, mockField(), mockType(), pluginEnv);
        String propertyName = propertyGenerator.getPropertyName();
        FieldSpec.Builder columnName = propertyGenerator.buildTablePropertyDeclaration("TABLE_MODEL_NAME");

        verifyNoMoreInteractions(mockedModelSpec);
        assertEquals(2, interfaces.size());

    }

    protected TableModelSpecWrapper mockTableModelSpec() {
        TableModelSpecWrapper modelSpec = mock(TableModelSpecWrapper.class);
        TypeElement modelSpecElement = mock(TypeElement.class);
        when(modelSpec.getModelSpecElement()).thenReturn(modelSpecElement);
      //  when(modelSpecElement.getAnnotation(annotationClass)).thenReturn(annotation);
        when(modelSpecElement.getSimpleName());
        return modelSpec;
    }

    private VariableElement mockField() {
        VariableElement result = mock(VariableElement.class);
        when(result.getSimpleName()).thenReturn(elements.getTypeElement(DateTime.class.getCanonicalName()).getQualifiedName());
        return result;
    }

    private TypeName mockType() {
        return TypeName.get(DateTime.class);
    }


    private TableModelSpecWrapper getMockedModelSpec() {
        TableModelSpecWrapper modelSpec = mock(TableModelSpecWrapper.class);
        TypeElement modelSpecElement = mock(TypeElement.class);
        List<TableModelPropertyGenerator> mockedPropertyGenerators = Collections.singletonList(mockPropertyGenerator(DateTime.class));
        when(modelSpec.getModelSpecElement()).thenReturn(modelSpecElement);
        when(modelSpec.getPropertyGenerators()).thenReturn(mockedPropertyGenerators);
       // when(modelSpec.getTableConstraintString()).thenReturn(tableConstraint);
        return modelSpec;
    }

}