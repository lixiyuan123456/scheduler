package com.naixue.nxdp.attachment.goshawk.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.naixue.nxdp.attachment.goshawk.dao.mapper.ClusterYarnMapper;
import com.naixue.nxdp.attachment.goshawk.service.IAsyncService;
import com.naixue.nxdp.attachment.goshawk.util.CheckHdfsType;
import com.naixue.nxdp.attachment.goshawk.util.HdfsClient;
import com.naixue.zzdp.common.util.ShellUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@PropertySource(value = {"classpath:/profiles/${spring.profiles.active}/goshawk.properties"})
public class AsyncServiceImpl implements IAsyncService {

    @Value("${fs.default.name}")
    private String fs_default_name;

    @Value("${hadoop.queuename}")
    private String hadoop_queuename;

    @Value("${hadoop.home.dir}")
    private String hadoop_home_dir;

    @Value("${merge.little.dir}")
    private String merge_little_dir;

    @Value("${parq.hive.site.dir}")
    private String parq_hive_site_dir;

    @Value("${little_dir.text.size}")
    private String little_dir_text_size;

    @Value("${little_dir.lzo.size}")
    private String little_dir_lzo_size;

    @Autowired
    private ClusterYarnMapper clusterYarnMapper;

    public static void main(String[] args) {
        // System.setProperty("hadoop.home.dir", "D:\\software\\hadoop-2.2.0");
        Configuration config = new Configuration();
        config.set("fs.default.name", "hdfs://192.168.171.175:8020");
        FileSystem fs = null;
        try {
            fs = FileSystem.get(new URI("hdfs://192.168.171.175:8020"), config, "work");
        } catch (InterruptedException | URISyntaxException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Path path = new Path("/tmp/");
    /*try {
    	fs.delete(path, true);
    } catch (IOException e1) {
    	e1.printStackTrace();
    }*/

        try {
            FileStatus[] files = fs.listStatus(path);
            for (int i = 0; i < files.length; i++) {
                String pathStr = files[i].getPath().toString();
                pathStr = pathStr.substring(pathStr.lastIndexOf("/") + 1);
                System.out.println(pathStr);
                // boolean flag = fs.isDirectory(files[i].getPath());
        /*ContentSummary contentSummary = fs.getContentSummary(files[i].getPath());
        long directoryCount = contentSummary.getDirectoryCount();
        System.out.println(files[i].getPath()+"???"+directoryCount);
        if(directoryCount>0) {

        	FileStatus[] files2 = fs.listStatus(new Path(files[i].getPath()+"/"));
        	for (int j = 0; j < files2.length; j++) {
        		boolean directory = fs.isDirectory(files2[j].getPath());
        		System.out.println("-----"+files2[j].getPath()+"  :   "+directory);
        	}
        }*/
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws Exception
     * @author wangkaixuan
     * @date 2018???12???17??? @Description:???????????????
     */
    @Override
    @Async("taskExecutor")
    public void delColdDir(List<Map<String, Object>> pathList) throws Exception {
        delColdDir(pathList, false);
    }

    @Override
    @Async("taskExecutor")
    public void delColdDir(List<Map<String, Object>> pathList, boolean force) throws Exception {
        if (CollectionUtils.isEmpty(pathList)) {
            return;
        }
        // System.setProperty("hadoop.home.dir", hadoop_home_dir);
        Configuration config = new Configuration();
        config.set("fs.default.name", fs_default_name);
        FileSystem fs = FileSystem.get(new URI(fs_default_name), config, "zdp");
        for (Iterator<Map<String, Object>> iterator = pathList.iterator(); iterator.hasNext(); ) {
            Map<String, Object> pathMap = (Map<String, Object>) iterator.next();
            Path path = new Path(pathMap.get("dir").toString());
            ContentSummary contentSummary;
            try {
                contentSummary = fs.getContentSummary(path);
                long directoryCount = contentSummary.getDirectoryCount();
                // ???????????????????????????
                Integer status = 2;
                String message = "";
                // ???????????????
                if (directoryCount > 1) {
                    if (force) {
                        boolean delete = fs.delete(path, true);
                        if (delete) {
                            status = 2;
                        } else {
                            status = 3;
                            message = "???????????????";
                        }
                    } else {
                        status = 3;
                        message = "???????????????";
                    }
                } else {
                    // ?????????????????????????????????
                    boolean delete = fs.delete(path, true);
                    if (delete) {
                        status = 2;
                    } else {
                        status = 3;
                        message = "???????????????";
                    }
                }
                clusterYarnMapper.updateColdDataById(status, message, pathMap.get("id").toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                clusterYarnMapper.updateColdDataById(3, "???????????????", pathMap.get("id").toString());
            } catch (IOException e) {
                e.printStackTrace();
                clusterYarnMapper.updateColdDataById(3, "???????????????", pathMap.get("id").toString());
            }
        }
        if (fs != null) {
            fs.close();
        }
    }

    /**
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     * @author wangkaixuan
     * @date 2018???12???21??? @Description:???????????????
     */
    @Override
    @Async("taskExecutor")
    public void mergeLittleDir(List<Map<String, Object>> pathList) throws Exception {
        if (CollectionUtils.isEmpty(pathList)) {
            return;
        }
        System.setProperty("hadoop.home.dir", hadoop_home_dir);
        Configuration config = new Configuration();
        config.set("fs.default.name", fs_default_name);
        FileSystem fs = FileSystem.get(new URI(fs_default_name), config, "zdp");
        for (Iterator<Map<String, Object>> iterator = pathList.iterator(); iterator.hasNext(); ) {
            Map<String, Object> map = (Map<String, Object>) iterator.next();
            Integer isMerge = (Integer) map.get("is_merge");
            Integer id = (Integer) map.get("id");
            if (isMerge == 2) {
                continue;
            }
            String dir = map.get("dir").toString();
            try {
        /*boolean checkFile = HdfsClient.checkFile(dir);
        if(!checkFile) {
        	clusterYarnMapper.updateLittleDirById(3, "???????????? ", id.toString());
        	continue;
        }*/
                // ???????????????????????????
                String fileSize = "100";
                Path path = new Path(dir + "/");
                String owner = fs.getFileStatus(path).getOwner();
                ContentSummary contentSummary = fs.getContentSummary(path);
                long directoryCount = contentSummary.getDirectoryCount();
                // ???????????????????????????
                if (directoryCount > 1) {
                    // ?????????????????????????????????
                    clusterYarnMapper.updateLittleDirById(3, "???????????????", id.toString());
                    continue;
                }

                FileStatus[] files = fs.listStatus(path);
                String postfix = null;
                boolean isError = false;
                String error = "";

                for (int i = 0; i < files.length; i++) {
                    String pathStr = files[i].getPath().toString();
                    String dirName = pathStr.substring(pathStr.lastIndexOf("/") + 1);
                    if (dirName.startsWith("_") || dirName.startsWith(".")) {
                        continue;
                    } else {
                        if (dirName.endsWith(".lzo") || dirName.endsWith(".LZO")) {
                            if (postfix != null && !postfix.equals("LZO")) {
                                isError = true;
                                error = "???????????????????????????";
                                break;
                            } else if (postfix == null) {
                                postfix = "LZO";
                                fileSize = little_dir_lzo_size;
                            }
                            // break;
                        } else {
                            Path file = files[i].getPath();
                            FSDataInputStream in = fs.open(file);
                            if (CheckHdfsType.isSequenceFile(pathStr, in)) {
                                isError = true;
                                error = "????????????Sequence??????????????????????????????";
                                break;
                            } else if (CheckHdfsType.isParquetFile(pathStr, in)) {
                                if (postfix != null && !postfix.equals("PARQ")) {
                                    isError = true;
                                    error = "???????????????????????????";
                                    break;
                                } else if (postfix == null) {
                                    postfix = "PARQ";
                                    // fileSize = little_dir_lzo_size;
                                }
                            } else {
                                if (postfix != null && !postfix.equals("TEXT")) {
                                    isError = true;
                                    error = "???????????????????????????";
                                    break;
                                } else if (postfix == null) {
                                    postfix = "TEXT";
                                    fileSize = little_dir_text_size;
                                }
                            }
                            if (in != null) {
                                in.close();
                            }
                        }

            /*if(!dirName.contains(".")) {
            	//????????????parquet
            	Path file = files[i].getPath();
            	FSDataInputStream in = fs.open(file);
            	if(CheckHdfsType.isSequenceFile(pathStr, in)) {
            		isError = true;
            		error = "????????????Sequence??????????????????????????????";
            		break;
            	}else if(CheckHdfsType.isParquetFile(pathStr, in)) {
            		if(postfix!=null && !postfix.equals("PARQ")) {
            			isError = true;
            			error = "???????????????????????????";
            			break;
            		}else if(postfix==null){
            			postfix = "PARQ";
            			//fileSize = little_dir_lzo_size;
            		}
            	}else {
            		if(postfix!=null && !postfix.equals("TEXT")) {
            			isError = true;
            			error = "???????????????????????????";
            			break;
            		}else if(postfix==null){
            			postfix = "TEXT";
            			fileSize = little_dir_text_size;
            		}
            	}
            }else if(dirName.contains(".")) {
            	isError = true;
            	error = "??????????????????????????????????????????????????????";
            	break;
            }*/
                    }
                }

                if (StringUtils.isBlank(postfix) || isError) {
                    clusterYarnMapper.updateLittleDirById(3, error, id.toString());
                } else {
                    if (postfix.equals("PARQ")) {
                        String jarDir = merge_little_dir + "mergeparquet-1.0-SNAPSHOT.jar";
            /*String commond =
                "spark-submit --queue root.offline.dp --master yarn --class com.zz.merge.file.Main --num-executors 4"
                    + "   --executor-cores 5   --executor-memory 8g --files "
                    + "/opt/soft/zdp/spark/conf/hive-site.xml "
                    + jarDir
                    + " "
                    + dir
                    + " "
                    + dir
                    + ".merge";
            log.info("commond???" + commond);*/
                        String command =
                                ("export HADOOP_USER_NAME=" + owner)
                                        + " && "
                                        + ("spark-submit --queue root.offline.dp --master yarn --class com.zz.merge.file.Main --num-executors 4 --executor-cores 5 --executor-memory 8g --files ")
                                        + (" " + parq_hive_site_dir + " ")
                                        + (" " + jarDir + " ")
                                        + (" " + dir + " ")
                                        + (" " + dir + ".merge");
                        ShellUtils.exec(command);
                    } else {
                        String jarDir = merge_little_dir + "mergetextlzo-1.0-SNAPSHOT.jar";

                        // String classPath =
                        // this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
                        // String jarPath = classPath.substring(0,
                        // classPath.lastIndexOf("zzdp.jar"))+"mergetextlzo-1.0-SNAPSHOT.jar";
                        // System.out.println("MR???jar????????????"+jarPath);
                        String mergeCommand =
                                "hadoop jar"
                                        + (" " + jarDir + " ")
                                        + (" " + postfix + " ")
                                        + (" " + hadoop_queuename + " ")
                                        + (" " + dir + " ")
                                        + (" " + dir + ".merge" + " ")
                                        + fileSize
                                        + " /opt/web/jar/mergetextlzo-1.0-SNAPSHOT.jar";
                        String command = "export HADOOP_USER_NAME=" + owner + " && " + mergeCommand;
                        log.info("command???" + command);
                        ShellUtils.exec(command);
            /*ShellUtils.exec(
            "export",
            "HADOOP_USER_NAME=" + owner,
            "&&",
            "hadoop",
            "jar",
            jarDir,
            postfix,
            hadoop_queuename,
            dir,
            dir + ".merge",
            fileSize,
            "mergetextlzo-1.0-SNAPSHOT.jar");*/
                        // Integer mergeLittleDir = MergeLittleDir.mergeLittleDir(postfix, dir, dir+".merge",
                        // fileSize,fs_default_name,jarPath,hadoop_queuename);
                    }
                    Integer mergeLittleDir = 0;
                    if (mergeLittleDir == 0) {
                        // ????????????
                        clusterYarnMapper.updateLittleDirById(2, "", id.toString());
                        // TODO??????????????????????????????
                        // boolean deleteSmallFiles = HdfsClient.deleteSmallFiles(dir,fs);
                        boolean deleteSmallFiles = HdfsClient.mvPath(dir, dir + ".back", fs);
                        if (deleteSmallFiles) {
                            boolean mvPath = HdfsClient.mvPath(dir + ".merge", dir, fs);
                            if (!mvPath) {
                                clusterYarnMapper.updateLittleDirById(3, "????????????????????????mv????????????????????????", id.toString());
                            }
                        } else {
                            clusterYarnMapper.updateLittleDirById(3, "????????????????????????????????????????????????", id.toString());
                        }
                    } else {
                        clusterYarnMapper.updateLittleDirById(
                                3, "???????????????????????????,??????code???" + mergeLittleDir, id.toString());
                    }
                }
            } catch (FileNotFoundException e) {
                log.error(e.toString(), e);
                clusterYarnMapper.updateLittleDirById(3, "???????????????", id.toString());
            } catch (IOException e) {
                log.error(e.toString(), e);
                clusterYarnMapper.updateLittleDirById(3, "?????????????????????", id.toString());
            } catch (Exception e) {
                log.error(e.toString(), e);
                clusterYarnMapper.updateLittleDirById(3, "??????????????????", id.toString());
            }
        }
        if (fs != null) {
            fs.close();
        }
    }
}
