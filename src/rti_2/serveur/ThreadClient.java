/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti_2.serveur;

import rti_2.checkinap.ReponseCHECKINAP;
import rti_2.checkinap.RequeteCHECKINAP;
import static rti_2.checkinap.RequeteCHECKINAP.BOOKING;
import static rti_2.checkinap.RequeteCHECKINAP.BUY;
import static rti_2.checkinap.RequeteCHECKINAP.CLOSE;
import static rti_2.checkinap.RequeteCHECKINAP.tableLogin;
import rti_2.database.facility.MyInstruction;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JOptionPane;
import static rti_2.checkinap.ReponseCHECKINAP.BOOKING_NOK;
import static rti_2.checkinap.ReponseCHECKINAP.BOOKING_OK;
import static rti_2.checkinap.ReponseCHECKINAP.BUY_NOK;
import static rti_2.checkinap.ReponseCHECKINAP.BUY_OK;
import static rti_2.checkinap.ReponseCHECKINAP.CLOSE_OK;
import static rti_2.checkinap.ReponseCHECKINAP.LOGIN_OK;
import static rti_2.checkinap.RequeteCHECKINAP.IS_CARTE_VALIDE;
import static rti_2.checkinap.RequeteCHECKINAP.LOGIN;
import rti_2.checkinap.requetereponse.ConsoleServeur;
import static rti_2.network.NetworkLibrary.EnvoyerReponse;
import static rti_2.network.NetworkLibrary.EnvoyerRequete;
import static rti_2.network.NetworkLibrary.RecevoirReponse;

/**
 *
 * @author fredm
 */
public class ThreadClient extends Thread {
    private SourceTaches tachesAExecuter;
    private String nom;
    private Socket CSocket;
    private Runnable tacheEnCours;
    private String chargeUtile;
    private Socket socketServeurCard;
    private ConsoleServeur cs;
    private ListeTaches listTaches;
    public ThreadClient(SourceTaches st, String n, ConsoleServeur cse)
    {
        tachesAExecuter = st;
        nom = n;
        cs = cse;
        listTaches = (ListeTaches) st;
    }
    
    public void run()
    {
        try
        {
            System.out.println("SERVER | Thread " + this.getId() + " avant getSocket");
            CSocket = listTaches.getSocket();
            System.out.println("SERVER | Socket client recue :" + CSocket);
        }
        catch(InterruptedException e)
        {
            System.out.println("SERVER | Interruption : " + e.getMessage());
        }
        while(!isInterrupted())
        {
            
            //attente autre requetes
            TraiterRequete(CSocket, cs);
        }
    }
    private synchronized void TraiterRequete(Socket s, ConsoleServeur cs) {
        //Attendre la requete
        System.out.println("SERVER | Thread " + this.getId() + " Attente requete");
        RequeteCHECKINAP req = null;
        req = RecevoirRequete(s);
        
        //Traiter la requete
        if(req.type == LOGIN)
        {
            traiteRequeteLogin(s, cs);
        }
        else if(req.type == BOOKING)
        {
            traiteRequeteBooking(s, cs);
        }
        else if(req.type == BUY)
        {
            traiteRequeteBuy(s, cs);
        }
        else if(req.type == CLOSE)
        {
            traiteRequeteClose(s, cs);
        }
    }
    
    
    
