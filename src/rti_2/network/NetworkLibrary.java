/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti_2.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import rti_2.checkinap.ReponseCHECKINAP;
import rti_2.checkinap.RequeteCHECKINAP;

/**
 *
 * @author fredm
 */
public class NetworkLibrary {
    public static void EnvoyerRequete(Socket cliSocket, int code, String chargeUtile)
    {
        ObjectOutputStream oos;
        RequeteCHECKINAP req = null;
        req = new RequeteCHECKINAP(code, chargeUtile);
        try
        {
            oos = new ObjectOutputStream(cliSocket.getOutputStream());
            oos.writeObject(req);
        }
        catch (IOException e)
        { System.err.println("Erreur réseau ? [" + e.getMessage() + "]"); }
    }
    public static void EnvoyerReponse(Socket cliSocket, int code, String chargeUtile)
    {
        ObjectOutputStream oos;
        ReponseCHECKINAP req = null;
        req = new ReponseCHECKINAP(code, chargeUtile);
        try
        {
            oos = new ObjectOutputStream(cliSocket.getOutputStream());
            oos.writeObject(req);
        }
        catch (IOException e)
        { System.err.println("Erreur réseau ? [" + e.getMessage() + "]"); }
    }
    public static ReponseCHECKINAP RecevoirReponse(Socket cliSocket)
    {   
        ObjectInputStream ois;
        
        ReponseCHECKINAP rep = null;
        try
        {
            ois = new ObjectInputStream(cliSocket.getInputStream());
            rep = (ReponseCHECKINAP)ois.readObject();
            System.out.println(" *** Reponse reçue : " + rep.getChargeUtile());
        }
        catch (ClassNotFoundException e)
        { System.out.println("--- erreur sur la classe = " + e.getMessage()); }
        catch (IOException e)
        { System.out.println("--- erreur IO = " + e.getMessage()); }
        
        return rep;
    }
    public static RequeteCHECKINAP RecevoirRequete(Socket cliSocket)
    {   
        ObjectInputStream ois;
        
        RequeteCHECKINAP rep = null;
        //rep = new RequeteCHECKINAP()
        try
        {
            ois = new ObjectInputStream(cliSocket.getInputStream());
            rep = (RequeteCHECKINAP)ois.readObject();
            System.out.println(" *** Reponse reçue : " + rep.getChargeUtile());
        }
        catch (ClassNotFoundException e)
        { System.out.println("--- erreur sur la classe = " + e.getMessage()); }
        catch (IOException e)
        { System.out.println("--- erreur IO = " + e.getMessage()); }
        
        return rep;
    }
    private static Socket ConnexionClient(String adresse, int port)
    {
        Socket cliSocket = null;
        try
        {
            cliSocket = new Socket(adresse, port);
            System.out.println("CLIENT | " + cliSocket.getInetAddress().toString());
        }
        catch (UnknownHostException e)
        { System.err.println("Erreur ! Host non trouvé [" + e + "]");
        }
        catch (IOException e)
        { System.err.println("Erreur ! Pas de connexion ? [" + e + "]");
        }
        return cliSocket;
    }
    private static ServerSocket CreerServeur(int port)
    {
        ServerSocket SSocket = null;
        try
        {
            SSocket = new ServerSocket(port);
            
        }catch(IOException e)
        {
            System.err.println("Erreur de port d'écoute ! ? [" + e + "]");
            return null;
        }
        return SSocket;
    }
}
