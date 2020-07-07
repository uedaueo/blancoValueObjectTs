/*
 * blanco Framework
 * Copyright (C) 2004-2020 IGA Tosiki
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
package blanco.valueobjectts;

import blanco.valueobjectts.task.BlancoValueObjectTsProcessImpl;
import blanco.valueobjectts.task.valueobject.BlancoValueObjectTsProcessInput;
import org.junit.Test;

import java.io.IOException;

/**
 * Kotlin言語用の生成試験。
 *
 * @author IGA Tosiki
 * @author tueda
 */
public class BlancoValueObjectTsTest {

    @Test
    public void testBlancoValueObjectTs() {
        BlancoValueObjectTsProcessInput input = new BlancoValueObjectTsProcessInput();
        input.setMetadir("meta/objects");
        input.setEncoding("UTF-8");
        input.setSheetType("php");
        input.setTmpdir("tmpTest");
        input.setTargetdir("sample/blanco");
        input.setTargetStyle("maven");
        input.setTabs(2);
        input.setVerbose(true);
        input.setLineSeparator("CR");

        BlancoValueObjectTsProcessImpl imple = new BlancoValueObjectTsProcessImpl();
        try {
            imple.execute(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateListClass() {
        BlancoValueObjectTsProcessInput input = new BlancoValueObjectTsProcessInput();
        input.setMetadir("meta/objects");
        input.setEncoding("UTF-8");
        input.setSheetType("php");
        input.setTmpdir("tmpTest");
        input.setTargetdir("sample/blanco");
        input.setTargetStyle("maven");
        input.setTabs(2);
        input.setVerbose(true);
        input.setListClass("ListClassSample");
        input.setGenerateToJson(true);

        BlancoValueObjectTsProcessImpl imple = new BlancoValueObjectTsProcessImpl();
        try {
            imple.execute(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
