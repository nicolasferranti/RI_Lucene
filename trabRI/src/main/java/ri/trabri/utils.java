/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ri.trabri;

/**
 *
 * @author nicolas
 */
public class utils {

    public static String tail(String[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array cannot be empty");
        }

        String tail = "";

        for (int i = 1; i < array.length; i++) {
            tail += array[i]+" ";
        }

        return tail;
    }
}
