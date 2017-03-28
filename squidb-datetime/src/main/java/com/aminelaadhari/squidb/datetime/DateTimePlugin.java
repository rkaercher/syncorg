package com.aminelaadhari.squidb.datetime;

import com.squareup.javapoet.TypeName;
import com.yahoo.squidb.processor.data.TableModelSpecWrapper;
import com.yahoo.squidb.processor.plugins.defaults.properties.BaseFieldPlugin;
import com.yahoo.squidb.processor.plugins.defaults.properties.generators.interfaces.TableModelPropertyGenerator;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.VariableElement;

public class DateTimePlugin extends BaseFieldPlugin<TableModelSpecWrapper, TableModelPropertyGenerator> {

    static final TypeName JODA_DATETIME = TypeName.get(DateTime.class);
    static final TypeName JODA_LOCALDATE = TypeName.get(LocalDate.class);
    static final TypeName JODA_LOCALTIME = TypeName.get(LocalTime.class);
    private static final TypeName JODA_LOCALDATETIME = TypeName.get(LocalDateTime.class);

    private static final Set<TypeName> supportedTypes = new HashSet<>(Arrays.asList(JODA_DATETIME, JODA_LOCALDATE, JODA_LOCALDATETIME, JODA_LOCALTIME));



    @Override
    protected TableModelPropertyGenerator getPropertyGenerator(VariableElement field, TypeName fieldType) {
        return new DateTimePropertyGenerator(modelSpec, field, fieldType, pluginEnv);
    }

    @Override
    protected boolean hasPropertyGeneratorForField(VariableElement field, TypeName fieldType) {
        return supportedTypes.contains(fieldType);
    }

    @Override
    protected Class<TableModelSpecWrapper> getHandledModelSpecClass() {
        return TableModelSpecWrapper.class;
    }
}