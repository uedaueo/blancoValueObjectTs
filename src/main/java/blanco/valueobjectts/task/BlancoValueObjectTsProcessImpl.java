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
import blanco.valueobject.BlancoValueObjectUtil;
import blanco.valueobjectts.*;
import blanco.valueobjectts.message.BlancoValueObjectTsMessage;
import blanco.valueobjectts.task.valueobject.BlancoValueObjectTsProcessInput;
import blanco.valueobjectts.valueobject.BlancoValueObjectTsClassStructure;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlancoValueObjectTsProcessImpl implements BlancoValueObjectTsProcess {

    /**
     * A message.
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
             * Determines the newline code.
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
             * Processes targetdir and targetStyle.
             * Sets the storage location for the generated code.
             * targetstyle = blanco:
             *  Creates a main directory under targetdir.
             * targetstyle = maven:
             *  Creates a main/java directory under targetdir.
             * targetstyle = free:
             *  Creates a directory using targetdir as is.
             *  However, if targetdir is empty, the default string (blanco) is used.
             * by tueda, 2019/08/30
             */
            String strTarget = input.getTargetdir();
            String style = input.getTargetStyle();
            // Always true when passing through here.
            boolean isTargetStyleAdvanced = true;
            if (style != null && BlancoValueObjectTsConstants.TARGET_STYLE_MAVEN.equals(style)) {
                strTarget = strTarget + "/" + BlancoValueObjectTsConstants.TARGET_DIR_SUFFIX_MAVEN;
            } else if (style == null ||
                    !BlancoValueObjectTsConstants.TARGET_STYLE_FREE.equals(style)) {
                strTarget = strTarget + "/" + BlancoValueObjectTsConstants.TARGET_DIR_SUFFIX_BLANCO;
            }
            /* If style is free, uses targetdir as is. */
            if (input.getVerbose()) {
                System.out.println("/* tueda */ TARGETDIR = " + strTarget);
            }

            boolean generateToJson = input.getGenerateToJson();

            // Creates a temporary directory.
            new File(input.getTmpdir()
                    + BlancoValueObjectTsConstants.TARGET_SUBDIRECTORY).mkdirs();

            new BlancoValueObjectTsMeta2Xml().processDirectory(fileMetadir, input
                    .getTmpdir()
                    + BlancoValueObjectTsConstants.TARGET_SUBDIRECTORY);

            // Generates ValueObject from XML-ized meta file.
            // Scans the temporary folder first.
            final File[] fileMeta2 = new File(input.getTmpdir()
                    + BlancoValueObjectTsConstants.TARGET_SUBDIRECTORY)
                    .listFiles();

        /*
         * First, searches all the sheets and makes a list of structures from the class names.
         * The reason is that in the PHP-style definitions, the package name is not specified when specifying a class.
         *  In TypeScript, the object placement directory has to be searched from the structure, so a list of package names is not sufficient.
         */
            BlancoValueObjectTsUtil.isVerbose = input.getVerbose();
            BlancoValueObjectTsUtil.processValueObjects(input.getTmpdir(), input.getSearchTmpdir(), BlancoValueObjectTsUtil.objects);

            /*
             * If listClass is specified, prepares to create a ValueObject that will hold the list of auto-generated classes.
             */
            boolean createClassList = false;
            String listClassName = input.getListClass();
            BlancoValueObjectTsClassStructure listClassStructure = null;
            List<BlancoValueObjectTsClassStructure> listClassStructures = new ArrayList<>();
            if (listClassName != null && listClassName.length() > 0) {
                createClassList = true;
            }

            // Next, scans the directory specified as the meta directory.
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
                 * If listClass is specified, it will collect a list of auto-generated class.
                 */
                for (int index2 = 0; createClassList && index2 < structures.length; index2++) {
                    BlancoValueObjectTsClassStructure classStructure = structures[index2];
                    if (listClassName.equals(classStructure.getName())) {
                        listClassStructure = classStructure;
                    } else {
                        listClassStructures.add(classStructure);
                    }
                }
            }

            /*
             * If listClass is specified, it will create a ValueObject that holds the list of auto-generated classes.
             */
            if (createClassList) {
                if (listClassStructure == null) {
                    System.out.println("[WARN] listClass is specified but no meta file. : " + listClassName);
                    return BlancoValueObjectTsBatchProcess.END_SUCCESS;
                }

                // Collects structure information from listTmpdir.
                Map<String, BlancoValueObjectTsClassStructure> searchListStructures = new HashMap<>();
                BlancoValueObjectTsUtil.processValueObjects(null, input.getListTmpdir(), searchListStructures);
                listClassStructures.addAll(searchListStructures.values());

                final BlancoValueObjectTsXml2TypeScriptClass xml2Class = new BlancoValueObjectTsXml2TypeScriptClass();
                xml2Class.setEncoding(input.getEncoding());
                xml2Class.setVerbose(input.getVerbose());
                xml2Class.setTargetStyleAdvanced(isTargetStyleAdvanced);
                xml2Class.setXmlRootElement(input.getXmlrootelement());
                xml2Class.setSheetLang(new BlancoCgSupportedLang().convertToInt(input.getSheetType()));
                xml2Class.setTabs(input.getTabs());
                /* listClass does not always generate toJSON. */
                xml2Class.setDefaultGenerateToJson(false);
                xml2Class.processListClass(listClassStructures, listClassStructure, new File(strTarget));
            }

            return BlancoValueObjectTsBatchProcess.END_SUCCESS;
        } catch (TransformerException e) {
            throw new IOException("An exception has occurred during the XML conversion process: " + e.toString());
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
