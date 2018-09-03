//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Paths;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class tests {
//    private static final String STRING_CONST = "\"[^\"\n]*\"";
//    private static final Pattern STRING_CONST_PATTERN = Pattern.compile(STRING_CONST);
//
//
//    private static String trims(String line) {
//
//
//        line = line.trim(); // trim the outlines spaces
//        int start = line.indexOf("\"");
//        int end = line.lastIndexOf("\"");
//        int lineCommentIndex = line.indexOf("//");
//
//
//        if (lineCommentIndex != -1 && lineCommentIndex < start || lineCommentIndex > end) {
//            line = line.substring(0, lineCommentIndex); // delete line comment
//        } else {
//            int lineBlockIndex = line.indexOf("/*");
//            if (lineBlockIndex != -1 && lineBlockIndex < start || lineBlockIndex > end) {
//                line = line.substring(0, lineBlockIndex); // delete method/func comment
//            } else {
//                int endLineBlockIndex = line.indexOf("*/");
//                if (endLineBlockIndex != -1 && endLineBlockIndex < start || endLineBlockIndex > end) {
//                    line = "";
//                } else {
//                    int middleBlockIndex = line.indexOf("*");
//                    if (middleBlockIndex == 0) {
//                        line = "";
//                    }
//                }
//
//            }
//
//
//        }
//        return line; // trim the outlines spaces
//    }
//
//
//
//    public static void main(String[] args) throws IOException {
//        String s = "     * and converts the value in RAM[8000] to binary.\n";
//        s = trims(s);
//        System.out.println(s);
//
//    }
//}
