package com.example.ShadowSocksShare.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.apache.commons.codec.binary.Base64;

import javax.persistence.*;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ssr 信息
 * 参考：
 * https://www.zfl9.com/ssr.html
 * http://rt.cn2k.net/?p=328
 * https://vxblue.com/archives/115.html
 */
@Entity
@Getter
@Setter
@ToString
// @RequiredArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id", "remarks", "group"})
public class ShadowSocksDetailsEntity implements Serializable {
	// SSR 连接 分隔符
	public static final String SSR_LINK_SEPARATOR = ":";
	private static final long serialVersionUID = 952212276705742190L;

	// 必填字段
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Setter
	private long id;

	@Column
	// @NonNull
	private String server; // 地址

	@Column
	// @NonNull
	private int server_port;    // 端口

	@Column
	// @NonNull
	private String password;    // 密码

	@Column
	// @NonNull
	private String method;    // 加密

	@Column
	// @NonNull
	private String protocol = "origin";    // 协议

	@Column
	// @NonNull
	private String obfs = "plain";    // 混淆

	// 非必填
	@Column
	private String remarks;    // 备注

	@Column(name = "grp")
	private String group; // 组

	@Column
	private boolean valid;    // 是否有效

	@Column
	private Date validTime;        // 有效性验证时间

	@Column
	private String title;        // 网站名

	public ShadowSocksDetailsEntity(String server, int server_port, String password, String method, String protocol, String obfs) {
		this.server = server;
		this.server_port = server_port;
		this.password = password;
		this.method = method;
		this.protocol = protocol;
		this.obfs = obfs;
	}
	public ShadowSocksDetailsEntity(String sslink){
		linktoJson(sslink);
	}

	public String getJsonStr() throws JsonProcessingException {
		Map<String, Object> json = new HashMap<>();
		json.put("server", server);
		json.put("server_port", server_port);
		json.put("local_address", "127.0.0.1");
		json.put("local_port", 1080);
		json.put("password", password);
		json.put("group", group);
		json.put("obfs", obfs);
		json.put("method", method);
		json.put("ssr_protocol", protocol);
		return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(json);
	}
	public void linktoJson(String sslink){
		// .replaceAll("-","+").replaceAll("_","/");
		String base64encodedString = sslink.substring(sslink.indexOf("://")+3);

		try {
			System.out.println("base64encodedString: " + sslink);
			byte[] base64decodedBytes = Base64.decodeBase64(base64encodedString);
//			System.out.println("原始字符串: " + new String(base64decodedBytes, "utf-8"));
			String[] ssparam = new String(base64decodedBytes, "utf-8").split(":");
			for(String param:ssparam) System.out.print(param+":");
			if (sslink.startsWith("ss:") && ssparam.length > 2) {
				//aes-256-cfb:eIW0Dnk69454e6nSwuspv9DmS201tQ0D@74.207.246.242:8099
				method = ssparam[0];
				String[] encode = ssparam[1].split("@");
				password = new String(Base64.decodeBase64(encode[0]));
				server = encode[1];
				server_port = Integer.parseInt(ssparam[2]);
			}else
				if(sslink.startsWith("ssr://") && ssparam.length > 5){
				method = ssparam[3];
				String[] encode = ssparam[5].split("/");
				password = new String(Base64.decodeBase64(encode[0]));
				server = ssparam[0];
				server_port = Integer.parseInt(ssparam[1]);
				protocol = ssparam[2];
				obfs = ssparam[4];
			}
			System.out.println(this);
		}catch(java.io.UnsupportedEncodingException e){
			System.out.println("Error :" + e.getMessage());
		}//catch (ArrayIndexOutOfBoundsException e){}
	}


	/**
	 * 生成连接
	 * 连接规则：https://github.com/ssrbackup/shadowsocks-rss/wiki/SSR-QRcode-scheme
	 */
	public String getLink() {
//		if(true) return "ssr://base64(host:port:protocol:method:obfs:base64pass/?obfsparam=base64param&protoparam=base64param&remarks=base64remarks&group=base64group&udpport=0&uot=0)";
		//ssr-02.ssrsub.xyz:443:auth_aes128_md5:aes-256-ctr:tls1.2_ticket_auth:aHR0cDovL3QuY24vRUdKSXlybA/?obfsparam=5LuY6LS5U1NS5rOo5YaMOmh0dHA6Ly90LmNuL0VHSkl5cmw&protoparam=dC5tZS9TU1JTVUI&remarks=QFNTUlNVQi3pn6nlm71zc3IwMi3ku5jotLlTU1LmjqjojZA6dC5jbi9FR0pJeXJs&group=dC5tZS9TU1JTVUI
		String string = "";
		System.out.println(this);
		if(server!=null&&server_port!=0&&protocol!=null&&method!=null&&obfs!=null&&password!=null) {
			StringBuilder link = new StringBuilder();
			link
					.append(server)
					.append(SSR_LINK_SEPARATOR).append(server_port)
					.append(SSR_LINK_SEPARATOR).append(protocol)
					.append(SSR_LINK_SEPARATOR).append(method)
					.append(SSR_LINK_SEPARATOR).append(obfs)
					.append(SSR_LINK_SEPARATOR).append(Base64.encodeBase64URLSafeString(password.getBytes(StandardCharsets.UTF_8)));
			if (remarks != null)
				link.append("/?obfsparam=")// .append("&protoparam=")
						.append("&remarks=").append(Base64.encodeBase64URLSafeString(remarks.getBytes(StandardCharsets.UTF_8)));
			if (group != null)
				link.append("&group=").append(Base64.encodeBase64URLSafeString(group.getBytes(StandardCharsets.UTF_8)));
			string = "ssr://" + Base64.encodeBase64URLSafeString(link.toString().getBytes(StandardCharsets.UTF_8));
		}
		return string;
	}

	/*public String getLinkNotSafe() {
		// 104.236.187.174:1118:auth_sha1_v4:chacha20:tls1.2_ticket_auth:ZGFzamtqZGFr/?obfsparam=&remarks=MTExOCDml6fph5HlsbEgMTDkurogMTAwRyBTU1I&group=Q2hhcmxlcyBYdQ
		StringBuilder link = new StringBuilder();
		link
				.append(server)
				.append(SSR_LINK_SEPARATOR).append(server_port)
				.append(SSR_LINK_SEPARATOR).append(protocol)
				.append(SSR_LINK_SEPARATOR).append(method)
				.append(SSR_LINK_SEPARATOR).append(obfs)
				.append(SSR_LINK_SEPARATOR).append(Base64.encodeBase64URLSafeString(password.getBytes(StandardCharsets.UTF_8)))
				.append("/?obfsparam=")
				// .append("&protoparam=")
				.append("&remarks=").append(Base64.encodeBase64URLSafeString(remarks.getBytes(StandardCharsets.UTF_8)))
				.append("&group=").append(Base64.encodeBase64URLSafeString(group.getBytes(StandardCharsets.UTF_8)));
		return "ssr://" + Base64.encodeBase64URLSafeString(link.toString().getBytes(StandardCharsets.UTF_8)) + " ";
	}*/

	@Override
	public String toString() {
		return "ShadowSocksDetailsEntity{" +
				"id=" + id +
				", server='" + server + '\'' +
				", server_port=" + server_port +
				", password='" + password + '\'' +
				", method='" + method + '\'' +
				", protocol='" + protocol + '\'' +
				", obfs='" + obfs + '\'' +
				", remarks='" + remarks + '\'' +
				", group='" + group + '\'' +
				", valid=" + valid +
				", validTime=" + validTime +
				", title='" + title + '\'' +
				'}';
	}
}
