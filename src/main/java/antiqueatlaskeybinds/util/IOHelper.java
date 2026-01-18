package antiqueatlaskeybinds.util;

public abstract class IOHelper {

    public static final String MARKER_EXPORT_DIRECTORY = "/atlasmarkerexports";
    public static final String MARKER_EXPORT_FILE_EXTENSION = ".markerexport";

    public static String simplifyFileName(String fileName){
        String[] splitHostName = fileName.split("/");
        return splitHostName[splitHostName.length - 1]
                .replace(".", "")
                .replace(":", "");
    }
}
