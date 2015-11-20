package org.linkeddatafragments.utils;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Created by ldevocht on 4/29/14.
 */
public class Config {
    public String datasource;
    public Map<String, String> prefixes;

    public Config() {
        prefixes = Maps.newHashMap();
    }
}
