/*
 * blanco Framework
 * Copyright (C) 2004-2010 IGA Tosiki
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
package blanco.valueobjectts;

import blanco.cg.BlancoCgObjectFactory;
import blanco.cg.BlancoCgSupportedLang;
import blanco.cg.transformer.BlancoCgTransformerFactory;
import blanco.cg.util.BlancoCgSourceUtil;
import blanco.cg.valueobject.*;
import blanco.commons.util.BlancoJavaSourceUtil;
import blanco.commons.util.BlancoNameAdjuster;
import blanco.commons.util.BlancoStringUtil;
import blanco.valueobjectts.message.BlancoValueObjectTsMessage;
import blanco.valueobjectts.resourcebundle.BlancoValueObjectTsResourceBundle;
import blanco.valueobjectts.valueobject.BlancoValueObjectTsClassStructure;
import blanco.valueobjectts.valueobject.BlancoValueObjectTsFieldStructure;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class that auto-generates TypeScript source code from intermediate XML files for value objects.
 *
 * This is one of the main classes of BlancoValueObjectTs.
 *
 * @author IGA Tosiki
 * @author tueda
 */
public class BlancoValueObjectTsXml2TypeScriptClass {
    /**
     * A message.
     */
    private final BlancoValueObjectTsMessage fMsg = new BlancoValueObjectTsMessage();

    /**
     * Resource bundle object for blancoValueObject.
     */
    private final BlancoValueObjectTsResourceBundle fBundle = new BlancoValueObjectTsResourceBundle();

    /**
     * A programming language expected for the input sheet.
     */
    private int fSheetLang = BlancoCgSupportedLang.JAVA;

    public void setSheetLang(final int argSheetLang) {
        fSheetLang = argSheetLang;
    }

    /**
     * Style of the source code generation destination directory
     */
    private boolean fTargetStyleAdvanced = false;
    public void setTargetStyleAdvanced(boolean argTargetStyleAdvanced) {
        this.fTargetStyleAdvanced = argTargetStyleAdvanced;
    }
    public boolean isTargetStyleAdvanced() {
        return this.fTargetStyleAdvanced;
    }

    private boolean fVerbose = false;
    public void setVerbose(boolean argVerbose) {
        this.fVerbose = argVerbose;
    }
    public boolean isVerbose() {
        return this.fVerbose;
    }

    private int fTabs = 4;
    public int getTabs() {
        return fTabs;
    }
    public void setTabs(int fTabs) {
        this.fTabs = fTabs;
    }

    private boolean defaultGenerateToJson = false;
    public boolean isDefaultGenerateToJson() {
        return this.defaultGenerateToJson;
    }
    public void setDefaultGenerateToJson(boolean defaultGenerateToJson) {
        this.defaultGenerateToJson = defaultGenerateToJson;
    }

    /**
     * A factory for blancoCg to be used internally.
     */
    private BlancoCgObjectFactory fCgFactory = null;

    /**
     * Source file information for blancoCg to be used internally.
     */
    private BlancoCgSourceFile fCgSourceFile = null;

    /**
     * Class information for blancoCg to be used internally.
     */
    private BlancoCgClass fCgClass = null;

    /**
     * Interface information for blancoCg to be used internally.
     */
    private BlancoCgInterface fCgInterface = null;

    /**
     * Character encoding of auto-generated source files.
     */
    private String fEncoding = null;

    public void setEncoding(final String argEncoding) {
        fEncoding = argEncoding;
    }

    private boolean fIsXmlRootElement = false;

    public void setXmlRootElement(final boolean isXmlRootElement) {
        fIsXmlRootElement = isXmlRootElement;
    }

