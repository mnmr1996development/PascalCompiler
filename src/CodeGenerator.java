import java.nio.ByteBuffer;
import java.util.Stack;

public class CodeGenerator {
    private static Byte[] instructionSet;
    private static Stack<Object> stack = new Stack<>();
    private static Byte[] DA = new Byte[4000];
    private static int ip = 0;
    private static int sp = 0;

    public static void generatetoken() {
        Parser.instructions opCode;
        do {
            opCode = getOpCode();
            if (opCode == Parser.instructions.PUSH){
                sp = getValueofAddress();
                stack.push(get(sp));
            }
            else if(opCode == Parser.instructions.CVR){
                float lol = Float.valueOf(String.valueOf(stack.pop()));
                stack.push(lol);
            }
            else if(opCode == Parser.instructions.PRINT_INT){
                System.out.print(stack.pop());
            }
            else if (opCode == Parser.instructions.PRINT_BOOL){
                int lol = (int) stack.pop();
                if (lol != 1) {
                    System.out.print("False");
                } else {
                    System.out.print("True");
                }
            }
            else  if(opCode == Parser.instructions.PUSHF){
                byte[] idkWhatToCallThis = new byte[4];
                int i = 0;
                while(i < 4){
                    idkWhatToCallThis[i] = instructionSet[ip++];
                    i += 1;
                }
                float lol =  ByteBuffer.wrap(idkWhatToCallThis).getFloat();
                stack.push(lol);
            }
            else if(opCode == Parser.instructions.POP){
                pop();
            }
            else if(opCode == Parser.instructions.XCHG){
                Object val1 = stack.pop();
                Object val2 = stack.pop();
                stack.push(val1);
                stack.push(val2);
            }
            else if(opCode == Parser.instructions.GET){
                sp = (int)stack.pop();
                stack.push(get(sp));
            }
            else if(opCode == Parser.instructions.PRINT_REAL){
                Object val = stack.pop();
                if (val instanceof Integer) {
                    byte[] valArray = ByteBuffer.allocate(4).putInt((int) val).array();
                    System.out.print(ByteBuffer.wrap(valArray).getFloat());
                } else {
                    System.out.print(val);
                }
            }
            else if(opCode == Parser.instructions.PRINT_CHAR){
                System.out.print(Character.toChars((Integer) stack.pop())[0]);
            }
            else if(opCode == Parser.instructions.PRINT_NEWLINE){
                System.out.println();
            }
            else if(opCode == Parser.instructions.HALT){
                System.exit(0);
            }
            else if(opCode == Parser.instructions.EQL){
                Integer intVal2 = (Integer) stack.pop();
                Float val2 = (float) intVal2;
                Integer intVal1 = (Integer) stack.pop();
                Float val1 = (float) intVal1;
                stack.push(val1.equals(val2));
            }
            else if(opCode == Parser.instructions.NEQL){
                Integer intVal2 = (Integer) stack.pop();
                Float val2 = (float) intVal2;
                Integer intVal1 = (Integer) stack.pop();
                Float val1 = (float) intVal1;
                stack.push(!val1.equals(val2));
            }
            else if(opCode == Parser.instructions.LSS){
                Integer intVal2 = (Integer) stack.pop();
                Float val2 = (float) intVal2;
                Integer intVal1 = (Integer) stack.pop();
                Float val1 = (float) intVal1;
                stack.push(val1 < val2);
            }
            else if(opCode == Parser.instructions.LEQ){
                Integer intVal2 = (Integer) stack.pop();
                Float val2 = (float) intVal2;
                Integer intVal1 = (Integer) stack.pop();
                Float val1 = (float) intVal1;
                stack.push(val1 <= val2);
            }
            else if(opCode == Parser.instructions.GTR){
                Integer intVal2 = (Integer) stack.pop();
                Float val2 = (float) intVal2;
                Integer intVal1 = (Integer) stack.pop();
                Float val1 = (float) intVal1;
                stack.push(val1 > val2);
            }
            else if(opCode == Parser.instructions.GEQ){
                Integer intVal2 = (Integer) stack.pop();
                Float val2 = (float) intVal2;
                Integer intVal1 = (Integer) stack.pop();
                Float val1 = (float) intVal1;
                stack.push(val1 >= val2);
            }
            else if(opCode == Parser.instructions.JFALSE){
                if (stack.pop().toString().equals("false")){
                    ip = getValueofAddress();
                } else {
                    getValueofAddress();
                }
            }
            else if(opCode == Parser.instructions.JTRUE){
                if (stack.pop().toString().equals("true")){
                    ip = getValueofAddress();
                } else {
                    getValueofAddress();
                }
            }
            else if(opCode == Parser.instructions.PUT){
                put();
            }
            else if(opCode == Parser.instructions.JMP){
                ip = getValueofAddress();
            }
            else if(opCode == Parser.instructions.PUSHI){
                int val = getValueofAddress();
                stack.push(val);
            }
            else if(opCode == Parser.instructions.ADD){
                int val1 = (int) stack.pop();
                int val2 = (int) stack.pop();
                stack.push(val1 + val2);
            }
            else if(opCode == Parser.instructions.FADD){
                float val1 = (float) stack.pop();
                float val2 = (float) stack.pop();
                stack.push(val1 + val2);
            }
            else if(opCode == Parser.instructions.SUB){
                int val1 = (int) stack.pop();
                int val2 = (int) stack.pop();
                stack.push(val1 - val2);
            }
            else if(opCode == Parser.instructions.FSUB){
                float val1 = (float) stack.pop();
                float val2 = (float) stack.pop();
                stack.push(val1 - val2);
            }
            else if(opCode == Parser.instructions.MULT){
                int val1 = (int) stack.pop();
                int val2 = (int) stack.pop();
                stack.push(val1 * val2);
            }
            else if(opCode == Parser.instructions.FMULT){
                float val1 = (float) stack.pop();
                float val2 = (float) stack.pop();
                stack.push(val1 * val2);
            }
            else if(opCode == Parser.instructions.DIV){
                int val2 = (int) stack.pop();
                int val1 = (int) stack.pop();
                stack.push(val1 / val2);
            }
            else if(opCode == Parser.instructions.FDIV){
                float val2 = (float) stack.pop();
                float val1 = (float) stack.pop();
                stack.push(val1 / val2);
            }
            else if(opCode == Parser.instructions.MOD){
                int val2 = (int) stack.pop();
                int val1 = (int) stack.pop();
                stack.push(val1 % val2);
            }
            else {
                throw new Error(String.format("%s hasn't been implemented yet", opCode));
            }

        }
        while (opCode != Parser.instructions.HALT);
    }

