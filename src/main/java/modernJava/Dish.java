package modernJava;

public record Dish(String name, boolean vegetarian, int calories, Type type) {
    public String getName() {
        return name;
    }
    public int getCalories() {
        return calories;
    }
    public Type getType() {
        return type;
    }
    public enum Type {MEAT, FISH, OTHER}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("modernJava.Dish{");
        sb.append("name='").append(name).append('\'');
        sb.append(", vegetarian=").append(vegetarian);
        sb.append(", calories=").append(calories);
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }
}