    private synchronized void traiteRequeteBooking(Socket s, ConsoleServeur cs)
    {
        System.out.println("SERVER | Booking Charge utile recue : " + chargeUtile);
        cs.TraceEvenements(s.getRemoteSocketAddress().toString() + "#BOOKING" + "#Thread"+ this.getId());
        //separation champs recus
        Vector infos = new Vector();
        String code, passagers;
        StringTokenizer parser = new StringTokenizer(chargeUtile, "#");
        System.out.println("tokens");
        while(parser.hasMoreTokens())
            infos.add(parser.nextToken());
        code = (String) infos.get(0);
        
        ReponseCHECKINAP rep;
        //si la reservation existe envoyer reponse OK
        if(BookingExiste(code))
        {
            int places = RecupPlaces(code);
            chargeUtile = Integer.toString(places);
            EnvoyerReponse(s, BOOKING_OK, getChargeUtile());
        }
        else
        {
            System.out.println("SERVER | booking n'existe pas !");
            EnvoyerReponse(s, BOOKING_NOK, getChargeUtile());
        }
        System.out.println("SERVER | Envoi réponse book");
    }
    private synchronized void traiteRequeteBuy(Socket s, ConsoleServeur cs)
    {
        System.out.println("SERVER | BUY Charge utile recue : " + chargeUtile);
        cs.TraceEvenements(s.getRemoteSocketAddress().toString() + "#BUY" + "#Thread"+ this.getId());
        Vector infos = new Vector();
        String nom, immatriculation, passagers, carte;
        StringTokenizer parser = new StringTokenizer(chargeUtile, "#");
        while(parser.hasMoreTokens())
            infos.add(parser.nextToken());
        nom = (String) infos.get(0);
        immatriculation = (String) infos.get(1);
        passagers = (String) infos.get(2);
        carte = (String) infos.get(3);
        ReponseCHECKINAP rep = null; 
        //Verif serveur de cartes
        System.out.println("SERVER | Carte : " + carte);
            ConnexionServeurCarte();
        
        if(CarteValide(carte, socketServeurCard))
        {
            System.out.println("SERVER | carte_remote ok");
            EnvoyerReponse(s, BUY_OK, getChargeUtile());
        }
        else
        {
            System.out.println("SERVER | carte_remote invalide");
            EnvoyerReponse(s, BUY_NOK, getChargeUtile());
        }
    }
    private synchronized void traiteRequeteClose(Socket s, ConsoleServeur cs)
    {
        System.out.println("SERVER |  CLOSE Recu");
        cs.TraceEvenements(s.getRemoteSocketAddress().toString() + "#CLOSE" + "#Thread"+ this.getId());
        
        //EnvoyerReponse(s, CLOSE_OK, getChargeUtile());
        chargeUtile = "Close";
        ReponseCHECKINAP rep = new ReponseCHECKINAP(CLOSE_OK, getChargeUtile());
        
        ObjectOutputStream oos;
        try
        {
        oos = new ObjectOutputStream(s.getOutputStream());
        oos.writeObject(rep); oos.flush();
        //oos.close();
        }
        catch (IOException e)
        {
        System.err.println("Erreur réseau ? [" + e.getMessage() + "]");
        }
        
        System.exit(0);
    }
    private synchronized void traiteRequeteLogin(Socket s, ConsoleServeur cs)
    {
        // Affichage des informations
        //String adresseDistante = s.getRemoteSocketAddress().toString();
        System.out.println("SERVER | TraiterLOGIN");
        System.out.println("SERVER | Charge utile recue : " + chargeUtile);
        cs.TraceEvenements(s.getRemoteSocketAddress().toString() + "#LOGIN" + "#Thread" + this.getId());
        //Separation champs user#pass
        Vector infos = new Vector();
        String user;
        String pass;
        StringTokenizer parser = new StringTokenizer(chargeUtile, "#");
        while(parser.hasMoreTokens())
            infos.add(parser.nextToken());
        user = (String) infos.get(0);
        pass = (String) infos.get(1);
        System.out.println("Login : " + user + "Pass: " + pass);
        // Verification dans la BD -> Hashtable pour le moment
        ReponseCHECKINAP rep;
        if(LoginExiste(user, pass))
        {
            EnvoyerReponse(s, LOGIN_OK, getChargeUtile());
        }
        else
        {
            EnvoyerReponse(s, LOGIN_OK, getChargeUtile());
        }
    }
    
    public String getChargeUtile() { return chargeUtile; }
    public void setChargeUtile(String chargeUtile)
    {
        this.chargeUtile = chargeUtile;
    } 

