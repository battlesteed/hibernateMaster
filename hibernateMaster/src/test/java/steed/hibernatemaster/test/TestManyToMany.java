package steed.hibernatemaster.test;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import steed.ext.util.base.BaseUtil;
import steed.hibernatemaster.sample.domain.user.Role;
import steed.hibernatemaster.sample.domain.user.User;
import steed.hibernatemaster.util.DaoUtil;
import steed.hibernatemaster.util.QueryBuilder;

public class TestManyToMany extends SteedTest{
	
	@Test
	public void testIn() {
		String nickName = "testInQuery";
		User user = new User();
		User user2 = new User("testInQuery2");
		
		Role e = new Role("testInQueryRole2");
		Role e2 = new Role("testInQueryRole1");
		
		prepareData(user, user2, e, e2, nickName);
		
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
//		queryBuilder.add("nickName", nickName);
		queryBuilder.add("nickName", "testInQuery%");
		queryBuilder.addNotIn("roleSet", Arrays.asList(new Role[] {e2}));
		count = DaoUtil.getCount(User.class, queryBuilder.getWhere());
		assert(count == 2);
		
		queryBuilder.addNotIn("roleSet", new Role[] {e2});
		count = DaoUtil.getCount(User.class, queryBuilder.getWhere());
		BaseUtil.out(count);
		assert(count == 2);
		
		queryBuilder.addNotIn("roleSet", new HashSet<>(Arrays.asList(new Role[] {e})));
		count = DaoUtil.getCount(User.class, queryBuilder.getWhere());
		assert(count == 2);
		
		queryBuilder.addNotIn("roleSet", Arrays.asList(new Role[] {e2}));
		count = DaoUtil.getCount(User.class, queryBuilder.getWhere());
		assert(count == 2);
		
		queryBuilder.add("nickName", "testInQuery");
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

	private void prepareData(User user,User user2, Role e, Role e2, String nickName) {
		
		user.setNickName(nickName);
		
		HashSet<Role> roleSet = new HashSet<>();
		roleSet.add(e);
		roleSet.add(e2);
		user.setRoleSet(roleSet);
		
		DaoUtil.deleteByQuery(user);
		DaoUtil.deleteByQuery(e);
		DaoUtil.deleteByQuery(e2);
		DaoUtil.deleteByQuery(user2);
		
		e.save();
		e2.save();
		user.save();
		user2.save();
	}
}
