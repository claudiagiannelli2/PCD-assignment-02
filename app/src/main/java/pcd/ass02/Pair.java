package pcd.ass02;
public class Pair<X,Y> {
	
	private final X left;
	private final Y right;
	
	private Pair(X left, Y right) {
		super();
		this.left = left;
		this.right = right;
	}

	public static <X, Y> Pair<X,Y> of(X left, Y right) {
		return new Pair<>(left, right);
	}

	public X getLeft() {
		return left;
	}

	public Y getRight() {
		return right;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Pair [x=" + left + ", y=" + right + "]";
	}
	
	public static <X> Pair<X, X> swapped(Pair<X, X> from) {
		return Pair.of(from.right, from.left);
	}

}
