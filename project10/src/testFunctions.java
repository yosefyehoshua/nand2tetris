//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.StringTokenizer;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class testFunctions {
//
//    protected static boolean inCommentScope = false;
//
//    public static String noBlockComments(String strIn) {
//
//        int startIndex = strIn.indexOf("/*");
//
//        if (startIndex == -1) return strIn;
//
//        String result = strIn;
//
//        int endIndex = strIn.indexOf("*/");
//
//        while (startIndex != -1) {
//
//            if (endIndex == -1) {
//
//                return strIn.substring(0, startIndex - 1);
//
//            }
//            result = result.substring(0, startIndex) + result.substring(endIndex + 2);
//
//            startIndex = result.indexOf("/*");
//            endIndex = result.indexOf("*/");
//        }
//
//        return result;
//    }
//
//
//    /**
//     * delete comments and leading and trailing white spaces from a given line,
//     * and updates the inCommentScope accordingly.
//     * @param line - string of the line currently read
//     * @return line without comments and leading and trailing white spaces
//     */
////    private static String trimCommentsAndSpaces(String line){
////        int charIndex = line.indexOf("//");
////        if (charIndex != -1){
////            line = line.substring(0, charIndex); // delete line comment
////        } else {
////            charIndex = line.indexOf("/*");
////            if (charIndex != -1) {
////                line = line.substring(0, charIndex); // delete method/func comment
////                inCommentScope = true;
////            } else {
////                charIndex = line.indexOf("*/");
////                if (charIndex != -1) {
////                    inCommentScope = false;
////                    line = "";
////                }
////            }
////        }
////        return line.trim(); // trim the outlines spaces
////    }
//    //constant for type
//    public final static int KEYWORD = 1;
//    public final static int SYMBOL = 2;
//    public final static int IDENTIFIER = 3;
//    public final static int INT_CONST = 4;
//    public final static int STRING_CONST = 5;
//
//    //constant for keyword
//    public final static int CLASS = 10;
//    public final static int METHOD = 11;
//    public final static int FUNCTION = 12;
//    public final static int CONSTRUCTOR = 13;
//    public final static int INT = 14;
//    public final static int BOOLEAN = 15;
//    public final static int CHAR = 16;
//    public final static int VOID = 17;
//    public final static int VAR = 18;
//    public final static int STATIC = 19;
//    public final static int FIELD = 20;
//    public final static int LET = 21;
//    public final static int DO = 22;
//    public final static int IF = 23;
//    public final static int ELSE = 24;
//    public final static int WHILE = 25;
//    public final static int RETURN = 26;
//    public final static int TRUE = 27;
//    public final static int FALSE = 28;
//    public final static int NULL = 29;
//    public final static int THIS = 30;
//
//    private static Pattern tokenPatterns;
//
//    private static String keyWordReg;
//    private static String totReg;
//
//
//    private static String symbolReg;
//    private static String intReg;
//    private static String strReg;
//    private static String idReg;
//
//    private static HashMap<String,Integer> keyWordMap = new HashMap<String, Integer>();
//    private static HashSet<Character> opSet = new HashSet<Character>();
//
//    static {
//
//        keyWordMap.put("class",CLASS);keyWordMap.put("constructor",CONSTRUCTOR);keyWordMap.put("function",FUNCTION);
//        keyWordMap.put("method",METHOD);keyWordMap.put("field",FIELD);keyWordMap.put("static",STATIC);
//        keyWordMap.put("var",VAR);keyWordMap.put("int",INT);keyWordMap.put("char",CHAR);
//        keyWordMap.put("boolean",BOOLEAN);keyWordMap.put("void",VOID);keyWordMap.put("true",TRUE);
//        keyWordMap.put("false",FALSE);keyWordMap.put("null",NULL);keyWordMap.put("this",THIS);
//        keyWordMap.put("let",LET);keyWordMap.put("do",DO);keyWordMap.put("if",IF);
//        keyWordMap.put("else",ELSE);keyWordMap.put("while",WHILE);keyWordMap.put("return",RETURN);
//
//        opSet.add('+');opSet.add('-');opSet.add('*');opSet.add('/');opSet.add('&');opSet.add('|');
//        opSet.add('<');opSet.add('>');opSet.add('=');
//    }
//
//private static void initRegs(String s){
//
//        keyWordReg = "static|void|method|var|constructor|false|this|do|while|int|boolean|field|null|else|function|char|true|let|class|if|return|";
//        symbolReg = "[\\&\\*\\+\\(\\)\\.\\/\\,\\-\\]\\;\\~\\}\\|\\{\\>\\=\\[\\<]";
//        intReg = "\\d+";
//        strReg = "\"[^\"]*\"";
//        idReg = "[\\w_]+";
//        totReg = keyWordReg + symbolReg + "|" + intReg + "|" + strReg + "|" + idReg;
//
//
//        tokenPatterns = Pattern.compile(totReg);
//        Matcher m = tokenPatterns.matcher(s);
//        while (m.find()) {
//            if (!m.group().equals("")) {
//                System.out.println(m.group());
//            }
//
//        }
//
//    }
//
//
//
//    public static void main(String[] args) {
//        String s = "method void    foo(){ ; ";
//        //s = noBlockComments(s);
//        //System.out.println(s);
//        initRegs(s);
//
//
//
//    }
//
//}