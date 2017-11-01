package steed.hibernatemaster.sample;


import org.junit.Test;

import steed.hibernatemaster.Config;
import steed.hibernatemaster.sample.domain.user.User;
import steed.hibernatemaster.util.DaoUtil;

/**
 * 快速入门.
 * @author 战马
 *
 */
public class QuickStart {
	/**
	 * 运行之后会保存一个niceName为'战马'的user实体类.
	 * 不需要提前建表,hibernate会自动生成.
	 */
	@Test
	public void save(){
		//设置自动提交事务
		Config.autoCommitTransaction = true;
		User user = new User();
		user.setNickName("战马");
		user.save();
	}
	
	/**
	 * 运行之后会把主键为"战马"的user实体类的数据库记录中name列设置为'战小马',其他列设置为null.因为会把其他列设置为null,所以一般不直接update
	 * 
	 * @see #updateNotNullFild()
	 */
	@Test
	public void update(){
		User user = new User();
		user.setNickName("战马");
		user.setName("战小马");
		user.update();
		//因为没有设置自动提交事务,默认是不会自动提交事务的,所以这里手动提交.
		DaoUtil.managTransaction();
	}
	/**
	 * 运行之后会把主键为"战马"的user实体类的数据库记录中e_main设置为'"battle_steed@163.com"',并且不影响其他记录.
	 * 这方法适合利用struts的modelDriven把值封装到实体类后更新到数据库.因为前台一般不把user的字段值全部传过来,比如密码就不会传,
	 * 这时候直接update就会把密码更新为null了,所以需要updateNotNullFild(只更新不为null的字段).
	 */
	@Test
	public void updateNotNullFild(){
		//设置自动提交事务
		Config.autoCommitTransaction = true;
		User user = new User();
		user.setNickName("战马");
		user.setE_mail("battle_steed@163.com");
		user.updateNotNullField(null);
	}
	
	
	/**
	 * 运行之后会删除一个niceName为'战马'的user实体类.
	 * 不需要提前建表,hibernate会自动生成.
	 */
	@Test
	public void delete(){
		//设置自动提交事务
		Config.autoCommitTransaction = true;
		User user = new User();
		user.setNickName("战马");
		user.delete();
	}
	
	
}
