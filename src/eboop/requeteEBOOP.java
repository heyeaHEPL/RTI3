/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eboop;
import rti_2.database.facility.MyInstruction;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import rti_2.checkinap.requetereponse.ConsoleServeur;
import rti_2.checkinap.requetereponse.Requete;

/**
 *
 * @author heyea
 */
public class requeteEBOOP implements Requete,Serializable
{
    public static int REQSTART = 30;
    public static int REQLOG = 31;
    public static int REQ_GET_TRAV = 32;
    public static int REQ_BOOK = 33;
    public static int REQ_SET_PAN = 34;
    public static int REQ_GET_PAN = 35;
    public static int REQ_DEL_PAN = 36;
    public static int REQ_BUY = 37;
    
    private static int CAPACITE_NAVIRE = 1;
    private static int MATRICULE_NAVIRE = 2;
    private static int ID_TRAVERSEES = 3;
    private static int NUMERO_CLIENT = 4;
    
    private static int PRIX = 5;
    private static int NUMERO_CARTE = 6;
    private static int DATE_CARTE = 7;
    private static int NOMBRE_PASSAGERS = 8;    
    private static int LAST_MINUTE = 9; 
    
    private static int DATE_TRAVERSEE = 10; 
    private static int NAVIRE_USED = 11;
    
    private ArrayList<String> dataList;
    
    private MyInstruction sgbd;
    private int code;
    private String data;
    boolean end = false;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    
    public requeteEBOOP(int c,String dat)
    {
        code = c;
        data = dat;
    }

    public String getChargeUtile()
    {
        return data;
    }
    @Override
    public Runnable createRunnable(Socket s, ConsoleServeur cs) {
        this.oos=oos;
        this.ois = ois;
        connectToDatabase();
        if(code==REQSTART)
        {
            return() -> {
                requeteStart(s);
            };
        }
        else return null;           
    }
    
    public int getCode()
    {
        return code;
    }
    
    @Override
    public void requeteStart(Socket s)
    {
        try {
            oos=(ObjectOutputStream)s.getOutputStream();
            ois=(ObjectInputStream)s.getInputStream();
            connectToDatabase();
        } catch (IOException ex) {
            System.err.println("requeteEBOOP : requeteStart : "+ex);
        }
        
        reponseEBOOP rep;
        rep = new reponseEBOOP(reponseEBOOP.ACK,getData());
        send(rep);
        
        while(end == false)
        {
            requeteEBOOP req = null;
            req = receive();
            
            dataList = new ArrayList();
            
            for(int i = 0;i<=15;i++)
                dataList.add(null);
            
            if(req.code == REQ_BOOK)
                reqBook(req);
            if(req.code == REQLOG)
                reqLog(req);
            if(req.code==REQ_GET_TRAV)
                reqGetTrav(req);
            if(req.code==REQ_SET_PAN)
                reqSetPan(req);
            if(req.code==REQ_GET_PAN)
                reqGetPan(req);
            if(req.code==REQ_BUY)
                reqBuy(req);
            if(req.code==REQ_DEL_PAN)
                reqDelPan(req);           
        }
        try
        {
            ois.close();
            oos.close();
            s.close();
        }
        catch(Exception e)
        {
            System.err.println("RequeteStart : "+e);
        }
    }
    
    private void reqGetTrav(requeteEBOOP req)
    {
        
    }
    
