package com.atguigu.gmall.manage.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.config.JedisUtil;
import com.atguigu.gmall.manage.constant.ManageConst;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class ManageServiceImpl implements ManageService {
    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private JedisUtil jedisUtil;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        List<BaseCatalog1> baseCatalog1List = this.baseCatalog1Mapper.selectAll();
        return baseCatalog1List;
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> baseCatalog2List = this.baseCatalog2Mapper.select(baseCatalog2);
        return baseCatalog2List;
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> baseCatalog3List = this.baseCatalog3Mapper.select(baseCatalog3);
        return baseCatalog3List;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
/*        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> baseAttrInfoList = this.baseAttrInfoMapper.select(baseAttrInfo);
        return baseAttrInfoList;*/

        return this.baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            this.baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        } else {
            this.baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }

        BaseAttrValue baseAttrValueDel = new BaseAttrValue();
        baseAttrValueDel.setAttrId(baseAttrInfo.getId());
        this.baseAttrValueMapper.delete(baseAttrValueDel);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        /*int k = 1/0;*/
        if (attrValueList != null && attrValueList.size() > 0) {
            for (int i = 0; i < attrValueList.size(); i++) {
                BaseAttrValue baseAttrValue = attrValueList.get(i);
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                this.baseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        List<BaseAttrValue> baseAttrValueList = this.baseAttrValueMapper.select(baseAttrValue);
        return baseAttrValueList;
    }


    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {

        BaseAttrInfo baseAttrInfo = this.baseAttrInfoMapper.selectByPrimaryKey(attrId);
        baseAttrInfo.setAttrValueList(getAttrValueList(attrId));

        return baseAttrInfo;
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
        //保存商品信息
        this.spuInfoMapper.insertSelective(spuInfo);
        //保存商品图片地址
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList != null && spuImageList.size() > 0) {
            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spuInfo.getId());
                this.spuImageMapper.insertSelective(spuImage);
            }
        }
        //保存销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList != null && spuSaleAttrList.size() > 0) {
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfo.getId());
                this.spuSaleAttrMapper.insertSelective(spuSaleAttr);
                //保存销售属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList != null && spuSaleAttrValueList.size() > 0) {
                    for (SpuSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                        saleAttrValue.setSpuId(spuInfo.getId());
                        this.spuSaleAttrValueMapper.insertSelective(saleAttrValue);

                    }
                }
            }

        }

    }

    @Override
    public List<SpuImage> listSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return this.spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuImage> listSpuImageList(SpuImage spuImage) {

        List<SpuImage> spuImages = this.spuImageMapper.select(spuImage);
        return spuImages;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return this.spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
        this.skuInfoMapper.insertSelective(skuInfo);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList != null && skuImageList.size() > 0) {
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                this.skuImageMapper.insertSelective(skuImage);
            }
        }

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList != null && skuAttrValueList.size() > 0) {
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                this.skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList != null && skuSaleAttrValueList.size() > 0) {
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                this.skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }
    }


    @Override
    public SkuInfo getSkuInfo(String skuId) {
        SkuInfo skuInfo = getSkuInfoRedisson(skuId);
        return skuInfo;
    }
    private SkuInfo getSkuInfoRedisson(String skuId) {
        SkuInfo skuInfo= null;
        Jedis jedis = null;
        RLock lock = null;

        try {
            jedis = jedisUtil.getJedis();
            // 定义key sku:skuId:info
            String skuKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            String skuJson = jedis.get(skuKey);
            if (StringUtils.isNotEmpty(skuJson)){
                // 从缓存
                skuInfo = JSON.parseObject(skuJson,SkuInfo.class);
                // 走缓存！
                return skuInfo;
            }else {
                // 创建config
                Config config = new Config();
                config.useSingleServer().setAddress("redis://192.168.195.136:6379");

                // 创建redisson
                RedissonClient redisson  = Redisson.create(config);

                lock = redisson.getLock("my-lock");
                // 在缓存为空，获取数据库中数据的时候上锁
                lock.lock();
                // 执行代码
                // 走数据库
                skuInfo = getSkuInfoByDB(skuId);
                // 将数据放入缓存
                jedis.setex(skuKey,ManageConst.SKUKEY_TIMEOUT,JSON.toJSONString(skuInfo));
                return skuInfo;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (jedis!=null){
                jedis.close();
            }
            if (lock!=null){
                // 关闭锁
                lock.unlock();
            }
        }
        // 当缓存宕机以后，走数据库！
        return getSkuInfoByDB(skuId);
    }

    private SkuInfo getSkuInfoBySet(String skuId) {
        SkuInfo skuInfo = null;
        Jedis jedis = null;
        String keys = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKUKEY_SUFFIX;
        String lockKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;
        try {
            //获取数据
            jedis =  jedisUtil.getJedis();
            String skuInfoJson = jedis.get(keys);
            if(StringUtils.isEmpty(skuInfoJson)){
                String lockResult = jedis.set(lockKey, "ok", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if("OK".equals(lockResult)){
                    skuInfo = getSkuInfoByDB(skuId);
                    String InfoJson = JSON.toJSONString(skuInfo);
                    jedis.setex(keys, ManageConst.SKUKEY_TIMEOUT, InfoJson);
                    jedis.del(lockKey);
                    jedis.close();

                    return skuInfo;
                }else{
                    Thread.sleep(1000);
                    return getSkuInfo(skuId);
                }
            }else{
                skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);
                jedis.close();
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return getSkuInfoByDB(skuId);
    }


    private SkuInfo getSkuInfoByDB(String skuId) {
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = this.skuImageMapper.select(skuImage);
        SkuInfo skuInfo = this.skuInfoMapper.selectByPrimaryKey(skuId);
        skuInfo.setSkuImageList(skuImageList);

        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = this.skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);
        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {

        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        return this.skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {
        String AttrValueIds = StringUtils.join(attrValueIdList.toArray(), ",");

        System.out.println("AttrValueIds"+AttrValueIds);
        List<BaseAttrInfo> AttrInfoList= this.baseAttrInfoMapper.selectAttrInfoListByIds(AttrValueIds);

        return AttrInfoList;


    }
}
