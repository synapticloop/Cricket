/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.out.file;

/**
 *
 * @author greg
 */
public interface CommandRunnerIface {
    public String execute();
    public String getProperty(String name);
}
