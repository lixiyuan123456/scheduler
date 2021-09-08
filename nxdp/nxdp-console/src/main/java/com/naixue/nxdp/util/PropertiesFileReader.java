package com.naixue.nxdp.util;

import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class PropertiesFileReader {

    public static Properties read(String configFilePath) {
        try {
            return PropertiesLoaderUtils.loadProperties(new ClassPathResource(configFilePath));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
