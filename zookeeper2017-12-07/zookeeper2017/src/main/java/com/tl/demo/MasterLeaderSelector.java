package com.tl.demo;

import java.util.List;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.shaded.com.google.common.collect.Lists;

/**
 * how to  use LeaderSelector  to implement master 
 *
 */

public class MasterLeaderSelector {
	
    //设置客户端的数量
	static int countClient=10;
	
	//设置leader的路径
	static String select_path="/selector";
	
	public static void main(String[] args) {
			List<CuratorFramework> clientList=Lists.newArrayListWithCapacity(countClient);
	        for(int i=0;i<countClient;i++) {
	        	CuratorFramework client=getClient();
	        	clientList.add(client);
	        	LeaderSelector leaderSelector=new LeaderSelector(client,select_path,new LeaderSelectorListener() {
					
					@Override
					public void stateChanged(CuratorFramework client, ConnectionState newState) {
						//失去连接是的状态
						System.out.println("Client的连接状态发生变化");
						
					}
					
					@Override
					public void takeLeadership(CuratorFramework client) throws Exception {
						//抢主成功后的业务处理逻辑
						System.out.println("获取leader的权利！");
						
					}
				});
	        	//leaderSelector 释放leader后会自动加入到抢主的行列
	        	leaderSelector.autoRequeue();
	        	leaderSelector.start();
	        	
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
