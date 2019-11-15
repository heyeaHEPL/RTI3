/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti_2.checkinap;

import rti_2.checkcarp.Serveur_Carte;
import rti_2.database.facility.MyInstruction;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import rti_2.checkinap.requetereponse.ConsoleServeur;
import rti_2.checkinap.requetereponse.Requete;

/**
 *
 * @author fredm
 */
public class RequeteCHECKINAP implements Requete, Serializable{
    public static int LOGIN = 1;
    public static int BOOKING = 2;
    public static int BUY = 3;
    public static int CLOSE = 4;
    public static int REQUEST_E_MAIL = 10;
    public static int REQUEST_TEMPORARY_KEY = 20;
    public static int IS_CARTE_VALIDE = 5;
    public static Hashtable tableLogin = new Hashtable();
    static
    {
        tableLogin.put("heyea", "azerty159");
        tableLogin.put("marcotty", "admin");
        tableLogin.put("a", "b"); //debug
    }
   
    public int type;
    private String chargeUtile;
    private Socket socketClient;
    private Socket socketServeurCard;
    
    public RequeteCHECKINAP(int t, String chu)
    {
        type = t; setChargeUtile(chu);
    }
    public RequeteCHECKINAP(int t, String chu, Socket s)
    {
        type = t; setChargeUtile(chu); socketClient =s;
    }
    public String getChargeUtile() { return chargeUtile; }
    public void setChargeUtile(String chargeUtile)
    {
        this.chargeUtile = chargeUtile;
    } 

    @Override
    public Runnable createRunnable(Socket s, ConsoleServeur cs) {
        return new Runnable()
        {
            public void run()
            {
                
            }
        };
    }
}
