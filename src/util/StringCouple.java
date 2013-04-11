package util;

public class StringCouple {
	private final String organization1, organization2;
	
	public StringCouple(String org1, String org2) {
		organization1 = org1;
		organization2 = org2;
	}
	
	public String getOrganization1() {
		return organization1;
	}
	
	public String getOrganization2() {
		return organization2;
	}
	
	@Override
	public int hashCode() {
		int hash1 = organization1.hashCode();
		int hash2 = organization2.hashCode();
		
		if (hash1 < hash2) {
			return (hash1 * 31) ^ hash2;
		} else {
			return (hash2 * 31) ^ hash1;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof StringCouple)) return false;
		
		StringCouple other = (StringCouple)o;
		return (organization1.equals(other.organization1) && organization2.equals(other.organization2))
				|| (organization1.equals(other.organization2) && organization2.equals(other.organization1));
	}
}