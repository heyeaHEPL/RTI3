/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rti_2.database.facility;

import static java.lang.System.exit;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author heyea
 */
public class MyInstruction extends MyConnexion
{
    private PreparedStatement PS;
    public MyInstruction(){super();}
    
    public synchronized void SelectionSimple (String table) throws SQLException
    {
        PS = Conn.prepareStatement("SELECT * FROM "+table);
        Resultat=PS.executeQuery();
    }
    
    public synchronized void SelectionCond(String table,String cond) throws SQLException
    {
        PS = Conn.prepareStatement("SELECT * FROM "+table+" where "+cond);
        Resultat=PS.executeQuery();
    }
    
    public synchronized void SelectCount(String table) throws SQLException
    {
        PS = Conn.prepareStatement("SELECT count(*) FROM "+table);
        Resultat=PS.executeQuery();
    }
    public synchronized void SelectCountCond(String table,String cond) throws SQLException
    {
        PS = Conn.prepareStatement("SELECT count(*) FROM "+table+" where "+cond);
        Resultat = PS.executeQuery();
    }
    
    public synchronized void UpdateCond (String table, String change, String cond) throws SQLException
    {
        PS = Conn.prepareStatement("update "+table+" set "+ change+ " where " + cond);
        PS.executeUpdate();
    }
    
    public synchronized void Instruction (String instruct)throws SQLException
    {
        PS = Conn.prepareStatement(instruct);
        Resultat = PS.executeQuery();
        //PS.executeUpdate();
    }
    public synchronized void InsertTrav (String id,String date,String depart, String dest, String navire)throws SQLException
    {
        String query = "insert into traversees (identifiant,date_depart,depart,destination,navire)"+ "values(?,?,?,?,?)";
        PS = Conn.prepareStatement(query);
        PS.setString(1, id);
        PS.setString(2, date);
        PS.setString(3, depart);
        PS.setString(4, dest);
        PS.setString(5, navire);
        
        PS.execute();        
    }
    public synchronized void InsertRes(String id,String trav,String voy,String paye,String check)throws SQLException
    {
        String query ="insert into reservations (identifiant,traversee,voyageur_titulaire,paye,passe_check)"+"values(?,?,?,?,?)";
        PS = Conn.prepareStatement(query);
        PS.setString(1, id);
        PS.setString(2, trav);
        PS.setString(3, voy);
        PS.setString(4, paye);
        PS.setString(5, check);
        
        PS.execute(); 
    }
    
    public synchronized void ListFromTravNDate(String trav,String date)throws SQLException
    {
        PS = Conn.prepareStatement("SELECT distinct voyageurs.* FROM reservations,traversees,voyageurs WHERE reservations.traversee LIKE '"+ trav + "' AND traversees.date_depart =" + date +" AND reservations.voyageur_titulaire = voyageurs.num_client");
        Resultat = PS.executeQuery();
    }
    
    public synchronized void ListFromTime(String TimeStart,String TimeEnd)throws SQLException
    {
        PS = Conn.prepareStatement("SELECT distinct voyageurs.* FROM reservations,traversees,voyageurs WHERE reservations.voyageur_titulaire = voyageurs.num_client AND traversees.date_depart BETWEEN "+TimeStart+" AND "+TimeEnd+" GROUP BY VOYAGEURS.PAYS");
        Resultat = PS.executeQuery();
    }

    public synchronized boolean BuyTicket (String Nom)throws SQLException
    {
        System.out.println("Cherche la prochaine traversée");
        String idTrav = getNextTravToday();
        System.out.println("id : " + idTrav);
        
        if(idTrav.equals("nope"))
        {
            System.out.println("pas de traversees aujourd'hui"); return false;
        }
        System.out.println("traversees trouvee : ajout d'un nouveau client");
        
        
        
        
        /*ICI IL FAUT MODIFIER POUR S'ASSURER QUE LE CLIENT N'EXISTE PAS ENCORE AVANT DE L'AJOUTER*/
        
        int idClient = getNewIdClient();
        
        String query = "insert into voyageurs (num_client,nom) values(?,?)";
        PS = Conn.prepareStatement(query);
        PS.setInt(1, idClient);
        PS.setString(2, Nom);
        
        PS.execute();
        /* FIN DE COMMENTAIRE*/
        
        
        
        System.out.println("client ajoute\najout de la reservation");
        
        query ="insert into reservations (identifiant,traversee,voyageur_titulaire,paye,passe_check)"+"values(?,?,?,?,?)";
        String idRes = getNewIdRes();
        PS = Conn.prepareStatement(query);
        PS.setString(1, idRes);
        PS.setString(2, idTrav);
        PS.setInt(3, idClient);
        PS.setString(4, "N");
        PS.setString(5, "N");
        
        PS.execute(); 
        return true;
    }
    
    public synchronized String getNextTravToday()throws SQLException
    {
        Date date;
        String today;
        Long millis = System.currentTimeMillis();
        date = new Date(millis);
        today = new SimpleDateFormat("yyyy-MM-dd").format(date);
        Resultat = null;
        this.SelectionCond("traversees", "date_depart >= "+today);
        System.out.println("date ajd : " + today);
        if(Resultat == null)
            return "nope";
        
        Resultat.next();
        return Resultat.getString("identifiant");              
    }
    
    public synchronized String getNewIdRes()throws SQLException
    {
        PS = Conn.prepareStatement("SELECT MAX(identifiant) FROM reservations");
        Resultat = PS.executeQuery();
        Resultat.next();
        String newID = Resultat.getString(1);
        String temp;
        temp = newID.substring(newID.length()-1);
        newID = newID.substring(0,newID.length()-1);
        
        int num = Integer.parseInt(temp);
        num = num+1;
        newID = newID+num;

        return newID;
    }
    
    public synchronized int getNewIdClient()throws SQLException
    {
        PS = Conn.prepareStatement("Select MAX(num_client) From voyageurs");
        Resultat = PS.executeQuery();
        Resultat.next();
        int res = Resultat.getInt(1)+1;
        return res;
    }
    
}
