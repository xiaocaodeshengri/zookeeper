package com.tl.api.curator.wacher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.tl.api.curator.CuratorCrud;

public class CuratorWatcherDemo {
	String connectString="192.168.6.2:2181";
	//设置重连策略
     RetryPolicy retryPolicy=new ExponentialBackoffRetry(3000,3);
	//使用工厂创建客户端
	 CuratorFramework client=CuratorFrameworkFactory.newClient(connectString,retryPolicy);
	
	 public CuratorWatcherDemo() {
		 //创建客户端的连接
		 client.start();
	 }
	 
	 //为path路径下所有的子节点建立监听watcher
	public void setListenterForPathChild(String path) throws Exception {
		 PathChildrenCache pathChildrenCache=new PathChildrenCache(client, path, true);
		 PathChildrenCacheListener listener =new PathChildrenCacheListener() {
			
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                        ChildData data = event.getData();
                        switch (event.getType()) {
						case CHILD_ADDED:
							System.out.println("CHILD_ADDED : "+ data.getPath() +"  数据:"+ new String (data.getData()));
							break;
						case CHILD_UPDATED:
							System.out.println("CHILD_UPDATED : "+ data.getPath() +"  数据:"+ new String (data.getData()));
							break;
						case CHILD_REMOVED:
							System.out.println("CHILD_REMOVED : "+ data.getPath() +"  数据:"+ new String (data.getData()));
							break;
						default:
							break;
						}
				
			}
		};
		pathChildrenCache.getListenable().addListener(listener );
		pathChildrenCache.start(StartMode.POST_INITIALIZED_EVENT);;
	} 
	
	//为path路径建立监听watcher不包括子节点
	public void setListnerForNode(String path) throws Exception {
		final NodeCache nodeCache=new NodeCache(client, path, false);
		ExecutorService pool =Executors.newCachedThreadPool() ;
		
		
		nodeCache.getListenable().addListener(new NodeCacheListener() {
			
			@Override
			public void nodeChanged() throws Exception {
				if(null!=nodeCache.getCurrentData()) {
					System.out.println("path : " + nodeCache.getCurrentData().getPath());
                    System.out.println("data : " + new String(nodeCache.getCurrentData().getData()));
                    System.out.println("stat : " + nodeCache.getCurrentData().getStat());
				}
			}
		}, pool);
		nodeCache.start();
	}
	
	//监控所有的父节点及子节点变化
	public void setListnerForPathAndNode(String path) throws Exception {
		TreeCache treeCache=new TreeCache(client, path);
		ExecutorService pool=Executors.newCachedThreadPool();
		treeCache.getListenable().addListener(new TreeCacheListener() {
			
			@Override
			public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                        ChildData data = event.getData();
                        if(null!=data) {
	                        switch (event.getType()) {
		                        case NODE_ADDED:
		                            System.out.println("NODE_ADDED : "+ data.getPath() +"  数据:"+ new String(data.getData()));
		                            break;
		                        case NODE_REMOVED:
		                            System.out.println("NODE_REMOVED : "+ data.getPath() +"  数据:"+ new String(data.getData()));
		                            break;
		                        case NODE_UPDATED:
		                            System.out.println("NODE_UPDATED : "+ data.getPath() +"  数据:"+ new String(data.getData()));
		                            break;
		                        default:
		                            break;
							}
                        }else {
                        	System.out.println( event.getType());
                        }
				
			}
		}, pool);
		treeCache.start();
	}

	
	 public static void main(String[] args) throws Exception {
		 String path = "/root";
	        /****============监听开启========**/
		 CuratorWatcherDemo curatorWatcher = new CuratorWatcherDemo();
	        curatorWatcher.setListenterForPathChild(path);//子节点重复监听
	       // curatorWatcher.setListnerForNode(path);//当前节点重复监听
	       // curatorWatcher.setListnerForPathAndNode(path);//当前和子节点重复监听
	        /****============数据更新========**/
	        CuratorCrud curatorCrud=new CuratorCrud();
	        if(null!=curatorCrud.checkExists(path)) {
	            curatorCrud.delete(path);
	        }
	        Thread.sleep(1000);
	        curatorCrud.create(path,"111".getBytes());
	        System.out.println("------创建目录-----");
	        Thread.sleep(1000);
	        curatorCrud.setData(path,"222".getBytes());
	        System.out.println("------设置目录-----");
	        Thread.sleep(1000);
	        curatorCrud.create(path+"/ccc","333".getBytes());
	        System.out.println("------创建子目录-----");
	        Thread.sleep(1000);
	        curatorCrud.setData(path+"/ccc","444".getBytes());
	        System.out.println("------设置子目录-----");
	        Thread.sleep(1000);

	} 
}
