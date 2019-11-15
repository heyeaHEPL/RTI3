/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti_2.serveur;

import rti_2.checkcarp.Serveur_Carte;
import java.net.*;
import java.io.*;
import java.util.Properties;
import rti_2.checkinap.requetereponse.ConsoleServeur;
import rti_2.checkinap.requetereponse.Requete;
/**
 *
 * @author fredm
 */

public class ThreadServeur extends Thread {
    
    private int port;
    private int NB_MAX_CLIENTS = 3;
    private SourceTaches tachesAExecuter;
    private ConsoleServeur guiApplication;
    private ServerSocket SSocket = null;
    private Socket socketServeurCard = null;
    public ListeTaches listTaches;
    public ThreadServeur(int p, SourceTaches st, ConsoleServeur fs)
    {
        port =p; tachesAExecuter = st; guiApplication = fs;
        NB_MAX_CLIENTS = ChargerNbClients();
        listTaches = new ListeTaches();
    }
    
    public void run()
    {
        //creer la socket server
        try
        {
            SSocket = new ServerSocket(port);
            
        }catch(IOException e)
        {
            System.err.println("Erreur de port d'écoute ! ? [" + e + "]"); 
            this.stop();
        }
        
        Serveur_Carte serv = new Serveur_Carte();

        //Démarrage du pool threads
        for(int i=0; i<NB_MAX_CLIENTS; i++)
        {
            ThreadClient thr = new ThreadClient(listTaches, "Thread du pool n°" + String.valueOf(i), guiApplication);
            thr.start();
        }
        
        Socket CSocket = null;
        
        while(!isInterrupted())
        {
            
            try
            {
              System.out.println("SERVER | En attente de clients");
              CSocket = SSocket.accept();
              listTaches.setSocket(CSocket);
              guiApplication.TraceEvenements(CSocket.getRemoteSocketAddress().toString() + "#accept#thread serveur");
            }
            catch(IOException e)
            {
                System.err.println("Erreur d'accept ! ? [" + e.getMessage() + "]");
            }
        }
    }
    private void ConnexionServeurCarte() {
        try {
            System.out.println("SERVER | Connexion au serveur carte");
            socketServeurCard = new Socket("0.0.0.0", 50055);
        } 
        catch (UnknownHostException e)
        { System.err.println("Erreur Serveur Cartes ! Host non trouvé [" + e + "]");
        }
        catch (IOException e)
        { System.err.println("Erreur Serveur Cartes ! Pas de connexion ? [" + e + "]");
        }
    }

    private int ChargerNbClients() {
        try
        {
            InputStream input = new FileInputStream("config.properties");
            Properties prop = new Properties();
            
            prop.load(input);
            System.out.println("SERVER | Config max clients : " + Integer.parseInt(prop.getProperty("NB_MAX_CLIENTS")));
            return Integer.parseInt(prop.getProperty("NB_MAX_CLIENTS"));
        }
        catch(IOException e)
        {
            System.out.println(e);
        }
        return 3;
    }
}
