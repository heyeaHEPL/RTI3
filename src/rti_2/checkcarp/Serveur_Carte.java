/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti_2.checkcarp;

import java.io.FileInputStream;
import rti_2.checkinap.ReponseCHECKINAP;

import rti_2.database.facility.MyInstruction;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        
        try {
            SocketCard = new ServerSocket(PORT_CARD);
        } catch (IOException ex) {
            Logger.getLogger(ThreadVerifCarte.class.getName()).log(Level.SEVERE, null, ex);
        }
        ThreadVerifCarte th = new ThreadVerifCarte(SocketCard);
        th.start();        
    }

    private void Config() {
        PORT_CARD = ChargerPort();
        System.out.println("Config serveur cartes terminee : port : " + PORT_CARD);
    }
    private int ChargerPort() {
        try
        {
            InputStream input = new FileInputStream("config.properties");
            Properties prop = new Properties();
            
            prop.load(input);
            System.out.println("vSERVER | Config max clients : " + Integer.parseInt(prop.getProperty("NB_MAX_CLIENTS")));
            return Integer.parseInt(prop.getProperty("portServeurCarte"));
        }
        catch(IOException e)
        {
            System.out.println(e);
        }
        return 3;
    }
    
}
