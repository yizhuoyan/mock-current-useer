package com.yizhuoyan.mockcurrentuser.transformer;

import com.yizhuoyan.mockcurrentuser.config.Configuration;
import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * <p>
 *
 * </p>
 *
 * @author jun.yi@resico.cn
 * @date 2022/1/8 12:53
 */

public class MockCurrentUserTransformer implements ClassFileTransformer {

    private Map<String, Set<String>> targetClassMethodMap;

    public MockCurrentUserTransformer(Map<String, Set<String>> targetClassMethodMap) {
        this.targetClassMethodMap = targetClassMethodMap;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className
                .replaceAll("/", ".");

        if (!isTargetClass(className, loader)) {
            return null;
        }
        Set<String> methods = targetClassMethodMap.get(className);
        if (methods == null || methods.isEmpty()) {
            return null;
        }
        try {
            System.out.println(className);
            return this.doTransformForClass(className, methods);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] doTransformForClass(String targetClassName, Set<String> methods) throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        CtClass cc = classPool.get(targetClassName);

        addThreadLocalField(cc);

        addMethodLoadValueFormRequest(cc);

        addMethodMockCurrentUser(cc);

        try {
            for (String method : methods) {
                this.doTransformForMethod(cc, method);
            }
            return cc.toBytecode();
        } finally {
            cc.detach();
        }
    }

    private void addThreadLocalField(CtClass cc) throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        CtField innerClassField = new CtField(classPool.get(ThreadLocal.class.getName()), "THREAD_LOCAL_MOCK_CURRENT_USER", cc);
        innerClassField.setModifiers(Modifier.STATIC | Modifier.PRIVATE | Modifier.FINAL);
        cc.addField(innerClassField, "new ThreadLocal();");
    }

    private void addMethodLoadValueFormRequest(CtClass cc) throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        CtMethod ctMethod = new CtMethod(classPool.get(String.class.getName()), "loadValueFormRequest", new CtClass[]{
                classPool.get(Object.class.getName()), classPool.get(String.class.getName()), classPool.get(String.class.getName())
        }, cc);
        ctMethod.setModifiers(Modifier.STATIC | Modifier.PRIVATE);
        String body = Configuration.loadSourceCode("/loadValueFormRequest");
        ctMethod.setBody(body);
        cc.addMethod(ctMethod);
    }

    private void addMethodMockCurrentUser(CtClass cc) throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        CtMethod ctMethod = new CtMethod(classPool.get(Object.class.getName()), "mockCurrentUser", new CtClass[]{
                classPool.get(Object.class.getName())
        }, cc);
        ctMethod.setModifiers(Modifier.STATIC | Modifier.PRIVATE);
        String body = Configuration.loadSourceCode("/mockCurrentUser");
        ctMethod.setBody(body);
        cc.addMethod(ctMethod);
    }


    private void doTransformForMethod(CtClass cc, String method) throws Exception {
        CtMethod m = cc.getDeclaredMethod(method);
        CtClass returnType = m.getReturnType();
        StringBuilder beforeCode = new StringBuilder();
        beforeCode.append("Object mock= mockCurrentUser(").append(returnType.getName()).append(".class);\n");
        beforeCode.append("if(mock!=null){")
                .append("return (").append(returnType.getName()).append(")mock;}");
        m.insertBefore(beforeCode.toString());
    }


    private boolean isTargetClass(String className, ClassLoader loader) {
        return targetClassMethodMap.containsKey(className);
    }
}