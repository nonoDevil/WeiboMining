package spider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;

import weibo4j.Account;
import weibo4j.Friendships;
import weibo4j.Timeline;
import weibo4j.Users;
import weibo4j.examples.oauth2.Log;
import weibo4j.model.Paging;
import weibo4j.model.RateLimitStatus;
import weibo4j.model.Status;
import weibo4j.model.StatusWapper;
import weibo4j.model.User;
import weibo4j.model.WeiboException;

public class GetInfo {

	/**
	 * @param args
	 */
	public String access_token = "";
	public String nameFileName = "./data/namelist.txt";
	public String idFileName = "./data/idlist.txt";
	public ArrayList<String> nameList = new ArrayList<String>();
	public ArrayList<String> idList = new ArrayList<String>();
	public RateLimitStatus json;
	public static int SLEEP_TIME = 3700000;
	
	public static void main(String[] args) {
		GetInfo getInfo = new GetInfo();
		getInfo.printApiLimit();
		getInfo.setNameList();
		getInfo.setIdList();
		//getInfo.getUrl();
		//getInfo.getUserInfo();
		getInfo.getUserFriends();
		//getInfo.getUserStatuses();
		getInfo.printApiLimit();
		System.out.println("Done");
	}
	
	
	//读取namelist.txt文件，存入到nameList数组中
	public void setNameList() {
		try {
			FileReader fr = new FileReader(nameFileName);
			BufferedReader br = new BufferedReader(fr);
			String temp = null;
			for (int i = 0; ; i++) {
				temp = br.readLine();
				if (temp == null) {
					break;
				} else {
					nameList.add(temp);
				}
			}
			br.close();
			fr.close();	
			//输出nameList
			int nameListSize = nameList.size();
			for (int i = 0; i < nameListSize; i++) {
				System.out.println(nameList.get(i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//从idist.txt中获取用户ID，存入IdList数组中
	public void setIdList() {
		try {
			FileReader fr = new FileReader(idFileName);
			BufferedReader br = new BufferedReader(fr);
			String temp = null;
			for (int i = 0; ; i++) {
				temp = br.readLine();
				if (temp == null) {
					break;
				} else {
					idList.add(temp);
				}
			}
			br.close();
			fr.close();	
			//输出nameList
			int idListSize = idList.size();
			for (int i = 0; i < idListSize; i++) {
				System.out.println(idList.get(i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//根据idList的内容获取用户信息，写入./data/info/文件夹中
	//信息包括id, screen_name, weibourl, imageUrl50, imageUrl180, description 
	public void getUserInfo() {
		Users um = new Users();
		um.client.setToken(access_token);
		
		User user;
		String userId;
		String userName;
		String userUrl;
		String userImageUrl50;
		String userImageUrl180;
		String userDescription;
		String lineInput;
		int idListSize = idList.size();

		try {

			String InfoFileName;
			for (int i = 0; i < idListSize; i++) {
				//判断API剩余次数，决定是否睡眠
				this.printApiLimit();
				if ((json.getRemainingIpHits() <= 10) || (json.getRemainingUserHits() <= 10 )) {
					System.out.println("Api count not enough!");
					Thread.sleep(SLEEP_TIME);
				}
				userId = idList.get(i);
				InfoFileName = "./data/info/" + userId + ".txt";
				File check = new File(InfoFileName);
				if (check.exists()) {
					continue;
				} 
				FileWriter fw = new FileWriter(InfoFileName);	
				user = um.showUserByScreenName(nameList.get(i));	
				userId = user.getId();
				userName = nameList.get(i);
				userUrl = "http://weibo.com/u/" + userId + "/";
				userImageUrl50 = user.getProfileImageUrl();
				userImageUrl180 = userImageUrl50.replace("/50/", "/180/");
				userDescription = user.getDescription();
				lineInput = userId + " " + userName + " " + userUrl + " " 
						  + userImageUrl50 + " " + userImageUrl180 + " " + userDescription;
				fw.write(lineInput);
				fw.write("\r\n");
				fw.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (WeiboException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	//获取用户关注ID
	public void getUserFriends() {
		
		int idListSize = idList.size();
		String friendsFileName;
		Users um = new Users();	
		Friendships fm = new Friendships();
		um.client.setToken(access_token);
		fm.client.setToken(access_token);
		String userId;
		User user;

		int friendsCount = 0;
		try {
			for (int i =0; i < idListSize; i++) {		
				userId = idList.get(i);
				
				//创建以查询用户ID为名的文件
				friendsFileName = "./data/relationship/" + userId + ".txt";
				File file = new File(friendsFileName);
				if (file.exists()) {	//文件存在则跳过此次获取用户ID
					continue;
				}
				
				//判断API剩余次数，决定是否睡眠
				this.printApiLimit();
				if ((json.getRemainingIpHits() <= 10) || (json.getRemainingUserHits() <= 10 )) {
					System.out.println("Api count not enough!");
					Thread.sleep(SLEEP_TIME);
				}
				user = um.showUserById(userId);
				
				if ((json.getRemainingIpHits() <= 10) || (json.getRemainingUserHits() <= 10 )) {
					System.out.println("Api count not enough!");
					Thread.sleep(SLEEP_TIME);
				}
				friendsCount = user.getFriendsCount();
			

				FileWriter fw = new FileWriter(friendsFileName);
				//获取用户关注ID列表并写入文件
				int count = 200;
				for (int cursor = 0; (0 == (cursor / friendsCount) && (cursor <= 5000)); cursor += 200) {
					String friendsId[] = fm.getFriendsIdsByUid(userId, count, cursor);
					for (int j = 0; j < friendsId.length; j++) {
						fw.write(friendsId[j]);
						fw.write("\r\n");
					}
				}
				fw.close();
			}
		} catch (WeiboException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	//获取用户的1000条微博
	public void getUserStatuses() {
		try {
			Timeline timeline = new Timeline();
			timeline.client.setToken(access_token);

			int nameListSize = nameList.size();
			for (int i =0; i < nameListSize; i++) {
				this.printApiLimit();
				if ((json.getRemainingIpHits() <= 10) || (json.getRemainingUserHits() <= 10)) {
					System.out.println("Api count not enough!");
					Thread.sleep(SLEEP_TIME);
				}
				//创建一个以微博用户ID命名的文件夹
				String userId = idList.get(i);
				String dirName = "./data/statuses/" + userId + "/";
				File dir = new File(dirName);
				if (dir.exists()) {
					System.out.println(dir.getName() + "is already exists!");
					continue;
				} else {
					dir.mkdirs();	
				}
				
				//开始获取微博
				int page = 1;		//从第一页开始
				int count = 100;	//每页获取微博条数
				int base_app = 0;
				int feature = 0;
				int item = 1;		//记录微博数
				String userStatus;
				Paging paging = new Paging();
				long statuses_totalnumber = 0;
				StatusWapper statusList = timeline.getUserTimelineByUid(userId);
				statuses_totalnumber = statusList.getTotalNumber();
				
				//一共获取最多1000条微博
				for (page = 1; page <= 10; page++) {
					paging.setPage(page);
					paging.setCount(count);
					this.printApiLimit();
					if ((json.getRemainingIpHits() <= 10) || (json.getRemainingUserHits() <= 10)) {
						System.out.println("Api count not enough!");
						Thread.sleep(SLEEP_TIME);
					}
					statusList = timeline.getUserTimelineByUid(
							userId, paging, base_app, feature);
					for (Status status : statusList.getStatuses()) {	
						String statusFileName = dir.getAbsolutePath() + "/" + Integer.toString(item) + ".txt";
						item++;
						File check = new File(statusFileName);
						if (check.exists()) {	//文件存在的话跳出
							continue;
						}
						FileWriter fw = new FileWriter(statusFileName);
						if (status.getRetweetedStatus() == null) {	//原创微博
							userStatus = status.getText();
						} else { //转发微博
							userStatus = status.getText()
									+ status.getRetweetedStatus().getText();
						}
						fw.write(userStatus);
						fw.close();
					}
					//判断跳出，减少API调用次数 
					if (((page * count)/(int)statuses_totalnumber) > 0) {
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WeiboException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//获取url测试
	public void getUrl() {
		Users um = new Users();
		um.client.setToken(access_token);
		
		System.out.println("Please input a screen name:");
		Scanner inputName = new Scanner(System.in);
		String screen_name = inputName.next();
		
		String userId;
		String userName;
		String userImageUrl50;
		String userImageUrl180;
		String userDescription;
		User user;
		try {
			user = um.showUserByScreenName(screen_name);
			userId = user.getId();
			userName = user.getName();
			userImageUrl50 = user.getProfileImageUrl();
			userImageUrl180 = userImageUrl50.replace("/50/", "/180/");
			userDescription = user.getDescription();
	
			System.out.println("id = " + userId);
			System.out.println("name = " + userName);
			System.out.println("url = " + "http://weibo.com/u/" + userId + "/");
			System.out.println("image50_url = " + userImageUrl50);
			System.out.println("image180_url = " + userImageUrl180);
			System.out.println("description = " + userDescription);
		} catch (WeiboException e) {
			e.printStackTrace();
		}
	}
	
	
	//输出剩余API调用次数
	public void printApiLimit() {
		Account account = new Account();
		account.client.setToken(access_token);
		try {
			json = account.getAccountRateLimitStatus();
        	System.out.println("RemainingIpHits: " + json.getRemainingIpHits());
        	System.out.println("RemainingUserHits" + json.getRemainingUserHits());
			Log.logInfo(json.toString());
		} catch (WeiboException e) {
			e.printStackTrace();
		}
	}
}
