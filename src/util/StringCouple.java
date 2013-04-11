package util;

public class StringCouple {
	private final String string1, string2;

	public StringCouple(String str1, String str2) {
		String string1 = str1 == null ? "" : str1.trim();
		String string2 = str2 == null ? "" : str2.trim();

		if (string1.hashCode() < string2.hashCode()) {
			this.string1 = string1;
			this.string2 = string2;
		} else {
			this.string2 = string1;
			this.string1 = string2;
		}
	}

	public String getString1() {
		return string1;
	}

	public String getString2() {
		return string2;
	}

	@Override
	public int hashCode() {
		int hash1 = string1.hashCode();
		int hash2 = string2.hashCode();

		return (hash1 * 31) ^ hash2;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o == null || !(o instanceof StringCouple))
			return false;

		StringCouple other = (StringCouple) o;
		return (string1.equals(other.string1) && string2.equals(other.string2));
	}

	public boolean sameStrings() {
		return string1.equals(string2);
	}

	@Override
	public String toString() {
		return String.format("StringCouple[one=\"%s\",two=\"%s\"]", string1, string2);
	}
}