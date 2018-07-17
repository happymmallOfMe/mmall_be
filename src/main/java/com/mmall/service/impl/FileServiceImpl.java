package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author Huanyu
 * @date 2018/4/24
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {
    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String upload(MultipartFile file, String path) {
        String fileName = file.getOriginalFilename();

        // 获取文件扩展名，如"jpg"
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 修改要上传文件的名称，避免上传文件因名称相同而覆盖
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("开始上传文件，上传文件的文件名：{}，上传的路径：{}，新文件名：{}", fileName, path, uploadFileName);

        File fileDir = new File(path);
        if (!fileDir.exists()) {
            // 赋予文件夹可写入的权限
            fileDir.setWritable(true);
            // 创建目录
            fileDir.mkdirs();
        }
        // 创建文件
        File targetFile = new File(path, uploadFileName);
        boolean isUpload;
        try {
            file.transferTo(targetFile);
            // 将文件上传到ftp服务器上
            isUpload = FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            // 删除upload目录下面的文件
            targetFile.delete();

        } catch (IOException e) {
            logger.error("上传文件异常", e);
            return null;
            // 因为uploadFile方法使用的是不将异常抛出的方式，所以上面的try语句块不会出现异常，即就是不会进入该catch语句块，
            // 修改建议：
            //          1.将uploadFile方法的异常抛出，在此本方法中处理，
            //          2.利用前面result的结果来判断返回targetFile.getName()还是null。
            // 已经做了对应修改
        }
        return isUpload ? targetFile.getName() : null;
    }
}
