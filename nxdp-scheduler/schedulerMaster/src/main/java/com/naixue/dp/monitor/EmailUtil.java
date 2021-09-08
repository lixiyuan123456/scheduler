package com.naixue.dp.monitor;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

/**
 * Created by sunzhiwei on 2018/1/25.
 */

public class EmailUtil {

    /**
     * 发送邮件方法
     * @param emailList
     */
//    public static void sendDataMail(List<MailBo> emailList) {
//        String host = "smtp.exmail.qq.com"; //smtp服务器
//        String user = "zeye@zhuanzhuan.com"; //用户名
//        String pwd = "LHMLvpxJZfgMMYAz"; //密码
//        String sendAddress = "zeye@zhuanzhuan.com";//发送地址
//
//        Properties prop = new Properties();
//        // 设置发送邮件的邮件服务器的属性enmj
//        prop.put("mail.smtp.host", host);
//        prop.put("mail.smtp.port", "25");
//        // 需要经过授权，也就是有户名和密码的校验，这样才能通过验证（一定要有这一条）
//        prop.put("mail.smtp.auth", "true");
//
//        // 用刚刚设置好的props对象构建一个session
//        Session session = Session.getInstance(prop);
//        // 有了这句便可以在发送邮件的过程中在console处显示过程信息，供调试使用（你可以在控制台（console)上看到发送邮件的过程）
//        session.setDebug(true);
//
//        for (MailBo mail : emailList) {
//            // 用session为参数定义消息对象
//            MimeMessage message = new MimeMessage(session);
//
//            try {
//                String[] allAddress = new String[mail.getUserList().size()];
//
//                for (int i = 0; i < allAddress.length; i++) {
//                    allAddress[i] = mail.getUserList().get(i);
//                }
//
//                String toList = getMailList(allAddress);
//                InternetAddress[] iaToList = new InternetAddress().parse(toList);
//
//                //加载发件人地址
//                message.setFrom(new InternetAddress(sendAddress));
//                //加载收件人地址
//                message.setRecipients(Message.RecipientType.TO, iaToList);
//                //加载标题
//                message.setSubject(mail.getSubject());
//                message.setSentDate(new Date());
//
//                //给消息对象设置内容向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
//                BodyPart mdp = new MimeBodyPart();//新建一个存放信件内容的BodyPart对象
//                String msg = mail.getSubject() + ":昨日数据" + mail.getNewValue() + "和时间" + mail.getDate() + "的数据" + mail.getOldValue() + "的比值为" + mail.getRatio() + ",已经超出预期波动指标" + mail.getCondition() + "。请您查看！";
//                mdp.setContent(msg, "text/html;charset=gb2312");//给BodyPart对象设置内容和格式/编码方式
//                Multipart mm = new MimeMultipart();//新建一个MimeMultipart对象用来存放BodyPart对象(事实上可以存放多个)
//                mm.addBodyPart(mdp);// 将BodyPart加入到MimeMultipart对象中(可以加入多个BodyPart)
//                //将multipart对象放到message中
//                message.setContent(mm);// 把mm作为消息对象的内容
//                //保存邮件
//                message.saveChanges();
//
//                //发送邮件
//                Transport transport = session.getTransport("smtp");
//                //连接服务器的邮箱
//                transport.connect(host, user, pwd);
//                //把邮件发送出去
//                transport.sendMessage(message, message.getAllRecipients());
//                transport.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    /**
     * 获取收件人列表
     * @param mailArray
     * @return
     */
    public static String getMailList(String[] mailArray){
        StringBuffer toList = new StringBuffer();
        int length = mailArray.length;

        for (int i = 0; i < length; i++){
            if (i == 0){
                toList.append(mailArray[i]);
            } else {
                toList.append(",").append(mailArray[i]);
            }
        }
//        if (mailArray != null && length < 2) {
//            toList.append(mailArray[0]);
//        } else {
//            for (int i = 0; i < length; i++) {
//                toList.append(mailArray[i]);
//                if (i != length - 1) {
//                    toList.append(",");
//                }
//            }
//        }

        return toList.toString();
    }

