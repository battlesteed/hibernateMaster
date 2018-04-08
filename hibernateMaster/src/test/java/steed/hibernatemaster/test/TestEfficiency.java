package steed.hibernatemaster.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import steed.hibernatemaster.Config;
import steed.hibernatemaster.sample.domain.Clazz;
import steed.hibernatemaster.sample.domain.Student;
import steed.hibernatemaster.util.DaoUtil;
import steed.util.base.BaseUtil;

public class TestEfficiency {
	private static AtomicInteger atomicInteger = new AtomicInteger(0);
	private static List<String> times  = new ArrayList<>();
	private static int count = 1000;
	
	public static void main(String[] args){
		Config.autoCommitTransaction = false;
		Config.devMode = true;
		DaoUtil.getSession();
		steed.util.test.TestEfficiency testEfficiency = new steed.util.test.TestEfficiency();
		testEfficiency.begin();
		for (int i = 0; i < count; i++) {
			test();
		}
		while (atomicInteger.get() < count) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for (String temp:times) {
			BaseUtil.out(temp);
		}
		testEfficiency.endAndOutUsedTime(atomicInteger.get()+"次查询总用时");
	}
	
	public static void test(){
		new Thread(new Runnable() {
			public void run() {
				steed.util.test.TestEfficiency testEfficiency = new steed.util.test.TestEfficiency();
				Student where = new Student();
				Clazz clazz = new Clazz();
				where.setClazz(clazz);
				clazz.setName("clazz"+new Random().nextInt(GenTestData.classCount));
				
				testEfficiency.begin();
				DaoUtil.listObj(10, new Random().nextInt(1000000), where, Arrays.asList("name"), null);
				times.add(testEfficiency.endAndOutUsedTime("用时"));
				DaoUtil.relese();
				DaoUtil.closeSessionNow();
				
				atomicInteger.incrementAndGet();
			}
		}).start();
	}
}
