/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti_2.checkcarp;

import rti_2.checkinap.ReponseCHECKINAP;
import rti_2.checkinap.RequeteCHECKINAP;
import rti_2.database.facility.MyInstruction;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author fredm
 */
public class ThreadVerifCarte extends Thread {
    private String nom;
    private Socket CSocket;
    private ServerSocket SocketCard;
    private MyInstruction sgbd;
    private int PORT_CARD = 50055;
    ThreadVerifCarte(ServerSocket s) {
        nom = "threadCarte";
        SocketCard = s;
        System.out.println("SERVER | Creation Thread Carte");
    }
    
    public void run()
    {
        RequeteCHECKINAP rep = null;
        String carte;
        while(!isInterrupted())
        {
            while(true)
            {
                try {
                    CSocket = SocketCard.accept();
                } catch (IOException ex) {
                    Logger.getLogger(ThreadVerifCarte.class.getName()).log(Level.SEVERE, null, ex);
                }
                ThreadCarteReq th = new ThreadCarteReq(CSocket);
                th.run();
            }
        }
        
             
        //guiApplication.TraceEvenements("localhost" + "#serveur_carte#thread serveur");
        
    }
    private void ConnexionServeur() {
        
        try
        {
            System.out.println("Serveur carte en attente de connexion");
            CSocket = SocketCard.accept();
            
        }catch(IOException e)
        {
            System.err.println("Erreur de port d'écoute ! ? [" + e + "]"); 
            //System.exit(1);
        }
        System.out.println("Connexion au serveur cartes établie");
    }
    private ServerSocket CreerServerSocket()
    {
        try {
            SocketCard = new ServerSocket(PORT_CARD);
        } catch (IOException ex) {
            Logger.getLogger(ThreadVerifCarte.class.getName()).log(Level.SEVERE, null, ex);
        }
        return SocketCard;
    }
}
