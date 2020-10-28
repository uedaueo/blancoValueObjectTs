/*
 * blanco Framework
 * Copyright (C) 2004-2008 IGA Tosiki
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
package blanco.valueobjectts;

import blanco.cg.BlancoCgSupportedLang;
import blanco.commons.util.BlancoNameUtil;
import blanco.commons.util.BlancoStringUtil;
import blanco.valueobjectts.message.BlancoValueObjectTsMessage;
import blanco.valueobjectts.resourcebundle.BlancoValueObjectTsResourceBundle;
import blanco.valueobjectts.valueobject.BlancoValueObjectTsClassStructure;
import blanco.valueobjectts.valueobject.BlancoValueObjectTsFieldStructure;
import blanco.xml.bind.BlancoXmlBindingUtil;
import blanco.xml.bind.BlancoXmlUnmarshaller;
import blanco.xml.bind.valueobject.BlancoXmlAttribute;
import blanco.xml.bind.valueobject.BlancoXmlDocument;
import blanco.xml.bind.valueobject.BlancoXmlElement;

import java.io.File;
import java.util.*;

/**
 * blancoValueObjectの 中間XMLファイル形式をパース(読み書き)するクラス。
 *
 * @author IGA Tosiki
 */
public class BlancoValueObjectTsXmlParser {
    /**
     * メッセージ。
     */
    private final BlancoValueObjectTsMessage fMsg = new BlancoValueObjectTsMessage();

    private boolean fVerbose = false;
    public void setVerbose(boolean argVerbose) {
        this.fVerbose = argVerbose;
    }
    public boolean isVerbose() {
        return fVerbose;
    }

    /**
     * blancoValueObjectのリソースバンドルオブジェクト。
     */
    private final static BlancoValueObjectTsResourceBundle fBundle = new BlancoValueObjectTsResourceBundle();

    public static Map<String, Integer> mapCommons = new HashMap<String, Integer>() {
        {put(fBundle.getMeta2xmlElementCommon(), BlancoCgSupportedLang.JAVA);}
        {put(fBundle.getMeta2xmlElementCommonCs(), BlancoCgSupportedLang.CS);}
        {put(fBundle.getMeta2xmlElementCommonJs(), BlancoCgSupportedLang.JS);}
        {put(fBundle.getMeta2xmlElementCommonVb(), BlancoCgSupportedLang.VB);}
        {put(fBundle.getMeta2xmlElementCommonPhp(), BlancoCgSupportedLang.PHP);}
        {put(fBundle.getMeta2xmlElementCommonRuby(), BlancoCgSupportedLang.RUBY);}
        {put(fBundle.getMeta2xmlElementCommonPython(), BlancoCgSupportedLang.PYTHON);}
        {put(fBundle.getMeta2xmlElementCommonKt(), BlancoCgSupportedLang.KOTLIN);}
        {put(fBundle.getMeta2xmlElementCommonTs(), BlancoCgSupportedLang.TS);}
    };

    public Map<String, List<String>> importHeaderList = new HashMap<>();

    /**
     * 中間XMLファイルのXMLドキュメントをパースして、バリューオブジェクト情報の配列を取得します。
     *
     * @param argMetaXmlSourceFile
     *            中間XMLファイル。
     * @return パースの結果得られたバリューオブジェクト情報の配列。
     */
    public BlancoValueObjectTsClassStructure[] parse(
            final File argMetaXmlSourceFile) {
        final BlancoXmlDocument documentMeta = new BlancoXmlUnmarshaller()
                .unmarshal(argMetaXmlSourceFile);
        if (documentMeta == null) {
            return null;
        }

        return parse(documentMeta);

    }

