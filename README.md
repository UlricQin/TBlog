TBlog
=====

Team Blog 团队博客

部署说明:
1、项目根目录下有个build.xml，用于使用ant来做编译构建
2、也可以导入eclipse直接让eclipse帮忙编译，我平时开发都是用的普通版本的eclipse，不是jee版，记得把webapp/WEB-INF/lib和lib目录加入到classpath
3、上面两种方法达到的效果就是：所有内容编译完成之后都会放置到webapp/WEB-INF目录下，即此时的webapp目录已经是一个符合jee规范的目录结构了
4、在tomcat的server.xml中配置一句话：<Context path="" docBase="/path/to/tblog/webapp" />放在<Host>标签内，注意path的配置，目前项目要求是只能部署在 / 下
5、data目录下有个iperl.sql的脚本，你懂的
6、conf/db.properties是数据库的配置，你懂的

O了


