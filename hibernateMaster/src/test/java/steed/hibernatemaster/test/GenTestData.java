package steed.hibernatemaster.test;

import java.util.Date;

import org.junit.Test;

import steed.hibernatemaster.sample.domain.Clazz;
import steed.hibernatemaster.sample.domain.School;
import steed.hibernatemaster.sample.domain.Student;
import steed.hibernatemaster.util.DaoUtil;
import steed.hibernatemaster.util.DaoUtil.ImmediatelyTransactionData;
import steed.util.test.TestEfficiency;

public class GenTestData {
	public final static int classSize = 48;
	public final static int classCount = 10;
	public final static int schoolCount = 2;
	
	@Test
	public void genData(){
		TestEfficiency testEfficiency = new TestEfficiency();
		testEfficiency.begin();
		ImmediatelyTransactionData immediatelyTransactionBegin = DaoUtil.immediatelyTransactionBegin();
		assert(DaoUtil.deleteByQuery(new Student()) != -1);
		assert(DaoUtil.deleteByQuery(new Clazz()) != -1);
		assert(DaoUtil.deleteByQuery(new School()) != -1);
		DaoUtil.managTransaction();
		for (int a = 0; a < schoolCount; a++) {
			School school = new School();
			school.setName("school"+a);
			school.setChargeMan("chargeMan"+a);
			school.save();
			genClass(school);
		}
		assert(DaoUtil.managTransaction());
		DaoUtil.immediatelyTransactionEnd(immediatelyTransactionBegin);
		testEfficiency.endAndOutUsedTime(String.format("插入%d个学校,每个学校%d个班级,每个班级%d学生,用时", schoolCount,classCount,classSize));
	}

	private void genClass(School school) {
		for (int i = 0; i < classCount; i++) {
			Clazz clazz = new Clazz();
			clazz.setName("class"+i);
			clazz.setSchool(school);
			clazz.save();
			genStudent(i, clazz);
		}
	}

	private void genStudent(int i, Clazz clazz) {
		for (int j = 0; j < classSize; j++) {
			Student student = new Student();
			student.setClazz(clazz);
			student.setInDate(new Date());
			student.setName("student_"+i+"_"+j);
			student.setSex(j%2);
			student.setStudentNumber((i*100*100+j)+"");
			student.save();
		}
	}
}