    /**
     * 中間XMLファイル形式のXMLドキュメントをパースして、バリューオブジェクト情報の配列を取得します。
     *
     * @param argXmlDocument
     *            中間XMLファイルのXMLドキュメント。
     * @return パースの結果得られたバリューオブジェクト情報の配列。
     */
    public BlancoValueObjectTsClassStructure[] parse(
            final BlancoXmlDocument argXmlDocument) {
        final List<BlancoValueObjectTsClassStructure> listStructure = new ArrayList<BlancoValueObjectTsClassStructure>();

        // ルートエレメントを取得します。
        final BlancoXmlElement elementRoot = BlancoXmlBindingUtil
                .getDocumentElement(argXmlDocument);
        if (elementRoot == null) {
            // ルートエレメントが無い場合には処理中断します。
            return null;
        }

        // sheet(Excelシート)のリストを取得します。
        final List<BlancoXmlElement> listSheet = BlancoXmlBindingUtil
                .getElementsByTagName(elementRoot, "sheet");

        final int sizeListSheet = listSheet.size();
        for (int index = 0; index < sizeListSheet; index++) {
            final BlancoXmlElement elementSheet = listSheet.get(index);

            /*
             * Java以外の言語用に記述されたシートにも対応．
             */
            List<BlancoXmlElement> listCommon = null;
            int sheetLang = BlancoCgSupportedLang.JAVA;
            for (String common : mapCommons.keySet()) {
                listCommon = BlancoXmlBindingUtil
                        .getElementsByTagName(elementSheet,
                                common);
                if (listCommon.size() != 0) {
                    BlancoXmlAttribute attr = new BlancoXmlAttribute();
                    attr.setType("CDATA");
                    attr.setQName("style");
                    attr.setLocalName("style");

                    sheetLang = mapCommons.get(common);
                    attr.setValue(new BlancoCgSupportedLang().convertToString(sheetLang));

                    elementSheet.getAtts().add(attr);

                    /* tueda DEBUG */
                    if (this.isVerbose()) {
                        System.out.println("/* tueda */ style = " + BlancoXmlBindingUtil.getAttribute(elementSheet, "style"));
                    }

                    break;
                }
            }

            if (listCommon == null || listCommon.size() == 0) {
                // commonが無い場合にはスキップします。
                continue;
            }

            // 最初のアイテムのみ処理しています。
            final BlancoXmlElement elementCommon = listCommon.get(0);
            final String name = BlancoXmlBindingUtil.getTextContent(
                    elementCommon, "name");
            if (BlancoStringUtil.null2Blank(name).trim().length() == 0) {
                continue;
            }

            BlancoValueObjectTsClassStructure objClassStructure = null;
            switch (sheetLang) {
                case BlancoCgSupportedLang.JAVA:
                    objClassStructure = parseElementSheet(elementSheet);
                    break;
                case BlancoCgSupportedLang.PHP:
                    objClassStructure = parseElementSheetPhp(elementSheet);
                    /* NOT YET SUPPORT ANOTHER LANGUAGES */
            }

            if (objClassStructure != null) {
                // 得られた情報を記憶します。
                listStructure.add(objClassStructure);
            }
        }

        final BlancoValueObjectTsClassStructure[] result = new BlancoValueObjectTsClassStructure[listStructure
                .size()];
        listStructure.toArray(result);
        return result;
    }