    private void reqBuy(requeteEBOOP req)
    {
        try
        {
            ArrayList<String> list_trav = new ArrayList<>();
            reponseEBOOP rep;
            StringTokenizer st = new StringTokenizer(req.getData(),",");
            
            dataList.set(PRIX, st.nextToken());
            dataList.set(NUMERO_CLIENT, st.nextToken());
            dataList.set(NUMERO_CARTE, st.nextToken());
            dataList.set(DATE_CARTE, st.nextToken());
            dataList.set(NOMBRE_PASSAGERS, st.nextToken());
            dataList.set(LAST_MINUTE, st.nextToken());
            
            while(st.hasMoreTokens())
                list_trav.add(st.nextToken());
            
            sgbd.getConnect().setAutoCommit(false);
            
            for(int i = 0; i < list_trav.size();i++)
            {
                sgbd.Query("select depart,navire from traversees where identifiant = '"+list_trav.get(i)+"'");
                sgbd.getResultat().next();
                dataList.set(DATE_TRAVERSEE,sgbd.getResultat().getString("depart"));
                dataList.set(NAVIRE_USED,sgbd.getResultat().getString("navire"));
                System.err.println("reqBuy -> recupere : " + dataList.get(DATE_TRAVERSEE)+"---"+ dataList.get(NAVIRE_USED));
                
                if(dataList.get(LAST_MINUTE).equals("OUI"))
                {
                    sgbd.Query("select cap_voiture from navires where matricule = '" + dataList.get(NAVIRE_USED)+"'");
                    sgbd.getResultat().next();
                    dataList.set(CAPACITE_NAVIRE, sgbd.getResultat().getString("cap_voiture"));
                    
                    if(Integer.valueOf(dataList.get(CAPACITE_NAVIRE))<=0)
                    {
                        rep = new reponseEBOOP(reponseEBOOP.FAIL,getData() + " : pas de place");
                        sgbd.getConnect().rollback();
                        sgbd.getConnect().setAutoCommit(true);
                        send(rep);
                        return;
                    }
                    
                    sgbd.UpdateCond("navires", "cap_voiture = " + (Integer.valueOf(dataList.get(CAPACITE_NAVIRE)) -1), "matricule ='"+dataList.get(NAVIRE_USED) + "'");
                }
                
                st = new StringTokenizer(dataList.get(DATE_TRAVERSEE),"-");
                String year = st.nextToken();
                String mond = st.nextToken();
                String day = st.nextToken();
                String date = day+"-"+mond+"-"+year;
                
                Long millis = System.currentTimeMillis();
                Date temp = new Date(millis);
                String today = new SimpleDateFormat("yyyyMMdd").format(temp);
                today = today+"-RES"+i;
                
                sgbd.upDate("insert into reservations values ('"+today+"','" + date + "','" + list_trav.get(i)+"','"+dataList.get(NUMERO_CLIENT) + "','O','N'");
            }
            
            
            //payement
            
            ArrayList<String> listTemp = new ArrayList<>();
            sgbd.SelectCountCond("paniers", "num_client = '"+dataList.get(NUMERO_CLIENT)+"'");
            
            while(sgbd.getResultat().next())
                listTemp.add(sgbd.getResultat().getString("id_trav"));
            
            for(int i = 0; i < listTemp.size();i++)
            {
                sgbd.Query("select nom,cap_voiture, matricule from navires where matricule = (select navire from traversees where identifiant = '"+ listTemp.get(i)+"'");
                
                while(sgbd.getResultat().next())
                {
                    dataList.set(CAPACITE_NAVIRE,sgbd.getResultat().getString("cap_voiture"));
                    dataList.set(MATRICULE_NAVIRE,sgbd.getResultat().getString("matricule"));                   
                }
                sgbd.UpdateCond("navires", "cap_voiture = '" + (Integer.valueOf(dataList.get(CAPACITE_NAVIRE))+1), "matricule = '" + dataList.get(MATRICULE_NAVIRE) + "'");
            }
            
            if(dataList.get(LAST_MINUTE).equals("NON"))
               sgbd.upDate("delete from panier where num_client = '" + dataList.get(NUMERO_CLIENT)+"'");
            
            
            sgbd.getConnect().commit();
            sgbd.getConnect().setAutoCommit(true);
            
            rep = new reponseEBOOP(reponseEBOOP.ACK,"commande OK");
            send(rep);
        }
        catch(SQLException e)
        {
            System.err.println("reqBuy : "+e);
        }
    }
    
    private void reqDelPan(requeteEBOOP req)
    {
        ArrayList<String> list_trav = new ArrayList<>();
        reponseEBOOP rep;
        try
        {
            sgbd.SelectionCond("paniers", "num_client = '" + req.getData() +"'");
            while(sgbd.getResultat().next())
                list_trav.add(sgbd.getResultat().getString("id_trav"));
            
            for(int i = 0; i<list_trav.size();i++)
            {
                sgbd.Query("select nom,cap_voiture,matricule from navires where matricule = (select navire from traversees where identifiant = '" + list_trav.get(i)+"')");
                
                while(sgbd.getResultat().next())
                {
                    dataList.set(CAPACITE_NAVIRE,sgbd.getResultat().getString("cap_voiture"));
                    dataList.set(MATRICULE_NAVIRE,sgbd.getResultat().getString("matricule"));
                }
                sgbd.UpdateCond("navires", "cap_voiture = " + Integer.valueOf(dataList.get(CAPACITE_NAVIRE))+1,"matricule = '"+ dataList.get(MATRICULE_NAVIRE) + "'");
                
            }
            
            sgbd.upDate("delete from panier where num_client = '" + req.getData()+"'");
            
            rep = new reponseEBOOP(reponseEBOOP.ACK,"panier vidé");
            send(rep);
            
        }
        catch(SQLException e)
        {
            System.err.println("reqDelPan : "+e);
        }
        
        
    }
    
