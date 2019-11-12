/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti_2.serveur;

import rti_2.checkinap.requetereponse.ConsoleServeur;
import rti_2.checkcarp.Serveur_Carte;

/**
 *
 * @author fredm
 */
public class ThreadCarte extends Thread {
    private String nom;
    
    private ConsoleServeur guiApplication;

    ThreadCarte(ConsoleServeur gui) {
        nom = "threadCarte";
        guiApplication = gui;
    }
    
    public void run()
    {
                
        guiApplication.TraceEvenements("threadCarte" + "#serveur_carte#thread serveur");
        Serveur_Carte ser = new Serveur_Carte();
        
        
        
    }
}
