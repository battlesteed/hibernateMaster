package steed.hibernatemaster.domain;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import steed.ext.util.base.BaseUtil;
import steed.ext.util.base.DomainUtil;

public abstract class BaseDatabaseDomain extends BaseDomain{
	private static final long serialVersionUID = -6872283825535497093L;

	
	
	public void initDefaultValue(){
		Map<Class<?>, Object> map = new HashMap<>();
		map.put(Integer.class, 0);
		map.put(Long.class, 0L);
		map.put(Date.class, new Date());
		map.put(Double.class, 0D);
		map.put(Float.class, 0F);
		initDefaultValue(map);
	}
	
	public void initDefaultValue(Map<Class<?>, Object> map){
		for (Field f:this.getClass().getDeclaredFields()) {
			Object value = map.get(f.getType());
			if (value != null) {
				f.setAccessible(true);
				try {
					if (BaseUtil.isObjEmpty(f.get(this))) {
						f.set(this, value);
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public abstract boolean update();
	public abstract boolean delete();
	public abstract boolean save();
	
	public abstract <T extends BaseDatabaseDomain> T smartLoad();
	public abstract <T extends BaseDatabaseDomain> T smartGet();
	/**
	 * 最终会调update或save方法,要做实体类切面编程,请重写update或save方法,无需重写该方法,该方法仅仅做判断调update还是save方法而已
	 * @return 是否保存或更新成功
	 */
	public abstract boolean saveOrUpdate();

	public abstract boolean updateNotNullField(List<String> updateEvenNull);
	
	public abstract boolean updateNotNullField(List<String> updateEvenNull,boolean strictlyMode);
	
	
	@Override
	public String toString() {
		Serializable id = DomainUtil.getDomainId(this);
		if (id == null) {
			return "";
		}
		return String.valueOf(id);
	}


	
}
