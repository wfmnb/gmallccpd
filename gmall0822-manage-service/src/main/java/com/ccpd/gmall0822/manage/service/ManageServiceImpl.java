package com.ccpd.gmall0822.manage.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.ccpd.gmall0822.bean.*;
import com.ccpd.gmall0822.manage.mapper.*;
import com.ccpd.gmall0822.service.ManageService;
import com.ccpd.gmall0822.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ManageServiceImpl implements ManageService{

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    public static final String SKUKEY_PREFIX = "sku:";
    public static final String SKUKEY_INFO_SUFFIX = ":info";
    public static final String SKUKEY_LOCK_SUFFIX = ":locked";

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select(baseCatalog2);
        return baseCatalog2List;
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select(baseCatalog3);
        return baseCatalog3List;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);
    }

    @Override
    @Transactional//开启事务
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if(baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0){
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }else{
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        Example example = new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId",baseAttrInfo.getId());
        baseAttrValueMapper.deleteByExample(example);
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : attrValueList){
            baseAttrValue.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.insertSelective(baseAttrValue);
        }
    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.select(baseAttrValue);
        baseAttrInfo.setAttrValueList(baseAttrValues);
        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> getSpuInfoList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
        //保存Spu
        spuInfoMapper.insert(spuInfo);

        //保存Spu图片
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insertSelective(spuImage);
        }
        //保存Spu销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insertSelective(spuSaleAttr);
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            //保存销售属性值
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }
        }
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuId);
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.select(spuSaleAttr);
        for (SpuSaleAttr saleAttr : spuSaleAttrList) {
            SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
            spuSaleAttrValue.setSpuId(spuId);
            spuSaleAttrValue.setSaleAttrId(saleAttr.getSaleAttrId());
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttrValueMapper.select(spuSaleAttrValue);
            saleAttr.setSpuSaleAttrValueList(spuSaleAttrValueList);
        }
        return spuSaleAttrList;
    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        Example example = new Example(SpuImage.class);
        example.createCriteria().andEqualTo("spuId",spuId);
        return spuImageMapper.selectByExample(example);
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
        if(skuInfo.getId() != null && skuInfo.getId().length() > 0){
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }else{
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        }

        for (SkuAttrValue skuAttrValue : skuInfo.getSkuAttrValueList()) {
            skuAttrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }

        for (SkuImage skuImage : skuInfo.getSkuImageList()) {
            skuImage.setSkuId(skuInfo.getId());
            skuImageMapper.insertSelective(skuImage);
        }

        for (SkuSaleAttrValue skuSaleAttrValue : skuInfo.getSkuSaleAttrValueList()) {
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }

    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {

        SkuInfo skuInfoResult = null;
        //过期时间
        int SKU_EXPIRE_SEC = 100;
        Jedis jedis = redisUtil.getJedis();
        String skuKey = SKUKEY_PREFIX+skuId+SKUKEY_INFO_SUFFIX;
        String skuJson = jedis.get(skuKey);
        if(skuJson != null)
        {
            if(!"EMPTY".equals(skuJson)){
                skuInfoResult = JSON.parseObject(skuJson,SkuInfo.class);
            }
        }else{
            //new一个redisson config
            Config config = new Config();
            //设置reids服务器地址
            config.useSingleServer().setAddress("redis://redis.gmall.com:6379");
            //获取redisson客户端
            RedissonClient redissonClient = Redisson.create(config);
            String skuKeyLock = SKUKEY_PREFIX+skuId+SKUKEY_LOCK_SUFFIX;
            //创建分布式锁
            RLock lock = redissonClient.getLock(skuKeyLock);
            boolean locked = false;
            try {
                //10秒内获取不到锁，则直接返回。 第二个参数 5秒后强制释放
                locked = lock.tryLock(10,5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(locked){
                String resultJson = jedis.get(skuKey);
                if(resultJson != null)
                {
                    if(!"EMPTY".equals(resultJson)){
                        return JSON.parseObject(resultJson,SkuInfo.class);
                    }
                }else{
                    skuInfoResult = getSkuInfoDB(skuId);
                    if(skuInfoResult != null){
                        resultJson = JSON.toJSONString(skuInfoResult);
                    }else{
                        resultJson = "EMPTY";
                    }
                    jedis.setex(skuKey,SKU_EXPIRE_SEC,resultJson);
                }
                lock.unlock();
            }
        }
        jedis.close();
        return skuInfoResult;
    }

    public SkuInfo getSkuInfoDB(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        if(skuInfo == null) return null;

        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);

        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.select(skuSaleAttrValue);

        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);

        skuInfo.setSkuAttrValueList(skuAttrValueList);
        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);
        skuInfo.setSkuImageList(skuImageList);
        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListBySpuIdChecked(String skuId, String spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrValueListAndCheck(skuId,spuId);
    }

    @Override
    public Map getSkuValueIdsMap(String spuId) {
        Map map = new HashMap();
        List<Map> skuSaleAttrValueList = skuSaleAttrValueMapper.getSkuSaleAttrValueList(spuId);
        for (Map map1 : skuSaleAttrValueList) {
            String skuId = map1.get("sku_id").toString();
            String valueIds = map1.get("value_ids").toString();
            map.put(valueIds,skuId);
        }
        return map;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {
        return baseAttrInfoMapper.selectAttrInfoListByIds(StringUtils.join(attrValueIdList,","));
    }
}
