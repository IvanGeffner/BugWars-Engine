package instrumenter;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.tree.AbstractInsnNode.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.*;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureWriter;

import bugwars.*;

public class Main {
    static boolean DEBUG = false;

    static String packageName = "DEFAULT";

    static boolean playerCode = true;

    static Set<String> disallowedClasses, allowedClasses;
    static HashMap<String, Integer> methodBytecode;

    final static String dirPath =
    		System.getProperty("user.dir") + File.separator +
                    "instrumenter/resources" + File.separator;

    /*reads DisallowedClasses.txt and puts them in a HashSet*/
    public static void readDisallowedClasses() throws Exception{
        try {
            InputStream in = Main.class.getResourceAsStream("resources/DisallowedClasses.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            disallowedClasses = new HashSet<>();
            String newLine;
            while ((newLine = reader.readLine()) != null) {
                disallowedClasses.add(newLine);
            }
            reader.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void readAllowedClasses() throws Exception{
        InputStream in = Main.class.getResourceAsStream("resources/AllowedClasses.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        allowedClasses = new HashSet<>();
        String newLine;
        while ((newLine = reader.readLine()) != null){
            allowedClasses.add(newLine);
        }
        reader.close();
    }

    public static void readBytecodeCost() throws Exception{
        InputStream in = Main.class.getResourceAsStream("resources/BytecodeCost.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        methodBytecode = new HashMap<>();
        String newLine;
        while ((newLine = reader.readLine()) != null){
            String[] splitString = newLine.split("\\s+");
            methodBytecode.put(splitString[0], Integer.parseInt(splitString[1]));
        }
        reader.close();
    }

    public static void main(final String args[]) throws Exception {
    	File dir = new File(args[0]);
    	packageName = args[1];
        playerCode = (args[2].equals("true")); //true if we are instrumenting user code

        readDisallowedClasses();
        readAllowedClasses();
        readBytecodeCost();
        
        instrument(dir);
    }

    public static void errorDetected(String msg) {
        System.err.println(msg);
        System.exit(1);
    }
    
    private static void instrument(File dir) throws IOException {
        if (DEBUG && dir.listFiles() == null) System.err.println("Wrong address!!!!");

        for (File file : dir.listFiles()) {
            if (DEBUG) System.err.println("Instrumenting file " + file);
    		if (file.isFile()) {
    			// If file, instrument it
	            FileInputStream is = new FileInputStream(file);
	            byte[] b;

	            ClassReader cr = new ClassReader(is);
	            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
	            ClassVisitor cv = new ClassAdapter(cw);
	            cr.accept(cv, 0);
	            b = cw.toByteArray();

	            FileOutputStream fos = new FileOutputStream(file);
	            fos.write(b);
	            fos.close();
    		} else if (file.isDirectory()) {
    			// If directory, recursive call
    			instrument(file);
    		}
        }
    }
}

class ClassAdapter extends ClassVisitor implements Opcodes {

    private String newClassName;
    private String newSuperName;

    public ClassAdapter(final ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {

        if (Main.DEBUG) System.err.println("VisitMethod " + name);

        /*redirect exceptions*/
        if (exceptions != null) {
            for (int i = 0; i < exceptions.length; i++) {
                exceptions[i] = InstrumentingMethodVisitor.newClass(exceptions[i]);
            }
        }


        MethodVisitor mv = cv.visitMethod(access, name,
                InstrumentingMethodVisitor.newMethodDescReference(desc),
                InstrumentingMethodVisitor.newMethodSignature(signature), exceptions);
        return mv == null ? null : new InstrumentingMethodVisitor(mv,access,newClassName,desc,signature,exceptions);
    }

    @Override
    public void visit(
            final int version,
            final int access,
            final String name,
            final String signature,
            final String superName,
            final String[] interfaces) {

        if (Main.DEBUG) System.err.println("visit " + name);

        newClassName = InstrumentingMethodVisitor.newClass(name);
        newSuperName = InstrumentingMethodVisitor.newClass(superName);

        for (int i = 0; i < interfaces.length; i++) {
            interfaces[i] = InstrumentingMethodVisitor.newClass(interfaces[i]);
        }
        super.visit(version, access, newClassName, InstrumentingMethodVisitor.newMethodSignature(signature), newSuperName, interfaces);
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (Main.DEBUG) System.err.println("FieldVisitor");


        if (Main.playerCode && (access & Opcodes.ACC_STATIC) != 0) Main.errorDetected("Cannot use static variables.");

        return cv.visitField(access,
                name,
                InstrumentingMethodVisitor.newClassDescReference(desc),
                InstrumentingMethodVisitor.newFieldSignature(signature),
                value);
    }

    public void visitOuterClass(String owner, String name, String desc) {
        if (Main.DEBUG) System.err.println("Visit OuterClass");
        super.visitOuterClass(InstrumentingMethodVisitor.newClass(owner), name, InstrumentingMethodVisitor.newMethodSignature(desc));
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        if (Main.DEBUG) System.err.println("Visit InnerClass");
        super.visitInnerClass(InstrumentingMethodVisitor.newClass(name), InstrumentingMethodVisitor.newClass(outerName), innerName, access);
    }
}

class InstrumentingMethodVisitor extends MethodNode implements Opcodes {

    private int bytecodeCtr = 0;
    private MethodVisitor methodWriter;
    private LabelNode startLabel; //start of try-Catch

    // all the exception handlers we've seen in the code
    private final Set<LabelNode> exceptionHandlers = new HashSet<>();
    private final Set<LabelNode> tryCatchStarts = new HashSet<>();

    public InstrumentingMethodVisitor(final MethodVisitor mv,
                                      final int access,
                                      final String methodName,
                                      final String methodDesc,
                                      final String signature,
                                      final String[] exceptions) {
        super(ASM5, access, methodName, methodDesc, signature, exceptions);
        methodWriter = mv;
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        /*Visit try-catch*/
        for (Object o : tryCatchBlocks) {
            visitTryCatchBlockNode((TryCatchBlockNode) o);
        }

        for (AbstractInsnNode node : instructions.toArray()) {
            switch (node.getType()) {
                case FIELD_INSN:
                    visitFieldInsnNode((FieldInsnNode) node);
                    break;
                case INSN:
                    visitInsnNode((InsnNode) node);
                    break;
                case INVOKE_DYNAMIC_INSN:
                    visitInvokeDynamicInsnNode((InvokeDynamicInsnNode) node);
                    break;
                case LDC_INSN:
                    visitLdcInsnNode((LdcInsnNode) node);
                    break;
                case METHOD_INSN:
                    visitMethodInsnNode((MethodInsnNode) node);
                    break;
                case MULTIANEWARRAY_INSN:
                    visitMultiANewArrayInsnNode((MultiANewArrayInsnNode) node);
                    break;
                case TYPE_INSN:
                    visitTypeInsnNode((TypeInsnNode) node);
                    break;
                case VAR_INSN:
                    visitVarInsnNode((VarInsnNode) node);
                    break;
                case LABEL:
                    visitLabelNode((LabelNode) node);
                    break;
                case FRAME:
                    visitFrameNode((FrameNode) node);
                    break;
                case JUMP_INSN:
                case LOOKUPSWITCH_INSN:
                case TABLESWITCH_INSN:
                    bytecodeCtr++;
                    endOfBasicBlock(node);
                    break;
                case INT_INSN:
                    visitIntInsnNode((IntInsnNode) node);
                    break;
                case IINC_INSN:
                    bytecodeCtr++;
                    break;
            }
        }

        startLabel = new LabelNode(new Label());
        instructions.insert(startLabel);

        boolean anyTryCatch = tryCatchBlocks.size() > 0; //goes before addDebugHandler

        addDebugHandler();

        if (anyTryCatch) {
            addRobotDeathHandler();
        }

        for (Object o : localVariables) {
            visitLocalVariableNode((LocalVariableNode) o);
        }

        super.visitMaxs(0, 0);
    }


    private void addRobotDeathHandler() {
        LabelNode robotDeathLabel = new LabelNode(new Label());
        LabelNode firstTryCatch = null;
        for(AbstractInsnNode node : instructions.toArray()) {
            if(node.getType()==AbstractInsnNode.LABEL&&tryCatchStarts.contains(node)) {
                firstTryCatch = (LabelNode)node;
                break;
            }
        }
        tryCatchBlocks.add(0, new TryCatchBlockNode(firstTryCatch, robotDeathLabel, robotDeathLabel, "java/lang/VirtualMachineError"));
        instructions.add(robotDeathLabel);
        instructions.add(new FrameNode(F_FULL, 0, new Object[0], 1, new Object[]{"java/lang/VirtualMachineError"}));
        instructions.add(new InsnNode(ATHROW));
    }

    private void visitFieldInsnNode(FieldInsnNode n) {
        bytecodeCtr++;
        n.owner = newClass(n.owner);
        n.desc = newClassDescReference(n.desc);
    }

    private void visitLdcInsnNode(LdcInsnNode n) {
        bytecodeCtr++;
        if (n.cst instanceof Type) {
            n.cst = Type.getType(newClassDescReference(n.cst.toString()));
        }
    }

    private void visitInsnNode(InsnNode n) {
        bytecodeCtr++;
        switch (n.getOpcode()) {
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case RETURN:
                endOfBasicBlock(n);
                break;
            case ATHROW:
                endOfBasicBlock(n);
                break;
            case MONITORENTER:
            case MONITOREXIT:
                if (Main.playerCode) {
                    Main.errorDetected("Players cannot exit monitor!");
                }
                instructions.set(n, new InsnNode(POP));
                break;
        }
    }

    private void visitTryCatchBlockNode(TryCatchBlockNode n) {
        exceptionHandlers.add(n.handler);
        tryCatchStarts.add(n.start);
        if (n.type != null) {
            n.type = newClass(n.type);
        }
    }

    private void visitInvokeDynamicInsnNode(InvokeDynamicInsnNode n) {
        n.desc = newMethodDescReference(n.desc);
        for (int i = 0; i < n.bsmArgs.length; i++) {
            final Object arg = n.bsmArgs[i];

            if (arg instanceof Type) {
                Type t = (Type) arg;
                n.bsmArgs[i] = Type.getType(newMethodDescReference(t.getDescriptor()));
            } else if (arg instanceof Handle) {
                Handle h = (Handle) arg;

                if (Main.playerCode) {
                    checkDisallowedMethod(h.getOwner(), h.getName(), h.getDesc());
                }

                n.bsmArgs[i] = new Handle(
                        h.getTag(),
                        newClass(h.getOwner()),
                        h.getName(),
                        newMethodDescReference(h.getDesc())
                );

            }
        }
    }
    
    public void visitMethodInsnNode(MethodInsnNode node) {
        if (Main.playerCode) {
            checkDisallowedMethod(node.owner, node.name, node.desc);
        }

        String completeName = node.owner + "/" + node.name;
        if (Main.methodBytecode.containsKey(completeName)) bytecodeCtr += Main.methodBytecode.get(completeName);
        else ++bytecodeCtr;


        node.owner = newClass(node.owner);
        node.desc = newMethodDescReference(node.desc);
        endOfBasicBlock(node);
    }

    void checkDisallowedMethod(String owner, String methodName, String desc) {
        // do wait/notify monitoring
        if ((desc.equals("()V") && (methodName.equals("wait") || methodName.equals("notify") || methodName.equals("notifyAll")))
                || (methodName.equals("wait") && (desc.equals("(J)V") || desc.equals("(JI)V")))) {
            Main.errorDetected("Error: Found wait or notify methods");
        }

        if (owner.equals("java/lang/Class")) {
            // There are some methods called automatically by Java
            // that we need to enable
            if (!methodName.equals("desiredAssertionStatus")) {
                Main.errorDetected("Error: Method name is desiredAssertionStatus");
            }
        }

        if (owner.equals("java/io/PrintStream") && methodName.equals("<init>") && desc.startsWith("(Ljava/lang/String;")) {
            Main.errorDetected("Error: Using PrintStream");
        }

        //No println
        if (owner.equals("java/io/PrintStream") && (methodName.equals("println") || methodName.equals("print")) && desc.startsWith("(Ljava/lang/String;")){
            Main.errorDetected("Error: Using println/print from PrintStream");
        }

        if (owner.equals("java/lang/String") && methodName.equals("intern")) {
            Main.errorDetected("Error: Using intern from String");
        }

        if (owner.equals("java/lang/System") && (
                methodName.equals("currentTimeMillis") ||
                        methodName.equals("gc") ||
                        methodName.equals("getProperties") ||
                        methodName.equals("getSecurityManager") ||
                        methodName.equals("getenv") ||
                        methodName.equals("load") ||
                        methodName.equals("loadLibrary") ||
                        methodName.equals("mapLibraryName") ||
                        methodName.equals("nanoTime") ||
                        methodName.equals("runFinalization") ||
                        methodName.equals("runFinalizersOnExit") ||
                        methodName.equals("setProperties") ||
                        methodName.equals("setSecurityManager")
        )) {
            Main.errorDetected("Error: Using disallowed methods from System");
        }

        // We can't outlaw classes in java.lang.invoke because the JVM uses them,
        // but we can prevent the user from using them.
        if (owner.startsWith("java/lang/invoke/")) {
            Main.errorDetected("Error: Using java/lang/invoke");
        }
    }

    private void visitMultiANewArrayInsnNode(MultiANewArrayInsnNode n) {
        n.desc = newClassDescReference(n.desc);
        ++bytecodeCtr;
    }

    private void visitVarInsnNode(VarInsnNode n) {
        bytecodeCtr++;
        if (n.getOpcode() == RET)
            endOfBasicBlock(n);
    }

    private void visitIntInsnNode(IntInsnNode n) {
        if (n.getOpcode() == NEWARRAY) {
            //TODO wtf is this
            //InsnList newInsns = new InsnList();
            //newInsns.add(new InsnNode(DUP));
            //newInsns.add(new MethodInsnNode(INVOKESTATIC, "battlecode/instrumenter/inject/RobotMonitor", "sanitizeArrayIndex", "(I)I"));
            //newInsns.add(new MethodInsnNode(INVOKESTATIC, "battlecode/instrumenter/inject/RobotMonitor", "incrementBytecodesWithoutInterrupt", "(I)V"));
            //instructions.insertBefore(n, newInsns);
        } else {
            bytecodeCtr++;
        }
    }

    private void visitTypeInsnNode(TypeInsnNode n) {
        n.desc = newClass(n.desc);
        if (n.getOpcode() == ANEWARRAY) {
            //TODO again wtf
            //InsnList newInsns = new InsnList();
            //newInsns.add(new InsnNode(DUP));
            //newInsns.add(new MethodInsnNode(INVOKESTATIC, "battlecode/instrumenter/inject/RobotMonitor", "sanitizeArrayIndex", "(I)I"));
            //newInsns.add(new MethodInsnNode(INVOKESTATIC, "battlecode/instrumenter/inject/RobotMonitor", "incrementBytecodesWithoutInterrupt", "(I)V"));
            //instructions.insertBefore(n, newInsns);
        } else {
            bytecodeCtr++;
        }
    }

    private void visitLocalVariableNode(LocalVariableNode n) {
        n.desc = newClassDescReference(n.desc);
        n.signature = newFieldSignature(n.signature);
    }

    private void visitLabelNode(LabelNode n) {
        endOfBasicBlock(n);
        if (exceptionHandlers.contains(n)) bytecodeCtr += GameConstants.EXCEPTION_BYTECODE_PENALTY;
    }

    private void replaceVars(List<Object> l) {
        if (l == null)
            return;
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i) instanceof String) {
                l.set(i, newClass((String) l.get(i)));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void visitFrameNode(FrameNode n) {
        replaceVars(n.local);
        replaceVars(n.stack);
    }
    
    @SuppressWarnings("unchecked")
    public void addDebugHandler() {
        // will be injected at the end of the method
        final LabelNode debugEndLabel = new LabelNode(new Label());

        // we wrap the method in a try / catch to enable debug mode
        tryCatchBlocks.add(new TryCatchBlockNode(
                startLabel,    // start our "try" at the beginning of the method
                debugEndLabel, // end at the end of the method
                debugEndLabel, // start the "finally" at the end of the method
                null           // catch any exception for finally
        ));

        // add debug label to the end
        instructions.add(debugEndLabel);

        // create a new stack frame
        instructions.add(new FrameNode(
                F_FULL, // a full new one
                0, new Object[0], // with no local variables
                1, new Object[]{"java/lang/Throwable"} // but an exception on the stack
        ));

        // throw the exception that brought us into the catch block
        instructions.add(new InsnNode(ATHROW));

    }

    @Override
    public void visitEnd() {
        accept(methodWriter);
    }

    private void endOfBasicBlock(AbstractInsnNode n) {
        if (bytecodeCtr < 0) bytecodeCtr = -bytecodeCtr;
        else if (bytecodeCtr == 0) {
            return;
        }
        if (bytecodeCtr > 1000000000) bytecodeCtr = 1000000000;
        instructions.insertBefore(n, new LdcInsnNode(bytecodeCtr));
        instructions.insertBefore(n, new MethodInsnNode(INVOKESTATIC, "bugwars/BytecodeManager", "incBytecodes", "(I)V", false));
        bytecodeCtr = 0;
    }

    private static void checkDisallowedClasses(String s) {
        if (Main.disallowedClasses.contains(s)) Main.errorDetected("Class " + s + " is illegal.");
        int index = s.lastIndexOf('/');
        if (index == -1 || !Main.allowedClasses.contains(s.substring(0, index))) Main.errorDetected("Class " + s + " is illegal.");
    }


    static String newClass(String oldClass) {
        if (oldClass == null) return null;

        if (oldClass.charAt(0) == '[') {
            int arrayIndex = oldClass.lastIndexOf('[');
            if (oldClass.charAt(arrayIndex + 1) == 'L') {
                String extractedClassName = oldClass.substring(arrayIndex + 2, oldClass.length() - 1);
                return oldClass.substring(0, arrayIndex + 2) + newClass(extractedClassName) + ";";
            } else {
                return oldClass;
            }
        }

        if (Main.playerCode && oldClass.startsWith(Main.packageName)) return oldClass;

        if (Main.playerCode && oldClass.startsWith("java/util/Random")) Main.errorDetected("Class java/util/Random is illegal, use Math.random() instead.");

        if (Main.playerCode) checkDisallowedClasses(oldClass);

        if (Main.DEBUG) System.err.println("Input " + oldClass);
        if (oldClass.startsWith("java/util/invoke") ||
                oldClass.startsWith("java/util/jar") ||
                oldClass.startsWith("java/util/zip") ||
                oldClass.equals("java/util/Iterator") ||
                oldClass.equals("java/util/concurrent/TimeUnit")) {
            if (Main.DEBUG) System.err.println("Output " + oldClass);
            return oldClass;
        }
        if (oldClass.startsWith("java/util/") || oldClass.startsWith("java/math/")) {
            if (Main.DEBUG) System.err.println("Output " + "instrumented/" + oldClass);
            return "instrumented/" + oldClass;
        }
        if (Main.DEBUG) System.err.println("Output " + oldClass);
        return oldClass;
    }

    static String newClassDescReference(String owner) {
        if (owner == null) return null;
        if (owner.charAt(0) == 'L') return "L" + newClass(owner.substring(1, owner.length() - 1)) + ";";
        else if (owner.charAt(0) == '[') {
            int last = owner.lastIndexOf('[');
            return owner.substring(0, last+1) + newClassDescReference(owner.substring(last+1, owner.length()));
        }
        return owner;
    }

    static String newMethodDescReference(String desc) {
        String ans = "(";
        for (Type argType : Type.getArgumentTypes(desc)) {
            if (argType.getSort() == Type.ARRAY || argType.getSort() == Type.OBJECT)
                ans = ans + newClassDescReference(argType.toString());
            else
                ans = ans + argType.toString();
        }
        ans = ans + ")";
        Type argType = Type.getReturnType(desc);
        if (argType.getSort() == Type.ARRAY || argType.getSort() == Type.OBJECT)
            ans = ans + newClassDescReference(argType.toString());
        else
            ans = ans + argType.toString();
        return ans;
    }

    static String newFieldSignature(String signature) {
        if (signature == null) return null;
        NewSignatureWriter writer = new NewSignatureWriter();
        SignatureReader reader = new SignatureReader(signature);
        reader.acceptType(writer);
        return writer.toString();
    }

    static String newMethodSignature(String signature) {
        if (signature == null) return null;
        NewSignatureWriter writer = new NewSignatureWriter();
        SignatureReader reader = new SignatureReader(signature);
        reader.accept(writer);
        return writer.toString();
    }

    private static class NewSignatureWriter extends SignatureWriter {
        public void visitClassType(String name) {
            super.visitClassType(newClass(name));
        }
    }

}
