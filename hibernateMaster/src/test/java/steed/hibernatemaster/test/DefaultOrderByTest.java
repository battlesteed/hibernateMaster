package steed.hibernatemaster.test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import steed.hibernatemaster.annotation.DefaultOrderBy;
import steed.hibernatemaster.domain.BaseDatabaseDomain;
import steed.hibernatemaster.sample.domain.Student;
import steed.hibernatemaster.util.DaoUtil;

public class DefaultOrderByTest extends SteedTest{
	
	@Test
	public void testSetDefaultOrderBy() {
		Map<Class<? extends BaseDatabaseDomain>, DefaultOrderBy> defaultOrderBy = getDefaultOrderBy();
		DefaultOrderBy preOrderby = defaultOrderBy.get(Student.class);
		assert(preOrderby != null);
		
		testStudentIDOrderBy(defaultOrderBy,true);
		testStudentIDOrderBy(defaultOrderBy,false);
		
		testStudentClassStudentCountOrderBy(defaultOrderBy,true);
		testStudentClassStudentCountOrderBy(defaultOrderBy,false);
		
		DaoUtil.setDefaultOrderBy(Student.class, preOrderby.value(), preOrderby.desc());
	}

	private void testStudentClassStudentCountOrderBy(Map<Class<? extends BaseDatabaseDomain>, DefaultOrderBy> defaultOrderBy,boolean desc) {
		DefaultOrderBy defaultOrderBy2 = null;
		DaoUtil.setDefaultOrderBy(Student.class, "clazz.studentCount", desc);
		defaultOrderBy2 = defaultOrderBy.get(Student.class);
		
		List<Student> students = DaoUtil.listAllObj(Student.class);
		Student last = null;
		for (Student temp:students) {
			if (last != null) {
				if (defaultOrderBy2.desc()) {
					assert(last.getClazz().getStudentCount().compareTo(temp.getClazz().getStudentCount()) >= 0);
				}else {
					assert(last.getClazz().getStudentCount().compareTo(temp.getClazz().getStudentCount()) <= 0);
				}
			}
			last = temp;
		}
	}
	private void testStudentIDOrderBy(Map<Class<? extends BaseDatabaseDomain>, DefaultOrderBy> defaultOrderBy,boolean desc) {
		DefaultOrderBy defaultOrderBy2 = null;
		DaoUtil.setDefaultOrderBy(Student.class, "id", desc);
		defaultOrderBy2 = defaultOrderBy.get(Student.class);
		
		List<Student> students = DaoUtil.listAllObj(Student.class);
		Student last = null;
		for (Student temp:students) {
			if (last != null) {
				if (defaultOrderBy2.desc()) {
					assert(last.getId().compareTo(temp.getId()) >= 0);
				}else {
					assert(last.getId().compareTo(temp.getId()) <= 0);
				}
			}
			last = temp;
		}
	}
	
	@Test
	public void testAnnotation() {
		Map<Class<? extends BaseDatabaseDomain>, DefaultOrderBy> defaultOrderBy = getDefaultOrderBy();
		DefaultOrderBy defaultOrderBy2 = defaultOrderBy.get(Student.class);
		assert(defaultOrderBy2 != null);
		List<Student> students = DaoUtil.listAllObj(Student.class);
		Student last = null;
		for (Student temp:students) {
			if (last != null) {
				if (defaultOrderBy2.desc()) {
					assert(last.getStudentNumber().compareTo(temp.getStudentNumber()) >= 0);
				}else {
					assert(last.getStudentNumber().compareTo(temp.getStudentNumber()) <= 0);
				}
			}
			last = temp;
		}
	}


	private Map<Class<? extends BaseDatabaseDomain>, DefaultOrderBy> getDefaultOrderBy() {
		try {
			Field field = DaoUtil.class.getDeclaredField("defaultOrderBy");
			field.setAccessible(true);
			return (Map<Class<? extends BaseDatabaseDomain>, DefaultOrderBy>) field.get(null);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException("反射获取defaultOrderBy失败",e);
		}
	}
}
