package com.yizhuoyan.mockcurrentuser.config;

import com.yizhuoyan.mockcurrentuser.MockCurrentUserAgent;
import com.yizhuoyan.mockcurrentuser.util.XUtil;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

/**
 * <p>
 *
 * </p>
 *
 * @author jun.yi@resico.cn
 * @date 2022/1/8 14:19
 */
public class Configuration {
    private final static Logger LOGGER=Logger.getLogger(Configuration.class.getName());

    private static final String DEFAULT_PATH=System.getProperty("user.home")+"/mockCurrentUser.properties";

    private static final Map<String, Set<String>> CLASS_METHOD_MAP=new HashMap<>();

    static {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void init()throws Exception{
        Properties properties=new Properties();
        Path path = Paths.get(DEFAULT_PATH);
        if(!Files.exists(path)){
            return;
        }
        properties.load(Files.newBufferedReader(path));
        for (String stringPropertyName : properties.stringPropertyNames()) {
            String methods = XUtil.trim2null(properties.getProperty(stringPropertyName, null));
            if(methods==null)continue;
            addTransformerClass(stringPropertyName,methods);
        }
        LOGGER.info("MockCurrentUserAgent init transformer"+CLASS_METHOD_MAP);
    }

    public static Map<String, Set<String>> loadTransformerClass(){
        return CLASS_METHOD_MAP;
    }

    public static void addTransformerClass(String className,String methodNames){
        Set<String> methodSet = CLASS_METHOD_MAP.get(className);
        if(methodSet==null){
            methodSet=new HashSet<>();
            CLASS_METHOD_MAP.put(className,methodSet);
        }
        methodSet.addAll(Arrays.asList(methodNames.split(",")));
    }
    public static void addTransformerClass(String... classMethods){
        for (String classMethod : classMethods) {
            int methodAt = classMethod.lastIndexOf('.');
            String className=classMethod.substring(0,methodAt);
            String methodNames=classMethod.substring(methodAt+1);
            addTransformerClass(className,methodNames);
        }
    }


    public static String loadSourceCode(String path)throws Exception{
        InputStream resourceAsStream = Configuration.class.getResourceAsStream(path);
        StringBuilder result=new StringBuilder();
        try(BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }
            return result.toString();
        }
    }




}
