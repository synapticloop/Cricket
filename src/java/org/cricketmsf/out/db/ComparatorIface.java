/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.out.db;

/**
 *
 * @author greg
 */
public interface ComparatorIface {
    
    public int compare(Object storedObject, Object pattern);
    
}