    private static Object put() {
        Object val = stack.pop();
        sp = (int)stack.pop();


        byte[] valBytes;
        if (val instanceof Integer) {
            valBytes = ByteBuffer.allocate(4).putInt((int) val).array();
        } else {
            valBytes = ByteBuffer.allocate(4).putFloat((float) val).array();
        }

        for (byte b: valBytes) {
            DA[sp++] = b;
        }

        return val;
    }

    public static Object pop(){
        Object val = stack.pop();
        byte[] valArray = new byte[4];
        int i=0;
        while (i < 4){
            valArray[i] = instructionSet[ip++];
            i += 1;
        }
        sp = ByteBuffer.wrap(valArray).getInt();

        byte[] valBytes;
        if (val instanceof Integer) {
            valBytes = ByteBuffer.allocate(4).putInt((int) val).array();
        } else {
            valBytes = ByteBuffer.allocate(4).putFloat((float) val).array();
        }
        for (byte b: valBytes) {
            DA[sp++] = b;
        }
        return val;
    }

    public static int get(int sp) {
        byte[] valArray = new byte[4];
        for (int i = 0; i < 4; i++) {
            valArray[i] = DA[sp++];
        }

        return ByteBuffer.wrap(valArray).getInt();
    }

    public static int getValueofAddress() {
        byte[] valArray = new byte[4];
        for (int i = 0; i < 4; i++) {
            valArray[i] = instructionSet[ip++];
        }
        return ByteBuffer.wrap(valArray).getInt();
    }


    public static Parser.instructions getOpCode(){
        return Parser.instructions.values()[instructionSet[ip++]];
    }

    public static void setInstructions(Byte[] instructions) {
        CodeGenerator.instructionSet = instructions;
    }
}