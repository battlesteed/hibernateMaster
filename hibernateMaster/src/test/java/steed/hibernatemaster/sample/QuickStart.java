package steed.hibernatemaster.sample;


import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import steed.hibernatemaster.sample.domain.user.User;
import steed.hibernatemaster.test.SteedTest;

/**
 * 快速入门.
 * @author 战马
 *
 */
@FixMethodOrder(MethodSorters.JVM)
public class QuickStart extends SteedTest{
	/**
	 * 运行之后会保存一个niceName为'战马'的user实体类.
	 * 不需要提前建表,hibernate会自动生成.
	 */
	@Test
	public void save(){
		User user = new User();
		user.setNickName("战马");
		user.save();
	}
	
	/**
	 * 运行之后会把主键为"战马"的user实体类的数据库记录中name列设置为'战小马',
	 * 其他列设置为null.因为会把其他列设置为null,所以一般不直接update
	 * @see #updateNotNullFild()
	 */
	@Test
	public void update(){
//		save();
		User user = new User();
		user.setNickName("战马");
		user.setName("战小马");
		user.update();
	}
	
	/**
	 * 运行之后会把主键为"战马"的user实体类的数据库记录中e_mail设置为'"battle_steed@163.com"',
	 * 并且不影响其他记录.这方法适合利用struts的modelDriven把值封装到实体类后更新到数据库.
	 * 因为前台一般不把user的字段值全部传过来,比如密码或用户状态等就不会传,
	 * 这时候直接update数据库记录的密码列更新为null了,所以需要updateNotNullFild(只更新不为null的字段).
	 */
	@Test
	public void updateNotNullFild(){
		User user = new User();
		user.setNickName("战马");
		user.setE_mail("battle_steed@163.com");
		user.updateNotNullField(null);
	}
	
	
	/**
	 * 运行之后会删除一个nickName为'战马'的user实体类.
	 * 不需要提前建表,hibernate会自动生成.
	 */
	@Test
	public void delete(){
		User user = new User();
		user.setNickName("战马");
		user.delete();
	}
}
