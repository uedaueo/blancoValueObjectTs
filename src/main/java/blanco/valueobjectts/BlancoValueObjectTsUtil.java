package blanco.valueobjectts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blanco.valueobjectts.resourcebundle.BlancoValueObjectTsResourceBundle;
import blanco.valueobjectts.valueobject.BlancoValueObjectTsClassStructure;

/**
 * Gets the list of Object created in BlancoValueObject from XML and stores it.
 *
 * Created by tueda on 15/07/05.
 */
public class BlancoValueObjectTsUtil {

    static public boolean isVerbose = false;
    static public boolean createToJson = false;

    static public Map<String, BlancoValueObjectTsClassStructure> objects = new HashMap<>();

    /**
     * Resource bundle object for blancoValueObject.
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

        /* searchTmpdir is comma separated. */
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

        // Reads information from XML-ized intermediate files.
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
             * The first step is to search all the sheets and make a list of class and package names.
             * This is because the package name is not specified when specifying a class in the PHP format definition.
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


    /**
     * Make canonical classname into Simple.
     *
     * @param argClassNameCanon
     * @return simpleName
     */
    static public String getSimpleClassName(final String argClassNameCanon) {
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
}
