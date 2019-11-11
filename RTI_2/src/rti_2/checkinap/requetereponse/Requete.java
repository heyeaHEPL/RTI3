package rti_2.checkinap.requetereponse;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.*;
/**
 *
 * @author fredm
 */
public interface Requete {
    
    public Runnable createRunnable(Socket s, ConsoleServeur cs);
    
}
