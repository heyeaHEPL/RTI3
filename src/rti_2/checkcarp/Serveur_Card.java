/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti_2.checkcarp;

import rti_2.database.facility.MyInstruction;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author fredm
 */
public class Serveur_Card {

    private Socket SocketCard;
    private MyInstruction sgbd;
    private String driver;
    private int PORT_CARD = 50055;

    public Serveur_Card() {
        Config();
        ConnexionServeur();
        //AttenteRequete();
        
        ConnexionDBCard();
        
        //EnvoiReponse();
    }

    private void ConnexionDBCard() {
        boolean erreur = false;
        System.out.println("Try driver");
        sgbd = new MyInstruction();
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e);
            JOptionPane.showMessageDialog(null, "Serveur_Card : driver introuvable", "Erreur", JOptionPane.ERROR_MESSAGE);
            erreur = true;
        }

        if (!erreur) {
            System.out.println("Connexion reussie");
            sgbd.setAdresse("jdbc:mysql://localhost:3306/bd_card");
            sgbd.setLogin("root");
            sgbd.setPassword("root");
            try {
                sgbd.Connexion();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Serveur_Card : connexion à la BD impossible", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean CarteValide(String carte) {
        try {
            sgbd.SelectionCond("carte", "numcarte LIKE '" + carte + "'"); //tous les résultats.

            if (sgbd.getResultat().next()) {
                if (sgbd.getResultat().getString("numcarte") != null) {
                    return true;
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Serveur_Card : erreur obtention infos carte", "Erreur", JOptionPane.ERROR_MESSAGE);
            System.out.println(e);
        }

        return false;
    }

    private void Config() {

        System.out.println("Config serveur cartes terminee");
    }

    private void ConnexionServeur() {
        
        try
        {
            System.out.println("Connexion serveur cartes");
            SocketCard = new Socket("0.0.0.0", PORT_CARD);
            
        }catch(IOException e)
        {
            System.err.println("Erreur de port d'écoute ! ? [" + e + "]"); 
            //System.exit(1);
        }
        System.out.println("Connexion au serveur cartes établie");
    }
}