    /**
     * Auto-generates TypeScript source code from an intermediate XML file representing a value object.
     *
     * @param argMetaXmlSourceFile
     *            An XML file containing meta-information about the ValueObject.
     * @param argDirectoryTarget
     *            Source code generation destination directory.
     * @throws IOException
     *             If an I/O exception occurs.
     * @return
     */
    public BlancoValueObjectTsClassStructure[] process(
            final File argMetaXmlSourceFile,
            final File argDirectoryTarget) throws IOException {
        BlancoValueObjectTsXmlParser parser = new BlancoValueObjectTsXmlParser();
        parser.setVerbose(this.isVerbose());
        parser.setDefaultGenerateToJson(this.isDefaultGenerateToJson());
        final BlancoValueObjectTsClassStructure[] structures = parser.parse(argMetaXmlSourceFile);
        for (int index = 0; index < structures.length; index++) {
            BlancoValueObjectTsClassStructure classStructure = structures[index];
            if (!classStructure.getInterface()) {
                // Generates TypeScript source code from the obtained information.
                generateClass(classStructure, argDirectoryTarget);
            } else {
                // Defines it as an interface.
                generateInterface(classStructure, argDirectoryTarget);
            }
        }
        return structures;
    }

    public void processListClass(
            final List<BlancoValueObjectTsClassStructure> classStructures,
            final BlancoValueObjectTsClassStructure listClassStructure,
            final File argDirectoryTarget) throws IOException {
        List<BlancoValueObjectTsFieldStructure> fieldList = listClassStructure.getFieldList();

        BlancoValueObjectTsXmlParser parser = new BlancoValueObjectTsXmlParser();

        if (this.isVerbose()) {
            System.out.println("**** processListClass : " + listClassStructure.getName());
        }

        for (BlancoValueObjectTsClassStructure structure : classStructures) {
            String className = structure.getName();
            String classPackageName = structure.getPackage();
            String classDescription = structure.getDescription();
            String classType = className;
            if (classPackageName != null && classPackageName.length() > 0) {
                classType = classPackageName + "." + className;
            }

            BlancoValueObjectTsFieldStructure fieldStructure = new BlancoValueObjectTsFieldStructure();
            listClassStructure.getFieldList().add(fieldStructure);

            String propertyName = BlancoNameAdjuster.toLowerCaseTitle(className);
            String aliasName = structure.getClassAlias();
            if (aliasName != null && aliasName.length() > 0) {
                propertyName = aliasName;
            }
            fieldStructure.setName(propertyName);
            fieldStructure.setType(classType);
            fieldStructure.setDefault("new " + className + "()");
            fieldStructure.setDescription(classDescription);

            if (isVerbose()) {
                System.out.println("processListClass: " + propertyName + ": " + classType);
            }

            /*
             * Creates import list.
             */
            if (listClassStructure.getCreateImportList()
                    && classPackageName != null
                    && classPackageName.length() > 0) {
                parser.makeImportHeaderList(classPackageName, className, listClassStructure, null);
            }
        }

        /*
         * Outputs the auto-generated one.
         * The current method requires the following assumptions.
         *  * One class definition per file
         *  * Represents directories with Java/Kotlin style package notation in the definition sheet
         * TODO: Should it be possible to define the directory where the files are located on the definition sheet?
         */
        if (listClassStructure.getCreateImportList()) {
            Map<String, List<String>> importHeaderList = parser.importHeaderList;
            Set<String> fromList = importHeaderList.keySet();
            for (String strFrom : fromList) {
                StringBuffer sb = new StringBuffer();
                sb.append("import { ");
                List<String> classNameList = importHeaderList.get(strFrom);
                int count = 0;
                for (String className : classNameList) {
                    if (count > 0) {
                        sb.append(", ");
                    }
                    sb.append(className);
                    count++;
                }
                if (count > 0) {
                    sb.append(" } from \"" + strFrom + "\"");
                    listClassStructure.getHeaderList().add(sb.toString());
                }
            }
        }
        /*
         * Generates (overwrites) class files.
         */
        generateClass(listClassStructure, argDirectoryTarget);
    }