    /**
     * 中間XMLファイル形式の「sheet」XMLエレメントをパースして、バリューオブジェクト情報を取得します。
     *
     * @param argElementSheet
     *            中間XMLファイルの「sheet」XMLエレメント。
     * @return パースの結果得られたバリューオブジェクト情報。「name」が見つからなかった場合には nullを戻します。
     */
    public BlancoValueObjectTsClassStructure parseElementSheet(
            final BlancoXmlElement argElementSheet) {
        final BlancoValueObjectTsClassStructure objClassStructure = new BlancoValueObjectTsClassStructure();
        final List<BlancoXmlElement> listCommon = BlancoXmlBindingUtil
                .getElementsByTagName(argElementSheet,
                        "blancovalueobject-common");
        if (listCommon == null || listCommon.size() == 0) {
            // commonが無い場合にはスキップします。
            return null;
        }
        final BlancoXmlElement elementCommon = listCommon.get(0);
        objClassStructure.setName(BlancoXmlBindingUtil.getTextContent(
                elementCommon, "name"));
        objClassStructure.setPackage(BlancoXmlBindingUtil.getTextContent(
                elementCommon, "package"));

        objClassStructure.setDescription(BlancoXmlBindingUtil.getTextContent(
                elementCommon, "description"));
        if (BlancoStringUtil.null2Blank(objClassStructure.getDescription())
                .length() > 0) {
            final String[] lines = BlancoNameUtil.splitString(objClassStructure
                    .getDescription(), '\n');
            for (int index = 0; index < lines.length; index++) {
                if (index == 0) {
                    objClassStructure.setDescription(lines[index]);
                } else {
                    // 複数行の description については、これを分割して格納します。
                    // ２行目からは、適切に文字参照エンコーディングが実施されているものと仮定します。
                    objClassStructure.getDescriptionList().add(lines[index]);
                }
            }
        }

        objClassStructure.setAccess(BlancoXmlBindingUtil.getTextContent(
                elementCommon, "access"));
        objClassStructure.setAbstract("true".equals(BlancoXmlBindingUtil
                .getTextContent(elementCommon, "abstract")));
        objClassStructure.setData("true".equals(BlancoXmlBindingUtil
                .getTextContent(elementCommon, "data")));
        objClassStructure.setInterface("true".equals(BlancoXmlBindingUtil
                .getTextContent(elementCommon, "interface")));
        objClassStructure.setGenerateToString("true"
                .equals(BlancoXmlBindingUtil.getTextContent(elementCommon,
                        "generateToString")));
        objClassStructure.setAdjustFieldName("true".equals(BlancoXmlBindingUtil
                .getTextContent(elementCommon, "adjustFieldName")));
        objClassStructure.setAdjustDefaultValue("true"
                .equals(BlancoXmlBindingUtil.getTextContent(elementCommon,
                        "adjustDefaultValue")));
        objClassStructure
                .setFieldList(new ArrayList<blanco.valueobjectts.valueobject.BlancoValueObjectTsFieldStructure>());

        if (BlancoStringUtil.null2Blank(objClassStructure.getName()).trim()
                .length() == 0) {
            // 名前が無いものはスキップします。
            return null;
        }

        if (objClassStructure.getPackage() == null) {
            throw new IllegalArgumentException(fMsg
                    .getMbvoji01(objClassStructure.getName()));
        }

        final List<BlancoXmlElement> extendsList = BlancoXmlBindingUtil
                .getElementsByTagName(argElementSheet,
                        "blancovalueobject-extends");
        if (extendsList != null && extendsList.size() != 0) {
            final BlancoXmlElement elementExtendsRoot = extendsList.get(0);
            objClassStructure.setExtends(BlancoXmlBindingUtil.getTextContent(
                    elementExtendsRoot, "name"));
        }

        final List<BlancoXmlElement> interfaceList = BlancoXmlBindingUtil
                .getElementsByTagName(argElementSheet,
                        "blancovalueobject-implements");
        if (interfaceList != null && interfaceList.size() != 0) {
            final BlancoXmlElement elementInterfaceRoot = interfaceList.get(0);
            final List<BlancoXmlElement> listInterfaceChildNodes = BlancoXmlBindingUtil
                    .getElementsByTagName(elementInterfaceRoot, "interface");
            for (int index = 0; index < listInterfaceChildNodes.size(); index++) {
                final BlancoXmlElement elementList = listInterfaceChildNodes
                        .get(index);

                final String interfaceName = BlancoXmlBindingUtil
                        .getTextContent(elementList, "name");
                if (interfaceName == null || interfaceName.trim().length() == 0) {
                    continue;
                }
                objClassStructure.getImplementsList().add(
                        BlancoXmlBindingUtil
                                .getTextContent(elementList, "name"));
            }
        }

        final List<BlancoXmlElement> listList = BlancoXmlBindingUtil
                .getElementsByTagName(argElementSheet, "blancovalueobject-list");
        if (listList != null && listList.size() != 0) {
            final BlancoXmlElement elementListRoot = listList.get(0);
            final List<BlancoXmlElement> listChildNodes = BlancoXmlBindingUtil
                    .getElementsByTagName(elementListRoot, "field");
            for (int index = 0; index < listChildNodes.size(); index++) {
                final BlancoXmlElement elementList = listChildNodes.get(index);
                final BlancoValueObjectTsFieldStructure fieldStructure = new BlancoValueObjectTsFieldStructure();

                fieldStructure.setNo(BlancoXmlBindingUtil.getTextContent(
                        elementList, "no"));
                fieldStructure.setName(BlancoXmlBindingUtil.getTextContent(
                        elementList, "name"));
                if (fieldStructure.getName() == null
                        || fieldStructure.getName().trim().length() == 0) {
                    continue;
                }

                fieldStructure.setType(BlancoXmlBindingUtil.getTextContent(
                        elementList, "type"));

                fieldStructure.setDescription(BlancoXmlBindingUtil
                        .getTextContent(elementList, "description"));
                final String[] lines = BlancoNameUtil.splitString(
                        fieldStructure.getDescription(), '\n');
                for (int indexLine = 0; indexLine < lines.length; indexLine++) {
                    if (indexLine == 0) {
                        fieldStructure.setDescription(lines[indexLine]);
                    } else {
                        // 複数行の description については、これを分割して格納します。
                        // ２行目からは、適切に文字参照エンコーディングが実施されているものと仮定します。
                        fieldStructure.getDescriptionList().add(
                                lines[indexLine]);
                    }
                }

                fieldStructure.setDefault(BlancoXmlBindingUtil.getTextContent(
                        elementList, "default"));
                fieldStructure.setMinLength(BlancoXmlBindingUtil
                        .getTextContent(elementList, "minLength"));
                fieldStructure.setMaxLength(BlancoXmlBindingUtil
                        .getTextContent(elementList, "maxLength"));
                fieldStructure.setLength(BlancoXmlBindingUtil.getTextContent(
                        elementList, "length"));
                fieldStructure.setMinInclusive(BlancoXmlBindingUtil
                        .getTextContent(elementList, "minInclusive"));
                fieldStructure.setMaxInclusive(BlancoXmlBindingUtil
                        .getTextContent(elementList, "maxInclusive"));
                fieldStructure.setPattern(BlancoXmlBindingUtil.getTextContent(
                        elementList, "pattern"));

                if (fieldStructure.getType() == null
                        || fieldStructure.getType().trim().length() == 0) {
                    throw new IllegalArgumentException(fMsg.getMbvoji02(
                            objClassStructure.getName(), fieldStructure
                                    .getName()));
                }

                objClassStructure.getFieldList().add(fieldStructure);
            }
        }

        return objClassStructure;
    }

