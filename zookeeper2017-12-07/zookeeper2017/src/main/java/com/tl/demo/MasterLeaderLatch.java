package com.tl.demo;
/**
 * how to  use LeaderLatch  to implement master 
 *
 */

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

public class MasterLeaderLatch {
	
    //connnect String for server
	String connectString="192.168.6.2:2181";
	
	//重试策略
	RetryPolicy  retryPolicy=new ExponentialBackoffRetry(1000,3);
	
	
	
	//the path latch can prove info
	String latchPath="/curator/latch";
	
	//number of client 
	int clientCount=10;
	
	//list for LeaderLatch
	List<LeaderLatch> leaderLatchList=new ArrayList<LeaderLatch>(clientCount);
	List<CuratorFramework> clientList=new ArrayList<CuratorFramework>(clientCount);
	//use curatorFramework to init leaderLatch
	public void initLeaderLatchList() throws Exception {
		
		for(int i=0;i<10;i++) {
			//客户端
			CuratorFramework client=getZkClient();
			clientList.add(client);
			/* client.start(); */
			LeaderLatch latch=new LeaderLatch(client,latchPath,"Client"+i );
			leaderLatchList.add(latch); 
			latch.start();
		}
		//wait for elect
		
		System.out.println("init client over! and complete election!");
	}
	
	
	   private static CuratorFramework getZkClient() {
	        String zkServerAddress = "192.168.6.2:2181";
	        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3, 5000);
	        CuratorFramework zkClient = CuratorFrameworkFactory.builder()
	                .connectString(zkServerAddress)
	                .sessionTimeoutMs(5000)
	                .connectionTimeoutMs(5000)
	                .retryPolicy(retryPolicy)
	                .build();
	        zkClient.start();
	        return zkClient;
	    }
	
	//get master and mantual get master
	public void getMasterAndOper() throws Exception {
		
		for (LeaderLatch latch:leaderLatchList) {
			if(latch.hasLeadership()) {
				System.out.println("current master id is "+latch.getId());
				//close master and reselect 
				latch.close();
				leaderLatchList.remove(latch);
				TimeUnit.SECONDS.sleep(5);
				break;
			}
		}
		
		
		
		
		
		
		for (LeaderLatch latch:leaderLatchList) {
			if(latch.hasLeadership()) {
				System.out.println("remove and reselect  master id is "+latch.getId());
				latch.close();
				leaderLatchList.remove(latch);
				System.out.println("reselect next node is "+leaderLatchList.get(0).getId());
				break;
			}
		}
		
		
		leaderLatchList.get(0).await(10,TimeUnit.SECONDS);
		
		for (LeaderLatch latch:leaderLatchList) {
			if(latch.hasLeadership()) {
				System.out.println("result is master is "+latch.getId());
				break;
			}
		}
		
		
		
	}
	
	public void close() {
		for (LeaderLatch latch:leaderLatchList) {
			CloseableUtils.closeQuietly(latch);
		}
		for (CuratorFramework client:clientList) {
			CloseableUtils.closeQuietly(client);
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		MasterLeaderLatch latch=new MasterLeaderLatch();
		latch.initLeaderLatchList();
		latch.getMasterAndOper();
		latch.close();
	}
	
}
