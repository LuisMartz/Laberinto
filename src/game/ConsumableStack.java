package game;

class ConsumableStack {
    private final String name;
    private final int count;

    public ConsumableStack(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }
}

