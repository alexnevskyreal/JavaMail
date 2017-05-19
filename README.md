一、第四版
1、命令行运行：
java -jar JavaMail-0.0.4.jar 2017.05.15 2017.05.16 "Daily Report" "Weekly Report"

2、最少有三个关键词（带空格的字符串用""）
（1）开始日期：2017.05.03 
（2）结束日期：2017.05.03
（3）标题："Daily Report"
（4）标题："Weekly Report"
（5）标题：可多个

3、配置邮箱，右键用解压程序打开JavaMail.jar
（1）mail.properties，修改邮箱账号密码（不能有空格!!!）
（2）application.properties，修改启动端口号
（3）thread.properties，修改线程数量

4、注意
（1）邮件不能随意删除移动，否则易造成邮件的排序紊乱
（2）程序中获取的邮件总数可能与邮箱中实际总数不同，是由于腾讯收件箱的数据达不到一致
