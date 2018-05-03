package com.liujie.gmall.manageservice.impl;

import com.liujie.gmall.BaseCatalog1;
import com.liujie.gmall.BaseCatalog2;
import com.liujie.gmall.BaseCatalog3;
import com.liujie.gmall.HttpClientUtil;
import com.liujie.gmall.manageservice.mapper.BaseCatalog1Mapper;
import com.liujie.gmall.manageservice.mapper.BaseCatalog2Mapper;
import com.liujie.gmall.manageservice.mapper.BaseCatalog3Mapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CatalogCrawler1 {

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    public static final String url="https://www.jd.com/allSort.aspx";


    public void doCrawl(){

        //读取网页
        String html = HttpClientUtil.doGet(url);

        Document document = Jsoup.parse(html);
        //一级菜单集合
        Elements catagoryItems = document.select("div[class='category-item m']");
        for (Element catagoryItem : catagoryItems) {
            String ctg1text = catagoryItem.select(".item-title span").text();

            BaseCatalog1 baseCatalog1=new BaseCatalog1();
            baseCatalog1.setName(ctg1text);

            baseCatalog1Mapper.insertSelective(baseCatalog1);

            System.out.println("ctg1 = " + ctg1text);
            //二级菜单集合
            Elements ctg2Items = catagoryItem.select(".items .clearfix");
            for (Element ctg2Item : ctg2Items) {
                String ctg2text = ctg2Item.select("dt a").text();

                BaseCatalog2 baseCatalog2=new BaseCatalog2();
                baseCatalog2.setName(ctg2text);
                baseCatalog2.setCatalog1Id(baseCatalog1.getId());
                baseCatalog2Mapper.insertSelective(baseCatalog2);

                System.out.println("   ctg2text = " + ctg2text);
                //三级菜单集合
                Elements ctg3Items = ctg2Item.select("dd a");
                for (Element ctg3Item : ctg3Items) {
                    String ctg3text = ctg3Item.text();

                    BaseCatalog3 baseCatalog3=new BaseCatalog3();
                    baseCatalog3.setName(ctg3text);
                    baseCatalog3.setCatalog2Id(baseCatalog2.getId());
                    baseCatalog3Mapper.insertSelective(baseCatalog3);

                    System.out.println("        ctg3text = " + ctg3text);
                }

            }
        }

    }
}