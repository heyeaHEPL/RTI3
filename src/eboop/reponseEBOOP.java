/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eboop;

import java.io.Serializable;
import rti_2.checkinap.requetereponse.Reponse;

/**
 *
 * @author heyea
 */
public class reponseEBOOP implements Reponse, Serializable
{
    public static int ACK = 601;
    public static int FAIL = 701;
    
    private int code;
    private String data;
    
    public reponseEBOOP(int c,String da)
    {
        code = c;
        data = da;
    }
    
    @Override
    public int GetCode()
    {
        return code;
    }
    
    public String GetData()
    {
        return data;
    }
    
    public void setData(String da)
    {
        data = da;
    }
}
