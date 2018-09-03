import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A tool to write VM commands into a given .vm files
 */
public class VMWriter {

    /** The writer to the output file */
    private PrintWriter writer = null;

    /**
     * Initializes a new VM writer
     * @param outputPath The path of the output file
     * @throws IOException in case of an IO error
     */
    public VMWriter(Path outputPath) throws IOException{
        writer = new PrintWriter(Files.newBufferedWriter(outputPath));
    }

    /**
     * Closes the VM writer
     */
    public void close(){
        writer.close();
    }

    /**
     * @param name refers to a specific segment
     * @return The name of the referred segment
     */
    private String translateSegment(String name){
        switch(name){
            case "var" :
                return "local";
            case "field":
                return "this";
            case "arg":
                return "argument";

            case "static": case "local":case "temp":case "this":case "that":case "argument":case
                    "constant":case "pointer":
                return name;
            default:
                System.out.println("unknown segment name");
                System.exit(1);
                return null;
        }
    }

    /**
     * @param command refers to a specific arithmetic operation
     * @return The operation referred to by 'command'
     */
    private String translateArithmetic(String command){
        switch(command){
            case "<" :
                return "lt";
            case ">" :
                return "gt";
            case "=":
                return "eq";
            case "+":
                return "add";
            case "neg":
                return command;
            case "~" :
                return "not";
            case "|" :
                return "or";
            case "&" :
                return "and";
            case "-":
                return "sub";
            default:
                System.out.println("unknown command");
                System.exit(1);
                return null;
        }
    }





















    /**
     * Writes a push command
     * @param segment The segment to push from
     * @param index The index in the segment
     */
    public void writePush(String segment, int index){
        writer.println("push "+translateSegment(segment)+" "+index);
        if (Objects.equals("push " + translateSegment(segment) + " " + index, "push this 0")){
            //System.out.println("push "+translateSegment(segment)+" "+index);
        }

    }

    /**
     * Writes a pop command
     * @param segment The segment to push from
     * @param index The index in the segment
     */
    public void writePop(String segment, int index){
        writer.println("pop "+translateSegment(segment)+" "+index);

    }

    /**
     * Writes an arithmetic command
     * @param command The command to write
     */
    public void writeArithmetic(String command){
        writer.println(translateArithmetic(command));
    }

    /**
     * Writes a label
     * @param label The name of the label
     */
    public void writeLabel(String label){
        writer.println("label "+label);
        if (label.equals("IF_TRUE0")) {
            System.out.println("here");
        }
    }

    /**
     * Writes a go-to command
     * @param label The name of the label to go to
     */
    public void writeGoto(String label){
        writer.println("goto "+label);
        //System.out.println("goto " + label);
    }

    /**
     * Writes an if-goto command
     * @param label The name of the label to go to
     */
    public void writeIf(String label){
        writer.println("if-goto "+label);
    }

    /**
     * Writes a call command
     * @param name The name of the function to call
     * @param nArgs The number of arguments the function receives
     */
    public void writeCall(String name, int nArgs){
        writer.println("call "+name+" "+nArgs);
    }

    /**
     * Writes a function command
     * @param name The name of the function
     * @param nLocals The number of local arguments in the function
     */
    public void writeFunction(String name, int nLocals){
        writer.println("function "+name+" "+nLocals);
    }

    /**
     * Writes a return command
     */
    public void writeReturn(){
        writer.println("return");
    }
}
