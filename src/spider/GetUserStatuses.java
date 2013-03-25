package spider;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

import weibo4j.Timeline;
import weibo4j.Weibo;
import weibo4j.model.Paging;
import weibo4j.model.Status;
import weibo4j.model.StatusWapper;
import weibo4j.model.User;
import weibo4j.model.WeiboException;

public class GetUserStatuses {

	/**
	 * @param args
	 * @author nonodevil
	 * @email linux.kakit@gmail.com
	 */
	public static void main(String[] args) throws IOException {
		try {
			String access_token = "";	//输入应用的access_token
			Weibo weibo = new Weibo();
			Timeline timeline = new Timeline();
			timeline.client.setToken(access_token);

			//输入需要查询的微博用户名
			System.out.println("Please input screen_name:");
			Scanner find_name = new Scanner(System.in);
			String screen_name = find_name.next();		
			
			//创建一个以微博用户微博的文件夹
			String dir_name = "./data/" + screen_name + "/";
			File dir = new File(dir_name);

			if (!dir.exists()) {
				dir.mkdirs();
			} else {
				System.out.println(dir.getName() + "is already exists!");
			}
			
			int page = 1;
			int count = 50;
			int base_app = 0;
			int feature = 0;
			int i = 1;
			int flag = 1;
			Paging paging = new Paging();
			String isVerified;
			String user_status;
			String user_verified_reason;
			String user_name;
			String user_id;
			String user_description;
			long statuses_totalnumber = 0;
			
			StatusWapper statusList = timeline.getUserTimelineByName(screen_name);
			statuses_totalnumber = statusList.getTotalNumber();
			for (page = 1; page < 50; page++) {
				paging.setPage(page);
				paging.setCount(count);
				statusList = timeline.getUserTimelineByName(
						screen_name, paging, base_app, feature);
				for (Status status : statusList.getStatuses()) {
					if (flag == 1) {
						//username.txt
						String user_file_name = dir.getAbsolutePath()  + "/" + screen_name + ".txt";
						RandomAccessFile user_file = new RandomAccessFile(user_file_name, "rw");
						User user = status.getUser();
						
						//name
						user_name = user.getName();
						user_file.write("name:".getBytes());
						user_file.write(user_name.getBytes());
						user_file.write("\r\n".getBytes());
						//id
						user_id = user.getId();
						user_file.write("id:".getBytes());
						user_file.write(user_id.getBytes());
						user_file.write("\r\n".getBytes());
						//description
						user_description = user.getDescription();
						user_file.write("description:".getBytes());
						user_file.write(user_description.getBytes());
						user_file.write("\r\n".getBytes());
						
						//加V信息
						if (user.isVerified()) {
							isVerified = "isVerified:Yes";
							user_file.write(isVerified.getBytes());
							user_file.write("\r\n".getBytes());
							user_verified_reason = "reason:"
									+ user.getVerified_reason();
							user_file.write(user_verified_reason.getBytes());
							user_file.write("\r\n".getBytes());
						} else {
							isVerified = "isvERIFIED:No";
							user_file.write(isVerified.getBytes());
							user_file.write("\r\n".getBytes());
						}	//end if (user.isVerified()) 
						
						flag = 0;
						user_file.close();
					}	//end if (flag == 1)
					
					//微博信息
					String weibo_file_name = dir.getAbsolutePath() + "/" + Integer.toString(i) + ".txt";
					RandomAccessFile weibo_file = new RandomAccessFile(weibo_file_name, "rw");
					i++;
					if (status.getRetweetedStatus() == null) {
						user_status = status.getText();
					} else {
						user_status = status.getText()
								+ status.getRetweetedStatus().getText();
					}
					weibo_file.write(user_status.getBytes());
					weibo_file.close();
				}	//end for (Status status : statusList.getStatuses())
				
				//判断跳出，减少API调用次数 
				if (((page * count)/(int)statuses_totalnumber) > 0) {
					break;
				}
			}	//end for (page = 1; page < 50; page++)
			System.out.println("Done");
		} catch (WeiboException e) {
			e.printStackTrace();
		}
	}

}
