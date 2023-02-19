package steed.hibernatemaster.domain;

import steed.hibernatemaster.domain.BaseRelationalDatabaseDomain;
import steed.hibernatemaster.util.DaoUtil;

public abstract class SelfGenIDDomain extends BaseRelationalDatabaseDomain{
	public boolean trySave() {
		genId();
		if (DaoUtil.isResultNull(this)) {
			return save();
		}
		return false;
	}
	@Override
	public boolean save() {
		genId();
		return super.save();
    }
	public abstract void genId();
}
