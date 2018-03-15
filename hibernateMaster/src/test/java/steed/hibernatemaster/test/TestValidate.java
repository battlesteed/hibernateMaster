package steed.hibernatemaster.test;

import java.util.Set;

import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Assert;
import org.junit.Test;

import steed.hibernatemaster.sample.domain.Clazz;

public class TestValidate extends SteedTest{

	@Test
    public void testValidate() {
    	Configuration<?> configure = Validation.byDefaultProvider().configure();
    	configure.addProperty("hibernate.validator.fail_fast", "true");
    	Validator validator = configure.buildValidatorFactory().getValidator();
    	Set<ConstraintViolation<Clazz>> validate = validator.validate(new Clazz());
    	
    	assert(validate.size() == 1);
		
    }
	
	@Test
	public void testValidate2() {
		Configuration<?> configure = Validation.byDefaultProvider().configure();
		configure.addProperty("hibernate.validator.fail_fast", "false");
		Validator validator = configure.buildValidatorFactory().getValidator();
		Clazz clazz = new Clazz();
		Set<ConstraintViolation<Clazz>> validate = validator.validate(clazz);
		assert(validate.size() == 2);
		clazz.setStudentCount(2);
		validate = validator.validate(clazz);
		assert(validate.size() == 2);
	}
	
	@Test
	public void testValidate3() {
		try {
			new Clazz().save();
			Assert.fail();
		} catch (Exception e) {
//			assert("班级名字必填".equals(e.getMessage()));
		}
		
	}

}
