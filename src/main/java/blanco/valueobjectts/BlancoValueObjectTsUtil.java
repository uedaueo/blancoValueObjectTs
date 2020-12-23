package blanco.valueobjectts;

import blanco.valueobjectts.resourcebundle.BlancoValueObjectTsResourceBundle;
import blanco.valueobjectts.task.valueobject.BlancoValueObjectTsProcessInput;
import blanco.valueobjectts.valueobject.BlancoValueObjectTsClassStructure;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * BlancoValueObject で作成されているObjectの一覧を XML から取得し，保持しておきます
 *
 * Created by tueda on 15/07/05.
 */
public class BlancoValueObjectTsUtil {

    static public boolean isVerbose = false;
    static public Map<String, BlancoValueObjectTsClassStructure> objects = new HashMap<>();

    /**
     * blancoValueObjectのリソースバンドルオブジェクト。
     */
    static private final BlancoValueObjectTsResourceBundle fBundle = new BlancoValueObjectTsResourceBundle();

    static public void processValueObjects(
            final String baseTmpdir,
            final String searchTmpdirs,
            final Map<String, BlancoValueObjectTsClassStructure> argObjects
    ) throws IOException {
        if (isVerbose) {
            System.out.println("BlancoValueObjectTsUtil : processValueObjects start !, tmpDir = " + baseTmpdir + ", serachTmpdir = " + searchTmpdirs);
        }

        if (argObjects == null) {
            throw new IllegalArgumentException(fBundle.getXml2sourceFileErr007());
        }

        /* searchTmpdir はカンマ区切り */
        List<String> searchTmpdirList = null;
        if (searchTmpdirs != null && !searchTmpdirs.equals(baseTmpdir)) {
            String[] searchTmpdirArray = searchTmpdirs.split(",");
            searchTmpdirList = new ArrayList<>(Arrays.asList(searchTmpdirArray));
        }
        if (searchTmpdirList == null) {
            searchTmpdirList = new ArrayList<>();
        }
        if (baseTmpdir != null) {
            searchTmpdirList.add(baseTmpdir);
        }

        for (String tmpdir : searchTmpdirList) {
            searchTmpdir(tmpdir.trim(), argObjects);
        }
    }

    static private void searchTmpdir(
            final String tmpdir,
            final Map<String, BlancoValueObjectTsClassStructure> argObjects
    ) {

        // XML化された中間ファイルから情報を読み込む
        final File[] fileMeta3 = new File(tmpdir
                + BlancoValueObjectTsConstants.TARGET_SUBDIRECTORY)
                .listFiles();

        if (fileMeta3 == null) {
            System.out.println("!!! NO FILES in " + tmpdir
                    + BlancoValueObjectTsConstants.TARGET_SUBDIRECTORY);
            throw new IllegalArgumentException("!!! NO FILES in " + tmpdir
                    + BlancoValueObjectTsConstants.TARGET_SUBDIRECTORY);
        }

        for (int index = 0; index < fileMeta3.length; index++) {
            if (fileMeta3[index].getName().endsWith(".xml") == false) {
                continue;
            }

            BlancoValueObjectTsXmlParser parser = new BlancoValueObjectTsXmlParser();
            parser.setVerbose(isVerbose);
            /*
             * まず始めにすべてのシートを検索して，クラス名とpackage名のリストを作ります．
             * php形式の定義書では，クラスを指定する際にpackage名が指定されていないからです．
             */
            final BlancoValueObjectTsClassStructure[] structures = parser.parse(fileMeta3[index]);

            if (structures != null ) {
                for (int index2 = 0; index2 < structures.length; index2++) {
                    BlancoValueObjectTsClassStructure structure = structures[index2];
                    if (structure != null) {
                        if (isVerbose) {
                            System.out.println("processValueObjects: " + structure.getName());
                        }
                        argObjects.put(structure.getName(), structure);
                    } else {
                        System.out.println("processValueObjects: a structure is NULL!!!");
                    }
                }
            } else {
                System.out.println("processValueObjects: structures are NULL!!!");
            }
        }
    }
}
