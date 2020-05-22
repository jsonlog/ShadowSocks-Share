package com.example.ShadowSocksShare.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import com.example.ShadowSocksShare.domain.ShadowSocksDetailsEntity;
import com.example.ShadowSocksShare.domain.ShadowSocksEntity;
import com.example.ShadowSocksShare.service.ShadowSocksCrawlerService;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class FileImportServiceImpl extends ShadowSocksCrawlerService {
    // 目标网站 URL
    private static final String TARGET_URL = "/Users/home/Documents/telegram/ssr";

    public ShadowSocksEntity getShadowSocks() {
//        List<String> list = new ArrayList<String>();
        Set<String> listset = new HashSet<>();
        File directory = new File(TARGET_URL);
        if(directory.exists()) {
            if(directory.isDirectory()) {
                for(File file:directory.listFiles()) {
                    try {
                        System.out.println(file.getAbsolutePath());
                        InputStreamReader read = new InputStreamReader(
                                new FileInputStream(file));//encoding);// 考虑到编码格式
                        BufferedReader bufferedReader = new BufferedReader(read);
                        String lineTxt = null;
                        while ((lineTxt = bufferedReader.readLine()) != null) {
                            if(lineTxt.startsWith("ssr://") || lineTxt.startsWith("ss://"))
                                listset.add(lineTxt);
                            System.out.println(lineTxt);
                        }
                        bufferedReader.close();
                        read.close();
                    }catch (FileNotFoundException e){} catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        Set<ShadowSocksDetailsEntity> set = Collections.synchronizedSet(new HashSet<>(listset.size()));
        Iterator<String> iterable = listset.iterator();
        System.out.println("list.size():"+listset.size());
        while (iterable.hasNext()) {
            ShadowSocksDetailsEntity ss = new ShadowSocksDetailsEntity(iterable.next());
            set.add(ss);
        }
        // 3. 生成 ShadowSocksEntity
        ShadowSocksEntity entity = new ShadowSocksEntity(TARGET_URL, "localFile", true, new Date());
        entity.setShadowSocksSet(set);
        return entity;

    }

    @Override
    protected Set<ShadowSocksDetailsEntity> parse(Document document) {
        return null;
    }

    @Override
    protected String getTargetURL() {
        return TARGET_URL;
    }

    @Override
    protected boolean isProxyEnable() {
        return false;
    }

    @Override
    protected String getProxyHost() {
        return null;
    }

    @Override
    protected int getProxyPort() {
        return 0;
    }
}
