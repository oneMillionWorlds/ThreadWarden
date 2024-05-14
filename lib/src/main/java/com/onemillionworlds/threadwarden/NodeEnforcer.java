package com.onemillionworlds.threadwarden;

import com.jme3.scene.Spatial;
import net.bytebuddy.asm.Advice;

public class NodeEnforcer{

    @Advice.OnMethodEnter
    public static void enterMethod(@Advice.This Spatial self) {
        boolean illegal = !Thread.currentThread().equals(ThreadWarden.mainThread) && ThreadWarden.nodesThatAreMainThreadReserved.contains(self);

        if(illegal){
            throw new ThreadWardenException("Spatial " + self + " was interacted with on the wrong thread: " + Thread.currentThread());
        }
    }

}
