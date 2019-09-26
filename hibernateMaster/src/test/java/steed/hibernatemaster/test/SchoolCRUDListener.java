package steed.hibernatemaster.test;

import steed.hibernatemaster.listener.CRUDListener;
import steed.hibernatemaster.sample.domain.School;
import steed.hibernatemaster.util.DaoUtil;

public class SchoolCRUDListener implements CRUDListener<School> {

	@Override
	public void beforSave(School domain) {
		if (CRUDListenerTest.CRUDListenerTestID.equals(domain.getId())) {
			domain.setName("beforSave");
		}
	}

	@Override
	public void afterSave(School domain) {
		if (CRUDListenerTest.CRUDListenerTestID.equals(domain.getId())) {
			DaoUtil.managTransaction();
			domain = domain.smartGet();
			assert("beforSave".equals(domain.getName()));
			domain.delete();
		}
	}
	
}
