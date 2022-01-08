package com.yizhuoyan.mockcurrentuser;
import com.yizhuoyan.mockcurrentuser.config.Configuration;
import com.yizhuoyan.mockcurrentuser.transformer.MockCurrentUserTransformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Set;
import java.util.logging.Logger;

/**
 * <p>
 *
 * </p>
 *
 * @author jun.yi@resico.cn
 * @date 2022/1/8 12:45
 */

public class MockCurrentUserAgent {

   private final static  Logger LOGGER=Logger.getLogger(MockCurrentUserAgent.class.getName());


    public static void premain(
            String agentArgs, Instrumentation inst) {
        LOGGER.info("[Agent] In premain method");
        String path=agentArgs;
        try {
            Configuration.init(path);
        }catch (Exception e){
            e.printStackTrace();
        }
        MockCurrentUserTransformer dt = new MockCurrentUserTransformer(Configuration.loadTransformerClass());
        inst.addTransformer(dt);
    }

    private static void transformClass(
            String className, Instrumentation instrumentation) {
        Class<?> targetCls = null;
        ClassLoader targetClassLoader = null;
        // see if we can get the class using forName
        try {
            targetCls = Class.forName(className);
            targetClassLoader = targetCls.getClassLoader();
            transform(targetCls, targetClassLoader, instrumentation);
            return;
        } catch (Exception ex) {
            LOGGER.warning(String.format("Class [%s] not found with Class.forName",className));
        }
        // otherwise iterate all loaded classes and find what we want
        for(Class<?> clazz: instrumentation.getAllLoadedClasses()) {
            if(clazz.getName().equals(className)) {
                targetCls = clazz;
                targetClassLoader = targetCls.getClassLoader();
                transform(targetCls, targetClassLoader, instrumentation);
                return;
            }
        }
        throw new RuntimeException(
                "Failed to find class [" + className + "]");
    }

    private static void transform(
            Class<?> clazz,
            ClassLoader classLoader,
            Instrumentation instrumentation) {

        try {
            instrumentation.retransformClasses(clazz);
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Transform failed for: [" + clazz.getName() + "]", ex);
        }
    }
}
