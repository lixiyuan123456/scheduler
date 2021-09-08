package com.naixue.spear;

public class Main {

    public static void main(String[] args) {
        ShellUtils.exec(
                // "sh",
                // "-c",
                // "kinit -kt /etc/krb5.keytab && ssh work@10.148.16.86 tail -c +0 -f
                // /opt/log/zzdp/log/script_log_713332_process_info"
                "ping", "www.baidu.com");
    }
}
