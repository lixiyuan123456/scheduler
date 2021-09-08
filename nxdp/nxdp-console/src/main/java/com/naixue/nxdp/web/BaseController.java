package com.naixue.nxdp.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.model.User;
import org.springframework.web.util.WebUtils;

import com.google.common.base.Strings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class BaseController {

    protected Object success() {
        return success(null, null, null);
    }

    protected Object success(String dataKey, Object dataValue) {
        return success(null, dataKey, dataValue);
    }

    protected Object success(Object dataValue) {
        return success(null, null, dataValue);
    }

    protected Object success(String status, String dataKey, Object dataValue) {
        Map<String, Object> data = new HashMap<>();
        if (Strings.isNullOrEmpty(status)) {
            data.put("status", "ok");
        } else {
            data.put("status", status);
        }
        if (Strings.isNullOrEmpty(dataKey)) {
            data.put("data", dataValue);
        } else {
            data.put(dataKey, dataValue);
        }
        return data;
    }

    protected User getCurrentUser(HttpServletRequest request) {
        return (User)
                WebUtils.getSessionAttribute(request, Web.HTTP_SESSION_ATTRIBUTE_KEY_CURRENT_USER);
    }

    public static class DataTableRequest<T> {
        @Getter
        @Setter
        private Integer start = 0;
        @Getter
        @Setter
        private Integer length = 0;
        @Getter
        @Setter
        private Integer draw = 0;
        @Getter
        @Setter
        private String condition;
        @Getter
        @Setter
        private T queryCondition;

    /*@SuppressWarnings("unchecked")
    public DataTableRequest() {
      try {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
          this.queryCondition = new Object();
        } else {
          Type type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
          Class<T> clazz;
          if (type instanceof ParameterizedType) {
            clazz = (Class<T>) ((ParameterizedType) type).getRawType();
          } else {
            clazz = (Class<T>) type;
          }
          this.queryCondition = clazz.newInstance();
        }
      } catch (Exception e) {
        throw new RuntimeException(e.getMessage());
      }
    }*/

        @Getter
        @Setter
        private Map<String, String> search;

        public static enum Search {
            value,
            regex;

            @Override
            public String toString() {
                return this.name();
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class DataTableResponse<T> {

        private Integer start;

        private Integer pageSize;

        private Integer draw;

        private Long recordsFiltered;

        private Long recordsTotal;

        private List<T> data;
    }

    public static class Web {

        public static final String DOMAIN = "zzdp.zhuanspirit.com";

        public static final String HTTP_SESSION_ATTRIBUTE_KEY_CURRENT_USER = "current_user";

        public static final String HTTP_SESSION_ATTRIBUTE_KEY_MAIN_MENUS = "main_menus";

        public static final String HTTP_REQUEST_ATTRIBUTE_KEY_MAIN_MENUS = "main_menus";

        public static final String HTTP_COOKIE_KEY_SSO_UID = "sso_uid";

        public static final String HTTP_COOKIE_KEY_CURRENT_USER = "current_user";
    }
}