    /**
     * Auto-generates source code from a given class information value object.
     *
     * @param argClassStructure
     *            Class information.
     * @param argDirectoryTarget
     *            Output directory for TypeScript source code.
     * @throws IOException
     *             If an I/O exception occurs.
     */
    public void generateClass(
            final BlancoValueObjectTsClassStructure argClassStructure,
            final File argDirectoryTarget) throws IOException {
        /*
         * The output directory will be in the format specified by the targetStyle argument of the ant task.
         * For compatibility, the output directory will be blanco/main if it is not specified.
         * by tueda, 2019/08/30
         */
        String strTarget = argDirectoryTarget
                .getAbsolutePath(); // advanced
        if (!this.isTargetStyleAdvanced()) {
            strTarget += "/main"; // legacy
        }
        final File fileBlancoMain = new File(strTarget);

        /* tueda DEBUG */
        if (this.isVerbose()) {
            System.out.println("/* tueda */ generateClass argDirectoryTarget : " + argDirectoryTarget.getAbsolutePath());
        }

        // Gets an instance of the BlancoCgObjectFactory class.
        fCgFactory = BlancoCgObjectFactory.getInstance();

        fCgSourceFile = fCgFactory.createSourceFile(argClassStructure
                .getPackage(), null);
        fCgSourceFile.setEncoding(fEncoding);
        fCgSourceFile.setTabs(this.getTabs());

        // Creates a class.
        fCgClass = fCgFactory.createClass(argClassStructure.getName(), "");
        fCgSourceFile.getClassList().add(fCgClass);

        // Sets access to the class.
        // If not specified, it will be public.
        if (argClassStructure.getAccess() == null ||
                argClassStructure.getAccess().length() == 0) {
            argClassStructure.setAccess("public");
        }
        if (isVerbose()) {
            System.out.println("/* tueda */ class access = " + argClassStructure.getAccess());
        }

        // In TypeScript, it ignores the "data" class specification.
        fCgClass.setAccess(argClassStructure.getAccess());
        // Final class or not.
        fCgClass.setFinal(argClassStructure.getFinal());
        // Abstract class or not.
        fCgClass.setAbstract(argClassStructure.getAbstract());

        // Inheritance
        if (BlancoStringUtil.null2Blank(argClassStructure.getExtends())
                .length() > 0) {
            fCgClass.getExtendClassList().add(
                    fCgFactory.createType(argClassStructure.getExtends()));
        }
        // Implementation
        for (int index = 0; index < argClassStructure.getImplementsList()
                .size(); index++) {
            final String impl = (String) argClassStructure.getImplementsList()
                    .get(index);
            fCgClass.getImplementInterfaceList().add(
                    fCgFactory.createType(impl));
        }

        if (fIsXmlRootElement) {
            fCgClass.getAnnotationList().add("XmlRootElement");
            fCgSourceFile.getImportList().add(
                    "javax.xml.bind.annotation.XmlRootElement");
        }

        // Sets the JavaDoc for the class.
        fCgClass.setDescription(argClassStructure.getDescription());
        for (String line : argClassStructure.getDescriptionList()) {
            fCgClass.getLangDoc().getDescriptionList().add(line);
        }

        /* Sets the annotation for the class. */
        List annotationList = argClassStructure.getAnnotationList();
        if (annotationList != null && annotationList.size() > 0) {
            fCgClass.getAnnotationList().addAll(argClassStructure.getAnnotationList());
            /* tueda DEBUG */
            if (this.isVerbose()) {
                System.out.println("/* tueda */ generateClass : class annotation = " + argClassStructure.getAnnotationList().get(0));
            }
        }

        /* In TypeScript, sets the header instead of import. */
        for (int index = 0; index < argClassStructure.getHeaderList()
                .size(); index++) {
            final String header = (String) argClassStructure.getHeaderList()
                    .get(index);
            fCgSourceFile.getHeaderList().add(header);
        }

        boolean toJson = false;
        for (int indexField = 0; indexField < argClassStructure.getFieldList()
                .size(); indexField++) {
            // Processes each field.
            final BlancoValueObjectTsFieldStructure fieldStructure = (BlancoValueObjectTsFieldStructure) argClassStructure
                    .getFieldList().get(indexField);

            // If a required field is not set, exception processing will be performed.
            if (fieldStructure.getName() == null) {
                throw new IllegalArgumentException(fMsg
                        .getMbvoji03(argClassStructure.getName()));
            }
            if (fieldStructure.getType() == null) {
                throw new IllegalArgumentException(fMsg.getMbvoji04(
                        argClassStructure.getName(), fieldStructure.getName()));
            }

            if (!toJson && !fieldStructure.getExcludeToJson()) {
                toJson = true;
            }

            // Generates a field.
            buildField(argClassStructure, fieldStructure);

            if (argClassStructure.getLabel() != true) {
                // Generates a setter method.
                if (!fieldStructure.getValue()) {
                    /*
                     * It does not generate a setter for constant values.
                     */
                    buildMethodSet(argClassStructure, fieldStructure);
                }

                // Generates a getter method.
                buildMethodGet(argClassStructure, fieldStructure);
            }
        }

        if ((toJson && this.defaultGenerateToJson) || argClassStructure.getGenerateEmptyToJSON()) {
            buildMethodToJSON(argClassStructure);
        }

        // TODO: Considers how to generate toString method.
//        if (argClassStructure.getGenerateToString()) {
//            // Generates toString method.
//            buildMethodToString(argClassStructure);
//        }

        // TODO: Considers whether to externally flag whether to generate copyTo method.
//        BlancoBeanUtils.generateCopyToMethod(fCgSourceFile, fCgClass);

        // Auto-generates the actual source code based on the collected information.
        BlancoCgTransformerFactory.getTsSourceTransformer().transform(
                fCgSourceFile, fileBlancoMain);
    }

