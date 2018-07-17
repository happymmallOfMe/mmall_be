#!/usr/bin/env bash

# 执行本脚本前必有的操作：
#       必须提前在/git-repository目录下clone项目文件，否则无法连接git远程仓库与本地目录，
#       后续也无法进行其他git操作；clone完成后，便会在/git-repository目录下新增mmall_learning文件夹；
#       如有需要，还需赋予用户该文件夹（mmall_learning）的相关权限。
# 问题主要来源：1命令行中sudo的使用；
#              2用户与文件夹的权限设置；
#              3github中下载的代码不是新修改代码


# 注意：此处目录地址“mmall_learning”需与项目名称一致
echo "===========1.进入git项目mmall_learning目录=============="
cd /developer/git-repository/mmall_learning


# 注意：checkout后面的参数需与项目分支名称一致
echo "================2.git切换分之到v1.0================="
sudo git checkout v1.0

# 注意：无论是master分支还是还是v1.0分支，他们的ssh地址都是一样的；
#      使用git clone {ssh地址}下载的源代码默认是master分支的代码，
#      在使用上面“git checkout v1.0”命令切换分支后，在使用下面的
#      “sudo git fetch”和“sudo git pull”命令更新代码可能不奏效，
#      所以应该注意检查代码，看是否属于v1.0分支代码。
#      建议：切换分支后，直接执行“git pull”。
echo "==================3.git fetch======================"
sudo git fetch

echo "===================4.git pull======================"
sudo git pull

# 注意：参数--Dmaven.test.skip=true需置于单引号中，
#      同时需要注意当前用户对父目录（即/mmall_learning）的权限设置，
#      此外还需在pom.xml文件的maven-compiler-plugin节点下添加version信息
echo "=================5.编译并跳过单元测试================"
# mvn clean package -Dmaven.test.skip=true
mvn clean install package '-Dmaven.test.skip=true'


echo "===================6.删除旧的ROOT.war==============="
rm /developer/apache-tomcat-9.0.0.M26/webapps/ROOT.war


echo "=======7.拷贝编译出来的war包到tomcat下-ROOT.war======="
cp /developer/git-repository/mmall_learning/target/mmall.war  /developer/apache-tomcat-9.0.0.M26/webapps/ROOT.war


echo "============8.删除tomcat下旧的ROOT文件夹============="
sudo rm -rf /developer/apache-tomcat-9.0.0.M26/webapps/ROOT

# 注意：如有需要，启动和关闭tomcat服务的命令需要添加sudo权限，且保证命令路径正确
echo "====================9.关闭tomcat===================="
sudo /developer/apache-tomcat-9.0.0.M26/bin/shutdown.sh


echo "====================10.sleep 10s===================="
for i in {1..10}
do
        echo $i"s"
        sleep 1s
done


echo "====================11.启动tomcat====================="
sudo /developer/apache-tomcat-9.0.0.M26/bin/startup.sh