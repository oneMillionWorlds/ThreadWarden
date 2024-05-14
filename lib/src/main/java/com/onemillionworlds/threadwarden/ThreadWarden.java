package com.onemillionworlds.threadwarden;

import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Thread warden keeps track of mutations to the scene graph and ensures that they are only done on the main thread
 * IF the parent node is marked as being reserved for the main thread (which basically means it's connected to teh
 * root node)

 */
public class ThreadWarden{

    public static Thread mainThread;
    public static final Set<Object> nodesThatAreMainThreadReserved = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));


    public static void setup(Node rootNode){
        ByteBuddyAgent.install();
        mainThread = Thread.currentThread();

        for(Class<?extends Spatial> classToInstrument : new Class[]{Node.class, Geometry.class}){
            new ByteBuddy()
                    .redefine(classToInstrument)
                    .visit(Advice.to(NodeEnforcer.class).on(ElementMatchers.isMethod()
                            .and(ElementMatchers.not(ElementMatchers.isStatic())
                                    .and(ElementMatchers.not(ElementMatchers.isConstructor())))
                            .and(ElementMatchers.not(ElementMatchers.nameStartsWith("get"))
                            )))
                    .visit(Advice.to(NodeCapture.class).on(ElementMatchers.named("setParent")))
                    .make()
                    .load(
                            Node.class.getClassLoader(),
                            ClassReloadingStrategy.fromInstalledAgent());
        }



        setTreeRestricted(rootNode);
    }

    public static void reset(){
        try{
            ClassReloadingStrategy.fromInstalledAgent().reset(Node.class);
            ClassReloadingStrategy.fromInstalledAgent().reset(Geometry.class);
            nodesThatAreMainThreadReserved.clear();
            mainThread = null;
        } catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args){
        Node node = new Node("hi");
        setup(node);
        node.attachChild(new Node());  // Should throw an exception if not on the correct thread
        int a=0;
    }

    public static void updateRequirement(Spatial spatial, Node newParent){
        boolean shouldNowBeRestricted = nodesThatAreMainThreadReserved.contains(newParent);
        boolean wasPreviouslyRestricted = nodesThatAreMainThreadReserved.contains(spatial);

        if(shouldNowBeRestricted == wasPreviouslyRestricted){
            return;
        }
        if(shouldNowBeRestricted){
            setTreeRestricted(spatial);
        }else{
            setTreeNotRestricted(spatial);
        }
    }

    /**
     * Runs through the entire tree and sets the restriction state of all nodes below the given node
     * @param spatial
     */
    private static void setTreeRestricted(Spatial spatial){
        nodesThatAreMainThreadReserved.add(spatial);
        if(spatial instanceof Node){
            for(Spatial child : ((Node) spatial).getChildren()){
                setTreeRestricted(child);
            }
        }
    }

    private static void setTreeNotRestricted(Spatial spatial){
        nodesThatAreMainThreadReserved.remove(spatial);
        if(spatial instanceof Node){
            for(Spatial child : ((Node) spatial).getChildren()){
                setTreeNotRestricted(child);
            }
        }
    }

    public static void clearRequirement(Spatial spatial){
        nodesThatAreMainThreadReserved.remove(spatial);
        if(spatial instanceof Node){
            for(Spatial child : ((Node) spatial).getChildren()){
                clearRequirement(child);
            }
        }
    }
}
