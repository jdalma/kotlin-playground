package map;

class CustomKey {
    private final int i;

    public CustomKey(int i) {
        this.i = i;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CustomKey customKey = (CustomKey) o;
        return i == customKey.i;
    }

    @Override
    public String toString() {
        return "map.CustomKey:" + i;
    }

    // 여기가 핵심
    @Override
    public int hashCode() {
        return i % 3;   // 0, 1, 2
    }
}
