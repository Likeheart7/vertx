/**
 * 整体结构如下：
 * 首先部署了3个HeatSensor，分别位于3000、3001、3002端口
 * 然后部署在8080端口部署CollectorServer，在4000部署SnapshotServer
 * 当client通过get请求访问8080端口的CollectorServer时，CollectorService并行的向3个HeatServer服务获取数据
 * 拿到3个json格式的响应结果后，先向位于4000端口的SnapshotServer发送数据，请求成功，确定SnapshotServer存在
 * 向client响应数据
 * 也就是说CollectorServer在通过HeatSensor获取数据后，先推给SnapshotServer，它会做记录（这里是打日志），成功后，将数据响应给client
 */