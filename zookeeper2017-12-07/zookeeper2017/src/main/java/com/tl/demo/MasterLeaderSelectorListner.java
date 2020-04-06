package com.tl.demo;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.shaded.com.google.common.collect.Lists;

public class MasterLeaderSelectorListner {

	
	 //设置客户端的数量
		static int countClient=10;
		
		//设置leader的路径
		static String select_path="/selector";
		
		public static void main(String[] args) {
				List<CuratorFramework> clientList=Lists.newArrayListWithCapacity(countClient);
		        for(int i=0;i<countClient;i++) {
		        	CuratorFramework client=getClient();
		        	clientList.add(client);
		        	SelectorClient	leaderSelector =new SelectorClient(client,select_path,"client"+i);
		        	//leaderSelector 释放leader后会自动加入到抢主的行列
					System.out.println("开始选举！");
					leaderSelector.start();
		        	
		        }	
		}

		 static class SelectorClient extends LeaderSelectorListenerAdapter implements Cloneable{
			 String nameString; 
			 LeaderSelector selector;
			
			 public SelectorClient(CuratorFramework client, String path, String name) {
				 nameString=name;
				 selector=new LeaderSelector(client, path, this);
				 System.out.println("释放权力！");
				 selector.autoRequeue();
			}

			@Override
			public void takeLeadership(CuratorFramework client) throws Exception {
				//run waittime off leader
				System.out.println("获取领导权！");
				final int waitTime=(int)(5*Math.random())+1;
				Thread.sleep(TimeUnit.SECONDS.toMillis(waitTime));
			}
			 
			public void start() {
				selector.start();
			}
			
			public void close() {
				selector.close();
			}
		 }
		
	    //create a client
		private static CuratorFramework getClient() {
			RetryPolicy policy=new ExponentialBackoffRetry(1000,3);
			CuratorFramework curatorFramework=	CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
			                                 .sessionTimeoutMs(1000)
			                                 .connectionTimeoutMs(3000)
			                                 .retryPolicy(policy)
			                                 .build();
			curatorFramework.start();
			return curatorFramework;
		}
}