    /**
     * Auto-generates source code from a given interface information value object.
     *
     * @param argInterfaceStructure
     *            A class information.
     * @param argDirectoryTarget
     *            Output directory for TypeScript source code
     * @throws IOException
     *             If an I/O exception occurs.
     */
    public void generateInterface(
            final BlancoValueObjectTsClassStructure argInterfaceStructure,
            final File argDirectoryTarget) throws IOException {
        /*
         * The output directory will be in the format specified by the targetStyle argument of the ant task.
         * For compatibility, the output directory will be blanco/main if it is not specified.
         * by tueda, 2019/08/30
         */
        String strTarget = argDirectoryTarget
                .getAbsolutePath(); // advanced
        if (!this.isTargetStyleAdvanced()) {
            strTarget += "/main"; // legacy
        }
        final File fileBlancoMain = new File(strTarget);

        /* tueda DEBUG */
        if (this.isVerbose()) {
            System.out.println("/* tueda */ generateInterface argDirectoryTarget : " + argDirectoryTarget.getAbsolutePath());
        }

        // Gets an instance of the BlancoCgObjectFactory class.
        fCgFactory = BlancoCgObjectFactory.getInstance();

        fCgSourceFile = fCgFactory.createSourceFile(argInterfaceStructure
                .getPackage(), null);
        fCgSourceFile.setEncoding(fEncoding);
        fCgSourceFile.setTabs(this.getTabs());

        // Creates an interface.
        fCgInterface = fCgFactory.createInterface(argInterfaceStructure.getName(), "");
        fCgSourceFile.getInterfaceList().add(fCgInterface);

        // In the case of an interface, ignores the access specification (always public).
        fCgInterface.setAccess("public");

        // Inheritance and interface can be created by extensions.
        if (BlancoStringUtil.null2Blank(argInterfaceStructure.getExtends())
                .length() > 0) {
            fCgInterface.getExtendClassList().add(
                    fCgFactory.createType(argInterfaceStructure.getExtends()));
        }

        // There is no implementation in the interface.

        if (fIsXmlRootElement) {
            fCgInterface.getAnnotationList().add("XmlRootElement");
            fCgSourceFile.getImportList().add(
                    "javax.xml.bind.annotation.XmlRootElement");
        }

        // Sets the JavaDoc for the class.
        fCgInterface.setDescription(argInterfaceStructure.getDescription());
        for (String line : argInterfaceStructure.getDescriptionList()) {
            fCgInterface.getLangDoc().getDescriptionList().add(line);
        }

        /* Sets the annotation for the class. */
        List annotationList = argInterfaceStructure.getAnnotationList();
        if (annotationList != null && annotationList.size() > 0) {
            fCgInterface.getAnnotationList().addAll(argInterfaceStructure.getAnnotationList());
            /* tueda DEBUG */
            if (this.isVerbose()) {
                System.out.println("/* tueda */ generateInterface : class annotation = " + argInterfaceStructure.getAnnotationList().get(0));
            }
        }

        /* In TypeScript, sets the header instead of import. */
        for (int index = 0; index < argInterfaceStructure.getHeaderList()
                .size(); index++) {
            final String header = (String) argInterfaceStructure.getHeaderList()
                    .get(index);
            fCgSourceFile.getHeaderList().add(header);
        }

        for (int indexField = 0; indexField < argInterfaceStructure.getFieldList()
                .size(); indexField++) {
            // Processes each field.
            final BlancoValueObjectTsFieldStructure fieldStructure = (BlancoValueObjectTsFieldStructure) argInterfaceStructure
                    .getFieldList().get(indexField);

            // If a required field is not set, exception processing will be performed.
            if (fieldStructure.getName() == null) {
                throw new IllegalArgumentException(fMsg
                        .getMbvoji03(argInterfaceStructure.getName()));
            }
            if (fieldStructure.getType() == null) {
                throw new IllegalArgumentException(fMsg.getMbvoji04(
                        argInterfaceStructure.getName(), fieldStructure.getName()));
            }

            // Generates a field.
            buildField(argInterfaceStructure, fieldStructure);

            // An interface does not have a Getter/Setter.
        }

        // TODO: Does the interface need a toString method?
//        if (argInterfaceStructure.getGenerateToString()) {
//            // Generates toString method.
//            buildMethodToString(argInterfaceStructure);
//        }

        // TODO: Considers whether to externally flag whether to generate copyTo method.
//        BlancoBeanUtils.generateCopyToMethod(fCgSourceFile, fCgClass);

        // Auto-generates the actual source code based on the collected information.
        BlancoCgTransformerFactory.getTsSourceTransformer().transform(
                fCgSourceFile, fileBlancoMain);
    }

