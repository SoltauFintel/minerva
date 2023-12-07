package minerva.task;

public enum TaskPriority {

    NORMAL(100),
    TOP(900),
    HIDE(-1);
    
    private final int p;
    
    TaskPriority(int p) {
        this.p = p;
    }
    
    public int prio() {
        return p;
    }
}
