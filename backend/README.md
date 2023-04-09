## 运行方法
1. 进入venv环境：
```
venv/Scripts/activate
```
2. 依次运行以下指令设置flask环境变量：
```
Powershell: 
$env:FLASK_APP="__init__.py"
$env:FLASK_RUN_HOST="0.0.0.0"
$env:FLASK_RUN_PORT=8000

CMD:
set FLASK_APP=__init__.py
set FLASK_RUN_HOST=0.0.0.0
set FLASK_RUN_PORT=8000
```
3. 启动服务器：
```
flask run
```
4. 测试服务器是否启动成功：
```
访问localhost:8000，若显示Index则服务器启动成功
```

## 登录
```
email: admin@gmail.com
password: Abc123456
```

## 数据库
1. 本地创建数据库：
```
DBSM：MySQL
端口：3306
创建用户：forum，密码：123456
数据库名称：forum_app
```

2. 使用MySQLWorkBench或任意MySQL工具运行forum_app.sql导入数据库结构即可

## 前后端接口文档
[接口文档](https://www.showdoc.com.cn/1931979514400970) 
- 可以用runapi或postman api等接口调试工具手动地上传图片等
