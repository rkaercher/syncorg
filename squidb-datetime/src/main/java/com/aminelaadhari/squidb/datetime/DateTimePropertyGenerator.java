package com.aminelaadhari.squidb.datetime;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.yahoo.squidb.processor.data.ModelSpec;
import com.yahoo.squidb.processor.plugins.PluginEnvironment;
import com.yahoo.squidb.processor.plugins.defaults.properties.generators.BasicLongPropertyGenerator;

import javax.lang.model.element.VariableElement;

class DateTimePropertyGenerator extends BasicLongPropertyGenerator {
    private final TypeName fieldType;

    DateTimePropertyGenerator(ModelSpec<?, ?> modelSpec, VariableElement field, TypeName fieldType,
                              PluginEnvironment pluginEnv) {
        super(modelSpec, field, pluginEnv);
        this.fieldType = fieldType;
    }


    // Add imports required by this property here
//    @Override
//    protected void registerAdditionalImports(Set<DeclaredTypeName> imports) {
//        super.registerAdditionalImports(imports);
//        super.
//        imports.add(this.fieldType);
//    }

    // Defines the type passed/returned in get/set
    @Override
    public TypeName getTypeForAccessors() {
        return fieldType;
    }

    protected void writeGetterBody(CodeBlock.Builder body, MethodSpec methodParams) {
        body.addStatement("$T instant = this.containsNonNullValue($L) ? this.get($L) : null", Long.class, propertyName, propertyName);
        body.addStatement("return instant == null ? null : new $T(instant)", this.fieldType);
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

    protected void writeSetterBody(CodeBlock.Builder body, MethodSpec methodParams) {
        String argName = methodParams.parameters.get(0).name;
        body.addStatement("this.set($L, $L == null ? null : $L.$L)", propertyName, argName, argName, getMillisMethodCall());
        body.addStatement("return this");
    }
}