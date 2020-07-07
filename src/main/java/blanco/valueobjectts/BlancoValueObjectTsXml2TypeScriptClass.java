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
 * バリューオブジェクト用中間XMLファイルから TypeScript ソースコードを自動生成するクラス。
 *
 * BlancoValueObjectTsの主たるクラスのひとつです。
 *
 * @author IGA Tosiki
 * @author tueda
 */
public class BlancoValueObjectTsXml2TypeScriptClass {
    /**
     * メッセージ。
     */
    private final BlancoValueObjectTsMessage fMsg = new BlancoValueObjectTsMessage();

    /**
     * blancoValueObjectのリソースバンドルオブジェクト。
     */
    private final BlancoValueObjectTsResourceBundle fBundle = new BlancoValueObjectTsResourceBundle();

    /**
     * 入力シートに期待するプログラミング言語
     */
    private int fSheetLang = BlancoCgSupportedLang.JAVA;

    public void setSheetLang(final int argSheetLang) {
        fSheetLang = argSheetLang;
    }

    /**
     * ソースコード生成先ディレクトリのスタイル
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
     * 内部的に利用するblancoCg用ファクトリ。
     */
    private BlancoCgObjectFactory fCgFactory = null;

    /**
     * 内部的に利用するblancoCg用ソースファイル情報。
     */
    private BlancoCgSourceFile fCgSourceFile = null;

    /**
     * 内部的に利用するblancoCg用クラス情報。
     */
    private BlancoCgClass fCgClass = null;

    /**
     * 内部的に利用するblancoCg用インタフェイス情報。
     */
    private BlancoCgInterface fCgInterface = null;

