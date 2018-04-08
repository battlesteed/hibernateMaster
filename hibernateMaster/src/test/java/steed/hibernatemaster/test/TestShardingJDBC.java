package steed.hibernatemaster.test;

import org.junit.Test;

import steed.hibernatemaster.sample.domain.School;
import steed.hibernatemaster.sample.domain.Student;
import steed.hibernatemaster.util.DaoUtil;
import steed.util.base.BaseUtil;

public class TestShardingJDBC extends SteedTest{
    
    @Test
    public void ttt(){
    	School school = new School();
    	school.setName("ddd");
    	school.save();
    	School listOne = DaoUtil.listOne(school);
    	BaseUtil.out(listOne);
    	BaseUtil.out(DaoUtil.listOne(new Student())+"");
    }
}
