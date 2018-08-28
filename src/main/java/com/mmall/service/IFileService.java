package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件处理业务层接口
 *
 * @author Huanyu
 * @date 2018/4/24
 */
public interface IFileService {
    String upload(MultipartFile file, String path);
}