    /**
     * 自動生成するソースファイルの文字エンコーディング。
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
     * バリューオブジェクトを表現する中間XMLファイルから、Javaソースコードを自動生成します。
     *
     * @param argMetaXmlSourceFile
     *            ValueObjectに関するメタ情報が含まれているXMLファイル
     * @param argDirectoryTarget
     *            ソースコード生成先ディレクトリ
     * @throws IOException
     *             入出力例外が発生した場合
     * @return
     */
    public BlancoValueObjectTsClassStructure[] process(
            final File argMetaXmlSourceFile,
            final File argDirectoryTarget) throws IOException {
        BlancoValueObjectTsXmlParser parser = new BlancoValueObjectTsXmlParser();
        parser.setVerbose(this.isVerbose());
        final BlancoValueObjectTsClassStructure[] structures = parser.parse(argMetaXmlSourceFile);
        for (int index = 0; index < structures.length; index++) {
            BlancoValueObjectTsClassStructure classStructure = structures[index];
            if (!classStructure.getInterface()) {
                // 得られた情報から TypeScript ソースコードを生成します。
                generateClass(classStructure, argDirectoryTarget);
            } else {
                // interface として定義します。
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

            /*
             * import list の作成
             */
            if (listClassStructure.getCreateImportList()
                    && classPackageName != null
                    && classPackageName.length() > 0) {
                parser.makeImportHeaderList(classPackageName, className, listClassStructure);
            }
        }

        /*
         * 自動生成されたものを出力します。
         * 現在の方式だと、以下の前提が必要。
         *  * 1ファイルに1クラスの定義
         *  * 定義シートでは Java/kotlin 式の package 表記でディレクトリを表現
         * TODO: 定義シート上にファイルの配置ディレクトリを定義できるようにすべし？
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
         * クラスファイルを（上書き）生成
         */
        generateClass(listClassStructure, argDirectoryTarget);
    }

    /**
     * 与えられたクラス情報バリューオブジェクトから、ソースコードを自動生成します。
     *
     * @param argClassStructure
     *            クラス情報
     * @param argDirectoryTarget
     *            TypeScript ソースコードの出力先ディレクトリ
     * @throws IOException
     *             入出力例外が発生した場合。
     */
    public void generateClass(
            final BlancoValueObjectTsClassStructure argClassStructure,
            final File argDirectoryTarget) throws IOException {
        /*
         * 出力ディレクトリはant taskのtargetStyel引数で
         * 指定された書式で出力されます。
         * 従来と互換性を保つために、指定がない場合は blanco/main
         * となります。
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

        // BlancoCgObjectFactoryクラスのインスタンスを取得します。
        fCgFactory = BlancoCgObjectFactory.getInstance();

        fCgSourceFile = fCgFactory.createSourceFile(argClassStructure
                .getPackage(), null);
        fCgSourceFile.setEncoding(fEncoding);
        fCgSourceFile.setTabs(this.getTabs());

        // クラスを作成します。
        fCgClass = fCgFactory.createClass(argClassStructure.getName(), "");
        fCgSourceFile.getClassList().add(fCgClass);

        // クラスのアクセスを設定。
        // 指定がない場合は public とします。
        if (argClassStructure.getAccess() == null ||
                argClassStructure.getAccess().length() == 0) {
            argClassStructure.setAccess("public");
        }
        if (isVerbose()) {
            System.out.println("/* tueda */ class access = " + argClassStructure.getAccess());
        }

        // TypeScript では data クラス指定は無視します。
        fCgClass.setAccess(argClassStructure.getAccess());
        // Finalクラスかどうか。
        fCgClass.setFinal(argClassStructure.getFinal());
        // 抽象クラスかどうか。
        fCgClass.setAbstract(argClassStructure.getAbstract());

        // 継承
        if (BlancoStringUtil.null2Blank(argClassStructure.getExtends())
                .length() > 0) {
            fCgClass.getExtendClassList().add(
                    fCgFactory.createType(argClassStructure.getExtends()));
        }
        // 実装
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

        // クラスのJavaDocを設定します。
        fCgClass.setDescription(argClassStructure.getDescription());
        for (String line : argClassStructure.getDescriptionList()) {
            fCgClass.getLangDoc().getDescriptionList().add(line);
        }

        /* クラスのannotation を設定します */
        List annotationList = argClassStructure.getAnnotationList();
        if (annotationList != null && annotationList.size() > 0) {
            fCgClass.getAnnotationList().addAll(argClassStructure.getAnnotationList());
            /* tueda DEBUG */
            if (this.isVerbose()) {
                System.out.println("/* tueda */ generateClass : class annotation = " + argClassStructure.getAnnotationList().get(0));
            }
        }

        /* TypeScript では import の代わりに header を設定します */
        for (int index = 0; index < argClassStructure.getHeaderList()
                .size(); index++) {
            final String header = (String) argClassStructure.getHeaderList()
                    .get(index);
            fCgSourceFile.getHeaderList().add(header);
        }

        boolean toJson = false;
        for (int indexField = 0; indexField < argClassStructure.getFieldList()
                .size(); indexField++) {
            // おのおののフィールドを処理します。
            final BlancoValueObjectTsFieldStructure fieldStructure = (BlancoValueObjectTsFieldStructure) argClassStructure
                    .getFieldList().get(indexField);

            // 必須項目が未設定の場合には例外処理を実施します。
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

            // フィールドの生成。
            buildField(argClassStructure, fieldStructure, false);

            // セッターメソッドの生成。
            if (!fieldStructure.getValue()) {
                /*
                 * 定数値に対してはsetterを生成しません。
                 */
                buildMethodSet(argClassStructure, fieldStructure);
            }

            // ゲッターメソッドの生成。
            buildMethodGet(argClassStructure, fieldStructure);
        }

        if (toJson && this.defaultGenerateToJson) {
            buildMethodToJSON(argClassStructure);
        }

        // TODO toString メソッドの生成方式を検討する
//        if (argClassStructure.getGenerateToString()) {
//            // toStringメソッドの生成。
//            buildMethodToString(argClassStructure);
//        }

        // TODO copyTo メソッドの生成有無を外部フラグ化するかどうか検討すること。
//        BlancoBeanUtils.generateCopyToMethod(fCgSourceFile, fCgClass);

        // 収集された情報を元に実際のソースコードを自動生成。
        BlancoCgTransformerFactory.getTsSourceTransformer().transform(
                fCgSourceFile, fileBlancoMain);
    }

