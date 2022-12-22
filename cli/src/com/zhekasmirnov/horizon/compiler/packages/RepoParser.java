package com.zhekasmirnov.horizon.compiler.packages;

import com.zhekasmirnov.horizon.compiler.util.XMLParser;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RepoParser {
    @SuppressWarnings("unused")
    private static final boolean DEBUG = true;
    public static final String KEY_ARCH = "arch";
    public static final String KEY_DEPENDS = "depends";
    public static final String KEY_DESC = "description";
    public static final String KEY_FILESIZE = "filesize";
    public static final String KEY_LOCAL_FILE_NAME = "file";
    public static final String KEY_PACKAGE = "package";
    public static final String KEY_PACKAGE_NAME = "name";
    public static final String KEY_REPLACES = "replaces";
    public static final String KEY_SIZE = "size";
    public static final String KEY_STATUS = "status";
    public static final String KEY_VERSION = "version";
    @SuppressWarnings("unused")
    private static final String TAG = "RepoParser";

    public List<PackageInfo> parseRepoXml(String str) {
        XMLParser xMLParser;
        Document domElement;
        String replaceMacro = RepoUtils.replaceMacro(str);
        ArrayList<PackageInfo> arrayList = new ArrayList<>();
        if (replaceMacro == null || (domElement = (xMLParser = new XMLParser()).getDomElement(replaceMacro)) == null) {
            return arrayList;
        }
        NodeList elementsByTagName = domElement.getElementsByTagName("package");
        for (int i = 0; i < elementsByTagName.getLength(); i++) {
            Element element = (Element) elementsByTagName.item(i);
            PrintStream printStream = System.out;
            printStream.println("RepoParser pkg [ " + xMLParser.getValue(element, "name") + " ][ " + xMLParser.getValue(element, "size") + "]");
            int intValue = xMLParser.getValue(element, "size").length() > 0 ? Integer.valueOf(xMLParser.getValue(element, "size").replaceAll("@SIZE@", "0")).intValue() : 0;
            int intValue2 = xMLParser.getValue(element, "filesize").length() > 0 ? Integer.valueOf(xMLParser.getValue(element, "filesize").replaceAll("@SIZE@", "0")).intValue() : intValue;
            if (RepoUtils.isContainsPackage(arrayList, xMLParser.getValue(element, "name"), xMLParser.getValue(element, "version"))) {
                PrintStream printStream2 = System.out;
                printStream2.println("RepoParserskip exists pkg" + xMLParser.getValue(element, "name"));
            } else {
                PackageInfo packageInfo = new PackageInfo(xMLParser.getValue(element, "name"), xMLParser.getValue(element, "file"), intValue, intValue2, xMLParser.getValue(element, "version"), xMLParser.getValue(element, "description"), xMLParser.getValue(element, "depends"), xMLParser.getValue(element, "arch"), xMLParser.getValue(element, "replaces"));
                arrayList.add(packageInfo);
                PrintStream printStream3 = System.out;
                printStream3.println("RepoParser added pkg = " + packageInfo.getName());
            }
        }
        return arrayList;
    }
}
