/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ri.trabri;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nicolas
 */
public class Documento {

    // PN, RN, AN, AU, TI, SO, MJ, MN, AB, RF, CT
    
    public Map<String,String> atributos = null;
    
    public Documento(){
        atributos = new HashMap<>();
    }
    
    public void add(String key, String val) {
       String str = "";
       if (atributos.get(key) == null) {
           str = val;
       } else {
           str = atributos.get(key);
           str = str.concat(val);
       }

       atributos.put(key, str);
   }
    
}