    /**
     * 中間XMLファイル形式の「sheet」XMLエレメント(PHP書式)をパースして、バリューオブジェクト情報を取得します。
     *
     * @param argElementSheet
     *            中間XMLファイルの「sheet」XMLエレメント。
     * @return パースの結果得られたバリューオブジェクト情報。「name」が見つからなかった場合には nullを戻します。
     */
    public BlancoValueObjectTsClassStructure parseElementSheetPhp(
            final BlancoXmlElement argElementSheet) {
        final BlancoValueObjectTsClassStructure objClassStructure = new BlancoValueObjectTsClassStructure();
        final List<BlancoXmlElement> listCommon = BlancoXmlBindingUtil
                .getElementsByTagName(argElementSheet,
                        "blancovalueobjectphp-common");
        if (listCommon == null || listCommon.size() == 0) {
            // commonが無い場合にはスキップします。
            return null;
        }

        final BlancoXmlElement elementCommon = listCommon.get(0);
        objClassStructure.setName(BlancoXmlBindingUtil.getTextContent(
                elementCommon, "name"));
        objClassStructure.setPackage(BlancoXmlBindingUtil.getTextContent(
                elementCommon, "package"));
        objClassStructure.setClassAlias(BlancoXmlBindingUtil.getTextContent(
                elementCommon, "classAlias"));
        objClassStructure.setBasedir(BlancoXmlBindingUtil.getTextContent(
                elementCommon, "basedir"));

        objClassStructure.setDescription(BlancoXmlBindingUtil.getTextContent(
                elementCommon, "description"));
        if (BlancoStringUtil.null2Blank(objClassStructure.getDescription())
                .length() > 0) {
            final String[] lines = BlancoNameUtil.splitString(objClassStructure
                    .getDescription(), '\n');
            for (int index = 0; index < lines.length; index++) {
                if (index == 0) {
                    objClassStructure.setDescription(lines[index]);
                } else {
                    // 複数行の description については、これを分割して格納します。
                    // ２行目からは、適切に文字参照エンコーディングが実施されているものと仮定します。
                    objClassStructure.getDescriptionList().add(lines[index]);
                }
            }
        }

        /* クラスの annotation に対応 */
        String classAnnotation = BlancoXmlBindingUtil.getTextContent(
                elementCommon, "annotation");
        if (BlancoStringUtil.null2Blank(classAnnotation).length() > 0) {
            String [] annotations = classAnnotation.split("\\\\\\\\");
            List<String> annotationList = new ArrayList<>(Arrays.asList(annotations));
            objClassStructure.setAnnotationList(annotationList);
        }

        objClassStructure.setAccess(BlancoXmlBindingUtil.getTextContent(
                elementCommon, "access"));
        objClassStructure.setFinal("true".equals(BlancoXmlBindingUtil
                .getTextContent(elementCommon, "final")));
        objClassStructure.setAbstract("true".equals(BlancoXmlBindingUtil
                .getTextContent(elementCommon, "abstract")));
        objClassStructure.setData("true".equals(BlancoXmlBindingUtil
                .getTextContent(elementCommon, "data")));
        objClassStructure.setInterface("true".equals(BlancoXmlBindingUtil
                .getTextContent(elementCommon, "interface")));
        objClassStructure.setGenerateToString("true"
                .equals(BlancoXmlBindingUtil.getTextContent(elementCommon,
                        "generateToString")));
        objClassStructure.setAdjustFieldName("true".equals(BlancoXmlBindingUtil
                .getTextContent(elementCommon, "adjustFieldName")));
        objClassStructure.setAdjustDefaultValue("true"
                .equals(BlancoXmlBindingUtil.getTextContent(elementCommon,
                        "adjustDefaultValue")));
        objClassStructure.setCreateImportList("true"
                .equals(BlancoXmlBindingUtil.getTextContent(elementCommon,
                        "createImportList")));
        objClassStructure
                .setFieldList(new ArrayList<blanco.valueobjectts.valueobject.BlancoValueObjectTsFieldStructure>());

        if (BlancoStringUtil.null2Blank(objClassStructure.getName()).trim()
                .length() == 0) {
            // 名前が無いものはスキップします。
            return null;
        }

        if (objClassStructure.getPackage() == null) {
            throw new IllegalArgumentException(fMsg
                    .getMbvoji01(objClassStructure.getName()));
        }

        final List<BlancoXmlElement> extendsList = BlancoXmlBindingUtil
                .getElementsByTagName(argElementSheet,
                        "blancovalueobjectphp-extends");
        if (extendsList != null && extendsList.size() != 0) {
            final BlancoXmlElement elementExtendsRoot = extendsList.get(0);
            String className = BlancoXmlBindingUtil.getTextContent(elementExtendsRoot, "name");
            if (className != null) {
                String classNameCanon = className;
                String packageName = BlancoXmlBindingUtil.getTextContent(elementExtendsRoot, "package");
                BlancoValueObjectTsClassStructure voStructure = BlancoValueObjectTsUtil.objects.get(className);
                if (BlancoStringUtil.null2Blank(packageName).length() == 0 && voStructure != null) {
                    /*
                     * このクラスのパッケージ名を探す
                     */
                    packageName = voStructure.getPackage();
                }
                if (BlancoStringUtil.null2Blank(packageName).length() > 0) {
                    classNameCanon = packageName + "." + className;
                }
                if (isVerbose()) {
                    System.out.println("/* tueda */ Extends : " + classNameCanon);
                }
                objClassStructure.setExtends(classNameCanon);

                /*
                 * TypeScript 用 import 情報の作成
                 */
                if (objClassStructure.getCreateImportList() && BlancoStringUtil.null2Blank(packageName).length() > 0) {
                    String targeBasedir = null;
                    if (voStructure != null) {
                        targeBasedir = voStructure.getBasedir();
                    }
                    this.makeImportHeaderList(packageName, className, objClassStructure, targeBasedir);
                }
            }
        }

        /* 実装 */
        final List<BlancoXmlElement> interfaceList = BlancoXmlBindingUtil
                .getElementsByTagName(argElementSheet,
                        "blancovalueobjectphp-implements");
        if (interfaceList != null && interfaceList.size() != 0) {
            final BlancoXmlElement elementInterfaceRoot = interfaceList.get(0);
            final List<BlancoXmlElement> listInterfaceChildNodes = BlancoXmlBindingUtil
                    .getElementsByTagName(elementInterfaceRoot, "interface");
            for (int index = 0;
                 listInterfaceChildNodes != null &&
                         index < listInterfaceChildNodes.size();
                 index++) {
                final BlancoXmlElement elementList = listInterfaceChildNodes
                        .get(index);

                final String interfaceName = BlancoXmlBindingUtil
                        .getTextContent(elementList, "name");
                if (interfaceName == null || interfaceName.trim().length() == 0) {
                    continue;
                }

                objClassStructure.getImplementsList().add(
                        BlancoXmlBindingUtil
                                .getTextContent(elementList, "name"));
                /*
                 * TypeScript 用 import 情報の作成
                 */
                if (objClassStructure.getCreateImportList()) {
                    String packageName = this.getPackageName(interfaceName);
                    String className = this.getSimpleClassName(interfaceName);
                    BlancoValueObjectTsClassStructure voStructure = BlancoValueObjectTsUtil.objects.get(className);
                    if (BlancoStringUtil.null2Blank(packageName).length() == 0 && voStructure != null) {
                        /*
                         * このクラスのパッケージ名を探す
                         */
                        packageName = voStructure.getPackage();
                    }
                    if (BlancoStringUtil.null2Blank(packageName).length() > 0) {
                        String targetBasedir = null;
                        if (voStructure != null) {
                            targetBasedir = voStructure.getBasedir();
                        }
                        this.makeImportHeaderList(packageName, className, objClassStructure, targetBasedir);
                    }
                }
            }
        }

        final List<BlancoXmlElement> listList = BlancoXmlBindingUtil
                .getElementsByTagName(argElementSheet, "blancovalueobjectphp-list");
        if (listList != null && listList.size() != 0) {
            final BlancoXmlElement elementListRoot = listList.get(0);
            final List<BlancoXmlElement> listChildNodes = BlancoXmlBindingUtil
                    .getElementsByTagName(elementListRoot, "field");
            for (int index = 0; index < listChildNodes.size(); index++) {
                final BlancoXmlElement elementList = listChildNodes.get(index);
                final BlancoValueObjectTsFieldStructure fieldStructure = new BlancoValueObjectTsFieldStructure();

                fieldStructure.setNo(BlancoXmlBindingUtil.getTextContent(
                        elementList, "no"));
                fieldStructure.setName(BlancoXmlBindingUtil.getTextContent(
                        elementList, "name"));
                if (fieldStructure.getName() == null
                        || fieldStructure.getName().trim().length() == 0) {
                    continue;
                }

                /*
                 * 型の取得．ここで TypeScript 風の型名に変えておく
                 */
                String phpType = BlancoXmlBindingUtil.getTextContent(elementList, "type");
                String javaType = phpType;
                if ("boolean".equalsIgnoreCase(phpType)) {
                    javaType = "boolean";
                } else
                if ("integer".equalsIgnoreCase(phpType)) {
                    javaType = "number";
                } else
                if ("double".equalsIgnoreCase(phpType)) {
                    javaType = "number";
                } else
                if ("float".equalsIgnoreCase(phpType)) {
                    javaType = "number";
                } else
                if ("string".equalsIgnoreCase(phpType)) {
                    javaType = "string";
                } else
//                if ("datetime".equalsIgnoreCase(phpType)) {
//                    javaType = "java.util.Date";
//                } else
                if ("array".equalsIgnoreCase(phpType)) {
                    javaType = "Array";
                } else
                if ("object".equalsIgnoreCase(phpType)) {
                    javaType = "any";
                } else {
                    /* この名前の package を探す */
                    String packageName = this.getPackageName(phpType);
                    String className = this.getSimpleClassName(phpType);
                    BlancoValueObjectTsClassStructure voStructure = BlancoValueObjectTsUtil.objects.get(className);
                    System.out.println("### packageName = " + packageName);
                    System.out.println("### className = " + className);
                    System.out.println("### voStructure = " + voStructure);
                    if (BlancoStringUtil.null2Blank(packageName).length() == 0 && voStructure != null) {
                        packageName = voStructure.getPackage();
                    }
                    if (BlancoStringUtil.null2Blank(packageName).length() > 0) {
                        javaType = packageName + "." + className;
                    }

                    /*
                     * TypeScript 用 import 情報の作成
                     */
                    if (objClassStructure.getCreateImportList() && BlancoStringUtil.null2Blank(packageName).length() > 0) {
                        String targetBasedir = null;
                        if (voStructure != null) {
                            targetBasedir = voStructure.getBasedir();
                        }
                        this.makeImportHeaderList(packageName, phpType, objClassStructure, targetBasedir);
                    }
                    /* その他はそのまま記述する */
                    System.out.println("/* tueda */ Unknown php type: " + javaType);
                }

                fieldStructure.setType(javaType);

                /* Generic に対応 */
                String phpGeneric = BlancoXmlBindingUtil.getTextContent(elementList, "generic");
                if (BlancoStringUtil.null2Blank(phpGeneric).length() != 0) {
                    String javaGeneric = phpGeneric;
                    if ("boolean".equalsIgnoreCase(phpGeneric)) {
                        javaGeneric = "boolean";
                    } else
                    if ("integer".equalsIgnoreCase(phpGeneric)) {
                        javaGeneric = "number";
                    } else
                    if ("double".equalsIgnoreCase(phpGeneric)) {
                        javaGeneric = "number";
                    } else
                    if ("float".equalsIgnoreCase(phpGeneric)) {
                        javaGeneric = "number";
                    } else
                    if ("string".equalsIgnoreCase(phpGeneric)) {
                        javaGeneric = "string";
                    } else
//                    if ("datetime".equalsIgnoreCase(phpGeneric)) {
//                        javaGeneric = "java.util.Date";
//                    } else
                    if ("array".equalsIgnoreCase(phpGeneric)) {
                        throw new IllegalArgumentException(fMsg.getMbvoji06(
                                objClassStructure.getName(),
                                fieldStructure.getName(),
                                phpGeneric,
                                phpGeneric
                        ));
                    } else
                    if ("object".equalsIgnoreCase(phpGeneric)) {
                        javaGeneric = "any";
                    } else {
                        /* この名前の package を探す */
                        String packageName = this.getPackageName(phpGeneric);
                        String className = this.getSimpleClassName(phpGeneric);
                        BlancoValueObjectTsClassStructure voStructure = BlancoValueObjectTsUtil.objects.get(className);
                        if (BlancoStringUtil.null2Blank(packageName).length() == 0 && voStructure != null) {
                            packageName = voStructure.getPackage();
                        }
                        if (BlancoStringUtil.null2Blank(packageName).length() > 0) {
                            javaGeneric = packageName + "." + phpGeneric;
                        }

                        /*
                         * TypeScript 用 import 情報の作成
                         */
                        if (objClassStructure.getCreateImportList() && BlancoStringUtil.null2Blank(packageName).length() > 0) {
                            String targeBasedir = null;
                            if (voStructure != null) {
                                targeBasedir = voStructure.getBasedir();
                            }
                            this.makeImportHeaderList(packageName, phpGeneric, objClassStructure, targeBasedir);
                        }
                        /* その他はそのまま記述する */
                        System.out.println("/* tueda */ Unknown php generic: " + javaGeneric);
                    }
                    fieldStructure.setGeneric(javaGeneric);
//                    fieldStructure.setType(javaType);
                }

                /* method の annnotation に対応 */
                String methodAnnotation = BlancoXmlBindingUtil.getTextContent(elementList, "annotation");
                if (BlancoStringUtil.null2Blank(methodAnnotation).length() != 0) {
                    String [] annotations = methodAnnotation.split("\\\\\\\\");
                    List<String> annotationList = new ArrayList<>(Arrays.asList(annotations));

                    fieldStructure.setAnnotationList(annotationList);
                }

                // abstract に対応
                fieldStructure.setAbstract("true".equals(BlancoXmlBindingUtil
                        .getTextContent(elementList, "abstract")));
                // Nullable に対応
                fieldStructure.setNullable("true".equals(BlancoXmlBindingUtil
                        .getTextContent(elementList, "nullable")));
                // value に対応
                fieldStructure.setValue("true".equals(BlancoXmlBindingUtil
                        .getTextContent(elementList, "fixedValue")));
                // constructorArg に対応
                fieldStructure.setConstArg("true".equals(BlancoXmlBindingUtil
                        .getTextContent(elementList, "constructorArg")));
                // toJSON に対応
                fieldStructure.setExcludeToJson("true".equals(BlancoXmlBindingUtil
                        .getTextContent(elementList, "excludeToJSON")));

                fieldStructure.setDescription(BlancoXmlBindingUtil
                        .getTextContent(elementList, "description"));
                final String[] lines = BlancoNameUtil.splitString(
                        fieldStructure.getDescription(), '\n');
                for (int indexLine = 0; indexLine < lines.length; indexLine++) {
                    if (indexLine == 0) {
                        fieldStructure.setDescription(lines[indexLine]);
                    } else {
                        // 複数行の description については、これを分割して格納します。
                        // ２行目からは、適切に文字参照エンコーディングが実施されているものと仮定します。
                        fieldStructure.getDescriptionList().add(
                                lines[indexLine]);
                    }
                }

                fieldStructure.setDefault(BlancoXmlBindingUtil.getTextContent(
                        elementList, "default"));
                fieldStructure.setMinLength(BlancoXmlBindingUtil
                        .getTextContent(elementList, "minLength"));
                fieldStructure.setMaxLength(BlancoXmlBindingUtil
                        .getTextContent(elementList, "maxLength"));
                fieldStructure.setLength(BlancoXmlBindingUtil.getTextContent(
                        elementList, "length"));
                fieldStructure.setMinInclusive(BlancoXmlBindingUtil
                        .getTextContent(elementList, "minInclusive"));
                fieldStructure.setMaxInclusive(BlancoXmlBindingUtil
                        .getTextContent(elementList, "maxInclusive"));
                fieldStructure.setPattern(BlancoXmlBindingUtil.getTextContent(
                        elementList, "pattern"));

                if (fieldStructure.getType() == null
                        || fieldStructure.getType().trim().length() == 0) {
                    throw new IllegalArgumentException(fMsg.getMbvoji02(
                            objClassStructure.getName(), fieldStructure
                                    .getName()));
                }

//                if (this.isVerbose()) {
//                    System.out.println("fieldStructure: " + fieldStructure.get+ fieldStructure.toString());
//                }

                objClassStructure.getFieldList().add(fieldStructure);
            }
        }

        /*
         * header の一覧作成
         * まず、定義書に書かれたものをそのまま出力します。
         */
        final List<BlancoXmlElement> headerList = BlancoXmlBindingUtil
                .getElementsByTagName(argElementSheet, "blancovalueobjectphp-header");
        if (headerList != null && headerList.size() != 0) {
            final BlancoXmlElement elementHeaderRoot = headerList.get(0);
            final List<BlancoXmlElement> listHeaderChildNodes = BlancoXmlBindingUtil
                    .getElementsByTagName(elementHeaderRoot, "header");
            for (int index = 0; index < listHeaderChildNodes.size(); index++) {
                final BlancoXmlElement elementList = listHeaderChildNodes
                        .get(index);

                final String headerName = BlancoXmlBindingUtil
                        .getTextContent(elementList, "name");
                if (this.isVerbose()) {
                    System.out.println("/* tueda */ header = " + headerName);
                }
                if (headerName == null || headerName.trim().length() == 0) {
                    continue;
                }
                objClassStructure.getHeaderList().add(
                        BlancoXmlBindingUtil
                                .getTextContent(elementList, "name"));
            }
        }

        /*
         * 次に、自動生成されたものを出力します。
         * 現在の方式だと、以下の前提が必要。
         *  * 1ファイルに1クラスの定義
         *  * 定義シートでは Java/kotlin 式の package 表記でディレクトリを表現
         * TODO: 定義シート上にファイルの配置ディレクトリを定義できるようにすべし？
         */
        Set<String> fromList = this.importHeaderList.keySet();
        for (String strFrom : fromList) {
            StringBuffer sb = new StringBuffer();
            sb.append("import { ");
            List<String> classNameList = this.importHeaderList.get(strFrom);
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
                objClassStructure.getHeaderList().add(sb.toString());
            }
        }

