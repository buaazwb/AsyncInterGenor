package cc.kaffa.rd.annotationprocessor;

public class InterfaceType {

	private String pkg;
	
	private String oriName;
	
	private String newName;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((newName == null) ? 0 : newName.hashCode());
		result = prime * result + ((oriName == null) ? 0 : oriName.hashCode());
		result = prime * result + ((pkg == null) ? 0 : pkg.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InterfaceType other = (InterfaceType) obj;
		if (newName == null) {
			if (other.newName != null)
				return false;
		} else if (!newName.equals(other.newName))
			return false;
		if (oriName == null) {
			if (other.oriName != null)
				return false;
		} else if (!oriName.equals(other.oriName))
			return false;
		if (pkg == null) {
			if (other.pkg != null)
				return false;
		} else if (!pkg.equals(other.pkg))
			return false;
		return true;
	}
	
}