    /**
     * Generates a field in the class.
     *
     * @param argClassStructure Class information.
     * @param argFieldStructure
     */
    private void buildField(
            final BlancoValueObjectTsClassStructure argClassStructure,
            final BlancoValueObjectTsFieldStructure argFieldStructure) {

//        System.out.println("%%% " + argFieldStructure.toString());

        boolean isInterface = argClassStructure.getInterface();
        boolean isLabel = isInterface ? false : argClassStructure.getLabel();

        /*
         * In blancoValueObject, the property name is prefixed with "f", but in TypeScript, it is not prefixed with interface.
         */
        String fieldName = argFieldStructure.getName();
        if (isInterface != true && isLabel != true) {
            fieldName = "f" + this.getFieldNameAdjustered(argClassStructure, argFieldStructure);
        }
        final BlancoCgField field = fCgFactory.createField(fieldName,
                argFieldStructure.getType(), null);

        if (isInterface != true) {
            fCgClass.getFieldList().add(field);
        } else {
            fCgInterface.getFieldList().add(field);
        }

        /*
         * Supports Generic. If you don't set it here, it will not be set correctly because blancoCg assumes that "<>" is attached and trims the package part.
         */
        String generic = argFieldStructure.getGeneric();
        if (generic != null && generic.length() > 0) {
            field.getType().setGenerics(generic);
        }

        if (this.isVerbose()) {
            System.out.println("!!! type = " + argFieldStructure.getType());
            System.out.println("!!! generic = " + field.getType().getGenerics());
        }

        if (isInterface != true && isLabel != true) {
            field.setAccess("private");
        } else {
            field.setAccess("public");
        }
        /*
         * For the time being, final will not be supported.
         */
        field.setFinal(false);

        /*
         * Supports const (val in Kotlin).
         */
        field.setConst(argFieldStructure.getValue());

        // Supports nullable.
        if (!argFieldStructure.getNullable()) {
            field.setNotnull(true);
        }

        // Sets the JavaDoc for the field.
        field.setDescription(argFieldStructure.getDescription());
        for (String line : argFieldStructure.getDescriptionList()) {
            field.getLangDoc().getDescriptionList().add(line);
        }
        field.getLangDoc().getDescriptionList().add(
                fBundle.getXml2javaclassFieldName(argFieldStructure.getName()));

        /*
         * In TypeScript, the default value of a property is mandatory in principle.
         * However, it cannot be set for interface.
         */
        if (isInterface != true) {
            final String type = field.getType().getName();
            String defaultRawValue = argFieldStructure.getDefault();
            boolean isNullable = argFieldStructure.getNullable();
            if (!isNullable && (defaultRawValue == null || defaultRawValue.length() <= 0)) {
                System.err.println("/* tueda */ No default value has been set for the field. If it is not an interface, be sure to set the default value or set it to Nullable.");
                throw new IllegalArgumentException(fMsg
                        .getMbvoji08(argClassStructure.getName(), argFieldStructure.getName()));
            }

            // Sets the default value for the field.
            field.getLangDoc().getDescriptionList().add(
                    BlancoCgSourceUtil.escapeStringAsLangDoc(BlancoCgSupportedLang.KOTLIN, fBundle.getXml2javaclassFieldDefault(argFieldStructure
                            .getDefault())));
            if (argClassStructure.getAdjustDefaultValue() == false) {
                // If the variant of the default value is off, the value on the definition sheet is adopted as it is.
                field.setDefault(argFieldStructure.getDefault());
            } else {

                if (type.equals("string")) {
                    // Adds double-quotes.
                    field.setDefault("\""
                            + BlancoJavaSourceUtil
                                    .escapeStringAsJavaSource(argFieldStructure
                                            .getDefault()) + "\"");
                } else {
                    /*
                     * For other types, for the time being, the given value is written as is.
                     */
                    field.setDefault(argFieldStructure.getDefault());
                }
            }
        }

        /* Sets the annotation for the method. */
        List annotationList = argFieldStructure.getAnnotationList();
        if (annotationList != null && annotationList.size() > 0) {
            field.getAnnotationList().addAll(annotationList);
            if (this.isVerbose()) {
                System.out.println("/* tueda */ method annotation = " + field.getAnnotationList().get(0));
            }
        }
    }

