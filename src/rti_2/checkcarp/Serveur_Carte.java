/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti_2.checkcarp;

import rti_2.checkinap.ReponseCHECKINAP;

import rti_2.database.facility.MyInstruction;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author fredm
 */
public class Serveur_Carte {

    private ServerSocket SocketCard;
    private Socket CSocket;
    private MyInstruction sgbd;
    private String driver;
    private int PORT_CARD = 50055;

    public Serveur_Carte() {
        Config();
        
        //ConnexionServeur();
        ThreadVerifCarte th = new ThreadVerifCarte(CSocket);
        th.start();        
    }

    private void Config() {

        System.out.println("Config serveur cartes terminee");
    }
    private void ConnexionServeur() {
        
        try
        {
            System.out.println("Serveur carte en attente de connexion");
            SocketCard = new ServerSocket(PORT_CARD);
            CSocket = SocketCard.accept();
            
        }catch(IOException e)
        {
            System.err.println("Erreur de port d'écoute ! ? [" + e + "]"); 
            //System.exit(1);
        }
        System.out.println("Connexion au serveur cartes établie");
    }
}
