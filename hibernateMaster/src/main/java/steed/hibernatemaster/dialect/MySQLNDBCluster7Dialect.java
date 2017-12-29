package steed.hibernatemaster.dialect;

public class MySQLNDBCluster7Dialect extends MySQLNDB7Dialect {
	private static final String ENGINE_NDBCluster = " ENGINE=NDBCLUSTER"; //$NON-NLS-1$

	@Override
	public String getTableTypeString() {
		return ENGINE_NDBCluster;
	}

	
}