    /**
     * Generates a set method.
     *
     * @param argFieldStructure
     *            Field information.
     */
    private void buildMethodSet(
            final BlancoValueObjectTsClassStructure argClassStructure,
            final BlancoValueObjectTsFieldStructure argFieldStructure) {
        // Generates a setter method for each field.
        final BlancoCgMethod method = fCgFactory.createMethod(argFieldStructure.getName(),
                fBundle.getXml2javaclassSetJavadoc01(argFieldStructure
                        .getName()));
        fCgClass.getMethodList().add(method);

        method.setAccess("set");

        // JavaDoc configuration of the method.
        if (argFieldStructure.getDescription() != null) {
            method.getLangDoc().getDescriptionList().add(
                    fBundle.getXml2javaclassSetJavadoc02(argFieldStructure
                            .getDescription()));
        }
        for (String line : argFieldStructure.getDescriptionList()) {
            method.getLangDoc().getDescriptionList().add(line);
        }

        BlancoCgParameter param = fCgFactory.createParameter("arg"
                        + getFieldNameAdjustered(argClassStructure,
                argFieldStructure),
                argFieldStructure.getType(),
                fBundle.getXml2javaclassSetArgJavadoc(argFieldStructure
                        .getName()));
        if (!argFieldStructure.getNullable()) {
            param.setNotnull(true);
        }
        method.getParameterList().add(param);
        // Supports generic if available.
        String generic = argFieldStructure.getGeneric();
        if (generic != null && generic.length() > 0) {
            param.getType().setGenerics(generic);
        }

        // Method implementation.
        method.getLineList().add(
                "this.f"
                        + getFieldNameAdjustered(argClassStructure, argFieldStructure)
                        + " = "
                        + "arg"
                        + getFieldNameAdjustered(argClassStructure,
                                argFieldStructure) + ";");
    }

