/*
本包中主要是音频推送，实现了浏览器访问播放的功能，访问路径为localhost:9527/download/xxx.mp3
可以使用netcat localhost 3000发送tcp消息，对应命令在代码中有详细解释
需要将mp3文件放在/src/main/resources/static/audio目录内
 */