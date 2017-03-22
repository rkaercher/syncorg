package com.aminelaadhari.squidb.datetime;

import com.yahoo.aptutils.model.DeclaredTypeName;
import com.yahoo.squidb.processor.data.ModelSpec;
import com.yahoo.squidb.processor.plugins.Plugin;
import com.yahoo.squidb.processor.plugins.PluginEnvironment;

import javax.lang.model.element.VariableElement;

public class DateTimePlugin extends Plugin {

    static final DeclaredTypeName JODA_DATETIME = new DeclaredTypeName("org.joda.time.DateTime");
    static final DeclaredTypeName JODA_LOCALDATE = new DeclaredTypeName("org.joda.time.LocalDate");
    static final DeclaredTypeName JODA_LOCALTIME = new DeclaredTypeName("org.joda.time.LocalTime");
    private static final DeclaredTypeName JODA_LOCALDATETIME = new DeclaredTypeName("org.joda.time.LocalDateTime");

    public DateTimePlugin(ModelSpec<?> modelSpec, PluginEnvironment pluginEnv) {
        super(modelSpec, pluginEnv);
    }

    public boolean processVariableElement(VariableElement field, DeclaredTypeName fieldType) {
        if (JODA_DATETIME.equals(fieldType) || JODA_LOCALDATE.equals(fieldType)
                || JODA_LOCALTIME.equals(fieldType) || JODA_LOCALDATETIME.equals(fieldType)) {
            modelSpec.addPropertyGenerator(new DateTimePropertyGenerator(modelSpec, field, fieldType, utils));
            return true;
        }
        return false;
    }
}