/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti_2.serveur;
import java.net.Socket;
import java.util.*;
/**
 *
 * @author fredm
 */
public class ListeTaches implements SourceTaches {
    
    private LinkedList listeTaches;
    private Socket CSocket;
    public ListeTaches()
    {
        listeTaches = new LinkedList();
    }
    public synchronized Runnable getTache() throws InterruptedException
    {
        System.out.println("getTache avant wait");
        while(!existTaches()) wait();
        return (Runnable) listeTaches.remove();
    }
    public synchronized boolean existTaches()
    {
        return !listeTaches.isEmpty();
    }
    public synchronized void recordTache(Runnable r)
    {
        listeTaches.addLast(r);
        System.out.println("ListeTaches : tache dans la file");
        notify();
    }
    public synchronized void setSocket(Socket s)
    {
        System.out.println("SetSocket de " + s);
        CSocket = s;
        notify();
    }
    public synchronized Socket getSocket() throws InterruptedException
    {
        while(CSocket == null)
        {
            System.out.println("Thread en attente");
            wait();
        }
        System.out.println("getSocket : soc : " + CSocket);
        return CSocket;
    }
    public synchronized boolean SocketNull()
    {
        if(CSocket == null)
            return true;
        return false;
    }
}
