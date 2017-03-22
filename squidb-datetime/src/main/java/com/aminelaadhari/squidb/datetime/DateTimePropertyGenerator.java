package com.aminelaadhari.squidb.datetime;

import com.yahoo.aptutils.model.DeclaredTypeName;
import com.yahoo.aptutils.utils.AptUtils;
import com.yahoo.aptutils.writer.JavaFileWriter;
import com.yahoo.aptutils.writer.parameters.MethodDeclarationParameters;
import com.yahoo.squidb.processor.data.ModelSpec;
import com.yahoo.squidb.processor.plugins.defaults.properties.generators.BasicLongPropertyGenerator;

import java.io.IOException;
import java.util.Set;

import javax.lang.model.element.VariableElement;

public class DateTimePropertyGenerator extends BasicLongPropertyGenerator {
    private final DeclaredTypeName fieldType;

    public DateTimePropertyGenerator(ModelSpec<?> modelSpec, VariableElement field, DeclaredTypeName fieldType, AptUtils utils) {
        super(modelSpec, field, utils);
        this.fieldType = fieldType;
    }

    // Add imports required by this property here
    @Override
    protected void registerAdditionalImports(Set<DeclaredTypeName> imports) {
        super.registerAdditionalImports(imports);
        imports.add(this.fieldType);
    }

    // Defines the type passed/returned in get/set
    @Override
    public DeclaredTypeName getTypeForAccessors() {
        return fieldType;
    }

    // Generates getter implementation using the static helper class from earlier
    @Override
    protected void writeGetterBody(JavaFileWriter writer) throws IOException {
        writer.writeStringStatement("Long instant = this.containsNonNullValue(" + propertyName + ") ? this.get(" + propertyName + ") : null");
        writer.writeStringStatement("return instant == null ? null : new " + this.fieldType.getSimpleName() + "(instant)");
    }

    private String getMillisMethodCall() {
        if (DateTimePlugin.JODA_DATETIME.equals(this.fieldType)) {
            return "getMillis()";
        } else if (DateTimePlugin.JODA_LOCALTIME.equals(this.fieldType)) {
            return "toDateTimeToday().getMillis()";
        } else if (DateTimePlugin.JODA_LOCALDATE.equals(this.fieldType)) {
            return "toDate().getTime()";
        } else {
            return "toDateTime().getMillis()";
        }
    }

    // Generates setter implementation using the static helper class from earlier
    @Override
    protected void writeSetterBody(JavaFileWriter writer, MethodDeclarationParameters parameters) throws IOException {
        String argName = parameters.getArgumentNames().get(0);
        writer.writeStringStatement("this.set(" + propertyName + ", " + argName + " == null ? null : " + argName + "." + getMillisMethodCall() + ")");
        writer.writeStringStatement("return this");
    }
}