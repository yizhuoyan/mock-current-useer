package com.yizhuoyan.mockcurrentuser.config;

import com.yizhuoyan.mockcurrentuser.util.XUtil;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * <p>
 *
 * </p>
 *
 * @author jun.yi@resico.cn
 * @date 2022/1/8 14:19
 */
public class Configuration {

    private static final String DEFAULT_PATH=System.getProperty("user.home")+"/mockCurrentUser.properties";

    private static final Map<String, Set<String>> CLASS_METHOD_MAP=new HashMap<>();

    public static void init(String path)throws Exception{
        path= XUtil.trim2default(path,DEFAULT_PATH);
        Properties properties=new Properties();
        properties.load(Files.newBufferedReader(Paths.get(path)));
        for (String stringPropertyName : properties.stringPropertyNames()) {
            String methods = XUtil.trim2null(properties.getProperty(stringPropertyName, null));
            if(methods==null)continue;
            CLASS_METHOD_MAP.put(stringPropertyName,new HashSet<>(Arrays.asList(methods.split(","))));
        }
        System.out.println("transformer:"+CLASS_METHOD_MAP);
    }

    public static Map<String, Set<String>> loadTransformerClass(){
        return CLASS_METHOD_MAP;
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
