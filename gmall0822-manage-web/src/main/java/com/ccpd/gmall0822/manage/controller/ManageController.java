package com.ccpd.gmall0822.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ccpd.gmall0822.bean.*;
import com.ccpd.gmall0822.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin//解决跨域问题
public class ManageController {

    @Reference
    ManageService manageService;

    @PostMapping("getCatalog1")
    public List<BaseCatalog1> getBaseCatalog1(){
        return manageService.getCatalog1();
    }

    @PostMapping("getCatalog2")
    public List<BaseCatalog2> getBaseCatalog2(String catalog1Id){
        return manageService.getCatalog2(catalog1Id);
    }

    @PostMapping("getCatalog3")
    public List<BaseCatalog3> getBaseCatalog3(String catalog2Id){
        return manageService.getCatalog3(catalog2Id);
    }

    @GetMapping("attrInfoList")
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
        return manageService.getAttrList(catalog3Id);
    }

    @PostMapping("saveAttrInfo")
    public String saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
        return "success";
    }

    @PostMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){

        return manageService.getAttrInfo(attrId).getAttrValueList() ;
    }

    @PostMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException, MyException {
        String configPath = this.getClass().getResource("/tracker.conf").getFile();
        //初始化
        ClientGlobal.init(configPath);
        //创建Tracker客户端
        TrackerClient trackerClient = new TrackerClient();
        //创建Tracker服务端
        TrackerServer trackerServer = trackerClient.getTrackerServer();
        //创建Storage客户端
        StorageClient storageClient = new StorageClient(trackerServer,null);
        //图片名称
        String imageName = file.getOriginalFilename();
        //上传图片后缀
        String imageBack = StringUtils.substringAfterLast(imageName,".");
        //上传文件并获得返回值
        String[] upload_file = storageClient.upload_file(file.getBytes(), imageBack, null);
        //服务器图片地址
        String imagePath = "http://file.gmall.com";
        //输出文件路径
        for (int i = 0; i < upload_file.length; i++) {
            imagePath += "/"+ upload_file[i];
        }
        return imagePath;
    }
    @GetMapping("spuList")
    public List<SpuInfo> getSpuList(String catalog3Id){
        return manageService.getSpuInfoList(catalog3Id);
    }

    @PostMapping("baseSaleAttrList")
    public List<BaseSaleAttr> baseSaleAttrList(){
        return manageService.getBaseSaleAttrList();
    }

    @PostMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return "success";
    }

    @GetMapping("spuSaleAttrList")
    public List<SpuSaleAttr> spuSaleAttrList(String spuId){
        return manageService.getSpuSaleAttrList(spuId);
    }

    @GetMapping("spuImageList")
    public List<SpuImage> spuImageList(String spuId){
        return manageService.getSpuImageList(spuId);
    }
}
