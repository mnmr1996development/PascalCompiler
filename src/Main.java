import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public final class Main {

    public static void main(String[] args) {


        File file = new File("src/pascalCompiler.txt");

        try {
            ArrayList<Token> itemsFound = TokenScanner.scan(file);
            Parser.setTokenArrayListIterator(itemsFound);
            Byte[] instructionSet = Parser.parseALL();
            CodeGenerator.setInstructions(instructionSet);
            CodeGenerator.generatetoken();

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
