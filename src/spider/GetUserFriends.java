package spider;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

import weibo4j.Account;
import weibo4j.Friendships;
import weibo4j.Users;
import weibo4j.Weibo;
import weibo4j.examples.oauth2.Log;
import weibo4j.model.RateLimitStatus;
import weibo4j.model.User;
import weibo4j.model.WeiboException;

/**
 *@description 获得指定用户关注者的ID,并写入到data/realation文件夹中,以指定用户ID命名的文件中
 *@create data 2013/3/28
 **/


public class GetUserFriends {


	/**
	 * @param args
	 * @author nonoDevil (linux.kakit@gmail.com) 
	 */
	public static void main(String[] args) throws IOException{
		try {
			//初始化数据，设置access_token
			Weibo weibo = new Weibo();
			Friendships fm = new Friendships();
			String access_token = "access_token";	//access_toekn改为应用的access_token
			Users um = new Users();
			um.setToken(access_token);
			fm.setToken(access_token);
			
			//输入要查询的用户名
			System.out.println("Please input scree_name:");
			Scanner find_name = new Scanner(System.in);
			String screen_name = find_name.next();
			
			//创建一个以微博用户微博的文件夹
			String dir_name = "./data/" + "relationship" + "/";
			File dir = new File(dir_name);
			if (!dir.exists()) {
				dir.mkdirs();
			} else {
				System.out.println(dir.getName() + "is already exists!");
			}
			
			//获取用户ID
			User user = um.showUserByScreenName(screen_name);
			String user_id = user.getId(); 
			int user_friends_count = user.getFriendsCount();
			
			//创建以查询用户ID为密码的文件
			String filename = dir.getPath() + "/" + user_id;
			RandomAccessFile outfile = new RandomAccessFile (filename,"rw");
			

			int IpLimit = 0;
			int RemainingIpHits = 0;
			int UserLimit = 0;
			int RemainingUserHits = 0;
			
			//获取剩余API调用次数
			Account am = new Account();
			am.client.setToken(access_token);
			
			int count = 200;
			for (int cursor = 0; (0 == (cursor / user_friends_count) && (cursor <= 5000)); cursor += 200) {
				
	        	RateLimitStatus json = am.getAccountRateLimitStatus();
	        	RemainingIpHits = json.getRemainingIpHits();
	        	RemainingUserHits = (int)json.getRemainingUserHits();
	        	System.out.println("RemainingIpHits: " + RemainingIpHits);
	        	System.out.println("RemainingUserHits: " + RemainingUserHits);
				if ((RemainingIpHits <= 10) || (RemainingUserHits <= 10)) {
					try {
						Thread.sleep(4000000);	//如果API次数收到限制，睡眠4000s
					} catch (InterruptedException e){
						e.printStackTrace();
					}
				}
	        	Log.logInfo(json.toString());
				
				String user_friends[] = fm.getFriendsIdsByUid(user_id, count, cursor);
				for (int i = 0; i < user_friends.length; i++) {
					outfile.write(user_friends[i].getBytes());
					outfile.write("\r\n".getBytes());
				}
				
			}
			outfile.close();
        	System.out.println("RemainingIpHits: " + RemainingIpHits);
        	System.out.println("RemainingUserHits: " + RemainingUserHits);
			System.out.println("Done");
		} catch (WeiboException e) {
			e.printStackTrace();
		} 
	}
}