    public void reqGetPan(requeteEBOOP req)
    {
        try
        {
            boolean tmp = false;
            
            reponseEBOOP rep;
            
            String panier = new String();
            
            sgbd.SelectionCond("paniers", "num_client = '" + req.getData() +"'");
            
            while(sgbd.getResultat().next())
            {
                tmp = true;
                panier = panier + sgbd.getResultat().getInt("num_client") + "," + sgbd.getResultat().getString("ID_TRAVERSEES") + ",";
            }
            if(tmp == false)
                panier = ",";
            
            rep = new reponseEBOOP(reponseEBOOP.ACK,panier.substring(0,panier.lastIndexOf(",")));
            send(rep);
        }
        catch(SQLException e)
        {
            System.err.println("reqGetPan : " + e);
        }
    }
    
    public void reqSetPan(requeteEBOOP req)
    {
        boolean tmp = false;
        
        reponseEBOOP rep;
        StringTokenizer st = new StringTokenizer(req.getData(),",");
        
        dataList.set(NUMERO_CLIENT, st.nextToken());
        dataList.set(ID_TRAVERSEES, st.nextToken());
        
        try
            
        {
            sgbd.Query("select cap_voiture,matricule from navires where matricule = (select navire from traversees where identifiant ='"+dataList.get(ID_TRAVERSEES) +"')");
            while(sgbd.getResultat().next())
            {
                tmp = true;
                dataList.set(CAPACITE_NAVIRE, sgbd.getResultat().getString("capacite_voiture"));
                dataList.set(MATRICULE_NAVIRE, sgbd.getResultat().getString("matricule"));
            }
            
            if(!tmp)
            {
                rep = new reponseEBOOP(reponseEBOOP.FAIL,getData()+" : " + "pas de navires");
                send(rep);
                return ;
            }
            
            if(Integer.valueOf(dataList.get(CAPACITE_NAVIRE))<=0)
            {
                rep = new reponseEBOOP(reponseEBOOP.FAIL,getData()+ " : " + "pas de place");
                send(rep);
                return;
            }
            
            sgbd.UpdateCond("navires", "cap_voiture = " + (Integer.valueOf(dataList.get(CAPACITE_NAVIRE))-1), "matricule = '" + dataList.get(MATRICULE_NAVIRE) + "'");
            
            tmp = false;
            String panier = new String();
            sgbd.upDate("inser into paniers values ('" + dataList.get(ID_TRAVERSEES)+"'"+dataList.get(NUMERO_CLIENT) + "')");
            sgbd.SelectionCond("paniers", "num_client = '" + dataList.get(NUMERO_CLIENT) + "'");
            
            while(sgbd.getResultat().next())
            {
                tmp=true;
                panier = panier +sgbd.getResultat().getInt("num_client") + " , " + sgbd.getResultat().getString("ID_TRAVERSEES") + ",";
            }
            
            if(tmp == false)
                panier = ",";
            
            rep = new reponseEBOOP(reponseEBOOP.ACK,panier.substring(0,panier.lastIndexOf(",")));
            send(rep);
            
        }
        catch(SQLException e)
        {
            System.err.println("reqSetPan : "+e);
        }
    }
    
    public void reqLog(requeteEBOOP req)
    {
        
    }
    
    public void reqBook(requeteEBOOP req)
    {
        reponseEBOOP rep;
        StringTokenizer st = new StringTokenizer(req.getData(),",");
        
        String sql = "insert into voyageurs values (null,'";
        
        for(int i = 0;i<6;i++)
            sql=sql + "," + st.nextToken();
        try
        {
            sgbd.upDate(sql);
            sgbd.Query("select max(num_client) as num_client from voyageurs");
            ResultSet rs = sgbd.getResultat();
            
            rep = new reponseEBOOP(reponseEBOOP.ACK,rs.getString("num_client"));
            
            send(rep);
        }
        catch(SQLException e)
        {
            System.err.println("reqBook : "+e);
        }
        
    }
    
    public void connectToDatabase()
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            sgbd.setAdresse("jdbc:mysql://localhost:3306/BD_FERRIES");
            sgbd.setLogin("root");
            sgbd.setPassword("root");
            sgbd.Connexion();
        }
        catch(ClassNotFoundException e)
        {
            System.out.println("classnotfound driver : "+e);
        }
        catch(SQLException e)
        {
            System.out.println("connexion impossible : "+e);
        }
    }
    
    public void setData(String dat)
    {
        data = dat;
    }
    public String getData()
    {
        return data;
    }
    
    public void send(reponseEBOOP rep)
    {
        try
        {
            oos.writeObject(rep);
            oos.flush();
        }
        catch(IOException e)
        {
            System.out.println("send : "+e);
        }
    }
    
    public requeteEBOOP receive()
    {
        requeteEBOOP req = null;
        try
        {
            req = (requeteEBOOP) ois.readObject();
        }
        catch(Exception e)
        {
            System.out.println("receive : "+e);
        }
        return req;
    }
    
}
