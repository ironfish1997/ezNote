package util;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailUtil {
	static ExecutorService threadPool = Executors.newCachedThreadPool();

	/**
	 * 发送激活邮件的方法
	 * 
	 * @param email 收信人的地址
	 * @param emailMsg 激活码
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	public static boolean sendActiveMail(String email, String activecode)
			throws InterruptedException, IOException, TimeoutException, ExecutionException {
		Properties prop = new Properties();
		prop.load(MailUtil.class.getResourceAsStream("/conf/mail.properties"));
		MessageSender ms = new MessageSender(prop, email, activecode);
		Future<Integer> ft = threadPool.submit(ms);
		AtomicInteger count = new AtomicInteger(0);
		try {
			ft.get(10, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			System.err.println("发送邮件时间超过平时时间");
			for(;;){
				threadPool.submit(ms);
				while(count.getAndIncrement()==10){
					throw e;
				}
			}
		}
		return true;
	}

	/**
	 * 发送邮件处理类，生成一个发送邮件的任务，给Executor框架执行
	 * 
	 * @author liuliyong
	 *
	 */
	static class MessageSender implements Callable<Integer> {
		private Properties prop;
		private Session session;
		private String toAddress;
		private String code;

		MessageSender(Properties prop, String toAddress, String code) {
			if (session == null) {
				session = JavaSessionLoader.JavaSessionLoaderInstance(prop).getSession();
				session.setDebug(true);
			}
			this.prop = prop;
			this.toAddress = toAddress;
			this.code = code;
		}

		@Override
		public Integer call() throws Exception {
			Message msg = null;
			Transport transport = null;
			try {
				// 创建邮件的实例对象
				msg = getMimeMessage(session);
				// 根据session对象获取邮件传输对象Transport
				transport = session.getTransport();
				// 设置发件人的账户名和密码
				transport.connect(prop.getProperty("mail.username").trim(), prop.getProperty("mail.password").trim());
				// 发送给指定的人
				transport.sendMessage(msg, new Address[] { new InternetAddress(toAddress) });
				// 关闭邮件连接
				transport.close();
			} catch (Exception e) {
				if (transport != null) {
					transport.close();
				}
				System.err.println("邮件发送失败");
				throw e;
			}
			return 1;
		}

		public MimeMessage getMimeMessage(Session session) throws Exception {
			// 创建一封邮件的实例对象
			MimeMessage msg = new MimeMessage(session);
			// 设置发件人地址
			String userName = prop.getProperty("mail.username");
			msg.setFrom(new InternetAddress(userName));
			/**
			 * 设置收件人地址（可以增加多个收件人、抄送、密送），即下面这一行代码书写多行
			 * MimeMessage.RecipientType.TO:发送 MimeMessage.RecipientType.CC：抄送
			 * MimeMessage.RecipientType.BCC：密送
			 */
			msg.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(toAddress));
			// 设置邮件主题
			msg.setSubject("易笔记邮箱验证", "UTF-8");
			
			// 创建文本"节点"
			MimeBodyPart text = new MimeBodyPart();
			// 这里添加图片的方式是将整个图片包含到邮件内容中, 实际上也可以以 http 链接的形式添加网络图片
			text.setContent("<h2>易笔记账号激活邮件</h2><br/>" + "你好鸭！<br/>"
					+ "&nbsp;&nbsp;请猛击<a href='http://120.79.79.151:8080/CloudNote/user/activeMail.do?email=" + toAddress
					+ "&activecode=" + code + "'>http://120.79.79.151:8080/CloudNote/user/checkmail.do?email="
					+ toAddress + "&activecode=" + code + "</a>完成验证。如果你没有注册过易笔记，请忽略这封邮件<br/>" + "谢谢鸭！",
					"text/html;charset=UTF-8");

			// 合成节点
			MimeMultipart content = new MimeMultipart();
			content.addBodyPart(text);

			msg.setContent(content);

			// 设置邮件的发送时间,默认立即发送
			msg.setSentDate(new Date());
			return msg;
		}
	}

	/**
	 * 通过读取conf/mail.properties来装配JavaMail的Session对象
	 * @author liuliyong
	 *
	 */
	static class JavaSessionLoader {
		private static JavaSessionLoader jsInstance;
		private static Session session = null;

		private JavaSessionLoader(Properties prop) {
			try {
				synchronized (Session.class) {
					if (session == null) {
						session = Session.getInstance(prop);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public static JavaSessionLoader JavaSessionLoaderInstance(Properties prop) {
			synchronized (JavaSessionLoader.class) {
				if (jsInstance == null) {
					jsInstance = new JavaSessionLoader(prop);
				}
			}
			return jsInstance;
		}

		public Session getSession() {
			return session;
		}
	}

	public static void main(String[] args)
			throws InterruptedException, IOException, TimeoutException, ExecutionException {
		System.out.println(MailUtil.sendActiveMail("1481980097@qq.com", "123"));
	}
}
