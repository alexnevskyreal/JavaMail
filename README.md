1、命令行运行：
java -jar JavaMail-0.0.1.jar 测试邮件 2017.05.03 2017.05.03

2、必须有三个关键词（如日期为空、参数量!=3、开始大于结束日期，则控制台会提示相应错误）
（1）标题：测试邮件
（2）开始日期：2017.05.03 
（3）结束日期：2017.05.03

3、动态配置邮箱
（1）右键使用解压程序打开JavaMail.jar
（2）打开根目录的mail.properties，修改邮箱的账号及密码（注意修改时= XXX后不能有空格）
（3）打开根目录的application.properties，修改启动端口号