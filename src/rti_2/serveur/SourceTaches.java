/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti_2.serveur;

import java.net.Socket;

/**
 *
 * @author fredm
 */
public interface SourceTaches {
    
    public Runnable getTache() throws InterruptedException;
    public boolean existTaches();
    public void recordTache(Runnable r);
    public void setSocket(Socket s);
    public Socket getSocket()throws InterruptedException;
}
