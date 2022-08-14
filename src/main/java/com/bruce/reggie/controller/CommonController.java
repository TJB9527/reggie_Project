package com.bruce.reggie.controller;

import com.bruce.reggie.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        log.info(file.toString());

        //原始文件名
        String originalFilename = file.getOriginalFilename();
        //动态获取原始文件名的后缀，如xxx.jpg
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用UUID重新生成文件名，防止文件名重复造成文件覆盖
        String fileName = UUID.randomUUID().toString()+suffix;

        //判断配置文件中配置的转存图片的目录是否存在，不存在则创建
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        //将临时文件转存到指定位置
        //上传文件逻辑：前端通过UI组件点击图片上传
        //注：形参file是由前端上传图片的同时向后端发送请求后端得以接收到的，但接收到的只是一个临时文件，该临时文件会默认存储到c盘一目录下，程序往后执行该临时文件就会被删除，
        //因此后端需要将该临时文件转存到其他位置以便使用，因此关键在于设置存储目录以及设置图片的文件名(不能使用原文件名，会面传的图片文件名相同就会出现覆盖)
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Result.success(fileName);  //向前端返回后端设定的图片文件名
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){

        try {
            //输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            //输出流，通过输出流将文件写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
