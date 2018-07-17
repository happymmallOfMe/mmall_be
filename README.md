# mmall_be
happymmall后端项目

本地测试修改建议：
1. 将datasource.properties文件中db.username值修改为root。
2. 将mmall.properties文件中ftp.server.ip值修改为127.0.0.1；
   ftp.server.http.prefix值修改为http://image.imooc.com/，需将image.imooc.com代理到localhost(在hosts中添加127.0.0.1 image.imooc.com，或者使用Charles)；
   将alipay.callback.url值修改为 {由natapp生成外网穿透地址}/order/alipay_callback.do，并将该地址添加到支付宝沙箱环境的回调地址栏中；
   本地ftpserver需设置文件上传路径，且需要与代码中文件上传地址ftp.server.image.path指向一致。
3. logback.xml文件日志输出地址修改为本地地址。
4. WEB-INF文件夹下lib中jar包需要额外引入。