    /**
     * 与えられたインタフェイス情報バリューオブジェクトから、ソースコードを自動生成します。
     *
     * @param argInterfaceStructure
     *            クラス情報
     * @param argDirectoryTarget
     *            TypeScript ソースコードの出力先ディレクトリ
     * @throws IOException
     *             入出力例外が発生した場合。
     */
    public void generateInterface(
            final BlancoValueObjectTsClassStructure argInterfaceStructure,
            final File argDirectoryTarget) throws IOException {
        /*
         * 出力ディレクトリはant taskのtargetStyel引数で
         * 指定された書式で出力されます。
         * 従来と互換性を保つために、指定がない場合は blanco/main
         * となります。
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

        // BlancoCgObjectFactoryクラスのインスタンスを取得します。
        fCgFactory = BlancoCgObjectFactory.getInstance();

        fCgSourceFile = fCgFactory.createSourceFile(argInterfaceStructure
                .getPackage(), null);
        fCgSourceFile.setEncoding(fEncoding);
        fCgSourceFile.setTabs(this.getTabs());

        // インタフェイスを作成します。
        fCgInterface = fCgFactory.createInterface(argInterfaceStructure.getName(), "");
        fCgSourceFile.getInterfaceList().add(fCgInterface);

        // インタフェイスではアクセス指定は無視します（常にpublic）。
        fCgInterface.setAccess("public");

        // 継承, interface が拡張によって作成される事はありえます
        if (BlancoStringUtil.null2Blank(argInterfaceStructure.getExtends())
                .length() > 0) {
            fCgInterface.getExtendClassList().add(
                    fCgFactory.createType(argInterfaceStructure.getExtends()));
        }

        // インタフェイスに実装はありません。

        if (fIsXmlRootElement) {
            fCgInterface.getAnnotationList().add("XmlRootElement");
            fCgSourceFile.getImportList().add(
                    "javax.xml.bind.annotation.XmlRootElement");
        }

        // クラスのJavaDocを設定します。
        fCgInterface.setDescription(argInterfaceStructure.getDescription());
        for (String line : argInterfaceStructure.getDescriptionList()) {
            fCgInterface.getLangDoc().getDescriptionList().add(line);
        }

        /* クラスのannotation を設定します */
        List annotationList = argInterfaceStructure.getAnnotationList();
        if (annotationList != null && annotationList.size() > 0) {
            fCgInterface.getAnnotationList().addAll(argInterfaceStructure.getAnnotationList());
            /* tueda DEBUG */
            if (this.isVerbose()) {
                System.out.println("/* tueda */ generateInterface : class annotation = " + argInterfaceStructure.getAnnotationList().get(0));
            }
        }

        /* TypeScript では import の代わりに header を設定します */
        for (int index = 0; index < argInterfaceStructure.getHeaderList()
                .size(); index++) {
            final String header = (String) argInterfaceStructure.getHeaderList()
                    .get(index);
            fCgSourceFile.getHeaderList().add(header);
        }

        for (int indexField = 0; indexField < argInterfaceStructure.getFieldList()
                .size(); indexField++) {
            // おのおののフィールドを処理します。
            final BlancoValueObjectTsFieldStructure fieldStructure = (BlancoValueObjectTsFieldStructure) argInterfaceStructure
                    .getFieldList().get(indexField);

            // 必須項目が未設定の場合には例外処理を実施します。
            if (fieldStructure.getName() == null) {
                throw new IllegalArgumentException(fMsg
                        .getMbvoji03(argInterfaceStructure.getName()));
            }
            if (fieldStructure.getType() == null) {
                throw new IllegalArgumentException(fMsg.getMbvoji04(
                        argInterfaceStructure.getName(), fieldStructure.getName()));
            }

            // フィールドの生成。
            buildField(argInterfaceStructure, fieldStructure, true);

            // インタフェイスには Getter/Setter はありません。
        }

        // TODO インタフェイスに toString メソッドは必要か？
//        if (argInterfaceStructure.getGenerateToString()) {
//            // toStringメソッドの生成。
//            buildMethodToString(argInterfaceStructure);
//        }

        // TODO copyTo メソッドの生成有無を外部フラグ化するかどうか検討すること。
//        BlancoBeanUtils.generateCopyToMethod(fCgSourceFile, fCgClass);

        // 収集された情報を元に実際のソースコードを自動生成。
        BlancoCgTransformerFactory.getTsSourceTransformer().transform(
                fCgSourceFile, fileBlancoMain);
    }

