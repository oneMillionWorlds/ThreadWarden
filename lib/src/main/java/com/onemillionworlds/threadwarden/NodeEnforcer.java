package com.onemillionworlds.threadwarden;

import com.jme3.scene.Spatial;
import net.bytebuddy.asm.Advice;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class NodeEnforcer{

    @Advice.OnMethodEnter
    public static void enterMethod(@Advice.This Spatial self) {
        boolean illegal = !Thread.currentThread().equals(ThreadWarden.mainThread) && ThreadWarden.nodesThatAreMainThreadReserved.contains(self);

        if(illegal){
            ThreadWardenException exception = new ThreadWardenException("Spatial " + self + " was interacted with on the wrong thread: " + Thread.currentThread());
            //log in case JME notices before something handles this exception (e.g. if we are in a future)
            exception.printStackTrace();
            throw exception;
        }
    }

}
