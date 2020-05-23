package com.example.ShadowSocksShare.service.impl;

import java.io.*;
import java.util.*;

import com.example.ShadowSocksShare.domain.ShadowSocksDetailsEntity;
import com.example.ShadowSocksShare.domain.ShadowSocksEntity;
import com.example.ShadowSocksShare.service.ShadowSocksCrawlerService;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.nodes.Document;
import org.springframework.jmx.export.naming.IdentityNamingStrategy;
import org.springframework.stereotype.Service;

@Service
public class FileImportServiceImpl extends ShadowSocksCrawlerService {
    // 目标网站 URL
    private static final String TARGET_URL = "/Users/home/Documents/telegram";

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
                            if(lineTxt.startsWith("ss://") || lineTxt.startsWith("ssr://"))
                                listset.add(lineTxt);
                            else System.out.println(lineTxt);
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
        Set<String> sslinkset = new HashSet<>();
        while (iterable.hasNext()) {
            String sslink = iterable.next();
            // .replaceAll("-","+").replaceAll("_","/");
            String base64encodedString = sslink.substring(sslink.indexOf("://")+3);
            if(sslink.indexOf("#") != -1) base64encodedString = base64encodedString.substring(0,base64encodedString.indexOf("#"));

            try {
//                System.out.println("base64encodedString: " + base64encodedString);
                byte[] base64decodedBytes = org.apache.commons.codec.binary.Base64.decodeBase64(base64encodedString);
//			System.out.println("原始字符串: " + new String(base64decodedBytes, "utf-8"));
                String[] ssparam = new String(base64decodedBytes, "utf-8").split(":");
//                for(String param:ssparam) System.out.print(param+":");
                if (sslink.startsWith("ss:") && ssparam.length > 2) {
                    //aes-256-cfb:eIW0Dnk69454e6nSwuspv9DmS201tQ0D@74.207.246.242:8099
                    String method = ssparam[0];
                    String[] encode = ssparam[1].split("@");
                    String password = encode[0];
                    String server = encode[1];
                    Integer server_port = Integer.parseInt(ssparam[2]);
//                    System.out.println(ssparam[0]+":"+encode[0]+":"+encode[1]+":"+ssparam[2]);
                    ShadowSocksDetailsEntity ss = new ShadowSocksDetailsEntity(server,server_port,password,method);
                    set.add(ss);
//                    System.out.println(ss.getLink()+",");
                    sslinkset.add(ss.getLink());
                }else
                if(sslink.startsWith("ssr://") && ssparam.length > 5){
                    System.out.println(sslink+",");
                    String method = ssparam[3];
                    String[] encode = ssparam[5].split("/");
                    String password = new String(Base64.decodeBase64(encode[0]));
                    String server = ssparam[0];
                    Integer server_port = Integer.parseInt(ssparam[1]);
                    String protocol = ssparam[2];
                    String obfs = ssparam[4];
//                    ShadowSocksDetailsEntity ss = new ShadowSocksDetailsEntity(server,server_port,password,method,protocol,obfs);
                    ShadowSocksDetailsEntity ss = new ShadowSocksDetailsEntity(sslink);
                    set.add(ss);
                    sslinkset.add(sslink);
                }
            }catch(java.io.UnsupportedEncodingException e){
                System.out.println("Error :" + e.getMessage());
            }//catch (ArrayIndexOutOfBoundsException e){}
        }
        // 3. 生成 ShadowSocksEntity
        ShadowSocksEntity entity = new ShadowSocksEntity(TARGET_URL, "localFile", true, new Date());
        entity.setShadowSocksSet(set);
        export(sslinkset.iterator());
        return entity;
    }

    public void export(Iterator<String> iterator){
        try {
            PrintWriter pw = new PrintWriter(new FileWriter("lib/out.txt"));
            if(iterator.hasNext()) pw.write(iterator.next());
            while (iterator.hasNext()){
                pw.write(",\n"+iterator.next());
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