    public static void sendMailNormal(String address,String title,String context) {
        //smtp服务器
        String host = "smtp.exmail.qq.com";
        //用户名
        String user = "zeye@zhuanzhuan.com";
        //密码
        String pwd = "LHMLvpxJZfgMMYAz";
        //发送地址
        String sendAddress = "zeye@zhuanzhuan.com";

        Properties prop = new Properties();
        // 设置发送邮件的邮件服务器的属性enmj
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.port", "25");
        // 需要经过授权，也就是有户名和密码的校验，这样才能通过验证（一定要有这一条）
        prop.put("mail.smtp.auth", "true");
        //部分邮件地址错误，仍然发送
        prop.put("mail.smtp.sendpartial", "true");
        // 用刚刚设置好的props对象构建一个session
        Session session = Session.getInstance(prop);
        // 有了这句便可以在发送邮件的过程中在console处显示过程信息，供调试使用（你可以在控制台（console)上看到发送邮件的过程）
        session.setDebug(true);

        // 用session为参数定义消息对象
        MimeMessage message = new MimeMessage(session);

        try {
            InternetAddress[] iaToList = InternetAddress.parse(address);

            //加载发件人地址
            message.setFrom(new InternetAddress(sendAddress));
            //加载收件人地址
            message.setRecipients(Message.RecipientType.TO, iaToList);
            //加载标题
            message.setSubject(title);
            message.setSentDate(new Date());

            //给消息对象设置内容向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
            //新建一个存放信件内容的BodyPart对象
            BodyPart mdp = new MimeBodyPart();
            //给BodyPart对象设置内容和格式/编码方式
            mdp.setContent(context, "text/html;charset=gb2312");
            //新建一个MimeMultipart对象用来存放BodyPart对象(事实上可以存放多个)
            Multipart mm = new MimeMultipart();
            // 将BodyPart加入到MimeMultipart对象中(可以加入多个BodyPart)
            mm.addBodyPart(mdp);
            //将multipart对象放到message中
            // 把mm作为消息对象的内容
            message.setContent(mm);
            //保存邮件
            message.saveChanges();

            //发送邮件
            Transport transport = session.getTransport("smtp");
            //连接服务器的邮箱
            transport.connect(host, user, pwd);
            //把邮件发送出去
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 发送邮件公用方法
     * @param addressList 收件人地址list
     * @param title 邮件标题
     * @param context 邮件内容
     */
    public static void sendMailNormal(List<String> addressList,String title,String context) {
        //smtp服务器
        String host = "smtp.exmail.qq.com";
        //用户名
        String user = "zeye@zhuanzhuan.com";
        //密码
        String pwd = "LHMLvpxJZfgMMYAz";
        //发送地址
        String sendAddress = "zeye@zhuanzhuan.com";

        Properties prop = new Properties();
        // 设置发送邮件的邮件服务器的属性enmj
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.port", "25");
        // 需要经过授权，也就是有户名和密码的校验，这样才能通过验证（一定要有这一条）
        prop.put("mail.smtp.auth", "true");
        //部分邮件地址错误，仍然发送
        prop.put("mail.smtp.sendpartial", "true");
        // 用刚刚设置好的props对象构建一个session
        Session session = Session.getInstance(prop);
        // 有了这句便可以在发送邮件的过程中在console处显示过程信息，供调试使用（你可以在控制台（console)上看到发送邮件的过程）
        session.setDebug(true);

        // 用session为参数定义消息对象
        MimeMessage message = new MimeMessage(session);

        try {
            String[] allAddress = new String[addressList.size()];

            for (int i = 0; i < allAddress.length; i++) {
                allAddress[i] = addressList.get(i);
            }

            String toList = getMailList(allAddress);
            InternetAddress[] iaToList = InternetAddress.parse(toList);

            //加载发件人地址
            message.setFrom(new InternetAddress(sendAddress));
            //加载收件人地址
            message.setRecipients(Message.RecipientType.TO, iaToList);
            //加载标题
            message.setSubject(title);
            message.setSentDate(new Date());

            //给消息对象设置内容向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
            //新建一个存放信件内容的BodyPart对象
            BodyPart mdp = new MimeBodyPart();
            //给BodyPart对象设置内容和格式/编码方式
            mdp.setContent(context, "text/html;charset=gb2312");
            //新建一个MimeMultipart对象用来存放BodyPart对象(事实上可以存放多个)
            Multipart mm = new MimeMultipart();
            // 将BodyPart加入到MimeMultipart对象中(可以加入多个BodyPart)
            mm.addBodyPart(mdp);
            //将multipart对象放到message中
            // 把mm作为消息对象的内容
            message.setContent(mm);
            //保存邮件
            message.saveChanges();

            //发送邮件
            Transport transport = session.getTransport("smtp");
            //连接服务器的邮箱
            transport.connect(host, user, pwd);
            //把邮件发送出去
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送邮件公用方法 带返回值  ok是正确  error是错误
     * @param addressList 收件人地址list
     * @param title 邮件标题
     * @param context 邮件内容
     */
    public static String sendMailNormalWithState(List<String> addressList,String title,String context) {

        String rs = "error";

        String host = "smtp.exmail.qq.com"; //smtp服务器
        String user = "zeye@zhuanzhuan.com"; //用户名
        String pwd = "LHMLvpxJZfgMMYAz"; //密码
        String sendAddress = "zeye@zhuanzhuan.com";//发送地址

        Properties prop = new Properties();
        // 设置发送邮件的邮件服务器的属性enmj
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.port", "25");
        // 需要经过授权，也就是有户名和密码的校验，这样才能通过验证（一定要有这一条）
        prop.put("mail.smtp.auth", "true");
        //部分邮件地址错误，仍然发送
        prop.put("mail.smtp.sendpartial", "true");
        // 用刚刚设置好的props对象构建一个session
        Session session = Session.getInstance(prop);
        // 有了这句便可以在发送邮件的过程中在console处显示过程信息，供调试使用（你可以在控制台（console)上看到发送邮件的过程）
        session.setDebug(true);

        // 用session为参数定义消息对象
        MimeMessage message = new MimeMessage(session);

        try {
            String[] allAddress = new String[addressList.size()];

            for (int i = 0; i < allAddress.length; i++) {
                allAddress[i] = addressList.get(i);
            }

            String toList = getMailList(allAddress);
            InternetAddress[] iaToList = new InternetAddress().parse(toList);

            //加载发件人地址
            message.setFrom(new InternetAddress(sendAddress));
            //加载收件人地址
            message.setRecipients(Message.RecipientType.TO, iaToList);
            //加载标题
            message.setSubject(title);
            message.setSentDate(new Date());

            //给消息对象设置内容向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
            BodyPart mdp = new MimeBodyPart();//新建一个存放信件内容的BodyPart对象
            mdp.setContent(context, "text/html;charset=gb2312");//给BodyPart对象设置内容和格式/编码方式
            Multipart mm = new MimeMultipart();//新建一个MimeMultipart对象用来存放BodyPart对象(事实上可以存放多个)
            mm.addBodyPart(mdp);// 将BodyPart加入到MimeMultipart对象中(可以加入多个BodyPart)
            //将multipart对象放到message中
            message.setContent(mm);// 把mm作为消息对象的内容
            //保存邮件
            message.saveChanges();

            //发送邮件
            Transport transport = session.getTransport("smtp");
            //连接服务器的邮箱
            transport.connect(host, user, pwd);
            //把邮件发送出去
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            rs = "ok";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    /**
     * 发送邮件公用方法  带附件 带返回值  ok是正确  error是错误
     * @param addressList 收件人地址list
     * @param title 邮件标题
     * @param context 邮件内容
     */
    public static String sendMailNormalWithStateFile(List<String> addressList,String title,String context,List<String> filePathList) {

        String rs = "error";

        String host = "smtp.exmail.qq.com"; //smtp服务器
        String user = "zeye@zhuanzhuan.com"; //用户名
        String pwd = "LHMLvpxJZfgMMYAz"; //密码
        String sendAddress = "zeye@zhuanzhuan.com";//发送地址

        Properties prop = new Properties();
        // 设置发送邮件的邮件服务器的属性enmj
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.port", "25");
        // 需要经过授权，也就是有户名和密码的校验，这样才能通过验证（一定要有这一条）
        prop.put("mail.smtp.auth", "true");
        //部分邮件地址错误，仍然发送
        prop.put("mail.smtp.sendpartial", "true");
        // 用刚刚设置好的props对象构建一个session
        Session session = Session.getInstance(prop);
        // 有了这句便可以在发送邮件的过程中在console处显示过程信息，供调试使用（你可以在控制台（console)上看到发送邮件的过程）
        session.setDebug(true);

        // 用session为参数定义消息对象
        MimeMessage message = new MimeMessage(session);

        try {
            String[] allAddress = new String[addressList.size()];

            for (int i = 0; i < allAddress.length; i++) {
                allAddress[i] = addressList.get(i);
            }

            String toList = getMailList(allAddress);
            InternetAddress[] iaToList = new InternetAddress().parse(toList);

            //加载发件人地址
            message.setFrom(new InternetAddress(sendAddress));
            //加载收件人地址
            message.setRecipients(Message.RecipientType.TO, iaToList);
            //加载标题
            message.setSubject(title);
            message.setSentDate(new Date());

            //给消息对象设置内容向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
            BodyPart mdp = new MimeBodyPart();//新建一个存放信件内容的BodyPart对象
            mdp.setContent(context, "text/html;charset=gb2312");//给BodyPart对象设置内容和格式/编码方式
            Multipart mm = new MimeMultipart();//新建一个MimeMultipart对象用来存放BodyPart对象(事实上可以存放多个)
            mm.addBodyPart(mdp);// 将BodyPart加入到MimeMultipart对象中(可以加入多个BodyPart)

            for(String filePath:filePathList){
                MimeBodyPart attch = new MimeBodyPart();
                DataSource ds = new FileDataSource(new File(filePath));
                DataHandler dh = new DataHandler(ds);
                attch.setDataHandler(dh);
                attch.setFileName(MimeUtility.encodeText(filePath.split("\\/")[filePath.split("\\/").length-1]));
                mm.addBodyPart(attch);
            }

            //将multipart对象放到message中
            message.setContent(mm);// 把mm作为消息对象的内容
            //保存邮件
            message.saveChanges();

            //发送邮件
            Transport transport = session.getTransport("smtp");
            //连接服务器的邮箱
            transport.connect(host, user, pwd);
            //把邮件发送出去
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            rs = "ok";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }
}