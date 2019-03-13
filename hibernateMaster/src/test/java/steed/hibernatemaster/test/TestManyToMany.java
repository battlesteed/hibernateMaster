package steed.hibernatemaster.test;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import steed.hibernatemaster.sample.domain.user.Role;
import steed.hibernatemaster.sample.domain.user.User;
import steed.hibernatemaster.util.DaoUtil;
import steed.hibernatemaster.util.QueryBuilder;
import steed.util.base.BaseUtil;

public class TestManyToMany extends SteedTest{
	
	@Test
	public void testIn() {
		User user = new User();
		Role e = new Role("testInQueryRole2");
		Role e2 = new Role("testInQueryRole1");
		String nickName = "testInQuery";
		user.setNickName(nickName);
		user.delete();
		e.delete();
		e2.delete();
		
		DaoUtil.managTransaction();
		DaoUtil.relese();
		
		HashSet<Role> roleSet = new HashSet<>();
		roleSet.add(e);
		roleSet.add(e2);
		user.setRoleSet(roleSet);
		
		
		e.save();
		e2.save();
		user.save();
		
		DaoUtil.managTransaction();
		DaoUtil.closeSessionNow();
		
		QueryBuilder queryBuilder = new  QueryBuilder();
		queryBuilder.add("roleSet", new Role[] {e}).add("nickName", nickName);
		long count = DaoUtil.getCount(User.class, queryBuilder.getWhere());
		assert(count == 1);
		
		queryBuilder.add("roleSet", new Role[] {e2});
		count = DaoUtil.getCount(User.class, queryBuilder.getWhere());
		BaseUtil.out(count);
		assert(count == 1);
		
		queryBuilder.add("roleSet", new HashSet<>(Arrays.asList(new Role[] {e})));
		count = DaoUtil.getCount(User.class, queryBuilder.getWhere());
		assert(count == 1);
		
		
		queryBuilder.add("roleSet", Arrays.asList(new Role[] {e2}));
		count = DaoUtil.getCount(User.class, queryBuilder.getWhere());
		assert(count == 1);
		
		
		queryBuilder = new  QueryBuilder(User.class);
		queryBuilder.add("nickName", nickName);
		queryBuilder.addNotIn("roleSet", Arrays.asList(new Role[] {e2}));
		count = DaoUtil.getCount(User.class, queryBuilder.getWhere());
		assert(count == 1);
		
		queryBuilder.addNotIn("roleSet", new Role[] {e2});
		count = DaoUtil.getCount(User.class, queryBuilder.getWhere());
		BaseUtil.out(count);
		assert(count == 1);
		
		queryBuilder.addNotIn("roleSet", new HashSet<>(Arrays.asList(new Role[] {e})));
		count = DaoUtil.getCount(User.class, queryBuilder.getWhere());
		assert(count == 1);
		
		queryBuilder.addNotIn("roleSet", Arrays.asList(new Role[] {e2}));
		count = DaoUtil.getCount(User.class, queryBuilder.getWhere());
		assert(count == 1);
		
		
	}
}
