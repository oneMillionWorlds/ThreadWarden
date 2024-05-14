package com.onemillionworlds.threadwarden;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import net.bytebuddy.asm.Advice;

public class NodeCapture{


    /**
     * Called after the parent is set
     * @param self
     */
    @Advice.OnMethodExit
    public static void exitMethod(@Advice.This Spatial self) {
        Node newParent = self.getParent();
        if(newParent == null){
            ThreadWarden.clearRequirement(self);
        }else{

        }
    }
}
