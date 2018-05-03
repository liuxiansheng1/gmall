package com.liujie.gmall.manageservice.constant;

import com.liujie.gmall.HttpClientUtil;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.springframework.stereotype.Component;





@Component
public class CatalogCrawler {

    public static final String url="https://www.jd.com/allSort.aspx";

    @Test
    public void doCrawl(){

        //读取网页
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String html = HttpClientUtil.doGet(url);

        Document document = Jsoup.parse(html);
        //一级菜单集合
        Elements catagoryItems = document.select("div[class='category-item m']");
        for (Element catagoryItem : catagoryItems) {
            System.out.println("ctg1 = " + catagoryItem.select(".item-title span").text());
            //二级菜单集合
            Elements ctg2Items = catagoryItem.select(".items .clearfix");
            for (Element ctg2Item : ctg2Items) {
                String ctg2text = ctg2Item.select("dt a").text();
                System.out.println("   ctg2text = " + ctg2text);
                //三级菜单集合
                Elements ctg3Items = ctg2Item.select("dd a");
                for (Element ctg3Item : ctg3Items) {
                    String ctg3text = ctg3Item.text();
                    System.out.println("        ctg3text = " + ctg3text);
                }

            }
        }

    }
}