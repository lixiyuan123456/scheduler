package com.naixue.nxdp.attachment.hue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Data;

@Data
@Configuration
@PropertySource(value = {"classpath:/profiles/${spring.profiles.active}/iQuery.properties"})
public class IQueryConfiguration {

    @Value("${iQuery_default_permissions_code}")
    private String iQuerydefaultPermissionsCode;
}