    /**
     * クラスにフィールドを生成します。
     *  @param argClassStructure
     *            クラス情報。
     * @param argFieldStructure
     * @param isInterface
     */
    private void buildField(
            final BlancoValueObjectTsClassStructure argClassStructure,
            final BlancoValueObjectTsFieldStructure argFieldStructure,
            boolean isInterface) {

//        System.out.println("%%% " + argFieldStructure.toString());

        /*
         * blancoValueObjectではプロパティ名の前にfをつける流儀であるが、
         * TypeScript では、Interface にはつけない。
         */
        String fieldName = argFieldStructure.getName();
        if (isInterface != true) {
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
         * Generic に対応します。blancoCg 側では <> が付いている前提かつ
         * package部をtrimするので、ここで設定しないと正しく設定されません。
         */
        String generic = argFieldStructure.getGeneric();
        if (generic != null && generic.length() > 0) {
            field.getType().setGenerics(generic);
        }

        if (this.isVerbose()) {
            System.out.println("!!! type = " + argFieldStructure.getType());
            System.out.println("!!! generic = " + field.getType().getGenerics());
        }

        if (isInterface != true) {
            field.setAccess("private");
        } else {
            field.setAccess("public");
        }
        /*
         * 当面、final には対応しません。
         */
        field.setFinal(false);

        /*
         * const (kotlin では val）に対応します。
         */
        field.setConst(argFieldStructure.getValue());

        // nullable に対応します。
        if (!argFieldStructure.getNullable()) {
            field.setNotnull(true);
        }

        // フィールドのJavaDocを設定します。
        field.setDescription(argFieldStructure.getDescription());
        for (String line : argFieldStructure.getDescriptionList()) {
            field.getLangDoc().getDescriptionList().add(line);
        }
        field.getLangDoc().getDescriptionList().add(
                fBundle.getXml2javaclassFieldName(argFieldStructure.getName()));

        /*
         * TypeScript ではプロパティのデフォルト値は原則必須
         * ただし、interface では設定不可。
         */
        if (isInterface != true) {
            final String type = field.getType().getName();
            String defaultRawValue = argFieldStructure.getDefault();
            boolean isNullable = argFieldStructure.getNullable();
            if (!isNullable && (defaultRawValue == null || defaultRawValue.length() <= 0)) {
                System.err.println("/* tueda */ フィールドにデフォルト値が設定されていません。interface でない場合は必ずデフォルト値を設定するか、Nullableを設定してください。");
                throw new IllegalArgumentException(fMsg
                        .getMbvoji08(argClassStructure.getName(), argFieldStructure.getName()));
            }

            // フィールドのデフォルト値を設定します。
            field.getLangDoc().getDescriptionList().add(
                    BlancoCgSourceUtil.escapeStringAsLangDoc(BlancoCgSupportedLang.KOTLIN, fBundle.getXml2javaclassFieldDefault(argFieldStructure
                            .getDefault())));
            if (argClassStructure.getAdjustDefaultValue() == false) {
                // デフォルト値の変形がoffの場合には、定義書上の値をそのまま採用。
                field.setDefault(argFieldStructure.getDefault());
            } else {

                if (type.equals("string")) {
                    // ダブルクオートを付与します。
                    field.setDefault("\""
                            + BlancoJavaSourceUtil
                                    .escapeStringAsJavaSource(argFieldStructure
                                            .getDefault()) + "\"");
                } else {
                    /*
                     * その他の型については当面、与えられた値をそのまま記述します。
                     */
                    field.setDefault(argFieldStructure.getDefault());
                }
            }
        }

        /* メソッドの annotation を設定します */
        List annotationList = argFieldStructure.getAnnotationList();
        if (annotationList != null && annotationList.size() > 0) {
            field.getAnnotationList().addAll(annotationList);
            if (this.isVerbose()) {
                System.out.println("/* tueda */ method annotation = " + field.getAnnotationList().get(0));
            }
        }
    }

    /**
     * setメソッドを生成します。
     *
     * @param argFieldStructure
     *            フィールド情報。
     */
    private void buildMethodSet(
            final BlancoValueObjectTsClassStructure argClassStructure,
            final BlancoValueObjectTsFieldStructure argFieldStructure) {
        // おのおののフィールドに対するセッターメソッドを生成します。
        final BlancoCgMethod method = fCgFactory.createMethod(argFieldStructure.getName(),
                fBundle.getXml2javaclassSetJavadoc01(argFieldStructure
                        .getName()));
        fCgClass.getMethodList().add(method);

        method.setAccess("set");

        // メソッドの JavaDoc設定。
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
        // generic があれば対応
        String generic = argFieldStructure.getGeneric();
        if (generic != null && generic.length() > 0) {
            param.getType().setGenerics(generic);
        }

        // メソッドの実装
        method.getLineList().add(
                "this.f"
                        + getFieldNameAdjustered(argClassStructure, argFieldStructure)
                        + " = "
                        + "arg"
                        + getFieldNameAdjustered(argClassStructure,
                                argFieldStructure) + ";");
    }

    /**
     * getメソッドを生成します。
     *
     * @param argFieldStructure
     *            フィールド情報。
     */
    private void buildMethodGet(
            final BlancoValueObjectTsClassStructure argClassStructure,
            final BlancoValueObjectTsFieldStructure argFieldStructure) {
        // おのおののフィールドに対するゲッターメソッドを生成します。
        final BlancoCgMethod method = fCgFactory.createMethod(argFieldStructure.getName(),
                fBundle.getXml2javaclassGetJavadoc01(argFieldStructure
                        .getName()));
        fCgClass.getMethodList().add(method);

        // Notnull に対応
        if (!argFieldStructure.getNullable()) {
            method.setNotnull(true);
        }

        method.setAccess("get");

        // メソッドの JavaDoc設定。
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
        // generic があれば対応
        String generic = argFieldStructure.getGeneric();
        if (generic != null && generic.length() > 0) {
            method.getReturn().getType().setGenerics(generic);
        }

        // メソッドの実装
        method.getLineList().add(
                "return this.f"
                        + this.getFieldNameAdjustered(argClassStructure, argFieldStructure) + ";");
    }

    /**
     * toJSON メソッドを生成します。
     *
     * @param argClassStructure
     */
    private void buildMethodToJSON(
            final BlancoValueObjectTsClassStructure argClassStructure
    ) {
        final BlancoCgMethod method = fCgFactory.createMethod("toJSON",
                "このバリューオブジェクトからJSONに書き出すプロパティを取得します。");
        fCgClass.getMethodList().add(method);

        method.setReturn(fCgFactory.createReturn("any",
                "toJSONが返すオブジェクト"));
        method.setNotnull(true);
        /*
         * 一応指定するが、Typescript では無効
         */
        method.setFinal(true);

        final List<java.lang.String> listLine = method.getLineList();

        listLine.add("return {");

        String line = "";
        for (int indexField = 0; indexField < argClassStructure.getFieldList()
                .size(); indexField++) {
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
        listLine.add("};");
    }

    /**
     * toStringメソッドを生成します。
     *
     * @param argClassStructure
     *            クラス情報。
     */
    @Deprecated
    private void buildMethodToString(
            final BlancoValueObjectTsClassStructure argClassStructure) {
        final BlancoCgMethod method = fCgFactory.createMethod("toString",
                "このバリューオブジェクトの文字列表現を取得します。");
        fCgClass.getMethodList().add(method);

        method.getLangDoc().getDescriptionList().add("<P>使用上の注意</P>");
        method.getLangDoc().getDescriptionList().add("<UL>");
        method.getLangDoc().getDescriptionList().add(
                "<LI>オブジェクトのシャロー範囲のみ文字列化の処理対象となります。");
        method.getLangDoc().getDescriptionList().add(
                "<LI>オブジェクトが循環参照している場合には、このメソッドは使わないでください。");
        method.getLangDoc().getDescriptionList().add("</UL>");
        method.setReturn(fCgFactory.createReturn("java.lang.String",
                "バリューオブジェクトの文字列表現。"));
        method.getAnnotationList().add("Override");

        /*
         * 当面の間、BlancoValueObjectTs ではtoStringのoverrideを許しません。
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
                // 2006.05.31 t.iga 配列の場合には、先に
                // その配列自身がnullかどうかのチェックが必要です。
                listLine.add("if (f" + fieldNameAdjustered + " == null) {");
                // 0番目の項目である場合にはカンマなしの特別扱いをします。
                listLine.add("buf.append(" + (indexField == 0 ? "\"" :
                // 0番目ではない場合には、常にカンマを付与します。
                        "\",") + field.getName() + "=null\");");
                listLine.add("} else {");

                // 配列の場合にはディープにtoStringします。
                listLine.add("buf.append("
                // 0番目の項目である場合にはカンマなしの特別扱いをします。
                        + (indexField == 0 ? "\"" :
                        // 0番目ではない場合には、常にカンマを付与します。
                                "\",") + field.getName() + "=[\");");
                listLine.add("for (int index = 0; index < f"
                        + fieldNameAdjustered + ".length; index++) {");
                // 2006.05.31 t.iga
                // ArrayListなどのtoStringと同様になるように、カンマのあとに半角スペースを付与するようにします。
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
     * 調整済みのフィールド名を取得します。
     *
     * @param argFieldStructure
     *            フィールド情報。
     * @return 調整後のフィールド名。
     */
    private String getFieldNameAdjustered(
            final BlancoValueObjectTsClassStructure argClassStructure,
            final BlancoValueObjectTsFieldStructure argFieldStructure) {
        return (argClassStructure.getAdjustFieldName() == false ? argFieldStructure
                .getName()
                : BlancoNameAdjuster.toClassName(argFieldStructure.getName()));
    }
}
