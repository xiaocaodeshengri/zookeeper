package com.tl.api.curator;

import java.util.List;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;

public class CuratorCurd<T> {
    
	//设置重置策略
	RetryPolicy retryPolicy=new ExponentialBackoffRetry(2000,5);
	//设置连接的字符串
	String conneString="192.168.6.2:2181";
	//工厂创建客户端
	CuratorFramework client=	CuratorFrameworkFactory.newClient(conneString,retryPolicy);
	
	//工厂创建客户端的另一种方式
	CuratorFramework client2=CuratorFrameworkFactory
			                  .builder()
							  .connectString(conneString)
							  .connectionTimeoutMs(3000)
							  .retryPolicy(retryPolicy)
							  .sessionTimeoutMs(3000)
							  .canBeReadOnly(false)
							  .defaultData(null)
							  .build();
	//开启客户端
	public CuratorCurd() {
		client.start();
	}
	
	/**
	 * 在path路径下新增节点及数据
	 * @param path
	 * @param data
	 */
	public void create(String path, byte[] data) throws Exception {
	  String res = client.create().creatingParentsIfNeeded().forPath(path, data);	
	}
	
	/**
	 * 删除path路径下的节点
	 * @param path
	 */
	public void delete(String path) throws Exception {
		 client.delete().forPath(path);
	}
	/**
	 * 获取path路径下的节点数据
	 * @param path
	 */
	public String  getData(String path) throws Exception {
		byte[] res = client.getData().forPath(path);
		return new String(res);
	}
	/**
	 * 设置path路径下的节点数据
	 * @param path
	 */
	public void  setData(String path,byte[] data) throws Exception {
		 client.setData().forPath(path,data);
	}
	/**
	 * path 路径下节点是否存在
	 * @param path
	 * stat是对znode节点的一个映射，stat=null表示节点不存在
	 */
	public Stat checkExists(String path) throws Exception {
		Stat res = client.checkExists().forPath(path);
		return res;
	}
	/**
	 * 获取子节点
	 * @param path
	 */
	public List<String> getChildren(String path) throws Exception {
		List<String> pathList = client.getChildren().forPath(path);
		return pathList;
	}
	//关闭客户端的连接
	public void close() {
		client.close();
	}
	
	//测试
	public static void main(String[] args) {
		String path="/root";
        CuratorCrud curatorCrud=new CuratorCrud();
        if(null!=curatorCrud.checkExists(path)) {
            curatorCrud.delete(path);
            System.out.println("---删除---");
        }
       curatorCrud.create(path+"/bbb"+"/ccc","hi".getBytes());//递归创建 可以支持赋值
       List<String> list= curatorCrud.getChildren("/");//获取path下面的节点
        for(String chipath:list){
            System.out.println(chipath);
        }
       curatorCrud.setData(path,"hello".getBytes());
       System.out.println(new String(curatorCrud.getData(path)));
	}
}
