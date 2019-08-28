package steed.hibernatemaster.test;

import steed.hibernatemaster.listener.CRUDListener;
import steed.hibernatemaster.sample.domain.School;
import steed.hibernatemaster.util.DaoUtil;

public class SchoolCRUDListener implements CRUDListener<School> {

	@Override
	public void beforSave(School domain) {
		domain.setName("beforSave");
	}

	@Override
	public void afterSave(School domain) {
		DaoUtil.managTransaction();
		domain = domain.smartGet();
		assert("beforSave".equals(domain.getName()));
		domain.delete();
	}
	
}