    /**
     * Generates a get method.
     *
     * @param argFieldStructure
     *            Field information.
     */
    private void buildMethodGet(
            final BlancoValueObjectTsClassStructure argClassStructure,
            final BlancoValueObjectTsFieldStructure argFieldStructure) {
        // Generates a getter method for each field.
        final BlancoCgMethod method = fCgFactory.createMethod(argFieldStructure.getName(),
                fBundle.getXml2javaclassGetJavadoc01(argFieldStructure
                        .getName()));
        fCgClass.getMethodList().add(method);

        // Supports for Notnull.
        if (!argFieldStructure.getNullable()) {
            method.setNotnull(true);
        }

        method.setAccess("get");

        // JavaDoc configuration of the method.
        if (argFieldStructure.getDescription() != null) {
            method.getLangDoc().getDescriptionList().add(
                    fBundle.getXml2javaclassGetJavadoc02(argFieldStructure
                            .getDescription()));
        }
        for (String line : argFieldStructure.getDescriptionList()) {
            method.getLangDoc().getDescriptionList().add(line);
        }
        if (argFieldStructure.getDefault() != null) {
            method.getLangDoc().getDescriptionList().add(
                    BlancoCgSourceUtil.escapeStringAsLangDoc(BlancoCgSupportedLang.JAVA, fBundle.getXml2javaclassGetDefaultJavadoc(argFieldStructure
                            .getDefault())));
        }

        BlancoCgReturn cgReturn = fCgFactory.createReturn(argFieldStructure.getType(),
                fBundle.getXml2javaclassGetReturnJavadoc(argFieldStructure.getName()));
        method.setReturn(cgReturn);
        // Supports generic if available.
        String generic = argFieldStructure.getGeneric();
        if (generic != null && generic.length() > 0) {
            method.getReturn().getType().setGenerics(generic);
        }

        // Method implementation.
        method.getLineList().add(
                "return this.f"
                        + this.getFieldNameAdjustered(argClassStructure, argFieldStructure) + ";");
    }

    /**
     * Generates toJSON method.
     *
     * @param argClassStructure
     */
    private void buildMethodToJSON(
            final BlancoValueObjectTsClassStructure argClassStructure
    ) {
        final BlancoCgMethod method = fCgFactory.createMethod("toJSON",
                "Gets the properties to be written to JSON from this value object.");
        fCgClass.getMethodList().add(method);

        method.setReturn(fCgFactory.createReturn("any",
                "An object returned by toJSON"));
        method.setNotnull(true);
        /*
         * Specified, but not valid in TypeScript.
         */
        method.setFinal(true);

        final List<java.lang.String> listLine = method.getLineList();

        String toJsonStart = "return {";
        String toJsonEnd = "};";

        /*
         * If the parent class exists, it is also included.
         * However, if toJSON does not exist in the parent class, it will not be included.
         */
        if (BlancoStringUtil.null2Blank(argClassStructure.getExtends()).length() > 0 && !argClassStructure.getGenerateEmptyToJSON()) {
            String className = BlancoValueObjectTsUtil.getSimpleClassName(argClassStructure.getExtends());
            System.out.println("toJSON ? className = " + className);
            BlancoValueObjectTsClassStructure voStructure = BlancoValueObjectTsUtil.objects.get(className);
            boolean superHasToJson = false;
            /*
             * There is no class-wide generateToJSON flag in the Excel sheet. Since it is given as an option in the ant task, it is assumed that all generation this time will have the same flag.
             */
            if (voStructure != null && this.isDefaultGenerateToJson()) {
                System.out.println("### Super Class = " + voStructure.getName());
                for (BlancoValueObjectTsFieldStructure fieldStructure : voStructure.getFieldList()) {
                    System.out.println("Exclude toJSON ? = " + fieldStructure.getExcludeToJson());
                    if (!fieldStructure.getExcludeToJson()) {
                        /* If there is any field that is not excluded from toJSON. */
                        superHasToJson = true;
                        break;
                    }
                }
            }

            if (superHasToJson) {
                toJsonStart = "return Object.assign(super.toJSON(), {";
                toJsonEnd = "});";
            } else {
                System.out.println("!!! Skip super toJSON !!!");
            }
        }

        listLine.add(toJsonStart);

        String line = "";
        for (int indexField = 0; indexField < argClassStructure.getFieldList().size() && !argClassStructure.getGenerateEmptyToJSON(); indexField++) {
            final BlancoValueObjectTsFieldStructure field = (BlancoValueObjectTsFieldStructure) argClassStructure
                    .getFieldList().get(indexField);

            if (!field.getExcludeToJson()) {
                if (indexField > 0 && line.length() > 0) {
                    listLine.add(line + ",");
                }
                line = field.getName() + ": this." + field.getName();
            }
        }
        if (line.length() > 0) {
            listLine.add(line);
        }
        listLine.add(toJsonEnd);
    }

