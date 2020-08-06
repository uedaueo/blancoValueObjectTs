/*
 * blanco Framework
 * Copyright (C) 2004-2008 IGA Tosiki
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
package blanco.valueobjectts.task;

import blanco.cg.BlancoCgSupportedLang;
import blanco.commons.util.BlancoStringUtil;
import blanco.valueobjectts.*;
import blanco.valueobjectts.message.BlancoValueObjectTsMessage;
import blanco.valueobjectts.task.valueobject.BlancoValueObjectTsProcessInput;
import blanco.valueobjectts.valueobject.BlancoValueObjectTsClassStructure;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlancoValueObjectTsProcessImpl implements BlancoValueObjectTsProcess {

    /**
     * メッセージ。
     */
    private final BlancoValueObjectTsMessage fMsg = new BlancoValueObjectTsMessage();

    /**
     * {@inheritDoc}
     */
    public int execute(final BlancoValueObjectTsProcessInput input)
            throws IOException, IllegalArgumentException {
        System.out.println("- " + BlancoValueObjectTsConstants.PRODUCT_NAME
                + " (" + BlancoValueObjectTsConstants.VERSION + ")");
        try {
            final File fileMetadir = new File(input.getMetadir());
            if (fileMetadir.exists() == false) {
                throw new IllegalArgumentException(fMsg.getMbvoja01(input
                        .getMetadir()));
            }

            /*
             * 改行コードを決定します。
             */
            String LF = "\n";
            String CR = "\r";
            String CRLF = CR + LF;
            String lineSeparatorMark = input.getLineSeparator();
            String lineSeparator = "";
            if ("LF".equals(lineSeparatorMark)) {
                lineSeparator = LF;
            } else if ("CR".equals(lineSeparatorMark)) {
                lineSeparator = CR;
            } else if ("CRLF".equals(lineSeparatorMark)) {
                lineSeparator = CRLF;
            }
            if (lineSeparator.length() != 0) {
                System.setProperty("line.separator", lineSeparator);
                if (input.getVerbose()) {
                    System.out.println("lineSeparator try to change to " + lineSeparatorMark);
                    String newProp = System.getProperty("line.separator");
                    String newMark = "other";
                    if (LF.equals(newProp)) {
                        newMark = "LF";
                    } else if (CR.equals(newProp)) {
                        newMark = "CR";
                    } else if (CRLF.equals(newProp)) {
                        newMark = "CRLF";
                    }
                    System.out.println("New System Props = " + newMark);
                }
            }

            /*
             * targetdir, targetStyleの処理。
             * 生成されたコードの保管場所を設定する。
             * targetstyle = blanco:
             *  targetdirの下に main ディレクトリを作成
             * targetstyle = maven:
             *  targetdirの下に main/java ディレクトリを作成
             * targetstyle = free:
             *  targetdirをそのまま使用してディレクトリを作成。
             *  ただしtargetdirがからの場合はデフォルト文字列(blanco)使用する。
             * by tueda, 2019/08/30
             */
            String strTarget = input.getTargetdir();
            String style = input.getTargetStyle();
            // ここを通ったら常にtrue
            boolean isTargetStyleAdvanced = true;
            if (style != null && BlancoValueObjectTsConstants.TARGET_STYLE_MAVEN.equals(style)) {
                strTarget = strTarget + "/" + BlancoValueObjectTsConstants.TARGET_DIR_SUFFIX_MAVEN;
            } else if (style == null ||
                    !BlancoValueObjectTsConstants.TARGET_STYLE_FREE.equals(style)) {
                strTarget = strTarget + "/" + BlancoValueObjectTsConstants.TARGET_DIR_SUFFIX_BLANCO;
            }
            /* style が free だったらtargetdirをそのまま使う */
            if (input.getVerbose()) {
                System.out.println("/* tueda */ TARGETDIR = " + strTarget);
            }

            boolean generateToJson = input.getGenerateToJson();

            // テンポラリディレクトリを作成。
            new File(input.getTmpdir()
                    + BlancoValueObjectTsConstants.TARGET_SUBDIRECTORY).mkdirs();

            new BlancoValueObjectTsMeta2Xml().processDirectory(fileMetadir, input
                    .getTmpdir()
                    + BlancoValueObjectTsConstants.TARGET_SUBDIRECTORY);

            // XML化されたメタファイルからValueObjectを生成
            // 最初にテンポラリフォルダを走査
            final File[] fileMeta2 = new File(input.getTmpdir()
                    + BlancoValueObjectTsConstants.TARGET_SUBDIRECTORY)
                    .listFiles();

        /*
         * まず始めにすべてのシートを検索して，クラス名とpackage名のリストを作ります．
         * php形式の定義書では，クラスを指定する際にpackage名が指定されていないからです．
         */
            BlancoValueObjectTsXmlParser.classList =
                    BlancoValueObjectTsXmlParser.createClassListFromSheets(fileMeta2);

            /*
             * listClass が指定されている場合は、自動生成したクラスの一覧を
             * 保持するValueObjectを作成する準備をします。
             */
            boolean createClassList = false;
            String listClassName = input.getListClass();
            BlancoValueObjectTsClassStructure listClassStructure = null;
            List<BlancoValueObjectTsClassStructure> listClassStructures = new ArrayList<>();
            if (listClassName != null && listClassName.length() > 0) {
                createClassList = true;
                /*
                 * ClassList 生成のために、
                 * searchTmpdir で指定されているディレクトリの情報を収集しておく。
                 */
                BlancoValueObjectTsUtil.isVerbose = input.getVerbose();
                BlancoValueObjectTsUtil.processValueObjects(input, listClassStructures);
            }

            // 次にメタディレクトリとして指定されているディレクトリを走査
            for (int index = 0; index < fileMeta2.length; index++) {
                if (fileMeta2[index].getName().endsWith(".xml") == false) {
                    continue;
                }

                final BlancoValueObjectTsXml2TypeScriptClass xml2Class = new BlancoValueObjectTsXml2TypeScriptClass();
                xml2Class.setEncoding(input.getEncoding());
                xml2Class.setVerbose(input.getVerbose());
                xml2Class.setTargetStyleAdvanced(isTargetStyleAdvanced);
                xml2Class.setXmlRootElement(input.getXmlrootelement());
                xml2Class.setSheetLang(new BlancoCgSupportedLang().convertToInt(input.getSheetType()));
                xml2Class.setTabs(input.getTabs());
                xml2Class.setDefaultGenerateToJson(generateToJson);
                BlancoValueObjectTsClassStructure [] structures = xml2Class.process(fileMeta2[index], new File(strTarget));

                /*
                 * listClass が指定されている場合は、自動生成した
                 * クラスの一覧を収集します。
                 */
                for (int index2 = 0; createClassList && index2 < structures.length; index2++) {
                    BlancoValueObjectTsClassStructure classStructure = structures[index2];
                    if (listClassName.equals(classStructure.getName())) {
                        listClassStructure = classStructure;
                    } else {
                        listClassStructures.add(classStructure);
                    }
                }
                // 単体試験コードの自動生成機能は 0.9.1以降では削除されました。
            }

            /*
             * listClass が指定されている場合は、自動生成したクラスの一覧を
             * 保持するValueObjectを作成します。
             */
            if (createClassList) {
                if (listClassStructure == null) {
                    System.out.println("[WARN] listClass is specified but no meta file. : " + listClassName);
                    return BlancoValueObjectTsBatchProcess.END_SUCCESS;
                }

                final BlancoValueObjectTsXml2TypeScriptClass xml2Class = new BlancoValueObjectTsXml2TypeScriptClass();
                xml2Class.setEncoding(input.getEncoding());
                xml2Class.setVerbose(input.getVerbose());
                xml2Class.setTargetStyleAdvanced(isTargetStyleAdvanced);
                xml2Class.setXmlRootElement(input.getXmlrootelement());
                xml2Class.setSheetLang(new BlancoCgSupportedLang().convertToInt(input.getSheetType()));
                xml2Class.setTabs(input.getTabs());
                /* listClass では常に toJSON は生成しない */
                xml2Class.setDefaultGenerateToJson(false);
                xml2Class.processListClass(listClassStructures, listClassStructure, new File(strTarget));
            }

            return BlancoValueObjectTsBatchProcess.END_SUCCESS;
        } catch (TransformerException e) {
            throw new IOException("XML変換の過程で例外が発生しました: " + e.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean progress(final String argProgressMessage) {
        System.out.println(argProgressMessage);
        return false;
    }
}
