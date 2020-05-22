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
import org.dom4j.DocumentException;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.print.Doc;

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
		System.out.println("ssrdList.size()	"+ssrdList.size());
//		export(ssrdList);
		return "index";
	}

	public void export(List<ShadowSocksDetailsEntity> ssrdList){
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
//		<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
		document.addDocType("plist","-//Apple//DTD PLIST 1.0//EN","http://www.apple.com/DTDs/PropertyList-1.0.dtd");
		Element plist = DocumentHelper.createElement("plist");
		plist.addAttribute("version","1.0");
		document.add(plist);
		Element dict = DocumentHelper.createElement("dict");
		plist.add(dict);

		Element key = dict.addElement("key");
		key.addText("ACLFileName");
		dict.addElement("string").addText("");

		dict.addElement("key").addText("ActiveServerProfileId");
		dict.addElement("string").addText("8F93222B-274F-4353-B702-214964A4EBF8");
		dict.addElement("key").addText("AutoUpdateSubscribe");
		dict.addElement("true");
		dict.addElement("key").addText("ConnectAtLaunch");
		dict.addElement("true");
		dict.addElement("key").addText("LocalSocks5.ListenPort");
		dict.addElement("integer").addText("1080");
		dict.addElement("key").addText("LocalSocks5.ListenPort.Old");
		dict.addElement("integer").addText("1080");
		dict.addElement("key").addText("ServerProfiles");

		String xmlStr = "<dict>\n" +
				"    <key>Id</key>\n" +
				"    <string>8F93222B-274F-4353-B702-214964A4EBF8</string>\n" +
				"    <key>Method</key>\n" +
				"    <string>aes-128-cfb</string>\n" +
				"    <key>Password</key>\n" +
				"    <string>QazEdcTgb159@$*</string>\n" +
				"    <key>Remark</key>\n" +
				"    <string>@SSRSUB-香港ssr06-付费SSR推荐:t.cn/EGJIyrl</string>\n" +
				"    <key>ServerHost</key>\n" +
				"    <string>134.175.195.183</string>\n" +
				"    <key>ServerPort</key>\n" +
				"    <integer>36129</integer>\n" +
				"    <key>ssrGroup</key>\n" +
				"    <string>t.me/SSRSUB</string>\n" +
				"    <key>ssrObfs</key>\n" +
				"    <string>plain</string>\n" +
				"    <key>ssrObfsParam</key>\n" +
				"    <string>付费SSR注册:http://t.cn/EGJIyrl</string>\n" +
				"    <key>ssrProtocol</key>\n" +
				"    <string>origin</string>\n" +
				"    <key>ssrProtocolParam</key>\n" +
				"    <string>t.me/SSRSUB</string>\n" +
				"</dict>";
		Element array = dict.addElement("array");
//		array.clearContent();
		Document parseText = null;
		try {
			Element dict1 = array.addElement("dict");
			parseText = DocumentHelper.parseText(xmlStr.trim());
			//将节点写入目标位置
			dict1.setContent(parseText.getRootElement().content());
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		for (ShadowSocksDetailsEntity detailsEntity : ssrdList) {
//			Optional<String> stringString = Optional.ofNullable("");
			String string;
			Element root = array.addElement("dict");

			root.addElement("key").addText("Id");
			root.addElement("string").addText(detailsEntity.getId()+"");//

			root.addElement("key").addText("Method");
			string = Optional.ofNullable(detailsEntity.getMethod()).orElse("");
			root.addElement("string").addText(string);

			root.addElement("key").addText("Password");
			string = Optional.ofNullable(detailsEntity.getPassword()).orElse("");
			root.addElement("string").addText(string);

			string = Optional.ofNullable(detailsEntity.getRemarks()).orElse("");
			if(string != "") {
				root.addElement("key").addText("Remark");
				root.addElement("string").addText(string);
			}

			root.addElement("key").addText("ServerHost");
			root.addElement("string").addText(String.valueOf(detailsEntity.getServer()));//

			root.addElement("key").addText("ServerPort");
			root.addElement("string").addText(Long.toString(detailsEntity.getServer_port()));//

			string = Optional.ofNullable(detailsEntity.getGroup()).orElse("");
			if(string != "") {
				root.addElement("key").addText("ssrGroup");
				root.addElement("string").addText(string);
			}

			root.addElement("key").addText("obfs");
			string = Optional.ofNullable(detailsEntity.getObfs()).orElse("");
			root.addElement("string").addText(string);

			root.addElement("key").addText("Protocol");
			string = Optional.ofNullable(detailsEntity.getProtocol()).orElse("");
			root.addElement("string").addText(string);
//			root.addElement("key").addText("ssrProtocolParam");
//			string = Optional.ofNullable(detailsEntity.getProtocol());
//			root.addElement("string").addText(string);
		}


		dict.addElement("key").addText("ShadowsocksOn");
		dict.addElement("false");
		dict.addElement("key").addText("ShadowsocksRunningMode");
		dict.addElement("string").addText("auto");
		dict.addElement("key").addText("Subscribes");
		Element dict2 = dict.addElement("array").addElement("dict");
		dict2.addElement("key").addText("feed");
		dict2.addElement("string").addText("https://raw.githubusercontent.com/ssrsub/ssr/master/ssrsub");
		dict2.addElement("key").addText("group");
		dict2.addElement("string").addText("ssr");
		dict2.addElement("key").addText("maxCount");
		dict2.addElement("integer").addText("10");
		dict2.addElement("key").addText("token");
		dict2.addElement("string").addText("");
		dict.addElement("key").addText("enable_showSpeed");
		dict.addElement("true");

		try{
			//4
			//OutputFormat format=OutputFormat.createCompactFormat();  //紧凑格式:去除空格换行
			XMLWriter writer = new XMLWriter(OutputFormat.createPrettyPrint());//漂亮格式：有空格换行
			FileOutputStream fos
					= new FileOutputStream("lib"+ File.separator + "com.qiuyuzhou.ShadowsocksX-NG.plist");
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
