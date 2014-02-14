package be.mmlab.ldfjena.utils;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

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
