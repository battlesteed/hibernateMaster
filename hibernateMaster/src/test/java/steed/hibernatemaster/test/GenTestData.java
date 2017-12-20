package steed.hibernatemaster.test;

import java.util.Date;

import steed.hibernatemaster.sample.domain.Clazz;
import steed.hibernatemaster.sample.domain.Student;
import steed.hibernatemaster.util.DaoUtil;
import steed.hibernatemaster.util.DaoUtil.ImmediatelyTransactionData;
import steed.util.test.TestEfficiency;

public class GenTestData {
	public final static int classSize = 100;
	public final static int classCount = 100;
	
	public void genData(){
		TestEfficiency testEfficiency = new TestEfficiency();
		testEfficiency.begin();
		ImmediatelyTransactionData immediatelyTransactionBegin = DaoUtil.immediatelyTransactionBegin();
		assert(DaoUtil.deleteByQuery(new Student()) != -1);
		assert(DaoUtil.deleteByQuery(new Clazz()) != -1);
		DaoUtil.managTransaction();
		for (int i = 0; i < classCount; i++) {
			Clazz clazz = new Clazz();
			clazz.setId((long) i);
			clazz.setName("class"+i);
			clazz.save();
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
		assert(DaoUtil.managTransaction());
		DaoUtil.immediatelyTransactionEnd(immediatelyTransactionBegin);
		testEfficiency.endAndOutUsedTime(String.format("插入%d个班级,每个班级%d学生,用时", classCount,classSize));
	}
}
