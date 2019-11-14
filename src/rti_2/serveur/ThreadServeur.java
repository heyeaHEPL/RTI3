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
        try
        {
            SSocket = new ServerSocket(port);
            
        }catch(IOException e)
        {
            System.err.println("Erreur de port d'écoute ! ? [" + e + "]"); 
            //System.exit(1);
            this.stop();
        }
        
        //Démarrage du pool threads
        for(int i=0; i<NB_MAX_CLIENTS; i++)
        {
            ThreadClient thr = new ThreadClient(listTaches, "Thread du pool n°" + String.valueOf(i), guiApplication);
            thr.start();
        }
        //Demarrage serveur carte
        ThreadCarte th = new ThreadCarte(guiApplication);
        th.start();
        //ConnexionServeurCarte();
        //Mise en attente du serveur
        Socket CSocket = null;
        
        while(!isInterrupted())
        {
            
            try
            {
              System.out.println("*********Serveur en attente de clients");
              CSocket = SSocket.accept();
              System.out.println("accept socket : " + CSocket);
              listTaches.setSocket(CSocket);
              guiApplication.TraceEvenements(CSocket.getRemoteSocketAddress().toString() + "#accept#thread serveur");
            }
            catch(IOException e)
            {
                System.err.println("Erreur d'accept ! ? [" + e.getMessage() + "]");
                System.exit(1);
            }
            /*
            ObjectInputStream ois =null;
            Requete req = null;
            try
            {
                ois = new ObjectInputStream(CSocket.getInputStream());
                req = (Requete)ois.readObject();
                System.out.println("Requete lue par le serveur, instance de " + req.getClass().getName());
            }
            catch(ClassNotFoundException e)
            {
                System.err.println("Erreur de def de classe [" + e.getMessage() + "]");
            }
            catch(IOException e)
            {
                System.err.println("Erreur ? [" + e.getMessage() + "]");
            }
            
            Runnable travail = req.createRunnable(CSocket, guiApplication);
            if(travail != null)
            {
                //tachesAExecuter.setSocket(CSocket);
                //System.out.println("affectation socket" + CSocket);
                tachesAExecuter.recordTache(travail);
                
                System.out.println("Travail mis dans la file");
            }
            else
                System.out.println("Pas de mise en file");
                    */
        }
    }
    private void ConnexionServeurCarte() {
        try {
            System.out.println("Try connexion au serveur carte");
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
            System.out.println("Config max clients : " + Integer.parseInt(prop.getProperty("NB_MAX_CLIENTS")));
            return Integer.parseInt(prop.getProperty("NB_MAX_CLIENTS"));
        }
        catch(IOException e)
        {
            System.out.println(e);
        }
        return 3;
    }
}
