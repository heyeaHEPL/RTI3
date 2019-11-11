/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti_2.checkinap;

import java.io.Serializable;
import rti_2.checkinap.requetereponse.Reponse;

/**
 *
 * @author fredm
 */
public class ReponseCHECKINAP  implements Reponse, Serializable{
    public static int LOGIN_OK = 101;
    public static int LOGIN_NOK = 102;
    public static int BOOKING_OK = 201;
    public static int BOOKING_NOK = 202;
    public static int BUY_OK = 301;
    public static int BUY_NOK = 302;
    public static int CLOSE_OK = 401;
    public static int CLOSE_NOK = 402;
    public static int CARTE_OK = 501;
    public static int CARTE_NOK = 501;
    private int codeRetour;
    private String chargeUtile;
    public ReponseCHECKINAP(int c, String chu)
    {
        codeRetour = c; setChargeUtile(chu);
    }
    public String getChargeUtile() { return chargeUtile; }
    public void setChargeUtile(String chargeUtile) { this.chargeUtile = chargeUtile; }

    @Override
    public int GetCode() {
         return codeRetour;
    }

}
