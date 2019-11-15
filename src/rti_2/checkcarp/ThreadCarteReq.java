/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti_2.checkcarp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import rti_2.checkinap.ReponseCHECKINAP;
import rti_2.checkinap.RequeteCHECKINAP;
import rti_2.database.facility.MyInstruction;

/**
 *
 * @author fredm
 */
    
public class ThreadCarteReq extends Thread{
    private Socket CSocket;
    private MyInstruction sgbd;
    private String driver;
    ThreadCarteReq(Socket s)
    {
        CSocket = s;
    }
    public void run()
    {
        RequeteCHECKINAP rep;
        rep = AttenteRequete();

            if(rep.type == RequeteCHECKINAP.IS_CARTE_VALIDE)
            {
                ConnexionDBCard();
                if(CarteValide(rep.getChargeUtile()))
                    EnvoiReponse(ReponseCHECKINAP.CARTE_OK ,"CARTE_OK");
                else
                    EnvoiReponse(ReponseCHECKINAP.CARTE_NOK, "CARTE_NOK");
            }
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
    private synchronized RequeteCHECKINAP AttenteRequete() {
        RequeteCHECKINAP rep = null;
        try
        {
            ObjectInputStream ois = new ObjectInputStream(CSocket.getInputStream());
            rep = (RequeteCHECKINAP)ois.readObject();
            System.out.println(" *** Reponse reçue : " + rep.getChargeUtile());
        }
        catch (ClassNotFoundException e)
        { System.out.println("--- erreur sur la classe = " + e.getMessage()); }
        catch (IOException e)
        { System.out.println("--- erreur IO = " + e.getMessage()); }
        return rep;
    }

    private synchronized void EnvoiReponse(int code, String chargeUtile) {
        ReponseCHECKINAP req = null; 
        req = new ReponseCHECKINAP(code, chargeUtile);
        
        // Envoi de la requête
        try
        {
            ObjectOutputStream oos = new ObjectOutputStream(CSocket.getOutputStream());
            oos.writeObject(req); oos.flush();
        }
        catch (IOException e)
        { System.err.println("Erreur réseau ? [" + e.getMessage() + "]"); }
    }

    private boolean CarteValide(String carte) {
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
}
