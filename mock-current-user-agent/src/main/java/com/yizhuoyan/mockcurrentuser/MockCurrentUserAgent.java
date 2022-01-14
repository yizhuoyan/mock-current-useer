package com.yizhuoyan.mockcurrentuser;
import com.yizhuoyan.mockcurrentuser.config.Configuration;
import com.yizhuoyan.mockcurrentuser.transformer.MockCurrentUserTransformer;
import com.yizhuoyan.mockcurrentuser.util.XUtil;

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
        LOGGER.info("MockCurrentUserAgent Working");
        agentArgs=XUtil.trim2null(agentArgs);
        if(agentArgs!=null){
            String[] classMethods = agentArgs.split(";");
            Configuration.addTransformerClass(classMethods);
        }
        MockCurrentUserTransformer dt = new MockCurrentUserTransformer(Configuration.loadTransformerClass());
        inst.addTransformer(dt);
    }


}
