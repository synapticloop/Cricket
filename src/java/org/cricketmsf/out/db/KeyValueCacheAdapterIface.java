/*
 * Copyright 2016 Grzegorz Skorupa <g.skorupa at gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cricketmsf.out.db;

import java.util.List;
import java.util.Map;
import org.cricketmsf.out.db.ComparatorIface;

/**
 *
 * @author greg
 */
public interface KeyValueCacheAdapterIface {
    
    public void start();
    public void put(String key, Object value);
    public Object get(String key);
    public Object get(String key, Object defaultValue);
    public Map getAll();
    public List search(ComparatorIface comparator, Object pattern);
    public boolean containsKey(String key);
    public boolean remove(String key);
    public void clear();
    public long getSize();
}
