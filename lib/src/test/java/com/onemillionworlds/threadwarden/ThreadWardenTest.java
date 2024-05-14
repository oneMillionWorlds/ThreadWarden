package com.onemillionworlds.threadwarden;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import com.jme3.scene.Node;

class ThreadWardenTest{

    static ExecutorService executorService;
    @BeforeAll
    static void setup(){
        executorService = newSingleThreadDaemonExecutor();
    }

    @AfterAll
    static void tearDown(){
        executorService.shutdown();
    }


    @Test
    void attachmentsBeforeInitialisationStillProtect(){
        try{
            Node rootNode = new Node("root");
            Node child = new Node("child");

            rootNode.attachChild(child);

            ThreadWarden.setup(rootNode);

            Future<Node> legalThreading = executorService.submit(() -> {
                //this is fine
                Node child2 = new Node("child2");
                Node child3 = new Node("child3");
                child2.attachChild(child3);
                return child2;
            });

            try{
                legalThreading.get();
            } catch(Exception e){
                fail("This should not have thrown an exception", e);
            }

            Future<Void> illegalThreading = executorService.submit(() -> {
                //this is fine
                Node child4= new Node("child4");
                child.attachChild(child4);
                return null;
            });

            try{
                illegalThreading.get();
                fail("This should have thrown an exception");
            } catch(Exception e){
                if(!(e.getCause() instanceof ThreadWardenException)){
                    fail("This should have thrown a ThreadWardenException", e);
                }
            }

        } finally{
            ThreadWarden.reset();
        }

    }

    @Test
    void detachmentReleasesProtection(){
        try{
            Node rootNode = new Node("root");

            ThreadWarden.setup(rootNode);

            Node child = new Node("child");
            rootNode.attachChild(child);
            child.removeFromParent();

            Future<Void> legalThreading = executorService.submit(() -> {
                //this is fine

                Node child2 = new Node("child2");
                child.attachChild(child2);
                return null;
            });

            try{
                legalThreading.get();
            } catch(Exception e){
                fail("This should not have thrown an exception", e);
            }

            rootNode.attachChild(child);
        } finally{
            ThreadWarden.reset();
        }

    }

    public static ExecutorService newSingleThreadDaemonExecutor() {
        return Executors.newSingleThreadExecutor(daemonThreadFactory());
    }

    public static ThreadFactory daemonThreadFactory(){
        return r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        };
    }

}