    private boolean LoginExiste(String user, String pass) {
        if(tableLogin.containsKey(user))
            if(tableLogin.get(user).equals(pass))
                return true;
        return false;
    }
    private boolean BookingExiste(String code) {
        MyInstruction sgbd;
        sgbd = new MyInstruction();
        System.out.println("SERVER | id reservation : " + code);
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch(ClassNotFoundException e)
        {
            JOptionPane.showMessageDialog(null, "driver introuvable", "Erreur driver", JOptionPane.ERROR_MESSAGE); 
        }
        System.out.println("SERVER | Connexion bd_ferries OK");
        sgbd.setAdresse("jdbc:mysql://localhost:3306/BD_FERRIES");
        sgbd.setLogin("root");
        sgbd.setPassword("root");
        try
        {
            sgbd.Connexion();
        }
        catch(SQLException e)
        {
            JOptionPane.showMessageDialog(null, "connexion à la BD impossible", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        try
        {
            sgbd.SelectionCond("reservations", "identifiant LIKE '"+ code + "'");
            if(sgbd.getResultat().next())
            {
               if(sgbd.getResultat().getString("identifiant") != null)
                {
                    System.out.println("SERVER | res bd : " + sgbd.getResultat().getString("identifiant"));
                    return true;
                }
            }
        }
        catch(SQLException e)
        {
            JOptionPane.showMessageDialog(null, "recup reservation erreur", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    private boolean PlaceDispo(String nom) {
        MyInstruction sgbd;
        
        sgbd = new MyInstruction();
        
        System.out.println("SERVER | Place dispo : ");
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch(ClassNotFoundException e)
        {
            JOptionPane.showMessageDialog(null, "driver introuvable", "Erreur", JOptionPane.ERROR_MESSAGE); 
        }
        System.out.println("SERVER | Connexion bd_ferries OK");
        sgbd.setAdresse("jdbc:mysql://localhost:3306/BD_FERRIES");
        sgbd.setLogin("root");
        sgbd.setPassword("root");
        try
        {
            sgbd.Connexion();
        }
        catch(SQLException e)
        {
            JOptionPane.showMessageDialog(null, "connexion à la BD impossible", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        try
        {
            //recup identifiant de la prochaine traversee ==> destination ?
            
            //ajout du voyageur (conducteur) et des accompagnants
            
            // ajout de la reservation pour la traversee et le conducteur
            
            if(sgbd.BuyTicket(nom))
            {
                return true;
            }
        }
        catch(SQLException e)
        {
            System.out.println(e);
            JOptionPane.showMessageDialog(null, "erreur buy ticket", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    private int RecupPlaces(String code) {
        MyInstruction sgbd;
        sgbd = new MyInstruction();
        int places;
        System.out.println("SERVER | Recup places : ");
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch(ClassNotFoundException e)
        {
            JOptionPane.showMessageDialog(null, "driver introuvable", "Erreur", JOptionPane.ERROR_MESSAGE); 
        }
        System.out.println("SERVER | Connexion bd_ferries OK");
        sgbd.setAdresse("jdbc:mysql://localhost:3306/BD_FERRIES");
        sgbd.setLogin("root");
        sgbd.setPassword("root");
        try
        {
            sgbd.Connexion();
        }
        catch(SQLException e)
        {
            JOptionPane.showMessageDialog(null, "connexion à la BD impossible", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        try
        {
            sgbd.SelectCountCond("reservations", "identifiant LIKE '" + code + "'");
            if(sgbd.getResultat().next())
            {
               if(sgbd.getResultat().getInt(1) != -1)
                {
                    System.out.println("places bd : " + sgbd.getResultat().getInt(1));
                    places = sgbd.getResultat().getInt(1);
                    return places;
                }
            }
        }
        catch(SQLException e)
        {
            System.out.println(e);
            JOptionPane.showMessageDialog(null, "recup reservation des places", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        return -1;
    }
    private boolean CarteValide(String carte, Socket card_Socket) {
        
        //ConnexionServeurCarte();
        EnvoyerRequete(card_Socket, IS_CARTE_VALIDE, carte);
        ReponseCHECKINAP rep = null;
        rep = RecevoirReponse(card_Socket);
        
        if(rep.GetCode() == ReponseCHECKINAP.CARTE_OK)
        {
            return true;
        }
        
        return false;
    }
    private ReponseCHECKINAP Recevoir(Socket card_Socket) {
        // Lecture de la réponse
        ReponseCHECKINAP rep = null;
        try
        {
            ObjectInputStream ois = new ObjectInputStream(card_Socket.getInputStream());
            rep = (ReponseCHECKINAP)ois.readObject();
            System.out.println(" *** Reponse reçue : " + rep.getChargeUtile());
        }
        catch (ClassNotFoundException e)
        { System.out.println("--- erreur sur la classe = " + e.getMessage()); }
        catch (IOException e)
        { System.out.println("--- erreur IO = " + e.getMessage()); }
        return rep;
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
    private RequeteCHECKINAP RecevoirRequete(Socket s) {
        RequeteCHECKINAP rep = null;
        System.out.println("SERVER | Reception requete "); 
        if(s == null)
           System.out.println("socket null ");  
        try
        {
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            rep = (RequeteCHECKINAP)ois.readObject();
            System.out.println("SERVER | Reponse reçue : " + rep.getChargeUtile());
            System.out.println("SERVER | Type : " + rep.type);
            chargeUtile = rep.getChargeUtile();
        }
        catch (ClassNotFoundException e)
        { System.out.println("--- erreur sur la classe = " + e.getMessage()); }
        catch (IOException e)
        { System.out.println("--- erreur IO = " + e.getMessage()); }
        return rep;
    }
}
