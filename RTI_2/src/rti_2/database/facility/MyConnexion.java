/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti_2.database.facility;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author heyea
 */
public class MyConnexion implements Serializable
{
    protected Connection Conn;
    protected Statement Instruction;
    protected ResultSet Resultat;
    protected String Adresse;
    protected String Login;
    protected String Password;
    
    public MyConnexion(){};
    
    public void setAdresse(String x)
    {
        Adresse = new String(x);
    }
    public void setLogin(String x)
    {
        Login = new String(x);
    }
    public void setPassword(String x)
    {
        Password = new String(x);
    }
    
    public ResultSet getResultat()
    {
        return Resultat;
    }
    
    public void Connexion() throws SQLException
    {
        Conn = DriverManager.getConnection(Adresse,Login,Password);
    }
}