    /**
     * Generates toString method.
     *
     * @param argClassStructure
     *            Class information.
     */
    @Deprecated
    private void buildMethodToString(
            final BlancoValueObjectTsClassStructure argClassStructure) {
        final BlancoCgMethod method = fCgFactory.createMethod("toString",
                "Gets the string representation of this value object.");
        fCgClass.getMethodList().add(method);

        method.getLangDoc().getDescriptionList().add("<P>Precautions for use</P>");
        method.getLangDoc().getDescriptionList().add("<UL>");
        method.getLangDoc().getDescriptionList().add(
                "<LI>Only the shallow range of the object will be subject to the stringification process.");
        method.getLangDoc().getDescriptionList().add(
                "<LI>Do not use this method if the object has a circular reference.");
        method.getLangDoc().getDescriptionList().add("</UL>");
        method.setReturn(fCgFactory.createReturn("java.lang.String",
                "String representation of a value object."));
        method.getAnnotationList().add("Override");

        /*
         * For the time being, BlancoValueObjectTs does not allow toString override.
         */
        method.setFinal(true);

        final List<java.lang.String> listLine = method.getLineList();

        listLine.add("final StringBuffer buf = new StringBuffer();");
        listLine.add("buf.append(\"" + argClassStructure.getPackage() + "."
                + argClassStructure.getName() + "[\");");
        for (int indexField = 0; indexField < argClassStructure.getFieldList()
                .size(); indexField++) {
            final BlancoValueObjectTsFieldStructure field = (BlancoValueObjectTsFieldStructure) argClassStructure
                    .getFieldList().get(indexField);

            final String fieldNameAdjustered = (argClassStructure
                    .getAdjustFieldName() == false ? field.getName()
                    : BlancoNameAdjuster.toClassName(field.getName()));

            if (field.getType().endsWith("[]") == false) {
                listLine.add("buf.append(\"" + (indexField == 0 ? "" : ",")
                        + field.getName() + "=\" + f" + fieldNameAdjustered
                        + ");");
            } else {
                // 2006.05.31 t.iga In the case of arrays, it is necessary to first check whether the array itself is null or not.
                listLine.add("if (f" + fieldNameAdjustered + " == null) {");
                // If it is the 0th item, it will be given special treatment without a comma.
                listLine.add("buf.append(" + (indexField == 0 ? "\"" :
                // If it is not the 0th, a comma is always given.
                        "\",") + field.getName() + "=null\");");
                listLine.add("} else {");

                // In the case of arrays, uses deep toString.
                listLine.add("buf.append("
                // If it is the 0th item, it will be given special treatment without a comma.
                        + (indexField == 0 ? "\"" :
                        // If it is not the 0th, a comma is always given.
                                "\",") + field.getName() + "=[\");");
                listLine.add("for (int index = 0; index < f"
                        + fieldNameAdjustered + ".length; index++) {");
                // 2006.05.31 t.iga
                // To make it similar to toString in ArrayList, etc., adds a half-width space after the comma.
                listLine.add("buf.append((index == 0 ? \"\" : \", \") + f"
                        + fieldNameAdjustered + "[index]);");
                listLine.add("}");
                listLine.add("buf.append(\"]\");");
                listLine.add("}");
            }
        }
        listLine.add("buf.append(\"]\");");
        listLine.add("return buf.toString();");
    }

    /**
     * Gets the adjusted field name.
     *
     * @param argFieldStructure
     *            Field information.
     * @return Adjusted field name.
     */
    private String getFieldNameAdjustered(
            final BlancoValueObjectTsClassStructure argClassStructure,
            final BlancoValueObjectTsFieldStructure argFieldStructure) {
        return (argClassStructure.getAdjustFieldName() == false ? argFieldStructure
                .getName()
                : BlancoNameAdjuster.toClassName(argFieldStructure.getName()));
    }
}
