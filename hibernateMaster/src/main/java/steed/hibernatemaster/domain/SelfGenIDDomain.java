package steed.hibernatemaster.domain;

import steed.hibernatemaster.util.DaoUtil;

public abstract class SelfGenIDDomain extends BaseRelationalDatabaseDomain{
	public boolean saveIfNotExist() {
		genId();
		if (DaoUtil.isResultNull(this)) {
			return save();
		}
		return false;
	}
	@Override
	public boolean save() {
		return super.save();
    }
	public abstract void genId();
}
