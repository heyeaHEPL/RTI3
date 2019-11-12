/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti_2;

import rti_2.client.Connexion;
import rti_2.serveur.Serveur_Compagnie;

/**
 *
 * @author fredm
 */
public class RTI_2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Serveur_Compagnie serv = new Serveur_Compagnie();
        serv.setLocation(800, 200);
        serv.setVisible(true);
        
        
        Connexion connect = new Connexion();
        connect.setLocation(200, 200);
        connect.setVisible(true);
    }
    
}
