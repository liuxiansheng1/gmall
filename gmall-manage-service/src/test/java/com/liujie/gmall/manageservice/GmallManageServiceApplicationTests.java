package com.liujie.gmall.manageservice;


import com.liujie.gmall.manageservice.impl.CatalogCrawler1;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageServiceApplicationTests {

	@Autowired
	CatalogCrawler1 catalogCrawler1;
	@Test
	public void contextLoads() {
		catalogCrawler1.doCrawl();
	}

	@Test
	public void test1(){
		String value = "1";
		System.out.println( value.toString());
	}

}
