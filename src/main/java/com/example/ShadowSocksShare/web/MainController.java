package com.example.ShadowSocksShare.web;


import com.example.ShadowSocksShare.domain.ShadowSocksDetailsEntity;
import com.example.ShadowSocksShare.domain.ShadowSocksEntity;
import com.example.ShadowSocksShare.service.CountSerivce;
import com.example.ShadowSocksShare.service.ShadowSocksCrawlerService;
import com.example.ShadowSocksShare.service.ShadowSocksSerivce;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.zxing.WriterException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
public class MainController {
	@Autowired
	private ShadowSocksSerivce shadowSocksSerivceImpl;
	@Autowired
	private CountSerivce countSerivce;
	@Autowired
	private ShadowSocksSerivce shadowSocksSerivce;
	@Autowired
	private ApplicationContext appContext;

	/**
	 * 首页
	 */
	@RequestMapping("/")
	public String index(@PageableDefault(page = 0, size = 50, sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable, Model model) {
		List<ShadowSocksEntity> ssrList = shadowSocksSerivceImpl.findAll(pageable);
		List<ShadowSocksDetailsEntity> ssrdList = new ArrayList<>();
		for (ShadowSocksEntity ssr : ssrList) {
			ssrdList.addAll(ssr.getShadowSocksSet());
		}
		// ssr 信息
		model.addAttribute("ssrList", ssrList);
		// ssr 明细信息，随机排序
		Collections.shuffle(ssrdList);
		model.addAttribute("ssrdList", ssrdList);
		export();
		return "index";
	}

	public void export(){
		/*
		 * 使用DOM生成XML文档的大致步骤:
		 * 1:创建一个Document对象表示一个空文档
		 * 2:向Document中添加根元素
		 * 3:按照文档应有的结构从根元素开始顺序添加
		 *   子元素来形成该文档结构。
		 * 4:创建XmlWriter对象
		 * 5:将Document对象写出
		 *   若写入到文件中则形成一个xml文件
		 *   也可以写出到网络中作为传输数据使用
		 */

		//1
		Document document = DocumentHelper.createDocument();
		Element new_root_node = DocumentHelper.createElement("type");
		Element new_module_1_node = DocumentHelper.createElement("module");

		new_module_1_node.addAttribute("id","1");
		new_root_node.add(new_module_1_node);
		document.add(new_root_node);

		try{
			//4
			XMLWriter writer = new XMLWriter(OutputFormat.createPrettyPrint());
			FileOutputStream fos
					= new FileOutputStream("myemp.xml");
			writer.setOutputStream(fos);

			//5
			writer.write(document);
			System.out.println("写出完毕!");
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * SSR 订阅地址
	 */
	@RequestMapping("/subscribe")
	@ResponseBody
	public ResponseEntity<String> subscribe(boolean valid, @PageableDefault(page = 0, size = 1000, sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {
		List<ShadowSocksEntity> ssrList = shadowSocksSerivceImpl.findAll(pageable);
		return ResponseEntity.ok()
				.contentType(MediaType.TEXT_PLAIN)
				.body(shadowSocksSerivceImpl.toSSRLink(ssrList, valid));
	}

	/**
	 * 订阅 一条 有效的 Json
	 */
	@RequestMapping("/subscribeJson")
	@ResponseBody
	public ResponseEntity<String> subscribeJson() throws JsonProcessingException {
		return ResponseEntity.ok()
				.contentType(MediaType.TEXT_PLAIN)
				.body(shadowSocksSerivceImpl.findFirstByRandom().getJsonStr());
	}

	/**
	 * 二维码
	 */
	@RequestMapping(value = "/createQRCode")
	@ResponseBody
	public ResponseEntity<byte[]> createQRCode(long id, String text, int width, int height, WebRequest request) throws IOException, WriterException {
		// 缓存未失效时直接返回
		if (request.checkNotModified(id))
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(MediaType.IMAGE_PNG_VALUE))
					.cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
					.eTag(String.valueOf(id))
					.body(null);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(MediaType.IMAGE_PNG_VALUE))
				.cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
				.eTag(String.valueOf(id))
				.body(shadowSocksSerivceImpl.createQRCodeImage(text, width, height));
	}

	@RequestMapping(value = "/count")
	@ResponseBody
	public ResponseEntity<String> count() {
		return ResponseEntity.ok().body(String.valueOf(countSerivce.get()));
	}

	@RequestMapping(value = "/run")
	@ResponseBody
	public ResponseEntity<String> run(String name) {
		if (StringUtils.isNotBlank(name)) {
			Object bean = appContext.getBean(name);

			if (bean instanceof ShadowSocksCrawlerService) {
				shadowSocksSerivce.crawlerAndSave(ShadowSocksCrawlerService.class.cast(bean));
			}
		}
		return ResponseEntity.ok().body("OK...");
	}
}
