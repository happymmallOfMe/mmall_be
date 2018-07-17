package com.mmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * ftp服务器工具类
 *
 * @author Huanyu
 * @date 2018/4/24
 */
public class FTPUtil {
    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");
    private static String ftpImgPath = PropertiesUtil.getProperty("ftp.server.image.path");

    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;

    public FTPUtil(String ip, int port, String user, String pwd) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp, 21, ftpUser, ftpPass);
        logger.info("开始连接ftp服务器");
        // 注意下面uploadFile方法的第一个参数为“img”，是为了与nginx配置文件中的root指向地址(具体为/product/ftpfile/img)呼应，
        // 否则文件会被上传至/product/ftpfile目录下。
        boolean result = ftpUtil.uploadFile(ftpImgPath, fileList);
        logger.info(result ? "开始连接ftp服务器，结束上传，上传成功" : "连接ftp服务器或上传文件异常");
        return result;
    }

    // 返回情况已经修改（与老师代码相比）
    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException {
        boolean upload = false;
        FileInputStream fileInputStream = null;
        // 连接ftp服务器
        if (connectServer(this.ip, this.port, this.user, this.pwd)) {
            try {
                ftpClient.changeWorkingDirectory(remotePath);
                // 设置缓冲区
                ftpClient.setBufferSize(1024);
                // 设置encoding
                ftpClient.setControlEncoding("UTF-8");
                // 设置文件类型，以防止乱码出现
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                // 打开本地被动模式
                ftpClient.enterLocalPassiveMode();
                for (File fileItem : fileList) {
                    fileInputStream = new FileInputStream(fileItem);
                    ftpClient.storeFile(fileItem.getName(), fileInputStream);
                }
                upload = true;
            } catch (IOException e) {
                logger.error("上传文件异常", e);
                upload = false;
                e.printStackTrace();
            }
            finally {
                fileInputStream.close();
                ftpClient.disconnect();
            }
        }
        return upload;
    }

    /**
     * 连接ftp服务器
     * @param ip ftp IP地址 // TODO: 2018/5/21 考虑IP地址为何可以不使用
     * @param user 客户端用户名
     * @param pwd 对应密码
     * @return boolean类型值，表示连接是否成功
     */
    private boolean connectServer(String ip, int port, String user, String pwd) {
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user, pwd);
        } catch (IOException e) {
            logger.error("连接ftp服务器异常", e);
        }
        return isSuccess;
    }

    public static String getFtpIp() {
        return ftpIp;
    }

    public static void setFtpIp(String ftpIp) {
        FTPUtil.ftpIp = ftpIp;
    }

    public static String getFtpUser() {
        return ftpUser;
    }

    public static void setFtpUser(String ftpUser) {
        FTPUtil.ftpUser = ftpUser;
    }

    public static String getFtpPass() {
        return ftpPass;
    }

    public static void setFtpPass(String ftpPass) {
        FTPUtil.ftpPass = ftpPass;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
