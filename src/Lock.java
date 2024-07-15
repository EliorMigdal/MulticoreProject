import java.util.List;

public interface Lock {
    void lock(List<Integer> indices);
    void unlock(List<Integer> indices);
}