        return objClassStructure;
    }

    public static Map<String, String> createClassListFromSheets(final File[] argFileMeta) {
        Map<String, String> classList = new HashMap<String, String>();

        for (int index = 0; index < argFileMeta.length; index++) {
            File metaXmlSourceFile = argFileMeta[index];

            if (metaXmlSourceFile.getName().endsWith(".xml") == false) {
                continue;
            }

            final BlancoXmlDocument documentMeta = new BlancoXmlUnmarshaller()
                    .unmarshal(metaXmlSourceFile);
            if (documentMeta == null) {
                continue;
            }

            // ルートエレメントを取得します。
            final BlancoXmlElement elementRoot = BlancoXmlBindingUtil
                    .getDocumentElement(documentMeta);
            if (elementRoot == null) {
                // ルートエレメントが無い場合には処理中断します。
                continue;
            }

            // sheet(Excelシート)のリストを取得します。
            final List<BlancoXmlElement> listSheet = BlancoXmlBindingUtil
                    .getElementsByTagName(elementRoot, "sheet");


            for (BlancoXmlElement elementSheet : listSheet) {
            /*
             * Java以外の言語用に記述されたシートにも対応．
             */
                List<BlancoXmlElement> listCommon = null;
                for (String common : mapCommons.keySet()) {
                    listCommon = BlancoXmlBindingUtil
                            .getElementsByTagName(elementSheet,
                                    common);
                    if (listCommon.size() != 0) {
                        BlancoXmlElement elementCommon = listCommon.get(0);
                        classList.put(
                                BlancoXmlBindingUtil.getTextContent(elementCommon, "name"),
                                BlancoXmlBindingUtil.getTextContent(elementCommon, "package")
                        );

//                        System.out.println("/* tueda */ createClassList = " +
//                                BlancoXmlBindingUtil.getTextContent(elementCommon, "name") + " : " +
//                                BlancoXmlBindingUtil.getTextContent(elementCommon, "package"));
                        break;
                    }
                }
            }
        }

        return classList;
    }

    /**
     * インポート文を生成する
     * @param className
     * @param objClassStructure
     * @param targetBasedir
     */
    public void makeImportHeaderList(String packageName, String className, BlancoValueObjectTsClassStructure objClassStructure, String targetBasedir) {
        if (objClassStructure == null) {
            throw new IllegalArgumentException("objClassStructure should not be NULL.");
        }
        if (className == null || className.length() == 0) {
            System.out.println("/* tueda */ className is not specified. SKIP.");
            return;
        }
        if (className.equals(objClassStructure.getName())) {
            System.out.println("/* tueda */ Maybe recursive defition. SKIP : " + className);
            return;
        }
        if (isVerbose()) {
            System.out.println("makeImportHeaderList: baseDir = " + objClassStructure.getBasedir() + "] targetBasedir = [" + targetBasedir + "]");
        }
        String basedir = targetBasedir;
        if (basedir == null || basedir.length() == 0) {
            basedir = objClassStructure.getBasedir();
        }
        if (basedir == null) {
            basedir = "";
        }
        String importFrom = "./" + className;
        if (packageName != null &&
                packageName.length() != 0 &&
                (packageName.equals(objClassStructure.getPackage()) != true ||
                basedir != objClassStructure.getBasedir())
        ) {
            String classNameCanon = packageName.replace('.', '/') + "/" + className;
            importFrom = basedir + "/" + classNameCanon;
        }

        List<String> importClassList = this.importHeaderList.get(importFrom);
        if (importClassList == null) {
            importClassList = new ArrayList<>();
            this.importHeaderList.put(importFrom, importClassList);
        }
        boolean isMatch = false;
        for (String myClass : importClassList) {
            if (className.equals(myClass)) {
                isMatch = true;
                break;
            }
        }
        if (!isMatch) {
            importClassList.add(className);
            if (this.isVerbose()) {
                System.out.println("/* tueda */ new import { " + className + " } from \"" + importFrom + "\"");
            }
        }
    }

    /**
     * Make canonical classname into Simple.
     *
     * @param argClassNameCanon
     * @return simpleName
     */
    private String getSimpleClassName(final String argClassNameCanon) {
        if (argClassNameCanon == null) {
            return "";
        }

        String simpleName = "";
        final int findLastDot = argClassNameCanon.lastIndexOf('.');
        if (findLastDot == -1) {
            simpleName = argClassNameCanon;
        } else if (findLastDot != argClassNameCanon.length() - 1) {
            simpleName = argClassNameCanon.substring(findLastDot + 1);
        }
        return simpleName;
    }

    /**
     * Make canonical classname into packageName
     *
     * @param argClassNameCanon
     * @return
     */
    private String getPackageName(final String argClassNameCanon) {
        if (argClassNameCanon == null) {
            return "";
        }

        String simpleName = "";
        final int findLastDot = argClassNameCanon.lastIndexOf('.');
        if (findLastDot > 0) {
            simpleName = argClassNameCanon.substring(0, findLastDot);
        }
        return simpleName;
    }
}
