package pset1;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

public class GraphGenerator {
    public CFG createCFG(String className) throws ClassNotFoundException {
        CFG cfg = new CFG();
        JavaClass jc = Repository.lookupClass(className);
        ClassGen cg = new ClassGen(jc);
        ConstantPoolGen cpg = cg.getConstantPool();
        
        for (Method m: cg.getMethods()) {
            MethodGen mg = new MethodGen(m, cg.getClassName(), cpg);
            InstructionList il = mg.getInstructionList();
            InstructionHandle[] handles = il.getInstructionHandles();
            for (InstructionHandle ih: handles) {
                int position = ih.getPosition();
                cfg.addNode(position, m, jc);
                Instruction inst = ih.getInstruction();

                if (inst instanceof BranchInstruction bi) {
                    InstructionHandle target = bi.getTarget();
                    int targetPos = target.getPosition();
                    cfg.addEdge(position, targetPos, m, jc);
                }

                InstructionHandle next = ih.getNext();
                if (next != null && !(inst instanceof ReturnInstruction)) {
                    int nextPos = next.getPosition();
                    cfg.addEdge(position, nextPos, m, jc);
                }
                if (inst instanceof INVOKESTATIC invoke) {
                    String targetClassName = invoke.getClassName(cpg);
                    String targetMethodName = invoke.getMethodName(cpg);
                    String targetSig = invoke.getSignature(cpg);

                    JavaClass targetClass = Repository.lookupClass(targetClassName);
                    for (Method tm : targetClass.getMethods()) {
                        if (tm.getName().equals(targetMethodName) && tm.getSignature().equals(targetSig)) {
                            MethodGen targetMg = new MethodGen(tm, targetClassName, new ConstantPoolGen(targetClass.getConstantPool()));
                            InstructionHandle[] targetHandles = targetMg.getInstructionList().getInstructionHandles();
                            if (targetHandles.length > 0) {
                                int calleeStartPos = targetHandles[0].getPosition();
                                cfg.addEdge(position,  m,  jc, calleeStartPos, tm, targetClass);
                            }
                        }
                    }
                }
            }
        }
        System.out.println(cfg);
        return cfg;
    }

    public CFG createCFGWithMethodInvocation(String className) throws ClassNotFoundException {
        // your code goes here
	return null; // dummy
    }
    
    public static void main(String[] a) throws ClassNotFoundException {
        GraphGenerator gg = new GraphGenerator();
        CFG psetC = gg.createCFG("pset1.C"); // example invocation of createCFG
        CFG psetD = gg.createCFGWithMethodInvocation("pset1.D"); // example invocation of createCFGWithMethodInovcation
        System.out.print(psetD.isReachable("main","pset1.D", "foo", "pset1.D"));
    }
}
