/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cricketmsf.out.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public interface SqlDBIface extends KeyValueDBIface{

    /**
     * Creates database 
     * @param conn database connection
     */
    public void createDatabase(Connection conn, String version);
    
    /**
     * Gets new database connection from the connection pool
     * @return database connection
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException;
    
    /**
     * Get service database version name
     * @return version name
     */
    public String getVersion();
    
    /**
     * Returns list of objects matching the givens SQL statement and parameters
     * @param tableName the database table name
     * @param statement SQL prepared statement
     * @param parameters array of the statement parameters
     * @return list of objects matching the statement
     * @throws KeyValueDBException
     */
    public List search(String tableName, String statement, String[] parameters) throws KeyValueDBException;
    
}
