import java.util.ArrayList;
import java.util.Vector;
public final class SymbolTable {
    //didn't have time to get this working properly so i erased it
    //but my essential thinking was to append a 0 at the end every time
    //you open a scope larger them te number in the level tracker
    //otherwise just levelTracker += 1 then every time scope is closed
    //do change add 1 to the value at level then decrease the level
    //then check is level vectors are equivalent
    public static ArrayList<Integer> level = new ArrayList<Integer>();

    static{
        level.add(0);
    }
    public int levelTracker = 0;

    void openScope(){
        if(levelTracker-2 > level.size()){
            level.add(0);
            levelTracker +=1;
        }
    }

    void closeScope(){
        int togoto = (int)level.get(levelTracker);
        Vector templevel = new Vector();
        for(int i = 0; i <togoto; i++){

        }

    }


    static class Scope {
        Symbol[] symbolTable = new Symbol[300];
        Scope next = null;
    }
    private static Scope headerScope = new Scope();
    static int hashingfunction(String symbolName) {
        int h = 0;
        for (int i = 0; i < symbolName.length(); i++) {
            h = h + h + symbolName.charAt(i);
        }
        h = h % 300;
        return h;
    }

    public static void insert(Symbol symbol) {
        int hashValue = hashingfunction(symbol.getName());

        Symbol senior = headerScope.symbolTable[hashValue];
        if (senior == null) {
            headerScope.symbolTable[hashValue] = symbol;
        } else {
            while (senior.next != null) {
                senior = senior.next;
            }
            senior.next = symbol;
        }
    }

    public static Symbol lookup(String symbolName) {
        int hashValue = hashingfunction(symbolName);
        Symbol senior = headerScope.symbolTable[hashValue];
        Scope scopeIndex = headerScope;
        while (scopeIndex != null) {
            while (senior != null) {
                if (senior.getName().equals(symbolName)) {
                    return senior;
                }
                senior = senior.next;
            }
            scopeIndex = scopeIndex.next;
        }
        return null;
    }



}